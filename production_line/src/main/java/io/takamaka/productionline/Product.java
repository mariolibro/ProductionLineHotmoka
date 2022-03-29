package io.takamaka.productionline;
import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.Exported;
import io.takamaka.code.util.StorageLinkedList;
import io.takamaka.code.util.StorageList;

@Exported
public class Product extends Storage{
	private String name;
	private StorageList<Operation> recipe;
	private boolean recipeCompleted = false;
	private boolean constraintRespected = false;
	
	public Product(String name) {
		this.name = name;
		this.recipe = new StorageLinkedList<Operation>();
	}
	
	public boolean isRecipeCompleted() {
		return recipeCompleted;
	}

	public void setRecipeCompleted(boolean recipeCompleted) {
		this.recipeCompleted = recipeCompleted;
	}

	public boolean isConstraintRespected() {
		return constraintRespected;
	}

	public void setConstraintRespected(boolean constraintRespected) {
		this.constraintRespected = constraintRespected;
	}

	public void addOperation(Operation operation) {
		recipe.add(operation);	
	}
	
	public boolean removeOperation(Operation operation) {
		  if(recipe.contains(operation)) {
			  recipe.remove(operation);
			  return true;
		  }
		  return false;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		String tmp = "";
		if (recipe != null) {
		for (Operation operation: recipe)
			  tmp += operation.getName() + " ";
		}
		return "Product: [name=" + name + ", recipeCompleted=" + recipeCompleted +
				", constraintRespected=" + constraintRespected + ", recipe=" + tmp + "]";
	}
	
}
