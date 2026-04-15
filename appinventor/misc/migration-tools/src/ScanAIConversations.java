import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Read-only scan of AI-agent conversation data in Datastore, used to plan the
 * multi-conversation backfill.
 *
 * <p>Goal: enumerate every {@code ConversationMessageData} row, group by
 * {@code conversationId}, compute a per-conversation summary
 * (message count, min/max timestamp, whether any message carries a
 * {@code userId} or {@code projectId} property), and cross-reference with
 * existing {@code ConversationData} rows to bucket each conversation:
 *
 * <ul>
 *   <li><b>ALREADY_MIGRATED</b>: a {@code ConversationData} row exists for
 *       this {@code conversationId}. Nothing to do.</li>
 *   <li><b>MAPPABLE</b>: no metadata row, but message rows expose a
 *       {@code userId} / {@code projectId} property — a backfill can
 *       create the metadata row directly from this.</li>
 *   <li><b>ORPHAN</b>: no metadata row, and messages don't carry ownership
 *       — these can't be surfaced in the new UI without an external
 *       mapping source. Candidates for purge.</li>
 * </ul>
 *
 * <p>This script writes nothing; it's safe to run against production.
 *
 * <p>Usage:
 * <pre>
 *   ant -f appinventor/misc/migration-tools/build.xml scan-ai-conversations \
 *       -Dproject=your-gcp-project [-Dextra.args='--csv=out.csv --limit=0']
 * </pre>
 *
 * <p>CSV output columns:
 * {@code conversationId,messageCount,minTimestamp,maxTimestamp,userId,projectId,bucket}
 */
public class ScanAIConversations {

  private static final String MESSAGE_KIND = "ConversationMessageData";
  private static final String METADATA_KIND = "ConversationData";
  private static final int QUERY_BATCH = 500;
  private static final String API_BASE = "https://datastore.googleapis.com/v1/projects/";

