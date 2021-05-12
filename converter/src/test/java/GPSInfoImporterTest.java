import de.dev4Agriculture.telemetryConverter.Converter;
import de.dev4Agriculture.telemetryConverter.dto.ConverterSettings;
import de.dev4Agriculture.telemetryConverter.exceptions.CSVLockedException;
import de.dev4Agriculture.telemetryConverter.exceptions.GPSNotFoundException;
import de.dev4Agriculture.telemetryConverter.exceptions.SettingsNotFoundException;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GPSInfoImporterTest {

    private String resourcePathFromURI(String resourceName) throws URISyntaxException {
        URI uri = ClassLoader.getSystemResource(resourceName).toURI();
        String mainPath = Paths.get(uri).toString();
        Path path = Paths.get(mainPath);
        return path.toString();
    }


    @Test
    public void ConvertGPS2CSVTest() throws GPSNotFoundException, CSVLockedException, URISyntaxException {
        Path input = Paths.get(resourcePathFromURI("inputGPS.bin"));
        Path output= Paths.get("export_gps.csv");
        Converter.setSettings(ConverterSettings.getDefault());
        Converter.convertGPS2CSV(input,output);
    }

    @Test
    public void ConvertGPS2KMLTest() throws GPSNotFoundException, CSVLockedException, URISyntaxException {
        Path input = Paths.get(resourcePathFromURI("inputGPS.bin"));
        Path output= Paths.get("export_gps.kml");
        Converter.setSettings(ConverterSettings.getDefault());
        Converter.convertGPS2KML(input,output);
    }

    @Test
    public void ConvertEFDI2CSVTest() throws GPSNotFoundException, CSVLockedException, URISyntaxException {
        Path input = Paths.get(resourcePathFromURI("inputEFDI.bin"));
        Path output= Paths.get("export_efdi.csv");
        Converter.setSettings(ConverterSettings.getDefault());
        Converter.convertEFDI2CSV(input,output);
    }

    @Test
    public void ConvertEFDI2KMLTest() throws GPSNotFoundException, CSVLockedException, URISyntaxException {
        Path input = Paths.get(resourcePathFromURI("inputEFDI.bin"));
        Path output= Paths.get("export_efdi.kml");
        Converter.setSettings(ConverterSettings.getDefault());
        Converter.convertEFDI2KML(input,output);
    }

    @Test
    public void loadSettingsWorks() throws URISyntaxException, SettingsNotFoundException {
        Path input = Paths.get(resourcePathFromURI("settings.json"));
        ConverterSettings settings = ConverterSettings.fromFile(input.toAbsolutePath().toString());
        assert(!settings.rawData);
        assert( settings.dateFormat.equals("yyyy.MM.dd HH:mm:ss"));
    }
}
