package Proj;

import java.awt.Point;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Octree implements Serializable {
	Node root;
	String name;
	Hashtable<String, Object> minOfCol;
	Hashtable<String, Object> maxOfCol;
	Hashtable<String, String> type;
	String table;
	ArrayList<String> cols;
	int N ;

	public Octree(String[] col, String table) throws IOException, ClassNotFoundException, ParseException {
		// this method updates the csv
		// it serializes the octree after it creates it
		Properties props = new Properties();
		String currentDir = System.getProperty("user.dir");
		FileInputStream fis = new FileInputStream(currentDir + "//DBApp.config");
		props.load(fis);
		String N = props.getProperty("MaximumEntriesinOctreeNode");
		this.N = Integer.parseInt(N);
		this.root = null;
		this.name = col[0] + col[1] + col[2] + "Index";
		cols = new ArrayList<String>();
		for (String c : col) {
			this.cols.add(c);
		}
		this.table = table;

		FileInputStream currentPageFileIn = new FileInputStream(currentDir + "\\" + table + ".class");
		ObjectInputStream currentPageObjectIn = new ObjectInputStream(currentPageFileIn);
		Table t = (Table) currentPageObjectIn.readObject();
		t.octrees.add(this.name);
		currentPageObjectIn.close();
		currentPageFileIn.close();
		FileOutputStream fileOut6 = new FileOutputStream(currentDir + "\\" + table + ".class");
		ObjectOutputStream out6 = new ObjectOutputStream(fileOut6);
		out6.writeObject(t);
		out6.close();
		fileOut6.close();

		minOfCol = new Hashtable<String, Object>();
		maxOfCol = new Hashtable<String, Object>();
		type = new Hashtable<String, String>();

		for (String key : t.colNameType.keySet()) {
			for (String col2 : cols) {
				if (col2.equals(key)) {
					type.put(key, t.colNameType.get(key));
				}
			}
		}
		for (String key : t.colNameMax.keySet()) {
			for (String col2 : cols) {
				if (col2.equals(key)) {
					if ((type.get(key)).equals("java.lang.Integer")) {
						maxOfCol.put(key, Integer.parseInt(t.colNameMax.get(key)));
					} else if ((type.get(key)).equals("java.lang.Double")) {
						maxOfCol.put(key, Double.parseDouble(t.colNameMax.get(key)));
					} else if ((type.get(key)).equals("java.util.Date")) {
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
						maxOfCol.put(key, dateFormat.parse(t.colNameMax.get(key)));
					} else {
						maxOfCol.put(key, t.colNameMax.get(key));
					}

				}
			}
		}

		for (String key : t.colNameMin.keySet()) {

			for (String col2 : cols) {
				if (col2.equals(key)) {
					if ((type.get(key)).equals("java.lang.Integer")) {
						minOfCol.put(key, Integer.parseInt(t.colNameMin.get(key)));
					} else if ((type.get(key)).equals("java.lang.Double")) {
						minOfCol.put(key, Double.parseDouble(t.colNameMin.get(key)));
					} else if ((type.get(key)).equals("java.util.Date")) {
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
						minOfCol.put(key, dateFormat.parse(t.colNameMin.get(key)));
					} else {
						minOfCol.put(key, t.colNameMin.get(key));
					}

				}
			}

		}
		FileOutputStream fileOut = new FileOutputStream(currentDir + "\\" + name + ".class");
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(this);
		out.close();
		fileOut.close();

		this.updateCSV(t);

	}

	public void updateCSV(Table t) {
		String currentDir = System.getProperty("user.dir");
		String csvFile = "meta-data.csv";
		String csvSplitBy = ",";

		try (BufferedReader br = new BufferedReader(new FileReader(currentDir + "\\" + csvFile))) {
			List<String[]> rows = new ArrayList<>();
			String line;

			while ((line = br.readLine()) != null) {
				String[] fields = line.split(csvSplitBy);
				rows.add(fields);
			}

			// Convert the list to a 2D array
			String[][] data = new String[rows.size()][];
			rows.toArray(data);

			// Access and manipulate the data as needed
			for (String[] row : data) {
				if (row[0].equals(t.name)
						&& (row[1].equals(cols.get(0)) || row[1].equals(cols.get(1)) || row[1].equals(cols.get(2)))) {
					row[4] = this.name;
					row[5] = "Octree";
				}
			}

			try (PrintWriter writer = new PrintWriter(new FileWriter(csvFile))) {
				for (String[] row : data) {
					String line3 = String.join(",", row);
					writer.println(line3);
				}

				// System.out.println("CSV file successfully written.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void insert(Hashtable<String, Object> value, String r, Object p) throws ParseException, IOException {
		Hashtable<String, Object> v = new Hashtable<String, Object>();

		for (String key : value.keySet()) {
			for (String col : cols) {
				if (key.equals(col)) {
					v.put(key, value.get(key));
				}

			}

		}

		if (this.root == null) {
			this.root = new Node(minOfCol, maxOfCol, N, cols.get(0), cols.get(1), cols.get(2));
			this.root.data.add(new ValueInNode(v, p, r, this.root));

		} else
			this.root.insertIntoNode(v, r, p);

		String currentDir = System.getProperty("user.dir");
		FileOutputStream fileOut = new FileOutputStream(currentDir + "\\" + name + ".class");
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(this);
		out.close();
		fileOut.close();
		return;

	}

	public ValueInNode searchValue(Hashtable<String, Object> value) {
		Hashtable<String, Object> v = new Hashtable<String, Object>();
		Hashtable<String, Object> value2 = new Hashtable<String, Object>();
		value2 = (Hashtable<String, Object>) value.clone();

		for (String key : value2.keySet()) {
			for (String col : cols) {
				if (key.equals(col)) {
					v.put(key, value.get(key));
				}
			}

		}
		return this.root.searchValue(v);
	}

	public ArrayList<String> delete(Hashtable<String, Object> value) throws IOException {
		Hashtable<String, Object> v = new Hashtable<String, Object>();
		for (String key : value.keySet()) {
			for (String col : cols) {
				if (key.equals(col)) {
					v.put(key, value.get(key));
				}
			}

		}

		return this.root.delete(value);

	}

	public void updateRef(Hashtable<String, Object> value, Object p, String s) throws IOException {
		Hashtable<String, Object> v = new Hashtable<String, Object>();

		for (String key : value.keySet()) {
			for (String col : cols) {
				if (key.equals(col)) {
					v.put(key, value.get(key));
				}

			}

		}
		int j = -1;
		for (int i = 0; i < this.searchValue(v).pk.size(); i++) {
			if ((this.searchValue(v).pk.get(i) instanceof Integer || this.searchValue(v).pk.get(i) instanceof Double)
					&& this.searchValue(v).pk.get(i) == p) {
				j = i;
			} else if ((this.searchValue(v).pk.get(i) instanceof Date
					|| this.searchValue(v).pk.get(i) instanceof String) && this.searchValue(v).pk.get(i).equals(p)) {
				j = i;
			}
		}
		if (j != -1) {
			this.searchValue(v).r.add(j, s);
		}
		String currentDir = System.getProperty("user.dir");
		FileOutputStream fileOut = new FileOutputStream(currentDir + "\\" + name + ".class");
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(this);
		out.close();
		fileOut.close();

	}

	public void update(Hashtable<String, Object> oldValue, Object p, String r, Hashtable<String, Object> newValue)
			throws IOException, ParseException {
		this.deleteSpecific(oldValue, p, r);
		this.insert(newValue, r, p);
		String currentDir = System.getProperty("user.dir");
		FileOutputStream fileOut = new FileOutputStream(currentDir + "\\" + name + ".class");
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(this);
		out.close();
		fileOut.close();
		return;
	}

	public ArrayList<Hashtable<String,Object>> select(SQLTerm[] arrSQLTerms) throws IOException, ClassNotFoundException {
		ArrayList<Node> list = new ArrayList<Node>();
		ArrayList<Hashtable<String,Object>> g = new ArrayList<Hashtable<String,Object>>();
		ArrayList<String> r = new ArrayList<String>();

		if (root.isLeaf) {
			list.add(root);
		} else
			list = this.root.selectHelper(arrSQLTerms, list);
		for (Node n : list) {
			for (int i = 0; i < n.data.size(); i++) {
				boolean f = true;
				for (SQLTerm t : arrSQLTerms) {

					if (t._objValue instanceof Integer) {
						if (t._strOperator.equals("=")
								&& (int) n.data.get(i).v.get(t._strColumnName) != (int) t._objValue) {
							f = false;
						}
						if (t._strOperator.equals("<")
								&& (int) n.data.get(i).v.get(t._strColumnName) >= (int) t._objValue) {
							f = false;
						}
						if (t._strOperator.equals(">")
								&& (int) n.data.get(i).v.get(t._strColumnName) <= (int) t._objValue) {
							f = false;
						}

						if (t._strOperator.equals("!=")
								&& (int) n.data.get(i).v.get(t._strColumnName) == (int) t._objValue) {
							f = false;
						}
						if (t._strOperator.equals(">=")
								&& (int) n.data.get(i).v.get(t._strColumnName) < (int) t._objValue) {
							f = false;
						}
						if (t._strOperator.equals("<=")
								&& (int) n.data.get(i).v.get(t._strColumnName) > (int) t._objValue) {
							f = false;
						}

					} else if (t._objValue instanceof Double) {
						if (t._strOperator.equals("=")
								&& (Double) n.data.get(i).v.get(t._strColumnName) != (Double) t._objValue) {
							f = false;
						}
						if (t._strOperator.equals("<")
								&& (Double) n.data.get(i).v.get(t._strColumnName) >= (Double) t._objValue) {
							f = false;
						}
						if (t._strOperator.equals(">")
								&& (Double) n.data.get(i).v.get(t._strColumnName) <= (Double) t._objValue) {
							f = false;
						}

						if (t._strOperator.equals("!=")
								&& (Double) n.data.get(i).v.get(t._strColumnName) == (Double) t._objValue) {
							f = false;
						}
						if (t._strOperator.equals(">=")
								&& (Double) n.data.get(i).v.get(t._strColumnName) < (Double) t._objValue) {
							f = false;
						}
						if (t._strOperator.equals("<=")
								&& (Double) n.data.get(i).v.get(t._strColumnName) > (Double) t._objValue) {
							f = false;
						}
					} else if (t._objValue instanceof String) {
						if (t._strOperator.equals("=")
								&& !((String) n.data.get(i).v.get(t._strColumnName)).equals((String) t._objValue)) {
							f = false;
						}
						if (t._strOperator.equals("<") && ((String) n.data.get(i).v.get(t._strColumnName))
								.compareTo((String) t._objValue) >= 0) {
							f = false;
						}
						if (t._strOperator.equals(">") && ((String) n.data.get(i).v.get(t._strColumnName))
								.compareTo((String) t._objValue) <= 0) {
							f = false;
						}

						if (t._strOperator.equals("!=")
								&& ((String) n.data.get(i).v.get(t._strColumnName)).equals((String) t._objValue)) {

							f = false;
						}
						if (t._strOperator.equals(">=") && ((String) n.data.get(i).v.get(t._strColumnName))
								.compareTo((String) t._objValue) < 0) {
							f = false;
						}
						if (t._strOperator.equals("<=") && ((String) n.data.get(i).v.get(t._strColumnName))
								.compareTo((String) t._objValue) > 0) {
							f = false;
						}
					} else {
						if (t._strOperator.equals("=")
								&& !((Date) n.data.get(i).v.get(t._strColumnName)).equals((Date) t._objValue)) {
							f = false;
						}
						if (t._strOperator.equals("<")
								&& ((Date) n.data.get(i).v.get(t._strColumnName)).compareTo((Date) t._objValue) >= 0) {
							f = false;
						}
						if (t._strOperator.equals(">")
								&& ((Date) n.data.get(i).v.get(t._strColumnName)).compareTo((Date) t._objValue) <= 0) {
							f = false;
						}

						if (t._strOperator.equals("!=")
								&& ((Date) n.data.get(i).v.get(t._strColumnName)).equals((Date) t._objValue)) {

							f = false;
						}
						if (t._strOperator.equals(">=")
								&& ((Date) n.data.get(i).v.get(t._strColumnName)).compareTo((Date) t._objValue) < 0) {
							f = false;
						}
						if (t._strOperator.equals("<=")
								&& ((Date) n.data.get(i).v.get(t._strColumnName)).compareTo((Date) t._objValue) > 0) {
							f = false;
						}
					}

				}
				if (f) {
					for (String a : n.data.get(i).r) {
						r.add(a);
					}
				}

			}
		}

		r = removeDuplicates(r);
		String currentDir = System.getProperty("user.dir");
		for (String s : r) {
			FileInputStream fileIn2 = new FileInputStream(currentDir + "\\" + s);
			ObjectInputStream in2 = new ObjectInputStream(fileIn2);
			Page page = (Page) in2.readObject();
			in2.close();
			fileIn2.close();
			for (Tuple tup : page.tuples) {

				boolean f = true;
				for (SQLTerm t : arrSQLTerms) {

					if (t._objValue instanceof Integer) {
						if (t._strOperator.equals("=") && (int) tup.row.get(t._strColumnName) != (int) t._objValue) {
							f = false;
						}
						if (t._strOperator.equals("<") && (int) tup.row.get(t._strColumnName) >= (int) t._objValue) {
							f = false;
						}
						if (t._strOperator.equals(">") && (int) tup.row.get(t._strColumnName) <= (int) t._objValue) {
							f = false;
						}

						if (t._strOperator.equals("!=") && (int) tup.row.get(t._strColumnName) == (int) t._objValue) {
							f = false;
						}
						if (t._strOperator.equals(">=")
								&& (int) tup.row.get(t._strColumnName) < (int) t._objValue) {
							f = false;
						}
						if (t._strOperator.equals("<=")
								&& (int) tup.row.get(t._strColumnName) > (int) t._objValue) {
							f = false;
						}

					} else if (t._objValue instanceof Double) {
						if (t._strOperator.equals("=")
								&& (Double) tup.row.get(t._strColumnName) != (Double) t._objValue) {
							f = false;
						}
						if (t._strOperator.equals("<")
								&& (Double)tup.row.get(t._strColumnName) >= (Double) t._objValue) {
							f = false;
						}
						if (t._strOperator.equals(">")
								&& (Double) tup.row.get(t._strColumnName) <= (Double) t._objValue) {
							f = false;
						}

						if (t._strOperator.equals("!=")
								&& (Double) tup.row.get(t._strColumnName) == (Double) t._objValue) {
							f = false;
						}
						if (t._strOperator.equals(">=")
								&& (Double) tup.row.get(t._strColumnName) < (Double) t._objValue) {
							f = false;
						}
						if (t._strOperator.equals("<=")
								&& (Double) tup.row.get(t._strColumnName) > (Double) t._objValue) {
							f = false;
						}
					} else if (t._objValue instanceof String) {
						if (t._strOperator.equals("=")
								&& !((String) tup.row.get(t._strColumnName)).equals((String) t._objValue)) {
							f = false;
						}
						if (t._strOperator.equals("<") && ((String) tup.row.get(t._strColumnName))
								.compareTo((String) t._objValue) >= 0) {
							f = false;
						}
						if (t._strOperator.equals(">") && ((String)tup.row.get(t._strColumnName))
								.compareTo((String) t._objValue) <= 0) {
							f = false;
						}

						if (t._strOperator.equals("!=")
								&& ((String) tup.row.get(t._strColumnName)).equals((String) t._objValue)) {

							f = false;
						}
						if (t._strOperator.equals(">=") && ((String) tup.row.get(t._strColumnName))
								.compareTo((String) t._objValue) < 0) {
							f = false;
						}
						if (t._strOperator.equals("<=") && ((String) tup.row.get(t._strColumnName))
								.compareTo((String) t._objValue) > 0) {
							f = false;
						}
					} else {
						if (t._strOperator.equals("=")
								&& !((Date) tup.row.get(t._strColumnName)).equals((Date) t._objValue)) {
							f = false;
						}
						if (t._strOperator.equals("<")
								&& ((Date) tup.row.get(t._strColumnName)).compareTo((Date) t._objValue) >= 0) {
							f = false;
						}
						if (t._strOperator.equals(">")
								&& ((Date) tup.row.get(t._strColumnName)).compareTo((Date) t._objValue) <= 0) {
							f = false;
						}

						if (t._strOperator.equals("!=")
								&& ((Date) tup.row.get(t._strColumnName)).equals((Date) t._objValue)) {

							f = false;
						}
						if (t._strOperator.equals(">=")
								&& ((Date) tup.row.get(t._strColumnName)).compareTo((Date) t._objValue) < 0) {
							f = false;
						}
						if (t._strOperator.equals("<=")
								&& ((Date) tup.row.get(t._strColumnName)).compareTo((Date) t._objValue) > 0) {
							f = false;
						}
					}

				}
				if (f) {
					g.add(tup.row);
				}

			}
		}
		return g;

	}

	public void deleteSpecific(Hashtable<String, Object> value, Object p, String r) {
		Hashtable<String, Object> v = new Hashtable<String, Object>();
		for (String key : value.keySet()) {
			for (String col : cols) {
				if (key.equals(col)) {
					v.put(key, value.get(key));
				}
			}

		}
		this.root.deleteSpecific(value, p, r);

	}

	public static ArrayList<String> removeDuplicates(ArrayList<String> listWithDuplicates) {
		// Create a new HashSet to store unique strings
		HashSet<String> uniqueSet = new HashSet<>(listWithDuplicates);

		// Create a new ArrayList and add the unique strings from the HashSet to it
		ArrayList<String> listWithoutDuplicates = new ArrayList<>(uniqueSet);

		return listWithoutDuplicates;
	}

}
