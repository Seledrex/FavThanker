package net.tailwigglers.favthanker.gui;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.WebClient;
import net.tailwigglers.favthanker.util.Constants;
import net.tailwigglers.favthanker.util.FavWriter;
import net.tailwigglers.favthanker.util.Group;
import net.tailwigglers.favthanker.util.ShoutWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

public class Model {

    private Properties props;
    private WebClient webClient;
    private ShoutWriter shoutWriter;
    private FavWriter favWriter;

    private boolean foundConfig;
    private boolean stopFlag;

    private String username;
    private ArrayList<String> messages;
    private ArrayList<Group> groups;

    Model() {
        // Initialize variables
        messages = new ArrayList<>();
        groups = new ArrayList<>();
        foundConfig = false;
        stopFlag = false;
        username = "";

        // Create new web client
        webClient = new WebClient();
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setTimeout(30000);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.waitForBackgroundJavaScript(10_000);
        webClient.waitForBackgroundJavaScriptStartingBefore(10000);
        webClient.setJavaScriptTimeout(10000);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getCache().setMaxSize(0);

        // Enable cookies
        CookieManager manager = webClient.getCookieManager();
        manager.setCookiesEnabled(true);
        webClient.setCookieManager(manager);

        // Read in config properties
        try (InputStream input = new FileInputStream(Constants.CONFIG_FILENAME)) {
            props = new Properties();
            props.load(input);
            foundConfig = true;
        } catch (IOException ex) {
            props = new Properties();
        }

        try {
            shoutWriter = new ShoutWriter();
            favWriter = new FavWriter();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    void persist() throws Exception {
        // Save config
        OutputStream output = new FileOutputStream(Constants.CONFIG_FILENAME);
        if (props == null)
            props = new Properties();
        props.setProperty(Constants.USERNAME, username);
        props.store(output, null);

        // Save cookies
        ObjectOutput out = new ObjectOutputStream(new FileOutputStream(Constants.COOKIE_FILENAME));
        out.writeObject(webClient.getCookieManager().getCookies());
        out.close();

        // Close stuff
        webClient.close();
        shoutWriter.close();
        favWriter.close();
    }

    public boolean getStopFlag() {
        return stopFlag;
    }

    public WebClient getWebClient() {
        return webClient;
    }

    public ShoutWriter getShoutWriter() {
        return shoutWriter;
    }

    public FavWriter getFavWriter() {
        return favWriter;
    }

    public String getUsername() {
        return username;
    }

    public ArrayList<String> getMessages() {
        return messages;
    }

    public ArrayList<Group> getGroups() {
        return groups;
    }

    Properties getProps() {
        return props;
    }

    boolean getFoundConfig() {
        return foundConfig;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setMessages(ArrayList<String> messages) {
        this.messages = messages;
    }

    public void setGroups(ArrayList<Group> groups) {
        this.groups = groups;
    }

    void setStopFlag(boolean stopFlag) {
        this.stopFlag = stopFlag;
    }
}
