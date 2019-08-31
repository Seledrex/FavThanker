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

/**
 * Task for loading Json.
 */
public class LoadJsonTask extends Task<Void> {

    private Model model;
    private View view;

    private File userFile;
    private boolean useCookie;

    private String tempUsername;
    private String tempPassword;
    private ArrayList<String> tempMessages = new ArrayList<>();

    /**
     * Creates load json task thread.
     * @param model Model.
     * @param view View.
     * @param userFile User file.
     * @param useCookie Using cookie.
     */
    public LoadJsonTask(final Model model, final View view, final File userFile, final boolean useCookie) {
        this.model = model;
        this.view = view;
        this.userFile = userFile;
        this.useCookie = useCookie;
    }

    /**
     * Task callback.
     * @return null.
     * @throws Exception Exception.
     */
    @Override
    protected Void call() throws Exception
    {
        // Create JSON object
        Object obj = new JSONParser().parse(new FileReader(userFile));
        JSONObject jo = (JSONObject) obj;

        // Store data
        tempUsername = (String) jo.get("username");
        tempPassword = (String) jo.get("password");
        JSONArray ja = (JSONArray) jo.get("messages");
        for (Object message : ja) {
            tempMessages.add((String) message);
        }

        return null;
    }

    /**
     * Failure callback.
     */
    @Override
    protected void failed()
    {
        view.setStateLoadJsonTaskError(userFile);
    }

    /**
     * Success callback.
     */
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
            new Thread(new GetCaptchaTask(model, view, tempUsername, tempPassword, tempMessages)).start();
        }
    }
}
