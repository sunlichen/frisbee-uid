package com.sunlichen.frisbee.service;

import com.sunlichen.frisbee.enums.SnowFlakeBits;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author me@sunlichen.com
 */
@Service
public class SnowFlakeService {
    private static final Logger log =  org.slf4j.LoggerFactory.getLogger(SnowFlakeService.class);

    @Autowired
    private NtpTimeService ntpTimeService;

    @Value("${frisbeeUid.startStamp}")
    private long startStamp ;               //系统初始运行配置的时间戳

    @Value("${frisbeeUid.workerId}")
    private long workerId;     //工作节点ID标识
    @Value("${frisbeeUid.datacenterId}")
    private long datacenterId;  //数据中心
    @Value("${frisbeeUid.tolerateTimeDiff:2000}")  //默认值2秒钟（2000毫秒）
    private long tolerateTimeDiff; // 前/后跳容忍范围（最新取得的时间和上一次的差值范围），小于此值正常处理，大于此值更新NTP时间差

    @Value("${frisbeeUid.sequenceModulo}")
    private long sequenceModulo;      //切片数量
    @Value("${frisbeeUid.sectionNode}")
    private long sectionNode;       //默认起始序号
    @Value("${frisbeeUid.sectionNode}")
    private long sequence;          //计算后序号（初始为默认起始序号）
    @Value("${frisbeeUid.sectionNode}")
    private long startPoint ; //同毫秒内第一个序号（起始点）

    private volatile long lastStamp = -1L;  //上一次获取sequence时间戳
    private volatile int count =0;          //计数器



    /**
     * 产生下一个ID
     * @return 根据时间戳+数据中心ID+工作节点ID+序号 生成的UID
     */
    public synchronized long nextId() {

        count = (count+1) & Integer.MAX_VALUE ;     //处理UID计数器，循环计数

        long currStamp = getNewStamp();
        //当前时间戳与上次执行时间戳差值大于配置 可允许差值 时，重新进行时间同步，获取最新的时间戳
        if (Math.abs( currStamp - lastStamp ) > tolerateTimeDiff) {
            log.info("本次时间戳与上次时间戳差值为：{} 毫秒，绝对值大于系统配置值 {} 毫秒，重新同步时间" ,currStamp - lastStamp,tolerateTimeDiff);
            ntpTimeService.updateNtpTimeOffset();
            currStamp = getNewStamp();
        }

        //序号根据配置 sequenceModulo 增加，起到分片的作用
        sequence = sequence + sequenceModulo;
        //序列数已经达到最大时，从新开始
        if ( sequence >= SnowFlakeBits.SEQUENCE.getMaxId() ) {
            sequence = sectionNode;
        }
        //时间相同的情况下，又循环到起始点，则等待到下一毫秒
        if ( currStamp == lastStamp && sequence ==startPoint ){
            currStamp = this.getNextMillisecond(currStamp);
        }else if (currStamp > lastStamp) {        //切换到下一毫秒时，保存当前序号起始点
            startPoint = sequence;
        }
        lastStamp = currStamp;

        return  ((currStamp - startStamp) << SnowFlakeBits.TIMESTAMP.getLeftShift())    //时间戳部分
                | (datacenterId << SnowFlakeBits.DATACENTER.getLeftShift())             //数据中心部分
                | (workerId << SnowFlakeBits.WORKER.getLeftShift())                     //工作节点标识部分
                | sequence ;                  //序列号部分
    }

    /**
     * 休眠等待下一毫秒
     * @param currStamp 待比较时间，如果不大于此时间则休眠等待
     * @return 最新的时间戳（已大于currStamp）
     */
    private long getNextMillisecond(long currStamp) {
        long millisecond = getNewStamp();
        while (millisecond <= currStamp) {
            try {
                log.debug("休眠等待 {} 毫秒后继续处理,已执行次数:{},时间差值:{}", (currStamp - millisecond + 1),count,ntpTimeService.offset);
                Thread.sleep(currStamp - millisecond + 1 );
            } catch (InterruptedException e) {
                log.error("休眠异常",e);
            }
            millisecond = getNewStamp();
        }
        return millisecond;
    }

    /**
     * 获取当前时间戳 （本地时间戳+NTP时间与本地时间差值）
     * 本地时间不准确的情况下通过时间差值修正，避免时间回调或前调等问题，使系统获取的时间戳是真正的NTP时间戳
     * 注意：如果NTP服务时间有问题，会导致生成UID有问题
     * @return 返回最新本地时间戳+时间差值，获得的基于NTP服务时间的时间戳
     */
    public long getNewStamp() {
        return System.currentTimeMillis() + ntpTimeService.offset;
    }

    public long getStartStamp() {
        return startStamp;
    }

    public long getWorkerId() {
        return workerId;
    }

    public long getDatacenterId() {
        return datacenterId;
    }

    public long getTolerateTimeDiff() {
        return tolerateTimeDiff;
    }

    public long getSequenceModulo() {
        return sequenceModulo;
    }

    public long getSectionNode() {
        return sectionNode;
    }

    public long getLastStamp() {
        return lastStamp;
    }

    public int getCount() {
        return count;
    }
}
