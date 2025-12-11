package com.example.Service;

import com.example.Mapper.RegistrationMapper;
import com.example.Mapper.WaitingMapper;
import com.example.conmon.exception.DuplicateRegistrationException;
import com.example.conmon.exception.SourceFullException;
import com.example.pojo.dto.WaitingDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class WaitingServiceImpl implements WaitingService {

    private static final int DAILY_WAITING_LIMIT = 3;
    private static final String WAITING_QUEUE_PREFIX = "waiting:queue:";
    private static final String WAITING_SET_PREFIX = "waiting:set:";
    private static final String WAITING_LIMIT_PREFIX = "waiting:limit:";
    private static final String WAITING_PATIENT_SCHEDULES_PREFIX = "waiting:patient_schedules:";

    @Autowired
    private WaitingMapper waitingMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public WaitingDto createWaiting(String patientId, String scheduleRecordId) {
        // 1. 校验排班是否存在
        Integer schExists = waitingMapper.countScheduleRecordById(scheduleRecordId);
        if (schExists == null || schExists == 0) {
            throw new IllegalArgumentException("排班记录不存在");
        }

        // 2. 校验号源是否已满 (只有满号才能候补)
        Integer left = waitingMapper.getScheduleLeftSource(scheduleRecordId);
        if (left != null && left > 0) {
            throw new SourceFullException();
        }

        // 3. 校验是否已有有效挂号
        Integer dupReg = registrationMapper.countActiveRegistrationByKey(patientId, scheduleRecordId);
        if (dupReg != null && dupReg > 0) {
            throw new DuplicateRegistrationException();
        }

        // 4. 校验是否已在候补队列 (Redis)
        String setKey = WAITING_SET_PREFIX + scheduleRecordId;
        Boolean isMember = redisTemplate.opsForSet().isMember(setKey, patientId);
        if (Boolean.TRUE.equals(isMember)) {
            throw new DuplicateRegistrationException();
        }

        // 5. 校验当日候补次数限制 (Redis)
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String limitKey = WAITING_LIMIT_PREFIX + patientId + ":" + today;
        Integer todayCount = (Integer) redisTemplate.opsForValue().get(limitKey);
        if (todayCount != null && todayCount >= DAILY_WAITING_LIMIT) {
            throw new IllegalArgumentException("已达到今日候补次数上限");
        }

        // 6. 构造 WaitingDto 并存入 Redis
        WaitingDto dto = new WaitingDto();
        dto.setPatientId(patientId);
        dto.setScheduleRecordId(scheduleRecordId);
        dto.setApplyTime(java.time.ZonedDateTime.now().format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        dto.setStatus("排队中");
        dto.setWaitingId(java.util.UUID.randomUUID().toString());

        String queueKey = WAITING_QUEUE_PREFIX + scheduleRecordId;
        String patientSchedulesKey = WAITING_PATIENT_SCHEDULES_PREFIX + patientId;

        // 执行 Redis 操作
        redisTemplate.opsForList().rightPush(queueKey, dto);
        redisTemplate.opsForSet().add(setKey, patientId);
        redisTemplate.opsForSet().add(patientSchedulesKey, scheduleRecordId);
        redisTemplate.opsForValue().increment(limitKey);
        redisTemplate.expire(limitKey, 1, TimeUnit.DAYS);

        // 7. 计算位置
        Long size = redisTemplate.opsForList().size(queueKey);
        dto.setPosition(size != null ? size.intValue() : 1);
        dto.setLimitCount(DAILY_WAITING_LIMIT - (todayCount != null ? todayCount : 0) - 1);

        return dto;
    }

    @Override
    public List<WaitingDto> getWaitingListBySchedule(String scheduleRecordId) {
        String queueKey = WAITING_QUEUE_PREFIX + scheduleRecordId;
        List<Object> list = redisTemplate.opsForList().range(queueKey, 0, -1);
        List<WaitingDto> result = new ArrayList<>();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                WaitingDto dto = (WaitingDto) list.get(i);
                dto.setPosition(i + 1);
                result.add(dto);
            }
        }
        return result;
    }

    @Override
    public List<WaitingDto> getWaitingListByPatient(String patientId, String date) {
        String patientSchedulesKey = WAITING_PATIENT_SCHEDULES_PREFIX + patientId;
        Set<Object> scheduleIds = redisTemplate.opsForSet().members(patientSchedulesKey);
        
        List<WaitingDto> result = new ArrayList<>();
        if (scheduleIds != null) {
            for (Object obj : scheduleIds) {
                String scheduleId = (String) obj;
                String queueKey = WAITING_QUEUE_PREFIX + scheduleId;
                List<Object> queue = redisTemplate.opsForList().range(queueKey, 0, -1);
                if (queue != null) {
                    for (int i = 0; i < queue.size(); i++) {
                        WaitingDto dto = (WaitingDto) queue.get(i);
                        if (patientId.equals(dto.getPatientId())) {
                            dto.setPosition(i + 1);
                            result.add(dto);
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public WaitingDto cancelWaiting(String patientId, String scheduleRecordId) {
        String queueKey = WAITING_QUEUE_PREFIX + scheduleRecordId;
        String setKey = WAITING_SET_PREFIX + scheduleRecordId;
        String patientSchedulesKey = WAITING_PATIENT_SCHEDULES_PREFIX + patientId;

        List<Object> list = redisTemplate.opsForList().range(queueKey, 0, -1);
        WaitingDto target = null;
        if (list != null) {
            for (Object obj : list) {
                WaitingDto dto = (WaitingDto) obj;
                if (patientId.equals(dto.getPatientId())) {
                    target = dto;
                    break;
                }
            }
        }

        if (target == null) {
            throw new IllegalArgumentException("未找到候补记录");
        }

        redisTemplate.opsForList().remove(queueKey, 1, target);
        redisTemplate.opsForSet().remove(setKey, patientId);
        redisTemplate.opsForSet().remove(patientSchedulesKey, scheduleRecordId);

        target.setStatus("已取消");
        return target;
    }

    @Override
    public WaitingDto confirmWaiting(String waitingId) {
        return null;
    }
}

