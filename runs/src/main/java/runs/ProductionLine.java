package runs;

import static io.hotmoka.beans.Coin.panarea;
import static io.hotmoka.beans.types.BasicTypes.INT;
import static java.math.BigInteger.ONE;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import io.hotmoka.beans.SignatureAlgorithm;
import io.hotmoka.beans.references.TransactionReference;
import io.hotmoka.beans.requests.ConstructorCallTransactionRequest;
import io.hotmoka.beans.requests.InstanceMethodCallTransactionRequest;
import io.hotmoka.beans.requests.JarStoreTransactionRequest;
import io.hotmoka.beans.requests.SignedTransactionRequest;
import io.hotmoka.beans.requests.SignedTransactionRequest.Signer;
import io.hotmoka.beans.signatures.CodeSignature;
import io.hotmoka.beans.signatures.ConstructorSignature;
import io.hotmoka.beans.signatures.NonVoidMethodSignature;
import io.hotmoka.beans.signatures.VoidMethodSignature;
import io.hotmoka.beans.types.BasicTypes;
import io.hotmoka.beans.types.ClassType;
import io.hotmoka.beans.values.BigIntegerValue;
import io.hotmoka.beans.values.BooleanValue;
import io.hotmoka.beans.values.FloatValue;
import io.hotmoka.beans.values.IntValue;
import io.hotmoka.beans.values.StorageReference;
import io.hotmoka.beans.values.StorageValue;
import io.hotmoka.beans.values.StringValue;
import io.hotmoka.crypto.Account;
import io.hotmoka.crypto.SignatureAlgorithmForTransactionRequests;
import io.hotmoka.helpers.GasHelper;
import io.hotmoka.helpers.SignatureHelper;
import io.hotmoka.nodes.Node;
import io.hotmoka.remote.RemoteNode;
import io.hotmoka.remote.RemoteNodeConfig;

public class ProductionLine {

  private final static String ADDRESS = "b3f24b2c5464a57ec021f235c2027b67f55ebd77683b2b0a0223c836ae3f77fc#0";
  private final static String PASSWORD = "mario";
  
  private final static ClassType COMPANY = new ClassType("io.takamaka.productionline.Company");
  private final static ClassType PRODUCT = new ClassType("io.takamaka.productionline.Product");
  private final static ClassType OPERATION = new ClassType("io.takamaka.productionline.Operation");
  private final static ClassType CONSTRAINTS = new ClassType("io.takamaka.productionline.Constraints");
  private final static ClassType PRINTERCONSTRAINTS = new ClassType("io.takamaka.productionline.PrinterConstraints");
  private final static ClassType MILLINGMACHINECONSTRAINTS = new ClassType("io.takamaka.productionline.MillingMachineConstraints");
  private final static ClassType MACHINE = new ClassType("io.takamaka.productionline.Machine");
  private final static ClassType MEASUREMENTS = new ClassType("io.takamaka.productionline.Measurements");
  private final static ClassType MILLINGMACHINEMEASUREMENTS = new ClassType("io.takamaka.productionline.MillingMachineMeasurements");
  private final static ClassType PRINTERMEASUREMENTS = new ClassType("io.takamaka.productionline.PrinterMeasurements");

  //==================================================
  // Constructors
  //==================================================
  
  private final static ConstructorSignature CONSTRUCTOR_COMPANY = new ConstructorSignature(COMPANY, ClassType.STRING, ClassType.STRING);
  private final static ConstructorSignature CONSTRUCTOR_PRODUCT = new ConstructorSignature(PRODUCT, ClassType.STRING);
  private final static ConstructorSignature CONSTRUCTOR_OPERATION1 = new ConstructorSignature(OPERATION, ClassType.STRING, MACHINE, CONSTRAINTS);
  private final static ConstructorSignature CONSTRUCTOR_OPERATION2 = new ConstructorSignature(OPERATION, ClassType.STRING);
  private final static ConstructorSignature CONSTRUCTOR_PRINTERCONSTRAINTS = new ConstructorSignature(PRINTERCONSTRAINTS, ClassType.STRING, ClassType.STRING);
  private final static ConstructorSignature CONSTRUCTOR_MILLINGMACHINECONSTRAINTS = new ConstructorSignature(MILLINGMACHINECONSTRAINTS, ClassType.STRING, BasicTypes.FLOAT, BasicTypes.FLOAT, BasicTypes.FLOAT, BasicTypes.INT, BasicTypes.INT);
  private final static ConstructorSignature CONSTRUCTOR_MACHINE = new ConstructorSignature(MACHINE, ClassType.STRING, ClassType.STRING, MEASUREMENTS);
  private final static ConstructorSignature CONSTRUCTOR_MILLINGMACHINEMEASUREMENTS = new ConstructorSignature(MILLINGMACHINEMEASUREMENTS, ClassType.STRING, BasicTypes.FLOAT, BasicTypes.INT, BasicTypes.INT);
  private final static ConstructorSignature CONSTRUCTOR_PRINTERMEASUREMENTS = new ConstructorSignature(PRINTERMEASUREMENTS, ClassType.STRING, ClassType.STRING);
  
