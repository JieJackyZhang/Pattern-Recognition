/**
 * Class for decision node in a decision tree
 * @author Jacky
 *
 */
public class DecisionNode extends Node {
	/**
	 * attribute name
	 */
	public String attribute;
	/** 
	 * a list of child node 
	 */
	public Node[] nodes;
	/** 
	 * the list of values for the attribute that correspond to the child nodes
	 */
	public String[] attrValues;
}
