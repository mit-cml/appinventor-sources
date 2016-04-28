/// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

/**
 * The class provides useful constant values related to LEGO MINDSTORMDS EV3 robots.
 *
 * @author jerry73204@gmail.com (jerry73204)
 * @author spaded06543@gmail.com (Alvin Chang)
 */
public class Ev3Constants {
  public static class Opcode {
    public static final byte ERROR                     = (byte) 0x00;  //      0000
    public static final byte NOP                       = (byte) 0x01;  //      0001
    public static final byte PROGRAM_STOP              = (byte) 0x02;  //      0010
    public static final byte PROGRAM_START             = (byte) 0x03;  //      0011
    public static final byte OBJECT_STOP               = (byte) 0x04;  //      0100
    public static final byte OBJECT_START              = (byte) 0x05;  //      0101
    public static final byte OBJECT_TRIG               = (byte) 0x06;  //      0110
    public static final byte OBJECT_WAIT               = (byte) 0x07;  //      0111
    public static final byte RETURN                    = (byte) 0x08;  //      1000
    public static final byte CALL                      = (byte) 0x09;  //      1001
    public static final byte OBJECT_END                = (byte) 0x0A;  //      1010
    public static final byte SLEEP                     = (byte) 0x0B;  //      1011
    public static final byte PROGRAM_INFO              = (byte) 0x0C;  //      1100
    public static final byte LABEL                     = (byte) 0x0D;  //      1101
    public static final byte PROBE                     = (byte) 0x0E;  //      1110
    public static final byte DO                        = (byte) 0x0F;  //      1111
    //                    ADD                     00..
    public static final byte ADD8                      = (byte) 0x10;  //        00
    public static final byte ADD16                     = (byte) 0x11;  //        01
    public static final byte ADD32                     = (byte) 0x12;  //        10
    public static final byte ADDF                      = (byte) 0x13;  //        11
    //                    SUB                     01..
    public static final byte SUB8                      = (byte) 0x14;  //        00
    public static final byte SUB16                     = (byte) 0x15;  //        01
    public static final byte SUB32                     = (byte) 0x16;  //        10
    public static final byte SUBF                      = (byte) 0x17;  //        11
    //                    MUL                     10..
    public static final byte MUL8                      = (byte) 0x18;  //        00
    public static final byte MUL16                     = (byte) 0x19;  //        01
    public static final byte MUL32                     = (byte) 0x1A;  //        10
    public static final byte MULF                      = (byte) 0x1B;  //        11
    //                    DIV                     11..
    public static final byte DIV8                      = (byte) 0x1C;  //        00
    public static final byte DIV16                     = (byte) 0x1D;  //        01
    public static final byte DIV32                     = (byte) 0x1E;  //        10
    public static final byte DIVF                      = (byte) 0x1F;  //        11
    //        LOGIC                           0010....
    //                    OR                      00..
    public static final byte OR8                       = (byte) 0x20;  //        00
    public static final byte OR16                      = (byte) 0x21;  //        01
    public static final byte OR32                      = (byte) 0x22;  //        10
    //                    AND                     01..
    public static final byte AND8                      = (byte) 0x24;  //        00
    public static final byte AND16                     = (byte) 0x25;  //        01
    public static final byte AND32                     = (byte) 0x26;  //        10
    //                    XOR                     10..
    public static final byte XOR8                      = (byte) 0x28;  //        00
    public static final byte XOR16                     = (byte) 0x29;  //        01
    public static final byte XOR32                     = (byte) 0x2A;  //        10
    //                    RL                      11..
    public static final byte RL8                       = (byte) 0x2C;  //        00
    public static final byte RL16                      = (byte) 0x2D;  //        01
    public static final byte RL32                      = (byte) 0x2E;  //        10
    public static final byte INIT_BYTES                = (byte) 0x2F;  //      1111
    //        MOVE                            0011....
    //                    MOVE8_                  00..
    public static final byte MOVE8_8                   = (byte) 0x30;  //        00
    public static final byte MOVE8_16                  = (byte) 0x31;  //        01
    public static final byte MOVE8_32                  = (byte) 0x32;  //        10
    public static final byte MOVE8_F                   = (byte) 0x33;  //        11
    //                    MOVE16_                 01..
    public static final byte MOVE16_8                  = (byte) 0x34;  //        00
    public static final byte MOVE16_16                 = (byte) 0x35;  //        01
    public static final byte MOVE16_32                 = (byte) 0x36;  //        10
    public static final byte MOVE16_F                  = (byte) 0x37;  //        11
    //                    MOVE32_                 10..
    public static final byte MOVE32_8                  = (byte) 0x38;  //        00
    public static final byte MOVE32_16                 = (byte) 0x39;  //        01
    public static final byte MOVE32_32                 = (byte) 0x3A;  //        10
    public static final byte MOVE32_F                  = (byte) 0x3B;  //        11
    //                    MOVEF_                  11..
    public static final byte MOVEF_8                   = (byte) 0x3C;  //        00
    public static final byte MOVEF_16                  = (byte) 0x3D;  //        01
    public static final byte MOVEF_32                  = (byte) 0x3E;  //        10
    public static final byte MOVEF_F                   = (byte) 0x3F;  //        11
    //        BRANCH                          010000..
    public static final byte JR                        = (byte) 0x40;  //        00
    public static final byte JR_FALSE                  = (byte) 0x41;  //        01
    public static final byte JR_TRUE                   = (byte) 0x42;  //        10
    public static final byte JR_NAN                    = (byte) 0x43;  //        11
    //        COMPARE                         010.....
    //                    CP_LT                  001..
    public static final byte CP_LT8                    = (byte) 0x44;  //        00
    public static final byte CP_LT16                   = (byte) 0x45;  //        01
    public static final byte CP_LT32                   = (byte) 0x46;  //        10
    public static final byte CP_LTF                    = (byte) 0x47;  //        11
    //                    CP_GT                  010..
    public static final byte CP_GT8                    = (byte) 0x48;  //        00
    public static final byte CP_GT16                   = (byte) 0x49;  //        01
    public static final byte CP_GT32                   = (byte) 0x4A;  //        10
    public static final byte CP_GTF                    = (byte) 0x4B;  //        11
    //                    CP_EQ                  011..
    public static final byte CP_EQ8                    = (byte) 0x4C;  //        00
    public static final byte CP_EQ16                   = (byte) 0x4D;  //        01
    public static final byte CP_EQ32                   = (byte) 0x4E;  //        10
    public static final byte CP_EQF                    = (byte) 0x4F;  //        11
    //                    CP_NEQ                 100..
    public static final byte CP_NEQ8                   = (byte) 0x50;  //        00
    public static final byte CP_NEQ16                  = (byte) 0x51;  //        01
    public static final byte CP_NEQ32                  = (byte) 0x52;  //        10
    public static final byte CP_NEQF                   = (byte) 0x53;  //        11
    //                    CP_LTEQ                101..
    public static final byte CP_LTEQ8                  = (byte) 0x54;  //        00
    public static final byte CP_LTEQ16                 = (byte) 0x55;  //        01
    public static final byte CP_LTEQ32                 = (byte) 0x56;  //        10
    public static final byte CP_LTEQF                  = (byte) 0x57;  //        11
    //                    CP_GTEQ                110..
    public static final byte CP_GTEQ8                  = (byte) 0x58;  //        00
    public static final byte CP_GTEQ16                 = (byte) 0x59;  //        01
    public static final byte CP_GTEQ32                 = (byte) 0x5A;  //        10
    public static final byte CP_GTEQF                  = (byte) 0x5B;  //        11
    //        SELECT                          010111..
    public static final byte SELECT8                   = (byte) 0x5C;  //        00
    public static final byte SELECT16                  = (byte) 0x5D;  //        01
    public static final byte SELECT32                  = (byte) 0x5E;  //        10
    public static final byte SELECTF                   = (byte) 0x5F;  //        11
    public static final byte SYSTEM                    = (byte) 0x60;
    public static final byte PORT_CNV_OUTPUT           = (byte) 0x61;
    public static final byte PORT_CNV_INPUT            = (byte) 0x62;
    public static final byte NOTE_TO_FREQ              = (byte) 0x63;
    //        BRANCH                          011000..
    //?       00
    //?       01
    //?       10
    //?       11
    //                    JR_LT                  001..
    public static final byte JR_LT8                    = (byte) 0x64;  //        00
    public static final byte JR_LT16                   = (byte) 0x65;  //        01
    public static final byte JR_LT32                   = (byte) 0x66;  //        10
    public static final byte JR_LTF                    = (byte) 0x67;  //        11
    //                    JR_GT                  010..
    public static final byte JR_GT8                    = (byte) 0x68;  //        00
    public static final byte JR_GT16                   = (byte) 0x69;  //        01
    public static final byte JR_GT32                   = (byte) 0x6A;  //        10
    public static final byte JR_GTF                    = (byte) 0x6B;  //        11
    //                    JR_EQ                  011..
    public static final byte JR_EQ8                    = (byte) 0x6C;  //        00
    public static final byte JR_EQ16                   = (byte) 0x6D;  //        01
    public static final byte JR_EQ32                   = (byte) 0x6E;  //        10
    public static final byte JR_EQF                    = (byte) 0x6F;  //        11
    //                    JR_NEQ                 100..
    public static final byte JR_NEQ8                   = (byte) 0x70;  //        00
    public static final byte JR_NEQ16                  = (byte) 0x71;  //        01
    public static final byte JR_NEQ32                  = (byte) 0x72;  //        10
    public static final byte JR_NEQF                   = (byte) 0x73;  //        11
    //                    JR_LTEQ                101..
    public static final byte JR_LTEQ8                  = (byte) 0x74;  //        00
    public static final byte JR_LTEQ16                 = (byte) 0x75;  //        01
    public static final byte JR_LTEQ32                 = (byte) 0x76;  //        10
    public static final byte JR_LTEQF                  = (byte) 0x77;  //        11
    //                    JR_GTEQ                110..
    public static final byte JR_GTEQ8                  = (byte) 0x78;  //        00
    public static final byte JR_GTEQ16                 = (byte) 0x79;  //        01
    public static final byte JR_GTEQ32                 = (byte) 0x7A;  //        10
    public static final byte JR_GTEQF                  = (byte) 0x7B;  //        11
    public static final byte INFO                      = (byte) 0x7C;  //  01111100
    public static final byte STRINGS                   = (byte) 0x7D;  //  01111101
    public static final byte MEMORY_WRITE              = (byte) 0x7E;  //  01111110
    public static final byte MEMORY_READ               = (byte) 0x7F;  //  01111111
    //        SYSTEM                          1.......
    //        UI                              100000..
    public static final byte UI_FLUSH                  = (byte) 0x80;  //        00
    public static final byte UI_READ                   = (byte) 0x81;  //        01
    public static final byte UI_WRITE                  = (byte) 0x82;  //        10
    public static final byte UI_BUTTON                 = (byte) 0x83;  //        11
    public static final byte UI_DRAW                   = (byte) 0x84;  //  10000100
    public static final byte TIMER_WAIT                = (byte) 0x85;  //  10000101
    public static final byte TIMER_READY               = (byte) 0x86;  //  10000110
    public static final byte TIMER_READ                = (byte) 0x87;  //  10000111
    //        BREAKPOINT                      10001...
    public static final byte BP0                       = (byte) 0x88;  //       000
    public static final byte BP1                       = (byte) 0x89;  //       001
    public static final byte BP2                       = (byte) 0x8A;  //       010
    public static final byte BP3                       = (byte) 0x8B;  //       011
    public static final byte BP_SET                    = (byte) 0x8C;  //  10001100
    public static final byte MATH                      = (byte) 0x8D;  //  10001101
    public static final byte RANDOM                    = (byte) 0x8E;  //  10001110
    public static final byte TIMER_READ_US             = (byte) 0x8F;  //  10001111
    public static final byte KEEP_ALIVE                = (byte) 0x90;  //  10010000
    //                                        100100
    public static final byte COM_READ                  = (byte) 0x91;  //        01
    public static final byte COM_WRITE                 = (byte) 0x92;  //        10
    //                                        100101
    public static final byte SOUND                     = (byte) 0x94;  //        00
    public static final byte SOUND_TEST                = (byte) 0x95;  //        01
    public static final byte SOUND_READY               = (byte) 0x96;  //        10
    //
    public static final byte INPUT_SAMPLE              = (byte) 0x97;  //  10010111
    //                                        10011...
    public static final byte INPUT_DEVICE_LIST         = (byte) 0x98;  //       000
    public static final byte INPUT_DEVICE              = (byte) 0x99;  //       001
    public static final byte INPUT_READ                = (byte) 0x9A;  //       010
    public static final byte INPUT_TEST                = (byte) 0x9B;  //       011
    public static final byte INPUT_READY               = (byte) 0x9C;  //       100
    public static final byte INPUT_READSI              = (byte) 0x9D;  //       101
    public static final byte INPUT_READEXT             = (byte) 0x9E;  //       110
    public static final byte INPUT_WRITE               = (byte) 0x9F;  //       111
    //                                        101.....
    public static final byte OUTPUT_GET_TYPE           = (byte) 0xA0;  //     00000
    public static final byte OUTPUT_SET_TYPE           = (byte) 0xA1;  //     00001
    public static final byte OUTPUT_RESET              = (byte) 0xA2;  //     00010
    public static final byte OUTPUT_STOP               = (byte) 0xA3;  //     00011
    public static final byte OUTPUT_POWER              = (byte) 0xA4;  //     00100
    public static final byte OUTPUT_SPEED              = (byte) 0xA5;  //     00101
    public static final byte OUTPUT_START              = (byte) 0xA6;  //     00110
    public static final byte OUTPUT_POLARITY           = (byte) 0xA7;  //     00111
    public static final byte OUTPUT_READ               = (byte) 0xA8;  //     01000
    public static final byte OUTPUT_TEST               = (byte) 0xA9;  //     01001
    public static final byte OUTPUT_READY              = (byte) 0xAA;  //     01010
    public static final byte OUTPUT_POSITION           = (byte) 0xAB;  //     01011
    public static final byte OUTPUT_STEP_POWER         = (byte) 0xAC;  //     01100
    public static final byte OUTPUT_TIME_POWER         = (byte) 0xAD;  //     01101
    public static final byte OUTPUT_STEP_SPEED         = (byte) 0xAE;  //     01110
    public static final byte OUTPUT_TIME_SPEED         = (byte) 0xAF;  //     01111
    public static final byte OUTPUT_STEP_SYNC          = (byte) 0xB0;  //     10000
    public static final byte OUTPUT_TIME_SYNC          = (byte) 0xB1;  //     10001
    public static final byte OUTPUT_CLR_COUNT          = (byte) 0xB2;  //     10010
    public static final byte OUTPUT_GET_COUNT          = (byte) 0xB3;  //     10011
    public static final byte OUTPUT_PRG_STOP           = (byte) 0xB4;  //     10100
    //                                        11000...
    public static final byte FILE                      = (byte) 0xC0;  //       000
    public static final byte ARRAY                     = (byte) 0xC1;  //       001
    public static final byte ARRAY_WRITE               = (byte) 0xC2;  //       010
    public static final byte ARRAY_READ                = (byte) 0xC3;  //       011
    public static final byte ARRAY_APPEND              = (byte) 0xC4;  //       100
    public static final byte MEMORY_USAGE              = (byte) 0xC5;  //       101
    public static final byte FILENAME                  = (byte) 0xC6;  //       110
    //                                        110010..
    public static final byte READ8                     = (byte) 0xC8;  //        00
    public static final byte READ16                    = (byte) 0xC9;  //        01
    public static final byte READ32                    = (byte) 0xCA;  //        10
    public static final byte READF                     = (byte) 0xCB;  //        11
    //                                        110011..
    public static final byte WRITE8                    = (byte) 0xCC;  //        00
    public static final byte WRITE16                   = (byte) 0xCD;  //        01
    public static final byte WRITE32                   = (byte) 0xCE;  //        10
    public static final byte WRITEF                    = (byte) 0xCF;  //        11
    //                                        11010...
    public static final byte COM_READY                 = (byte) 0xD0;  //       000
    public static final byte COM_READDATA              = (byte) 0xD1;  //       001
    public static final byte COM_WRITEDATA             = (byte) 0xD2;  //       010
    public static final byte COM_GET                   = (byte) 0xD3;  //       011
    public static final byte COM_SET                   = (byte) 0xD4;  //       100
    public static final byte COM_TEST                  = (byte) 0xD5;  //       101
    public static final byte COM_REMOVE                = (byte) 0xD6;  //       110
    public static final byte COM_WRITEFILE             = (byte) 0xD7;  //       111
    //                                        11011...
    public static final byte MAILBOX_OPEN              = (byte) 0xD8;  //       000
    public static final byte MAILBOX_WRITE             = (byte) 0xD9;  //       001
    public static final byte MAILBOX_READ              = (byte) 0xDA;  //       010
    public static final byte MAILBOX_TEST              = (byte) 0xDB;  //       011
    public static final byte MAILBOX_READY             = (byte) 0xDC;  //       100
    public static final byte MAILBOX_CLOSE             = (byte) 0xDD;  //       101
    //        SPARE                           111.....
    public static final byte TST                       = (byte) 0xFF;   //  11111111
  }

