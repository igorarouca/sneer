package dfcsantos.tracks.player.impl;

import static sneer.foundation.environments.Environments.my;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javazoom.jl.player.Player;
import sneer.bricks.hardware.cpu.lang.contracts.WeakContract;
import sneer.bricks.hardware.cpu.threads.Threads;
import sneer.bricks.pulp.blinkinglights.BlinkingLights;
import sneer.bricks.pulp.blinkinglights.LightType;
import sneer.bricks.pulp.reactive.Signal;
import dfcsantos.tracks.Track;
import dfcsantos.tracks.player.TrackContract;

class TrackContractImpl implements TrackContract {

	private PausableInputStream _trackStream;
	private Player _player;

	private volatile boolean _isDisposed;
	
	@SuppressWarnings("unused")	private WeakContract _refToAvoidGc;

	
	TrackContractImpl(final Track track, final Signal<Boolean> isPlaying, final Runnable toCallWhenFinished) {
		try {
			_trackStream = new PausableInputStream(new FileInputStream(track.file()), isPlaying);
		} catch (FileNotFoundException e) {
			my(BlinkingLights.class).turnOn(LightType.WARNING, "Unable to find file " + track.file() , "File might have been deleted manually.", 15000);
		} 
		
		my(Threads.class).startDaemon("Track Player", new Runnable() { @Override public void run() {
			play(toCallWhenFinished);
		}});
	}

		
	@Override
	public void dispose() {
		_isDisposed = true;
		if (_player != null) _player.close();
	}


	@Override
	public int trackElapsedTime() {
		return _player == null ? 0 : _player.getPosition();
	}

	
	private void play(final Runnable toCallWhenFinished) {
		try {
			_player = new Player(_trackStream);
			_player.play();
		} catch (Throwable t) {
			if (!_isDisposed)
				my(BlinkingLights.class).turnOn(LightType.WARNING, "Error reading track", "Error reading track", t, 30000);
		} finally {
			if (_isDisposed) return;
			dispose();
			toCallWhenFinished.run();
		}
	}

}
