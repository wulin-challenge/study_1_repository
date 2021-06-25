package org.wulin.jvm.jdk.thread.condition;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Test;

/**
  * As a heuristic to avoid indefinite writer starvation,
             * block if the thread that momentarily appears to be head
             * of queue, if one exists, is a waiting writer.  This is
             * only a probabilistic effect since a new reader will not
             * block if there is a waiting writer behind other enabled
             * readers that have not yet drained from the queue.
             */
public class ReentrantLockStudy {
	
	private Lock lock = new ReentrantLock();
	
	Condition newCondition = lock.newCondition();
	
	private int i = 0;
	
	@Test
	public void lockTest() {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				addCount2();
			}
		});
		thread.start();
		addCount();
		System.out.println("完成");
	}
	
	private void addCount() {
		System.out.println("准备执行...");
		try {
//			lock.tryLock();
			lock.lock();
			++i;
			newCondition.await();
			System.out.println("这是一个耗时操作!,我是1,需要等待2完成");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		};
	}
	
	private void addCount2() {
		System.out.println("准备执行...");
		try {
//			lock.tryLock();
			lock.lock();
			++i;
			newCondition.signal();
			System.out.println("这是一个耗时操作!,我是2,通知1可以继续了");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
			
		};
	}

}
