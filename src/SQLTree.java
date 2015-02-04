import java.util.ArrayList;
import java.util.HashMap;


public class SQLTree {
  public ArrayList<Node> vertexArray; 
  public HashMap<String, Frequency> localEncodedSubtrees; // Encoded number as key, freq as value

  public class Frequency {
    Integer frequency;
    
    Frequency() {
      frequency = 1;
    }
    
    public int getFreq() {
      return frequency;
    }
    public void setFreq(int freq) {
      frequency = freq;
    }
  }
  
  
  
  SQLTree() {
    vertexArray = new ArrayList<Node>();
    localEncodedSubtrees = new HashMap<String, Frequency>();
  }
  
  public void addVertex(int local_node_id, String node_label) {
    Node curVertex = new Node(local_node_id, node_label);
    vertexArray.add(curVertex);
  }  
  
  public void addEdge(Integer parent_local_id, Integer child_local_id) {
    vertexArray.get(parent_local_id).addChild(child_local_id);
  }
  
  public void recordFreq(String encode_num) {
     Frequency curFreq;
      curFreq = localEncodedSubtrees.get(encode_num);
      if (curFreq != null) {   // current hash table contains the string!
        // update frequency, update recent_tree_id
        curFreq.setFreq(curFreq.getFreq() + 1);
  
      }
      else {
        curFreq = new Frequency();
        localEncodedSubtrees.put(encode_num, curFreq);
      }

  }
  
  public void markRealLeaves() {
    for (Node cur_vertex: vertexArray) {
      if (cur_vertex.getNumberOfChildren() == 0) {
        cur_vertex.setFlag(0);
      }
      else {
        cur_vertex.setFlag(1);
      } // end if
    } // end for
  }
  
  // initialize the Real Label to Numbers to facilitate 
  private void mapRealLabelToNumber() {
    for (Node cur_vertex: vertexArray) {
      // the Number goes from 1 so that 0 (-1) is a better choince than -1 to 
      // represent word dosn't from dictionary
      cur_vertex.setLabel(Integer.toString
        (FPMiningDriver.
           global_dictionary.indexOf(cur_vertex.getLabel()) + 1));
      recordFreq(cur_vertex.getLabel() );
    }
  }
  
  // the relabeling process! Deal with Hash Table!
  private void assignNewLabel(int cur_h, String tree_id) {
    int new_flag;
    for (Node curVertex: vertexArray) {     // O(|V_D|)
      if (curVertex.getFlag() == 1) {
  //      System.out.println(curVertex.getLabel());
        StringBuilder p_children = new StringBuilder();
        new_flag = 0;
        p_children.append(curVertex.getLabel());

        // Consider this line as if you want to print out the neighbors of all 
	// vertices, what is the time complexity? O(2*|V_D|) = O(|V_D|)
        for (Integer child : curVertex.getChildren()) { 
          new_flag += vertexArray.get(child).getFlag() ; 
          p_children.append(" "+vertexArray.get(child).getLabel());
        } // end for child
        
        curVertex.setFlag(new_flag > 0 ? 1 : 0);
                // get new label and use Hash Table
        //!!!!!!!!!!!!!!!!!!!!!Pay attention to overflow 
        // if overflows, can try to use Long instead 
        curVertex.setLabel(Integer.toString( FPMiningDriver.encodingBase*cur_h  
                            + FPMiningDriver.wl_by_heights[cur_h-1].
                              getLocalIndex(p_children.toString(), tree_id) ));
        recordFreq(curVertex.getLabel());   // add to local tree
        // mark as bottom-up subtree
        if (new_flag == 0) {
          FPMiningDriver.wl_by_heights[cur_h-1].markBottomUp(p_children.toString());
        }        

      } // end if getFlag()
    } // end for
    
  } // end assignNewLabel()
  
  // start the Main Part of Frequent WL Subtree Mining
  // @param: H is the maximum number of iterations
  public void encoding(int H, String tree_id) {
    int cur_h = 0;
    
    mapRealLabelToNumber();
    
    cur_h = 1;
    while (cur_h <= H) {
    //  System.out.println("h is:"+cur_h);
      assignNewLabel(cur_h, tree_id);
      cur_h++;
    }  // end while
   } // end encoding method
  
}
