package jp.hutcraft.pt.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jp.hutcraft.pt.entity.Voice;
import jp.hutcraft.pt.util.AudioUtil;

public class GuiMain extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final String version = "1.0";

	private final List<Key> displayKeys = new ArrayList<Key>();
	private final List<Key> allKeys = new ArrayList<Key>();
	private final JPanel keyPanel = new JPanel();
	private final OctavePane octaveController = new OctavePane();
	private final JPanel menuPanel = new MenuPane();
	private final JPanel tuneController = new JPanel();
	private final GuiKeyListener keyListener = new GuiKeyListener();
	private final HowToUsePane howToUsePane = new HowToUsePane();

	public GuiMain() {
		initGuiBase();

		initTuneController();
		initKeys();
		initOctaveController();
		initMenu();
		
		setVisible(true);
		
		soundTest();
	}

	private void soundTest() {
		// exceptionをexpectする、こういう書き方はしないほうがいいです。
		try {
			AudioUtil.test();
		} catch (final Exception e) {
			JOptionPane.showMessageDialog(null,
					"An error occurred in testing sounds.\r\n" +
					"サウンド再生が行えないため、この環境（パソコン）では正しく動きません。\r\n"+ e);
		}
	}

	private void initGuiBase() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle(String.format("%s ver%s", "Piano Tune", version));
