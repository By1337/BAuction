package org.by1337.bauction.serialize;

import org.by1337.bauction.api.serialize.SerializableToByteArray;

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

    public static <T extends SerializableToByteArray> void writeSerializableCollection(DataOutputStream data, Collection<T> collection) throws IOException {
        data.writeInt(collection.size());
        for (T element : collection) {
            byte[] arr = element.getBytes();
            data.writeInt(arr.length);
            data.write(arr);
        }
    }

    public static <T> List<T> readSerializableCollection(DataInputStream in, Deserializable<T> deserializable) throws IOException {
        int size = in.readInt();
        List<T> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            int chunk = in.readInt();
            result.add(
                    deserializable.deserialize(in.readNBytes(chunk))
            );
        }
        return result;
    }

    public static void writeUUID(UUID uuid, DataOutputStream data) throws IOException {
        data.writeLong(uuid.getMostSignificantBits());
        data.writeLong(uuid.getLeastSignificantBits());
    }

    public static UUID readUUID(DataInputStream in) throws IOException {
        return new UUID(in.readLong(), in.readLong());
    }
}
