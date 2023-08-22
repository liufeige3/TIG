package easyopt.model;

import java.util.ArrayList;
import java.util.List;

/**
 * lfg
 */
public class Factory {
    /**
     * 当前工厂编号
     */
    int facId;

    /**当前工厂的作业动态数组
     */
   List<Job> fjobSeq = new ArrayList<>();


    //当前工厂的Promachine
    int[][] proMachine;
    /**
     * 当前工厂的最大完工时间
     */
    double facCMax;

    //---------------------------相关方法----------------------------------

    /**
     * 获取当前工厂编号
     * @return
     */
    public int getFacId(int i) {
        return facId;
    }

    /**
     * 设置当前工厂编号
     * @param facId
     */
    public void setFacId(int facId) {
        this.facId = facId;
    }

    /**
     * 设置当前工厂的作业链表
     *
     * @return Job类的动态链表
     */
    public List<Job> getFjobSeq() {

        return fjobSeq;
    }

    /**
     * 获取当前工厂的作业链表
     * @param fjobSeq Job类的动态链表
     */

    public void setFjobSeq(ArrayList<Job> fjobSeq) {
        this.fjobSeq = fjobSeq;
    }


    //ProMachine
    public int[][] getProMachine() {
        return proMachine;
    }

    public void setProMachine(int[][] proMachine) {
        this.proMachine = proMachine;
    }

    /**
     * 获取当前工厂的最大完工时间
     * @return
     */
    public double getFacCMax() {
        return facCMax;
    }

    /**
     * 设置当前工厂的最大完工时间
     * @param facCMax
     */
    public void setFacCMax(double facCMax) {
        this.facCMax = facCMax;
    }
}
