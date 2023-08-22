package Problem;

import easyopt.commom.EasyMath;
import easyopt.model.Job;
import easyopt.model.Factory;
import easyopt.model.Mach;

import java.util.*;

/**
 * lfg
 */
public class DHFSP {
    /**
     * 工厂获取作业加工序列
     * @param allJobOder
     * @param facJobQty 每个工厂的作业数量{2，3，3}
     * @return Factory类的动态数组
     */
   public static ArrayList<Factory> evaluate01(List<Job> allJobOder, int[] facJobQty){

        //确定每个工厂中的作业数量和作业编号
       int n = 0;
       ArrayList<Factory> facs=new ArrayList<>();
       for (int i = 0; i < facJobQty.length; i++) {
           int jobQty = facJobQty[i];
           Factory factory = new Factory();
           ArrayList<Job> facjobs=new ArrayList<>();
           for (int j = 0; j < jobQty; j++) {
               Job job;
               job = allJobOder.get(n);
               facjobs.add(job);
               n++;
           }
           factory.setFjobSeq(facjobs);
           facs.add(factory);
       }
       return facs;

   }
    /**
     * 对分布式混合流水问题进行解码
     * @param  jobSeq Job类的List ，初始解
     * @param facJobQty 每个工厂的作业数量
     * @return 分布式的最大完工时间
     */
    public static double evaluate(int[] jobSeq, double[][] protime,int[] facJobQty){
        double[] facCmax_ac = new double[facJobQty.length];
        double[] facCmax_noac = new double[facJobQty.length];

        //实例的数据
        int jobQty = jobSeq.length;
        int proQty = protime.length;


        //初始化作业Job类的List
        List<Job> initJobSeq = new ArrayList<>();
        for (int i = 0; i < jobQty; i++) {
            Job job = new Job();
            job.setJobId(jobSeq[i]);
            //获取作业每阶段的加工时间
            double[] jobproTime = new double[proQty];
            for (int j = 0; j < proQty; j++) {
                jobproTime[j] = protime[j][jobSeq[i]];
            }
            job.setJobProTime(jobproTime);
            //初始化作业最先从0开始
            job.setCurendTime(0);
            double[] n1 = new double[proQty];
            double[] n2 = new double[proQty];
            int[] n3 = new int[proQty];
            job.setJobProStarTime(n1);
            job.setJobProEndTime(n2);
            job.setMachNum(n3);
//            //作业的平均处理时间Ej
//            double Ej = 0;
//            for (int j = 0; j < proQty; j++) {
//                Ej = jobproTime[i]/proQty;
//            }
//            job.setEj(Ej);
            initJobSeq.add(job);
        }

        //每个工厂获取其加工序列
        List<Factory> facSeq=new ArrayList<>();
        facSeq = evaluate01(initJobSeq,facJobQty);
        int facQty = facJobQty.length;

        //工厂i在每阶段的机器安排
        for (int i = 0; i < facQty; i++) {
            facSeq.get(i).setFacId(i);
            //工厂的机器安排（同构工厂）
            int t=0;
            int[][] proMachine = new int[proQty][2];
            for (int j = 0; j < proQty; j++) {
                for (int k = 0; k < 2; k++) {
                    proMachine[j][k] = t++;
                }
            }
            facSeq.get(i).setProMachine(proMachine);
        }

//        //分布式混流
//        for (int i = 0; i < facQty; i++) {
//            System.out.println("-------工厂" + i + "----------" );
//            Factory factory;
//            factory = facSeq.get(i);
//            List<Job> jobseq = factory.getFjobSeq();
//            int[][] machQty = factory.getProMachine();
//            double facEndTime = DHFSP.facevaluate(jobseq,machQty);
//            if(facEndTime > facCmax){
//                facCmax = facEndTime;
//            }
//            factory.setFacCMax(facEndTime);
//            System.out.println("facCmax = " + facEndTime );
//        }

//      //阻塞-分布式混流（活动）
        for (int i = 0; i < facQty; i++) {
//            System.out.println("-------工厂" + i + "----------" );
            Factory factory;
            factory = facSeq.get(i);
            List<Job> jobseq = factory.getFjobSeq();
            int[][] machQty = factory.getProMachine();

            double[] result_blocktime_Cmax = new double[2];
            result_blocktime_Cmax=DHFSP.facevaluate_block_act(jobseq,machQty);
            facCmax_ac[i] = result_blocktime_Cmax[1];
//            System.out.println("facCmax = " + result_blocktime_Cmax[1]);

        }

        //阻塞-分布式混流
        for (int i = 0; i < facQty; i++) {
//            System.out.println("-------工厂" + i + "----------" );
            Factory factory;
            factory = facSeq.get(i);
            List<Job> jobseq = factory.getFjobSeq();
            int[][] machQty = factory.getProMachine();

            double[] result_blocktime_Cmax = new double[2];
            result_blocktime_Cmax=DHFSP.facevaluate_block(jobseq,machQty);
            facCmax_noac[i] = result_blocktime_Cmax[1];
//            System.out.println("facCmax = " + result_blocktime_Cmax[1]);
        }

        double makespan_ac = 0;
        for (int i = 0; i < facQty; i++) {
            if(makespan_ac<facCmax_ac[i]){
                makespan_ac = facCmax_ac[i];
            }
        }

        double makespan_noac = 0;
        for (int i = 0; i < facQty; i++) {
            if(makespan_noac<facCmax_noac[i]){
                makespan_noac = facCmax_noac[i];
            }
        }

        return makespan_noac;
    }

