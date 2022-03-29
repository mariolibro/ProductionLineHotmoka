package io.takamaka.productionline;
import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.Exported;
import io.takamaka.code.util.StorageLinkedList;
import io.takamaka.code.util.StorageList;

@Exported
public class PrinterMeasurements extends Measurements{
	private String filamentType;
	private String filamentColour;
	private StorageList<Float> plateTemperatures;
	
	public PrinterMeasurements(
			String filamentType, 
			String filamentColour,
			int startTime,
			int duration) {
				super(startTime, duration);
				this.filamentType = filamentType;
				this.filamentColour = filamentColour;
				this.plateTemperatures = new StorageLinkedList<Float>();
	}

	public String getFilamentType() {
		return filamentType;
	}

	public String getFilamentColour() {
		return filamentColour;
	}

	public void addPlateTemperatures(float plateTemperature) {
		plateTemperatures.add(plateTemperature);
	}
	
	public void setEndTime(int endTime) {
		super.setEndTime(endTime);
	}

}
