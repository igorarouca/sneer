package sneer.bricks.pulp.clock;

import sneer.bricks.pulp.threads.Stepper;
import sneer.foundation.brickness.Brick;

@Brick
public interface Clock {
	
	long time();
	
	void sleepAtLeast(long millis);

	void wakeUpNoEarlierThan(long timeToWakeUp, Runnable runnable);
	void wakeUpInAtLeast(long millisFromNow, Runnable runnable);
	void wakeUpEvery(long minimumPeriodInMillis, Stepper stepper);
	void wakeUpNowAndEvery(long minimumPeriodInMillis, Stepper stepper);

	void advanceTime(long deltaMillis);
	void advanceTimeTo(long absoluteTimeMillis);

}