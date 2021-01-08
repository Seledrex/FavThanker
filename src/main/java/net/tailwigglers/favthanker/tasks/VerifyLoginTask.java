package net.tailwigglers.favthanker.tasks;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import javafx.concurrent.Task;
import net.tailwigglers.favthanker.gui.Model;
import net.tailwigglers.favthanker.gui.View;
import net.tailwigglers.favthanker.util.Constants;
import net.tailwigglers.favthanker.util.Group;

import java.io.FileOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;

public class VerifyLoginTask extends Task<Void> {

    private final Model model;
    private final View view;
    private final String tempUsername;
    private final ArrayList<String> tempMessages;
    private final ArrayList<Group> tempGroups;
    private final String cookieA;
    private final String cookieB;

    public VerifyLoginTask(Model model,
                           View view,
                           String tempUsername,
                           ArrayList<String> tempMessages,
                           ArrayList<Group> tempGroups,
                           String cookieA,
                           String cookieB
    ) {
        this.model = model;
        this.view = view;
        this.tempUsername = tempUsername;
        this.tempMessages = tempMessages;
        this.tempGroups = tempGroups;
        this.cookieA = cookieA;
        this.cookieB = cookieB;
    }

    public VerifyLoginTask(Model model,
                           View view,
                           String tempUsername,
                           ArrayList<String> tempMessages,
                           ArrayList<Group> tempGroups
    ) {
        this(model, view, tempUsername, tempMessages, tempGroups, null, null);
    }

    @Override
    protected Void call() throws Exception {
        if (cookieA != null && cookieB != null) {
            model.getWebClient().getCookieManager().clearCookies();
            model.getWebClient().addCookie(String.format("a=%s", cookieA), new URL(Constants.FA_BASE_URL), null);
            model.getWebClient().addCookie(String.format("b=%s", cookieB), new URL(Constants.FA_BASE_URL), null);
        }

        HtmlPage faHomePage = model.getWebClient().getPage(Constants.FA_BASE_URL);

        if (faHomePage.getWebResponse().getContentAsString().contains(tempUsername)) {
            // Write cookies to file
            ObjectOutput out = new ObjectOutputStream(new FileOutputStream(Constants.COOKIE_FILENAME));
            out.writeObject(model.getWebClient().getCookieManager().getCookies());
            out.close();
        } else {
            throw new Exception("Login failed! Try again.");
        }

        return null;
    }

    @Override
    protected void succeeded() {
        model.setUsername(tempUsername);
        model.setMessages(tempMessages);
        model.setGroups(tempGroups);
        view.welcomeUser();
    }

    @Override
    protected void failed() {
        view.setStateError(this.getException());
    }
}
