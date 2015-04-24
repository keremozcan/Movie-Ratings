import java.util.*;
import java.io.*;
import java.net.*;

public class Douban {
	public static void main(String args[]) throws IOException {
		long startTime = System.currentTimeMillis();
		System.setProperty("http.proxyHost", "proxy.mydomain.com");
		System.setProperty("http.proxyPort", "8080");
		Map<String, Movie> movies = Movie.readImdbFile("allcombined.txt", ',');
		PrintStream output = new PrintStream("douban"
				+ System.currentTimeMillis() + ".txt");
		doubanMovies(movies, output, startTime);
	}

	public static void doubanMovies(Map<String, Movie> movies, PrintStream output, long startTime)
			throws IOException {
		int count = 0;
		int totalSize = movies.keySet().size();
		for (String key : movies.keySet()) {
			Movie myMovie = movies.get(key);
			myMovie.doubanLink = getDoubanLink(myMovie);
			if (myMovie.doubanLink != null) {
				myMovie.doubanDist = getDoubanDist(myMovie);
			} else {
				myMovie.doubanLink = "0";
				myMovie.doubanDist = "0,0,0,0,0,";
			}
			output.println(myMovie.imdbString() + myMovie.doubanString());
			count++;
			System.out.println("(" + count + '/' + totalSize + ')');
			if (count % 10 == 0) {
				ImdbWebsite.timer(count + " movies" ,startTime);
			}
			System.out.println();
		}
	}

	public static String getDoubanLink(Movie myMovie) throws IOException {
		URL urlSearch = new URL(
				"http://movie.douban.com/subject_search?search_text="
						+ myMovie.imdbLink + "&cat=1002");
		HttpURLConnection test = (HttpURLConnection) urlSearch.openConnection();
		int status = test.getResponseCode();
		if (status != 500 && status != 400) {
			InputStream searchPage = urlSearch.openStream();
			Scanner searchPageScanner = new Scanner(searchPage);
			while (searchPageScanner.hasNext()) {
				String token = searchPageScanner.next();
				if (token.startsWith("id=\"collect_form_")) {
					searchPageScanner.close();
					return token.replaceAll("[^\\d.]", "");
				}
			}
			System.out.println(myMovie.imdbName
					+ " not found! No more tokens in page.");
			searchPageScanner.close();
			return null;
		} else {
			System.out.println(myMovie.imdbName + " not found! (" + status
					+ " error)");
			return null;
		}
	}

	public static String getDoubanDist(Movie myMovie) throws IOException {
		URL urlRatingPage = new URL("http://movie.douban.com/subject/"
				+ myMovie.doubanLink + "/collections");
		HttpURLConnection test = (HttpURLConnection) urlRatingPage
				.openConnection();
		int status = test.getResponseCode();
		if (status != 500 && status != 400) {
			InputStream ratingPage = urlRatingPage.openStream();
			Scanner ratingPageScanner = new Scanner(ratingPage);
			while (ratingPageScanner.hasNext()) {
				String token = ratingPageScanner.next();
				int ratingCount = 0;
				if (token.equals("<h2>")) {
					String ratingDist = "";
					token = ratingPageScanner.next();
					double numVotes = Double.parseDouble(token.replaceAll(
							"[^\\d.]", ""));
					System.out.println(myMovie.imdbName);
					System.out.println((int) numVotes);
					while (ratingCount < 5) {
						token = ratingPageScanner.next();
						if (token.endsWith("</div>")) {
							token = ratingPageScanner.next();
							double myRatingPercent = Double.parseDouble(token
									.replaceAll("[^\\d.]", ""));
							int myRating = (int) ((myRatingPercent * numVotes / 100) + 0.5);
							System.out.println(myRating);
							ratingDist = ratingDist + myRating + ",";
							ratingCount++;
						}
					}
					System.out.println(ratingDist);
					ratingPageScanner.close();
					return ratingDist;
				}
			}
			System.out.println(myMovie.imdbName
					+ " not found! No more tokens in rating page.");
			ratingPageScanner.close();
			return "0,0,0,0,0,";
		} else {
			System.out.println(myMovie.imdbName + " rating page not found! ("
					+ status + " error)");
			return "0,0,0,0,0,";
		}
	}
}