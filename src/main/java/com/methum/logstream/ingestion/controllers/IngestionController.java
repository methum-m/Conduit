package com.methum.logstream.ingestion.controllers;

import com.methum.logstream.ingestion.LogEntry;
import com.methum.logstream.ingestion.dtos.LogEntryRequestDto;
import com.methum.logstream.ingestion.service.IngestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("")
public class IngestionController {


    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService){
        this.ingestionService = ingestionService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<String> ingestLog(@RequestBody LogEntryRequestDto logEntryRequestDto) throws IOException {

        ingestionService.handleLogEntryRequest(logEntryRequestDto);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/read")
    public List<LogEntry> read(){


        return ingestionService.handleReadLogEntryRequest();

    }

    @GetMapping("/search/{term}")
    public List<LogEntry> search(@PathVariable String term) throws IOException {

       return ingestionService.handleSearch(term);

    }






}
