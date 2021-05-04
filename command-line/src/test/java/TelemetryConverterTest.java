import de.dev4Agriculture.telemetryconverter.exceptions.EFDINotFoundException;
import de.dev4Agriculture.telemetryconverter.exceptions.GPSNotFoundException;
import de.devAgriculture.telemetryConverter.cli.TelemetryConverterCLI;
import org.apache.log4j.BasicConfigurator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class TelemetryConverterTest {

    public static  String rootpath;

    @BeforeAll
    public static void init(){
        BasicConfigurator.configure();
        rootpath = TelemetryConverterTest.class.getResource("resrootfile.txt").getPath().toString().substring(1);

    }

    @Test
    public void canConvertGPSFiles(){
        rootpath = rootpath.replace("resrootfile.txt","");
        assertDoesNotThrow(()->TelemetryConverterCLI.convert(rootpath+"exampleData/gps/gps_input_1.bin",rootpath+"exampleData/gps_output_1.csv",rootpath+"exampleData/settings.json", TelemetryConverterCLI.InputFormat.GPS));
        assertDoesNotThrow(()->TelemetryConverterCLI.convert(rootpath+"exampleData/gps/gps_input_2.bin",rootpath+"exampleData/gps_output_2.csv",rootpath+"exampleData/settings.json", TelemetryConverterCLI.InputFormat.GPS));

        assertThrows(GPSNotFoundException.class,
                () -> {TelemetryConverterCLI.convert(rootpath + "exampleData/gps/notthere.bin",rootpath+"exampleData/gps_output_3.bin",rootpath + "exampleData/settings.json", TelemetryConverterCLI.InputFormat.GPS);},
                "Expected notFoundError"
                );

    }

    @Test
    public void canConvertEFDIFiles(){
        rootpath = rootpath.replace("resrootfile.txt","");
        assertDoesNotThrow(()->TelemetryConverterCLI.convert(rootpath+"exampleData/efdi/unpacked/TLG00001.bin",rootpath+"exampleData/efdi_output_1.csv",rootpath+"exampleData/settings.json", TelemetryConverterCLI.InputFormat.EFDI));

        assertThrows(EFDINotFoundException.class,
                () -> {TelemetryConverterCLI.convert(rootpath + "exampleData/efdi/notthere.bin",rootpath+"exampleData/gps_output_3.bin",rootpath + "exampleData/settings.json", TelemetryConverterCLI.InputFormat.EFDI);},
                "Expected notFoundError"
        );

    }

}