  private final Node node;
  private final Path productionLinePath = Paths.get("../production_line/target/production_line-0.0.1.jar");
  private final TransactionReference takamakaCode;
  private final TransactionReference productionLine;
  private final StorageReference account;
  private final Signer signer;
  private final KeyPair keys;
  private final String chainId;
  private final GasHelper gasHelper;
  private BigInteger nonce;
    
  public static void main(String[] args) throws Exception {
	    RemoteNodeConfig config = new RemoteNodeConfig.Builder()
	      .setURL("panarea.hotmoka.io")
	      .build();

	    try (Node node = RemoteNode.of(config)) {
	    	new ProductionLine(node);
	    }
	}

  private ProductionLine(Node node) throws Exception {
	  this.node = node;
	  takamakaCode = node.getTakamakaCode();
      account = new StorageReference(ADDRESS);
      // we get the signing algorithm to use for requests
      SignatureAlgorithm<SignedTransactionRequest> signature
        = SignatureAlgorithmForTransactionRequests.mk
            (node.getNameOfSignatureAlgorithmForRequests());
      keys = loadKeys(node, account);
      // we create a signer that signs with the private key of our account
      signer = Signer.with(signature, keys.getPrivate());
      gasHelper = new GasHelper(node);
      // we get the nonce of our account: we use the account itself as caller and
      // an arbitrary nonce (ZERO in the code) since we are running
      // a @View method of the account
      nonce = ((BigIntegerValue) node
        .runInstanceMethodCallTransaction(new InstanceMethodCallTransactionRequest
          (account, // payer
          BigInteger.valueOf(100_000_00), // gas limit
          takamakaCode, // class path for the execution of the transaction
          CodeSignature.NONCE, // method
          account))) // receiver of the method call
        .value;

      // we get the chain identifier of the network
      chainId = getChainId();
      productionLine = installJar();
      // we increase our copy of the nonce, ready for further
      // transactions having the account as payer
      nonce = nonce.add(ONE);
      
      //==================================================
      //MillingMachineConstraints object Instantiation
      //==================================================
      
      String millingMachineConstraintsBit = "bit";
      float millingMachineConstraintsBitSize = 1.0f;
      float millingMachineConstraintsChuckVelocityMax = 1.0f;
      float millingMachineConstraintsChuckVelocityMin = 1.0f;
      int millingMachineConstraintsDuration = 1;
      int millingMachineConstraintsFrequency = 2;
      
      StorageReference millingMachineConstraints = millingMachineConstraintsConstructor(
    		  millingMachineConstraintsBit,
    		  millingMachineConstraintsBitSize,
    		  millingMachineConstraintsChuckVelocityMax,
    		  millingMachineConstraintsChuckVelocityMin,
    		  millingMachineConstraintsDuration,
    		  millingMachineConstraintsFrequency);
      
      millingMachineConstraintsSetBit(millingMachineConstraints, "bit2");
      millingMachineConstraintsSetBitSize(millingMachineConstraints, 2.0f);
      millingMachineConstraintsSetRespected(millingMachineConstraints, true);
      millingMachineConstraintsToString(millingMachineConstraints);

      //==================================================
      //MillingMachineMeasurements object Instantiation
      //==================================================
     
      String millingMachineMeasurementsBit = "bit";
      float millingMachineMeasurementsBitSize = 1.0f;
      int millingMachineMeasurementsStartTime = 1;
      int millingMachineMeasurementsDuration = 1;
      
      StorageReference millingMachineMeasurements = millingMachineMeasurementsConstructor(
    		  millingMachineMeasurementsBit,
    		  millingMachineMeasurementsBitSize, 
    		  millingMachineMeasurementsStartTime, 
    		  millingMachineMeasurementsDuration);
      
      millingMachineMeasurementsSetBit(millingMachineMeasurements, "bit2");
      millingMachineMeasurementsSetBitSize(millingMachineMeasurements, 2.0f);
      millingMachineMeasurementsSetEndTime(millingMachineMeasurements, 10);
      //millingMachineMeasurementsAddChuckVelocity(millingMachineMeasurements, 1.0f);
      millingMachineMeasurementsToString(millingMachineMeasurements);

      //==================================================
      //Machine object Instantiation
      //==================================================
      
      String machineName = "machine1";
      String machineOperator = "operator1";
      StorageReference machine = machineConstructor(machineName, machineOperator, millingMachineMeasurements);

      machineSetMeasurements(machine, millingMachineMeasurements);
      machineToString(machine);
      
      //==================================================
      //Operation object 1 Instantiation
      //==================================================
      
      String operation1Name = "operation1";
      StorageReference operation1 = operationConstructor1(operation1Name, machine, millingMachineConstraints);
      
      operationToString(operation1);
      
      //==================================================
      //Operation object 2 Instantiation
      //==================================================
      
      String operation2Name = "operation2";
      StorageReference operation2 = operationConstructor2(operation2Name);
 
      operationSetConstraints(operation2, millingMachineConstraints);
      operationSetMachine(operation2, machine);
      operationSetStatus(operation2, true);
      operationToString(operation2);
      
      //==================================================
      //Company object Instantiation
      //==================================================
      
      String companyName = "company1";
      String companyManager = "manager1";
      StorageReference company = companyConstructor(companyName, companyManager);
      
      //==================================================
      //Product object 1 Instantiation
      //==================================================
      
      String productName1 = "product1";
      StorageReference product1 = productConstructor(productName1);
      
      productAddOperation(product1, operation1);
      productAddOperation(product1, operation2);
      productSetConstraintRespected(product1, true);
      productSetRecipeCompleted(product1, true);
      productToString(product1);
      productRemoveOperation(product1, operation2);
      productToString(product1);
      
      //==================================================
      //Product object 2 Instantiation
      //==================================================
      
      String productName2 = "product2";
      StorageReference product2 = productConstructor(productName2);
      
      //==================================================
      //Add Products to Company
      //==================================================
      
      companyAddProduct(company, product1);
      companyAddProduct(company, product2);
      companyToString(company);
      companyRemoveProduct(company, product2);
      companyToString(company);
}

