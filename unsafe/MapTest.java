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