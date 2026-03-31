package com.playwright.core;

public class Navigation {
    
    public static class URLs {
        public static final String YATRA_HOME = "https://www.yatra.com";
        public static final String YATRA_FLIGHTS = "https://www.yatra.com/flights";
        public static final String YATRA_HOTELS = "https://www.yatra.com/hotels";
        public static final String YATRA_HOLIDAYS = "https://www.yatra.com/holidays";
    }
    
    public static class Timeouts {
        public static final int DEFAULT_TIMEOUT = 30000;
        public static final int SHORT_TIMEOUT = 5000;
        public static final int LONG_TIMEOUT = 60000;
    }
}