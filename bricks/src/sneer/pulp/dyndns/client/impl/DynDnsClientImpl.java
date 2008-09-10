package sneer.pulp.dyndns.client.impl;

import java.io.IOException;

import sneer.kernel.container.Inject;
import sneer.pulp.blinkinglights.BlinkingLights;
import sneer.pulp.blinkinglights.Light;
import sneer.pulp.clock.Alarm;
import sneer.pulp.clock.Clock;
import sneer.pulp.dyndns.client.DynDnsClient;
import sneer.pulp.dyndns.ownaccount.Account;
import sneer.pulp.dyndns.ownaccount.OwnAccountKeeper;
import sneer.pulp.dyndns.ownip.OwnIpDiscoverer;
import sneer.pulp.dyndns.updater.BadAuthException;
import sneer.pulp.dyndns.updater.Updater;
import sneer.pulp.dyndns.updater.UpdaterException;
import sneer.pulp.propertystore.PropertyStore;
import wheel.reactive.impl.Receiver;

class DynDnsClientImpl implements DynDnsClient {
	
	private static final String LAST_IP_KEY = "dyndns.lastIp";

	@Inject
	static private OwnIpDiscoverer _ownIpDiscoverer;
	
	@Inject
	static private OwnAccountKeeper _ownAccountKeeper;
	
	@Inject
	static private Updater _updater;
	
	@Inject
	static private PropertyStore _propertyStore;
	
	@Inject
	static private BlinkingLights _blinkingLights;
	
	@Inject
	static private Clock _clock;
	
	private State _state = new DefaultState();
	
	final Receiver<Account> _ownAccountReceiver = new Receiver<Account>(_ownAccountKeeper.ownAccount()) { @Override public void consume(Account value) {
		_state.update(value);
	}};
	
	final Receiver<String> _ownIpReceiver = new Receiver<String>(_ownIpDiscoverer.ownIp()) { @Override public void consume(String value) {
		_state.update(value);
	}};
	
	private void submitUpdateRequest(final Account account, String ip) {
		try {
			_updater.update(account.host(), account.user(), account.password(), ip);
			recordLastIp(ip);
		} catch (BadAuthException e) {
			switchTo(new BadAuthState(e));
		} catch (IOException e) {
			switchTo(new RetryLaterState(e));
		} catch (UpdaterException e) {
			throw new wheel.lang.exceptions.NotImplementedYet(e); // Fix Handle this exception.
		}
	}
	
	abstract class State {
		public void update(@SuppressWarnings("unused") String newIp) {
			// log(newIp);
		}

		public void update(@SuppressWarnings("unused") Account newAccount) {
			// log(newAccount);
		}
	}
	
	private final class DefaultState extends State {
		
		@Override
		public void update(String ip) {
			if (ip.equals(lastIp())) return;
			
			submitUpdateRequest(currentAccount(), ip);
		}

	}
	
	private abstract class ErrorState extends State {
		
		private final Light _light;
		
		public ErrorState(String message, Exception e) {
			_light = _blinkingLights.turnOn(message, e);
		}

		protected void retry() {
			_light.turnOff();
			submitUpdateRequest(currentAccount(), currentIp());
			switchTo(new DefaultState());
		}
	}
	
	private final class RetryLaterState extends ErrorState {
		
		static final int retryTimeoutInMinutes = 5;
		
		private final Alarm _alarm;

		public RetryLaterState(IOException e) {
			super("It was not possible to connect to the dyndns server. Sneer will retry again in " + retryTimeoutInMinutes + " minutes.", e);
			_alarm = _clock.setAlarm(retryTimeoutInMinutes * 60 * 1000, new Runnable() { @Override public void run() {
				_alarm.turnOff();
				retry();
			}});
		}
	}
	
	private final class BadAuthState extends ErrorState {

		public BadAuthState(BadAuthException e) {
			super(e.getHelp(), e);
		}

		@Override
		public void update(Account newAccount) {
			retry();
		}
	}
	
	private void recordLastIp(String ip) {
		_propertyStore.set(LAST_IP_KEY, ip);
	}

	public void switchTo(State state) {
		_state = state;
	}

	private String lastIp() {
		return _propertyStore.get(LAST_IP_KEY);
	}

	private Account currentAccount() {
		return _ownAccountKeeper.ownAccount().currentValue();
	}

	private String currentIp() {
		return _ownIpDiscoverer.ownIp().currentValue();
	}
}
