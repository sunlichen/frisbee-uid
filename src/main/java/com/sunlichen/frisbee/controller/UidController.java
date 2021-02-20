package com.sunlichen.frisbee.controller;

import com.sunlichen.frisbee.service.FrisbeeService;
import com.sunlichen.frisbee.service.SnowFlakeService;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author me@sunlichen.com
 */
@RestController
public class UidController {

    @Autowired
    private FrisbeeService frisbeeService;
    @Autowired
    private SnowFlakeService snowFlakeService;

    /**
     * 从队列中获取已生成的UID
     * @return 从队列中获取的UID(此ID非实时生成，而是在队列中缓存的）
     */
    @RequestMapping(value="/queue_uid",produces = "application/json;charset=UTF-8")
    public long getQueueUid(){
        return frisbeeService.getUid();
    }

    /**
     * 获取实时生成的UID
     * @return 实时生成的UID
     */
    @RequestMapping(value="/current_uid",produces = "application/json;charset=UTF-8")
    public long getCurrentUid(){
        return snowFlakeService.nextId();
    }

    @RequestMapping(value="/status", produces = "application/json;charset=UTF-8")
    public String getStatus(){
        return String.format("{\"Date\":\"%s\",\"StartStamp\":\"%s\",\"datacenterId\":\"%d\",\"workerId\":\"%d\"," +
                        "\"SequenceModulo\":\"%d\",\"SectionNode\":\"%d\",\"TolerateTimeDiff\":\"%d\",\"count\":\"%d\"," +
                        "\"LastStamp\":\"%s\",\"QueueSize\":\"%d\"}",
                DateFormatUtils.format(snowFlakeService.getNewStamp(),"yyyy-MM-dd HH:mm:ss.SSS"),
                DateFormatUtils.format(snowFlakeService.getStartStamp(),"yyyy-MM-dd HH:mm:ss.SSS"),
                snowFlakeService.getDatacenterId(),
                snowFlakeService.getWorkerId(),
                snowFlakeService.getSequenceModulo(),
                snowFlakeService.getSectionNode(),
                snowFlakeService.getTolerateTimeDiff(),
                snowFlakeService.getCount(),
                DateFormatUtils.format(snowFlakeService.getLastStamp(),"yyyy-MM-dd HH:mm:ss.SSS"),
                frisbeeService.getQueueSize());


    }
}
