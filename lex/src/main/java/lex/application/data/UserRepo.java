package lex.application.data;

import lex.application.model.User;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepo {
    private final String baseDir = System.getProperty("user.home") + File.separator + "CinemaManagerData" + File.separator + "lex";
    private final String filePath = baseDir + File.separator + "credentials.txt";

    /**
     * Această metodă citește tot fișierul și transformă fiecare linie
     * într-un obiect de tip User.
     */
    public List<User> loadAllUsers() {
    List<User> users = new ArrayList<>();
    File folder = new File(baseDir);
    File file = new File(filePath);

    try {
        // 1. Verific și creeaza folderul 'lex' dacă nu există
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // 2. Verifică și creeaza fișierul 'credentials.txt' dacă nu există
        if (!file.exists()) {
            file.createNewFile();
            System.out.println("Info: Fișierul credentials.txt a fost creat.");
            return users; // Returneaza lista goala, fiind nou creat
        }

        // 3. Citește fișierul și încarcă utilizatorii
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    users.add(new User(parts[0].trim(), parts[1].trim()));
                }
            }
        }
    } catch (IOException e) {
        System.err.println("Eroare la inițializarea bazei de date utilizatori: " + e.getMessage());
    }

    return users;
}
// Metoda pentru a salva un nou utilizator în fișier
    public void saveUser(User user) {
    File folder = new File("lex");
    if (!folder.exists()) folder.mkdirs();

    try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, true))) { 
        writer.println(user.getUsername() + "," + user.getPassword());
    } catch (IOException e) {
        System.err.println("Eroare la salvarea utilizatorului: " + e.getMessage());
    }
}
}