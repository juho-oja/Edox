import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.imageio.*;

import java.util.ArrayList;

// default page size = 800 x 600

class BookPage {
	private static double oscillator_height = 130.0;
	private static double oscillator_width = 80.0;

	private static double small_height = 80.0;
	private static double small_width = 40.0;

	private XMLTag 		xmlpage;
	private BookWindow	host;
	private double		scale;

	private int			sx, sy;				// shift x & y, set by book window which handles all mouse events
	private Font		default_font;		// 10-point Arial, used for oscillator text
	private int			sleep_ms;
	private int			selected_osc;

	private int		lbuttony, lbuttonh;		// y coordinate and height for the next and prev page buttons

//	Page elements
	private ArrayList<Textline>			textlines;
	private ArrayList<BookImage>		pictures;
	protected ArrayList<Oscillator>		oscillators;
	protected ArrayList<Oscillator>		hidden_oscillators;
		private ArrayList<OscState>		osc_states;
		private char[]					osc_keys;
		private char[]					hidden_keys;
	private ArrayList<Indicator>		indicators;
	private ArrayList<Fkey>				fkeys;

	ArrayList<Oscillator> getOscillators() {
		return oscillators;
	}

	ArrayList<Oscillator> getHiddenOscillators() {
		return hidden_oscillators;
	}

// Basic Constructor
	public BookPage(BookWindow p_host, XMLTag p_page) {
	//	System.out.println("\tInside BookPage()");
		if (p_page == null || !p_page.name().equals("page"))
			throw new IllegalArgumentException("BookPage(): provided XML is null or not a page");

		host = p_host;
		xmlpage = p_page;

		default_font = new Font("Arial", 0, 12);
		textlines = new ArrayList<Textline>();
		pictures = new ArrayList<BookImage>();
		oscillators = new ArrayList<Oscillator>();
		hidden_oscillators = new ArrayList<Oscillator>();
		indicators = new ArrayList<Indicator>();
		fkeys = new ArrayList<Fkey>();		// F1 - F12 key grouped oscillator activations
		sleep_ms = Edox.audio_handler.getSleep();

		XMLTag tag = null;
		for (int i = 0; i < xmlpage.nsub(); i++) {

		// handle text lines
			if (xmlpage.sub(i).name().equals("line"))
				constr_handleTextLine(xmlpage.sub(i));

			if (xmlpage.sub(i).name().equals("img"))
				constr_handleImage(xmlpage.sub(i));

		// handle oscillators
			if (xmlpage.sub(i).name().equals("osc"))
				constr_handleOscillator(xmlpage.sub(i));

		}
	// do some stuff
		if (oscillators.size() == 0) selected_osc = -1;
		else makeKeys();
	}

	private void makeKeys() {
		osc_keys = new char[oscillators.size()];
		hidden_keys = new char[hidden_oscillators.size()];

		for (int i = 0; i < osc_keys.length; i++) {
			if (oscillators.get(i).key() != null)
				osc_keys[i] = oscillators.get(i).key().charAt(0);
			else osc_keys[i] = 0;
		}

		for (int i = 0; i < hidden_keys.length; i++) {
			if (hidden_oscillators.get(i).key() != null)
				hidden_keys[i] = hidden_oscillators.get(i).key().charAt(0);
			else hidden_keys[i] = 0;
		}
	}

	private void constr_handleTextLine(XMLTag tag) {
		Textline text = new Textline();
		text.font = default_font;

		try {
			text.x = Integer.parseInt(tag.attr("x"));
			text.y = Integer.parseInt(tag.attr("y"));
			text.text = tag.text();	// no need to use, because returned text is already new
			if (text.text != null) textlines.add(text);
		} catch (Exception e) {
			System.out.println("BookPage(): failed to handle a text line");
		}

		try {
			text.font_base = Integer.parseInt(tag.attr("s"));
		} catch (Exception e) {
			text.font_base = 14;
		} finally {
			text.font = new java.awt.Font("Serif", 0, text.font_base);
		}
	}

