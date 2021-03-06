package com.wulin.jvm.study.chapter4.section3_1;

import java.util.ArrayList;
import java.util.List;

/**
 * 学习就console工具
 * <p> vm 参数: -Xms100m -Xmx100m -XX:+UseSerialGC
 * @author wulin
 *
 */
public class JConsoleStudyOfHeap {
	
	static class OOMObject{
		public byte[] placeholder = new byte[64 * 1024];
	}
	
	public static void fillHeap(int num) throws InterruptedException {
		List<OOMObject> list = new ArrayList<OOMObject>();
		
		for (int i = 0; i < num; i++) {
			//稍作延时,令监视曲线的变化更加明显
			Thread.sleep(50);
			list.add(new OOMObject());
		}
		System.gc();
	}
	
	public static void main(String[] args) throws InterruptedException {
		fillHeap(2000);
	}

}
