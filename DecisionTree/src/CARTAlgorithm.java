import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class for CART Algorithm (Gini Index)
 * @author Jacky
 *
 */
public class CARTAlgorithm extends Algorithm {
	
	/**
	 * map of values for each attribute
	 */
	private Map<String, Set<String>> mapAttrValue;
	
	/**
	 * map of value subset for each attribute with min Gini Index
	 */
	private Map<String, List<String>> mapAttrSubset;
	
	/**
	 * Constructor
	 * @param allAttributeList
	 * @param classLabels
	 * @param mapAttrValue
	 */
	public CARTAlgorithm(List<String> allAttributeList, Set<String> classLabels, Map<String, Set<String>> mapAttrValue) {
		super(allAttributeList, classLabels);
		this.mapAttrValue = mapAttrValue;
		mapAttrSubset = new HashMap<String, List<String>>();
	}
	
	/**
	 * Get value subset of a given attribute with min Gini Index
	 * @param attribute
	 * @return list of value subset for that attribute
	 */
	public List<String> getSubsetByAttribute(String attribute) {
		return mapAttrSubset.get(attribute);
	}

	/**
	 * Find the best splitting criterion
	 */
	@Override
	public String attributeSelection(List<String[]> dataset, List<String> attributeList, Map<String, Integer> targetFrequency) {
		String bestAttribute = "";
		double minGiniIndex = Double.POSITIVE_INFINITY;
		// find the best attribute with min gini index
		for(String attribute : attributeList) {
			double giniIndex = calcGiniIndex(dataset, attribute);
//			System.out.println("\t" + "GiniIndex_" + attribute + mapAttrSubset.get(attribute).toString() + " = " + giniIndex);
			if(giniIndex < minGiniIndex) {
				minGiniIndex = giniIndex;
				bestAttribute = attribute;
			}
		}		
		return bestAttribute;
	}
	
	/**
	 * Get all subsets for an given array
	 * @param allSubsets
	 * @param prefix
	 * @param array
	 * @param i
	 */
	public static void getSubsets(List<List<String>> allSubsets, List<String> prefix, String[] array, int i) {
		 List<String> subset = new ArrayList<String>();
		 subset.addAll(prefix);
		 subset.add(array[i]);
		 allSubsets.add(subset);
		 i++;
		 if(i < array.length) {
			 getSubsets(allSubsets, prefix, array, i);
			 getSubsets(allSubsets, subset, array, i);
		 }
	}
	
	/**
	 * Calculate gini index for an attribute
	 * Get the min gini index with a binary split
	 * @param dataset
	 * @param attribute
	 * @return gini index
	 */
	private double calcGiniIndex(List<String[]> dataset, String attribute) {
		// get an array of all values for that attribute
		String[] valueArray = mapAttrValue.get(attribute).toArray(new String[0]);
		// get all subsets of the attribute values
		List<List<String>> allSubsets = new ArrayList<List<String>>();
		List<String> prefix = new ArrayList<String>();
		getSubsets(allSubsets, prefix, valueArray, 0);
		// get the best binary split for that attribute with min gini index
		double minGiniIndex = Double.POSITIVE_INFINITY;
		for(int i = 0; i < allSubsets.size(); i++) {
			List<String> subset = allSubsets.get(i);
			if(subset.size() == mapAttrValue.get(attribute).size()) {
				// exclude the power set
				continue;
			}			
			double giniIndex = calcGiniIndexBySubset(dataset, attribute, subset);
			if(giniIndex < minGiniIndex) {
				minGiniIndex = giniIndex;
				mapAttrSubset.put(attribute, subset);
			}
		}
		return minGiniIndex;
	}
	
	/**
	 * Calculate gini index with a given binary split for an attribute
	 * @param dataset
	 * @param attribute
	 * @param subset
	 * @return gini index
	 */
	private double calcGiniIndexBySubset(List<String[]> dataset, String attribute, List<String> subset) {
		// get the index for that attribute
		int attrIndex = allAttributeList.indexOf(attribute);
		// count the number of tuples of two partitions
		int tupleCountD1 = 0;
		int tupleCountD2 = 0;
		// count the frequency of target value of two partitions
		Map<String, Integer> mapTargetFrequencyD1 = new HashMap<String, Integer>();
		Map<String, Integer> mapTargetFrequencyD2 = new HashMap<String, Integer>();		
		for(String[] tuple : dataset) {
			if(subset.contains(tuple[attrIndex])) {
				// if value in subset, add to D1
				String targetValue = tuple[allAttributeList.size()];
				if(mapTargetFrequencyD1.get(targetValue) == null) {
					mapTargetFrequencyD1.put(targetValue, 1);
				} else {
					mapTargetFrequencyD1.put(targetValue, mapTargetFrequencyD1.get(targetValue)+1);
				}
				tupleCountD1++;
			} else {
				// if value not in subset, add to D2
				String targetValue = tuple[allAttributeList.size()];
				if(mapTargetFrequencyD2.get(targetValue) == null) {
					mapTargetFrequencyD2.put(targetValue, 1);
				} else {
					mapTargetFrequencyD2.put(targetValue, mapTargetFrequencyD2.get(targetValue)+1);
				}
				tupleCountD2++;
			}
		}
		
		// Gini(D) = 1 - sum((p_i)^2)
		double giniD1 = 1d;
		for(String targetValue : classLabels) {
			Integer frequency = mapTargetFrequencyD1.get(targetValue);
			if(frequency != null) {
				giniD1 -= Math.pow(frequency / (double) tupleCountD1, 2);
			}
		}
		double giniD2 = 1d;
		for(String targetValue : classLabels) {
			Integer frequency = mapTargetFrequencyD2.get(targetValue);
			if(frequency != null) {
				giniD2 -= Math.pow(frequency / (double) tupleCountD2, 2);
			}
		}
		// Gini_A(D) = |D1| / |D| * Gini(D1) + |D2| / |D| * Gini(D2)
		double giniIndex = (giniD1 * tupleCountD1 + giniD2 * tupleCountD2) / ((double) dataset.size());
//		System.out.println("\t\t" + "GiniIndex_" + attribute + subset.toString() + " = " + giniIndex);
		return giniIndex;
	}

}
