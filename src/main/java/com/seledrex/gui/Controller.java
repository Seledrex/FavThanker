package com.seledrex.gui;

import com.seledrex.tasks.ThankingTask;
import com.seledrex.tasks.LoadJsonTask;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.io.File;

/**
 * Controller handles the callbacks for each of the GUI components.
 */
public class Controller implements EventHandler<ActionEvent> {

    private final Model model;
    private final View view;

    /**
     * Creates a controller with model and view.
     * @param model GUI model.
     * @param view GUI view.
     */
    Controller(final Model model, final View view) {
        this.model = model;
        this.view = view;
    }

    /**
     * Handles action events for components registered to the controller.
     * @param event Action event.
     */
    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == view.getStartButton()) {
            handleStartButton();
        }
        else if (event.getSource() == view.getStopButton()) {
            handleStopButton();
        }
        else if (event.getSource() == view.getSelectUserButton()) {
            handleSelectUserButton();
        }
        event.consume();
    }

    /**
     * Handles the callback for the start button. This will start the FavingTask.
     */
    private void handleStartButton()
    {
        view.setStateInProgress();
        model.setStopFlag(false);

        new Thread(new ThankingTask(model, view)).start();
    }

    /**
     * Handles the stop button.
     */
    private void handleStopButton() {
        model.setStopFlag(true);
    }

    /**
     * Handles the select user button.
     */
    private void handleSelectUserButton()
    {
        view.setVeilVisible(true);
        File file = view.openFile();

        // Login using file
        if (file != null) {
            login(file, false);
        } else {
            view.setVeilVisible(false);
        }
    }

    /**
     * Logs into FA.
     * @param userFile user profile JSON.
     * @param useCookie log in using a cookie.
     */
    void login(File userFile, boolean useCookie)
    {
        new Thread(new LoadJsonTask(model, view, userFile, useCookie)).start();
    }

}
