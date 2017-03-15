package de.tum.msrg.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FileUtils {

    /**
     * Check whether a file exists
     * 
     * @param fileName
     *            The name of the file to be checked.
     * @return true if the file exists, false otherwise
     */
    public static boolean fileExists(String fileName) {
        File checkFile = new File(fileName);
        return checkFile.exists();
    }

    /**
     * Check for the existence of the filename and deletes it if it exists.
     * 
     * @param fileName
     *            The name of the file to be deleted.
     */
    public static void cleanUpFile(String fileName) {
        File outFile = new File(fileName);
        if (outFile.exists())
            outFile.delete();

    }

    /**
     * Check and create a directory.
     * 
     * @param dirName
     *            The name of the directory to be created.
     * @return true if success, false if the directory already exists.
     */
    public static boolean chkNCreateDir(String dirName) {
        File outDirHandle = new File(dirName);
        if (!outDirHandle.exists()) {
            outDirHandle.mkdir();
            return true;
        }
        return false;
    }

    /**
     * Delete all the files in the specified directory, but not the directory
     * itself.
     * 
     * @param dirName
     *            The directory to be cleaned.
     * @return true if success, false if the directory does not exist.
     */
    public static boolean cleanDir(String dirName) {
        File dirHandle = new File(dirName);
        if (dirHandle.exists()) {
            File[] fileList = dirHandle.listFiles();
            for (File fileName : fileList) {
                fileName.delete();
            }
            return true;
        }
        System.err.printf("Dir %s does not exist!\n", dirName);
        return false;
    }

    /**
     * Extract a column of data from a file into a ArrayList
     * 
     * @param fileName
     *            the file to be processed. The data in each line should be
     *            equal and tab seperated.
     * @param col
     *            the column of the data required
     * @return ArrayList of the data found in the column; null if an error
     *         occurred with warning.
     */
    public static ArrayList<String> extractCol(String fileName, int col) {
        ArrayList<String> dataList = new ArrayList<String>();
        try {
            BufferedReader inFile = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = inFile.readLine()) != null) {
                line = line.trim();
                String[] lineParts = line.split("[\\s]+");
                if (lineParts.length < col + 1) {
                    System.out.println("extractCol(): data not enough in file "
                            + fileName);
                    return null;
                }
                dataList.add(lineParts[col]);
            }
            inFile.close();
        } catch (IOException e) {
            System.out.println("extractCol(): " + e.getMessage());
            return null;
        }
        return dataList;
    }

    /**
     * Copy a file to another.
     * 
     * @param src
     *            the source file.
     * @param dest
     *            the destination file
     */
    public static void copyFile(String src, String dest) {
        try {
            BufferedReader inFile = new BufferedReader(new FileReader(src));
            BufferedWriter outFile = new BufferedWriter(new FileWriter(dest));
            String line = inFile.readLine();
            while (line != null) {
                outFile.write(line);
                outFile.newLine();
                line = inFile.readLine();
            }
            outFile.close();
            inFile.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return;
    }

    public static String joinFilePaths(String path1, String path2) {
    	return path1 + File.separator + path2;
    }
}
