import java.util.*;
import java.io.*;
import java.net.*;

public class Allocine {

	// This program uses a base list created by ImdbWebsite.java
	// It creates a combined list scraping data from bunch of other
	// movie sites, namely; Allocine (French), SensaCine (Spanish)
	// Filmstarts (German), AdoroCinema (Portuguese) and Beyazperde
	// (Turkish). It then outputs a semicolon delimited file for
	// comparative analysis.
	//
	// The basic idea is all of the above sites use the same movie
	// IDs so I decided to combine all of them in this method.
	//
	// When I began coding this I didn't realize that all the other
	// movie databases belonged to Allocine so the program got a
	// little complicated to read towards later on. so sorry I tried
	// to create a comprehensive scraper. Sorry about the potential
	// readability issues this might cause.
	//
	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
		System.setProperty("http.proxyHost", "proxy.mydomain.com");
		System.setProperty("http.proxyPort", "8080");
		// reads the imdb file and creates a Map of Movie objects.
		// The keys are the names of the movies.
		Map<String, Movie> movies = readImdbFile();
		PrintStream output = new PrintStream("combined"
				+ System.currentTimeMillis() + ".txt");
		// this combines them and prints them.
		movies = alloMovies(movies, output, startTime);

	}

	// reads the given imdbList file and creates a map.
	// Keys are the names of movies and values are Movie
	// objects.

	public static Map<String, Movie> readImdbFile()
			throws FileNotFoundException {
		Scanner input = new Scanner(new File("imdbList.txt"));
		Map<String, Movie> movies = new TreeMap<String, Movie>();
		while (input.hasNextLine()) {
			String line = input.nextLine();
			int n = 21;
			while (!line.substring(n).startsWith(";")) {
				n++;
			}
			String year = line.substring(0, 4);
			String link = line.substring(5, 14);
			String rating = line.substring(15, 18);
			String numVotes = line.substring(19, n);
			String name = line.substring(n + 1);

			Movie myMovie = new Movie(year, link, rating, numVotes, name);
			movies.put(myMovie.imdbName, myMovie);

		}
		input.close();
		return movies;
	}

	// This method takes the imdbMovies map and creates a combined list
	// from Allocine and other movie databases.
	public static Map<String, Movie> alloMovies(Map<String, Movie> imdbMovies,
			PrintStream output, long startTime) throws IOException {
		Map<String, Movie> alloMovies = new TreeMap<String, Movie>();

		// Change the below numPage if the process is interrupted for
		// some reason.
		int numPage = 1046;
		// The below variables are just for reference to keep you updated
		// during the process.
		// currently a total of 1831 pages for 2010s
		int firstPage = numPage - 1;
		int totalPage = 1831 - numPage;

		// The URL below should be changed if the movie year range you are
		// looking for is not between 2010-2019. It goes to the French
		// database (which has the most movies) goes through every page.
		URL url = new URL(
				"http://www.allocine.fr/films/decennie-2010/alphabetique/?page="
						+ numPage);

		boolean lastPage = false;
		// unless it is the last page it keeps running.
		while (!lastPage) {
			waitWhileError(url);
			InputStream searchStream = url.openStream();
			Scanner myPage = new Scanner(searchStream);
			while (myPage.hasNext()) {
				String token = myPage.next();
				if (token.startsWith("href=\"/film/fichefilm_gen_cfilm=")) {
					int i = 33;
					while (token.charAt(i) != '.') {
						i++;
					}
					// This is the movie ID found int the page.
					// This id is the same across all 5 sites
					// listed above.
					String link = token.substring(32, i);
					// below is for debugging purposes.
					// link = "225966";

					System.out.print(".");

					URL myMovieURL = new URL(
							"http://www.allocine.fr/film/fichefilm_gen_cfilm="
									+ link + ".html");
					waitWhileError(myMovieURL);
					InputStream pageStream = myMovieURL.openStream();
					Scanner myMovieScanner = new Scanner(pageStream);
					boolean frenchTitle = false;
					String name = "";
					while (myMovieScanner.hasNext()) {
						String movieToken = myMovieScanner.next();

						// checks the title of the movie. Since the base
						// site is in French, the title is French in most
						// cases. If the movie is in another language the
						// below if clause takes care of that case.
						if (!frenchTitle) {
							if (movieToken.endsWith("<title>")) {
								String add = myMovieScanner.next();
								name = name + add;
								add = myMovieScanner.next();
								while (!add.equals("-")) {
									name = name + " " + add;
									add = myMovieScanner.next();
								}
							}
						}

						// Checks if the movie has a title in "original title"
						// if it does, it replaces the tile with the original
						// title, since this is how we obtain the information
						// from imdb as well.
						if (movieToken.startsWith("original</div></th><td>")) {
							int n = 23;
							while (movieToken.charAt(n) != '<') {
								n++;
								if (movieToken.length() <= n) {
									movieToken = movieToken + " "
											+ myMovieScanner.next();
								}
							}
							name = movieToken.substring(23, n);
						}
					}

					// Checks if the imdb movies has the same title. This
					// is necessary because the only way to scrape movies
					// from AlloCine comprehensively is scraping them by
					// decade. This is because some movies in Allocine
					// database do not have the year information and others
					// are different from the imdb database numbers.
					if (imdbMovies.containsKey(name)) {
						System.out.println();
						System.out.println(link);
						System.out.println(name);
						// gets the ratings from each site

						URL frLink = new URL(
								"http://www.allocine.fr/film/fichefilm-" + link
										+ "/critiques/spectateurs/");
						URL trLink = new URL(
								"http://www.beyazperde.com/filmler/film-"
										+ link + "/kullanici-elestirileri/");
						URL esLink = new URL(
								"http://www.sensacine.com/peliculas/pelicula-"
										+ link + "/criticas-espectadores/");
						URL deLink = new URL(
								"http://www.filmstarts.de/kritiken/" + link
										+ "/userkritiken/");
						URL ptLink = new URL(
								"http://www.adorocinema.com/filmes/filme-"
										+ link + "/criticas/espectadores/");
						String fr = getRatings("French: ", "critique", frLink);
						String es = getRatings("Spanish: ", "cr", esLink);
						String pt = getRatings("Portuguese: ", "cr", ptLink);
						String de = getRatings("German: ", "Kritik", deLink);
						String tr = getRatings("Turkish: ", "kritik", trLink);
						// checks if it's all null
						boolean allNull = (fr.equals("0;0;0;0;0;0;0;0;")
								&& fr.equals(tr) && fr.equals(de)
								&& fr.equals(es) && fr.equals(pt));
						System.out.println();
						// if not at least one title has one rating then
						// it saves it to database and prints out to the
						// output.
						if (!allNull) {
							Movie myMovie = imdbMovies.get(name);
							myMovie.addAllocineRatings(fr, es, pt, de, tr, link);
							alloMovies.put(name, myMovie);
							printFile(myMovie, output);
						} else {
							System.out.println("No ratings found, not saving!");
							System.out.println();
						}

					}

					myMovieScanner.close();
					// If it finds the last page, it terminates the
					// program once it's done.
				} else if (token.equals("btn-disabled\">Suivante<i")) {
					System.out.println("last page!");
					lastPage = true;
				}
			}

			// Keeps the user updated about the process.
			System.out.println();
			System.out.println("Size: " + alloMovies.size());
			System.out.print(((100 * (numPage - firstPage)) / totalPage)
					+ "% done in ");
			System.out.print(timer(startTime));
			System.out.println(" (page " + numPage + ")");
			System.out.println();

			// Goes to the next search page.
			numPage++;
			url = new URL(
					"http://www.allocine.fr/films/decennie-2010/alphabetique/?page="
							+ numPage);
			myPage.close();
		}

		return alloMovies;
	}

	// This method takes a movie link, goes to the review section of that
	// movie in alloCine, and scrapes the rating information. It returns
	// a string of ratings, semicolon delimited. The first number is the
	// number of votes, the second number is the average rating, the
	// remaining numbers are the distribution of starts as 5*, 4*, 3*,
	// 2*, 1* and 0*, for the WRITTEN REVIEWS. It doesn't explicitly
	// show distribution of ALL ratings.

	public static String getRatings(String language, String critique, URL myLink)
			throws IOException {
		String numVotes = "0;";
		String avgRating = "0;";
		boolean ratingFound = false;
		boolean critFound = false;
		String result = "0;0;";

		waitWhileError(myLink);
		HttpURLConnection con = (HttpURLConnection) myLink.openConnection();
		int status = con.getResponseCode();
		if (status == 500 || status == 400 || status == 404) {
			System.out.println(language + "0;0;0;0;0;0;0;0; (" + status
					+ " error)");
			return "0;0;0;0;0;0;0;0;";
		}

		Scanner myScanner = new Scanner(myLink.openStream());
		while (myScanner.hasNext()) {
			String myToken = myScanner.next();
			if (!ratingFound && myToken.startsWith("itemprop=\"ratingValue\"")) {
				myToken = myScanner.next();
				avgRating = cleanRating(myToken);
				ratingFound = true;
				result = avgRating + ";";
			}
			if (myToken.startsWith("itemprop=\"ratingCount\">")) {
				numVotes = myToken.replaceAll("[^\\d.]", "");
				result = numVotes + ";" + result;
			}
			if (myToken.equals("stars_medium\">")) {
				critFound = true;
				String prev = "";
				while (!myToken.startsWith(critique)) {
					prev = myToken;
					myToken = myScanner.next();
				}
				int finder = prev.length() - 1;
				while (prev.charAt(finder) != '>') {
					finder--;
				}
				result = result + prev.substring(finder + 1) + ";";
			}
		}
		boolean noRatings = false;
		if (result.equals("0;0;")) {
			result = "0;0;0;0;0;0;0;0;";
			noRatings = true;
		} else if (!critFound) {
			result = result + "0;0;0;0;0;0;";
		}
		myScanner.close();
		System.out.print(language + result);
		if (noRatings) {
			System.out.print("(No Ratings)");
		}
		System.out.println();
		return result;
	}

	// Times the process
	// keeps the user updated.
	public static String timer(long startTime) {
		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime) / 1000;
		return (duration / 60 + " minutes");
	}

	// Prints the Movie object to the provided file.
	public static void printFile(Movie myMovie, PrintStream output) {
		output.println(myMovie);

	}

	// removes everything non numerical and turns them into
	// relevant format.
	public static String cleanRating(String myToken) {
		String avg = myToken.replaceAll("[^\\d.]", "");
		if (avg.charAt(1) == '.') {
			avg = avg.substring(0, 1) + avg.substring(2);
		}
		avg = avg.replaceAll("[^\\d.]", "");
		avg = avg.substring(avg.length() - 2, avg.length());
		return avg.charAt(0) + "." + avg.charAt(1);
	}

	// Waits while the internet connection is gone or the server does not
	// respond for some reason. It takes an integer and number of seconds
	// it should try reconnect.
	public static void waitWhileError(URL url) throws IOException {
		HttpURLConnection test = (HttpURLConnection) url.openConnection();
		int status = test.getResponseCode();
		while (status == 503 || status == 404 || status == 400 || status == 408) {
			test = (HttpURLConnection) url.openConnection();
			status = test.getResponseCode();
			try {
				Thread.sleep(1000);
				waitWhileError(url);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
