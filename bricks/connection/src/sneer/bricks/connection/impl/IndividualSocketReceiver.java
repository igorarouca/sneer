package sneer.bricks.connection.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import sneer.bricks.connection.ConnectionManager;
import sneer.bricks.keymanager.DuplicateKeyForContact;
import sneer.bricks.keymanager.KeyBelongsToOtherContact;
import sneer.bricks.keymanager.KeyManager;
import sneer.bricks.network.ByteArraySocket;
import sneer.contacts.Contact;
import sneer.contacts.ContactManager;
import sneer.lego.Brick;
import sneer.lego.ContainerUtils;
import sneer.log.Logger;
import wheel.lang.exceptions.IllegalParameter;

class IndividualSocketReceiver {

	private static final byte[] SNEER_WIRE_PROTOCOL_1 = toByteArray("Sneer Wire Protocol 1");
	private static final byte[] FALLBACK = toByteArray("Fallback");
	private static final byte[] OK = toByteArray("OK");
	
	@Brick
	private KeyManager _keyManager;
	
	@Brick
	private ContactManager _contactManager;
	
	@Brick
	private ConnectionManager _connectionManager;
	
	@Brick
	private	Logger _logger;
	
	private final ByteArraySocket _socket;
	private byte[] _peersPublicKey;

	
	IndividualSocketReceiver(ByteArraySocket socket) {
		ContainerUtils.inject(this);
		_socket = socket;

		try {
			tryToServe();
		} catch (Exception e) {
			_logger.info("Exception thrown by incoming socket.", e);
			_socket.crash();
		}
	}

	private void tryToServe() throws IOException {
		shakeHands();

		_peersPublicKey = _socket.read();	

		if (!tryToAuthenticate()) {
			_socket.crash();
			return;
		}
		
		Contact contact = produceContact();
		if(contact != null)
			_connectionManager.manageIncomingSocket(contact, _socket);
		else
			_socket.crash();
	}

	private Contact produceContact() {
		while(true) {
			Contact contact = _keyManager.contactGiven(_peersPublicKey);
			if (contact == null) {
				contact = createUnconfirmedContact();
				try {
					_keyManager.addKey(contact, _peersPublicKey);
					return contact;
				} catch (DuplicateKeyForContact e) {
					//how did this happen?
					_logger.error("Error producing contact",e);
					return null;
				} catch (KeyBelongsToOtherContact e) {
					//Other thread assigned this pk to other contact. Try again
					_logger.info("Key belongs to other contact. Trying again...",e);
					continue;
				}
			}
		}
	}

	private Contact createUnconfirmedContact() {
		String baseNick = "Unconfirmed";   //"Unconfirmed (2)", "Unconfirmed (3)", etc.
		
		Contact result = tryToCreate(baseNick);
		if (result != null) return result;
		
		int i = 2;
		while (tryToCreate(baseNick + " (" + i + ")") == null)
			i++;
		
		return result;
	}

	private Contact tryToCreate(String nickname) {
		if (_contactManager.isNicknameAlreadyUsed(nickname))
			return null;
		
		try {
			return _contactManager.addContact(nickname);
		} catch (IllegalParameter e) {
			return null;
		}
	}

	private static byte[] toByteArray(String string) {
		try {
			return string.getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException();
		}
	}


	private boolean tryToAuthenticate() {
		byte[] ownPublicKey = _keyManager.ownPublicKey();
		if (Arrays.equals(_peersPublicKey, ownPublicKey))
			return false;
		
		//Implement: Challenge! :)
		return true;
	}


	private void shakeHands() throws IOException {
		while (true) {
			byte[] header = _socket.read();
			if (Arrays.equals(header, SNEER_WIRE_PROTOCOL_1)) {
				_socket.write(OK);
				return;
			}
			discardFirstDataPacket();
			_socket.write(FALLBACK);
		}
	}
		
	private void discardFirstDataPacket() throws IOException {
		_socket.read();
	}

	
}
