package sneer.bricks.hardwaresharing.files.protocol;

import sneer.bricks.hardware.cpu.algorithms.crypto.Sneer1024;
import sneer.foundation.brickness.Tuple;

public class FileRequest extends Tuple {

	public final Sneer1024 hashOfContents;
	public final int blockNumber;
	public final String debugInfo;

	public FileRequest(Sneer1024 hashOfContents_, int blockNumber_, String debugInfo_) {
		hashOfContents = hashOfContents_;
		blockNumber = blockNumber_;
		debugInfo = debugInfo_;
	}

}