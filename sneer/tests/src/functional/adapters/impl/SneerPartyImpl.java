package functional.adapters.impl;

import java.io.File;

import snapps.wind.Shout;
import snapps.wind.Wind;
import sneer.kernel.container.Brick;
import sneer.kernel.container.Container;
import sneer.pulp.brickmanager.BrickManager;
import sneer.pulp.clock.Clock;
import sneer.pulp.connection.ByteConnection;
import sneer.pulp.connection.ConnectionManager;
import sneer.pulp.connection.SocketOriginator;
import sneer.pulp.connection.SocketReceiver;
import sneer.pulp.contacts.Contact;
import sneer.pulp.contacts.ContactManager;
import sneer.pulp.deployer.BrickBundle;
import sneer.pulp.deployer.Deployer;
import sneer.pulp.internetaddresskeeper.InternetAddressKeeper;
import sneer.pulp.keymanager.KeyManager;
import sneer.pulp.keymanager.PublicKey;
import sneer.pulp.own.name.OwnNameKeeper;
import sneer.pulp.port.PortKeeper;
import sneer.pulp.probe.ProbeManager;
import wheel.lang.Threads;
import wheel.lang.Types;
import wheel.lang.exceptions.IllegalParameter;
import wheel.lang.exceptions.NotImplementedYet;
import wheel.reactive.Signal;
import wheel.reactive.lists.ListSignal;
import functional.SovereignParty;
import functional.adapters.SneerParty;
import static wheel.lang.Environments.my;

public class SneerPartyImpl implements SneerParty {
	
	static private final String MOCK_ADDRESS = "localhost";

	private Container _container = my(Container.class);
	
	private Clock _clock = my(Clock.class);

	private ContactManager _contactManager = my(ContactManager.class);

	private ConnectionManager _connectionManager = my(ConnectionManager.class);
	
	private PortKeeper _sneerPortKeeper = my(PortKeeper.class);
	
	private OwnNameKeeper _ownNameKeeper = my(OwnNameKeeper.class);
	
	private InternetAddressKeeper _internetAddressKeeper = my(InternetAddressKeeper.class);

	private Wind _wind = my(Wind.class);

	@SuppressWarnings("unused")
	private ProbeManager _probes = my(ProbeManager.class);
	
	
	@SuppressWarnings("unused") //We need to start this brick so that it listens to others and does its thing.
	private SocketOriginator _originator = my(SocketOriginator.class);

	@SuppressWarnings("unused") //We need to start this brick so that it listens to others and does its thing.
	private SocketReceiver _receiver = my(SocketReceiver.class);

	private KeyManager _keyManager = my(KeyManager.class);
	
	private Deployer _deployer = my(Deployer.class);
	
	private BrickManager _brickManager = my(BrickManager.class);


	@Override
	public void setSneerPort(int port) {
		try {
			_sneerPortKeeper.portSetter().consume(port);
		} catch (IllegalParameter e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public void bidirectionalConnectTo(SovereignParty party) {
		Contact contact = addContact(party.ownName());

		SneerParty sneerParty = (SneerParty)party;
		storePublicKey(contact, sneerParty.publicKey());
		_internetAddressKeeper.add(contact, MOCK_ADDRESS, sneerParty.sneerPort());
		
		sneerParty.giveNicknameTo(this, this.ownName());
		waitUntilOnline(contact);
	}

	private void storePublicKey(Contact contact, PublicKey publicKey) {
		_keyManager.addKey(contact, publicKey);
	}

	private Contact addContact(String nickname) {
		try {
			return _contactManager.addContact(nickname);
		} catch (IllegalParameter e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String ownName() {
		return _ownNameKeeper.name().currentValue();
	}

	@Override
	public void setOwnName(String newName) {
		_ownNameKeeper.nameSetter().consume(newName);
	}
	
    @Override
    public void giveNicknameTo(SovereignParty peer, String newNickname) {
    	PublicKey publicKey = ((SneerParty)peer).publicKey();
		Contact contact = waitForContactGiven(publicKey);

		try {
			_contactManager.nicknameSetterFor(contact).consume(newNickname);
		} catch (IllegalParameter e) {
			throw new IllegalStateException(e);
		}
		
		waitUntilOnline(contact);

    }

	private void waitUntilOnline(Contact contact) {
		ByteConnection connection = _connectionManager.connectionFor(contact);
		
		while (!connection.isOnline().currentValue())
			Threads.sleepWithoutInterruptions(1);
	}

	private Contact waitForContactGiven(PublicKey publicKey) {
		while (true) {
			Contact contact = _keyManager.contactGiven(publicKey);
			if (contact != null) return contact;
			Threads.sleepWithoutInterruptions(1);
			_clock.advanceTime(60 * 1000);
		}
	}

	@Override
    public PublicKey publicKey() {
		return _keyManager.ownPublicKey();
	}

	@Override
    public Signal<String> navigateAndGetName(String nicknamePath) {
		throw new NotImplementedYet();
    }


	@Override
	public int sneerPort() {
        return _sneerPortKeeper.port().currentValue();
    }

	@Override
	public PublicKey ownPublicKey() {
		return _keyManager.ownPublicKey();
	}


	@Override
	public BrickBundle publishBricks(File sourceDirectory) {
		BrickBundle brickBundle = _deployer.pack(sourceDirectory);
		//brickBundle.prettyPrint();
		_brickManager.install(brickBundle);
		return brickBundle;
	}

	@Override
	public Brick produce(Class<? extends Brick> brick) {
		return Types.cast(_container.provide(brick));
	}

	@Override
	public void shout(String phrase) {
		_wind.megaphone().consume(phrase);
	}

	@Override
	public ListSignal<Shout> shoutsHeard() {
		return _wind.shoutsHeard();
	}

	
}

