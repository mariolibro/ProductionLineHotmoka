package io.takamaka.productionline;
import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.Exported;

@Exported
public class Machine extends Storage{
	private String name;
	private String operator;
	private Measurements measurements;

	public Machine(String name, String operator, Measurements measurements) {
		this.name = name;
		this.operator = operator;
		this.measurements = measurements;
	}
	
	public String getName() {
		return name;
	}
	
	public String getOperator() {
		return operator;
	}
	
	public void setMeasurements(Measurements measurements) {
		this.measurements = measurements;
	}
	
	public Measurements getMeasurements() {
		return measurements;
	}

	@Override
	public String toString() {
		return "Machine [name=" + name + ", operator=" + operator + ", measurements=" + measurements.toString() + "]";
	}

}
