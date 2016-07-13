import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class for algorithm
 * An acstract class with an abstract method attributeSelection()
 * @author Jacky
 *
 */
public abstract class Algorithm {
	
	/**
	 * list of all attributes
	 */
	protected List<String> allAttributeList;
	
	/**
	 * set of class labels (categories of target attribute)
	 */
	protected Set<String> classLabels;
	
	/**
	 * Constructor
	 * @param allAttributeList
	 * @param classLabels
	 */
	public Algorithm(List<String> allAttributeList, Set<String> classLabels) {
		this.allAttributeList = allAttributeList;
		this.classLabels = classLabels;
	}
	
	/**
	 * Find the best splitting criterion
	 * @param dataset
	 * @param attributeList
	 * @param targetFrequency  frequency of each class label in dataset
	 * @return String attribute
	 */
	abstract public String attributeSelection(List<String[]> dataset, List<String> attributeList, Map<String, Integer> targetFrequency);
	
	/**
	 * Calculate frequency of each value for an attribute in a given dataset
	 * @param dataset
	 * @param attrIndex  index of attribute
	 * @return a map where keys are values of an attribute, values are frequency
	 */
	public Map<String, Integer> calcFreqOfAttributeValues(List<String[]> dataset, int attrIndex) {
		Map<String, Integer> mapAttrFreq = new HashMap<String, Integer>();
		for(String[] tuple : dataset) {
			//get value of the attribute for that tuple
			String value = tuple[attrIndex];
			if(mapAttrFreq.get(value) == null) {
				//if not exist, create
				mapAttrFreq.put(value, 1);
			} else {
				//if exist, add frequency by 1
				mapAttrFreq.put(value, mapAttrFreq.get(value)+1);
			}
		}
		return mapAttrFreq;
	}
	
	/**
	 * Calculate n*log2(n) for a given number n
	 * @param n
	 * @return double n*log2(n)
	 */
	protected double nlog2n(double n) {
		if ( n == 0 )
			return 0;
	    return n * Math.log(n) / Math.log(2);
	}
			
}
