// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.UnsupportedEncodingException;

/**
 * The class provides utility functions to encode and decode commands
 * that are sent to or received from LEGO MINDSTORMS EV3 robots.
 *
 * @author jerry73204@gmail.com (jerry73204)
 * @author spaded06543@gmail.com (Alvin Chang)
 */

public class Ev3BinaryParser {
  private static byte PRIMPAR_SHORT = (byte) 0x00;
  private static byte PRIMPAR_LONG  = (byte) 0x80;

  private static byte PRIMPAR_CONST    = (byte) 0x00;
  private static byte PRIMPAR_VARIABEL = (byte) 0x40;
  private static byte PRIMPAR_LOCAL    = (byte) 0x00;
  private static byte PRIMPAR_GLOBAL   = (byte) 0x20;
  private static byte PRIMPAR_HANDLE   = (byte) 0x10;
  private static byte PRIMPAR_ADDR     = (byte) 0x08;

  private static byte PRIMPAR_INDEX      = (byte) 0x1F;
  private static byte PRIMPAR_CONST_SIGN = (byte) 0x20;
  private static byte PRIMPAR_VALUE      = (byte) 0x3F;

  private static byte PRIMPAR_BYTES = (byte) 0x07;

  private static byte PRIMPAR_STRING_OLD = (byte) 0;
  private static byte PRIMPAR_1_BYTE     = (byte) 1;
  private static byte PRIMPAR_2_BYTES    = (byte) 2;
  private static byte PRIMPAR_4_BYTES    = (byte) 3;
  private static byte PRIMPAR_STRING     = (byte) 4;

  private static class FormatLiteral {
    public char symbol;
    public int size;

    public FormatLiteral(char symbol, int size) {
      this.symbol = symbol;
      this.size = size;
    }
  }

  public static byte[] pack(String format, Object... values) throws IllegalArgumentException {
    // parse format string
    String[] formatTokens = format.split("(?<=\\D)");
    FormatLiteral[] literals = new FormatLiteral[formatTokens.length];
    int index = 0;
    int bufferCapacity = 0;

    // calculate buffer size
    for (int i = 0; i < formatTokens.length; i++) {
      String token = formatTokens[i];
      char symbol = token.charAt(token.length() - 1);
      int size = 1;
      boolean sizeSpecified = false;

      if (token.length() != 1)
      {
        size = Integer.parseInt(token.substring(0, token.length() - 1));
        sizeSpecified = true;

        if (size < 1)
          throw new IllegalArgumentException("Illegal format string");
      }

      switch (symbol) {
      case 'x':
        bufferCapacity += size;
        break;

      case 'b':
        bufferCapacity += size;
        index += size;
        break;

      case 'B':
        bufferCapacity += size;
        index++;
        break;

      case 'h':
        bufferCapacity += size * 2;
        index += size;
        break;

      case 'H':
        bufferCapacity += size * 2;
        index++;
        break;

      case 'i':
        bufferCapacity += size * 4;
        index += size;
        break;

      case 'I':
        bufferCapacity += size * 4;
        index++;
        break;

      case 'l':
        bufferCapacity += size * 8;
        index += size;
        break;

      case 'L':
        bufferCapacity += size * 8;
        index++;
        break;

      case 'f':
        bufferCapacity += size * 4;
        index += size;
        break;

      case 'F':
        bufferCapacity += size * 4;
        index++;
        break;

      case 's':
        if (size != ((String) values[index]).length())
          throw new IllegalArgumentException("Illegal format string");

        bufferCapacity += size;
        index++;
        break;

      case 'S':
        if (sizeSpecified)
          throw new IllegalArgumentException("Illegal format string");

        bufferCapacity += ((String) values[index]).length() + 1;
        index++;
        break;

      default:
        throw new IllegalArgumentException("Illegal format string");
      }

      literals[i] = new FormatLiteral(symbol, size);
    }

    if (index != values.length)
      throw new IllegalArgumentException("Illegal format string");

    // generate byte buffer
    index = 0;
    ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);
    buffer.order(ByteOrder.LITTLE_ENDIAN);

