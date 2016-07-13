import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Class for decision tree.
 * @author Jacky
 *
 */
public class DecisionTree {
	
	/**
	 * an instance of DataIO 
	 */
	private DataIO dataIO;
	
	/**
	 * root of decision tree
	 */
	private Node root;
	
	/**
	 * list of all attributes
	 */
	private List<String> allAttributeList;
	
	/**
	 * map of values for each attribute
	 */
	private Map<String, Set<String>> mapAttrValue;
	
	/**
	 * set of class labels (categories of target attribute)
	 */
	private Set<String> classLabels;
	
	/**
	 * attribute selection algorithm
	 */
	private Algorithm algorithm;
	
	/**
	 * Constructor
	 * @param dataIO
	 */
	public DecisionTree(DataIO dataIO) {
		this.dataIO = dataIO;		
	}
	
	/**
	 * Create a decision tree from training data,
	 * based on three attribute selection algorithms:
	 * ID3, C4.5, CART.
	 */
	public void create() {
		// get a list of all attributes, excluding the target attribute
		allAttributeList = new ArrayList<String>();
		for(int i = 0; i < dataIO.getAllAttributes().length-1; i++) {
			allAttributeList.add(dataIO.getAllAttributes()[i]);
		}
		// get map of values for each attribute
		mapAttrValue = dataIO.getMapAttrValue();
		// get class labels (values for target attribute)
		classLabels = dataIO.getClassLabels();
		// create decision tree based on different attibute selection algorithm
		switch (dataIO.getAlgorithmID()) {
			case 1: 
				algorithm = new ID3Algorithm(allAttributeList, classLabels);
				root = generateDecisionTree(dataIO.trainingTuples, allAttributeList);
				break;
			case 2:
				algorithm = new C45Algorithm(allAttributeList, classLabels);
				root = generateDecisionTree(dataIO.trainingTuples, allAttributeList);
				break;
			case 3:
				algorithm = new CARTAlgorithm(allAttributeList, classLabels, mapAttrValue);
				root = generateBiDecisionTree(dataIO.trainingTuples, allAttributeList);
				break;
		}		
		
	}
	
	/**
	 * Generate decision tree if algorithm is ID3 or C4.5.
	 * @param dataset
	 * @param attributeList
	 * @return node of decision tree
	 */
	private Node generateDecisionTree(List<String[]> dataset, List<String> attributeList) {
		// calculate the frequency of each target attribute value
		Map<String, Integer> targetFrequency = algorithm.calcFreqOfAttributeValues(dataset, allAttributeList.size());

		// if all tuples are from the same class
		// return a class node with that class
		if (targetFrequency.size() == 1) {
			ClassNode classNode = new ClassNode();
			classNode.className = (String) targetFrequency.keySet().toArray()[0];
			return classNode;
		}
		// if attributeList is empty
		// return a class node with the majority class in dataset
		if(attributeList.isEmpty()) {
			int maxCount = 0;
			String className = "";
			for(Entry<String, Integer> entry : targetFrequency.entrySet()) {
				if(entry.getValue() > maxCount) {
					maxCount = entry.getValue();
					className = entry.getKey();
				}
			}
			ClassNode classNode = new ClassNode();
			classNode.className = className;
			return classNode;
		}
		
		// find the best splitting criterion
		String attribute = algorithm.attributeSelection(dataset, attributeList, targetFrequency);
		DecisionNode decisionNode = new DecisionNode();
		decisionNode.attribute = attribute;
		// remove splitting attribute
		List<String> newAttributeList = new ArrayList<String>(attributeList);
		newAttributeList.remove(attribute);
		int attrIndex = allAttributeList.indexOf(attribute);
		// split the dataset into partitions according to the selected attribute
		Map<String, List<String[]>> partitions = new HashMap<String, List<String[]>>();
		for(String value : mapAttrValue.get(attribute)) {
			partitions.put(value, new ArrayList<String[]>());
		}
		for (String[] tuple : dataset) {
			String value = tuple[attrIndex];
			partitions.get(value).add(tuple);
		}
		
		// Create the values for the subnodes
		decisionNode.nodes = new Node[partitions.size()];
		decisionNode.attrValues = new String[partitions.size()];
		
		// For each partition, make a recursive call to create
		// the corresponding branches in the tree.
		int index = 0;
		for (Entry<String, List<String[]>> partition : partitions.entrySet()) {
			decisionNode.attrValues[index] = partition.getKey();
			if(partition.getValue().isEmpty()) {
				// if partition is empty
				// attach a class node with the majority class in dataset to decision node
				int maxCount = 0;
				String className = "";
				for(Entry<String, Integer> entry : targetFrequency.entrySet()) {
					if(entry.getValue() > maxCount) {
						maxCount = entry.getValue();
						className = entry.getKey();
					}
				}
				ClassNode classNode = new ClassNode();
				classNode.className = className;
				decisionNode.nodes[index] = classNode;				
			} else {
				// recursive call
				decisionNode.nodes[index] = generateDecisionTree(partition.getValue(), newAttributeList); 				
			}
			index++;
		}
		
		// return the root node of the subtree created
		return decisionNode;		
	}
	
