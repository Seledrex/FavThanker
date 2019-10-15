package com.seledrex.util;

public class Constants {

    public static final int WAIT_SHOUT = 20000;
    public static final int ONE_MINUTE = 60000;

    public static final String COOKIE_FILENAME = "cookie.file";
    public static final String CONFIG_FILENAME = "config.properties";
    public static final String SHOUTS_CSV_FILENAME = "shouts.csv";
    public static final String FAVORITES_CSV_FILENAME = "favorites.csv";

    public static final String USERNAME = "username";

    public static final String SELECT_USER_PROMPT = "Please select a user!";
    public static final String START = "Start";
    public static final String STOP = "Stop";
    public static final String SELECT_USER = "Select User";
    public static final String STOPPED = "Stopped";
    public static final String STARTING = "Starting";

    public static final String TITLE = "FA Favorite Thanker";

    public static final String FA_BASE_URL = "http://www.furaffinity.net/";

    public static final String FAV_PATTERN = "(name=\"favorites\\[]\" value=\")" +
            "(\\d{9})" +
            "(\"><a href=\"/)" +
            "([^\"]*)" +
            "(\"><strong>)" +
            "([^<]*)" +
            "(</strong>)" +
            "(</a> favorited <strong>\"<a href=\"/)" +
            "(view/\\d++/)" +
            "(\">)" +
            "([^<]*)" +
            "(</a>\"</strong>)";

    public static final String COMMENT_PATTERN = "(<a href=\")" +
            "([^\"]*)" +
            "(\"><img class=\"comment_useravatar\" src=\")" +
            "([^\"]*)" +
            "(\" alt=\")" +
            "([^\"]*)" +
            "(\" />)";

    public static final String LIMIT_PATTERN = "You have posted 15 comments or shouts in the last 5 minutes. " +
            "Please try again later.";

}
