import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DataIO {
	/**
	 * input file name
	 */
	private String inputFile;
	
	/**
	 * output file name
	 */
	private String outputFile;
	
	/**
	 * number of all tuples
	 */
	private int transactionCount;
	
	/**
	 * minimum support threshold (%)
	 */
	private float minSupRatio;
	
	/**
	 * absolute minimum support: minSupRatio * transactionCount / 100
	 */
	private int minSupport;
	
	/**
	 * delimiter which splits values in one tuple
	 */
	private String delimiter;
	
	/**
	 * count of each item
	 */
	private final Map<String, Integer> mapSingleItem;
	
	/**
	 * Constructor.
	 * @param inputFile  input file name
	 * @param outputFile  output file name
	 * @param minSupRatio  minimum support threshold (%)
	 */
	public DataIO(String inputFile, String outputFile, float minSupRatio) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.minSupRatio = minSupRatio;
		mapSingleItem = new HashMap<String, Integer>();
	}
	
	/**
	 * Set relative minimum support threshold.
	 * @param minSupRatio minimum support threshold (%)
	 */
	public void setMinSupRatio(float minSupRatio) {
		this.minSupRatio = minSupRatio;
	}
	
	/**
	 * Get absolute minimum support.
	 * @return minSupport
	 */
	public int getMinSupport() {
		return minSupport;
	}
	
	/**
	 * Get number of all transactions.
	 * @return transactionCount
	 */
	public int getTransactionCount() {
		return transactionCount;
	}
	
	/**
	 * Get input file name.
	 * @return input file name
	 */
	public String getInputFile() {
		return inputFile;
	}
	
	/**
	 * Get delimiter.
	 * @return delimiter
	 */
	public String getDelimiter() {
		return delimiter; 
	}
	
	/**
	 * Get a map of count of each single item in database.
	 * @return mapSingleItem
	 */
	public Map<String, Integer> getMapSingleItem() {
		return mapSingleItem;
	}
	
	/**
	 * Print count of each single item in database.
	 */
	public void printSingleItemSet() {
		for(Map.Entry<String, Integer> entry : mapSingleItem.entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
	}
	
	/**
	 * find the delimiter in input file
	 * @param line  one transaction
	 * @return  delimiter
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
	 * Read input file.
	 * Scan database once,
	 * get number of all transactions, 
	 * get count of each single item.
	 */
	public void readFile() {
		try {
			File file = new File(inputFile);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			System.out.print("Read data >>>\t");
			String line;
			while((line = reader.readLine()) != null) {
				if(line.isEmpty() == true) {
					//if the line is empty
					continue;
				}
				if(transactionCount == 0) {
					//find delimiter when reading first transaction
					delimiter = findDelimiter(line);
				}
				
				String[] transactionString = line.split(delimiter);
				for(String item : transactionString) {
					if(mapSingleItem.get(item) == null) {
						//no such item, add one
						mapSingleItem.put(item, 1);
					}
					else {
						//has item, increase its count by 1
						mapSingleItem.put(item, mapSingleItem.get(item)+1);
					}
				}				
				transactionCount++;
			}			
			reader.close();
			System.out.println("Complete!");
			//set minimum support
			minSupport = (int) Math.ceil(minSupRatio * transactionCount / 100);	
		} catch(FileNotFoundException e) {
			System.out.println("No such file!");
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Print absolute minimum support.
	 */
	public void printMinSup() {
		System.out.println("Min_Sup: " + minSupRatio + "% of " + transactionCount + " transactions = " + minSupport);		
	}
	
	/**
	 * create the base FP-tree by scanning transactions in database
	 * @param fpTree the created FP-tree
	 */
	public void createTreeFromData(FPTree fpTree) {		
		try {
			File file = new File(inputFile);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			System.out.print("Create FP-tree >>>\t");
			String line;
			while((line = reader.readLine()) != null) {
				if(line.isEmpty() == true) {
					//if the line is empty
					continue;
				}
				String[] transactionString = line.split(delimiter);
				List<String> transaction = new ArrayList<String>();
				for(String item : transactionString) {
					if(mapSingleItem.get(item) >= minSupport) {
						//if item is frequent, add it to transaction
						//otherwise, prune
						transaction.add(item);
					}
				}
				//sort items by descending order of support
				Collections.sort(transaction, new Comparator<String>() {
					@Override
					public int compare(String item1, String item2) { 
						int compare = mapSingleItem.get(item2) - mapSingleItem.get(item1);
						// if same support, sort in lexicographical order
						if(compare == 0){ 
							return item1.compareTo(item2);
						}
						return compare;
					}
				});
//				System.out.println(transaction);
				//add the sorted transaction to the FP-tree
				fpTree.addTransaction(transaction);
			}
			reader.close();
			System.out.println("Complete!");
			System.out.print("\n");				
		} catch(FileNotFoundException e) {
			System.out.println("No such file!");
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Write frequent patterns into output file.
	 */
	public void writeFile(List<String> result) {
		try {
			File file = new File(outputFile);
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for(String each : result) {
				writer.write(each + "\n");
			}
			writer.flush();
			writer.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}

}
