package jp.hutcraft.pt.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.UnsupportedAudioFileException;

import jp.hutcraft.pt.entity.Voice;

public class AudioUtil {
	public static void main(final String[] args) {
		System.out.println("hoge");
		play(Voice.An3, 1);
	}
	private static final List<LineFolder> lineFolders = Collections.synchronizedList(new CopyOnWriteArrayList<LineFolder>());
	private static final float dinamicsRange = 50f;
	
	private static volatile float volume = 100f;
	/**
	 * @param volume 0 ~ 100
	 */
	public static void setMasterVolume(final float volume) {
		AudioUtil.volume = volume;
		for (final LineFolder folder : lineFolders) {
			if (!folder.line.isRunning()) continue;
			folder.setVolume(-dinamicsRange + (dinamicsRange * volume/100));
		}
	}
	public static void changePlayingRate(final Voice voice, final double newRate) {
		for (final LineFolder folder : lineFolders) {
			if (!folder.voice.equals(voice)) continue;
			folder.setSampleRate(newRate);
		}
	}
	public static void play(final Voice voice, final double rate) {
		final Thread t = new Thread(new Runnable(){
			@Override
			public void run() {
				AudioUtil.doPlay(voice, rate);
			}});
		t.setDaemon(true);
		t.start(); // スレッドが途中で強制終了したときplay内で保持してるリソースどうなんの
	}
	private static void doPlay(final Voice voice, final double rate) {
		try {
			final InputStream inputStream = new FileInputStream(voice.getFile());
			try {
				final AudioInputStream audioStream = AudioSystem.getAudioInputStream(inputStream);
				final AudioFormat format = audioStream.getFormat();
				final DataLine.Info info = new DataLine.Info(Clip.class, format);
				
				// 他のスレッドにより発音中の音であれば停止を送信
				for (final LineFolder folder : lineFolders) {
					if (!folder.voice.equals(voice)) continue;
					folder.setVolume(-100f);
					folder.line.close();
				}

				try {
					final Mixer mixer = AudioSystem.getMixer(null);
					final Clip line = (Clip) mixer.getLine(info);
					final LineFolder folder = new LineFolder(voice, line);
					lineFolders.add(folder);
					try {
						line.addLineListener(new LineListener(){
							@Override
							public void update(final LineEvent event) {
								if (event.getType() == LineEvent.Type.CLOSE) {
									for (final LineFolder folder : lineFolders) {
										if (folder.voice.equals(voice)) lineFolders.remove(folder);
									}
								}
							}});
						line.open(audioStream);
						folder.setSampleRate(rate);
						folder.setVolume(-dinamicsRange + (dinamicsRange * volume/100));
						line.start();
						line.drain();
					} catch (final LineUnavailableException e) {
						e.printStackTrace();
					} finally {
						line.close();
					}
				} catch (final LineUnavailableException e) {
					e.printStackTrace();
				} finally {
					audioStream.close();
				}
			} catch (final UnsupportedAudioFileException e) {
				e.printStackTrace();
			} catch (final IOException e) {
				e.printStackTrace();
			} finally {
				inputStream.close();
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public static void stopAll() {
		for (final LineFolder folder : lineFolders) {
			folder.line.close();
		}
	}

	private static class LineFolder {
		private final Voice voice;
		private final Clip line;
		private LineFolder(final Voice voice, final Clip line) {
			this.voice = voice;
			this.line = line;
		}
		private void setVolume(final float volume) {
			final FloatControl volumeControl = (FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN);
			volumeControl.setValue(volume);
		}
		private void setSampleRate(final double rate) {
			final FloatControl sampleRateControl = (FloatControl)line.getControl(FloatControl.Type.SAMPLE_RATE);
			sampleRateControl.setValue(44100.0f * Double.valueOf(rate).floatValue());
		}
	}

	/**
	 * デバイスが利用可能かテストする
	 * テスト失敗時はExcpetionを投げます。
	 */
	public static void test() {
		try {
			final InputStream inputStream = new FileInputStream("resources/empty.wav");
			try {
				final AudioInputStream audioStream = AudioSystem.getAudioInputStream(inputStream);
				final AudioFormat format = audioStream.getFormat();
				final DataLine.Info info = new DataLine.Info(Clip.class, format);
				try {
					final Mixer mixer = AudioSystem.getMixer(null);
					final Clip line = (Clip) mixer.getLine(info);
					try {
						line.open(audioStream);
						final FloatControl volumeControl = (FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN);
						volumeControl.setValue(-dinamicsRange + (dinamicsRange * volume/100f));
						final FloatControl sampleRateControl = (FloatControl)line.getControl(FloatControl.Type.SAMPLE_RATE);
						sampleRateControl.setValue(44100.0f * Double.valueOf(-dinamicsRange + (dinamicsRange * volume/100f)).floatValue());
//						line.start();
//						line.drain();
					} catch (final LineUnavailableException e) {
						e.printStackTrace();
					} finally {
						line.close();
					}
				} catch (final LineUnavailableException e) {
					e.printStackTrace();
				} finally {
					audioStream.close();
				}
			} catch (final UnsupportedAudioFileException e) {
				throw new RuntimeException(e);
			} catch (final IOException e) {
				e.printStackTrace();
			} finally {
				inputStream.close();
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
