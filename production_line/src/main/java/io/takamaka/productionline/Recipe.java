package io.takamaka.productionline;

import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.View;

import java.util.Iterator;

import io.takamaka.code.lang.Exported;
import io.takamaka.code.util.StorageLinkedList;
import io.takamaka.code.util.StorageList;

@Exported
public class Recipe extends Storage {
	private String name;
	private StorageList<OperationToDo> operations_to_do;
	private StorageList<Product> products_produced;

	public Recipe(String name) {
		this.name = name;
		this.operations_to_do = new StorageLinkedList<OperationToDo>();
		this.products_produced = new StorageLinkedList<Product>();
	}

	@View
	public String checkConstraints() {
		StorageList<OperationDone> operations_done = new StorageLinkedList<OperationDone>();
		String tmp = "";
		tmp += this.name + ": [";
		for (OperationToDo opToDo : operations_to_do) {
			tmp += "(" + opToDo.getName() + "," + opToDo.getMachine().getName() + ")";
		}
		tmp += "]\n";
		for (Product product : this.products_produced) {
			tmp += "\t" + product.getId() + ": [recipe_respected|constraints_respected]->";
			if (checkRecipeRespectedForProduct(product))
				tmp += "[V|";
			else
				tmp += "[X|";
			if (checkConstraintRespectedForProduct(product))
				tmp += "V]\n";
			else
				tmp += "X]\n";

			operations_done = product.getOperations_done();
			int count = 1;
			for (OperationDone opDone : operations_done) {
				tmp += "\t \t(" + opDone.getName() + "," + opDone.getMachine().getName() + ")->[";
				if (checkConstraintRespectedForOperationDone(product, opDone) && count <= operations_to_do.size())
					tmp += "V] \n";
				else
					tmp += "X] \n";
				count++;
			}
		}

		return tmp;
	}

	// check if the couple (machine,operationDone) is in the right order, respect to operationtToDo
	@View
	public boolean checkRecipeRespectedForProduct(Product productToCheck) {
		StorageList<OperationDone> operations_done = new StorageLinkedList<OperationDone>();

		for (Product product : this.products_produced) {
			if (product.equals(productToCheck)) {
				operations_done = product.getOperations_done();
				break;
			}
		}

		if (operations_to_do.size() != operations_done.size()) {
			return false;
		}

		Iterator<OperationToDo> iterToDo = operations_to_do.iterator();
		Iterator<OperationDone> iterDone = operations_done.iterator();
		boolean flag = true;
		while (iterToDo.hasNext() && iterDone.hasNext()) {
			OperationToDo opToDo = iterToDo.next();
			OperationDone opDone = iterDone.next();

			String nameToDo = opToDo.getName();
			String nameDone = opDone.getName();
			String machineToDo = opToDo.getMachine().getName();
			String machineDone = opDone.getMachine().getName();

			if (!nameToDo.equals(nameDone) || !machineToDo.equals(machineDone)) {
				flag = false;
				break;
			}
		}

		return flag;
	}

	@View
	//check if there is at least an operationDone that didn't respected its constraints
	public boolean checkConstraintRespectedForProduct(Product productToCheck) {
		if (!checkRecipeRespectedForProduct(productToCheck))
			return false;

		StorageList<OperationDone> operations_done = new StorageLinkedList<OperationDone>();

		for (Product product : products_produced) {
			if (product.equals(productToCheck)) {
				operations_done = product.getOperations_done();
				break;
			}
		}

		Iterator<OperationToDo> iterToDo = operations_to_do.iterator();
		Iterator<OperationDone> iterDone = operations_done.iterator();

		while (iterToDo.hasNext() && iterDone.hasNext()) {

			Constraints constraints = iterToDo.next().getConstraints();
			Measurements measurement = iterDone.next().getMeasurements();

			if (constraints instanceof MillingMachineConstraints && measurement instanceof MillingMachineMeasurements) {
				MillingMachineConstraints millingConstraints = (MillingMachineConstraints) constraints;
				MillingMachineMeasurements millingMeasurement = (MillingMachineMeasurements) measurement;
				if (!(millingMeasurement.getDuration() <= millingConstraints.getDuration()
						&& millingMeasurement.getBit().equals(millingConstraints.getBit())
						&& millingMeasurement.getBitSize() == millingConstraints.getBitSize()
						&& millingMeasurement.getChuckVelocityMax() <= millingConstraints.getChuckVelocityMax()
						&& millingMeasurement.getChuckVelocityMin() >= millingConstraints.getChuckVelocityMin()
						&& millingMeasurement.getFrequency() == millingConstraints.getFrequency()))
					return false;
			} else if (constraints instanceof PrinterConstraints && measurement instanceof PrinterMeasurements) {
				PrinterConstraints printerConstraints = (PrinterConstraints) constraints;
				PrinterMeasurements printerMeasurement = (PrinterMeasurements) measurement;
				if (!(printerMeasurement.getDuration() <= printerConstraints.getDuration()
						&& printerMeasurement.getFilamentType().equals(printerConstraints.getFilamentType())
						&& printerMeasurement.getFilamentColour().equals(printerConstraints.getFilamentColour())
						&& printerMeasurement.getTemperatureMax() <= printerConstraints.getPlateTemperatureMax()
						&& printerMeasurement.getTemperatureMin() >= printerConstraints.getPlateTemperatureMin()
						&& printerMeasurement.getFrequency() == printerConstraints.getFrequency()))
					return false;
			} else {
				return false;
			}
		}
		return true;
	}

