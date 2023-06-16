package runs;

import static io.hotmoka.beans.Coin.panarea;
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
	
	//change this with your account's storage reference and respective password
	private final static String ADDRESS = "88490f9a03cac663467794ef96ba9098c687debb1816a477cf98d6b08b07059b#0";
	private final static String PASSWORD = "mario";
	
	// ==================================================
	// Classes definition
	// ==================================================
	private final static ClassType COMPANY = new ClassType("io.takamaka.productionline.Company");
	private final static ClassType RECIPE = new ClassType("io.takamaka.productionline.Recipe");
	private final static ClassType PRODUCT = new ClassType("io.takamaka.productionline.Product");
	private final static ClassType OPERATIONTODO = new ClassType("io.takamaka.productionline.OperationToDo");
	private final static ClassType OPERATIONDONE = new ClassType("io.takamaka.productionline.OperationDone");
	private final static ClassType CONSTRAINTS = new ClassType("io.takamaka.productionline.Constraints");
	private final static ClassType PRINTERCONSTRAINTS = new ClassType("io.takamaka.productionline.PrinterConstraints");
	private final static ClassType MILLINGMACHINECONSTRAINTS = new ClassType("io.takamaka.productionline.MillingMachineConstraints");
	private final static ClassType MACHINE = new ClassType("io.takamaka.productionline.Machine");
	private final static ClassType MEASUREMENTS = new ClassType("io.takamaka.productionline.Measurements");
	private final static ClassType MILLINGMACHINEMEASUREMENTS = new ClassType("io.takamaka.productionline.MillingMachineMeasurements");
	private final static ClassType PRINTERMEASUREMENTS = new ClassType("io.takamaka.productionline.PrinterMeasurements");

	// ==================================================
	// Constructors definition for each class
	// ==================================================
	private final static ConstructorSignature CONSTRUCTOR_COMPANY = new ConstructorSignature(COMPANY, ClassType.STRING, ClassType.STRING);
	private final static ConstructorSignature CONSTRUCTOR_RECIPE = new ConstructorSignature(RECIPE, ClassType.STRING);
	private final static ConstructorSignature CONSTRUCTOR_PRODUCT = new ConstructorSignature(PRODUCT, ClassType.STRING);
	private final static ConstructorSignature CONSTRUCTOR_OPERATIONTODO1 = new ConstructorSignature(OPERATIONTODO, ClassType.STRING, MACHINE, CONSTRAINTS);
	private final static ConstructorSignature CONSTRUCTOR_OPERATIONTODO2 = new ConstructorSignature(OPERATIONTODO, ClassType.STRING);
	private final static ConstructorSignature CONSTRUCTOR_OPERATIONDONE1 = new ConstructorSignature(OPERATIONDONE, ClassType.STRING, MACHINE, MEASUREMENTS);
	private final static ConstructorSignature CONSTRUCTOR_OPERATIONDONE2 = new ConstructorSignature(OPERATIONDONE, ClassType.STRING, MACHINE);
	private final static ConstructorSignature CONSTRUCTOR_PRINTERCONSTRAINTS = new ConstructorSignature(PRINTERCONSTRAINTS, ClassType.STRING, ClassType.STRING, BasicTypes.FLOAT, BasicTypes.FLOAT, BasicTypes.INT,	BasicTypes.INT);
	private final static ConstructorSignature CONSTRUCTOR_MILLINGMACHINECONSTRAINTS = new ConstructorSignature(MILLINGMACHINECONSTRAINTS, ClassType.STRING, BasicTypes.FLOAT, BasicTypes.FLOAT, BasicTypes.FLOAT, BasicTypes.INT, BasicTypes.INT);
	private final static ConstructorSignature CONSTRUCTOR_MACHINE = new ConstructorSignature(MACHINE, ClassType.STRING);
	private final static ConstructorSignature CONSTRUCTOR_MILLINGMACHINEMEASUREMENTS = new ConstructorSignature(MILLINGMACHINEMEASUREMENTS, ClassType.STRING, BasicTypes.FLOAT, BasicTypes.FLOAT, BasicTypes.FLOAT,	BasicTypes.INT, BasicTypes.INT, BasicTypes.INT);
	private final static ConstructorSignature CONSTRUCTOR_PRINTERMEASUREMENTS = new ConstructorSignature(PRINTERMEASUREMENTS, ClassType.STRING, ClassType.STRING, BasicTypes.FLOAT, BasicTypes.FLOAT, BasicTypes.INT, BasicTypes.INT, BasicTypes.INT);

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
		RemoteNodeConfig config = new RemoteNodeConfig.Builder().setURL("54.194.239.91").build();
		try (Node node = RemoteNode.of(config)) {
			new ProductionLine(node);
		}
	}

	private ProductionLine(Node node) throws Exception {
		this.node = node;
		takamakaCode = node.getTakamakaCode();
		account = new StorageReference(ADDRESS);
		// we get the signing algorithm to use for requests
		SignatureAlgorithm<SignedTransactionRequest> signature = SignatureAlgorithmForTransactionRequests
				.mk(node.getNameOfSignatureAlgorithmForRequests());
		keys = loadKeys(node, account);
		// we create a signer that signs with the private key of our account
		signer = Signer.with(signature, keys.getPrivate());
		gasHelper = new GasHelper(node);
		// we get the nonce of our account: we use the account itself as caller and
		// an arbitrary nonce (ZERO in the code) since we are running
		// a @View method of the account
		nonce = ((BigIntegerValue) node
				.runInstanceMethodCallTransaction(new InstanceMethodCallTransactionRequest(account, // payer
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

		// ==================================================
		// Company1 object Instantiation
		// ==================================================
		System.out.println("Company instantiation...");
		String company1_name = "company1";
		String company1_manager = "manager1";
		StorageReference company1 = companyConstructor(company1_name, company1_manager);

		// ==================================================
		// Recipe1 and Recipe2 object Instantiation
		// ==================================================
		System.out.println("Recipes instantiation...");
		String recipe1_name = "recipe1";
		StorageReference recipe1 = recipeConstructor(recipe1_name);
		companyAddRecipe(company1, recipe1);
		
		String recipe2_name = "recipe2";
		StorageReference recipe2 = recipeConstructor(recipe2_name);
		companyAddRecipe(company1, recipe2);

		// ==================================================
		// OperationToDo (Constraints, Machine, Measurements) 
		// object Instantiation, for both recipes
		// ==================================================
		System.out.println("OperationToDo Recipe instantiation...");
		String recipe1_operationToDo1_name = "Print";
		String recipe1_operationToDo2_name = "Engrave";
		String recipe1_operationToDo3_name = "Drill";
		
		StorageReference recipe1_operationToDo1_constraints = printerConstraintsConstructor("PLA", "Black", 200.0f, 40.0f, 120, 700); // FilamentType,FilamentColour, plateTemperatureMax, plateTemperatureMin, duration ,frequency
		StorageReference recipe1_operationToDo2_constraints = millingMachineConstraintsConstructor("bit1", 150.0f, 3000.0f, 200.0f, 120, 700); // Bit,BitSize,ChuckVelocityMax,ChuckVelocityMin,Duration,Frequency 
		StorageReference recipe1_operationToDo3_constraints = millingMachineConstraintsConstructor("bit2", 150.0f, 3000.0f, 200.0f, 120, 700); // Bit,BitSize,ChuckVelocityMax,ChuckVelocityMin,Duration,Frequency
		
		StorageReference millingMachine_machine = machineConstructor("MillingMachine");
		StorageReference printer_machine = machineConstructor("Printer");

		StorageReference recipe1_operationToDo1 = operationToDoConstructor1(recipe1_operationToDo1_name, printer_machine, recipe1_operationToDo1_constraints);
		StorageReference recipe1_operationToDo2 = operationToDoConstructor1(recipe1_operationToDo2_name, millingMachine_machine, recipe1_operationToDo2_constraints);
		StorageReference recipe1_operationToDo3 = operationToDoConstructor1(recipe1_operationToDo3_name, millingMachine_machine, recipe1_operationToDo3_constraints);

		recipeAddOperationToDo(recipe1, recipe1_operationToDo1);
		recipeAddOperationToDo(recipe1, recipe1_operationToDo2);
		recipeAddOperationToDo(recipe1, recipe1_operationToDo3);
		
		recipeAddOperationToDo(recipe2, recipe1_operationToDo1);
		recipeAddOperationToDo(recipe2, recipe1_operationToDo2);
		recipeAddOperationToDo(recipe2, recipe1_operationToDo3);

		// ==================================================
		// Products 1-6 objects Instantiation, giving a name
		// ==================================================
		
		String product_id1 = "product1_respected"; // recipe respected and constraints respected
		String product_id2 = "product2_wrong_order"; // recipe NOT respected, wrong order
		String product_id3 = "product3_missing_op"; // recipe NOT respected, missing one operation
		String product_id4 = "product4_extra_op"; // recipe NOT respected, extra operation not required
		String product_id5 = "product5_wrong_op"; // recipe NOT respected, wrong operation used
		String product_id6 = "product6_!constr_respected"; //recipe respected, but constraints not respected
		
		StorageReference product1 = productConstructor(product_id1);
		StorageReference product2 = productConstructor(product_id2);
		StorageReference product3 = productConstructor(product_id3);
		StorageReference product4 = productConstructor(product_id4);
		StorageReference product5 = productConstructor(product_id5);
		StorageReference product6 = productConstructor(product_id6);

		// ==================================================
		// Product1 OperationDone objects Instantiation
		// recipe respected and constraints respected
		// ==================================================
		System.out.println("Product1 instantiation...");
		String product1_operationDone1_name = "Print";
		String product1_operationDone2_name = "Engrave";
		String product1_operationDone3_name = "Drill";
		
		//Measurements obtained from each operationDone
		StorageReference product1_operationDone1_measurements = printerMeasurementsConstructor("PLA", "Black", 200.0f, 40.0f, 700, 20, 140); // FilamentType,FilamentColour, plateTemperatureMax, plateTemperatureMin, duration ,frequency
		StorageReference product1_operationDone2_measurements = millingMachineMeasurementsConstructor("bit1", 150.0f, 3000.0f, 200.0f, 700, 20, 140); // Bit,BitSize,ChuckVelocityMax,ChuckVelocityMin,Frequency, StartTime, EndTime
		StorageReference product1_operationDone3_measurements = millingMachineMeasurementsConstructor("bit2", 150.0f, 3000.0f, 200.0f, 700, 20, 140); // Bit,BitSize,ChuckVelocityMax,ChuckVelocityMin,Frequency, StartTime, EndTime
		
		//Operation performed on the product
		StorageReference product1_operationDone1 = operationDoneConstructor1(product1_operationDone1_name, printer_machine, product1_operationDone1_measurements);
		StorageReference product1_operationDone2 = operationDoneConstructor1(product1_operationDone2_name, millingMachine_machine, product1_operationDone2_measurements);
		StorageReference product1_operationDone3 = operationDoneConstructor1(product1_operationDone3_name, millingMachine_machine, product1_operationDone3_measurements);
		
		productAddOperationDone(product1, product1_operationDone1);
		productAddOperationDone(product1, product1_operationDone2);
		productAddOperationDone(product1, product1_operationDone3);

		// ==================================================
		// Product2 OperationDone objects Instantiation
		// recipe NOT respected, wrong order
		// ==================================================
		System.out.println("Product2 instantiation...");
		String product2_operationDone1_name = "Engrave";
		String product2_operationDone2_name = "Print";
		String product2_operationDone3_name = "Drill";

		StorageReference product2_operationDone1_measurements = millingMachineMeasurementsConstructor("bit1", 150.0f, 3000.0f, 200.0f, 700, 20, 140); // Bit,BitSize,ChuckVelocityMax,ChuckVelocityMin,Frequency, StartTime, EndTime
		StorageReference product2_operationDone2_measurements = printerMeasurementsConstructor("PLA", "Black", 200.0f, 40.0f, 700, 20, 140); // FilamentType,FilamentColour, plateTemperatureMax, plateTemperatureMin, duration ,frequency
		StorageReference product2_operationDone3_measurements = millingMachineMeasurementsConstructor("bit2", 150.0f, 3000.0f, 200.0f, 700, 20, 140); // Bit,BitSize,ChuckVelocityMax,ChuckVelocityMin,Frequency, StartTime, EndTime

		StorageReference product2_operationDone1 = operationDoneConstructor1(product2_operationDone1_name, millingMachine_machine, product2_operationDone1_measurements);
		StorageReference product2_operationDone2 = operationDoneConstructor1(product2_operationDone2_name, printer_machine, product2_operationDone2_measurements);
		StorageReference product2_operationDone3 = operationDoneConstructor1(product2_operationDone3_name, millingMachine_machine, product2_operationDone3_measurements);

		productAddOperationDone(product2, product2_operationDone1);
		productAddOperationDone(product2, product2_operationDone2);
		productAddOperationDone(product2, product2_operationDone3);

		// ==================================================
		// Product3 OperationDone objects Instantiation
		// recipe NOT respected, missing one operation
		// ==================================================
		System.out.println("Product3 instantiation...");
		String product3_operationDone1_name = "Print";
		String product3_operationDone2_name = "Engrave";

		StorageReference product3_operationDone1_measurements = printerMeasurementsConstructor("PLA", "Black", 200.0f, 40.0f, 700, 20, 140); // FilamentType,FilamentColour, plateTemperatureMax, plateTemperatureMin, duration ,frequency
		StorageReference product3_operationDone2_measurements = millingMachineMeasurementsConstructor("bit1", 150.0f, 3000.0f, 200.0f, 700, 20, 140); // Bit,BitSize,ChuckVelocityMax,ChuckVelocityMin,Frequency, StartTime, EndTime
		
		StorageReference product3_operationDone1 = operationDoneConstructor1(product3_operationDone1_name, printer_machine, product3_operationDone1_measurements);
		StorageReference product3_operationDone2 = operationDoneConstructor1(product3_operationDone2_name, millingMachine_machine, product3_operationDone2_measurements);
	
		productAddOperationDone(product3, product3_operationDone1);
		productAddOperationDone(product3, product3_operationDone2);
		
		// ==================================================
		// Product4 OperationDone objects Instantiation
		// recipe NOT respected, extra operation not required
		// ==================================================
		System.out.println("Product4 instantiation...");
		String product4_operationDone1_name = "Print";
		String product4_operationDone2_name = "Engrave";
		String product4_operationDone3_name = "Drill";
		String product4_operationDone4_name = "Drill";

		StorageReference product4_operationDone1_measurements = printerMeasurementsConstructor("PLA", "Black", 200.0f, 40.0f, 700, 20, 140); // FilamentType,FilamentColour, plateTemperatureMax, plateTemperatureMin, duration ,frequency
		StorageReference product4_operationDone2_measurements = millingMachineMeasurementsConstructor("bit1", 150.0f, 3000.0f, 200.0f, 700, 20, 140); // Bit,BitSize,ChuckVelocityMax,ChuckVelocityMin,Frequency, StartTime, EndTime
		StorageReference product4_operationDone3_measurements = millingMachineMeasurementsConstructor("bit2", 150.0f, 3000.0f, 200.0f, 700, 20, 140); // Bit,BitSize,ChuckVelocityMax,ChuckVelocityMin,Frequency, StartTime, EndTime
		StorageReference product4_operationDone4_measurements = millingMachineMeasurementsConstructor("bit2", 150.0f, 3000.0f, 200.0f, 700, 20, 140); // Bit,BitSize,ChuckVelocityMax,ChuckVelocityMin,Frequency, StartTime, EndTime

		StorageReference product4_operationDone1 = operationDoneConstructor1(product4_operationDone1_name, printer_machine, product4_operationDone1_measurements);
		StorageReference product4_operationDone2 = operationDoneConstructor1(product4_operationDone2_name, millingMachine_machine, product4_operationDone2_measurements);
		StorageReference product4_operationDone3 = operationDoneConstructor1(product4_operationDone3_name, millingMachine_machine, product4_operationDone3_measurements);
		StorageReference product4_operationDone4 = operationDoneConstructor1(product4_operationDone4_name, millingMachine_machine, product4_operationDone4_measurements);

		productAddOperationDone(product4, product4_operationDone1);
		productAddOperationDone(product4, product4_operationDone2);
		productAddOperationDone(product4, product4_operationDone3);
		productAddOperationDone(product4, product4_operationDone4);
		
		// ==================================================
		// Product OperationDone objects Instantiation
		// recipe NOT respected, wrong operation used
		// ==================================================
		System.out.println("Product5 instantiation...");
		String product5_operationDone1_name = "Print";
		String product5_operationDone2_name = "Mill";
		String product5_operationDone3_name = "Drill";

		StorageReference product5_operationDone1_measurements = printerMeasurementsConstructor("PLA", "Black", 200.0f, 40.0f, 700, 20, 140); // FilamentType,FilamentColour, plateTemperatureMax, plateTemperatureMin, duration ,frequency
		StorageReference product5_operationDone2_measurements = millingMachineMeasurementsConstructor("bit1", 150.0f, 3000.0f, 200.0f, 700, 20, 140); // Bit,BitSize,ChuckVelocityMax,ChuckVelocityMin,Frequency, StartTime, EndTime
		StorageReference product5_operationDone3_measurements = millingMachineMeasurementsConstructor("bit2", 150.0f, 3000.0f, 200.0f, 700, 20, 140); // Bit,BitSize,ChuckVelocityMax,ChuckVelocityMin,Frequency, StartTime, EndTime

		StorageReference product5_operationDone1 = operationDoneConstructor1(product5_operationDone1_name, printer_machine, product5_operationDone1_measurements);
		StorageReference product5_operationDone2 = operationDoneConstructor1(product5_operationDone2_name, millingMachine_machine, product5_operationDone2_measurements);
		StorageReference product5_operationDone3 = operationDoneConstructor1(product5_operationDone3_name, millingMachine_machine, product5_operationDone3_measurements);

		productAddOperationDone(product5, product5_operationDone1);
		productAddOperationDone(product5, product5_operationDone2);
		productAddOperationDone(product5, product5_operationDone3);
		
		// ==================================================
		// Product OperationDone objects Instantiation
		//recipe respected, but constraints not respected
		// ==================================================
		System.out.println("Product6 instantiation...");
		String product6_operationDone1_name = "Print";
		String product6_operationDone2_name = "Engrave";
		String product6_operationDone3_name = "Drill";

		StorageReference product6_operationDone1_measurements = printerMeasurementsConstructor("wrong_material", "Black", 200.0f, 40.0f, 700, 20, 140); // FilamentType,FilamentColour, plateTemperatureMax, plateTemperatureMin, duration ,frequency
		StorageReference product6_operationDone2_measurements = millingMachineMeasurementsConstructor("wrong_bit", 150.0f, 3000.0f, 200.0f, 700, 20, 140); // Bit,BitSize,ChuckVelocityMax,ChuckVelocityMin,Frequency, StartTime, EndTime
		StorageReference product6_operationDone3_measurements = millingMachineMeasurementsConstructor("bit2", 150.0f, 3000.0f, 200.0f, 700, 20, 140); // Bit,BitSize,ChuckVelocityMax,ChuckVelocityMin,Frequency, StartTime, EndTime

		StorageReference product6_operationDone1 = operationDoneConstructor1(product6_operationDone1_name, printer_machine, product6_operationDone1_measurements);
		StorageReference product6_operationDone2 = operationDoneConstructor1(product6_operationDone2_name, millingMachine_machine, product6_operationDone2_measurements);
		StorageReference product6_operationDone3 = operationDoneConstructor1(product6_operationDone3_name, millingMachine_machine, product6_operationDone3_measurements);

		productAddOperationDone(product6, product6_operationDone1);
		productAddOperationDone(product6, product6_operationDone2);
		productAddOperationDone(product6, product6_operationDone3);
		
		// ==================================================
		// Bind each product to the recipe
		// ==================================================
		recipeAddProductProduced(recipe1, product1);
		recipeAddProductProduced(recipe1, product2);
		recipeAddProductProduced(recipe1, product3);
		recipeAddProductProduced(recipe2, product4);
		recipeAddProductProduced(recipe2, product5);
		recipeAddProductProduced(recipe2, product6);
		
		//StorageReference recipe1 = new StorageReference("62457378d9e8c9758701a65aec9df1f7b4d8cde70adac655db9bd24cb65501f1#0");

		// ==================================================
		// Check if the recipe is respected, and its respective constraints
		// ==================================================
		System.out.println("Checking constraints for recipe1...");
		System.out.println(recipeCheckConstraints(recipe1));
		System.out.println("Checking constraints for recipe2...");
		System.out.println(recipeCheckConstraints(recipe2));
	}

	// ==================================================
	// Company Constructor
	// ==================================================
	private StorageReference companyConstructor(String companyName, String companyManager) throws Exception {
		StorageReference company = node.addConstructorCallTransaction(new ConstructorCallTransactionRequest(signer,
				account, nonce, chainId, BigInteger.valueOf(100_000_00), panarea(gasHelper.getSafeGasPrice()),
				productionLine, CONSTRUCTOR_COMPANY, new StringValue(companyName), new StringValue(companyManager)));
		System.out.println("\tCompany object chain identifier: " + company);
		// System.out.println("\t companyName:" + companyName);
		// System.out.println("\t companyManager:" + companyManager);
		nonce = nonce.add(ONE);
		return company;
	}

	// ==================================================
	// Recipe Constructor
	// ==================================================
	private StorageReference recipeConstructor(String recipeName) throws Exception {
		StorageReference recipe = node.addConstructorCallTransaction(new ConstructorCallTransactionRequest(signer,
				account, nonce, chainId, BigInteger.valueOf(100_000_00), panarea(gasHelper.getSafeGasPrice()),
				productionLine, CONSTRUCTOR_RECIPE, new StringValue(recipeName)));
		System.out.println("\tRecipe object chain identifier: " + recipe);
		// System.out.println("\t recipeName:" + recipeName);
		nonce = nonce.add(ONE);
		return recipe;
	}

	// ==================================================
	// Product Constructor
	// ==================================================
	private StorageReference productConstructor(String productId) throws Exception {
		StorageReference product = node.addConstructorCallTransaction(new ConstructorCallTransactionRequest(signer, // an object that signs with the payer's private key
				account, // payer
				nonce, // payer's nonce: relevant since this is not a call to a @View method!
				chainId, // chain identifier: relevant since this is not a call to a @View method!
				BigInteger.valueOf(100_000_00), // gas limit: enough for a small object
				panarea(gasHelper.getSafeGasPrice()), // gas price, in panareas
				productionLine, // class path for the execution of the transaction
				CONSTRUCTOR_PRODUCT, new StringValue(productId)));
		// System.out.println("Product object chain identifier: " + product);
		// System.out.println("\t productId:" + productId);
		nonce = nonce.add(ONE);
		return product;
	}

	// ==================================================
	// OperationToDo1 Constructor
	// ==================================================
	private StorageReference operationToDoConstructor1(String operationToDoName, StorageReference operationToDoMachine,
			StorageReference operationToDoConstraints) throws Exception {
		StorageReference operationToDo = node.addConstructorCallTransaction(
				new ConstructorCallTransactionRequest(signer, account, nonce, chainId, BigInteger.valueOf(100_000_00),
						panarea(gasHelper.getSafeGasPrice()), productionLine, CONSTRUCTOR_OPERATIONTODO1,
						new StringValue(operationToDoName), operationToDoMachine, operationToDoConstraints));
		// System.out.println("OperationToDo object chain identifier: " + operationToDo);
		// System.out.println("\t operationToDo1Name:" + operationToDoName);
		// System.out.println("\t operationToDo1Machine:" + operationToDoMachine);
		// System.out.println("\t operationToDo1Constraints:" +
		// operationToDoConstraints);
		nonce = nonce.add(ONE);
		return operationToDo;
	}

	// ==================================================
	// OperationToDo2 Constructor
	// ==================================================
	private StorageReference operationToDoConstructor2(String operationToDoName) throws Exception {
		StorageReference operationToDo = node.addConstructorCallTransaction(new ConstructorCallTransactionRequest(
				signer, account, nonce, chainId, BigInteger.valueOf(100_000_00), panarea(gasHelper.getSafeGasPrice()),
				productionLine, CONSTRUCTOR_OPERATIONTODO2, new StringValue(operationToDoName)));
		// System.out.println("OperationToDo object chain identifier: " + operationToDo);
		// System.out.println("\t operationToDo2Name:" + operationToDoName);
		nonce = nonce.add(ONE);
		return operationToDo;
	}

	// ==================================================
	// OperationDone1 Constructor
	// ==================================================
	private StorageReference operationDoneConstructor1(String operationDoneName, StorageReference operationDoneMachine,
			StorageReference operationDoneMeasurements) throws Exception {
		StorageReference operationDone = node.addConstructorCallTransaction(
				new ConstructorCallTransactionRequest(signer, account, nonce, chainId, BigInteger.valueOf(100_000_00),
						panarea(gasHelper.getSafeGasPrice()), productionLine, CONSTRUCTOR_OPERATIONDONE1,
						new StringValue(operationDoneName), operationDoneMachine, operationDoneMeasurements));
		// System.out.println("OperationDone object chain identifier: " + operationDone);
		// System.out.println("\t operationDoneName:" + operationDoneName);
		// System.out.println("\t operationDoneMachine:" + operationDoneMachine);
		// System.out.println("\t operationDoneMeasurements:" +
		// operationDoneMeasurements);
		nonce = nonce.add(ONE);
		return operationDone;

	}

	// ==================================================
	// OperationDone2 Constructor
	// ==================================================
	private StorageReference operationDoneConstructor2(String operationDoneName, StorageReference operationDoneMachine)
			throws Exception {
		StorageReference operationDone = node.addConstructorCallTransaction(new ConstructorCallTransactionRequest(
				signer, account, nonce, chainId, BigInteger.valueOf(100_000_00), panarea(gasHelper.getSafeGasPrice()),
				productionLine, CONSTRUCTOR_OPERATIONDONE2, new StringValue(operationDoneName), operationDoneMachine));
		// System.out.println("OperationDone object chain identifier: " + operationDone);
		// System.out.println("\t operationDoneName:" + operationDoneName);
		// System.out.println("\t operationDoneMachine:" + operationDoneMachine);
		nonce = nonce.add(ONE);
		return operationDone;
	}

	// ==================================================
	// PrinterConstraints Constructor
	// ==================================================
	private StorageReference printerConstraintsConstructor(String printerConstraintsFilamentType,
			String printerConstraintsFilamentColour, float printerConstraintsPlateTemperatureMax,
			float printerConstraintsPlateTemperatureMin, int printerConstraintsDuration,
			int printerConstraintsFrequency) throws Exception {
		StorageReference printerConstraints = node.addConstructorCallTransaction(new ConstructorCallTransactionRequest(
				signer, account, nonce, chainId, BigInteger.valueOf(100_000_00), panarea(gasHelper.getSafeGasPrice()),
				productionLine, CONSTRUCTOR_PRINTERCONSTRAINTS, new StringValue(printerConstraintsFilamentType),
				new StringValue(printerConstraintsFilamentColour),
				new FloatValue(printerConstraintsPlateTemperatureMax),
				new FloatValue(printerConstraintsPlateTemperatureMin), new IntValue(printerConstraintsDuration),
				new IntValue(printerConstraintsFrequency)));
		// System.out.println("PrinterConstraints object chain identifier: " + printerConstraints);
		// System.out.println("\t printerConstraintsFilamentType:" + printerConstraintsFilamentType);
		// System.out.println("\t printerConstraintsFilamentColour:" + printerConstraintsFilamentColour);
		// System.out.println("\t printerConstraintsPlateTemperatureMax:" + printerConstraintsPlateTemperatureMax);
		// System.out.println("\t printerConstraintsPlateTemperatureMin:" + printerConstraintsPlateTemperatureMin);
		// System.out.println("\t printerConstraintsDuration:" + printerConstraintsDuration);
		// System.out.println("\t printerConstraintsFrequency:" + printerConstraintsFrequency);
		nonce = nonce.add(ONE);
		return printerConstraints;
	}

	// ==================================================
	// MillingMachineConstraints Constructor
	// ==================================================
	private StorageReference millingMachineConstraintsConstructor(String millingMachineConstraintsBit,
			float millingMachineConstraintsBitSize, float millingMachineConstraintsChuckVelocityMax,
			float millingMachineConstraintsChuckVelocityMin, int millingMachineConstraintsDuration,
			int millingMachineConstraintsFrequency) throws Exception {
		StorageReference millingMachineConstraints = node.addConstructorCallTransaction(
				new ConstructorCallTransactionRequest(signer, account, nonce, chainId, BigInteger.valueOf(100_000_00),
						panarea(gasHelper.getSafeGasPrice()), productionLine, CONSTRUCTOR_MILLINGMACHINECONSTRAINTS,
						new StringValue(millingMachineConstraintsBit), new FloatValue(millingMachineConstraintsBitSize),
						new FloatValue(millingMachineConstraintsChuckVelocityMax),
						new FloatValue(millingMachineConstraintsChuckVelocityMin),
						new IntValue(millingMachineConstraintsDuration),
						new IntValue(millingMachineConstraintsFrequency)));
		// System.out.println("MillingMachineConstraints object chain identifier: " + millingMachineConstraints);
		// System.out.println("\t machineConstraintsBit:" + millingMachineConstraintsBit);
		// System.out.println("\t machineConstraintsBitSize:" + millingMachineConstraintsBitSize);
		// System.out.println("\t machineConstraintsChuckVelocityMax:" + millingMachineConstraintsChuckVelocityMax);
		// System.out.println("\t machineConstraintsChuckVelocityMin:" + millingMachineConstraintsChuckVelocityMin);
		// System.out.println("\t machineConstraintsDuration:" + millingMachineConstraintsDuration);
		// System.out.println("\t machineConstraintsFrequency:" + millingMachineConstraintsFrequency);
		nonce = nonce.add(ONE);
		return millingMachineConstraints;
	}

	// ==================================================
	// Machine Constructor
	// ==================================================
	private StorageReference machineConstructor(String machineName) throws Exception {
		StorageReference machine = node.addConstructorCallTransaction(new ConstructorCallTransactionRequest(signer,
				account, nonce, chainId, BigInteger.valueOf(100_000_00), panarea(gasHelper.getSafeGasPrice()),
				productionLine, CONSTRUCTOR_MACHINE, new StringValue(machineName)));
		// System.out.println("Machine object chain identifier: " + machine);
		// System.out.println("\t machineName:" + machineName);
		nonce = nonce.add(ONE);
		return machine;
	}

	// ==================================================
	// MillingMachineMeasurements Constructor
	// ==================================================
	private StorageReference millingMachineMeasurementsConstructor(String millingMachineMeasurementsBit,
			float millingMachineMeasurementsBitSize, float millingMachineMeasurementsChuckVelocityMax,
			float millingMachineMeasurementsChuckVelocityMin, int millingMachineMeasurementsFrequency,
			int millingMachineMeasurementsStartTime, int millingMachineMeasurementsEndTime) throws Exception {
		StorageReference millingMachineMeasurements = node
				.addConstructorCallTransaction(new ConstructorCallTransactionRequest(signer, account, nonce, chainId,
						BigInteger.valueOf(100_000_00), panarea(gasHelper.getSafeGasPrice()), productionLine,
						CONSTRUCTOR_MILLINGMACHINEMEASUREMENTS, new StringValue(millingMachineMeasurementsBit),
						new FloatValue(millingMachineMeasurementsBitSize),
						new FloatValue(millingMachineMeasurementsChuckVelocityMax),
						new FloatValue(millingMachineMeasurementsChuckVelocityMin),
						new IntValue(millingMachineMeasurementsFrequency),
						new IntValue(millingMachineMeasurementsStartTime),
						new IntValue(millingMachineMeasurementsEndTime)));
		// System.out.println("MillingMachineMeasurements object chain identifier: " + millingMachineMeasurements);
		// System.out.println("\t millingMachineMeasurementsBit:" + millingMachineMeasurementsBit);
		// System.out.println("\t millingMachineMeasurementsBitSize:" + millingMachineMeasurementsBitSize);
		// System.out.println("\t millingMachineMeasurementsChuckVelocityMax:" + millingMachineMeasurementsChuckVelocityMax);
		// System.out.println("\t millingMachineMeasurementsChuckVelocityMin:" + millingMachineMeasurementsChuckVelocityMin);
		// System.out.println("\t millingMachineMeasurementsFrequency:" + millingMachineMeasurementsFrequency);
		// System.out.println("\t millingMachineMeasurementsStartTime:" + millingMachineMeasurementsStartTime);
		// System.out.println("\t millingMachineMeasurementsEndTime:" + millingMachineMeasurementsEndTime);
		nonce = nonce.add(ONE);
		return millingMachineMeasurements;
	}

	// ==================================================
	// PrinterMeasurements Constructor
	// ==================================================
	private StorageReference printerMeasurementsConstructor(String printerMeasurementsFilamentType,
			String printerMeasurementsFilamentColour, float printerMeasurementsChuckTemperatureMax,
			float printerMeasurementsChuckTemperatureMin, int printerMeasurementsFrequency,
			int printerMeasurementsStartTime, int printerMeasurementsEndTime) throws Exception {
		StorageReference printerMeasurements = node.addConstructorCallTransaction(new ConstructorCallTransactionRequest(
				signer, account, nonce, chainId, BigInteger.valueOf(100_000_00), panarea(gasHelper.getSafeGasPrice()),
				productionLine, CONSTRUCTOR_PRINTERMEASUREMENTS, new StringValue(printerMeasurementsFilamentType),
				new StringValue(printerMeasurementsFilamentColour),
				new FloatValue(printerMeasurementsChuckTemperatureMax),
				new FloatValue(printerMeasurementsChuckTemperatureMin), new IntValue(printerMeasurementsFrequency),
				new IntValue(printerMeasurementsStartTime), new IntValue(printerMeasurementsEndTime)));
		// System.out.println("PrinterMeasurements object chain identifier: " + printerMeasurements);
		// System.out.println("\t printerMeasurementsFilamentType:" + printerMeasurementsFilamentType);
		// System.out.println("\t printerMeasurementsFilamentColour:" + printerMeasurementsFilamentColour);
		// System.out.println("\t printerMeasurementsChuckTemperatureMax:" + printerMeasurementsChuckTemperatureMax);
		// System.out.println("\t printerMeasurementsChuckTemperatureMin:" + printerMeasurementsChuckTemperatureMin);
		// System.out.println("\t printerMeasurementsFrequency:" + printerMeasurementsFrequency);
		// System.out.println("\t printerMeasurementsStartTime:" + printerMeasurementsStartTime);
		// System.out.println("\t printerMeasurementsEndTime:" + printerMeasurementsEndTime);
		nonce = nonce.add(ONE);
		return printerMeasurements;
	}

	// ==================================================
	// Recipe Methods
	// ==================================================
	private void recipeAddProductProduced(StorageReference recipe, StorageReference product) throws Exception {
		StorageValue s = node.addInstanceMethodCallTransaction(new InstanceMethodCallTransactionRequest(signer, account,
				nonce, chainId, BigInteger.valueOf(100_000_00), panarea(gasHelper.getSafeGasPrice()), productionLine,
				new VoidMethodSignature(RECIPE, "addProductProduced", PRODUCT), recipe, product));
		// System.out.println("Recipe.addProduct(product) receiver -> " + recipe);
		// System.out.println("\t productAdded:" + product);
		nonce = nonce.add(ONE);
	}

	private void recipeAddOperationToDo(StorageReference recipe, StorageReference operationToDo) throws Exception {
		StorageValue s = node.addInstanceMethodCallTransaction(new InstanceMethodCallTransactionRequest(signer, account,
				nonce, chainId, BigInteger.valueOf(100_000_00), panarea(gasHelper.getSafeGasPrice()), productionLine,
				new VoidMethodSignature(RECIPE, "addOperationToDo", OPERATIONTODO), recipe, operationToDo));
		// System.out.println("Recipe.addOperationToDo(operationToDo) receiver -> " +
		// recipe);
		// System.out.println("\t operationToDoAdded:" + operationToDo);
		nonce = nonce.add(ONE);
	}

	private String recipeCheckConstraints(StorageReference recipe) throws Exception {
		return ((StringValue) node
				.runInstanceMethodCallTransaction(new InstanceMethodCallTransactionRequest(signer, account, nonce,
						chainId, BigInteger.valueOf(100_000_00), panarea(gasHelper.getSafeGasPrice()), productionLine,
						new NonVoidMethodSignature(RECIPE, "checkConstraints", ClassType.STRING), recipe))).value;

	}

	private StorageValue recipeGetProductsProduced(StorageReference recipe) throws Exception {
		StorageValue s = node.addInstanceMethodCallTransaction(new InstanceMethodCallTransactionRequest(signer, account,
				nonce, chainId, BigInteger.valueOf(100_000_00), panarea(gasHelper.getSafeGasPrice()), productionLine,
				new NonVoidMethodSignature(RECIPE, "getProducts_produced", ClassType.STORAGE_LIST), recipe));
		nonce = nonce.add(ONE);
		return s;
	}
	
	// ==================================================
	// Product Methods
	// ==================================================
	private void productAddOperationDone(StorageReference product, StorageReference operationDone) throws Exception {
		StorageValue s = node.addInstanceMethodCallTransaction(new InstanceMethodCallTransactionRequest(signer, account,
				nonce, chainId, BigInteger.valueOf(100_000_00), panarea(gasHelper.getSafeGasPrice()), productionLine,
				new VoidMethodSignature(PRODUCT, "addOperationDone", OPERATIONDONE), product, operationDone));
		// System.out.println("Product.addOpeartionDone(operationDone) receiver -> " + product);
		// System.out.println("\t operationDoneAdded:" + operationDone);
		nonce = nonce.add(ONE);
	}

	
	private void productToString(StorageReference product) throws Exception {
	    StorageValue s = node.addInstanceMethodCallTransaction (new
	    InstanceMethodCallTransactionRequest (signer, account, nonce, chainId,
	    BigInteger.valueOf(100_000_00), panarea(gasHelper.getSafeGasPrice()),
	    productionLine, new NonVoidMethodSignature(PRODUCT, "toString",ClassType.STRING), product ));
	    System.out.println("Product.toString() receiver -> " + product);
	    System.out.println("\t " + s); nonce = nonce.add(ONE); 
	}
	 
	// ==================================================
	// Machine Methods
	// ==================================================
	private void machineAddMeasurements(StorageReference machine, StorageReference measurements) throws Exception {
		StorageValue s = node.addInstanceMethodCallTransaction(new InstanceMethodCallTransactionRequest(signer, account,
				nonce, chainId, BigInteger.valueOf(100_000_00), panarea(gasHelper.getSafeGasPrice()), productionLine,
				new VoidMethodSignature(MACHINE, "addMeasurements", MEASUREMENTS), machine, measurements));
		// System.out.println("Machine.addMeasurements(measurements) receiver -> " +
		// machine);
		// System.out.println("\t measurementsAdded:" + measurements);
		nonce = nonce.add(ONE);
	}

	
	private void machineToString(StorageReference machine) throws Exception {
		StorageValue s = node.addInstanceMethodCallTransaction (new
		InstanceMethodCallTransactionRequest (signer, account, nonce, chainId,
		BigInteger.valueOf(100_000_00), panarea(gasHelper.getSafeGasPrice()),
		productionLine, new NonVoidMethodSignature(MACHINE, "toString",
		ClassType.STRING), machine ));
		System.out.println("Machine.toString() receiver -> " + machine);
		System.out.println("\t " + s); nonce = nonce.add(ONE); 
	}

	// ==================================================
	// Company Methods
	// ==================================================
	private void companyAddRecipe(StorageReference company, StorageReference recipe) throws Exception {
		StorageValue s = node.addInstanceMethodCallTransaction(new InstanceMethodCallTransactionRequest(signer, account,
				nonce, chainId, BigInteger.valueOf(100_000_00), panarea(gasHelper.getSafeGasPrice()), productionLine,
				new VoidMethodSignature(COMPANY, "addRecipe", RECIPE), company, recipe));
		// System.out.println("Company.addRecipe(recipe) receiver -> " + company);
		// System.out.println("\t recipeAdded:" + recipe);
		nonce = nonce.add(ONE);
	}

	private void companyToString(StorageReference company) throws Exception {
		StorageValue s = node.addInstanceMethodCallTransaction(new InstanceMethodCallTransactionRequest(signer, account,
				nonce, chainId, BigInteger.valueOf(100_000_00), panarea(gasHelper.getSafeGasPrice()), productionLine,
				new NonVoidMethodSignature(COMPANY, "toString", ClassType.STRING), company));
		System.out.println("Company.toString() receiver -> " + company);
		System.out.println("\t " + s);
		nonce = nonce.add(ONE);
	}

	private String getChainId() throws Exception {
		return ((StringValue) node.runInstanceMethodCallTransaction(new InstanceMethodCallTransactionRequest(
				account, // payer
				BigInteger.valueOf(100_000_00), // gas limit
				takamakaCode, // class path for the execution of the transaction
				CodeSignature.GET_CHAIN_ID, // method
				node.getManifest()))) // receiver of the method call
						.value;
	}

	private TransactionReference installJar() throws Exception {
		System.out.println("Installing jar...");

		return node.addJarStoreTransaction(new JarStoreTransactionRequest(signer, 
				// an object that signs with the payer's private key
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
		return new Account(account, "..").keys(PASSWORD, new SignatureHelper(node).signatureAlgorithmFor(account));
	}
}