  public static class InputDeviceSubcode {
    public static final byte GET_FORMAT     = 2;
    public static final byte CAL_MINMAX     = 3;
    public static final byte CAL_DEFAULT    = 4;
    public static final byte GET_TYPEMODE   = 5;
    public static final byte GET_SYMBOL     = 6;
    public static final byte CAL_MIN        = 7;
    public static final byte CAL_MAX        = 8;
    public static final byte SETUP          = 9;
    public static final byte CLR_ALL        = 10;
    public static final byte GET_RAW        = 11;
    public static final byte GET_CONNECTION = 12;
    public static final byte STOP_ALL       = 13;
    public static final byte GET_NAME       = 21;
    public static final byte GET_MODENAME   = 22;
    public static final byte SET_RAW        = 23;
    public static final byte GET_FIGURES    = 24;
    public static final byte GET_CHANGES    = 25;
    public static final byte CLR_CHANGES    = 26;
    public static final byte READY_PCT      = 27;
    public static final byte READY_RAW      = 28;
    public static final byte READY_SI       = 29;
    public static final byte GET_MINMAX     = 30;
    public static final byte GET_BUMPS      = 31;
  }

  public static class SoundSubcode {
    public static final byte BREAK   = 0;
    public static final byte TONE    = 1;
    public static final byte PLAY    = 2;
    public static final byte REPEAT  = 3;
    public static final byte SERVICE = 4;
  }

