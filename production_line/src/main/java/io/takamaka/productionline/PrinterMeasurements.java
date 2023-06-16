package io.takamaka.productionline;

import io.takamaka.code.lang.View;
import io.takamaka.code.lang.Exported;

@Exported
public class PrinterMeasurements extends Measurements {

	private String filamentType;
	private String filamentColour;
	private float temperatureMax;
	private float temperatureMin;

	public PrinterMeasurements(String filamentType, String filamentColour, float temperatureMax, float temperatureMin,
			int frequency, int startTime, int endTime) {
		super(startTime, endTime, endTime - startTime, frequency);
		this.filamentType = filamentType;
		this.filamentColour = filamentColour;
		this.temperatureMax = temperatureMax;
		this.temperatureMin = temperatureMin;
	}

	@View
	public float getTemperatureMin() {
		return temperatureMin;
	}

	@View
	public float getTemperatureMax() {
		return temperatureMax;
	}

	@View
	public String getFilamentType() {
		return filamentType;
	}

	@View
	public String getFilamentColour() {
		return filamentColour;
	}

	public void setEndTime(int endTime) {
		super.setEndTime(endTime);
	}

}
