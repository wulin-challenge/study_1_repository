package org.wulin.jvm.jdk.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class TestThreadPoolExecutor {
	
	private final BlockingQueue<Runnable> consumeRequestQueue = new LinkedBlockingQueue<Runnable>(5);

	private ExecutorService consumer1 = new ThreadPoolExecutor(1, 2,
            5, TimeUnit.SECONDS,
            consumeRequestQueue,
            new ThreadFactoryImpl("BaseAsynchronousBatchCommitCode"),
            new SubmitBlockingPolicy());
	
	@Test
	public void producerAndConsumerTest() {
		procucer();
	}
	
	private void procucer() {
		for (int i = 0; i < 20; i++) {
			consumer1.submit(new ConsumerTask(i));
		}
		
	}
	
	/**
	 * 消费者
	 * @author wulin
	 *
	 */
	private static class ConsumerTask implements Runnable{
		private Integer i;
		
		public ConsumerTask(Integer i) {
			this.i = i;
		}

		@Override
		public void run() {
			String name = Thread.currentThread().getName();
			System.out.println("自动消费 : "+name+" : "+i);
		}
	}
	
}
