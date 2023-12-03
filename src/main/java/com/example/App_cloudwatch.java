package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App_cloudwatch {

    final static Logger logger = LoggerFactory.getLogger(App_cloudwatch.class);

    public static void main(String[] args) {
        logger.info("CloudWatch Hello World");
        System.out.println("Hello, World!");
        logger.warn("CloudWatch Hi");

    }
}
