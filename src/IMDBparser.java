// This was to parse raw data file provided by IMDB
// but there are issues with it so I decided to write
// my own web scraping program.

import java.util.*;
import java.io.*;

public class IMDBparser {
	public static void main(String[] args) throws FileNotFoundException {
		Scanner input = new Scanner(new File("IMDBratings.list"));
		PrintStream output = new PrintStream(new File("2013Ratings.txt"));
		while (input.hasNextLine()) {

			boolean date = false;
			String line = input.nextLine();
			if (!line.contains("{")) {
				Scanner token = new Scanner(line);
				output.print(token.next() + ';');
				output.print(token.next() + ';');
				output.print(token.next() + ';');
				while (token.hasNext()) {
					String myToken = token.next();
					if (myToken.startsWith("(") && myToken.endsWith(")")
							&& !date && myToken.length() == 6
							&& !token.hasNext()) {
						date = true;
						output.print(";");
						System.out.println(myToken);
						output.print(myToken.substring(1, 5));
					} else {
						// if (myToken.startsWith("{")) {
						// output.print(";");
						// myToken = myToken.substring(1);
						// }
						// if (myToken.endsWith("}")) {
						// myToken = myToken.substring(0, myToken.length() - 1);
						// }
						output.print(myToken + " ");
					}
				}
				output.println();
			}
		}
	}
}
