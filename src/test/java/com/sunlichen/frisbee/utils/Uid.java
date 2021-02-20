package com.sunlichen.frisbee.utils;

import com.sunlichen.frisbee.enums.SnowFlakeBits;

import org.apache.commons.lang.time.DateFormatUtils;
import java.util.Date;

/**
 * @author me@sunlichen.com
 */
public class Uid {

    /**
     * 转换UID为可读内容
     * @param uid 待解码的UID
     * @return 解码后的UID，json串格式
     */
    public static String parseUID(long uid,long startStamp){

        long sequence = uid &  SnowFlakeBits.SEQUENCE.getMaxId();
        long workerId = uid >> SnowFlakeBits.SEQUENCE.getBits() & SnowFlakeBits.WORKER.getMaxId();
        long datacenterId = uid >> SnowFlakeBits.SEQUENCE.getBits()+SnowFlakeBits.WORKER.getBits() & SnowFlakeBits.DATACENTER.getMaxId();
        long timestamp = uid >> SnowFlakeBits.SEQUENCE.getBits()+SnowFlakeBits.WORKER.getBits()+
                SnowFlakeBits.DATACENTER.getBits() ;

        Date time = new Date(timestamp + startStamp);
        String strTime = DateFormatUtils.format(time,"yyyy-MM-dd HH:mm:ss.SSS");

        return String.format("{\"UID\":\"%d\",\"timestamp\":\"%s\",\"datacenterId\":\"%d\",\"workerId\":\"%d\",\"sequence\":\"%d\"}",
                uid, strTime,datacenterId, workerId, sequence);

    }

}
