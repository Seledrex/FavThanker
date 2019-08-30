package com.seledrex.gui;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.WebClient;
import com.seledrex.util.Constants;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

public class Model {

    private Properties props;

    private WebClient webClient;

    private boolean foundConfig;
    private boolean stopFlag;

    private String username;

    private ArrayList<String> messages;

    public Model() {
        // Initialize variables
        messages = new ArrayList<>();
        foundConfig = false;
        stopFlag = false;
        username = "";

        // Create new web client
        webClient = new WebClient();
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setTimeout(30000);

        // Enable cookies
        CookieManager manager = webClient.getCookieManager();
        manager.setCookiesEnabled(true);
        webClient.setCookieManager(manager);

        // Read in config properties
        try (InputStream input = new FileInputStream("config.properties")) {
            props = new Properties();
            props.load(input);
            foundConfig = true;
        } catch (IOException ex) {
            props = new Properties();
        }
    }

    public void persist() throws Exception {
        // Save config
        OutputStream output = new FileOutputStream("config.properties");
        if (props == null)
            props = new Properties();
        props.setProperty("username", username);
        props.store(output, null);

        // Save cookies
        ObjectOutput out = new ObjectOutputStream(new FileOutputStream(Constants.COOKIE_FILENAME));
        out.writeObject(webClient.getCookieManager().getCookies());
        out.close();

        // Close web client
        webClient.close();
    }

    public boolean getStopFlag() {
        return stopFlag;
    }

    public void setStopFlag(boolean stopFlag) {
        this.stopFlag = stopFlag;
    }

    public WebClient getWebClient() {
        return webClient;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ArrayList<String> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<String> messages) {
        this.messages = messages;
    }

    public boolean isFoundConfig() {
        return foundConfig;
    }

    public Properties getProps() {
        return props;
    }
}
