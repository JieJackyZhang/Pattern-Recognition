import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Class for ID3 Algorithm (Infomation Gain)
 * @author Jacky
 *
 */
public class ID3Algorithm extends Algorithm {
	
	/**
	 * Constructor
	 * @param allAttributeList
	 * @param classLabels
	 */
	public ID3Algorithm(List<String> allAttributeList, Set<String> classLabels) {
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
//		System.out.println("Global entropy = " + globalEntropy);
		
		String bestAttribute = "";
		double maxInfoGain = Double.NEGATIVE_INFINITY;
		// find the best attribute with max info gain
		for(String attribute : attributeList) {
			double infoGain = calcInfoGain(dataset, attribute, globalEntropy);
//			System.out.println("\t" + "Gain_" + attribute + " = " + infoGain);
			if(infoGain > maxInfoGain) {
				maxInfoGain = infoGain;
				bestAttribute = attribute;
			}
		}		
		return bestAttribute;
	}
	
	/**
	 * Calculate info gain for an attribute
	 * @param dataset
	 * @param attribute
	 * @param globalEntropy
	 * @return info gain
	 */
	private double calcInfoGain(List<String[]> dataset, String attribute, double globalEntropy) {
		// count the frequency of each value for the attribute
		int attrIndex = allAttributeList.indexOf(attribute);
		Map<String, Integer> mapValueFrequency = calcFreqOfAttributeValues(dataset, attrIndex);
		// calculate info for that attribute 
		double info = 0;
		// Info_A(D) = sum(|D_j| / |D| * Info(D_j)) 
		for (Entry<String, Integer> entry : mapValueFrequency.entrySet()) {
			info += entry.getValue() / ((double) dataset.size()) * calculateEntropyByValue(dataset, attrIndex, entry.getKey());
		}
		// Gain_A = Info(D) - Info_A(D)
		return globalEntropy - info;
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
