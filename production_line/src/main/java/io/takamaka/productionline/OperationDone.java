package io.takamaka.productionline;

import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.View;
import io.takamaka.code.lang.Exported;

@Exported
public class OperationDone extends Storage {
	private String name;
	private Machine machine;
	private Measurements measurements;

	public OperationDone(String name, Machine machine, Measurements measurements) {
		this.name = name;
		this.measurements = measurements;
		this.machine = machine;
	}

	public OperationDone(String name, Machine machine) {
		this.name = name;
		this.measurements = null;
		this.machine = machine;
	}

	public void setMachine(Machine machine) {
		this.machine = machine;
	}

	public void setMeasurements(Measurements measurements) {
		this.measurements = measurements;
	}

	@View
	public String getName() {
		return name;
	}

	@View
	public Machine getMachine() {
		return machine;
	}

	@View
	public Measurements getMeasurements() {
		return measurements;
	}

	@Override
	public @View String toString() {
		return "Operation [name=" + name + ", machine=" + machine.toString() + "]";
	}

}