package easyopt.model;

public class DHFSP_Instance {
    /**
     * 作业数量
     */
    int jobQty;
    /**
     * 阶段数
     */
    int proQty;

    /**
     * 工厂数量
     */
    int facQty;

    /**
     * 每个工厂的作业数
     */
    int[] facjobQty;


    /**
     * 作业加工时间
     */
    double[][] ptime;

    //----------------------------相关方法--------------------------------


    public int getJobQty() {
        return jobQty;
    }

    public void setJobQty(int jobQty) {
        this.jobQty = jobQty;
    }

    public int getProQty() {
        return proQty;
    }

    public void setProQty(int proQty) {
        this.proQty = proQty;
    }

    public int getFacQty() {
        return facQty;
    }

    public void setFacQty(int facQty) {
        this.facQty = facQty;
    }

    public double[][] getPtime() {
        return ptime;
    }

    public void setPtime(double[][] ptime) {
        this.ptime = ptime;
    }

    public int[] getFacjobQty() {
        return facjobQty;
    }

    public void setFacjobQty(int[] facjobQty) {
        this.facjobQty = facjobQty;
    }
}
