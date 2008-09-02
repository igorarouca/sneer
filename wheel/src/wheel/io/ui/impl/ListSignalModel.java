package wheel.io.ui.impl;

import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractListModel;

import wheel.lang.Omnivore;
import wheel.lang.Types;
import wheel.reactive.Signal;
import wheel.reactive.lists.ListSignal;
import wheel.reactive.lists.impl.VisitingListReceiver;

public class ListSignalModel<T> extends AbstractListModel {

	public interface SignalChooser<E> {
		Signal<?>[] signalsToReceiveFrom(E element);
	}

	private final ListSignal<T> _input;
	private final List<Omnivore<?>> _elementReceivers = new LinkedList<Omnivore<?>>();
	private final SignalChooser<T> _chooser;
	private final ListChangeReceiver _listReceiverToAvoidGc = new ListChangeReceiver();

	public ListSignalModel(ListSignal<T> input, SignalChooser<T> chooser) {
		_input = input;
		_chooser = chooser;
		
		_input.addListReceiver(_listReceiverToAvoidGc);
	}

	public ListSignalModel(ListSignal<T> input) {
		this(input, null);
	}

	private class ListChangeReceiver extends VisitingListReceiver {

		@Override
		public void elementAdded(int index) {
			addReceiverToElement(index);
			fireIntervalAdded(this, index, index);
		}

		@Override
		public void elementToBeRemoved(int index) {
			removeReceiverFromElement(index);
		}

		@Override
		public void elementRemoved(int index) {
			fireIntervalRemoved(this, index, index);
		}

		@Override
		public void elementToBeReplaced(int index) {
			removeReceiverFromElement(index);
		}

		@Override
		public void elementReplaced(int index) {
			addReceiverToElement(index);
			fireContentsChanged(this, index, index);
		}

	}
	
	public int getSize() {
		return _input.currentSize();
	}
	
	public T getElementAt(int index) {
		return _input.currentGet(index);
	}

	private void removeReceiverFromElement(int index) {
		T element = getElementAt(index);
		Omnivore<?> receiver = _elementReceivers.remove(index);

		if (_chooser == null) return;
		for (Signal<?> signal : _chooser.signalsToReceiveFrom(element))
			removeReceiverFromSignal(receiver, signal);
	}

	private void addReceiverToElement(int index) {
		T element = getElementAt(index);

		Omnivore<?> receiver = createElementReceiver(element);
		_elementReceivers.add(index, receiver);
		
		if (_chooser == null) return;
		for (Signal<?> signal : _chooser.signalsToReceiveFrom(element))
			addReceiverToSignal(receiver, signal);
	}

	private <U> void addReceiverToSignal(Omnivore<?> receiver, Signal<U> signal) {
		Omnivore<U> castedReceiver = Types.cast(receiver);
		signal.addReceiver(castedReceiver);
	}
	
	private <U> void removeReceiverFromSignal(Omnivore<?> receiver, Signal<U> signal) {
		Omnivore<U> casted = Types.cast(receiver);
		signal.removeReceiver(casted);
	}

	private <U> Omnivore<U> createElementReceiver(final T element) {
		return new Omnivore<U>() { public void consume(U ignored) {
			int i = 0;
			for (T candidate : _input) {  //Optimize
				if (candidate == element) fireContentsChanged(this, i, i);
				i++;
			}
		}};
	}

	private static final long serialVersionUID = 1L;

}
