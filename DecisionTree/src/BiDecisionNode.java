import java.util.List;

/**
 * Class for decision node in a binary decision tree
 * @author Jacky
 *
 */
public class BiDecisionNode extends Node {

	/**
	 * attribute name
	 */
	public String attribute;
	
	/**
	 * list of values for the attribute in the yes branch
	 */
	public List<String> attrValues;
	
	/**
	 * child node of yes branch
	 */
	public Node yesNode;
	
	/**
	 * child node of no branch
	 */
	public Node noNode;
	
}
