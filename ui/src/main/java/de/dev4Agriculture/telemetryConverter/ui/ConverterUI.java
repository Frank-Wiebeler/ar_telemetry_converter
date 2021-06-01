package de.dev4Agriculture.telemetryConverter.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import de.dev4Agriculture.telemetryConverter.Converter;
import de.dev4Agriculture.telemetryConverter.ConverterCallback;
import de.dev4Agriculture.telemetryConverter.Exporter.CSVExporter;
import de.dev4Agriculture.telemetryConverter.Exporter.DataExporter;
import de.dev4Agriculture.telemetryConverter.Exporter.KMLExporter;
import de.dev4Agriculture.telemetryConverter.Importer.DataImporter;
import de.dev4Agriculture.telemetryConverter.Importer.EFDIImporter;
import de.dev4Agriculture.telemetryConverter.Importer.GPSInfoImporter;
import de.dev4Agriculture.telemetryConverter.enumations.OutputFormatEnum;
import de.dev4Agriculture.telemetryConverter.enumations.InputFormatEnum;
import de.dev4Agriculture.telemetryConverter.exceptions.CSVLockedException;
import de.dev4Agriculture.telemetryConverter.exceptions.EFDINotFoundException;
import de.dev4Agriculture.telemetryConverter.exceptions.GPSNotFoundException;
import de.dev4Agriculture.telemetryConverter.exceptions.ZipNotLoadedException;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class ConverterUI extends ConverterCallback {
    private JButton inputPathSelectionButton;
    private JTextField inputPathTextField;
    private JButton outputPathSelectionButton;
    private JTextField outputPathTextField;
    private JComboBox inputFormatCombobox;
    private JComboBox outputFormatCombobox;
    private JButton convertButton;
    private JButton aboutButton;
    private JTextField floatSplitterTextField;
    private JTextField columnSplitterTextField;
    private JTextField dateFormatTextField;
    private JCheckBox sortByDateCheckBox;
    private JCheckBox removePointsWithoutPositionCheckBox;
    private JPanel mainContainer;
    private JList logArea;
    private JCheckBox exportRawDataCheckBox;
    private DefaultListModel<String> logAreaModel;

    private static UISettings uiSettings;
    private static final String UI_SETTINGS_PATH = "./uiSettings.json";

    private BidiMap<InputFormatEnum, String> inputComboboxEntries;

    private BidiMap<OutputFormatEnum, String> outputComboboxEntries;
    Border border = BorderFactory.createLineBorder(Color.RED);


    public ConverterUI() {

    }

    @Override
    public void printLn(String entry) {
        super.printLn(entry);
        SwingUtilities.invokeLater(() -> {
            logAreaModel.addElement(entry + "\n");
        });
    }

    public void saveJSONConfig() {
        try {
            uiSettings.toFile(Paths.get(UI_SETTINGS_PATH));
        } catch (IOException ioException) {
            printLn("Could not store updated Settings");
            ioException.printStackTrace();
        }
    }

    public int indexOfValueInHashMap(Map<?, String> map, Enum key) {
        int index = 0;
        for (String keyEntry : map.values()) {
            if (keyEntry.equals(key)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public void init() {
        outputFormatCombobox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!outputFormatCombobox.getSelectedItem().toString().equals("CSV")) {
                    floatSplitterTextField.setEnabled(false);
                    columnSplitterTextField.setEnabled(false);
                    dateFormatTextField.setEnabled(false);
                } else {
                    floatSplitterTextField.setEnabled(true);
                    columnSplitterTextField.setEnabled(true);
                    dateFormatTextField.setEnabled(true);
                }

            }
        });
        floatSplitterTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyTyped(e);
                if (floatSplitterTextField.getText().length() > 1 || floatSplitterTextField.getText().length() == 0) {
                    floatSplitterTextField.setBorder(border);
                } else {
                    floatSplitterTextField.setBorder(new JTextField().getBorder());
                }
            }
        });
        columnSplitterTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyTyped(e);
                if (columnSplitterTextField.getText().length() > 1 || columnSplitterTextField.getText().length() == 0) {
                    columnSplitterTextField.setBorder(border);
                } else {
                    columnSplitterTextField.setBorder(null);
                }
            }
        });
        logAreaModel = new DefaultListModel<>();
        logArea.setModel(logAreaModel);
        try {
            uiSettings = UISettings.fromFile(Paths.get(UI_SETTINGS_PATH));
        } catch (IOException ioException) {
            uiSettings = new UISettings();
            printLn("Could not load Settings");
        }

        inputComboboxEntries = new DualHashBidiMap();
        inputComboboxEntries.put(InputFormatEnum.GPS, "GPS:Info.bin");
        inputComboboxEntries.put(InputFormatEnum.EFDI, "EFDI.bin");
        inputComboboxEntries.put(InputFormatEnum.EFDI_ZIP, "EFDI.zip");

        for (String entry : inputComboboxEntries.values()) {
            inputFormatCombobox.addItem(entry);
        }

        if (uiSettings.inputFormat != InputFormatEnum.UNKNOWN) {
            inputFormatCombobox.setSelectedItem(indexOfValueInHashMap(inputComboboxEntries, uiSettings.inputFormat));
        }

        inputFormatCombobox.addItemListener(this::inputFormatChanged);

        inputPathTextField.setText(uiSettings.inputPath);

        inputPathSelectionButton.addActionListener(e -> {
            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.setDialogTitle("Select Input Data Folder");
            jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jFileChooser.setCurrentDirectory(new File(uiSettings.inputPath));
            jFileChooser.setSelectedFile(new File(uiSettings.inputPath));
            jFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("GPS or EFDI", "bin"));
            jFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("EFDI_ZIP", "zip"));
            if (jFileChooser.showDialog(mainContainer.getComponent(1), "Open") == JFileChooser.APPROVE_OPTION) {
                File file = jFileChooser.getSelectedFile();
                inputPathTextField.setText(file.getAbsolutePath());
                uiSettings.inputPath = file.getAbsolutePath();
                saveJSONConfig();
            }
        });


        outputComboboxEntries = new DualHashBidiMap<>();
        outputComboboxEntries.put(OutputFormatEnum.CSV, "CSV");
        outputComboboxEntries.put(OutputFormatEnum.KML, "KML");

        for (String entry : outputComboboxEntries.values()) {
            outputFormatCombobox.addItem(entry);
        }
        if (uiSettings.outputFormat != OutputFormatEnum.UNKNOWN) {
            outputFormatCombobox.setSelectedItem(indexOfValueInHashMap(outputComboboxEntries, uiSettings.outputFormat));
        }

        outputFormatCombobox.addItemListener(this::outputFormatChanged);

        outputPathTextField.setText(uiSettings.outputPath);

        outputPathSelectionButton.addActionListener(e -> {
            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.setDialogTitle("Select Output Data Folder");
            jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jFileChooser.setCurrentDirectory(new File(uiSettings.outputPath));
            jFileChooser.setSelectedFile(new File(uiSettings.outputPath));
            if (jFileChooser.showDialog(mainContainer.getComponent(1), "Save") == JFileChooser.APPROVE_OPTION) {
                File file = jFileChooser.getSelectedFile();
                outputPathTextField.setText(file.getAbsolutePath());
                uiSettings.outputPath = file.getAbsolutePath();
                saveJSONConfig();
            }
        });

        columnSplitterTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {

            }

            @Override
            public void focusLost(FocusEvent e) {
                uiSettings.converterSettings.columnSplitter = columnSplitterTextField.getText();
                saveJSONConfig();
            }
        });


        floatSplitterTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {

            }

            @Override
            public void focusLost(FocusEvent e) {
                uiSettings.converterSettings.floatSplitter = floatSplitterTextField.getText();
                saveJSONConfig();
            }
        });


        dateFormatTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {

            }

            @Override
            public void focusLost(FocusEvent e) {
                uiSettings.converterSettings.dateFormat = dateFormatTextField.getText();
                saveJSONConfig();
            }
        });


        sortByDateCheckBox.addActionListener(e -> {
            if (sortByDateCheckBox.isSelected()) {
                uiSettings.converterSettings.sortData = true;
            } else {
                uiSettings.converterSettings.sortData = false;
            }
            saveJSONConfig();
        });


        removePointsWithoutPositionCheckBox.addActionListener(e -> {
            if (removePointsWithoutPositionCheckBox.isSelected()) {
                uiSettings.converterSettings.cleanData = true;
            } else {
                uiSettings.converterSettings.cleanData = false;
            }
            saveJSONConfig();
        });

        exportRawDataCheckBox.addActionListener(e -> {
            if (exportRawDataCheckBox.isSelected()) {
                uiSettings.converterSettings.rawData = true;
            } else {
                uiSettings.converterSettings.rawData = false;
            }
            saveJSONConfig();
        });


        exportRawDataCheckBox.setSelected(uiSettings.converterSettings.rawData);
        sortByDateCheckBox.setSelected(uiSettings.converterSettings.sortData);
        removePointsWithoutPositionCheckBox.setSelected(uiSettings.converterSettings.cleanData);
        dateFormatTextField.setText(uiSettings.converterSettings.dateFormat);
        columnSplitterTextField.setText(uiSettings.converterSettings.columnSplitter);
        floatSplitterTextField.setText(uiSettings.converterSettings.floatSplitter);

        convertButton.addActionListener(e -> {
            convertDataSet();
        });

        aboutButton.addActionListener(e ->
                JOptionPane.showMessageDialog(null,
                        "Agrirouter Telemetry Converter by Frank Wiebeler, dev4Agriculture \n" +
                                "Copyright 2021 \n" +
                                "\n" +
                                "Version of Converter: " + Converter.getVersion() + "\n" +
                                "\n" +
                                "Visit https://www.dev4Agriculture.de \n" +
                                "\n" +
                                "For License see delivered License.txt"));
    }


    public void inputFormatChanged(ItemEvent e) {
        String content = e.getItem().toString();
        for (Map.Entry<InputFormatEnum, String> entry : inputComboboxEntries.entrySet()) {
            if (entry.getValue().equals(content)) {
                uiSettings.inputFormat = entry.getKey();
                saveJSONConfig();
            }
        }
    }


    public void outputFormatChanged(ItemEvent e) {
        String content = e.getItem().toString();
        for (Map.Entry<OutputFormatEnum, String> entry : outputComboboxEntries.entrySet()) {
            if (entry.getValue().equals(content)) {
                uiSettings.outputFormat = entry.getKey();
                saveJSONConfig();
            }
        }
    }

    public void convertDataSet() {
        DataImporter dataImporter;
        boolean isZip;
        if (uiSettings.inputFormat.equals(InputFormatEnum.EFDI)) {
            dataImporter = new EFDIImporter();
            isZip = false;
        } else if (uiSettings.inputFormat.equals(InputFormatEnum.EFDI_ZIP)) {
            dataImporter = new EFDIImporter();
            isZip = true;
        } else if (uiSettings.inputFormat.equals(InputFormatEnum.GPS)) {
            dataImporter = new GPSInfoImporter();
            isZip = false;
        } else {
            printLn("No Import Format selected");
            return;
        }

        DataExporter dataExporter;

        if (uiSettings.outputFormat.equals(OutputFormatEnum.CSV)) {
            dataExporter = new CSVExporter();
        } else {
            dataExporter = new KMLExporter();
        }

        Converter.setSettings(uiSettings.converterSettings);
        if (isZip) {
            try {
                Converter.convertEFDIZip(Paths.get(uiSettings.inputPath), Paths.get(uiSettings.outputPath), dataImporter, dataExporter);
            } catch (CSVLockedException csvLockedException) {
                printLn("Could not access CSV; is it opened?");
            } catch (EFDINotFoundException efdiNotFoundException) {
                printLn("EFDI file not found or broken");
            } catch (ZipNotLoadedException zipNotLoadedException) {
                printLn("Zip could not be loaded, is it defect?");
            }
        } else {
            try {
                Converter.convert(Paths.get(uiSettings.inputPath), Paths.get(uiSettings.outputPath), dataImporter, dataExporter);
            } catch (GPSNotFoundException gpsNotFoundException) {
                printLn("GPSList File could not be found");
            } catch (CSVLockedException csvLockedException) {
                printLn("Could not access CSV; is it opened?");
            }
        }
    }

    public void splitterChanged() {

    }


    public static void main(String[] args) {
        Handler handler;
        try {
            handler = new FileHandler("test.log", 10000000, 1);
            Logger.getLogger("").addHandler(handler);//TODO Redirect this to the Output field logArea
        } catch (IOException e) {
            e.printStackTrace();
        }
        JFrame jframe = new JFrame("Telemetry Data Converter by dev4Agriculture Copyright 2021");

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
        try {
            Image icon = ImageIO.read(ConverterUI.class.getResource("favicon.png"));
            jframe.setIconImage(icon);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        ConverterUI converterUI = new ConverterUI();
        jframe.setContentPane(converterUI.mainContainer);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setVisible(true);
        jframe.setResizable(false);
        jframe.setSize(1280, 720);
        jframe.pack();

        converterUI.init();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainContainer = new JPanel();
        mainContainer.setLayout(new GridLayoutManager(9, 10, new Insets(0, 0, 0, 0), -1, -1));
        mainContainer.setMaximumSize(new Dimension(1280, 720));
        mainContainer.setMinimumSize(new Dimension(1280, 720));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 9, new Insets(10, 20, 10, 10), -1, -1));
        panel1.setBackground(new Color(-1));
        mainContainer.add(panel1, new GridConstraints(0, 0, 2, 10, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-16777216)), "INPUT", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$("Helvetia", Font.BOLD, 15, panel1.getFont()), new Color(-16777216)));
        final JLabel label1 = new JLabel();
        label1.setText("Path");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        inputPathTextField = new JTextField();
        panel1.add(inputPathTextField, new GridConstraints(0, 1, 1, 7, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        inputPathSelectionButton = new JButton();
        inputPathSelectionButton.setText("..");
        panel1.add(inputPathSelectionButton, new GridConstraints(0, 8, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Format");
        panel1.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(38, 33), null, 0, false));
        inputFormatCombobox = new JComboBox();
        panel1.add(inputFormatCombobox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, 20), new Dimension(100, 20), new Dimension(100, 20), 1, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 9, new Insets(10, 20, 10, 10), -1, -1));
        panel2.setBackground(new Color(-1));
        mainContainer.add(panel2, new GridConstraints(3, 0, 2, 10, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-16777216)), "OUTPUT", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$("Helvetia", Font.BOLD, 15, panel2.getFont()), null));
        outputPathTextField = new JTextField();
        panel2.add(outputPathTextField, new GridConstraints(0, 1, 1, 7, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Path");
        panel2.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Format");
        panel2.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        outputFormatCombobox = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        outputFormatCombobox.setModel(defaultComboBoxModel1);
        panel2.add(outputFormatCombobox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, 20), new Dimension(100, 20), new Dimension(100, 20), 1, false));
        outputPathSelectionButton = new JButton();
        outputPathSelectionButton.setText("...");
        panel2.add(outputPathSelectionButton, new GridConstraints(0, 8, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("FloatSplitter");
        panel2.add(label5, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Date Format");
        panel2.add(label6, new GridConstraints(1, 6, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dateFormatTextField = new JTextField();
        panel2.add(dateFormatTextField, new GridConstraints(1, 7, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        floatSplitterTextField = new JTextField();
        panel2.add(floatSplitterTextField, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(80, 20), new Dimension(81, 20), new Dimension(80, 20), 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Column Splitter");
        panel2.add(label7, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(87, 16), null, 0, false));
        columnSplitterTextField = new JTextField();
        panel2.add(columnSplitterTextField, new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(80, 20), new Dimension(80, 20), new Dimension(80, 20), 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 7, new Insets(10, 20, 10, 10), -1, -1));
        panel3.setBackground(new Color(-1));
        mainContainer.add(panel3, new GridConstraints(6, 0, 1, 10, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-16777216)), "SETTING", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$("Helvetia", Font.BOLD, 15, panel3.getFont()), null));
        final JLabel label8 = new JLabel();
        label8.setBackground(new Color(-1));
        label8.setText("Processing");
        panel3.add(label8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sortByDateCheckBox = new JCheckBox();
        sortByDateCheckBox.setBackground(new Color(-1));
        sortByDateCheckBox.setText("Sort by Date");
        panel3.add(sortByDateCheckBox, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        removePointsWithoutPositionCheckBox = new JCheckBox();
        removePointsWithoutPositionCheckBox.setBackground(new Color(-1));
        removePointsWithoutPositionCheckBox.setText("Remove Points without position");
        panel3.add(removePointsWithoutPositionCheckBox, new GridConstraints(0, 4, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(135, 21), null, 0, false));
        exportRawDataCheckBox = new JCheckBox();
        exportRawDataCheckBox.setBackground(new Color(-1));
        exportRawDataCheckBox.setText("Export Raw Data");
        panel3.add(exportRawDataCheckBox, new GridConstraints(0, 6, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(10, 20, 10, 10), -1, -1));
        panel4.setBackground(new Color(-1));
        mainContainer.add(panel4, new GridConstraints(8, 0, 1, 10, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-16777216)), "LOG", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, this.$$$getFont$$$("Helvetia", Font.BOLD, 15, panel4.getFont()), null));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel4.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        logArea = new JList();
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        logArea.setModel(defaultListModel1);
        scrollPane1.setViewportView(logArea);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 8, new Insets(10, 20, 10, 10), -1, -1));
        panel5.setBackground(new Color(-1));
        mainContainer.add(panel5, new GridConstraints(7, 0, 1, 10, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        convertButton = new JButton();
        Font convertButtonFont = this.$$$getFont$$$("Helvetia", Font.BOLD, 18, convertButton.getFont());
        if (convertButtonFont != null) convertButton.setFont(convertButtonFont);
        convertButton.setHorizontalAlignment(0);
        convertButton.setHorizontalTextPosition(0);
        convertButton.setText("Convert");
        convertButton.setVerticalAlignment(0);
        convertButton.setVerticalTextPosition(0);
        panel5.add(convertButton, new GridConstraints(0, 0, 1, 7, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        aboutButton = new JButton();
        Font aboutButtonFont = this.$$$getFont$$$("Helvetia", Font.BOLD, 18, aboutButton.getFont());
        if (aboutButtonFont != null) aboutButton.setFont(aboutButtonFont);
        aboutButton.setText("About");
        panel5.add(aboutButton, new GridConstraints(0, 7, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainContainer;
    }
}
