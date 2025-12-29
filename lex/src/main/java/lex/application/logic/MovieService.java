package lex.application.logic;

import lex.application.data.MovieRepo;
import lex.application.model.Movie;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.image.Image;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Clasa Service care gestionează logica de business pentru colecția de filme.
 * Face legătura între stratul de date (MovieRepo) și interfața grafică (UI).
 */
public class MovieService {

    // Dependența către repository pentru salvare/încărcare date
    private MovieRepo movieRepo;

    // 1. GESTIONAREA STĂRII (State Management)
    // Sursa principală de date în memorie (RAM)
    private ObservableList<Movie> allMovies;

    // Listă "wrapper" care permite filtrarea fără a șterge elemente din sursa principală
    private FilteredList<Movie> filteredMovies;

    /**
     * Constructor: Inițializează repo-ul și încarcă datele pentru utilizatorul specificat.
     * @param username Numele utilizatorului pentru a accesa fișierul corespunzător de date.
     */
    public MovieService(String username) {
        this.movieRepo = new MovieRepo(username);
        // Transformă lista simplă primită din Repo într-o listă observabilă de JavaFX
        this.allMovies = FXCollections.observableArrayList(movieRepo.LoadAllMovies());
        // Inițializăm filtrul pentru a afișa tot (predicat mereu true)
        this.filteredMovies = new FilteredList<>(allMovies, p -> true);
    }

    /**
     * Returnează lista filtrată care trebuie legată de elementele UI (ex: TableView sau ListView).
     * Orice modificare în allMovies se va reflecta automat aici.
     */
    public ObservableList<Movie> getFilteredMovies() {
        return filteredMovies;
    }

    // 2. LOGICA DE BUSINESS (FILTRARE)
    /**
     * Filtrează colecția de filme pe baza unui șir de caractere.
     * Căutarea se face în Titlu, Regizor, An sau Categorii.
     */
    public void filterMovies(String searchText) {
        filteredMovies.setPredicate(movie -> {
            // Dacă textul de căutare este gol, afișăm toate filmele
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }

            String lowerCaseFilter = searchText.toLowerCase();

            // Verificăm dacă vreun câmp al filmului conține textul căutat (case-insensitive)
            return movie.getTitle().toLowerCase().contains(lowerCaseFilter) ||
                movie.getDirector().toLowerCase().contains(lowerCaseFilter) ||
                String.valueOf(movie.getYear()).contains(lowerCaseFilter) || 
                (movie.getCategories() != null && movie.getCategories().toLowerCase().contains(lowerCaseFilter));
        });
    }

    /**
     * Adaugă un film nou în memorie și persistă datele pe disc.
     * @throws InvalidMovieDataException Dacă datele introduse nu respectă regulile de business.
     */
    public void addMovie(String title, String director, int year, String path, String categories, double rating, String imdbId)
    throws InvalidMovieDataException {

        // Validări riguroase înainte de a crea obiectul
        if (title == null || title.trim().isEmpty()) {
            throw new InvalidMovieDataException("Titlul filmului nu poate fi gol!");
        }

        if (rating < 1 || rating > 10) {
            throw new InvalidMovieDataException("Rating-ul trebuie să fie un număr între 1 și 10!");
        }

        if (year < 1888 || year > 2100) {
            throw new InvalidMovieDataException("Anul lansării este invalid!");
        }

        Movie newMovie = new Movie(title, director, year, path, categories, rating, imdbId);
        
        // Adăugarea în listă declanșează actualizarea automată a UI-ului (datorită ObservableList)
        this.allMovies.add(newMovie);
        
        // Salvare permanentă în fișier (JSON/TXT prin MovieRepo)
        movieRepo.saveMovies(allMovies);
    }

    /**
     * Șterge un film selectat și actualizează stocarea permanentă.
     */
    public void deleteMovie(Movie movie) {
        if (movie != null) {
            // Eliminăm din lista RAM
            this.allMovies.remove(movie);

            // Actualizăm fișierul fizic pentru a reflecta ștergerea
            movieRepo.saveMovies(allMovies);
        }
    }

    // 4. LOGICA MEDIA (PLAYER & COPERTĂ)
    /**
     * Deschide fișierul video asociat filmului folosind player-ul implicit al sistemului de operare.
     * Caută primul fișier .mp4 din folderul specificat al filmului.
     */
    public void playMovie(Movie movie) {
        if (movie.getMoviePath() == null || movie.getMoviePath().isEmpty()) return;

        File dir = new File(movie.getMoviePath());

        if (dir.exists() && dir.isDirectory()) {
            // Filtrăm folderul pentru a găsi doar fișiere video
            File[] videoFiles = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".mp4"));

            if (videoFiles != null && videoFiles.length > 0) {
                try {
                    // Lansează aplicația default a sistemului (ex: VLC, Windows Media Player)
                    Desktop.getDesktop().open(videoFiles[0]);
                } catch (IOException e) {
                    System.err.println("Eroare la deschiderea player-ului: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Caută o imagine (JPG/PNG) în folderul filmului pentru a fi folosită ca poster.
     * @return Imaginea găsită sau o imagine de tip "placeholder" dacă nu există.
     */
    public Image getMovieCover(Movie movie) {
        if (movie.getMoviePath() == null || movie.getMoviePath().isEmpty()) {
            return getDefaultCover();
        }

        File dir = new File(movie.getMoviePath());

        if (dir.exists() && dir.isDirectory()) {
            File[] imageFiles = dir.listFiles((d, name) ->
                name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png")
            );

            if (imageFiles != null && imageFiles.length > 0) {
                // Returnează prima imagine găsită în folder
                return new Image(imageFiles[0].toURI().toString());
            }
        }

        return getDefaultCover();
    }

    /**
     * Returnează o imagine de rezervă în cazul în care filmul nu are copertă.
     */
    private Image getDefaultCover() {
        return new Image("https://via.placeholder.com/150x200?text=No+Cover");
    }

    /**
     * Generează un raport text formatat, grupând filmele pe categorii și sortându-le alfabetic.
     * @param file Fișierul destinație unde va fi scris raportul.
     */
    public void generateReport(File file) {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("==========================================");
            writer.println("      RAPORT COLECȚIE FILME - MANAGER     ");
            writer.println("==========================================\n");

            // 1. Grupăm filmele folosind Java Streams (Stream API)
            // Rezultă un Map unde cheia e Categoria și valoarea e Lista de filme din acea categorie
            Map < String, List<Movie>> groupedMovies = allMovies.stream()
                .collect(Collectors.groupingBy(m -> m.getCategories() != null && !m.getCategories().isEmpty() ?
                    m.getCategories() : "Fără Categorie"));

            // 2. Parcurgem Map-ul pentru a scrie în fișier
            groupedMovies.forEach((category, movies) -> {
                writer.println("CATEGORIE: " + category.toUpperCase());
                writer.println("------------------------------------------");

                // 3. Sortăm filmele din categoria curentă alfabetic după titlu
                List<Movie> sortedMovies = movies.stream()
                .sorted(Comparator.comparing(Movie::getTitle))
                .collect(Collectors.toList());

                for (Movie m: sortedMovies) {
                    // Formatăm linia pentru o lizibilitate crescută
                    writer.printf("- %s | Regizor: %s | An: %d | Rating: %.1f | IMDB: %s\n",
                        m.getTitle(), m.getDirector(), m.getYear(), m.getRating(), m.getImdbId());
                }

                writer.println(); // Linie goală între categorii
            });

            System.out.println("Raport generat cu succes la: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Eroare la generarea raportului: " + e.getMessage());
        }
    }
}