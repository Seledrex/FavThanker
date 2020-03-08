package net.tailwigglers.favthanker.util;

import java.util.regex.Pattern;

public class Constants {

    public static final int WAIT_SHOUT = 20000;

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
    public static final String VERSION = "Version 1.0";
    public static final String COPYRIGHT = "© Tail Wigglers";

    public static final String FA_BASE_URL = "http://www.furaffinity.net/";

    public static final Pattern FAV_PATTERN = Pattern.compile(
            "(name=\"favorites\\[]\" value=\")" +
            "(\\d+)" +
            "(\"><a href=\"/)" +
            "([^\"]*)" +
            "(\"><strong>)" +
            "([^<]*)" +
            "(</strong>)" +
            "(</a> favorited <a href=\"/)" +
            "(view/\\d+/)" +
            "(\"><strong>\")" +
            "([^\"]*)" +
            "(\"</strong>)");

    public static final Pattern COMMENT_PATTERN = Pattern.compile(
            "(<a href=\")" +
            "([^\"]*)" +
            "(\"><img class=\"comment_useravatar\" src=\")" +
            "([^\"]*)" +
            "(\" alt=\")" +
            "([^\"]*)" +
            "(\" />)");

}
