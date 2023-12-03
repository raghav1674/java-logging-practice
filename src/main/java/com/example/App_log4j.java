package com.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App_log4j {

    private final static Logger logger = LogManager.getLogger(App_log4j.class);

    public static void main2(String[] args) {
        logger.info("Log4j Hello World");
        System.out.println("Hello World!");
        logger.warn("Log4j Hi");
    }
}
