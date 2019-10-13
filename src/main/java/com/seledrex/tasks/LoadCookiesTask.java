package com.seledrex.tasks;

import com.gargoylesoftware.htmlunit.util.Cookie;
import com.seledrex.gui.Model;
import com.seledrex.gui.View;
import javafx.concurrent.Task;

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

    LoadCookiesTask(final Model model,
                    final View view,
                    final File cookieFile,
                    final String tempUsername,
                    final ArrayList<String> tempMessages) {
        this.model = model;
        this.view = view;
        this.cookieFile = cookieFile;
        this.tempUsername = tempUsername;
        this.tempMessages = tempMessages;
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

        model.setUsername(tempUsername);
        model.setMessages(tempMessages);
        view.welcomeUser();

        return null;
    }

    @Override
    protected void failed() {
        view.setStateError(this.getException());
    }

}
