package com.seledrex.gui;

import com.seledrex.tasks.FavingTask;
import com.seledrex.tasks.LoadJsonTask;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.io.File;

public class Controller implements EventHandler<ActionEvent> {

    private final Model model;
    private final View view;

    public Controller(final Model model, final View view) {
        this.model = model;
        this.view = view;
    }

    //==================================================================================================================
    // Event Handler
    //==================================================================================================================

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

    private void handleStartButton()
    {
        view.setStateInProgress();
        model.setStopFlag(false);

        // Run and bind faving task
        FavingTask favingTask = new FavingTask(model, view);
        new Thread(favingTask).start();
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
     *
     * @param userFile user profile JSON.
     * @param useCookie log in using a cookie.
     */
    public void login(File userFile, boolean useCookie)
    {
        // Run JSON task
        new Thread(new LoadJsonTask(model, view, userFile, useCookie)).start();
    }

}
