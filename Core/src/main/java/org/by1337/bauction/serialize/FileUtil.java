package org.by1337.bauction.serialize;

import org.by1337.bauction.Main;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FileUtil {

    public static void write(File file, Collection<? extends SerializableToByteArray> source) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file);
             DataOutputStream dos = new DataOutputStream(fos)) {

            for (SerializableToByteArray serializable : source) {
                try {
                    byte[] byteArray = serializable.getBytes();
                    dos.writeInt(byteArray.length);
                    dos.write(byteArray);
                } catch (Exception e) {
                    Main.getMessage().error("failed to save %s", e, source);
                }
            }
        }
    }

    public static <T> List<T> read(File file, Deserializable<T> deserializeProvider) throws IOException {
        List<T> list = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] initialBuffer = new byte[4];
            int bytesRead = fis.read(initialBuffer);

            while (bytesRead == 4) {
                int dynamicChunkSize = byteToInt(initialBuffer);

                byte[] buffer = new byte[dynamicChunkSize];
                int chunkBytesRead = fis.read(buffer);

                if (chunkBytesRead == dynamicChunkSize) {
                    list.add(deserializeProvider.deserialize(buffer));
                    bytesRead = fis.read(initialBuffer);
                } else {
                    break;
                }
            }
        }
        return list;
    }

    public static File createNewFile(File file) throws IOException {
        if (!file.createNewFile()) {
            throw new FileCreateException("failed to create file: " + file);
        }
        return file;
    }

    public static File createNewInDataFolderIfNotExist(String file) throws IOException {
        if (file.startsWith("/") || file.startsWith("\\")) file = file.substring(1);
        File f = new File(Main.getInstance().getDataFolder() + "/" + file);
        if (!f.exists()) {
            return createNewFile(f);
        }
        return f;
    }

    public static File createNewInDataFolder(String file) throws IOException {
        if (file.startsWith("/") || file.startsWith("\\")) file = file.substring(1);
        File f = new File(Main.getInstance().getDataFolder() + "/" + file);
        return createNewFile(f);
    }


    public static void deleteFile(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getPath());
        }
        if (file.isDirectory()) {
            deleteInFolder(file);
        }
        if (!file.delete()) {
            throw new FileDeleteException("failed to delete file: " + file);
        }
    }

    private static void deleteInFolder(File folder) {
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteInFolder(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
    }

    public static void deleteFileInDataFolder(String file) throws IOException {
        if (file.startsWith("/") || file.startsWith("\\")) file = file.substring(1);
        File f = new File(Main.getInstance().getDataFolder() + "/" + file);
        deleteFile(f);
    }

    public static void deleteFileInDataFolderIfExist(String file) throws IOException {
        if (file.startsWith("/") || file.startsWith("\\")) file = file.substring(1);
        File f = new File(Main.getInstance().getDataFolder() + "/" + file);
        if (f.exists())
            deleteFile(f);
    }

    private static int byteToInt(byte[] bytes) {
        int value = 0;
        for (byte b : bytes) {
            value = (value << 8) + (b & 0xFF);
        }
        return value;
    }
}
