package org.wulin.jvm.study.optimization.observer.simple;

import java.util.Observable;
import java.util.Observer;

public class Subscriber implements Observer {
	
	Observable observable;
	
	/**
	 * 构造器，设置被观察者，并将自己设为观察者
	 * @param observable 被观察者
	 */
	public Subscriber(Observable observable) {
		this.observable = observable;
		observable.addObserver(this);
	}
 
	/**
	 * 当被观察者改变时，主动调用
	 */
	@Override
	public void update(Observable o, Object arg) {
		System.out.println("订阅者收到新文章：" + arg);
	}
	
	/**
	 * 用于主动从被观察者拉数据
	 * @return 最新文章标题
	 */
	public String get() {
		if (observable instanceof WeChatSubscription) {
			WeChatSubscription weChatSubscription = (WeChatSubscription) observable;
			return weChatSubscription.getNewest();
		}
		throw new UnsupportedOperationException("不支持的操作！");
	}
 
}
