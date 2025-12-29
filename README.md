CinemaManager - Desktop Media Library
A modern, lightweight JavaFX application designed to manage personal movie collections. This project features a sleek dark-themed interface, multi-user support with data isolation, and automated local persistence.

🚀 Features
Multi-User System: Secure Sign-Up and Login functionality.

Data Isolation: Each user has a dedicated database file (movies_username.txt), ensuring private collections.

Dynamic Catalog: Visual movie grid with custom covers, titles, and context menus.

Advanced Filtering: Explore collections via a TreeView sidebar organized by Genre, Director, or Release Year.

Modern UI/UX:

Smooth vertical scrolling (web-like experience).

Interactive hover effects on movie cards.

Dark Mode aesthetic (Purple & Turquoise palette).

Report Generation: Export your movie list to a formatted text file.

Native Windows App: Fully packagable as a standalone .exe installer.

🛠️ Tech Stack
Language: Java 11/21

Framework: JavaFX 13

Build Tool: Maven

Packaging: jpackage & WiX Toolset v3.11

Persistence: Flat-file system (CSV/Text)

🏗️ Architecture
The project follows a 3-Tier Architecture to ensure maintainability and scalability:

Presentation Layer: JavaFX Views (Login, Main Dashboard) handling user interaction.

Logic Layer: Services (AuthService, MovieService) processing business rules and filtering.

Data Layer: Repositories (UserRepo, MovieRepo) managing file I/O operations.

📦 Installation & Setup
Prerequisites
JDK 11 or higher.

Maven installed and added to your System PATH.

WiX Toolset v3.11 (only required for building the .exe installer).

Building the Project
Clone or download the repository.

Navigate to the project root (where pom.xml is located).

Run the following command to compile and package the JAR:

Bash

mvn clean package
Running the App (Development)
To launch the application directly from the source code:

Bash

mvn javafx:run
Creating the Executable (.exe)
After running mvn package, use the following command to generate the Windows installer:

PowerShell

jpackage --type exe --input target/ --main-jar lex-21.jar --main-class lex.application.Launcher --name "CinemaManager" --win-shortcut --win-menu
📂 Data Storage
To avoid Windows permission issues, the application automatically creates a data folder in the user's home directory: C:\Users\<YourUser>\CinemaManagerData\lex

This folder contains:

credentials.txt: Encrypted-style user login data.

movies_<username>.txt: Personalized movie databases.

🤝 Contributing
This project was developed as a university assignment focused on Software Engineering patterns and Java Desktop development.