	@View
	//for the OperationDone that are in the right spot check if constraints are respected
	public boolean checkConstraintRespectedForOperationDone(Product productToCheck,
			OperationDone operationDoneToCheck) {
		StorageList<OperationDone> operations_done = new StorageLinkedList<OperationDone>();

		for (Product product : products_produced) {
			if (product.equals(productToCheck)) {
				operations_done = product.getOperations_done();
				break;
			}
		}

		Iterator<OperationToDo> iterToDo = operations_to_do.iterator();
		Iterator<OperationDone> iterDone = operations_done.iterator();
		while (iterToDo.hasNext() && iterDone.hasNext()) {
			OperationDone opDone = iterDone.next();
			OperationToDo opToDo = iterToDo.next();
			String nameToDo = opToDo.getName();
			String nameDone = opDone.getName();
			String machineToDo = opToDo.getMachine().getName();
			String machineDone = opDone.getMachine().getName();
			if (opDone.equals(operationDoneToCheck)) {
				if (!nameToDo.equals(nameDone) || !machineToDo.equals(machineDone)) {
					//make sure that the operationDone is in the right spot in respect to the recipe
					return false;
				}
				if (machineDone.equals("MillingMachine") && machineToDo.equals("MillingMachine")) { // substitute with instanceof
					MillingMachineConstraints millingConstraints = (MillingMachineConstraints) opToDo.getConstraints();
					MillingMachineMeasurements millingMeasurement = (MillingMachineMeasurements) opDone
							.getMeasurements();
					if (!(millingMeasurement.getDuration() <= millingMeasurement.getDuration()
							&& millingMeasurement.getBit().equals(millingConstraints.getBit())
							&& millingMeasurement.getBitSize() == millingConstraints.getBitSize()
							&& millingMeasurement.getChuckVelocityMax() <= millingConstraints.getChuckVelocityMax()
							&& millingMeasurement.getChuckVelocityMin() >= millingConstraints.getChuckVelocityMin()
							&& millingMeasurement.getFrequency() == millingConstraints.getFrequency())) {
						return false;
					}
				} else if (machineDone.equals("Printer") && machineToDo.equals("Printer")) {
					PrinterConstraints printerConstraints = (PrinterConstraints) opToDo.getConstraints();
					PrinterMeasurements printerMeasurement = (PrinterMeasurements) opDone.getMeasurements();
					if (!(printerMeasurement.getDuration() <= printerConstraints.getDuration()
							&& printerMeasurement.getFilamentType().equals(printerConstraints.getFilamentType())
							&& printerMeasurement.getFilamentColour().equals(printerConstraints.getFilamentColour())
							&& printerMeasurement.getTemperatureMax() <= printerConstraints.getPlateTemperatureMax()
							&& printerMeasurement.getTemperatureMin() >= printerConstraints.getPlateTemperatureMin()
							&& printerMeasurement.getFrequency() == printerConstraints.getFrequency())) {
						return false;
					}
				}
				break;
			}
		}
		return true;
	}

	@View
	public StorageList<OperationToDo> getOperations_to_do() {
		return operations_to_do;
	}

	@View
	public StorageList<Product> getProducts_produced() {
		return products_produced;
	}

	public void addOperationToDo(OperationToDo operationToDo) {
		operations_to_do.add(operationToDo);
	}

	public boolean removeOperationToDo(OperationToDo operationToDo) {
		if (operations_to_do.contains(operationToDo)) {
			operations_to_do.remove(operationToDo);
			return true;
		}
		return false;
	}

	public void addProductProduced(Product product) {
		products_produced.add(product);
	}

	public boolean removeProduct(Product product) {
		if (products_produced.contains(product)) {
			products_produced.remove(product);
			return true;
		}
		return false;
	}

	@View
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		String tmp = "";
		String tmp1 = "";
		if (operations_to_do != null) {
			for (OperationToDo operationToDo : operations_to_do)
				tmp += operationToDo.getName() + " ";
		}

		if (products_produced != null) {
			for (Product product : products_produced)
				tmp1 += product.getId() + " ";
		}
		return "Recipe: [name=" + name + ", recipe=" + tmp + ", products=" + tmp1 + "]";
	}

}
