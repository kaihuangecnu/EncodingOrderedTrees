
import java.util.ArrayList;
import java.util.List;

public class Node {
    public String nodeLabel;
    public int local_node_seq_id;  // starts from 0;
    public int flag;
    public List<Integer> children;
 
    public Node(int id, String label) {
    	  setLocalNodeSeqID(id);
        setLabel(label);
        setFlag(flag);
    }
     
    public List<Integer> getChildren() {
        if (this.children == null) {
            return new ArrayList<Integer>();
        }
        return this.children;
    }
    
    public void setChildren(List<Integer> children) {
        this.children = children;
    }
 
    public int getNumberOfChildren() {
        if (children == null) {
            return 0;
        }
        return children.size();
    }
     
    public void addChild(Integer child) {
        if (children == null) {
            children = new ArrayList<Integer>();
        }
        children.add(child);
    }
     
    public void insertChildAt(int index, Integer child) throws IndexOutOfBoundsException {
        if (index == getNumberOfChildren()) {
            // this is really an append
            addChild(child);
            return;
        } else {
            children.get(index); //just to throw the exception, and stop here
            children.add(index, child);
        }
    }
     
    public void removeChildAt(int index) throws IndexOutOfBoundsException {
        children.remove(index);
    }
 
    public String getLabel() {
        return this.nodeLabel;
    }
 
    public void setLabel(String label) {
        this.nodeLabel = label;
    }
    
    public int getFlag() {
    	return this.flag; 
    }
    
    public void setFlag(int flag) {
    	this.flag = flag;
    }
    
    public void setLocalNodeSeqID(int id) {
    	local_node_seq_id = id;
    }
    public int getLocalNodeSeqID() {
    	return local_node_seq_id;
    }
     
//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("{").append(getData().toString()).append(",[");
//        int i = 0;
//        for (Node<T> e : getChildren()) {
//            if (i > 0) {
//                sb.append(",");
//            }
//            sb.append(e.getData().toString());
//            i++;
//        }
//        sb.append("]").append("}");
//        return sb.toString();
//    }
}