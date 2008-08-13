package sneer.bricks.ownTagline.impl;

import sneer.bricks.ownTagline.OwnTaglineKeeper;
import wheel.lang.Omnivore;
import wheel.reactive.Register;
import wheel.reactive.Signal;
import wheel.reactive.impl.RegisterImpl;

public class OwnTaglineKeeperImpl implements OwnTaglineKeeper {

	private Register<String> _tagline = new RegisterImpl<String>(null);

	@Override
	public Signal<String> tagline() {
		return _tagline.output();
	}

	@Override
	public Omnivore<String> taglineSetter() {
		return _tagline.setter();
	}
}
