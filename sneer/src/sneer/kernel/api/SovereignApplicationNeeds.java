package sneer.kernel.api;

import sneer.apps.asker.Asker;
import sneer.apps.transferqueue.TransferQueue;
import sneer.kernel.business.contacts.ContactAttributes;
import sneer.kernel.communication.Channel;
import sneer.kernel.pointofview.Contact;
import wheel.io.ui.User;
import wheel.io.ui.User.Notification;
import wheel.lang.Omnivore;
import wheel.reactive.Signal;
import wheel.reactive.lists.ListSignal;

public interface SovereignApplicationNeeds {

	User user();
	Omnivore<Notification> briefUserNotifier();

	Channel channel();

	Signal<String> ownName();
	ListSignal<Contact> contacts();
	ListSignal<ContactAttributes> contactAttributes(); //Refactor: Apps dont need to know all this. They only need to know contact ids.

	Asker asker();
	TransferQueue transfer();
	
	Object prevalentState();

}
