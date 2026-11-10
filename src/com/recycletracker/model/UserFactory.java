package com.recycletracker.model;

/** Creates the right {@link User} subclass from a type name. */
public final class UserFactory {

    public static final String[] TYPES = {"Household", "Business"};

    private UserFactory() {
    }

    public static User create(String type, String id, String name) {
        switch (type) {
            case "Household":
                return new Household(id, name);
            case "Business":
                return new Business(id, name);
            default:
                throw new IllegalArgumentException("Unknown user type: " + type);
        }
    }
}