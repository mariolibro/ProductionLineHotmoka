package io.takamaka.productionline;

import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.Exported;
import io.takamaka.code.util.StorageLinkedList;
import io.takamaka.code.util.StorageList;
import io.takamaka.code.lang.View;

@Exported
public class Company extends Storage {
	private String name;
	private String manager;
	private StorageList<Recipe> recipes;

	public Company(String name, String manager) {
		this.name = name;
		this.manager = manager;
		this.recipes = new StorageLinkedList<Recipe>();
	}

	public void addRecipe(Recipe recipe) {
		recipes.add(recipe);
	}

	@View
	public String getName() {
		return name;
	}

	@View
	public String getManager() {
		return manager;
	}

	@View
	public StorageList<Recipe> getRecipes() {
		return recipes;
	}

	@Override
	public @View String toString() {
		String tmp = "";
		if (recipes != null) {
			for (Recipe recipe : recipes)
				tmp += recipe.getName() + " ";
		}
		return "Company: [name=" + name + ", manager=" + manager + ", recipes=" + tmp + "]";
	}

}