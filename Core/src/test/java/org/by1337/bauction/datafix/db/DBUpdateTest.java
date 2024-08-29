/*
package org.by1337.bauction.datafix.db;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.by1337.bauction.datafix.DataVersion;
import org.by1337.bauction.datafix.db.sellitem.SellItemUpdater;
import org.by1337.bauction.datafix.db.sellitem.V100ToV101;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.stream.Stream;

public class DBUpdateTest {
    private final String sellItemV100 = """
            {
                "item": "e2lkOiJtaW5lY3JhZnQ6cGlua19zaHVsa2VyX2JveCIsdGFnOntkaXNwbGF5OntOYW1lOid7ImNvbG9yIjoicmVkIiwidGV4dCI6IkN1c3RvbSBzaHVsa2VyIGJveCJ9J30sUHVibGljQnVra2l0VmFsdWVzOnsiYmxpYjpibGliX2V4YW1wbGUiOjFifX0sQ291bnQ6MWJ9",
                "sellerName": "_By1337_",
                "sellerUuid": "f5e129de-9747-384f-80e1-526d17bab96d",
                "price": 1337.0,
                "saleByThePiece": true,
                "tags": [
                    "pink_shulker_box",
                    "is_block",
                    "is_solid",
                    "blib_example"
                ],
                "timeListedForSale": 1337,
                "removalDate": 1337,
                "uuid": "e4a3a2f3-a209-4151-96e0-cfd65d4ac1fb",
                "material": "PINK_SHULKER_BOX",
                "amount": 1,
                "priceForOne": 1337.0,
                "sellFor": []
            }
            """;
    private final String unsoldItemV100 = """
            {
                "item": "e2lkOiJtaW5lY3JhZnQ6cGlua19zaHVsa2VyX2JveCIsdGFnOntkaXNwbGF5OntOYW1lOid7ImNvbG9yIjoicmVkIiwidGV4dCI6IkN1c3RvbSBzaHVsa2VyIGJveCJ9J30sUHVibGljQnVra2l0VmFsdWVzOnsiYmxpYjpibGliX2V4YW1wbGUiOjFifX0sQ291bnQ6MWJ9",
                "expired": 1337,
                "deleteVia": 1337,
                "owner": "f5e129de-9747-384f-80e1-526d17bab96d",
                "uuid": "e4a3a2f3-a209-4151-96e0-cfd65d4ac1fb"
            }
            """;

    private final String userV100 = """
            {
                "nickName": "_By1337_",
                "uuid": "f5e129de-9747-384f-80e1-526d17bab96d",
                "unsoldItems": [],
                "itemForSale": [
                    "e4a3a2f3-a209-4151-96e0-cfd65d4ac1fb",
                    "e47f5758-e607-45ba-92f3-55a54888847d",
                    "cf9f8c42-751c-468f-972e-139996578e63"
                ],
                "dealCount": 1337,
                "dealSum": 1337.0
            }
            """;


    @Test
    public void sellItemUpdateTest() throws IOException {
        Gson gson = new Gson();
        V100ToV101 v101 = new V100ToV101();
        Stream<SellItemUpdater.SellItemRaw> stream = Stream.of(v101.update(gson.fromJson(sellItemV100, JsonObject.class)));

        SellItemUpdater updater = new SellItemUpdater();

        stream = updater.update(stream, DataVersion.SellItemVersion.V101, DataVersion.SellItemVersion.V103);

    }

    @Test
    public void unsoldItemUpdateTest() throws IOException {
        Gson gson = new Gson();
        byte[] sellItem107to108 = DBUpdateV100toV101.getUnsoldItemBytes(gson.fromJson(unsoldItemV100, JsonObject.class));
        byte[] sellItem108to109 = DBUpdate108.fromBytesUnsoldItem(sellItem107to108).getBytes();
        // 109-110- skip
        byte[] sellItem110to114 = DBUpdate110.fromBytesUnsoldItem(sellItem108to109).getBytes();
    }

    @Test
    public void UserUpdateTest() throws IOException {
        Gson gson = new Gson();
        byte[] sellItem108 = DBUpdateV100toV101.getUserBytes(gson.fromJson(userV100, JsonObject.class));
        byte[] sellItem109 = DBUpdate108.fromBytesUser(sellItem108).getBytes();
        // 109-110 skip
        // 110-114 skip
    }

}*/
