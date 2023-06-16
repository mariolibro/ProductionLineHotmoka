package io.takamaka.productionline;

import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.View;
import io.takamaka.code.lang.Exported;

@Exported
public class OperationToDo extends Storage {
	private String name;
	private Machine machine;
	private Constraints constraints;

	public OperationToDo(String name, Machine machine, Constraints constraints) {
		this.name = name;
		this.constraints = constraints;
		this.machine = machine;
	}

	public OperationToDo(String name) {
		this.name = name;
		this.constraints = null;
		this.machine = null;
	}

	public void setMachine(Machine machine) {
		this.machine = machine;
	}

	public void setConstraints(Constraints constraints) {
		this.constraints = constraints;
	}

	@View
	public String getName() {
		return name;
	}

	@View
	public Constraints getConstraints() {
		return constraints;
	}

	@View
	public Machine getMachine() {
		return machine;
	}

	@Override
	public @View String toString() {
		return "OperationToDo [name=" + name + ", machine=" + machine.toString();
	}

}