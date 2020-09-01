package lock8;

import java.util.concurrent.TimeUnit;

/**
 * 8锁，就是关于锁的8个问题
 * 1. 标准情况下，两个线程先打印 sendSms or call？	1 sendSms 2 call
 * 2. sendSms延迟4秒，两个线程先打印 sendSms or call？	1 sendSms 2 call
 * 3. 增加了一个普通方法hello后，两个线程先打印 sendSms or hello？	1 hello 2 sendSms		
 * 4. 两个对象下，两个线程先打印 sendSms or hello？	1 hello 2 sendSms		
 */
public class Test2 {

	public static void main(String[] args) {

		// 两个对象，两个调用者，两把锁
		Phone2 phone1 = new Phone2();
		Phone2 phone2 = new Phone2();

		// 锁的存在
		new Thread(() -> {
			phone1.sendSms();
		}, "A").start();

		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		new Thread(() -> {
			phone2.call();
		}, "B").start();
	}

}

class Phone2 {

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
	
	// 这里没有锁，不受锁的影响
	public void hello() {
		System.out.println("hello");
	}
}