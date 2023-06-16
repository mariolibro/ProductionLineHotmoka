package io.takamaka.productionline;

import io.takamaka.code.lang.Exported;
import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.View;


@Exported
public abstract class Constraints extends Storage {
	private int duration;
	private int frequency;

	public Constraints(int duration, int frequency) {
		this.duration = duration;
		this.frequency = frequency;
	}

	@View
	public int getDuration() {
		return duration;
	}

	@View
	public int getFrequency() {
		return frequency;
	}

}