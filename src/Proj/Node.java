package Proj;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

public class Node implements Serializable {
	boolean isLeaf;
	// ArrayList<Hashtable<String, Object>> data;
	ArrayList<ValueInNode> data;

	// ArrayList<String> ref;
	Hashtable<String, Object> min;
	Hashtable<String, Object> max;
	ArrayList<Node> nodes;
	String x;
	String y;
	String z;
	int maximumVal;

	public Node(Hashtable<String, Object> min, Hashtable<String, Object> max, int maximumVal, String x, String y,
			String z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.maximumVal = maximumVal;
		isLeaf = true;
		this.max = new Hashtable<String, Object>();
		this.min = new Hashtable<String, Object>();
		this.max = max;
		this.min = min;
		this.data = new ArrayList<ValueInNode>();
		// this.ref = new ArrayList<String>();
		this.nodes = new ArrayList<Node>();

		nodes.add(null);
		nodes.add(null);
		nodes.add(null);
		nodes.add(null);
		nodes.add(null);
		nodes.add(null);
		nodes.add(null);
		nodes.add(null);

	}

	public boolean o1Smaller(Object o1, Object o2) {
		if (o1 instanceof Integer || o1 instanceof Double) {
			return ((int) o1 <= (int) o2);
		} else if (o1 instanceof Date) {
			if (((Date) o1).compareTo((Date) o1) <= 0) {
				return true;
			} else
				return false;
		} else {
			if (((String) o1).compareTo((String) o1) <= 0) {
				return true;
			} else
				return false;
		}
	}

	static String printMiddleString(String S, String T, int N) {
		// Stores the base 26 digits after addition
		int[] a1 = new int[N + 1];

		for (int i = 0; i < N; i++) {
			a1[i + 1] = (int) S.charAt(i) - 97 + (int) T.charAt(i) - 97;
		}

		// Iterate from right to left
		// and add carry to next position
		for (int i = N; i >= 1; i--) {
			a1[i - 1] += (int) a1[i] / 26;
			a1[i] %= 26;
		}

		// Reduce the number to find the middle
		// string by dividing each position by 2
		for (int i = 0; i <= N; i++) {

			// If current value is odd,
			// carry 26 to the next index value
			if ((a1[i] & 1) != 0) {

				if (i + 1 <= N) {
					a1[i + 1] += 26;
				}
			}

			a1[i] = (int) a1[i] / 2;
		}
		String s = "";

		for (int i = 1; i <= N; i++) {
			s = s + (char) (a1[i] + 97);
		}
		return s;
	}

