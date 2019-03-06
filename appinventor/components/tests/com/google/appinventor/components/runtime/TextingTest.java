package com.google.appinventor.components.runtime;

import android.content.Intent;
import android.provider.Telephony.Sms.Intents;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import com.google.appinventor.components.runtime.shadows.ShadowEventDispatcher;
import com.google.appinventor.components.runtime.util.SmsBroadcastReceiver;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.shadows.ShadowSmsManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Tests for the Texting component
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class TextingTest extends RobolectricTestBase {

  private final static String PHONE = "555-555-5555";

  private Texting texting;

  @Before
  public void setUp() {
    super.setUp();
    texting = new Texting(getForm());
  }

  @Test
  public void testShortSMSReception() {
    texting.onInitialize();
    final String MESSAGE = makeRandomMessage(30);
    SmsBroadcastReceiver receiver = new SmsBroadcastReceiver();
    Intent smsIntent = makeSmsIntent(PHONE, MESSAGE);
    receiver.onReceive(getForm(), smsIntent);
    ShadowEventDispatcher.assertEventFired(texting, "MessageReceived", PHONE, MESSAGE);
  }

  /**
   * Tests that long SMS messages, which will have multiple PDUs, are correctly reconstructed by
   * the SmsBroadcastReceiver before being passed to the Texting.MessageReceived event.
   */
  @Test
  public void testLongSMSReception() {
    texting.onInitialize();
    final String MESSAGE = makeRandomMessage(400);
    SmsBroadcastReceiver receiver = new SmsBroadcastReceiver();
    Intent smsIntent = makeSmsIntent(PHONE, MESSAGE);
    receiver.onReceive(getForm(), smsIntent);
    ShadowEventDispatcher.assertEventFired(texting, "MessageReceived", PHONE, MESSAGE);
  }

  /**
   * Helper function to construct an SMS received intent similar to what the Telephony subsystem
   * will pass on the device.
   * @param phone The phone number to "receive" the text from
   * @param message The contents of the SMS message
   * @return An SMS_RECEIVED_ACTION intent with its pdus extra populated with one or more PDUs
   */
  private static Intent makeSmsIntent(String phone, String message) {
    List<String> parts = ShadowSmsManager.getDefault().divideMessage(message);
    byte[][] messages = new byte[SmsMessage.calculateLength(message, false)[0]][];
    for (int i = 0; i < parts.size(); i++) {
      messages[i] = createFakePdu(phone, parts.get(i));
    }
    Intent i = new Intent(Intents.SMS_RECEIVED_ACTION);
    i.putExtra("pdus", messages);
    return i;
  }

  /**
   * Constructs a PDU simulating an SMS message from {@code phone} with the content {@code message}
   * @param phone The phone number originating the message
   * @param msg The body of the message
   * @return byte array containing the PDU
   * @see <a href="http://twit88.com/home/utility/sms-pdu-encode-decode">SMS PDU decoder</a>
   */
  private static byte[] createFakePdu(String phone, String msg) {
    byte[] pdu = null;
    byte[] scBytes = PhoneNumberUtils.networkPortionToCalledPartyBCD("0000000000");
    byte[] senderBytes = PhoneNumberUtils.networkPortionToCalledPartyBCD(phone);
    int lsmcs = scBytes.length;
    byte[] dateBytes = new byte[7];
    Calendar calendar = new GregorianCalendar();
    dateBytes[0] = reverseByte((byte) calendar.get(Calendar.YEAR));
    dateBytes[1] = reverseByte((byte) (calendar.get(Calendar.MONTH) + 1));
    dateBytes[2] = reverseByte((byte) calendar.get(Calendar.DAY_OF_MONTH));
    dateBytes[3] = reverseByte((byte) calendar.get(Calendar.HOUR_OF_DAY));
    dateBytes[4] = reverseByte((byte) calendar.get(Calendar.MINUTE));
    dateBytes[5] = reverseByte((byte) calendar.get(Calendar.SECOND));
    dateBytes[6] = reverseByte((byte) ((calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / (60 * 1000 * 15)));
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      out.write(lsmcs);        // Length of SMSC information
      out.write(scBytes);      // SMSC type + number
      out.write(4);        // First octet of SMS-DELIVER message
      out.write((byte) 10);    // number of digits in the phone number
      out.write(senderBytes);  // Sender type + number
      out.write(0);        // Protocol identifier
      out.write(0);        // Data encoding scheme (7-bit GSM default)
      out.write(dateBytes);    // Timestamp
      try {
        // We access a private method of Android that handles mapping Unicode into the 7-bit characters of the GSM alphabet.
        // We may need to implement this as part of the test in the future if this private API ever goes away.
        String sReflectedClassName = "com.android.internal.telephony.GsmAlphabet";
        Class cReflectedNFCExtras = Class.forName(sReflectedClassName);
        Method stringToGsm7BitPacked = cReflectedNFCExtras.getMethod(
            "stringToGsm7BitPacked", String.class);
        stringToGsm7BitPacked.setAccessible(true);
        byte[] bodybytes = (byte[]) stringToGsm7BitPacked.invoke(null, msg);
        out.write(bodybytes);
      } catch (Exception e) {
      }

      pdu = out.toByteArray();
    } catch (IOException e) {
    }
    return pdu;
  }

  /**
   * Converts a byte into semi-octet notation for PDU.
   * @param b An octet to convert to a semi-octet
   * @return The semi-octet for {@code b}
   */
  private static byte reverseByte(byte b) {
    return (byte) ((b & 0xF0) >> 4 | (b & 0x0F) << 4);
  }

  /**
   * Creates a random message body containing {@code len} alphabetic characters.
   * @param len The length of the message to construct
   * @return A new message
   */
  private static String makeRandomMessage(int len) {
    StringBuilder sb = new StringBuilder();
    while (len > 0) {
      sb.append(Character.toChars('A' + (int)(Math.random() * 26)));
      len--;
    }
    return sb.toString();
  }
}
