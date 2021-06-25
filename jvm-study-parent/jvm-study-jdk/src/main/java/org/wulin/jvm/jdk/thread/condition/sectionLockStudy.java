package org.wulin.jvm.jdk.thread.condition;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Test;

/**
 * 分段锁学习,这里主要学习String作为锁的对象
 * @author wulin
 *
 */
public class sectionLockStudy {
	private Lock lock = new ReentrantLock();
	
	@Test
	public void testStringSectionLock() throws IOException {
		for (int i = 0; i < 10; i++) {
			Thread thread = new Thread(()->{
				executeLock();
			});
			thread.setName(i+"_thread");
			thread.start();
		}
		
		System.in.read();
	}
	
	private void executeLock() {
		String name = Thread.currentThread().getName();
		System.out.println("======="+name);
	
		String monitor = getLockMonitor();
//		String monitor = "111";
		
		synchronized (monitor) {
			
			System.out.println(getLockMonitor()+":"+name);
			System.out.println("----------------");
		}
	}
	
	
	public String getLockMonitor() {
		Random random = new Random();
//		int val = random.nextInt(2);
		int val = new Integer(22);;
		String monitor = val+"_monitor";
		System.out.println(monitor+":"+monitor.hashCode());
		return "111"; //由于String有常量池,因此没有都是同一个对象
//		return new String("1111111111111"); //由于每次地址都不一样,因此是不同的对象
	}

}


