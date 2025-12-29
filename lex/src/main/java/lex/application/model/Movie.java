package lex.application.model;

public class Movie {
    //Variabilele cerute in cerinte + o variabila sa stocheze filmul si imaginea de coperta
    private String title;
    private String director;
    private int year;
    private String categories;
    private double rating;
    private String imdbId;
    private String moviePath;

    public Movie(String title, String director, int year, String moviePath, String categories, double rating, String imdbId) {
        this.title = title;
        this.director = director;
        this.year = year;
        this.moviePath = moviePath;
        this.categories = categories;
        this.rating = rating;
        this.imdbId = imdbId;
    }

   //Getters
    public String getTitle() { return title; }
    public String getDirector() { return director; }
    public int getYear() { return year; }
    public String getMoviePath() { return moviePath; }
    public String getCategories() { return categories; }
    public double getRating() { return rating; }
    public String getImdbId() { return imdbId; }
}