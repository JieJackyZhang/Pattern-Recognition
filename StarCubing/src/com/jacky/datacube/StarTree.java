package com.jacky.datacube;

import java.util.ArrayList;

/**
 * Class for a star tree
 * Create a star tree from star table.
 * Do star-cubing to compute iceberg cubes
 */
public class StarTree {
	
	/**
	 * Class for a node in StarTree.
	 * A node has four fields:
	 * the attribute value, aggregate value, first child, and first sibling.
	 */
	private static class Node {
		/**
		 * the attribute value
		 */
		private int value;
		
		/**
		 * the antimonotonic aggregate value, such as count
		 */
		private int measure;
		
		/**
		 * pointer to the first child
		 */
		private Node firstChild;
		
		/**
		 * pointer to the first sibling
		 */
		private Node sibling;		
		
		/**
		 * Constructor: creates a new Node instance.
		 */
		public Node() {
			value = 0;
			measure = 0;
			firstChild = null;
			sibling = null;			
		}
	}
	
	
	/**
	 * Class for helping create base star tree from data
	 */
	private static class CreateTreeHelper {
		/**
		 * an instance of DataIO to get data information
		 */
		private DataIO dataIO;
		
		/**
		 * index of sorted data by node ordering 
		 */
		private int[] sortedIndex;
		
		/**
		 * the previous index of each value
		 */
		private int[] preIndex;
		
		/**
		 * links same value, -1 is the end
		 */
		private int[] linkedIndex;
		
		/**
		 * partition data by node ordering
		 */
		private ArrayList<ArrayList<Integer>> partition;
		
		public CreateTreeHelper(DataIO dataIO) {
			this.dataIO = dataIO;
			sortedIndex = new int[dataIO.getTupleCount()];
			for(int i = 0; i < dataIO.getTupleCount(); i++) {
				sortedIndex[i] = i;
			}
			linkedIndex = new int[dataIO.getTupleCount()];
			partition = new ArrayList<ArrayList<Integer>>(dataIO.getDimensionCount());
			for(int i = 0; i < dataIO.getDimensionCount(); i++) {
				partition.add(new ArrayList<Integer>());
			}
			
		}
		
		/**
		 * Recursively create star tree with Node root from sortedIndex[left...right]
		 * @param root
		 * @param dimension
		 * @param left
		 * @param right
		 */
		private void createTree(Node root, int dimension, int left, int right) {
			//set measure
			root.measure = right - left;
			int[] dimensionData = dataIO.getDataByDimension(dimension);
			
			//Node ordering: *, p1, p2, ..., pn
			//preIndex stores the previous index of each value now
			//linkedIndex links same value with original ordering
			preIndex = new int[dataIO.getCardinality(dimension)+1];
			for(int i = 0; i < preIndex.length; i++) {
				preIndex[i] = -1;
			}
			for(int i = left; i < right; i++) {
				int index = sortedIndex[i];
				int value = dimensionData[index];
				linkedIndex[index] = preIndex[value];
				preIndex[value] = index;
			}
			//sort values in sortedIndex and store the partition position
			partition.get(dimension).clear();
			partition.get(dimension).add(left);
			int p = left;
			for(int i = 0; i < dataIO.getCardinality(dimension)+1; i++) {
				if(preIndex[i] != -1) {
					int index = preIndex[i];
					while(index != -1) {
						sortedIndex[p++] = index;
						index = linkedIndex[index];
					}
					partition.get(dimension).add(p);
				}
				
			}
			
			Node cur = new Node();
			boolean first = true;
			for(int i = 0; i < partition.get(dimension).size()-1; i++) {						
				if(first == true) {
					//generate first child
					first = false;
					root.firstChild = cur;
				}
				else {
					//generate sibling
					Node temp = new Node();
					cur.sibling = temp;
					cur = temp;
				}			
				int newLeft = partition.get(dimension).get(i);
				int newRight = partition.get(dimension).get(i+1);
				cur.value = dimensionData[sortedIndex[newLeft]];
				if(dimension == dataIO.getDimensionCount()-1) {
					//if leaf
					cur.measure = newRight - newLeft;
				} else {
					//if not leaf, recursion
					createTree(cur, dimension+1, newLeft, newRight);
				}
			}			
		
		}
	}
	
