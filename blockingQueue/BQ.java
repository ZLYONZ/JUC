package blockingQueue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class BQ {

	public static void main(String[] args) throws InterruptedException {
		test4();
	}

	/**
	 * 抛出异常
	 */
	public static void test1() {
		// 队列的大小
		ArrayBlockingQueue<Object> bq = new ArrayBlockingQueue<>(3);

		System.out.println(bq.add("a"));
		System.out.println(bq.add("b"));
		System.out.println(bq.add("c"));

		// IllegalStateException: Queue full 抛出异常!
		// System.out.println(bq.add("d"));
		
		System.out.println(bq.element()); // 查看队首元素
		System.out.println("-------------------------------");

		System.out.println(bq.remove());
		System.out.println(bq.remove());
		System.out.println(bq.remove());

		// NoSuchElementException 抛出异常!
		// System.out.println(bq.remove());
	}

	/**
	 * 有返回值，不抛出异常
	 */
	public static void test2() {
		// 队列的大小
		ArrayBlockingQueue<Object> bq = new ArrayBlockingQueue<>(3);
		
		System.out.println(bq.offer("a"));
		System.out.println(bq.offer("b"));
		System.out.println(bq.offer("c"));
		
		// 返回false，不抛出异常
		//System.out.println(bq.offer("d")); 
		
		System.out.println("---------------------");
		
		System.out.println(bq.poll());
		System.out.println(bq.poll());
		System.out.println(bq.poll());
		
		System.out.println(bq.peek()); // 检测队首元素，返回null

		// 返回null，不抛出异常 
		// System.out.println(bq.poll());
	}
	
	/**
	 * 阻塞等待（一直阻塞）
	 * @throws InterruptedException 
	 */
	public static void test3() throws InterruptedException {
		// 队列的大小
		ArrayBlockingQueue<Object> bq = new ArrayBlockingQueue<>(3);
		
		bq.put("a");
		bq.put("b");
		bq.put("c");

		// 队列没有位置，一直阻塞
		// bq.put("d"); 		
		
		System.out.println("---------------------");
		
		System.out.println(bq.take());
		System.out.println(bq.take());
		System.out.println(bq.take());
		
		// 没有元素，一直阻塞
		// System.out.println(bq.take());
	}
	
	/**
	 * 超时等待
	 * @throws InterruptedException 
	 */
	public static void test4() throws InterruptedException {
		// 队列的大小
		ArrayBlockingQueue<Object> bq = new ArrayBlockingQueue<>(3);
		
		bq.offer("a");
		bq.offer("b");
		bq.offer("c");
		
		// 等待超过后退出
		// bq.offer("d", 2, TimeUnit.SECONDS);
		
		System.out.println("---------------------");
		
		System.out.println(bq.poll());
		System.out.println(bq.poll());
		System.out.println(bq.poll());
		
		// 等待超过后退出
		// bq.poll(2, TimeUnit.SECONDS);
	}
}