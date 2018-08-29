package com.google.appinventor.components.runtime;

import junit.framework.TestCase;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.junit.Assert.assertEquals;
import android.view.InputDevice;
/**
 * Tests Gamepad.java
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({KeyEvent.class, Form.class, MotionEvent.class})
public class GamePadTest {

	private final Form formMock = PowerMock.createNiceMock(Form.class);
	private final KeyEvent keyMock = PowerMock.createNiceMock(KeyEvent.class);
	private final MotionEvent motionEventMock = PowerMock.createNiceMock(MotionEvent.class);
	private GamePad gamepad= new GamePad(formMock);

	
	@Before
  public void setUp() throws Exception {
    EasyMock.expect(keyMock.getSource()).andReturn(0x00000400 | 0x00000001).anyTimes();
    EasyMock.expect(motionEventMock.getSource()).andReturn(0x00000400 | 0x00000001).anyTimes();
    EasyMock.expect(motionEventMock.getHistorySize()).andReturn(1).anyTimes();  
  }

	@Test
	public void testA() throws Exception{
		EasyMock.expect(keyMock.getKeyCode()).andReturn(96).anyTimes();
		EasyMock.replay(keyMock);
		gamepad.onKeyDown(keyMock.getKeyCode(), keyMock);
		assertEquals(true, gamepad.AButton());
		gamepad.onKeyUp(keyMock.getKeyCode(), keyMock);
		assertEquals(false, gamepad.AButton());
		EasyMock.reset(keyMock);
	}
	@Test
	public void testB() throws Exception{
		EasyMock.expect(keyMock.getKeyCode()).andReturn(97).anyTimes();
		EasyMock.replay(keyMock);
		gamepad.onKeyDown(keyMock.getKeyCode(), keyMock);
		assertEquals(true, gamepad.BButton());
		gamepad.onKeyUp(keyMock.getKeyCode(), keyMock);
		assertEquals(false, gamepad.BButton());
		EasyMock.reset(keyMock);
	}
	@Test
	public void testX() throws Exception{
		EasyMock.expect(keyMock.getKeyCode()).andReturn(99).anyTimes();
		EasyMock.replay(keyMock);
		gamepad.onKeyDown(keyMock.getKeyCode(), keyMock);
		assertEquals(true, gamepad.XButton());
		gamepad.onKeyUp(keyMock.getKeyCode(), keyMock);
		assertEquals(false, gamepad.XButton());
		EasyMock.reset(keyMock);
	}
	@Test
	public void testY() throws Exception{
		EasyMock.expect(keyMock.getKeyCode()).andReturn(100).anyTimes();
		EasyMock.replay(keyMock);
		gamepad.onKeyDown(keyMock.getKeyCode(), keyMock);
		assertEquals(true, gamepad.YButton());
		gamepad.onKeyUp(keyMock.getKeyCode(), keyMock);
		assertEquals(false, gamepad.YButton());
		EasyMock.reset(keyMock);
	}
	@Test
	public void testDpadUp() throws Exception{
		EasyMock.expect(keyMock.getKeyCode()).andReturn(19).anyTimes();
		EasyMock.replay(keyMock);
		gamepad.onKeyDown(keyMock.getKeyCode(), keyMock);
		assertEquals(true, gamepad.DpadUp());
		gamepad.onKeyUp(keyMock.getKeyCode(), keyMock);
		assertEquals(false, gamepad.DpadUp());
		EasyMock.reset(keyMock);

		Float q = new Float(-1);
		EasyMock.expect(motionEventMock.getAxisValue(MotionEvent.AXIS_HAT_Y)).andReturn(q).anyTimes();
		EasyMock.replay(motionEventMock);
		gamepad.processJoystickInput(motionEventMock, 0);
		assertEquals(true, gamepad.DpadUp());
		assertEquals(false, gamepad.DpadDown());
		EasyMock.reset(motionEventMock);
	}
	@Test
	public void testDpadDown() throws Exception{
		EasyMock.expect(keyMock.getKeyCode()).andReturn(20).anyTimes();
		EasyMock.replay(keyMock);
		gamepad.onKeyDown(keyMock.getKeyCode(), keyMock);
		assertEquals(true, gamepad.DpadDown());
		gamepad.onKeyUp(keyMock.getKeyCode(), keyMock);
		assertEquals(false, gamepad.DpadDown());	
		EasyMock.reset(keyMock);

		Float q = new Float(1);
		EasyMock.expect(motionEventMock.getAxisValue(MotionEvent.AXIS_HAT_Y)).andReturn(q).anyTimes();
		EasyMock.replay(motionEventMock);
		gamepad.processJoystickInput(motionEventMock, 0);
		assertEquals(true, gamepad.DpadDown());
		assertEquals(false, gamepad.DpadUp());
		EasyMock.reset(motionEventMock);
	}
	@Test
	public void testDpadRight() throws Exception{
		EasyMock.expect(keyMock.getKeyCode()).andReturn(22).anyTimes();
		EasyMock.replay(keyMock);
		gamepad.onKeyDown(keyMock.getKeyCode(), keyMock);
		assertEquals(true, gamepad.DpadRight());
		gamepad.onKeyUp(keyMock.getKeyCode(), keyMock);
		assertEquals(false, gamepad.DpadRight());
		EasyMock.reset(keyMock);	

		Float q = new Float(1);
		EasyMock.expect(motionEventMock.getAxisValue(MotionEvent.AXIS_HAT_X)).andReturn(q).anyTimes();
		EasyMock.replay(motionEventMock);
		gamepad.processJoystickInput(motionEventMock, 0);
		assertEquals(true, gamepad.DpadRight());
		assertEquals(false, gamepad.DpadLeft());
		EasyMock.reset(motionEventMock);
	}
	@Test
	public void testDpadLeft() throws Exception{
		EasyMock.expect(keyMock.getKeyCode()).andReturn(21).anyTimes();
		EasyMock.replay(keyMock);
		gamepad.onKeyDown(keyMock.getKeyCode(), keyMock);
		assertEquals(true, gamepad.DpadLeft());
		gamepad.onKeyUp(keyMock.getKeyCode(), keyMock);
		assertEquals(false, gamepad.DpadLeft());
		EasyMock.reset(keyMock);	

		Float q = new Float(-1);
		EasyMock.expect(motionEventMock.getAxisValue(MotionEvent.AXIS_HAT_X)).andReturn(q).anyTimes();
		EasyMock.replay(motionEventMock);
		gamepad.processJoystickInput(motionEventMock, 0);
		assertEquals(true, gamepad.DpadLeft());
		assertEquals(false, gamepad.DpadRight());
		EasyMock.reset(motionEventMock);
	}
	@Test
	public void testRightBumper() throws Exception{
		EasyMock.expect(keyMock.getKeyCode()).andReturn(103).anyTimes();
		EasyMock.replay(keyMock);
		gamepad.onKeyDown(keyMock.getKeyCode(), keyMock);
		assertEquals(true, gamepad.RightBumper());
		gamepad.onKeyUp(keyMock.getKeyCode(), keyMock);
		assertEquals(false, gamepad.RightBumper());	
		EasyMock.reset(keyMock);

	}
	@Test
	public void testLeftBumper() throws Exception{
		EasyMock.expect(keyMock.getKeyCode()).andReturn(102).anyTimes();
		EasyMock.replay(keyMock);
		gamepad.onKeyDown(keyMock.getKeyCode(), keyMock);
		assertEquals(true, gamepad.LeftBumper());
		gamepad.onKeyUp(keyMock.getKeyCode(), keyMock);
		assertEquals(false, gamepad.LeftBumper());	
		EasyMock.reset(keyMock);
	}
	@Test
	public void testLeftJoystickX() throws Exception{
		Float q = new Float(1);
		EasyMock.expect(motionEventMock.getAxisValue(MotionEvent.AXIS_X)).andReturn(q).anyTimes(); 
		EasyMock.replay(motionEventMock);
		gamepad.processJoystickInput(motionEventMock, 0);
		assertEquals(1, gamepad.LeftJoystickX(), 0);
		EasyMock.reset(motionEventMock);
		q = new Float(0); 
		EasyMock.expect(motionEventMock.getAxisValue(MotionEvent.AXIS_X)).andReturn(q).anyTimes(); 
		EasyMock.replay(motionEventMock);
		gamepad.processJoystickInput(motionEventMock, 0);
		assertEquals(0, gamepad.LeftJoystickX(), 0);
	}
	@Test
	public void testLeftJoystickY() throws Exception{
		Float q = new Float(1);
		EasyMock.expect(motionEventMock.getAxisValue(MotionEvent.AXIS_Y)).andReturn(q).anyTimes(); 
		EasyMock.replay(motionEventMock);
		gamepad.processJoystickInput(motionEventMock, 0);
		assertEquals(1, gamepad.LeftJoystickY(), 0);
		EasyMock.reset(motionEventMock);
		q = new Float(0); 
		EasyMock.expect(motionEventMock.getAxisValue(MotionEvent.AXIS_Y)).andReturn(q).anyTimes(); 
		EasyMock.replay(motionEventMock);
		gamepad.processJoystickInput(motionEventMock, 0);
		assertEquals(0, gamepad.LeftJoystickY(), 0);
	}
	@Test
	public void testRightJoystickX() throws Exception{
		Float q = new Float(1);
		EasyMock.expect(motionEventMock.getAxisValue(12)).andReturn(q).anyTimes(); 
		EasyMock.replay(motionEventMock);
		gamepad.processJoystickInput(motionEventMock, 0);
		assertEquals(1, gamepad.RightJoystickX(), 0);
		EasyMock.reset(motionEventMock);
		q = new Float(0); 
		EasyMock.expect(motionEventMock.getAxisValue(12)).andReturn(q).anyTimes(); 
		EasyMock.replay(motionEventMock);
		gamepad.processJoystickInput(motionEventMock, 0);
		assertEquals(0, gamepad.RightJoystickX(), 0);
	}
	@Test
	public void testRightJoystickY() throws Exception{
		Float q = new Float(1);
		EasyMock.expect(motionEventMock.getAxisValue(13)).andReturn(q).anyTimes(); 
		EasyMock.replay(motionEventMock);
		gamepad.processJoystickInput(motionEventMock, 0);
		assertEquals(1, gamepad.RightJoystickY(), 0);
		EasyMock.reset(motionEventMock);
		q = new Float(0); 
		EasyMock.expect(motionEventMock.getAxisValue(13)).andReturn(q).anyTimes(); 
		EasyMock.replay(motionEventMock);
		gamepad.processJoystickInput(motionEventMock, 0);
		assertEquals(0, gamepad.RightJoystickY(), 0);
	}

}