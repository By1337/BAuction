package org.by1337.bauction.util.config;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.by1337.bauction.Main;
import org.by1337.blib.configuration.YamlConfig;

import java.io.File;

public class ConfigUtil {
    public static YamlConfig load(String path) {
        return tryRun(() -> new YamlConfig(trySave(path)));
    }

    @CanIgnoreReturnValue
    public static File trySave(String path) {
        path = path.replace('\\', '/');
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        var f = new File(Main.getInstance().getDataFolder(), path);
        if (!f.exists()) {
            Main.getInstance().saveResource(path, false);
        }
        return f;
    }

    public static <T> T tryRun(ThrowableRunnable<T> runnable) {
        try {
            return runnable.run();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface ThrowableRunnable<T> {
        T run() throws Throwable;
    }
}
