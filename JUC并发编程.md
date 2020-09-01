

# 多线程进阶 => JUC并发编程



## 1 什么是JUC？

**源码+官方文档** 面试高频问！

java.util 工具包、包、分类

**业务：普通的线程代码 Thread**

**Runnable** 没有返回值，效率相比于callable较低！



## 2 线程和进程

### 线程

一个程序	QQ.exe	Music.exe	程序的集合

一个进程可以包含多个线程，至少包含一个！

Java默认有几个线程？2个	main	GC



### 线程

开了一个进程Typora，写字，自动保存(线程负责)

对于Java而言：Thread、Runnable、callable



**Java真的可以开启线程吗？**不能

```java
public synchronized void start() {
        /**
         * This method is not invoked for the main method thread or "system"
         * group threads created/set up by the VM. Any new functionality added
         * to this method in the future may have to also be added to the VM.
         *
         * A zero status value corresponds to state "NEW".
         */
        if (threadStatus != 0)
            throw new IllegalThreadStateException();

        /* Notify the group that this thread is about to be started
         * so that it can be added to the group's list of threads
         * and the group's unstarted count can be decremented. */
        group.add(this);

        boolean started = false;
        try {
            start0();
            started = true;
        } finally {
            try {
                if (!started) {
                    group.threadStartFailed(this);
                }
            } catch (Throwable ignore) {
                /* do nothing. If start0 threw a Throwable then
                  it will be passed up the call stack */
            }
        }
    }
	// 本地方法，底层的C++，Java无法直接操作硬件
    private native void start0();
```



### 并发，并行

并发编程：并发、并行

并发（多线程操作同一个资源）

- CPU一核，模拟出来多条线程

并行（多个人一起行走）

- CPU多核，多个线程可以同时执行；线程池

```java
public class Test1 {
	public static void main(String[] args) {
		// 获取CPU的核数
		// CPU 密集型，IO密集型
		System.out.println(Runtime.getRuntime().availableProcessors());
	}
}
```

并发编程的本质：**充分利用CPU的资源**

所有的公司都很看重！



**线程有几个状态**

```java
public enum State {
     
   		// 新生
        NEW,

        // 运行
        RUNNABLE,

        // 阻塞
        BLOCKED,

        // 等待
        WAITING,

        // 超时等待
        TIMED_WAITING,

        // 终止
        TERMINATED;
    }
```



### wait/sleep 区别

1. **来自不同的类**

   wait => Object

   sleep => Thread

   企业当中，

2. **关于锁的释放**

   wait会释放锁，sleep睡觉了，抱着锁睡觉，不会释放！

3. **使用的范围是不同的**

   wait 必须在同步代码块中

   sleep 可以在任何地方睡

4. **是否需要捕获异常**

   wait 不需要捕获异常

   sleep 必须要捕获异常



## 3 Lock锁（重点）

### 传统 Synchronized

```java
package juc;

/**
 * 真正的多线程开发，公司中的开发 线程就是一个单独的资源类，没有任何的附属操作 
 * 1. 属性、方法
 */
public class SaleTicketDemo1 {

	public static void main(String[] args) {

		// 并发：多线程操作同一个资源类，
		Ticket ticket = new Ticket();

		// @FunctionalInterface 函数式接口，jdk1.8 Lambda表达式 (参数)->{代码}
		new Thread(() -> {
			for (int i = 1; i < 60; i++) {
				ticket.sale();
			}
		}, "A").start();

		new Thread(() -> {
			for (int i = 1; i < 60; i++) {
				ticket.sale();
			}
		}, "B").start();

		new Thread(() -> {
			for (int i = 1; i < 60; i++) {
				ticket.sale();
			}
		}, "C").start();
	}
}

// 资源类 OOP
class Ticket {
	// 属性、方法
	private int num = 50;

	// 卖票的方式
	// synchronized 本质：队列，锁
	public synchronized void sale() {
		if (num > 0) {
			System.out.println(Thread.currentThread().getName() + "卖出了" + (num--) + "票，剩余：" + num);
		}
	}
}

```



