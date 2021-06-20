package de.dev4Agriculture.telemetryConverter.ui;

import com.google.gson.Gson;
import de.dev4Agriculture.telemetryConverter.enumations.OutputFormatEnum;
import de.dev4Agriculture.telemetryConverter.enumations.InputFormatEnum;
import de.dev4Agriculture.telemetryConverter.dto.ConverterSettings;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class UISettings {
    public String inputPath;
    public String outputPath;
    public InputFormatEnum inputFormat;
    public OutputFormatEnum outputFormat;
    public ConverterSettings converterSettings;

    public static Gson gson = new Gson();

    public UISettings(){
        this.outputFormat = OutputFormatEnum.CSV;
        this.outputPath = "";
        this.inputFormat = InputFormatEnum.EFDI;
        this.inputPath = "";
        this.converterSettings = ConverterSettings.getDefault();
    }

    public void toFile(Path path) throws IOException {
        Files.write(path,gson.toJson(this).getBytes(StandardCharsets.UTF_8));
    }

    public static UISettings fromFile(Path path) throws IOException {
        if(!path.toFile().exists()){
            throw new FileNotFoundException();
        }

        byte[] fileData = Files.readAllBytes(path);
        String fileContentJSON = new String(fileData);

        UISettings settings = gson.fromJson(fileContentJSON,UISettings.class);

        if(settings == null){
            settings = new UISettings();
        }

        return settings;
    }
}
