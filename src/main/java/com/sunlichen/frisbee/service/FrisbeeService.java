package com.sunlichen.frisbee.service;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author me@sunlichen.com
 */
@Service
public class FrisbeeService {
    private static final Logger log =  org.slf4j.LoggerFactory.getLogger(FrisbeeService.class);

    @Autowired
    private SnowFlakeService snowFlakeService;
    private ArrayBlockingQueue<Long> arrayBlockingQueue = null;
    @Value("${frisbeeUid.queueSize}")
    private int queueSize;          //计算后序号（初始为默认起始序号）

    public void initFrisbeeService(){
        synchronized (this){
            if ( arrayBlockingQueue == null ){
                arrayBlockingQueue = new ArrayBlockingQueue<Long>(queueSize);
            }
        }
    }

    public void putUid(){
        try {
            arrayBlockingQueue.put(snowFlakeService.nextId());
        } catch (InterruptedException e) {
            log.error("将UID放入队列异常",e);
        }
    }

    public long getUid(){
        try {
            return arrayBlockingQueue.take() ;
        } catch (InterruptedException e) {
            log.error("取出队列中UID异常",e);
        }
        return -1;
    }

    public int getQueueSize(){
        return arrayBlockingQueue.size();
    }
}
