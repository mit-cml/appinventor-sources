import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Backfills {@code ConversationData} metadata rows for every
 * {@code ConversationMessageData} group that has a mapping entry.
 *
 * <p>Required input: a CSV file where each row has (at minimum)
 * columns {@code conversation_id,message}, and the {@code message} value
 * matches the {@code AIDebug} production log format, e.g.
 * {@code processRequest: userId=103437..., projectId=5488703...}.
 * Produce this file from Cloud Logging / BigQuery export, then pass
 * {@code --mapping=path.csv}.
 *
 * <p>Pipeline:
 * <ol>
 *   <li>Parse the CSV → in-memory
 *       {@code Map<conversationId, Mapping(userId, projectId)>}.
 *       Duplicates (repeated log entries for the same conversation) are
 *       deduped; first seen wins.</li>
 *   <li>Paginate {@code ConversationMessageData} → per-conversation
 *       message count + min/max timestamp.</li>
 *   <li>Paginate {@code ConversationData} → skip conversations that
 *       already have a metadata row (idempotent re-runs).</li>
 *   <li>For every remaining conversation with a mapping entry, queue an
 *       {@code insert} mutation. Commit in batches of 500 non-transactional.</li>
 * </ol>
 *
 * <p>Usage:
 * <pre>
 *   ant -f appinventor/misc/migration-tools/build.xml backfill-ai-conversations \
 *       -Dproject=gcp-project -Dextra.args='--mapping=logs.csv [--commit]'
 * </pre>
 *
 * <p>Dry-run is the default; pass {@code --commit} to actually write rows.
 */
public class BackfillAIConversations {

  private static final String MESSAGE_KIND = "ConversationMessageData";
  private static final String METADATA_KIND = "ConversationData";
  private static final int QUERY_BATCH = 500;
  private static final int COMMIT_BATCH = 500;
  private static final String API_BASE = "https://datastore.googleapis.com/v1/projects/";

  // Matches log lines like "processRequest: userId=103437..., projectId=5488..."
  private static final Pattern USER_PROJECT_RX = Pattern.compile(
      "userId=(\\d+)(?:[^,]*)?,\\s*projectId=(\\d+)");

