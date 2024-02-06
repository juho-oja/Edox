import java.util.Locale;
import java.awt.*;
import java.awt.event.*;
import java.awt.Font;
import javax.swing.*;

import java.io.FileWriter;
import java.util.ArrayList;

class Edox extends JFrame implements ActionListener {

	public static SignalHandler		audio_handler;
	private ArrayList<Interval> 	intervals;
	private ArrayList<Scale>		scales;
	private Scale 					active_scale;
	private XMLTag 					xml_scale;

	private static XMLTag settings;
	private ScaleEditor scale_editor;
	private static double uiscale;

	private JMenuBar main_menu;
		private JMenu m_file;
			private JMenuItem mi_new, mi_open, mi_open_book, mi_save, mi_saveas, mi_quit;
			private JMenuItem mi_export_txt, mi_export_svg, mi_export_html, mi_export_scala;
		private JMenu m_edit;
			private JMenuItem mi_undo;
		private JMenu m_charts;
			private JMenuItem mi_intervals;
		private JMenu m_scale;
			private JMenuItem mi_reference, mi_swaproot, mi_transpose;
		private JMenu m_tools;
			private JMenuItem mi_keyboard, mi_freeboard, mi_beatpairs, mi_calculator, mi_oscilloscope;
		private JMenu m_settings;
			private JMenuItem mi_interface, mi_settings_save, mi_settings_load, mi_compare;
		private JMenu m_about;
	private JTabbedPane tabs;

	static {
	// Load Settings
		try {
			settings = XMLTag.fromFile("settings.xml");
			uiscale = Double.parseDouble(settings.sub("uiscale").attr("factor"));
		} catch (Exception e) {
			System.out.println("Could not load settings");
			buildDefaultSettings();
		}

		try {
			if (settings.sub("tempering").attr("template").equals(""))
				Integer.parseInt("error");
		} catch (Exception e) {
			XMLTag old = settings.sub("tempering");
			XMLTag replacer = new XMLTag("tempering");
				replacer.addAttr("template", "templates/tempering_kb.xml");

			if (old == null) {
				settings.addSubtag(replacer);
			} else settings.replaceSubtag(old, replacer);
			System.out.println("Error, default tempering template set");
		}
	}

	private static void buildDefaultSettings() {
		settings = new XMLTag("settings");

	// default scaling
		uiscale = 1.0;
		XMLTag insert = new XMLTag("uiscale");
			insert.addAttribute("factor", "1.0");
		settings.addSubtag(insert);
	// default buffer
		insert = new XMLTag("buffer");
			insert.addAttribute("segment", "128");
			insert.addAttribute("count", "64");
		settings.addSubtag(insert);

		insert = new XMLTag("tempering");
			insert.addAttr("template", "templates/tempering_kb.xml");
		settings.addSubtag(insert);
	}

	public Edox() {
	// WINDOW PRIMITIVES
		setSize(1024, 768);
		setLayout(new GridLayout(1,1));
		setTitle("Edox Tuning Utility");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		scales = new ArrayList<Scale>();
		intervals = new ArrayList<Interval>();

	// WINDOW ELEMENTS
		makeMenubar();
		setJMenuBar(main_menu);

		scale_editor = new ScaleEditor(intervals);
    //  JComponent panel2 = makeTextPanel("Panel #2");
    //  JComponent panel3 = makeTextPanel("Panel #3");

		tabs = new JTabbedPane();
			tabs.addTab("Scale view", null, scale_editor, null);
		//	tabs.addTab("Player", null, panel2, null);
		//	tabs.addTab("Tuning practice", null, panel3, null);
        tabs.setMnemonicAt(0, KeyEvent.VK_1);
    //    tabs.setMnemonicAt(1, KeyEvent.VK_2);
    //    tabs.setMnemonicAt(2, KeyEvent.VK_3);
		tabs.setInputMap(0, null);

		add(tabs);
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		setVisible(true);
	}
    
