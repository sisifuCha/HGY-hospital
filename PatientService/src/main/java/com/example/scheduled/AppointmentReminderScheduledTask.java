package com.example.scheduled;

import com.example.Service.AppointmentReminderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 就诊提醒定时任务
 * 
 * 定时任务说明：
 * 1. 每天8:00:00  - 发送1天后上午场次（8:00开始）就诊提醒
 * 2. 每天13:30:00 - 发送1天后下午场次（13:30开始）就诊提醒
 * 3. 每天5:00:00  - 发送3小时后上午场次（8:00开始）就诊提醒
 * 4. 每天10:30:00 - 发送3小时后下午场次（13:30开始）就诊提醒
 */
@Component
@Slf4j
public class AppointmentReminderScheduledTask {

    @Autowired
    private AppointmentReminderService appointmentReminderService;

    /**
     * 每天8:00:00触发 - 发送1天后上午场次就诊提醒
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendOneDayBeforeMorningReminder() {
        log.info("【定时任务】触发1天后上午场次就诊提醒任务");
        try {
            appointmentReminderService.sendOneDayBeforeMorningReminder();
        } catch (Exception e) {
            log.error("【定时任务】1天后上午场次就诊提醒任务执行失败", e);
        }
    }

    /**
     * 每天13:30:00触发 - 发送1天后下午场次就诊提醒
     */
    @Scheduled(cron = "0 30 13 * * ?")
    public void sendOneDayBeforeAfternoonReminder() {
        log.info("【定时任务】触发1天后下午场次就诊提醒任务");
        try {
            appointmentReminderService.sendOneDayBeforeAfternoonReminder();
        } catch (Exception e) {
            log.error("【定时任务】1天后下午场次就诊提醒任务执行失败", e);
        }
    }

    /**
     * 每天5:00:00触发 - 发送3小时后上午场次就诊提醒
     */
    @Scheduled(cron = "0 0 5 * * ?")
    public void sendThreeHoursBeforeMorningReminder() {
        log.info("【定时任务】触发3小时后上午场次就诊提醒任务");
        try {
            appointmentReminderService.sendThreeHoursBeforeMorningReminder();
        } catch (Exception e) {
            log.error("【定时任务】3小时后上午场次就诊提醒任务执行失败", e);
        }
    }

    /**
     * 每天10:30:00触发 - 发送3小时后下午场次就诊提醒
     */
    @Scheduled(cron = "0 30 10 * * ?")
    public void sendThreeHoursBeforeAfternoonReminder() {
        log.info("【定时任务】触发3小时后下午场次就诊提醒任务");
        try {
            appointmentReminderService.sendThreeHoursBeforeAfternoonReminder();
        } catch (Exception e) {
            log.error("【定时任务】3小时后下午场次就诊提醒任务执行失败", e);
        }
    }
}
