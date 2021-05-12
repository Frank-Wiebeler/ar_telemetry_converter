package de.dev4Agriculture.telemetryConverter.Importer;

import de.dev4Agriculture.telemetryConverter.dto.GPSList;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface DataImporter {

    GPSList loadAndConvertToGPSList(Path importFilePath) throws IOException;
    GPSList loadAndConvertToGPSList(InputStream inputStream) throws IOException;
}
