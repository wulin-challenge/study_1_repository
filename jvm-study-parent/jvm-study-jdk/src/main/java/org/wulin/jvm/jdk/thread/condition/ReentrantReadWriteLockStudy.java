package org.wulin.jvm.jdk.thread.condition;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.junit.Test;

public class ReentrantReadWriteLockStudy {
	private  int number = 0;
	 
    // 可重入读写锁
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    
    @Test
	public void lockTest() {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				write(5);
				write(10);
			}
		});
		thread.start();
		read();
		read();
		System.out.println("完成");
	}
 
    public void read(){
        lock.readLock().lock();
        try {
            System.out.println("当前的值为"  + this.number);
        } finally {
            lock.readLock().unlock();
        }
    }
 
    public void write(Integer value){
        lock.writeLock().lock();
        try {
            System.out.println(String.format("当前线程{%s}正在进行写操作{%s}", Thread.currentThread().getName(),value));
            this.number = value;
        } finally {
            lock.writeLock().unlock();
        }
 
    }

}
