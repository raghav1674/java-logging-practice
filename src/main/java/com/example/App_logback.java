package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App_logback {

    final static Logger logger = LoggerFactory.getLogger(App_logback.class);

    public static void main3(String[] args) {
        logger.info("LogBack Hello World");
        System.out.println("Hello World!");
        logger.warn("LogBack Hi");
    }
}
