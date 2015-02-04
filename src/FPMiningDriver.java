import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FPMiningDriver {
  public int fs_write ;  //flag whether to write to the file system
  public static ArrayList<String> global_dictionary;   //the global alphabet
  public String directory;                   // the database tree folder
  public String outputDir;                   // the encoded trees folder
  public ArrayList<String> tree_ids;         // the tree ids (names of trees)
  public ArrayList<SQLTree> sqlForest;       // the forest
  public static WLSubtreesHT[] wl_by_heights;  // the output: encoded WLSubtrees 
                         // of different heights
  public int h;          // number of iterations for WL algorithm
  public static int encodingBase = 1000000;
  
  
  public class Tuple {
    String num;
    Integer freq;
    Tuple (String n, Integer f) {
      num = n;
      freq = f;
    }
    String getNum() {
      return num;
    }
    int getFreq() {
      return freq;
    }
  }
  
  public class EncodingComparator implements Comparator<Tuple> {
      public int compare(Tuple o1, Tuple o2) {
        int a = Integer.parseInt(o1.getNum());
        int b = Integer.parseInt(o2.getNum());
        if (a < b) 
          return -1;
        else if (a > b) 
          return 1;
        else
          return 0;
      
      }
  }
  
  FPMiningDriver(int height, int encodingBase) {
    fs_write = 0;
    if (height < 0)
    	h = 3;
    else 
    	h = height;
    
    //if (encodingBase > 1000000 && encodingBase % 1000000 == 0)
    FPMiningDriver.encodingBase = encodingBase;
    
    if (Integer.MAX_VALUE /FPMiningDriver.encodingBase < h) {
    	System.err.println("************************************************************");
    	System.err.println("Encoding will be very likely causing Java Integeroverflow");
    	System.err.println("If you really want the h value and encoding number to be that large.\n I will probably change Integer to long.");
    	System.err.println("Otherwise, reduce the encoding base or height h!");
    	System.err.println("************************************************************");
    	System.exit(1);
    }
    
    global_dictionary = new ArrayList<String>();
    tree_ids = new ArrayList<String>();
    sqlForest = new ArrayList<SQLTree>();
    wl_by_heights = new WLSubtreesHT[h];     // hash table for WL subtrees initialized
    for (int i=0; i<h; i++) {
      wl_by_heights[i] = new WLSubtreesHT();
    }
  }
  
  //get from a file 
  void getTreeIDs(String treeIDFile) {
    String line = new String();
    try {

      // Always wrap FileReader in BufferedReader.
      // File should be under workspace folder name directory
        BufferedReader bufferedReader = 
            new BufferedReader(new FileReader(treeIDFile));

        while((line = bufferedReader.readLine()) != null) {
          tree_ids.add(line);
        }  
        // Always close files.
        bufferedReader.close();      
     }
        catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file '" + 
                    treeIDFile + "'");        
        }
       catch(IOException ex) {
            System.out.println(
                "Error reading file '" 
                + treeIDFile + "'");          
            // Or we could just do this: 
            // ex.printStackTrace();
        }
  }  // end getTreeIDs
  
  //get from a file 
  void getGlobalDictionary(String dictionaryFile) {
    String line = new String();
    try {

            // Always wrap FileReader in BufferedReader.
      // File should be under workspace folder name directory
            BufferedReader bufferedReader = 
                new BufferedReader(new FileReader(dictionaryFile));

            while((line = bufferedReader.readLine()) != null) {
              //To Upper Case!
                global_dictionary.add(line.toUpperCase());
            }  
            
            //System.out.println(global_dictionary.size());
            //System.out.println(global_dictionary);

            // Always close files.
            bufferedReader.close();      
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file '" + 
                dictionaryFile + "'");        
        }
       catch(IOException ex) {
            System.out.println(
                "Error reading file '" 
                + dictionaryFile + "'");          
            // Or we could just do this: 
            // ex.printStackTrace();
        }
  }  // end getGlobalDictionary
  
  // construct the SQL forest
  public void constructSQLForest() {
    for (int i=0; i<tree_ids.size(); i++) {
      SQLTree curSQLTree = new SQLTree();
      try {  
        String line;
        BufferedReader bufferedReader = 
            new BufferedReader( new FileReader(directory + tree_ids.get(i)) );
        
          // Read in Vertices
        line = bufferedReader.readLine();
        // Keeps reading
        while (line != null) {
          String [] cur_vertex_line = line.split(" ", 0);
  
          if (cur_vertex_line[0].contentEquals("t")) {
            // do nothing
          } 
          else if (cur_vertex_line[0].contentEquals("v")) {
            // add the vertex into current SQL Tree
            curSQLTree.addVertex(Integer.parseInt(cur_vertex_line[1]),
                cur_vertex_line[2]);
            
          } 
          else if (cur_vertex_line[0].contentEquals("e")) {
            // add to the current SQL Tree (unlabeled) edges 
            curSQLTree.addEdge(Integer.parseInt(cur_vertex_line[1]),
                               Integer.parseInt(cur_vertex_line[2])  );
          }  
          else  {
            // do nothing
          }
          // next line 
          line = bufferedReader.readLine();
          
        } // end of while                         
        // Always close files.
        bufferedReader.close();      
      } // end of try
      catch(FileNotFoundException ex) {
        System.out.println("Unable to open file");        
      }
      catch(IOException ex) {
        System.out.println("Error reading file");          
      }
  
      // the tree is in memory area curSQLTree 
      System.out.println("processing NO. "+(i+1)+" tree "+tree_ids.get(i));
      // process it or store it to forest
      curSQLTree.markRealLeaves();
      // tree_ids is to avoid weighted counting
      curSQLTree.encoding(this.h, tree_ids.get(i));
      
      if (fs_write > 0) {
      // end of encoding, print out what we get
        // 1. dump to a common data structure 
      // 2. sort the data structure 
      // 3. dump to the file
  
      ArrayList<Tuple> a = new ArrayList<Tuple>();
      for (String encode_num: curSQLTree.localEncodedSubtrees.keySet()) {
        a.add(new Tuple (encode_num, curSQLTree.localEncodedSubtrees.get(encode_num).getFreq() ));          
      }
      Collections.sort(a, new EncodingComparator());
      
      
      // Write to file
      try {
          FileWriter fileWriter = new FileWriter(outputDir+tree_ids.get(i));
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        for (int j=0; j<a.size(); j++) {
          bufferedWriter.write(a.get(j).getNum() + " " + a.get(j).getFreq()+"\n");
        }
        bufferedWriter.close();
        System.out.println("Just dumped NO. "+(i+1)+" tree "+tree_ids.get(i));
        }catch (IOException e) {
          
      }
      
      }  // enable fs write or not
      // store it to forest, seems that there is even no need to do this
      // sqlForest.add(curSQLTree);
    } // end for  
  }

  public void printWLSubtrees() {
    try {
          FileWriter fileWriter = new FileWriter("encoding_details.txt");
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        for (int i=0; i<h; i++) {
          if (wl_by_heights[i].next_number != 0) {
            for (String s: wl_by_heights[i].encodedSubtrees.keySet()) {
              bufferedWriter.write((i+1) + " " 
                             + wl_by_heights[i].getFreqof(s) + " "
                             + (FPMiningDriver.encodingBase*(i+1)+wl_by_heights[i].getCurNumof(s))
                             + " : " 
                             + s + "\n");          
            }
          } // end if  
        } // end for
        bufferedWriter.close();        
        }catch (IOException e) {
          
      }
    
  }
    
  public void printWLSubtreesSupport() {
    for (int i=0; i<h; i++) {
      if (wl_by_heights[i].next_number != 0) {
        for (String s: wl_by_heights[i].encodedSubtrees.keySet()) {
          System.out.println((i+1) + " " 
                         + wl_by_heights[i].getFreqof(s) + " "
                         + wl_by_heights[i].getSupof(s) + " "
                         + (FPMiningDriver.encodingBase*(i+1)+wl_by_heights[i].getCurNumof(s))
                         + " : " 
                         + s);          
        }
      } // end if  
    } // end for
  }
  
  public void printBottomUpSubtrees() {
    for (int i=0; i<h; i++) {
      if (wl_by_heights[i].next_number != 0) {
        for (String s: wl_by_heights[i].encodedSubtrees.keySet()) {
          if (wl_by_heights[i].isBottomUp(s)) {
            System.out.println((i+1) + " " 
                         + wl_by_heights[i].getFreqof(s) + " "
                         + (FPMiningDriver.encodingBase*(i+1)+wl_by_heights[i].getCurNumof(s))
                         + " : " 
                         + s);
          } //end if
        }
      } // end if  
    } // end for
  }

  public void printBottomUpSubtreesSupport() {
    for (int i=0; i<h; i++) {
      if (wl_by_heights[i].next_number != 0) {
        for (String s: wl_by_heights[i].encodedSubtrees.keySet()) {
          if (wl_by_heights[i].isBottomUp(s)) {
            System.out.println((i+1) + " " 
                         + wl_by_heights[i].getFreqof(s) + " "
                         + wl_by_heights[i].getSupof(s) + " "
                         + (FPMiningDriver.encodingBase*(i+1)+wl_by_heights[i].getCurNumof(s))
                         + " : " 
                         + s);
          } //end if
        }
      } // end if  
    } // end for
  }
  
  public void printWLProperty() {
    for (int i=0; i<h; i++) {
      System.out.println((i+1) + " " + wl_by_heights[i].next_number);          
    } 
  }
  
  /** Requires four arguments 
   * @ the Directory that contain LineTrees
   * @ the TreeID file that contain tree names
   * @ the global dictionary file
   * @ the outputDir that contains the encoded_trees
   */
   public static void main(String[] aArgs) throws IOException {

      if (aArgs.length < 6) {
        System.out.println("Need parameters: Directory/ tree_names global_alphabet outputDir height EncodingBase[default:1000000] [0/1]");
        return;
      }
      
      // tree height is stored in aArgs[4]
      FPMiningDriver fsm = new FPMiningDriver(
    		  Integer.parseInt(aArgs[4]), Integer.parseInt(aArgs[5]));
      
      // set three parameters
      fsm.directory = aArgs[0];
      fsm.getTreeIDs(aArgs[1]);
      fsm.getGlobalDictionary(aArgs[2]);
      fsm.outputDir = aArgs[3];
      
      if (aArgs.length > 6)
        fsm.fs_write = Integer.parseInt(aArgs[6]);
      
      File theDir = new File(fsm.outputDir);

        // if the directory does not exist, create it
        if (!theDir.exists()) {
          System.out.println("creating directory: " + theDir);
          boolean result = theDir.mkdir();  
          if(result) {    
             System.out.println("DIR created");  
           }
        }
      
      // begin constructing each tree
      // and do frequent WL-Subtree mining immediately
      // after construction complete  
      fsm.constructSQLForest();
      
      // dump WL Subtrees
      fsm.printWLSubtrees();
      //fsm.printBottomUpSubtrees();
      //fsm.printBottomUpSubtreesSupport();
      fsm.printWLProperty();
  }  // end main method
    
}
