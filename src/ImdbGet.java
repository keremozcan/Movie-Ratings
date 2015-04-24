import java.io.*;
import java.util.*;
import java.net.*;

public class ImdbGet {

	public static void main(String[] args) throws IOException {
		Scanner input = new Scanner(new File("imdb2013id2.txt"));
		List<String> movies = getTitles(input);
		PrintStream output = new PrintStream("imdb"
				+ System.currentTimeMillis() + ".txt");
		parse(movies, output);
	}

	public static List<String> getTitles(Scanner input) {
		List<String> movies = new ArrayList<String>();
		while (input.hasNextLine()) {
			String title = input.nextLine();
			movies.add(title);
			System.out.println(title);
		}
		return movies;
	}

	public static void parse(List<String> movies, PrintStream output)
			throws IOException {
		for (int i = 0; i < movies.size(); i++) {

			String link = movies.get(i);
			Scanner page = scanPage(link);
			String token = "";
			String distribution = "";
			boolean rating = true;
			while (page.hasNext() && !token.equals("mean")) {
				token = page.next();
				if (token.startsWith("align=\"right\">")) {
					rating = !rating;
					token = token.replaceAll("[^\\d.]", "");
					if (rating) {
						distribution = distribution + token + ',';
					}
				}
			}
			System.out.println(link + " (" + (i + 1) + "/" + movies.size()
					+ ")");
			System.out.println(distribution);
			System.out.println();
			output.print(link + ',');
			output.println(distribution);

		}
		output.close();
	}

	public static void parse(Set<String> movies, PrintStream output)
			throws IOException {
		List<String> movieList = new ArrayList<String>();
		for (String movie : movies) {
			movieList.add(movie);
		}
		parse(movieList, output);
	}

	public static Scanner scanPage(String link) throws IOException {
		URL url = new URL("http://www.imdb.com/title/" + link + "/ratings-usa");
		//Allocine.waitWhileError(url);
		InputStream searchStream = url.openStream();
		return new Scanner(searchStream);
	}
}
