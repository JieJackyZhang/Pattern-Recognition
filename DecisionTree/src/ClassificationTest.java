import java.io.File;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Class for test
 * @author Jacky
 *
 */
public class ClassificationTest {
	
	/**
	 * input stream
	 */
	public Scanner input;
	
	/**
	 * an instance of DataIO 
	 */
	private DataIO dataIO;
	
	/**
	 * training file name
	 */
	private String trainingFile;
	
	/**
	 * test file name
	 */
	private String testFile;
	
	/**
	 * output file name
	 */
	private String outputFile;
	
	/**
	 * an instance of decision tree
	 */
	private DecisionTree dt;
	
	/**
	 * decision tree algorithm id
	 */
	private int algorithmID;
	
	/**
	 * Initialization.
	 */
	private void init() {
		dataIO = new DataIO(trainingFile, testFile, outputFile, algorithmID);
	}
	
	/**
	 * Check if algorithm id is valid.
	 * @param algorithmID
	 * @return true if algorithm id is valid, false otherwise
	 */
	private boolean checkAlgorithmID(float algorithmID) {
		if(algorithmID == 1 || algorithmID == 2 || algorithmID == 3)
			return true;
		else
			return false;
	}		
	
	/**
	 * User Interface when launching the program.
	 * Set algorithm, training file, test file and output file. 
	 */
	public void set() {				
		System.out.println("Welcome to Classification: Decision Tree Induction >>>>>>\n");		
		// choose algorithm
		System.out.println("[1] Information Gain (ID3)");
		System.out.println("[2] Gain Ratio (C4.5)");
		System.out.println("[3] Gini Index (CART)");
		System.out.println("");
		System.out.print("Please choose the ALGORITHM: ");
		boolean isValidID = false;
		try {
			algorithmID = input.nextInt();
			isValidID = checkAlgorithmID(algorithmID);
		} catch(InputMismatchException e) {
			isValidID = false;
		}
		while(isValidID == false) {
			input.nextLine();
			System.out.println("The input is not a valid number!");	
			System.out.print("Please choose the ALGORITHM: ");	
			try {
				algorithmID = input.nextInt();
				isValidID = checkAlgorithmID(algorithmID);
			} catch(InputMismatchException e) {
				isValidID = false;
			}
		}
		// choose training file
		System.out.print("Please enter the TRAINING file name: ");
		trainingFile = input.next();
		// check if it is a valid file
		boolean isFile = new File(trainingFile).isFile();
		while(isFile == false) {
			System.out.println("The input is not a valid file!");
			System.out.print("Please enter the TRAINING file name: ");		
			trainingFile = input.next();
			isFile = new File(trainingFile).isFile();
		}		
		// choose test file
		System.out.print("Please enter the TEST file name: ");
		testFile = input.next();
		// check if it is a valid file
		isFile = new File(testFile).isFile();
		while(isFile == false) {
			System.out.println("The input is not a valid file!");
			System.out.print("Please enter the TEST file name: ");		
			testFile = input.next();
			isFile = new File(testFile).isFile();
		}
		// choose output file
		System.out.print("Please enter the OUTPUT file name: ");		
		outputFile = input.next();
		
		System.out.print("\n");				
		
		//initialize an instance of DataIO.
		init();
			
	}
	
	/**
	 * Train data to create decision tree
	 */
	public void train() {
		// get start time
		long startTime = System.currentTimeMillis();
		// read training data from file
		dataIO.readTrainingFile();
		// create decision tree from training data
		dt = new DecisionTree(dataIO);
		dt.create();
		// get end time
		long endTime = System.currentTimeMillis();
		// print decision tree
		dt.print();
		// display training time
		System.out.println("Training time: " + (endTime-startTime) + "ms\n");
	}
	
	/**
	 * Test tuples based on the created decision tree.
	 */
	public void test() {
		// read test data from test file
		dataIO.readTestFile();
		// get classification results for each tuple in test file
		String[] results = new String[dataIO.testTuples.size()];
		for(int i = 0; i < dataIO.testTuples.size(); i++) {
			results[i] = dt.classify(dataIO.testTuples.get(i));
		}
		//write results to output file
		dataIO.writeFile(results);
	}
	
	/**
	 * Main method to launch the program.
	 * @param args
	 */
	public static void main(String[] args) {
		ClassificationTest test = new ClassificationTest();
		test.input = new Scanner(System.in);
		// initialization
		test.set();
		// train data
		test.train();
		// test data
		test.test();
		test.input.close();
		System.exit(-1);
	}

}
