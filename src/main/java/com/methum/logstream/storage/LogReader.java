package com.methum.logstream.storage;

import com.methum.logstream.ingestion.LogEntry;
import com.methum.logstream.ingestion.LogLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class LogReader {

    private final Path filePath;

    public LogReader(@Value("${logstream.storage.path}") String filePath) {
        this.filePath = Path.of(filePath);
    }


    public List<LogEntry> read(){


        List<LogEntry> logEntries = new ArrayList<LogEntry>();

        try(
                DataInputStream dis = new DataInputStream(new BufferedInputStream(Files.newInputStream(filePath)))

        ){
            while (true){

                long timestamp;
                try{
                  timestamp = dis.readLong();
                }catch (EOFException e){
                    break;
                }

                byte level =   dis.readByte();

                int serviceLength = dis.readInt();

                byte [] serviceArr = new byte[serviceLength];

                dis.readFully(serviceArr);

                int messageLength = dis.readInt();

                byte [] messageArr = new byte[messageLength];

                dis.readFully(messageArr);

                logEntries.add(new LogEntry
                        (Instant.ofEpochMilli(timestamp),
                                LogLevel.fromNumber(level),
                                new String(serviceArr,StandardCharsets.UTF_8),
                                new String(messageArr,StandardCharsets.UTF_8)));


            }
        }

        catch (Exception e) {
            throw new RuntimeException(e);
        }

        return logEntries;

    }
}
