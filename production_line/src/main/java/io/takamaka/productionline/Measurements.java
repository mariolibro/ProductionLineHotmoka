package io.takamaka.productionline;

import io.takamaka.code.lang.Exported;
import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.View;

@Exported
public abstract class Measurements extends Storage {
	private int startTime = 0;
	private int endTime = 0;
	private int duration;
	private int frequency;

	public Measurements(int startTime, int endTime, int duration, int frequency) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.duration = duration;
		this.frequency = frequency;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}

	@View
	public int getStartTime() {
		return startTime;
	}

	@View
	public int getEndTime() {
		return endTime;
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