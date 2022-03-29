package io.takamaka.productionline;
import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.Exported;

@Exported
public class PrinterConstraints extends Constraints{
	private String filamentType;
	private String filamentColour;
	private float plateTemperatureMax;
	private float plateTemperatureMin;
	
	public PrinterConstraints(
			String filamentType, String filamentColour, 
			float plateTemperatureMax,
			float plateTemperatureMin, 
			int duration, int frequency) {
				super(duration, frequency);
				this.filamentType = filamentType;
				this.filamentColour = filamentColour;
				this.plateTemperatureMax = plateTemperatureMax;
				this.plateTemperatureMin = plateTemperatureMin;
	}

	public String getFilamentType() {
		return filamentType;
	}

	public void setFilamentType(String filamentType) {
		this.filamentType = filamentType;
	}

	public String getFilamentColour() {
		return filamentColour;
	}

	public void setFilamentColour(String filamentColour) {
		this.filamentColour = filamentColour;
	}

	public float getPlateTemperatureMax() {
		return plateTemperatureMax;
	}

	public void setPlateTemperatureMax(float plateTemperatureMax) {
		this.plateTemperatureMax = plateTemperatureMax;
	}

	public float getPlateTemperatureMin() {
		return plateTemperatureMin;
	}

	public void setPlateTemperatureMin(float plateTemperatureMin) {
		this.plateTemperatureMin = plateTemperatureMin;
	}

	@Override
	public String toString() {
		return "PrinterConstraints [filamentType=" + filamentType + ", filamentColour=" + filamentColour
				+ ", plateTemperatureMax=" + plateTemperatureMax + ", plateTemperatureMin=" + plateTemperatureMin +
				", duration=" + getDuration() +", frequency=" + getFrequency() +"]";
	}


	
	

}