	/**
	 * Generate decision tree if algorithm is CART
	 * @param dataset
	 * @param attributeList
	 * @return node of binary decision tree
	 */
	private Node generateBiDecisionTree(List<String[]> dataset, List<String> attributeList) {
		// calculate the frequency of each target attribute value
		Map<String, Integer> targetFrequency = algorithm.calcFreqOfAttributeValues(dataset, allAttributeList.size());

		// if all tuples are from the same class
		// return a class node with that class
		if (targetFrequency.size() == 1) {
			ClassNode classNode = new ClassNode();
			classNode.className = (String) targetFrequency.keySet().toArray()[0];
			return classNode;
		}
		// if attributeList is empty
		// return a class node with the majority class in dataset
		if(attributeList.isEmpty()) {
			int maxCount = 0;
			String className = "";
			for(Entry<String, Integer> entry : targetFrequency.entrySet()) {
				if(entry.getValue() > maxCount) {
					maxCount = entry.getValue();
					className = entry.getKey();
				}
			}
			ClassNode classNode = new ClassNode();
			classNode.className = className;
			return classNode;
		}
		
		// find the best splitting criterion
		String attribute = algorithm.attributeSelection(dataset, attributeList, targetFrequency);
		List<String> subset = ((CARTAlgorithm) algorithm).getSubsetByAttribute(attribute);
		BiDecisionNode biDecisionNode = new BiDecisionNode();
		biDecisionNode.attribute = attribute;
		biDecisionNode.attrValues = subset;
		// remove splitting attribute
		List<String> newAttributeList = new ArrayList<String>(attributeList);
		newAttributeList.remove(attribute);
		int attrIndex = allAttributeList.indexOf(attribute);
//		System.out.println(attribute + " " + attrIndex);
		// split the dataset into two partitions according to the subset of the selected attribute
		List<String[]> partitionD1 = new ArrayList<String[]>();
		List<String[]> partitionD2 = new ArrayList<String[]>();
		for (String[] tuple : dataset) {
			String value = tuple[attrIndex];
			if(subset.contains(value)) {
				partitionD1.add(tuple);
			} else {
				partitionD2.add(tuple);
			}
		}		
		
		// For each partition, make a recursive call to create
		// the corresponding branches in the tree.
		if(partitionD1.isEmpty()) {
			// if partition is empty
			// attach a class node with the majority class in dataset to decision node
			int maxCount = 0;
			String className = "";
			for(Entry<String, Integer> entry : targetFrequency.entrySet()) {
				if(entry.getValue() > maxCount) {
					maxCount = entry.getValue();
					className = entry.getKey();
				}
			}
			ClassNode classNode = new ClassNode();
			classNode.className = className;
			biDecisionNode.yesNode = classNode;	
		} else {
			// recursive call
			biDecisionNode.yesNode = generateBiDecisionTree(partitionD1, newAttributeList); 	
		}
		
		if(partitionD2.isEmpty()) {
			// if partition is empty
			// attach a class node with the majority class in dataset to decision node
			int maxCount = 0;
			String className = "";
			for(Entry<String, Integer> entry : targetFrequency.entrySet()) {
				if(entry.getValue() > maxCount) {
					maxCount = entry.getValue();
					className = entry.getKey();
				}
			}
			ClassNode classNode = new ClassNode();
			classNode.className = className;
			biDecisionNode.noNode = classNode;	
		} else {
			// recursive call
			biDecisionNode.noNode = generateBiDecisionTree(partitionD2, newAttributeList); 	
		}
		
		// return the root node of the subtree created
		return biDecisionNode;		
	}		
	
	/**
	 * Print the decision tree.
	 */
	public void print() {
		System.out.println("DECISION TREE");
		String indent = " ";
		if(dataIO.getAlgorithmID() == 3) {
			// if algorithm is CART
			printCART(root, indent, "");
		} else {
			// if algorithm is ID3 or C4.5
			print(root, indent, "");
		}
	}