  //==================================================
  //Product Constructor&Methods
  //==================================================
  
  //TODO non c'è la possibilità di passare una lista di storage reference (lista di prodotti)
  private StorageReference productConstructor(String productName) throws Exception {
	  /* constructor Product(
	   * String name, 
	   */
	  StorageReference product = node.addConstructorCallTransaction
	    (new ConstructorCallTransactionRequest
	      (signer, // an object that signs with the payer's private key
	      account, // payer
	      nonce, // payer's nonce: relevant since this is not a call to a @View method!
	      chainId, // chain identifier: relevant since this is not a call to a @View method!
	      BigInteger.valueOf(100_000_00), // gas limit: enough for a small object
	      panarea(gasHelper.getSafeGasPrice()), // gas price, in panareas
	      productionLine, // class path for the execution of the transaction
	      CONSTRUCTOR_PRODUCT,
	      new StringValue(productName)
	  ));
	  
	  System.out.println("Product object chain identifier: " + product);
	  System.out.println("\t productName:" + productName);
	  nonce = nonce.add(ONE);
	  return product;
  }
  
  private void productToString(StorageReference product) throws Exception {
	  /* String toString()
	   * Product.toString()
	   * */
	  StorageValue s = node.addInstanceMethodCallTransaction
		        (new InstanceMethodCallTransactionRequest
		         (signer,
		         account,
		         nonce,
		         chainId,
		         BigInteger.valueOf(100_000_00),
		         panarea(gasHelper.getSafeGasPrice()),
		         productionLine,
		         new NonVoidMethodSignature(PRODUCT, "toString", ClassType.STRING),
		         product
		        ));
		      System.out.println("Product.toString() receiver -> " + product);
		      System.out.println("\t " + s);
		      nonce = nonce.add(ONE);
  }
  
  private void productAddOperation(StorageReference product, StorageReference operation) throws Exception {
	  /* void addOpeartion(Operation operation)
	   * Product.addOpeartion(operation)
	   * */
	  StorageValue s = node.addInstanceMethodCallTransaction
		        (new InstanceMethodCallTransactionRequest
		         (signer,
		         account,
		         nonce,
		         chainId,
		         BigInteger.valueOf(100_000_00),
		         panarea(gasHelper.getSafeGasPrice()),
		         productionLine,
		         new VoidMethodSignature(PRODUCT, "addOperation", OPERATION),
		         product,
		         operation
		        ));
		      System.out.println("Product.addOpeartion(operation)  receiver -> " + product);
		      System.out.println("\t operationAdded:" + operation);
		      nonce = nonce.add(ONE);
  }
  
  private void productRemoveOperation(StorageReference product, StorageReference operation) throws Exception {
	  /* boolean removeOperation(Operation operation)
	   * Product.removeOperation(operation)
	   * */
	  StorageValue s = node.addInstanceMethodCallTransaction
		        (new InstanceMethodCallTransactionRequest
		         (signer,
		         account,
		         nonce,
		         chainId,
		         BigInteger.valueOf(100_000_00),
		         panarea(gasHelper.getSafeGasPrice()),
		         productionLine,
		         new NonVoidMethodSignature(PRODUCT, "removeOperation", BasicTypes.BOOLEAN, OPERATION),
		         product,
		         operation
		        ));
		      System.out.println("Product.removeOperation(Operation)  receiver -> " + product);
		      if(s.toString() == "true") {
			      System.out.println("\t operationRemoved:" + operation);
		      }else {
		    	  System.out.println("\t operationNOTRemoved:" + operation);
		      }

		      nonce = nonce.add(ONE);
  }
  
  private void productSetConstraintRespected(StorageReference product, Boolean status) throws Exception {
	  /* void setConstraintRespected(Boolean status)
	   * Product.setConstraintRespected(status)
	   * */
	  StorageValue s = node.addInstanceMethodCallTransaction
		        (new InstanceMethodCallTransactionRequest
		         (signer,
		         account,
		         nonce,
		         chainId,
		         BigInteger.valueOf(100_000_00),
		         panarea(gasHelper.getSafeGasPrice()),
		         productionLine,
		         new VoidMethodSignature(PRODUCT, "setConstraintRespected", BasicTypes.BOOLEAN),
		         product,
		         new BooleanValue(status)
		        ));
		      System.out.println("Product.setConstraintRespected(status) receiver -> " + product);
		      System.out.println("\t constraintRespected:" + status);
		      nonce = nonce.add(ONE);
  }
  
