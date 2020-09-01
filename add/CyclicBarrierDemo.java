package add;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class CyclicBarrierDemo {

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
			},String.valueOf(i)).start();
		}
	}
}
