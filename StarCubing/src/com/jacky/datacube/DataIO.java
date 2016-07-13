package com.jacky.datacube;

import java.io.*;
import java.util.*;

/**
 * Class for data input and output
 * 
 *
 */
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
	 * saved data
	 */
	private int[][] data;
	
	/**
	 * number of all tuples
	 */
	private int tupleCount;
	
	/**
	 * number of dimensions
	 */
	private int dimensionCount; //number of dimensions
	
	/**
	 * cardinality of each dimension
	 */
	private int[] cardinality; //cardinality of each dimension
	
	/**
	 * number of values of each cardinality of each dimension
	 */
	private int[][] valueCount;
	
	/**
	 * minimum support threshold
	 */
	private int minSupport;
	
	/**
	 * cardinality of each dimension after star reduction
	 */
	private int[] starCardinality;
	
	/**
	 * non-star value of each dimension after star reduction:
	 */
	private int[][] nonStarValue;
	
	/**
	 * do star reduction and rename non-star values
	 */
	private int[][] starValueHelper;
	
	/**
	 * store original values of data, the index is its new value in star-cubing
	 */
	private ArrayList<ArrayList<String>> valueMap;
	
	/**
	 * store iceberg cubes 
	 */
	private ArrayList<String> result;
	
	/**
	 * delimiter which splits values in one tuple
	 */
	private String delimiter;
	
	/**
	 * true if input file has a header
	 */
	private boolean hasHeader;
	
	/**
	 * true if need reorder dimensions
	 */
	private boolean needReorder;
	
	/**
	 * store dimension map after reordering
	 */
	private int[] dimensionMap;
	
	/**
	 * Constructor
	 * @param inputFile  input file name
	 * @param outputFile  output file name
	 */
	public DataIO(String inputFile, String outputFile) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}
	
	/**
	 * Set value of minSupport.
	 * @param minSupport  minimum support threshold
	 */
	public void setMinSupport(int minSupport) {
		this.minSupport = minSupport;
	}
	
	/**
	 * Set value of hasHeader.
	 * @param hasHeader  true if input file has header
	 */
	public void setHasHeader(boolean hasHeader) {
		this.hasHeader = hasHeader;
	}
	
	/**
	 * Set value of hasReorder.
	 * @param hasReorder  true if need reorder dimensions.
	 */
	public void setNeedReorder(boolean needReorder) {
		this.needReorder = needReorder;
	}
	
	/**
	 * Get value of minSupport.
	 * @return minimum support threshold
	 */
	public int getMinSupport() {
		return minSupport;
	}
	
	/**
	 * Get value of tupleCount.
	 * @return number of all tuples
	 */
	public int getTupleCount() {
		return tupleCount;
	}
	
	/**
	 * Get value of dimensionCount.
	 * @return number of dimensionCount
	 */
	public int getDimensionCount() {
		return dimensionCount;
	}
	
	/**
	 * Get values of one dimension.
	 * @param dimension  
	 * @return  an array of values of one dimension
	 */
	public int[] getDataByDimension(int dimension) {
		return data[dimension];
	}
	
	/**
	 * Get value of one tuple
	 * @param tuple
	 * @return  value of one tuple 
	 */
	public int[] getDataByTuple(int tuple) {
		int[] temp = new int[dimensionCount];
		for(int di = 0; di < dimensionCount; di++) {
			temp[di] = data[di][tuple];
		}
		return temp;
	}
	
	/**
	 * Get cardinality of one dimension after star reduction
	 * @param dimension
	 * @return cardinality of one dimension
	 */
	public int getCardinality(int dimension) {
		return starCardinality[dimension];
	}
	
	
	/**
	 * Print all data.
	 */
	public void printData() {
		for(int ti = 0; ti < tupleCount; ti++) {
			for(int di = 0; di < dimensionCount; di++) {
				System.out.print(data[di][ti]+" ");
			}
			System.out.print('\n');
		}
	}
	
	
	/**
	 * find the delimiter in input file
	 * @param line  one tuple
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
	 * Do data preprocessing. Called by readFile() method.
	 * Get number of tuples, number of dimensions, and cardinality of each dimension.
	 * Transform original values into integers starting from 1 for each dimension.
	 * Count values for each dimension 
	 * @param reader  a random access file stream to read from input file
	 */
	private boolean preprocess(RandomAccessFile reader) {				
		try {
			reader.seek(0);
			String line = null;
			tupleCount = 0;
			if(hasHeader) {
				//if has header, skip one line
				line = reader.readLine();
			}
			while((line = reader.readLine()) != null) {
				//get number of tuples
				//get number of dimensions
				delimiter = findDelimiter(line);
				String[] tuple = line.split(delimiter);
				if(dimensionCount == 0) {
					dimensionCount = tuple.length;
					valueMap = new ArrayList<ArrayList<String>>(dimensionCount);
					for(int di = 0; di < dimensionCount; di++) {
						valueMap.add(new ArrayList<String>());
					}
				}
				for(int di = 0; di < dimensionCount; di++) {
					if(! valueMap.get(di).contains(tuple[di])) {
						valueMap.get(di).add(tuple[di]);
					}
				}
				tupleCount++;
			}
			
			if(tupleCount == 0) {
				//if file is empty
				System.out.println("No data!");
				return false;
			}
			
			//initialize dimension map
			dimensionMap = new int[dimensionCount];
			for(int di = 0; di < dimensionCount; di++) {
				dimensionMap[di] = di;
			}
			
			//get cardinality of each dimension
			cardinality = new int[dimensionCount];
			for(int di = 0; di < dimensionCount; di++) {
				cardinality[di] = valueMap.get(di).size();
			}
			
			//count values for each dimension
			//transform original values into integers starting from 1 for each dimension 
			reader.seek(0);
			valueCount = new int[dimensionCount][];
			for(int di = 0; di < dimensionCount; di++) {
				valueCount[di] = new int[cardinality[di]];
			}						
			data = new int[dimensionCount][tupleCount];	
			if(hasHeader) {
				line = reader.readLine();
			}
			for(int ti = 0; ti < tupleCount; ti++) {
				String[] tuple = reader.readLine().split(delimiter);
				for(int di = 0; di < dimensionCount; di++) {
					int value = valueMap.get(di).indexOf(tuple[di]) + 1;
					data[di][ti] = value;
					valueCount[di][value-1]++;
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}	
		return true;	
	}
	
	/**
	 * Do data compression. Called by readFile() method.
	 * Replace star value by 0
	 * Rename non-star values
	 */
	private void compress() {
		starCardinality = new int[dimensionCount];
		nonStarValue = new int[dimensionCount][];
		starValueHelper = new int[dimensionCount][];
		for(int di = 0; di < dimensionCount; di++) {
			//do star reduction and rename non-star values
			starValueHelper[di] = new int[cardinality[di]];
			int newValue = 0;
			for(int i = 0; i < cardinality[di]; i++) {
				if(valueCount[di][i] >= minSupport) {
					starValueHelper[di][i] = ++newValue;
				}
			}
			//construct compressed table
			for(int ti = 0; ti < tupleCount; ti++) {
				data[di][ti] = starValueHelper[di][data[di][ti]-1];
			}
			//get starCardinality
			starCardinality[di] = newValue;
			//get non-star value
			nonStarValue[di] = new int[starCardinality[di]];
			for(int i = 0; i < cardinality[di]; i++) {
				if(starValueHelper[di][i] > 0) {
					nonStarValue[di][starValueHelper[di][i]-1] = i+1;
				}
			}
		}			
	}
	
	/**
	 * Reorder dimensions by decreasing cardinality. Called by readFile() method.
	 */
	private void reorderDimensions() {
		int[] dimensions = new int[dimensionCount];
		for(int di = 0; di < dimensionCount; di++) {
			dimensions[di] = di;
		}
		for(int i = 0; i < dimensionCount; i++) {
			for(int j = i+1; j < dimensionCount; j++) {
				if(starCardinality[i] < starCardinality[j]) {
					swap(starCardinality, i, j);
					swap(dimensions, i, j);
					swap(data, i, j);
				}
			}
		}
		for(int i = 0; i < dimensionCount; i++) {
			dimensionMap[dimensions[i]] = i;
		}
	}
	
	/**
	 * Swap two values in an array.
	 * @param a
	 * @param i
	 * @param j
	 */
	private void swap(int[] a, int i, int j) {
		int temp = a[i];
		a[i] = a[j];
		a[j] = temp;
	}
	/**
	 * Swap two rows in a 2-d array.
	 * @param a
	 * @param i
	 * @param j
	 */
	private void swap(int[][] a, int i, int j) {
		for(int k = 0; k < a[i].length; k++) {
			int temp = a[i][k];
			a[i][k] = a[j][k];
			a[j][k] = temp;
		}
	}

	/**
	 * Read input file. Called by run() method.
	 * Do data preprocessing and compression, store data for star cubing.
	 */
	private void readFile() {
		boolean success = false;
		try {
			File file = new File(inputFile);
			RandomAccessFile reader = new RandomAccessFile(file, "r");
			//if open file, do data preprocessing
			System.out.print("Read data >>>\t");
			success = preprocess(reader);													
			reader.close();
		} catch(FileNotFoundException e) {
			System.out.println("No such file!");
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		if(success) {
			System.out.println("Complete!");
//			printData();
			//if file is not empty, do data compression
			System.out.print("Compress data >>>\t");
			compress();
			System.out.println("Complete!");
//			printData();
			
			if(needReorder) {
				//if need reorder, reorder dimensions by decreasing cardinality
				System.out.print("Reorder dimensions >>>\t");
				reorderDimensions();
				System.out.println("Complete!");
//				printData();
			}
		}
	}
	

	/**
	 * Output iceberg cube on screen.
	 * @param tuple
	 * @param measure
	 */
	public void output(int[] tuple, int measure) {
		if(result == null) {
			result = new ArrayList<String>();
		}
		//store one iceberg cell
		String res = new String();
		for(int di = 0; di < dimensionCount; di++) {
			//output value by original dimension ordering
			int temp = tuple[dimensionMap[di]];
			if(temp != 0) {
				temp = nonStarValue[di][temp-1];
			}
			//get original values
			String s = (temp == StarTree.ALL) ? "*": valueMap.get(di).get(temp-1);
			System.out.print(String.format("%-5s", s));
			res = res + s + " ";
		}
		System.out.println(": " + measure);
		//add to result list, later write into output file
		result.add(res + ": " + measure);
	}
	
	/**
	 * Write iceberg cubes into output file. Called by run() method.
	 */
	private void writeFile() {
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
	
	/**
	 * Run star-cubing algorithm
	 */
	public void run() {
		long startTime = System.currentTimeMillis();
		readFile();
		StarTree[] treeList = new StarTree[dimensionCount];
		for(int i = 0; i < treeList.length; i++) {
			treeList[i] = new StarTree(this, i, treeList);
		}
		System.out.print("Create base tree >>>\t");		
		treeList[0].createTreeFromData();
		System.out.println("Complete!");
		System.out.print("Do star-cubing >>>\n");
		treeList[0].starCubing();
		long endTime = System.currentTimeMillis();
		writeFile();
		System.out.println("\nIceberg cube computation is complete!");
		System.out.println("Running time: " + (endTime-startTime) + "ms\n");
	}
	
}
