package org.wulin.jvm.jdk.thread;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class TestScheduled {

	private ScheduledExecutorService consumer = Executors.newScheduledThreadPool(5, new ThreadFactoryImpl("BaseAsynchronousBatchCommitCode"));
	
	@Test
	public void testFixedRate() throws IOException {
		
		consumer.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				System.out.println();
			}
		}, 2, 3, TimeUnit.SECONDS);
		System.in.read();
	}
}
