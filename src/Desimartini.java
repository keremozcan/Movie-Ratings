import java.util.*;
import java.io.*;
import java.net.*;

public class Desimartini {
	public static void main(String args[]) throws IOException {
		long startTime = System.currentTimeMillis();
		System.setProperty("http.proxyHost", "proxy.mydomain.com");
		System.setProperty("http.proxyPort", "8080");
		Map<String, Movie> movies = Movie.readImdbFile("imdbList.txt", ',');
		PrintStream output = new PrintStream("desi"
				+ System.currentTimeMillis() + ".txt");
		desiMovies(movies, output, startTime);
	}
	
	public static void desiMovies(Map<String, Movie> movies,
			PrintStream output, long startTime) throws IOException {
		for (String key : movies.keySet()) {
			System.out.println(movies.get(key));
		}
		Stack<String> reverse = new Stack<String>();
		for (String key : movies.keySet()) {
			reverse.push(key);
		}
		/*for (String key : reverse)*/
		while (!reverse.isEmpty()){
			String keyTemp = reverse.pop();
			Movie myMovie = movies.get(keyTemp);
			Scanner input = ratingScanner(myMovie.imdbName,
					myMovie.imdbDirector);
			if (input != null) {
				myMovie.desiDist = getRatings(input);
				System.out.println("found! Ratings:" + myMovie.desiDist);
				output.println(myMovie.imdbString() + myMovie.desiString());
			} else {
				output.println(myMovie.imdbString() + "0,0,0,0,0,");
				System.out
						.println("Not found: Director mismatch or Movie isn't there!");
			}
			System.out.println();
		}
	}
	
	public static Scanner ratingScanner(String name, String director)
			throws IOException {

		System.out.println(name);
		URL urlSearch = new URL("http://www.desimartini.com/search/movies/"
				+ name.replace(' ', '-') + '-' + director + '/');
		Allocine.waitWhileError(urlSearch);
		HttpURLConnection test = (HttpURLConnection) urlSearch.openConnection();
		int status = test.getResponseCode();
		if (status != 500 && status != 400 && status != 502) {
			InputStream searchPage = urlSearch.openStream();
			return ratingScanner(new Scanner(searchPage), director);
		} else {
			System.out.println(name + " not found! (" + status + " error)");
			return null;
		}
	}

	public static Scanner ratingScanner(Scanner searchPage, String imdbDirector)
			throws IOException {
		boolean reviewFound = false;
		boolean noResults = false;
		boolean directorFound = false;
		String desiDirector = "";
		String token = searchPage.next();
		while (!reviewFound && !noResults && searchPage.hasNext()) {
			token = searchPage.next();
			if (!directorFound && token.equals("temp3")) {
				searchPage.next();
				token = searchPage.next();
				desiDirector = token;
				token = searchPage.next();
				while (!token.startsWith("\"")) {
					desiDirector += " " + token;
					token = searchPage.next();
				}
				desiDirector = desiDirector.substring(1);
				directorFound = true;
				System.out.println(desiDirector + "?");

			} else if (token.startsWith("href=\"/reviews")) {
				reviewFound = true;
			} else if (token.startsWith("id=\"no_search_results")) {
				noResults = true;
			}
		}

		if (noResults || !imdbDirector.equalsIgnoreCase(desiDirector)) {
			return null;
		}

		while (token.charAt(token.length() - 1) != '"') {
			token = token.substring(0, token.length() - 1);
			// System.out.println(token);
		}

		URL urlReview = new URL("http://www.desimartini.com/"
				+ token.substring(7, token.length() - 1));
		Allocine.waitWhileError(urlReview);
		InputStream reviewPage = urlReview.openStream();
		// System.out.println(urlReview);
		return new Scanner(reviewPage);
	}



	public static String getRatings(Scanner input) {
		int ratingCount = 0;
		String result = "";
		boolean ratingBegins = false;
		boolean ratingEnds = false;
		int currentIndex = 0;
		while (ratingCount < 5 && input.hasNext()) {
			String token = input.next();
			if (token.startsWith("style=\"width")) {
				// System.out.println(token);
				// System.out.println("Rating Count =" + ratingCount);
				while (!ratingEnds) {

					if (token.length() <= currentIndex) {
						return null;
					}

					char myChar = token.charAt(currentIndex);

					if (myChar == '(') {

						ratingBegins = true;
					} else if (ratingBegins && (myChar != ')')
							&& (myChar != ',')) {
						// System.out.print(myChar);
						result += myChar;
					} else if (ratingBegins && (myChar == ')')) {
						result += ',';
						ratingEnds = true;
					}
					currentIndex++;
				}
				ratingBegins = false;
				ratingEnds = false;
				currentIndex = 0;
				ratingCount++;
			}
		}
		return result;
	}
}
