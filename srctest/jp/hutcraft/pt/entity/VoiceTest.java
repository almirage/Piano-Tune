package jp.hutcraft.pt.entity;

import junit.framework.TestCase;

public class VoiceTest extends TestCase {

	public void test() {
		assertEquals(true, Voice.Fs3.isBlack());
		assertEquals(false, Voice.Fn3.isBlack());
		assertEquals(0.9438743126816934, Voice.Fn3.getAdjust());
		assertEquals(1.0, Voice.Fs3.getAdjust());
		assertEquals(0.8908987181403393, Voice.Gn3.getAdjust());
	}
}
