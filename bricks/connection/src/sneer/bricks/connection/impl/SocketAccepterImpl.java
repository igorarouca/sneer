package sneer.bricks.connection.impl;

import java.io.IOException;

import sneer.bricks.blinkinglights.BlinkingLights;
import sneer.bricks.connection.SocketAccepter;
import sneer.bricks.network.ByteArrayServerSocket;
import sneer.bricks.network.ByteArraySocket;
import sneer.bricks.network.Network;
import sneer.lego.Brick;
import sneer.lego.Startable;
import sneer.log.Logger;
import spikes.legobricks.name.PortKeeper;
import wheel.lang.Omnivore;
import wheel.lang.Threads;
import wheel.reactive.EventNotifier;
import wheel.reactive.EventSource;
import wheel.reactive.impl.EventNotifierImpl;

public class SocketAccepterImpl implements SocketAccepter, Startable {
	
	private EventNotifier<ByteArraySocket> _notifier = new EventNotifierImpl<ByteArraySocket>();

	private ByteArrayServerSocket _serverSocket;
	
	private volatile boolean _isStopped;

	private final transient Object _portToListenMonitor = new Object();

	private int _portToListen;

	@Brick
	private PortKeeper _portKeeper;
	
	@Brick
	private Network _network;

	@Brick
	private BlinkingLights _exceptionHandler;
	
    @Brick
    private Logger _log;

	@Override
	public void start() throws Exception {
		Threads.startDaemon(new Runnable(){ @Override public void run() {
			listenToSneerPort();
		}});
		_portKeeper.port().addReceiver(new Omnivore<Integer>() { @Override public void consume(Integer port) {
			setPort(port);
		}});
	}

	@Override
    public EventSource<ByteArraySocket> lastAcceptedSocket() {
    	return _notifier.output();
    }

    private void setPort(int port) {
		synchronized (_portToListenMonitor) {
			_portToListen = port;
			_portToListenMonitor.notify();
		}
	}

    private void listenToSneerPort() {
    	while (true) {
    		int myPortToListen = _portToListen;
    		crashServerSocketIfNecessary();
    		openServerSocket(myPortToListen);	
    		if(_serverSocket != null) startAccepting();
    		
    		synchronized (_portToListenMonitor) {
    			if (myPortToListen == _portToListen)
    				Threads.waitWithoutInterruptions(_portToListenMonitor);
    		}
    	}
    }
	
	private void startAccepting() {
		_isStopped = false;
		Threads.startDaemon(new Runnable() { @Override public void run() {
			while (!_isStopped) {
				try {
					ByteArraySocket clientSocket = _serverSocket.accept();
					_notifier.notifyReceivers(clientSocket);
				} catch (IOException e) {
					if (!_isStopped)
						_exceptionHandler.handle("Error accepting client connection", e);
				} 
			}
		}});
	}

	private void openServerSocket(int port) {
		if (port == 0) return;
		
		try {
			_log.info("starting server socket at {}", port);
			_serverSocket = _network.openServerSocket(port);
		} catch (IOException e) {
			_exceptionHandler.handle("Error trying to open socket at " + port, e);
		}
	}

	private void crashServerSocketIfNecessary() {
		if(_serverSocket == null) return;

		_log.info("crashing server socket");
		_isStopped = true;
		_serverSocket.crash();
	}
}
