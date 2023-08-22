import Problem.DHFSP;
import easyopt.commom.CreateExcel;
import easyopt.commom.File;
import easyopt.model.DHFSP_Instance;
import jxl.write.WriteException;


import java.io.IOException;

/**
 * lfg
 */
public class test {
    public static void main(String[] args) throws IOException {
        //write file

        int[] job = {40,60,80,100,150,200};
        int[] pro = {4,5,6,7,8};
        int[] fac = {2,4};
        double[] d = {1};
        double[] a = {0.6,0.7,0.8,0.9,1};
        double[] b = {0.3,0.4,0.5,0.6,0.7};
//        for (int i = 0; i < job.length; i++) {
//            for (int j = 0; j < pro.length; j++) {
//                for (int k = 0; k < fac.length; k++) {
//                    WriteFile.write_DHFSP_Instance(job[i], pro[j], fac[k]);
//                }
//            }
//        }
        /*实验,参数验证*/
        double[][] result = new double[60][10];
        String[] result_INS = new String[60];
        int t = 0;
        for (int i = 0; i < job.length; i++) {
            for (int j = 0; j < pro.length; j++) {
                for (int k = 0; k < fac.length; k++) {
                    //read file
                    //C:\Users\86157\Desktop\DBHFSP_L - 5\src\Instance\Instance_80_6_4.txt
                    String filePath = "C:\\Users\\86157\\Desktop\\DBHFSP_L - 5\\src\\Instance\\Instance_"+job[i]+"_"+pro[j]+"_"+fac[k]+".txt";
                    DHFSP_Instance dhfsp_instance = new DHFSP_Instance();
                    File file = new File();
                    dhfsp_instance = file.readProblem(filePath);

                    double[][] protime = dhfsp_instance.getPtime().clone();
                    int[] facJobQty = dhfsp_instance.getFacjobQty().clone();

                    //数组存一个解
                    int[] jobSeq = new int[protime[0].length];
                    for (int i1 = 0; i1 < protime[0].length; i1++) {
                        jobSeq[i1] = i1;
                    }

                    for (int l = 0; l < a.length; l++) {
                        //实验20次 //a:
//                        double[] NEH_des_block = new double[20];
//                        for (int i2 = 0; i2 < 20; i2++) {
//                            NEH_des_block[i2] = Algorithm.BIG(jobSeq,protime,facJobQty,3,a[l],0.5);
//                        }
//                        double RPI1= 0;
//                        for (int i2 = 0; i2 < 20; i2++) {
//                            RPI1+= NEH_des_block[i2];
//                        }
                        //实验20次 //b:
                        double[] NEH_enb = new double[20];
                        for (int i2 = 0; i2 < 20; i2++) {
                            NEH_enb[i2] = Algorithm.BIG(jobSeq,protime,facJobQty,3,0.9,b[l]);
                        }
                        double RPI2= 0;
                        for (int i2 = 0; i2 < 20; i2++) {
                            RPI2+= NEH_enb[i2];
                        }
                        result_INS[t] = job[i]+"_"+pro[j]+"_"+fac[k];
//                        result[t][l] = RPI1;
                        result[t][l+5] = RPI2;
                        System.out.println(job[i]+"_"+pro[j]+"_"+fac[k]);
//                        System.out.println("RPI1 = "+a[l] + "_" + RPI1);
                        System.out.println("RPI2 = "+b[l]+ "_" + RPI2);
                    }
                    t++;
                }
            }
        }
        try {
            CreateExcel.result1(result_INS,result);
        } catch (WriteException e) {
            throw new RuntimeException(e);
        }

        /*实验一*/
        double[][] result0 = new double[36][6];
        String[] result0_INS = new String[36];
//        int t0 = 0;
//        for (int i = 0; i < job.length; i++) {
//            for (int j = 0; j < pro.length; j++) {
//                for (int k = 0; k < fac.length; k++) {
//                    //read file
//                    String filePath = "D:\\DBHFSP_L\\src\\Instance\\Instance_"+job[i]+"_"+pro[j]+"_"+fac[k]+".txt";
//                    DHFSP_Instance dhfsp_instance = new DHFSP_Instance();
//                    File file = new File();
//                    dhfsp_instance = file.readProblem(filePath);
//
//                    double[][] protime = dhfsp_instance.getPtime().clone();
//                    int[] facJobQty = dhfsp_instance.getFacjobQty().clone();
//
//                    //数组存一个解
//                    int[] jobSeq = new int[protime[0].length];
//                    for (int i1 = 0; i1 < protime[0].length; i1++) {
//                        jobSeq[i1] = i1;
//                    }
//                    //实验20次
//                    double[] IG = new double[20];
//                    double[] IG_a = new double[20];
//                    for (int i2 = 0; i2 < 20; i2++) {
//                        double jobSeq_IG = Algorithm.BasicIG(jobSeq,protime,facJobQty,4,1);
//                        double jobSeq_IG_a = Algorithm.BasicIG(jobSeq,protime,facJobQty,4,0);
//                        IG[i2] = jobSeq_IG;
//                        IG_a[i2] = jobSeq_IG_a;
//                    }
//                    double minIG_a = IG_a[0];
//                    double minIG = IG[0];
//                    double maxIG_a = IG_a[0];
//                    double maxIG = IG[0];
//                    for (int i2 = 0; i2 < 20; i2++) {
//                        if(minIG>IG[i2]){minIG = IG[i2];}
//                        if(maxIG<IG[i2]){maxIG = IG[i2];}
//                        if(minIG_a>IG_a[i2]){minIG_a = IG_a[i2];}
//                        if(maxIG_a<IG_a[i2]){maxIG_a = IG_a[i2];}
//                    }
//                    //ARPI
//                    double ARPIIG = 0;
//                    double ARPIIG_a = 0;
//                    for (int l = 0; l < 20; l++) {
//                        ARPIIG += (IG[l]-minIG)/minIG*((double) 1/20);
//                        ARPIIG_a += (IG_a[l]-minIG_a)/minIG_a*((double) 1/20);
//                    }
//                    result0_INS[t0] = job[i]+"_"+pro[j]+"_"+fac[k];
//                    result0[t0][0] = ARPIIG_a;
//                    result0[t0][1] = minIG_a;
//                    result0[t0][2] = maxIG_a;
//                    result0[t0][4] = minIG;
//                    result0[t0][5] = maxIG;
//                    result0[t0++][3] = ARPIIG;
//                    System.out.println(job[i]+"_"+pro[j]+"_"+fac[k]);
//                    System.out.println("ARPIIG = " + ARPIIG);
//                    System.out.println("minIG = " + minIG);
//                    System.out.println("maxIG = " + maxIG);
//                    System.out.println("ARPIIG_a = "  + ARPIIG_a);
//                    System.out.println("maxIG_a = "  + maxIG_a);
//                    System.out.println("minIG_a = " + minIG_a);
//                }
//            }
//        }
//
//        //记录结果
//        try {
//            CreateExcel.result0(result0_INS,result0);
//        } catch (WriteException e) {
//            throw new RuntimeException(e);
//        }

        /*实验二*/
        double[][] result1 = new double[360][3];
        String[] result1_INS = new String[360];
        int t1 = 0;
//        for (int i = 0; i < job.length; i++) {
//            for (int j = 0; j < pro.length; j++) {
//                for (int k = 0; k < fac.length; k++) {
//                    //read file
//                    //D:\DBHFSP_L - 5\src\Instance\Instance_40_6_4.txt
//                    String filePath = "D:\\DBHFSP_L - 5\\src\\Instance\\Instance_"+job[i]+"_"+pro[j]+"_"+fac[k]+".txt";
//                    DHFSP_Instance dhfsp_instance = new DHFSP_Instance();
//                    File file = new File();
//                    dhfsp_instance = file.readProblem(filePath);
//
//                    double[][] protime = dhfsp_instance.getPtime().clone();
//                    int[] facJobQty = dhfsp_instance.getFacjobQty().clone();
//
//                    //数组存一个解
//                    int[] jobSeq = new int[protime[0].length];
//                    for (int i1 = 0; i1 < protime[0].length; i1++) {
//                        jobSeq[i1] = i1;
//                    }
//                    //d:
//                    for (int l = 0; l < d.length; l++) {
//                        //实验20次
//                        double[] IG_a = new double[20];
//                        for (int i2 = 0; i2 < 20; i2++) {
//                            double jobSeq_BIG = Algorithm.BIG(jobSeq,protime,facJobQty,d[l]);
//                            IG_a[i2] = jobSeq_BIG;
//                        }
//                        double minIG_a = IG_a[0];
//                        double maxIG_a = IG_a[0];
//                        double ARPIIG_a = 0;
//                        for (int i2 = 0; i2 < 20; i2++) {
//                            if(minIG_a>IG_a[i2]){minIG_a = IG_a[i2];}
//                            if(maxIG_a<IG_a[i2]){maxIG_a = IG_a[i2];}
//                            ARPIIG_a += IG_a[i2];
//                        }
//                        result1_INS[t1] = job[i]+"_"+pro[j]+"_"+fac[k]+"_"+d[l];
//                        result1[t1][0] = ARPIIG_a;
//                        result1[t1][1] = minIG_a;
//                        result1[t1++][2] = maxIG_a;
//                        System.out.println(job[i]+"_"+pro[j]+"_"+fac[k]+"_"+d[l]);
//                        System.out.println(ARPIIG_a);
//                        System.out.println(minIG_a);
//                    }
//                }
//            }
//        }

        //记录结果
//        try {
//            CreateExcel.result1(result1_INS,result1);
//        } catch (WriteException e) {
//            throw new RuntimeException(e);
//        }

//        //read file
//        String filePath = "D:\\DBHFSP_L\\src\\Instance\\DHFSP03_50_5_2.txt";
//        DHFSP_Instance dhfsp_instance = new DHFSP_Instance();
//        File file = new File();
//        dhfsp_instance = file.readProblem(filePath);
//
//        double[][] protime = dhfsp_instance.getPtime().clone();
//        int[] facJobQty = dhfsp_instance.getFacjobQty().clone();
//
//        //数组存一个解
//        int[] jobSeq = new int[protime[0].length];
//        for (int i = 0; i < protime[0].length; i++) {
//            jobSeq[i] = i;
//        }

//        /*随机初始化*/
//        for(int i=0;i<jobSeq.length;i++){
//            int iRandNum = (int)(Math.random() * jobSeq.length);
//            int temp = jobSeq[iRandNum];
//            jobSeq[iRandNum] = jobSeq[i];
//            jobSeq[i] = temp;
//        }

//        /*带阻塞约束的分布式混流*/
//        double sch = DHFSP.evaluate(jobSeq,protime,facJobQty);
//        double Cmax = DHFSP.evaluate(jobSeq,protime,facJobQty);
//        System.out.println("Cmax = " + Cmax );


////        /*NEH2EE启发式初始化*/
//        int[] jobSeq_NEH2EE = operator.NEH2EE(jobSeq,protime,facJobQty);
//        int[][] jobSeq_NEH_des_blocka = operator.NEH_des_block(jobSeq,protime,facJobQty,0.9,0);
//        int[][] jobSeq_NEH2_ena = operator.NEH2_enb(jobSeq,protime,facJobQty,0,0.5);
//        int[][] jobSeq_NEH_des_block = operator.NEH_des_block(jobSeq,protime,facJobQty,0.9,0);
//        int[][] jobSeq_NEH2_en = operator.NEH2_enb(jobSeq,protime,facJobQty,0,1);
//
//        System.out.println("jobSeq_NEH_des_block = " + DHFSP.evaluate_exper(jobSeq_NEH_des_block[0],protime,jobSeq_NEH_des_block[1])[0]);
//        System.out.println("jobSeq_NEH2_en = " + DHFSP.evaluate_exper(jobSeq_NEH2_en[0],protime,jobSeq_NEH2_en[1])[0]);
//        System.out.println("jobSeq_NEH_des_blocka = " + DHFSP.evaluate_exper(jobSeq_NEH_des_blocka[0],protime,jobSeq_NEH_des_blocka[1])[0]);
//        System.out.println("jobSeq_NEH2_ena= " + DHFSP.evaluate_exper(jobSeq_NEH2_ena[0],protime,jobSeq_NEH2_ena[1])[0]);

////
////
////
//        //BasicIG

//
//        double[][] R = new double[10][8];
//        //BIG 909 909 903 915 901 906
//        for (int i = 0; i <10; i++) {
//            int[][] jobSeq_BIG = Algorithm.BIG(jobSeq,protime,facJobQty,3,1000);
//            double result_best = DHFSP.evaluate_exper(jobSeq_BIG[0],protime,jobSeq_BIG[1])[0];
//            R[i][0] = result_best;
//            System.out.println("makespan1 " + result_best);
//        }

//        //CIG 910 908 908 896 915 916
//        for (int i = 0; i < 10; i++) {
//            int[][] jobSeq_CIG = Algorithm.CIG(jobSeq,protime,facJobQty,3,200);
//            double result_best = DHFSP.evaluate_exper(jobSeq_CIG[0],protime,jobSeq_CIG[1])[1];
//            R[i][1] = result_best;
//            System.out.println("makespan " + result_best);
//        }
//
//        //IGA 948 941 932 938 934
//        for (int i = 0; i < 10; i++) {
//            int[][] jobSeq_IGA = Algorithm.IGA( jobSeq,protime,facJobQty ,3,200);
//            double result_best = DHFSP.evaluate(jobSeq_IGA[0],protime,jobSeq_IGA[1]);
//            R[i][2] = result_best;
//            System.out.println("makespan " + result_best);
//        }
//
//        //IGS 929 925 926 926 925 930
//        for (int i = 0; i < 10; i++) {
//            int[][] jobSeq_IGS = Algorithm.IGS(jobSeq,protime,facJobQty,3,200);
//            double result_best = DHFSP.evaluate(jobSeq_IGS[0],protime,jobSeq_IGS[1]);
//            R[i][3] = result_best;
//            System.out.println("makespan " + result_best);
//        }
//
//        //HEDFOA 924 934 921 894 920
//        for (int i = 0; i <10; i++) {
//            int[][] jobSeq_HEDFOA = Algorithm.HEDFOA(jobSeq,protime,facJobQty,2,0.55,0.7);
//            double result_best = DHFSP.evaluate(jobSeq_HEDFOA[0],protime,jobSeq_HEDFOA[1]);
//            R[i][4] = result_best;
//            System.out.println("makespan " + result_best);
//        }
//
//        //DDE 958 962 953 958 962 966
//        for (int i = 0; i <10; i++) {
//            int[][] jobSeq_DDE = Algorithm.DDE(jobSeq,protime,facJobQty,50,200);
//            double result_best = DHFSP.evaluate(jobSeq_DDE[0],protime,jobSeq_DDE[1]);
//            R[i][5] = result_best;
//            System.out.println("makespan " + result_best);
//        }
//
//        //GA 957 930 940 982 928 940
//        for (int i = 0; i <10; i++) {
//            int[][] jobSeq_GA = Algorithm.GA(jobSeq,protime,facJobQty,100,200,70,0.85,0.7);
//            double result_best = DHFSP.evaluate(jobSeq_GA[0],protime,jobSeq_GA[1]);
//            R[i][6] = result_best;
//            System.out.println("makespan " + result_best);
//        }
//
//        //PSO 969 967 941 960 956 954
//        for (int i = 0; i <10; i++) {
//            int[][] jobSeq_PSO = Algorithm.PSO(jobSeq,protime,facJobQty,100,20000,10,5);
//            double result_best = DHFSP.evaluate(jobSeq_PSO[0],protime,jobSeq_PSO[1]);
//            R[i][7] = result_best;
//            System.out.println("makespan " + result_best);
//        }
//        try {
//            CreateExcel.result(R);
//        } catch (WriteException e) {
//            throw new RuntimeException(e);
//        }

    }

}
