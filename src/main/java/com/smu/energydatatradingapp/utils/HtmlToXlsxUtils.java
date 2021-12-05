package com.smu.energydatatradingapp.utils;

import com.groupdocs.conversion.Converter;
import com.groupdocs.conversion.filetypes.FileType;
import com.groupdocs.conversion.options.convert.ConvertOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * This class provides method to convert Excel MIME type files to Excel xlsx format
 * @version 1.0 13 Sep 2021
 * @author Chen Jian Yu
 */
public class HtmlToXlsxUtils {
    private final Logger LOGGER = LoggerFactory.getLogger(HtmlToXlsxUtils.class);

    /**
     * This method converts Excel MIME type files to proper Excel xlsx file format so that these Excel files can be
     * read and processed.
     * @param countryFolder The folder name which consists of all the Excel MIME type files
     */
    public void htmlToXlsx (String countryFolder) {
        String projRootDir = System.getProperty("user.dir");
        File folder = new File(projRootDir + "/src/main/data/" + countryFolder);

        File[] listOfFiles = folder.listFiles();

        try {
            if (listOfFiles == null) {
                throw new FileNotFoundException();
            }

            for (File file: listOfFiles) {
                if (file.isFile() && file.getName().endsWith(".xls")) {
                    String filePath = file.getPath();
                    Converter converter = new Converter(filePath);
                    ConvertOptions convertOptions = FileType.fromExtension("xlsx").getConvertOptions();
                    converter.convert(filePath.substring(0, filePath.length()-3) + "xlsx", convertOptions);
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("No files found in folder path: " + folder);
        }
    }
}
