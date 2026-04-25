package com.methum.logstream.storage;

import com.methum.logstream.ingestion.LogEntry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.core.io.UTF8Writer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Component
public class LogWriter {

    private final Path filepath;

    public LogWriter(@Value("${logstream.storage.path}") String filePath){

        this.filepath = Path.of(filePath);
    }

    public void write(LogEntry logEntry){

        byte[] serviceBytes = logEntry.service().getBytes(StandardCharsets.UTF_8);
        byte[] messageBytes = logEntry.message().getBytes(StandardCharsets.UTF_8);

        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(filepath, StandardOpenOption.APPEND)))) {

            dos.writeLong(logEntry.timestamp().toEpochMilli());
            dos.writeByte(logEntry.level().getNumber());
            dos.writeInt(serviceBytes.length);
            dos.write(serviceBytes);
            dos.writeInt(messageBytes.length);
            dos.write(messageBytes);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
