package lock8;

import java.util.concurrent.TimeUnit;

/**
 * 8锁，就是关于锁的8个问题
 * 1. 标准情况下，两个线程先打印 sendSms or call？	1 sendSms 2 call
 * 2. sendSms延迟4秒，两个线程先打印 sendSms or call？	1 sendSms 2 call
 * 3. 增加了一个普通方法hello后，两个线程先打印 sendSms or hello？	1 hello 2 sendSms		
 * 4. 两个对象下，两个线程先打印 sendSms or call？	1 call 2 sendSms		
 * 5. 增加两个静态的同步方法，两个线程先打印 sendSms or call？	1 sendSms 2 call		
 * 6. 两个对象下，两个静态的同步方法，两个线程先打印 sendSms or call？	1 sendSms 2 call		
 */
public class Test3 {

	@SuppressWarnings("static-access")
	public static void main(String[] args) {

		// 两个对象，两个调用者，两把锁
		Phone3 phone1 = new Phone3();
		Phone3 phone2 = new Phone3();

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

// Phone3是唯一的一个class对象
class Phone3 {

	// synchronized 锁的对象是方法的调用者！
	// static 静态方法
	// 类一加载就有了！锁的是Class
	public static synchronized void sendSms() {
		try {
			TimeUnit.SECONDS.sleep(4);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("sendSms");
	}

	public static synchronized void call() {
		System.out.println("call");
	}
}