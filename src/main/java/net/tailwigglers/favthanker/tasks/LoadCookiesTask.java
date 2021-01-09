package net.tailwigglers.favthanker.tasks;

import com.gargoylesoftware.htmlunit.util.Cookie;
import javafx.concurrent.Task;
import net.tailwigglers.favthanker.gui.Model;
import net.tailwigglers.favthanker.gui.View;
import net.tailwigglers.favthanker.util.Group;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Set;

public class LoadCookiesTask extends Task<Void> {

    private Model model;
    private View view;

    private File cookieFile;
    private String tempUsername;
    private ArrayList<String> tempMessages;
    private ArrayList<Group> tempGroups;

    LoadCookiesTask(final Model model,
                    final View view,
                    final File cookieFile,
                    final String tempUsername,
                    final ArrayList<String> tempMessages,
                    final ArrayList<Group> tempGroups) {
        this.model = model;
        this.view = view;
        this.cookieFile = cookieFile;
        this.tempUsername = tempUsername;
        this.tempMessages = tempMessages;
        this.tempGroups = tempGroups;
    }

    @Override
    protected Void call() throws Exception
    {
        // Read in cookie file
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(cookieFile));
        @SuppressWarnings("unchecked")
        Set<Cookie> cookies = (Set<Cookie>) in.readObject();
        in.close();

        // Add cookies
        for (Cookie cookie : cookies) {
            model.getWebClient().getCookieManager().addCookie(cookie);
        }

        return null;
    }

    @Override
    protected void succeeded() {
        new Thread(new VerifyLoginTask(model, view, tempUsername, tempMessages, tempGroups)).start();
    }

    @Override
    protected void failed() {
        view.setStateError(this.getException());
    }

}
