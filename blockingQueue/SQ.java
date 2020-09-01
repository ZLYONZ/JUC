package blockingQueue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * 同步队列 和其他的 BlockingQueue 不一样，SynchronousQueue 不存储元素
 * put了一个元素，必须从里面先take，否则不能再put
 */
public class SQ {

	public static void main(String[] args) throws InterruptedException {

		BlockingQueue<String> bq = new SynchronousQueue<>(); // 同步队列

		new Thread(() -> {
			try {
				System.out.println(Thread.currentThread().getName() + " put 1");
				bq.put("1");
				System.out.println(Thread.currentThread().getName() + " put 2");
				bq.put("2");
				System.out.println(Thread.currentThread().getName() + " put 3");
				bq.put("3");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}, "T1").start();

		new Thread(() -> {
			try {
				TimeUnit.SECONDS.sleep(2);
				System.out.println(Thread.currentThread().getName() + "=>" + bq.take());
				TimeUnit.SECONDS.sleep(2);
				System.out.println(Thread.currentThread().getName() + "=>" + bq.take());
				TimeUnit.SECONDS.sleep(2);
				System.out.println(Thread.currentThread().getName() + "=>" + bq.take());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}, "T2").start();
	}
}
