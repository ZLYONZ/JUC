package producerAndConsumer;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PC3 { // Producer and Consumer

	public static void main(String[] args) {

		Data3 data = new Data3();

		new Thread(() -> {
			for (int i = 0; i < 10; i++) {
				data.printA();
			}
		}, "A").start();

		new Thread(() -> {
			for (int i = 0; i < 10; i++) {
				data.printB();
			}
		}, "B").start();

		new Thread(() -> {
			for (int i = 0; i < 10; i++) {
				data.printC();
			}
		}, "C").start();
	}
}

/**
 * A执行完调用B，B执行完调用C，C执行完调用A
 */
// 判断等待，业务，通知
class Data3 { // 数字 资源类

	private Lock lock = new ReentrantLock();
	private Condition condition1 = lock.newCondition();
	private Condition condition2 = lock.newCondition();
	private Condition condition3 = lock.newCondition();
	
	private int num = 1; // 1A 2B 3C

//	condition.await(); // 等待
//	condition.signalAll(); // 唤醒全部

	public void printA() {
		lock.lock();
		try {
			// 业务代码	判断-> 执行-> 通知
			while (num != 1) {
				// 等待
				condition1.await();
			}
			System.out.println(Thread.currentThread().getName() + "=>AAAAAAAA");
			//唤醒指定的，B
			num = 2;
			condition2.signalAll();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

	public void printB() {
		lock.lock();
		try {
			// 业务代码
			while (num != 2) {
				// 等待
				condition2.await();
			}
			System.out.println(Thread.currentThread().getName() + "=>BBBBBBBB");
			// 唤醒指定的，C
			num = 3;
			condition3.signalAll();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

	public void printC() {
		lock.lock();
		try {
			// 业务代码
			while (num != 3) {
				// 等待
				condition3.await();
			}
			System.out.println(Thread.currentThread().getName() + "=>CCCCCCCC");
			// 唤醒指定的，A
			num = 1;
			condition1.signalAll();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}
	
	// 生产线： 下单->支付->交易->物流
}