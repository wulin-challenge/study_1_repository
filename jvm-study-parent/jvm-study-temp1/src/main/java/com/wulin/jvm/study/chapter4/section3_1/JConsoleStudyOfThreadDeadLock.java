package com.wulin.jvm.study.chapter4.section3_1;

/**
 * 学习就console工具
 * <p> vm 参数: -Xms100m -Xmx100m -XX:+UseSerialGC
 * @author wulin
 *
 */
public class JConsoleStudyOfThreadDeadLock {
	
	/**
	 * 线程死锁等待演示
	 * @author wulin
	 *
	 */
	static class SynAddRunnable implements Runnable{

		/**
		 * a和b的数字为1/2
		 */
		int a,b;
		
		public SynAddRunnable(int a, int b) {
			super();
			this.a = a;
			this.b = b;
		}

		@Override
		public void run() {
			
			/**
			 * 造成死锁的原因是Integer.valueOf()方法基于减少对象创建次数和节省内存的考虑,[-128,127]之间的数字
			 * 会被缓存,当valueOf()方法传入参数在这个范围之内,将直接返回缓存中的对象.
			 */
			synchronized(Integer.valueOf(a)) {
				synchronized(Integer.valueOf(b)) {
					System.out.println(a + b);
				}
			}
		}
		
		public static void main(String[] args) {
			for (int i = 0; i < 100; i++) {
				new Thread(new SynAddRunnable(1, 2)).start();
				new Thread(new SynAddRunnable(2, 1)).start();
			}
		}
	}
}
