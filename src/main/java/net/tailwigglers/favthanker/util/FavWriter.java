package net.tailwigglers.favthanker.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class FavWriter {

    private CSVPrinter printer;

    public enum Headers {
        Timestamp, User, UserLink, Art, ArtLink
    }

    public FavWriter() throws IOException {
        File favoritesCsv = new File(Constants.FAVORITES_CSV_FILENAME);
        if (favoritesCsv.exists()) {
            printer = new CSVPrinter(
                    new FileWriter(Constants.FAVORITES_CSV_FILENAME, true),
                    CSVFormat.DEFAULT);
        } else {
            printer = new CSVPrinter(new FileWriter(Constants.FAVORITES_CSV_FILENAME), CSVFormat.DEFAULT);
            printer.printRecord(
                    Headers.Timestamp,
                    Headers.User,
                    Headers.UserLink,
                    Headers.Art,
                    Headers.ArtLink);
        }
    }

    public void printFavorite(Favorite favorite) throws IOException {
        String timestamp = DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(new Date());
        printer.printRecord(timestamp, favorite.user, favorite.userLink, favorite.art, favorite.artLink);
    }

    public void close() throws IOException {
        printer.close();
    }
}
