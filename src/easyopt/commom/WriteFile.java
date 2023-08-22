package easyopt.commom;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WriteFile {



    public static void write_DHFSP_Instance(int jobQty,int proQty,int facQty)
    {

        int[] fac_jobQty = new int[facQty];
        for (int i = 0; i < fac_jobQty.length; i++) {
            fac_jobQty[i] = jobQty/facQty; //确保没有余数
        }
        int[][] protime = new int[jobQty][proQty];
        for (int i = 0; i < jobQty; i++) {
            for (int j = 0; j < proQty; j++) {
                protime[i][j] = (int)(Math.random()*100+1);
            }
        }


        try {
            String content;
            content= jobQty +" "+ proQty+" "+ facQty +"\n";

            for (int i = 0; i < facQty; i++) {

                if(i==facQty-1){
                    content += fac_jobQty[i] + "\n";
                }else content += fac_jobQty[i] + " ";

            }

            for (int i = 0; i < jobQty; i++) {
                for (int j = 0; j < proQty; j++) {

                    if(j==proQty-1){
                        content +=protime[i][j] + "\n";
                    }else content += protime[i][j] + " ";
                }
            }

            File file = new File("D:\\DBHFSP_L\\src\\Instance\\Instance_"+jobQty+"_"+proQty+"_"+facQty+".txt");
            if(!file.exists()){
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fileWriter);
            bw.write(content);
            bw.close();
            System.out.println("finish");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
