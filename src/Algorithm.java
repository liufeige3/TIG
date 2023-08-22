import Problem.DHFSP;
import easyopt.commom.CreateExcel;
import easyopt.commom.GA;
import easyopt.commom.PSO;
import jxl.write.WriteException;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class Algorithm {

    /**
     * IG
     * @param jobSeq 作业编号排序
     * @param protime 作业的加工时间
     * @param facJobQty 每个工厂中作业的数量
     * @param d 破坏个数
     * @param evaluate
     * @return
     */
    public  static double BasicIG(int[] jobSeq, double[][] protime, int[] facJobQty ,double d,int evaluate){

        //1、NEH初始化
        int[][] jobSeq_NEH_result;
        //result[0]: NEH2初始化后的作业加工顺序  result[1] :每个工厂中的作业数量
        jobSeq_NEH_result = operator.NEH2_des(jobSeq,protime,facJobQty,evaluate);
        int[] jobSeq_NEH = jobSeq_NEH_result[0];
        int[] jobQty_eachfac = jobSeq_NEH_result[1];

        //2、记录初始解
        int[][] jobSeq_best = new int[2][];
        jobSeq_best[0] = jobSeq_NEH.clone();
        jobSeq_best[1] = jobQty_eachfac.clone();
        double result_best = DHFSP.evaluate_exper(jobSeq_best[0],protime,jobSeq_best[1])[evaluate];
        //3、进入迭代
        long startTime = System.currentTimeMillis();
        long endTime = startTime + 10* jobSeq.length* protime.length* facJobQty.length;
        while (System.currentTimeMillis() < endTime){
            int[][] jobSeq_temp = new int[2][];
            jobSeq_temp[0] = jobSeq_NEH.clone();
            jobSeq_temp[1] = jobQty_eachfac.clone();

            //3.1 破坏，随机地选择d个作业删除
            int[][] jobSeq_Des_result = operator.Destruction(jobSeq_temp[0],protime,jobSeq_temp[1],d);

            //3、2 重组，将被删除的d个作业贪婪插入
            int[][] jobSeq_Con_result = operator.Construction(jobSeq_Des_result[0],protime,
                    jobSeq_Des_result[1],jobSeq_Des_result[2],evaluate);

            //4、模拟退火
            double result_temp = DHFSP.evaluate_exper(jobSeq_Con_result[0],protime,jobSeq_Con_result[1])[evaluate];
            double facQty = facJobQty.length;
            double machQty = protime.length*2;
            double jobQty = jobSeq.length;
            double T=0.8;
            double SA_Temper = operator.SA(result_best,result_temp,protime,
                    facQty,machQty,jobQty,T);
            double randonNum = Math.random();
            if(result_temp < result_best){
                jobSeq_best = jobSeq_Con_result;
                result_best = result_temp;
            }else if(randonNum < SA_Temper && result_temp >= result_best){
                jobSeq_best = jobSeq_Con_result;
            }

//            System.out.println("makespan " + result_best);
        }
        int[][] jobSeq_IG_result = new int[2][];
        jobSeq_IG_result[0] = jobSeq_best[0].clone();
        jobSeq_IG_result[1] = jobSeq_best[1].clone();
        return result_best;
    }

    /**
     * BIG
     * @param jobSeq 作业编号排序
     * @param protime 作业的加工时间
     * @param facJobQty 每个工厂中作业的数量
     * @param d 破坏个数
     * @return
     */
    public  static double BIG(int[] jobSeq, double[][] protime, int[] facJobQty ,double d,double a,double b){

        //1、NEH初始化
        int[][] jobSeq_NEH_result = operator.NEH2_enb(jobSeq,protime,facJobQty,0,b);
        int[][] NEH_des_block = operator.NEH_des_block(jobSeq,protime,facJobQty,a,0);
        //result[0]: NEH2初始化后的作业加工顺序  result[1] :每个工厂中的作业数量

        //2、记录初始解
        int[][] jobSeq_best = new int[2][];
        jobSeq_best[0]= NEH_des_block[0].clone();
        jobSeq_best[1] = NEH_des_block[1].clone();

        int[][] Y1 = NEH_des_block;
        int[][] Y2 = jobSeq_NEH_result;
        //3、进入迭代
        long startTime = System.currentTimeMillis();
        long endTime = startTime + 10 * jobSeq.length* protime.length* facJobQty.length;
        int InterNum = 0;
        while (System.currentTimeMillis() < endTime){

            //全局搜索
            int[][] DE_Mutation = operator.mutation(Y1,Y2,protime,0.5); //act
            //3.1 破坏，随机地选择d个作业删除
            int[][] jobSeq_Des_y1 = operator.Destruction(Y1[0],protime,Y1[1],d);
            int[][] jobSeq_Des_y2 = operator.Destruction(Y2[0],protime,Y2[1],d);
            int[][] jobSeq_Des_y3 = operator.Destruction(DE_Mutation[0],protime,DE_Mutation[1],d);


            //3、2 重组，将被删除的d个作业贪婪插入
            int[][] jobSeq_Con_y1 = operator.Construction1(jobSeq_Des_y1[0],protime,jobSeq_Des_y1[1],jobSeq_Des_y1[2],0);
            int[][] jobSeq_Con_y2 = operator.Construction1(jobSeq_Des_y2[0],protime,jobSeq_Des_y2[1],jobSeq_Des_y2[2],0);
            int[][] jobSeq_Con_y3 = operator.Construction1(jobSeq_Des_y3[0],protime,jobSeq_Des_y3[1],jobSeq_Des_y3[2],0);


            //局部搜索
            int[][] jobSeq_ls1_y1 = operator.Insert_critical_fac(jobSeq_Con_y1[0],protime,jobSeq_Con_y1[1],1);
            int[][] jobSeq_ls1_y2 = operator.Insert_critical_fac(jobSeq_Con_y2[0],protime,jobSeq_Con_y2[1],1);
            int[][] jobSeq_ls1_y3 = operator.Insert_critical_fac(jobSeq_Con_y3[0],protime,jobSeq_Con_y3[1],1);



            int[][] jobSeq_ls2_y1 = operator.Sequential_swap(jobSeq_ls1_y1[0],protime,jobSeq_ls1_y1[1],0);
            int[][] jobSeq_ls2_y2 = operator.Sequential_swap(jobSeq_ls1_y2[0],protime,jobSeq_ls1_y2[1],0);
            int[][] jobSeq_ls2_y3 = operator.Sequential_swap(jobSeq_ls1_y3[0],protime,jobSeq_ls1_y3[1],0);

            //4 比较1，2
            double result1 = DHFSP.evaluate_exper(jobSeq_ls2_y1[0],protime,jobSeq_ls2_y1[1])[0];
            double result2 = DHFSP.evaluate_exper(jobSeq_ls2_y2[0],protime,jobSeq_ls2_y2[1])[0];
            double result3 = DHFSP.evaluate_exper(jobSeq_ls2_y3[0],protime,jobSeq_ls2_y3[1])[0];


             int[][] jobSeq_temp = new int[2][];
            jobSeq_temp[0]= NEH_des_block[0].clone();
            jobSeq_temp[1] = NEH_des_block[1].clone();

            if(result3<result1&&result3<result1){
                jobSeq_temp=jobSeq_ls2_y3;
                if(result1<result2){
                    Y1=jobSeq_ls2_y1;
                    Y2=jobSeq_ls2_y3;
                }else {
                    Y1=jobSeq_ls2_y3;
                    Y2=jobSeq_ls2_y2;
                }
            }else {
                Y1=jobSeq_ls2_y1;
                Y2=jobSeq_ls2_y2;
                if(result1<result2){
                    jobSeq_temp=jobSeq_ls2_y1;
                }else {
                    jobSeq_temp=jobSeq_ls2_y2;
                }
            }

//          5、保留最优的 比较jobSeq_Con和jobSeq_best
            double result_best = DHFSP.evaluate_exper(jobSeq_best[0],protime,jobSeq_best[1])[0];
            double result_temp = DHFSP.evaluate_exper(jobSeq_temp[0],protime,jobSeq_temp[1])[0];
            if(result_temp < result_best){
                jobSeq_best = jobSeq_temp;
            }
            //5.1 输出每次迭代的结果

//            System.out.println("makespan " + makespan);
            InterNum++;
        }
//        System.out.println("耗时： " +(endTime-startTime));
//        //5.1 输出每次迭代的结果
//                try {
//                    CreateExcel.result(Makespan,Makespan);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                } catch (WriteException e) {
//                    throw new RuntimeException(e);
//                }
        int[][] jobSeq_BIG_result = jobSeq_best;
        double makespan = DHFSP.evaluate_exper(jobSeq_best[0],protime,jobSeq_best[1])[0];
        return makespan;
    }

    /**
     * GA
     * @param jobSeq 作业编号排序
     * @param protime 作业的加工时间
     * @param facJobQty 每个工厂中作业的数量
     * @param popSize 种群的大小
     * @param maxInterNum 最大迭代次数
     * @return
     */
    public  static int[][] GA (int[] jobSeq, double[][] protime, int[] facJobQty ,int popSize,
                               int maxInterNum,int elistQty,double crossRate,double muteRate)   {
        //1、初始化：设置进化代数计数器t=0，设置最大进化代数T，随机生成M个个体作为初始群体P(0)
        //1、1
        int geneLength = jobSeq.length;
        int[][] chromosomes_init = new int[popSize][geneLength];
        chromosomes_init = GA.initSequenceChrome(popSize, geneLength);
        //2、个体评价：计算群体P(t)中各个个体的适应度。
        long startTime = System.currentTimeMillis();
        long endTime = startTime + 250* jobSeq.length* protime.length* facJobQty.length;
        int InterNum = 0;
        int[][] result = new int[2][];
        while (System.currentTimeMillis() < endTime){

            //3、选择运算：将选择算子作用于群体。
            int[][] chromosomes_select ;
            double[] fitnesses = new double[popSize];
            for (int i = 0; i < popSize; i++) {
                fitnesses[i] = DHFSP.evaluate(chromosomes_init[i],protime,facJobQty);
            }
            chromosomes_select = GA.selectionElistMin(chromosomes_init,fitnesses,elistQty);

            //4、交叉运算：将交叉算子作用于群体。
            int[][] chromosomes_cross;
            int[][] inChrome=new int[popSize][geneLength];
            //copy chromosomes_select
            for (int i = 0; i < popSize; i++) {
                for (int j = 0; j < geneLength; j++) {
                    inChrome[i][j] = chromosomes_select[i][j];
                }
            }
//            chromosomes_cross = GA.crossPBX(inChrome,crossRate);
            chromosomes_cross = GA.crossPc(inChrome,crossRate,protime,facJobQty);
//            chromosomes_cross = GA.crossOX(inChrome,crossRate);

            //5、变异运算：将变异算子作用于群体。
            int[][] chromosomes_mute;
            int[][] inChrome1=new int[popSize][geneLength];
            //copy chromosomes_select
            for (int i = 0; i < popSize; i++) {
                for (int j = 0; j < geneLength; j++) {
                    inChrome1[i][j] = chromosomes_cross[i][j];
                }
            }
            chromosomes_mute = GA.muteTwoPointSwap(inChrome1,muteRate,protime,facJobQty);
//            chromosomes_mute = GA.muteTwoPointReverse(inChrome1,muteRate);

            //下一次迭代
            chromosomes_init = chromosomes_mute;


            //输出每一代的最优结果
            int[][] jobSeq_GA = chromosomes_init;
            double[] fitnesses1 = new double[popSize];
            double minCmax = 483648;
            int Idx = 0;
            for (int i = 0; i < popSize; i++) {
                fitnesses1[i] = DHFSP.evaluate(jobSeq_GA[i],protime,facJobQty);
                if(fitnesses1[i]<minCmax){
                    minCmax = fitnesses1[i];
                    Idx = i;
                    result[0] = jobSeq_GA[i];
                    result[1] = facJobQty;
                }
            }
//            makespan[InterNum] = minCmax;
            System.out.println("facCmax = " + minCmax);
            InterNum++;

//        System.out.println("facCmax = " + minCmax + "\n"+" jobseq ");
//        for (int i = 0; i <jobSeq_GA[Idx].length ; i++) {
//            System.out.print(jobSeq_GA[Idx][i]+ " ");
//        }

        }
        //5.1 输出每次迭代的结果
//        try {
//            CreateExcel.result(makespan,makespan);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (WriteException e) {
//            throw new RuntimeException(e);
//        }

        return result;
    }

    /**
     * IGA
     * A simple and effective iterated greedy algorithm for the permutation flowshop scheduling problem
     * @param jobSeq 作业编号排序
     * @param protime 作业的加工时间
     * @param facJobQty 每个工厂中作业的数量
     * @param d 破坏个数
     * @param maxInterNum 最大迭代次数
     * @return
     */
    public  static int[][] IGA(int[] jobSeq, double[][] protime, int[] facJobQty ,double d,int maxInterNum){

        //1、NEH初始化
        int[][] jobSeq_NEHdes_result;
        //result[0]: NEH2初始化后的作业加工顺序  result[1] :每个工厂中的作业数量
        jobSeq_NEHdes_result = operator.NEH2_des(jobSeq,protime,facJobQty,1);
        double r0 = DHFSP.evaluate(jobSeq_NEHdes_result[0],protime,jobSeq_NEHdes_result[1]);
        //LS
        int[][] jobSeq_LS;
        jobSeq_LS = operator.LS3(jobSeq_NEHdes_result,protime);
        double r1 = DHFSP.evaluate(jobSeq_LS[0],protime,jobSeq_LS[1]);

        //2、记录初始解

        int[][] jobSeq_best = new int[2][];
        jobSeq_best[0]= jobSeq_LS[0].clone();
        jobSeq_best[1] = jobSeq_LS[1].clone();
        int[][] jobSeq_temp = new int[2][];
        jobSeq_temp[0]= jobSeq_LS[0].clone();
        jobSeq_temp[1] = jobSeq_LS[1].clone();

        //3、进入迭代
        long startTime = System.currentTimeMillis();
        long endTime = startTime + 250* jobSeq.length* protime.length* facJobQty.length;
        int InterNum = 0;
        while (System.currentTimeMillis() < endTime){

            //3.1 破坏，随机地选择d个作业删除
            int[][] jobSeq_Des_result = new int[3][];
            jobSeq_Des_result = operator.Destruction(jobSeq_temp[0],protime, jobSeq_temp[1],d);

            //3、2 重组，将被删除的d个作业贪婪插入
            int[][] jobSeq_Con_result = new int[2][];
            jobSeq_Con_result = operator.Construction(jobSeq_Des_result[0],protime,
                    jobSeq_Des_result[1],jobSeq_Des_result[2],1);
            int[] jobSeq_Con = jobSeq_Con_result[0];
            int[]  jobQty_infac_Con = jobSeq_Con_result[1];

            //4、LS
            int[][] jobSeq_LS1 = operator.LS3(jobSeq_Con_result,protime);

            //接受准则
            double result_LS1= DHFSP.evaluate(jobSeq_LS1[0],protime,jobSeq_LS1[1]);
            double result_best = DHFSP.evaluate(jobSeq_best[0],protime,jobSeq_best[1]);
            double result_temp = DHFSP.evaluate(jobSeq_Con,protime,jobQty_infac_Con);
            double facQty = facJobQty.length;
            double machQty = facJobQty.length*2;
            double jobQty = jobSeq.length;
            double T=0.8;
            double SA_Temper = operator.SA(result_best,result_temp,protime,
                    facQty,machQty,jobQty,T);
            double randonNum = Math.random();
            if(result_LS1<result_temp){
                jobSeq_temp = jobSeq_LS1;
                if(result_temp<result_best){
                    jobSeq_best = jobSeq_temp;
                }
            } else if (randonNum < SA_Temper) {
                jobSeq_temp = jobSeq_LS1;
            }
            //5.1 输出每次迭代的结果
//            System.out.println("makespan " + result_best);
            InterNum++;
        }

        //5.1 输出每次迭代的结果
//        try {
//            CreateExcel.result(makespan,makespan);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (WriteException e) {
//            throw new RuntimeException(e);
//        }

        int[][] jobSeq_IG_result = new int[2][];
        jobSeq_IG_result[0] = jobSeq_best[0].clone();
        jobSeq_IG_result[1] = jobSeq_best[1].clone();
        return jobSeq_IG_result;
    }

    /**
     * IGS
     * @param jobSeq 作业编号排序
     * @param protime 作业的加工时间
     * @param facJobQty 每个工厂中作业的数量
     * @param d 破坏个数
     * @param maxInterNum 最大迭代次数
     * @return
     */
    public  static int[][] IGS(int[] jobSeq, double[][] protime, int[] facJobQty ,double d,int maxInterNum){

        //1、NEH初始化,升序-降序
        int[][] jobSeq_result;
        jobSeq_result = operator.NEH2_des(jobSeq,protime,facJobQty,1);

        //2、初始化最优解
        int[][] jobSeq_best = jobSeq_result;
        int[][] jobSeq_Origin = new int[2][];
        jobSeq_Origin[0] = jobSeq_result[0].clone();
        jobSeq_Origin[1] = jobSeq_result[1].clone();

        //3、进入迭代
        int InterNum = 0;
        double[] makespan = new double[maxInterNum];
        long startTime = System.currentTimeMillis();
        long endTime = startTime + 250* jobSeq.length* protime.length* facJobQty.length;
        while (System.currentTimeMillis() < endTime){
            //3.1 破坏重组
            int[][] jobSeq_Des_result1 = operator.Destruction(jobSeq_Origin[0],protime,jobSeq_Origin[1],d);

            //3、2 重组，将被删除的d个作业贪婪插入
            int[][] jobSeq_Con_result1 = operator.Construction(jobSeq_Des_result1[0],protime,
                    jobSeq_Des_result1[1],jobSeq_Des_result1[2],1);

//            double n1 = DHFSP.evaluate(jobSeq_best[0],protime,jobSeq_best[1]);
//            double n2 = DHFSP.evaluate(jobSeq_Con_result1[0],protime,jobSeq_Con_result1[1]);
            if(DHFSP.evaluate(jobSeq_best[0],protime,jobSeq_best[1]) >
                    DHFSP.evaluate(jobSeq_Con_result1[0],protime,jobSeq_Con_result1[1])){
                jobSeq_best = jobSeq_Con_result1;
            }

            //3、3 Local PerturbationStrategy
            int[][] jobSeq_Local_result1 =operator.Sequential_swap(jobSeq_Con_result1[0],protime,jobSeq_Con_result1[1],1);
//            double n3 = DHFSP.evaluate(jobSeq_Origin[0],protime,jobSeq_Origin[1]);
//            double n4 = DHFSP.evaluate(jobSeq_Local_result1[0],protime,jobSeq_Local_result1[1]);
//            double n33 = DHFSP.evaluate_exper(jobSeq_Origin[0],protime,jobSeq_Origin[1])[0];
//            double n44 = DHFSP.evaluate_exper(jobSeq_Local_result1[0],protime,jobSeq_Local_result1[1])[0];

            if(DHFSP.evaluate(jobSeq_Origin[0],protime,jobSeq_Origin[1]) >
                    DHFSP.evaluate(jobSeq_Local_result1[0],protime,jobSeq_Local_result1[1])){
                jobSeq_Origin = jobSeq_Local_result1;
            }
            if(DHFSP.evaluate(jobSeq_best[0],protime,jobSeq_best[1]) >
                    DHFSP.evaluate(jobSeq_Local_result1[0],protime,jobSeq_Local_result1[1])){
                jobSeq_best = jobSeq_Local_result1;
            }

            //3、4 Global perturbationStrategy
            int[][] jobSeq_Global_result1 =operator.IG_half_swap(jobSeq_Local_result1[0],protime,jobSeq_Local_result1[1]);
//            double n5 = DHFSP.evaluate(jobSeq_best[0],protime,jobSeq_best[1]);
//            double n6 = DHFSP.evaluate(jobSeq_Global_result1[0],protime,jobSeq_Global_result1[1]);
            jobSeq_Origin = jobSeq_Global_result1;
            if(DHFSP.evaluate(jobSeq_best[0],protime,jobSeq_best[1]) >
                    DHFSP.evaluate(jobSeq_Global_result1[0],protime,jobSeq_Global_result1[1])){
                jobSeq_best = jobSeq_Local_result1;
            }
//          5、保留最优的 比较jobSeq_Con和jobSeq_best
            double result_best = DHFSP.evaluate(jobSeq_best[0],protime,jobSeq_best[1]);
            //5.1 输出每次迭代的结果

            System.out.println("makespan " + result_best);
//          6、下一次迭代
            InterNum++;
        }
        //5.1 输出每次迭代的结果
//        try {
//            CreateExcel.result(makespan,makespan);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (WriteException e) {
//            throw new RuntimeException(e);
//        }


        int[][] jobSeq_IGS_result = new int[2][];
        jobSeq_IGS_result[0] = jobSeq_best[0];
        jobSeq_IGS_result[1] = jobSeq_best[1];
        return jobSeq_IGS_result;
    }

    /**
     * CIG
     * @param jobSeq 作业编号排序
     * @param protime 作业的加工时间
     * @param facJobQty 每个工厂中作业的数量
     * @param d 破坏个数
     * @param maxInterNum 最大迭代次数
     * @return
     */
    public  static int[][] CIG(int[] jobSeq, double[][] protime, int[] facJobQty ,double d,int maxInterNum){

        //1、NEH初始化,升序-降序
        int[][] jobSeq_NEHasc_result = operator.NEH2_asc(jobSeq,protime,facJobQty);
        int[][] jobSeq_NEHdes_result = operator.NEH2_des(jobSeq,protime,facJobQty,1);
        //result[0]: NEH2初始化后的作业加工顺序  result[1] :每个工厂中的作业数量

        //2、记录初始解
        int[][] jobSeq_best = new int[2][];
        jobSeq_best[0] = jobSeq.clone();
        jobSeq_best[1] = facJobQty.clone();

        int[][] jobSeq_temp1 = new int[2][];
        jobSeq_temp1[0] = jobSeq_NEHasc_result[0].clone();
        jobSeq_temp1[1] = jobSeq_NEHasc_result[1].clone();
        int[][] jobSeq_temp2 = new int[2][];
        jobSeq_temp2[0] = jobSeq_NEHdes_result[0].clone();
        jobSeq_temp2[1] = jobSeq_NEHdes_result[1].clone();

        //3、进入迭代
        int InterNum = 0;
        long startTime = System.currentTimeMillis();
        long endTime = startTime + 250* jobSeq.length* protime.length* facJobQty.length;
        while (System.currentTimeMillis() < endTime){

            //3.1 跨工厂的局部搜索
            int[][] jobSeq_Rcf_result1 = operator.Random_critical_fac(jobSeq_temp1[0],protime,jobSeq_temp1[1]);
            int[][] jobSeq_Rcf_result2 = operator.Random_discretionary(jobSeq_temp2[0],protime,jobSeq_temp2[1]);

            //3.2 破坏，随机地选择d个作业删除
            int[][] jobSeq_Des_result1 = operator.Destruction(jobSeq_Rcf_result1[0],protime,jobSeq_Rcf_result1[1],d);
            int[][] jobSeq_Des_result2 = operator.Destruction(jobSeq_Rcf_result2[0],protime,jobSeq_Rcf_result2[1],d);

            //3、3 重组，将被删除的d个作业贪婪插入
            int[][] jobSeq_Con_result1 = operator.Construction(jobSeq_Des_result1[0],protime,
                                                                jobSeq_Des_result1[1],jobSeq_Des_result1[2],1);
            int[][] jobSeq_Con_result2 = operator.Construction(jobSeq_Des_result2[0],protime,
                                                                jobSeq_Des_result2[1],jobSeq_Des_result2[2],1);


            //3、4 Local Intersification
            int[][] jobSeq_L_result1 = operator.Random_swap(jobSeq_Con_result1[0],protime,jobSeq_Con_result1[1]);
            int[][] jobSeq_LI_result1 = operator.Sequential_swap(jobSeq_L_result1[0],protime,jobSeq_L_result1[1],1);
            int[][] jobSeq_L_result2 = operator.Random_swap(jobSeq_Con_result2[0],protime,jobSeq_Con_result2[1]);
            int[][] jobSeq_LI_result2 = operator.Sequential_swap(jobSeq_L_result2[0],protime,jobSeq_L_result2[1],1);


            //4 比较1，2
            double result1 = DHFSP.evaluate_exper(jobSeq_LI_result1[0],protime,jobSeq_LI_result1[1])[1];
            double result2 = DHFSP.evaluate_exper(jobSeq_LI_result2[0],protime,jobSeq_LI_result2[1])[1];
            if(result2<result1){
                jobSeq_temp1 = jobSeq_LI_result2;
                jobSeq_temp2 = jobSeq_LI_result2;
            }else {
                jobSeq_temp2 = jobSeq_LI_result1;
                jobSeq_temp1 = jobSeq_LI_result1;
            }


//          5、保留最优的 比较jobSeq_Con和jobSeq_best
            double result_best = DHFSP.evaluate_exper(jobSeq_best[0],protime,jobSeq_best[1])[1];
            double result_temp = DHFSP.evaluate_exper(jobSeq_temp1[0],protime,jobSeq_temp1[1])[1];
            if(result_temp < result_best){
                jobSeq_best = jobSeq_temp1;
            }
            //5.1 输出每次迭代的结果

//            System.out.println("makespan " + makespan[InterNum]);
//          6、下一次迭代
            InterNum++;
        }
//        System.out.println("耗时： " +(endTime-startTime));

        //5.1 输出每次迭代的结果
//        try {
//            CreateExcel.result(makespan,makespan);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (WriteException e) {
//            throw new RuntimeException(e);
//        }

        int[][] jobSeq_CIG_result = new int[2][];
        jobSeq_CIG_result[0] = jobSeq_best[0].clone();
        jobSeq_CIG_result[1] = jobSeq_best[1].clone();
        double makespan=DHFSP.evaluate(jobSeq_best[0],protime,jobSeq_best[1]);
        return jobSeq_CIG_result;
    }

    /**DDE
     * Discrete differential evolution algorithm for distributed blocking flowshop
     * scheduling with makespan criterion
     * @param jobSeq
     * @param protime
     * @param facJobQty
     * @param popSize
     * @param maxInterNum
     * @return
     */
    public  static int[][] DDE(int[] jobSeq, double[][] protime, int[] facJobQty ,int popSize,int maxInterNum){

        //1、初始化
        int[][] jobSeq_DLPT_result,jobSeq_DLS_result,jobSeq_DSPT_result,jobSeq_DNEH_result;
        //result[0]: NEH2初始化后的作业加工顺序  result[1] :每个工厂中的作业数量
        jobSeq_DSPT_result = operator.DSPT(jobSeq,protime,facJobQty);
        jobSeq_DLPT_result = operator.DLPT(jobSeq,protime,facJobQty);
        jobSeq_DLS_result = operator.DLS(jobSeq,protime,facJobQty);
        jobSeq_DNEH_result = operator.NEH2_des(jobSeq,protime,facJobQty,1);
        //种群的作业顺序
        int[][] popjob = new int[popSize][];
        int[] jobSeq_DSPT =popjob[0]= jobSeq_DSPT_result[0];
        popjob[1]= jobSeq_DLPT_result[0];
        popjob[2]= jobSeq_DLS_result[0];
        popjob[3]= jobSeq_DNEH_result[0];
        //种群的工厂机器数
        int[][] popjob_infac = new int[popSize][];
        int[] jobQty_eachfac_DSPT =popjob_infac[0] = jobSeq_DSPT_result[1];
        popjob_infac[1]= jobSeq_DLPT_result[1];
        popjob_infac[2]= jobSeq_DLS_result[1];
        popjob_infac[3]= jobSeq_DNEH_result[1];
        int[] minplan = jobSeq_DSPT;
        int[] minplanfac = jobQty_eachfac_DSPT;
        for (int i = 0; i <4; i++) {
            if(DHFSP.evaluate(minplan,protime,minplanfac)>DHFSP.evaluate(popjob[i], protime,popjob_infac[i])){
                minplan = popjob[i];
                minplanfac = popjob_infac[i];
            }
        }
        popjob[0]= minplan;
        popjob_infac[0] = minplanfac;

        //随机种群中的其他解
        for (int i = 1; i < popSize; i++) {

            //随机初始化
            int[] jobseq_ra = jobSeq.clone();
            for(int j=0;j<jobseq_ra.length;j++){
                int iRandNum = (int)(Math.random() * jobseq_ra.length);
                int temp = jobseq_ra[iRandNum];
                jobseq_ra[iRandNum] = jobseq_ra[j];
                jobseq_ra[j] = temp;
            }
            //jobs的工厂安排，前facQty个作业分别给每个工厂，其他的则随机
            int[] job_fac = new int[jobseq_ra.length];
            for (int j = 0; j < facJobQty.length; j++) {
                job_fac[j] = j;
            }
            for (int j = facJobQty.length; j <jobseq_ra.length; j++) {
                Random r = new Random();
                int randfac = r.nextInt(facJobQty.length);
                job_fac[j] = randfac;
            }
            //生成解
            int[] jobQty_infac = new int[facJobQty.length];
            for (int j = 0; j < facJobQty.length; j++) {
                for (int k = 0; k < job_fac.length; k++) {
                    if(job_fac[k]==j){
                        jobQty_infac[j] +=1;
                    }
                }
            }
            int[] jobseq_random = new int[jobseq_ra.length];
            int n=0;
            for (int j = 0; j < facJobQty.length; j++) {
                for (int k = 0; k < jobseq_ra.length; k++) {
                    if(job_fac[k]==j){
                        jobseq_random[n++] = jobseq_ra[k];
                    }
                }
            }
            popjob[i] = jobseq_random;
            popjob_infac[i] = jobQty_infac;
        }


        int[][] result_k = new int[2][];
        result_k[0] = popjob[0].clone();
        result_k[1] = popjob_infac[0].clone();
        double minMaxspan = DHFSP.evaluate(result_k[0], protime, result_k[1]);

        long startTime = System.currentTimeMillis();
        long endTime = startTime + 250* jobSeq.length* protime.length* facJobQty.length;
        while (System.currentTimeMillis() < endTime){
            //2、local search
            //2、1 选出种群最优
            double min_k = DHFSP.evaluate(popjob[0], protime, popjob_infac[0]);
            int[] minjobSeqk = popjob[0];
            int[] minjobQtyk = popjob_infac[0];
            for (int i = 0; i < popSize; i++) {
                if (min_k > DHFSP.evaluate(popjob[i], protime, popjob_infac[i])) {
                    minjobSeqk = popjob[i];
                    minjobQtyk = popjob_infac[i];
                    min_k = DHFSP.evaluate(popjob[i], protime, popjob_infac[i]);
                }
            }
            result_k[0] = minjobSeqk;
            result_k[1] = minjobQtyk;
            double m1 = DHFSP.evaluate(result_k[0], protime, result_k[1]);
            //局部搜索
            int n = 0;
            long startTime1 = System.currentTimeMillis();
            long endTime1 = startTime1 + 30;
            while (System.currentTimeMillis() < endTime1){
                //2、2 ICFI
                int[][] result_ICFI = operator.ICFI(result_k, protime);
                if (DHFSP.evaluate(result_k[0], protime, result_k[1]) >
                        DHFSP.evaluate(result_ICFI[0], protime, result_ICFI[1])) {
                    result_k = result_ICFI;

                }
                //2、3 EDFI
                int[][] result_EDFI = operator.EDFI(result_k, protime);
                if (DHFSP.evaluate(result_k[0], protime, result_k[1]) >
                        DHFSP.evaluate(result_EDFI[0], protime, result_EDFI[1])) {
                    result_k = result_ICFI;
                }
                //2、4 ECFS
                int[][] result_ECFS = operator.ECFS(result_k, protime);
                double m2 = DHFSP.evaluate(result_ECFS[0], protime, result_ECFS[1]);
                if (DHFSP.evaluate(result_k[0], protime, result_k[1]) >
                        DHFSP.evaluate(result_EDFI[0], protime, result_EDFI[1])) {
                    result_k = result_ECFS;
                }
                n++;
            }
//            System.out.println("makespan " + DHFSP.evaluate(result_k[0], protime, result_k[1]));
            if(minMaxspan>DHFSP.evaluate(result_k[0], protime, result_k[1])){
                minMaxspan = DHFSP.evaluate(result_k[0], protime, result_k[1]);
            }

            //精英保留
//            if(DHFSP.evaluate(result_k[0], protime, result_k[1])<minMaxspan) {
//                for (int i = 0; i < popSize; i++) {
//                    Random r = new Random();
//                    int popID = r.nextInt(popSize);
//                    if (DHFSP.evaluate(popjob[popID], protime, popjob_infac[popID]) > DHFSP.evaluate(result_k[0], protime, result_k[1])) {
//                        popjob[popID] = result_k[0].clone();
//                        popjob_infac[popID] = result_k[1].clone();
//                        minMaxspan = DHFSP.evaluate(result_k[0], protime, result_k[1]);
//                        break;
//                    }
//                }
//                popjob[0] = result_k[0];
//                popjob_infac[0] = result_k[1];
//            }

            int t=0;
            while (t<popSize){
                //3、Mutation
                //从种群中随机3个个体，选出最优
                int[][] result_x = new int[2][];
                double minIden = 483648;
                int Iden_Id = 0;
                int[] pop_ra_3 = operator.random(3, popSize);
                for (int i = 0; i < 3; i++) {
                    if (DHFSP.evaluate(popjob[pop_ra_3[i]], protime, popjob_infac[pop_ra_3[i]]) < minIden) {
                        result_x[0] = popjob[pop_ra_3[i]];
                        result_x[1] = popjob_infac[pop_ra_3[i]];
                        Iden_Id = pop_ra_3[i];
                        minIden = DHFSP.evaluate(popjob[pop_ra_3[i]], protime, popjob_infac[pop_ra_3[i]]);
                    }
                }

                int[][] result_v = operator.DDEmutation(result_x, protime, 0.2);

                popjob[Iden_Id] = result_x[0];
                popjob_infac[Iden_Id] = result_x[1];
                double m2 = DHFSP.evaluate(result_v[0], protime, result_v[1]);
                int[][] result_U1 = operator.DDEcrossover(result_x, result_v, 0.1);
                int[][] result_U2 = operator.DDEcrossover(result_v, result_x, 0.1);
                int[][] result_U;
                if (DHFSP.evaluate(result_U1[0], protime, result_U1[1]) >
                        DHFSP.evaluate(result_U2[0], protime, result_U2[1])) {
                    result_U = result_U2;
                } else result_U = result_U1;

                //4 Selection
                double makespan_U = DHFSP.evaluate(result_U[0], protime, result_U[1]);
                double makespan_X = DHFSP.evaluate(result_x[0], protime, result_x[1]);
                double RE = (makespan_U - makespan_X) / makespan_X;
                double Se_rate = 0.02;   //选择的概率，lanmuda
                double random = Math.random();
                if (RE < 0 || random < Math.max(Se_rate - RE, 0)) {
                    result_x = result_U;
                }
                popjob[Iden_Id] = result_x[0];
                popjob_infac[Iden_Id] = result_x[1];
                t++;
            }

        }

        //5.1 输出每次迭代的结果
//        try {
//            CreateExcel.result(makespan,makespan);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (WriteException e) {
//            throw new RuntimeException(e);
//        }
        System.out.println("minmakespan " + minMaxspan);
        return result_k;


    }

    /**PSO
     * Discrete differential evolution algorithm for distributed blocking flowshop
     * scheduling with makespan criterion
     * @param jobSeq
     * @param protime
     * @param facJobQty
     * @param popSize
     * @param maxInterNum
     * @return
     */
    public  static int[][] PSO(int[] jobSeq, double[][] protime, int[] facJobQty ,int popSize,
                               int maxInterNum,double c1,double c2){
        //1、粒子初始化，工厂中的作业数量均匀分布
        //SPT dispatching 作业按加工时间升序
        int[][] SPT = operator.SPT(jobSeq,protime,facJobQty);
        //NEH
        int[][] NEH = operator.NEH2_des(jobSeq,protime,facJobQty,1);
        //random
        double[][] X_init = PSO.initX(popSize,jobSeq.length);   // 作业编号/位置
        double[] fitness_init = new double[popSize];      // 种群位置/适应度值


        int[][] chromosomes_init = PSO.parseInt(X_init);          // 加工顺序/作业编号
        chromosomes_init[0] = SPT[0];
        X_init[0] = PSO.parseDouble(SPT[0]);
        chromosomes_init[1] = NEH[0];
        X_init[1] = PSO.parseDouble(NEH[0]);
        //2、找到粒子的局部最佳位置
        for (int i = 0; i < popSize; i++) {
            fitness_init[i] = DHFSP.evaluate(chromosomes_init[i],protime,facJobQty);
        }
        int Lidx = PSO.getMinFitIdx(fitness_init);
        //3、初始化粒子的速度,初始速度为粒子的初始位置与局部最佳位置的绝对差,采用启发是规则的v为0
        double[][] V_init = new double[popSize][jobSeq.length];
//        for (int i = 0; i <popSize ; i++) {
//            for (int j = 0; j < jobSeq.length; j++) {
//                V_init[i][j] = Math.abs(X_init[i][j] - X_init[Lidx][j]);
//            }
//        }

        //4、确定粒子的全局最优位置
        double[] Gbest_X = X_init[Lidx].clone();
        double Gbest = DHFSP.evaluate(chromosomes_init[Lidx],protime,facJobQty);
        //每个粒子的当前最优
        double[][] Pbest_X = new double[popSize][];
        for (int i = 0; i < popSize; i++) {
            Pbest_X[i] = X_init[i].clone();
        }

        double makespan = Gbest;
        long startTime = System.currentTimeMillis();
        long endTime = startTime + 250* jobSeq.length* protime.length* facJobQty.length;
        while (System.currentTimeMillis() < endTime){

            //6、更新粒子的位置和速度
            double[] fitness_before = new double[popSize];
            int[][] chromosomes_before = PSO.parseInt(X_init);          // 加工顺序/作业编号
            for (int j = 0; j < popSize; j++) {
                fitness_before[j] = DHFSP.evaluate(chromosomes_before[j],protime,facJobQty);
            }

            //每个更新粒子的位置和速度
            for (int i = 0; i < popSize; i++) {

                for (int j = 0; j < jobSeq.length; j++) {
                    double r1 = Math.random();
                    double r2 = Math.random();
                    V_init[i][j] = V_init[i][j] + c1*r1*(Pbest_X[i][j] - X_init[i][j])
                            + c2*r2*(Gbest_X[j] - X_init[i][j]);
                    X_init[i][j] = X_init[i][j] + V_init[i][j];
                }
            }

            //评估适应度函数
            double[] fitness_temp = new double[popSize];
            int[][] chromosomes_temp = PSO.parseInt(X_init);          // 加工顺序/作业编号
            for (int j = 0; j < popSize; j++) {
                fitness_temp[j] = DHFSP.evaluate(chromosomes_temp[j],protime,facJobQty);
            }

            //变邻域搜索VNS，对最优的粒子进行VNS
            int minpos = PSO.getMinFitIdx(fitness_temp);
            int[][] minsol_temp = new int[2][];
            minsol_temp[0]= chromosomes_temp[minpos].clone();
            minsol_temp[1]= facJobQty.clone();
            double minsol_temp_makespan = DHFSP.evaluate(minsol_temp[0],protime,minsol_temp[1]);
            int l=0;
            while (l<2){
                //n1
                int[][] temp1 = operator.Sequential_swap(minsol_temp[0],protime,facJobQty,1);
                double temp1_makespan = DHFSP.evaluate(temp1[0],protime,temp1[1]);
                //n2
                int[][] temp2 = operator.Random_swap(minsol_temp[0],protime,facJobQty);
                double temp2_makespan = DHFSP.evaluate(temp2[0],protime,temp2[1]);
                if(temp1_makespan<minsol_temp_makespan||temp2_makespan<minsol_temp_makespan){
                    if(temp1_makespan<temp2_makespan){
                        minsol_temp=temp1;
                        minsol_temp_makespan=temp1_makespan;
                        l=0;
                    }else {
                        minsol_temp=temp2;
                        minsol_temp_makespan=temp2_makespan;
                        l=0;
                    }
                }else l+=1;
            }

            //更新局部搜索后的加工顺序对应的位置
            //返回X_init[minpos]升序排列
            double[] X_temp = X_init[minpos].clone();
            Arrays.sort(X_temp);
            for (int j = 0; j < X_init[minpos].length; j++) {
                X_init[minpos][minsol_temp[0][j]] = X_temp[j];
            }

            //更新个体最优和全局最优
            for (int i = 0; i < popSize; i++) {
                if(fitness_temp[i]<fitness_before[i]){
                    Pbest_X[i] = X_init[i];
                }
                if(Gbest > fitness_temp[i]){
                    Gbest_X = X_init[i];
                }
            }
//            System.out.println("makespan " + minsol_temp_makespan);
            if(makespan>minsol_temp_makespan){
                makespan = minsol_temp_makespan;
            }
        }
//        try {
//            CreateExcel.result(makespan,makespan);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (WriteException e) {
//            throw new RuntimeException(e);
//        }
//        System.out.println("minmakespan " + makespan);
        int[] best = PSO.parseInt(Gbest_X);
        int[][] jobSeq_PSO_result = new int[2][];
        jobSeq_PSO_result[0] = best;
        jobSeq_PSO_result[1] = facJobQty;
        return jobSeq_PSO_result;
    }

    public  static int[][] HEDFOA(int[] jobSeq, double[][] protime, int[] facJobQty ,int NP ,
                               double init_a,double init_b){
        //初始化
        int[][] jobSq1 = operator.EHPF2(jobSeq,protime,facJobQty,init_a,init_b);
        int[][] jobSq2 = new int[2][];
        jobSq2[0] = jobSq1[0].clone();
        jobSq2[1] = jobSq1[1].clone();


        int[][] job_best = new int[2][];
        job_best[0] = jobSq1[0].clone();
        job_best[1] = jobSq1[1].clone();
        double min = DHFSP.evaluate(job_best[0],protime,job_best[1]);
        //
        long startTime = System.currentTimeMillis();
        long endTime = startTime + 250* jobSeq.length* protime.length* facJobQty.length;
        while (System.currentTimeMillis() < endTime){
            //smell_based foraging
            int[][] jobSq1_des = operator.Destruction(jobSq1[0],protime,jobSq1[1],4);
            int[][] jobSq2_des = operator.Destruction(jobSq2[0],protime,jobSq2[1],4);
            int[][] jobSq1_Con = operator.Construction1(jobSq1_des[0],protime,jobSq1_des[1],jobSq1_des[2],1);
            int[][] jobSq2_Con = operator.Construction1(jobSq2_des[0],protime,jobSq2_des[1],jobSq2_des[2],1);

            //vision_based foraging
            int[][] jobSq_v1 = operator.Insert_vision_based(jobSq1_Con[0],protime,jobSq1_Con[1]);
            int[][] jobSq_v2 = operator.Insert_vision_based(jobSq2_Con[0],protime,jobSq2_Con[1]);
            double n1 = DHFSP.evaluate(jobSq_v1[0],protime,jobSq_v1[1]);
            double n2 = DHFSP.evaluate(jobSq_v2[0],protime,jobSq_v2[1]);
            double SA1 = operator.SA(min,n1,protime,facJobQty.length,
                    protime.length*2,jobSeq.length,0.2);
            double SA2 = operator.SA(min,n2,protime,facJobQty.length,
                    protime.length*2,jobSeq.length,0.2);
            if(min>n1){
                min = n1;
                job_best=jobSq_v1;
                double randonNum = Math.random();
                if(randonNum<SA1){
                    jobSq1 = jobSq_v1;
                    jobSq2 = jobSq_v1;
                }
            }
            if (min>n2) {
                min = n2;
                job_best=jobSq_v2;
                double randonNum = Math.random();
                if(randonNum<SA2){
                    jobSq1 = jobSq_v2;
                    jobSq2 = jobSq_v2;
                }
            }
//            System.out.println("makespan " + min);
        }

        return job_best;
    }

    }
