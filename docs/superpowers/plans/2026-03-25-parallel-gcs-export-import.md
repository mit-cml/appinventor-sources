# Parallel GCS Export/Import Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Parallelize GCS reads during project export and GCS writes during project import to reduce latency from O(N × RTT) to O(RTT).

**Architecture:** Extract `readGcsFile()` and `writeGcsFile()` helpers from inline GCS I/O code. Use `ExecutorService` with `ThreadManager.currentRequestThreadFactory()` to run GCS operations concurrently, then feed results into the existing sequential ZIP/metadata flows.

**Tech Stack:** Java 17, App Engine bundled services, GcsService, Objectify, ThreadManager, java.util.concurrent

**Spec:** `docs/superpowers/specs/2026-03-25-parallel-gcs-export-import-design.md`

---

## File Map

| File | Action | Responsibility |
|------|--------|---------------|
| `appinventor/appengine/src/com/google/appinventor/server/storage/ObjectifyStorageIo.java` | Modify | Add helpers, parallelize export and import |
| `appinventor/appengine/src/com/google/appinventor/server/FileImporterImpl.java` | Modify | Fix 1-byte read buffer |

---

### Task 1: Extract `readGcsFile()` helper

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/storage/ObjectifyStorageIo.java`

- [ ] **Step 1: Add the `readGcsFile` method**

Add this method after the `useGCSforFile` method (around line 1830). The `role` parameter ensures the correct GCS bucket is used (`GCS_BUCKET_NAME` for SOURCE, `APK_BUCKET_NAME` for TARGET via `getGcsBucketToUse`):

```java
/**
 * Reads a single file from GCS with retry logic for transient NPE failures.
 * Thread-safe — can be called concurrently from multiple threads.
 *
 * @param role       the file role, used to select the GCS bucket
 * @param gcsName    the GCS object name
 * @param fatalError if true, throws IOException when all NPE retries are exhausted;
 *                   if false, returns an empty byte[] on permanent NPE failure
 * @return file content bytes, or empty byte[] on non-fatal permanent failure
 */
