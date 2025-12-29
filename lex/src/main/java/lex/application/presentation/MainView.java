package lex.application.presentation;

import lex.application.logic.InvalidMovieDataException;
import lex.application.logic.MovieService;
import lex.application.model.Movie;

import java.io.File;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.input.KeyCombination;

/**
 * Clasa principala a interfetei utilizator (UI).
 * Utilizeaza un BorderPane pentru a organiza elementele: Meniu (Top), Sidebar (Left) si Catalog (Center).
 */
public class MainView {
    private MovieService movieService;
    private String currentUser;
    private Runnable onLogout; // Callback pentru revenirea la ecranul de Login
    
    // Componente dinamice care necesita refresh
    private FlowPane catalog = new FlowPane(); 
    private TreeView<String> treeView = new TreeView<>();
    
    // Paleta de culori pentru consistenta vizuala (Dark Mode / Cyberpunk style)
    private final String darkPurpleBg = "-fx-background-color: #1a103c;";
    private final String cardBg = "-fx-background-color: #2d1b5e;";
    private final String turquoiseAccent = "#00ced1";

    public MainView(String username, Runnable onLogout) {
        this.currentUser = username;
        this.onLogout = onLogout;
        this.movieService = new MovieService(username);
    }

    /**
     * Construieste scena principala a aplicatiei.
     */
    public Scene getScene() {
        BorderPane root = new BorderPane();
        root.setStyle(darkPurpleBg);

        // --- 1. MENIUL DE SUS ---
        root.setTop(createMenuBar());

        // --- 2. SIDEBAR STÂNGA (Navigatie si Actiuni) ---
        setupLeftPane(root);

        // --- 3. ZONA CENTRALĂ (Catalogul cu Scroll) ---
        catalog.setPadding(new Insets(30));
        catalog.setHgap(25); // Spatiu orizontal intre carduri
        catalog.setVgap(25); // Spatiu vertical intre randuri
        catalog.setStyle(darkPurpleBg);
        
        // Legam latimea catalogului de latimea ferestrei pentru a permite asezarea automata (wrap)
        catalog.prefWidthProperty().bind(root.widthProperty().subtract(260));

        ScrollPane scrollPane = new ScrollPane(catalog);
        scrollPane.setFitToWidth(true); 
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Doar scroll vertical
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); 
        
        // Eliminam bordurile default ale ScrollPane-ului pentru un aspect curat
        scrollPane.setStyle("-fx-background: #1a103c; " +
                            "-fx-background-color: #1a103c; " +
                            "-fx-border-color: transparent;");
        
        root.setCenter(scrollPane);

        // Populare initiala a filmelor
        refreshCatalog();