    /**
     * 对分布式混合流水问题进行解码（活动+非活动）
     * @param  jobSeq Job类的List ，初始解
     * @param facJobQty 每个工厂的作业数量
     * @return 分布式的最大完工时间  result_makespan[0] - 活动 ; result_makespan[1] - 非活动;
     * result_makespan[2] - 活动的关键工厂 ;  result_makespan[3] - 非活动的关键工厂；result_makespan[4] 最大的阻塞时间与空闲时间之和;
     */
    public static double[] evaluate_exper(int[] jobSeq, double[][] protime,int[] facJobQty){
        double[] facCmax_ac = new double[facJobQty.length];
        double[] facBloc_ac = new double[facJobQty.length];
        double[] facIdle_ac = new double[facJobQty.length];
        double[] facCmax_noac = new double[facJobQty.length];

        //实例的数据
        int jobQty = jobSeq.length;
        int proQty = protime.length;
        //初始化作业Job类的List
        List<Job> initJobSeq = new ArrayList<>();
        for (int i = 0; i < jobQty; i++) {
            Job job = new Job();
            job.setJobId(jobSeq[i]);
            //获取作业每阶段的加工时间
            double[] jobproTime = new double[proQty];
            for (int j = 0; j < proQty; j++) {
                jobproTime[j] = protime[j][jobSeq[i]];
            }
            job.setJobProTime(jobproTime);
            //初始化作业最先从0开始
            job.setCurendTime(0);
            double[] n1 = new double[proQty];
            double[] n2 = new double[proQty];
            int[] n3 = new int[proQty];
            job.setJobProStarTime(n1);
            job.setJobProEndTime(n2);
            job.setMachNum(n3);
//            //作业的平均处理时间Ej
//            double Ej = 0;
//            for (int j = 0; j < proQty; j++) {
//                Ej = jobproTime[i]/proQty;
//            }
//            job.setEj(Ej);
            initJobSeq.add(job);
        }

        //每个工厂获取其加工序列
        List<Factory> facSeq=new ArrayList<>();
        facSeq = evaluate01(initJobSeq,facJobQty);
        int facQty = facJobQty.length;

        //工厂i在每阶段的机器安排
        for (int i = 0; i < facQty; i++) {
            facSeq.get(i).setFacId(i);
            //工厂的机器安排（同构工厂）
            int t=0;
            int[][] proMachine = new int[proQty][2];
            for (int j = 0; j < proQty; j++) {
                for (int k = 0; k < 2; k++) {
                    proMachine[j][k] = t++;
                }
            }
            facSeq.get(i).setProMachine(proMachine);
        }

//        //分布式混流
//        for (int i = 0; i < facQty; i++) {
//            System.out.println("-------工厂" + i + "----------" );
//            Factory factory;
//            factory = facSeq.get(i);
//            List<Job> jobseq = factory.getFjobSeq();
//            int[][] machQty = factory.getProMachine();
//            double facEndTime = DHFSP.facevaluate(jobseq,machQty);
//            if(facEndTime > facCmax){
//                facCmax = facEndTime;
//            }
//            factory.setFacCMax(facEndTime);
//            System.out.println("facCmax = " + facEndTime );
//        }

//      //阻塞-分布式混流（活动）
        for (int i = 0; i < facQty; i++) {
//            System.out.println("-------工厂" + i + "----------" );
            Factory factory;
            factory = facSeq.get(i);
            List<Job> jobseq = factory.getFjobSeq();
            int[][] machQty = factory.getProMachine();

            double[] resultact_blocktime_Cmax = new double[2];
            resultact_blocktime_Cmax=DHFSP.facevaluate_block_act(jobseq,machQty);
            facCmax_ac[i] = resultact_blocktime_Cmax[1];
            facBloc_ac[i] = resultact_blocktime_Cmax[0];
            facIdle_ac[i] = resultact_blocktime_Cmax[2];

//            System.out.println("facCmax = " + resultact_blocktime_Cmax[1]);

        }

        //阻塞-分布式混流
        for (int i = 0; i < facQty; i++) {
//            System.out.println("-------工厂" + i + "----------" );
            Factory factory;
            factory = facSeq.get(i);
            List<Job> jobseq = factory.getFjobSeq();
            int[][] machQty = factory.getProMachine();

            double[] result_blocktime_Cmax=DHFSP.facevaluate_block(jobseq,machQty);

            facCmax_noac[i] = result_blocktime_Cmax[1];
//            System.out.println("facCmax = " + result_blocktime_Cmax[1]);
        }

        //关键时间和关键工厂
        double makespan_ac = 0;
        int criticalFac_act = 0;
        for (int i = 0; i < facQty; i++) {
            if(makespan_ac<facCmax_ac[i]){
                makespan_ac = facCmax_ac[i];
                criticalFac_act = i;
            }
        }
        double makespan_noac = 0;
        int criticalFac_noact = 0;
        for (int i = 0; i < facQty; i++) {
            if(makespan_noac<facCmax_noac[i]){
                makespan_noac = facCmax_noac[i];
                criticalFac_noact = i;
            }
        }

        //活动,阻塞时间与空闲时间之和最大的值
        double blockaddidle_critiTime_ac = facIdle_ac[0]+facBloc_ac[0];
        for (int i = 0; i < facQty; i++) {
            if(blockaddidle_critiTime_ac<facIdle_ac[i]+facBloc_ac[i]){
                blockaddidle_critiTime_ac = facIdle_ac[i]+facBloc_ac[i];
            }
        }

        double[] result_makespan = new double[5];
        result_makespan[0] = makespan_ac;
        result_makespan[1] = makespan_noac;
        result_makespan[2] = criticalFac_act;
        result_makespan[3] = criticalFac_noact;
        result_makespan[4] = blockaddidle_critiTime_ac;
        return result_makespan;
    }


