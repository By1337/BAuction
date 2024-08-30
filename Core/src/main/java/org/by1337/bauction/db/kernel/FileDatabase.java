package org.by1337.bauction.db.kernel;

import org.by1337.bauction.Main;
import org.by1337.bauction.db.io.ChunkedFileReaderWriter;
import org.by1337.bauction.db.io.codec.Codec;
import org.by1337.bauction.util.auction.Category;
import org.by1337.bauction.util.auction.Sorting;
import org.by1337.blib.util.NameKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FileDatabase extends MemoryDatabase {
    private static final Logger LOGGER = LoggerFactory.getLogger("BAuction");
    protected final File dataFolder;

    public FileDatabase(Map<NameKey, Category> categoryMap, Map<NameKey, Sorting> sortingMap, List<DatabaseModule> modules) {
        super(categoryMap, sortingMap, modules);
        dataFolder = new File(Main.getInstance().getDataFolder(), "dataV2");
        dataFolder.mkdirs();
    }

    @Override
    public void load() {
        super.load();
        writeLock(this::load0); // Acquire lock to prevent waiting on synchronization of addSellItem, addUnsoldItem, and setUser during database loading
    }

    private void load0() {
        tryReadFile(new File(dataFolder, "sellItems.bauc"), SellItem.CODEC, this::addSellItem);
        tryReadFile(new File(dataFolder, "unsoldItems.bauc"), UnsoldItem.CODEC, this::addUnsoldItem);
        tryReadFile(new File(dataFolder, "users.bauc"), User.CODEC, this::setUser);
    }

    private <T> void tryReadFile(File file, Codec<T> codec, Consumer<T> consumer) {
        if (!file.exists() || !file.isFile()) return;
        readAll(file, codec, item -> {
            try {
                consumer.accept(item);
            } catch (Throwable t) {
                LOGGER.error("Failed to load item " + item, t);
            }
        });
    }

    private <T> void readAll(File file, Codec<T> codec, Consumer<T> consumer) {
        try (ChunkedFileReaderWriter<T> chunkedFileRw = new ChunkedFileReaderWriter<>(file, false, codec.getMagicNumber(), codec.getVersion(), codec)) {
            while (chunkedFileRw.hasNext()) {
                T item = chunkedFileRw.readNext();
                consumer.accept(item);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        super.close();

        writeData(new File(dataFolder, "sellItems.bauc"), SellItem.CODEC, list -> forEachSellItems(list::add), getSellItemsCount());
        writeData(new File(dataFolder, "unsoldItems.bauc"), UnsoldItem.CODEC, list -> forEachUnsoldItems(list::add), getUnsoldItemsCount());
        writeData(new File(dataFolder, "users.bauc"), User.CODEC, list -> forEachUsers(list::add), getUsersCount());
    }

    private <T> void writeData(File file, Codec<T> codec, Consumer<List<T>> filler, int initSize) {
        List<T> list = new ArrayList<>(initSize);
        filler.accept(list);
        writeAll(file, codec, list);
    }

    private <T> void writeAll(File file, Codec<T> codec, List<T> list) {
        try (ChunkedFileReaderWriter<T> chunkedFileRw = new ChunkedFileReaderWriter<>(file, true, codec.getMagicNumber(), codec.getVersion(), codec)) {
            for (T t : list) {
                chunkedFileRw.write(t);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
