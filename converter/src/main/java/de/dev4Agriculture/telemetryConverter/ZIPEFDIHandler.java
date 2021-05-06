package de.dev4Agriculture.telemetryConverter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZIPEFDIHandler {

    private class ZipEFDIEntry{
        public String name;
        public InputStream inputStream;
    }
    private ArrayList<ZipEFDIEntry> zipEFDIEntries;
    private int entry;
    private ZipFile zipFile;

    public ZIPEFDIHandler(String zipPath) throws IOException {
        zipEFDIEntries = new ArrayList<>();
        entry = -1;
        ZipFile zipFile = new ZipFile(zipPath);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while(entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            if(zipEntry.getName().toUpperCase(Locale.ROOT).startsWith("TLG")) {
                ZipEFDIEntry zipEFDIEntry = new ZipEFDIEntry();
                zipEFDIEntry.name = zipEntry.getName();
                zipEFDIEntry.inputStream = zipFile.getInputStream(zipEntry);
                zipEFDIEntries.add(zipEFDIEntry);
            }
        }

    }

    public int count() {
        return zipEFDIEntries.size();
    }

    public boolean nextZipEFDIEntry(){
        entry++;
        if(entry< zipEFDIEntries.size()){
            return true;
        } else {
            return false;
        }
    }

    public String getName(){
        if(entry<zipEFDIEntries.size()){
            return zipEFDIEntries.get(entry).name;
        } else {
            return "";
        }
    }

    public String getCSVName() {
        String name = this.getName();
        name = name.replace(".bin",".csv");
        name = name.replace(".BIN",".CSV");
        return  name;
    }

    public InputStream getZipEFDIEntryStream() throws  IOException{
        if(entry < zipEFDIEntries.size()){
            return zipEFDIEntries.get(entry).inputStream;
        } else {
            throw new IOException();
        }
    }

    public void close() throws IOException {
        if(zipFile != null) {
            zipEFDIEntries.clear();
            zipFile.close();
        }
    }

}
