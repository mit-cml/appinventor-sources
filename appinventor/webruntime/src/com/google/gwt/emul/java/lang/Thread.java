package java.lang;

public class Thread {
  public interface UncaughtExceptionHandler {
    void uncaughtException(Thread thread, Throwable throwable);
  }

  public static void setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler handler) {
  }
}
