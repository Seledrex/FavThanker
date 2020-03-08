package net.tailwigglers.favthanker.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import net.tailwigglers.favthanker.tasks.CheckFaOnlineTask;
import net.tailwigglers.favthanker.tasks.LoadJsonTask;
import net.tailwigglers.favthanker.tasks.ThankingTask;

import java.io.File;

public class Controller implements EventHandler<ActionEvent> {

    private final Model model;
    private final View view;

    Controller(final Model model, final View view) {
        this.model = model;
        this.view = view;
    }

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

    private void handleStartButton() {
        view.setStateInProgress();
        model.setStopFlag(false);

        new Thread(new ThankingTask(model, view)).start();
    }

    private void handleStopButton() {
        model.setStopFlag(true);
    }

    private void handleSelectUserButton() {
        view.setVeilVisible(true);
        File file = view.openFile();

        // Login using file
        if (file != null) {
            login(file, false);
        } else {
            view.setVeilVisible(false);
        }
    }

    void login(File userFile, boolean useCookie) {
        new Thread(new LoadJsonTask(model, view, userFile, useCookie)).start();
    }

    void checkFaOnline() {
        new Thread(new CheckFaOnlineTask(model, view)).start();
    }

}
