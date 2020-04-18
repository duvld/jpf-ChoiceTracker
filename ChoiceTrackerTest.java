import gov.nasa.jpf.util.test.TestJPF;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Random;

import org.junit.Test;

/**
 * Tests the ChoiceTracker listener. Tests the listener against all types Random
 * can generate (boolean, int, double, float, double).
 * 
 * Also tests for choices that result from multiple threads.
 *
 * @author Anji Tong
 */
public class ChoiceTrackerTest extends TestJPF {
	/**
	 * Instance of Random class to generate random values of types mentioned in
	 * description.
	 */
	private Random random = new Random();

	/**
	 * Maximum value for the random Integer; either 0 or 1.
	 */
	private static final int MAX_RANDOM_INT = 2;

	/**
	 * JPF properties.
	 */
	private static final String[] CONFIGURATION = { "+listener=ChoiceTracker",
			"+classpath=/home/anjitong/Dev/Eclipse/workspace/4315/bin",
			"+@include=/home/anjitong/Dev/jpf/jpf-core/jpf.properties",
			"+native_classpath=/home/anjitong/Dev/Eclipse/workspace/4315/bin", "+cg.enumerate_random=true",
			"+choice.trace=" };

	/**
	 * Tests ChoiceTracker listener on an empty app.
	 */
	@Test
	public void emptyTest() {
		if (this.verifyNoPropertyViolation(CONFIGURATION)) {
			// empty app
		}
	}

	/**
	 * Tests ChoiceTracker listener on a basic boolean choice.
	 */
	@Test
	public void basicBooleanTest() {
		if (this.verifyNoPropertyViolation(CONFIGURATION)) {
			if (random.nextBoolean()) {
				System.out.println("1");
			} else {
				System.out.println("2");
			}
		}
	}

	/**
	 * Tests ChoiceTracker listener on a boolean choice that triggers
	 * propertyViolated
	 */
	@Test
	public void cgBooleanTest() {
		PrintStream out = System.out;
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		System.setOut(new PrintStream(stream));
		if (this.verifyUnhandledException("java.lang.RuntimeException", CONFIGURATION)) {
			if (random.nextBoolean()) {
				System.out.println("1");
				throw new RuntimeException();
			} else {
				System.out.println("2");
				throw new RuntimeException();
			}
		} else {
			System.setOut(out);
			TestJPF.assertTrue("Incorrect choice tracked, should be Boolean", stream.toString()
					.contains("BooleanChoiceGenerator[[id=\"verifyGetBoolean\",isCascaded:false,{>false,true}]"));
		}
	}

	/**
	 * Tests ChoiceTracker listener on a Integer choice that triggers
	 * propertyViolated
	 */
	@Test
	public void cgIntTest() {
		int nextChoice = random.nextInt(MAX_RANDOM_INT);
		PrintStream out = System.out;
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		System.setOut(new PrintStream(stream));
		if (this.verifyUnhandledException("java.lang.RuntimeException", CONFIGURATION)) {
			if (nextChoice == 0) {
				System.out.println("1");
				throw new RuntimeException();
			} else {
				System.out.println("2");
				throw new RuntimeException();
			}
		} else {
			System.setOut(out);
			TestJPF.assertTrue("Incorrect choice tracked, should be Integer", stream.toString()
					.contains("IntIntervalGenerator[id=\"verifyGetInt(II)\",isCascaded:false,0..1,delta=+1,cur=0]"));
		}
	}

	/**
	 * Tests ChoiceTracker listener on a Double choice that triggers
	 * propertyViolated
	 */
	@Test
	public void cgDoubleTest() {
		double nextChoice = random.nextDouble();
		PrintStream out = System.out;
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		System.setOut(new PrintStream(stream));
		if (this.verifyUnhandledException("java.lang.RuntimeException", CONFIGURATION)) {
			if (nextChoice >= 0.5) {
				System.out.println("1");
				throw new RuntimeException();
			} else {
				System.out.println("2");
				throw new RuntimeException();
			}
		} else {
			System.setOut(out);
			TestJPF.assertTrue("Incorrect choice tracked, should be Double", stream.toString().contains(
					"DoubleChoiceFromList[id=\"verifyDoubleList([D)\",isCascaded:false,>4.9E-324,0.0,1.7976931348623157E308]"));
		}
	}

	/**
	 * Tests ChoiceTracker listener on a Float choice that triggers propertyViolated
	 */
	@Test
	public void cgFloatTest() {
		double nextChoice = random.nextFloat();
		PrintStream out = System.out;
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		System.setOut(new PrintStream(stream));
		if (this.verifyUnhandledException("java.lang.RuntimeException", CONFIGURATION)) {
			if (nextChoice >= 0.5) {
				System.out.println("1");
				throw new RuntimeException();
			} else {
				System.out.println("2");
				throw new RuntimeException();
			}
		} else {
			System.setOut(out);
			TestJPF.assertTrue("Incorrect choice tracked, should be Double", stream.toString().contains(
					"FloatChoiceFromList[id=\"verifyFloatList([F)\",isCascaded:false,>1.4E-45,0.0,3.4028235E38]"));
		}
	}

	/**
	 * Tests ChoiceTracker listener on a Long choice that triggers propertyViolated
	 */
	@Test
	public void cgLongTest() {
		double nextChoice = random.nextLong();
		PrintStream out = System.out;
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		System.setOut(new PrintStream(stream));
		if (this.verifyUnhandledException("java.lang.RuntimeException", CONFIGURATION)) {
			if (nextChoice >= 0.5) {
				System.out.println("1");
				throw new RuntimeException();
			} else {
				System.out.println("2");
				throw new RuntimeException();
			}
		} else {
			System.setOut(out);
			TestJPF.assertTrue("Incorrect choice tracked, should be Double", stream.toString().contains(
					"LongChoiceFromList[id=\"verifyLongList([J)\",isCascaded:false,>-9223372036854775808,0,9223372036854775807]"));
		}
	}

	/**
	 * Tests ChoiceTracker listener on a choice that results from multiple threads that triggers propertyViolated
	 */
	@Test
	public void cgConcurrencyTest() {
		double nextChoice = random.nextLong();
		PrintStream out = System.out;
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		System.setOut(new PrintStream(stream));
		if (this.verifyUnhandledException("java.lang.RuntimeException", CONFIGURATION)) {
			(new Thread() {
				public void run() {
					throw new RuntimeException();
				}
			}).start();
			(new Thread() {
				public void run() {
					throw new RuntimeException();
				}
			}).start();
		} else {
			System.out.println("stream: " + stream.toString());
			System.setOut(out);
			TestJPF.assertTrue("Incorrect choice tracked, should have START", stream.toString().contains(
					"ThreadChoiceFromSet {id:\"START\" ,1/2,isCascaded:false}"));
			TestJPF.assertTrue("Incorrect choice tracked, should have LOCK", stream.toString().contains(
					"ThreadChoiceFromSet {id:\"LOCK\" ,1/2,isCascaded:false}"));
			TestJPF.assertTrue("Incorrect choice tracked, should have TERMINATE", stream.toString().contains(
					"ThreadChoiceFromSet {id:\"TERMINATE\" ,1/2,isCascaded:false}"));
		}
	}

	/**
	 * Runs the test methods with the given names. If no names are given, all test
	 * methods are run.
	 *
	 * @param testMethods the names of the test methods to be run.
	 */
	public static void main(String[] testMethods) {
		runTestsOfThisClass(testMethods);
	}
}