  private void productSetRecipeCompleted(StorageReference product, Boolean status) throws Exception {
	  /* void setRecipeCompleted(Boolean status)
	   * Product.setRecipeCompleted(status)
	   * */
	  StorageValue s = node.addInstanceMethodCallTransaction
		        (new InstanceMethodCallTransactionRequest
		         (signer,
		         account,
		         nonce,
		         chainId,
		         BigInteger.valueOf(100_000_00),
		         panarea(gasHelper.getSafeGasPrice()),
		         productionLine,
		         new VoidMethodSignature(PRODUCT, "setRecipeCompleted", BasicTypes.BOOLEAN),
		         product,
		         new BooleanValue(status)
		        ));
		      System.out.println("Product.setRecipeCompleted(status) receiver -> " + product);
		      System.out.println("\t recipeCompleted:" + status);
		      nonce = nonce.add(ONE);
  }
  
  //==================================================
  //Operation Constructor&Methods
  //==================================================
  
  private StorageReference operationConstructor1(String operationName, StorageReference operationMachine, StorageReference operationConstraints) throws Exception {
      /* constructor Operation(
       * String name, 
       * Machine machine, 
       * Constraints constraints
       */
	  StorageReference operation = node.addConstructorCallTransaction
        (new ConstructorCallTransactionRequest
          (signer,
          account,
          nonce,
          chainId,
          BigInteger.valueOf(100_000_00),
          panarea(gasHelper.getSafeGasPrice()),
          productionLine,
          CONSTRUCTOR_OPERATION1,
          new StringValue(operationName),
          operationMachine,
          operationConstraints
      ));
      System.out.println("Operation object chain identifier: " + operation);
      System.out.println("\t operation1Machine:" + operationMachine);
      System.out.println("\t operation1Constraints:" + operationConstraints);
      nonce = nonce.add(ONE);  
      return operation;
  }


  private StorageReference operationConstructor2(String operationName) throws Exception {
      /* constructor Operation(
       * String name, 
       */
	  StorageReference operation = node.addConstructorCallTransaction
	    (new ConstructorCallTransactionRequest
	      (signer,
	      account,
	      nonce,
	      chainId,
	      BigInteger.valueOf(100_000_00),
	      panarea(gasHelper.getSafeGasPrice()),
	      productionLine,
	      CONSTRUCTOR_OPERATION2,
	      new StringValue(operationName)
	  ));
	  System.out.println("Operation object chain identifier: " + operation);
	  System.out.println("\t operationName:" + operationName);
	  nonce = nonce.add(ONE); 
	  return operation;
  }
  
  private void operationToString(StorageReference operation) throws Exception {
	  /* void toString()
	   * Operation.toString()
	   * */
	  StorageValue s = node.addInstanceMethodCallTransaction
		        (new InstanceMethodCallTransactionRequest
		         (signer,
		         account,
		         nonce,
		         chainId,
		         BigInteger.valueOf(100_000_00),
		         panarea(gasHelper.getSafeGasPrice()),
		         productionLine,
		         new NonVoidMethodSignature(OPERATION, "toString",ClassType.STRING),
		         operation
		        ));
		      System.out.println("Operation.toString() receiver -> " + operation);
		      System.out.println("\t " + s);
		      nonce = nonce.add(ONE);
  }
  
  private void operationSetMachine(StorageReference operation, StorageReference machine) throws Exception {
	  /* void setMachine(Machine machine)
	   * Operation.setMachine(machine)
	   * */
	  StorageValue s = node.addInstanceMethodCallTransaction
		        (new InstanceMethodCallTransactionRequest
		         (signer,
		         account,
		         nonce,
		         chainId,
		         BigInteger.valueOf(100_000_00),
		         panarea(gasHelper.getSafeGasPrice()),
		         productionLine,
		         new VoidMethodSignature(OPERATION, "setMachine", MACHINE),
		         operation,
		         machine
		        ));
		      System.out.println("Operation.setMachine(machine) receiver -> " + operation);
		      System.out.println("\t machine:" + machine);
		      nonce = nonce.add(ONE);
  }
  
  private void operationSetConstraints(StorageReference operation, StorageReference constraints) throws Exception {
	  /* void setConstraints(Constraints constraints)
	   * Operation.setConstraints(constraints)
	   * */
	  StorageValue s = node.addInstanceMethodCallTransaction
		        (new InstanceMethodCallTransactionRequest
		         (signer,
		         account,
		         nonce,
		         chainId,
		         BigInteger.valueOf(100_000_00),
		         panarea(gasHelper.getSafeGasPrice()),
		         productionLine,
		         new VoidMethodSignature(OPERATION, "setConstraints", CONSTRAINTS),
		         operation,
		         constraints
		        ));
		      System.out.println("Operation.setConstraints(constraints) receiver -> " + operation);
		      System.out.println("\t constraints:" + constraints);
		      nonce = nonce.add(ONE);
  }
  