    /** 对当前的工厂进行解码，混合流水
     * @return 工厂的最大完工时间
     * @param jobSeq Job类的动态数组
     * @param machQty 每阶段的机器，二维数组，i阶段的所有机器编号
     */
    public static double facevaluate(List<Job> jobSeq,int[][] machQty){
        //机器总数
        int MachQty = 0;
        for (int j = 0; j < machQty.length; j++) {
                MachQty += machQty[j].length;
            }
        //当前机器上作业的加工完成时间，idx伟工厂的机器编号
        double[] machPreEndTime = new double[MachQty];
        double facEndTime = 0;
        //按照阶段顺序解码
        for (int i = 0; i < machQty.length; i++) {
            //按照本阶段的工序加工
            List<Job> machingSeq = jobSeq;
            for (int j = 0; j < machingSeq.size(); j++) {
                Job job = machingSeq.get(j);
                double protime = job.getJobProTime()[i];
                double jobCurEndTime = job.getCurendTime();
                double[] jobProStarTime = job.getJobProStarTime().clone();
                double[] jobProEndTime = job.getJobProEndTime().clone();
                int[] jobProMach = job.getMachNum().clone();

                //机器选择，选出当前阶段最先空闲机器
                int machId = selectMachine(machPreEndTime,machQty[i]);
                //确定作业开工时间
                double jobStarTime = Math.max(machPreEndTime[machId],jobCurEndTime);
                double jobEndTime = jobStarTime + protime;

                //更新机的当前加工完成时间和作业的当前加工完成时间
                machPreEndTime[machId] = jobEndTime;
                job.setCurendTime(jobEndTime);

                //获取作业的开工和完工时间、该阶段的机器选择
                jobProStarTime[i] = jobStarTime;
                jobProEndTime[i] = jobEndTime;
                jobProMach[i] = machId;
                job.setJobProStarTime(jobProStarTime);
                job.setJobProEndTime(jobProEndTime);
                job.setMachNum(jobProMach);
            }
            //更新下一阶段的加工序列，按照作业的完成时间升序排序
            machingSeq.sort(Comparator.comparingDouble(Job::getCurendTime));
            //获取当前最后一个阶段最后一个作业的加工完成时间
            if(i == machQty.length - 1){
                facEndTime = machingSeq.get(machingSeq.size()-1).getJobProEndTime()[i];
            }
        }
        return facEndTime;
    }

