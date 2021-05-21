import de.dev4Agriculture.telemetryConverter.enumations.OutputFormatEnum;
import de.dev4Agriculture.telemetryConverter.enumations.InputFormatEnum;
import de.dev4Agriculture.telemetryConverter.exceptions.EFDINotFoundException;
import de.dev4Agriculture.telemetryConverter.exceptions.GPSNotFoundException;
import de.dev4Agriculture.telemetryConverter.cli.TelemetryConverterCLI;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.lf5.util.Resource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class TelemetryConverterTest {

    public static  String rootpath;

    @BeforeAll
    public static void init(){
        BasicConfigurator.configure();
        URL resourceURL =TelemetryConverterTest.class.getResource("resrootfile.txt");
        if(resourceURL != null) {
            rootpath = resourceURL.getPath().substring(1);
        } else {
            rootpath ="";
        }

    }

    @Test
    public void canConvertGPSFiles(){
        rootpath = rootpath.replace("resrootfile.txt","");
        assertDoesNotThrow(()->TelemetryConverterCLI.convert(rootpath+"exampleData/gps/gps.bin",rootpath+"exampleData/gps_output_1.csv",rootpath+"exampleData/settings.json", InputFormatEnum.GPS, OutputFormatEnum.CSV));

        assertThrows(GPSNotFoundException.class,
                () -> TelemetryConverterCLI.convert(rootpath + "exampleData/gps/notthere.bin",rootpath+"exampleData/gps_output_3.bin",rootpath + "exampleData/settings.json", InputFormatEnum.GPS, OutputFormatEnum.CSV),
                "Expected notFoundError"
                );

    }

    @Test
    public void canConvertEFDIFiles(){
        rootpath = rootpath.replace("resrootfile.txt","");
        assertDoesNotThrow(()->TelemetryConverterCLI.convert(rootpath+"exampleData/efdi.bin",rootpath+"exampleData/efdi_output_1.csv",rootpath+"exampleData/settings.json", InputFormatEnum.EFDI, OutputFormatEnum.CSV));

        assertThrows(EFDINotFoundException.class,
                () -> TelemetryConverterCLI.convert(rootpath + "exampleData/efdi/notthere.bin",rootpath+"exampleData/efdi_output_2.csv",rootpath + "exampleData/settings.json", InputFormatEnum.EFDI, OutputFormatEnum.CSV),
                "Expected notFoundError"
        );

    }

}
