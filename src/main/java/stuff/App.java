package stuff;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.util.Cookie;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App extends Application implements EventHandler<ActionEvent> {

    private static final int WAIT_SHOUT = 20000;
    private static final int ONE_MINUTE = 60000;

    private static final String COOKIE_FILENAME = "cookie.file";

    //==================================================================================================================
    // Application Variables
    //==================================================================================================================

    private Properties props;
    private WebClient webClient;

    private boolean foundConfig;
    private boolean stopFlag;

    private Label userLabel;

    private Button startButton;
    private Button stopButton;
    private Button selectUserButton;

    private ProgressBar progressBar;
    private Label progressLabel;

    private TextArea textArea;
    private Region veil;
    private Stage stage;

    private String username;
    private ArrayList<String> messages;

    //==================================================================================================================
    // Initialization
    //==================================================================================================================

    @Override
    public void init() {

        // Initialize variables
        messages = new ArrayList<>();
        foundConfig = false;
        stopFlag = false;
        username = "";

        // Create new web client
        webClient = new WebClient();
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setTimeout(30000);

        // Enable cookies
        CookieManager manager = webClient.getCookieManager();
        manager.setCookiesEnabled(true);
        webClient.setCookieManager(manager);

        // Read in config properties
        try (InputStream input = new FileInputStream("config.properties")) {
            props = new Properties();
            props.load(input);
            foundConfig = true;
        } catch (IOException ex) {
            props = new Properties();
        }
    }

    //==================================================================================================================
    // Startup
    //==================================================================================================================

    @Override
    public void start(Stage primaryStage) {

        // Set up GUI components
        userLabel = new Label("Please select a user!");
        userLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        userLabel.setAlignment(Pos.CENTER);

        startButton = new Button("Start");
        startButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        startButton.setDisable(true);
        startButton.addEventHandler(ActionEvent.ANY, this);

        stopButton = new Button("Stop");
        stopButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        stopButton.setDisable(true);
        stopButton.addEventHandler(ActionEvent.ANY, this);

        selectUserButton = new Button("Select User");
        selectUserButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        selectUserButton.addEventHandler(ActionEvent.ANY, this);

        progressBar = new ProgressBar();
        progressBar.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        progressLabel = new Label("Stopped");
        progressLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        progressLabel.setAlignment(Pos.CENTER);

        textArea = new TextArea();
        textArea.textProperty().addListener((observable, oldValue, newValue) -> textArea.setScrollTop(Double.MAX_VALUE));
        textArea.setEditable(false);

        // Create grid layout
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        int numCols = 3;
        for (int i = 0; i < numCols; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / (double) numCols);
            grid.getColumnConstraints().add(col);
        }

        RowConstraints row1 = new RowConstraints();
        RowConstraints row2 = new RowConstraints();
        RowConstraints row3 = new RowConstraints();
        RowConstraints row4 = new RowConstraints();
        RowConstraints row5 = new RowConstraints();

        row1.setPercentHeight(10.0);
        row2.setPercentHeight(10.0);
        row3.setPercentHeight(10.0);
        row4.setPercentHeight(35.0);
        row5.setPercentHeight(35.0);

        grid.getRowConstraints().addAll(row1, row2, row3, row4, row5);

        // Add GUI components to grid
        grid.add(userLabel, 0, 0, 3, 1);
        GridPane.setFillWidth(userLabel, true);
        GridPane.setFillHeight(userLabel, true);

        grid.add(startButton, 0, 1);
        GridPane.setFillWidth(startButton, true);
        GridPane.setFillHeight(startButton, true);

        grid.add(stopButton, 1, 1);
        GridPane.setFillWidth(stopButton, true);
        GridPane.setFillHeight(stopButton, true);

        grid.add(selectUserButton, 2, 1);
        GridPane.setFillWidth(selectUserButton, true);
        GridPane.setFillHeight(selectUserButton, true);

        grid.add(progressBar, 0, 2, 2, 1);
        GridPane.setFillWidth(progressBar, true);
        GridPane.setFillHeight(progressBar, true);

        grid.add(progressLabel, 2, 2, 1, 1);
        GridPane.setFillWidth(progressLabel, true);
        GridPane.setFillHeight(progressLabel, true);

        grid.add(textArea, 0, 3, 3, 3);
        GridPane.setFillWidth(textArea, true);
        GridPane.setFillHeight(textArea, true);

        // Create veil to overlay GUI while loading
        veil = new Region();
        veil.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3)");
        veil.setVisible(true);

        // Put grid and veil together in stack pane
        StackPane root = new StackPane();
        root.getChildren().addAll(grid, veil);

        // Create scene
        Scene scene = new Scene(root, 600, 300);

        // Set up stage
        stage = primaryStage;
        stage.setTitle("FA Favorite Thanker");
        stage.setScene(scene);
        stage.show();

        // Login if previously logged in
        if (foundConfig && props.getProperty("username") != null && !props.getProperty("username").equals("")) {
            String jsonFilename = props.getProperty("username") + ".json";
            login(new File(jsonFilename), true);
        } else {
            veil.setVisible(false);
        }
    }

    //==================================================================================================================
    // Stop
    //==================================================================================================================

    @Override
    public void stop() {

        // Save config
        try (OutputStream output = new FileOutputStream("config.properties")) {
            if (props == null)
                props = new Properties();
            props.setProperty("username", username);
            props.store(output, null);
        } catch (IOException io) {
            createExceptionDialog(io);
        }

        // Save cookies
        try {
            ObjectOutput out = new ObjectOutputStream(new FileOutputStream(COOKIE_FILENAME));
            out.writeObject(webClient.getCookieManager().getCookies());
            out.close();
        } catch (Exception e) {
            createExceptionDialog(e);
        }

        // Close web client
        webClient.close();
    }

    //==================================================================================================================
    // Event Handler
    //==================================================================================================================

    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == startButton) {
            handleStartButton();
        }
        else if (event.getSource() == stopButton) {
            handleStopButton();
        }
        else if (event.getSource() == selectUserButton) {
            handleSelectUserButton();
        }
        event.consume();
    }

    //==================================================================================================================
    // Methods
    //==================================================================================================================

    /**
     * Logs into FA.
     *
     * @param userFile user profile JSON.
     * @param useCookie log in using a cookie.
     */
    private void login(File userFile, boolean useCookie)
    {
        // Task for loading JSON file
        class LoadJsonTask extends Task<Void> {

            private String tempUsername;
            private String tempPassword;
            private ArrayList<String> tempMessages = new ArrayList<>();

            @Override
            protected Void call() throws Exception
            {
                // Create JSON object
                Object obj = new JSONParser().parse(new FileReader(userFile));
                JSONObject jo = (JSONObject) obj;

                // Store data
                tempUsername = (String) jo.get("username");
                tempPassword = (String) jo.get("password");
                JSONArray ja = (JSONArray) jo.get("messages");
                for (Object message : ja) {
                    tempMessages.add((String) message);
                }

                return null;
            }

            @Override
            protected void failed()
            {
                Platform.runLater(() -> {
                    Alert alert = new Alert(
                            Alert.AlertType.ERROR,
                            "Could not load " +
                                    userFile.getName() +
                                    ". Make sure you have correctly created your user file!"
                    );
                    alert.showAndWait();
                    veil.setVisible(false);
                });
            }

            @Override
            protected void succeeded()
            {
                // Check for cookies
                File cookieFile = new File(COOKIE_FILENAME);

                // Check whether to login with cookie
                if (cookieFile.exists() && useCookie)
                {
                    // Task for loading cookies
                    Task<Void> loadCookiesTask = new Task<Void>()
                    {
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
                                webClient.getCookieManager().addCookie(cookie);
                            }

                            welcomeUser();
                            return null;
                        }

                        @Override
                        protected void failed() {
                            Platform.runLater(() -> createExceptionDialog(this.getException()));
                        }
                    };

                    // Run task
                    new Thread(loadCookiesTask).start();
                }
                else {

                    // Task for getting captcha
                    class GetCaptchaTask extends Task<Void> {

                        private HtmlPage captchaLoginPage;
                        private HtmlForm form;

                        @Override
                        protected Void call() throws Exception
                        {
                            // Get the login page
                            HtmlPage loginPage = webClient.getPage("https://www.furaffinity.net/login/");
                            HtmlAnchor anchor = loginPage.getAnchorByHref("/login/?mode=imagecaptcha");
                            captchaLoginPage = anchor.click();

                            // Get the login form
                            List<HtmlForm> formList = captchaLoginPage.getForms();
                            form = formList.get(1);

                            // Get the username and password fields
                            HtmlTextInput usernameInput = form.getInputByName("name");
                            HtmlPasswordInput passwordInput = form.getInputByName("pass");

                            // Set fields
                            usernameInput.setText(tempUsername);
                            passwordInput.setText(tempPassword);

                            // Get and save captcha
                            HtmlImage captcha = captchaLoginPage.getHtmlElementById("captcha_img");
                            captcha.saveAs(new File("captcha.jpg"));
                            return null;
                        }

                        @Override
                        protected void failed() {
                            Platform.runLater(() -> createExceptionDialog(this.getException()));
                        }

                        @Override
                        protected void succeeded()
                        {
                            // Create the custom dialog.
                            Dialog<String> dialog = new Dialog<>();
                            dialog.setTitle("Captcha Dialog");
                            dialog.setHeaderText("Enter the captcha code.");

                            // Set the captcha
                            dialog.setGraphic(new ImageView(new File("captcha.jpg").toURI().toString()));

                            // Set the button types.
                            ButtonType enterButtonType = new ButtonType("Enter", ButtonBar.ButtonData.OK_DONE);
                            dialog.getDialogPane().getButtonTypes().addAll(enterButtonType, ButtonType.CANCEL);

                            // Create the captcha field
                            GridPane grid = new GridPane();
                            grid.setHgap(10);
                            grid.setVgap(10);
                            grid.setPadding(new Insets(20, 150, 10, 10));

                            TextField captchaField = new TextField();
                            captchaField.setPromptText("Captcha");

                            grid.add(new Label("Captcha:"), 0, 0);
                            grid.add(captchaField, 1, 0);

                            // Enable/Disable enter button depending on whether a captcha was entered.
                            Node enterButton = dialog.getDialogPane().lookupButton(enterButtonType);
                            enterButton.setDisable(true);

                            // Do some validation
                            captchaField.textProperty().addListener((observable, oldValue, newValue) ->
                                    enterButton.setDisable(newValue.trim().isEmpty()));

                            dialog.getDialogPane().setContent(grid);

                            // Convert the result
                            dialog.setResultConverter(dialogButton -> {
                                if (dialogButton == enterButtonType) {
                                    return captchaField.getText();
                                }
                                return null;
                            });

                            // Show dialog and get result
                            Optional<String> result = dialog.showAndWait();

                            // Check if captcha was actually entered
                            if (result.isPresent())
                            {
                                // Task for logging in
                                Task<Void> loginTask = new Task<Void>()
                                {
                                    @Override
                                    protected Void call() throws Exception
                                    {
                                        // Get captcha message and put into form
                                        String captchaMessage = result.get();
                                        HtmlTextInput captchaInput = form.getInputByName("captcha");
                                        captchaInput.setText(captchaMessage);

                                        // Hit login button
                                        HtmlInput loginButton = form.getInputByName("login");
                                        HtmlPage afterLoginPage = captchaLoginPage = loginButton.click();

                                        // Validate login
                                        if (!afterLoginPage.getUrl().toString().equals("http://www.furaffinity.net/")) {
                                            print(afterLoginPage.getUrl().toString());
                                            throw new Exception("Login failed! Try again.");
                                        }

                                        // Write cookies to file
                                        ObjectOutput out = new ObjectOutputStream(new FileOutputStream(COOKIE_FILENAME));
                                        out.writeObject(webClient.getCookieManager().getCookies());
                                        out.close();

                                        welcomeUser();
                                        return null;
                                    }

                                    @Override
                                    protected void failed() {
                                        Platform.runLater(() -> createExceptionDialog(this.getException()));
                                    }
                                };

                                // Run login task
                                new Thread(loginTask).start();
                            }
                            else {
                                Platform.runLater(() -> veil.setVisible(false));
                            }
                        }
                    }

                    // Run captcha task
                    GetCaptchaTask getCaptchaTask = new GetCaptchaTask();
                    new Thread(getCaptchaTask).start();
                }
            }

            /**
             * Helper method used to welcome a new user.
             */
            private void welcomeUser() {
                Platform.runLater(() -> {
                    username = tempUsername;
                    messages = tempMessages;

                    userLabel.setText("Welcome " + username + "!");
                    startButton.setDisable(false);
                    veil.setVisible(false);
                });
            }
        }

        // Run JSON task
        new Thread(new LoadJsonTask()).start();
    }

    /**
     * Helper method for printing to text area.
     * @param text Text to print.
     */
    private void print(String text) {
        Platform.runLater(() -> textArea.appendText(text + "\n"));
    }

    /**
     * Creates and shows a dialog that displays an exception.
     * @param e Exception.
     */
    private void createExceptionDialog(Throwable e)
    {
        e.printStackTrace();

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error has occurred.");
        alert.setContentText(e.getMessage());

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
        veil.setVisible(false);
    }

    /**
     *
     */
    private void handleStartButton()
    {
        // Update progress bar
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        progressLabel.setText("Starting");

        // Update buttons
        startButton.setDisable(true);
        stopButton.setDisable(false);
        selectUserButton.setDisable(true);

        // Update flag
        stopFlag = false;

        // Task for thanking favs
        class FavingTask extends Task<Void>
        {
            private int favCount;

            @Override
            protected Void call() throws Exception
            {
                // Get favorites page and source
                HtmlPage userPageLink = webClient.getPage("http://www.furaffinity.net/msg/others/#favorites");
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
                    Pattern favPattern = Pattern.compile("(name=\"favorites\\[\\]\" value=\")(\\d{9})(\"><a href=\")([^\"]*)(\"><strong>)([^<]*)(</strong>)");
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
                            if (stopFlag) {
                                print("Stopped");
                                return null;
                            }

                            // Sleeping to make sure we don't send requests too quickly
                            Thread.sleep(1000);

                            // Get the entry and save the user and fav count
                            Map.Entry<String, Integer> entry = entryIt.next();
                            String otherUser = entry.getKey();
                            int favs = entry.getValue();
                            print("Processing " + otherUser);

                            // Remove underscores
                            otherUser = otherUser.replace("_", "");

                            // Load other user's page
                            HtmlPage otherUserPage = webClient.getPage("http://www.furaffinity.net/user/" + otherUser + "/");
                            String src = otherUserPage.getWebResponse().getContentAsString();

                            // Get the form
                            List<HtmlForm> formList = otherUserPage.getForms();

                            // Search for shouts made by user and other user
                            matcher = commentPattern.matcher(src);
                            boolean foundUser = false;
                            while (matcher.find()) {
                                if (matcher.group(6).equals(username.toLowerCase()) || matcher.group(6).equals(otherUser.toLowerCase())) {
                                    foundUser = true;
                                    break;
                                }
                            }

                            // Make sure they did not disable their account
                            if (formList.size() < 2)
                                foundUser = true;

                            // If not valid, remove the other user
                            if (foundUser) {
                                print("Skipping " + otherUser);
                                entryIt.remove();

                                numProcessed += favs;
                                setProgress(numProcessed, favCount);
                                continue;
                            }

                            HtmlForm form = formList.get(1);

                            // Get shout box and submit button
                            HtmlTextArea shoutBox = form.getTextAreaByName("shout");
                            HtmlButton submitButton = form.getButtonByName("submit");

                            // Take a random message and set inside the shout box
                            int rand = ThreadLocalRandom.current().nextInt(0, messages.size());
                            shoutBox.setText(messages.get(rand));
                            otherUserPage = submitButton.click();

                            // Check the source of response
                            src = otherUserPage.getWebResponse().getContentAsString();
                            matcher = commentPattern.matcher(src);

                            // See if user is there
                            while (matcher.find()) {
                                if (matcher.group(6).equals(username.toLowerCase())) {
                                    foundUser = true;
                                    break;
                                }
                            }

                            // If shout is successfully verified, then remove!
                            if (foundUser) {
                                print("Shouted at " + otherUser);
                                entryIt.remove();

                                numProcessed += favs;
                                setProgress(numProcessed, favCount);

                                if (numProcessed != favCount) {
                                    Thread.sleep(WAIT_SHOUT);
                                }
                            } else {

                                // See why shout failed
                                print("Shout failed for " + otherUser);
                                matcher = limitPattern.matcher(src);

                                // Perform cool down if too many shouts were made
                                if (matcher.find()) {
                                    print("15 shouts made within 5 minutes!");
                                    print("Cooldown period beginning...");
                                    for (int i = 0; i < 5; i++) {
                                        Thread.sleep(ONE_MINUTE);
                                        print("...");
                                    }
                                    print("Proceeding...");
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

                    print("Cleared favorite notifications");
                }

                return null;
            }

            @Override
            protected void failed()
            {
                Platform.runLater(() -> {
                    createExceptionDialog(this.getException());

                    startButton.setDisable(false);
                    selectUserButton.setDisable(false);
                    stopButton.setDisable(true);

                    progressBar.progressProperty().unbind();
                    progressBar.progressProperty().setValue(ProgressBar.INDETERMINATE_PROGRESS);
                    progressLabel.setText("Stopped");
                });
            }

            @Override
            protected void succeeded()
            {
                updateProgress(favCount, favCount);
                Platform.runLater(() -> {
                    progressBar.progressProperty().unbind();
                    startButton.setDisable(false);
                    stopButton.setDisable(true);
                    selectUserButton.setDisable(false);

                    if (stopFlag) {
                        progressLabel.setText("Stopped");
                    } else {
                        progressLabel.setText(favCount + "/" + favCount);
                    }
                });
            }

            /**
             * Updates progress bar and label.
             * @param current Current progress.
             * @param max Max progress.
             */
            private void setProgress(double current, double max) {
                updateProgress(current, max);
                Platform.runLater(() -> progressLabel.setText((int) current + "/" + (int) max));
            }
        }

        // Run and bind faving task
        FavingTask favingTask = new FavingTask();
        progressBar.progressProperty().bind(favingTask.progressProperty());
        new Thread(favingTask).start();
    }

    /**
     * Handles the stop button.
     */
    private void handleStopButton() {
        stopFlag = true;
    }

    /**
     * Handles the select user button.
     */
    private void handleSelectUserButton()
    {
        veil.setVisible(true);

        // Open file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open User File");
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File file = fileChooser.showOpenDialog(stage);

        // Login using file
        if (file != null) {
            login(file, false);
        } else {
            veil.setVisible(false);
        }
    }

    //==================================================================================================================
    // Entry Point
    //==================================================================================================================

    public static void main(String[] args) {
        launch(args);
    }
}