### Lock 接口

```shell
Lock l = ... l.lock(); 
try {
// access the resource protected by this lock
} finally { 
l.unlock();
}
```



### Interface Lock

```shell
所有已知实现类：
ReentraLock						 可重入锁
ReentrantReadWriteLock.ReadLock		读锁
ReentrantReadWriteLock.WriteLock	写锁
```



**公平锁**：十分公平，可以先来后到

**非公平锁**：十分不公平，可以插队

```shell
public ReentrantLock() {
        sync = new NonfairSync();
    }

    /**
     * Creates an instance of {@code ReentrantLock} with the
     * given fairness policy.
     *
     * @param fair {@code true} if this lock should use a fair ordering policy
     */
    public ReentrantLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
    }
```



```java
package juc;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 真正的多线程开发，公司中的开发 线程就是一个单独的资源类，没有任何的附属操作 
 * 1. 属性、方法
 *
 */
public class SaleTicketDemo2 {

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
```



### Synchronized 和 Lock 的区别

1、synchronized 是内置的Java关键字；Lock 是一个Java类

2、synchronized 无法判断获取锁的状态；Lock 可以判断是否获取到了锁

3、synchronized 会自动释放锁；Lock 必须要手动释放锁！如果不释放锁，死锁

4、synchronized 线程1（获得锁） 线程2（等待，傻傻的等）；Lock 就不一定会等下去

5、synchronized 可重入锁，不可以中断的，非公平；Lock 可重入锁，可以判断锁，非公平（可以自己设置）

6、synchronized 适合锁少量的代码同步问题；Lock 适合锁大量的同步代码



### 锁

是什么？如何判断锁的是谁？



## 4 生产者和消费者问题

Synchronized版	wait	notify

JUC版	lock	unlock

面试：单例模式、排序算法、生产者和消费者、死锁



### 4.1 Synchronized版

```java
package juc;

/**
 * 线程之间的通信问题：生产者和消费者问题	等待唤醒，通知唤醒
 * 线程交替执行  A & B 操作同一个变量   num=0
 * A num+1
 * B num-1
 * 
 */
public class PC { // Producer and Consumer

	public static void main(String[] args) {

		Data data = new Data();

		new Thread(() -> {
			for (int i = 0; i < 10; i++) {
				try {
					data.increment();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, "A").start();

		new Thread(() -> {
			for (int i = 0; i < 10; i++) {
				try {
					data.decrement();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, "B").start();
	}
}

// 判断等待，业务，通知
class Data { // 数字 资源类

	private int num = 0;

	// +1
	public synchronized void increment() throws InterruptedException {
		if (num != 0) {
			// 等待
			this.wait();
		}
		num++;
		System.out.println(Thread.currentThread().getName() + "=>" + num);
		// 通知其他线程，我+1完毕了
		this.notifyAll();
	}

	// -1
	public synchronized void decrement() throws InterruptedException {
		if (num == 0) {
			// 等待
			this.wait();
		}
		num--;
		System.out.println(Thread.currentThread().getName() + "=>" + num);
		// 通知其他线程，我-1完毕了
		this.notifyAll();
	}
}
```

**问题存在**：A B C D 4个线程！虚假唤醒

![image-20200824154526563](C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20200824154526563.png)



#### if 改成 while判断

```java
// +1
	public synchronized void increment() throws InterruptedException {
		while (num != 0) {
			// 等待
			this.wait();
		}
		num++;
		System.out.println(Thread.currentThread().getName() + "=>" + num);
		// 通知其他线程，我+1完毕了
		this.notifyAll();
	}

	// -1
	public synchronized void decrement() throws InterruptedException {
		while (num == 0) {
			// 等待
			this.wait(); 
		}
		num--;
		System.out.println(Thread.currentThread().getName() + "=>" + num);
		// 通知其他线程，我-1完毕了
		this.notifyAll();
	}
```



