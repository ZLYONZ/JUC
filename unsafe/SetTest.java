package unsafe;

//import java.util.Collections;
//import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 同理可证: ConcurrentModificationException
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