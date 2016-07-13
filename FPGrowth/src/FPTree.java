import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for FP-tree
 * @author Jacky
 */
public class FPTree {
	/**
	 * root of the FP-tree
	 */
	private FPNode root;
	
	/**
	 * list of items in header table, in descending order of support 
	 */
	private List<String> headerList;
	
	/**
	 * count of each single item in this tree
	 */
	private Map<String, Integer> mapSingleItem;
	
	/**
	 * node for each item in header table (key: item, value: node)
	 */
	private Map<String, FPNode> mapHeaderNode;
	
	/**
	 * last node for each item using node link (key: item, value: node)
	 */
	private Map<String, FPNode> mapLastNode;
	
	/**
	 * true if tree contains a single path
	 */
	private boolean hasSinglePath;
	
	/**
	 * Constructor.
	 * @param mapSingleItem  count of each single item in this tree
	 */
	public FPTree(Map<String, Integer> mapSingleItem) {
		root = new FPNode();
		mapHeaderNode = new HashMap<String, FPNode>();
		mapLastNode = new HashMap<String, FPNode>();
		hasSinglePath = true;
		this.mapSingleItem = mapSingleItem;
	}
	
	
	/**
	 * Check if tree contains a single path or not.
	 * @return true if tree contains a single path, false otherwise
	 */
	public boolean hasSinglePath() {
		return hasSinglePath;
	}
	
	/**
	 * Check if tree is empty or not.
	 * @return true if tree is empty, false otherwise
	 */
	public boolean isEmpty() {
		return (root.children.size() == 0);
	}
	
	/**
	 * Get length of header list.
	 * @return length of header list
	 */
	public int getHeaderListLength() {
		return headerList.size();
	}
	
	/**
	 * Get item from header list given a specific index.
	 * @param i  index in header list
	 * @return  item of index i in header list
	 */
	public String getItemFromHeaderList(int i) {
		return headerList.get(i);
	}
	
	/**
	 * Get node whose item is the last item in header list.
	 * @return node whose item is the last item in header list
	 */
	public FPNode getLastHeaderNode() {
		return mapHeaderNode.get(headerList.get(headerList.size()-1));
	}
	
	/**
	 * Get the first node in tree given a specific item. 
	 * @param item  
	 * @return the first node in tree with the given item
	 */
	public FPNode getNodeByItem(String item) {
		return mapHeaderNode.get(item);
	}
	
	/**
	 * Get support of the given item in header table.
	 * @param item
	 * @return support of the given item in header table
	 */
	public int getSupportByItem(String item) {
		return mapSingleItem.get(item);
	}
	
	/**
	 * Print header table of this tree.
	 * For each item in header list, its support is stored in Map mapSingleItem.
	 */
	public void printHeaderTable() {
		for(int i = 0; i < headerList.size(); i++) {
			String item = headerList.get(i);
			String s = (i < headerList.size()-1) ? "{" + item + ":" + mapSingleItem.get(item) + "}, " : "{" + item + ":" + mapSingleItem.get(item) + "}\n";
			System.out.print(s);
		}
	}
	
	/**
	 * Add node link after inserting a new node.
	 * Called by method addTransaction() and method addPrefixPath().
	 * @param item
	 * @param newNode
	 */
	private void addNodeLink(FPNode newNode) {
		FPNode lastNode = mapLastNode.get(newNode.item);
		if(lastNode != null) {
			//has last node, add node link to the new node
			lastNode.nodelink = newNode;
		} else {
			//no last node, add newNode to header table
			mapHeaderNode.put(newNode.item, newNode);
		}
		//set new node as last node
		mapLastNode.put(newNode.item, newNode);
	}
	
	/**
	 * Insert one transaction into FP-tree.
	 * Called when creating the base FP-tree.
	 * @param transaction
	 */
	public void addTransaction(List<String> transaction) {
		FPNode currentNode = root;
		for(String item: transaction) {
			FPNode childNode = currentNode.getChildByItem(item);
			if(childNode == null) {
				//no such node, add one
				FPNode newNode = new FPNode();
				newNode.item = item;
				newNode.count = 1;
				newNode.parent = currentNode;
				currentNode.children.add(newNode);
				//add node link
				addNodeLink(newNode);	
				//check if it contains a single path
				if(hasSinglePath == true && currentNode.children.size() > 1) {
					hasSinglePath = false;
				}
				currentNode = newNode;
			}
			else {
				//has a child, increase count by 1
				childNode.count++;
				currentNode = childNode;
			}
		}
	}
	
	/**
	 * Insert one prefix path into FP-tree.
	 * Called when creating the conditional FP-tree.
	 * @param path  a prefix path to be added
	 * @param minSupport  absolute minimum support
	 */
	public void addPrefixPath(List<FPNode> path, int minSupport) {
		//get support of the path 
		int pathCount = path.get(0).count;
		
		FPNode currentNode = root;
		for(int i = path.size()-1; i >= 1; i--) {
			FPNode node = path.get(i);
			if(mapSingleItem.get(node.item) >= minSupport) {
				//if item is frequent, add it to the tree
				FPNode childNode = currentNode.getChildByItem(node.item);
				if(childNode == null) {
					//no such node, add one
					FPNode newNode = new FPNode();
					newNode.item = node.item;
					newNode.count = pathCount;
					newNode.parent = currentNode;
					currentNode.children.add(newNode);
					//add node link
					addNodeLink(newNode);	
					//check if it contains a single path
					if(hasSinglePath == true && currentNode.children.size() > 1) {
						hasSinglePath = false;
					}
					currentNode = newNode;
				}
				else {
					//has a child, increase count by path count
					childNode.count += pathCount;
					currentNode = childNode;
				}
				
			}
		}
	}
	
	/**
	 * Create list of items in header table, in descending order of support 
	 * @param mapSingleItemInDatabase  count of each single item in database
	 */
	public void createHeaderList(Map<String, Integer> mapSingleItemInDatabase) {
		headerList = new ArrayList<String>(mapHeaderNode.keySet());
		//sort in descending order of support
		Collections.sort(headerList, new Comparator<String>() {
			@Override
			public int compare(String item1, String item2) { 
				int compare = mapSingleItemInDatabase.get(item2) - mapSingleItemInDatabase.get(item1);
				// if same support, sort in lexicographical order
				if(compare == 0){ 
					return item1.compareTo(item2);
				}
				return compare;
			}
		});
		//print header table after creating a new FP-tree 
//		System.out.print("HeaderTable: ");
//		printHeaderTable();
	}
	
	/**
	 * Traverse FP-tree.
	 */
	public void traverse() {
		System.out.println("FP-tree:");
		traverseHelper(root, 0);
		System.out.println("");
	}
	
	/**
	 * Depth first traverse FP-tree recursively.
	 * @param node 
	 * @param depth  depth of node in tree
	 */
	public void traverseHelper(FPNode node, int depth) {
		if(node == null)
			return;
		for(int i = 0; i < depth*3; i++)
			System.out.print(" ");
		System.out.println(node);
		for(FPNode child : node.children) {
			traverseHelper(child, depth+1);
		}
	}
	
}
