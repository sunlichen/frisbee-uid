package com.sunlichen.frisbee.job;

import com.sunlichen.frisbee.service.NtpTimeService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author me@sunlichen.com
 */
@Component
@EnableScheduling
public class JobManager {
    private static final Logger log =  org.slf4j.LoggerFactory.getLogger(JobManager.class);

    @Autowired
    private NtpTimeService ntpTimeService;

    @Scheduled(cron = "${frisbeeUid.ntpSync.cron}")
    public void syncNtpDateTime(){
        long begin = System.currentTimeMillis();
        ntpTimeService.updateNtpTimeOffset();
        log.info("定时任务更新NTP服务时间差异完成,差异值：{},任务执行时长：{}",ntpTimeService.offset,System.currentTimeMillis()-begin);
    }
}