	/**
	 * an instance of DataIO to get data information
	 */
	private DataIO dataIO;
	
	/**
	 * minimum support threshold for iceberg condition
	 */
	private int minSupport;
	
	/**
	 * number of dimensions
	 */
	private int dimensionCount;
	
	/**
	 * start dimension of this tree, (0, ..., dimensionCount-1)
	 */
	private int startDimension;
	
	/**
	 * depth of this tree
	 */
	private int depth;
	
	/**
	 * root of this tree
	 */
	private Node root;
	
	/**
	 * next-level child trees created by this tree
	 */
	private StarTree[] childTree;
	
	/**
	 * number of child trees
	 */
	private int childTreeCount;
	
	/**
	 * number of star nodes in one path
	 */
	private int starCount;
	
	/**
	 * output buffer of one tuple
	 */
	private int[] outputBuf;
	
	/**
	 * store current state when generating node
	 */
	private int currentDepth;
	private Node[] currentNode;
	private Node[] starNode;
	private Node[] existNode;

	/**
	 * value of STAR and ALL
	 */
	public static final int STAR = 0;
	public static final int ALL = 0;
	
	/**
	 * Constructor: create a StarTree instance.
	 * @param dataIO  dataIO from which the data information is received
	 * @param startDimension  start dimension of this tree
	 * @param childTree  next-level child trees created by this tree
	 */
	public StarTree(DataIO dataIO, int startDimension, StarTree[] childTree) {
		this.dataIO = dataIO;
		this.minSupport = dataIO.getMinSupport();	
		dimensionCount = dataIO.getDimensionCount();
		this.startDimension = startDimension;
		depth = dimensionCount - startDimension;
		root = null;
		this.childTree = childTree;
		outputBuf = new int[dimensionCount];
		currentNode = new Node[depth+1];
		starNode = new Node[depth+1];
		existNode = new Node[depth+1];
	}	
	
	/**
	 * Create base star tree.
	 */
	public void createTreeFromData() {
		root = new Node();
		CreateTreeHelper helper = new CreateTreeHelper(dataIO);
		helper.createTree(root, 0, 0, dataIO.getTupleCount());		
	}
			
	
	/**
	 * First traversal to Node n.
	 * Generate child trees.
	 * @param n
	 * @param nDepth
	 */
	private void firstVisit(Node n, int nDepth) {		
		if(nDepth > 0)
			outputBuf[startDimension + nDepth - 1] = n.value;
		if(n.value == STAR)
			starCount++;	
		
		if(starCount == 0 && n.measure >= minSupport && nDepth <= depth-2) {
			//initiate a new child tree
			//child tree root contains star node, prune
			//child tree root does not satisfy minSupport, prune
			//if(nDepth == depth-1), no need to create a new child tree, since depth == 0
			childTree[startDimension + 1 + nDepth].init(outputBuf);
			childTreeCount++;
		}
		//Generate Node n in child trees
		for(int i = 1; i <= childTreeCount; i++) {
			childTree[startDimension + i].generate(n);
		}
			
			
	}
	
	/**
	 * Backtrack to Node n.
	 * Output when satisfying conditions.
	 * @param n
	 * @param nDepth
	 */
	private void backVisit(Node n, int nDepth) {
		for(int i = childTreeCount; i >= 1; i--) {
			//move up
			childTree[startDimension + i].moveBack();
		}
		if(childTreeCount > nDepth) {
			//if complete, destroy
			childTreeCount--;
		}
		if(starCount == 0 && n.measure >= minSupport) {
			//no star nodes and satisfy minSupport
			if(nDepth == depth) {
				//leaf
				output(n.measure);
			} else if(nDepth == depth - 1) {
				//make the last value be ALL
				outputBuf[dimensionCount - 1] = ALL;
				output(n.measure);
			}
		}
		if(n.value == STAR)
			starCount--;
	}
	
