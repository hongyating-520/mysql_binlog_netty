package com.example.netty_demo.MysqlSlave;

/*
 * @author ZZQ
 * @Date 2022/4/1 3:17 下午
 */
public class PSVMDEMO {
    private static Object lock = new Object();
    public static void main(String[] args) {
        int i = -1 & 0xFF;
        System.out.println(i);
        System.out.println(Integer.toBinaryString(i));
    }
    public static void await(){
        synchronized (lock){
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void anotify(){
        synchronized (lock){
            lock.notifyAll();
        }
    }
}
