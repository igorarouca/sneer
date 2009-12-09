package dfcsantos.wusic;

import java.io.File;

import sneer.bricks.pulp.reactive.Signal;
import sneer.bricks.software.bricks.snappstarter.Snapp;
import sneer.foundation.brickness.Brick;
import sneer.foundation.lang.PickyConsumer;

@Snapp
@Brick
public interface Wusic {

	enum OperatingMode { OWN, PEERS };
	void switchOperatingMode();
	Signal<OperatingMode> operatingMode();

	void setPlayingFolder(File selectedFolder);
	void setSharedTracksFolder(File selectedFolder);

	void setShuffle(boolean shuffle);

	void start();
	Signal<String> playingTrackName();
	Signal<String> playingTrackTime();

	void pauseResume();
	void back();
	void skip();
	void stop();

	void meToo();
	void noWay();

	Signal<String> numberOfPeerTracks();
	
	Signal<Boolean> isPlaying();

	int DEFAULT_TRACKS_DOWNLOAD_ALLOWANCE = 100; // MBs
	Signal<Boolean> isTracksDownloadAllowed();
	void allowTracksDownload(boolean b);
	Signal<Integer> tracksDownloadAllowance();
	PickyConsumer<Integer> tracksDownloadAllowanceSetter();

}