package net.tailwigglers.favthanker.tasks;

import javafx.concurrent.Task;
import net.tailwigglers.favthanker.gui.Model;
import net.tailwigglers.favthanker.gui.View;
import net.tailwigglers.favthanker.util.Constants;
import net.tailwigglers.favthanker.util.Group;
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
    private ArrayList<Group> tempGroups = new ArrayList<>();

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

        // Store group info
        JSONObject groups = (JSONObject) jo.get("groups");
        groups.forEach((Object groupName, Object group) -> {
            ArrayList<String> users = new ArrayList<>();
            ArrayList<String> messages = new ArrayList<>();

            JSONObject jGroup = (JSONObject) group;
            JSONArray jUsers = (JSONArray) jGroup.get("users");
            JSONArray jMessages = (JSONArray) jGroup.get("messages");

            for (Object user : jUsers)
                users.add((String) user);

            for (Object message : jMessages)
                messages.add((String) message);

            tempGroups.add(new Group((String) groupName, users, messages));
        });

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
            new Thread(new LoadCookiesTask(model, view, cookieFile, tempUsername, tempMessages, tempGroups)).start();
        }
        else {
            new Thread(new GetCaptchaTask(model, view, tempUsername, tempMessages, tempGroups)).start();
        }
    }
}
