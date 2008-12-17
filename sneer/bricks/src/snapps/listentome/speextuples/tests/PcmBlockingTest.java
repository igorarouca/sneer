package snapps.listentome.speextuples.tests;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;

import snapps.listentome.speextuples.SpeexTuples;
import sneer.pulp.distribution.filtering.TupleFilterManager;
import sneer.skin.sound.PcmSoundPacket;
import tests.Contribute;
import tests.TestThatIsInjected;
import static wheel.lang.Environments.my;

public class PcmBlockingTest extends TestThatIsInjected {
	
	private final Mockery _mockery = new JUnit4Mockery();
	
	@Contribute
	private final TupleFilterManager _filter = _mockery.mock(TupleFilterManager.class);
	
	{ 
		_mockery.checking(new Expectations() {{
			one(_filter).block(PcmSoundPacket.class);
		}});
	}

	@SuppressWarnings("unused")
	private SpeexTuples _subject = my(SpeexTuples.class);
	
	@Test
	public void testPcmBlocking() throws Exception {
	}
	
}