  public static class UIWriteSubcode {
    public static final byte WRITE_FLUSH   = 1;
    public static final byte FLOATVALUE    = 2;
    public static final byte STAMP         = 3;
    public static final byte PUT_STRING    = 8;
    public static final byte VALUE8        = 9;
    public static final byte VALUE16       = 10;
    public static final byte VALUE32       = 11;
    public static final byte VALUEF        = 12;
    public static final byte ADDRESS       = 13;
    public static final byte CODE          = 14;
    public static final byte DOWNLOAD_END  = 15;
    public static final byte SCREEN_BLOCK  = 16;
    public static final byte TEXTBOX_APPEND = 21;
    public static final byte SET_BUSY      = 22;
    public static final byte SET_TESTPIN   = 24;
    public static final byte INIT_RUN      = 25;
    public static final byte UPDATE_RUN    = 26;
    public static final byte LED           = 27;
    public static final byte POWER         = 29;
    public static final byte GRAPH_SAMPLE  = 30;
    public static final byte TERMINAL      = 31;
  }

  public static class UIReadSubcode {
    public static final byte GET_VBATT     = 1;
    public static final byte GET_IBATT     = 2;
    public static final byte GET_OS_VERS   = 3;
    public static final byte GET_EVENT     = 4;
    public static final byte GET_TBATT     = 5;
    public static final byte GET_IINT      = 6;
    public static final byte GET_IMOTOR    = 7;
    public static final byte GET_STRING    = 8;
    public static final byte GET_HW_VERS   = 9;
    public static final byte GET_FW_VERS   = 10;
    public static final byte GET_FW_BUILD  = 11;
    public static final byte GET_OS_BUILD  = 12;
    public static final byte GET_ADDRESS   = 13;
    public static final byte GET_CODE      = 14;
    public static final byte KEY           = 15;
    public static final byte GET_SHUTDOWN  = 16;
    public static final byte GET_WARNING   = 17;
    public static final byte GET_LBATT     = 18;
    public static final byte TEXTBOX_READ  = 21;
    public static final byte GET_VERSION   = 26;
    public static final byte GET_IP        = 27;
    public static final byte GET_POWER     = 29;
    public static final byte GET_SDCARD    = 30;
    public static final byte GET_USBSTICK  = 31;
  }