        return new Scene(root, 700, 600); 
    }

    /**
     * Configureaza panoul lateral: Arborele de explorare si butoanele de actiune.
     */
    private void setupLeftPane(BorderPane root) {
        Label lblExplorare = new Label("EXPLORARE");
        lblExplorare.setStyle("-fx-text-fill: " + turquoiseAccent + "; -fx-font-weight: bold; -fx-font-size: 16px;");

        setupTreeView(); // Creeaza ierarhia de categorii/regizori
        treeView.setPrefHeight(400);

        // Buton pentru deschiderea dialogului de adaugare
        Button addMovieBtn = new Button("+ ADD MOVIE");
        String addBtnStyle = "-fx-background-color: " + turquoiseAccent + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;";
        addMovieBtn.setStyle(addBtnStyle);
        addMovieBtn.setMaxWidth(Double.MAX_VALUE); // Butonul se intinde pe toata latimea sidebar-ului
        addMovieBtn.setPadding(new Insets(12));
        addMovieBtn.setOnAction(e -> showAddDialog());

        // Buton pentru exportul colectiei in format TXT
        Button reportBtn = new Button("GENEREAZĂ RAPORT");
        String reportBtnStyle = "-fx-background-color: transparent; -fx-text-fill: #00ced1; -fx-border-color: #00ced1; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;";
        reportBtn.setStyle(reportBtnStyle);
        reportBtn.setMaxWidth(Double.MAX_VALUE);
        reportBtn.setPadding(new Insets(10));
        
        reportBtn.setOnAction(e -> {
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            fc.setTitle("Salvează Raportul");
            fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Fișier Text", "*.txt"));
            File file = fc.showSaveDialog(null);
            if (file != null) {
                movieService.generateReport(file);
                showInfoAlert("Succes", "Raport generat cu succes!");
            }
        });

        // Buton de Logout pentru schimbarea utilizatorului
        Button logoutBtn = new Button("LOGOUT (" + currentUser + ")");
        String logoutBtnStyle = "-fx-background-color: transparent; -fx-text-fill: #ff4d4d; -fx-border-color: #ff4d4d; -fx-border-radius: 5; -fx-cursor: hand;";
        logoutBtn.setStyle(logoutBtnStyle);
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> onLogout.run());

        VBox leftPane = new VBox(20); 
        leftPane.setPadding(new Insets(20));
        leftPane.getChildren().addAll(lblExplorare, treeView, addMovieBtn, reportBtn, logoutBtn);
        leftPane.setPrefWidth(240);
        leftPane.setStyle("-fx-background-color: #1a103c; -fx-border-color: #2d1b5e; -fx-border-width: 0 1 0 0;");
        
        root.setLeft(leftPane);
    }

    /**
     * Construieste ierarhia (TreeView) pentru filtrarea rapida a filmelor.
     * Extrage dinamic genurile, regizorii si anii existenti in lista.
     */
    private void setupTreeView() {
        TreeItem<String> rootItem = new TreeItem<>("Cinema Manager");
        rootItem.setExpanded(true);

        // Nod pentru Categorii - folosim flatMap pentru a separa genurile multiple (ex: "Actiune, Drama")
        TreeItem<String> categoriesNode = new TreeItem<>("Categorii");
        movieService.getFilteredMovies().stream()
            .map(m -> m.getCategories())
            .filter(c -> c != null && !c.isEmpty())
            .flatMap(c -> java.util.Arrays.stream(c.split(",")))
            .map(String::trim)
            .distinct()
            .sorted()
            .forEach(cat -> categoriesNode.getChildren().add(new TreeItem<>(cat)));

        // Nod pentru Regizori
        TreeItem<String> directorsNode = new TreeItem<>("Regizori");
        movieService.getFilteredMovies().stream()
            .map(Movie::getDirector)
            .distinct().sorted()
            .forEach(dir -> directorsNode.getChildren().add(new TreeItem<>(dir)));

        // Nod pentru Ani
        TreeItem<String> yearsNode = new TreeItem<>("Ani");
        movieService.getFilteredMovies().stream()
            .map(movie -> String.valueOf(movie.getYear()))
            .distinct().sorted()
            .forEach(year -> yearsNode.getChildren().add(new TreeItem<>(year)));

        rootItem.getChildren().addAll(java.util.List.of(categoriesNode, directorsNode, yearsNode));
        treeView.setRoot(rootItem);
        treeView.setShowRoot(false);
        treeView.setStyle("-fx-background-color: #2d1b5e; -fx-control-inner-background: #2d1b5e; -fx-text-fill: white;");

        // Eveniment de selectie: la click pe o frunza (leaf), filtram catalogul
        treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.isLeaf()) {
                movieService.filterMovies(newVal.getValue());
            } else {
                movieService.filterMovies(""); // Resetare filtru
            }
            refreshCatalog();
        });
    }

    /**
     * Reincarca vizual cardurile de filme in FlowPane.
     */
    private void refreshCatalog() {
        catalog.getChildren().clear();
        for (Movie movie : movieService.getFilteredMovies()) {
            catalog.getChildren().add(createMovieCard(movie));
        }
    }

    /**
     * Creeaza un element vizual (card) pentru un film.
     * Include imaginea de coperta, titlul si meniul contextual (Play/Delete).
     */
    private VBox createMovieCard(Movie movie) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10));
        card.setStyle(cardBg + "-fx-background-radius: 10; -fx-cursor: hand;");
        card.setPrefSize(170, 260);

        ImageView cover = new ImageView(movieService.getMovieCover(movie));
        cover.setFitWidth(140);
        cover.setFitHeight(180);
        cover.setPreserveRatio(true);

        Label title = new Label(movie.getTitle());
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-text-alignment: center;");
        title.setWrapText(true);

        card.getChildren().addAll(cover, title);

        // --- MENIU CONTEXTUAL (Click Dreapta) ---
        ContextMenu contextMenu = new ContextMenu();
        MenuItem playItem = new MenuItem("Play Movie");
        playItem.setOnAction(e -> movieService.playMovie(movie));
        
        MenuItem deleteItem = new MenuItem("Șterge Film");
        deleteItem.setStyle("-fx-text-fill: #ff4d4d;");
        deleteItem.setOnAction(e -> {
            movieService.deleteMovie(movie);
            refreshCatalog();
            setupTreeView(); // Refresh si la arborele de navigatie
        });

        contextMenu.getItems().addAll(playItem, new SeparatorMenuItem(), deleteItem);
        card.setOnContextMenuRequested(e -> contextMenu.show(card, e.getScreenX(), e.getScreenY()));
        
        // Dublu click pentru pornirea rapida a filmului
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) movieService.playMovie(movie);
        });

        return card;
    }

    /**
     * Creeaza bara de meniu de sus cu scurtaturi de tastatura (Accelerators).
     */
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: #2d1b5e;");

        Menu fileMenu = new Menu("Fișier");
        MenuItem exitItem = new MenuItem("Ieșire");
        exitItem.setAccelerator(KeyCombination.keyCombination("Ctrl+X"));
        exitItem.setOnAction(e -> Platform.exit());
        fileMenu.getItems().add(exitItem);

        Menu editMenu = new Menu("Acțiuni");
        MenuItem addItem = new MenuItem("Adaugă Film Nou");
        addItem.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        addItem.setOnAction(e -> showAddDialog());
        editMenu.getItems().add(addItem);

        menuBar.getMenus().addAll(fileMenu, editMenu);
        return menuBar;
    }

    /**
     * Deschide o fereastra de tip Dialog (Stage secundar) pentru colectarea datelor filmului nou.
     */
    private void showAddDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Adaugă Film Nou");
        
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #1a103c;");
        root.setAlignment(Pos.CENTER);

        String fieldStyle = "-fx-background-color: #2d1b5e; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8;";

        // Input-uri detaliate pentru crearea obiectului Movie
        TextField titleIn = new TextField(); titleIn.setPromptText("Titlu Film"); titleIn.setStyle(fieldStyle);
        TextField directorIn = new TextField(); directorIn.setPromptText("Regizor"); directorIn.setStyle(fieldStyle);
        TextField yearIn = new TextField(); yearIn.setPromptText("An Lansare"); yearIn.setStyle(fieldStyle);
        TextField categoriesIn = new TextField(); categoriesIn.setPromptText("Categorii (ex: Acțiune, Dramă)"); categoriesIn.setStyle(fieldStyle);
        TextField ratingIn = new TextField(); ratingIn.setPromptText("Rating (1-10)"); ratingIn.setStyle(fieldStyle);
        TextField imdbIn = new TextField(); imdbIn.setPromptText("ID IMDB (ex: tt0111161)"); imdbIn.setStyle(fieldStyle);

        Label pathLabel = new Label("Niciun folder selectat");
        pathLabel.setStyle("-fx-text-fill: #8f8f8f; -fx-font-size: 11px;");
        
        // DirectoryChooser permite selectarea folderului unde se afla fisierul video si coperta
        Button browseBtn = new Button("Alege Folder Film");
        browseBtn.setStyle("-fx-background-color: #2d1b5e; -fx-text-fill: #00ced1; -fx-border-color: #00ced1; -fx-border-radius: 5;");
        
        final String[] selectedPath = {""};
        browseBtn.setOnAction(e -> {
            DirectoryChooser dc = new DirectoryChooser();
            File folder = dc.showDialog(dialog);
            if (folder != null) {
                selectedPath[0] = folder.getAbsolutePath();
                pathLabel.setText(selectedPath[0]);
            }
        });

        Button saveBtn = new Button("SALVEAZĂ FILM");
        saveBtn.setStyle("-fx-background-color: #00ced1; -fx-text-fill: white; -fx-font-weight: bold;");
        saveBtn.setPrefWidth(220);

        saveBtn.setOnAction(e -> {
            try {
                // Conversia si colectarea datelor din UI
                String title = titleIn.getText();
                String director = directorIn.getText();
                int year = Integer.parseInt(yearIn.getText());
                String path = selectedPath[0];
                String categories = categoriesIn.getText();
                double rating = Double.parseDouble(ratingIn.getText());
                String imdbId = imdbIn.getText();

                // Trimiterea datelor catre logic layer
                movieService.addMovie(title, director, year, path, categories, rating, imdbId);
                
                // Inchiderea dialogului si refresh UI
                refreshCatalog();
                setupTreeView(); 
                dialog.close();

            } catch (NumberFormatException ex) {
                showErrorAlert("Eroare de Format", "Anul și Rating-ul trebuie să fie numere valide!");
            } catch (InvalidMovieDataException ex) {
                // Prindem exceptia de business si afisam mesajul de eroare utilizatorului
                showErrorAlert("Date Invalide", ex.getMessage());
            }
        });

        root.getChildren().addAll(
            new Label("DETALII FILM") {{ setStyle("-fx-text-fill: #00ced1; -fx-font-weight: bold; -fx-font-size: 16px;"); }},
            titleIn, directorIn, yearIn, categoriesIn, ratingIn, imdbIn, browseBtn, pathLabel, saveBtn
        );

        dialog.setScene(new Scene(root, 380, 580));
        dialog.show();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}