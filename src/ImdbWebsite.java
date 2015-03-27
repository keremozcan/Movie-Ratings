import java.util.*;
import java.io.*;
import java.net.*;

public class ImdbWebsite {
	public static void main(String args[]) throws IOException {
		long startTime = System.currentTimeMillis();
		System.setProperty("http.proxyHost", "proxy.mydomain.com");
		System.setProperty("http.proxyPort", "8080");

		Set<String> movieLinks = new LinkedHashSet<String>();

		// Enter the beginning and end years to search movies within those years
		// Enter the same year for both for searching only one year
		// numMinVotes is the minimum number of votes a movie should take in
		// order to be included in the final output.
		int begYear = 2013;
		int endYear = 2013;
		int numMinVotes = 10;

		int myMoviePage = 1;
		Scanner input = createUrlScanner(myMoviePage, begYear, endYear);
		PrintStream output = new PrintStream("imdb"
				+ System.currentTimeMillis() + ".txt");

		int numMovies = findNumMovies(input);

		// goes through each page in the search results. calls findLinksInPage
		// to fetch all the links in page and adds them to a Set.
		while (myMoviePage < numMovies) {
			input = createUrlScanner(myMoviePage, begYear, endYear);
			findLinksInPage(movieLinks, input);
			myMoviePage += 50;

			// keeps user updated about the process.
			System.out.print("%" + (100 * movieLinks.size() / numMovies));
			System.out.println("(" + movieLinks.size() + " of " + numMovies
					+ " titles)");

			// I added a 1 second pause because otherwise IMDB bans me.
			// Maybe a shorter pause could be OK as well. I didn't try.
			// (1000 milliseconds equals one second)
			try {
				Thread.sleep(500); 
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}

		// keeps user updated about the process
		long endOfFetching = timer("link fetching", startTime);

		for (String link : movieLinks) {
			input = createMovieScanner(link);
			String name = null;
			String year = null;
			String rating = null;
			String numVotes = null;
			int numVotesInt = 0;
			boolean foundName = false;
			boolean foundYear = false;
			boolean foundRating = false;
			boolean foundNumVotes = false;
			
			// goes through the source code and parses it.
			while (input.hasNext()) {
				String token = input.next();
				if (!foundName && token.startsWith("itemprop=\"name\">")) {
					String title = token;
					while (!token.endsWith("</span>")) {
						token = input.next();
						title = title + " " + token;
					}
					name = title.substring(16, title.length() - 7);
					name.replaceAll(";", ",:");
					foundName = true;
				} else if (!foundYear && token.endsWith("</a>)</span>")) {
					year = token.substring(1, 5);
					foundYear = true;
				} else if (!foundRating
						&& token.startsWith("itemprop=\"ratingValue\">")) {
					rating = token.substring(23, 26);
					foundRating = true;
				} else if (!foundNumVotes
						&& token.startsWith("itemprop=\"ratingCount\">")) {
					numVotes = removeCommas(token.substring(23,
							token.length() - 7));
					numVotesInt = Integer.parseInt(numVotes);
					foundNumVotes = true;
				}
			}

			// the program doesn't print the movie to output file if it doesn't
			// have enough votes or if it's not a movie in the first place (such
			// as a TV show, video game etc.). The year information for non
			// movie titles are coded differently so they don't pass the
			// foundDate test.
			if (foundName && foundYear && foundRating && foundNumVotes
					&& (numVotesInt >= numMinVotes)) {
				System.out.println();
				System.out.println("\"" + name + "\" added!");
				System.out.println("year:\t" + year);
				System.out.println("rating:\t" + rating);
				System.out.println("votes:\t" + numVotes);
				output.println(year + ";" + link + ";" + rating + ";"
						+ numVotes + ";" + name);
			} else {
				System.out.println();
				System.out.println("\"" + name
						+ "\" doesn't fit requirements.");
			}

		}

		timer("Creating output file", endOfFetching);
		timer("Running program", startTime);
		output.close();
	}

	// Creates a search result URL scanner from the given number of initial
	// movie i, within the given search string of beginning and end years (both
	// inclusive)
	public static Scanner createUrlScanner(int i, int begYear, int endYear)
			throws IOException {
		URL urlSearch = new URL(
				"http://www.imdb.com/search/title?at=0&sort=alpha&start=" + i
						+ "&title_type=feature&year=" + begYear + "," + endYear);
		InputStream searchStream = urlSearch.openStream();
		return new Scanner(searchStream);
	}

	// Creates a movie page URL scanner from the given movie ID string
	public static Scanner createMovieScanner(String link) throws IOException {
		URL urlSearch = new URL("http://www.imdb.com/title/" + link + '/');
		InputStream searchStream = urlSearch.openStream();
		return new Scanner(searchStream);
	}

	// Finds number of movies for the given search string and returns that
	// number as an int.
	public static int findNumMovies(Scanner searchScanner) {
		while (!searchScanner.next().equals("id=\"left\">")) {
		}
		searchScanner.next();
		searchScanner.next();
		String numMovies = searchScanner.next();
		return Integer.parseInt(removeCommas(numMovies));
	}

	// removes commas from a given number in String form
	public static String removeCommas(String number) {
		int numCommas = number.length() / 4;
		for (int i = 1; i <= numCommas; i++) {
			number = number.substring(0, number.length() - 3 * i - 1)
					+ number.substring(number.length() - 3 * i);
		}
		return number;
	}

	// finds the movie links in the given page and puts them into a set.
	public static void findLinksInPage(Set<String> links, Scanner searchScanner) {
		while (searchScanner.hasNext()) {
			String token = searchScanner.next();
			if (token.startsWith("href=\"/title/")) {
				char test = ' ';
				int endIndex = 15;
				while (test != '/') {
					endIndex++;
					test = token.charAt(endIndex);
				}
				links.add(token.substring(13, endIndex));
			}
		}
	}

	// checks how long it has been since the given time.
	// returns end time.
	public static long timer(String process, long startTime) {
		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime) / 1000;
		System.out.println();
		System.out.println(process + " done! It took " + duration / 60
				+ " minutes and " + duration % 60 + " seconds");
		return endTime;
	}

}