  public static class UIButtonSubcode {
    public static final byte SHORTPRESS      = 1;
    public static final byte LONGPRESS       = 2;
    public static final byte WAIT_FOR_PRESS  = 3;
    public static final byte FLUSH           = 4;
    public static final byte PRESS           = 5;
    public static final byte RELEASE         = 6;
    public static final byte GET_HORZ        = 7;
    public static final byte GET_VERT        = 8;
    public static final byte PRESSED         = 9;
    public static final byte SET_BACK_BLOCK  = 10;
    public static final byte GET_BACK_BLOCK  = 11;
    public static final byte TESTSHORTPRESS  = 12;
    public static final byte TESTLONGPRESS   = 13;
    public static final byte GET_BUMBED      = 14;
    public static final byte GET_CLICK       = 15;
  }

  public static class UIDrawSubcode {
    public static final byte UPDATE        = 0;
    public static final byte CLEAN         = 1;
    public static final byte PIXEL         = 2;
    public static final byte LINE          = 3;
    public static final byte CIRCLE        = 4;
    public static final byte TEXT          = 5;
    public static final byte ICON          = 6;
    public static final byte PICTURE       = 7;
    public static final byte VALUE         = 8;
    public static final byte FILLRECT      = 9;
    public static final byte RECT          = 10;
    public static final byte NOTIFICATION  = 11;
    public static final byte QUESTION      = 12;
    public static final byte KEYBOARD      = 13;
    public static final byte BROWSE        = 14;
    public static final byte VERTBAR       = 15;
    public static final byte INVERSERECT   = 16;
    public static final byte SELECT_FONT   = 17;
    public static final byte TOPLINE       = 18;
    public static final byte FILLWINDOW    = 19;
    public static final byte SCROLL        = 20;
    public static final byte DOTLINE       = 21;
    public static final byte VIEW_VALUE    = 22;
    public static final byte VIEW_UNIT     = 23;
    public static final byte FILLCIRCLE    = 24;
    public static final byte STORE         = 25;
    public static final byte RESTORE       = 26;
    public static final byte ICON_QUESTION = 27;
    public static final byte BMPFILE       = 28;
    public static final byte POPUP         = 29;
    public static final byte GRAPH_SETUP   = 30;
    public static final byte GRAPH_DRAW    = 31;
    public static final byte TEXTBOX       = 32;
  }

