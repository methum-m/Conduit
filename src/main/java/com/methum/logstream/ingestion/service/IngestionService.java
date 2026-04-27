package com.methum.logstream.ingestion.service;

import com.methum.logstream.ingestion.LogEntry;
import com.methum.logstream.ingestion.dtos.LogEntryRequestDto;
import com.methum.logstream.storage.InvertedIndex;
import com.methum.logstream.storage.LogReader;
import com.methum.logstream.storage.LogWriter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Service
public class IngestionService {

    private final LogWriter logWriter;

    private final LogReader logReader;

    private final InvertedIndex index;

    public IngestionService(LogWriter logWriter, LogReader logReader, InvertedIndex index){
        this.logWriter = logWriter;
        this.logReader = logReader;
        this.index = index;
    }

    public void handleLogEntryRequest(LogEntryRequestDto logEntryRequestDto) throws IOException {

        LogEntry logEntry = new LogEntry(Instant.now(),logEntryRequestDto.level(),logEntryRequestDto.service(),logEntryRequestDto.message());

        logWriter.write(logEntry);

    }


    public List<LogEntry> handleReadLogEntryRequest(){

        return logReader.read();

    }

    public List<LogEntry> handleSearch(String term) throws IOException {


        return index.search(term);
    }
}
