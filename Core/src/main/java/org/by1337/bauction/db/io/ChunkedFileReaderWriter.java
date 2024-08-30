package org.by1337.bauction.db.io;

import org.by1337.bauction.db.io.codec.Codec;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


public class ChunkedFileReaderWriter<T> implements AutoCloseable {
    private static final int INT_SIZE = Integer.BYTES;
    protected final FileChannel fileChannel;
    protected final RandomAccessFile randomAccessFile;
    protected int version;
    private final int magicNumber;
    private final Codec<T> codec;

    public ChunkedFileReaderWriter(File file, boolean forWriting, int magicNumber, int actualVersion, Codec<T> codec) throws IOException {
        this.magicNumber = magicNumber;
        this.version = actualVersion;
        this.codec = codec;
        randomAccessFile = new RandomAccessFile(file, forWriting ? "rw" : "r");
        fileChannel = randomAccessFile.getChannel();
        if (forWriting) {
            writeHeader();
        } else {
            readHeader();
        }
    }

    private void readHeader() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(INT_SIZE * 2);
        fileChannel.read(buffer);
        buffer.flip();

        int magic = buffer.getInt();
        version = buffer.getInt();

        if (magic != magicNumber) {
            throw new IOException("Invalid magic number: " + Integer.toHexString(magic));
        }
    }

    private void writeHeader() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(INT_SIZE * 2);
        buffer.putInt(magicNumber);
        buffer.putInt(version);
        buffer.flip();
        fileChannel.write(buffer);
    }


    public boolean hasNext() throws IOException {
        return fileChannel.position() < fileChannel.size();
    }

    public T readNext() throws IOException {
        org.by1337.blib.io.ByteBuffer buffer = new org.by1337.blib.io.ByteBuffer(readNextChunk());
        return codec.read(buffer, version);
    }

    public byte[] readNextChunk() throws IOException {
        ByteBuffer sizeBuffer = ByteBuffer.allocate(INT_SIZE);
        fileChannel.read(sizeBuffer);
        sizeBuffer.flip();
        int chunkSize = sizeBuffer.getInt();

        ByteBuffer chunkBuffer = ByteBuffer.allocate(chunkSize);
        fileChannel.read(chunkBuffer);
        return chunkBuffer.array();
    }

    public void write(T val) throws IOException {
        org.by1337.blib.io.ByteBuffer buffer = new org.by1337.blib.io.ByteBuffer();
        codec.write(val, buffer);
        writeChunk(buffer.toByteArray());
    }

    public void writeChunk(byte[] chunkData) throws IOException {
        ByteBuffer sizeBuffer = ByteBuffer.allocate(INT_SIZE);
        sizeBuffer.putInt(chunkData.length);
        sizeBuffer.flip();
        fileChannel.write(sizeBuffer);

        ByteBuffer chunkBuffer = ByteBuffer.wrap(chunkData);
        fileChannel.write(chunkBuffer);
    }

    @Override
    public void close() throws IOException {
        randomAccessFile.close();
    }
}
