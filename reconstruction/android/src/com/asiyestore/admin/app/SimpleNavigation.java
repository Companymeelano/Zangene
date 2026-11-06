package com.asiyestore.admin.app;

/** Stable page identifiers recovered from the isolated demo. */
public final class SimpleNavigation {
    public static final int HOME = 0, ADVICE = 1, CART = 2, CONTACT = 3, ACCOUNT = 4, CUSTOMER_MORE = 5;
    public static final int SUMMARY = 0, PRODUCTS = 1, ORDERS = 2, STOCK = 3, SETTINGS = 4, ADMIN_MORE = 5;
    private SimpleNavigation() { }

    public static boolean customerMoreSelected(int page) {
        return page == CUSTOMER_MORE || page == ADVICE || page == CONTACT;
    }
    public static boolean adminMoreSelected(int page) {
        return page == ADMIN_MORE || page == STOCK || page == SETTINGS;
    }
}
