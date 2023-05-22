package Proj;

import java.io.Serializable;
import java.util.Hashtable;

public class Tuple implements Serializable{
    private static final long serialVersionUID = 1L;
    Hashtable<String, Object> row;
String clusteringKey;
    
    public Tuple(String clusteringKey) {
        this.row = new Hashtable<String, Object>();
        this.clusteringKey = clusteringKey;
    }
    
    public Object getId() {
        return  this.row.get(this.clusteringKey);
        
    }
    
    public void setValue(String columnName, Object value) {
        this.row.put(columnName, value);
    }
    
    public Object getValue(String columnName) {
        return this.row.get(columnName);
    }

}