  private void operationSetStatus(StorageReference operation, Boolean status) throws Exception {
	  /* void setStatus(boolean status)
	   * Operation.setStatus(status)
	   * */
	  StorageValue s = node.addInstanceMethodCallTransaction
		        (new InstanceMethodCallTransactionRequest
		         (signer,
		         account,
		         nonce,
		         chainId,
		         BigInteger.valueOf(100_000_00),
		         panarea(gasHelper.getSafeGasPrice()),
		         productionLine,
		         new VoidMethodSignature(OPERATION, "setStatus", BasicTypes.BOOLEAN),
		         operation,
		         new BooleanValue(status)
		        ));
		      System.out.println("Operation.setStatus(status) receiver -> " + operation);
		      System.out.println("\t status:" + status);
		      nonce = nonce.add(ONE);
  }
  
  //==================================================
  //MillingMachineConstraints Constructor&Methods
  //==================================================
  
  private StorageReference millingMachineConstraintsConstructor(
		  String millingMachineConstraintsBit,
		  float millingMachineConstraintsBitSize,
		  float millingMachineConstraintsChuckVelocityMax,
		  float millingMachineConstraintsChuckVelocityMin,
		  int millingMachineConstraintsDuration,
		  int millingMachineConstraintsFrequency
		  ) throws Exception {
      /* constructor MillingMachineConstraints(
       * String bit, 
       * float bit_size,
       * float chuck_velocity_max,
       * float chuck_velocity_min, 
       * int duration,
       * int frequency
       */
	  StorageReference millingMachineConstraints = node.addConstructorCallTransaction
        (new ConstructorCallTransactionRequest
          (signer,
          account,
          nonce,
          chainId,
          BigInteger.valueOf(100_000_00),
          panarea(gasHelper.getSafeGasPrice()),
          productionLine,
          CONSTRUCTOR_MILLINGMACHINECONSTRAINTS,
          new StringValue(millingMachineConstraintsBit),
          new FloatValue(millingMachineConstraintsBitSize),
          new FloatValue(millingMachineConstraintsChuckVelocityMax),
          new FloatValue(millingMachineConstraintsChuckVelocityMin),
          new IntValue(millingMachineConstraintsDuration),
          new IntValue(millingMachineConstraintsFrequency)
      ));
      System.out.println("MillingMachineConstraints object chain identifier: " + millingMachineConstraints);
      System.out.println("\t machineConstraintsBit:" + millingMachineConstraintsBit);
      System.out.println("\t machineConstraintsBitSize:" + millingMachineConstraintsBitSize);
      System.out.println("\t machineConstraintsChuckVelocityMax:" + millingMachineConstraintsChuckVelocityMax);
      System.out.println("\t machineConstraintsChuckVelocityMin:" + millingMachineConstraintsChuckVelocityMin);
      System.out.println("\t machineConstraintsDuration:" + millingMachineConstraintsDuration);
      System.out.println("\t machineConstraintsFrequency:" + millingMachineConstraintsFrequency);
      nonce = nonce.add(ONE);      
      return millingMachineConstraints;
  }
  
  private void millingMachineConstraintsToString(StorageReference millingMachineConstraints) throws Exception {
	  /* void toString()
	   * MillingMachineConstraints.toString()
	   * */
	  StorageValue s = node.addInstanceMethodCallTransaction
		        (new InstanceMethodCallTransactionRequest
		         (signer,
		         account,
		         nonce,
		         chainId,
		         BigInteger.valueOf(100_000_00),
		         panarea(gasHelper.getSafeGasPrice()),
		         productionLine,
		         new NonVoidMethodSignature(MILLINGMACHINECONSTRAINTS, "toString",ClassType.STRING),
		         millingMachineConstraints
		        ));
		      System.out.println("MillingMachineConstraints.toString() receiver -> " + millingMachineConstraints);
		      System.out.println("\t " + s);
		      nonce = nonce.add(ONE);
  }
  
  private void millingMachineConstraintsSetBit(StorageReference millingMachineConstraints, String bit) throws Exception {
	  /* void setBit(String bit)
	   * MillingMachineConstraints.setBit(bit)
	   * */
	  StorageValue s = node.addInstanceMethodCallTransaction
		        (new InstanceMethodCallTransactionRequest
		         (signer,
		         account,
		         nonce,
		         chainId,
		         BigInteger.valueOf(100_000_00),
		         panarea(gasHelper.getSafeGasPrice()),
		         productionLine,
		         new VoidMethodSignature(MILLINGMACHINECONSTRAINTS, "setBit", ClassType.STRING),
		         millingMachineConstraints,
		         new StringValue(bit)
		        ));
		      System.out.println("MillingMachineConstraints.setBit(bit) receiver -> " + millingMachineConstraints);
		      System.out.println("\t bit:" + bit);
		      nonce = nonce.add(ONE);
  }
  
