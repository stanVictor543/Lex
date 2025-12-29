package lex.application.logic;

import lex.application.data.UserRepo;
import lex.application.model.User;
import java.util.List;


public class AuthService {

	private UserRepo userRepo;

	public AuthService() {
		this.userRepo = new UserRepo(); // Inițializare punte către date
	}

	public boolean authenticate(String username, String password) {
		// 1. Cerem lista de utilizatori de la Repo
		List<User> users = userRepo.loadAllUsers();

		// 2. Verificăm dacă există cineva cu aceste credențiale
		for (User user: users) {
			if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
				return true; // Am găsit o potrivire!
			}
		}

		// 3. Dacă am parcurs toată lista și nu am returnat true
		return false;
	}

	public boolean register(String username, String password) {
		List<User> users = userRepo.loadAllUsers();
		// Verificăm dacă user-ul există deja
		for (User u: users) {
			if (u.getUsername().equals(username)) return false;
		}

		userRepo.saveUser(new User(username, password));
		return true;
	}
}