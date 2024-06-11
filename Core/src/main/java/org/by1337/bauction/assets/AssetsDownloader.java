package org.by1337.bauction.assets;

import org.by1337.blib.nbt.NBT;
import org.by1337.blib.util.Pair;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

public class AssetsDownloader {
    public static final String SITE = "https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets";
    public static final String LANG_PATH = "assets/minecraft/lang";

    public static CompletableFuture<File> downloadFrom(String fullUrl, File saveTo) {
        return CompletableFuture.supplyAsync(() -> {
            if (saveTo.exists()) return saveTo;
            String data = parsePage(fullUrl);
            try {
                Files.writeString(saveTo.toPath(), data, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return saveTo;
        });
    }

    private static String parsePage(String url) {
        HttpURLConnection connection = null;
        try {
            URL url0 = new URL(url);
            connection = (HttpURLConnection) url0.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(15000);
            connection.setRequestMethod("GET");

            int code = connection.getResponseCode();

            if (code == 200) {
                try (InputStream inputStream = connection.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    return String.join("\n", reader.lines().toList());
                }
            }
            throw new IOException("code: " + code + " Url: " + url);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
