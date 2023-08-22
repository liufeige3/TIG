package easyopt.model;

import java.util.LinkedList;

public class Mach {
    /**
     * 机器的空闲时间段
     */
     LinkedList<double[]> idleTimeList;

    /**
     * 机器的空闲时间
     */
     double idleTime;

    /**
     * 机器的加工时间
     */
    double proTime;

    /**
     * 机器的阻塞时间
     */
    double blockTime;

    /**
     * 当前机器的允许开工时间
     */
    double reTime;
    //--------------------------------相关方法----------------------------


    public LinkedList<double[]> getIdleTimeList() {
        return idleTimeList;
    }

    public void setIdleTimeList(LinkedList<double[]> idleTimeList) {
        this.idleTimeList = idleTimeList;
    }

    public double getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(double idleTime) {
        this.idleTime = idleTime;
    }

    public double getProTime() {
        return proTime;
    }

    public void setProTime(double proTime) {
        this.proTime = proTime;
    }

    public double getBlockTime() {
        return blockTime;
    }

    public void setBlockTime(double blockTime) {
        this.blockTime = blockTime;
    }

    public double getReTime() {
        return reTime;
    }

    public void setReTime(double reTime) {
        this.reTime = reTime;
    }
}