  private void millingMachineConstraintsSetBitSize(StorageReference millingMachineConstraints, float bitSize) throws Exception {
	  /* void setBitSize(float bitSize)
	   * MillingMachineConstraints.setBitSize(bitSize)
	   * */
	  StorageValue s = node.addInstanceMethodCallTransaction
		        (new InstanceMethodCallTransactionRequest
		         (signer,
		         account,
		         nonce,
		         chainId,
		         BigInteger.valueOf(100_000_00),
		         panarea(gasHelper.getSafeGasPrice()),
		         productionLine,
		         new VoidMethodSignature(MILLINGMACHINECONSTRAINTS, "setBitSize", BasicTypes.FLOAT),
		         millingMachineConstraints,
		         new FloatValue(bitSize)
		        ));
		      System.out.println("MillingMachineConstraints.setBitSize(bitSize) receiver -> " + millingMachineConstraints);
		      System.out.println("\t bitSize:" + bitSize);
		      nonce = nonce.add(ONE);
  }
  
  private void millingMachineConstraintsSetRespected(StorageReference millingMachineConstraints, boolean respected) throws Exception {
	  /* void setRespected(boolean respected)
	   * MillingMachineConstraints.setRespected(respected)
	   * */
	  StorageValue s = node.addInstanceMethodCallTransaction
		        (new InstanceMethodCallTransactionRequest
		         (signer,
		         account,
		         nonce,
		         chainId,
		         BigInteger.valueOf(100_000_00),
		         panarea(gasHelper.getSafeGasPrice()),
		         productionLine,
		         new VoidMethodSignature(MILLINGMACHINECONSTRAINTS, "setRespected", BasicTypes.BOOLEAN),
		         millingMachineConstraints,
		         new BooleanValue(respected)
		        ));
		      System.out.println("MillingMachineConstraints.setRespected(respected) receiver -> " + millingMachineConstraints);
		      System.out.println("\t respected:" + respected);
		      nonce = nonce.add(ONE);
  }
  
  //==================================================
  //Machine Constructor&Methods
  //==================================================
  
  private StorageReference machineConstructor(String machineName, String machineOperator, StorageReference machineMeasurements) throws Exception {
      /* constructor Machine(
       * String name, 
       * String operator,
       * Measurements measurements 
       */   
      StorageReference machine = node.addConstructorCallTransaction
        (new ConstructorCallTransactionRequest
          (signer,
          account,
          nonce,
          chainId,
          BigInteger.valueOf(100_000_00),
          panarea(gasHelper.getSafeGasPrice()),
          productionLine,
          CONSTRUCTOR_MACHINE,
          new StringValue(machineName),
          new StringValue(machineOperator),
          machineMeasurements
      ));
      System.out.println("Machine object chain identifier: " + machine);
      System.out.println("\t machineName:" + machineName);
      System.out.println("\t machineOperator:" + machineOperator);
      System.out.println("\t measurements:" + machineMeasurements);
      nonce = nonce.add(ONE);  
      return machine;
  }
  
  
  private void machineSetMeasurements(StorageReference machine, StorageReference measurements) throws Exception {
	  /* void setMeasurements(Measurements measurements)
	   * Machine.setMeasurements(measurements)
	   * */
	  StorageValue s = node.addInstanceMethodCallTransaction
		        (new InstanceMethodCallTransactionRequest
		         (signer,
		         account,
		         nonce,
		         chainId,
		         BigInteger.valueOf(100_000_00),
		         panarea(gasHelper.getSafeGasPrice()),
		         productionLine,
		         new VoidMethodSignature(MACHINE, "setMeasurements", MEASUREMENTS),
		         machine,
		         measurements
		        ));
		      System.out.println("Machine.setMeasurements(measurements) receiver -> " + machine);
		      System.out.println("\t measurements:" + measurements);
		      nonce = nonce.add(ONE);
  }
  
  private void machineToString(StorageReference machine) throws Exception {
	  /* void toString()
	   * Machine.toString()
	   * */
	  StorageValue s = node.addInstanceMethodCallTransaction
		        (new InstanceMethodCallTransactionRequest
		         (signer,
		         account,
		         nonce,
		         chainId,
		         BigInteger.valueOf(100_000_00),
		         panarea(gasHelper.getSafeGasPrice()),
		         productionLine,
		         new NonVoidMethodSignature(MACHINE, "toString", ClassType.STRING),
		         machine
		        ));
		      System.out.println("Machine.toString() receiver -> " + machine);
		      System.out.println("\t " + s);
		      nonce = nonce.add(ONE);
  }
  
  //==================================================
  //MillingMachineMeasurements Constructor&Methods
  //==================================================
  
  private StorageReference millingMachineMeasurementsConstructor(String millingMachineMeasurementsBit, float millingMachineMeasurementsBitSize, int millingMachineMeasurementsStartTime, int millingMachineMeasurementsDuration) throws Exception {
      /* constructor MillingMachineMeasurements(
       * String bit, 
       * float bitSize,
       * int startTime,
       * int duration
       */      
      StorageReference millingMachineMeasurements = node.addConstructorCallTransaction
        (new ConstructorCallTransactionRequest
          (signer,
          account,
          nonce,
          chainId,
          BigInteger.valueOf(100_000_00),
          panarea(gasHelper.getSafeGasPrice()),
          productionLine,
          CONSTRUCTOR_MILLINGMACHINEMEASUREMENTS,
          new StringValue(millingMachineMeasurementsBit),
          new FloatValue(millingMachineMeasurementsBitSize),
          new IntValue(millingMachineMeasurementsStartTime),
          new IntValue(millingMachineMeasurementsDuration)
      ));
      System.out.println("MillingMachineMeasurements object chain identifier: " + millingMachineMeasurements);
      System.out.println("\t millingMachineMeasurementsBit:" + millingMachineMeasurementsBit);
      System.out.println("\t millingMachineMeasurementsBitSize:" + millingMachineMeasurementsBitSize);
      System.out.println("\t millingMachineMeasurementsStartTime:" + millingMachineMeasurementsStartTime);
      System.out.println("\t millingMachineMeasurementsDuration:" + millingMachineMeasurementsDuration);
      nonce = nonce.add(ONE);      
      return millingMachineMeasurements;
  }
  