	private void constr_handleImage(XMLTag tag) {
		BookImage i = new BookImage();
	// non-essentials
		try {
			i.w = Double.parseDouble(tag.attr("w"));
		} catch (Exception e) {
			try {
				i.sc = Double.parseDouble(tag.attr("scale"));
			} catch (Exception f) { }
		}

	//	System.out.printf("BookPage.constr_handleImage(): width is %f\n", i.w);
	// must-haves
		try {
			i.x = Double.parseDouble(tag.attr("x")); i.y = Double.parseDouble(tag.attr("y"));
			i.loadImage(new File(host.getDir() + tag.attr("file")));
			pictures.add(i);
		//	System.out.println("BookPage.constr_handleImage(): Add image " + host.getDir() + tag.attr("file"));
		} catch (Exception e) {
			if (host.getDir() == null) System.out.println("BookPage.constr_handleImage(): host directory is null");
			if (tag.attr("file") != null) System.out.println("BookPage.constr_handleImage(): Failed to load file " + tag.attr("file") + ": " + e.getMessage());
		}
	}

	private void constr_handleOscillator(XMLTag tag) {
		String type = tag.attr("type");
	//	basics, which must be met
		Oscillator newosc = new Oscillator();

		double random_shift = 0.0;
		try {
			random_shift = Double.parseDouble(tag.attr("random"));
			random_shift = random_shift * (Math.random() - 0.5);
		//	System.out.printf("Found random with value %.4f\n", random_shift);
		} catch (Exception e) { }

		boolean hidden = false;
		try {
			if (tag.attr("type").equals("hidden")) hidden = true;
		} catch (Exception e) { }

		try {
			newosc.setXY(Integer.parseInt(tag.attr("x")), Integer.parseInt(tag.attr("y")));
			double init = Double.parseDouble(tag.attr("init"));
			newosc.setFreq(init + random_shift); newosc.setInit(init + random_shift);
		//	newosc.toggleActive();
			if (hidden == true) hidden_oscillators.add(newosc);
			else oscillators.add(newosc);
		} catch (Exception e) {
			System.out.println("BookPage(): Failed to handle an oscillator: " + tag.headerText());
		}

	//	nonesstential stuff
		try {
			newosc.setGuiType(tag.attr("size"));
		} catch (Exception e) { }

		try {
			newosc.setMin(Double.parseDouble(tag.attr("min")));
		} catch (Exception e) { }

		try {
			newosc.setMax(Double.parseDouble(tag.attr("max")));
		} catch (Exception e) { }

		try {
			newosc.setVol((float)Double.parseDouble(tag.attr("vol")));
		} catch (Exception e) { }

		try {
			if (tag.attr("disp").equals("true"))
				newosc.setDisplay(true);
		} catch (Exception e) { }
		
		try {
			newosc.setWaveform(tag.attr("wave"));
		} catch (Exception e) { }

		try {
			newosc.setKey(tag.attr("key"));
		} catch (Exception e) { }

		try {
			newosc.setName(tag.attr("name"));
		} catch (Exception e) { }

		try {
			newosc.setId(tag.attr("id"));
		} catch (Exception e) { }

		try {
			if (tag.attr("type").equals("fixed")) newosc.toggleFixed();
		} catch (Exception e) { }
	
		try {
			if (tag.attr("active").equals("true")) newosc.toggleActive();
		} catch (Exception e) { }

		try {
			newosc.setColor(constr_parseColor(tag.attr("color")));
		} catch (Exception e) {
			System.out.printf("BookPage%s\n  %s\n", e.getMessage(), tag.headerText());
		}
	}
/*
	@Override
	public void run() {
		this.sleep(sleep_ms);
	}
*/
	public void activate() {
	//	Edox.audio_handler.clearOscillators();
	//	for (int i = 0; i < oscillators.size(); i++)
	//		Edox.audio_handler.addOscillator(oscillators.get(i));
		Edox.audio_handler.newContext(oscillators);
		Edox.audio_handler.newContext(hidden_oscillators);
	//	for (int i = 0; i < hidden_oscillators.size(); i++)
	//		Edox.audio_handler.addOscillator(hidden_oscillators.get(i));
	}