### 4.2 JUC版

![image-20200824105824472](C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20200824105824472.png)



#### 通过 Lock 找到 condition

![image-20200824105913987](C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20200824105913987.png)

```java
package juc;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PC2 { // Producer and Consumer

	public static void main(String[] args) {

		Data2 data = new Data2();

		new Thread(() -> {
			for (int i = 0; i < 10; i++) {
				try {
					data.increment();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, "A").start();

		new Thread(() -> {
			for (int i = 0; i < 10; i++) {
				try {
					data.decrement();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, "B").start();

		new Thread(() -> {
			for (int i = 0; i < 10; i++) {
				try {
					data.increment();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, "C").start();

		new Thread(() -> {
			for (int i = 0; i < 10; i++) {
				try {
					data.decrement();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, "D").start();
	}
}

// 判断等待，业务，通知
class Data2 { // 数字 资源类

	private int num = 0;

	Lock lock = new ReentrantLock();
	Condition condition = lock.newCondition();

//	condition.await(); // 等待
//	condition.signalAll(); // 唤醒全部

	// +1
	public void increment() throws InterruptedException {

		lock.lock();
		try {
			// 业务代码
			while (num != 0) {
				// 等待
				condition.await();
			}
			num++;
			System.out.println(Thread.currentThread().getName() + "=>" + num);
			// 通知其他线程，我+1完毕了
			condition.signalAll();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

	// -1
	public void decrement() throws InterruptedException {
		
		lock.lock();
		try {
			while (num == 0) {
				// 等待
				condition.await();
			}
			num--;
			System.out.println(Thread.currentThread().getName() + "=>" + num);
			// 通知其他线程，我-1完毕了
			condition.signalAll();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}
}
```

**任何一个新的技术，绝对不仅仅只是覆盖了原来的技术，优势和补充！**



### Condition 精准的通知和唤醒线程

```java
package juc;

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
}
```



## 5 8锁现象

如何判断锁的是谁？永远的知道什么锁，锁到底锁的是谁！

对象	Class



### 八大场景

1. 场景一

   ```shell
    * 标准情况下 是先sendEmail()　还是先callPhone()?
    * 答案：sendEmail
    * 解释：被 synchronized 修饰的方式，锁的对象是方法的调用者
    * 所以说这里两个方法调用的对象是同一个，先调用的先执行！
   ```

2. 场景二

   ```shell
    * sendEmail()休眠三秒后  是先执行sendEmail() 还是 callPhone()？
    * 答案： sendEmail
    * 解释：被 synchronized 修饰的方式，锁的对象是方法的调用者
    * 所以说这里两个方法调用的对象是同一个，先调用的先执行！
   ```

3. 场景三

   ```shell
    * 被synchronized 修饰的方式和普通方法 先执行sendEmail() 还是 callPhone()？
    * 答案： callPhone
    * 解释：新增加的这个方法没有 synchronized 修饰，不是同步方法，不受锁的影响！
   ```

4. 场景四

   ```shell
    * 被synchronized 修饰的不同方法 先执行sendEmail() 还是callPhone()？
    * 答案：callPhone
    * 解释：被synchronized 修饰的不同方法 锁的对象是调用者
    * 这里锁的是两个不同的调用者，所有互不影响，按时间先后执行
   ```

5. 场景五

   ```shell
    * 两个静态同步方法 都被synchronized 修饰 是先sendEmail() 还是callPhone()？
    * 答案：sendEmial
    * 解释：只要方法被 static 修饰，锁的对象就是 Class模板对象,这个则全局唯一！
    * 所以说这里是同一个锁，并不是因为synchronized  这里程序会从上往下依次执行
   ```

6. 场景六

   ```shell
    * 被synchronized 修饰的普通方法和静态方法  是先sendEmail() 还是 callPhone()?
    * 答案：callPhone
    * 解释：只要被static修饰锁的是class模板, 而synchronized 锁的是调用的对象
    * 这里是两个锁互不影响，按时间先后执行
   ```

