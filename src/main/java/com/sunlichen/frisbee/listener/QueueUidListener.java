package com.sunlichen.frisbee.listener;

import com.sunlichen.frisbee.service.FrisbeeService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @author me@sunlichen.com
 */
@Component
public class QueueUidListener implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger log =  org.slf4j.LoggerFactory.getLogger(QueueUidListener.class);

    @Autowired
    private FrisbeeService frisbeeService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info("开始将UID放到待取队列中...");
        frisbeeService.initFrisbeeService();
        Thread thread = new Thread(() -> {
            for (;;) {
                frisbeeService.putUid();
            }
        },"putQueueUID");
        thread.start();
    }
}
