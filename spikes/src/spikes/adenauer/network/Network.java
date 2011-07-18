package spikes.adenauer.network;

import java.io.IOException;

import sneer.bricks.identity.seals.Seal;
import sneer.bricks.pulp.events.EventSource;
import sneer.bricks.pulp.reactive.collections.SetSignal;

public interface Network {
	SetSignal<Seal> peersOnline();
	void send(byte[] data, Seal destination) throws IOException;
	EventSource<Packet> packetsReceived();


	public interface Packet {
		Seal sender();
		byte[] data();
	}

}
