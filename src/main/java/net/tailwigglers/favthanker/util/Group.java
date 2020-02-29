package net.tailwigglers.favthanker.util;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Group {

    private String name;
    private ArrayList<String> users;
    private ArrayList<String> messages;

    public Group(String name, ArrayList<String> users, ArrayList<String> messages) {
        this.name = name;
        this.users = users;
        this.messages = messages;
    }

    public boolean containsUser(String userToCheck) {
        return users.stream().anyMatch((String user) -> user.equals(userToCheck));
    }

    public String getName() {
        return name;
    }

    public String getRandomMessage() {
        int rand = ThreadLocalRandom.current().nextInt(0, messages.size());
        return messages.get(rand);
    }

}
