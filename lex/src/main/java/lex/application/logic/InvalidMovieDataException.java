package lex.application.logic;

//Exceptie personalizata pentru date de film invalide
public class InvalidMovieDataException extends Exception {
    public InvalidMovieDataException(String message) {
        super(message);
    }
}
