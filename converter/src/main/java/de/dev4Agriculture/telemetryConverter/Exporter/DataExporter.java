package de.dev4Agriculture.telemetryConverter.Exporter;

import de.dev4Agriculture.telemetryConverter.dto.ConverterSettings;
import de.dev4Agriculture.telemetryConverter.dto.GPSList;

import java.io.IOException;
import java.nio.file.Path;

public interface DataExporter {
    void setConverterSettings(ConverterSettings converterSettings);

    void export(GPSList gpsList, Path filePath) throws IOException;

}