	public void deactivate() {
		Edox.audio_handler.removeContext(oscillators);
		Edox.audio_handler.removeContext(hidden_oscillators);
	}

	public void reset() {
		for (int i = 0; i < oscillators.size(); i++)
			oscillators.get(i).reset();
		for (int i = 0; i < hidden_oscillators.size(); i++)
			hidden_oscillators.get(i).reset();
	}

	private class Textline {
		public Font font;
		public int x, y, font_base;
		public String text;

		public Textline() {

		}
	}
/*
	public void prevOsc() {
		if (selected_osc > 0) {
			selected_osc--;

		// skip all fixed oscillators while going back towards the beginning
			if (oscillators.get(selected_osc).fixed())
				for (; selected_osc > 0 && oscillators.get(selected_osc).fixed(); selected_osc--);
			else return;

		// if the oscillator is still fixed, loop back towards the end until a free osc is found
			if (oscillators.get(selected_osc).fixed())
				for (; selected_osc < oscillators.size() && oscillators.get(selected_osc).fixed(); selected_osc++);
			else return;
		
		// if every single oscillator was fixed, set it to -1 so that inputs can be ignored altogether
			if (oscillators.get(selected_osc).fixed()) selected_osc = -1;
		}
	}

	public void nextOsc() {
		if (selected_osc < oscillators.size() - 1) {
			selected_osc++;

		// skip all fixed oscillators while going back towards the beginning
			if (oscillators.get(selected_osc).fixed())
				for (; selected_osc < oscillators.size() && oscillators.get(selected_osc).fixed(); selected_osc++);
			else return;

		// if the oscillator is still fixed, loop back towards the beginning until a free osc is found
			if (oscillators.get(selected_osc).fixed())
				for (; selected_osc > 0 && oscillators.get(selected_osc).fixed(); selected_osc--);
			else return;
		
		// if every single oscillator was fixed, set it to -1 so that inputs can be ignored altogether
			if (oscillators.get(selected_osc).fixed()) selected_osc = -1;
		}
	}
*/
	public void keyPressed(KeyEvent press) {
		int mods = press.getModifiersEx();

		if (press.getExtendedKeyCode() == KeyEvent.VK_ESCAPE) {
			for (int i = 0; i < oscillators.size(); i++)
				oscillators.get(i).setActive(false);
			return;
		}

	// characters
		char keychar = press.getKeyChar();
	//	if (keychar >= 'a' && keychar <= 'z') keychar -= 'a' - 'A';
		
	// lower case letters = intermittent play
		if ((keychar >= 'a' && keychar <= 'z') || (keychar >= '0' && keychar <= '9')) {
			for (int i = 0; i < osc_keys.length; i++) {
				if (keychar == osc_keys[i]) {
					selected_osc = i;
					if (oscillators.get(i).active() == false) {
						oscillators.get(i).setIntermittent(true);
						oscillators.get(i).setActive(true);
					}
				//	Edox.audio_handler.addOscillator(oscillators.get(i));
				}
			}
			for (int i = 0; i < hidden_keys.length; i++) {
				if (keychar == hidden_keys[i]) {
					if (hidden_oscillators.get(i).active() == false) {
						hidden_oscillators.get(i).setIntermittent(true);
						hidden_oscillators.get(i).setActive(true);
					}
				//	Edox.audio_handler.addOscillator(oscillators.get(i));
				}
			}
			return;	// if it was a number or letter but not found, no need to check anything else
		}

	//	System.out.printf("BookPage.keyPressed: %c\n", keychar);

	// Arrow keys
		if (selected_osc >= 0) {
			if (oscillators.get(selected_osc).fixed()) return;

			if ((mods & KeyEvent.SHIFT_DOWN_MASK) > 0) {
				if (press.getKeyCode() == KeyEvent.VK_UP)
					{ oscillators.get(selected_osc).setState(3); return; }

				if (press.getKeyCode() == KeyEvent.VK_DOWN)
					{ oscillators.get(selected_osc).setState(-3); return; }
			}

			if ((mods & KeyEvent.CTRL_DOWN_MASK) > 0) {
				if (press.getKeyCode() == KeyEvent.VK_UP)
					{ oscillators.get(selected_osc).setState(1); return; }

				if (press.getKeyCode() == KeyEvent.VK_DOWN)
					{ oscillators.get(selected_osc).setState(-1); return; }
			}

			if (press.getKeyCode() == KeyEvent.VK_UP)
				{ oscillators.get(selected_osc).setState(2); return; }

			if (press.getKeyCode() == KeyEvent.VK_DOWN)
				{ oscillators.get(selected_osc).setState(-2); return; }
		}

	// Letters and numbers ("playing" oscillators)
	}

