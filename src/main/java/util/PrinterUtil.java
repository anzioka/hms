package main.java.util;

import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.transform.Scale;
import main.Main;

/**
 * Created by alfonce on 31/07/2017.
 */
public class PrinterUtil {
    public static boolean printNode(Node node, Paper paper) {
        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob != null) {
            Printer printer = printerJob.getPrinter();

            if (printer != null) {
                PageLayout layout = printer.createPageLayout(paper, PageOrientation.PORTRAIT, Printer.MarginType
                        .DEFAULT);
                double scaleX = layout.getPrintableWidth() / node.getBoundsInParent().getWidth();
                double scaleY = layout.getPrintableHeight() / node.getBoundsInParent().getHeight();
                node.getTransforms().add(new Scale(scaleX, scaleY));

                printerJob.showPrintDialog(Main.stage);

                //hide buttons
                boolean print = printerJob.printPage(layout, node);
                if (print) {
                    printerJob.endJob();
                    return true;
                }
            }
        }
        return false;

    }
}
