package io.takamaka.productionline;

import io.takamaka.code.lang.Exported;
import io.takamaka.code.util.StorageLinkedList;
import io.takamaka.code.util.StorageList;

@Exported
public class MillingMachineMeasurements extends Measurements{
	private String bit;
	private float bitSize;
	private StorageList<Float> chuckVelocities;
	
	public MillingMachineMeasurements(
			String bit, 
			float bitSize,
			int startTime,
			int duration) {
				super(startTime, duration);
				this.bit = bit;
				this.bitSize = bitSize;
				this.chuckVelocities = new StorageLinkedList<Float>();
	}

	public String getBit() {
		return bit;
	}

	public float getBitSize() {
		return bitSize;
	}

	public void setBit(String bit) {
		this.bit = bit;
	}

	public void setBitSize(float bitSize) {
		this.bitSize = bitSize;
	}

	public void addChuckVelocity(float chuckVelocity) {
		chuckVelocities.add(chuckVelocity);
	}
	
	public void setEndTime(int endTime) {
		super.setEndTime(endTime);
	}

	@Override
	public String toString() {
		return "MillingMachineMeasurements [bit=" + bit + ", bitSize=" + bitSize + ", chuckVelocities="
				+ chuckVelocities + ", startTime="+ super.getStartTime() + ", endTime=" + super.getEndTime() + ", duration=" + super.getDuration() +"]";
	}


}
