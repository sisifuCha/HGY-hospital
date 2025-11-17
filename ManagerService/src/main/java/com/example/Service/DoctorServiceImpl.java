package com.example.Service;

//import com.example.Mapper.DoctorMapper;
//import com.example.pojo.entity.Doctor;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Mapper.DepartmentMapper;
import com.example.Mapper.DoctorMapper;
import com.example.Conmon.result.Result;
import com.example.pojo.dto.DoctorDTO;
import com.example.pojo.dto.DoctorsRequestDTO;
import com.example.pojo.entity.Department;
import com.example.pojo.entity.Doctor;
import com.example.pojo.entity.DoctorSchedule;
import com.example.pojo.vo.ScheduleWeekVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.List;

@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private DoctorMapper DoctorMapper;
    @Autowired
    private DepartmentMapper DepartmentMapper;

    @Override
    //@Transactional(rollbackFor = Exception.class)
    public Result<String> updateDoctor(DoctorDTO doctorDTO) {
        try {
            // 1. 检查医生是否存在
            Doctor existingDoctor = DoctorMapper.selectById(doctorDTO.getUserId());
            if (existingDoctor == null) {
                return Result.fail(404, "医生信息不存在");
            }

            // 2. 检查账号名是否重复
            int count = DoctorMapper.checkAccountNameExists(doctorDTO.getUserAccount(),doctorDTO.getUserId());
            if (count > 0) {
                return Result.fail(400, "账号名已存在");
            }

            // 3. DTO转Entity
            Doctor doctor = convertToEntity(doctorDTO);

            // 4. 执行更新
            int result = DoctorMapper.updateDoctor(doctor);
            if (result > 0) {
                System.out.println("医生信息更新成功，ID: {}"+ doctorDTO.getUserId());
                return Result.success("医生信息更新成功", null);
            } else {
                return Result.fail(500, "医生信息更新失败");
            }

        } catch (Exception e) {
            System.out.println("更新医生信息失败: {}"+e.getMessage());
            throw new RuntimeException("系统异常，更新失败");
        }
    }

    @Override
    public Result<Doctor> getDoctorById(String id) {
        return Result.success(DoctorMapper.selectById(id));
    }

    @Override
    public Result<IPage<Doctor>> getDoctorListWithPlus(int page, int num, String filterName, String filterValue) {
        Page<Doctor> pageParam = new Page<>(page, num);

        // 使用MyBatis-Plus的查询条件
        QueryWrapper<Doctor> queryWrapper = new QueryWrapper<>();
        if (filterValue != null && !filterValue.isEmpty()) {
            if ("depart".equals(filterName)) {
                queryWrapper.eq("depart_id", filterValue);
            } else if ("title".equals(filterName)) {
                queryWrapper.eq("doc_title_id", filterValue);
            }
        }
        queryWrapper.orderByDesc("id");

        return Result.success(DoctorMapper.selectDoctorPage(pageParam, queryWrapper));
    }

    @Override
    public List<Department> getDepartmentOptions() {
        // 使用 MyBatis-Plus 查询 Department 表中的所有数据
        // (null) 表示没有查询条件
        return DepartmentMapper.selectList(null);
    }

    private Doctor convertToEntity(DoctorDTO dto) {
        Doctor doctor = new Doctor();
        BeanUtils.copyProperties(dto, doctor);
        return doctor;
    }

    @Override
    public Result<ScheduleWeekVO> getScheduleWeek(Integer week, String departName) {
        LocalDate currentDate = LocalDate.now();
        LocalDate monday;
        LocalDate sunday;

        //TODO 获取对应departId
        String departId = DepartmentMapper.getIdByName(departName);
        // 获取当前日期是星期几（1-7，1代表星期一，7代表星期日）
        int dayOfWeek = currentDate.getDayOfWeek().getValue();

        if (week == 0) {
            // 获取本周周一和周日的日期
            monday = currentDate.minusDays(dayOfWeek - 1); // 本周一
            sunday = monday.plusDays(6); // 本周日
            List<DoctorSchedule> doctorSchedules = DoctorMapper.selectDoctorSchedule(monday,sunday,departId);
            //处理VO
            return Result.success(getScheduleWeekVO(doctorSchedules));
        } else if (week == 1) {
            // 获取下周一到下周日的日期
            LocalDate nextMonday = currentDate.plusDays(8 - dayOfWeek); // 下周一
            monday = nextMonday;
            sunday = nextMonday.plusDays(6); // 下周日
            List<DoctorSchedule> doctorSchedules = DoctorMapper.selectDoctorSchedule(monday,sunday,departId);
            //处理VO
            return Result.success(getScheduleWeekVO(doctorSchedules));
        } else {
            return Result.fail("无效的周次");
        }

    }

    private ScheduleWeekVO getScheduleWeekVO(List<DoctorSchedule> doctorSchedules){
        ScheduleWeekVO res = new ScheduleWeekVO();
        for (DoctorSchedule doctorSchedule : doctorSchedules) {
            LocalDate date = doctorSchedule.getDate();
            switch (date.getDayOfWeek().getValue()) {
                case 1:
                    res.getMon().add(doctorSchedule);
                    break;
                case 2:
                    res.getTue().add(doctorSchedule);
                    break;
                case 3:
                    res.getWed().add(doctorSchedule);
                    break;
                case 4:
                    res.getThu().add(doctorSchedule);
                    break;
                case 5:
                    res.getFri().add(doctorSchedule);
                    break;
                case 6:
                    res.getSat().add(doctorSchedule);
                    break;
                case 7:
                    res.getSun().add(doctorSchedule);
                    break;
                default:
                    break;
            }
        }
        return res;
    }
}