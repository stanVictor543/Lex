package lex.application.presentation;

import lex.application.logic.AuthService;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Clasa responsabilă pentru interfața grafică a ecranului de Login/Sign Up.
 * Face parte din stratul de Prezentare (Presentation Layer).
 */
public class LoginView { 
    
    /**
     * Callback de tip Consumer care va fi apelat la logarea cu succes.
     * Permite decuplarea vederii (View) de logica principală a aplicației (App).
     */
    private java.util.function.Consumer<String> onLoginSuccess;
    
    // Serviciul care gestionează verificarea credențialelor și salvarea utilizatorilor
    private AuthService authService = new AuthService(); 

    /**
     * Constructorul primeste o funcție (callback) care se va executa când utilizatorul intră în aplicație.
     */
    public LoginView(java.util.function.Consumer<String> onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }

    /**
     * Construiește și returnează scena de JavaFX pentru fereastra de login.
     * Conține definiții de stil (CSS inline) și handlere de evenimente.
     */
    public Scene getScene() {
        // --- DEFINIRE STILURI (CSS-in-Java) ---
        String darkPurpleBg = "-fx-background-color: #1a103c;";
        String inputBg = "-fx-background-color: #2d1b5e; -fx-text-fill: white; -fx-prompt-text-fill: #8f8f8f;";
        String turquoiseAccent = "#00ced1";
        
        // Stiluri pentru butoane (Stare normală vs Hover)
        String buttonStyle = "-fx-background-color: " + turquoiseAccent + "; -fx-text-fill: #1a103c; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;";
        String buttonHoverStyle = "-fx-background-color: #22e3e6; -fx-text-fill: #1a103c; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;";
        String secondaryBtnStyle = "-fx-background-color: transparent; -fx-text-fill: " + turquoiseAccent + "; -fx-border-color: " + turquoiseAccent + "; -fx-border-radius: 5; -fx-font-weight: bold; -fx-cursor: hand;";

        // --- COMPONENTE UI ---
        Label lbl = new Label("Welcome Back");
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lbl.setStyle("-fx-text-fill: " + turquoiseAccent + ";");
        
        // Adăugare efect vizual de strălucire (Glow) sub titlu
        DropShadow ds = new DropShadow(10, Color.web(turquoiseAccent));
        lbl.setEffect(ds);

        TextField userField = new TextField();
        userField.setPromptText("Username");
        userField.setStyle(inputBg + "-fx-background-radius: 5; -fx-padding: 10;");
        userField.setMaxWidth(250);

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.setStyle(inputBg + "-fx-background-radius: 5; -fx-padding: 10;");
        passField.setMaxWidth(250);

        // --- BUTON LOGIN ---
        Button loginBtn = new Button("LOGIN");
        loginBtn.setStyle(buttonStyle);
        loginBtn.setPrefWidth(250);
        // Efect vizual la trecerea mouse-ului peste buton
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle(buttonHoverStyle));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle(buttonStyle));

        // --- BUTON SIGN UP ---
        Button signUpBtn = new Button("SIGN UP");
        signUpBtn.setStyle(secondaryBtnStyle);
        signUpBtn.setPrefWidth(250);

        // Label pentru afișarea erorilor de validare sau succes
        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: #ff4d4d; -fx-font-size: 12px;");

        // --- LOGICA LOGIN ---
        loginBtn.setOnAction(e -> {
            String inputUser = userField.getText();
            String inputPass = passField.getText();

            // Apelăm serviciul de autentificare
            if (authService.authenticate(inputUser, inputPass)) {
                // Dacă e ok, transmitem numele utilizatorului către App.java prin callback
                onLoginSuccess.accept(inputUser); 
            } else {
                // Tratare eroare: Mesaj vizual și bordură roșie pentru input-uri
                errorLbl.setText("Invalid credentials provided.");
                errorLbl.setStyle("-fx-text-fill: #ff4d4d;");
                String errorStyle = inputBg + "-fx-border-color: #ff4d4d; -fx-background-radius: 5; -fx-padding: 10; -fx-border-radius: 5;";
                userField.setStyle(errorStyle);
                passField.setStyle(errorStyle);
            }
        });

        // --- LOGICA SIGN UP (Înregistrare) ---
        signUpBtn.setOnAction(e -> {
            String inputUser = userField.getText();
            String inputPass = passField.getText();

            // Validare minimală la nivel de UI
            if (inputUser.isEmpty() || inputPass.isEmpty()) {
                errorLbl.setText("Fields cannot be empty.");
                errorLbl.setStyle("-fx-text-fill: #ff4d4d;");
            } else if (authService.register(inputUser, inputPass)) {
                // Succes la înregistrare
                errorLbl.setText("Account created! You can now login.");
                errorLbl.setStyle("-fx-text-fill: #00ff00;"); 
            } else {
                // Cazul în care user-ul există deja în baza de date
                errorLbl.setText("Username already exists.");
                errorLbl.setStyle("-fx-text-fill: #ff4d4d;");
            }
        });

        // --- ORGANIZARE LAYOUT ---
        // VBox (Vertical Box) aranjează elementele unul sub altul cu o distanțare de 15px
        VBox layout = new VBox(15);
        layout.getChildren().addAll(lbl, userField, passField, loginBtn, signUpBtn, errorLbl);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle(darkPurpleBg);
        layout.setPadding(new Insets(40));

        // Returnăm scena gata configurată pentru a fi afișată în Stage
        return new Scene(layout, 350, 480);
    }
}