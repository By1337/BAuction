package org.by1337.bauction.datafix.db;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.by1337.bauction.Main;
import org.by1337.bauction.serialize.FileUtil;
import org.by1337.bauction.api.serialize.SerializableToByteArray;
import org.by1337.bauction.serialize.SerializeUtils;
import org.by1337.bauction.util.TimeCounter;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class DBUpdate107 {

    // обновляет файлы сохранения с версий 1.0.4-b до 1.0.6.2-b (включительно) на новый формат принятый начиная с версии 1.0.7-b
    public void update() throws IOException {
        Main.getMessage().logger("Loading items v1.0.4...");
        List<JsonObject> items = load("sellItems", new TypeToken<List<JsonObject>>() {
        }.getType());
        new File(Main.getInstance().getDataFolder() + "/sellItems").delete();
        FileUtil.deleteFileInDataFolderIfExist("sellItems");

        List<JsonObject> users = load("users", new TypeToken<List<JsonObject>>() {
        }.getType());
        new File(Main.getInstance().getDataFolder() + "/users").delete();
        FileUtil.deleteFileInDataFolderIfExist("users");

        List<JsonObject> unsoldItems = load("unsoldItems", new TypeToken<List<JsonObject>>() {
        }.getType());
        new File(Main.getInstance().getDataFolder() + "/unsoldItems").delete();
        FileUtil.deleteFileInDataFolderIfExist("unsoldItems");

        Main.getMessage().logger("loaded %s sell items, %s users and %s unsold items", items.size(), users.size(), unsoldItems.size());

        File home = new File(Main.getInstance().getDataFolder() + "/data");
        if (!home.exists()) {
            home.mkdir();
        }
        File fItems = FileUtil.createNewInDataFolderIfNotExist("data/items.bauc");
        File fUsers = FileUtil.createNewInDataFolderIfNotExist("data/users.bauc");
        File fUnsoldItems = FileUtil.createNewInDataFolderIfNotExist("data/unsoldItems.bauc");


        TimeCounter timeCounter = new TimeCounter();

        if (!items.isEmpty()) {
            FileUtil.write(fItems, items.stream().map(jsonObject -> (SerializableToByteArray) () -> getSellItemBytes(jsonObject)).toList());
        }
        if (!users.isEmpty()) {
            FileUtil.write(fUsers, users.stream().map(jsonObject -> (SerializableToByteArray) () -> getUserBytes(jsonObject)).toList());
        }
        if (!unsoldItems.isEmpty()) {
            FileUtil.write(fUnsoldItems, unsoldItems.stream().map(jsonObject -> (SerializableToByteArray) () -> getUnsoldItemBytes(jsonObject)).toList());
        }
        Main.getMessage().logger(
                "updated %s sell items, %s users and %s unsold items in %s ms.",
                items.size(),
                users.size(),
                items.size(),
                timeCounter.getTime()
        );

    }
    // извлекает данные из json объекта и перегоняет их в байты для сохранения по формату сохранения версии 1.0.7-b
    private byte[] getUserBytes(JsonObject jsonObject) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream data = new DataOutputStream(out)) {
            data.writeUTF(jsonObject.getAsJsonPrimitive("nickName").getAsString());
            data.writeUTF(jsonObject.getAsJsonPrimitive("uuid").getAsString());
            data.writeInt(jsonObject.getAsJsonPrimitive("dealCount").getAsInt());
            data.writeDouble(jsonObject.getAsJsonPrimitive("dealSum").getAsDouble());
            data.flush();
            return out.toByteArray();
        }
    }
    // извлекает данные из json объекта и перегоняет их в байты для сохранения по формату сохранения версии 1.0.7-b
    private byte[] getUnsoldItemBytes(JsonObject jsonObject) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream data = new DataOutputStream(out)) {
            data.writeUTF(jsonObject.getAsJsonPrimitive("item").getAsString());
            data.writeLong(jsonObject.getAsJsonPrimitive("expired").getAsLong());
            data.writeUTF(jsonObject.getAsJsonPrimitive("owner").getAsString());
            data.writeUTF(jsonObject.getAsJsonPrimitive("uuid").getAsString());
            data.writeInt(-1);
            data.writeLong(-1);
            data.writeLong(jsonObject.getAsJsonPrimitive("deleteVia").getAsLong());
            data.flush();
            return out.toByteArray();
        }
    }
    // извлекает данные из json объекта и перегоняет их в байты для сохранения по формату сохранения версии 1.0.7-b
    private byte[] getSellItemBytes(JsonObject jsonObject) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream data = new DataOutputStream(out)) {
            data.writeUTF(jsonObject.getAsJsonPrimitive("item").getAsString());
            data.writeUTF(jsonObject.getAsJsonPrimitive("sellerName").getAsString());
            data.writeUTF(jsonObject.getAsJsonPrimitive("sellerUuid").getAsString());
            data.writeDouble(jsonObject.getAsJsonPrimitive("price").getAsDouble());
            data.writeBoolean(jsonObject.getAsJsonPrimitive("saleByThePiece").getAsBoolean());
            SerializeUtils.writeCollectionToStream(data,
                    toList(jsonObject.getAsJsonArray("tags")).stream().map(JsonElement::getAsString).toList()
            );
            data.writeLong(jsonObject.getAsJsonPrimitive("timeListedForSale").getAsLong());
            data.writeLong(jsonObject.getAsJsonPrimitive("removalDate").getAsLong());
            data.writeUTF(jsonObject.getAsJsonPrimitive("uuid").getAsString());
            data.writeInt(-1);
            data.writeLong(-1);
            data.writeUTF(jsonObject.getAsJsonPrimitive("material").getAsString());
            data.writeInt(jsonObject.getAsJsonPrimitive("amount").getAsInt());
            data.writeDouble(jsonObject.getAsJsonPrimitive("priceForOne").getAsDouble());
            SerializeUtils.writeCollectionToStream(data, new ArrayList<>()); // в версиях ниже всегда пустой
            data.flush();
            return out.toByteArray();
        }
    }
    // в 1.16.5 отсутствует метод JsonArray#asList
    private List<JsonElement> toList(JsonArray array){
        List<JsonElement> list = new ArrayList<>(array.size());
        for (JsonElement jsonElement : array) {
            list.add(jsonElement);
        }
        return list;
    }
    // считывает все json файлы находящиеся в папке
    private <T> List<T> load(String dir, Type type) {
        File home = new File(Main.getInstance().getDataFolder() + "/" + dir);
        List<T> out = new ArrayList<>();
        Gson gson = new Gson();
        try {
            if (!home.exists()) {
                home.mkdir();
                return out;
            }
            for (File file : home.listFiles()) {
                try (FileReader reader = new FileReader(file)) {
                    out.addAll(gson.fromJson(reader, type));
                }
            }
        } catch (IOException e) {
            Main.getMessage().error("failed to save!", e);
        }
        return out;
    }
}
