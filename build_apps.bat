mvn clean install
mkdir apps
cp cli/target/cli-1.0-SNAPSHOT.jar ./apps/ar_telemetry_converter.jar
cp ./scripts/ar_telemetry_converter.bat ./apps/ar_telemetry_converter.bat
cp ./scripts/ar_telemetry_converter.sh ./apps/ar_telemetry_converter.sh