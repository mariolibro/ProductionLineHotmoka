package io.takamaka.productionline;
import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.Exported;


@Exported
public class Operation extends Storage{
	private String name;
	private Machine machine;
	private Constraints constraints;
	private boolean status = false;
	
	
	public Operation(String name, Machine machine, Constraints constraints) {
		this.name = name;
		this.constraints = constraints;
		this.machine = machine;
	}
	
	public Operation(String name) {
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

	public String getName() {
		return name;
	}
	
	public boolean getStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Operation [name=" + name + ", machine=" + machine.toString() + ", constraintsRespected="
				+ constraints.isRespected() + ", status=" + status +"]";
	}
		
}