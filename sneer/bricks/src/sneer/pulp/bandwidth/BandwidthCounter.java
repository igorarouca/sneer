package sneer.pulp.bandwidth;

import sneer.kernel.container.Brick;
import wheel.reactive.Signal;

public interface BandwidthCounter extends Brick {

	void sent(int sizeBytes);
	void received(int sizeBytes);

	Signal<Integer> downloadSpeed();
	Signal<Integer> uploadSpeed();
}