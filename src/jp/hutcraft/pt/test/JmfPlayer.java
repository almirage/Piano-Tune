package jp.hutcraft.pt.test;

import java.io.File;
import java.io.IOException;

import javax.media.Control;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoDataSourceException;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.protocol.DataSource;

public class JmfPlayer {
	public static void main(final String[] args) {
		final MediaLocator ml = new MediaLocator("file://"+ new File(".").getAbsoluteFile().getParent()+"/resources/an3.wav");
		try {
			final DataSource dataSource = Manager.createDataSource(ml);
			try {
				Player p = null;
				try {
					p = Manager.createPlayer(dataSource);
				} catch (NoPlayerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
//				final Processor p = Manager.createProcessor(dataSource);
//				p.configure();
//				while (p.getState() != Processor.Configured) ThreadUtil.sleep(1);
//				p.setContentDescriptor(null);
//				final TrackControl tc[] = p.getTrackControls();
//				TrackControl audioTrack = null;
//				for (int i = 0; i < tc.length; i++) {
//					if (tc[i].getFormat() instanceof AudioFormat) {
//						audioTrack = tc[i];
//						break;
//					}
//				}
//				p.prefetch();

				for (Control c : p.getControls()) {
					System.out.println(c.getClass());
				}
				com.sun.media.controls.BitRateAdapter pbe = (com.sun.media.controls.BitRateAdapter)p.getControl("com.sun.media.PlaybackEngine$BitRateA");
				pbe.setBitRate(30000);
				
//				FrameRateControl control = (FrameRateControl)p.getControl("javax.media.control.FrameRateControl");
//				control.setFrameRate(44100f);
				p.start();
//			} catch (NoProcessorException e) {
//				e.printStackTrace();
			} finally {
				
			}
		} catch (NoDataSourceException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
