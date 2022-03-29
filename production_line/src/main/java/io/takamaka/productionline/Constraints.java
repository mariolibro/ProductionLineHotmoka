package io.takamaka.productionline;
import io.takamaka.code.lang.Exported;
import io.takamaka.code.lang.Storage;
//import io.takamaka.code.lang.Exported;


@Exported
public abstract class Constraints extends Storage{
	private int duration;
	private int frequency;
	private boolean respected = false;

	public Constraints(int duration, int frequency){
		this.duration = duration;
		this.frequency = frequency;
	}

	public boolean isRespected() {
		return respected;
	}

	public void setRespected(boolean respected) {
		this.respected = respected;
	}

	public int getDuration() {
		return duration;
	}

	public int getFrequency() {
		return frequency;
	}
	
	
}