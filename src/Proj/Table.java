package Proj;

import java.io.*;
import java.util.*;

public class Table implements Serializable {
	public String name;
	String clusteringKey;
	public Hashtable<String, String> colNameType;
	public Hashtable<String, String> colNameMin;
	public Hashtable<String, String> colNameMax;
	static int pageCount;
	public Vector<String> pages;
	String idType;
	int Maximum;
	Vector<String> octrees;

	public Table(String name, String clusteringKey, Hashtable<String, String> colNameType,
			Hashtable<String, String> colNameMin, Hashtable<String, String> colNameMax)
			throws DBAppException, IOException {

		octrees = new Vector<String>();
		Properties props = new Properties();
		String currentDir = System.getProperty("user.dir");
		FileInputStream fis = new FileInputStream(currentDir + "//DBApp.config");
		props.load(fis);
		String N = props.getProperty("MaximumRowsCountinTablePage");
		this.Maximum = Integer.parseInt(N);
		this.name = name;
		this.clusteringKey = clusteringKey;
		this.colNameType = colNameType;
		this.colNameMin = colNameMin;
		this.colNameMax = colNameMax;
		this.pageCount = 0;
		this.pages = new Vector<>();

		try {

			FileWriter writer = new FileWriter("meta-data.csv");
			PrintWriter printWriter = new PrintWriter(writer);
			for (String colname : colNameType.keySet()) {
				String type = colNameType.get(colname);
				boolean f;
				if (clusteringKey.equals(colname)) {
					f = true;
					this.idType = type;
				} else {
					f = false;
				}

				String Min = colNameMin.get(colname);
				String Max = colNameMax.get(colname);
				printWriter.print(name + "," + colname + "," + type + "," + f + ",Null,Null," + Min + "," + Max + "\n");

			}
			printWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void createPage() throws IOException {
		pageCount++;
		Page p = new Page(Maximum, pageCount, this.clusteringKey, this);
		this.pages.add(this.name + "-" + pageCount + ".class");
		FileOutputStream fileOut1 = new FileOutputStream(this.name + ".class");
		ObjectOutputStream out1 = new ObjectOutputStream(fileOut1);
		out1.writeObject(this);
		out1.close();
		fileOut1.close();
		FileOutputStream fileOut = new FileOutputStream(this.name + "-" + pageCount + ".class");
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(p);
		out.close();
		fileOut.close();

	}

	public void clean(String start) throws IOException, ClassNotFoundException {
		String currentDir = System.getProperty("user.dir");
		FileInputStream fileIn = new FileInputStream(currentDir + "\\" + this.name + ".class");
		ObjectInputStream in = new ObjectInputStream(fileIn);
		Table table = (Table) in.readObject();
		in.close();
		fileIn.close();
		int s = table.pages.indexOf(start);

		for (int i = s; i < table.pages.size(); i++) {

			String currentPagePath = table.pages.get(i);
			FileInputStream currentPageFileIn = new FileInputStream(currentDir + "\\" + currentPagePath);
			ObjectInputStream currentPageObjectIn = new ObjectInputStream(currentPageFileIn);

			Page currentPage = (Page) currentPageObjectIn.readObject();

			currentPageObjectIn.close();
			currentPageFileIn.close();
			if (currentPage.tuples.size() > currentPage.MaxRows) {
				if (i == table.pages.size() - 1) {
					table.createPage();

				}
				FileInputStream fileIn1 = new FileInputStream(currentDir + "\\" + this.name + "-" + (i + 2) + ".class");
				ObjectInputStream in1 = new ObjectInputStream(fileIn1);
				Page newPage = (Page) in1.readObject();
				in1.close();
				fileIn1.close();
				for (String o : this.octrees) {
					FileInputStream fileIn5 = new FileInputStream(currentDir + "\\" + o + ".class");
					ObjectInputStream in5 = new ObjectInputStream(fileIn5);
					Octree oct = (Octree) in5.readObject();
					int k=currentPage.tuples.size()-1;
					oct.updateRef(currentPage.tuples.get(k).row,currentPage.tuples.get(k).row.get(currentPage.tuples.get(k).clusteringKey), this.name + "-" + (i + 2));
					

				}

				newPage.tuples.add(currentPage.tuples.remove(currentPage.tuples.size() - 1));

				Collections.sort(newPage.tuples, new Comparator<Tuple>() {
					public int compare(Tuple t1, Tuple t2) {
						Object id1 = t1.getId();
						Object id2 = t2.getId();

						if (id1 instanceof Integer && id2 instanceof Integer) {
							return ((Integer) id1).compareTo((Integer) id2);
						} else {
							return id1.toString().compareTo(id2.toString());
						}
					}
				});

				FileOutputStream currentPageFileOut = new FileOutputStream(currentDir + "\\" + currentPagePath);
				ObjectOutputStream currentPageObjectOut = new ObjectOutputStream(currentPageFileOut);

				FileOutputStream nextPageFileOut = new FileOutputStream(
						currentDir + "\\" + this.name + "-" + (i + 2) + ".class");
				ObjectOutputStream nextPageObjectOut = new ObjectOutputStream(nextPageFileOut);

				currentPageObjectOut.writeObject(currentPage);
				nextPageObjectOut.writeObject(newPage);

				currentPageObjectOut.close();
				currentPageFileOut.close();
				nextPageObjectOut.close();
				nextPageFileOut.close();

			}

		}
	}

}
