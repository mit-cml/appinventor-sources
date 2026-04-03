package com.google.appinventor.server.aiagent;

import com.google.appinventor.common.testutils.TestUtils;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.aiagent.AIStreamStatus;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import junit.framework.TestCase;

public class StreamBufferTest extends TestCase {
  private static final String APPENGINE_GENERATED_DIR = TestUtils.APP_INVENTOR_ROOT_DIR +
      "/appengine/build/tests/appengine-generated";
  private static final long PROJECT_ID = 12345L;

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig()
              .setBackingStoreLocation(APPENGINE_GENERATED_DIR),
          new LocalMemcacheServiceTestConfig());

  private StorageIo storageIo;
  private StreamBuffer buffer;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    System.setProperty("appengine.generated.dir", APPENGINE_GENERATED_DIR);
    helper.setUp();
    storageIo = StorageIoInstanceHolder.getInstance();
    buffer = new StreamBuffer(storageIo, PROJECT_ID);
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    helper.tearDown();
  }

  public void testInitAndConsumeEmpty() {
    buffer.init();
    AIStreamStatus status = buffer.consume();
    assertNull(status.getStatusText());
    assertNull(status.getTextDelta());
    assertFalse(status.isDone());
  }

  public void testAppendTextAndConsume() {
    buffer.init();
    buffer.appendText("Hello ");
    buffer.appendText("world");
    AIStreamStatus status = buffer.consume();
    assertEquals("Hello world", status.getTextDelta());
    assertNull(status.getStatusText());
    assertFalse(status.isDone());
  }

  public void testConsumeIsIncremental() {
    buffer.init();
    buffer.appendText("first");
    AIStreamStatus s1 = buffer.consume();
    assertEquals("first", s1.getTextDelta());

    buffer.appendText("second");
    AIStreamStatus s2 = buffer.consume();
    assertEquals("second", s2.getTextDelta());
  }

  public void testStatusUpdates() {
    buffer.init();
    buffer.appendStatus("Building context...");
    buffer.appendText("token1");
    buffer.appendStatus("Calling AI...");
    AIStreamStatus status = buffer.consume();
    assertEquals("Calling AI...", status.getStatusText());
    assertEquals("token1", status.getTextDelta());
  }

  public void testMarkDone() {
    buffer.init();
    buffer.appendText("final");
    buffer.markDone();
    AIStreamStatus status = buffer.consume();
    assertEquals("final", status.getTextDelta());
    assertTrue(status.isDone());
  }

  public void testClear() {
    buffer.init();
    buffer.appendText("data");
    buffer.clear();
    AIStreamStatus status = buffer.consume();
    assertNull(status.getTextDelta());
    assertFalse(status.isDone());
  }

  public void testInitResetsStaleBuffer() {
    buffer.init();
    buffer.appendText("old data");
    buffer.markDone();
    buffer.init();
    AIStreamStatus status = buffer.consume();
    assertNull(status.getTextDelta());
    assertFalse(status.isDone());
  }

  public void testEmptyTextIgnored() {
    buffer.init();
    buffer.appendText("");
    buffer.appendText(null);
    AIStreamStatus status = buffer.consume();
    assertNull(status.getTextDelta());
  }
}
