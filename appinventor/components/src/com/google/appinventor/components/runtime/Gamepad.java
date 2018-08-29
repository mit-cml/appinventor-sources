/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  android.annotation.TargetApi
 *  android.os.Build
 *  android.os.Build$VERSION
 *  android.view.InputDevice
 *  android.view.KeyEvent
 *  android.view.MotionEvent
 */
package com.qualcomm.robotcore.hardware;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.robocol.RobocolParsable;
import com.qualcomm.robotcore.robocol.RobocolParsableBase;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.util.RobotLog;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Set;

public class Gamepad
extends RobocolParsableBase {
    public static final int ID_UNASSOCIATED = -1;
    public float left_stick_x = 0.0f;
    public float left_stick_y = 0.0f;
    public float right_stick_x = 0.0f;
    public float right_stick_y = 0.0f;
    public boolean dpad_up = false;
    public boolean dpad_down = false;
    public boolean dpad_left = false;
    public boolean dpad_right = false;
    public boolean a = false;
    public boolean b = false;
    public boolean x = false;
    public boolean y = false;
    public boolean guide = false;
    public boolean start = false;
    public boolean back = false;
    public boolean left_bumper = false;
    public boolean right_bumper = false;
    public boolean left_stick_button = false;
    public boolean right_stick_button = false;
    public float left_trigger = 0.0f;
    public float right_trigger = 0.0f;
    public byte user = -1;
    public int id = -1;
    public long timestamp = 0;
    protected float dpadThreshold = 0.2f;
    protected float joystickDeadzone = 0.2f;
    private final GamepadCallback c;
    private static Set<Integer> d = new HashSet<Integer>();
    private static Set<a> e = null;

    public Gamepad() {
        this(null);
    }

    public Gamepad(GamepadCallback callback) {
        this.c = callback;
    }

    public void copy(Gamepad gamepad) throws RobotCoreException {
        this.fromByteArray(gamepad.toByteArray());
    }

    public void reset() {
        try {
            this.copy(new Gamepad());
        }
        catch (RobotCoreException var1_1) {
            RobotLog.e("Gamepad library in an invalid state");
            throw new IllegalStateException("Gamepad library in an invalid state");
        }
    }

    public void setJoystickDeadzone(float deadzone) {
        if (deadzone < 0.0f || deadzone > 1.0f) {
            throw new IllegalArgumentException("deadzone cannot be greater than max joystick value");
        }
        this.joystickDeadzone = deadzone;
    }

    public void update(MotionEvent event) {
        this.id = event.getDeviceId();
        this.timestamp = event.getEventTime();
        this.left_stick_x = this.cleanMotionValues(event.getAxisValue(0));
        this.left_stick_y = this.cleanMotionValues(event.getAxisValue(1));
        this.right_stick_x = this.cleanMotionValues(event.getAxisValue(11));
        this.right_stick_y = this.cleanMotionValues(event.getAxisValue(14));
        this.left_trigger = event.getAxisValue(17);
        this.right_trigger = event.getAxisValue(18);
        this.dpad_down = event.getAxisValue(16) > this.dpadThreshold;
        this.dpad_up = event.getAxisValue(16) < - this.dpadThreshold;
        this.dpad_right = event.getAxisValue(15) > this.dpadThreshold;
        this.dpad_left = event.getAxisValue(15) < - this.dpadThreshold;
        this.callCallback();
    }

    public void update(KeyEvent event) {
        this.id = event.getDeviceId();
        this.timestamp = event.getEventTime();
        int n2 = event.getKeyCode();
        if (n2 == 19) {
            this.dpad_up = this.pressed(event);
        } else if (n2 == 20) {
            this.dpad_down = this.pressed(event);
        } else if (n2 == 22) {
            this.dpad_right = this.pressed(event);
        } else if (n2 == 21) {
            this.dpad_left = this.pressed(event);
        } else if (n2 == 96) {
            this.a = this.pressed(event);
        } else if (n2 == 97) {
            this.b = this.pressed(event);
        } else if (n2 == 99) {
            this.x = this.pressed(event);
        } else if (n2 == 100) {
            this.y = this.pressed(event);
        } else if (n2 == 110) {
            this.guide = this.pressed(event);
        } else if (n2 == 108) {
            this.start = this.pressed(event);
        } else if (n2 == 109) {
            this.back = this.pressed(event);
        } else if (n2 == 103) {
            this.right_bumper = this.pressed(event);
        } else if (n2 == 102) {
            this.left_bumper = this.pressed(event);
        } else if (n2 == 106) {
            this.left_stick_button = this.pressed(event);
        } else if (n2 == 107) {
            this.right_stick_button = this.pressed(event);
        }
        this.callCallback();
    }

    @Override
    public RobocolParsable.MsgType getRobocolMsgType() {
        return RobocolParsable.MsgType.GAMEPAD;
    }

    @Override
    public byte[] toByteArray() throws RobotCoreException {
        ByteBuffer byteBuffer = this.getWriteBuffer(42);
        try {
            int n2 = 0;
            byteBuffer.put(2);
            byteBuffer.putInt(this.id);
            byteBuffer.putLong(this.timestamp).array();
            byteBuffer.putFloat(this.left_stick_x).array();
            byteBuffer.putFloat(this.left_stick_y).array();
            byteBuffer.putFloat(this.right_stick_x).array();
            byteBuffer.putFloat(this.right_stick_y).array();
            byteBuffer.putFloat(this.left_trigger).array();
            byteBuffer.putFloat(this.right_trigger).array();
            n2 = (n2 << 1) + (this.left_stick_button ? 1 : 0);
            n2 = (n2 << 1) + (this.right_stick_button ? 1 : 0);
            n2 = (n2 << 1) + (this.dpad_up ? 1 : 0);
            n2 = (n2 << 1) + (this.dpad_down ? 1 : 0);
            n2 = (n2 << 1) + (this.dpad_left ? 1 : 0);
            n2 = (n2 << 1) + (this.dpad_right ? 1 : 0);
            n2 = (n2 << 1) + (this.a ? 1 : 0);
            n2 = (n2 << 1) + (this.b ? 1 : 0);
            n2 = (n2 << 1) + (this.x ? 1 : 0);
            n2 = (n2 << 1) + (this.y ? 1 : 0);
            n2 = (n2 << 1) + (this.guide ? 1 : 0);
            n2 = (n2 << 1) + (this.start ? 1 : 0);
            n2 = (n2 << 1) + (this.back ? 1 : 0);
            n2 = (n2 << 1) + (this.left_bumper ? 1 : 0);
            n2 = (n2 << 1) + (this.right_bumper ? 1 : 0);
            byteBuffer.putInt(n2);
            byteBuffer.put(this.user);
        }
        catch (BufferOverflowException var2_3) {
            RobotLog.logStacktrace(var2_3);
        }
        return byteBuffer.array();
    }

    @Override
    public void fromByteArray(byte[] byteArray) throws RobotCoreException {
        if (byteArray.length < 47) {
            throw new RobotCoreException("Expected buffer of at least 47 bytes, received " + byteArray.length);
        }
        ByteBuffer byteBuffer = this.getReadBuffer(byteArray);
        int n2 = 0;
        byte by = byteBuffer.get();
        if (by >= 1) {
            this.id = byteBuffer.getInt();
            this.timestamp = byteBuffer.getLong();
            this.left_stick_x = byteBuffer.getFloat();
            this.left_stick_y = byteBuffer.getFloat();
            this.right_stick_x = byteBuffer.getFloat();
            this.right_stick_y = byteBuffer.getFloat();
            this.left_trigger = byteBuffer.getFloat();
            this.right_trigger = byteBuffer.getFloat();
            n2 = byteBuffer.getInt();
            this.left_stick_button = (n2 & 16384) != 0;
            this.right_stick_button = (n2 & 8192) != 0;
            this.dpad_up = (n2 & 4096) != 0;
            this.dpad_down = (n2 & 2048) != 0;
            this.dpad_left = (n2 & 1024) != 0;
            this.dpad_right = (n2 & 512) != 0;
            this.a = (n2 & 256) != 0;
            this.b = (n2 & 128) != 0;
            this.x = (n2 & 64) != 0;
            this.y = (n2 & 32) != 0;
            this.guide = (n2 & 16) != 0;
            this.start = (n2 & 8) != 0;
            this.back = (n2 & 4) != 0;
            this.left_bumper = (n2 & 2) != 0;
            boolean bl = this.right_bumper = (n2 & 1) != 0;
        }
        if (by >= 2) {
            this.user = byteBuffer.get();
        }
        this.callCallback();
    }

    public boolean atRest() {
        return this.left_stick_x == 0.0f && this.left_stick_y == 0.0f && this.right_stick_x == 0.0f && this.right_stick_y == 0.0f && this.left_trigger == 0.0f && this.right_trigger == 0.0f;
    }

    public String type() {
        return "Standard";
    }

    public String toString() {
        String string = new String();
        if (this.dpad_up) {
            string = string + "dpad_up ";
        }
        if (this.dpad_down) {
            string = string + "dpad_down ";
        }
        if (this.dpad_left) {
            string = string + "dpad_left ";
        }
        if (this.dpad_right) {
            string = string + "dpad_right ";
        }
        if (this.a) {
            string = string + "a ";
        }
        if (this.b) {
            string = string + "b ";
        }
        if (this.x) {
            string = string + "x ";
        }
        if (this.y) {
            string = string + "y ";
        }
        if (this.guide) {
            string = string + "guide ";
        }
        if (this.start) {
            string = string + "start ";
        }
        if (this.back) {
            string = string + "back ";
        }
        if (this.left_bumper) {
            string = string + "left_bumper ";
        }
        if (this.right_bumper) {
            string = string + "right_bumper ";
        }
        if (this.left_stick_button) {
            string = string + "left stick button ";
        }
        if (this.right_stick_button) {
            string = string + "right stick button ";
        }
        return String.format("ID: %2d user: %2d lx: % 1.2f ly: % 1.2f rx: % 1.2f ry: % 1.2f lt: %1.2f rt: %1.2f %s", this.id, Byte.valueOf(this.user), Float.valueOf(this.left_stick_x), Float.valueOf(this.left_stick_y), Float.valueOf(this.right_stick_x), Float.valueOf(this.right_stick_y), Float.valueOf(this.left_trigger), Float.valueOf(this.right_trigger), string);
    }

    protected float cleanMotionValues(float number) {
        if (number < this.joystickDeadzone && number > - this.joystickDeadzone) {
            return 0.0f;
        }
        if (number > 1.0f) {
            return 1.0f;
        }
        if (number < -1.0f) {
            return -1.0f;
        }
        number = number > 0.0f ? (float)Range.scale(number, this.joystickDeadzone, 1.0, 0.0, 1.0) : (float)Range.scale(number, - this.joystickDeadzone, -1.0, 0.0, -1.0);
        return number;
    }

    protected boolean pressed(KeyEvent event) {
        return event.getAction() == 0;
    }

    protected void callCallback() {
        if (this.c != null) {
            this.c.gamepadChanged(this);
        }
    }

    public static void enableWhitelistFilter(int vendorId, int productId) {
        if (e == null) {
            e = new HashSet<a>();
        }
        e.add(new a(vendorId, productId));
    }

    public static void clearWhitelistFilter() {
        e = null;
    }

    @TargetApi(value=19)
    public static synchronized boolean isGamepadDevice(int deviceId) {
        int[] arrn;
        if (d.contains(deviceId)) {
            return true;
        }
        d = new HashSet<Integer>();
        for (int n2 : arrn = InputDevice.getDeviceIds()) {
            InputDevice inputDevice = InputDevice.getDevice((int)n2);
            int n3 = inputDevice.getSources();
            if ((n3 & 1025) != 1025 && (n3 & 16777232) != 16777232) continue;
            if (Build.VERSION.SDK_INT >= 19) {
                if (e != null && !e.contains(new a(inputDevice.getVendorId(), inputDevice.getProductId()))) continue;
                d.add(n2);
                continue;
            }
            d.add(n2);
        }
        if (d.contains(deviceId)) {
            return true;
        }
        return false;
    }

    private static class a
    extends AbstractMap.SimpleEntry<Integer, Integer> {
        public a(int n2, int n3) {
            super(n2, n3);
        }
    }

    public static interface GamepadCallback {
        public void gamepadChanged(Gamepad var1);
    }

}

