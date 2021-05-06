package de.dev4Agriculture.telemetryConverter.dto;

import com.google.gson.Gson;
import de.dev4Agriculture.telemetryConverter.exceptions.SettingsNotFoundException;

import java.nio.file.Files;
import java.nio.file.Paths;

public class ConverterSettings {
    public String dateFormat;
    public boolean rawData;
    public boolean sortData;
    public boolean cleanData;
    public String floatSplitter;
    public String columnSplitter;



    public static ConverterSettings getDefault(){
        ConverterSettings converterSettings = new ConverterSettings();
        converterSettings.cleanData = false;
        converterSettings.rawData = false;
        converterSettings.columnSplitter = ";";
        converterSettings.floatSplitter = ",";
        converterSettings.dateFormat = "yyyy.MM.dd HH:mm:ss";

        return  converterSettings;
    }


    public static ConverterSettings fromJSON(String json){
        Gson gson = new Gson();
        ConverterSettings converterSettings = gson.fromJson(json,ConverterSettings.class);
        return  converterSettings;
    }

    public static ConverterSettings fromFile(String settingsPath) throws SettingsNotFoundException {
        try {
            byte[] fileContent = Files.readAllBytes(Paths.get(settingsPath));
            String json = new String(fileContent);
            return ConverterSettings.fromJSON(json);
        } catch (Exception e){
            throw new SettingsNotFoundException();
        }
    }
}
