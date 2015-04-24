import java.io.*;
import java.util.*;

public class MergeDouban {

	public static void main(String[] args) throws IOException {
		Scanner input1 = new Scanner(new File("imdb2013id.csv"));
		Scanner input2 = new Scanner(new File("douban2013.csv"));
		List<String> imdb = imdb2013get.getTitles(input1);
		List<String> douban = imdb2013get.getTitles(input2);
		merge(imdb, douban);
	}

	public static void merge(List<String> imdb, List<String> douban)
			throws IOException {
		PrintStream output = new PrintStream("mergeddouban"
				+ System.currentTimeMillis() + ".csv");
		Map<String, String> dMap = doubanSplit(douban);
		for (int i = 0; i < imdb.size(); i++) {
			output.print(imdb.get(i) + ',');
			if (dMap.containsKey(imdb.get(i))) {
				output.print(dMap.get(imdb.get(i)));
			}
			output.println();
		}
		output.close();
	}

	public static Map<String, String> doubanSplit(List<String> input) {
		Map<String, String> result = new LinkedHashMap<String, String>();
		for (String line : input) {
			int i = 0;
			boolean look = true;
			while (look) {
				System.out.println();
				if (line.charAt(i) == ',') {
					look = false;
				} else {
					i++;
				}
			}			
			result.put(line.substring(0, i), line.substring(i + 1));
		}

		return result;
	}
}
