package main.java.util;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Created by alfonce on 08/05/2017.
 */
public class CurrencyUtil {
    public static String formatCurrency(double amount) {
        return NumberFormat.getInstance().format(amount); //
    }

    public static StringProperty getStringProperty(double amount) {
        return new SimpleStringProperty(formatCurrency(amount));
    }

    public static double parseCurrency(String value) {
        if (value == null || value.isEmpty()) {
            return -1;
        } else {
            try {
                return NumberFormat.getInstance().parse(value).doubleValue();
            } catch (ParseException e) {
                return 0;
            }
        }
    }

    public static double calculateSellingPriceFromMarkUp(double percentMarkUp, double buyingPrice) {
        return NumberUtil.getNearestWholeNumber((1 + percentMarkUp / 100) * buyingPrice);
    }
}
