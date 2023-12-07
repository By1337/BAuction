package org.by1337.bauction.serialize;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

public class SerializeUtils {

    public static void writeCollectionToStream(DataOutputStream data, Collection<String> collection) throws IOException {
        data.writeInt(collection.size());
        for (String element : collection) {
            data.writeUTF(element);
        }
    }
    public static List<String> readCollectionFromStream(DataInputStream in) throws IOException {
        int size = in.readInt();
        List<String> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(in.readUTF());
        }
        return result;
    }
}
