package lex.application;

public class Launcher {
    public static void main(String[] args) {
        //Pentru creearea unui fisier exe este nevoie ca clasa care creeaza aplicatia sa nu fie o interfata
        //So am creat o alta clasa care doar apelaza mainul clasei App
        
        App.main(args);
    }
}