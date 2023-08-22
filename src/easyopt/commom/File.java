package easyopt.commom;

import easyopt.model.DHFSP_Instance;
import easyopt.model.HFSPIdentical;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**对一些用于读取算例数据文件的方法进行封装的类
 * */
public class File {

  /**读取输入数据文件路径中的数据，形成对应的同质并行机混合流水车间调度算例对象
   * 输入文件中数据的模板参看"Design of a testbed for hybrid flow shop scheduling with identical machines"文中给出的算例文件，
   * txt数据格式网址为：www.iescm.com/instances
   * 或者http://grupo.us.es/oindustrial/en/research/results
   * */
  public static HFSPIdentical readFileHFSP(String txtPath) {
    HFSPIdentical hfsp=new HFSPIdentical();
    String filePath = txtPath;
    FileInputStream fin=null;
    try {
      fin = new FileInputStream(filePath);
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    InputStreamReader reader = new InputStreamReader(fin);
    BufferedReader buffReader = new BufferedReader(reader);
    List<String> strList=new ArrayList<String>();
    String strTmp = "";
    try {
      while((strTmp = buffReader.readLine())!=null){
        strList.add(strTmp);
//        System.out.println(strTmp);
      }     
      //拆分并进行数据提取--作业数量和工序数量
      String[] firstRow=strList.get(0).trim().split("\\s+");
      hfsp.jobQty=Integer.parseInt(firstRow[0]);
      hfsp.processQty=Integer.parseInt(firstRow[1]);
      //拆分并进行数据提取--各道工序中机器的数量      
      String[] secondRow=strList.get(1).trim().split("\\s+");
      int[] machQtys=new int[hfsp.processQty];
      for(int m=0;m<hfsp.processQty;m++){
        machQtys[m]=Integer.parseInt(secondRow[m]);
      }
      hfsp.procMachQty=machQtys;
      //拆分并进行数据提取--每个作业在各道工序的工时
      double[][] processTimes=new double[hfsp.jobQty][hfsp.processQty];
      for(int j=2;j<strList.size();j++){
        String[] midStr=strList.get(j).trim().split("\\s+");
        if(midStr.length!=hfsp.jobQty){
          System.out.println(" input data has error, the jobQty is not equal to the processTimes's column");
        }
        for(int k=0;k<hfsp.jobQty;k++){
          processTimes[k][j-2]=Double.parseDouble(midStr[k]);
        }
      }
      hfsp.processTimes=processTimes;
      
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    try {
      buffReader.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }    
    return hfsp;
  }

  public DHFSP_Instance readProblem(String file) throws IOException{
    //new一个实例对象
    DHFSP_Instance instance_DHFSP = new DHFSP_Instance();
    Reader inputFile = new BufferedReader(              //将字符流放到字符流缓冲区
            new InputStreamReader(                      //将字节流变成字符流
                    new FileInputStream(file)));        //将文件读取成字节流
    StreamTokenizer token = new StreamTokenizer(inputFile);  //处理字符串  （默认空格为分隔符
    try {
      //逐一读取
      //初始化工件数
      token.nextToken();
      int jobQty = (int) token.nval;
      instance_DHFSP.setJobQty(jobQty);
      //初始化工序数
      token.nextToken() ;
      int proQty = (int) token.nval;
      instance_DHFSP.setProQty(proQty);
      //初始化工厂数
      token.nextToken() ;
      int facQty = (int) token.nval;
      instance_DHFSP.setFacQty(facQty);
      //初始化每个工厂分配的机器数
      int[] facJobQty = new int[facQty];
      for (int i = 0; i < facQty; i++) {
        token.nextToken();
        facJobQty[i] = (int) token.nval;
      }
      instance_DHFSP.setFacjobQty(facJobQty);
      //初始化作业每阶段的加工时间
      double[][] ptime = new double[proQty][jobQty];
      for (int i = 0; i < jobQty; i++) {
        for (int j = 0; j < proQty; j++) {
          token.nextToken();
          ptime[j][i] = (int) token.nval;
        }
      }
      instance_DHFSP.setPtime(ptime);
//      numberOfAvailableMachine_ = new int[numberOfProcess_];      //初始化每阶段可用机器数
//      token.nextToken() ;
//      numberOfMachine_ = (int) token.nval;                        //初始化机器总数
//      for (int i = 0;i<numberOfProcess_;i++){
//        token.nextToken();
//        numberOfAvailableMachine_[i] = (int) token.nval;        //每阶段可用的机器数赋值
//      }
//
//      startTime_ = new int [numberOfWorkpieces_][numberOfProcess_];
//
//      WorkPiecesTime_ = new int[numberOfWorkpieces_][numberOfProcess_] ;   //每行代表某工件的全部流程  每列代表某流程全部工件
//
//      token.nextToken() ;
//
//      for (int i = 0;i<numberOfWorkpieces_;i++){
//        for (int j =0;j<numberOfProcess_;j++){
//          token.nextToken();
//          WorkPiecesTime_[i][j] = (int)token.nval;
//          token.nextToken();
//        }
//      }
//      // Read the data
    } // try
    catch (Exception e) {
      System.err.println ("HFSP.readProblem(): error when reading data file "+e);
      System.exit(1);
    } // catch
    return instance_DHFSP;
  }
  
}
