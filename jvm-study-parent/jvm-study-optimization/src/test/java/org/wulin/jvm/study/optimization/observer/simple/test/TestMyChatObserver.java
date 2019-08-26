package org.wulin.jvm.study.optimization.observer.simple.test;

import org.wulin.jvm.study.optimization.observer.simple.Subscriber;
import org.wulin.jvm.study.optimization.observer.simple.WeChatSubscription;

public class TestMyChatObserver {
 
	public static void main(String[] args) {
		// 创建可观察者：这里是微信公众号
		WeChatSubscription observable = new WeChatSubscription();
		
		// 创建观察者：这里是微信用户
		Subscriber observer = new Subscriber(observable);
		
		// 可观察者发生改变：发布新文章
		observable.publish("观察者模式");
		
		// 观察者自己拉取最新文章
		String title = observer.get();
		System.out.println("观察者自己拉取最新文章：" + title);
 
	}
 
}