    /**
     * 当前工厂的解码，带阻塞约束的混合流水的解码（活动解码）
     * @param jobSeq 作业的加工顺序 List
     * @param machNum 每个阶段的机器安排，数组内元素是机器编号，按阶段顺序给机器编号，从0开始
     * @return result[0] 总阻塞时间; result[1] = makespan;result[2] = 总空闲时间;
     */
    public static double[] facevaluate_block_act(List<Job> jobSeq,int[][] machNum){
        double Cmac_fac = 0;
        int jobQty = jobSeq.size();
        int proQty = machNum.length;
        int machQty = 0;
        for (int i = 0; i < machNum.length; i++) {
            machQty += machNum[i].length;
        }

        //初始化机器集合
        HashMap<Integer,Mach> machHashMap= new HashMap<>();
        for (int i = 0; i < machQty; i++) {
            Mach mach = new Mach();
            LinkedList<double[]> idleTimeList = new LinkedList<>();
            double[] idleTimePart = {0,483648};
            idleTimeList.add(idleTimePart);
            mach.setIdleTimeList(idleTimeList);
            machHashMap.put(i,mach);
        }

        //每个机器的阻塞时间
        double[] mach_blockTime = new double[machQty];

        //按照作业顺序逐一解码
        for (int i = 0; i < jobQty; i++) {

            //初始化
            Job job = jobSeq.get(i);
            double[] jobProTime = job.getJobProTime().clone();
            double[] jobProStarTime = job.getJobProStarTime().clone();
            double[] jobProEndTime = job.getJobProEndTime().clone();
            int[] jobProMach = job.getMachNum().clone();


            //在第一阶段 ProNum=0
            int proId = 0;
            if(proId == 0){
                //machines in process 1
                int[]  proMachNum = machNum[0];
                //机器选择  0:机器编号 1：开始时间
                double[] minresult = DHFSP.selectMach_pro0(machHashMap,proMachNum);
                //更新Job
                jobProMach[0] = (int) minresult[0];
                jobProStarTime[0] = minresult[1];
                jobProEndTime[0] = jobProStarTime[0]+ jobProTime[0];
                //更新Mach
                Mach mach = new Mach();
                mach = (Mach) machHashMap.get(jobProMach[0]);
                LinkedList<double[]> machIdleTimeList;
                machIdleTimeList = mach.getIdleTimeList();
                //第一次修改，若后续有阻塞这继续修改
                machIdleTimeList.get(0)[0] =  jobProEndTime[0];
            }

            // 后续阶段机器选择
            // result_mach_idx: [0] : ST , [1]:MachId , [2]:Idx 483648为最后一个part
            double[][] result_mach_idx = new double[3][proQty-1];
            result_mach_idx = DHFSP.selectMach_pro(machHashMap,
                    machNum,i,jobProEndTime[0],jobProTime);
            //job插入的位置
            double[] machInsertIdx = new double[proQty];
            machInsertIdx[0] =0;

            //更新job
            for (int j = 1; j < proQty; j++) {
                jobProStarTime[j] = result_mach_idx[0][j-1];
                jobProEndTime[j] = result_mach_idx[0][j-1] + jobProTime[j];
                jobProMach[j] = (int)result_mach_idx[1][j-1];
                machInsertIdx[j] = result_mach_idx[2][j-1];
                job.setJobProStarTime(jobProStarTime);
                job.setJobProEndTime(jobProEndTime);
                job.setMachNum(jobProMach);
            }
            //后续阶段，更新job/mach，
            for (int j = 1; j < proQty; j++) {
                //更新Mach
                //第一阶段的第二次修改,发生阻塞
                if(j==1&&jobProStarTime[j]-jobProEndTime[j-1]>0){
                    //第一阶段阻塞
                    Mach mach = new Mach();
                    mach = (Mach) machHashMap.get(jobProMach[0]);
                    LinkedList<double[]> machIdleTimeList;
                    machIdleTimeList = mach.getIdleTimeList();
                    //第一次修改，若后续有阻塞这继续修改
                    machIdleTimeList.get(0)[0] = jobProStarTime[j];
                    //机器阻塞时间
                    double blockTime = mach.getBlockTime();
                    blockTime += jobProStarTime[j]-jobProEndTime[j-1];
                    mach.setBlockTime(blockTime);
                    //机器加工时间
                    double proTime = mach.getProTime();
                    proTime += jobProTime[j-1];
                    mach.setBlockTime(proTime);
                }
                //后续阶段更新Mach
                Mach mach = new Mach();
                mach = (Mach) machHashMap.get(jobProMach[j]);
                LinkedList<double[]> machIdleTimeList;
                machIdleTimeList = mach.getIdleTimeList();
                double[] idlePart;
                //插入位置是否为最后一个part
                if(machInsertIdx[j]!= 483648){
                    idlePart = machIdleTimeList.get((int)machInsertIdx[j]);
                }
                else idlePart = machIdleTimeList.getLast();
                //插入后是否要加入一个part
                //需要插入一个part
                if(jobProStarTime[j]>idlePart[0]&&jobProEndTime[j]<idlePart[1]){

                    double[] insert_part = new double[2];
                    insert_part[0] = idlePart[0];
                    insert_part[1] = jobProStarTime[j];

                    if(j!=proQty-1){
                        idlePart[0] = jobProStarTime[j+1];
                    }else
                        idlePart[0] = jobProEndTime[j];
                    //插入到List
                    if(machInsertIdx[j]!= 483648){
                        machIdleTimeList.add((int)machInsertIdx[j],insert_part);
                    }
                    else machIdleTimeList.addLast(insert_part);

                }
                //不需要插入
                else if(jobProStarTime[j]==idlePart[0]){
                    if(j!=proQty-1){
                        idlePart[0] = jobProStarTime[j+1];
                    }else
                        idlePart[0] = jobProEndTime[j];
                }
                else if (jobProEndTime[j]==idlePart[1]) {
                    idlePart[1] = jobProStarTime[j];
                }
                else System.out.println("insert error! ");

                //计算作业在该阶段的机器上是否阻塞，并累计该机器的阻塞时间
                if(j<proQty-1&&jobProEndTime[j]!=jobProStarTime[j+1]){
                    //存在阻塞的情况
                    double blockTime = mach.getBlockTime();
                    blockTime =blockTime + jobProStarTime[j+1]-jobProEndTime[j];
                    mach.setBlockTime(blockTime);
                    mach_blockTime[jobProMach[j]] += blockTime;
                }

            }

            //计算工厂的总加工完成时间
            if(jobProEndTime[proQty-1]>Cmac_fac){
                Cmac_fac=jobProEndTime[proQty-1];
            }

        }
        //计算总的阻塞时间
        double total_blockTime = 0;
        for (int i = 0; i < machQty; i++) {
            total_blockTime += mach_blockTime[i];
        }
        //计算总空闲时间，将每台机器的空闲时间List的前t-1个片段相加
        double tatal_idleTime = 0;
        for (int i = 0; i < machHashMap.size(); i++) {
            Mach mach = machHashMap.get(i);
            LinkedList<double[]> idleTimeList = mach.getIdleTimeList();
            for (int j = 0; j < idleTimeList.size()-1; j++) {
                double[] idlePart = idleTimeList.get(j);
                tatal_idleTime += idlePart[1]-idlePart[0];
            }
        }
        
        double[] blocktime_result_idle = new double[3];
        blocktime_result_idle[0] = total_blockTime;
        blocktime_result_idle[1] = Cmac_fac;
        blocktime_result_idle[2] = tatal_idleTime;
        return blocktime_result_idle ;

    }