    for (FormatLiteral literal: literals) {
      switch (literal.symbol) {
      case 'x':
        for (int i = 0; i < literal.size; i++)
          buffer.put((byte) 0x00);
        break;

      case 'b':
        for (int i = 0; i < literal.size; i++) {
          buffer.put((Byte) values[index]);
          index += 1;
        }
        break;

      case 'B':
        buffer.put((byte[]) values[index]);
        index++;
        break;

      case 'h':
        for (int i = 0; i < literal.size; i++) {
          buffer.putShort((Short) values[index]);
          index += 1;
        }
        break;

      case 'H':
        for (int i = 0; i < literal.size; i++) {
          buffer.putShort(((short[]) values[index])[i]);
        }
        index++;
        break;

      case 'i':
        for (int i = 0; i < literal.size; i++) {
          buffer.putInt((Integer) values[index]);
          index += 1;
        }
        break;

      case 'I':
        for (int i = 0; i < literal.size; i++) {
          buffer.putInt(((int[]) values[index])[i]);
        }
        index++;
        break;

      case 'l':
        for (int i = 0; i < literal.size; i++) {
          buffer.putLong((Long) values[index]);
          index += 1;
        }
        break;

      case 'L':
        for (int i = 0; i < literal.size; i++) {
          buffer.putLong(((long[]) values[index])[i]);
        }
        index++;
        break;

      case 'f':
        for (int i = 0; i < literal.size; i++) {
          buffer.putFloat((Float) values[index]);
          index += 1;
        }
        break;

      case 'F':
        for (int i = 0; i < literal.size; i++) {
          buffer.putFloat(((float[]) values[index])[i]);
        }
        index++;
        break;

      case 's':
        try {
          buffer.put(((String) values[index]).getBytes("US-ASCII"));
        } catch (UnsupportedEncodingException e) {
          throw new IllegalArgumentException(); // non-ASCII cases are regarded as wrong argument exception
        }
        index++;
        break;

      case 'S':
        try {
          buffer.put(((String) values[index]).getBytes("US-ASCII"));
        } catch (UnsupportedEncodingException e) {
          throw new IllegalArgumentException(); // non-ASCII cases are regarded as wrong argument exception
        }
        buffer.put((byte) 0x00);
        index++;
      }
    }

