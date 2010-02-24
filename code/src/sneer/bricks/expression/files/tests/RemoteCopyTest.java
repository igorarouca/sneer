package sneer.bricks.expression.files.tests;

import static sneer.foundation.environments.Environments.my;

import java.io.File;
import java.io.IOException;

import sneer.bricks.expression.files.client.FileClient;
import sneer.bricks.expression.files.client.downloads.TimeoutException;
import sneer.bricks.expression.files.server.FileServer;
import sneer.bricks.hardware.clock.Clock;
import sneer.bricks.hardware.clock.ticker.custom.CustomClockTicker;
import sneer.bricks.hardware.cpu.crypto.Sneer1024;
import sneer.bricks.hardware.cpu.threads.Threads;
import sneer.bricks.pulp.tuples.TupleSpace;
import sneer.bricks.software.folderconfig.FolderConfig;
import sneer.foundation.environments.Environment;
import sneer.foundation.environments.Environments;
import sneer.foundation.lang.Closure;
import sneer.foundation.lang.ClosureX;

public class RemoteCopyTest extends FileCopyTestBase {

	@Override
	protected void copyFileFromFileMap(final Sneer1024 hashOfContents, final File destination) throws Exception {
		copyFromFileMap(new ClosureX<Exception>() { @Override public void run() throws IOException, TimeoutException {
			my(FileClient.class).startFileDownload(destination, hashOfContents).waitTillFinished();
		}});
	}

	
	@Override
	protected void copyFolderFromFileMap(final Sneer1024 hashOfContents, final File destination) throws Exception {
		copyFromFileMap(new ClosureX<Exception>() { @Override public void run() throws IOException, TimeoutException {
			my(FileClient.class).startFolderDownload(destination, hashOfContents).waitTillFinished();
		}});
	}

	
	private void copyFromFileMap(ClosureX<Exception> closure) throws Exception {
		@SuppressWarnings("unused") FileServer server = my(FileServer.class);
		my(CustomClockTicker.class).start(10, 15000);
		Environment remote = newTestEnvironment(my(TupleSpace.class), my(Clock.class));
		configureStorageFolder(remote);
		
		Environments.runWith(remote, closure);
		
		crash(remote);
		my(Threads.class).crashAllThreads();
	}

	
	private void configureStorageFolder(Environment remote) {
		Environments.runWith(remote, new Closure() { @Override public void run() {
			my(FolderConfig.class).storageFolder().set(newTmpFile("remote"));
		}});
	}

	
	private void crash(Environment remote) {
		Environments.runWith(remote, new Closure() { @Override public void run() {
			my(Threads.class).crashAllThreads();
		}});
	}
}
