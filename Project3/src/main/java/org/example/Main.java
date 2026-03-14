package org.example;

/**
 * Name: [Your Name]
 * Course: CNT 4714 - Spring 2026
 * Assignment: Project 3
 * Date: March 2026
 * <p>
 * Launcher for Project 3 applications.
 * Run with arg "accountant" to start Accountant App, otherwise Main SQL Client.
 * Example: `java -jar Project3.jar accountant`
 */
public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && "accountant".equalsIgnoreCase(args[0])) {
            AccountantApp.main(args);
        } else {
            SQLClientApp.main(args);
        }
    }
}
