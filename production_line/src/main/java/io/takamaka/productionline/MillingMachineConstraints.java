package io.takamaka.productionline;
import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.Exported;

@Exported
public class MillingMachineConstraints extends Constraints{
	private String bit;
	private float bitSize;
	private float chuckVelocityMax;
	private float chuckVelocityMin;
	
	public MillingMachineConstraints(
			String bit, 
			float bitSize,
			float chuckVelocityMax,
			float chuckVelocityMin, 
			int duration,int frequency) {
				super(duration, frequency);
				this.bit = bit;
				this.bitSize = bitSize;
				this.chuckVelocityMax = chuckVelocityMax;
				this.chuckVelocityMin = chuckVelocityMin;
	}

	public String getBit() {
		return bit;
	}

	public void setBit(String bit) {
		this.bit = bit;
	}

	public float getBitSize() {
		return bitSize;
	}

	public void setBitSize(float bitSize) {
		this.bitSize = bitSize;
	}

	public float getChuckVelocityMax() {
		return chuckVelocityMax;
	}

	public void setChuckVelocityMax(float chuckVelocityMax) {
		this.chuckVelocityMax = chuckVelocityMax;
	}

	public float getChuckVelocityMin() {
		return chuckVelocityMin;
	}

	public void setChuckVelocityMin(float chuckVelocityMin) {
		this.chuckVelocityMin = chuckVelocityMin;
	}
	
	public void setRespected(boolean respected) {
		super.setRespected(respected);
	}
	
	public String toString() {
		return "MillingMachineConstraints: [bit=" + bit + ", bitSize=" + bitSize +
				", chuckVelocityMax=" + chuckVelocityMax + ", chuckVelocityMin=" + chuckVelocityMin +
				", duration=" + getDuration() + ", frequency=" + getFrequency() + ", respected=" + isRespected() +"]";
	}


}