  private void millingMachineMeasurementsToString(StorageReference millingMachineMeasurements) throws Exception {
	  /* void toString()
	   * MillingMachineMeasurements.toString()
	   * */
	  StorageValue s = node.addInstanceMethodCallTransaction
		        (new InstanceMethodCallTransactionRequest
		         (signer,
		         account,
		         nonce,
		         chainId,
		         BigInteger.valueOf(100_000_00),
		         panarea(gasHelper.getSafeGasPrice()),
		         productionLine,
		         new NonVoidMethodSignature(MILLINGMACHINEMEASUREMENTS, "toString", ClassType.STRING),
		         millingMachineMeasurements
		        ));
		      System.out.println("MillingMachineMeasurements.toString() receiver -> " + millingMachineMeasurements);
		      System.out.println("\t " + s);
		      nonce = nonce.add(ONE);

  }
  
  private void millingMachineMeasurementsSetBit(StorageReference millingMachineMeasurements, String bit) throws Exception {
	  /* void setBit(String bit)
	   * MillingMachineMeasurements.setBit(bit)
	   * */
	  StorageValue s = node.addInstanceMethodCallTransaction
		        (new InstanceMethodCallTransactionRequest
		         (signer,
		         account,
		         nonce,
		         chainId,
		         BigInteger.valueOf(100_000_00),
		         panarea(gasHelper.getSafeGasPrice()),
		         productionLine,
		         new VoidMethodSignature(MILLINGMACHINEMEASUREMENTS, "setBit", ClassType.STRING),
		         millingMachineMeasurements,
		         new StringValue(bit)
		        ));
		      System.out.println("MillingMachineMeasurements.setBit(bit) receiver -> " + millingMachineMeasurements);
		      System.out.println("\t bit:" + bit);
		      nonce = nonce.add(ONE);
  }
  
  private void millingMachineMeasurementsSetBitSize(StorageReference millingMachineMeasurements, float bitSize) throws Exception {
	  /* void setBitSize(float bitSize)
	   * MillingMachineMeasurements.setBitSize(bitSize)
	   * */
	  StorageValue s = node.addInstanceMethodCallTransaction
		        (new InstanceMethodCallTransactionRequest
		         (signer,
		         account,
		         nonce,
		         chainId,
		         BigInteger.valueOf(100_000_00),
		         panarea(gasHelper.getSafeGasPrice()),
		         productionLine,
		         new VoidMethodSignature(MILLINGMACHINEMEASUREMENTS, "setBitSize", BasicTypes.FLOAT),
		         millingMachineMeasurements,
		         new FloatValue(bitSize)
		        ));
		      System.out.println("MillingMachineMeasurements.setBitSize(bitSize) receiver -> " + millingMachineMeasurements);
		      System.out.println("\t bitSize:" + bitSize);
		      nonce = nonce.add(ONE);
  }
  
  private void millingMachineMeasurementsSetEndTime(StorageReference millingMachineMeasurements, int endTime) throws Exception {
	  /* void setEndTime(int endTime)
	   * MillingMachineMeasurements.setEndTime(endTime)
	   * */
	  StorageValue s = node.addInstanceMethodCallTransaction
		        (new InstanceMethodCallTransactionRequest
		         (signer,
		         account,
		         nonce,
		         chainId,
		         BigInteger.valueOf(100_000_00),
		         panarea(gasHelper.getSafeGasPrice()),
		         productionLine,
		         new VoidMethodSignature(MILLINGMACHINEMEASUREMENTS, "setEndTime", BasicTypes.INT),
		         millingMachineMeasurements,
		         new IntValue(endTime)
		        ));
		      System.out.println("MillingMachineMeasurements.setEndTime(endTime) receiver -> " + millingMachineMeasurements);
		      System.out.println("\t endTime:" + endTime);
		      nonce = nonce.add(ONE);
  }
  
