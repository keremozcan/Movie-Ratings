public class Movie {

	public String imdbLink;
	public String imdbName;
	public String imdbYear;
	public String imdbRating;
	public String imdbNumVotes;

	// the links and original names for all 4 sites should be the same.
	public String alloLink;

	// From 0 (inclusive) to 5(inclusive), with semicolons in between
	public String frNumStars;
	public String esNumStars;
	public String ptNumStars;
	public String deNumStars;
	public String trNumStars;

	public Movie(String year, String link, String rating, String numVotes,
			String name) {
		
		this.imdbLink = link;
		this.imdbName = name;
		this.imdbYear = year;
		this.imdbRating = rating;
		this.imdbNumVotes = numVotes;
		
		frNumStars = null;
		esNumStars = null;
		ptNumStars = null;
		deNumStars = null;
		trNumStars = null;
	}

	public void addAllocineRatings(String fr, String es, String pt, String de,
			String tr, String link) {
		this.frNumStars = fr;
		this.esNumStars = es;
		this.ptNumStars = pt;
		this.deNumStars = de;
		this.trNumStars = tr;
		this.alloLink = link;
	}

	public String toString() {
		return imdbName + ";" + imdbYear + ";" + imdbLink + ";" + alloLink
				+ ";" + imdbNumVotes + ";" + imdbRating + ";" + frNumStars
				+ esNumStars + ptNumStars + deNumStars + trNumStars;
	}
}
