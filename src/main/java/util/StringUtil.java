package main.java.util;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.List;

/**
 * Created by alfonce on 17/07/2017.
 */
public class StringUtil {
    public static StringProperty getStringProperty(String string) {
        return new SimpleStringProperty(string);
    }

    public static String getNumberedList(List<String> strings) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strings.size(); i++) {
            builder.append(i + 1).append(".");
            builder.append(strings.get(i)).append(" ").append(" ");
        }
        return builder.toString();
    }

    public static String getUnnumberedList(List<String> strings) {
        StringBuilder builder = new StringBuilder();
        for (String string : strings) {
            builder.append(string).append("\n");
        }
        return builder.toString();
    }
}
