package main.java.util;

import javafx.stage.FileChooser;
import main.Main;

import java.io.File;

/**
 * Created by alfonce on 30/07/2017.
 */
public class FileUtil {
    public static File getSelectedFile(FileChooser.ExtensionFilter filter) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(filter);
        return chooser.showOpenDialog(Main.stage);
    }
}