	public void keyReleased(KeyEvent press) {
		int mods = press.getModifiersEx();

	// characters
		char keychar = press.getKeyChar();
		int keycode = press.getKeyCode();

		if (keychar >= 'A' && keychar <= 'Z') keychar += 'a' - 'A';

	// lower case letters = intermittent play
		if ((keychar >= 'a' && keychar <= 'z') || (keycode >= '0' && keycode <= '9')) {
			for (int i = 0; i < osc_keys.length; i++) {
				if (keychar == osc_keys[i] || keycode == (int)osc_keys[i]) {
			// check the shift-variant first
					if ((mods & KeyEvent.SHIFT_DOWN_MASK) > 0) {
						oscillators.get(i).toggleActive();
						oscillators.get(i).setIntermittent(false);
						selected_osc = i;
					} else {
						if (oscillators.get(i).intermittent() == true) {
							oscillators.get(i).setIntermittent(false);
							oscillators.get(i).setActive(false);
						//	Edox.audio_handler.removeOscillator(oscillators.get(i));
						}
					}
				}
			}
			for (int i = 0; i < hidden_keys.length; i++) {
				if (keychar == hidden_keys[i] || keycode == (int)hidden_keys[i]) {
			// check the shift-variant first
					if ((mods & KeyEvent.SHIFT_DOWN_MASK) > 0) {
						hidden_oscillators.get(i).toggleActive();
						hidden_oscillators.get(i).setIntermittent(false);
					} else {
						if (hidden_oscillators.get(i).intermittent() == true) {
							hidden_oscillators.get(i).setIntermittent(false);
							hidden_oscillators.get(i).setActive(false);
						//	Edox.audio_handler.removeOscillator(oscillators.get(i));
						}
					}
				}
			}
			return;
		}


		if (selected_osc >= 0) {

			if (keycode == KeyEvent.VK_ENTER)
				System.out.printf("Oscillator %s: %.6f\n", oscillators.get(selected_osc).name(), oscillators.get(selected_osc).getFreq());

			if (keycode == KeyEvent.VK_PAGE_UP) {
				oscillators.get(selected_osc).volUp();
				return;
			}

			if (keycode == KeyEvent.VK_PAGE_DOWN) {
				oscillators.get(selected_osc).volDown();
				return;
			}

			if (press.getKeyCode() == KeyEvent.VK_SPACE) {
				oscillators.get(selected_osc).toggleActive();
			/*	if (oscillators.get(selected_osc).active()) Edox.audio_handler.addOscillator(oscillators.get(selected_osc));
				else Edox.audio_handler.removeOscillator(oscillators.get(selected_osc)); */
			}

			if (press.getKeyCode() == KeyEvent.VK_LEFT) {
				if (selected_osc > 0) {
					oscillators.get(selected_osc).setState(0);
					selected_osc--;
				//	System.out.printf("Selected osc = %d\n", selected_osc);
				}
			}
			if (press.getKeyCode() == KeyEvent.VK_RIGHT) {
				if (selected_osc < oscillators.size() - 1) {
					oscillators.get(selected_osc).setState(0);
					selected_osc++;
				//	System.out.printf("Selected osc = %d\n", selected_osc);
				}
			}

			if (press.getKeyCode() == KeyEvent.VK_UP)
				{ oscillators.get(selected_osc).setState(0); return; }

			if (press.getKeyCode() == KeyEvent.VK_DOWN)
				{ oscillators.get(selected_osc).setState(0); return; }
		}
	}