  public static class FontType {
    public static final byte NORMAL_FONT = 0;
    public static final byte SMALL_FONT  = 1;
    public static final byte LARGE_FONT  = 2;
    public static final byte TINY_FONT   = 3;
  }

  public static class DataFormat {
    // public static final byte DATA_8   = 0x00; //!< DATA8  (don't change)
    // public static final byte DATA_16  = 0x01; //!< DATA16 (don't change)
    // public static final byte DATA_32  = 0x02; //!< DATA32 (don't change)
    // public static final byte DATA_F   = 0x03; //!< DATAF  (don't change)
    // public static final byte DATA_S   = 0x04; //!< Zero terminated string
    // public static final byte DATA_A   = 0x05; //!< Array handle
    // public static final byte DATA_V   = 0x07; //!< Variable type
    public static final byte DATA_PCT = 0x10; //!< Percent (used in opINPUT_READEXT)
    public static final byte DATA_RAW = 0x12; //!< Raw     (used in opINPUT_READEXT)
    public static final byte DATA_SI  = 0x13; //!< SI unit (used in opINPUT_READEXT)
  }

  public static class SystemCommandType {
    public static final byte SYSTEM_COMMAND_REPLY    = (byte) 0x01; // System command, reply required
    public static final byte SYSTEM_COMMAND_NO_REPLY = (byte) 0x81; // System command, reply not require
  }