  //TODO StorageList non può contenere valori Float
  private void millingMachineMeasurementsAddChuckVelocity(StorageReference millingMachineMeasurements, float chuckVelocity) throws Exception {
	  /* void addChuckVelocity(float chuckVelocity)
	   * MillingMachineMeasurements.addChuckVelocity(chuckVelocity)
	   * */
	  StorageValue s = node.addInstanceMethodCallTransaction
		        (new InstanceMethodCallTransactionRequest
		         (signer,
		         account,
		         nonce,
		         chainId,
		         BigInteger.valueOf(100_000_00),
		         panarea(gasHelper.getSafeGasPrice()),
		         productionLine,
		         new VoidMethodSignature(MILLINGMACHINEMEASUREMENTS, "addChuckVelocity", BasicTypes.FLOAT),
		         millingMachineMeasurements,
		         new FloatValue(chuckVelocity)
		        ));
		      System.out.println("MillingMachineMeasurements.addChuckVelocity(chuckVelocity) receiver -> " + millingMachineMeasurements);
		      System.out.println("\t Added chuckVelocity:" + chuckVelocity);
		      nonce = nonce.add(ONE);
  }
  
  //==================================================
  //Company Constructor&Methods
  //==================================================

  private StorageReference companyConstructor(String companyName, String companyManager) throws Exception {
  		StorageReference company = node.addConstructorCallTransaction
        (new ConstructorCallTransactionRequest
          (signer,
          account,
          nonce,
          chainId,
          BigInteger.valueOf(100_000_00),
          panarea(gasHelper.getSafeGasPrice()),
          productionLine,
          CONSTRUCTOR_COMPANY,
          new StringValue(companyName), new StringValue(companyManager)
      ));
      System.out.println("Company object chain identifier: " + company);
      System.out.println("\t companyName:" + companyName);
      System.out.println("\t companyManager:" + companyManager);
      nonce = nonce.add(ONE);  
      return company;	  
  }
  
  private void companyAddProduct(StorageReference company, StorageReference product) throws Exception {
	  /* void addProduct(Product product)
	   * Company.addProduct(product)
	   * */
	  StorageValue s = node.addInstanceMethodCallTransaction
		        (new InstanceMethodCallTransactionRequest
		         (signer,
		         account,
		         nonce,
		         chainId,
		         BigInteger.valueOf(100_000_00),
		         panarea(gasHelper.getSafeGasPrice()),
		         productionLine,
		         new VoidMethodSignature(COMPANY, "addProduct", PRODUCT),
		         company,
		         product
		        ));
		      System.out.println("Company.addProduct(Product) receiver -> " + company);
		      System.out.println("\t productAdded:" + product);
		      nonce = nonce.add(ONE);
  }
  
  private void companyRemoveProduct(StorageReference company, StorageReference product) throws Exception {
	  /* boolean removeProduct(Product product)
	   * Company.removeProduct(product)
	   * */
	  StorageValue s = node.addInstanceMethodCallTransaction
		        (new InstanceMethodCallTransactionRequest
		         (signer,
		         account,
		         nonce,
		         chainId,
		         BigInteger.valueOf(100_000_00),
		         panarea(gasHelper.getSafeGasPrice()),
		         productionLine,
		         new NonVoidMethodSignature(COMPANY, "removeProduct", BasicTypes.BOOLEAN, PRODUCT),
		         company,
		         product
		        ));
		      System.out.println("Company.removeProduct(Product) receiver -> " + company);
		      if(s.toString() == "true") {
			      System.out.println("\t productRemoved:" + product);
		      }else {
		    	  System.out.println("\t productNOTRemoved:" + product);
		      }

		      nonce = nonce.add(ONE);
  }
  
  private void companyToString(StorageReference company) throws Exception {
	  /* String toString()
	   * Company.toString()
	   * */
	  StorageValue s = node.addInstanceMethodCallTransaction
		        (new InstanceMethodCallTransactionRequest
		         (signer,
		         account,
		         nonce,
		         chainId,
		         BigInteger.valueOf(100_000_00),
		         panarea(gasHelper.getSafeGasPrice()),
		         productionLine,
		         new NonVoidMethodSignature(COMPANY, "toString", ClassType.STRING),
		         company
		        ));
		      System.out.println("Company.toString() receiver -> " + company);
		      System.out.println("\t " + s);
		      nonce = nonce.add(ONE);
  }
  
  private String getChainId() throws Exception {
	    return ((StringValue) node.runInstanceMethodCallTransaction
	      (new InstanceMethodCallTransactionRequest
	      (account, // payer
	      BigInteger.valueOf(100_000_00), // gas limit
	      takamakaCode, // class path for the execution of the transaction
	      CodeSignature.GET_CHAIN_ID, // method
	      node.getManifest()))) // receiver of the method call
	      .value;
	  }

	private TransactionReference installJar() throws Exception {
	    System.out.println("Installing jar");

	    return node.addJarStoreTransaction(new JarStoreTransactionRequest
	      (signer, // an object that signs with the payer's private key
	      account, // payer
	      nonce, // payer's nonce
	      chainId, // chain identifier
	      BigInteger.valueOf(100_000_00), // gas limit: enough for this very small jar
	      gasHelper.getSafeGasPrice(), // gas price: at least the current gas price of the network
	      takamakaCode, // class path for the execution of the transaction
	      Files.readAllBytes(productionLinePath), // bytes of the jar to install
	      takamakaCode)); // dependency
	  }
  
  private static KeyPair loadKeys(Node node, StorageReference account) throws Exception {
    return new Account(account, "..").keys
      (PASSWORD, new SignatureHelper(node).signatureAlgorithmFor(account));
  }
}