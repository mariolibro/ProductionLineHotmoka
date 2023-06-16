package io.takamaka.productionline;

import io.takamaka.code.lang.Exported;
import io.takamaka.code.lang.View;

@Exported
public class MillingMachineMeasurements extends Measurements {
	private String bit;
	private float bitSize;
	private float chuckVelocityMax;
	private float chuckVelocityMin;

	public MillingMachineMeasurements(String bit, float bitSize, float chuckVelocityMax, float chuckVelocityMin,
			int frequency, int startTime, int endTime) {
		super(startTime, endTime, endTime - startTime, frequency);
		this.bit = bit;
		this.bitSize = bitSize;
		this.chuckVelocityMax = chuckVelocityMax;
		this.chuckVelocityMin = chuckVelocityMin;
	}

	@View
	public String getBit() {
		return bit;
	}

	@View
	public float getBitSize() {
		return bitSize;
	}

	@View
	public float getChuckVelocityMax() {
		return chuckVelocityMax;
	}

	@View
	public float getChuckVelocityMin() {
		return chuckVelocityMin;
	}

	public void setBit(String bit) {
		this.bit = bit;
	}

	public void setBitSize(float bitSize) {
		this.bitSize = bitSize;
	}

	public void setEndTime(int endTime) {
		super.setEndTime(endTime);
	}

	@Override
	public @View String toString() {
		return "MillingMachineMeasurements [bit=" + bit + ", bitSize=" + bitSize + ", startTime=" + super.getStartTime()
				+ ", endTime=" + super.getEndTime() + ", duration=" + super.getDuration() + "]";
	}

}