	public void insertIntoNode(Hashtable<String, Object> value, String r, Object p) {

		if (isLeaf) {
			for (ValueInNode d : this.data) {
				if (areHashtablesEqual(value, d.v)) {
					d.insert(p, r);
					return;

				}
			}
			if (data.size() < maximumVal) {
				ValueInNode x = new ValueInNode(value, p, r, this);
				data.add(x);
			} else {
				ValueInNode vin = new ValueInNode(value, p, r, this);
				data.add(vin);
				Hashtable<String, Object> nos = new Hashtable<String, Object>();
				for (String key : value.keySet()) {
					if (value.get(key) instanceof Date) {
						long averageTime = (((Date) (min.get(key))).getTime() + (((Date) (max.get(key))).getTime()))
								/ 2;
						Date middleDate = new Date(averageTime);
						nos.put(key, middleDate);
					} else if (value.get(key) instanceof String) {
						String one = (String) min.get(key);
						String two = (String) max.get(key);
						if (one.length() < two.length()) {
							one = one + two.substring(one.length(), two.length());
						} else {
							two = two + one.substring(two.length(), one.length());
						}
						nos.put(key, printMiddleString(one, two, one.length()));

					} else if (value.get(key) instanceof Double) {
						Double mid = (((Double) max.get(key)) - ((Double) min.get(key))) / 2;
						nos.put(key, mid);
					} else {
						int mid = (((int) max.get(key)) - ((int) min.get(key))) / 2;
						// System.out.print((mid));
						nos.put(key, mid);
					}
				}
				// System.out.println("in insert");
				this.isLeaf = false;
				nodes.add(0, new Node(this.min, nos, this.maximumVal, x, y, z));
				Hashtable<String, Object> start = new Hashtable<String, Object>();
				Hashtable<String, Object> end = new Hashtable<String, Object>();
				start.put(x, nos.get(x));
				start.put(y, min.get(y));
				start.put(z, min.get(z));
				end.put(x, max.get(x));
				end.put(y, nos.get(y));
				end.put(z, nos.get(z));
				nodes.add(1, new Node(start, end, this.maximumVal, x, y, z));
				Hashtable<String, Object> start1 = new Hashtable<String, Object>();
				Hashtable<String, Object> end1 = new Hashtable<String, Object>();
				start1.put(x, min.get(x));
				start1.put(y, nos.get(y));
				end1.put(x, nos.get(x));
				end1.put(y, max.get(y));
				end1.put(z, nos.get(z));
				start1.put(z, min.get(z));
				nodes.add(2, new Node(start1, end1, this.maximumVal, x, y, z));
				Hashtable<String, Object> start2 = new Hashtable<String, Object>();
				Hashtable<String, Object> end2 = new Hashtable<String, Object>();
				start2.put(x, nos.get(x));
				end2.put(x, max.get(x));
				end2.put(y, max.get(y));
				end2.put(z, nos.get(z));
				start2.put(z, min.get(z));
				start2.put(y, nos.get(y));
				nodes.add(3, new Node(start2, end2, this.maximumVal, x, y, z));
				Hashtable<String, Object> start3 = new Hashtable<String, Object>();
				Hashtable<String, Object> end3 = new Hashtable<String, Object>();
				start3.put(x, min.get(x));
				start3.put(y, min.get(y));
				start3.put(z, nos.get(z));
				end3.put(x, nos.get(x));
				end3.put(y, nos.get(y));
				end3.put(z, max.get(z));
				nodes.add(4, new Node(start3, end3, this.maximumVal, x, y, z));
				Hashtable<String, Object> start4 = new Hashtable<String, Object>();
				Hashtable<String, Object> end4 = new Hashtable<String, Object>();
				start4.put(x, nos.get(x));
				end4.put(x, max.get(x));
				start4.put(y, min.get(y));
				start4.put(z, nos.get(z));
				end4.put(y, nos.get(y));
				end4.put(z, max.get(z));
				nodes.add(5, new Node(start4, end4, this.maximumVal, x, y, z));
				Hashtable<String, Object> start5 = new Hashtable<String, Object>();
				Hashtable<String, Object> end5 = new Hashtable<String, Object>();
				start5.put(x, min.get(x));
				start5.put(y, nos.get(y));
				end5.put(x, nos.get(x));
				end5.put(y, max.get(y));
				start5.put(z, nos.get(z));
				end5.put(z, max.get(z));
				nodes.add(6, new Node(start5, end5, this.maximumVal, x, y, z));
				Hashtable<String, Object> start6 = new Hashtable<String, Object>();
				Hashtable<String, Object> end6 = new Hashtable<String, Object>();
				start6.put(x, nos.get(x));
				end6.put(x, max.get(x));
				start6.put(y, nos.get(y));
				end6.put(y, max.get(y));
				start6.put(z, nos.get(z));
				end6.put(z, max.get(z));
				nodes.add(7, new Node(start6, end6, this.maximumVal, x, y, z));

				for (int i = 0; i < data.size(); i++) {
					ValueInNode v = data.get(i);
					if (this.o1Smaller(v.v.get(x), nos.get(x))) {
						if (this.o1Smaller(v.v.get(y), nos.get(y))) {
							if (this.o1Smaller(v.v.get(z), nos.get(z))) {
								nodes.get(0).data.add(v);

							} else {
								nodes.get(4).data.add(v);
							}
						} else {
							if (this.o1Smaller(v.v.get(z), nos.get(z))) {
								nodes.get(2).data.add(v);

							} else {
								nodes.get(6).data.add(v);

							}
						}
					} else {
						if (this.o1Smaller(v.v.get(y), nos.get(y))) {
							if (this.o1Smaller(v.v.get(z), nos.get(z))) {
								nodes.get(1).data.add(v);

							} else {
								nodes.get(5).data.add(v);

							}
						} else {
							if (this.o1Smaller(v.v.get(z), nos.get(z))) {
								nodes.get(3).data.add(v);

							} else {
								nodes.get(7).data.add(v);
							}
						}
					}
				}
			}

		}

		else {
			(this.whichChild(value)).insertIntoNode(value, r, p);
		}
	}

