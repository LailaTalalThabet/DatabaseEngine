package Proj;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

public class ValueInNode implements Serializable {
	Hashtable<String, Object> v;
	ArrayList<Object> pk;
	ArrayList<String> r;
	Node node;

	public ValueInNode(Hashtable<String, Object> v, Object p, String ref,Node node) {
		this.v = v;
		pk = new ArrayList<Object>();
		r = new ArrayList<String>();
		pk.add(p);
		r.add(ref);
		this.node=node;
	}

	public void insert(Object p, String ref) {
		pk.add(p);
		r.add(ref);
	}

}