    /**
     * 分布式-阻塞约束-混合流水的基本解码
     * @param jobSeq 作业的加工顺序
     * @param machNum 工厂每个阶段的机器集
     * @return result[0] 总阻塞时间;result[1] = makespan;
     */
    public static double[] facevaluate_block(List<Job> jobSeq, int[][] machNum){
        double Cmac_fac = 0;
        int jobQty = jobSeq.size();
        int proQty = machNum.length;
        int machQty = 0;
        for (int i = 0; i < machNum.length; i++) {
            machQty += machNum[i].length;
        }
        //每个机器的阻塞时间
        double[] mach_blockTime = new double[machQty];

        //初始化机器集合
        HashMap<Integer,Mach> machHashMap= new HashMap<>();
        for (int i = 0; i < machQty; i++) {
            Mach mach = new Mach();
            double reTime = 0;
            double blocktime = 0;
            mach.setBlockTime(blocktime);
            mach.setReTime(reTime);
            machHashMap.put(i,mach);
        }

        for (int i = 0; i < jobQty; i++) {

            //初始化
            Job job = jobSeq.get(i);
            double[] jobProTime = job.getJobProTime().clone();
            double[] jobProStarTime = job.getJobProStarTime().clone();
            double[] jobProEndTime = job.getJobProEndTime().clone();
            int[] jobProMach = job.getMachNum().clone();


            for (int j = 0; j < proQty; j++) {

                int[] proMachId;
                proMachId = machNum[j];
                double minST =483648 ;
                int minMachId = proMachId[0];

                //机器选择
                for (int k = 0; k < proMachId.length; k++) {
                    int MachId = proMachId[k];
                    Mach mach = new Mach();
                    mach = (Mach) machHashMap.get(MachId);
                    double retime  = mach.getReTime();
                    if(retime<minST){
                        minST = retime;
                        minMachId = proMachId[k];
                    }
                }
                //更新job
                if(j==0){
                    jobProStarTime[j] = Math.max(jobProStarTime[0],minST);

                }else{
                    jobProStarTime[j] = Math.max(jobProEndTime[j-1],minST);

                }
                jobProEndTime[j] = jobProStarTime[j] + jobProTime[j];
                jobProMach[j] = minMachId;
                //判断上一阶段是否阻塞
                if(j>0&&jobProStarTime[j]>jobProEndTime[j-1]){
                    //上一阶段有阻塞,更新上一段所在机器的retime
                    int MachId = jobProMach[j-1];
                    Mach mach = new Mach();
                    mach = (Mach) machHashMap.get(MachId);
                    double blocktime = mach.getBlockTime();
                    blocktime =blocktime + jobProStarTime[j]-jobProEndTime[j-1];
                    double retime = jobProStarTime[j];
                    mach.setBlockTime(blocktime);
                    mach.setReTime(retime);
                    mach_blockTime[MachId]= blocktime;
                }

                //更新当前Mach
                int MachId = jobProMach[j];
                Mach mach = new Mach();
                mach = (Mach) machHashMap.get(MachId);
                double retime = jobProEndTime[j];
                mach.setReTime(retime);
            }
            //传入Job
            job.setJobProStarTime(jobProStarTime);
            job.setJobProEndTime(jobProEndTime);
            job.setMachNum(jobProMach);


            //计算Cmax
            if(Cmac_fac < jobProEndTime[proQty-1]){
                Cmac_fac=jobProEndTime[proQty-1];
            }
        }
        //计算总的阻塞时间
        double total_blockTime = 0;
        for (int i = 0; i < machQty; i++) {
            total_blockTime += mach_blockTime[i];
        }

        double[] result = new double[3];
        result[0] = total_blockTime;
        result[1] = Cmac_fac;
        return result;
    }

