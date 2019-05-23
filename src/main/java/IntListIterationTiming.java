import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Benchmark the difference between iteration for implementations of simple {@link IntList} interface.
 * @author Roman Elizarov
 */
public class IntListIterationTiming {
	private static final int MIN_SIZE = 1000;
	private static final int MAX_SIZE = 10000000;
	private static final int INITIAL_ITERATIONS = 100000000;
	private static final int TARGET_TIME = 500000000; // 500 ms
	private static final int STABLE_PASS = 2;
	private static final int WARM_UP_REPS = 3;

	static class Test {
		private final IntList list;

		private int dummy; // to avoid HotSpot optimizing away iteration
		private Map<Integer, TimeStats> stats = new HashMap<Integer, TimeStats>();

		private Test(String className, int size) throws Exception {
			list = (IntList)Class.forName(IntList.class.getName() + "$" + className).newInstance();
			Random random = new Random(1);
			for	(int i = 0; i < size; i++)
				list.add(random.nextInt());
		}

		private double run(int pass, int size) {
			TimeStats s = stats.get(size);
			if (s == null)
				stats.put(size, s = new TimeStats());
			int reps = pass > STABLE_PASS ? (int)(TARGET_TIME / s.avgTime() / size) : INITIAL_ITERATIONS / size;
			time(size, WARM_UP_REPS);
			double time = time(size, reps);
			if (pass >= STABLE_PASS)
				s.add(time);
			System.out.printf(Locale.US, "%30s[%8d]: %.2f %s ns per item (%d x %d)%n",
					list.getClass().getSimpleName(), size, time, s, dummy, reps);
			return time;
		}

		private double time(int size, int reps) {
			dummy = 0;
			long start = System.nanoTime();
			for	(int rep = 0; rep < reps; rep++)
				dummy = runIteration(size);
			return (double)(System.nanoTime() - start) / reps / size;
		}

		private int runIteration(int size) {
			int sum = 0;
			for (int i = 0; i < size; i++)
				sum += list.getInt(i);
			return sum;
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.err.println("Usage: " + IntListIterationTiming.class + " <passes> <impl> [<impl> ...]");
			System.err.println("Where: <passes>  is the number of passes to run tests for.");
			System.err.println("       <impl>    is one of: ");
			for (Class c : IntList.class.getDeclaredClasses())
				System.err.println("                    " + c.getSimpleName());
			return;
		}

		int passes = Integer.decode(args[0]);
		String[] classes = Arrays.copyOfRange(args, 1, args.length);

		List<Test> tests = new ArrayList<Test>();
		for (String className : classes)
			tests.add(new Test(className, MAX_SIZE));

		PrintWriter log = new PrintWriter(new FileOutputStream("IntListIterationTiming-" +
				new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + "-.log"), true);
		log.println("impl asize time");

		for (int pass = 1; pass <= passes; pass++) {
			System.out.printf("----- PASS %d -----%n", pass);
			for (Test test : tests)
				for (int size = MIN_SIZE; size <= MAX_SIZE; size *= 10) {
					double time = test.run(pass, size);
					if (pass >= STABLE_PASS)
						log.printf(Locale.US, "%s %d %.4f%n", test.list.getClass().getSimpleName(), size, time);
				}
		}
		log.close();
	}
}
