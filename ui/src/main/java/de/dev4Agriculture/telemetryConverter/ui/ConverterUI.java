package de.dev4Agriculture.telemetryConverter.ui;

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
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
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
    public void printLn(String entry){
        super.printLn(entry);
        SwingUtilities.invokeLater(() -> {
            logAreaModel.addElement(entry + "\n");
        });
    }

    public void  saveJSONConfig(){
        try {
            uiSettings.toFile(Paths.get(UI_SETTINGS_PATH));
        } catch (IOException ioException) {
            printLn("Could not store updated Settings");
            ioException.printStackTrace();
        }
    }

    public int indexOfValueInHashMap(Map<?,String> map, Enum key){
        int index = 0;
        for(String keyEntry: map.values()){
            if(keyEntry.equals(key)){
                return index;
            }
            index++;
        }
        return -1;
    }

    public void init(){
        outputFormatCombobox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if(!outputFormatCombobox.getSelectedItem().toString().equals("CSV")){
                    floatSplitterTextField.setEnabled(false);
                    columnSplitterTextField.setEnabled(false);
                    dateFormatTextField.setEnabled(false);
                }else{
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
                if(floatSplitterTextField.getText().length()>1 || floatSplitterTextField.getText().length()==0){
                    floatSplitterTextField.setBorder(border);
                }else{
                    floatSplitterTextField.setBorder(new JTextField().getBorder());
                }
            }
        });
        columnSplitterTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyTyped(e);
                if(columnSplitterTextField.getText().length()>1 || columnSplitterTextField.getText().length()==0){
                    columnSplitterTextField.setBorder(border);
                }else{
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
        inputComboboxEntries.put(InputFormatEnum.GPS,"GPS:Info.bin");
        inputComboboxEntries.put(InputFormatEnum.EFDI, "EFDI.bin");
        inputComboboxEntries.put(InputFormatEnum.EFDI_ZIP,"EFDI.zip");

        for(String entry: inputComboboxEntries.values()) {
            inputFormatCombobox.addItem(entry);
        }

        if(uiSettings.inputFormat != InputFormatEnum.UNKNOWN){
            inputFormatCombobox.setSelectedItem(indexOfValueInHashMap(inputComboboxEntries,uiSettings.inputFormat));
        }

        inputFormatCombobox.addItemListener(this::inputFormatChanged);

        inputPathTextField.setText(uiSettings.inputPath);

        inputPathSelectionButton.addActionListener(e -> {
            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.setDialogTitle("Select Input Data Folder");
            jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jFileChooser.setCurrentDirectory(new File(uiSettings.inputPath));
            jFileChooser.setSelectedFile(new File(uiSettings.inputPath));
            jFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("GPS or EFDI","bin"));
            jFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("EFDI_ZIP","zip"));
            if (jFileChooser.showDialog(mainContainer.getComponent(1), "Open") == JFileChooser.APPROVE_OPTION) {
                File file = jFileChooser.getSelectedFile();
                inputPathTextField.setText(file.getAbsolutePath());
                uiSettings.inputPath = file.getAbsolutePath();
                saveJSONConfig();
            }
        });


        outputComboboxEntries = new DualHashBidiMap<>();
        outputComboboxEntries.put(OutputFormatEnum.CSV, "CSV");
        outputComboboxEntries.put(OutputFormatEnum.KML, "KML" );

        for(String entry: outputComboboxEntries.values()) {
            outputFormatCombobox.addItem(entry);
        }
        if(uiSettings.outputFormat != OutputFormatEnum.UNKNOWN){
            outputFormatCombobox.setSelectedItem(indexOfValueInHashMap(outputComboboxEntries,uiSettings.outputFormat));
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
            if(sortByDateCheckBox.isSelected()){
                uiSettings.converterSettings.sortData = true;
            } else {
                uiSettings.converterSettings.sortData = false;
            }
            saveJSONConfig();
        });


        removePointsWithoutPositionCheckBox.addActionListener(e -> {
            if(removePointsWithoutPositionCheckBox.isSelected()){
                uiSettings.converterSettings.cleanData = true;
            } else {
                uiSettings.converterSettings.cleanData = false;
            }
            saveJSONConfig();
        });

        exportRawDataCheckBox.addActionListener(e -> {
            if(exportRawDataCheckBox.isSelected()){
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


    public void inputFormatChanged(ItemEvent e){
        String content = e.getItem().toString();
        for(Map.Entry<InputFormatEnum, String> entry: inputComboboxEntries.entrySet()){
            if(entry.getValue().equals(content)) {
                uiSettings.inputFormat = entry.getKey();
                saveJSONConfig();
            }
        }
    }


    public void outputFormatChanged(ItemEvent e){
        String content = e.getItem().toString();
        for(Map.Entry<OutputFormatEnum, String> entry: outputComboboxEntries.entrySet()){
            if(entry.getValue().equals(content)) {
                uiSettings.outputFormat = entry.getKey();
                saveJSONConfig();
            }
        }
    }

    public void convertDataSet(){
        DataImporter dataImporter;
        boolean isZip;
        if(uiSettings.inputFormat.equals(InputFormatEnum.EFDI)){
            dataImporter = new EFDIImporter();
            isZip = false;
        } else if(uiSettings.inputFormat.equals(InputFormatEnum.EFDI_ZIP)){
            dataImporter = new EFDIImporter();
            isZip = true;
        } else if(uiSettings.inputFormat.equals(InputFormatEnum.GPS)){
            dataImporter = new GPSInfoImporter();
            isZip = false;
        } else {
            printLn("No Import Format selected");
            return;
        }

        DataExporter dataExporter;

        if(uiSettings.outputFormat.equals(OutputFormatEnum.CSV)){
            dataExporter = new CSVExporter();
        } else {
            dataExporter = new KMLExporter();
        }

        Converter.setSettings(uiSettings.converterSettings);
        if(isZip){
            try {
                Converter.convertEFDIZip( Paths.get(uiSettings.inputPath), Paths.get(uiSettings.outputPath), dataImporter, dataExporter);
            } catch (CSVLockedException csvLockedException) {
                printLn("Could not access CSV; is it opened?");
            } catch (EFDINotFoundException efdiNotFoundException) {
                printLn("EFDI file not found or broken");
            } catch (ZipNotLoadedException zipNotLoadedException) {
                printLn("Zip could not be loaded, is it defect?");
            }
        } else {
            try {
                Converter.convert( Paths.get(uiSettings.inputPath), Paths.get(uiSettings.outputPath), dataImporter, dataExporter);
            } catch (GPSNotFoundException gpsNotFoundException) {
                printLn("GPSList File could not be found");
            } catch (CSVLockedException csvLockedException) {
                printLn("Could not access CSV; is it opened?");
            }
        }
    }

    public void splitterChanged(){

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
        jframe.setSize(1280,720);
        jframe.pack();

        converterUI.init();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