    /**
     * 一个工厂阻塞约束-混合流水的基本解码
     * @param jobList 作业的加工顺序
     * @param machNum 工厂每个阶段的机器集
     * @return result[0] 总阻塞时间;result[1] = makespan;result[2] = cost function;
     */
    public static double[] evaluate_block(LinkedList jobList, int[][] machNum,double[][] ProTime,double initb){
        List<Job> jobSeq = new ArrayList<>();
        for (int i = 0; i < jobList.size(); i++) {
            int jobID = (int)jobList.get(i);
            Job job = new Job();
            job.setJobId(jobID);
            jobSeq.add(job);
        }

        double Cmac_fac = 0;
        int jobQty = jobSeq.size();
        int proQty = machNum.length;
        int machQty = 0;
        for (int i = 0; i < machNum.length; i++) {
            machQty += machNum[i].length;
        }
        //每个机器的阻塞时间
        double[] mach_blockTime = new double[machQty];

        //初始化机器集合
        HashMap<Integer,Mach> machHashMap= new HashMap<>();
        for (int i = 0; i < machQty; i++) {
            Mach mach = new Mach();
            double reTime = 0;
            double blocktime = 0;
            mach.setBlockTime(blocktime);
            mach.setReTime(reTime);
            machHashMap.put(i,mach);
        }


        for (int i = 0; i < jobQty; i++) {

            double[] jobProTime=new double[proQty];
            for (int j = 0; j < proQty; j++) {
                jobProTime[j] = ProTime[j][i];
            }
            //初始化
            Job job = jobSeq.get(i);
            job.setJobProTime(jobProTime);
            double[] jobProStarTime = new double[proQty]; job.setJobProStarTime(jobProStarTime);
            double[] jobProEndTime = new double[proQty]; job.setJobProEndTime(jobProEndTime);
            double[] idleTime = new double[proQty]; job.setIdleTime(idleTime);
            int[] jobProMach = new int[proQty]; job.setMachNum(jobProMach);


            for (int j = 0; j < proQty; j++) {

                int[] proMachId;
                proMachId = machNum[j];
                double minST =483648 ;
                int minMachId = proMachId[0];

                //机器选择
                for (int k = 0; k < proMachId.length; k++) {
                    int MachId = proMachId[k];
                    Mach mach = new Mach();
                    mach = (Mach) machHashMap.get(MachId);
                    double retime  = mach.getReTime();
                    if(retime<minST){
                        minST = retime;
                        minMachId = proMachId[k];
                    }
                }
                //更新job
                if(j==0){
                    jobProStarTime[j] = Math.max(jobProStarTime[0],minST);

                }else{
                    jobProStarTime[j] = Math.max(jobProEndTime[j-1],minST);

                }
                idleTime[j] = jobProStarTime[j]-minST;
                jobProEndTime[j] = jobProStarTime[j] + jobProTime[j];
                jobProMach[j] = minMachId;
                //判断上一阶段是否阻塞
                if(j>0&&jobProStarTime[j]>jobProEndTime[j-1]){
                    //上一阶段有阻塞,更新上一段所在机器的retime
                    int MachId = jobProMach[j-1];
                    Mach mach = new Mach();
                    mach = (Mach) machHashMap.get(MachId);
                    double blocktime = mach.getBlockTime();
                    blocktime =blocktime + jobProStarTime[j]-jobProEndTime[j-1];
                    double retime = jobProStarTime[j];
                    mach.setBlockTime(blocktime);
                    mach.setReTime(retime);
                    mach_blockTime[MachId]= blocktime;
                }

                //更新当前Mach
                int MachId = jobProMach[j];
                Mach mach = new Mach();
                mach = (Mach) machHashMap.get(MachId);
                double retime = jobProEndTime[j];
                mach.setReTime(retime);
            }
            //传入Job
            job.setJobProStarTime(jobProStarTime);
            job.setJobProEndTime(jobProEndTime);
            job.setMachNum(jobProMach);
            job.setIdleTime(idleTime);

            //计算Cmax
            if(Cmac_fac < jobProEndTime[proQty-1]){
                Cmac_fac=jobProEndTime[proQty-1];
            }
        }
        //计算总的阻塞时间
        double total_blockTime = 0;
        for (int i = 0; i < machQty; i++) {
            total_blockTime += mach_blockTime[i];
        }

        //计算cost function
        Job job = jobSeq.get(jobQty-1);
        double[] idtime = job.getIdleTime();
        double[] protime = job.getJobProTime();
        double cost = 0;
        for (int i = 0; i < proQty; i++) {
            cost+=initb*idtime[i];
        }
        cost += (1-initb)*(idtime[proQty-1]+protime[proQty-1]);

        double[] result = new double[3];
        result[0] = total_blockTime;
        result[1] = Cmac_fac;
        result[2] = cost;
        return result;
    }