	public void draw(Graphics pg, double pscale) {
		Graphics2D g = (Graphics2D)pg;
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

	// if scale changes, update all fonts
		if (pscale != scale) {
		//	System.out.println("BookPage.draw(): Updating font sizes...");
			default_font = new java.awt.Font("Serif", 0, (int)(12.0 * pscale));
			for (int i = 0; i < textlines.size(); i++)
				textlines.get(i).font = new java.awt.Font("Serif", 0,
					(int)((double)textlines.get(i).font_base * pscale));
			scale = pscale;
		}

		for (int i = 0; i < textlines.size(); i++) {
			Textline thisline = textlines.get(i);
			g.setFont(thisline.font);
			g.setColor(new java.awt.Color(0,0,0));
			g.drawString(textlines.get(i).text, (int)(textlines.get(i).x * scale), (int)(textlines.get(i).y * scale));
		}

		if (pictures != null) {
			for (int i = 0; i < pictures.size(); i++) {
				g.drawImage(pictures.get(i).image, (int)(pictures.get(i).x * scale), (int)(pictures.get(i).y), null);
			}
		}

	// Draw border around selected oscillator

		if (selected_osc >= 0) {
			double ulx = (oscillators.get(selected_osc).x() - 3.0) * scale;
			double uly = (oscillators.get(selected_osc).y() - 3.0) * scale;

			g.setColor(Colors.selection);

	border: {
				int type = oscillators.get(selected_osc).getGuiType();
				if (type == 0) {
					g.fillRect((int)ulx, (int)uly, (int)((oscillator_width + 6.0) * scale), (int)((oscillator_height + 6.0) * scale));
					break border;
				}
	
				if (type == 1) {
					g.fillRect((int)ulx, (int)uly, (int)((small_width + 6.0) * scale), (int)((small_height + 6.0) * scale));
				}
			}
		}

	// Draw oscillator
		for (int i = 0; i < oscillators.size(); i++) {
			if (oscillators.get(i).fixed()) g.setColor(Colors.fixed);
			else g.setColor(oscillators.get(i).color);

			double tx = oscillators.get(i).x() * scale;
			double ty = oscillators.get(i).y() * scale;

			if (oscillators.get(i).getGuiType() == 0) {
				g.fillRect((int)tx, (int)ty, (int)(oscillator_width * scale), (int)(oscillator_height * scale));
				g.setColor(Colors.black);
				g.drawRect((int)tx, (int)ty, (int)(oscillator_width * scale), (int)(oscillator_height * scale));

				g.setFont(default_font);
				if (oscillators.get(i).name() != null) {
					g.drawString(oscillators.get(i).name(), (int)(tx + (7.0 * scale)), (int)(ty + (20.0 * scale)));
				}

				if (oscillators.get(i).display())
					g.drawString(String.format("%.2f", oscillators.get(i).getFreq()), (int)(tx + (7.0 * scale)), (int)(ty + (42.0 * scale)));

				if (oscillators.get(i).active()) g.setColor(Colors.active);
				else g.setColor(Colors.inactive);
			
				tx = (oscillators.get(i).x() + 25.0) * scale;
				ty = (oscillators.get(i).y() + 65.0) * scale;
				g.fillRect((int)tx, (int)ty, (int)(30.0 * scale), (int)(30.0 * scale));

				g.setColor(Colors.black);
				g.drawRect((int)tx, (int)ty, (int)(30.0 * scale), (int)(30.0 * scale));

				if (oscillators.get(i).key() != null) {
					g.drawString(oscillators.get(i).key(), (int)(tx + (12.0 * scale)), (int)(ty + (55.0 * scale)));
				}
			}

			if (oscillators.get(i).getGuiType() == 1) {
				g.fillRect((int)tx, (int)ty, (int)(small_width * scale), (int)(small_height * scale));
				g.setColor(Colors.black);
				g.drawRect((int)tx, (int)ty, (int)(small_width * scale), (int)(small_height * scale));

				g.setFont(default_font);
				if (oscillators.get(i).name() != null) {
					g.drawString(oscillators.get(i).name(), (int)(tx + (5.0 * scale)), (int)(ty + (20.0 * scale)));
				}

				if (oscillators.get(i).active()) g.setColor(Colors.active);
				else g.setColor(Colors.inactive);
			
				tx = (oscillators.get(i).x() + 10.0) * scale;
				ty = (oscillators.get(i).y() + 30.0) * scale;
				g.fillRect((int)tx, (int)ty, (int)(20.0 * scale), (int)(20.0 * scale));

				g.setColor(Colors.black);
				g.drawRect((int)tx, (int)ty, (int)(20.0 * scale), (int)(20.0 * scale));

				if (oscillators.get(i).key() != null) {
					g.drawString(oscillators.get(i).key(), (int)(tx + (6.0 * scale)), (int)(ty + (40.0 * scale)));
				}

			}
		}

		for (int i = 0; i < indicators.size(); i++) {

		}
	//	Draw box around selected oscillator
	}

