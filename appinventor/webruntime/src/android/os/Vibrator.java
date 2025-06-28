package android.os;

public class Vibrator {
  public void vibrate(long milliseconds) {
    // Simulate vibration for the specified duration
    System.out.println("Vibrating for " + milliseconds + " milliseconds");
  }

  public void cancel() {
    // Simulate stopping vibration
    System.out.println("Vibration cancelled");
  }
}
