package saleTicket;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 真正的多线程开发，公司中的开发 线程就是一个单独的资源类，没有任何的附属操作 
 * 1. 属性、方法
 *
 */
public class Demo2 {

	public static void main(String[] args) {

		// 并发：多线程操作同一个资源类，
		Ticket2 ticket = new Ticket2();

		// @FunctionalInterface 函数式接口，jdk1.8 Lambda表达式 (参数)->{代码}
		new Thread(() -> {for (int i = 1; i < 60; i++) ticket.sale();}, "A").start();
		new Thread(() -> {for (int i = 1; i < 60; i++) ticket.sale();}, "B").start();
		new Thread(() -> {for (int i = 1; i < 60; i++) ticket.sale();}, "C").start();
	}
}

// Lock三部曲
// 1. new ReentrantLock()
// 2. lock.lock(); 加锁
// 3. lock.unlock(); 解锁
class Ticket2 {
	
	// 属性、方法
	private int num = 50;
	Lock lock = new ReentrantLock();

	// 卖票的方式
	public void sale() {

		lock.lock(); // 加锁
		try {
			// 业务代码
			if (num > 0) {
				System.out.println(Thread.currentThread().getName() + "卖出了" + (num--) + "票，剩余：" + num);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock(); // 解锁
		}
	}
}