  public static class SystemCommand {
    public static final byte BEGIN_DOWNLOAD      = (byte) 0x92; // Begin file download
    public static final byte CONTINUE_DOWNLOAD   = (byte) 0x93; // Continue file download
    public static final byte BEGIN_UPLOAD        = (byte) 0x94; // Begin file upload
    public static final byte CONTINUE_UPLOAD     = (byte) 0x95; // Continue file upload
    public static final byte BEGIN_GETFILE       = (byte) 0x96; // Begin get bytes from a file (while writing to the file)
    public static final byte CONTINUE_GETFILE    = (byte) 0x97; // Continue get byte from a file (while writing to the file)
    public static final byte CLOSE_FILEHANDLE    = (byte) 0x98; // Close file handle
    public static final byte LIST_FILES          = (byte) 0x99; // List files
    public static final byte CONTINUE_LIST_FILES = (byte) 0x9A; // Continue list files
    public static final byte CREATE_DIR          = (byte) 0x9B; // Create directory
    public static final byte DELETE_FILE         = (byte) 0x9C; // Delete
    public static final byte LIST_OPEN_HANDLES   = (byte) 0x9D; // List handles
    public static final byte WRITEMAILBOX        = (byte) 0x9E; // Write to mailbox
    public static final byte BLUETOOTHPIN        = (byte) 0x9F; // Transfer trusted pin code to brick
    public static final byte ENTERFWUPDATE       = (byte) 0xA0; // Restart the brick in Firmware update mode
  }

