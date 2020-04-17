package org.wulin.jvm.jdk.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import org.junit.Test;

/**
 * Java 6的并发编程包中的SynchronousQueue是一个没有数据缓冲的BlockingQueue，
 * 生产者线程对其的插入操作put必须等待消费者的移除操作take，反过来也一样。
 * @author wulin
 *
 */
public class SynchronousQueueTest {
	
	private BlockingQueue<Integer> queue = new SynchronousQueue<Integer>();
	
	/**
	 * 同步生产者和消费者测试
	 */
	@Test
	public void synchronousProducerAndConsumerTest() {
		
		//启动消费者
		consumer();
		
		//模拟生产者
		for (int i = 0; i < 100; i++) {
			try {
				System.out.println("生产者: "+i);
				queue.put(i);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("完毕!,退出线程");
	}
	
	private void consumer() {
		Thread thread = new Thread(()-> {
			try {
				Integer take = queue.take();
				System.out.println("消费者: "+take);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		thread.start();
	}

}
