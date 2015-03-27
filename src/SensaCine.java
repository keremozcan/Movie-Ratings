import java.util.*;
import java.io.*;
import java.net.*;

//This program uses a base list created by ImdbWebsite.java
public class SensaCine {

	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
		System.setProperty("http.proxyHost", "proxy.mydomain.com");
		System.setProperty("http.proxyPort", "8080");
		Map<String, Movie> movies = readImdbFile();
		PrintStream output = new PrintStream("combined"
				+ System.currentTimeMillis() + ".txt");
		movies = alloMovies(movies, output, startTime);

	}

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

	public static Map<String, Movie> alloMovies(Map<String, Movie> imdbMovies,
			PrintStream output, long startTime) throws IOException {
		Map<String, Movie> alloMovies = new TreeMap<String, Movie>();
		int numPage = 1; // currently a total of 1831 for 2010s
		int firstPage = numPage - 1;
		int totalPage = 1831 - numPage;
		URL url = new URL(
				"http://www.allocine.fr/films/decennie-2010/alphabetique/?page="
						+ numPage);

		boolean lastPage = false;
		while (!lastPage) {
			InputStream searchStream = url.openStream();
			Scanner myPage = new Scanner(searchStream);
			while (myPage.hasNext()) {
				String token = myPage.next();
				if (token.startsWith("href=\"/film/fichefilm_gen_cfilm=")) {
					int i = 33;
					while (token.charAt(i) != '.') {
						i++;
					}
					String link = token.substring(32, i);
					//link = "227258";

					System.out.print(".");

					URL myMovieURL = new URL(
							"http://www.allocine.fr/film/fichefilm_gen_cfilm="
									+ link + ".html");
					// myMovieURL = new URL(
					// "http://www.allocine.fr/film/fichefilm_gen_cfilm="
					// + "115362" + ".html");
					InputStream pageStream = myMovieURL.openStream();
					Scanner myMovieScanner = new Scanner(pageStream);
					boolean frenchTitle = false;
					String name = "";
					while (myMovieScanner.hasNext()) {
						String movieToken = myMovieScanner.next();

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
							// original</div></th><td>About Time</td>
						}
					}
					// System.out.println(name);
					// System.out.println();
					if (imdbMovies.containsKey(name)) {
						System.out.println();
						System.out.println(link);
						System.out.println(name);
						String fr = frRatings(link);
						String tr = trRatings(link);
						String es = esRatings(link);
						String de = deRatings(link);
						String pt = ptRatings(link);
						boolean allNull = (fr.equals("0;0;0;0;0;0;0;0;")
								&& fr.equals(tr) && fr.equals(de)
								&& fr.equals(es) && fr.equals(pt));
						System.out.println();
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
					// http://www.allocine.fr/film/fichefilm_gen_cfilm=
				} else if (token.equals("btn-disabled\">Suivante<i")) {
					System.out.println("last page!");
					lastPage = true;
				}
			}

			System.out.println();
			System.out.println("Size: " + alloMovies.size());
			System.out.print(((100 * (numPage - firstPage)) / totalPage)
					+ "% done in ");
			System.out.print(timer(startTime));
			System.out.println(" (page " + numPage + ")");
			System.out.println();
			numPage++;
			url = new URL(
					"http://www.allocine.fr/films/decennie-2010/alphabetique/?page="
							+ numPage);
			myPage.close();
		}

		return alloMovies;
	}

	public static String frRatings(String link) throws IOException {
		String frResult = "0;0;";
		String frNumVotes = "0;";
		String frAvgRating = "0;";
		boolean ratingFound = false;
		boolean critFound = false;
		URL frLink = new URL("http://www.allocine.fr/film/fichefilm-" + link
				+ "/critiques/spectateurs/");
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

		// <div class="stareval stars_medium">
		while (frScanner.hasNext()) {
			String myToken = frScanner.next();
			if (!ratingFound && myToken.startsWith("itemprop=\"ratingValue\"")) {
				myToken = frScanner.next();
				String avg = myToken.replaceAll("[^\\d.]", "");
				avg = avg.substring(avg.length() - 2, avg.length());
				frAvgRating = avg.charAt(0) + "." + avg.charAt(1);
				ratingFound = true;
				frResult = frAvgRating + ";";
				// System.out.println("frAvgRating: " + frAvgRating);
			}
			if (myToken.startsWith("itemprop=\"ratingCount\">")) {
				frNumVotes = myToken.replaceAll("[^\\d.]", "");
				frResult = frNumVotes + ";" + frResult;
				// System.out.println("frNumVotes: " + frNumVotes);
				// itemprop="ratingCount">
			}
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
		if (frResult.equals("0;0;")) {
			frResult = "0;0;0;0;0;0;0;0;";
			noRatings = true;
		} else if (!critFound) {
			frResult = frResult + "0;0;0;0;0;0;";
		}
		frScanner.close();
		System.out.print("French: " + frResult);
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
				String avg = myToken.replaceAll("[^\\d.]", "");
				avg = avg.substring(avg.length() - 2, avg.length());
				trAvgRating = avg.charAt(0) + "." + avg.charAt(1);
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
				String avg = myToken.replaceAll("[^\\d.]", "");
				avg = avg.substring(avg.length() - 2, avg.length());
				esAvgRating = avg.charAt(0) + "." + avg.charAt(1);
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
				String avg = myToken.replaceAll("[^\\d.]", "");
				avg = avg.substring(avg.length() - 2, avg.length());
				deAvgRating = avg.charAt(0) + "." + avg.charAt(1);
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
		if (status == 500 || status == 404 || status == 400) {
			System.out.println("Portuguese: 0;0;0;0;0;0;0;0; (" + status
					+ " error)");
			return "0;0;0;0;0;0;0;0;";
		} else if (status == 408) {
			return ptRatings(link);
		}
		Scanner ptScanner = new Scanner(ptLink.openStream());

		// <div class="stareval stars_medium">
		while (ptScanner.hasNext()) {
			String myToken = ptScanner.next();
			if (!ratingFound && myToken.startsWith("itemprop=\"ratingValue\"")) {
				myToken = ptScanner.next();
				String avg = myToken.replaceAll("[^\\d.]", "");
				avg = avg.substring(avg.length() - 2, avg.length());
				ptAvgRating = avg.charAt(0) + "." + avg.charAt(1);
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

	public static String timer(long startTime) {
		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime) / 1000;
		return (duration / 60 + " minutes");
	}

	// http://www.allocine.fr/films/alphabetique/?page=2
	// href="/film/fichefilm_gen_cfilm=
	// btn-disabled">Suivante<i

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

}