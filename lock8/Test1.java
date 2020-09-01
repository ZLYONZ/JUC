package lock8;

import java.util.concurrent.TimeUnit;

/**
 * 8锁，就是关于锁的8个问题
 * 1. 标准情况下，两个线程先打印 sendSms or call？	1 sendSms 2 call
 * 2. sendSms延迟4秒，两个线程先打印 sendSms or call？	1 sendSms 2 call
 */
public class Test1 {

	public static void main(String[] args) {

		Phone1 phone = new Phone1();
	
		// 锁的存在
		new Thread(()->{
			phone.sendSms();
		},"A").start();
		
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		new Thread(()->{
			phone.call();
		},"B").start();
	}

}

class Phone1{
	
	// synchronized 锁的对象是方法的调用者！
	// 两个方法用的是同一把锁，谁先拿到谁先执行！
	public synchronized void sendSms() {
		try {
			TimeUnit.SECONDS.sleep(4);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("sendSms");
	}
	
	public synchronized void call() {
		System.out.println("call");
	}
	
}