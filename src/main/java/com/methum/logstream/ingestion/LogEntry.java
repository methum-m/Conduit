package com.methum.logstream.ingestion;

import java.time.Instant;

public record LogEntry(
        Instant timestamp,
        String level,
        String service,
        String message
) {}
