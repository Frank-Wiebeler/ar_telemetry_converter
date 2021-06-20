package de.dev4Agriculture.telemetryConverter.ui;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

public class UIOutputStream extends OutputStream {
    private DefaultListModel<String> logList;
    private StringBuilder entryLine;

    public UIOutputStream(DefaultListModel<String> logList) {
        this.logList = logList;
        entryLine = new StringBuilder();
    }

    @Override
    public void write(int b) throws IOException {
        // redirects data to the text area
        if( (char)b != '\n'){
            entryLine.append((char)b);
        } else {
            String nextLine = entryLine.toString();
            entryLine = new StringBuilder();
            SwingUtilities.invokeLater(() -> {
                this.logList.addElement(nextLine + "\n");
            });
        }


    }
}
