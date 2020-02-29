package net.tailwigglers.favthanker.tasks;

import com.gargoylesoftware.htmlunit.html.*;
import javafx.concurrent.Task;
import net.tailwigglers.favthanker.gui.Model;
import net.tailwigglers.favthanker.gui.View;
import net.tailwigglers.favthanker.util.Constants;
import net.tailwigglers.favthanker.util.Group;

import java.io.FileOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class LoginTask extends Task<Void> {

    private Model model;
    private View view;

    private String tempUsername;
    private String tempPassword;
    private ArrayList<String> tempMessages;
    private ArrayList<Group> tempGroups;

    private HtmlPage captchaLoginPage;
    private String captchaMessage;

    LoginTask(final Model model,
              final View view,
              final String tempUsername,
              final String tempPassword,
              final ArrayList<String> tempMessages,
              final ArrayList<Group> tempGroups,
              final HtmlPage captchaLoginPage,
              final String captchaMessage) {
        this.model = model;
        this.view = view;
        this.tempUsername = tempUsername;
        this.tempPassword = tempPassword;
        this.tempMessages = tempMessages;
        this.tempGroups = tempGroups;
        this.captchaLoginPage = captchaLoginPage;
        this.captchaMessage = captchaMessage;
    }

    @Override
    protected Void call() throws Exception
    {
        // Get the login form
        List<HtmlForm> formList = captchaLoginPage.getForms();
        HtmlForm form = formList.get(1);

        // Get the username, password, and captcha fields
        HtmlTextInput usernameInput = form.getInputByName("name");
        HtmlPasswordInput passwordInput = form.getInputByName("pass");
        HtmlTextInput captchaInput = form.getInputByName("captcha");

        // Set fields
        usernameInput.setText(tempUsername);
        passwordInput.setText(tempPassword);
        captchaInput.setText(captchaMessage);

        // Hit login button
        HtmlInput loginButton = form.getInputByName("login");
        HtmlPage afterLoginPage = loginButton.click();

        // Validate login
        if (!afterLoginPage.getUrl().toString().equals("http://www.furaffinity.net/") &&
                !afterLoginPage.getUrl().toString().equals("https://www.furaffinity.net/")) {
            view.print(afterLoginPage.getUrl().toString());
            throw new Exception("Login failed! Try again.");
        }

        // Write cookies to file
        ObjectOutput out = new ObjectOutputStream(new FileOutputStream(Constants.COOKIE_FILENAME));
        out.writeObject(model.getWebClient().getCookieManager().getCookies());
        out.close();

        model.setUsername(tempUsername);
        model.setMessages(tempMessages);
        model.setGroups(tempGroups);
        view.welcomeUser();
        return null;
    }

    @Override
    protected void failed() {
        view.setStateError(this.getException());
    }
}
