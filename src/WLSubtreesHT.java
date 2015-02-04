import java.util.HashMap;

public class WLSubtreesHT {
	int next_number;
	public HashMap<String, Value> encodedSubtrees;
	
	private class Value {
		int cur_number;  // the local number in this Hash Table 
		int frequency;   // count multiple times in a tree
		int support;     // count only once in a tree, this needs Value to include 
										 // the most recent tree id this Value comes from
		boolean bottom_up;   // is bottom-up subtree or not
		String  recent_tree; // the most recent tree Value is from
		
		Value (int c_num, int freq, int sup, boolean bu, String rt ) {
			cur_number = c_num;
			frequency = freq;
			support = sup;
			bottom_up = bu;
			recent_tree = rt;
		}
		public String getRecentTree () {
			return recent_tree;
		}
		
		public void setRecentTree (String rt) {
			recent_tree = rt;
		}
		
		public int getFreq() {
			return frequency;
		}
		public int getSup() {
			return support;
		}
		public void setFreq(int freq) {
			frequency = freq;
		}
		public void setSup (int sup) {
			support = sup;
		}
		public int getCurNumber () {
			return cur_number;
		}
//		public void setCurNumber(int num) {
//			cur_number = num;
//		}
		public boolean getBottomUp () {
			return bottom_up;
		}
		public void setBottomUp (boolean bool) {
			bottom_up = bool;
		}
		
	} // end of Value definition
	
	WLSubtreesHT() {
		encodedSubtrees = new HashMap<String, Value>();
		next_number = 0;
	}
	
	// mark the p_children as bottom-up subtree
	public void markBottomUp (String p_children) {
		Value curValue;
		curValue = encodedSubtrees.get(p_children);
		assert(curValue != null);
		curValue.setBottomUp(true);
	}
	
	public int getLocalIndex(String p_children, String tree_id) {
		// if the p_children string is not in current hash table
  	Value curValue;
  	curValue = encodedSubtrees.get(p_children);
  	if (curValue != null) {   // current hash table contains the string!
  		// update frequency, update recent_tree_id
  		curValue.setFreq(curValue.getFreq() + 1);
  		// update support only if the number now appears on a different tree
  		if (!tree_id.equals(curValue.getRecentTree())) {
  			curValue.setSup(curValue.getSup() + 1);
  			curValue.setRecentTree(tree_id);
  		}
  		return curValue.getCurNumber();
  	}
  	else {
  		curValue = new Value (this.next_number, 1, 1, false, tree_id);
  		encodedSubtrees.put(p_children, curValue);
  		
  		// add error checking
  		if (next_number >= FPMiningDriver.encodingBase) {
  			System.err.println("ERROR!ERROR!ERROR!ERROR!ERROR!ERROR!ERROR!");
  			System.err.println("Unbelievable: encodingbase too small. Multiply encoding base by 10 and try again.");
  			System.err.println("ERROR!ERROR!ERROR!ERROR!ERROR!ERROR!ERROR!");
  			System.exit(1);
  		}
  		return next_number++;
  	}
	}
	
	// the hash table must contain p_children
	public int getFreqof(String p_children) {
		Value curValue = encodedSubtrees.get(p_children);
		return curValue.getFreq();
	}
	public int getSupof(String p_children) {
		Value curValue = encodedSubtrees.get(p_children);
		return curValue.getSup();
	}

	public int getCurNumof(String p_children) {
		Value curValue = encodedSubtrees.get(p_children);
		return curValue.getCurNumber();
	}
	public boolean isBottomUp (String p_children) {
		Value curValue = encodedSubtrees.get(p_children);
		return curValue.getBottomUp();
	}
}
