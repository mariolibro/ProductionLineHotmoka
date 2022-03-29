package io.takamaka.productionline;
import io.takamaka.code.lang.Exported;
import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.Exported;

@Exported
public abstract class Measurements extends Storage{
	private int startTime=0;
	private int endTime=0;
	private int duration;

	public Measurements(int startTime, int duration){
		this.startTime = endTime;
		this.duration = duration;
	}
	
	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}
	
	public int getStartTime() {
		return startTime;
	}
	
	public int getEndTime() {
		return endTime;
	}
	
	public int getDuration() {
		if(endTime!=0)
			return endTime-startTime;
		else
			return -1;
	}
	
}