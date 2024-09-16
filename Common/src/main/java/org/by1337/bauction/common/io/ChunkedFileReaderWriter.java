package org.by1337.bauction.common.io;

import org.by1337.bauction.common.io.codec.Codec;
import org.by1337.btcp.common.io.AbstractByteBuffer;

import java.io.*;
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
        AbstractByteBuffer buffer = AbstractByteBuffer.wrap(new ByteArrayInputStream(readNextChunk()));
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
        AbstractByteBuffer buffer = AbstractByteBuffer.wrap(new ByteArrayOutputStream());
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
