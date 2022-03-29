package io.takamaka.productionline;
import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.Exported;
import io.takamaka.code.util.StorageLinkedList;
import io.takamaka.code.util.StorageList;

@Exported
public class Company extends Storage{
  private String name;
  private String manager;
  private StorageList<Product> products;
  
  public Company(String name, String manager) {
	  this.name = name;
	  this.manager = manager;
	  this.products = new StorageLinkedList<Product>();
  }
  
  public void addProduct(Product product) {
	  products.add(product);
  }
  
  public boolean removeProduct(Product product) {
	  if(products.contains(product)) {
		  products.remove(product);
		  return true;
	  }
	  return false;
  }

  @Override
  public String toString() {
	  String tmp ="";
	  if(products != null) {
	  for (Product product: products)
		  tmp += product.getName() + " ";
	  }
	  return "Company: [name=" + name + ", manager=" + manager + ", products=" + tmp + "]";
  }
		  
}