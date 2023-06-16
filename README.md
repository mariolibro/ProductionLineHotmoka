# Cyber Security for IoT - Project 

University project for the course of Cyber Security for IoT.

This project is designed to develop a method for certifying and verifying industrial manufacturing processes using the Hotmoka blockchain.

## Introduction
The central aim of this project is to facilitate the definition and description of key concepts inherent within an industrial plant, and subsequently store this information in the [Hotmoka blockchain](https://github.com/Hotmoka/hotmoka). This process will allow for future access and verification of the stored data, thereby improving transparency and accountability in the manufacturing process.

The project primarily concentrates on outlining the elements of a production recipe. This includes understanding the sequence of operations and identifying the machines involved in the process. Additionally, it underscores the constraints imposed by the recipe on the operations needed to create a finished product. Moreover, it tracks the operations executed to produce a specific product. The main structure of this process is detailed in the class diagram available [here](https://github.com/MarioLibro/ProductionLineHotmoka/blob/main/uml.png).

## Install and run

- Clone or download this repository.
- Follow the installation process to [install](https://github.com/Hotmoka/hotmoka#moka) the latest release of Moka (v1.0.11), and assure that is working correctly.
- Create a moka account following this [instructions](https://github.com/Hotmoka/hotmoka#creation-of-a-first-account). Make sure to execute the command-line in the entry point of the repository (`./ProductionLineHotmoka`).
Ensure to note down the *password* and the *entropy* (random sequence of bits that ends with #0).

```shell
    $ moka create-account 50000000000 --payer faucet --url panarea.hotmoka.io
```

- Using EclipseIDE, import the two projects:
  - `/runs`
  - `/production_line`

- In `/production_line` execute via terminal:

```shell
    $ mvn clean package
    $ mvn package
```

- In `/runs/src/main/java/runs/ProductionLine.java` update the `ADDRESS` and `PASSWORD` variables at lines 40 and 41 with your respective *entropy* and *password*.

- Build and execute `runs/src/main/java/runs/ProductionLine.java`
- The execution will take approximately 5-10 minutes. It will install the Jar in a Hotmoka Node, instantiate all the objects, and store them in a Hotmoka node. At the end it will run the `checkConstraints()` method that verifies if the recipe's constraints are respected.
- It is also possibile to run the `checkConstraints()` for recipes that have been already stored in the blockchain. To test this functionality you need to annotate the `entropy` of the recipe, printed on the terminal (`Recipe object chain identifier: **recipe_entropy**`) and execute this command:

```shell
    $ moka call **recipe_entropy_placeholder** checkConstraints --payer **account_entropy_placeholder** --url panarea.hotmoka.io
```

## Results

To better understand the results obtained, I recommend reviewing the documents located in the `/docs directory`.

