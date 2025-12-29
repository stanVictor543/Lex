package lex.application;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import lex.application.presentation.LoginView;
import lex.application.presentation.MainView;


public class App extends Application {

	private Stage primaryStage;
	//Metoda de start a aplicatiei JavaFX
	@Override
	public void start(Stage stage) {
		this.primaryStage = stage;
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
		showLoginScreen();
		stage.setTitle("");
		stage.show();
	}

    /**
     * Afiseaza ecranul de Login.
     * Transmite un mecanism de callback (Lambda) care va fi executat 
     * DOAR atunci cand autentificarea reuseste.
     */
	public void showLoginScreen() {
        // 1. LoginView nu stie cum sa schimbe ferestrele (decuplare).
        // 2. Cand Login reuseste, primim 'username'-ul de la serviciul de Auth
        //si il trimitem imediat catre dashboard pentru a incarca datele corecte.
		LoginView login = new LoginView(username -> showDashboard(username));
		primaryStage.setScene(login.getScene());
	}

	public void showDashboard(String username) {
        // Transmitem:
        // 1. Username-ul: pentru a sti ale cui filme le afisam.
        // 2. Un Runnable pentru Logout: permite Dashboard-ului sa ceara App-ului
        //sa revina la ecranul de login fara ca Dashboard sa stie de existenta App.
		MainView dashboard = new MainView(username, () -> showLoginScreen());
		primaryStage.setScene(dashboard.getScene());
		primaryStage.centerOnScreen();
	}

	public static void main(String[] args) {

		launch(args);
	}
}