	public Node whichChild(Hashtable<String, Object> v) {

		for (int i = 0; i < this.nodes.size(); i++) {
			boolean flag = o1Smaller((this.nodes.get(i)).min.get(this.x), v.get(this.x));
			boolean flag2 = o1Smaller((this.nodes.get(i)).min.get(this.y), v.get(this.y));
			boolean flag3 = o1Smaller((this.nodes.get(i)).min.get(this.z), v.get(this.z));
			boolean flag4 = o1Smaller(v.get(this.x), (this.nodes.get(i)).max.get(this.x));
			boolean flag5 = o1Smaller(v.get(this.y), (this.nodes.get(i)).max.get(this.y));
			boolean flag6 = o1Smaller(v.get(this.z), (this.nodes.get(i)).max.get(this.z));
			if (flag && flag2 && flag3 && flag4 && flag5 && flag6) {

				// System.out.print(i);
				return this.nodes.get(i);
			}
		}
		return null;

	}

	public static boolean areHashtablesEqual(Hashtable<String, Object> hash1, Hashtable<String, Object> Hash2) {

		boolean equal = true;
		for (String key : hash1.keySet()) {
			if (!Hash2.containsKey(key) || !Hash2.get(key).equals(hash1.get(key))) {
				equal = false;
				break;
			}
		}
		return equal;
	}

	public boolean o1Smallerbas(Object o1, Object o2) {
		if (o1 instanceof Integer || o1 instanceof Double) {
			return ((int) o1 < (int) o2);
		} else if (o1 instanceof Date) {
			if (((Date) o1).compareTo((Date) o1) < 0) {
				return true;
			} else
				return false;
		} else {
			if (((String) o1).compareTo((String) o1) < 0) {
				return true;
			} else
				return false;
		}
	}

	public ValueInNode searchValue(Hashtable<String, Object> v) {
		ArrayList<String> s = new ArrayList<String>();
		boolean flag = o1Smaller(this.min.get(this.x), v.get(this.x));
		boolean flag2 = o1Smaller(this.min.get(this.y), v.get(this.y));
		boolean flag3 = o1Smaller(this.min.get(this.z), v.get(this.z));
		boolean flag4 = o1Smallerbas(v.get(this.x), this.max.get(this.x));
		boolean flag5 = o1Smallerbas(v.get(this.y), this.max.get(this.y));
		boolean flag6 = o1Smallerbas(v.get(this.z), this.max.get(this.z));
		System.out.print(this.isLeaf);

		if (this.isLeaf) {
			if (flag && flag2 && flag3 && flag4 && flag5 && flag6) {
				for (int i = 0; i < this.data.size(); i++) {

					Hashtable<String, Object> a = new Hashtable<String, Object>();
					Hashtable<String, Object> b = new Hashtable<String, Object>();

					a = (Hashtable<String, Object>) v.clone();
					b = (Hashtable<String, Object>) data.get(i).v.clone();

					if (areHashtablesEqual(a, b)) {
						// System.out.print(this.data.get(i).toString());
						return this.data.get(i);
					}
				}

			}
		} else {
			// System.out.print("ana");
			this.whichChild(v).searchValue(v);

		}
		return null;

	}