    /**
     * 作业除第一阶段外，后续每个阶段的机器选择，作业选择最先允许加工的机器（可向前面空闲的时间段插入），
     * 若当前阶段向前插入会使上阶段的阻塞时间超过对应的时间段则放弃插入，改作业的所有阶段都选择机器的最后一个片段（选最先开始）
     * @param machHashMap 工厂的机器集 <机器编号，Mach>
     * @param machNum  每阶段的机器分配
     * @param jobId 当前进行机器选择的作业编号
     * @param pro1_et 作业在第一阶段的加工完成时间
     * @param jobprotime 作业每阶段的加工时间
     * @return  该作业除第一阶段外，每个阶段的机器选择，[0]：作业的开始加工时间 [1]:MachId , [2]:Idx 483648为最后一个part
     */
    public static double[][] selectMach_pro(HashMap machHashMap,int[][] machNum,
                                            int jobId, double pro1_et,double[] jobprotime){
        int proNum_part = machNum.length - 1;
        //每阶段的机器
        double[] minMachId = new double[proNum_part];
        for (int i = 0; i < proNum_part; i++) {
            minMachId[i] = machNum[i+1][0];
        }

        double[] Idx = new double[proNum_part];
        Idx[0] = 0;

        //job在每个阶段的开始加工时间、加工完成时间
        double[] jobprost = new double[proNum_part+1];
        double[] jobproet = new double[proNum_part+1];
        jobproet[0] = pro1_et;
        
        //是否每个阶段都满足插入的情况
        int sign = 0;

        for (int i = 1; i < proNum_part+1; i++) {
            //该阶段的可选机器
            int[] proMachNum = machNum[i];
            int[] proMachNum_pre = machNum[i-1];

            double minStarTime =483648;
            //机器选择
            for (int j = 0; j < proMachNum.length; j++) {
                int machId = proMachNum[j];
                Mach mach = new Mach();
                mach = (Mach) machHashMap.get(machId);
                LinkedList<double[]> idleTimeList;
                idleTimeList = mach.getIdleTimeList();
                double ST = 0;
                int idx_temp = 0;
                //遍历该机器的idleList
                for (int k = 0; k < idleTimeList.size(); k++) {
                     //1:可以插入
                    if(Math.max(jobproet[i-1],idleTimeList.get(k)[0])
                                    +jobprotime[i]<idleTimeList.get(k)[1]){
                         ST =Math.max(jobproet[i-1],idleTimeList.get(k)[0]) ;
                         idx_temp = k;
                         break;
                    }
                }
                if(ST<minStarTime){
                    minStarTime = ST;
                    minMachId[i-1] = proMachNum[j];
                    Idx[i-1] = idx_temp;
                }
            }
            //更新job
            jobprost[i] = minStarTime;
            jobproet[i] = minStarTime+jobprotime[i];
            
            //检测上一阶段的插入是否成立（若造成阻塞，阻塞部分是否是空的）
            if(i>1) {
                Mach mach = new Mach();
                mach = (Mach) machHashMap.get((int) minMachId[i - 1]);
                LinkedList<double[]> idleTimeList_pro;
                idleTimeList_pro = mach.getIdleTimeList();
                double[] idle_part = idleTimeList_pro.get((int) Idx[i - 1]);
                //若该阶段确定开始时间后，会导致上阶段的插入失败，即阻塞部分超过空闲时间
                if (jobprost[i] > idle_part[1]) {
                    //这一作业不向前插入，重新选择，在机器的最后一个idlepart
                    sign = 1;
                    break;
                }
            }
        }
        
        //若不满足插入的情况 sign=1
        if(sign==1) {
            for (int i = 1; i < proNum_part + 1; i++) {
                int[] proMachNum = machNum[i];
                double minStarTime = 483648;
                int machid = 0;
                for (int j = 0; j < proMachNum.length; j++) {
                    int machId = proMachNum[j];
                    Mach mach = new Mach();
                    mach = (Mach) machHashMap.get(machId);
                    LinkedList<double[]> idleTimeList;
                    idleTimeList = mach.getIdleTimeList();
                    double[] idle_part = idleTimeList.getLast();
                    double ST = Math.max(idle_part[0], jobproet[i - 1]);
                    if (ST < minStarTime) {
                        minStarTime = ST;
                        machid = proMachNum[j];
                    }
                }
                //在最后一个part插入
                jobprost[i] = minStarTime;
                jobproet[i] = minStarTime + jobprotime[i];
                minMachId[i - 1] = machid;
                Idx[i - 1] = 483648;
            }
        }
        double[][] result = new double[3][proNum_part];
        double[] t = new double[proNum_part];
        for (int i = 0; i < proNum_part; i++) {
            t[i] = jobprost[i+1];
        }
        result[0] = t;
        result[1] = minMachId;
        result[2] = Idx;
        return result;
    }

