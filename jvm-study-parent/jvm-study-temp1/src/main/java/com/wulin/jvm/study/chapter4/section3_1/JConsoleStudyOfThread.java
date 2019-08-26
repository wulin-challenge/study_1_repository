package com.wulin.jvm.study.chapter4.section3_1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 学习就console工具
 * <p> vm 参数: -Xms100m -Xmx100m -XX:+UseSerialGC
 * @author wulin
 *
 */
public class JConsoleStudyOfThread {
	
	
	/**
	 * 线程死循环演示
	 */
	public static void createBusyThread() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				while(true) {//第19行
					
				}
			}
		},"testBusyThread");
		
		thread.start();
	}

	/**
	 * 线程锁等待演示
	 * @param lock
	 */
	public static void createLockThread(final Object lock) {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				synchronized(lock) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		},"testLockThread");
		
		thread.start();
	}
	
	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		br.readLine();
		createBusyThread();
		br.readLine();
		
		Object lock = new Object();
		createLockThread(lock);
	}
}