	public void deleteSpecific(Hashtable<String, Object> v, Object p, String r) {
		boolean flag = o1Smaller(this.min.get(this.x), v.get(this.x));
		boolean flag2 = o1Smaller(this.min.get(this.y), v.get(this.y));
		boolean flag3 = o1Smaller(this.min.get(this.z), v.get(this.z));
		boolean flag4 = o1Smaller(v.get(this.x), this.max.get(this.x));
		boolean flag5 = o1Smaller(v.get(this.y), this.max.get(this.y));
		boolean flag6 = o1Smaller(v.get(this.z), this.max.get(this.z));

		if (this.isLeaf) {
			if (flag && flag2 && flag3 && flag4 && flag5 && flag6) {

				for (int i = 0; i < this.data.size(); i++) {

					if (areHashtablesEqual(v, data.get(i).v)) {

						for (int j = 0; j < data.get(i).pk.size(); j++) {

							if (data.get(i).pk.get(j) instanceof Integer) {
								if ((int) data.get(i).pk.get(j) == (int) p) {
									data.get(i).pk.remove(j);
									data.get(i).r.remove(j);
									break;

								}
							} else if (data.get(i).pk.get(j) instanceof Double) {
								if ((Double) data.get(i).pk.get(j) == (Double) p) {
									System.out.print(p);
									data.get(i).pk.remove(j);
									data.get(i).r.remove(j);
									break;

								}
							} else if (data.get(i).pk.get(j) instanceof Date) {
								if (((Date) data.get(i).pk.get(j)).equals((Date) p)) {
									System.out.print(p);
									data.get(i).pk.remove(j);
									data.get(i).r.remove(j);
									break;

								}
							} else {
								if (((String) data.get(i).pk.get(j)).equals((String) p)) {
									System.out.print(p);
									data.get(i).pk.remove(j);
									data.get(i).r.remove(j);
									break;

								}
							}

							if (data.get(i).pk.isEmpty()) {
								data.remove(i);
								break;
							}
						}

					}
				}

			}
		} else {
			this.whichChild(v).deleteSpecific(v, p, r);
		}

	}