7. 场景七

   ```shell
    * 同被static+synchronized 修饰的两个方法，是先sendEmail()还是callPhone()?
    * 答案：sendEmail
    * 解释：只要方法被 static 修饰，锁的对象就是 Class模板对象,这个则全局唯一
    * 所以说这里是同一个锁，并不是因为synchronized
   ```

8. 场景八

   ```shell
    * 一个被static+synchronized 修饰的方法和普通的synchronized方法，先执行sendEmail()还是callPhone()？
    * 答案：callPhone()
    * 解释： 只要被static 修饰的锁的就是整个class模板
    * 这里一个锁的是class模板 一个锁的是调用者 
    * 所以锁的是两个对象，互不影响，按时间先后执行
   ```

   

### 小结

new	this	具体的一个手机

static	class	唯一的一个模板



## 6 集合类不安全

### 6.1 list 不安全

```shell
package unsafe;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

// java.util.ConcurrentModificationException 并发修改异常
public class ListTest {

	public static void main(String[] args) {
		
		// 并发下 ArrayList 不安全
		/**
		 * 解决方法：
		 * 1. List<String> list = new Vector<>();
		 * 2. List<String> list = Collections.synchronizedList(new ArrayList<>());
		 * 3. List<String> list = new CopyOnWriteArrayList<String>();
		 */
		
		// copyOnWrite 写入时复制	COW 	计算机程序设计领域的一种优化策略
		// 多个线程调用的时候，list，读取的时候，固定的，写入（覆盖）
		// 在写入的时候避免覆盖，造成数据问题
		// 读写分离 'MyCat'
		// CopyOnWriteArrayList 比 vector 厉害在哪里？？
		
		List<String> list = new CopyOnWriteArrayList<String>();
				
		for (int i = 0; i < 10; i++) {
			new Thread(() -> {
				list.add(UUID.randomUUID().toString().substring(0, 5));
				System.out.println(list);
			},String.valueOf(i)).start();
		}
	}
}
```
学习方法推荐：1. 先回用	2. 货比3家，寻找其他解决方案	3. 看原码



### 6.2 Set 不安全

```shell
package unsafe;

//import java.util.Collections;
//import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 同理可证: ConcurrentModificationException
 * 
 *
 */
public class SetTest {

	public static void main(String[] args) {

//		Set<String> set = new HashSet<>();
//		Set<String> set = Collections.synchronizedSet(new HashSet<>());
		Set<String> set = new CopyOnWriteArraySet<String>();

		for (int i = 0; i <= 10; i++) {
			new Thread(() -> {
				set.add(UUID.randomUUID().toString().substring(0, 5));
				System.out.println(set);
			}, String.valueOf(i)).start();
		}
	}
}
```



**hashSet的底层是什么？**

```java
public HashSet(){
    map = new hashMap<>();
}

// add
// set的本质就是map，key是无法重复的！
public boolean add(E e){
    return map.put(e, PRESENT) == null;
}

private static final Object PRESENT = new Object(); // 不变的值
```



### 6.3 HashMap 不安全

```java
package unsafe;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// ConcurrentModificationException
public class MapTest {

	public static void main(String[] args) {
		
		// map 是这样用的吗？不是，工作中不用HashMap
		// 默认等价于什么？	   初始化容量 Initial Capacity = 16		默认加载因子 LoadFactor = 0.75f
		// Map<String, String> map = new HashMap<>();

		Map<String, String> map = new ConcurrentHashMap<>();
		
		for (int i = 0; i <= 30; i++) {
			new Thread(() -> {
				map.put(Thread.currentThread().getName(), UUID.randomUUID().toString().substring(0, 5));
				System.out.println(map);
			}, String.valueOf(i)).start();
		}
	}
}
```



## 7 Callable