    private JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
    }

	private void makeMenubar() {
		main_menu = new JMenuBar();

		m_file = new JMenu("File");
			mi_new = new JMenuItem("New scale...");		mi_new.setActionCommand("new");
			mi_new.addActionListener(this);				m_file.add(mi_new);

			mi_open = new JMenuItem("Open scale...");	mi_open.setActionCommand("open");
			mi_open.addActionListener(this);			m_file.add(mi_open);

			m_file.addSeparator();

			mi_open_book = new JMenuItem("Open book...");	mi_open_book.setActionCommand("open_book");
			mi_open_book.addActionListener(this);			m_file.add(mi_open_book);

			m_file.addSeparator();

			mi_save = new JMenuItem("Save");			mi_save.setActionCommand("save");
			mi_save.addActionListener(this);			m_file.add(mi_save);

			mi_saveas = new JMenuItem("Save As...");	mi_saveas.setActionCommand("save as");
			mi_saveas.addActionListener(this);			m_file.add(mi_saveas);

			m_file.addSeparator();

			mi_export_txt = new JMenuItem("Export as txt...");		mi_export_txt.setActionCommand("export_txt");
			mi_export_txt.addActionListener(this);					m_file.add(mi_export_txt);

			mi_export_html = new JMenuItem("Export as html...");	mi_export_html.setActionCommand("export_html");
			mi_export_html.addActionListener(this);					m_file.add(mi_export_html);

			mi_export_svg = new JMenuItem("Export as svg...");		mi_export_svg.setActionCommand("export_svg");
			mi_export_svg.addActionListener(this);					m_file.add(mi_export_svg);

			m_file.addSeparator();

			mi_quit = new JMenuItem("Exit");		mi_quit.setActionCommand("exit");
			mi_quit.addActionListener(this);		m_file.add(mi_quit);
		main_menu.add(m_file);

		m_scale = new JMenu("Scale");
			mi_reference = new JMenuItem("Set Reference Tone...");	mi_reference.setActionCommand("reference");
			mi_reference.addActionListener(this);					m_scale.add(mi_reference);

			mi_swaproot = new JMenuItem("Swap Scale Root");			mi_swaproot.setActionCommand("swaproot");
			mi_swaproot.addActionListener(this);					m_scale.add(mi_swaproot);

			mi_transpose = new JMenuItem("Transpose Scale");		mi_transpose.setActionCommand("transpose");
			mi_transpose.addActionListener(this);					m_scale.add(mi_transpose);
		main_menu.add(m_scale);

		m_tools = new JMenu("Tools");
			mi_keyboard = new JMenuItem("12-Tone Keyboard");	mi_keyboard.setActionCommand("keyboard");
			mi_keyboard.addActionListener(this);				m_tools.add(mi_keyboard);

			mi_freeboard = new JMenuItem("Freeboard");			mi_freeboard.setActionCommand("freeboard");
			mi_freeboard.addActionListener(this);				m_tools.add(mi_freeboard);

			mi_beatpairs = new JMenuItem("Overtone Beat Pairs"); mi_beatpairs.setActionCommand("beatpairs");
			mi_beatpairs.addActionListener(this);				m_tools.add(mi_beatpairs);

			mi_calculator = new JMenuItem("Calculator");		mi_calculator.setActionCommand("calculator");
			mi_calculator.addActionListener(this);				m_tools.add(mi_calculator);

			mi_oscilloscope = new JMenuItem("Oscilloscope");	mi_oscilloscope.setActionCommand("scope");
			mi_oscilloscope.addActionListener(this);			m_tools.add(mi_oscilloscope);
		main_menu.add(m_tools);
		
		m_settings = new JMenu("Settings");
			mi_settings_load = new JMenuItem("Load Settings...");	mi_settings_load.setActionCommand("load settings");
			mi_settings_load.addActionListener(this);				m_settings.add(mi_settings_load);

			mi_settings_save = new JMenuItem("Save Settings...");	mi_settings_save.setActionCommand("save settings");
			mi_settings_save.addActionListener(this);				m_settings.add(mi_settings_save);
			
			m_settings.addSeparator();

			mi_interface = new JMenuItem("Interface...");	mi_interface.setActionCommand("interface");
			mi_interface.addActionListener(this);			m_settings.add(mi_interface);

			mi_compare = new JMenuItem("Comparison intervals...");	mi_compare.setActionCommand("intervals");
			mi_compare.addActionListener(this);						m_settings.add(mi_compare);
	//	m_settings.setActionCommand("setup");
	//	m_settings.addActionListener(this);
		main_menu.add(m_settings);
		//	mi_new.setToolTipText("Creates a new scale");
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if (command.equals("new")) {;
			new NewScale(this);
			repaint();
		}

		if (command.equals("open")) {
			FileDialog file_select = new FileDialog(new JFrame(), "Choose scale XML...", FileDialog.LOAD);
			file_select.setDirectory("scales");
			file_select.setVisible(true);
			String dir = file_select.getDirectory();
			String file = file_select.getFile();
			if (dir != null && file != null) {
				System.out.println(String.format("Path: %s%s", dir, file));
				try {
					xml_scale = XMLTag.fromFile(dir + file);
					Scale loaded = new Scale(xml_scale);
					loaded.print();
					active_scale = loaded;
				/*
					for (int i = 0; i < scales.size(); i++) { // if scale already exists, replace it
					
					}
				*/
					scale_editor.updateScale(active_scale);
					repaint();
				} catch (Exception f) {
					System.out.println("Edox: Couldn't load scale...");
					return;
				}
			}
		}

		if (command.equals("save as")) {
			FileDialog file_select = new FileDialog(new JFrame(), "Save Scale As...", FileDialog.SAVE);
			file_select.setDirectory("scales");
			file_select.setVisible(true);
			String dir = file_select.getDirectory();
			String file = file_select.getFile();
			if (dir != null && file != null) {
				System.out.printf("Path: %s%s", dir, file);
				active_scale.toFile(dir + file);
			}
		}

		if (command.equals("open_book")) {
			FileDialog file_select = new FileDialog(new JFrame(), "Choose Book XML...", FileDialog.LOAD);
			file_select.setDirectory("books");
			file_select.setVisible(true);

			String dir = file_select.getDirectory();
			String file = file_select.getFile();

			if (dir != null && file != null) {
				System.out.println(String.format("Dir: %s", dir));
				System.out.println(String.format("File: %s", file));

				try {
					new BookWindow(dir, file);
				} catch (Exception f) {
					System.out.println("Edox: Couldn't load book...");
					return;
				}
			}
		}

		if (command.equals("freeboard")) {
			try {
				new FreeboardWindow(this);
			} catch (Exception f) { }
		}

		if (command.equals("keyboard")) {
			try {
				new KeyboardWindow(this);
			} catch (Exception f) {}
			return;
		}

		if (command.equals("beatpairs")) {
			new BeatPairsWindow(this);
			return;
		}

		if (command.equals("swaproot")) {
			new SwapRootWindow(this);
			return;
		}

		if (command.equals("reference")) {
			new ReferenceToneWindow(this);
			return;
		}

		if (command.equals("transpose")) {
			new TransposeWindow(this);
			return;
		}

		if (command.equals("exit")) System.exit(0);
	}

	public void setScale(Scale p_scale) {
		if (p_scale == null) return;
		else {
			active_scale = p_scale;
			scale_editor.updateScale(active_scale);
			repaint();
		}
	}

	public void refresh() {
		this.repaint();
	}

	public Scale getActiveScale() {
		return active_scale;
	}

	public XMLTag getXMLScale() {
		return xml_scale;
	}

	public static double getUiScale() {
		return uiscale;
	}

	public static XMLTag settings() {
		return settings;
	}

	public static void main(String[] args) {
		Locale.setDefault(Locale.US);
		audio_handler = new SignalHandler();
		SawGenerator.init();
		SquareGenerator.init();
		SineGenerator.init();
		audio_handler.start();
		new Edox();
	}
}
