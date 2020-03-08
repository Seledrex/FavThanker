package net.tailwigglers.favthanker.tasks;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import javafx.concurrent.Task;
import net.tailwigglers.favthanker.gui.Model;
import net.tailwigglers.favthanker.gui.View;
import net.tailwigglers.favthanker.util.Constants;

public class CheckFaOnlineTask extends Task<Void> {

    private Model model;
    private View view;

    public CheckFaOnlineTask(Model model, View view) {
        this.model = model;
        this.view = view;
    }

    @Override
    protected Void call() throws Exception {
        HtmlPage faHomePage = model.getWebClient().getPage(Constants.FA_BASE_URL);

        if (faHomePage.getWebResponse().getContentAsString().contains(Constants.CLOUDFLARE_PATTERN)) {
            throw new Exception("Cloudflare");
        }

        return null;
    }

    protected void failed() {
        view.setFaStatusLabel(false);
        this.getException().printStackTrace();
    }

    @Override
    protected void succeeded() {
        view.setFaStatusLabel(true);
    }

}
