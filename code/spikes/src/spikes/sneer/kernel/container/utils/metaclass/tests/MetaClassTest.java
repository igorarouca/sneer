package spikes.sneer.kernel.container.utils.metaclass.tests;

import static sneer.foundation.environments.Environments.my;

import org.junit.Test;

import sneer.bricks.hardware.io.log.tests.BrickTestWithLogger;
import sneer.bricks.software.code.metaclass.MetaClass;
import sneer.bricks.software.code.metaclass.MetaClasses;
import spikes.sneer.kernel.container.utils.metaclass.tests.bean.Bean;


public class MetaClassTest extends BrickTestWithLogger {

	@Test
	public void testMetaClass() throws Exception {
		MetaClass metaClass = my(MetaClasses.class).metaClass(Bean.class);
		assertTrue(metaClass.isInterface());
		assertEquals("spikes.sneer.kernel.container.utils.metaclass.tests.bean.Bean", metaClass.getName());
		assertEquals("spikes.sneer.kernel.container.utils.metaclass.tests.bean", metaClass.getPackageName());

		//assertTrue(metaClass.isAssignanbleTo(Object.class));
	}
}
