import java.util.*;
import java.io.*;
import java.net.*;

// This program uses a base list created by ImdbWebsite.java
// It creates a combined list scraping data from bunch of other
// movie sites such as Allocine (French), SensaCine (Spanish)
// Filmstarts (German), AdoroCinema (Portuguese) and Beyazperde
// (Turkish). It then outputs a semicolon delimited file for
// comperative analysis.
// 
// When I began coding this I didn't realize that all the other
// movie databases belonged to Allocine so the program got a 
// little complicated to read towards later on.
//
// The basic idea is all of the above sites use the same movie
// IDs so I decided to combine all of them in this method.
public class SensaCine {

	// this maybe the single most complicated and ugly code I
	// have created until now so sorry about the potential
	// readibility issues.
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

			// (Below are for debugging purposes)
			// System.out.println(myMovie.imdbLink);
			// System.out.println(myMovie.imdbName);
			// System.out.println(myMovie.imdbNumVotes);
			// System.out.println(myMovie.imdbRating);
			// System.out.println(myMovie.imdbYear);
			// System.out.println();

			// try {
			// Thread.sleep(1500);
			// } catch (InterruptedException ex) {
			// Thread.currentThread().interrupt();
			// }

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

			HttpURLConnection test1 = (HttpURLConnection) url.openConnection();
			int status1 = test1.getResponseCode();
			while (status1 == 500 || status1 == 404 || status1 == 400
					|| status1 == 408) {
				test1 = (HttpURLConnection) url.openConnection();
				status1 = test1.getResponseCode();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			}
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
					// myMovieURL = new URL(
					// "http://www.allocine.fr/film/fichefilm_gen_cfilm="
					// + "115362" + ".html");
					HttpURLConnection test2 = (HttpURLConnection) myMovieURL
							.openConnection();
					int status2 = test2.getResponseCode();
					while (status2 == 500 || status2 == 404 || status2 == 400
							|| status2 == 408) {
						test2 = (HttpURLConnection) url.openConnection();
						status2 = test2.getResponseCode();
						try {
							Thread.sleep(1000);
						} catch (InterruptedException ex) {
							Thread.currentThread().interrupt();
						}
					}
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
							// Example string that we are looking for:
							// original</div></th><td>About Time</td>
						}
					}
					// various debugging
					// System.out.println(name);
					// System.out.println();

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
						String fr = frRatings(link);
						String tr = trRatings(link);
						String es = esRatings(link);
						String de = deRatings(link);
						String pt = ptRatings(link);
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
							myMovie.frNumStars = fr;
							myMovie.trNumStars = tr;
							myMovie.deNumStars = de;
							myMovie.esNumStars = es;
							myMovie.ptNumStars = pt;
							myMovie.alloLink = link;
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

	// Gets the movie ratings from each individual site. This particular
	// one is for the French site. I didn't initially realize that the
	// Sites were using the same database so my method was for this
	// individual site. I would have coded this much better without
	// copy pasting the code below and tweaking it for other sites but
	// I got lazy and I coded this for my own purposes anyways :)
	// The comments for below methods apply for the following methids
	// as well.
	//
	// This method takes a movieID, goes to the review section of that
	// movie in allocine, and scrapes the rating information. It returns
	// a string of ratings, semicolon delimited. The first number is the
	// number of votes, the second number is the average rating, the
	// remaining numbers are the distribution of starts as 5*, 4*, 3*,
	// 2*, 1* and 0*, for the WRITTEN REVIEWS. It doesn't explicitly
	// show distribution of ALL ratings.
	public static String frRatings(String link) throws IOException {
		String frResult = "0;0;";
		String frNumVotes = "0;";
		String frAvgRating = "0;";
		boolean ratingFound = false;
		boolean critFound = false;
		URL frLink = new URL("http://www.allocine.fr/film/fichefilm-" + link
				+ "/critiques/spectateurs/");

		// This chunk works brilliantly when internet is gone for a while
		// or you get a temporary response error from site. The recursive
		// nature makes it try again and again until int works. It may not
		// be the most efficient way but I tested it and it didn't take a
		// long enough time to become a concern.
		HttpURLConnection con = (HttpURLConnection) frLink.openConnection();
		int status = con.getResponseCode();
		if (status == 500 || status == 404 || status == 400) {
			System.out.println("French: 0;0;0;0;0;0;0;0; (" + status
					+ " error)");
			return "0;0;0;0;0;0;0;0;";
		} else if (status == 408) {
			return frRatings(link);
		}

		Scanner frScanner = new Scanner(frLink.openStream());

		while (frScanner.hasNext()) {
			String myToken = frScanner.next();

			// Gets the rating of the movie. There are multiple rating values
			// for other movies in the page but the first one belongs to the
			// Movie we are looking for. So it has a boolean flag that
			// prevents it to become modified on later occasions.
			if (!ratingFound && myToken.startsWith("itemprop=\"ratingValue\"")) {
				myToken = frScanner.next();
				frAvgRating = cleanRating(myToken);
				ratingFound = true;
				frResult = frAvgRating + ";";
				// for debugging:
				// System.out.println("frAvgRating: " + frAvgRating);
			}

			// Gets the number of votes cast.
			if (myToken.startsWith("itemprop=\"ratingCount\">")) {
				// removes everything non numerical and turns them into
				// relevant format.
				frNumVotes = myToken.replaceAll("[^\\d.]", "");
				frResult = frNumVotes + ";" + frResult;
				// System.out.println("frNumVotes: " + frNumVotes);
				// itemprop="ratingCount">
			}
			// If the movie has any written critiques this case
			// will find them and integrate them to the rating.
			// Sample text we are looking for:
			// <div class="stareval stars_medium">
			if (myToken.equals("stars_medium\">")) {
				critFound = true;
				String prev = "";
				while (!myToken.startsWith("critique")) {
					prev = myToken;
					// System.out.println(prev);
					myToken = frScanner.next();
				}
				// System.out.println("TEST");
				int finder = prev.length() - 1;
				while (prev.charAt(finder) != '>') {
					finder--;
					// System.out.println(finder);
				}
				frResult = frResult + prev.substring(finder + 1) + ";";
			}
		}

		boolean noRatings = false;
		// If there are no ratings it returns a string with bunch of
		// zeros. If there are ratings but no critiques it completes
		// the string with 6 zeros for the star critiques.
		if (frResult.equals("0;0;")) {
			frResult = "0;0;0;0;0;0;0;0;";
			noRatings = true;
		} else if (!critFound) {
			frResult = frResult + "0;0;0;0;0;0;";
		}
		frScanner.close();
		System.out.print("French: " + frResult);

		// keeps the user updated.
		if (noRatings) {
			System.out.print("(No Ratings)");
		}
		System.out.println();
		return frResult;
	}

	public static String trRatings(String link) throws IOException {
		String trResult = "0;0;";
		String trNumVotes = "0;";
		String trAvgRating = "0;";
		boolean ratingFound = false;
		boolean critFound = false;
		URL trLink = new URL("http://www.beyazperde.com/filmler/film-" + link
				+ "/kullanici-elestirileri/");
		HttpURLConnection con = (HttpURLConnection) trLink.openConnection();
		int status = con.getResponseCode();
		if (status == 500 || status == 404 || status == 400) {
			System.out.println("Turkish: 0;0;0;0;0;0;0;0; (" + status
					+ " error)");
			return "0;0;0;0;0;0;0;0;";
		} else if (status == 408) {
			return trRatings(link);
		}
		Scanner trScanner = new Scanner(trLink.openStream());

		// <div class="stareval stars_medium">
		while (trScanner.hasNext()) {
			String myToken = trScanner.next();
			if (!ratingFound && myToken.startsWith("itemprop=\"ratingValue\"")) {
				myToken = trScanner.next();
				trAvgRating = cleanRating(myToken);
				ratingFound = true;
				trResult = trAvgRating + ";";
				// System.out.println("frAvgRating: " + frAvgRating);
			}
			if (myToken.startsWith("itemprop=\"ratingCount\">")
					|| myToken.startsWith("data-prop=ratingCount")) {
				trNumVotes = myToken.replaceAll("[^\\d.]", "");
				// System.out.println(myToken);
				trResult = trNumVotes + ";" + trResult;
				// System.out.println("frNumVotes: " + frNumVotes);
				// itemprop="ratingCount">
			}

			if (myToken.equals("stars_medium\">")) {
				critFound = true;
				String prev = "";
				while (!myToken.startsWith("kritik")) {
					prev = myToken;
					// System.out.println(prev);
					myToken = trScanner.next();
				}
				// System.out.println("TEST");
				int finder = prev.length() - 1;
				while (prev.charAt(finder) != '>') {
					finder--;
					// System.out.println(finder);
				}
				trResult = trResult + prev.substring(finder + 1) + ";";
			}
		}

		boolean noRatings = false;
		if (trResult.equals("0;0;")) {
			trResult = "0;0;0;0;0;0;0;0;";
			noRatings = true;
		} else if (!critFound) {
			trResult = trResult + "0;0;0;0;0;0;";
		}
		trScanner.close();
		System.out.print("Turkish: " + trResult);
		if (noRatings) {
			System.out.print("(No Ratings)");
		}
		System.out.println();
		return trResult;
	}

	public static String esRatings(String link) throws IOException {
		String esResult = "0;0;";
		String esNumVotes = "0;";
		String esAvgRating = "0;";
		boolean ratingFound = false;
		boolean critFound = false;
		URL esLink = new URL("http://www.sensacine.com/peliculas/pelicula-"
				+ link + "/criticas-espectadores/");
		HttpURLConnection con = (HttpURLConnection) esLink.openConnection();
		int status = con.getResponseCode();
		if (status == 500 || status == 404 || status == 400) {
			System.out.println("Spanish: 0;0;0;0;0;0;0;0; (" + status
					+ " error)");
			return "0;0;0;0;0;0;0;0;";
		} else if (status == 408) {
			return esRatings(link);
		}
		Scanner esScanner = new Scanner(esLink.openStream());

		// <div class="stareval stars_medium">
		while (esScanner.hasNext()) {
			String myToken = esScanner.next();
			if (!ratingFound && myToken.startsWith("itemprop=\"ratingValue\"")) {
				myToken = esScanner.next();
				esAvgRating = cleanRating(myToken);
				ratingFound = true;
				esResult = esAvgRating + ";";
				// System.out.println("frAvgRating: " + frAvgRating);
			}
			if (myToken.startsWith("itemprop=\"ratingCount\">")) {
				esNumVotes = myToken.replaceAll("[^\\d.]", "");
				esResult = esNumVotes + ";" + esResult;
				// System.out.println("frNumVotes: " + frNumVotes);
				// itemprop="ratingCount">
			}
			if (myToken.equals("stars_medium\">")) {
				critFound = true;
				String prev = "";
				while (!myToken.startsWith("cr")) {
					prev = myToken;
					// System.out.println(prev);
					myToken = esScanner.next();
				}
				// System.out.println("TEST");
				int finder = prev.length() - 1;
				while (prev.charAt(finder) != '>') {
					finder--;
					// System.out.println(finder);
				}
				esResult = esResult + prev.substring(finder + 1) + ";";
			}
		}

		boolean noRatings = false;
		if (esResult.equals("0;0;")) {
			esResult = "0;0;0;0;0;0;0;0;";
			noRatings = true;
		} else if (!critFound) {
			esResult = esResult + "0;0;0;0;0;0;";
		}
		esScanner.close();
		System.out.print("Spanish: " + esResult);
		if (noRatings) {
			System.out.print("(No Ratings)");
		}
		System.out.println();
		return esResult;
	}

	public static String deRatings(String link) throws IOException {
		String deNumVotes = "0;";
		String deAvgRating = "0;";
		boolean ratingFound = false;
		boolean critFound = false;
		String deResult = "0;0;";
		URL deLink = new URL("http://www.filmstarts.de/kritiken/" + link
				+ "/userkritiken/");
		HttpURLConnection con = (HttpURLConnection) deLink.openConnection();
		int status = con.getResponseCode();
		if (status == 500 || status == 404 || status == 400) {
			System.out.println("German: 0;0;0;0;0;0;0;0; (" + status
					+ " error)");
			return "0;0;0;0;0;0;0;0;";
		} else if (status == 408) {
			return deRatings(link);
		}
		Scanner deScanner = new Scanner(deLink.openStream());

		// <div class="stareval stars_medium">
		while (deScanner.hasNext()) {
			String myToken = deScanner.next();
			if (!ratingFound && myToken.startsWith("itemprop=\"ratingValue\"")) {
				myToken = deScanner.next();
				deAvgRating = cleanRating(myToken);
				ratingFound = true;
				deResult = deAvgRating + ";";
				// System.out.println("frAvgRating: " + frAvgRating);
			}
			if (myToken.startsWith("itemprop=\"ratingCount\">")) {
				deNumVotes = myToken.replaceAll("[^\\d.]", "");
				deResult = deNumVotes + ";" + deResult;
				// System.out.println("frNumVotes: " + frNumVotes);
				// itemprop="ratingCount">
			}

			if (myToken.equals("stars_medium\">")) {
				critFound = true;
				String prev = "";
				while (!myToken.startsWith("Kritik")) {
					prev = myToken;
					// System.out.println(prev);
					myToken = deScanner.next();
				}
				// System.out.println("TEST");
				int finder = prev.length() - 1;
				while (prev.charAt(finder) != '>') {
					finder--;
					// System.out.println(prev.charAt(finder));
					// System.out.println(finder);
				}
				deResult = deResult + prev.substring(finder + 1) + ";";
			}
		}
		boolean noRatings = false;
		if (deResult.equals("0;0;")) {
			deResult = "0;0;0;0;0;0;0;0;";
			noRatings = true;
		} else if (!critFound) {
			deResult = deResult + "0;0;0;0;0;0;";
		}
		deScanner.close();
		System.out.print("German: " + deResult);
		if (noRatings) {
			System.out.print("(No Ratings)");
		}
		System.out.println();
		return deResult;
	}

	public static String getRatings(String language, String part1, String link,
			String part2) throws IOException {
		String numVotes = "0;";
		String avgRating = "0;";
		boolean ratingFound = false;
		boolean critFound = false;
		String result = "0;0;";
		URL myLink = new URL(part1 + link + part2);
		HttpURLConnection con = (HttpURLConnection) myLink.openConnection();
		int status = con.getResponseCode();
		if (status == 500 || status == 400) {
			System.out.println(language + "0;0;0;0;0;0;0;0; (" + status
					+ " error)");
			return "0;0;0;0;0;0;0;0;";
		} else if (status == 408 || status == 404 || status == 503) {
			return getRatings(language, part1, link, part2);
		}
		Scanner myScanner = new Scanner(myLink.openStream());

		// <div class="stareval stars_medium">
		while (myScanner.hasNext()) {
			String myToken = myScanner.next();
			if (!ratingFound && myToken.startsWith("itemprop=\"ratingValue\"")) {
				myToken = myScanner.next();
				avgRating = cleanRating(myToken);
				ratingFound = true;
				result = avgRating + ";";
				// System.out.println("frAvgRating: " + frAvgRating);
			}
			if (myToken.startsWith("itemprop=\"ratingCount\">")) {
				numVotes = myToken.replaceAll("[^\\d.]", "");
				result = numVotes + ";" + result;
				// System.out.println("frNumVotes: " + frNumVotes);
				// itemprop="ratingCount">
			}
			if (myToken.equals("stars_medium\">")) {
				critFound = true;
				String prev = "";
				while (!myToken.startsWith("cr")) {
					prev = myToken;
					// System.out.println(prev);
					myToken = myScanner.next();
				}
				// System.out.println("TEST");
				int finder = prev.length() - 1;
				while (prev.charAt(finder) != '>') {
					finder--;
					// System.out.println(finder);
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

	public static String ptRatings(String link) throws IOException {
		String ptNumVotes = "0;";
		String ptAvgRating = "0;";
		boolean ratingFound = false;
		boolean critFound = false;
		String ptResult = "0;0;";
		URL ptLink = new URL("http://www.adorocinema.com/filmes/filme-" + link
				+ "/criticas/espectadores/");
		HttpURLConnection con = (HttpURLConnection) ptLink.openConnection();
		int status = con.getResponseCode();
		if (status == 500 || status == 400) {
			System.out.println("Portuguese: 0;0;0;0;0;0;0;0; (" + status
					+ " error)");
			return "0;0;0;0;0;0;0;0;";
		} else if (status == 408 || status == 404 || status == 503) {
			return ptRatings(link);
		}
		Scanner ptScanner = new Scanner(ptLink.openStream());

		// <div class="stareval stars_medium">
		while (ptScanner.hasNext()) {
			String myToken = ptScanner.next();
			if (!ratingFound && myToken.startsWith("itemprop=\"ratingValue\"")) {
				myToken = ptScanner.next();
				ptAvgRating = cleanRating(myToken);
				ratingFound = true;
				ptResult = ptAvgRating + ";";
				// System.out.println("frAvgRating: " + frAvgRating);
			}
			if (myToken.startsWith("itemprop=\"ratingCount\">")) {
				ptNumVotes = myToken.replaceAll("[^\\d.]", "");
				ptResult = ptNumVotes + ";" + ptResult;
				// System.out.println("frNumVotes: " + frNumVotes);
				// itemprop="ratingCount">
			}
			if (myToken.equals("stars_medium\">")) {
				critFound = true;
				String prev = "";
				while (!myToken.startsWith("cr")) {
					prev = myToken;
					// System.out.println(prev);
					myToken = ptScanner.next();
				}
				// System.out.println("TEST");
				int finder = prev.length() - 1;
				while (prev.charAt(finder) != '>') {
					finder--;
					// System.out.println(finder);
				}
				ptResult = ptResult + prev.substring(finder + 1) + ";";
			}
		}

		boolean noRatings = false;
		if (ptResult.equals("0;0;")) {
			ptResult = "0;0;0;0;0;0;0;0;";
			noRatings = true;
		} else if (!critFound) {
			ptResult = ptResult + "0;0;0;0;0;0;";
		}
		ptScanner.close();
		System.out.print("Portuguese: " + ptResult);
		if (noRatings) {
			System.out.print("(No Ratings)");
		}
		System.out.println();
		return ptResult;
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
		output.print(myMovie.imdbName + ";");
		output.print(myMovie.imdbYear + ";");
		output.print(myMovie.imdbLink + ";");
		output.print(myMovie.alloLink + ";");
		output.print(myMovie.imdbNumVotes + ";");
		output.print(myMovie.imdbRating + ";");
		output.print(myMovie.frNumStars);
		output.print(myMovie.esNumStars);
		output.print(myMovie.ptNumStars);
		output.print(myMovie.deNumStars);
		output.print(myMovie.trNumStars);
		output.println();

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

}