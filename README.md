# ar_telemetry_converter
This tool converts [gps:info](https://github.com/DKE-Data/agrirouter-interface-documentation/blob/develop/docs/tmt/gps.adoc) [efdi:timelog](https://github.com/DKE-Data/agrirouter-interface-documentation/blob/develop/docs/tmt/efdi.adoc#iso11783-10time_logprotobuf---efdi-timelog) packages as received via [agrirouter](https://my-agrirouter.com/en/); e.g. via [IO-Tool](https://io.my-agrirouter.com) to CSV files

# Who's behind it

![dev4Agriculture](assets/dev4Agriculture.png)

Development by [dev4Agriculture](https://www.dev4Agriculture.de)


# Standalone app

You can create a standalone jar file from the code using this command

```bash
    mvn clean package
```


## CommandLine

The tool can be called with different parameters:

````
java -jar arTelemetryConverter.jar -i ./gps.bin -o ./gps.csv -s ./settings.json
````


Description of Parameters:
* -i: Is followed by the source file
* -o: Is followed by the destination file
* -s: Is followed by the settings file 


### Settings.json

The Settings.json includes all parameters required to configure the conversion:


* dateFormat : The Format of the TimeStamp and Date
* rawGPSPositions": If true, GPS Values including Altitude are not converted but given in the raw format as they are in the protobuf
* sortData": If true, the datasets are sorted by their Timestamp
* cleanData": If true, all thos datasets, that include invalid or non-existent GPS data is deleted
* floatSplitter": Includes the splitter for Float numbers, e.g. "," for "5,3" or "." for "5.3"
* columnSplitter": Includes the splitter between the columns of the CSV. By default, that's ";"

If no settings are provided, a default setting is used