    /**
     * 第一阶段的机器选择，优先选择最先加工的机器，若开工时间相同选序号小的
     * @param machHashMap <machId,Mach>
     * @param proMachNum 第一阶段的加工时间
     * @return double[0]机器编号,double[1]开始时间
     */
    public static double[] selectMach_pro0(HashMap machHashMap,int[] proMachNum){
        //遍历每个机器的idleTimeList
        int minMachId = proMachNum[0];
        double minStarTime = 483648;

        for (int i = 0; i < proMachNum.length; i++) {
            int machId = proMachNum[i];
            Mach mach = new Mach();
            mach = (Mach) machHashMap.get(machId);
            LinkedList<double[]> idleTimeList;
            idleTimeList = mach.getIdleTimeList();
            double idlePartST = (idleTimeList.get(0))[0];
            if(idlePartST<minStarTime){
                minStarTime = idlePartST;
                minMachId = machId;
            }
        }
        double[] minresult = new double[2];
        minresult[0] = minMachId;
        minresult[1] = minStarTime;
        return minresult;
    }


    /**
     * 选出当前阶段最先空闲的机器,混合流水
     * @param machPreEndTime 当前所有机器的完工时间
     * @param proNum 当前阶段的所有机器编号
     * @return 空闲机器的编号
     */
    public static int selectMachine(double[] machPreEndTime,int[] proNum){
        int minIdx = proNum[0];
        double minTime = machPreEndTime[proNum[0]];

        for (int i = proNum[0]; i < proNum.length + proNum[0]; i++) {
            if(machPreEndTime[i] < minTime){
                minIdx = i;
                minTime = machPreEndTime[i];
            }
        }
        return minIdx;
    }


}
