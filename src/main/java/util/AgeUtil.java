package main.java.util;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.*;

/**
 * Created by alfonce on 13/07/2017.
 */
public class AgeUtil {
    public static String getAge(LocalDate localDate) {
        int numYears = (int) YEARS.between(localDate, LocalDate.now());
        if (numYears > 0) {
            return numYears + " year(s)";
        } else {
            int numMonths = (int) MONTHS.between(localDate, LocalDate.now());
            if (numMonths > 0) {
                return numMonths + " month(s)";
            } else {
                int numWeeks = (int) WEEKS.between(localDate, LocalDate.now());
                if (numWeeks > 0) {
                    return numWeeks + " week(s)";
                } else {
                    return (int) DAYS.between(localDate, LocalDate.now()) + " day(s)";
                }
            }

        }
    }

    public static int getYears(LocalDate localDate) {
        return (int) YEARS.between(localDate, LocalDate.now());
    }

    public static StringProperty getAgeStringProperty(LocalDate localDate) {
        return new SimpleStringProperty(getAge(localDate));
    }
}
