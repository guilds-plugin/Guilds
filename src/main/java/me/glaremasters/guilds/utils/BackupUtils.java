/*
 * MIT License
 *
 * Copyright (c) 2023 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.glaremasters.guilds.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A utility class to perform backup operations, such as compressing files.
 */
public class BackupUtils {

    /**
     * Zips a directory into a specified zip file name.
     * @param zipFileName the name of the zip file
     * @param dir the directory to be compressed
     * @throws Exception if there was an error creating the zip file or adding the directory
     */
    public static void zipDir(String zipFileName, String dir) throws Exception {
        File dirObj = new File(dir);
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName))) {
            addDir(dirObj, out);
        }
    }

    /**
     * Adds a directory to a zip output stream.
     * @param dirObj the directory to add
     * @param out the zip output stream
     * @throws IOException if there was an error writing to the output stream
     */
    static void addDir(File dirObj, ZipOutputStream out) throws IOException {
        File[] files = dirObj.listFiles();
        byte[] tmpBuf = new byte[1024];

        for (File file : files) {
            if (file.isDirectory()) {
                addDir(file, out);
                continue;
            }
            try (FileInputStream in = new FileInputStream(file.getAbsolutePath())) {
                out.putNextEntry(new ZipEntry(file.getAbsolutePath()));
                int len;
                while ((len = in.read(tmpBuf)) > 0) {
                    out.write(tmpBuf, 0, len);
                }
                out.closeEntry();
            }
        }
    }

}
