package sneer.kernel.business.contacts;

import java.io.Serializable;

@Deprecated
public class ContactInfo implements Serializable {

	public final String _nick;
	public final String _host;
	public final int _port;
	public final String _publicKey;
	public final String _state;

	public ContactInfo(String nick, String host, int port, String publicKey, String state) {
		_nick = nick;
		_host = host;
		_port = port;
		_publicKey = publicKey;
		_state = state;
	}

	private static final long serialVersionUID = 1L;
	public static final String CONFIRMED_STATE = "confirmed";
	public static final String UNCONFIRMED_STATE = "unconfirmed";

}
