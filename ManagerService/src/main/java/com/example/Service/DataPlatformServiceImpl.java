package com.example.Service;

import com.example.Conmon.result.Result;
import com.example.Mapper.DataPlatformMapper;
import com.example.pojo.vo.HotDataPlatformVO;
import com.example.pojo.vo.StaticDataPlatformVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DataPlatformServiceImpl implements DataPlatformService {

    @Autowired
    private DataPlatformMapper dataPlatformMapper;

    @Override
    public Result<StaticDataPlatformVO> getStaticData() {
        StaticDataPlatformVO staticDataPlatformVO = new StaticDataPlatformVO();

        // 获取本周的预约挂号人数
        Integer weeklyRegistrationCount = dataPlatformMapper.getWeeklyRegistrationCount();
        staticDataPlatformVO.setRegis_count(weeklyRegistrationCount != null ? weeklyRegistrationCount : 0);

        // 获取本周的就诊人数
        Integer weeklyVisitCount = dataPlatformMapper.getWeeklyVisitCount();
        staticDataPlatformVO.setVisitor_count(weeklyVisitCount != null ? weeklyVisitCount : 0);

        // 获取科室就诊人数
        List<Map<String, Object>> departmentVisitData = dataPlatformMapper.getDepartmentVisitCount();
        List<StaticDataPlatformVO.DepVisitorCount> depVisitorCountList = departmentVisitData.stream()
                .map(map -> {
                    StaticDataPlatformVO.DepVisitorCount depVisitorCount = new StaticDataPlatformVO.DepVisitorCount();
                    depVisitorCount.setDep_name((String) map.get("dep_name"));
                    depVisitorCount.setCount((Long) map.get("count"));
                    return depVisitorCount;
                })
                .collect(Collectors.toList());
        staticDataPlatformVO.setDep_visitor_count(depVisitorCountList);

        // 获取医生就诊人数
        List<Map<String, Object>> doctorVisitData = dataPlatformMapper.getDoctorVisitCount();
        List<StaticDataPlatformVO.DocVisitorCount> docVisitorCountList = doctorVisitData.stream()
                .map(map -> {
                    StaticDataPlatformVO.DocVisitorCount docVisitorCount = new StaticDataPlatformVO.DocVisitorCount();
                    docVisitorCount.setDoc_name((String) map.get("name"));
                    docVisitorCount.setCount((Long) map.get("count"));
                    return docVisitorCount;
                })
                .collect(Collectors.toList());
        staticDataPlatformVO.setDoc_visitor_count(docVisitorCountList);

        // 获取医生排班量
        List<Map<String, Object>> doctorScheduleData = dataPlatformMapper.getDoctorScheduleCount();
        List<StaticDataPlatformVO.DocScheduleCount> docScheduleCountList = doctorScheduleData.stream()
                .map(map -> {
                    StaticDataPlatformVO.DocScheduleCount docScheduleCount = new StaticDataPlatformVO.DocScheduleCount();
                    docScheduleCount.setDoc_name((String) map.get("name"));
                    docScheduleCount.setCount((Long) map.get("count"));
                    return docScheduleCount;
                })
                .collect(Collectors.toList());
        staticDataPlatformVO.setDoc_schedule_count(docScheduleCountList);

        return Result.success(staticDataPlatformVO);
    }

    @Override
    public Result<HotDataPlatformVO> getHotData() {
        HotDataPlatformVO hotDataPlatformVO = new HotDataPlatformVO();

        // 获取今日挂号人数
        Integer todayRegisterCount = dataPlatformMapper.getTodayRegisterCount();
        hotDataPlatformVO.setRegister_num(todayRegisterCount != null ? todayRegisterCount : 0);

        // 获取今日已经就诊人数
        Integer todayVisitedCount = dataPlatformMapper.getTodayVisitedCount();
        hotDataPlatformVO.setVisited_num(todayVisitedCount != null ? todayVisitedCount : 0);

        // 获取今日正在排队的患者人数
        Integer todayLiningCount = dataPlatformMapper.getTodayLiningCount();
        hotDataPlatformVO.setLining_num(todayLiningCount != null ? todayLiningCount : 0);

        return Result.success(hotDataPlatformVO);
    }
}