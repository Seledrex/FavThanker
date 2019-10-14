package com.seledrex.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ShoutWriter {

    private CSVPrinter printer;

    public enum Headers {
        Timestamp, User, Group, Message, Link
    }

    public ShoutWriter() throws IOException {
        File shoutsCsv = new File(Constants.SHOUTS_CSV_FILENAME);
        if (shoutsCsv.exists()) {
            printer = new CSVPrinter(
                    new FileWriter(Constants.SHOUTS_CSV_FILENAME, true),
                    CSVFormat.DEFAULT);

        } else {
            printer = new CSVPrinter(new FileWriter(Constants.SHOUTS_CSV_FILENAME), CSVFormat.DEFAULT);
            printer.printRecord(
                    Headers.Timestamp,
                    Headers.User,
                    Headers.Group,
                    Headers.Message,
                    Headers.Link);
        }
    }

    public void printShout(String timestamp,
                           String user,
                           String group,
                           String message,
                           String link) throws IOException {
        printer.printRecord(timestamp, user, group, message, link);
    }

    public void close() throws IOException {
        printer.close();
    }

}
