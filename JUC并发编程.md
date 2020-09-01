

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

![image-20200901103643494](C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20200901103643494.png)



```java
package readWriteLock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 独占锁（写锁）一次只能被一个线程占用
 * 共享锁（写锁）多个线程可以同时占有
 * ReadWriteLock
 * 读-读	可以共存！
 * 读-写	不能共存！
 * 写-写	不能共存！
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
	@SuppressWarnings("unused")
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
	@SuppressWarnings("unused")
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
	@SuppressWarnings("unused")
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

```







## 10 阻塞队列 

![image-20200901143126152](C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20200901143126152.png)



**阻塞队列：**

![image-20200901112605517](C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20200901112605517.png)

**队列：**

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20200901143026797.png" alt="image-20200901143026797" style="zoom: 80%;" />

**Blocking Queue**： 不是新的东西

<img src="C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20200901142941970.png" alt="image-20200901142941970" style="zoom: 67%;" />

什么情况下我们会使用阻塞队列：多线程并发处理，A - B	线程池



**学会使用队列**

添加、移除



### 四组API

| 方式         | 抛出异常   | 有返回值，不抛出异常 | 阻塞等待 | 超时等待         |
| ------------ | ---------- | -------------------- | -------- | ---------------- |
| 添加         | add( )     | offer( )             | put( )   | offer( )重载方法 |
| 移除         | remove( )  | poll( )              | take( )  | poll( )重载方法  |
| 检测队首元素 | element( ) | peek( )              | -        | -                |

```java
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
    
	System.out.println("-------------------------------");

	System.out.println(bq.remove());
	System.out.println(bq.remove());
	System.out.println(bq.remove());
    
	// NoSuchElementException 抛出异常!
	// System.out.println(bq.remove());
}
```

```java
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
		
	// 返回null，不抛出异常 
	// System.out.println(bq.poll());	
}
```

```java
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
```

```java
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
```



### 同步队列

Synchronous Queue	没有容量

进去一个元素，必须等待取出来之后，才能再往里放元素

```java
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
```



## 11 线程池

线程池：3大方法、7大参数、4种拒绝策略

> 池化技术

程序的运行，本质：占用系统的资源！优化资源的使用！

线程池、连接池、内存池、对象池。。。创建，销毁，十分浪费资源

池化技术：事先准备好一些资源，有人要用就可以拿，用完之后再还回来



**线程池的好处：**

1. 降低资源的消耗
2. 提高响应的速度
3. 方便管理

！线程复用，可以控制最大并发数，管理线程！



**线程池：三大方法**







## 12











## 13









