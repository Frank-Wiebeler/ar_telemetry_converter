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

public class GPSInfoConverterTest {

    private String resourcePathFromURI(String resourceName) throws URISyntaxException {
        URI uri = ClassLoader.getSystemResource(resourceName).toURI();
        String mainPath = Paths.get(uri).toString();
        Path path = Paths.get(mainPath);
        return path.toString();
    }


    @Test
    public void ConvertExampleDataAsRawDataTest() throws GPSNotFoundException, CSVLockedException, URISyntaxException {
        Path input = Paths.get(resourcePathFromURI("inputGPS.bin"));
        Path output= Paths.get("export.csv");
        Converter.setSettings(ConverterSettings.getDefault());
        Converter.convertGPSDataFile(input,output);
    }

    @Test
    public void loadSettingsWorks() throws URISyntaxException, SettingsNotFoundException {
        Path input = Paths.get(resourcePathFromURI("settings.json"));
        ConverterSettings settings = ConverterSettings.fromFile(input.toAbsolutePath().toString());
        assert(!settings.rawData);
        assert( settings.dateFormat.equals("yyyy.MM.dd HH:mm:ss"));
    }
}