	public void processMouseclick(int x, int y, int button) {

	}

	public void processKeyPress(char key) {

	}

	private class Fkey {
		public ArrayList<Oscillator> oscs;
	}

	private class OscState {
		double freq;
		boolean active;

		public OscState(double p_freq, boolean p_active) {
			freq = p_freq; active = p_active;
		}
	}

	private class BookImage {
		protected double x, y, w, h, sc;
		protected Image image;

		public BookImage() {
			w = -1.0;
			h = -1.0;
			sc = 1.0;
		}

		public void loadImage(File pfile) {
			try {
				double tscale = Edox.getUiScale();
				image = ImageIO.read(pfile);
				if (w != -1.0) {
					image = image.getScaledInstance((int)(tscale * w), (int)(h * tscale), Image.SCALE_SMOOTH);
					return;
				}

				if (sc != 1.0) {
					image = image.getScaledInstance((int)(tscale * image.getWidth(null) * sc), -1, Image.SCALE_SMOOTH);
					return;
				}

				if (tscale != 1.0) {
				//	System.out.println("Image width is " + image.getWidth(null));
					image = image.getScaledInstance((int)(tscale * image.getWidth(null)), -1, Image.SCALE_SMOOTH);
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid error " + e.getMessage());
			}
		}
	}

	public void saveState() {
		osc_states = null;
		osc_states = new ArrayList<OscState>();
		for (int i = 0; i < oscillators.size(); i++)
			osc_states.add(new OscState(oscillators.get(i).getFreq(), oscillators.get(i).active()));
	}

	public void restoreState() {
		System.out.println("BookPage.restoreState(): starting...");
		if (osc_states == null) return;
		for (int i = 0; i < osc_states.size(); i++) {
			oscillators.get(i).setFreq(osc_states.get(i).freq);
		//	oscillators.get(i).setActive(osc_states.get(i).active);
		}
		this.activate();
	}

	public java.awt.Color constr_parseColor(String string) {
		if (string == null) return null;
		if (string.length() != 6) throw new IllegalArgumentException(".constr_parseColor(): Invalid color length");
		int r, g, b;
		r = charValue(string.charAt(0)) << 4;
		r += charValue(string.charAt(1));

		g = charValue(string.charAt(2)) << 4;
		g += charValue(string.charAt(3));

		b = charValue(string.charAt(4)) << 4;
		b += charValue(string.charAt(5));

		return new java.awt.Color(r, g, b);
	}

	int charValue(char c) {
		if (c >= '0' && c <= '9')
			return c - '0';

		if (c >= 'a' && c <= 'f')
			return 10 + c - 'a';

		throw new IllegalArgumentException(String.format(".charValue(): %c is not a valid hex digit", c));
	}
}
