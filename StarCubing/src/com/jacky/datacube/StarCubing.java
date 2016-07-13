package com.jacky.datacube;

import java.util.Scanner;

/**
 * Main-Class for launching the program.
 */
public class StarCubing {
	
	/**
	 * an instance of DataIO to get data information
	 */
	private DataIO dataIO;
	
	/**
	 * Initialization of dataIO.
	 * @param inputFile  input file name
	 * @param hasHeader  true if input file has a header
	 * @param hasReorder  true if reordering dimensions
	 * @param minSupport  minimum support threshold
	 * @param outputFile  output file name
	 */
	private void init(String inputFile, boolean hasHeader, boolean needReorder, int minSupport, String outputFile) {
		dataIO = new DataIO(inputFile, outputFile);
		dataIO.setHasHeader(hasHeader);
		dataIO.setNeedReorder(needReorder);
		dataIO.setMinSupport(minSupport);
	}
	
	/**
	 * Do iceberg cubes computation.
	 * Call dataIO.run() method
	 */
	public void compute() {
		dataIO.run();
	}
	
	/**
	 * main method
	 * Choose input file, output file, and set minimum support, then do iceberg cubes computation
	 * @param args
	 */
	public static void main(String[] args) {
		
		StarCubing cubing = new StarCubing();
		Scanner input = new Scanner(System.in);
		String answer = new String();
		
		System.out.println("Welcome to Star-Cubing >>>>>>\n");
		
		System.out.print("Please enter the INPUT file name: ");		
		String inputFile = input.next();
		
//		System.out.print("Does this file has a HEADER? [y/n]: ");		
//		String header = input.next();
//		boolean hasHeader = false;
//		if(header.equals("y")) {
//			hasHeader = true;
//		}
		
//		System.out.print("Do you want to REORDER the dimensions? [y/n]: ");		
//		String reorder = input.next();
//		boolean needReorder = false;
//		if(reorder.equals("y")) {
//			needReorder = true;
//		}
//		
		System.out.print("Please enter the MINIMUM SUPPORT: ");		
		int minSupport = input.nextInt();
		
		System.out.print("Please enter the OUTPUT file name: ");		
		String outputFile = input.next();
		
		System.out.print("\n");		
		
		cubing.init(inputFile, false, true, minSupport, outputFile);
			
		//compute multiple times if needed
		do {
			cubing.compute();
			System.out.print("Do you need to compute again? [y/n]: ");
			answer = input.next();
		} while(answer.equals("y"));
			
		input.close();
		
		System.exit(-1);
	}

}
