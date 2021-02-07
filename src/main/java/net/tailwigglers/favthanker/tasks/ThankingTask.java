package net.tailwigglers.favthanker.tasks;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.html.*;
import javafx.concurrent.Task;
import net.tailwigglers.favthanker.gui.Model;
import net.tailwigglers.favthanker.gui.View;
import net.tailwigglers.favthanker.util.Constants;
import net.tailwigglers.favthanker.util.Favorite;
import net.tailwigglers.favthanker.util.Group;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;

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
        HtmlPage userPageLink = model.getWebClient().getPage(Constants.FA_BASE_URL + "msg/others/#favorites");
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
            Matcher matcher = Constants.FAV_PATTERN.matcher(userPageSrc);

            // Create map top hold favorite information
            HashMap<String, Integer> shouteeMap = new HashMap<>();
            ArrayList<Favorite> favoriteList = new ArrayList<>();

            // Keep track of the number of favorites a user gave
            while (matcher.find()) {
                String shoutee = matcher.group(6);
                String shouteeLink = Constants.FA_BASE_URL + matcher.group(4);
                String art = matcher.group(11);
                String artLink = Constants.FA_BASE_URL + matcher.group(9);

                favoriteList.add(new Favorite(shoutee, shouteeLink, art, artLink));

                if (!shouteeMap.containsKey(shoutee))
                    shouteeMap.put(shoutee, 1);
                else
                    shouteeMap.put(shoutee, shouteeMap.get(shoutee) + 1);
            }

            // Done if no more favorites are found
            if (shouteeMap.isEmpty())
                break;

            // Loop while there are still users in the map
            while (shouteeMap.size() != 0)
            {
                // Loop through all the users that left a favorite
                for (Iterator<Map.Entry<String, Integer>> entryIt = shouteeMap.entrySet().iterator(); entryIt.hasNext();)
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
                    final String shouteeWithoutUnderscore = shoutee.replace("_", "");
                    int favs = entry.getValue();
                    view.print("Processing " + shoutee);

                    // Load other user's page
                    String shouteeLink = String.format(Constants.FA_BASE_URL + "user/%s/", shouteeWithoutUnderscore);
                    HtmlPage shouteeUserPage = model.getWebClient().getPage(shouteeLink);

                    String src = shouteeUserPage.getWebResponse().getContentAsString();

                    // Get the form
                    List<HtmlForm> formList = shouteeUserPage.getForms();

                    // Search for shouts made by user and other user
                    matcher = Constants.COMMENT_PATTERN.matcher(src);
                    boolean foundUser = false;
                    while (matcher.find()) {
                        if (matcher.group(6).equals(model.getUsername().toLowerCase())
                                || matcher.group(6).equals(shouteeWithoutUnderscore.toLowerCase())) {
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

                    // Fix character encoding
                    String encodedMessage = new String(message.getBytes(), StandardCharsets.UTF_8);

                    // Set inside the shout box and submit
                    shoutBox.setText(encodedMessage);
                    shouteeUserPage = submitButton.click();

                    // Check the source of response
                    src = shouteeUserPage.getWebResponse().getContentAsString();
                    matcher = Constants.COMMENT_PATTERN.matcher(src);

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
                        // For safety, we will just throw an exception here!
                        view.print("Shout failed for " + shoutee);
                        System.err.println(src);
                        throw new Exception("Shout failed for " + shoutee);
                    }
                }
            }

            // Print favorites
            for (Favorite favorite : favoriteList)
                model.getFavWriter().printFavorite(favorite);

            // Get the correct form
            List<HtmlForm> formList = userPageLink.getForms();
            HtmlForm form = formList.stream()
                    .filter(htmlForm -> {
                        List<HtmlInput> inputs = htmlForm.getInputsByName("favorites[]");
                        return !inputs.isEmpty();
                    })
                    .findFirst()
                    .orElseThrow(() -> new Exception("Could not find notifications form!"));

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
