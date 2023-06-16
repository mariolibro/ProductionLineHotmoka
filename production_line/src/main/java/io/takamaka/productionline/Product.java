package io.takamaka.productionline;

import io.takamaka.code.lang.Exported;
import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.View;
import io.takamaka.code.util.StorageLinkedList;
import io.takamaka.code.util.StorageList;

@Exported
public class Product extends Storage {
	private String id;
	private StorageList<OperationDone> operations_done;

	public Product(String id) {
		this.id = id;
		this.operations_done = new StorageLinkedList<OperationDone>();
	}

	public void addOperationDone(OperationDone operationDone) {
		operations_done.add(operationDone);
	}
	
	@View
	public String getId() {
		return id;
	}
	
	@View
	public StorageList<OperationDone> getOperations_done() {
		return operations_done;
	}

	@Override
	public @View String toString() {
		String tmp = "";
		if (operations_done != null) {
			for (OperationDone operationDone : operations_done)
				tmp += operationDone.getName() + " ";
		}
		return "Product [id=" + id + ", operations_done=" + tmp + "]";
	}

}