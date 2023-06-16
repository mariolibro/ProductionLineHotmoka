package io.takamaka.productionline;

import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.View;
import io.takamaka.code.util.StorageLinkedList;
import io.takamaka.code.util.StorageList;
import io.takamaka.code.lang.Exported;

@Exported
public class Machine extends Storage {
	private String name;
	private StorageList<Measurements> measurements_list;

	public Machine(String name) {
		this.name = name;
		this.measurements_list = new StorageLinkedList<Measurements>();
		;
	}

	public void addMeasurement(Measurements measurements) {
		measurements_list.add(measurements);
	}

	@View
	public String getName() {
		return name;
	}

	@View
	public StorageList<Measurements> getMeasurements_list() {
		return measurements_list;
	}

	@Override
	public @View String toString() {
		return "Machine [name=" + name + ", measurements=" + measurements_list.toString() + "]";
	}

}
