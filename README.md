# Ozhuku

Ozhuku is an open-source, configuration-driven data ingestion and integration platform.

## Status

Development phase.

Architecture and planning are complete. Implementation is beginning with the repository and build foundation.

## Architecture

Ozhuku is designed as a modular monolith with:

- Java 17 minimum runtime
- Java 21 reference runtime
- Spring Boot runtime
- Streaming-first processing
- Bounded memory
- Backpressure
- Durable execution and recovery
- PostgreSQL control plane
- Pluggable storage, format, transport, acquisition, persistence, security, and notification adapters

## Current Development Target

```text
Local File
    ↓
RESOURCE_TRANSFER
    ↓
Local File