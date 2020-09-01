package readWriteLock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ReadWriteLock
 */
public class RWL {

	public static void main(String[] args) {

		MyCacheLock lock = new MyCacheLock();

		// 写入
		for (int i = 1; i <= 5; i++) {
			final int temp = i;
			new Thread(() -> {
				lock.put(temp + "", temp + "");
			}, String.valueOf(i)).start();
		}

		// 读取
		for (int i = 1; i <= 5; i++) {
			final int temp = i;
			new Thread(() -> {
				lock.get(temp + "");
			}, String.valueOf(i)).start();
		}
	}
}

/**
 * 自定义缓存
 */
class MyCache {

	private volatile Map<String, Object> map = new HashMap<>();

	// 存 / 写
	public void put(String key, Object value) {
		System.out.println(Thread.currentThread().getName() + "写入" + key);
		map.put(key, value);
		System.out.println(Thread.currentThread().getName() + "写入成功");
	}

	// 取 / 读
	public void get(String key) {
		System.out.println(Thread.currentThread().getName() + "读取" + key);
		Object obj = map.get(key);
		System.out.println(Thread.currentThread().getName() + "读取成功");
	}
}

// 加锁
class MyCacheLock {

	private volatile Map<String, Object> map = new HashMap<>();

	// 读写锁，更加细粒度的控制
	private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private Lock lock = new ReentrantLock();

	// 存，写入的时候，只希望同时只有一个线程写
	public void put(String key, Object value) {

		readWriteLock.writeLock().lock();

		try {
			System.out.println(Thread.currentThread().getName() + "写入" + key);
			map.put(key, value);
			System.out.println(Thread.currentThread().getName() + "写入成功");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			readWriteLock.writeLock().unlock();
		}
	}

	// 取，读的时候，所有人都可以读
	public void get(String key) {

		readWriteLock.readLock().lock();

		try {
			System.out.println(Thread.currentThread().getName() + "读取" + key);
			Object obj = map.get(key);
			System.out.println(Thread.currentThread().getName() + "读取成功");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			readWriteLock.readLock().unlock();
		}
	}
}