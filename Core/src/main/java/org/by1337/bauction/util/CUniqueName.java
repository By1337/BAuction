package org.by1337.bauction.util;

import org.by1337.bauction.api.util.UniqueName;

import java.io.*;

public class CUniqueName implements UniqueName {
    private final String key;
    private final int seed;
    private final long pos;

    public CUniqueName(String key, int seed, long pos) {
        this.key = key;
        this.seed = seed;
        this.pos = pos;
    }

    public CUniqueName(String key) {
        this.key = key;
        seed = -1;
        pos = -1;
    }

    public String getKey() {
        return key;
    }

    public int getSeed() {
        return seed;
    }

    public long getPos() {
        return pos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CUniqueName that)) return false;
        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    public boolean canBeCompressToSeedAndPos() {
        return seed > 0 && pos > 0;
    }

    @Override
    public String toString() {
        return key + ":" + seed + ":" + pos;
    }

    public static CUniqueName fromString(String s) {
        String[] parts = s.split(":");
        if (parts.length == 3) {
            String key = parts[0];
            int seed = Integer.parseInt(parts[1]);
            long pos = Long.parseLong(parts[2]);
            return new CUniqueName(key, seed, pos);
        } else if (parts.length == 1) {
            return new CUniqueName(parts[0]);
        } else {
            throw new IllegalArgumentException("Invalid string format for CUniqueName: " + s);
        }
    }

    @Override
    public byte[] getBytes() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream data = new DataOutputStream(out)) {
            data.writeUTF(getKey());
            data.flush();
            return out.toByteArray();
        }
    }

    public static UniqueName fromBytes(byte[] arr) throws IOException {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(arr))) {
            return new CUniqueName(
                    in.readUTF()
            );
        }
    }

}
