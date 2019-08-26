package org.wulin.jvm.study.optimization.observer.simple;

import java.util.Observable;

public class WeChatSubscription extends Observable {
 
	private String newest;
	
	/**
	 * 发布文章
	 * @param title 文章标题
	 */
	public void publish(String title) {
		System.out.println("微信公众号发布了文章：" + title);
		this.newest = title;
		// 设置改变
		setChanged();
		// 提醒观察者
		notifyObservers(title);
	}
	
	/**
	 * 提供数据的获取接口，以便观察者主动拉数据
	 * @return
	 */
	public String getNewest() {
		return newest;
	}
}