	/**
	 * Recursive depth first traversal. Called by starCubing() method.
	 * @param n  current node
	 * @param nDepth  depth of current node
	 */
	private void depthFirstTraversal(Node n, int nDepth) {
		if(n == null)
			return;
		//first traversal to Node n
		firstVisit(n, nDepth);
		depthFirstTraversal(n.firstChild, nDepth+1);
		//backtracking
		backVisit(n, nDepth);
		depthFirstTraversal(n.sibling, nDepth);
	}
	
	/**
	 * Initiate a new child tree.
	 * @param outputBuf  output buffer
	 */
	private void init(int[] outputBuf) {
		for(int i = 0; i < startDimension - 1; i++)
			this.outputBuf[i] = outputBuf[i];
		this.outputBuf[startDimension - 1] = ALL;
		currentDepth = -2;
	}
	
	/**
	 * Generate node in this tree
	 * @param n
	 */
	private void generate(Node n) {
		if(currentDepth == -2) {
			//create root
			root = new Node();
			currentDepth = -1;
			currentNode[0] = root;
			starNode[0] = root;
			existNode[0] = root;
			return;
		}
		Node parent = currentDepth == -1 ? null : currentNode[currentDepth];
		currentDepth++;
		
		if(n.value == STAR || currentDepth == 0) {
			//star node or root
			if(starNode[currentDepth] == null) {
				//no star node, add one
				Node star = new Node();
				star.value = n.value;
				star.measure = n.measure;
				star.sibling = parent.firstChild;
				parent.firstChild = star;
				currentNode[currentDepth] = star;
				starNode[currentDepth] = star;
			} else {
				//has star node, combine
				currentNode[currentDepth] = starNode[currentDepth];
				currentNode[currentDepth].measure += n.measure;				
			}
		} else {
			//not a star node
			Node lastNode = null;
			while(existNode[currentDepth] != null && existNode[currentDepth].value < n.value) {
				lastNode = existNode[currentDepth];
				existNode[currentDepth] = lastNode.sibling;
			}
			if(existNode[currentDepth] == null || existNode[currentDepth].value > n.value) {
				//no node with same value, insert one
				Node node = new Node();
				node.value = n.value;
				node.measure = n.measure;
				node.sibling = existNode[currentDepth];
				if(lastNode != null) {
					lastNode.sibling = node;
				} else {
					parent.firstChild = node;
				}
				currentNode[currentDepth] = node;
			} else {
				//has node with same value, combine
				currentNode[currentDepth] = existNode[currentDepth];
				currentNode[currentDepth].measure += n.measure;
			}
			
		}
		
		if(currentDepth > 0) {
			existNode[currentDepth] = parent.firstChild;
		}
		
		if(currentDepth < depth) {
			currentNode[currentDepth + 1] = currentNode[currentDepth].firstChild;
			starNode[currentDepth + 1] = 
					(currentNode[currentDepth + 1] != null && currentNode[currentDepth + 1].value == STAR) ? 
					currentNode[currentDepth + 1] : null;
			existNode[currentDepth + 1] = currentNode[currentDepth].firstChild;
		}				
	}
	
	/**
	 * Move up one step.
	 */
	private void moveBack() {
		currentDepth--;
		if(currentDepth == -2){
			//tree is complete, recursively star-cubing
			starCubing();
		}			
	}
	
	/**
	 * Do star-cubing.
	 * Traversal is depth-first.
	 */
	public void starCubing() {
		childTreeCount = 0;
		starCount = -1;
		depthFirstTraversal(root, 0);
	}
	
	/**
	 * Output one iceberg cell.
	 * @param measure
	 */
	private void output(int measure) {
		dataIO.output(outputBuf, measure);
	}
	
}