private byte[] readGcsFile(FileData.RoleEnum role, String gcsName, boolean fatalError) throws IOException {
  int count;
  boolean npfHappened = false;
  boolean recovered = false;
  byte[] data = null;
  for (count = 0; count < 5; count++) {
    GcsFilename gcsFileName = new GcsFilename(getGcsBucketToUse(role), gcsName);
    int bytesRead = 0;
    int fileSize = 0;
    ByteBuffer resultBuffer;
    try {
      fileSize = (int) gcsService.getMetadata(gcsFileName).getLength();
      resultBuffer = ByteBuffer.allocate(fileSize);
      GcsInputChannel readChannel = gcsService.openReadChannel(gcsFileName, 0);
      try {
        while (bytesRead < fileSize) {
          bytesRead += readChannel.read(resultBuffer);
          if (bytesRead < fileSize) {
            if (DEBUG) {
              LOG.log(Level.INFO, "readChannel: bytesRead = " + bytesRead + " fileSize = " + fileSize);
            }
          }
        }
        recovered = true;
        data = resultBuffer.array();
        break;
      } finally {
        readChannel.close();
      }
    } catch (NullPointerException e) {
      LOG.log(Level.WARNING, "readGcsFile: NPF recorded for " + gcsName);
      npfHappened = true;
      resultBuffer = ByteBuffer.allocate(0);
      data = resultBuffer.array();
    }
  }
  if (npfHappened) {
    if (recovered) {
      LOG.log(Level.WARNING, "recovered from NPF in readGcsFile filename = " + gcsName
          + " count = " + count);
    } else {
      LOG.log(Level.WARNING, "FATAL NPF in readGcsFile filename = " + gcsName);
      if (fatalError) {
        throw new IOException("FATAL Error reading file from GCS filename = " + gcsName);
      }
    }
  }
  return data;
}
```

- [ ] **Step 2: Refactor `downloadRawFile` to use the helper**

In `downloadRawFile`, replace the entire `if (isTrue(fileData.isGCS))` block (lines 1724-1778) with:

```java
      if (isTrue(fileData.isGCS)) {
        try {
          result.t = readGcsFile(fileData.role, fileData.gcsName, false);
        } catch (IOException e) {
          throw CrashReport.createAndLogError(LOG, null,
              collectProjectErrorInfo(userId, projectId, fileName), e);
        }
```

The rest of the method (`} else if (fileData.isBlob) {` etc.) remains unchanged.

- [ ] **Step 3: Run tests to verify no regression**

Run: `cd appinventor && ant AiServerLibTests`
Expected: All 21 tests pass, including `testExportProjectZip` and `testExportProjectZipNoSCM`.

- [ ] **Step 4: Commit**

```
Extract readGcsFile() helper and refactor downloadRawFile to use it
```

---

### Task 2: Extract `writeGcsFile()` and `createRawFileMetadata()` helpers

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/storage/ObjectifyStorageIo.java`

- [ ] **Step 1: Add the `writeGcsFile` method**

Add near `readGcsFile`. Takes `role` parameter for bucket selection, matching the pattern in `readGcsFile`:

```java
/**
 * Writes a single file to GCS.
 * Thread-safe — can be called concurrently from multiple threads.
 */
private void writeGcsFile(FileData.RoleEnum role, String gcsName, byte[] content) throws IOException {
  GcsOutputChannel outputChannel =
      gcsService.createOrReplace(new GcsFilename(getGcsBucketToUse(role), gcsName), GcsFileOptions.getDefaultInstance());
  outputChannel.write(ByteBuffer.wrap(content));
  outputChannel.close();
}
```

- [ ] **Step 2: Add the `createRawFileMetadata` method**

Add directly above the existing `createRawFile` method (line 606):

```java
/**
 * Creates a FileData object with metadata only — no GCS I/O.
 * Calls useGCSforFile() to determine storage location. For GCS files,
 * sets isGCS=true and gcsName but does NOT write content to GCS.
 * For inline files, stores content directly in FileData.content.
 * Caller checks isGCS to decide whether to schedule a parallel GCS write.
 */
private FileData createRawFileMetadata(Key<ProjectData> projectKey, FileData.RoleEnum role,
    String userId, String fileName, byte[] content) throws ObjectifyException {
  validateGCS();
  FileData file = new FileData();
  file.fileName = fileName;
  file.projectKey = projectKey;
  file.role = role;
  file.userId = userId;
  if (useGCSforFile(fileName, content.length)) {
    file.isGCS = true;
    file.gcsName = makeGCSfileName(fileName, projectKey.getId());
  } else {
    file.content = content;
  }
  return file;
}
```

- [ ] **Step 3: Refactor `createRawFile` to delegate**

Replace the existing `createRawFile` method body (lines 606-625) with:

```java
private FileData createRawFile(Key<ProjectData> projectKey, FileData.RoleEnum role,
    String userId, String fileName, byte[] content) throws ObjectifyException, IOException {
  FileData file = createRawFileMetadata(projectKey, role, userId, fileName, content);
  if (isTrue(file.isGCS)) {
    writeGcsFile(file.role, file.gcsName, content);
  }
  return file;
}
```

- [ ] **Step 4: Run tests**

Run: `cd appinventor && ant AiServerLibTests`
Expected: All tests pass. The refactoring is purely structural — `createRawFile` still does exactly the same work.

- [ ] **Step 5: Commit**

```
Extract writeGcsFile() and createRawFileMetadata() helpers
```

---

### Task 3: Parallelize GCS reads in `exportProjectSourceZip`

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/storage/ObjectifyStorageIo.java`

- [ ] **Step 1: Add imports and constants**

Add these imports at the top of the file (after the existing `java.util` imports, around line 108):

```java
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.google.appengine.api.ThreadManager;
```

Add constant after `TWENTYFOURHOURS` (line 143):

```java
private static final int EXPORT_PARALLEL_GCS_READS = 20;
```

- [ ] **Step 2: Add parallel GCS prefetch before the file loop**

In `exportProjectSourceZip`, between the `runJobWithRetries` return (line 1931) and the `for (FileData fd : fileData)` loop (line 1938), insert:

```java
      // Prefetch GCS file contents in parallel to reduce export latency.
      // Each GCS object requires metadata + channel read HTTP round-trips,
      // so reading N assets sequentially takes N × RTT. Parallel reads overlap these.
      Map<String, byte[]> gcsContents = new ConcurrentHashMap<>();
      List<FileData> gcsFiles = new ArrayList<>();
      for (FileData fd : fileData) {
        if (isTrue(fd.isGCS)) {
          gcsFiles.add(fd);
        }
      }
      if (!gcsFiles.isEmpty()) {
        int parallelism = Math.min(gcsFiles.size(), EXPORT_PARALLEL_GCS_READS);
        ExecutorService gcsPool = Executors.newFixedThreadPool(parallelism,
            ThreadManager.currentRequestThreadFactory());
        try {
          List<Future<?>> gcsFutures = new ArrayList<>();
          for (FileData fd : gcsFiles) {
            gcsFutures.add(gcsPool.submit(() -> {
              try {
                byte[] data = readGcsFile(fd.role, fd.gcsName, fatalError);
                gcsContents.put(fd.fileName, data);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            }));
          }
          for (Future<?> f : gcsFutures) {
            try {
              f.get(30, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
              f.cancel(true);
              throw new IOException("GCS read timed out", e);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              throw new IOException("GCS read interrupted", e);
            } catch (Exception e) {
              Throwable cause = e.getCause();
              if (cause instanceof RuntimeException && cause.getCause() instanceof IOException) {
                throw (IOException) cause.getCause();
              }
              if (cause instanceof IOException) {
                throw (IOException) cause;
              }
              throw new IOException("GCS parallel read failed: " + e.getMessage(), e);
            }
          }
        } finally {
          gcsPool.shutdownNow();
        }
      }
```

- [ ] **Step 3: Replace the inline GCS read in the file loop**

In the `for (FileData fd : fileData)` loop, replace the entire `} else if (isTrue(fd.isGCS)) {` block (lines 1951-2007) with:

```java
        } else if (isTrue(fd.isGCS)) {
          data = gcsContents.get(fd.fileName);
```

The existing `if (data == null) { data = new byte[0]; }` fallback at line 2023 handles the case where a GCS file was not populated in the map.

- [ ] **Step 4: Run tests**

Run: `cd appinventor && ant AiServerLibTests`
Expected: All tests pass, including `testExportProjectZip` and `testExportProjectZipNoSCM`.

If tests fail because `ThreadManager.currentRequestThreadFactory()` is unavailable in the test environment, proceed to Task 6 early to add the fallback, then return here.

- [ ] **Step 5: Commit**

```
Parallelize GCS reads in exportProjectSourceZip (up to 20 threads)
```

---

### Task 4: Parallelize GCS writes in `createProject`

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/storage/ObjectifyStorageIo.java`

- [ ] **Step 1: Add constant**

Add after `EXPORT_PARALLEL_GCS_READS`:

```java
private static final int IMPORT_PARALLEL_GCS_WRITES = 10;
```

- [ ] **Step 2: Add `gcsFileContents` map at method scope and modify the transaction callback**

In `createProject`, add a method-scoped map alongside `addedFiles` (line 505):

```java
    final Map<String, byte[]> gcsFileContents = new ConcurrentHashMap<>();
```

Then replace the file creation loops inside the transaction callback (lines 533-550) with metadata-only creation. The `gcsFileContents` map must be at method scope (not inside the anonymous class) so it's accessible after the transaction:

```java
          Key<ProjectData> projectKey = projectKey(projectId.t);
          for (TextFile file : project.getSourceFiles()) {
            try {
              byte[] contentBytes = file.getContent().getBytes(DEFAULT_ENCODING);
              FileData fd = createRawFileMetadata(projectKey, FileData.RoleEnum.SOURCE, userId,
                  file.getFileName(), contentBytes);
              addedFiles.add(fd);
              if (isTrue(fd.isGCS)) {
                gcsFileContents.put(fd.gcsName, contentBytes);
              }
            } catch (IOException e) {
              throw CrashReport.createAndLogError(LOG, null,
                  collectProjectErrorInfo(userId, projectId.t, file.getFileName()), e);
            }
          }
          for (RawFile file : project.getRawSourceFiles()) {
            FileData fd = createRawFileMetadata(projectKey, FileData.RoleEnum.SOURCE, userId,
                file.getFileName(), file.getContent());
            addedFiles.add(fd);
            if (isTrue(fd.isGCS)) {
              gcsFileContents.put(fd.gcsName, file.getContent());
            }
          }
          datastore.put(addedFiles);  // batch put
```

- [ ] **Step 3: Add parallel GCS writes between the two `runJobWithRetries` calls**

After the first `runJobWithRetries` returns (line 558, the `Server.isProductionServer()` call) and before the second one (line 569, the UserProjectData write), insert:

```java
      // Write GCS files in parallel outside the transaction.
      // This is safer than the previous approach where GCS writes happened
      // inside the transaction callback — transaction retries could orphan GCS files.
      if (!gcsFileContents.isEmpty()) {
        int parallelism = Math.min(gcsFileContents.size(), IMPORT_PARALLEL_GCS_WRITES);
        ExecutorService gcsPool = Executors.newFixedThreadPool(parallelism,
            ThreadManager.currentRequestThreadFactory());
        try {
          List<Future<?>> gcsFutures = new ArrayList<>();
          for (Map.Entry<String, byte[]> entry : gcsFileContents.entrySet()) {
            gcsFutures.add(gcsPool.submit(() -> {
              try {
                writeGcsFile(FileData.RoleEnum.SOURCE, entry.getKey(), entry.getValue());
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            }));
          }
          for (Future<?> f : gcsFutures) {
            try {
              f.get(30, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
              f.cancel(true);
              throw new IOException("GCS write timed out", e);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              throw new IOException("GCS write interrupted", e);
            } catch (Exception e) {
              Throwable cause = e.getCause();
              if (cause instanceof RuntimeException && cause.getCause() instanceof IOException) {
                throw (IOException) cause.getCause();
              }
              if (cause instanceof IOException) {
                throw (IOException) cause;
              }
              throw new IOException("GCS parallel write failed: " + e.getMessage(), e);
            }
          }
        } finally {
          gcsPool.shutdownNow();
        }
      }
```

- [ ] **Step 4: Widen the catch block and fix brace alignment**

Replace the existing catch/rollback block (lines 580-597) with properly aligned code that also catches `IOException` from the parallel GCS writes:

```java
    } catch (ObjectifyException | IOException e) {
      for (FileData addedFile : addedFiles) {
        if (isTrue(addedFile.isGCS)) {
          if (addedFile.gcsName != null) {
            try {
              gcsService.delete(new GcsFilename(getGcsBucketToUse(addedFile.role), addedFile.gcsName));
            } catch (IOException ee) {
              LOG.log(Level.WARNING, "Unable to delete " + addedFile.gcsName +
                  " from GCS while aborting project creation.", ee);
            }
          }
        }
      }
      addedFiles.clear();
      throw CrashReport.createAndLogError(LOG, null,
          collectUserProjectErrorInfo(userId, projectId.t), e);
    }
```

Note: Some `addedFiles` entries may reference GCS files that were never written (if the parallel write failed partway). The `gcsService.delete()` call tolerates this — the existing `catch (IOException ee)` logs a warning and continues.

- [ ] **Step 5: Run tests**

Run: `cd appinventor && ant AiServerLibTests`
Expected: All tests pass, including `testCreateProjectSuccessful`, `testCreateProjectFailFirst`, `testCreateProjectFailSecond`.

- [ ] **Step 6: Commit**

```
Parallelize GCS writes in createProject (up to 10 threads)
```

---

### Task 5: Fix FileImporterImpl 1-byte read buffer

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/FileImporterImpl.java:182-201`

- [ ] **Step 1: Replace the read loop**

Remove the `BufferedOutputStream bos` declaration (line 182) and replace lines 184-199 (the comment, buffer declaration, read loop, and `bos.flush()`) with:

```java
    int bytesRead;
    long fileLength = 0;
    byte[] buffer = new byte[8192];
    while ((bytesRead = bis.read(buffer, 0, buffer.length)) != -1) {
      fileLength += bytesRead;
      if (fileLength > maxSizeBytes) {
        throw new FileImporterException(UploadResponse.Status.FILE_TOO_LARGE);
      }
      os.write(buffer, 0, bytesRead);
    }
```

The old code drained the rest of the input stream before throwing `FILE_TOO_LARGE`. This is unnecessary — the servlet container closes the connection on error, and draining a potentially huge stream wastes time and memory.

- [ ] **Step 2: Run tests**

Run: `cd appinventor && ant AiServerLibTests`
Expected: All tests pass.

- [ ] **Step 3: Commit**

```
Fix FileImporterImpl 1-byte read buffer to 8KB with early size abort
```

---

### Task 6: Handle ThreadManager fallback for tests/dev server

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/storage/ObjectifyStorageIo.java` (if needed)

This task is conditional — only needed if `ThreadManager.currentRequestThreadFactory()` fails in the test environment.

- [ ] **Step 1: Run full test suite**

Run: `cd appinventor && ant AiServerLibTests`
Expected: All tests pass.

- [ ] **Step 2: If ThreadManager fails in tests, add fallback**

If tests fail because `ThreadManager.currentRequestThreadFactory()` returns null or throws, add a helper method:

```java
private static java.util.concurrent.ThreadFactory getThreadFactory() {
  try {
    java.util.concurrent.ThreadFactory tf = ThreadManager.currentRequestThreadFactory();
    if (tf != null) {
      return tf;
    }
  } catch (Exception e) {
    // Not in App Engine request context (e.g., dev server or tests)
  }
  return Executors.defaultThreadFactory();
}
```

Replace `ThreadManager.currentRequestThreadFactory()` in both Task 3 and Task 4 with `getThreadFactory()`.

- [ ] **Step 3: Run tests again if fallback was added**

Run: `cd appinventor && ant AiServerLibTests`
Expected: All tests pass.

- [ ] **Step 4: Commit if fallback was needed**

```
Add ThreadManager fallback for dev server and test environments
```
