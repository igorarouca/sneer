package functional.freedom7;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Method;

import org.junit.Test;

import functional.SovereignFunctionalTest;
import functional.SovereignParty;
import functional.TestDashboard;

public abstract class Freedom7Test extends SovereignFunctionalTest {

	private static final String Z = "sneer.bricks.z.Z";
	
	private static final String X = "sneer.bricks.x.X";
	
	@Test
	public void testPublish() throws Exception {
		
		if (!TestDashboard.newTestsShouldRun()) return;
		
		BrickPublisher publisher = wrapParty(_a);
		BrickPublisher receiver = wrapParty(_b);

		File sourceFolder = askSourceFolder();
		/*
		 * compiles all bricks found under _sourceFolder_ and installs them locally
		 */
		publisher.publishBrick(sourceFolder);

		//test Z
		Object s1 = publisher.produce(Z);
		ClassLoader cl1 = s1.getClass().getClassLoader();
		String logFactory1 = callLogFactory(s1);
		assertTrue("wrong directory for brick class loader: "+cl1.toString(),cl1.toString().indexOf("sneer+AnaAlmeida") > 0);
		
		
		receiver.meToo(publisher, Z);
		Object s2 = receiver.produce(Z);
		ClassLoader cl2 = s2.getClass().getClassLoader();
		String logFactory2 = callLogFactory(s2);
		assertTrue("wrong directory for brick class loader: "+cl2.toString(),cl2.toString().indexOf("sneer+BrunoBarros") > 0);
		
		assertFalse("LogFactory should have been loaded on a different class loader", logFactory1.equals(logFactory2));
		assertNotSame(s1, s2);
		assertNotSame(cl1, cl2);
		
		//test X, depends on Y and Z
		receiver.meToo(publisher, X);
		Object o2 = receiver.produce(X);
	}

	
	private String callLogFactory(Object obj) throws Exception {
		Method m = obj.getClass().getMethod("logFactory", (Class<?>[]) null);
		m.setAccessible(true);
		Object result = m.invoke(obj, (Object[]) null);
		return result.toString();
	}

	protected abstract  File askSourceFolder();

	protected abstract BrickPublisher wrapParty(SovereignParty party);
}