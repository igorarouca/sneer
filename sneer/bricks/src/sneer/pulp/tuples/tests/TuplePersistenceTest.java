package sneer.pulp.tuples.tests;

import static wheel.lang.Environments.my;

import java.util.List;

import org.junit.Test;

import sneer.kernel.container.Container;
import sneer.kernel.container.Containers;
import sneer.pulp.config.persistence.mocks.PersistenceConfigMock;
import sneer.pulp.tuples.Tuple;
import sneer.pulp.tuples.TupleSpace;
import wheel.lang.Environments;
import wheel.testutil.TestThatMightUseResources;

public class TuplePersistenceTest extends TestThatMightUseResources {

	private final PersistenceConfigMock _persistenceMock  = new PersistenceConfigMock(tmpDirectory());


	@Test
	public void testTuplePersistence() {
		runInNewEnvironment(new Runnable() { @Override public void run() {
			TupleSpace subject1 = createSubject();
	
			assertEquals(0, subject1.keptTuples().size());
	
			subject1.keep(TestTuple.class);
			subject1.publish(tuple(0));
			subject1.publish(tuple(1));
			subject1.publish(tuple(2));
		}});
		
		runInNewEnvironment(new Runnable() { @Override public void run() {
			
			TupleSpace subject2 = createSubject();
			List<Tuple> kept = subject2.keptTuples();
			assertEquals(3, kept.size());
			assertEquals(0, ((TestTuple)kept.get(0)).intArray[0]);
			assertEquals(1, ((TestTuple)kept.get(1)).intArray[0]);
			assertEquals(2, ((TestTuple)kept.get(2)).intArray[0]);

		}});
	}


	private TestTuple tuple(int i) {
		return new TestTuple(new int[] {i});
	}
	
	private void runInNewEnvironment(Runnable runnable) {
		Environments.runWith(newContainer(), runnable);
	}
	
	private TupleSpace createSubject() {
		return my(TupleSpace.class);
	}


	private Container newContainer() {
		final Container c = Containers.newContainer(_persistenceMock);
		return c;
	}
}


