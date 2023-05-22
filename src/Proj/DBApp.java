package Proj;

import java.util.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

public class DBApp {
	public DBApp() {

	}

	public void init() {
		File file = new File("meta-data.csv");
	}

	public boolean tableFound(String name) {
		String currentDir = System.getProperty("user.dir");
		File directory = new File(currentDir);
		File[] files = directory.listFiles();

		// Loop through each file and directory.
		for (File file : files) {
			if (file.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public void createTable(String strTableName, String strClusteringKeyColumn,
			Hashtable<String, String> htblColNameType, Hashtable<String, String> htblColNameMin,
			Hashtable<String, String> htblColNameMax) throws DBAppException {

		if (tableFound(strTableName + ".class")) {
			throw new DBAppException("Table " + strTableName + " already exists.");
		}
		for (String key : htblColNameType.keySet()) {
			if (!(htblColNameType.get(key).equals("java.util.Date")
					|| htblColNameType.get(key).equals("java.lang.Integer")
					|| htblColNameType.get(key).equals("java.lang.Double")
					|| htblColNameType.get(key).equals("java.lang.String"))) {
				throw new DBAppException("The type you entered is not supported");
			}
		}

		Table t;
		try {
			t = new Table(strTableName, strClusteringKeyColumn, htblColNameType, htblColNameMin, htblColNameMax);
			FileOutputStream fileOut = new FileOutputStream(t.name + ".class");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(t);
			out.close();
			fileOut.close();
		} catch (DBAppException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void insertIntoTable(String strTableName, Hashtable<String, Object> htblColNameValue)
			throws DBAppException

	{
		try {
		String currentDir = System.getProperty("user.dir");

		if (tableFound(strTableName + ".class") == false) {
			throw new DBAppException("Table " + strTableName + " does not exists");
		}
		String line = "";
		String cvsSplitBy = ",";
		String[][] data;

		try (BufferedReader br = new BufferedReader(new FileReader("meta-data.csv"))) {
			// Determine number of rows and columns
			int numRows = 0;
			int numCols = 0;
			while ((line = br.readLine()) != null) {
				numRows++;
				String[] row = line.split(cvsSplitBy);
				numCols = Math.max(numCols, row.length);
			}

			// Initialize data array
			data = new String[numRows][numCols];

			// Read CSV file into data array
			int row = 0;
			BufferedReader cr = new BufferedReader(new FileReader("meta-data.csv"));
			while ((line = cr.readLine()) != null) {
				String[] fields = line.split(cvsSplitBy);
				for (int col = 0; col < fields.length; col++) {
					data[row][col] = fields[col];
				}
				row++;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// checks if the values are the same type as given
		for (int row = 0; row < data.length; row++) {

			for (String colname : htblColNameValue.keySet()) {
				Object value = htblColNameValue.get(colname);
				if ((data[row][1] + "").equals(colname)) {
					if ((data[row][2] + "").equals("java.lang.Integer")) {
						if (!((Object) value instanceof Integer)) {
							throw new DBAppException("Values you are trying to insert are not the right type");
						}
					}
					if ((data[row][2] + "").equals("java.lang.String")) {
						if (!((Object) value instanceof String)) {
							throw new DBAppException("Values you are trying to insert are not the right type");
						}
					}
					if ((data[row][2] + "").equals("java.lang.Double")) {
						if (!((Object) value instanceof Double)) {
							throw new DBAppException("Values you are trying to insert are not the right type");
						}
					}
					if ((data[row][2] + "").equals("java.util.Date")) {
						if (!((Object) value instanceof Date)) {
							throw new DBAppException("Values you are trying to insert are not the right type");
						}
					}
				}

			}

		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		// checks min and max
		for (int row = 0; row < data.length; row++) {

			for (String colname : htblColNameValue.keySet()) {
				Object value = htblColNameValue.get(colname);
				if ((data[row][1] + "").equals(colname)) {
					if ((data[row][2] + "").equals("java.lang.Integer")) {
						if (Integer.parseInt(data[row][6]) > (int) value
								|| Integer.parseInt(data[row][7]) < (int) value) {
							throw new DBAppException("Values you are trying to insert are out of bounds");
						}

					}
					if ((data[row][2] + "").equals("java.lang.String")) {
						if (((String) data[row][6]).compareTo((String) value) > 0
								|| ((String) data[row][7]).compareTo((String) value) > 0) {

							throw new DBAppException("Values you are trying to insert are out of bounds");
						}
					}

					if ((data[row][2] + "").equals("java.util.Date")) {
						Date date = dateFormat.parse(data[row][6]);
						Date date2 = dateFormat.parse(data[row][7]);
						if (date2.compareTo((Date) value) < 0 || ((Date) value).compareTo(date) < 0) {
							throw new DBAppException("Values you are trying to insert are out of bounds");
						}
					}
					if ((data[row][2] + "").equals("java.lang.Double")) {
						if (Double.parseDouble(data[row][6]) > (double) value
								|| Double.parseDouble(data[row][7]) < (double) value) {
							throw new DBAppException("Values you are trying to insert are out of bounds");
						}

					}

				}

			}

		}
		// in case the table is empty
		FileInputStream fileIn = new FileInputStream(currentDir + "\\" + strTableName + ".class");
		ObjectInputStream in = new ObjectInputStream(fileIn);
		Table table = (Table) in.readObject();
		in.close();
		fileIn.close();
		if (table.pages.isEmpty()) {
			table.createPage();
			FileOutputStream tableout = new FileOutputStream(currentDir + "\\" + strTableName + ".class");
			ObjectOutputStream currentPageObjectOut2 = new ObjectOutputStream(tableout);
			currentPageObjectOut2.writeObject(table);
			currentPageObjectOut2.close();
			tableout.close();

			Tuple tuple = new Tuple(table.clusteringKey);
			try {
				// Deserialize the first page in the table to insert the tuple
				FileInputStream fileIn1 = new FileInputStream(currentDir + "\\" + strTableName + "-1.class");
				ObjectInputStream in1 = new ObjectInputStream(fileIn1);
				Page obj = (Page) in1.readObject();
				in1.close();
				fileIn1.close();

				tuple.row = htblColNameValue;
				obj.tuples.add(tuple);
				table.pages.add(table.name + "-" + obj.pageNum);
				table.pageCount = 1;
				FileOutputStream currentPageFileOut = new FileOutputStream(
						currentDir + "\\" + strTableName + "-1.class");
				ObjectOutputStream currentPageObjectOut = new ObjectOutputStream(currentPageFileOut);
				currentPageObjectOut.writeObject(obj);
				currentPageObjectOut.close();
				currentPageFileOut.close();
				if (!table.octrees.isEmpty()) {
					for (String oct : table.octrees) {
						FileInputStream fileIn7 = new FileInputStream(currentDir + "\\" + oct + ".class");
						ObjectInputStream in7 = new ObjectInputStream(fileIn7);
						Octree octree = (Octree) in7.readObject();
						in7.close();
						fileIn7.close();
						octree.insert(htblColNameValue, strTableName + "-1.class",
								htblColNameValue.get(tuple.clusteringKey));
						FileOutputStream tableout7 = new FileOutputStream(currentDir + "\\" + oct + ".class");
						ObjectOutputStream currentPageObjectOut27 = new ObjectOutputStream(tableout7);
						currentPageObjectOut27.writeObject(octree);
						currentPageObjectOut27.close();
						tableout7.close();
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

		} else {

			Object id = htblColNameValue.get(table.clusteringKey);
			Tuple tuple = new Tuple(table.clusteringKey);
			tuple.row = htblColNameValue;
			String ToInsert = binarySearch(id, table);

			FileInputStream fileIn3 = new FileInputStream(currentDir + "\\" + ToInsert);
			ObjectInputStream in3 = new ObjectInputStream(fileIn3);
			Page obj3 = (Page) in3.readObject();
			in3.close();
			fileIn3.close();
			obj3.tuples.add(tuple);
			for (String o : table.octrees) {
				FileInputStream fileIn5 = new FileInputStream(currentDir + "\\" + o + ".class");
				ObjectInputStream in5 = new ObjectInputStream(fileIn5);
				Octree oct = (Octree) in5.readObject();
				in5.close();
				fileIn5.close();
				oct.insert(htblColNameValue, ToInsert, htblColNameValue.get(tuple.clusteringKey));

			}

			boolean needsCleaning = false;
			if (obj3.tuples.size() > obj3.MaxRows) {
				needsCleaning = true;
			}
			String type = table.idType;
			Collections.sort(obj3.tuples, new Comparator<Tuple>() {
				public int compare(Tuple t1, Tuple t2) {
					Object id1 = t1.getId();
					Object id2 = t2.getId();

					if (id1 instanceof Integer && id2 instanceof Integer) {
						return ((Integer) id1).compareTo((Integer) id2);
					} else if (id1 instanceof Double && id2 instanceof Double) {
						return ((Double) id1).compareTo((Double) id2);
					} else if (id1 instanceof Date && id2 instanceof Date) {
						return ((Date) id1).compareTo((Date) id2);
					} else {
						return ((id1.toString()).compareTo(id2.toString()));
					}
				}
			});
			FileOutputStream currentPageFileOut = new FileOutputStream(currentDir + "\\" + ToInsert);
			ObjectOutputStream currentPageObjectOut = new ObjectOutputStream(currentPageFileOut);
			currentPageObjectOut.writeObject(obj3);
			currentPageObjectOut.close();
			currentPageFileOut.close();
			if (needsCleaning) {
				table.clean(ToInsert);
			}

		}}
		catch(IOException e) {
			e.printStackTrace();
		}
		catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		catch(ParseException e) {
			e.printStackTrace();
		}

	}

	public void updateTable(String strTableName, String strClusteringKeyValue,
			Hashtable<String, Object> htblColNameValue)
			throws DBAppException {
		String currentDir = System.getProperty("user.dir");
		try {
		String idType = "";

		if (tableFound(strTableName + ".class") == false) {
			throw new DBAppException("Table " + strTableName + " does not exists");
		}

		FileInputStream fileIn = new FileInputStream(currentDir + "\\" + strTableName + ".class");
		ObjectInputStream in = new ObjectInputStream(fileIn);
		Table table = (Table) in.readObject();
		in.close();
		fileIn.close();
		String primary = table.clusteringKey;
		for (String key : htblColNameValue.keySet()) {
			if (htblColNameValue.get(key).equals(primary)) {
				throw new DBAppException("Cannot update clustering key");
			}
		}

		String line = "";
		String cvsSplitBy = ",";
		String[][] data;

		try (BufferedReader br = new BufferedReader(new FileReader("meta-data.csv"))) {
			// Determine number of rows and columns
			int numRows = 0;
			int numCols = 0;
			while ((line = br.readLine()) != null) {
				numRows++;
				String[] row = line.split(cvsSplitBy);
				numCols = Math.max(numCols, row.length);
			}

			// Initialize data array
			data = new String[numRows][numCols];

			// Read CSV file into data array
			int row = 0;
			BufferedReader cr = new BufferedReader(new FileReader("meta-data.csv"));
			while ((line = cr.readLine()) != null) {
				String[] fields = line.split(cvsSplitBy);
				for (int col = 0; col < fields.length; col++) {
					data[row][col] = fields[col];
				}
				row++;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// checks if the values are the same type as given
		for (int row = 0; row < data.length; row++) {
			if (data[row][1].equals(table.clusteringKey)) {
				idType = data[row][2];

			}

			for (String colname : htblColNameValue.keySet()) {
				Object value = htblColNameValue.get(colname);
				if ((data[row][1] + "").equals(colname)) {
					if ((data[row][2] + "").equals("java.lang.Integer")) {
						if (!((Object) value instanceof Integer)) {
							throw new DBAppException("Values you are trying to insert are not the right type");
						}
					}
					if ((data[row][2] + "").equals("java.lang.String")) {
						if (!((Object) value instanceof String)) {
							throw new DBAppException("Values you are trying to insert are not the right type");
						}
					}
					if ((data[row][2] + "").equals("java.lang.Double")) {
						if (!((Object) value instanceof Double)) {
							throw new DBAppException("Values you are trying to insert are not the right type");
						}
					}
					if ((data[row][2] + "").equals("java.util.Date")) {
						if (!((Object) value instanceof Date)) {
							throw new DBAppException("Values you are trying to insert are not the right type");
						}
					}
				}

			}

		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		// checks min and max
		for (int row = 0; row < data.length; row++) {

			for (String colname : htblColNameValue.keySet()) {
				Object value = htblColNameValue.get(colname);
				if ((data[row][1] + "").equals(colname)) {
					if ((data[row][2] + "").equals("java.lang.Integer")) {
						if (Integer.parseInt(data[row][6]) > (int) value
								|| Integer.parseInt(data[row][7]) < (int) value) {
							throw new DBAppException("Values you are trying to insert are out of bounds");
						}

					}
					if ((data[row][2] + "").equals("java.lang.String")) {
						if (((String) data[row][6]).compareTo((String) value) > 0
								|| ((String) data[row][7]).compareTo((String) value) < 0) {
							throw new DBAppException("Values you are trying to insert are out of bounds");
						}
					}

					if ((data[row][2] + "").equals("java.util.Date")) {
						Date date = dateFormat.parse(data[row][6]);
						Date date2 = dateFormat.parse(data[row][7]);
						if (date.compareTo((Date) value) > 0 || (date2).compareTo((Date) value) < 0) {
							throw new DBAppException("Values you are trying to insert are out of bounds");
						}
					}
					if ((data[row][2] + "").equals("java.lang.double")) {
						if (Double.parseDouble(data[row][6]) > (double) value
								|| Double.parseDouble(data[row][7]) < (double) value) {
							throw new DBAppException("Values you are trying to insert are out of bounds");
						}

					}

				}
			}

		}

		Object id = null;
		if (idType.equals("java.lang.Integer")) {
			id = Integer.parseInt(strClusteringKeyValue);
		} else if (idType.equals("java.lang.Double")) {
			id = Double.parseDouble(strClusteringKeyValue);
		} else if (idType.equals("java.lang.String")) {
			id = strClusteringKeyValue;
		} else {

			id = dateFormat.parse(strClusteringKeyValue);
		}

		String ToInsert = binarySearch(id, table);
		FileInputStream fileIn1 = new FileInputStream(currentDir + "\\" + ToInsert);
		ObjectInputStream in1 = new ObjectInputStream(fileIn1);
		Page page = (Page) in1.readObject();
		in1.close();
		fileIn1.close();
		Tuple tup = null;
		for (int i = 0; i < page.tuples.size(); i++) {
			if (compare(id, page.tuples.get(i).getId()) == 0) {
				tup = page.tuples.get(i);
				if (tup != null) {
					for (String o : table.octrees) {

						FileInputStream fileIn9 = new FileInputStream(currentDir + "\\" + o + ".class");
						ObjectInputStream in9 = new ObjectInputStream(fileIn9);
						Octree oct = (Octree) in9.readObject();
						in9.close();
						fileIn9.close();
						Hashtable<String, Object> temp = new Hashtable<String, Object>();
						temp = (Hashtable<String, Object>) tup.row.clone();
						for (String f : htblColNameValue.keySet()) {
							temp.put(f, htblColNameValue.get(f));
						}
						oct.update(tup.row, tup.row.get(tup.clusteringKey), ToInsert, temp);
					}
				}

				for (String key : htblColNameValue.keySet()) {
					Object value = htblColNameValue.get(key);
					page.tuples.get(i).row.put(key, value);
				}

			}
		}

		FileOutputStream fileOut = new FileOutputStream(currentDir + "\\" + ToInsert);
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(page);
		out.close();
		fileOut.close();
	}
	catch(IOException e) {
		e.printStackTrace();
	}
	catch(ClassNotFoundException e) {
		e.printStackTrace();
	}
	catch(ParseException e) {
		e.printStackTrace();
	}

	}

	public int compare(Object o1, Object o2) {

		if (o1 instanceof Integer && o2 instanceof Integer) {
			return ((Integer) o1).compareTo((Integer) o2);
		} else if (o1 instanceof String && o2 instanceof String) {
			return o1.toString().compareTo(o2.toString());
		} else if (o1 instanceof Double && o2 instanceof Double) {
			return ((Double) o1).compareTo((Double) o2);
		} else {
			return ((Date) o1).compareTo((Date) o2);
		}
	}

	public String binarySearch(Object id, Table table) throws ClassNotFoundException, IOException {
		int start = 0;
		int end = table.pages.size() - 1;
		String currentDir = System.getProperty("user.dir");

		while (start <= end) {
			int midpoint = (start + end) / 2;
			String p = table.pages.get(midpoint);
			FileInputStream fileIn21 = new FileInputStream(currentDir + "\\" + p);
			ObjectInputStream in21 = new ObjectInputStream(fileIn21);
			Page obj1 = (Page) in21.readObject();
			in21.close();
			fileIn21.close();

			if (compare(id, obj1.tuples.get(0).getId()) < 0) {
				end = midpoint - 1;
			} else if (compare(id, obj1.tuples.get(0).getId()) > 0) {
				start = midpoint + 1;
			} else {
				return p;
			}
		}
		String p1 = table.pages.get(0);
		String p2 = table.pages.get(table.pages.size() - 1);
		FileInputStream fileIn21 = new FileInputStream(currentDir + "\\" + p1);
		ObjectInputStream in21 = new ObjectInputStream(fileIn21);
		Page obj1 = (Page) in21.readObject();
		in21.close();
		fileIn21.close();

		if (compare(id, obj1.tuples.get(0).getId()) < 0) {
			return p1;
		} else {
			return p2;
		}

		// If we reach here, value is not in the list.
		// Return the start index to indicate where the tuple should be inserted.
		// return table.pages.get(0);
	}

	public void deleteFromTable(String strTableName, Hashtable<String, Object> htblColNameValue)
			throws DBAppException{
		try {
		String currentDir = System.getProperty("user.dir");

		if (tableFound(strTableName + ".class") == false) {
			throw new DBAppException("Table " + strTableName + " does not exists");
		}

		FileInputStream fileIn = new FileInputStream(currentDir + "\\" + strTableName + ".class");
		ObjectInputStream in = new ObjectInputStream(fileIn);
		Table table = (Table) in.readObject();
		in.close();
		fileIn.close();
		ArrayList<String> a = new ArrayList<String>();
		if (!table.octrees.isEmpty()) {

			for (String o : table.octrees) {

				FileInputStream fileIn1 = new FileInputStream(currentDir + "\\" + o + ".class");
				ObjectInputStream in1 = new ObjectInputStream(fileIn1);
				Octree oct = (Octree) in1.readObject();
				in1.close();
				fileIn1.close();
				if (htblColNameValue.containsKey(oct.cols.get(0)) && htblColNameValue.containsKey(oct.cols.get(1))
						&& htblColNameValue.containsKey(oct.cols.get(2))) {
					a = oct.delete(htblColNameValue);
					currentDir = System.getProperty("user.dir");
					FileOutputStream fileOut = new FileOutputStream(currentDir + "\\" + oct.name + ".class");
					ObjectOutputStream out = new ObjectOutputStream(fileOut);
					out.writeObject(oct);
					out.close();
					fileOut.close();
					Vector<String> v = new Vector<String>();
					for (String s : a) {
						v.add(s);
					}
					this.linearDelete(v, table, htblColNameValue);
					return;

				}

			}

		}
		this.linearDelete(table.pages, table, htblColNameValue);
		return;
		
	}
	catch(IOException e) {
		e.printStackTrace();
	}
	catch(ClassNotFoundException e) {
		e.printStackTrace();
	}
	
	}

	public void createIndex(String strTableName, String[] strarrColName)
			throws DBAppException {
		if (strarrColName.length < 3) {
			throw new DBAppException("index must be on 3 columns");
		}
		try {
		String currentDir = System.getProperty("user.dir");

		FileInputStream fileIn = new FileInputStream(currentDir + "\\" + strTableName + ".class");
		ObjectInputStream in = new ObjectInputStream(fileIn);
		Table table = (Table) in.readObject();
		in.close();
		fileIn.close();
		for (String o : table.octrees) {
			FileInputStream fileIn5 = new FileInputStream(currentDir + "\\" + strTableName + ".class");
			ObjectInputStream in5 = new ObjectInputStream(fileIn5);
			Octree oct = (Octree) in5.readObject();
			in5.close();
			fileIn5.close();
			for (String s : strarrColName) {
				for (String ss : oct.cols) {
					if (s.equals(ss)) {
						throw new DBAppException("there is an index already created on one of the columns");
					}
				}
			}

		}

		Octree tree = new Octree(strarrColName, strTableName);
		table.octrees.add(tree.name);
		FileOutputStream fileOut2 = new FileOutputStream(currentDir + "\\" + strTableName + ".class");
		ObjectOutputStream out2 = new ObjectOutputStream(fileOut2);
		out2.writeObject(table);
		out2.close();
		fileOut2.close();

		if (table.pages.size() != 0) {
			for (String pp : table.pages) {

				FileInputStream fileIn2 = new FileInputStream(currentDir + "\\" + pp);
				ObjectInputStream in2 = new ObjectInputStream(fileIn2);
				Page p = (Page) in2.readObject();
				in2.close();
				fileIn2.close();
				for (Tuple t : p.tuples) {
					tree.insert(t.row, currentDir + "\\" + pp, t.row.get(t.clusteringKey));
				}
			}
		}
	}
	catch(IOException e) {
		e.printStackTrace();
	}
	catch(ClassNotFoundException e) {
		e.printStackTrace();
	}
	catch(ParseException e) {
		e.printStackTrace();
	}

	}

	public void linearDelete(Vector<String> pages, Table table, Hashtable<String, Object> htblColNameValue)
			throws ClassNotFoundException, IOException {
		String currentDir = System.getProperty("user.dir");
		for (int i = 0; i < pages.size(); i++) {
			FileInputStream fileIn1 = new FileInputStream(currentDir + "\\" + table.pages.get(i));
			ObjectInputStream in1 = new ObjectInputStream(fileIn1);
			Page page = (Page) in1.readObject();
			in1.close();
			fileIn1.close();

			for (int j = 0; j < page.tuples.size(); j++) {
				boolean cond = true;
				for (String key : htblColNameValue.keySet()) {
					if (compare(page.tuples.get(j).row.get(key), htblColNameValue.get(key)) != 0) {
						cond = false;
					}
				}
				if (cond) {

					for (String o : table.octrees) {
						FileInputStream fileIn = new FileInputStream(currentDir + "\\" + o + ".class");
						ObjectInputStream in = new ObjectInputStream(fileIn);
						Octree oct = (Octree) in.readObject();
						in.close();
						fileIn.close();

						oct.delete(page.tuples.get(j).row);
						currentDir = System.getProperty("user.dir");
						FileOutputStream fileOut = new FileOutputStream(currentDir + "\\" + oct.name + ".class");
						ObjectOutputStream out = new ObjectOutputStream(fileOut);
						out.writeObject(oct);
						out.close();
						fileOut.close();
					}

					page.tuples.remove(j);
					j--;

					FileOutputStream fileOut = new FileOutputStream(currentDir + "\\" + table.pages.get(i));
					ObjectOutputStream out = new ObjectOutputStream(fileOut);
					out.writeObject(page);
					out.close();
					fileOut.close();
					if (page.tuples.isEmpty()) {
						File file = new File(currentDir + "\\" + table.pages.get(i));
						file.delete();
						table.pages.remove(table.pages.get(i));
						FileOutputStream fileOut2 = new FileOutputStream(currentDir + "\\" + table.name + ".class");
						ObjectOutputStream out2 = new ObjectOutputStream(fileOut2);
						out2.writeObject(table);
						out2.close();
						fileOut2.close();

						i--;
					}

				}
			}
		}
	}

	public Iterator selectFromTable(SQLTerm[] arrSQLTerms, String[] strarrOperators)
			throws DBAppException {
		Page page =null;
		Octree oct= null;
		String currentDir = System.getProperty("user.dir");
		String t = arrSQLTerms[0]._strTableName;
		Table table=null;;
		for (SQLTerm s : arrSQLTerms) {
			if (!s._strTableName.equals(t)) {
				throw new DBAppException("Cannot select from different tables");
			}
		}
		FileInputStream fileIn1;
		try {
			fileIn1 = new FileInputStream(currentDir + "\\" + t + ".class");
			ObjectInputStream in1 = new ObjectInputStream(fileIn1);
			 table = (Table) in1.readObject();
			in1.close();
			fileIn1.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		

		boolean allAnd = true;
		for (String s : strarrOperators) {
			if (!s.equals("AND")) {
				allAnd = false;
			}
		}
		if (allAnd && arrSQLTerms.length == 3) {
			
			Octree octree = null;
			for (String o : table.octrees) {
				FileInputStream fileIn;
				try {
					fileIn = new FileInputStream(currentDir + "\\" + o + ".class");
					ObjectInputStream in = new ObjectInputStream(fileIn);
					oct = (Octree) in.readObject();
					in.close();
					fileIn.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch(ClassNotFoundException e) {
					e.printStackTrace();
				}
				catch(IOException e) {
					e.printStackTrace();
				}
				

				boolean x = false;
				boolean y = false;
				boolean z = false;
				for (SQLTerm s : arrSQLTerms) {
					if (s._strColumnName.equals(oct.cols.get(0))) {
						x = true;
					}
					if (s._strColumnName.equals(oct.cols.get(1))) {
						y = true;
					}
					if (s._strColumnName.equals(oct.cols.get(2))) {
						z = true;
					}
				}
				if (x && y && z) {
					octree = oct;
					break;

				}
			}
			if (octree != null) {

				try {
					octree.select(arrSQLTerms).iterator();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// search by el octree hena
				// w return
			}

		}
		// search mn gher el octree

		ArrayList<Hashtable<String, Object>> result = new ArrayList<Hashtable<String, Object>>();
		for (String p : table.pages) {
			FileInputStream fileIn2;
			try {
				fileIn2 = new FileInputStream(currentDir + "\\" + p);
				ObjectInputStream in2 = new ObjectInputStream(fileIn2);
				page = (Page) in2.readObject();
				in2.close();
				fileIn2.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			Vector<Boolean> b = new Vector<Boolean>();

			for (Tuple tup : page.tuples) {

				b.clear();
				for (SQLTerm s : arrSQLTerms) {
					b.add(this.check(tup.row, s._strColumnName, s._strOperator, s._objValue));

				}

				for (int i = 0; i < strarrOperators.length; i++) {
					String op = strarrOperators[i];
					boolean temp;
					if (op.equals("AND")) {
						// System.out.print(b.toString());
						temp = b.get(0) & b.get(1);
						b.remove(1);
						b.remove(0);
						b.add(0, temp);

					}
					if (op.equals("OR")) {
						temp = b.get(0) | b.get(1);
						b.remove(1);
						b.remove(0);
						b.add(0, temp);
					}
					if (op.equals("XOR")) {
						temp = b.get(0) ^ b.get(1);
						b.remove(1);
						b.remove(0);
						b.add(0, temp);
					}

				}
				if (b.get(0) == true)
					result.add(tup.row);

			}

		}

		return result.iterator();

	}

	public boolean check(Hashtable<String, Object> h, String c, String o, Object v) {
		// h.get(c)

		if (o.equals("=")) {
			if (v instanceof Integer) {
				return (int) h.get(c) == (int) v;
			} else if (v instanceof Double) {
				return (Double) h.get(c) == (Double) v;
			} else if (v instanceof String) {
				return ((String) h.get(c)).equals((String) v);
			} else {
				return ((Date) h.get(c)).equals((Date) v);
			}

		} else if (o.equals(">=")) {
			if (v instanceof Integer) {
				return (int) h.get(c) >= (int) v;
			} else if (v instanceof Double) {
				return (Double) h.get(c) >= (Double) v;
			} else if (v instanceof String) {
				return ((String) h.get(c)).compareTo((String) v) >= 0;
			} else {
				return (((Date) h.get(c)).compareTo((Date) v) >= 0);
			}

		} else if (o.equals("<=")) {
			if (v instanceof Integer) {
				return (int) h.get(c) <= (int) v;
			} else if (v instanceof Double) {
				return (Double) h.get(c) <= (Double) v;
			} else if (v instanceof String) {
				return ((String) h.get(c)).compareTo((String) v) <= 0;
			} else {
				return (((Date) h.get(c)).compareTo((Date) v) <= 0);
			}

		} else if (o.equals(">")) {
			if (v instanceof Integer) {
				return (int) h.get(c) > (int) v;
			} else if (v instanceof Double) {
				return (Double) h.get(c) > (Double) v;
			} else if (v instanceof String) {
				return ((String) h.get(c)).compareTo((String) v) > 0;
			} else {
				return (((Date) h.get(c)).compareTo((Date) v) > 0);
			}

		} else if (o.equals("<")) {
			if (v instanceof Integer) {
				return (int) h.get(c) < (int) v;
			} else if (v instanceof Double) {
				return (Double) h.get(c) < (Double) v;
			} else if (v instanceof String) {
				return ((String) h.get(c)).compareTo((String) v) < 0;
			} else {
				return (((Date) h.get(c)).compareTo((Date) v) < 0);
			}

		} else {
			if (v instanceof Integer) {
				return (int) h.get(c) != (int) v;
			} else if (v instanceof Double) {
				return (Double) h.get(c) != (Double) v;
			} else if (v instanceof String) {
				return ((String) h.get(c)).compareTo((String) v) != 0;
			} else {
				return (((Date) h.get(c)).compareTo((Date) v) != 0);
			}

		}
	}

	

}
