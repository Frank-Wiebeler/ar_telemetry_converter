package de.dev4Agriculture.telemetryConverter.Exporter;

import agrirouter.technicalmessagetype.Gps;
import de.dev4Agriculture.telemetryConverter.Converter;
import de.dev4Agriculture.telemetryConverter.dto.ConverterSettings;
import de.dev4Agriculture.telemetryConverter.dto.GPSList;
import de.dev4Agriculture.telemetryConverter.dto.GPSListEntry;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class KMLExporter implements DataExporter {
    private static org.apache.log4j.Logger log = Logger.getLogger(Converter.class);
    private static final double ALTITUDE_FACTOR = (1.0 / 1000);
    private ConverterSettings settings;

    private String getHeader(){
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n" +
                "  <Document>\n" +
                "    <name>Paths</name>\n" +
                "    <description>Driving Route exported from gps:info files.</description>\n" +
                "    <Style id=\"yellowLineGreenPoly\">\n" +
                "      <LineStyle>\n" +
                "        <color>7f00ffff</color>\n" +
                "        <width>4</width>\n" +
                "      </LineStyle>\n" +
                "      <PolyStyle>\n" +
                "        <color>7f00ff00</color>\n" +
                "      </PolyStyle>\n" +
                "    </Style>\n" +
                "    <Placemark>\n" +
                "      <name>Absolute Extruded</name>\n" +
                "      <description>Transparent green wall with yellow outlines</description>\n" +
                "      <styleUrl>#yellowLineGreenPoly</styleUrl>\n" +
                "      <LineString>\n" +
                "        <extrude>1</extrude>\n" +
                "        <tessellate>1</tessellate>\n" +
                "        <coordinates> ";
    }

    private String getKMLGPSList(GPSList gpsList){
        StringBuilder fileContent = new StringBuilder();
        for (GPSListEntry entry : gpsList.getList()) {
            if (
                    (entry.status != Gps.GPSList.GPSEntry.PositionStatus.D_ERROR) &&
                            (entry.status != Gps.GPSList.GPSEntry.PositionStatus.D_NO_GPS) &&
                            (entry.status != Gps.GPSList.GPSEntry.PositionStatus.D_NOT_AVAILABLE)
            ) {
                fileContent.append(entry.longitude + "," + entry.latitude + "," + entry.altitude * ALTITUDE_FACTOR + " \n");
            }
        }
        return  fileContent.toString();
    }

    private String getFooter(){

        return  "        </coordinates>\n" +
                "      </LineString>\n" +
                "    </Placemark>\n" +
                "  </Document>\n" +
                "</kml>";
    }

    @Override
    public void setConverterSettings(ConverterSettings converterSettings) {
        this.settings = converterSettings;
    }

    @Override
    public void export(GPSList gpsList, Path filePath) throws IOException {
        String fileContent = getHeader() + getKMLGPSList(gpsList) + getFooter();
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toString()));

        writer.write(fileContent);
        writer.close();
        log.info("Export successfully written");
    }
}
