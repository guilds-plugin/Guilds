package me.glaremasters.guilds.utils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Glare
 * Date: 4/27/2019
 * Time: 11:12 AM
 */
public class LanguageUpdateUtils {

    /**
     * Used to download an updated copy of the languages
     * @throws IOException
     */
    public static void downloadLanguages() throws IOException {
        URL url = new URL("https://ci.ender.zone/job/Guilds/lastSuccessfulBuild/artifact/target/languages.zip");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", Constants.USER_AGENT);
        InputStream in = connection.getInputStream();
        FileOutputStream out = new FileOutputStream("plugins/Guilds/languages.zip");
        copy(in, out, 1024);
        out.close();
    }


    /**
     * Used in the method to get an updated version of the languages
     * @param input the input steam
     * @param output output file
     * @param bufferSize buffer size
     * @throws IOException
     */
    public static void copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        byte[] buf = new byte[bufferSize];
        int n = input.read(buf);
        while (n >= 0) {
            output.write(buf, 0, n);
            n = input.read(buf);
        }
        output.flush();
    }

    /**
     * Unzips the language file and updates the language files
     * @throws ZipException
     * @throws IOException
     */
    public static void upzipLanguages() throws ZipException, IOException {
        String source = "plugins/Guilds/languages.zip";
        String destination = "plugins/Guilds/languages/";
        ZipFile zipFile = new ZipFile(source);
        zipFile.extractAll(destination);
        File file = new File(source);
        FileUtils.forceDelete(file);
    }

    /**
     * When called, this will download the latest languages and update them on the server
     * @throws ZipException
     * @throws IOException
     */
    public static void updateLanguages() throws ZipException, IOException {
        try {
            downloadLanguages();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            upzipLanguages();
        }
    }

}
