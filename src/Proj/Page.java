package Proj;

import java.io.*;
import java.util.*;

public class Page implements Serializable {
    private static final long serialVersionUID = 1L;
    public  Vector<Tuple> tuples;
    int pid;
    final int MaxRows;
    int pageNum;
    String tableName;
    String Clustering;
    Table t;
    public Page(int Rows,int pageNum,String Clustering,Table t) throws IOException {
    	
       tuples=new Vector<Tuple>();
       this.MaxRows=Rows;
       this.pageNum=pageNum;
       this.Clustering=Clustering;
       this.t=t;
       
       tableName=t.name;
    }
    public boolean isFull() {
    	if(tuples.size()==MaxRows) {
    		return true;
    	}
    	else {
    		return false;
    		}
    }

}
