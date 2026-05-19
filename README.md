# Siphon

**A high-performance log ingestion and search system designed for scalable, real-time processing.**

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-blue?style=flat-square&logo=githubactions)](https://github.com/features/actions)
[![Deploy](https://img.shields.io/badge/Deployed-DigitalOcean-0080FF?style=flat-square&logo=digitalocean)](https://digitalocean.com)

---

## Overview

Conduit is a backend log ingestion and search engine built from the ground up in Java 21. Rather than relying on established solutions like Apache Flume or Logstash, Conduit is built from primitives, featuring a **custom binary storage format**, a **length-prefix framing scheme**, and a **minimal-dependency architecture** that keeps the system transparent, inspectable, and easy to reason about.

The project was built as a deliberate exercise in low-level infrastructure engineering: understanding how data moves from a network request to a byte on disk, and how to make that process efficient without hiding complexity behind libraries.

---

## Architecture

A log entry flows through Conduit in clearly separated stages:

```
Client Request
     │
     ▼
┌─────────────────┐
│  Ingestion API  │  POST /ingest — accepts log entries over HTTP
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Binary Encoder │  Serializes log data into a custom binary format
│                 │  with length-prefix framing per record
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  On-Disk Store  │  Appends encoded records to the log segment file
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  In-Memory Index│  Maintains a term → offset index for fast lookup
└────────┬────────┘
         │
     ┌───┴────┐
     ▼        ▼
GET /read  GET /search
```

**Ingestion Layer** — receives HTTP requests and validates incoming log entries before passing them downstream.

**Binary Encoder** — serializes each log record into a compact binary format. Each record is framed with a length prefix, allowing the reader to efficiently seek through the file without scanning every byte.

**On-Disk Store** — appends encoded records sequentially to a segment file. Sequential writes are intentional: they are significantly faster than random-access writes and align with how production log systems like Kafka handle persistence.

**Index** — an in-memory structure that maps search terms to byte offsets in the segment file, enabling search without a full file scan. *(Persistent index is on the roadmap.)*

---

## Key Design Decisions

### Custom Binary Format over JSON
Storing logs as JSON on disk is convenient but wasteful field names are repeated for every record, and the parser has to scan every byte to find field boundaries. Conduit's binary format encodes each record as a fixed structure with length-prefixed fields. The reader always knows exactly how many bytes to read, which allows for efficient sequential reads and direct offset-based access.

### Length-Prefix Framing
Each field in a record is preceded by its byte length. This means the decoder never needs to scan for a delimiter it reads the length, reads exactly that many bytes, and moves on. This is the same framing approach used in systems like Protocol Buffers and Kafka's wire format.

### Variable Length Encoding *(In Progress)*
For length prefix values, Conduit will use a custom Variable Length Encoding (VLE) scheme. Small values (the common case) encode in a single byte; larger values use continuation bits to span multiple bytes. This reduces the per-record overhead for short fields without imposing a fixed upper limit on field size.

### Minimal Dependencies
Conduit avoids pulling in heavy dependencies for things that can be implemented directly. This is a conscious tradeoff: more code to write, but a system that is fully legible and doesn't abstract away the infrastructure decisions that make it interesting.

---

## API Reference

Full interactive documentation is available via Swagger UI at `/swagger-ui/index.html` on the live deployment.

---

### `POST /ingest`

Ingests a new log entry into the system.

**Request body:**
```json
{
  "message": "User login failed",
  "level": "ERROR",
  "source": "auth-service",
  "timestamp": "2025-04-30T10:00:00Z"
}
```

**Response:** `200 OK` on success.

---

### `GET /search?q={term}`

Searches the ingested logs for entries matching the given term. Uses the in-memory index to resolve matching byte offsets and retrieves the corresponding records from disk.

**Example:**
```
GET /search?q=ERROR
```

**Response:** Array of matching log entries.

---

### `GET /read`

Returns all ingested log entries from the on-disk store, decoded from the binary format back into structured JSON.

**Response:** Array of log entries in ingestion order.

---

## Running Locally

**Prerequisites:** Java 21, Maven 3.8+

```bash
# Clone the repository
git clone https://github.com/methum-m/conduit.git
cd conduit

# Build
mvn clean package

# Run
java -jar target/conduit-*.jar
```

The server starts on `http://localhost:8080` by default.  
Swagger UI: `http://localhost:8080/swagger-ui/index.html`

---

## Author

**Methum Thimbiripola**  
[github.com/methum-m](https://github.com/methum-m)
