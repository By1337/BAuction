package org.by1337.bauction.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;

public class ConfigDiffTest {

    @Test
    public void run() throws IOException, InvalidConfigurationException {
//        YamlConfiguration configuration = new YamlConfiguration();
//        configuration.load(new InputStreamReader(getResource("ru/config.yml")));
    }

    private InputStream getResource(String path) {

        ClassLoader classLoader = ConfigDiffTest.class.getClassLoader();

        InputStream inputStream = classLoader.getResourceAsStream("path");

        if (inputStream == null) {
            throw new IllegalArgumentException(new NoSuchElementException(path));
        }
        return inputStream;
    }
}