  private static String accessToken;
  private static String projectId;

  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      usage();
      System.exit(1);
    }
    projectId = args[0];
    String mappingPath = null;
    boolean commit = false;

    for (int i = 1; i < args.length; i++) {
      if (args[i].startsWith("--mapping=")) {
        mappingPath = args[i].substring("--mapping=".length());
      } else if ("--commit".equals(args[i])) {
        commit = true;
      } else if ("--help".equals(args[i]) || "-h".equals(args[i])) {
        usage();
        return;
      }
    }

    if (mappingPath == null) {
      System.err.println("--mapping=<path> is required.");
      usage();
      System.exit(1);
    }

    accessToken = getGcloudToken();
    System.out.printf("Project: %s | Mapping: %s | Mode: %s%n",
        projectId, mappingPath, commit ? "COMMIT" : "dry-run");
    run(mappingPath, commit);
  }

  private static void usage() {
    System.err.println("Usage: BackfillAIConversations <project-id> --mapping=<path.csv> [--commit]");
    System.err.println();
    System.err.println("  --mapping=path  CSV with columns conversation_id,message where");
    System.err.println("                  message contains 'userId=N, projectId=N'.");
    System.err.println("  --commit        Actually write to Datastore (default: dry-run).");
  }

  // ---------- Mapping file ----------

  private static final class Mapping {
    final String userId;
    final long projectId;

    Mapping(String userId, long projectId) {
      this.userId = userId;
      this.projectId = projectId;
    }
  }

  /**
   * Parses a CSV where the first line is a header and subsequent lines
   * contain at least {@code conversation_id} and {@code message} columns.
   * Accepts quoted fields with embedded commas. First mapping per
   * conversationId wins.
   */
  private static Map<String, Mapping> loadMapping(String path) throws IOException {
    Map<String, Mapping> out = new HashMap<>();
    int total = 0, parsed = 0, noMatch = 0;

    try (BufferedReader r = new BufferedReader(new FileReader(path))) {
      String header = r.readLine();
      if (header == null) {
        throw new IOException("Empty CSV: " + path);
      }
      List<String> cols = parseCsvRow(header);
      int idxConv = cols.indexOf("conversation_id");
      int idxMsg = cols.indexOf("message");
      if (idxConv < 0) {
        idxConv = cols.indexOf("conversationId");
      }
      if (idxConv < 0 || idxMsg < 0) {
        throw new IOException("CSV must have 'conversation_id' and 'message' columns; got: "
            + cols);
      }

      String line;
      while ((line = r.readLine()) != null) {
        if (line.isEmpty()) continue;
        total++;
        List<String> row = parseCsvRow(line);
        if (row.size() <= Math.max(idxConv, idxMsg)) continue;
        String convId = row.get(idxConv);
        String msg = row.get(idxMsg);
        if (convId == null || convId.isEmpty()) continue;
        if (out.containsKey(convId)) continue; // first-seen wins

        Matcher m = USER_PROJECT_RX.matcher(msg);
        if (!m.find()) {
          noMatch++;
          continue;
        }
        String uid = m.group(1);
        long pid;
        try {
          pid = Long.parseLong(m.group(2));
        } catch (NumberFormatException e) {
          noMatch++;
          continue;
        }
        out.put(convId, new Mapping(uid, pid));
        parsed++;
      }
    }

    System.out.printf("Mapping file: %,d rows read | %,d unique convs mapped | %,d unmatched%n",
        total, parsed, noMatch);
    return out;
  }

  /** Tiny RFC-4180-ish CSV splitter handling double-quoted fields. */
  private static List<String> parseCsvRow(String line) {
    List<String> out = new ArrayList<>();
    StringBuilder cur = new StringBuilder();
    boolean inQuotes = false;
    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      if (inQuotes) {
        if (c == '"') {
          if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
            cur.append('"');
            i++;
          } else {
            inQuotes = false;
          }
        } else {
          cur.append(c);
        }
      } else {
        if (c == ',') {
          out.add(cur.toString());
          cur.setLength(0);
        } else if (c == '"') {
          inQuotes = true;
        } else {
          cur.append(c);
        }
      }
    }
    out.add(cur.toString());
    return out;
  }

  // ---------- Datastore scans ----------

  private static final class Stats {
    long count;
    long minTs = Long.MAX_VALUE;
    long maxTs = Long.MIN_VALUE;

    void add(long ts) {
      count++;
      if (ts > 0) {
        if (ts < minTs) minTs = ts;
        if (ts > maxTs) maxTs = ts;
      }
    }
  }

  private static Map<String, Stats> scanMessages() {
    Map<String, Stats> out = new HashMap<>();
    String cursor = null;
    long startTime = System.currentTimeMillis();
    long scanned = 0;

    while (true) {
      JSONObject q = new JSONObject();
      q.put("kind", new JSONArray().put(new JSONObject().put("name", MESSAGE_KIND)));
      q.put("limit", QUERY_BATCH);
      if (cursor != null) q.put("startCursor", cursor);

      JSONObject resp = post(API_BASE + projectId + ":runQuery",
          new JSONObject().put("query", q));
      JSONObject batch = resp.getJSONObject("batch");
      JSONArray results = batch.optJSONArray("entityResults");
      if (results == null || results.length() == 0) break;

      for (int i = 0; i < results.length(); i++) {
        JSONObject entity = results.getJSONObject(i).getJSONObject("entity");
        JSONObject props = entity.optJSONObject("properties");
        if (props == null) continue;
        String cid = stringProp(props, "conversationId");
        if (cid == null) continue;
        long ts = longProp(props, "timestamp");
        out.computeIfAbsent(cid, k -> new Stats()).add(ts);
        scanned++;
      }

      if (scanned % 10_000 < QUERY_BATCH) {
        printProgress(scanned, out.size(), startTime);
      }

      String more = batch.optString("moreResults", "");
      cursor = batch.optString("endCursor", null);
      if ("NO_MORE_RESULTS".equals(more) || cursor == null || cursor.isEmpty()) break;
    }
    printProgress(scanned, out.size(), startTime);
    return out;
  }

  private static Set<String> scanMetadata() {
    Set<String> ids = new HashSet<>();
    String cursor = null;
    while (true) {
      JSONObject q = new JSONObject();
      q.put("kind", new JSONArray().put(new JSONObject().put("name", METADATA_KIND)));
      q.put("limit", QUERY_BATCH);
      if (cursor != null) q.put("startCursor", cursor);
      JSONObject resp = post(API_BASE + projectId + ":runQuery",
          new JSONObject().put("query", q));
      JSONObject batch = resp.getJSONObject("batch");
      JSONArray results = batch.optJSONArray("entityResults");
      if (results == null || results.length() == 0) break;
      for (int i = 0; i < results.length(); i++) {
        JSONObject entity = results.getJSONObject(i).getJSONObject("entity");
        JSONObject props = entity.optJSONObject("properties");
        if (props == null) continue;
        String cid = stringProp(props, "conversationId");
        if (cid != null) ids.add(cid);
      }
      String more = batch.optString("moreResults", "");
      cursor = batch.optString("endCursor", null);
      if ("NO_MORE_RESULTS".equals(more) || cursor == null || cursor.isEmpty()) break;
    }
    return ids;
  }

  // ---------- Main pipeline ----------

  private static void run(String mappingPath, boolean commit) throws IOException {
    long t0 = System.currentTimeMillis();

    System.out.println();
    System.out.println("=== Step 1: loading mapping CSV ===");
    Map<String, Mapping> mapping = loadMapping(mappingPath);

    System.out.println();
    System.out.println("=== Step 2: scanning " + MESSAGE_KIND + " ===");
    Map<String, Stats> stats = scanMessages();
    System.out.printf("Unique conversations in Datastore: %,d%n", stats.size());

    System.out.println();
    System.out.println("=== Step 3: scanning " + METADATA_KIND + " ===");
    Set<String> migrated = scanMetadata();
    System.out.printf("Existing metadata rows: %,d%n", migrated.size());

    System.out.println();
    System.out.println("=== Step 4: planning inserts ===");
    List<JSONObject> queued = new ArrayList<>();
    int alreadyMigrated = 0;
    int noMapping = 0;
    int toInsert = 0;
    List<String> noMappingIds = new ArrayList<>();

    for (Map.Entry<String, Stats> e : stats.entrySet()) {
      String convId = e.getKey();
      Stats s = e.getValue();
      if (migrated.contains(convId)) {
        alreadyMigrated++;
        continue;
      }
      Mapping m = mapping.get(convId);
      if (m == null) {
        noMapping++;
        if (noMappingIds.size() < 20) noMappingIds.add(convId);
        continue;
      }
      long created = s.minTs == Long.MAX_VALUE ? 0 : s.minTs;
      long updated = s.maxTs == Long.MIN_VALUE ? created : s.maxTs;
      queued.add(buildInsertMutation(convId, m.userId, m.projectId, created, updated));
      toInsert++;
    }

    System.out.printf("  ALREADY_MIGRATED: %,d%n", alreadyMigrated);
    System.out.printf("  NO_MAPPING:       %,d%n", noMapping);
    System.out.printf("  TO_INSERT:        %,d%n", toInsert);
    if (!noMappingIds.isEmpty()) {
      System.out.println("  (first up to 20 NO_MAPPING conversationIds:)");
      for (String id : noMappingIds) {
        System.out.println("    " + id);
      }
    }

    System.out.println();
    if (commit && toInsert > 0) {
      System.out.println("=== Step 5: committing inserts ===");
      commitBatches(queued);
    } else if (!commit && toInsert > 0) {
      System.out.println("*** DRY RUN — no inserts applied. Re-run with --commit to write. ***");
    } else {
      System.out.println("Nothing to insert.");
    }

    long elapsed = System.currentTimeMillis() - t0;
    System.out.printf("Done in %.1fs.%n", elapsed / 1000.0);
  }

  // ---------- Mutation building ----------

  /**
   * Builds a ConversationData insert. Matches {@code StoredData.ConversationData}:
   * {@code conversationId}, {@code projectId}, {@code userId} are indexed;
   * {@code title}, {@code createdAt}, {@code updatedAt} are not.
   */
  private static JSONObject buildInsertMutation(String conversationId, String userId,
      long projectIdNumeric, long createdAt, long updatedAt) {
    JSONObject entity = new JSONObject();

    // Incomplete key — Datastore will allocate an id on commit.
    entity.put("key", new JSONObject()
        .put("path", new JSONArray()
            .put(new JSONObject().put("kind", METADATA_KIND))));

    JSONObject props = new JSONObject();
    // Indexed fields (no excludeFromIndexes).
    props.put("conversationId", new JSONObject().put("stringValue", conversationId));
    props.put("userId", new JSONObject().put("stringValue", userId));
    props.put("projectId", new JSONObject()
        .put("integerValue", Long.toString(projectIdNumeric)));
    // Unindexed fields.
    props.put("title", new JSONObject()
        .put("nullValue", JSONObject.NULL)
        .put("excludeFromIndexes", true));
    props.put("createdAt", new JSONObject()
        .put("integerValue", Long.toString(createdAt))
        .put("excludeFromIndexes", true));
    props.put("updatedAt", new JSONObject()
        .put("integerValue", Long.toString(updatedAt))
        .put("excludeFromIndexes", true));
    entity.put("properties", props);

    return new JSONObject().put("insert", entity);
  }

  private static void commitBatches(List<JSONObject> mutations) {
    int total = mutations.size();
    int committed = 0;
    for (int start = 0; start < total; start += COMMIT_BATCH) {
      int end = Math.min(start + COMMIT_BATCH, total);
      List<JSONObject> slice = mutations.subList(start, end);
      JSONArray arr = new JSONArray();
      for (JSONObject m : slice) arr.put(m);
      JSONObject body = new JSONObject()
          .put("mode", "NON_TRANSACTIONAL")
          .put("mutations", arr);
      try {
        post(API_BASE + projectId + ":commit", body);
        committed += slice.size();
        System.out.printf("Committed %,d / %,d%n", committed, total);
      } catch (Exception e) {
        System.err.printf("Error committing batch [%d..%d): %s%n", start, end, e.getMessage());
      }
    }
  }

  // ---------- REST + CLI helpers ----------

  private static String stringProp(JSONObject props, String name) {
    if (props == null) return null;
    JSONObject p = props.optJSONObject(name);
    if (p == null) return null;
    return p.optString("stringValue", null);
  }

  private static long longProp(JSONObject props, String name) {
    JSONObject p = props.optJSONObject(name);
    if (p == null) return 0L;
    if (p.has("integerValue")) {
      Object v = p.get("integerValue");
      if (v instanceof Number) return ((Number) v).longValue();
      if (v instanceof String) {
        try { return Long.parseLong((String) v); } catch (NumberFormatException e) { return 0L; }
      }
    }
    return 0L;
  }

  private static JSONObject post(String url, JSONObject body) {
    try {
      HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Authorization", "Bearer " + accessToken);
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setDoOutput(true);

      try (OutputStream os = conn.getOutputStream()) {
        os.write(body.toString().getBytes(StandardCharsets.UTF_8));
      }

      int code = conn.getResponseCode();
      BufferedReader reader;
      if (code >= 200 && code < 300) {
        reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
      } else {
        reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        throw new RuntimeException("HTTP " + code + ": " + sb);
      }
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) sb.append(line);
      reader.close();
      return new JSONObject(sb.toString());
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static String getGcloudToken() {
    try {
      Process proc = new ProcessBuilder("gcloud", "auth", "print-access-token")
          .redirectErrorStream(true)
          .start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
      String token = reader.readLine();
      int exit = proc.waitFor();
      if (exit != 0 || token == null || token.isEmpty()) {
        throw new RuntimeException("Failed to get gcloud token. Run: gcloud auth login");
      }
      return token.trim();
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException("gcloud not found. Install Google Cloud SDK and run: gcloud auth login", e);
    }
  }

  private static void printProgress(long scanned, int unique, long startTime) {
    long elapsed = Math.max(1, System.currentTimeMillis() - startTime);
    double rate = scanned * 1000.0 / elapsed;
    System.out.printf("Scanned: %,d messages | Unique convs: %,d | Rate: %.0f/s%n",
        scanned, unique, rate);
  }
}
