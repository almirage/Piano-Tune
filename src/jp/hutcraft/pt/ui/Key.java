package jp.hutcraft.pt.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JPanel;

import jp.hutcraft.pt.entity.EqualTemperament;
import jp.hutcraft.pt.entity.Voice;
import jp.hutcraft.pt.util.AudioUtil;
import jp.hutcraft.pt.util.ThreadUtil;

public class Key extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private static enum Type {
		WHITE(Color.WHITE, Color.BLACK, new Color(64, 64, 255), new Dimension(40, 200)),
		BLACK(Color.BLACK, Color.WHITE, new Color(64, 64, 255), new Dimension(24, 140));
		/*package private*/ final Color bgColor;
		/*package private*/ final Color frontColor;
		/*package private*/ final Color resultColor;
		/*package private*/ final Dimension size;
		private Type(final Color bgColor, final Color frontColor, final Color resultColor, final Dimension size) {
			this.bgColor = bgColor;
			this.frontColor = frontColor;
			this.resultColor = resultColor;
			this.size = size;
		}
	}
	
	private static final double adjustRange = EqualTemperament.harfToneRate - 1D;
	private /*final*/ boolean selected = false;
	private /*final*/ double adjust; // 単位はセント、100セントで半音。double型じゃなくてcent型にすればいいんじゃね？
	private final List<Component> tuneControllers = new ArrayList<Component>();
	private /*final*/ boolean displayResult = false;
	private final Type type;
	private /*final*/ boolean clicked = false;
	
	private final Voice voice;
	public Key(final Voice v) {
		voice = v;
		type = v.isBlack() ? Type.BLACK : Type.WHITE;
		setSize(type.size);
		setBackground(type.bgColor);
		addMouseListener(new MouseListener(){
			@Override public void mouseClicked(final MouseEvent arg0) {}
			@Override public void mouseEntered(final MouseEvent arg0) {}
			@Override public void mouseExited(final MouseEvent arg0) {}
			@Override public void mousePressed(final MouseEvent event) {
				if (clickedWithShift(event)) {
					selected = !selected;
					getParent().repaint();
					return;
				}
				play();
			}
			@Override public void mouseReleased(final MouseEvent arg0) {}
		});

		setVisible(true);
	}
	
	private static boolean clickedWithShift(final MouseEvent event) {
		return 						(event.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK ||
		(event.getModifiers() & InputEvent.BUTTON2_MASK) == InputEvent.BUTTON2_MASK ||
		(event.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK;
	}

	@Override
	public void paint(final Graphics g) {
		super.paint(g);
		final Font defaultFont = getFont();
		if (selected || clicked) {
			g.setColor(new Color(255, 128, 128));
			g.fillRect(0, 0, getSize().width, getSize().height);
		}
		g.setColor(Color.BLACK); // 枠表示
		g.drawRect(0, 0, getSize().width-1, getSize().height-1);
		if (!voice.isBlack()) {
			// 白鍵のラベルを表示する。黒鍵のラベルはいいや。
			g.setColor(type.frontColor);
			g.drawString(voice.getLabel(), 2, getSize().height - defaultFont.getSize());
		}
		if (displayResult) {
			// 答え合わせ
			g.setColor(type.resultColor);
			g.drawString(String.format("%+.0f", adjust), 2, getSize().height - defaultFont.getSize() * 2);
		}
		refreshController();
	}

	private void refreshController() {
		for (final Component c : tuneControllers) c.setVisible(selected);
	}

	public void play() {
		AudioUtil.play(voice, voice.getAdjust() + getTuneRate());
		clicked = true;
		getParent().repaint();
		new Thread(new Runnable(){
			@Override public void run() {
				ThreadUtil.sleep(200);
				clicked = false;
				getParent().repaint();
			}}).start();
	}
	public Voice getVoice() {
		return voice;
	}
	public void playIfSelected() {
		if (selected) play();
	}
	private double getTuneRate() {
		return adjustRange * adjust / 100;
	}
	public synchronized void tunePlus() {
		adjust = adjust + 1;
		AudioUtil.changePlayingRate(voice, voice.getAdjust() + getTuneRate());
	}
	public synchronized void tuneMinus() {
		adjust = adjust - 1;
		AudioUtil.changePlayingRate(voice, voice.getAdjust() + getTuneRate());
	}
	public void honkytonk() {
		if (voice == Voice.An3) {
			adjust = 0;
			return;
		}
		adjust = new Random().nextInt(100) - 50;
	}
	public void addController(final Component controller) {
		tuneControllers.add(controller);
		refreshController();
	}
	public void clearControllers() {
		tuneControllers.clear();
	}

	public void setDisplayResult(boolean displayResult) {
		this.displayResult = displayResult;
	}

	public void reset() {
		adjust = 0;
	}

}
