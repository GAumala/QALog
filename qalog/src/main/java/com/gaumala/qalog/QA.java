package com.gaumala.qalog;

/**
 * Main API
 * This file is written in Java to make
 * the API more friendly to java projects.
 */
public class QA {
    protected static Logger logger = new Logger();

    public static void log(String line) {
        logger.log(System.currentTimeMillis(), line);
    }
}
