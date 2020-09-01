package callable;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class CallableTest {

	public static void main(String[] args) throws InterruptedException, ExecutionException {

		// new Thread(new Runnable()).start();
		// new Thread(new FutureTask<V>()).start();
		// new Thread(new FutureTask<V>(Callable)).start();

		new Thread().start(); // 怎么启动callable

		MyThread thread = new MyThread();
		@SuppressWarnings({ "rawtypes", "unchecked" })
		FutureTask task = new FutureTask(thread); // 适配类

		new Thread(task, "A").start();
		new Thread(task, "B").start();

		Integer integer = (Integer) task.get(); // 获取Callable的返回结果;
		// get方法 可能会产生阻塞，把它放到最后，或者使用异步通信来处理
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