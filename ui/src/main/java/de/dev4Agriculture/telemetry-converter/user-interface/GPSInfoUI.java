package de.dev4Agriculture.telemetryconverter;

import de.dev4Agriculture.telemetryconverter.ui.GPSInfoConverterUI;

import javax.swing.*;
import java.net.URL;

import static com.sun.javafx.scene.control.skin.Utils.getResource;

public class GPSInfoUI {
  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (UnsupportedLookAndFeelException e) {
      e.printStackTrace();
    }
    URL iconURL = GPSInfoUI.class.getResource("favicon.png");
// iconURL is null when not found
    ImageIcon icon = new ImageIcon(iconURL);
    JFrame jFrame = new JFrame("GPSInfo converter");
    jFrame.setIconImage(icon.getImage());
    JPanel windowPane = new GPSInfoConverterUI().getWindowPane();
    jFrame.setContentPane(windowPane);
    jFrame.pack();
    jFrame.setVisible(true);
    jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
  }
}
