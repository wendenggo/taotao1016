package xyz.taotao.common.util;

import java.util.Random;

/**
 * 各种id生成策略
 * <p>Title:IDUtils</p>
 */
public class IDUtils {

    /*
    图片名生成
     */
    public static String genImageName(){
        //取当前时间的长整形值包含毫秒
        long millis = System.currentTimeMillis();
        //Long millis = System.nanoTime();
        //加上三位随机数
        Random random = new Random();
        int end3 = random.nextInt(999);
        String str = millis + String.format("%03d",end3);
        return str;
    }

    public static long genItemId(){
        //取当前时间的长整形值包含毫秒
        long millis = System.currentTimeMillis();
        //long millis = System.nanoTime();
        //加上两位随机数
        Random random = new Random();
        int end2 = random.nextInt(99);
        //如果不足两位面前补0
        String str = millis + String.format("%02d",end2);
        long id = new Long(str);
        return  id;
    }

    public static void main(String[] args) {
        for (int i=0;i<100;i++){
            System.out.println(genItemId());
        }
    }

}
