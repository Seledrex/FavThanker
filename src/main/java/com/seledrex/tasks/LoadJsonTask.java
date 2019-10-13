package com.seledrex.tasks;

import com.seledrex.gui.Model;
import com.seledrex.gui.View;
import com.seledrex.util.Constants;
import javafx.concurrent.Task;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class LoadJsonTask extends Task<Void> {

    private Model model;
    private View view;

    private File userFile;
    private boolean useCookie;

    private String tempUsername;
    private ArrayList<String> tempMessages = new ArrayList<>();

    public LoadJsonTask(final Model model, final View view, final File userFile, final boolean useCookie) {
        this.model = model;
        this.view = view;
        this.userFile = userFile;
        this.useCookie = useCookie;
    }

    @Override
    protected Void call() throws Exception
    {
        // Create JSON object
        Object obj = new JSONParser().parse(new FileReader(userFile));
        JSONObject jo = (JSONObject) obj;

        // Store data
        tempUsername = (String) jo.get("username");
        JSONArray ja = (JSONArray) jo.get("messages");
        for (Object message : ja) {
            tempMessages.add((String) message);
        }

        return null;
    }

    protected void failed()
    {
        view.setStateLoadJsonTaskError(userFile);
    }

    @Override
    protected void succeeded()
    {
        // Check for cookies
        File cookieFile = new File(Constants.COOKIE_FILENAME);

        // Check whether to login with cookie
        if (cookieFile.exists() && useCookie)
        {
            new Thread(new LoadCookiesTask(model, view, cookieFile, tempUsername, tempMessages)).start();
        }
        else {
            new Thread(new GetCaptchaTask(model, view, tempUsername, tempMessages)).start();
        }
    }
}