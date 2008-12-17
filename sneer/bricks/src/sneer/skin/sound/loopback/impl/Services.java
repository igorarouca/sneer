package sneer.skin.sound.loopback.impl;

import static wheel.lang.Environments.my;
import sneer.pulp.threadpool.ThreadPool;
import sneer.skin.sound.kernel.Audio;

public class Services {
	public static ThreadPool threads() {
		return my(ThreadPool.class);
	}
	
	public static Audio audio() {
		return my(Audio.class);
	}
}
