import Problem.DHFSP;
import easyopt.commom.EasyMath;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class operator {

    /**SPT
     * 相对于NEH2 在每次确定一个作业的插入位置之后，随机选择该作业的前一个或后一个作业重新插入（在原工厂中）
     * @param jobSeq
     * @param protime
     * @param facJobQty
     * @return
     */
    public  static int[][] SPT(int[] jobSeq, double[][] protime, int[] facJobQty) {
        //1、求每个工序在每台机器上的加工时间总长，按递减顺序排列
        double jobQty = jobSeq.length;
        double proQty = protime.length;
        double[] jobProTime_toatal = new double[(int)jobQty];
        //求和
        for (int i = 0; i < jobQty; i++) {
            double jobProTimetatal = 0;
            for (int j = 0; j < proQty; j++) {
                jobProTimetatal+=protime[j][i];
            }
            jobProTime_toatal[i] = jobProTimetatal;
        }
        //排序返回升序的作业编号
        int[] jobSeq_ascend;
        jobSeq_ascend = EasyMath.sortArray(jobProTime_toatal);

        //result[0]: NEH2初始化后的作业加工顺序  result[1] :每个工厂中的作业数量
        int[][]  result = new int[2][];
        result[0] = jobSeq_ascend;
        result[1] = facJobQty;

        return result;
    }

    /**
     *
     * @param jobSeq
     * @param protime
     * @param facJobQty
     * @param a 作业加工时间与作业没阶段相邻时间之差的占比
     * @return
     */
    public  static int[][] NEH_des_block(int[] jobSeq, double[][] protime, int[] facJobQty,double a,int evaluate) {
        //1、求每个工序在每台机器上的加工时间总长，按递减顺序排列
        double jobQty = jobSeq.length;
        double proQty = protime.length;
        double facQty = facJobQty.length;
        double[] PB = new double[(int)jobQty];
        double[] jobProTime_toatal = new double[(int)jobQty];
        double[] jobProTime_diff = new double[(int)jobQty];

        //加工时间求和
        for (int i = 0; i < jobQty; i++) {
            double jobProTimetatal = 0;
            for (int j = 0; j < proQty; j++) {
                jobProTimetatal+=protime[j][i];
            }
            jobProTime_toatal[i] = jobProTimetatal;
        }
        //每阶段加工时间之差的和
        for (int i = 0; i < jobQty; i++) {
            double diff_pro = 0;
            for (int j = 1; j < proQty; j++) {
                diff_pro += Math.abs(protime[j][i]-protime[j-1][i]);
            }
            jobProTime_diff[i] = diff_pro;
            PB[i] = a*jobProTime_toatal[i] + (1-a)*jobProTime_diff[i];
        }

        //排序返回升序的机器编号
        int[] PB_ascend;
        PB_ascend = EasyMath.sortArray(PB);

        //2、取出第一步的后facQty个分别放入到每个工厂，后续的作业则依次放入到每个工厂的最小Makespan位置
        int[] jobQtyInfac = new int[(int)facQty];
        HashMap<Integer, LinkedList> jobSeqInfac= new HashMap<>();
        for (int i = 0; i < facQty; i++) {
            LinkedList<Integer> jobList = new LinkedList<Integer>();
            jobSeqInfac.put(i,jobList);
        }
        //取出第一步的后facQty个分别放入到每个工厂
        for (int i = 0; i < facQty; i++) {
            LinkedList jobList = jobSeqInfac.get(i);
            jobList.add(PB_ascend[(int)jobQty-1-i]);
            jobQtyInfac[i] += 1;
        }
        //后续的作业则依次放入到每个工厂的最小Makespan位置
        for (int i = (int)(jobQty-facQty-1); i >=0 ; i--) {
            int minfac = 0;
            int minposit = 0;
            double minresult =483648;
            //遍历工厂
            for (int j = 0; j < facQty; j++) {
                LinkedList jobList = jobSeqInfac.get(j);
                //遍历工厂的可插入位置
                for (int k = 0; k < jobList.size()+1; k++) {
                    jobList.add(k,PB_ascend[i]);
                    jobQtyInfac[j] += 1;
                    //获取完整的解
                    int[] jobSeq_temp = changeList(jobSeqInfac,jobQtyInfac);
                    double tempresult = DHFSP.evaluate_exper(jobSeq_temp,protime,jobQtyInfac)[evaluate];
                    if(tempresult<minresult){
                        minresult = tempresult;
                        minfac = j;
                        minposit = k;
                    }
                    jobQtyInfac[j] -= 1;
                    jobList.remove(k);
                }
            }

            //将其插入到最小工厂的最小位置
            LinkedList jobList = jobSeqInfac.get(minfac);
            jobList.add(minposit,PB_ascend[i]);
            jobQtyInfac[minfac] += 1;

        }
        //result[0]: NEH2初始化后的作业加工顺序  result[1] :每个工厂中的作业数量
        int[][]  result = new int[2][];
        int[] NEH_des_block = changeList(jobSeqInfac,jobQtyInfac);
        result[0] = NEH_des_block;
        result[1] = jobQtyInfac;

        return result;
    }

    /**NEH2_en
     * 相对于NEH2 在每次确定一个作业的插入位置之后，随机选择该作业的前一个或后一个作业重新插入每个工厂的每个位置
     * @param jobSeq
     * @param protime
     * @param facJobQty
     * @return
     */
    public  static int[][] NEH2_enb(int[] jobSeq, double[][] protime, int[] facJobQty,int evaluate,double a) {
        //1、求每个工序在每台机器上的加工时间总长，按递减顺序排列
        double jobQty = jobSeq.length;
        double proQty = protime.length;
        double[] jobProTime_toatal = new double[(int)jobQty];
        //求和
        for (int i = 0; i < jobQty; i++) {
            double jobProTimetatal = 0;
            for (int j = 0; j < proQty; j++) {
                jobProTimetatal+=protime[j][i];
            }
            jobProTime_toatal[i] = jobProTimetatal;
        }
        //排序返回升序的作业编号
        int[] jobSeq_ascend;
        jobSeq_ascend = EasyMath.sortArray(jobProTime_toatal);

        //2、取出第一步的后facQty个分别放入到每个工厂，后续的作业则依次放入到每个工厂的最小Makespan位置

        double facQty = facJobQty.length;
        int[] jobQtyInfac = new int[(int)facQty];

        HashMap<Integer, LinkedList> jobSeqInfac= new HashMap<>();
        for (int i = 0; i < facQty; i++) {
            LinkedList<Integer> jobList = new LinkedList<Integer>();
            jobSeqInfac.put(i,jobList);
        }
        //取出第一步的后facQty个分别放入到每个工厂
        for (int i = 0; i < facQty; i++) {
            LinkedList jobList = jobSeqInfac.get(i);
            jobList.add(jobSeq_ascend[(int)jobQty-1-i]);
            jobQtyInfac[i] += 1;
        }
        //后续的作业则依次放入到每个工厂的最小Makespan位置
        for (int i = (int)(jobQty-facQty-1); i >=0 ; i--) {
            int minfac = 0;
            int minposit = 0;
            double minresult =483648;
            //遍历工厂
            for (int j = 0; j < facQty; j++) {
                LinkedList jobList = jobSeqInfac.get(j);
                //遍历工厂的可插入位置
                for (int k = 0; k < jobList.size()+1; k++) {
                    jobList.add(k,jobSeq_ascend[i]);
                    jobQtyInfac[j] += 1;
                    //获取完整的解
                    int[] jobSeq_temp = changeList(jobSeqInfac,jobQtyInfac);
                    double tempresult = (1-a)*DHFSP.evaluate_exper(jobSeq_temp,protime,jobQtyInfac)[evaluate]+
                            a*DHFSP.evaluate_exper(jobSeq_temp,protime,jobQtyInfac)[4];

                    if(tempresult<minresult){
                        minresult = tempresult;
                        minfac = j;
                        minposit = k;
                    }
                    jobQtyInfac[j] -= 1;
                    jobList.remove(k);
                }
            }

            //将其插入到最小工厂的最小位置
            LinkedList jobList = jobSeqInfac.get(minfac);
            jobList.add(minposit,jobSeq_ascend[i]);
            jobQtyInfac[minfac] += 1;

            //随机选择该位置的前一个或者后一个位置的作业重新插入
            int r1 = random_1(2);
            int jobID_adjacent = 0;
            int adjacent = 0;
            if(r1==0&&minposit-1>-1){
                adjacent = minposit-1;
                LinkedList jobList_A = jobSeqInfac.get(minfac);
                jobID_adjacent = (int) jobList_A.get(adjacent);
                jobList_A.remove(adjacent);
                jobQtyInfac[minfac] -= 1;
            }else if(r1==0&&minposit-1==-1){
                adjacent = minposit+1;
                LinkedList jobList_A = jobSeqInfac.get(minfac);
                jobID_adjacent = (int) jobList_A.get(adjacent);
                jobList_A.remove(adjacent);
                jobQtyInfac[minfac] -= 1;
            }else if (r1==1&&minposit+1<jobSeqInfac.get(minfac).size()){
                adjacent = minposit+1;
                LinkedList jobList_A = jobSeqInfac.get(minfac);
                jobID_adjacent = (int) jobList_A.get(adjacent);
                jobList_A.remove(adjacent);
                jobQtyInfac[minfac] -= 1;
            }else if(r1==1&&minposit+1==jobSeqInfac.get(minfac).size()){
                adjacent = minposit-1;
                LinkedList jobList_A = jobSeqInfac.get(minfac);
                jobID_adjacent = (int) jobList_A.get(adjacent);
                jobList_A.remove(adjacent);
                jobQtyInfac[minfac] -= 1;
            }
            int minfac_e = minfac;
            int minposit_e = adjacent;
            double minresult_e =minresult;
            //遍历工厂
            for (int j = 0; j < facQty; j++) {
                LinkedList jobList_e = jobSeqInfac.get(j);
                //遍历工厂的可插入位置
                for (int k = 0; k < jobList_e.size()+1; k++) {
                    jobList_e.add(k,jobID_adjacent);
                    jobQtyInfac[j] += 1;
                    //获取完整的解
                    int[] jobSeq_temp = changeList(jobSeqInfac,jobQtyInfac);
                    double tempresult = (1-a)*DHFSP.evaluate_exper(jobSeq_temp,protime,jobQtyInfac)[evaluate]+
                            a*DHFSP.evaluate_exper(jobSeq_temp,protime,jobQtyInfac)[4];
                    if(tempresult<minresult_e){
                        minresult_e = tempresult;
                        minfac_e = j;
                        minposit_e = k;
                    }
                    jobQtyInfac[j] -= 1;
                    jobList_e.remove(k);
                }
            }

            //将其插入到最小工厂的最小位置
            LinkedList jobList_e = jobSeqInfac.get(minfac_e);
            jobList_e.add(minposit_e,jobID_adjacent);
            jobQtyInfac[minfac_e] += 1;



        }
        //result[0]: NEH2初始化后的作业加工顺序  result[1] :每个工厂中的作业数量
        int[][]  result = new int[2][];
        int[] jobSeq_NEH_en;
        jobSeq_NEH_en = changeList(jobSeqInfac,jobQtyInfac);

        result[0] = jobSeq_NEH_en;
        result[1] = jobQtyInfac;

        return result;
    }

    /**
     * NEH2初始化，针对于分布式的编码情况,降序
     * @param jobSeq 作业的加工顺序
     * @param protime 作业的每阶段的加工时间
     * @param facJobQty 每个工厂中作业的数量，初始的，后续可以改变
     * @return 作业的加工顺序以及每个工厂中加工的作业数量，
     * result[0]: NEH2初始化后的作业加工顺序  result[1] :每个工厂中的作业数量
     */
    public  static int[][] NEH2_des(int[] jobSeq, double[][] protime, int[] facJobQty,int evaluate) {
        //1、求每个工序在每台机器上的加工时间总长，按递减顺序排列
        double jobQty = jobSeq.length;
        double proQty = protime.length;
        double[] jobProTime_toatal = new double[(int)jobQty];
            //求和
        for (int i = 0; i < jobQty; i++) {
            double jobProTimetatal = 0;
            for (int j = 0; j < proQty; j++) {
                jobProTimetatal+=protime[j][i];
            }
            jobProTime_toatal[i] = jobProTimetatal;
        }
            //排序返回升序的机器编号
        int[] jobSeq_ascend;
        jobSeq_ascend = EasyMath.sortArray(jobProTime_toatal);

        //2、取出第一步的后facQty个分别放入到每个工厂，后续的作业则依次放入到每个工厂的最小Makespan位置
        double facQty = facJobQty.length;
        int[] jobQtyInfac = new int[(int)facQty];

        HashMap<Integer, LinkedList> jobSeqInfac= new HashMap<>();
        for (int i = 0; i < facQty; i++) {
            LinkedList<Integer> jobList = new LinkedList<Integer>();
            jobSeqInfac.put(i,jobList);
        }
            //取出第一步的后facQty个分别放入到每个工厂
        for (int i = 0; i < facQty; i++) {
            LinkedList jobList = jobSeqInfac.get(i);
            jobList.add(jobSeq_ascend[(int)jobQty-1-i]);
            jobQtyInfac[i] += 1;
        }
            //后续的作业则依次放入到每个工厂的最小Makespan位置
        for (int i = (int)(jobQty-facQty-1); i >=0 ; i--) {
            int minfac = 0;
            int minposit = 0;
            double minresult = 483648;
            //遍历工厂
            for (int j = 0; j < facQty; j++) {
                LinkedList jobList = jobSeqInfac.get(j);
                //遍历工厂的可插入位置
                for (int k = 0; k < jobList.size()+1; k++) {
                    jobList.add(k,jobSeq_ascend[i]);
                    jobQtyInfac[j] += 1;
                    //获取完整的解
                    int[] jobSeq_temp = changeList(jobSeqInfac,jobQtyInfac);
                    double tempresult = DHFSP.evaluate_exper(jobSeq_temp,protime,jobQtyInfac)[evaluate];

                    if(tempresult<minresult){
                        minresult = tempresult;
                        minfac = j;
                        minposit = k;
                    }
                    jobQtyInfac[j] -= 1;
                    jobList.remove(k);
                }
            }

            //将其插入到最小工厂的最小位置
            LinkedList jobList = jobSeqInfac.get(minfac);
            jobList.add(minposit,jobSeq_ascend[i]);
            jobQtyInfac[minfac] += 1;

        }

        //result[0]: NEH2初始化后的作业加工顺序  result[1] :每个工厂中的作业数量
        int[][]  result = new int[2][];
        int[] jobSeq_NEH = changeList(jobSeqInfac,jobQtyInfac);
        result[0] = jobSeq_NEH;
        result[1] = jobQtyInfac;

        return result;
    }

    /**
     * NEH2初始化，针对于分布式的编码情况，升序
     * @param jobSeq 作业的加工顺序
     * @param protime 作业的每阶段的加工时间
     * @param facJobQty 每个工厂中作业的数量，初始的，后续可以改变
     * @return 作业的加工顺序以及每个工厂中加工的作业数量，
     * result[0]: NEH2初始化后的作业加工顺序  result[1] :每个工厂中的作业数量
     */
    public  static int[][] NEH2_asc(int[] jobSeq, double[][] protime, int[] facJobQty) {
        //1、求每个工序在每台机器上的加工时间总长，按递减顺序排列
        double jobQty = jobSeq.length;
        double proQty = protime.length;
        double[] jobProTime_toatal = new double[(int)jobQty];
        //求和
        for (int i = 0; i < jobQty; i++) {
            double jobProTimetatal = 0;
            for (int j = 0; j < proQty; j++) {
                jobProTimetatal+=protime[j][i];
            }
            jobProTime_toatal[i] = jobProTimetatal;
        }
        //排序返回升序的机器编号
        int[] jobSeq_ascend;
        jobSeq_ascend = EasyMath.sortArray(jobProTime_toatal);
        for (int i = 0; i <  jobSeq_ascend.length; i++) {
            for (int j =  jobSeq_ascend.length - 1; j > i; j--) {
                int temp =  jobSeq_ascend[i];
                if ( jobSeq_ascend[i] <  jobSeq_ascend[j]) {
                    jobSeq_ascend[i] =  jobSeq_ascend[j];
                    jobSeq_ascend[j] = temp;
                }
            }
        }

        //2、取出第一步的后facQty个分别放入到每个工厂，后续的作业则依次放入到每个工厂的最小Makespan位置
        double facQty = facJobQty.length;
        int[] jobQtyInfac = new int[(int)facQty];

        HashMap<Integer, LinkedList> jobSeqInfac= new HashMap<>();
        for (int i = 0; i < facQty; i++) {
            LinkedList<Integer> jobList = new LinkedList<Integer>();
            jobSeqInfac.put(i,jobList);
        }
        //取出第一步的后facQty个分别放入到每个工厂
        for (int i = 0; i < facQty; i++) {
            LinkedList jobList = jobSeqInfac.get(i);
            jobList.add(jobSeq_ascend[(int)jobQty-1-i]);
            jobQtyInfac[i] += 1;
        }
        //后续的作业则依次放入到每个工厂的最小Makespan位置
        for (int i = (int)(jobQty-facQty-1); i >=0 ; i--) {
            int minfac = 0;
            int minposit = 0;
            double minresult =483648;
            //遍历工厂
            for (int j = 0; j < facQty; j++) {
                LinkedList jobList = jobSeqInfac.get(j);
                //遍历工厂的可插入位置
                for (int k = 0; k < jobList.size()+1; k++) {
                    jobList.add(k,jobSeq_ascend[i]);
                    jobQtyInfac[j] += 1;
                    //获取完整的解
                    int[] jobSeq_temp = changeList(jobSeqInfac,jobQtyInfac);
                    double tempresult = DHFSP.evaluate(jobSeq_temp,protime,jobQtyInfac);

                    if(tempresult<minresult){
                        minresult = tempresult;
                        minfac = j;
                        minposit = k;
                    }
                    jobQtyInfac[j] -= 1;
                    jobList.remove(k);
                }
            }

            //将其插入到最小工厂的最小位置
            LinkedList jobList = jobSeqInfac.get(minfac);
            jobList.add(minposit,jobSeq_ascend[i]);
            jobQtyInfac[minfac] += 1;

        }
        //result[0]: NEH2初始化后的作业加工顺序  result[1] :每个工厂中的作业数量
        int[][]  result = new int[2][];
        int[] jobSeq_NEH = new int[(int)jobQty];
        jobSeq_NEH = changeList(jobSeqInfac,jobQtyInfac);

        result[0] = jobSeq_NEH;
        result[1] = jobQtyInfac;

        return result;
    }

    public  static int[][] EHPF2(int[] jobSeq, double[][] protime, int[] facJobQty,double a,double b) {
        //1、求每个工序在每台机器上的加工时间总长，按递减顺序排列
        double jobQty = jobSeq.length;
        double proQty = protime.length;
        double facQty = facJobQty.length;
        double[] bicriteria_index = new double[(int)jobQty];
        double[] totalprotime = new double[(int)jobQty];
        double[] facLoad = new double[(int) facQty];
        double aveload = 0;
        //5
        int[][] proMachine = {{0,1}, {2,3}, {4,5}, {6,7}, {8,9}};
        //8
//            int[][] proMachine = {{0,1}, {2,3}, {4,5}, {6,7},{8,9},{10,11},{12,13},{14,15}};
        //10
//            int[][] proMachine = {{0,1}, {2,3}, {4,5}, {6,7}, {8,9},{10,11},{12,13},{14,15},{16,17},{18,19}};

        //求和
        for (int i = 0; i < jobQty; i++) {
            double jobProTimetatal = 0;
            for (int j = 0; j < proQty; j++) {
                jobProTimetatal+=(1-a)*protime[j][i]+2*a*(proQty-j)*protime[j][i]/(proQty-1);
                aveload+=protime[j][i]/facQty;
                totalprotime[i] +=protime[j][i];
            }
            bicriteria_index[i] = jobProTimetatal;
        }
        //排序返回升序的机器编号
        int[] asc_bicIdex=EasyMath.sortArray(bicriteria_index);
        //asc_bicIdex变成list
        LinkedList<Integer> asclist_bicIdex = new LinkedList<Integer>();
        for (int i = 0; i < jobQty; i++) {
            asclist_bicIdex.add(asc_bicIdex[i]);
        }

        //2、取出第一步的后facQty个分别放入到每个工厂，后续的作业则依次放入到每个工厂的最小Makespan位置
        int[] jobQtyInfac = new int[(int)facQty];
        HashMap<Integer, LinkedList> jobSeqInfac= new HashMap<>();
        for (int i = 0; i < facQty; i++) {
            LinkedList<Integer> jobList = new LinkedList<Integer>();
            jobSeqInfac.put(i,jobList);
        }

        //取出第一步的后facQty个分别放入到每个工厂
        for (int i = 0; i < facQty; i++) {
            LinkedList jobList = jobSeqInfac.get(i);
            int jobId = asclist_bicIdex.get(i);
            asclist_bicIdex.remove((Integer) jobId);
            jobList.add(jobId);
            facLoad[i] += totalprotime[jobId];
            jobQtyInfac[i] += 1;
        }
        //后续的作业则依次放入到每个工厂,依照cost measure
        int k=0;
        while (k<facQty&&asclist_bicIdex.size()>0){

            LinkedList jobList = jobSeqInfac.get(k);
            //选择作业
            double mincost = 483648;
            int minjob = asclist_bicIdex.get(0);
            for (int i = 0; i < asclist_bicIdex.size(); i++) {
                int jobId = asclist_bicIdex.get(i);
                jobList.add((Integer)jobId);
                double tempcost = DHFSP.evaluate_block(jobList,proMachine,protime,b)[2];
                if(mincost>tempcost){
                    mincost=tempcost;
                    minjob=jobId;
                }
                jobList.remove((Integer)jobId);
            }

            if(facLoad[k]+totalprotime[minjob]>aveload){
                if(facLoad[k]+totalprotime[minjob]-aveload>aveload-facLoad[k]){
                    k+=1;
                    continue;
                }
            }
            //add minjob
            asclist_bicIdex.remove((Integer) minjob);
            jobList.add(minjob);
            facLoad[k]+=totalprotime[minjob];
            jobQtyInfac[k] += 1;
        }

        for (int i = 0; i < facQty; i++) {
            LinkedList jobList = jobSeqInfac.get(i);
            LinkedList jobListnew = new LinkedList<>();

            while (jobList.size()>0){

                if(jobListnew.size()==0){
                    int job = (int) jobList.remove(0);
                    jobListnew.add(job);
                }else {
                    double minmakespan = 483648;
                    int minposit = 0;
                    int job = (int) jobList.remove(0);
                    for (int j = 0; j < jobListnew.size()+1; j++) {
                        jobListnew.add(j,job);
                        if(j==jobListnew.size()){
                            jobListnew.add(job);
                        }
                        double tempC = DHFSP.evaluate_block(jobListnew,proMachine,protime,b)[1];
                        if(tempC<minmakespan){
                            minmakespan=tempC;
                            minposit=j;
                        }
                        jobListnew.remove(j);
                    }
                    jobListnew.add(minposit,job);
                }
            }
            jobSeqInfac.put(i,jobListnew);
        }

        //result[0]: NEH2初始化后的作业加工顺序  result[1] :每个工厂中的作业数量
        int[][]  result = new int[2][];
        int[] jobSeq_NEH = changeList(jobSeqInfac,jobQtyInfac);
        result[0] = jobSeq_NEH;
        result[1] = jobQtyInfac;

        return result;
    }

    /**设PV = {p1, p2 1，⋯，p1 n, p2 n}为一个临时向量,然后对PV按递减顺序排序，通过将PV的每个位置上的元素转
     * 换为它所属的作业，生成序列π *。最后，将π *的作业有序插入到当前局部解的最佳位置,构造一个完整的调度。
     * 在重新插入作业之前，必须删除已经出现在当前部分序列中的作业。
     * @param jobSeq 作业编号排序
     * @param protime 作业的加工时间
     * @param facJobQty 每个工厂中作业的数量
     * @return NEH2EE初始化后的解
     */
    public  static int[] NEH2EE(int[] jobSeq, double[][] protime, int[] facJobQty){

        //计算每个作业的p1和p2
        //n1 0：作业编号 1：P1  2：P2
        int jobQty = jobSeq.length;
        double[][] n1 = new double[3][jobQty];
        for (int i = 0; i < jobQty; i++) {
            double p1 = 0;
            double p2 = 0;

            //获取job的每阶段加工时间
            double[] jobproTime = new double[protime.length];
            for (int j = 0; j < protime.length; j++) {
                jobproTime[j] = protime[j][jobSeq[i]];
            }

            for (int j = 0; j < jobproTime.length; j++) {
                p2 += jobproTime[j]/jobproTime.length;
                if(j >= 1){
                    p1 = Math.abs(jobproTime[j] - jobproTime[j-1])/jobproTime.length+ p1;
                }
            }
            n1[0][i] = jobSeq[i];
            n1[1][i] = p1 + p2;
            n1[2][i] = p2;
        }

        //PV表,合并P1，P2对应的作业编号,P1:0至jobQty-1，P2：jobQty至2*jobQty-1
        int n = jobQty * 2;
        double[] PV = new double[n];
        int[] pv;
        for (int i = 0; i < n; i++) {
            if(i<jobQty){
                PV[i] = n1[1][i];
            }
            else PV[i] = n1[2][i%jobQty];
        }

        //将PV升序排序，排序后对应的作业编号存入pv
        EasyMath easyMath = new EasyMath();
        pv = easyMath.sortArray(PV);
        for (int i = 0; i < n; i++) {
            if(pv[i] >= jobQty){
                pv[i] = pv[i] % jobQty;
            }
        }

//        算法主循环
//        将jobSeq复制为动态的jobList
        LinkedList<Integer> jobList = new LinkedList<Integer>();
        for (int i = 0; i < jobSeq.length; i++) {
            jobList.add(jobSeq[i]);
        }

        //按照pv表迭代
        for (int i = 0; i < pv.length; i++) {
            int selectjob = pv[i];
            double minCmax =DHFSP.evaluate(jobSeq,protime,facJobQty);
            int minpos = 0;

            //在joblist中删除selectjob
            for (int j = 0; j < jobList.size(); j++) {
                if(selectjob == jobList.get(j)){
                    jobList.remove(j);
                    break;
                }
            }

            //找出使Cmax最小的插入位置
            for (int j = 0; j < jobQty; j++) {
                jobList.add(j,selectjob);

                int[] jobList_l = new int[jobQty];
                for (int k = 0; k < jobList_l.length; k++) {
                    jobList_l[k] = jobList.get(k);
                }

                double Cmax = DHFSP.evaluate(jobList_l,protime,facJobQty);
                if (Cmax < minCmax){
                    minpos = j;
                    minCmax = Cmax;
                }
                jobList.remove(j);
            }

            //将selectjob插入最小位置
            jobList.add(minpos,selectjob);

        }
        int[] jobSeq_new = new int[jobQty];
        for (int j = 0; j < jobList.size(); j++) {
            jobSeq_new[j] = jobList.get(j);
        }
        int[] jobSeq_NEHEE = jobSeq_new;
        System.out.println("jobSeq_NEHEE = " + DHFSP.evaluate(jobSeq_NEHEE,protime,facJobQty) );
        System.out.println("jobSeq = " + DHFSP.evaluate(jobSeq,protime,facJobQty) );
        return jobSeq_NEHEE;

    }

    /**
     * 将关键工厂中的作业逐一再次插入
     * @param jobSeq 作业的加工顺序
     * @param protime 作业的每阶段的加工时间
     * @param facJobQty 每个工厂中作业的数量，初始的，后续可以改变
     * @return 交换后的解
     */
    public  static int[][] Insert_vision_based(int[] jobSeq, double[][] protime, int[] facJobQty) {
        int facQty = facJobQty.length;
        int[] facJobQty_t = facJobQty.clone();
        //将关键工厂中的作业逐一再次插入
        //1、找出关键工厂
        int criticalFac = (int) DHFSP.evaluate_exper(jobSeq,protime,facJobQty_t)[3];
        //每个工厂的作业范围
        int[] facJobQty_range = new int[facJobQty_t.length];
        for (int i = 0; i < facJobQty_range.length; i++) {
            if(i==0){
                facJobQty_range[i]= facJobQty_t[i];
            }else
                facJobQty_range[i] =facJobQty_range[i-1] + facJobQty_t[i];
        }

        //将数组变为链表
        //Map<工厂编号，工厂中的作业加工顺序>
        HashMap<Integer, LinkedList> jobSeqInfac= new HashMap<>();
        for (int i = 0; i < facQty; i++) {
            LinkedList<Integer> jobList = new LinkedList<Integer>();
            jobSeqInfac.put(i,jobList);
        }
        //将原来的解数组变为List
        for (int i = 0; i <facJobQty_range.length ; i++) {
            LinkedList<Integer> jobList_i = jobSeqInfac.get(i);
            int j;
            if(i==0){j = 0;}
            else {j = facJobQty_range[i-1];}
            while (j < facJobQty_range[i]){
                jobList_i.add(jobSeq[j]);
                j++;
            }
        }

        double minmakespan = DHFSP.evaluate_exper(jobSeq,protime,facJobQty_t)[0];
//        double minBI = DHFSP.evaluate_exper(jobSeq,protime,facJobQty)[4];
//        double minMBI = b*minmakespan+(1-b)*minBI;

        //将关键工厂的每个位置删除然后重新插入每个工厂的每个位置1
            for (int i = 0; i < facJobQty_t[criticalFac]; i++) {
                int job = (int) jobSeqInfac.get(criticalFac).get(i);
                int minfacId = criticalFac;
                int minposi = i;
                //删
                jobSeqInfac.get(criticalFac).remove((Integer)job);
                facJobQty_t[criticalFac] -=1;
                for (int j = 0; j < jobSeqInfac.size(); j++) {
                    LinkedList<Integer> jobList_fi = jobSeqInfac.get(j);
                    facJobQty_t[j] +=1;
                    for (int k = 0; k < jobList_fi.size()+1; k++) {
                        jobList_fi.add(k,job);
                        int[] jobSeqt = changeList(jobSeqInfac,facJobQty_t);
                        double tempresult = DHFSP.evaluate_exper(jobSeqt,protime,facJobQty_t)[0];
//                    double tempBI = DHFSP.evaluate_exper(jobSeqt,protime,facJobQty)[4];
//                    double tempMBI = b*tempresult+(1-b)*tempBI;
                        if(tempresult<minmakespan){
                            minmakespan = tempresult;
                            minfacId = j;
                            minposi = k;
                        }
//                    if(tempMBI<minMBI){
//                        minMBI = tempMBI;
//                        minfacId = j;
//                        minposi = k;
//                    }
                        jobList_fi.remove(k);
                    }
                    facJobQty_t[j] -=1;
                }
                //增
                jobSeqInfac.get(minfacId).add(minposi,(Integer)job);
                facJobQty_t[minfacId] +=1;
            }


        int[][]  result = new int[2][];
        result[0] = changeList(jobSeqInfac,facJobQty_t);
        result[1] = facJobQty_t;
        double n111 = DHFSP.evaluate_exper(result[0],protime,result[1])[0];

        return result;
    }

    /**
     * 随机选择一个fac，找出关键工厂，每个fac中随机选一个作业交换
     *A collaborative iterative greedy algorithm for the scheduling of distributed
     * heterogeneous hybrid flow shop with blocking constraints
     * @param jobSeq 作业的加工顺序
     * @param protime 作业的每阶段的加工时间
     * @param facJobQty 每个工厂中作业的数量，初始的，后续可以改变
     * @return 交换后的解
     */
    public  static int[][] Random_critical_fac(int[] jobSeq, double[][] protime, int[] facJobQty) {

        double jobQty = jobSeq.length;
        double proQty = protime.length;

        //1、找出关键工厂
        int criticalFac = (int) DHFSP.evaluate_exper(jobSeq,protime,facJobQty)[3];
        //随机一个非关键工厂
        int randomFac = criticalFac ;
        while (randomFac==criticalFac){
            randomFac = random_1(facJobQty.length);
        }

        int[] jobSeq_temp = jobSeq.clone();

        int n = 0;
        while (n<jobQty*jobQty){
            //2、分别在两个工厂中随机选中两个作业(位置)
            //每个工厂的作业范围
            n++;
            int[] facJobQty_range = new int[facJobQty.length];
            for (int i = 0; i < facJobQty_range.length; i++) {
                if(i==0){
                    facJobQty_range[i]= facJobQty[i];
                }else
                    facJobQty_range[i] =facJobQty_range[i-1] + facJobQty[i];
            }

            int randomJob_0,randomJob_1;
            int max_0 = facJobQty_range[criticalFac] - 1;
            int min_0 = facJobQty_range[criticalFac] - facJobQty[criticalFac];
            randomJob_0 = (int) (Math.random() * (max_0 - min_0 + 1) + min_0);
            int max_1 = facJobQty_range[randomFac] - 1;
            int min_1 = facJobQty_range[randomFac] - facJobQty[randomFac];
            randomJob_1 = (int) (Math.random() * (max_1 - min_1 + 1) + min_1);

            //3、将两个位置的作业交换
            int[] jobSeq_change = jobSeq_temp.clone();
            int temp;
            temp = jobSeq_change[randomJob_0];
            jobSeq_change[randomJob_0] = jobSeq_change[randomJob_1];
            jobSeq_change[randomJob_1] = temp;

            //比较交换前后的makespan
            double makespan_init = DHFSP.evaluate(jobSeq_temp,protime,facJobQty);
            double makespan_change = DHFSP.evaluate(jobSeq_change,protime,facJobQty);
            if(makespan_init>makespan_change){
                jobSeq_temp = jobSeq_change;

            }
//            System.out.println("makespan " + makespan_init);

        }

        int[][]  result = new int[2][];
        result[0] = jobSeq_temp;
        result[1] = facJobQty;
        return result;
    }

    /**
     * 随机选择两个工厂，每个fac中随机选一个作业交换
     *A collaborative iterative greedy algorithm for the scheduling of distributed
     * heterogeneous hybrid flow shop with blocking constraints
     * @param jobSeq 作业的加工顺序
     * @param protime 作业的每阶段的加工时间
     * @param facJobQty 每个工厂中作业的数量，初始的，后续可以改变
     * @return 交换后的解
     */
    public  static int[][] Random_discretionary (int[] jobSeq, double[][] protime, int[] facJobQty) {

        double jobQty = jobSeq.length;
        double proQty = protime.length;

        //1、随机两个工厂
        int[] facId = new int[2];
        facId = random(2,facJobQty.length);
        int randomFac_0,randomFac_1;
        randomFac_0 = facId[0];
        randomFac_1 = facId[1];

        int[] jobSeq_temp = jobSeq.clone();
        int n = 0;
        while (n<jobQty*jobQty){
            //2、分别在两个工厂中随机选中两个作业(位置)
            //每个工厂的作业范围
            n++;
            int[] facJobQty_range = new int[facJobQty.length];
            for (int i = 0; i < facJobQty_range.length; i++) {
                if(i==0){
                    facJobQty_range[i]= facJobQty[i];
                }else
                    facJobQty_range[i] =facJobQty_range[i-1] + facJobQty[i];
            }

            int randomJob_0,randomJob_1;
            int max_0 = facJobQty_range[randomFac_0] - 1;
            int min_0 = facJobQty_range[randomFac_0] - facJobQty[randomFac_0];
            randomJob_0 = random_1(max_0-min_0) + min_0;
            int max_1 = facJobQty_range[randomFac_1] - 1;
            int min_1 = facJobQty_range[randomFac_1] - facJobQty[randomFac_1];
            randomJob_1 = random_1(max_1-min_1) + min_1;

            //3、将两个位置的作业交换
            int[] jobSeq_change = jobSeq_temp.clone();
            int temp;
            temp = jobSeq_change[randomJob_0];
            jobSeq_change[randomJob_0] = jobSeq_change[randomJob_1];
            jobSeq_change[randomJob_1] = temp;

            //比较交换前后的makespan
            double makespan_init = DHFSP.evaluate(jobSeq_temp,protime,facJobQty);
            double makespan_change = DHFSP.evaluate(jobSeq_change,protime,facJobQty);
            if(makespan_init>makespan_change){
                jobSeq_temp = jobSeq_change;
            }
//            System.out.println("makespan " + makespan_init);

        }

        int[][]  result = new int[2][];
        result[0] = jobSeq_temp;
        result[1] = facJobQty;
        return result;
    }

    /**
     * 每个工厂随机选择两个作业交换
     *A collaborative iterative greedy algorithm for the scheduling of distributed
     * heterogeneous hybrid flow shop with blocking constraints
     * @param jobSeq 作业的加工顺序
     * @param protime 作业的每阶段的加工时间
     * @param facJobQty 每个工厂中作业的数量，初始的，后续可以改变
     * @return 交换后的解
     */
    public  static int[][] Random_swap (int[] jobSeq, double[][] protime, int[] facJobQty) {

        double jobQty = jobSeq.length;
        double facQty = facJobQty.length;

        //1、each工厂
        int[] jobSeq_temp = jobSeq.clone();
        for (int i = 0; i < facQty; i++) {
            for (int j = 0; j < jobQty*jobQty; j++) {

                int[] facJobQty_range = new int[facJobQty.length];
                for (int k = 0; k < facJobQty_range.length; k++) {
                    if(k==0){
                        facJobQty_range[k]= facJobQty[k];
                    }else
                        facJobQty_range[k] =facJobQty_range[k-1] + facJobQty[k];
                }

                //在工厂中随机两个作业
                int randomJob_0,randomJob_1;
                int max_0 = facJobQty_range[i] - 1;
                int min_0 = facJobQty_range[i] - facJobQty[i];
                randomJob_0 = (int) (Math.random() * (max_0 - min_0 + 1) + min_0);
                do {
                    int max_1 = facJobQty_range[i] - 1;
                    int min_1 = facJobQty_range[i] - facJobQty[i];
                    randomJob_1 = (int) (Math.random() * (max_1 - min_1 + 1) + min_1);
                }while (randomJob_0 == randomJob_1);

                //3、将两个位置的作业交换
                int[] jobSeq_change = jobSeq_temp.clone();
                int temp;
                temp = jobSeq_change[randomJob_0];
                jobSeq_change[randomJob_0] = jobSeq_change[randomJob_1];
                jobSeq_change[randomJob_1] = temp;

                //比较交换前后的makespan
//                double makespan_init = DHFSP.evaluate(jobSeq_temp,protime,facJobQty);
//                double makespan_change = DHFSP.evaluate(jobSeq_change,protime,facJobQty);
                double makespan_init = DHFSP.evaluate_exper(jobSeq_temp,protime,facJobQty)[1];
                double makespan_change = DHFSP.evaluate_exper(jobSeq_change,protime,facJobQty)[1];
                if(makespan_init>makespan_change){
                    jobSeq_temp = jobSeq_change;
                }
//                System.out.println("makespan " + makespan_init);

            }
        }

        int[][]  result = new int[2][];
        result[0] = jobSeq_temp;
        result[1] = facJobQty;
        return result;
    }

    /**
     * 每个作业与所在工厂的其他作业交换
     *A collaborative iterative greedy algorithm for the scheduling of distributed
     * heterogeneous hybrid flow shop with blocking constraints
     * @param jobSeq 作业的加工顺序,传入Random_swap后的作业顺序
     * @param protime 作业的每阶段的加工时间
     * @param facJobQty 每个工厂中作业的数量，初始的，后续可以改变
     * @return 交换后的解
     */
    public  static int[][] Sequential_swap (int[] jobSeq, double[][] protime, int[] facJobQty,int evaluate) {

        double jobQty = jobSeq.length;
        double facQty = facJobQty.length;

        int[] facJobQty_range = new int[facJobQty.length];
        for (int k = 0; k < facJobQty_range.length; k++) {
            if(k==0){
                facJobQty_range[k]= facJobQty[k];
            }else
                facJobQty_range[k] =facJobQty_range[k-1] + facJobQty[k];
        }
        //1、each工厂
        int[] jobSeq_temp = jobSeq.clone();
        for (int i = 0; i < facQty; i++) {
            int end = facJobQty_range[i] - 1;
            int begin = facJobQty_range[i] - facJobQty[i];
            for (int j = begin; j <=end; j++) {
                for (int k = begin; k <=end; k++) {
                    int[] jobSeq_temp1 = jobSeq_temp.clone();
                    int temp;
                    temp = jobSeq_temp1[j];
                    jobSeq_temp1[j] = jobSeq_temp1[k];
                    jobSeq_temp1[k] = temp;

//                    double makespan_init = DHFSP.evaluate(jobSeq_temp,protime,facJobQty);
//                    double makespan_change = DHFSP.evaluate(jobSeq_temp1,protime,facJobQty);
                    double makespan_init = DHFSP.evaluate_exper(jobSeq_temp,protime,facJobQty)[evaluate];
                    double makespan_change = DHFSP.evaluate_exper(jobSeq_temp1,protime,facJobQty)[evaluate];
                    if(makespan_init>makespan_change){
                        jobSeq_temp = jobSeq_temp1;
                    }
                }
            }
        }

        int[][]  result = new int[2][];
        result[0] = jobSeq_temp;
        result[1] = facJobQty;
        return result;
    }

    /**
     * 基于半交换算子的全局扰动，每个工厂内的作业分半，对于makespan较大的一部分进行贪婪插入
     *A collaborative iterative greedy algorithm for the scheduling of distributed
     * heterogeneous hybrid flow shop with blocking constraints
     * @param jobSeq 作业的加工顺序,传入Random_swap后的作业顺序
     * @param protime 作业的每阶段的加工时间
     * @param facJobQty 每个工厂中作业的数量，初始的，后续可以改变
     * @return 交换后的解
     */
    public  static int[][] IG_half_swap (int[] jobSeq, double[][] protime, int[] facJobQty) {

        double jobQty = jobSeq.length;
        double facQty = facJobQty.length;

        int[] facJobQty_range = new int[facJobQty.length];
        for (int k = 0; k < facJobQty_range.length; k++) {
            if(k==0){
                facJobQty_range[k]= facJobQty[k];
            }else
                facJobQty_range[k] =facJobQty_range[k-1] + facJobQty[k];
        }
        //1、each工厂
        int[] jobSeq_temp = jobSeq.clone();
        for (int i = 0; i < facQty; i++) {
            int end = facJobQty_range[i] ;
            int begin = facJobQty_range[i] - facJobQty[i];
            //比较前一半和后一半的makespan

            //迁移到分布式的问题
            int[] jobQtyInFac = new int[2];
            jobQtyInFac[0] = facJobQty[i] / 2;
            jobQtyInFac[1] = facJobQty[i] - facJobQty[i] / 2;
            int[] jobSeq_temp_Fa = new int[facJobQty[i]];
            for (int j = 0; j < facJobQty[i]; j++) {
                jobSeq_temp_Fa[j] = jobSeq_temp[begin + j];
            }
            double part = DHFSP.evaluate_exper(jobSeq_temp_Fa, protime, jobQtyInFac)[3];
            if (part == 0) {
                for (int j = begin; j < begin+facJobQty[i] / 2; j++) {
                    for (int k = begin; k <begin+facJobQty[i] / 2; k++) {
                        int[] jobSeq_temp1 = jobSeq_temp.clone();
                        int temp;
                        temp = jobSeq_temp1[j];
                        jobSeq_temp1[j] = jobSeq_temp1[k];
                        jobSeq_temp1[k] = temp;

                        double makespan_init = DHFSP.evaluate(jobSeq_temp, protime, facJobQty);
                        double makespan_change = DHFSP.evaluate(jobSeq_temp1, protime, facJobQty);
                        if (makespan_init > makespan_change) {
                            jobSeq_temp = jobSeq_temp1;
                        }
                    }
                }
            }else {
                for (int j = begin+facJobQty[i] / 2; j < end; j++) {
                    for (int k = begin+facJobQty[i] / 2; k <end; k++) {
                        int[] jobSeq_temp1 = jobSeq_temp.clone();
                        int temp;
                        temp = jobSeq_temp1[j];
                        jobSeq_temp1[j] = jobSeq_temp1[k];
                        jobSeq_temp1[k] = temp;

                        double makespan_init = DHFSP.evaluate(jobSeq_temp, protime, facJobQty);
                        double makespan_change = DHFSP.evaluate(jobSeq_temp1, protime, facJobQty);
                        if (makespan_init > makespan_change) {
                            jobSeq_temp = jobSeq_temp1;
                        }
                    }
                }
            }
        }
        int[][]  result = new int[2][];
        result[0] = jobSeq_temp;
        result[1] = facJobQty;
        return result;
    }


    /**
     * 破坏，随机地选择d个作业删除
     * @param jobSeq 作业的加工顺序
     * @param protime 作业的每阶段的加工时间
     * @param facJobQty 每个工厂中作业的数量，初始的，后续可以改变
     * @param d 随机删除的作业个数
     * @return 作业的加工顺序以及每个工厂中加工的作业数量，以及被删除作业的编号
     * result[0]: 删除后作业加工顺序  result[1] :删除后每个工厂中的作业数量 result[2] : 随机删除的作业的编号
     */
    public  static int[][] Destruction(int[] jobSeq, double[][] protime, int[] facJobQty,double d){
        int[][] result_Des = new int[3][];
        int[] jobSeq_Des = new int[(int) (jobSeq.length- d)];
        int[] facJobQty_Des = facJobQty.clone();

        //随机地选择d个作业
        int[] delectJobId ;
        delectJobId = random((int)d,jobSeq.length);
        int[] facJobQty_range = new int[facJobQty_Des.length];
        for (int i = 0; i < facJobQty_range.length; i++) {
            if(i==0){
                facJobQty_range[i]= facJobQty_Des[i];
            }else
            facJobQty_range[i] =facJobQty_range[i-1] + facJobQty_Des[i];
        }
        int n = 0;
        for (int i = 0; i < jobSeq.length; i++) {
            int sign = 0;
            for (int j = 0; j < delectJobId.length; j++) {
                if(jobSeq[i]==delectJobId[j]){
                    sign = 1;
                    //更新每个工厂中的作业数量
                    for (int k = 0; k < facJobQty_range.length; k++) {
                        if(i < facJobQty_range[0]){
                            facJobQty_Des[0] -= 1;
                            break;
                        } else if (i < facJobQty_range[k] && i >= facJobQty_range[k-1]) {
                            facJobQty_Des[k] -=1 ;
                            break;
                        }
                    }
                    break;
                }
            }
            if(n < jobSeq.length- d && sign==0){
                //复制新的作业加工顺序
                jobSeq_Des[n] = jobSeq[i];
                n++;
            }

        }
        result_Des[0] = jobSeq_Des;
        result_Des[1] = facJobQty_Des;
        result_Des[2] = delectJobId;
        return  result_Des;
    }

    /**
     * 重组
     * @param jobSeq_Des 删除后作业加工顺序
     * @param protime 作业的每阶段的加工时间
     * @param jobQty_infac_DES 每个工厂中作业的数量
     * @param delectJobId 随机删除的作业
     * @return 贪心重组后的作业加工顺序
     */
    public  static int[][] Construction(int[] jobSeq_Des, double[][] protime, int[] jobQty_infac_DES,
                                        int[] delectJobId, int evaluate){

        double jobQty = jobSeq_Des.length + delectJobId.length;
        double facQty = jobQty_infac_DES.length;
        int[] jobQtyInfac = jobQty_infac_DES.clone();

        //每个工厂的作业范围
        int[] facJobQty_range = new int[jobQty_infac_DES.length];
        for (int i = 0; i < facJobQty_range.length; i++) {
            if(i==0){
                facJobQty_range[i]= jobQty_infac_DES[i];
            }else
                facJobQty_range[i] =facJobQty_range[i-1] + jobQty_infac_DES[i];
        }

        //Map<工厂编号，工厂中的作业加工顺序>
        HashMap<Integer, LinkedList> jobSeqInfac= new HashMap<>();
        for (int i = 0; i < facQty; i++) {
            LinkedList<Integer> jobList = new LinkedList<Integer>();
            jobSeqInfac.put(i,jobList);
        }

        //将原来的Des解数组变为List
        for (int i = 0; i <facJobQty_range.length ; i++) {
            LinkedList<Integer> jobList_i = jobSeqInfac.get(i);
            int j;
            if(i==0){j = 0;}
            else {j = facJobQty_range[i-1];}
            while (j < facJobQty_range[i]){
                jobList_i.add(jobSeq_Des[j]);
                j++;
            }
        }


        //后续的作业则依次放入到每个工厂的最小Makespan位置
        for (int i = 0; i < delectJobId.length; i++) {
            int minfac = 0;
            int minposit = 0;
            double minresult =483648;
            //遍历工厂
            for (int j = 0; j < facQty; j++) {
                LinkedList jobList = jobSeqInfac.get(j);
                //遍历工厂的可插入位置
                for (int k = 0; k < jobList.size()+1; k++) {
                    jobList.add(k,delectJobId[i]);
                    jobQtyInfac[j] += 1;
                    //获取完整的解
                    int[] jobSeq_temp = changeList(jobSeqInfac,jobQtyInfac);
//                    double tempresult = DHFSP.evaluate(jobSeq_temp,protime,jobQtyInfac);
                    double tempresult = DHFSP.evaluate_exper(jobSeq_temp,protime,jobQtyInfac)[evaluate];

                    if(tempresult<minresult){
                        minresult = tempresult;
                        minfac = j;
                        minposit = k;
                    }
                    jobQtyInfac[j] -= 1;
                    jobList.remove(k);
                }
            }

            //将其插入到最小工厂的最小位置
            LinkedList jobList = jobSeqInfac.get(minfac);
            jobList.add(minposit,delectJobId[i]);
            jobQtyInfac[minfac] += 1;


        }
        //result[0]: NEH2初始化后的作业加工顺序  result[1] :每个工厂中的作业数量
        int[][]  result = new int[2][];
        int[] jobSeq_Con = new int[(int)jobQty];
        jobSeq_Con = changeList(jobSeqInfac,jobQtyInfac);

        result[0] = jobSeq_Con;
        result[1] = jobQtyInfac;

        return result;
    }

    /**
     * 重组
     * @param jobSeq_Des 删除后作业加工顺序
     * @param protime 作业的每阶段的加工时间
     * @param jobQty_infac_DES 每个工厂中作业的数量
     * @param delectJobId 随机删除的作业
     * @return 贪心重组后的作业加工顺序
     */
    public  static int[][] Construction1(int[] jobSeq_Des, double[][] protime, int[] jobQty_infac_DES,
                                        int[] delectJobId, int evaluate){

        double jobQty = jobSeq_Des.length + delectJobId.length;
        double facQty = jobQty_infac_DES.length;
        int[] jobQtyInfac = jobQty_infac_DES.clone();

        //每个工厂的作业范围
        int[] facJobQty_range = new int[jobQty_infac_DES.length];
        for (int i = 0; i < facJobQty_range.length; i++) {
            if(i==0){
                facJobQty_range[i]= jobQty_infac_DES[i];
            }else
                facJobQty_range[i] =facJobQty_range[i-1] + jobQty_infac_DES[i];
        }

        //Map<工厂编号，工厂中的作业加工顺序>
        HashMap<Integer, LinkedList> jobSeqInfac= new HashMap<>();
        for (int i = 0; i < facQty; i++) {
            LinkedList<Integer> jobList = new LinkedList<Integer>();
            jobSeqInfac.put(i,jobList);
        }

        //将原来的Des解数组变为List
        for (int i = 0; i <facJobQty_range.length ; i++) {
            LinkedList<Integer> jobList_i = jobSeqInfac.get(i);
            int j;
            if(i==0){j = 0;}
            else {j = facJobQty_range[i-1];}
            while (j < facJobQty_range[i]){
                jobList_i.add(jobSeq_Des[j]);
                j++;
            }
        }


        //后续的作业则依次放入到每个工厂的最小Makespan位置
        for (int i = 0; i < delectJobId.length; i++) {
            int minfac = 0;
            int minposit = 0;
            double minresult =483648;
            //遍历工厂
            for (int j = 0; j < facQty; j++) {
                LinkedList jobList = jobSeqInfac.get(j);
                //遍历工厂的可插入位置
                for (int k = 0; k < jobList.size()+1; k++) {
                    jobList.add(k,delectJobId[i]);
                    jobQtyInfac[j] += 1;
                    //获取完整的解
                    int[] jobSeq_temp = changeList(jobSeqInfac,jobQtyInfac);
//                    double tempresult = DHFSP.evaluate(jobSeq_temp,protime,jobQtyInfac);
                    double tempresult = DHFSP.evaluate_exper(jobSeq_temp,protime,jobQtyInfac)[evaluate];

                    if(tempresult<minresult){
                        minresult = tempresult;
                        minfac = j;
                        minposit = k;
                    }
                    jobQtyInfac[j] -= 1;
                    jobList.remove(k);
                }
            }

            //将其插入到最小工厂的最小位置
            LinkedList jobList = jobSeqInfac.get(minfac);
            jobList.add(minposit,delectJobId[i]);
            jobQtyInfac[minfac] += 1;

            //随机选择该位置的前一个或者后一个位置的作业重新插入
            int r1 = random_1(2);
            int jobID_adjacent = 0;
            int adjacent = 0;
            if(r1==0&&minposit-1>-1){
                adjacent = minposit-1;
                LinkedList jobList_A = jobSeqInfac.get(minfac);
                jobID_adjacent = (int) jobList_A.get(adjacent);
                jobList_A.remove(adjacent);
                jobQtyInfac[minfac] -= 1;
            }else if(r1==0&&minposit-1==-1){
                adjacent = minposit+1;
                LinkedList jobList_A = jobSeqInfac.get(minfac);
                jobID_adjacent = (int) jobList_A.get(adjacent);
                jobList_A.remove(adjacent);
                jobQtyInfac[minfac] -= 1;
            }else if (r1==1&&minposit+1<jobSeqInfac.get(minfac).size()){
                adjacent = minposit+1;
                LinkedList jobList_A = jobSeqInfac.get(minfac);
                jobID_adjacent = (int) jobList_A.get(adjacent);
                jobList_A.remove(adjacent);
                jobQtyInfac[minfac] -= 1;
            }else if(r1==1&&minposit+1==jobSeqInfac.get(minfac).size()){
                adjacent = minposit-1;
                LinkedList jobList_A = jobSeqInfac.get(minfac);
                jobID_adjacent = (int) jobList_A.get(adjacent);
                jobList_A.remove(adjacent);
                jobQtyInfac[minfac] -= 1;
            }
            int minfac_e = minfac;
            int minposit_e = adjacent;
            double minresult_e =minresult;
            //遍历工厂
            for (int j = 0; j < facQty; j++) {
                LinkedList jobList_e = jobSeqInfac.get(j);
                //遍历工厂的可插入位置
                for (int k = 0; k < jobList_e.size()+1; k++) {
                    jobList_e.add(k,jobID_adjacent);
                    jobQtyInfac[j] += 1;
                    //获取完整的解
                    int[] jobSeq_temp = changeList(jobSeqInfac,jobQtyInfac);
                    double tempresult = DHFSP.evaluate_exper(jobSeq_temp,protime,jobQtyInfac)[evaluate];
                    if(tempresult<minresult_e){
                        minresult_e = tempresult;
                        minfac_e = j;
                        minposit_e = k;
                    }
                    jobQtyInfac[j] -= 1;
                    jobList_e.remove(k);
                }
            }

            //将其插入到最小工厂的最小位置
            LinkedList jobList_e = jobSeqInfac.get(minfac_e);
            jobList_e.add(minposit_e,jobID_adjacent);
            jobQtyInfac[minfac_e] += 1;

        }
        //result[0]: NEH2初始化后的作业加工顺序  result[1] :每个工厂中的作业数量
        int[][]  result = new int[2][];
        int[] jobSeq_Con = new int[(int)jobQty];
        jobSeq_Con = changeList(jobSeqInfac,jobQtyInfac);

        result[0] = jobSeq_Con;
        result[1] = jobQtyInfac;

        return result;
    }
    /**
     * 模拟退火
     * @param result_best 当前最优解
     * @param result_temp 当前解
     * @param protime 加工时间
     * @param facQty 工厂数量
     * @param machQty 机器数量
     * @param jobQty 作业数量 ？
     * @param T 温度参数
     * @return 返回解被接受的概率
     */
    public  static double SA(double result_best,double result_temp,
                             double[][] protime, double facQty,double machQty,double jobQty,double T){
        //返回解被接受的概率
        double SA_Temper = 0;
        //计算Temperature
        double Temperature = 0;
        double n1 =0;
        for (int i = 0; i < protime.length; i++) {
            for (int j = 0; j < protime[i].length; j++) {
                n1 += protime[i][j];
            }
        }
        double n2 =0;
        n2 = Math.sqrt(facQty);
        Temperature = T*n1/(n2*jobQty*machQty*10);
        //计算 SA_Temper
        double n3 =0;
        n3 = (result_best - result_temp)/Temperature;
        SA_Temper = Math.exp(n3);
        return SA_Temper;
    }

    /**
     * DLPT
     * Discrete differential evolution algorithm for distributed blocking flowshop
     * scheduling with makespan criterion
     * @param jobSeq 作业的加工顺序
     * @param protime 作业的每阶段的加工时间
     * @param facJobQty 每个工厂中作业的数量，初始的，后续可以改变
     * @return 作业的加工顺序以及每个工厂中加工的作业数量，
     * result[0]: DLPT初始化后的作业加工顺序  result[1] :每个工厂中的作业数量
     */
    public  static int[][] DLPT(int[] jobSeq, double[][] protime, int[] facJobQty) {
        //1、求每个工序在每台机器上的加工时间总长，按递减顺序排列
        double jobQty = jobSeq.length;
        double proQty = protime.length;
        double[] jobProTime_toatal = new double[(int)jobQty];
        //求和
        for (int i = 0; i < jobQty; i++) {
            double jobProTimetatal = 0;
            for (int j = 0; j < proQty; j++) {
                jobProTimetatal+=protime[j][i];
            }
            jobProTime_toatal[i] = jobProTimetatal;
        }
        //排序返回升序的机器编号
        int[] jobSeq_ascend;
        jobSeq_ascend = EasyMath.sortArray(jobProTime_toatal);

        //2、取出第一步的后facQty个分别放入到每个工厂，后续的作业则依次放入到每个工厂的最小Makespan位置
        double facQty = facJobQty.length;
        int[] jobQtyInfac = new int[(int)facQty];

        HashMap<Integer, LinkedList> jobSeqInfac= new HashMap<>();
        for (int i = 0; i < facQty; i++) {
            LinkedList<Integer> jobList = new LinkedList<Integer>();
            jobSeqInfac.put(i,jobList);
        }
        //取出第一步的后facQty个分别放入到每个工厂
        for (int i = 0; i < facQty; i++) {
            LinkedList jobList = jobSeqInfac.get(i);
            jobList.add(jobSeq_ascend[(int)jobQty-1-i]);
            jobQtyInfac[i] += 1;
        }
        //后续的作业则依次放入到每个工厂的最小Makespan位置
        for (int i = (int)(jobQty-facQty-1); i >=0 ; i--) {
            int minfac = 0;
            int minposit = 0;
            double minresult =483648;
            //遍历工厂
            for (int j = 0; j < facQty; j++) {
                LinkedList jobList = jobSeqInfac.get(j);
                    jobList.addLast(jobSeq_ascend[i]);
                    jobQtyInfac[j] += 1;
                    //获取完整的解
                    int[] jobSeq_temp = changeList(jobSeqInfac,jobQtyInfac);
                    double tempresult = DHFSP.evaluate(jobSeq_temp,protime,jobQtyInfac);

                    if(tempresult<minresult){
                        minresult = tempresult;
                        minfac = j;
                    }
                    jobQtyInfac[j] -= 1;
                    jobList.removeLast();
            }

            //将其插入到最小工厂的最小位置
            LinkedList jobList = jobSeqInfac.get(minfac);
            jobList.addLast(jobSeq_ascend[i]);
            jobQtyInfac[minfac] += 1;

        }
        //result[0]: NEH2初始化后的作业加工顺序  result[1] :每个工厂中的作业数量
        int[][]  result = new int[2][];
        int[] jobSeq_NEH ;
        jobSeq_NEH = changeList(jobSeqInfac,jobQtyInfac);

        result[0] = jobSeq_NEH;
        result[1] = jobQtyInfac;

        return result;
    }
    /**
     * DSPT
     * Discrete differential evolution algorithm for distributed blocking flowshop
     * scheduling with makespan criterion
     * @param jobSeq 作业的加工顺序
     * @param protime 作业的每阶段的加工时间
     * @param facJobQty 每个工厂中作业的数量，初始的，后续可以改变
     * @return 作业的加工顺序以及每个工厂中加工的作业数量，
     * result[0]: DLPT初始化后的作业加工顺序  result[1] :每个工厂中的作业数量
     */
    public  static int[][] DSPT(int[] jobSeq, double[][] protime, int[] facJobQty) {
        //1、求每个工序在每台机器上的加工时间总长，按递减顺序排列
        double jobQty = jobSeq.length;
        double proQty = protime.length;
        double[] jobProTime_toatal = new double[(int)jobQty];
        //求和
        for (int i = 0; i < jobQty; i++) {
            double jobProTimetatal = 0;
            for (int j = 0; j < proQty; j++) {
                jobProTimetatal+=protime[j][i];
            }
            jobProTime_toatal[i] = jobProTimetatal;
        }
        //排序返回升序的机器编号
        int[] jobSeq_ascend;
        jobSeq_ascend = EasyMath.sortArray(jobProTime_toatal);
        //逆序后 jobSeq_ascend变为 jobSeq_decrease
//        long startTime = System.currentTimeMillis();
        for (int i = 0; i <  jobSeq_ascend.length; i++) {
            for (int j =  jobSeq_ascend.length - 1; j > i; j--) {
                int temp =  jobSeq_ascend[i];
                if ( jobSeq_ascend[i] <  jobSeq_ascend[j]) {
                    jobSeq_ascend[i] =  jobSeq_ascend[j];
                    jobSeq_ascend[j] = temp;
                }
            }
        }
//        long endTime = System.currentTimeMillis();
//        System.out.println("耗时:" + (endTime - startTime));

        //2、取出第一步的后facQty个分别放入到每个工厂，后续的作业则依次放入到每个工厂的最小Makespan位置

        double facQty = facJobQty.length;
        int[] jobQtyInfac = new int[(int)facQty];

        HashMap<Integer, LinkedList> jobSeqInfac= new HashMap<>();
        for (int i = 0; i < facQty; i++) {
            LinkedList<Integer> jobList = new LinkedList<Integer>();
            jobSeqInfac.put(i,jobList);
        }
        //取出第一步的后facQty个分别放入到每个工厂
        for (int i = 0; i < facQty; i++) {
            LinkedList jobList = jobSeqInfac.get(i);
            jobList.add(jobSeq_ascend[(int)jobQty-1-i]);
            jobQtyInfac[i] += 1;
        }
        //后续的作业则依次放入到每个工厂的最小Makespan位置
        for (int i = (int)(jobQty-facQty-1); i >=0 ; i--) {
            int minfac = 0;
            int minposit = 0;
            double minresult =483648;
            //遍历工厂
            for (int j = 0; j < facQty; j++) {
                LinkedList jobList = jobSeqInfac.get(j);
                jobList.addLast(jobSeq_ascend[i]);
                jobQtyInfac[j] += 1;
                //获取完整的解
                int[] jobSeq_temp = changeList(jobSeqInfac,jobQtyInfac);
                double tempresult = DHFSP.evaluate(jobSeq_temp,protime,jobQtyInfac);

                if(tempresult<minresult){
                    minresult = tempresult;
                    minfac = j;
                }
                jobQtyInfac[j] -= 1;
                jobList.removeLast();
            }

            //将其插入到最小工厂的最小位置
            LinkedList jobList = jobSeqInfac.get(minfac);
            jobList.addLast(jobSeq_ascend[i]);
            jobQtyInfac[minfac] += 1;

        }
        //result[0]: NEH2初始化后的作业加工顺序  result[1] :每个工厂中的作业数量
        int[][]  result = new int[2][];
        int[] jobSeq_NEH ;
        jobSeq_NEH = changeList(jobSeqInfac,jobQtyInfac);

        result[0] = jobSeq_NEH;
        result[1] = jobQtyInfac;

        return result;
    }
    /**
     * DLS
     * Discrete differential evolution algorithm for distributed blocking flowshop
     * scheduling with makespan criterion
     * @param jobSeq 作业的加工顺序
     * @param protime 作业的每阶段的加工时间
     * @param facJobQty 每个工厂中作业的数量，初始的，后续可以改变
     * @return 作业的加工顺序以及每个工厂中加工的作业数量，
     * result[0]: DLPT初始化后的作业加工顺序  result[1] :每个工厂中的作业数量
     */
    public  static int[][] DLS(int[] jobSeq, double[][] protime, int[] facJobQty) {
        //1、求每个工序在每台机器上的加工时间总长，按递减顺序排列
        double jobQty = jobSeq.length;
        double proQty = protime.length;
        double[] jobProTime_toatal = new double[(int)jobQty];
        //求和
        for (int i = 0; i < jobQty; i++) {
            double jobProTimetatal = 0;
            for (int j = 0; j < proQty; j++) {
                jobProTimetatal+=protime[j][i];
            }
            jobProTime_toatal[i] = jobProTimetatal;
        }
        //排序返回升序的机器编号
        int[] jobSeq_ascend;
        jobSeq_ascend = EasyMath.sortArray(jobProTime_toatal);
        //逆序后 jobSeq_ascend变为 jobSeq_decrease
        for (int i = 0; i <  jobSeq_ascend.length; i++) {
            for (int j =  jobSeq_ascend.length - 1; j > i; j--) {
                int temp =  jobSeq_ascend[i];
                if ( jobSeq_ascend[i] <  jobSeq_ascend[j]) {
                    jobSeq_ascend[i] =  jobSeq_ascend[j];
                    jobSeq_ascend[j] = temp;
                }
            }
        }
        //交叉
        int n=0;
        int[] jobSeq_DLS = new int[(int) jobQty];
        for (int i = 0; i < jobSeq_ascend.length/2; i++) {
            jobSeq_DLS[n++] = jobSeq_ascend[i];
            jobSeq_DLS[n++] = jobSeq_ascend[jobSeq_ascend.length-1-i];
        }

        //2、取出第一步的后facQty个分别放入到每个工厂，后续的作业则依次放入到每个工厂的最小Makespan位置

        double facQty = facJobQty.length;
        int[] jobQtyInfac = new int[(int)facQty];

        HashMap<Integer, LinkedList> jobSeqInfac= new HashMap<>();
        for (int i = 0; i < facQty; i++) {
            LinkedList<Integer> jobList = new LinkedList<Integer>();
            jobSeqInfac.put(i,jobList);
        }
        //取出第一步的后facQty个分别放入到每个工厂
        for (int i = 0; i < facQty; i++) {
            LinkedList jobList = jobSeqInfac.get(i);
            jobList.add(jobSeq_DLS[i]);
            jobQtyInfac[i] += 1;
        }
        //后续的作业则依次放入到每个工厂的最小Makespan位置
        for (int i = (int) facQty; i <jobSeq_DLS.length; i++) {
            int minfac = 0;
            int minposit = 0;
            double minresult =483648;
            //遍历工厂
            for (int j = 0; j < facQty; j++) {
                LinkedList jobList = jobSeqInfac.get(j);
                jobList.addLast(jobSeq_DLS[i]);
                jobQtyInfac[j] += 1;
                //获取完整的解
                int[] jobSeq_temp = changeList(jobSeqInfac,jobQtyInfac);
                double tempresult = DHFSP.evaluate(jobSeq_temp,protime,jobQtyInfac);

                if(tempresult<minresult){
                    minresult = tempresult;
                    minfac = j;
                }
                jobQtyInfac[j] -= 1;
                jobList.removeLast();
            }

            //将其插入到最小工厂的最小位置
            LinkedList jobList = jobSeqInfac.get(minfac);
            jobList.addLast(jobSeq_DLS[i]);
            jobQtyInfac[minfac] += 1;

        }
        //result[0]: NEH2初始化后的作业加工顺序  result[1] :每个工厂中的作业数量
        int[][]  result = new int[2][];
        int[] jobSeq_NEH ;
        jobSeq_NEH = changeList(jobSeqInfac,jobQtyInfac);

        result[0] = jobSeq_NEH;
        result[1] = jobQtyInfac;

        return result;
    }

    /**ECFS
     * 随机选择一个fac，找出关键工厂，分别随机一个作业，然后交换
     * @param result_k 解
     * @param protime 作业的每阶段的加工时间
     * @return 交换后的解
     */
    public  static int[][] ECFS(int[][] result_k, double[][] protime) {
        int[] jobSeq=result_k[0].clone();
        int[] facJobQty = result_k[1].clone();
        double jobQty = jobSeq.length;
        double proQty = protime.length;

        //1、找出关键工厂
        int criticalFac = (int) DHFSP.evaluate_exper(jobSeq,protime,facJobQty)[3];
        //随机一个非关键工厂
        int randomFac = criticalFac ;
        while (randomFac==criticalFac){
            randomFac = random_1(facJobQty.length);
        }

        int[] jobSeq_temp = jobSeq.clone();

            //2、分别在两个工厂中随机选中两个作业(位置)
            //每个工厂的作业范围

            int[] facJobQty_range = new int[facJobQty.length];
            for (int i = 0; i < facJobQty_range.length; i++) {
                if(i==0){
                    facJobQty_range[i]= facJobQty[i];
                }else
                    facJobQty_range[i] =facJobQty_range[i-1] + facJobQty[i];
            }

            int randomJob_0,randomJob_1;
            int max_0 = facJobQty_range[criticalFac] - 1;
            int min_0 = facJobQty_range[criticalFac] - facJobQty[criticalFac];
            randomJob_0 = (int) (Math.random() * (max_0 - min_0 + 1) + min_0);
            int max_1 = facJobQty_range[randomFac] - 1;
            int min_1 = facJobQty_range[randomFac] - facJobQty[randomFac];
            randomJob_1 = (int) (Math.random() * (max_1 - min_1 + 1) + min_1);

            //3、将两个位置的作业交换
            int[] jobSeq_change = jobSeq_temp.clone();
            int temp;
            temp = jobSeq_change[randomJob_0];
            jobSeq_change[randomJob_0] = jobSeq_change[randomJob_1];
            jobSeq_change[randomJob_1] = temp;

            //比较交换前后的makespan
            double makespan_init = DHFSP.evaluate(jobSeq_temp,protime,facJobQty);
            double makespan_change = DHFSP.evaluate(jobSeq_change,protime,facJobQty);
            if(makespan_init>makespan_change){
                jobSeq_temp = jobSeq_change;

            }


        int[][]  result = new int[2][];
        result[0] = jobSeq_temp;
        result[1] = facJobQty;
        return result;
    }

    /**ICFI
     * 找出关键工厂，随机一个作业，然后工厂内插入寻找最优
     * @param result_k 解
     * @param protime 作业的每阶段的加工时间
     * @return 作业的加工顺序以及每个工厂中加工的作业数量，
     * result[0]: NEH2初始化后的作业加工顺序  result[1] :每个工厂中的作业数量
     */
    public  static int[][] ICFI(int[][] result_k, double[][] protime) {
        int[] jobSeq=result_k[0].clone();
        int[] facJobQty = result_k[1].clone();
        double facQty = facJobQty.length;

        //1、找出关键工厂
        int criticalFac = (int) DHFSP.evaluate_exper(jobSeq,protime,facJobQty)[3];
        //随机一个非关键工厂
        int randomFac = criticalFac ;
        while (randomFac==criticalFac){
            randomFac = random_1(facJobQty.length);
        }

        int[] jobSeq_temp = jobSeq.clone();

        //2、在关键工厂中随机一个作业重新插入

        //每个工厂的作业范围
        int[] facJobQty_range = new int[facJobQty.length];
        for (int i = 0; i < facJobQty_range.length; i++) {
            if(i==0){
                facJobQty_range[i]= facJobQty[i];
            }else
                facJobQty_range[i] =facJobQty_range[i-1] + facJobQty[i];
        }

//        int randomJob_0;
//        int max_0 = facJobQty_range[criticalFac] - 1;
//        int min_0 = facJobQty_range[criticalFac] - facJobQty[criticalFac];
//        randomJob_0 = (int) (Math.random() * (max_0 - min_0 + 1) + min_0);
//        int random_jobId = jobSeq_temp[randomJob_0];

        //3、重新插入
        //Map<工厂编号，工厂中的作业加工顺序>
        HashMap<Integer, LinkedList> jobSeqInfac= new HashMap<>();
        for (int i = 0; i < facQty; i++) {
            LinkedList<Integer> jobList = new LinkedList<Integer>();
            jobSeqInfac.put(i,jobList);
        }

        //将原来的解数组变为List
        for (int i = 0; i <facJobQty_range.length ; i++) {
            LinkedList<Integer> jobList_i = jobSeqInfac.get(i);
            int j;
            if(i==0){j = 0;}
            else {j = facJobQty_range[i-1];}
            while (j < facJobQty_range[i]){
                jobList_i.add(jobSeq_temp[j]);
                j++;
            }
        }

        //后续的作业则依次放入到每个工厂的最小Makespan位置
        //找出关键工厂的List，移除随机的作业
        LinkedList<Integer> jobList_CF = jobSeqInfac.get(criticalFac);
        int randomJobposi = random_1(facJobQty[criticalFac]);
        int random_jobId = jobList_CF.get(randomJobposi);
        jobList_CF.remove((Integer) random_jobId);
        //重新插入最小位置
        double minresult =DHFSP.evaluate(jobSeq,protime,facJobQty);
        int minposit = randomJobposi;
        for (int i = 0; i < jobList_CF.size()+1; i++) {
            jobList_CF.add(i,random_jobId);
            int[] jobSeqt = changeList(jobSeqInfac,facJobQty);
            double tempresult = DHFSP.evaluate(jobSeqt,protime,facJobQty);
            if(tempresult<minresult){
                minresult = tempresult;
                minposit = i;
            }
            jobList_CF.remove(i);
            }

            //将其插入到最小工厂的最小位置
            jobList_CF.add(minposit,random_jobId);

        int[] jobSeq_ICFI = changeList(jobSeqInfac,facJobQty);
        int[][]  result = new int[2][];
        result[0] = jobSeq_ICFI;
        result[1] = facJobQty;
        return result;
    }

    /**EDFI
     * 随机选择一个fac，找出关键工厂，关键工厂中随机一个作业，然后插入到非关键工厂的最优位置
     * @param result_k 解
     * @param protime 作业的每阶段的加工时间
     * @return 作业的加工顺序以及每个工厂中加工的作业数量，
     * result[0]: NEH2初始化后的作业加工顺序  result[1] :每个工厂中的作业数量
     */
    public  static int[][] EDFI(int[][] result_k, double[][] protime) {
        int[] jobSeq=result_k[0].clone();
        int[] facJobQty = result_k[1].clone();
        double facQty = facJobQty.length;

        //1、找出关键工厂
        int criticalFac = (int) DHFSP.evaluate_exper(jobSeq,protime,facJobQty)[3];
        //随机一个非关键工厂
        int randomFac = criticalFac ;
        while (randomFac==criticalFac){
            randomFac = random_1(facJobQty.length);
        }
        facJobQty[criticalFac] -= 1;
        facJobQty[randomFac] += 1;

        int[] jobSeq_temp = jobSeq.clone();

        //2、在关键工厂中随机一个作业重新插入

        //每个工厂的作业范围
        int[] facJobQty_range = new int[facJobQty.length];
        for (int i = 0; i < facJobQty_range.length; i++) {
            if(i==0){
                facJobQty_range[i]= facJobQty[i];
            }else
                facJobQty_range[i] =facJobQty_range[i-1] + facJobQty[i];
        }

        int randomJob_0;
        int max_0 = facJobQty_range[criticalFac] - 1;
        int min_0 = facJobQty_range[criticalFac] - facJobQty[criticalFac];
        randomJob_0 = (int) (Math.random() * (max_0 - min_0 + 1) + min_0);
        int random_jobId = jobSeq_temp[randomJob_0];

        //3、重新插入
        //Map<工厂编号，工厂中的作业加工顺序>
        HashMap<Integer, LinkedList> jobSeqInfac= new HashMap<>();
        for (int i = 0; i < facQty; i++) {
            LinkedList<Integer> jobList = new LinkedList<Integer>();
            jobSeqInfac.put(i,jobList);
        }

        //将原来的Des解数组变为List
        for (int i = 0; i <facJobQty_range.length ; i++) {
            LinkedList<Integer> jobList_i = jobSeqInfac.get(i);
            int j;
            if(i==0){j = 0;}
            else {j = facJobQty_range[i-1];}
            while (j < facJobQty_range[i]){
                jobList_i.add(jobSeq_temp[j]);
                j++;
            }
        }

        //后续的作业则依次放入到每个工厂的最小Makespan位置
        //找出关键工厂的List，移除随机的作业
        LinkedList<Integer> jobList_CF = jobSeqInfac.get(criticalFac);
        LinkedList<Integer> jobList_RD = jobSeqInfac.get(randomFac);
        jobList_CF.remove((Integer) random_jobId);
        //重新插入最小位置
        double minresult =483648;
        int minposit = 0;
        for (int i = 0; i < jobList_RD.size()+1; i++) {
            jobList_RD.add(i,random_jobId);
            int[] jobSeqt = changeList(jobSeqInfac,facJobQty);
            double tempresult = DHFSP.evaluate(jobSeqt,protime,facJobQty);
            if(tempresult<minresult){
                minresult = tempresult;
                minposit = i;
            }
            jobList_RD.remove(i);
        }

        //将其插入到最小工厂的最小位置
        jobList_RD.add(minposit,random_jobId);
        int[] jobSeq_EDFI = changeList(jobSeqInfac,facJobQty);
        int[][]  result = new int[2][];
        result[0] = jobSeq_EDFI;
        result[1] = facJobQty;
        return result;
    }

    /** DDEmutation
     *
     * @param result_k
     * @param protime
     * @param K 变异概率
     * @return
     */
    public  static int[][] DDEmutation(int[][] result_k, double[][] protime,double K) {
        int[] jobSeq=result_k[0].clone();
        int[] facJobQty = result_k[1].clone();
        int jobQty = jobSeq.length;
        int proQty = protime.length;
        int facQty = facJobQty.length;
        //1 随机Ya和Yb,运算得到derta Y
        int[] Ya = random(jobQty,jobQty);
        int[] Yb = random(jobQty,jobQty);
        int[] Y1= new int[jobQty]; //derta
        int[] Y2= new int[jobQty]; //fai
        for (int i = 0; i <jobQty; i++) {
            if(Ya[i]==Yb[i]){
                Y1[i] = 0;
            }else Y1[i] = Ya[i];
        }
        //依据K得到 fai Y2
        for (int i = 0; i < jobQty; i++) {
            double random = Math.random();
            if(random<K){
                Y2[i] = Y1[i];
            }else Y2[i] = 0;
        }

        int[] jobSeqT=jobSeq.clone();
        for (int i = 0; i < Y2.length; i++) {

            int[] facJobQty_range = new int[facJobQty.length];
            for (int j = 0; j < facJobQty_range.length; j++) {
                if(j==0){
                    facJobQty_range[j]= facJobQty[j];
                }else
                    facJobQty_range[j] =facJobQty_range[j-1] + facJobQty[j];
            }

            //将原来的解数组变为List
            LinkedList<Integer> jobList = new LinkedList<>();
            for (int j = 0; j <jobQty; j++) {
                jobList.add(jobSeqT[j]);
            }

            if(Y2[i] != 0){
                double random = Math.random();
                int job1 = Y2[i];
                int job2 = jobSeqT[i];
                int idx1 = jobList.indexOf((Integer) job1);
                int idx2 = jobList.indexOf((Integer)job2);
                if(random<0.5){
                   //交换
                    int temp;
                    temp = jobSeqT[idx1];
                    jobSeqT[idx1] = jobSeqT[idx2];
                    jobSeqT[idx2] = temp;
                }else {
                    //2插在1后面，同一工厂
                    jobList.remove(idx2);
                    if(idx1==jobQty-1){
                        jobList.addLast(job2);
                    }else jobList.add(idx1+1,job2);
                    //将List变为int[]
                    for (int j = 0; j < jobList.size(); j++) {
                        jobSeqT[j] = jobList.get(j);
                    }
                    //更新facJobQty,工厂中的作业数，确定j1 和j2 所在工厂
                    int fac1=0,fac2 = 0;
                    for (int j = 0; j < facJobQty_range.length; j++) {
                        if(idx1<facJobQty_range[j]&&idx1>=facJobQty_range[j]-facJobQty[j]){ fac1=j; }
                        else if(idx2<facJobQty_range[j]&&idx2>=facJobQty_range[j]-facJobQty[j]){ fac2=j; }
                    }
                    facJobQty[fac2]-=1;
                    facJobQty[fac1]+=1;
                }
            }
        }

        int[][]  result = new int[2][];
        result[0] = jobSeqT;
        result[1] = facJobQty;
        return result;
    }

    /**交叉算子
     * Discrete differential evolution algorithm for distributed blocking flowshop
     * scheduling with makespan criterion
     * @param result_k
     * @param result_v
     * @param Cp
     * @return
     */
    public  static int[][] DDEcrossover(int[][] result_k, int[][] result_v,double Cp) {
        int[] jobSeq_k=result_k[0].clone();
        int[] jobSeq_v=result_v[0].clone();
        int[] facJobQty_k = result_k[1].clone();
        int jobQty = jobSeq_k.length;
        int facQty = facJobQty_k.length;

        //1 随机概率 生成sita数组 Y中是选择作业坐在位置
        int[] Y = new int[jobQty];
        int n=0;
        for (int i = 0; i < jobQty; i++) {
            double random = Math.random();
            if(random >= Cp){
                Y[n++] = i;
            }
        }
        //转换成K中对应的作业编号
        int[] Y_jobId = new int[n];
        for (int i = 0; i < n; i++) {
            Y_jobId[i] = jobSeq_k[Y[i]];
        }
        //上面作业在v中内部顺序
        int[] Y_jobId_V = new int[n];
        int n1=0;
        for (int i = 0; i < jobSeq_v.length; i++) {
            for (int j = 0; j < Y_jobId.length; j++) {
                if(jobSeq_v[i]==Y_jobId[j]){
                    Y_jobId_V[n1++] = Y_jobId[j];
                }
            }
        }
        //按照Y_jobId_V的顺序更新jobSeq_k
        for (int i = 0; i < Y_jobId_V.length; i++) {
            jobSeq_k[Y[i]] = Y_jobId_V[i];
        }

        int[][]  result = new int[2][];
        result[0] = jobSeq_k;
        result[1] = facJobQty_k;
        return result;
    }

    /**LS3
     * 找出关键工厂，随机一个作业，然后工厂内插入寻找最优
     * @param result_0 解
     * @param protime 作业的每阶段的加工时间
     * @return 作业的加工顺序以及每个工厂中加工的作业数量，
     * result[0]: NEH2初始化后的作业加工顺序  result[1] :每个工厂中的作业数量
     */
    public  static int[][] LS3(int[][] result_0, double[][] protime) {
        int[] jobSeq=result_0[0].clone();
        int[] facJobQty = result_0[1].clone();
        int facQty = facJobQty.length;


        //每个工厂的作业范围
        int[] facJobQty_range = new int[facJobQty.length];
        for (int i = 0; i < facJobQty_range.length; i++) {
            if(i==0){
                facJobQty_range[i]= facJobQty[i];
            }else
                facJobQty_range[i] =facJobQty_range[i-1] + facJobQty[i];
        }
        //将数组变为链表
        //Map<工厂编号，工厂中的作业加工顺序>
        HashMap<Integer, LinkedList> jobSeqInfac= new HashMap<>();
        for (int i = 0; i < facQty; i++) {
            LinkedList<Integer> jobList = new LinkedList<Integer>();
            jobSeqInfac.put(i,jobList);
        }
        //将原来的解数组变为List
        for (int i = 0; i <facJobQty_range.length ; i++) {
            LinkedList<Integer> jobList_i = jobSeqInfac.get(i);
            int j;
            if(i==0){j = 0;}
            else {j = facJobQty_range[i-1];}
            while (j < facJobQty_range[i]){
                jobList_i.add(jobSeq[j]);
                j++;
            }
        }


        double minresult = DHFSP.evaluate_exper(jobSeq,protime,facJobQty)[1];
        int n=0;
        while (n<facQty){
            //随机一个工厂
            int criticalFac = random_1(facQty);
            int improve = 0;
            while (improve==0) {
                improve = 1;
                for (int i = 0; i < facJobQty[criticalFac]; i++) {
                    int jobposit = random_1(facJobQty[criticalFac]);
                    int job = (int) jobSeqInfac.get(criticalFac).get(jobposit);
                    int minposi = jobposit;
                    //删
                    jobSeqInfac.get(criticalFac).remove((Integer) job);
                        LinkedList<Integer> jobList_fi = jobSeqInfac.get(n);
                        for (int k = 0; k < jobList_fi.size() + 1; k++) {
                            jobList_fi.add(k, job);
                            int[] jobSeqt = changeList(jobSeqInfac, facJobQty);
                            double tempresult = DHFSP.evaluate_exper(jobSeqt, protime, facJobQty)[1];
                            if (tempresult < minresult) {
                                minresult = tempresult;
                                minposi = k;
                                improve = 0;
                            }
                            jobList_fi.remove(k);
                        }

                    //增
                    if(minposi<jobSeqInfac.get(criticalFac).size()) {
                        jobSeqInfac.get(criticalFac).add(minposi, job);
                    }else jobSeqInfac.get(criticalFac).add(job);
                }
            }
            n++;
        }
        int[] jobSeq_LS3 = changeList(jobSeqInfac,facJobQty);
        int[][]  result = new int[2][];
        result[0] = jobSeq_LS3;
        result[1] = facJobQty;
        return result;
    }

    /**
     * 将jobList转换为int[]
     * @param jobSeqInfac 作业顺序链表
     * @return 作业顺序数组
     */
    public  static int[] changeList(HashMap<Integer, LinkedList> jobSeqInfac,int[] jobQtyInfac){
        int jobQty=0;
        for (int i = 0; i < jobQtyInfac.length; i++) {
            jobQty += jobQtyInfac[i];
        }

        int[] jobseq_new= new int[jobQty];
        int i =0;

            for (int j = 0; j < jobSeqInfac.size(); j++) {
                LinkedList jobList = jobSeqInfac.get(j);
                for (int k = 0; k < jobList.size(); k++) {
                   if(i<jobQty) {
                       jobseq_new[i] = (int) jobList.get(k);
                       i++;
                   }
                }
            }


        return jobseq_new;
    }


        /**
         * 随机生成n个不同的数
         * @param amount 需要的数量
         * @param max 最大值(不含)，例：max为100，则100不能取到，范围为0~99；
         * @return 数组
         */
        public static int[] random(int amount, int max) {
            if (amount > max) { // 需要数字总数必须小于数的最大值，以免死循环！
                throw new ArrayStoreException(
                        "The amount of array element must smallar than the maximum value !");
            }
            int[] array = new int[amount];
            for (int i = 0; i < array.length; i++) {
                array[i] = -1; // 初始化数组，避免后面比对时数组内不能含有0。
            }
            Random random = new Random();
            int num;
            amount -= 1; // 数组下标比数组长度小1
            while (amount >= 0) {
                num = random.nextInt(max);
                if (exist(num, array, amount - 1)) {
                    continue;
                }
                array[amount] = num;
                amount--;
            }
            return array;
        }

        /**
         * 判断随机的数字是否存在数组中
         * @param num 随机生成的数
         * @param array 判断的数组
         * @param need 还需要的个数
         * @return 存在true，不存在false
         */
        private static boolean exist(int num, int[] array, int need) {

            for (int i = array.length - 1; i > need; i--) {// 大于need用于减少循环次数，提高效率。
                if (num == array[i]) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 随机生成一个数
         * @param max 最大值(不含)
         * @return 整型数
         */
        public static int random_1(int max) {

            return random(1, max)[0];
        }

    /** mutation
     * 找出Yi与Yj不相同的位置，逐一交换
     * @param
     * @param protime
     * @param K 变异概率
     * @return
     */
    public  static int[][]  mutation(int[][] Yi,int[][] Yj, double[][] protime,double K) {
        int[] jobSeq=Yi[0].clone();
        int[] facJobQty = Yj[1].clone();
        int jobQty = jobSeq.length;

        //1 随机Ya和Yb,运算得到derta Y
        int[] Ya = Yi[0].clone();
        int[] Yb = Yj[0].clone();
        LinkedList<Integer> Y1 = new LinkedList<>();//derta
        int n=0;
        for (int i = 0; i <jobQty; i++) {
            if(Ya[i]!=Yb[i]){
                Y1.add(i);
            }
        }
       //
        int n1=0;
        int[][] min = new int[2][];
        min[0] = Ya.clone();
        min[1] = Yi[1].clone();
        double minCmax = DHFSP.evaluate_exper(min[0],protime,min[1])[0];
        for (int i = 0; i < Y1.size(); i++) {
            for (int j = 0; j < Y1.size(); j++) {
                int[] tempsol = min[0].clone();
                int temp;
                temp = tempsol[Y1.get(i)];
                tempsol[Y1.get(i)] = tempsol[Y1.get(j)];
                tempsol[Y1.get(j)] = temp;
                double Camc = DHFSP.evaluate_exper(tempsol,protime,min[1])[0];
                if(Camc < minCmax){
                    min[0] =  tempsol;
                    minCmax = Camc;
                }
            }
        }

        int[][]  result = new int[2][];
        result[0] = min[0];
        result[1] = min[1];
        return result;
    }

    /**
     * 将关键工厂中的作业逐一再次插入,若有更优方案则跳出循环再选择关键工厂
     * @param jobSeq 作业的加工顺序
     * @param protime 作业的每阶段的加工时间
     * @param facJobQty 每个工厂中作业的数量，初始的，后续可以改变
     * @return 交换后的解
     */
    public  static int[][] Insert_critical_fac(int[] jobSeq, double[][] protime, int[] facJobQty,double b) {
        int facQty = facJobQty.length;
        int[] facJobQty_t = facJobQty.clone();
        //将关键工厂中的作业逐一再次插入
        //1、找出关键工厂
        int criticalFac = (int) DHFSP.evaluate_exper(jobSeq,protime,facJobQty_t)[3];
        //每个工厂的作业范围
        int[] facJobQty_range = new int[facJobQty_t.length];
        for (int i = 0; i < facJobQty_range.length; i++) {
            if(i==0){
                facJobQty_range[i]= facJobQty_t[i];
            }else
                facJobQty_range[i] =facJobQty_range[i-1] + facJobQty_t[i];
        }

        //将数组变为链表
        //Map<工厂编号，工厂中的作业加工顺序>
        HashMap<Integer, LinkedList> jobSeqInfac= new HashMap<>();
        for (int i = 0; i < facQty; i++) {
            LinkedList<Integer> jobList = new LinkedList<Integer>();
            jobSeqInfac.put(i,jobList);
        }
        //将原来的解数组变为List
        for (int i = 0; i <facJobQty_range.length ; i++) {
            LinkedList<Integer> jobList_i = jobSeqInfac.get(i);
            int j;
            if(i==0){j = 0;}
            else {j = facJobQty_range[i-1];}
            while (j < facJobQty_range[i]){
                jobList_i.add(jobSeq[j]);
                j++;
            }
        }

        double minmakespan = DHFSP.evaluate_exper(jobSeq,protime,facJobQty_t)[0];
//        double minBI = DHFSP.evaluate_exper(jobSeq,protime,facJobQty)[4];
//        double minMBI = b*minmakespan+(1-b)*minBI;

        //将关键工厂的每个位置删除然后重新插入每个工厂的每个位置1
        int n=0;
        while (n<facQty){
            int idx = 0;
            for (int i = 0; i < facJobQty_t[criticalFac]; i++) {
                int job = (int) jobSeqInfac.get(criticalFac).get(i);
                int minfacId = criticalFac;
                int minposi = i;
                //删
                jobSeqInfac.get(criticalFac).remove((Integer)job);
                facJobQty_t[criticalFac] -=1;
                for (int j = 0; j < jobSeqInfac.size(); j++) {
                    LinkedList<Integer> jobList_fi = jobSeqInfac.get(j);
                    facJobQty_t[j] +=1;
                    for (int k = 0; k < jobList_fi.size()+1; k++) {
                        jobList_fi.add(k,job);
                        int[] jobSeqt = changeList(jobSeqInfac,facJobQty_t);
                        double tempresult = DHFSP.evaluate_exper(jobSeqt,protime,facJobQty_t)[0];
//                    double tempBI = DHFSP.evaluate_exper(jobSeqt,protime,facJobQty)[4];
//                    double tempMBI = b*tempresult+(1-b)*tempBI;
                        if(tempresult<minmakespan){
                            minmakespan = tempresult;
                            minfacId = j;
                            minposi = k;
                            idx = 1;
                        }
//                    if(tempMBI<minMBI){
//                        minMBI = tempMBI;
//                        minfacId = j;
//                        minposi = k;
//                    }
                        jobList_fi.remove(k);
                    }
                    facJobQty_t[j] -=1;
                }
                //增
                jobSeqInfac.get(minfacId).add(minposi,(Integer)job);
                facJobQty_t[minfacId] +=1;
                if(idx==1){
                    break;
                }
            }
            n++;
        }


        int[][]  result = new int[2][];
        result[0] = changeList(jobSeqInfac,facJobQty_t);
        result[1] = facJobQty_t;
        double n111 = DHFSP.evaluate_exper(result[0],protime,result[1])[0];

        return result;
    }

    /**
     * 每个作业与所在工厂的其他作业交换
     *A collaborative iterative greedy algorithm for the scheduling of distributed
     * heterogeneous hybrid flow shop with blocking constraints
     * @param jobSeq 作业的加工顺序,传入Random_swap后的作业顺序
     * @param protime 作业的每阶段的加工时间
     * @param facJobQty 每个工厂中作业的数量，初始的，后续可以改变
     * @return 交换后的解
     */
    public  static int[][] swap (int[] jobSeq, double[][] protime, int[] facJobQty,int evaluate) {

        double jobQty = jobSeq.length;
        double facQty = facJobQty.length;

        int[] facJobQty_range = new int[facJobQty.length];
        for (int k = 0; k < facJobQty_range.length; k++) {
            if(k==0){
                facJobQty_range[k]= facJobQty[k];
            }else
                facJobQty_range[k] =facJobQty_range[k-1] + facJobQty[k];
        }
        //1、each工厂
        int[] jobSeq_best = jobSeq.clone();
        for (int i = 0; i < facQty; i++) {
            int end = facJobQty_range[i] - 1;
            int begin = facJobQty_range[i] - facJobQty[i];
            for (int j = begin; j <=end; j++) {
                for (int k = begin; k <=end; k++) {
                    int[] jobSeq_temp1 = jobSeq_best.clone();
                    int temp;
                    temp = jobSeq_temp1[j];
                    jobSeq_temp1[j] = jobSeq_temp1[k];
                    jobSeq_temp1[k] = temp;

                    double b = 0.6;
                    double makespan_init = (1-b)*DHFSP.evaluate_exper(jobSeq_best,protime,facJobQty)[4]+
                            b*DHFSP.evaluate_exper(jobSeq_best,protime,facJobQty)[0];
                    double makespan_change = (1-b)*DHFSP.evaluate_exper(jobSeq_temp1,protime,facJobQty)[4]+
                            b*DHFSP.evaluate_exper(jobSeq_temp1,protime,facJobQty)[0];
                    if(makespan_init>makespan_change){
                        jobSeq_best = jobSeq_temp1;
                    }
                }
            }
        }

        int[][]  result = new int[2][];
        result[0] = jobSeq_best;
        result[1] = facJobQty;
        return result;
    }


}