	/**
	 * Print a sub-tree if algorithm is ID3 or C4.5.
	 * @param nodeToPrint the root note
	 * @param indent the current indentation
	 * @param value  a string that indicates the value of that branch
	 */
	private void print(Node node, String indent, String value) {
		if(value.isEmpty() == false) {
			System.out.println(indent + value);
		}
		String newIndent = indent + "  ";

		// if it is a class node
		if(node instanceof ClassNode){
			// cast to a class node and print it
			ClassNode classNode = (ClassNode) node;
			System.out.println(newIndent + "  ="+ classNode.className);
		}else{
			// if it is a decision node, cast it to a decision node
			// and print it.
			DecisionNode decisionNode = (DecisionNode) node;
			System.out.println(newIndent + decisionNode.attribute + "->");

			newIndent = newIndent + "  ";
			// then recursively call the method for subtrees
			for(int i=0; i< decisionNode.nodes.length; i++){
				print(decisionNode.nodes[i], newIndent, decisionNode.attrValues[i]);
			}
		}		
	}
	
	/**
	 * Print a sub-tree if algorithm is CART.
	 * @param nodeToPrint the root note
	 * @param indent the current indentation
	 * @param branch  a string that indicates "yes" or "no" branch
	 */
	private void printCART(Node node, String indent, String branch) {
		if(branch.isEmpty() == false) {
			System.out.println(indent + branch);
		}
		String newIndent = indent + "  ";

		// if it is a class node
		if(node instanceof ClassNode){
			// cast to a class node and print it
			ClassNode classNode = (ClassNode) node;
			System.out.println(newIndent + "  ="+ classNode.className);
		}else{
			// if it is a decision node, cast it to a decision node
			// and print it.
			BiDecisionNode biDecisionNode = (BiDecisionNode) node;
			System.out.println(newIndent + biDecisionNode.attribute + biDecisionNode.attrValues.toString() + "->");
			newIndent = newIndent + "  ";
			// then recursively call the method for subtrees
			printCART(biDecisionNode.yesNode, newIndent, "yes");
			printCART(biDecisionNode.noNode, newIndent, "no");
		}		
	}
	
	/**
	 * Classify a test tuple.
	 * @param tuple
	 * @return class label
	 */
	public String classify(String[] tuple) {
		if(dataIO.getAlgorithmID() == 3) {
			// if algorithm is CART
			return classifyByBiDecisionTree(root, tuple);
		} else {
			// if algorithm is ID3 or C4.5
			return classifyByDecisionTree(root, tuple);
		}
	}
	
	/**
	 * Classify a test tuple by traversing the decision tree, 
	 * if algorithm is ID3 or C4.5.
	 * @param node
	 * @param tuple
	 * @return class label
	 */
	private String classifyByDecisionTree(Node node, String[] tuple) {
		if(node instanceof ClassNode) {
			// if node is a class node, return class name
			return ((ClassNode) node).className;
		} else {
			// otherwise, check which subtree we should follow
			DecisionNode decisionNode = (DecisionNode) node;
			// get attribute of this decision node
			int attrIndex = allAttributeList.indexOf(decisionNode.attribute);
			// get attribute value of the tuple
			String value = tuple[attrIndex];
			// find the branch of that value
			for(int i=0; i< decisionNode.attrValues.length; i++){
				if(decisionNode.attrValues[i].equals(value)){
					return classifyByDecisionTree(decisionNode.nodes[i], tuple);
				}
			}
		}
		// if no subtree correspond to the attribute value
		return null;
	}
	
	/**
	 * Classify a test tuple by traversing the decision tree,
	 * if algorithm is CART.
	 * @param node
	 * @param tuple
	 * @return class node
	 */
	private String classifyByBiDecisionTree(Node node, String[] tuple) {
		if(node instanceof ClassNode) {
			// if node is a class node, return class name
			return ((ClassNode) node).className;
		} else {
			// otherwise, check which subtree we should follow
			BiDecisionNode biDecisionNode = (BiDecisionNode) node;
			// get attribute of this decision node
			int attrIndex = allAttributeList.indexOf(biDecisionNode.attribute);
			// get attribute value of the tuple
			String value = tuple[attrIndex];
			if(biDecisionNode.attrValues.contains(value)) {
				// if node contains that value, then "yes" branch
				return classifyByBiDecisionTree(biDecisionNode.yesNode, tuple);
			} else {
				// if not, then "no" branch
				return classifyByBiDecisionTree(biDecisionNode.noNode, tuple);
			}
		}
	}
	
}
