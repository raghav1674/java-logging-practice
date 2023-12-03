package com.example;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Logger;

public class App {

    private final static Logger logger = Logger.getLogger(App.class.getName());

    static {
        logger.addHandler(new ConsoleHandler());
        try {
            FileHandler fileHandler = new FileHandler("app.log", true);
            fileHandler.setFormatter(new Formatter() {
                @Override
                public String format(java.util.logging.LogRecord record) {
                    return record.getLevel() + ": " + record.getMessage() + "\n";
                }
            });
            logger.addHandler(fileHandler);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
        logger.setLevel(java.util.logging.Level.ALL);
    }

    public static void main1(String[] args) {
        logger.info("Hello World");
        System.out.println("Hello World!");
        logger.warning("Hi");
    }
}
