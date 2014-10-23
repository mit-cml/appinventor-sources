// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.YailList;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests BluetoothConnectionBase.java.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class BluetoothConnectionBaseTest extends TestCase {
  private BluetoothConnectionBase connection;
  private ByteArrayOutputStream outputStream;
  private int recordedErrorNumber;
  private PipedOutputStream pipe;

  @Override
  protected void setUp() throws Exception {

    outputStream = new ByteArrayOutputStream();
    pipe = new PipedOutputStream();

    connection = new BluetoothConnectionBase(outputStream, new PipedInputStream(pipe)) {
      @Override
      protected void bluetoothError(String functionName, int errorNumber, Object... messageArgs) {
        recordedErrorNumber = errorNumber;
      }
      @Override
      protected void write(String functionName, byte b) {
        super.write(functionName, b);
        try {
          pipe.write(b);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      @Override
      protected void write(String functionName, byte[] bytes) {
        super.write(functionName, bytes);
        try {
          pipe.write(bytes);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  public void testSendAndReceiveText() {
    connection.SendText("Hello");
    assertEquals(5, connection.BytesAvailableToReceive());
    assertEquals("Hello", connection.ReceiveText(5));

    // Test DelimiterByte with default value of 0
    connection.SendText("World");
    connection.Send1ByteNumber("0x0"); // send delimiter
    assertEquals(6, connection.BytesAvailableToReceive());
    assertEquals("World", connection.ReceiveText(-1));

    // Test DelimiterByte with value of 10 (line feed)
    connection.DelimiterByte(10);
    connection.SendText("Hello World");
    connection.Send1ByteNumber("13"); // send carriage return
    connection.Send1ByteNumber("10"); // send line feed
    assertEquals(13, connection.BytesAvailableToReceive());
    assertEquals("Hello World\r", connection.ReceiveText(-1));

    assertEquals(0, recordedErrorNumber);

    byte[] bytes = outputStream.toByteArray();
    assertEquals(24, bytes.length);
    int i = 0;
    assertEquals((byte)  72, bytes[i++]);  // H
    assertEquals((byte) 101, bytes[i++]);  // e
    assertEquals((byte) 108, bytes[i++]);  // l
    assertEquals((byte) 108, bytes[i++]);  // l
    assertEquals((byte) 111, bytes[i++]);  // o
    assertEquals((byte)  87, bytes[i++]);  // W
    assertEquals((byte) 111, bytes[i++]);  // o
    assertEquals((byte) 114, bytes[i++]);  // r
    assertEquals((byte) 108, bytes[i++]);  // l
    assertEquals((byte) 100, bytes[i++]);  // d
    assertEquals((byte)   0, bytes[i++]);  // delimiter 0
    assertEquals((byte)  72, bytes[i++]);  // H
    assertEquals((byte) 101, bytes[i++]);  // e
    assertEquals((byte) 108, bytes[i++]);  // l
    assertEquals((byte) 108, bytes[i++]);  // l
    assertEquals((byte) 111, bytes[i++]);  // o
    assertEquals((byte)  32, bytes[i++]);  // space
    assertEquals((byte)  87, bytes[i++]);  // W
    assertEquals((byte) 111, bytes[i++]);  // o
    assertEquals((byte) 114, bytes[i++]);  // r
    assertEquals((byte) 108, bytes[i++]);  // l
    assertEquals((byte) 100, bytes[i++]);  // d
    assertEquals((byte)  13, bytes[i++]);  // carriage return
    assertEquals((byte)  10, bytes[i++]);  // line feed
  }

  public void testSendandReceive1ByteNumber() {
    connection.Send1ByteNumber("0");
    assertEquals(0, connection.ReceiveUnsigned1ByteNumber());
    connection.Send1ByteNumber("1");
    assertEquals(1, connection.ReceiveUnsigned1ByteNumber());
    connection.Send1ByteNumber("-1");
    assertEquals(-1, connection.ReceiveSigned1ByteNumber());
    connection.Send1ByteNumber("127");
    assertEquals(127, connection.ReceiveUnsigned1ByteNumber());
    connection.Send1ByteNumber("-128");
    assertEquals(-128, connection.ReceiveSigned1ByteNumber());
    connection.Send1ByteNumber("255");
    assertEquals(255, connection.ReceiveUnsigned1ByteNumber());
    connection.Send1ByteNumber("0x0");
    assertEquals(0x0, connection.ReceiveUnsigned1ByteNumber());
    connection.Send1ByteNumber("0x1");
    assertEquals(0x1, connection.ReceiveUnsigned1ByteNumber());
    connection.Send1ByteNumber("0xFF");
    assertEquals(0xFF, connection.ReceiveUnsigned1ByteNumber());
    connection.Send1ByteNumber("0xab");
    assertEquals(0xab, connection.ReceiveUnsigned1ByteNumber());

    assertEquals(0, recordedErrorNumber);

    connection.Send1ByteNumber("abc");
    assertEquals(ErrorMessages.ERROR_BLUETOOTH_COULD_NOT_DECODE, recordedErrorNumber);

    recordedErrorNumber = 0;
    connection.Send1ByteNumber("256");
    assertEquals(ErrorMessages.ERROR_BLUETOOTH_COULD_NOT_FIT_NUMBER_IN_BYTE, recordedErrorNumber);

    byte[] bytes = outputStream.toByteArray();
    assertEquals(10, bytes.length);
    int i = 0;
    assertEquals((byte) 0x00, bytes[i++]);  // 0
    assertEquals((byte) 0x01, bytes[i++]);  // 1
    assertEquals((byte) 0xFF, bytes[i++]);  // -1
    assertEquals((byte) 0x7F, bytes[i++]);  // 127
    assertEquals((byte) 0x80, bytes[i++]);  // -128
    assertEquals((byte) 0xFF, bytes[i++]);  // 255
    assertEquals((byte) 0x00, bytes[i++]);  // 0x00
    assertEquals((byte) 0x01, bytes[i++]);  // 0x01
    assertEquals((byte) 0xFF, bytes[i++]);  // 0xFF
    assertEquals((byte) 0xAB, bytes[i++]);  // 0xab
  }

  public void testSendAndReceive2ByteNumber() {
    connection.HighByteFirst(true);
    connection.Send2ByteNumber("0");
    assertEquals(0, connection.ReceiveUnsigned2ByteNumber());
    connection.Send2ByteNumber("1");
    assertEquals(1, connection.ReceiveUnsigned2ByteNumber());
    connection.Send2ByteNumber("-1");
    assertEquals(-1, connection.ReceiveSigned2ByteNumber());
    connection.Send2ByteNumber("32767");
    assertEquals(32767, connection.ReceiveUnsigned2ByteNumber());
    connection.Send2ByteNumber("-32768");
    assertEquals(-32768, connection.ReceiveSigned2ByteNumber());
    connection.Send2ByteNumber("65535");
    assertEquals(65535, connection.ReceiveUnsigned2ByteNumber());
    connection.Send2ByteNumber("0x0");
    assertEquals(0x0, connection.ReceiveUnsigned2ByteNumber());
    connection.Send2ByteNumber("0x1");
    assertEquals(0x1, connection.ReceiveUnsigned2ByteNumber());
    connection.Send2ByteNumber("0xFFFF");
    assertEquals(0xFFFF, connection.ReceiveUnsigned2ByteNumber());
    connection.Send2ByteNumber("0xabcd");
    assertEquals(0xabcd, connection.ReceiveUnsigned2ByteNumber());

    connection.HighByteFirst(false);
    connection.Send2ByteNumber("0");
    assertEquals(0, connection.ReceiveUnsigned2ByteNumber());
    connection.Send2ByteNumber("1");
    assertEquals(1, connection.ReceiveUnsigned2ByteNumber());
    connection.Send2ByteNumber("-1");
    assertEquals(-1, connection.ReceiveSigned2ByteNumber());
    connection.Send2ByteNumber("32767");
    assertEquals(32767, connection.ReceiveUnsigned2ByteNumber());
    connection.Send2ByteNumber("-32768");
    assertEquals(-32768, connection.ReceiveSigned2ByteNumber());
    connection.Send2ByteNumber("65535");
    assertEquals(65535, connection.ReceiveUnsigned2ByteNumber());
    connection.Send2ByteNumber("0x0");
    assertEquals(0x0, connection.ReceiveUnsigned2ByteNumber());
    connection.Send2ByteNumber("0x1");
    assertEquals(0x1, connection.ReceiveUnsigned2ByteNumber());
    connection.Send2ByteNumber("0xFFFF");
    assertEquals(0xFFFF, connection.ReceiveUnsigned2ByteNumber());
    connection.Send2ByteNumber("0xabcd");
    assertEquals(0xabcd, connection.ReceiveUnsigned2ByteNumber());

    assertEquals(0, recordedErrorNumber);

    connection.Send2ByteNumber("abc");
    assertEquals(ErrorMessages.ERROR_BLUETOOTH_COULD_NOT_DECODE, recordedErrorNumber);

    recordedErrorNumber = 0;
    connection.Send2ByteNumber("65536");
    assertEquals(ErrorMessages.ERROR_BLUETOOTH_COULD_NOT_FIT_NUMBER_IN_BYTES, recordedErrorNumber);

    byte[] bytes = outputStream.toByteArray();
    assertEquals(40, bytes.length);
    int i = 0;
    assertEquals((byte) 0x00, bytes[i++]);  // 0 high byte first
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);  // 1 high byte first
    assertEquals((byte) 0x01, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);  // -1 high byte first
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0x7F, bytes[i++]);  // 32767 high byte first
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0x80, bytes[i++]);  // -32768 high byte first
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);  // 65535 high byte first
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);  // 0x0 high byte first
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);  // 0x1 high byte first
    assertEquals((byte) 0x01, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);  // 0xFFFF high byte first
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0xAB, bytes[i++]);  // 0xabcd high byte first
    assertEquals((byte) 0xCD, bytes[i++]);

    assertEquals((byte) 0x00, bytes[i++]);  // 0 low byte first
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x01, bytes[i++]);  // 1 low byte first
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);  // -1 low byte first
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);  // 32767 low byte first
    assertEquals((byte) 0x7F, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);  // -32768 low byte first
    assertEquals((byte) 0x80, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);  // 65535 low byte first
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);  // 0x0 low byte first
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x01, bytes[i++]);  // 0x1 low byte first
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);  // 0xFFFF low byte first
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0xCD, bytes[i++]);  // 0xabcd low byte first
    assertEquals((byte) 0xAB, bytes[i++]);

  }

  public void testSendAndReceive4ByteNumber() {
    connection.HighByteFirst(true);
    connection.Send4ByteNumber("0");
    assertEquals(0, connection.ReceiveUnsigned4ByteNumber());
    connection.Send4ByteNumber("1");
    assertEquals(1, connection.ReceiveUnsigned4ByteNumber());
    connection.Send4ByteNumber("-1");
    assertEquals(-1, connection.ReceiveSigned4ByteNumber());
    connection.Send4ByteNumber("2147483647");
    assertEquals(2147483647, connection.ReceiveUnsigned4ByteNumber());
    connection.Send4ByteNumber("-2147483648");
    assertEquals(-2147483648, connection.ReceiveSigned4ByteNumber());
    connection.Send4ByteNumber("4294967295");
    assertEquals(4294967295L, connection.ReceiveUnsigned4ByteNumber());
    connection.Send4ByteNumber("0x0");
    assertEquals(0x0, connection.ReceiveUnsigned4ByteNumber());
    connection.Send4ByteNumber("0x1");
    assertEquals(0x1, connection.ReceiveUnsigned4ByteNumber());
    connection.Send4ByteNumber("0xFFFFFFFF");
    assertEquals(0xFFFFFFFFL, connection.ReceiveUnsigned4ByteNumber());
    connection.Send4ByteNumber("0xabcdef");
    assertEquals(0xabcdef, connection.ReceiveUnsigned4ByteNumber());

    connection.HighByteFirst(false);
    connection.Send4ByteNumber("0");
    assertEquals(0, connection.ReceiveUnsigned4ByteNumber());
    connection.Send4ByteNumber("1");
    assertEquals(1, connection.ReceiveUnsigned4ByteNumber());
    connection.Send4ByteNumber("-1");
    assertEquals(-1, connection.ReceiveSigned4ByteNumber());
    connection.Send4ByteNumber("2147483647");
    assertEquals(2147483647, connection.ReceiveUnsigned4ByteNumber());
    connection.Send4ByteNumber("-2147483648");
    assertEquals(-2147483648, connection.ReceiveSigned4ByteNumber());
    connection.Send4ByteNumber("4294967295");
    assertEquals(4294967295L, connection.ReceiveUnsigned4ByteNumber());
    connection.Send4ByteNumber("0x0");
    assertEquals(0x0, connection.ReceiveUnsigned4ByteNumber());
    connection.Send4ByteNumber("0x1");
    assertEquals(0x1, connection.ReceiveUnsigned4ByteNumber());
    connection.Send4ByteNumber("0xFFFFFFFF");
    assertEquals(0xFFFFFFFFL, connection.ReceiveUnsigned4ByteNumber());
    connection.Send4ByteNumber("0xabcdef");
    assertEquals(0xabcdef, connection.ReceiveUnsigned4ByteNumber());

    assertEquals(0, recordedErrorNumber);

    connection.Send4ByteNumber("abc");
    assertEquals(ErrorMessages.ERROR_BLUETOOTH_COULD_NOT_DECODE, recordedErrorNumber);

    recordedErrorNumber = 0;
    connection.Send4ByteNumber("4294967296");
    assertEquals(ErrorMessages.ERROR_BLUETOOTH_COULD_NOT_FIT_NUMBER_IN_BYTES, recordedErrorNumber);

    byte[] bytes = outputStream.toByteArray();
    assertEquals(80, bytes.length);
    int i = 0;
    assertEquals((byte) 0x00, bytes[i++]);  // 0 high byte first
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);  // 1 high byte first
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x01, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);  // -1 high byte first
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0x7F, bytes[i++]);  // 2147483647 high byte first
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0x80, bytes[i++]);  // -2147483648 high byte first
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);  // 4294967295 high byte first
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);  // 0x0 high byte first
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);  // 0x1 high byte first
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x01, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);  // 0xFFFFFFFF high byte first
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);  // 0xabcdef high byte first
    assertEquals((byte) 0xAB, bytes[i++]);
    assertEquals((byte) 0xCD, bytes[i++]);
    assertEquals((byte) 0xEF, bytes[i++]);

    assertEquals((byte) 0x00, bytes[i++]);  // 0 low byte first
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x01, bytes[i++]);  // 1 low byte first
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);  // -1 low byte first
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);  // 2147483647 low byte first
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0x7F, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);  // -2147483648 low byte first
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x80, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);  // 4294967295 low byte first
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);  // 0x0 low byte first
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x01, bytes[i++]);  // 0x1 low byte first
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);  // 0xFFFFFFFF low byte first
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0xFF, bytes[i++]);
    assertEquals((byte) 0xEF, bytes[i++]);  // 0xabcdef low byte first
    assertEquals((byte) 0xCD, bytes[i++]);
    assertEquals((byte) 0xAB, bytes[i++]);
    assertEquals((byte) 0x00, bytes[i++]);
  }

  public void testSendAndReceiveBytes() {
    List<Object> list = new ArrayList<Object>();
    list.add((byte) 0);
    list.add((short) 1);
    list.add(-1);
    list.add((long) 127);
    list.add((byte) -128);
    list.add((short) 255);
    list.add("0x0");
    list.add("0x1");
    list.add("0xFF");
    list.add("0xab");
    connection.SendBytes(YailList.makeList(list));
    assertEquals(0, recordedErrorNumber);
    assertEquals(10, connection.BytesAvailableToReceive());

    List<Integer> signedBytes = connection.ReceiveSignedBytes(5);
    assertEquals(0, signedBytes.get(0).intValue());
    assertEquals(1, signedBytes.get(1).intValue());
    assertEquals(-1, signedBytes.get(2).intValue());
    assertEquals(127, signedBytes.get(3).intValue());
    assertEquals(-128, signedBytes.get(4).intValue());
    List<Integer> unsignedBytes = connection.ReceiveUnsignedBytes(5);
    assertEquals(255, unsignedBytes.get(0).intValue());
    assertEquals(0x0, unsignedBytes.get(1).intValue());
    assertEquals(0x1, unsignedBytes.get(2).intValue());
    assertEquals(0xFF, unsignedBytes.get(3).intValue());
    assertEquals(0xab, unsignedBytes.get(4).intValue());

    assertEquals(0, recordedErrorNumber);

    list = new ArrayList<Object>();
    list.add("abc");
    connection.SendBytes(YailList.makeList(list));
    assertEquals(ErrorMessages.ERROR_BLUETOOTH_COULD_NOT_DECODE_ELEMENT, recordedErrorNumber);

    recordedErrorNumber = 0;
    list = new ArrayList<Object>();
    list.add("256");
    connection.SendBytes(YailList.makeList(list));
    assertEquals(ErrorMessages.ERROR_BLUETOOTH_COULD_NOT_FIT_ELEMENT_IN_BYTE, recordedErrorNumber);

    byte[] bytes = outputStream.toByteArray();
    assertEquals(10, bytes.length);
    int i = 0;
    assertEquals((byte) 0x00, bytes[i++]);  // 0
    assertEquals((byte) 0x01, bytes[i++]);  // 1
    assertEquals((byte) 0xFF, bytes[i++]);  // -1
    assertEquals((byte) 0x7F, bytes[i++]);  // 127
    assertEquals((byte) 0x80, bytes[i++]);  // -128
    assertEquals((byte) 0xFF, bytes[i++]);  // 255
    assertEquals((byte) 0x00, bytes[i++]);  // 0x00
    assertEquals((byte) 0x01, bytes[i++]);  // 0x01
    assertEquals((byte) 0xFF, bytes[i++]);  // 0xFF
    assertEquals((byte) 0xAB, bytes[i++]);  // 0xab
  }
}
