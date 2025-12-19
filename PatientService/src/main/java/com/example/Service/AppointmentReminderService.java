package com.example.Service;

import com.example.Mapper.AppointmentReminderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 就诊提醒服务
 * 负责定时发送就诊提醒消息
 */
@Service
@Slf4j
public class AppointmentReminderService {

    @Autowired
    private AppointmentReminderMapper reminderMapper;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    /**
     * 发送1天后上午场次就诊提醒
     * 定时任务：每天8:00:00触发
     */
    @Transactional
    public void sendOneDayBeforeMorningReminder() {
        LocalDate targetDate = LocalDate.now().plusDays(1);
        log.info("开始发送1天后（{}）上午场次就诊提醒", targetDate);

        List<Map<String, Object>> appointments = reminderMapper.findOneDayBeforeMorningAppointments(targetDate);
        int count = sendReminders(appointments, "明天", targetDate);

        log.info("1天后上午场次就诊提醒发送完成，共发送{}条消息", count);
    }

    /**
     * 发送1天后下午场次就诊提醒
     * 定时任务：每天13:30:00触发
     */
    @Transactional
    public void sendOneDayBeforeAfternoonReminder() {
        LocalDate targetDate = LocalDate.now().plusDays(1);
        log.info("开始发送1天后（{}）下午场次就诊提醒", targetDate);

        List<Map<String, Object>> appointments = reminderMapper.findOneDayBeforeAfternoonAppointments(targetDate);
        int count = sendReminders(appointments, "明天", targetDate);

        log.info("1天后下午场次就诊提醒发送完成，共发送{}条消息", count);
    }

    /**
     * 发送3小时后上午场次就诊提醒
     * 定时任务：每天5:00:00触发
     */
    @Transactional
    public void sendThreeHoursBeforeMorningReminder() {
        LocalDate targetDate = LocalDate.now();
        log.info("开始发送3小时后（今天）上午场次就诊提醒");

        List<Map<String, Object>> appointments = reminderMapper.findThreeHoursBeforeMorningAppointments(targetDate);
        int count = sendReminders(appointments, "今天", targetDate);

        log.info("3小时后上午场次就诊提醒发送完成，共发送{}条消息", count);
    }

    /**
     * 发送3小时后下午场次就诊提醒
     * 定时任务：每天10:30:00触发
     */
    @Transactional
    public void sendThreeHoursBeforeAfternoonReminder() {
        LocalDate targetDate = LocalDate.now();
        log.info("开始发送3小时后（今天）下午场次就诊提醒");

        List<Map<String, Object>> appointments = reminderMapper.findThreeHoursBeforeAfternoonAppointments(targetDate);
        int count = sendReminders(appointments, "今天", targetDate);

        log.info("3小时后下午场次就诊提醒发送完成，共发送{}条消息", count);
    }

    /**
     * 发送提醒消息
     * @param appointments 预约信息列表
     * @param dayLabel 日期标签（"明天"或"今天"）
     * @param targetDate 就诊日期
     * @return 发送的消息数量
     */
    private int sendReminders(List<Map<String, Object>> appointments, String dayLabel, LocalDate targetDate) {
        int count = 0;
        LocalDateTime now = LocalDateTime.now();

        for (Map<String, Object> appointment : appointments) {
            try {
                String patientId = (String) appointment.get("patient_id");
                String patientName = (String) appointment.get("patient_name");
                String doctorName = (String) appointment.get("doctor_name");
                String departmentName = (String) appointment.get("department_name");
                String timePeriodName = (String) appointment.get("time_period_name");
                Object startTimeObj = appointment.get("start_time");
                String clinicNumber = (String) appointment.get("clinic_number");
                String clinicLocation = (String) appointment.get("clinic_location");

                // 格式化开始时间
                String startTimeStr = "";
                if (startTimeObj != null) {
                    startTimeStr = startTimeObj.toString();
                    if (startTimeStr.length() > 5) {
                        startTimeStr = startTimeStr.substring(0, 5);
                    }
                }

                // 构建消息标题
                String title = String.format("就诊提醒：%s %s就诊", dayLabel, timePeriodName);

                // 构建消息内容
                StringBuilder content = new StringBuilder();
                content.append(String.format("尊敬的%s患者：\n\n", patientName));
                content.append(String.format("您好！这是您的就诊提醒。\n\n"));
                content.append(String.format("【就诊时间】%s %s（%s）\n", 
                    targetDate.format(DATE_FORMATTER), startTimeStr, timePeriodName));
                content.append(String.format("【就诊医生】%s\n", doctorName));
                content.append(String.format("【就诊科室】%s\n", departmentName));
                
                if (clinicNumber != null) {
                    content.append(String.format("【诊室号码】%s\n", clinicNumber));
                }
                if (clinicLocation != null) {
                    content.append(String.format("【诊室位置】%s\n", clinicLocation));
                }
                
                content.append("\n请您提前15分钟到达候诊区等候，避免错过就诊时间。\n");
                content.append("\n如有疑问，请联系医院服务台。\n");
                content.append("\n祝您早日康复！\n");
                content.append("医院挂号系统");

                // 设置消息过期时间为就诊时间后1天
                LocalDateTime overTime = targetDate.plusDays(1).atStartOfDay();

                // 插入消息记录
                int inserted = reminderMapper.insertReminderMessage(
                    title,
                    content.toString(),
                    patientId,
                    now,
                    overTime
                );

                if (inserted > 0) {
                    count++;
                    log.debug("已为患者{}（ID:{}）发送{}就诊提醒", patientName, patientId, dayLabel);
                }
            } catch (Exception e) {
                log.error("发送就诊提醒失败", e);
            }
        }

        return count;
    }
}