1. 可以有返回值
2. 可以抛出异常
3. 方法不同，run() / call()

![image-20200826164954472](C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20200826164954472.png)

> 代码测试

```java
package callable;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class CallableTest {

	public static void main(String[] args) throws InterruptedException, ExecutionException {

		// new Thread(new MyThread()).start();
		// new Thread(new FutureTask<V>()).start();
		// new Thread(new FutureTask<V>(Callable)).start();

		new Thread().start(); // 怎么启动callable

		MyThread thread = new MyThread();
		FutureTask task = new FutureTask(thread); // 适配类

		new Thread(task, "A").start();

		Integer integer = (Integer) task.get(); // 获取Callable的返回结果
		System.out.println(integer);
	}
}

class MyThread implements Callable<Integer> {

	@Override
	public Integer call() {
		System.out.println("call()");
		return 1024;
	}
}
```

**细节**：

1. 有缓存
2. 结果可能需要等待，会阻塞



## 8 常用的辅助类

### 8.1 CountDownLatch

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20200831141512063.png" alt="image-20200831141512063" style="zoom:125%;" />

**减法计数器**

```java
package add;

import java.util.concurrent.CountDownLatch;

// 计数器
public class CountDownLatchTest {

	public static void main(String[] args) throws InterruptedException {

		// 总数是6
		CountDownLatch cdl = new CountDownLatch(6);

		for (int i = 0; i <= 6; i++) {
			new Thread(() -> {
				System.out.println(Thread.currentThread().getName() + " Go out");
				cdl.countDown(); // 数量-1
			}, String.valueOf(i)).start();
		}

		cdl.await(); // 等待计数器归零，然后再向下执行

		System.out.println("Close door");
	}
}

```

**原理：**

countDownLatch.countDown( ); // 数量-1

countDownLatch.await( ); // 等待计数器归零，然后再向下执行

每次有线程调用countDown( )，数量-1，假设计数器变为0，await( )就会被唤醒，继续执行



### 8.2 CyclicBarrier

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20200831144145119.png" style="zoom:125%;" />

**加法计数器**

```java
package add;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class CyclicBarrierTest {

	public static void main(String[] args) {
		/**
		 * 集齐7颗龙珠召唤神龙
		 */

		// 召唤龙珠的线程
		CyclicBarrier cb = new CyclicBarrier(7, () -> {
			System.out.println("召唤神龙成功！");
		});

		for (int i = 1; i <= 7; i++) {
			final int temp = i;
			new Thread(() -> {
				System.out.println(Thread.currentThread().getName() + "收集" + temp + "个龙珠");

				try {
					cb.await(); // 等待
				} catch (InterruptedException | BrokenBarrierException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}).start();
		}
	}
}
```



### 8.3 Semaphore

Semaphore：信息量

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20200831152030824.png" alt="image-20200831152030824" style="zoom:100%;" />

抢车位	6车---3个停车位

```java
package add;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class SemaphoreDemo {

	public static void main(String[] args) {

		// 线程数量：停车位！限流！
		Semaphore semaphore = new Semaphore(6);

		for (int i = 1; i <= 6; i++) {
			new Thread(() -> {

				// acquire() 得到
				try {
					semaphore.acquire();
					System.out.println(Thread.currentThread().getName() + "抢到车位");
					TimeUnit.SECONDS.sleep(2);
					System.out.println(Thread.currentThread().getName() + "离开车位");
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					// release() 释放
					semaphore.release();
				}
			},String.valueOf(i)).start();
		}
	}
}

```

**原理：**

semaphore.acquire( )  获得，假设如果已经满了，等待，等待被释放为止

semaphore.release( )   释放，会将当前的信号量释放+1，然后唤醒等待的线程

作用：多个共享资源互斥的使用！并发限流，控制最大的线程数



## 9 读写锁 

**ReadWriteLock**









## 10 阻塞队列 

**Blocking Queue**

### 10.1 API







## 11 同步队列