	public ArrayList<Node> selectHelper(SQLTerm[] arrSQLTerms, ArrayList<Node> k) {
		boolean f = true;
		ArrayList<Node> nn = new ArrayList<Node>();
		if(this.isLeaf) {
			for (Node n : this.nodes) {
				for (SQLTerm t : arrSQLTerms) {
					if (t._objValue instanceof Integer) {
						if (t._strOperator.equals("=") && (int) n.min.get(t._strColumnName) > (int) t._objValue
								|| (int) n.max.get(t._strColumnName) < (int) t._objValue) {
							f = false;
						}
						if (t._strOperator.equals("<") 
								&& (int) n.min.get(t._strColumnName) >= (int) t._objValue) {
							f = false;
						}
						if (t._strOperator.equals(">") &&(int) n.max.get(t._strColumnName) <= (int) t._objValue) {
							f = false;
						}
						
						if (t._strOperator.equals("!=") && (int) n.min.get(t._strColumnName) < (int) t._objValue
								&& (int) n.max.get(t._strColumnName) > (int) t._objValue) {
							f = false;
						}
						if (t._strOperator.equals(">=") &&(int) n.max.get(t._strColumnName) < (int) t._objValue) {
							f = false;
						}
						if (t._strOperator.equals("<=") 
								&& (int) n.min.get(t._strColumnName) > (int) t._objValue) {
							f = false;
						}
						
						
					}
					else if(t._objValue instanceof Double) {
						if (t._strOperator.equals("=") && (Double) n.min.get(t._strColumnName) > (Double) t._objValue
								|| (Double) n.max.get(t._strColumnName) < (Double) t._objValue) {
							f = false;
						}
						if (t._strOperator.equals("<") 
								&& (Double) n.min.get(t._strColumnName) >= (Double) t._objValue) {
							f = false;
						}
						if (t._strOperator.equals(">") &&(Double) n.max.get(t._strColumnName) <= (Double) t._objValue) {
							f = false;
						}
						
						if (t._strOperator.equals("!=") && (Double) n.min.get(t._strColumnName) < (Double) t._objValue
								&& (Double) n.max.get(t._strColumnName) > (Double) t._objValue) {
							f = false;
						}
						if (t._strOperator.equals(">=") &&(Double) n.max.get(t._strColumnName) < (Double) t._objValue) {
							f = false;
						}
						if (t._strOperator.equals("<=") 
								&& (Double) n.min.get(t._strColumnName) > (Double) t._objValue) {
							f = false;
						}
					}
					else if (t._objValue instanceof String) {
						if (t._strOperator.equals("=") && ((String) n.min.get(t._strColumnName)).compareTo((String) t._objValue
								) >0|| ((String) n.max.get(t._strColumnName)).compareTo((String) t._objValue)<0) {
							f = false;
						}
						if (t._strOperator.equals("<") 
								&& ((String) n.min.get(t._strColumnName)).compareTo((String) t._objValue
										) >=0) {
							f = false;
						}
						if (t._strOperator.equals(">")&& ((String) n.max.get(t._strColumnName)).compareTo((String) t._objValue)<=0) {
							f = false;
						}
						
						if (t._strOperator.equals("!=") && ((String) n.min.get(t._strColumnName)).compareTo((String) t._objValue
								) <0|| ((String) n.max.get(t._strColumnName)).compareTo((String) t._objValue)>0) {
							f = false;
						}
						if (t._strOperator.equals(">=") && ((String) n.max.get(t._strColumnName)).compareTo((String) t._objValue
								) <0) {
							f = false;
						}
						if (t._strOperator.equals("<=") 
								&& ((String) n.min.get(t._strColumnName)).compareTo((String) t._objValue
										) >0) {
							f = false;
						}
					}
					else {
						if (t._strOperator.equals("=") && ((Date) n.min.get(t._strColumnName)).compareTo((Date) t._objValue
								) >0|| ((Date) n.max.get(t._strColumnName)).compareTo((Date) t._objValue)<0) {
							f = false;
						}
						if (t._strOperator.equals("<") 
								&& ((Date) n.min.get(t._strColumnName)).compareTo((Date) t._objValue
										) >=0) {
							f = false;
						}
						if (t._strOperator.equals(">")&& ((Date) n.max.get(t._strColumnName)).compareTo((Date) t._objValue)<=0) {
							f = false;
						}
						
						if (t._strOperator.equals("!=") && ((Date) n.min.get(t._strColumnName)).compareTo((Date) t._objValue
								) <0|| ((Date) n.max.get(t._strColumnName)).compareTo((Date) t._objValue)>0) {
							f = false;
						}
						if (t._strOperator.equals(">=") && ((Date) n.max.get(t._strColumnName)).compareTo((Date) t._objValue
								) <0) {
							f = false;
						}
						if (t._strOperator.equals("<=") 
								&& ((Date) n.min.get(t._strColumnName)).compareTo((Date) t._objValue
										) >0) {
							f = false;
						}
					}

				}
				if (f)
					nn.add(n);
			}
		}

		
		for (Node n : nn) {
			if (n.isLeaf)
				k.add(n);
			else {
				
				n.selectHelper(arrSQLTerms, k);
			}
		}
		return k;

	}

	public ArrayList<String> delete(Hashtable<String, Object> v) {
		ArrayList<String> result = new ArrayList<String>();
		boolean flag = o1Smaller(this.min.get(this.x), v.get(this.x));
		boolean flag2 = o1Smaller(this.min.get(this.y), v.get(this.y));
		boolean flag3 = o1Smaller(this.min.get(this.z), v.get(this.z));
		boolean flag4 = o1Smaller(v.get(this.x), this.max.get(this.x));
		boolean flag5 = o1Smaller(v.get(this.y), this.max.get(this.y));
		boolean flag6 = o1Smaller(v.get(this.z), this.max.get(this.z));

		if (this.isLeaf) {
			if (flag && flag2 && flag3 && flag4 && flag5 && flag6) {

				for (int i = 0; i < this.data.size(); i++) {

					if (areHashtablesEqual(v, data.get(i).v)) {

						for (String s : data.get(i).r) {
							result.add(s);
						}
						data.remove(i);
						// System.out.print(data.toString());
						return result;
						// i--;
					}
				}

			}
		} else {

			return this.whichChild(v).delete(v);
		}
		return null;

	}
}
