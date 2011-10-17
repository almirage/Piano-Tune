package jp.hutcraft.pt.test;

import jp.hutcraft.pt.entity.Voice;
import jp.hutcraft.pt.util.AudioUtil;

public class VolumeTest {
	public static void main(final String[] args) {
		AudioUtil.play(Voice.An3, 1);
	}
}
