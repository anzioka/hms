package main.java.util;

/**
 * Created by alfonce on 17/07/2017.
 */
public enum ColorCode {

    YELLOW("#FFFF00"), RED("#FF0000"), GREEN("#008000"), WHITE("#f5f5f5");

    private final String value;

    ColorCode(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
