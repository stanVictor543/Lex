package lex.application.data;

import lex.application.model.Movie;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class MovieRepo {

	private final String baseDir = System.getProperty("user.home") + File.separator + "CinemaManagerData" + File.separator + "lex";

	private String filePath;

	public MovieRepo(String username) {
		// Combină folderul de bază cu numele specific al fișierului utilizatorului
		this.filePath = baseDir + File.separator + "movies_" + username + ".txt";

		ensureDirectoryExists();
	}

	private void ensureDirectoryExists() {
		File folder = new File(baseDir);

		if (!folder.exists()) {
			folder.mkdirs();
		}
	}

	public List<Movie> LoadAllMovies() {
		List<Movie> movies = new ArrayList<>();
		File folder = new File("lex");
		File file = new File(filePath); // Foloseste calea specifică utilizatorului
		try {
			//Checkuri pentru folder si fisier
			if (!folder.exists()) {
				folder.mkdirs();
			}

			
			if (!file.exists()) {
				file.createNewFile();
				System.out.println("Info: Baza de date pentru utilizator a fost creată.");
				return movies;
			}

			//Citirea efectivă a datelor(filme)
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				String line;

				while ((line = reader.readLine()) != null) {
					if (line.trim().isEmpty()) continue;
					String[] parts = line.split(",");

					if (parts.length == 7) {
						movies.add(new Movie(
							parts[0].trim(), parts[1].trim(),
							Integer.parseInt(parts[2].trim()), parts[3].trim(),
							parts[4].trim(), Double.parseDouble(parts[5].trim()),
							parts[6].trim()
						));
					}
				}
			}
		} catch (IOException | NumberFormatException e) {
			System.err.println("Eroare la încărcarea filmelor: " + e.getMessage());
		}

		return movies;
	}

	public void saveMovies(List<Movie> movies) {
		
		File folder = new File("lex");

		if (!folder.exists()) {
			folder.mkdirs();
		}

		try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
			for (Movie m: movies) {
				// Salvarea toate cele 7 câmpuri separate prin virgulă
				writer.println(m.getTitle() + "," +
					m.getDirector() + "," +
					m.getYear() + "," +
					m.getMoviePath() + "," +
					m.getCategories() + "," +
					m.getRating() + "," +
					m.getImdbId());

			}

			System.out.println("Datele au fost salvate permanent.");
		} catch (IOException e) {
			System.err.println("Eroare la salvare: " + e.getMessage());
		}
	}
}