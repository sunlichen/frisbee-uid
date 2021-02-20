package com.sunlichen.frisbee;

import com.sunlichen.frisbee.service.FrisbeeService;
import com.sunlichen.frisbee.service.NtpTimeService;
import com.sunlichen.frisbee.service.SnowFlakeService;
import com.sunlichen.frisbee.utils.Uid;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author me@sunlichen.com
 */
@SpringBootTest(classes = {FrisbeeService.class, SnowFlakeService.class, NtpTimeService.class})
public class QueueUidTest {
    private static final Logger log =  org.slf4j.LoggerFactory.getLogger(QueueUidTest.class);

    private static final int SIZE = 10000000;
    private static final int THREADS = Runtime.getRuntime().availableProcessors() << 1;
    private static final boolean isDecodeUid = false;    //是否输出UID解码内容，更具可读性（可直接修改值）

    @Value("${frisbeeUid.startStamp}")
    private long startStamp ;               //系统初始运行配置的时间戳

    @Resource
    private FrisbeeService frisbeeService;

    /**
     * 将UID放入队列中等待后续测试用例使用
     */
    @BeforeEach
    public void putQueueUid(){
        frisbeeService.initFrisbeeService();
        Thread thread = new Thread(() -> {
            for (int i = 0; i < SIZE; i++) {
                frisbeeService.putUid();
            }
        },"putQueueUid");
        thread.start();
    }

    /**
     * 检查获取UID时，是否会重复
     */
    @Test
    public void testQueueUid() {
        long begin = System.currentTimeMillis();
        Set<Long> uidSet = new HashSet<>(SIZE);
        for (int i = 0; i < SIZE; i++) {
            doUidGenerate(uidSet);
        }

        checkUniqueID(uidSet);
        log.info("运行时长：{}毫秒",System.currentTimeMillis()-begin);
    }

    /**
     * 检查多线程获取UID时，是否会重复
     * @throws InterruptedException 中断异常
     */
    @Test
    public void testParallelQueueUid() throws InterruptedException {
        long begin = System.currentTimeMillis();
        AtomicInteger executeCount = new AtomicInteger(-1);
        Set<Long> uidSet = new ConcurrentSkipListSet<>();

        List<Thread> threadList = new ArrayList<>(THREADS);
        for (int i = 0; i < THREADS; i++) {
            Thread thread = new Thread(() -> putUidRun(uidSet,executeCount));
            thread.setName("UID-Thread-" + i);

            threadList.add(thread);
            thread.start();
        }

        for (Thread thread : threadList) {
            thread.join();
        }

        Assertions.assertEquals(SIZE, uidSet.size());

        checkUniqueID(uidSet);
        log.info("运行时长：{}毫秒",System.currentTimeMillis()-begin);
    }

    /**
     * 多线程添加UID处理器
     * @param uidSet UID去重存储器
     * @param executeCount 执行次数计数器
     */
    private void putUidRun(Set<Long> uidSet,AtomicInteger executeCount) {
        for (;;) {
            int cnt = executeCount.updateAndGet(old -> (old == SIZE ? SIZE : old + 1));
            if (cnt == SIZE) {
                return;
            }

            doUidGenerate(uidSet);
        }
    }

    /**
     * 获取UID放到SET中，并检查是否有重复
     * @param uidSet UID去重存储器
     */
    private void doUidGenerate(Set<Long> uidSet) {
        long uid = frisbeeService.getUid();
        boolean existed = !uidSet.add(uid);
        if (existed) {
            log.info("检查到重复的UID:{},{}" , uid, Uid.parseUID(uid,startStamp));
        }

        if ( isDecodeUid){
            log.info("UID:{}" , Uid.parseUID(uid,startStamp));
        }

        Assertions.assertTrue(uid > 0L);
    }

    /**
     * 检查获取UID数量是否和配置相等，如不等则为有重复值
     * @param uidSet UID去重存储器
     */
    private void checkUniqueID(Set<Long> uidSet) {
        log.info("实际获取UID数量：{}",uidSet.size());
        Assertions.assertEquals(SIZE, uidSet.size());
    }
}
