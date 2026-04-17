# Parallel GCS Reads/Writes for Project Export and Import

**Date:** 2026-03-25
**Status:** Approved
**Scope:** `ObjectifyStorageIo.java`, `FileImporterImpl.java`

## Problem

Project export (`exportProjectSourceZip`) and import (`createProject`) make sequential GCS round-trips for every file stored in Google Cloud Storage. Each GCS read requires 2 HTTP round-trips (metadata + channel read); each write requires 1. For a project with N GCS-stored files, export takes N × 2 × RTT and import takes N × RTT. This affects both user-facing .aia downloads and build server submissions (which call `exportProjectSourceZip` with `includeYail=true`).

## Solution

Parallelize GCS I/O using `ExecutorService` backed by `ThreadManager.currentRequestThreadFactory()` (required because the codebase uses legacy App Engine bundled `GcsService`).

### Export: `exportProjectSourceZip`

**Phase 1 — Parallel GCS fetch:**
After the Datastore transaction collects `FileData`, separate GCS files from inline/blobstore files. Submit all GCS reads as `Callable<byte[]>` tasks to an executor (max 20 threads). Collect results into a `ConcurrentHashMap<String, byte[]>` keyed by filename. Guard with `if (!gcsFiles.isEmpty())` to avoid creating an executor when there are no GCS files.

**Phase 2 — Sequential ZIP write:**
Iterate `fileData` in original order. For GCS files, look up pre-fetched content from the map. For blobstore/datastore files, handle inline as today. Write each entry to `ZipOutputStream`.

**Refactoring:** Extract the GCS read logic (metadata + channel read + NPE retry loop) into a helper:

```java
/**
 * Reads a single file from GCS with retry logic for transient NPE failures.
 * Thread-safe — can be called concurrently from multiple threads.
 *
 * @param gcsName  the GCS object name
 * @param fatalError  if true, throws IOException when all NPE retries are exhausted;
 *                    if false, returns an empty byte[] on permanent NPE failure
 * @return file content bytes, or empty byte[] on non-fatal permanent failure
 */
private byte[] readGcsFile(String gcsName, boolean fatalError) throws IOException
```

This helper also replaces the duplicated GCS read code in `downloadRawFile` for consistency.

### Import: `createProject`

**Phase 1 — Metadata creation (in transaction):**
Split `createRawFile()` into `createRawFileMetadata()` + `writeGcsFile()`.

```java
/**
 * Creates a FileData object with metadata only — no GCS I/O.
 * Calls useGCSforFile() to determine storage location. For GCS files,
 * sets isGCS=true and gcsName but does NOT write content to GCS.
 * For inline files, stores content directly in FileData.content.
 *
 * @return FileData with metadata populated; caller checks isGCS to decide
 *         whether to add content to the parallel write map
 */
private FileData createRawFileMetadata(Key<ProjectData> projectKey,
    FileData.RoleEnum role, String userId, String fileName, byte[] content)
```

During the transaction, call `createRawFileMetadata()` for each file. If `isTrue(fd.isGCS)`, add the content bytes to a `Map<String, byte[]>` keyed by `gcsName`. The existing `createRawFile()` is preserved as a thin wrapper that calls `createRawFileMetadata()` + `writeGcsFile()` (used by `uploadRawFile` which remains unchanged).

**Phase 2 — Parallel GCS writes (outside transaction):**
After `runJobWithRetries` returns (between the first job at line 558 and the second job at line 569), submit all GCS writes as tasks to an executor (max 10 threads). Wait for completion with `Future.get(30, TimeUnit.SECONDS)` timeout per task.

On failure, the existing rollback `catch` block deletes any GCS files from `addedFiles`. Note: some entries may reference GCS files that were never written (if the parallel write failed before reaching them) — the existing `catch (IOException ee)` in the rollback loop handles this gracefully by logging a warning.

This approach is actually safer than the current code: previously, GCS writes happened inside the transaction callback, so transaction retries could orphan GCS files. Now GCS writes only execute after the transaction succeeds.

The catch block must be widened from `catch (ObjectifyException e)` to `catch (ObjectifyException | IOException e)` to handle GCS write failures.

**Refactoring:** Extract GCS write helper:

```java
/**
 * Writes a single file to GCS.
 * Thread-safe — can be called concurrently from multiple threads.
 */
private void writeGcsFile(String gcsName, byte[] content) throws IOException
```

### Bonus: FileImporterImpl buffer fix

Replace the 1-byte read buffer in `FileImporterImpl.importFile()` with an 8KB buffer and move the size check inside the read loop for early abort on oversized uploads.

## Threading Constraints

- **Runtime:** Java 17 gen2 with `<app-engine-apis>true</app-engine-apis>` (legacy bundled services)
- **ThreadManager required:** Threads calling `GcsService` must be created via `ThreadManager.currentRequestThreadFactory()`
- **Limit:** 50 request-scoped threads per request; we use max 20 (export) or 10 (import), leaving headroom for the request thread and other work
- **Thread counts rationale:** Export reads are blocking I/O with 2 round-trips each (higher benefit from parallelism); import writes are 1 round-trip each. 20/10 keeps us well under the 50-thread limit.
- **Executor lifecycle:** Created at start of parallel section, `shutdownNow()` in `finally` block. Request-scoped, not stored as a field.
- **Dev server compatibility:** `ThreadManager` is available in the dev server environment when `<app-engine-apis>true</app-engine-apis>` is set. The parallel code path does not need a production gate.

## Thread Safety

- `GcsService` is HTTP-based and stateless per call — safe for concurrent use
- `ConcurrentHashMap` for shared result collection
- `ZipOutputStream` writing remains single-threaded

## Error Handling

- `Future.get(30, TimeUnit.SECONDS)` with timeout to prevent indefinite blocking on hung GCS reads
- `ExecutionException`: unwrap cause and rethrow as `IOException`
- `TimeoutException`: cancel the future, throw `IOException`
- `InterruptedException`: restore interrupt flag via `Thread.currentThread().interrupt()`, throw `IOException`
- On import failure, existing rollback loop deletes written GCS files (tolerates deleting non-existent objects)
- GCS read retry logic (5 attempts for NPE) preserved inside `readGcsFile()`; `fatalError=false` returns empty `byte[]` on permanent failure

## Memory Impact

For a project with N GCS files averaging M bytes, peak memory increases by approximately N×M bytes during the parallel fetch phase (all file contents held simultaneously in the `ConcurrentHashMap`). In practice this is similar to the final ZIP buffer size and acceptable for typical projects.

## Files Modified

1. **`ObjectifyStorageIo.java`** — Add `readGcsFile()`, `writeGcsFile()`, `createRawFileMetadata()` helpers. Modify `exportProjectSourceZip` and `createProject` to use parallel executors. Refactor `downloadRawFile` to use `readGcsFile()` helper.
2. **`FileImporterImpl.java`** — Fix 1-byte read buffer to 8KB, early size-limit abort.

## Files NOT Modified

- `StorageIo.java` interface — no signature changes
- `uploadRawFile` — single-file operation, uses `createRawFile()` wrapper unchanged
- Blobstore/Datastore read paths — no GCS I/O involved

## Reference

Pattern validated by Kodular commits:
- `f47c919` — Parallel GCS Export
- `610f5cd` — Parallel filesystem writes during project import