  private static String accessToken;
  private static String projectId;

  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.err.println("Usage: ScanAIConversations <project-id> [--csv=path] [--limit=N]");
      System.err.println("  project-id: GCP project ID");
      System.err.println("  --csv=path: write per-conversation CSV (omit for stdout summary only)");
      System.err.println("  --limit=N:  scan only the first N message rows (0 = no limit, default)");
      System.exit(1);
    }

    projectId = args[0];
    String csvPath = null;
    long limit = 0;
    for (int i = 1; i < args.length; i++) {
      if (args[i].startsWith("--csv=")) {
        csvPath = args[i].substring("--csv=".length());
      } else if (args[i].startsWith("--limit=")) {
        limit = Long.parseLong(args[i].substring("--limit=".length()));
      }
    }

    accessToken = getGcloudToken();
    System.out.printf("Project: %s | CSV: %s | Limit: %d%n",
        projectId, csvPath == null ? "(summary only)" : csvPath, limit);

    run(csvPath, limit);
  }

  // Per-conversation aggregate collected while streaming messages.
  private static final class ConvStats {
    long messageCount;
    long minTimestamp = Long.MAX_VALUE;
    long maxTimestamp = Long.MIN_VALUE;
    String userId;      // non-null iff any message row carries userId
    String projectId;   // non-null iff any message row carries projectId

    void addMessage(long ts) {
      messageCount++;
      if (ts > 0) {
        if (ts < minTimestamp) minTimestamp = ts;
        if (ts > maxTimestamp) maxTimestamp = ts;
      }
    }
  }

  private static void run(String csvPath, long limit) throws IOException {
    System.out.println();
    System.out.println("=== Phase 1: scanning " + MESSAGE_KIND + " ===");
    Map<String, ConvStats> stats = scanMessages(limit);
    System.out.printf("Unique conversations: %,d%n", stats.size());

    System.out.println();
    System.out.println("=== Phase 2: scanning " + METADATA_KIND + " ===");
    Set<String> migrated = scanMetadata();
    System.out.printf("Existing metadata rows: %,d%n", migrated.size());

    System.out.println();
    System.out.println("=== Phase 3: bucketising ===");
    long already = 0, mappable = 0, orphan = 0;
    long orphanMessages = 0, mappableMessages = 0;
    for (Map.Entry<String, ConvStats> e : stats.entrySet()) {
      ConvStats s = e.getValue();
      if (migrated.contains(e.getKey())) {
        already++;
      } else if (s.userId != null && s.projectId != null) {
        mappable++;
        mappableMessages += s.messageCount;
      } else {
        orphan++;
        orphanMessages += s.messageCount;
      }
    }

    System.out.printf("  ALREADY_MIGRATED: %,d conversations%n", already);
    System.out.printf("  MAPPABLE:         %,d conversations (%,d messages)%n",
        mappable, mappableMessages);
    System.out.printf("  ORPHAN:           %,d conversations (%,d messages)%n",
        orphan, orphanMessages);

    // Metadata rows with no corresponding messages (unusual but worth flagging).
    long metadataOnly = 0;
    for (String cid : migrated) {
      if (!stats.containsKey(cid)) {
        metadataOnly++;
      }
    }
    if (metadataOnly > 0) {
      System.out.printf("  METADATA_ONLY:    %,d (metadata row exists but no messages)%n",
          metadataOnly);
    }

    if (csvPath != null) {
      writeCsv(csvPath, stats, migrated);
      System.out.println("Wrote " + csvPath);
    }
  }

  // ---------- Phase 1: scan ConversationMessageData ----------

  private static Map<String, ConvStats> scanMessages(long limit) {
    Map<String, ConvStats> stats = new HashMap<>();
    long scanned = 0;
    String cursor = null;
    long startTime = System.currentTimeMillis();

    while (true) {
      if (limit > 0 && scanned >= limit) break;

      JSONObject queryObj = new JSONObject();
      queryObj.put("kind", new JSONArray().put(new JSONObject().put("name", MESSAGE_KIND)));
      queryObj.put("limit", QUERY_BATCH);
      if (cursor != null) {
        queryObj.put("startCursor", cursor);
      }

      JSONObject body = new JSONObject().put("query", queryObj);
      JSONObject response = post(API_BASE + projectId + ":runQuery", body);
      JSONObject batch = response.getJSONObject("batch");
      JSONArray results = batch.optJSONArray("entityResults");
      if (results == null || results.length() == 0) break;

      for (int i = 0; i < results.length(); i++) {
        JSONObject entity = results.getJSONObject(i).getJSONObject("entity");
        JSONObject props = entity.optJSONObject("properties");
        if (props == null) continue;

        String convId = stringProp(props, "conversationId");
        if (convId == null) continue;

        ConvStats s = stats.computeIfAbsent(convId, k -> new ConvStats());
        long ts = longProp(props, "timestamp");
        s.addMessage(ts);

        // Opportunistically capture ownership hints — some forks store these
        // on the message row; upstream does not.  First non-null wins.
        if (s.userId == null) {
          String uid = stringProp(props, "userId");
          if (uid != null) s.userId = uid;
        }
        if (s.projectId == null) {
          String pid = stringProp(props, "projectId");
          if (pid != null) s.projectId = pid;
        }
        scanned++;
        if (limit > 0 && scanned >= limit) break;
      }

      if (scanned % 10_000 < QUERY_BATCH) {
        printScanProgress(scanned, stats.size(), startTime);
      }

      String moreResults = batch.optString("moreResults", "");
      cursor = batch.optString("endCursor", null);
      if ("NO_MORE_RESULTS".equals(moreResults) || cursor == null || cursor.isEmpty()) {
        break;
      }
    }

    printScanProgress(scanned, stats.size(), startTime);
    return stats;
  }

  // ---------- Phase 2: scan ConversationData ----------

  private static Set<String> scanMetadata() {
    Set<String> ids = new HashSet<>();
    String cursor = null;

    while (true) {
      JSONObject queryObj = new JSONObject();
      queryObj.put("kind", new JSONArray().put(new JSONObject().put("name", METADATA_KIND)));
      queryObj.put("limit", QUERY_BATCH);
      if (cursor != null) {
        queryObj.put("startCursor", cursor);
      }

      JSONObject body = new JSONObject().put("query", queryObj);
      JSONObject response = post(API_BASE + projectId + ":runQuery", body);
      JSONObject batch = response.getJSONObject("batch");
      JSONArray results = batch.optJSONArray("entityResults");
      if (results == null || results.length() == 0) break;

      for (int i = 0; i < results.length(); i++) {
        JSONObject entity = results.getJSONObject(i).getJSONObject("entity");
        JSONObject props = entity.optJSONObject("properties");
        if (props == null) continue;
        String convId = stringProp(props, "conversationId");
        if (convId != null) ids.add(convId);
      }

      String moreResults = batch.optString("moreResults", "");
      cursor = batch.optString("endCursor", null);
      if ("NO_MORE_RESULTS".equals(moreResults) || cursor == null || cursor.isEmpty()) {
        break;
      }
    }
    return ids;
  }

  // ---------- Phase 3: CSV ----------

  private static void writeCsv(String path, Map<String, ConvStats> stats, Set<String> migrated)
      throws IOException {
    List<Map.Entry<String, ConvStats>> rows = new ArrayList<>(stats.entrySet());
    rows.sort(Comparator.comparingLong(
        (Map.Entry<String, ConvStats> e) -> e.getValue().maxTimestamp).reversed());

    try (PrintWriter pw = new PrintWriter(path, "UTF-8")) {
      pw.println("conversationId,messageCount,minTimestamp,maxTimestamp,userId,projectId,bucket");
      for (Map.Entry<String, ConvStats> e : rows) {
        ConvStats s = e.getValue();
        String bucket;
        if (migrated.contains(e.getKey())) {
          bucket = "ALREADY_MIGRATED";
        } else if (s.userId != null && s.projectId != null) {
          bucket = "MAPPABLE";
        } else {
          bucket = "ORPHAN";
        }
        pw.printf("%s,%d,%d,%d,%s,%s,%s%n",
            csv(e.getKey()),
            s.messageCount,
            s.minTimestamp == Long.MAX_VALUE ? 0 : s.minTimestamp,
            s.maxTimestamp == Long.MIN_VALUE ? 0 : s.maxTimestamp,
            csv(s.userId),
            csv(s.projectId),
            bucket);
      }
    }
  }

  private static String csv(String v) {
    if (v == null) return "";
    if (v.indexOf(',') < 0 && v.indexOf('"') < 0 && v.indexOf('\n') < 0) return v;
    return "\"" + v.replace("\"", "\"\"") + "\"";
  }

  // ---------- Datastore REST helpers ----------

  private static String stringProp(JSONObject props, String name) {
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

  private static void printScanProgress(long scanned, int unique, long startTime) {
    long elapsed = Math.max(1, System.currentTimeMillis() - startTime);
    double rate = scanned * 1000.0 / elapsed;
    System.out.printf("Scanned: %,d messages | Unique convs: %,d | Rate: %.0f/s%n",
        scanned, unique, rate);
  }
}
