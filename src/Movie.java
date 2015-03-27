public class Movie {

	public String imdbLink;
	public String imdbName;
	public String imdbYear;
	public String imdbRating;
	public String imdbNumVotes;

	// the links and original names for all 4 sites should be the same.
	public String alloLink;
	public String alloName;

	// From 0 (inclusive) to 5(inclusive), with semicolons in between
	public String frNumStars;
	public String esNumStars;
	public String deNumStars;
	public String trNumStars;
	public String ptNumStars;

	public Movie(String year, String link, String rating, String numVotes,
			String name) {
		this.imdbLink = link;
		this.imdbName = name;
		this.imdbYear = year;
		this.imdbRating = rating;
		this.imdbNumVotes = numVotes;
		frNumStars = null;
		esNumStars = null;
		deNumStars = null;
		trNumStars = null;
		ptNumStars = null;
	}

}
