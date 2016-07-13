import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class for data input and output
 * @author Jacky
 *
 */
public class DataIO {
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
	 * decision tree algorithm id: 1, 2, 3
	 */
	private int algorithmID;
	
	/**
	 * delimiter which splits values in one tuple
	 */
	private String delimiter;
	
	/**
	 * list of all attributes
	 */
	private String[] allAttributes;
	
	/**
	 * store all training tuples	
	 */
	public List<String[]> trainingTuples;
	
	/**
	 * store all test tuples
	 */
	public List<String[]> testTuples;
	
	/**
	 * set of class labels (categories of target attribute)
	 */
	private Set<String> classLabels;
	
	/**
	 * map of values for each attribute
	 */
	private Map<String, Set<String>> mapAttrValue;
		
	/**
	 * Constructor
	 * @param trainingFile
	 * @param testFile
	 * @param outputFile
	 * @param algorithmID
	 */
	public DataIO(String trainingFile, String testFile, String outputFile, int algorithmID) {
		this.trainingFile = trainingFile;
		this.testFile = testFile;
		this.outputFile = outputFile;
		this.algorithmID = algorithmID;
	}
	
	/**
	 * Get algorithm id
	 * @return int algorithmID
	 */
	public int getAlgorithmID() {
		return algorithmID;
	}
	
	/**
	 * Get the list of all attributes
	 * @return String[] allAttributes
	 */
	public String[] getAllAttributes() {
		return allAttributes;
	}	
	
	/**
	 * Get class labels
	 * @return Set<String> classLabels
	 */
	public Set<String> getClassLabels() {
		return classLabels;
	}
	
	/**
	 * Get map of attribute values
	 * @return Map<String, Set<String>> mapAttrValue
	 */
	public Map<String, Set<String>> getMapAttrValue() {
		return mapAttrValue;
	}
	
	/**
	 * Find the delimiter in input file
	 * @param line  one tuple
	 * @return  String delimiter
	 */
	private String findDelimiter(String line) {
		String d = null;
		if(line.indexOf(",") > 0) {
			d = ",";
		} else if(line.indexOf(";") > 0) {
			d = ";";
		} else if(line.indexOf(" ") > 0) {
			d = " ";
		}
		return d;
	}
	
	/**
	 * Read training file.
	 * Scan database once,
	 * get all attributes and values, 
	 * get class lables.
	 */
	public void readTrainingFile() {
		try {
			File file = new File(trainingFile);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			System.out.print("Read training data >>>\t");
			// read first line to get attribute names
			String line = reader.readLine();
			// find delimiter
			delimiter = findDelimiter(line);
			// get all attributes
			allAttributes = line.split(delimiter);
			// target attribute is at the last position
			int targetIndex = allAttributes.length - 1;
			
			trainingTuples = new ArrayList<String[]>();
			classLabels = new HashSet<String>();
			mapAttrValue = new HashMap<String, Set<String>>();
			while((line = reader.readLine()) != null) {
				if(line.isEmpty() == true) {
					//if the line is empty
					continue;
				}				
				// read one tuple
				String[] tuple = line.split(delimiter);
				trainingTuples.add(tuple);
				// add attribute value to mapAttrValue
				for(int i = 0; i < targetIndex; i++) {
					String attribute = allAttributes[i];
					String value = tuple[i];
					Set<String> valueSet = mapAttrValue.get(attribute);
					if(valueSet == null) {
						valueSet = new HashSet<String>();
						mapAttrValue.put(attribute, valueSet);
					}
					valueSet.add(value);
				}
				// get class label
				classLabels.add(tuple[targetIndex]);
			}			
			reader.close();
			System.out.println("Complete!");	
		} catch(FileNotFoundException e) {
			System.out.println("No such file!");
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Read test file.
	 */
	public void readTestFile() {
		try {
			File file = new File(testFile);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			testTuples = new ArrayList<String[]>();
			while((line = reader.readLine()) != null) {
				if(line.isEmpty() == true) {
					//if the line is empty
					continue;
				}				
				String[] tuple = line.split(delimiter);
				testTuples.add(tuple);
			}			
			reader.close();
		} catch(FileNotFoundException e) {
			System.out.println("No such file!");
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Write classification results of test file into output file.
	 * @param result classification results for each tuple in test file
	 */
	public void writeFile(String[] results) {
		try {
			File file = new File(outputFile);
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for(int i = 0; i < results.length; i++) {
				String[] tuple = testTuples.get(i);
				// convert String[] to String
				String tupleString = "";
				for(int j = 0; j < tuple.length; j++) {
					tupleString += tuple[j] + ",";
				}
				writer.write(tupleString + results[i] + "\n");
			}
			writer.flush();
			writer.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
