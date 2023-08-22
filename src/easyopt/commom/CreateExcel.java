package easyopt.commom;

import java.io.File;
import java.io.IOException;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * @author BieHongLi
 * @version 创建时间：2017年3月3日 下午4:03:18
 * 创建excel表格
 */
public class CreateExcel {

    public static void result(double[][] makespan)
            throws IOException, RowsExceededException, WriteException {
        //1:创建excel文件
        File file=new File("D:\\DBHFSP_L\\src\\Instance\\DHFSP05.xls");
        file.createNewFile();

        //2:创建工作簿
        WritableWorkbook workbook=Workbook.createWorkbook(file);
        //3:创建sheet,设置第二三四..个sheet，依次类推即可
//        WritableSheet sheet1=workbook.createSheet("解码对比", 0);
        WritableSheet sheet2=workbook.createSheet("算法对比", 1);

        //4：设置titles
//        String[] titles={"编号","活动","无活动"};
        String[] titles1={"运行次数","BIG","CIG","IGA","IGS","HEDFOA","DDE","GA","PSO"};

        //5:单元格
        Label label=null;
        Label label1=null;
        //6:给第一行设置列名
//        for(int i=0;i<titles.length;i++){
//            //x,y,第一行的列名
//            label=new Label(i,0,titles[i]);
//            //7：添加单元格
//            sheet1.addCell(label);
//        }
        //6.1 CIG的每次迭代的makespan
        for(int i=0;i<titles1.length;i++){
            //x,y,第一行的列名
            label1=new Label(i,0,titles1[i]);
            //7：添加单元格
            sheet2.addCell(label1);
        }
        //8：模拟数据库导入数据
//        for(int i=1;i<101;i++){
//            //添加编号，第二行第一列
//            label=new Label(0,i,i-1+"");
//            sheet1.addCell(label);
//
//            //添加账号
//            label=new Label(1,i,makespan_ac[i-1]+"");
//            sheet1.addCell(label);
//
//            //添加密码
//            label=new Label(2,i,makespan_noac[i-1]+"");
//            sheet1.addCell(label);
//        }
        //8.1 CIG的每次迭代的makespan

        for (int i = 1; i < makespan.length+1; i++) {
            //（列，行，内容）运行次数
            label=new Label(0,i,i+"");
            sheet2.addCell(label);

            for(int j=1;j<makespan[j].length+1;j++){
                //（列，行，内容）迭代结果
                label=new Label(j,i,makespan[i-1][j-1]+"");
                sheet2.addCell(label);
            }
        }

        //写入数据，一定记得写入数据，不然你都开始怀疑世界了，excel里面啥都没有
        workbook.write();
        //最后一步，关闭工作簿
        workbook.close();
    }

    //验证解码方式
    public static void result0(String[] result0_INS,double[][] result0)
            throws IOException, RowsExceededException, WriteException {
        //1:创建excel文件
        File file=new File("D:\\DBHFSP_L\\src\\Instance\\Result1.xls");
        file.createNewFile();

        //2:创建工作簿
        WritableWorkbook workbook=Workbook.createWorkbook(file);
        //3:创建sheet,设置第二三四..个sheet，依次类推即可
        WritableSheet sheet1=workbook.createSheet("解码对比", 0);

        //4：设置titles
//        String[] titles={"编号","活动","无活动"};
        String[] titles1={"Instance","ac","ac_min","ac_max","normal","no_min","no_max",};

        //5:单元格
        Label label=null;
        Label label1=null;
        //6.1 CIG的每次迭代的makespan
        for(int i=0;i<titles1.length;i++){
            //x,y,第一行的列名
            label1=new Label(i,0,titles1[i]);
            //7：添加单元格
            sheet1.addCell(label1);
        }
        //8.1 CIG的每次迭代的makespan
        for (int i = 1; i < result0.length+1; i++) {
            //（列，行，内容）运行次数
            label=new Label(0,i,result0_INS[i-1]+"");
            sheet1.addCell(label);
            label=new Label(1,i,result0[i-1][0]+"");
            sheet1.addCell(label);
            label=new Label(2,i,result0[i-1][1]+"");
            sheet1.addCell(label);
            label=new Label(3,i,result0[i-1][2]+"");
            sheet1.addCell(label);
            label=new Label(4,i,result0[i-1][3]+"");
            sheet1.addCell(label);
            label=new Label(5,i,result0[i-1][4]+"");
            sheet1.addCell(label);
            label=new Label(6,i,result0[i-1][5]+"");
            sheet1.addCell(label);
        }

        //写入数据，一定记得写入数据，不然你都开始怀疑世界了，excel里面啥都没有
        workbook.write();
        //最后一步，关闭工作簿
        workbook.close();
    }
    //参数灵敏度
    public static void result1(String[] result1_INS,double[][] result1)
            throws IOException, RowsExceededException, WriteException {
        //1:创建excel文件
        File file=new File("C:\\Users\\86157\\Desktop\\DBHFSP_L - 5\\src\\Instance\\Result2.xls");
        file.createNewFile();

        //2:创建工作簿
        WritableWorkbook workbook=Workbook.createWorkbook(file);
        //3:创建sheet,设置第二三四..个sheet，依次类推即可
        WritableSheet sheet1=workbook.createSheet("d对比", 0);

        //4：设置titles
//        String[] titles={"编号","活动","无活动"};
        String[] titles1={"Instance","a=0.6","a=0.7","a=0.8","a=0.9","a=1",
        "b=0.3","b=0.4","b=0.5","b=0.6","b=0.7"};

        //5:单元格
        Label label=null;
        Label label1=null;
        //6.1 CIG的每次迭代的makespan
        for(int i=0;i<titles1.length;i++){
            //x,y,第一行的列名
            label1=new Label(i,0,titles1[i]);
            //7：添加单元格
            sheet1.addCell(label1);
        }
        //8.1 CIG的每次迭代的makespan
        for (int i = 1; i < result1.length+1; i++) {
            //（列，行，内容）运行次数
            label=new Label(0,i,result1_INS[i-1]+"");
            sheet1.addCell(label);
            for (int j = 0; j < result1[0].length; j++) {
                label=new Label(j+1,i,result1[i-1][j]+"");
                sheet1.addCell(label);
            }
        }

        //写入数据，一定记得写入数据，不然你都开始怀疑世界了，excel里面啥都没有
        workbook.write();
        //最后一步，关闭工作簿
        workbook.close();
    }
}