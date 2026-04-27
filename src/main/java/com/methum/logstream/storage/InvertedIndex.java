package com.methum.logstream.storage;

import com.methum.logstream.ingestion.LogEntry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class InvertedIndex {

    private final LogReader logReader;

    HashMap<String, List<Long>> termOffsets = new HashMap<String,List<Long>>();

    public InvertedIndex(LogReader logReader) {
        this.logReader = logReader;
    }

    public void index(LogEntry logEntry,long offset){

        // get the individual terms from the message
        String [] terms = logEntry.message().split("\\s+");


        for (String term : terms) {

            termOffsets.computeIfAbsent(term, k -> new ArrayList<>()).add(offset);

        }

    }


    public List<LogEntry> search(String term) throws IOException {

        List<LogEntry> entries = new ArrayList<>();

        if (termOffsets.containsKey(term)){

            List<Long> offsets = termOffsets.get(term);

            for (Long offset : offsets) {
                LogEntry logEntry = logReader.readAt(offset);
                entries.add(logEntry);
            }


            }else{

                    return new ArrayList<>();
            }



        return entries;

    }

    @PostConstruct
    public void rebuildIndex(){

       Map<Long,LogEntry> logEntryMap =  logReader.readWithOffsets();

       Set<Map.Entry<Long,LogEntry>> logEntries = logEntryMap.entrySet();

      for (Map.Entry<Long,LogEntry> entry : logEntries){

          long offset = entry.getKey();
          LogEntry logEntry = entry.getValue();
          index(logEntry,offset);

      }



    }





}







