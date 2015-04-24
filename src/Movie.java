import java.io.*;
import java.util.*;

public class Movie {

	public String imdbYear;
	public String imdbLink;
	public String imdbName;
	public String imdbDirector;

	public String imdbOther;

	public String imdbDist;
	public String imdbUsDist;
	public String imdbOtherDist;

	// Desimartini website distribution
	public String desiDist;
	
	// Douban website distribution
		public String doubanLink;
		public String doubanDist;

	// the links and original names for all 4 sites should be the same.
	public String alloLink;

	// From 0 (inclusive) to 5(inclusive), with semicolons in between
	public String frDist;
	public String esDist;
	public String ptDist;
	public String deDist;
	public String trDist;

	public Movie(String year, String link, String name, String director,
			String other) {

		this.imdbYear = year;
		this.imdbLink = link;
		this.imdbName = name;
		this.imdbDirector = director;
		this.imdbOther = other;

		frDist = null;
		esDist = null;
		ptDist = null;
		deDist = null;
		trDist = null;
	}

	public void addAllocineRatings(String fr, String es, String pt, String de,
			String tr, String link) {
		this.frDist = fr;
		this.esDist = es;
		this.ptDist = pt;
		this.deDist = de;
		this.trDist = tr;
		this.alloLink = link;
	}

	public void addDesiRatings(String desiDist) {
		this.desiDist = desiDist;
	}

	// reads the given imdbList file and creates a map.
	// Keys are the 4 digit years of the movies + names of movies
	// and values are Movie objects.
	public static Map<String, Movie> readImdbFile(String file, char delimiter)
			throws FileNotFoundException {
		Scanner input = new Scanner(new File(file));
		Map<String, Movie> movies = new TreeMap<String, Movie>();
		// input.nextLine();
		while (input.hasNextLine()) {
			String line = input.nextLine();
			boolean quoteFlag = false;
			String[] part = { "", "", "", "", "" };
			int myPart = 0;
			for (int i = 0; i < line.length(); i++) {
				char currentChar = line.charAt(i);
				if (myPart >= 4) {
					part[4] += currentChar;
				} else if (currentChar == '"'){
					quoteFlag = !quoteFlag;
				} else if (currentChar == delimiter && !quoteFlag) {
					myPart++;
				} else {
					part[myPart] += currentChar;
				}
			}
			

			if(part[2].startsWith("\"") && part[2].endsWith("\"")){
				part[2] = part[2].substring(1, part[2].length() - 1);
			}
			


			Movie myMovie = new Movie(part[0], part[1], part[2], part[3],
					part[4]);
			movies.put(myMovie.imdbLink, myMovie);
			System.out.println(myMovie.imdbName);
			System.out.println(myMovie.imdbLink);
			System.out.println(myMovie.imdbYear);
			System.out.println(myMovie.imdbDirector.replaceAll("\\p{M}", ""));
			System.out.println(myMovie.imdbOther);
			System.out.println();

		}
		input.close();
		return movies;
	}

	public String imdbString() {
		return imdbYear + ',' + imdbLink + ',' + '"' + imdbName + '"' + ','
				+ imdbDirector + ',' + imdbOther + ',';
	}

	public String alloString() {
		return alloLink + ',' + frDist + esDist + ptDist + deDist + trDist;
	}

	public String desiString() {
		return desiDist;
	}

	public String toString() {
		return imdbString() + desiString() + alloString();
	}
	
	public String doubanString(){
		return doubanLink + ',' + doubanDist;
	}
}
