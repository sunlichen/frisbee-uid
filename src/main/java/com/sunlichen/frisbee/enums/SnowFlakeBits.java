package com.sunlichen.frisbee.enums;

/**
 * @author me@sunlichen.com
 */
public enum SnowFlakeBits {

    //序列号占用的位数
    SEQUENCE(12,0),
    //机器标识占用的位数
    WORKER(5,SnowFlakeBits.SEQUENCE.leftShift+SnowFlakeBits.SEQUENCE.bits),
    //数据中心占用的位数
    DATACENTER(5,SnowFlakeBits.WORKER.leftShift+SnowFlakeBits.WORKER.bits),
    //时间戳占用的位数
    TIMESTAMP(41,SnowFlakeBits.DATACENTER.leftShift+SnowFlakeBits.DATACENTER.bits);

    private final long bits;              //每一部分占用的位数
    private final long maxId;             //每一部分的最大值
    private final long leftShift;         //每一部分向左的位移量

    SnowFlakeBits(long bits, long leftShift){
        this.bits= bits;
        this.maxId = ~(-1L << bits);
        this.leftShift = leftShift;
    }

    public long getBits() {
        return bits;
    }

    public long getMaxId() {
        return maxId;
    }

    public long getLeftShift() {
        return leftShift;
    }
}