  public static class SystemReplyType {
    public static final byte SYSTEM_REPLY       = (byte) 0x03;       // System command reply OK
    public static final byte SYSTEM_REPLY_ERROR = (byte) 0x05; // System command reply ERROR
  };

  public static class SystemReplyStatus {
    public static final byte SUCCESS              = (byte) 0x00;
    public static final byte UNKNOWN_HANDLE       = (byte) 0x01;
    public static final byte HANDLE_NOT_READY     = (byte) 0x02;
    public static final byte CORRUPT_FILE         = (byte) 0x03;
    public static final byte NO_HANDLES_AVAILABLE = (byte) 0x04;
    public static final byte NO_PERMISSION        = (byte) 0x05;
    public static final byte ILLEGAL_PATH         = (byte) 0x06;
    public static final byte FILE_EXITS           = (byte) 0x07;
    public static final byte END_OF_FILE          = (byte) 0x08;
    public static final byte SIZE_ERROR           = (byte) 0x09;
    public static final byte UNKNOWN_ERROR        = (byte) 0x0A;
    public static final byte ILLEGAL_FILENAME     = (byte) 0x0B;
    public static final byte ILLEGAL_CONNECTION   = (byte) 0x0C;
  }

  public static class DirectCommandType {
    public static final byte DIRECT_COMMAND_REPLY    = (byte) 0x00;
    public static final byte DIRECT_COMMAND_NO_REPLY = (byte) 0x80;
  }

  public static class DirectReplyType {
    public static final byte DIRECT_REPLY       = (byte) 0x02;
    public static final byte DIRECT_REPLY_ERROR = (byte) 0x04;
  }
}
