package com.seledrex.tasks;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.html.*;
import com.seledrex.gui.Model;
import com.seledrex.gui.View;
import com.seledrex.util.Constants;
import com.seledrex.util.Group;
import javafx.concurrent.Task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThankingTask extends Task<Void> {

    private Model model;
    private View view;

    private int favCount;

    public ThankingTask(final Model model, final View view) {
        this.model = model;
        this.view = view;
        view.getProgressBar().progressProperty().bind(this.progressProperty());
    }

    @Override
    protected Void call() throws Exception
    {
        // Get favorites page and source
        HtmlPage userPageLink = model.getWebClient().getPage("http://www.furaffinity.net/msg/others/#favorites");
        String userPageSrc = userPageLink.getWebResponse().getContentAsString();

        // Retrieve fav count
        HtmlAnchor favoritesAnchor;
        try {
            favoritesAnchor = userPageLink.getAnchorByHref("/msg/others/#favorites");
        } catch (ElementNotFoundException e) {
            throw new Exception("No favorites in notification center.");
        }

        favCount = Integer.parseInt(favoritesAnchor.getTextContent().substring(0, favoritesAnchor.getTextContent().length() - 1));
        int numProcessed = 0;

        // Update progress
        setProgress(numProcessed, favCount);

        // Loop until all favorites are processed
        while (numProcessed < favCount)
        {
            // Find favorites on user page
            Pattern favPattern = Pattern.compile("(name=\"favorites\\[]\" value=\")(\\d{9})(\"><a href=\")([^\"]*)(\"><strong>)([^<]*)(</strong>)");
            Matcher matcher = favPattern.matcher(userPageSrc);

            // Create map top hold favorite information
            HashMap<String, Integer> userMap = new HashMap<>();

            // Keep track of the number of favorites a user gave
            while (matcher.find()) {
                String otherUser = matcher.group(6);
                if (!userMap.containsKey(otherUser))
                    userMap.put(otherUser, 1);
                else
                    userMap.put(otherUser, userMap.get(otherUser) + 1);
            }

            // Done if no more favorites are found
            if (userMap.isEmpty())
                break;

            // Patterns for comments and shout limit
            Pattern commentPattern = Pattern.compile("(<a href=\")([^\"]*)(\"><img class=\"comment_useravatar\" src=\")([^\"]*)(\" alt=\")([^\"]*)(\" />)");
            Pattern limitPattern = Pattern.compile("You have posted 15 comments or shouts in the last 5 minutes. Please try again later.");

            // Loop while there are still users in the map
            while (userMap.size() != 0)
            {

                // Loop through all the users that left a favorite
                for (Iterator<Map.Entry<String, Integer>> entryIt = userMap.entrySet().iterator(); entryIt.hasNext();)
                {
                    // Check if we need to stop
                    if (model.getStopFlag()) {
                        view.print("Stopped");
                        return null;
                    }

                    // Sleeping to make sure we don't send requests too quickly
                    Thread.sleep(1000);

                    // Get the entry and save the user and fav count
                    Map.Entry<String, Integer> entry = entryIt.next();
                    final String shoutee = entry.getKey();
                    int favs = entry.getValue();
                    view.print("Processing " + shoutee);

                    // Load other user's page
                    String shouteeLink = String.format("http://www.furaffinity.net/user/%s/",
                            shoutee.replace("_", ""));
                    HtmlPage otherUserPage = model.getWebClient().getPage(shouteeLink);

                    String src = otherUserPage.getWebResponse().getContentAsString();

                    // Get the form
                    List<HtmlForm> formList = otherUserPage.getForms();

                    // Search for shouts made by user and other user
                    matcher = commentPattern.matcher(src);
                    boolean foundUser = false;
                    while (matcher.find()) {
                        if (matcher.group(6).equals(model.getUsername().toLowerCase()) || matcher.group(6).equals(shoutee.toLowerCase())) {
                            foundUser = true;
                            break;
                        }
                    }

                    // Make sure they did not disable their account
                    if (formList.size() < 2)
                        foundUser = true;

                    // If not valid, remove the other user
                    if (foundUser) {
                        view.print("Skipping " + shoutee);
                        entryIt.remove();

                        numProcessed += favs;
                        setProgress(numProcessed, favCount);
                        continue;
                    }

                    HtmlForm form = formList.get(formList.size() - 1);

                    // Get shout box and submit button
                    HtmlTextArea shoutBox = form.getTextAreaByName("shout");
                    HtmlButton submitButton = form.getButtonByName("submit");

                    // Take a random message
                    String message = model.getGroups().stream()
                            .filter(group -> group.containsUser(shoutee))
                            .findFirst()
                            .map(Group::getRandomMessage)
                            .orElseGet(() -> {
                                int rand = ThreadLocalRandom.current().nextInt(0, model.getMessages().size());
                                return model.getMessages().get(rand);
                            });

                    // Set inside the shout box and submit
                    shoutBox.setText(message);
                    otherUserPage = submitButton.click();

                    // Check the source of response
                    src = otherUserPage.getWebResponse().getContentAsString();
                    matcher = commentPattern.matcher(src);

                    // See if user is there
                    while (matcher.find()) {
                        if (matcher.group(6).equals(model.getUsername().toLowerCase())) {
                            foundUser = true;
                            break;
                        }
                    }

                    // If shout is successfully verified, then remove!
                    if (foundUser) {
                        String groupName = model.getGroups().stream()
                                .filter(group -> group.containsUser(shoutee))
                                .findFirst()
                                .map(Group::getName)
                                .orElse("None");

                        view.print("Shouted at " + shoutee);
                        model.getShoutWriter().printShout(shoutee, groupName, message, shouteeLink);
                        entryIt.remove();

                        numProcessed += favs;
                        setProgress(numProcessed, favCount);

                        if (numProcessed != favCount) {
                            Thread.sleep(Constants.WAIT_SHOUT);
                        }
                    } else {
                        // See why shout failed
                        view.print("Shout failed for " + shoutee);
                        matcher = limitPattern.matcher(src);

                        // Perform cool down if too many shouts were made
                        if (matcher.find()) {
                            view.print("15 shouts made within 5 minutes!");
                            view.print("Cooldown period beginning...");
                            for (int i = 0; i < 5; i++) {
                                Thread.sleep(Constants.ONE_MINUTE);
                                view.print("...");
                            }
                            view.print("Proceeding...");
                        }
                    }
                }
            }

            // Get the login form
            List<HtmlForm> formList = userPageLink.getForms();
            HtmlForm form = formList.get(1);

            // Get checkboxes
            List<HtmlInput> checkBoxInputs = form.getInputsByName("favorites[]");
            HtmlButton removeSelectedButton = form.getButtonByName("remove-favorites");

            // Check all checkboxes
            for (HtmlInput checkBoxInput : checkBoxInputs) {
                checkBoxInput.setChecked(true);
            }

            // Remove favorites
            userPageLink = removeSelectedButton.click();
            userPageSrc = userPageLink.getWebResponse().getContentAsString();

            view.print("Cleared favorite notifications");
        }

        return null;
    }

    @Override
    protected void failed()
    {
        view.setStateProgressError(this.getException());
    }

    @Override
    protected void succeeded()
    {
        updateProgress(favCount, favCount);
        view.setStateProgressSuccess(favCount);
    }

    private void setProgress(double current, double max) {
        updateProgress(current, max);
        view.updateProgress(current, max);
    }
}
