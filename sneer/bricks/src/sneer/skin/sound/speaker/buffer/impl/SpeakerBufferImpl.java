package sneer.skin.sound.speaker.buffer.impl;

import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import sneer.skin.sound.PcmSoundPacket;
import sneer.skin.sound.speaker.buffer.SpeakerBuffer;
import wheel.lang.Consumer;
import wheel.lang.Threads;

class SpeakerBufferImpl implements SpeakerBuffer {

	private static final int MAX_INTERRUPTED = 30;
	private static final int MAX_GAP = 500;

	private final Consumer<? super PcmSoundPacket> _consumer;
	private boolean _isRunning = true;

	private int _lastPlayed = -1;
	
	private final SortedSet<PcmSoundPacket> _sortedSet = new TreeSet<PcmSoundPacket>(
		new Comparator<PcmSoundPacket>(){@Override public int compare(PcmSoundPacket packet1, PcmSoundPacket packet2) {

			//This subtraction only works because shorts are promoted to int before subtraction
			if(Math.abs(packet1.sequence - packet2.sequence) > MAX_GAP )
				return packet1.sequence+MAX_GAP - (packet2.sequence+MAX_GAP); 
			
			return packet1.sequence - packet2.sequence; 
	}});

	@Override
	public synchronized void crash() {
		_isRunning = false;
		notify();
	}
	
	public SpeakerBufferImpl(Consumer<? super PcmSoundPacket> consumer) {
		_consumer = consumer;
	}

	@Override
	public synchronized void consume(PcmSoundPacket packet) {
		drainIfNecessary(packet);
		if(_lastPlayed+1 == packet.sequence){
			play(packet);
			return;
		}
		_sortedSet.add(packet);
		doStep();
	}


	private synchronized boolean doStep() {
		if (!_isRunning) return false;
		
		if (_sortedSet.isEmpty())
			Threads.waitWithoutInterruptions(this);
		
		if (!_isRunning) return false;
		
		leftDrain(_lastPlayed);
		drainOldPackets();
		playUninterruptedPackets();
		playInterruptedPackets();
		return true;
	}
	
	private void drainIfNecessary(PcmSoundPacket packet) {
		Iterator<PcmSoundPacket> iterator = _sortedSet.iterator();
		if(!iterator.hasNext()) return;
		PcmSoundPacket previous = iterator.next();

		if(Math.abs(packet.sequence-previous.sequence)  > MAX_GAP){
			_lastPlayed = packet.sequence-1;
			leftDrain(previous.sequence);
			return;
		}
	}

	private void drainOldPackets() {
		Iterator<PcmSoundPacket> iterator = _sortedSet.iterator();
		if(!iterator.hasNext()) return;
		PcmSoundPacket previous = iterator.next();
		
		while (iterator.hasNext()) {
			PcmSoundPacket packet = iterator.next();
			if(Math.abs(packet.sequence-previous.sequence)  > MAX_GAP){
				_lastPlayed = packet.sequence-1;
				leftDrain(previous.sequence);
				return;
			}
			previous = packet;
		}		
	}

	private void leftDrain(int limit) {
		Iterator<PcmSoundPacket> iterator = _sortedSet.iterator();
		while (iterator.hasNext()) {
			PcmSoundPacket packet = iterator.next();
			if(packet.sequence>limit) return;
			iterator.remove();
			continue;
		}	
	}

	private void playInterruptedPackets() {
		if(_sortedSet.size()<2) return;
		PcmSoundPacket lastPacket = _sortedSet.last();
		int maxSequenceToPlay = lastPacket.sequence - MAX_INTERRUPTED;
		Iterator<PcmSoundPacket> iterator = _sortedSet.iterator();
		while (iterator.hasNext()) {
			PcmSoundPacket packet = iterator.next();
			if(packet.sequence>maxSequenceToPlay) return;
			play(packet);
			iterator.remove();
		}	
	}

	private void playUninterruptedPackets() {
		Iterator<PcmSoundPacket> iterator = _sortedSet.iterator();
		while (iterator.hasNext()) {
			PcmSoundPacket packet = iterator.next();
			if(nextSequenceToPlay() != packet.sequence) return;
			play(packet);
			iterator.remove();
		}
	}
	
	private void play(PcmSoundPacket packet) {
		_lastPlayed = packet.sequence;
		_consumer.consume(packet);
	}

	private int nextSequenceToPlay() {
		return _lastPlayed+1;
	}
}

class ReverseSequence extends PcmSoundPacket{

	ReverseSequence(PcmSoundPacket packet, short seq) {
		super(packet.publisher(), packet.publicationTime(), packet.payload, seq);
	}
}