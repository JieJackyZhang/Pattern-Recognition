import java.util.List;
import java.util.ArrayList;

/**
 * Class for a node in FP-tree
 * @author Jacky
 */
public class FPNode {
	/**
	 * item of that node
	 */
	public String item;
	
	/**
	 * support of that node
	 */
	public int count;
	
	/**
	 * reference to parent node
	 * null if this is a root
	 */
	public FPNode parent;
	
	/**
	 * reference to children nodes
	 */
	public List<FPNode> children;
	
	/**
	 * link to next node with same item
	 */
	public FPNode nodelink;
	
	/**
	 * Constructor
	 */
	public FPNode() {
		item = null;
		count = 0;
		parent = null;
		children = new ArrayList<FPNode>();
		nodelink = null;
	}
	
	
	/**
	 * Get the child node by given a item
	 * @param item the given item
	 * @return child node with that item, or null if no such node exists
	 */
	public FPNode getChildByItem(String item) {
		for(FPNode child : children) {
			if(child.item.equals(item)) {
				return child;
			}
		}
		return null;
	}
	
	/**
	 * Returns a string representation of FPNode
	 */
	public String toString() {
		return item + ": " + count;
	}
}
