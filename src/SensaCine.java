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
		movies = alloMovies(movies);

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

	// public static List<Movie> alloTrim(List<Movie> movies) throws IOException
	// {
	// int size = movies.size();
	// for (int i = size - 1; i >= 0; i--) {
	// Movie myMovie = movies.get(i);
	// System.out.println(myMovie.imdbName);
	// boolean startLooking = false;
	// boolean notFound = false;
	// String encodedName = URLEncoder.encode(myMovie.imdbName, "UTF-8");
	// URL myQuery = new URL("http://www.allocine.fr/recherche/?q="
	// + encodedName);
	// InputStream searchStream = myQuery.openStream();
	// Scanner page = new Scanner(searchStream);
	// while (!startLooking) {
	// String token = page. ();
	// if (token.equals("class=\"title\">Oups")) {
	// notFound = true;
	// startLooking = true;
	// } else if (token.equals("titres")) {
	// startLooking = true;
	// }
	// }
	// if (notFound) {
	// movies.remove(myMovie);
	// } else {
	// String token = page.next();
	// while (!token.startsWith("href='/film/fichefilm_gen_cfilm=")) {
	// token = page.next();
	// }
	// int n = 33;
	// while (token.charAt(n) != '.') {
	// n++;
	// }
	// String link = token.substring(32, n);
	// System.out.println(link);
	// System.out.println();
	// }
	// page.close();
	// }
	// return movies;
	// }

	public static Map<String, Movie> alloMovies(Map<String, Movie> imdbMovies)
			throws IOException {
		Map<String, Movie> alloMovies = new TreeMap<String, Movie>();
		int numPage = 1;
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

					System.out.println(link);

					URL myMovieURL = new URL(
							"http://www.allocine.fr/film/fichefilm_gen_cfilm="
									+ link + ".html");
					InputStream pageStream = myMovieURL.openStream();
					Scanner myMovie = new Scanner(pageStream);
					boolean frenchTitle = false;
					String name = "";
					while(myMovie.hasNext()){
						String movieToken = myMovie.next();
						
						if(!frenchTitle){
							if (movieToken.endsWith("<title>")){
								String add = myMovie.next();
								name = name + add;
								add = myMovie.next();
								while (!add.equals("-")){
									name = name + " " + add;
									add = myMovie.next();
								}
							}
						} else if (movieToken.startsWith("original</div></th><td>")) {
							int n = 23;
							while(movieToken.charAt(n) != '<'){
								n++;
							}
							name = movieToken.substring(23, n);
							//original</div></th><td>About Time</td>
						}
					}
					//System.out.println(name);
					//System.out.println();
					if(imdbMovies.containsKey(name)){
						
					}
					myMovie.close();
					// http://www.allocine.fr/film/fichefilm_gen_cfilm=
				} else if (token.equals("btn-disabled\">Suivante<i")) {
					System.out.println("last page!");
					lastPage = true;
				}
			}
			numPage++;
			url = new URL(
					"http://www.allocine.fr/films/decennie-2010/alphabetique/?page="
							+ numPage);
			System.out.println();
			System.out.println("Size: " + alloMovies.size());
			System.out.println();
			myPage.close();
		}

		return alloMovies;
	}

	// http://www.allocine.fr/films/alphabetique/?page=2

	// href="/film/fichefilm_gen_cfilm=
	// btn-disabled">Suivante<i

}