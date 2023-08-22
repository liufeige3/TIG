package easyopt.model;

/**
 * lfg
 */
public class Job {
    /**
     * 作业在所有作业中的编号，从0开始
     */
    int jobId;

    /**
     * 作业在每阶段的加工时间
     */
    double[] jobProTime;

    /**
     * 作业每个阶段的开始时间-该机器上上衣个作业的结束时间
     */
    double[] idleTime;

    /**
     * 作业的平均处理时间Ej
     */
    double Ej;

    /**
     * 当前作业在上一阶段的完成时间
     */
    double curendTime;

    /**
     * 在i阶段作业的开始加工时间
     */
    double[] jobProStarTime;

    /**
     * 在i阶段作业的加工完成时间
     */
    double[] jobProEndTime;

    /**
     * 在i阶段所在机器编号
     */
    int[] machNum;


    //------------------------相关方法--------------------------
    /**
     * 作业每个阶段的开始时间-该机器上上衣个作业的结束时间
     */
    public double[] getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(double[] idleTime) {
        this.idleTime = idleTime;
    }

    /**
     * 获取作业在所有作业中的编号
     * @return
     */
    public int getJobId() {
        return jobId;
    }

    /**
     * 设置作业在所有作业中的编号
     * @param jobId
     */
    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    /**
     * 作业在每阶段的加工时间
     * @return
     */
    public double[] getJobProTime() {
        return jobProTime;
    }

    /**
     * 作业在每阶段的加工时间
     * @param jobProTime
     */
    public void setJobProTime(double[] jobProTime) {
        this.jobProTime = jobProTime;
    }

    /**
     * 当前作业在上一阶段的完成时间
     * @return
     */
    public double getCurendTime() {
        return curendTime;
    }

    /**
     * 当前作业在上一阶段的完成时间
     * @param curendTime
     */
    public void setCurendTime(double curendTime) {
        this.curendTime = curendTime;
    }

    /**
     * 在i阶段作业的开始加工时间
     * @return
     */
    public double[] getJobProStarTime() {
        return jobProStarTime;
    }

    /**
     * 在i阶段作业的开始加工时间
     * @param jobProStarTime
     */
    public void setJobProStarTime(double[] jobProStarTime) {
        this.jobProStarTime = jobProStarTime;
    }

    /**
     * 在i阶段作业的加工完成时间
     * @return
     */
    public double[] getJobProEndTime() {
        return jobProEndTime;
    }

    /**
     * 在i阶段作业的加工完成时间
     * @param jobProEndTime
     */
    public void setJobProEndTime(double[] jobProEndTime) {
        this.jobProEndTime = jobProEndTime;
    }

    /**
     * 在i阶段所在机器编号
     * @return
     */
    public int[] getMachNum() {
        return machNum;
    }

    /**
     * 在i阶段所在机器编号
     * @param machNum
     */
    public void setMachNum(int[] machNum) {
        this.machNum = machNum;
    }

    /**
     * 作业的平均处理时间Ej
     * @return
     */
    public double getEj() {
        return Ej;
    }

    /**
     * 作业的平均处理时间Ej
     * @param ej
     */
    public void setEj(double ej) {
        Ej = ej;
    }
}

