import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Class for C4.5 Algorithm (Gain Ratio)
 * @author Jacky
 *
 */
public class C45Algorithm extends Algorithm {
	
	/**
	 * Constructor
	 * @param allAttributeList
	 * @param classLabels
	 */
	public C45Algorithm(List<String> allAttributeList, Set<String> classLabels) {
		super(allAttributeList, classLabels);
	}

	/**
	 * Find the best splitting criterion
	 */
	@Override
	public String attributeSelection(List<String[]> dataset, List<String> attributeList, Map<String, Integer> targetFrequency) {
		// calculate global entropy
		// Info(D) = - sum(p_i*log2(p_i))
		double globalEntropy = 0d;
		for(String value : classLabels) {
			Integer frequency = targetFrequency.get(value);
			if(frequency != null) {
				// if frequency is not 0
				double p = frequency / (double) dataset.size();
				globalEntropy -= nlog2n(p);
			}
		}
		
		String bestAttribute = "";
		double maxGainRatio = Double.NEGATIVE_INFINITY;
		// find the best attribute with max gain ratio
		for(String attribute : attributeList) {
			double gainRatio = calcGainRatio(dataset, attribute, globalEntropy);
//			System.out.println("\tGainRatio_" + attribute + " = " + gainRatio);
			if(gainRatio > maxGainRatio) {
				maxGainRatio = gainRatio;
				bestAttribute = attribute;
			}
		}		
		return bestAttribute;
	}
	
	/**
	 * Calculate gain ratio for an attribute
	 * @param dataset
	 * @param attribute
	 * @param globalEntropy
	 * @return  gain ratio
	 */
	private double calcGainRatio(List<String[]> dataset, String attribute, double globalEntropy) {
		// count the frequency of each value for the attribute
		int attrIndex = allAttributeList.indexOf(attribute);
		Map<String, Integer> mapValueFrequency = calcFreqOfAttributeValues(dataset, attrIndex);
		// calculate info and split info for that attribute 
		double info = 0;
		double splitInfo = 0;
		// Info_A(D) = sum(|D_j| / |D| * Info(D_j)) 
		// SplitInfo_A(D) = - sum(|D_j| / |D| * log2(D_j| / |D|))
		for (Entry<String, Integer> entry : mapValueFrequency.entrySet()) {
			double weight = entry.getValue() / ((double) dataset.size());
			info += weight * calculateEntropyByValue(dataset, attrIndex, entry.getKey());
			splitInfo -= nlog2n(weight);
		}
//		System.out.println("\tSplitInfo_" + attribute + " = " + splitInfo);
		if(splitInfo == 0) {
			// if split information is 0, return Inf
			return Double.POSITIVE_INFINITY;
		}
		// Gain_A = Info(D) - Info_A(D)
		// GainRatio_A = Gain_A / SplitInfo_A(D)
		return  (globalEntropy - info) / splitInfo;
	}
	
	/**
	 * Calculate entropy of a given value of an attribute, Info(D_j)
	 * @param dataset
	 * @param attrIndex
	 * @param value
	 * @return info
	 */
	private double calculateEntropyByValue(List<String[]> dataset, int attrIndex, String value) {
		// count the number of tuples containing that value
		int tupleCount = 0;
		// count the frequency of target value
		Map<String, Integer> mapTargetFrequency = new HashMap<String, Integer>();
		
		for(String[] tuple : dataset) {
			if(tuple[attrIndex].equals(value)) {
				// if tuple contains that value
				// count the frequency of each target value
				String targetValue = tuple[allAttributeList.size()];
				if(mapTargetFrequency.get(targetValue) == null) {
					mapTargetFrequency.put(targetValue, 1);
				} else {
					mapTargetFrequency.put(targetValue, mapTargetFrequency.get(targetValue)+1);
				}
				tupleCount++;
			}
		}
		
		// calculate Info(D_j)
		// Info(D_j) = - sum(p_i*log2(p_i))
		double entropy = 0d;
		for(String targetValue : classLabels) {
			Integer frequency = mapTargetFrequency.get(targetValue);
			if(frequency != null) {
				double p = frequency / (double) tupleCount;
				entropy -= nlog2n(p);
			}
		}
		return entropy;
	}

}