    return buffer.array();
  }

  public static Object[] unpack(String format, byte[] bytes) throws IllegalArgumentException {
    String[] formatTokens = format.split("(?<=\\D)");
    ArrayList<Object> decodedObjects = new ArrayList<Object>();
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    buffer.order(ByteOrder.LITTLE_ENDIAN);

    for (String token: formatTokens) {
      boolean sizeSpecified = false;
      int size = 1;
      char symbol = token.charAt(token.length() - 1);

      if (token.length() > 1) {
        sizeSpecified = true;
        size = Integer.parseInt(token.substring(0, token.length() - 1));

        if (size < 1)
          throw new IllegalArgumentException("Illegal format string");
      }

      switch (symbol) {
      case 'x':
        for (int i = 0; i < size; i++)
          buffer.get();
        break;

      case 'b':
        for (int i = 0; i < size; i++)
          decodedObjects.add(buffer.get());
        break;

      case 'B':
        byte[] byteArray = new byte[size];
        buffer.get(byteArray, 0, size);
        decodedObjects.add(byteArray);
        break;

      case 'h':
        for (int i = 0; i < size; i++)
          decodedObjects.add(buffer.getShort());
        break;

      case 'H':
        short[] shorts = new short[size];
        for (short i = 0; i < size; i++)
          shorts[i] = buffer.getShort();
        decodedObjects.add(shorts);
        break;

      case 'i':
        for (int i = 0; i < size; i++)
          decodedObjects.add(buffer.getInt());
        break;

      case 'I':
        int[] integers = new int[size];
        for (int i = 0; i < size; i++)
          integers[i] = buffer.getInt();
        decodedObjects.add(integers);
        break;

      case 'l':
        for (int i = 0; i < size; i++)
          decodedObjects.add(buffer.getLong());
        break;

      case 'L':
        long[] longs = new long[size];
        for (int i = 0; i < size; i++)
          longs[i] = buffer.getLong();
        decodedObjects.add(longs);
        break;

      case 'f':
        for (int i = 0; i < size; i++)
          decodedObjects.add(buffer.getFloat());
        break;

      case 'F':
        float[] floats = new float[size];
        for (int i = 0; i < size; i++)
          floats[i] = buffer.getFloat();
        decodedObjects.add(floats);
        break;

      case 's':
        byte[] byteString = new byte[size];
        buffer.get(byteString, 0, size);
        try {
          decodedObjects.add(new String(byteString, "US-ASCII"));
        } catch (UnsupportedEncodingException e) {
          throw new IllegalArgumentException(); //  // non-ASCII cases are regarded as wrong argument exception
        }
        break;

      case 'S':
        if (sizeSpecified)
          throw new IllegalArgumentException("Illegal format string");

        StringBuffer stringBuffer = new StringBuffer();

        while (true) {
          byte b = buffer.get();
          if (b != (byte) 0x00)
            stringBuffer.append((char) b);
          else
            break;
        }

        decodedObjects.add(stringBuffer.toString());
        break;

      case '$':
        if (sizeSpecified)
          throw new IllegalArgumentException("Illegal format string");

        if (buffer.hasRemaining())
          throw new IllegalArgumentException("Illegal format string");

      default:
        throw new IllegalArgumentException("Illegal format string");
      }
    }

    return decodedObjects.toArray();
  }

  public static byte[] encodeLC0(byte v) {
    if (v < -31 || v > 31)
      throw new IllegalArgumentException("Encoded value must be in range [0, 127]");

    return new byte[] { (byte) (v & PRIMPAR_VALUE) };
  }

  public static byte[] encodeLC1(byte v) {
    return new byte[] {(byte) ((byte) (PRIMPAR_LONG | PRIMPAR_CONST) | PRIMPAR_1_BYTE),
                       (byte) (v & 0xFF)};
  }

  public static byte[] encodeLC2(short v) {
    return new byte[] {(byte) ((byte) (PRIMPAR_LONG | PRIMPAR_CONST) | PRIMPAR_2_BYTES),
                       (byte) (v & 0xFF),
                       (byte) ((v >>> 8) & 0xFF)};
  }

  public static byte[] encodeLC4(int v) {
    return new byte[] {(byte) ((byte) (PRIMPAR_LONG | PRIMPAR_CONST) | PRIMPAR_4_BYTES),
                       (byte) (v & 0xFF),
                       (byte) ((v >>> 8) & 0xFF),
                       (byte) ((v >>> 16) & 0xFF),
                       (byte) ((v >>> 24) & 0xFF)};
  }


  public static byte[] encodeLV0(int i) {
    return new byte[] {(byte) ((i & PRIMPAR_INDEX) | PRIMPAR_SHORT | PRIMPAR_VARIABEL | PRIMPAR_LOCAL)};
  }

  public static byte[] encodeLV1(int i) {
    return new byte[] {(byte) (PRIMPAR_LONG  | PRIMPAR_VARIABEL | PRIMPAR_LOCAL | PRIMPAR_1_BYTE),
                       (byte) (i & 0xFF)};
  }

  public static byte[] encodeLV2(int i) {
    return new byte[] {(byte) (PRIMPAR_LONG  | PRIMPAR_VARIABEL | PRIMPAR_LOCAL | PRIMPAR_2_BYTES),
                       (byte) (i & 0xFF),
                       (byte) ((i >>> 8) & 0xFF)};
  }

  public static byte[] encodeLV4(int i) {
    return new byte[] {(byte) (PRIMPAR_LONG  | PRIMPAR_VARIABEL | PRIMPAR_LOCAL | PRIMPAR_4_BYTES),
                       (byte) (i & 0xFF),
                       (byte) ((i >>> 8) & 0xFF),
                       (byte) ((i >>> 16) & 0xFF),
                       (byte) ((i >>> 24) & 0xFF)};
  }

  public static byte[] encodeGV0(int i) {
    return new byte[] {(byte)((i & PRIMPAR_INDEX) | PRIMPAR_SHORT | PRIMPAR_VARIABEL | PRIMPAR_GLOBAL)};
  }

  public static byte[] encodeGV1(int i) {
    return new byte[] {(byte) (PRIMPAR_LONG  | PRIMPAR_VARIABEL | PRIMPAR_GLOBAL | PRIMPAR_1_BYTE),
                       (byte) (i & 0xFF)};
  }

  public static byte[] encodeGV2(int i) {
    return new byte[] {(byte) (PRIMPAR_LONG  | PRIMPAR_VARIABEL | PRIMPAR_GLOBAL | PRIMPAR_2_BYTES),
                       (byte) (i & 0xFF),
                       (byte) ((i >>> 8) & 0xFF)};
  }

  public static byte[] encodeGV4(int i) {
    return new byte[] {(byte) (PRIMPAR_LONG  | PRIMPAR_VARIABEL | PRIMPAR_GLOBAL | PRIMPAR_4_BYTES),
                       (byte) (i & 0xFF),
                       (byte) ((i >>> 8) & 0xFF),
                       (byte) ((i >>> 16) & 0xFF),
                       (byte) ((i >>> 24) & 0xFF)};
  }


  public static byte[] encodeSystemCommand(byte command, boolean needReply, Object... parameters) {
    int bufferCapacity = 2;

    // calculate buffer size
    for (Object obj : parameters) {
      if (obj instanceof Byte)
        bufferCapacity += 1;
      else if (obj instanceof Short)
        bufferCapacity += 2;
      else if (obj instanceof Integer)
        bufferCapacity += 4;
      else if (obj instanceof String)
        bufferCapacity += ((String) obj).length() + 1;
      else
        throw new IllegalArgumentException("Parameters should be one of the class types: Byte, Short, Integer, String");
    }

    // generate byte buffer
    ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    buffer.put(needReply ? Ev3Constants.SystemCommandType.SYSTEM_COMMAND_REPLY :
                           Ev3Constants.SystemCommandType.SYSTEM_COMMAND_NO_REPLY);
    buffer.put(command);

    for (Object obj : parameters) {
      if (obj instanceof Byte)
        buffer.put((Byte) obj);
      else if (obj instanceof Short)
        buffer.putShort((Short) obj);
      else if (obj instanceof Integer)
        buffer.putInt((Integer) obj);
      else if (obj instanceof String) {
        try {
          buffer.put(((String) obj).getBytes("US-ASCII"));
        } catch (UnsupportedEncodingException e) {
          throw new IllegalArgumentException("Non-ASCII string encoding is not supported"); // non-ASCII cases are regarded as wrong argument exception
        }
        buffer.put((byte) 0);
      } else
        throw new IllegalArgumentException("Parameters should be one of the class types: Byte, Short, Integer, String");
    }

    return buffer.array();
  }

  public static byte[] encodeDirectCommand(byte opcode, boolean needReply, int globalAllocation, int localAllocation, String paramFormat, Object... parameters) {
    if (globalAllocation < 0 || globalAllocation > 0x3ff || localAllocation < 0 || localAllocation > 0x3f || paramFormat.length() != parameters.length)
      throw new IllegalArgumentException();


    // encode parameters
    ArrayList<byte[]> payloads = new ArrayList<byte[]>();

    for (int i = 0; i < paramFormat.length(); i++) {
      char letter = paramFormat.charAt(i);
      Object obj = parameters[i];

      switch (letter) {
      case 'c':
        if (obj instanceof Byte) {
          if ((((Byte) obj) <= 31) && (((Byte) obj) >= -31))
            payloads.add(encodeLC0((Byte) obj));
          else
            payloads.add(encodeLC1((Byte) obj));
        }
        else if (obj instanceof Short)
          payloads.add(encodeLC2((Short) obj));
        else if (obj instanceof Integer)
          payloads.add(encodeLC4((Integer) obj));
        else
          throw new IllegalArgumentException();
        break;

      case 'l':
        if (obj instanceof Byte) {
          if ((((Byte) obj) <= 31) && (((Byte) obj) >= -31))
            payloads.add(encodeLV0((Byte) obj));
          else
            payloads.add(encodeLV1((Byte) obj));
        }
        else if (obj instanceof Short)
          payloads.add(encodeLV2((Short) obj));
        else if (obj instanceof Integer)
          payloads.add(encodeLV4((Integer) obj));
        else
          throw new IllegalArgumentException();
        break;

      case 'g':
        if (obj instanceof Byte) {
          if ((((Byte) obj) <= 31) && (((Byte) obj) >= -31))
            payloads.add(encodeGV0((Byte) obj));
          else
            payloads.add(encodeGV1((Byte) obj));
        }
        else if (obj instanceof Short)
          payloads.add(encodeGV2((Short) obj));
        else if (obj instanceof Integer)
          payloads.add(encodeGV4((Integer) obj));
        else
          throw new IllegalArgumentException();
        break;

      case 's':
        if (!(obj instanceof String))
          throw new IllegalArgumentException();

        try {
          payloads.add((((String) obj) + '\0').getBytes("US-ASCII"));
        } catch (UnsupportedEncodingException e) {
          throw new IllegalArgumentException();
        }
        break;

      default:
        throw new IllegalArgumentException("Illegal format string");
      }
    }

    // calculate buffer size
    int bufferCapacity = 4;
    for (byte[] array : payloads)
      bufferCapacity += array.length;

    // generate byte buffer
    ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    buffer.put(needReply ? Ev3Constants.DirectCommandType.DIRECT_COMMAND_REPLY :
                           Ev3Constants.DirectCommandType.DIRECT_COMMAND_NO_REPLY);
    buffer.put(new byte[] {(byte) (globalAllocation & 0xff),
                           (byte) (((globalAllocation >>> 8) & 0x3) | (localAllocation << 2))});
    buffer.put(opcode);

    for (byte[] array : payloads)
      buffer.put(array);

    return buffer.array();
  }
}
