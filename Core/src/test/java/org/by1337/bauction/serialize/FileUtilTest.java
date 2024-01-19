package org.by1337.bauction.serialize;

import junit.framework.TestCase;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class FileUtilTest extends TestCase {

    private SerializeTest obj = new SerializeTest("test");

    public void testWrite() throws IOException {
        File file = new File("./src/test/FileUtilTest.something");
        if (!file.exists()) {
            file.createNewFile();
        } else {
            file.delete();
            file.createNewFile();
        }
        FileUtil.write(file, List.of(obj));
    }

    public void testRead() throws IOException {
        File file = new File("./src/test/FileUtilTest.something");
        if (!file.exists()) {
            throw new IllegalArgumentException("file non-exist! Pls run testWrite first");
        }
        SerializeTest deserialized = FileUtil.read(file, SerializeTest::deserialize).get(0);
        Assert.assertEquals(deserialized, obj);
        file.delete();
    }

    private record SerializeTest(String str) implements SerializableToByteArray {

        @Override
            public byte[] getBytes() {
                return str.getBytes();
            }

            public static SerializeTest deserialize(byte[] arr) {
                return new SerializeTest(new String(arr));
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                SerializeTest that = (SerializeTest) o;
                return Objects.equals(str, that.str);
            }

    }
}