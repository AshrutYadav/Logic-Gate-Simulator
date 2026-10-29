package frontend;

import javafx.application.Application;

/**
 * Application entry point for the Logic Gate Simulator.
 * Delegates to MainWindow JavaFX lifecycle.
 */
public class Main {
    public static void main(String[] args) {
        Application.launch(MainWindow.class, args);
    }
}