//		setIconImage();
		setBounds(new Rectangle(0, 0, 800, 600));
		setLayout(null);
		
		howToUsePane.setBounds(20, 380, getSize().width - 40, 160);
		add(howToUsePane);
		
		addKeyListener(keyListener);
	}
	
	// ちょっちゲームっぽく使いたいんで、子コンポーネントはフォーカスとれないようにする
	// GuiKeyListenerを動かすには必須
	@Override
	public Component add(final Component c) {
		disableFocus(c);
		return super.add(c);
	}

	private void disableFocus(final Component c) {
		c.setFocusable(false);
		if (c instanceof Container) {
			final Container container = (Container)c;
			for (final Component child : container.getComponents()) disableFocus(child);
		}
	}

	private void initTuneController() {
		tuneController.setBounds(20, 270, getSize().width - 40, 40);
		tuneController.setLayout(null);
		add(tuneController);
	}

	private void initMenu() {
		menuPanel.setBounds(20, 330, getSize().width - 40, 50);
		add(menuPanel);
	}
	
	private void initOctaveController() {
		octaveController.setBounds(20, 20, getBounds().width - 40, 40);
		octaveController.init();
		add(octaveController);
	}

	private void playSelected() {
		for (final Key k : displayKeys) {
			k.playIfSelected();
		}
	}

	private void initKeys() {
		keyPanel.setBounds(20, 60, getBounds().width - 40, 200);
		keyPanel.setLayout(null);
		add(keyPanel);
		for (final Voice v : Voice.getInstances()) {
			allKeys.add(new Key(v));
		}
	}
	
	private void refreshDisplayKeys(final int startIndex) {
		if (startIndex < 0) throw new IllegalArgumentException("startIndex = "+ startIndex);
		for (final Key k : displayKeys) k.clearControllers();
		displayKeys.clear();
		keyPanel.removeAll();
		final int toIndex = startIndex + (startIndex + 29 > allKeys.size()  ? allKeys.size() - startIndex : 29);
		for (final Key k : allKeys.subList(startIndex, toIndex)) {
			displayKeys.add(k);
		}
		prepareDisplayKeys();
	}
	
	private void prepareDisplayKeys() {
		final Key firstKey = displayKeys.get(0);
		for (final Key k : displayKeys) {
			// 黒鍵だけ
			if (!k.getVoice().isBlack()) continue;
			k.setLocation(calcKeyPositionX(k.getVoice(), firstKey.getVoice()), 0);
			keyPanel.add(k);
		}
		for (final Key k : displayKeys) {
			// 今度は白鍵だけ
			if (k.getVoice().isBlack()) continue;
			k.setLocation(calcKeyPositionX(k.getVoice(), firstKey.getVoice()), 0);
			keyPanel.add(k);
		}
		tuneController.removeAll();
		for (final Key k : displayKeys) {
			final JButton upButton = new JButton("up");
			upButton.setSize(40, 20);
			upButton.setLocation(calcKeyPositionX(k.getVoice(), firstKey.getVoice()), 0);
			upButton.addActionListener(new ActionListener(){
				@Override public void actionPerformed(final ActionEvent e) {
					k.tunePlus();
					keyPanel.repaint();
				}});
			final JButton downButton = new JButton("dwn");
			downButton.setSize(40, 20);
			downButton.setLocation(calcKeyPositionX(k.getVoice(), firstKey.getVoice()), 20);
			downButton.addActionListener(new ActionListener(){
				@Override public void actionPerformed(final ActionEvent e) {
					k.tuneMinus();
					keyPanel.repaint();
				}});
			disableFocus(upButton);
			disableFocus(downButton);
			tuneController.add(upButton);
			tuneController.add(downButton);
			k.addController(upButton);
			k.addController(downButton);
		}
		tuneController.repaint();
		keyPanel.repaint();
		keyListener.refreshKeyMap();
	}

	private int calcKeyPositionX(Voice to, Voice from) {
		if (to.equals(from)) return 0;
		final List<Voice> instances = Voice.getInstances();
		final int distance = instances.indexOf(to) - instances.indexOf(from);
		final int firstLayoutIndex = KeyLayout.getInstances().indexOf(KeyLayout.valueOf(""+from.name().charAt(0)+from.name().charAt(1)));
		
		final List<KeyLayout> layoutInstances = KeyLayout.getInstances();
		int result = 0;
		for (int i = 1; i <= distance; i++) { // 最初の１要素のオフセットは無視するので１オリジン
			result += layoutInstances.get((firstLayoutIndex + i) % (layoutInstances.size())).getLeftMargin();
		}
		return result;
	}
	
	private class MenuPane extends JPanel {
		private static final long serialVersionUID = 1L;
		private MenuPane() {
			initResultsButton();
			initResetButton();
			initHonkeytonkButton();
			initPlayButton();
			initVolumeSlider();
		}

		private void initVolumeSlider() {
			final JSlider volumeSlider = new JSlider();
			volumeSlider.addChangeListener(new ChangeListener(){
				@Override
				public void stateChanged(final ChangeEvent event) {
					AudioUtil.setMasterVolume(volumeSlider.getValue());
				}});
			volumeSlider.setValue(100);
//			add(volumeSlider);
		}

		private void initPlayButton() {
			final JButton playButton = new JButton("play selected keys");
			playButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(final ActionEvent event) {
					playSelected();
				}});
			add(playButton);
		}

		private void initHonkeytonkButton() {
			final JButton createHonkeytonkButton = new JButton("create honkeytonk");
			createHonkeytonkButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(final ActionEvent event) {
					final int result = JOptionPane.showConfirmDialog(null,
							"it makes the keys out of order.\n" +
							"the current working set will be destroyed.\n" +
							"you can start tuning from A3 that will be tuned 440hz automatically.");
					if (result != JOptionPane.OK_OPTION) return;
					for (final Key k : allKeys) {
						k.honkytonk();
					}
					keyPanel.repaint();
				}});
			add(createHonkeytonkButton);
		}
		
		private void initResetButton() {
			final JButton createHonkeytonkButton = new JButton("reset");
			createHonkeytonkButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(final ActionEvent event) {
					final int result = JOptionPane.showConfirmDialog(null,
							"it makes the keys equal temperament.\n" +
							"the current working set will be destroyed.\n");
					if (result != JOptionPane.OK_OPTION) return;
					for (final Key k : allKeys) {
						k.reset();
					}
					keyPanel.repaint();
				}});
			add(createHonkeytonkButton);
		}

		private void initResultsButton() {
			final JToggleButton resultsButton = new JToggleButton("results");
			resultsButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(final ActionEvent event) {
					if (resultsButton.isSelected()) {
						final int result = JOptionPane.showConfirmDialog(null, "really?");
						if (result != JOptionPane.OK_OPTION) {
							resultsButton.setSelected(false); // もどす
							return;
						}
					}
					for (final Key k : allKeys) {
						k.setDisplayResult(resultsButton.isSelected());
					}
					keyPanel.repaint();
				}});
			add(resultsButton);
		}
	}
	
	private class OctavePane extends JPanel {
		private static final long serialVersionUID = 1L;
		// doClick()のために遅延初期化が必要
		private void init() {
			final ButtonGroup group = new ButtonGroup();
			for (int i = 0; i < 6; i++) {
				final JButton btn = new JButton("octave "+ (i+1));
				final int index = i;
				btn.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(final ActionEvent event) {
						final int margin = index == 0 ? 0 : 3; // 0オクターブ時はAn-1~Bn-1までのおまけ領域を表示する。それ以外はCからスタート
						refreshDisplayKeys(index * 12 + margin);
						final JButton sourceButton = (JButton)event.getSource(); // ぎゃー
						group.setSelected(sourceButton.getModel(), true);
					}});
				add(btn);
				group.add(btn);
				keyListener.addOctaveTab(btn);
				if (index == 2) {
					EventQueue.invokeLater(new Runnable(){
						@Override
						public void run() {
							btn.doClick(); // デフォルト2
							btn.requestFocusInWindow();
						}});
				}
			}
		}
	}
	
	private static class HowToUsePane extends JPanel {
		private static final long serialVersionUID = 1L;
		private static final String msg =
				"調律の練習をするアプリケーションです。\n" +
				"ホンキートンク（ずれたピアノの意味）を作成し、各キーを調律してください。\n" +
				"ホンキートンク作成時、A3だけが440Hzになります。ここから調律を始めてください。\n"+
				"右クリック、Shift+クリックでキーを選択、スペースキーで選択中のキーを発音。\n" +
				"[1~6]: オクターブ切り替え、 [a~l, w~p]: 個別に発音、[ESC]: 発音中の音を止めます。\n" +
				"キー調整のUp、Downボタンは1クリックで1セントです。\n" +
				"\n" +
				"Thank you for your playing. Enjoy tuning life. 2011 Hutcracft";
		private HowToUsePane() {
			setLayout(new GridLayout(1, 1));
			final JLabel label = new JLabel();
			label.setText("<html>"+ msg.replaceAll("\n", "<br>") +"</html>");
			add(label);
		}
		@Override
		public void paint(final Graphics g) {
			super.paint(g);
		}
	}
	
	private class GuiKeyListener implements KeyListener {
		private final List<JButton> octaveButtons = new ArrayList<JButton>();
		private final List<Character> allKeys = new ArrayList<Character>(
				Arrays.asList(new Character[]{'a','w','s','e','d','r','f','t','g','y','h','u','j','i','k','o','l','p',';','[','\'',']'}));
		private final Map<Character, Key> keyMap = new HashMap<Character, Key>();
		@Override
		public void keyPressed(final KeyEvent event) {
			if (event.getKeyChar() == ' ') {
				playSelected();
			}
			else if (event.getKeyChar() >= '1' && event.getKeyChar() <= '6') {
				octaveButtons.get(event.getKeyChar() - '1').doClick();
			}
			else if (keyMap.containsKey(event.getKeyChar())) {
				keyMap.get(event.getKeyChar()).play();
			}
			else if (event.getKeyChar() == KeyEvent.VK_ESCAPE) {
				AudioUtil.stopAll();
			}
		}
		private void refreshKeyMap() {
			keyMap.clear();
			int index = 0;
			for (final Key k : displayKeys) {
				if (allKeys.size() <= index) break;
				keyMap.put(allKeys.get(index), k);
				index ++;
				if (k.getVoice().getLabel().startsWith("E") || k.getVoice().getLabel().startsWith("B")) {
					index ++;
				}
			}
		}
		@Override
		public void keyReleased(final KeyEvent event) {
		}
		@Override
		public void keyTyped(final KeyEvent event) {
		}
		public void addOctaveTab(JButton btn) {
			octaveButtons.add(btn);
		}
	}
	
	private static enum KeyLayout {
		Cn(40),
		Cs(24),
		Dn(16),
		Ds(30),
		En(10),
		Fn(40),
		Fs(24),
		Gn(16),
		Gs(28),
		An(12),
		As(30),
		Bn(10);
		private static List<KeyLayout> instances = new ArrayList<KeyLayout>();
		static {
			instances.addAll(Arrays.asList(values()));
		}
		public static List<KeyLayout> getInstances() {
			return Collections.unmodifiableList(instances);
		}
		private final int leftMargin;
		private KeyLayout(final int leftMargin) {
			this.leftMargin = leftMargin;
		}
		public int getLeftMargin() {
			return leftMargin;
		}
	}
	public static void main(final String[] args) {
		new GuiMain();
	}
}
