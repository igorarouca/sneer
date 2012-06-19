package sneer.bricks.network.computers.udp.connections.impl;

import static basis.environments.Environments.my;
import static sneer.bricks.network.computers.udp.connections.UdpPacketType.Data;
import static sneer.bricks.network.computers.udp.connections.UdpPacketType.Hail;
import static sneer.bricks.network.computers.udp.connections.impl.UdpByteConnectionUtils.send;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import sneer.bricks.hardware.cpu.threads.Threads;
import sneer.bricks.network.computers.connections.ByteConnection;
import sneer.bricks.network.computers.udp.connections.UdpPacketType;
import sneer.bricks.network.computers.udp.sightings.SightingKeeper;
import sneer.bricks.network.social.Contact;
import sneer.bricks.pulp.reactive.Signal;
import basis.lang.Closure;
import basis.lang.Consumer;

class UdpByteConnection implements ByteConnection {
	
	private Consumer<? super byte[]> receiver;
	private final Contact contact;
	private final ConnectionMonitor monitor;

	UdpByteConnection(Contact contact) {
		this.contact = contact;
		this.monitor = new ConnectionMonitor(my(SightingKeeper.class).sightingsOf(contact));
	}

	@Override
	public Signal<Boolean> isConnected() {
		return monitor.isConnected();
	}
	
	@Override
	public void initCommunications(final PacketScheduler scheduler, Consumer<? super byte[]> receiver) {
		if (this.receiver != null) throw new IllegalStateException();
		this.receiver = receiver;
		my(Threads.class).startStepping("ByteConnection", new Closure() { @Override public void run() {
			tryToSendPacketFor(scheduler);
		}});
	}
	
	private void tryToSendPacketFor(PacketScheduler scheduler) {
		byte[] payload = scheduler.highestPriorityPacketToSend();
		if (send(Data, payload, monitor.lastSighting()))
			scheduler.previousPacketWasSent();
	}
	
	void handle(UdpPacketType type, InetSocketAddress origin, ByteBuffer data) {
		my(SightingKeeper.class).keep(contact, origin);
		
		if(type == Hail) {
			long hailTimestamp = data.getLong();
			monitor.handleHail(origin, hailTimestamp);
			return;
		}
		
		if (receiver == null) return;
		byte[] payload = new byte[data.remaining()];
		data.get(payload);
		receiver.consume(payload);
	}
	
}
