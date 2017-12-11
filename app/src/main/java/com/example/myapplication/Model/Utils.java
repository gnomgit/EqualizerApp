package com.example.myapplication.Model;

import java.text.DecimalFormat;
import java.util.List;

public class Utils {
    public static Entity result;

    public static <T extends Entity> T byId(List<? extends Entity> content, final long id, Class<T> type) {

        result = null;
        for (Entity e : content) {
            if (type == Person.class) {
                if (((Person)e).id == id) return (T) e;
            }
            if (type == Task.class) {
                if (((Task)e).id == id) return (T) e;
            }
            if (type == Payment.class) {
                if (((Payment)e).id == id) return (T) e;
            }
            if (type == Event.class) {
                if (((Event)e).id == id) return (T) e;
            }
        }
        return null;
    }

    public static String amount2string (double amount) {
        DecimalFormat decim = new DecimalFormat("#.00");
        return decim.format(amount);
    }
}
