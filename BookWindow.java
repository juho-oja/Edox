import java.awt.*;
import java.awt.event.*;
import java.awt.Font;
import javax.swing.*;
import javax.imageio.*;

import java.util.ArrayList;

// default book page size = 800 x 600

class BookWindow extends JFrame implements KeyListener, ActionListener {

	private BookPanel	drawpanel;
	private int			width, height;
	private Insets		insets;
	protected double 	scale;
	private Timer		timer;

	private XMLTag xbook;	// xml of book
	private String dir, file;

	protected ArrayList<BookPage>	pages;
	private ArrayList<OscId>		osc_ids;
	private int						current_page;

	private static java.awt.Color	color_black, color_white, color_button, color_nobutton;
	private static java.awt.Font	page_number;
	private boolean 				numbering;

	private static final int		vertical_padding = 40;

	private JButton		next_page, previous_page, save_state, restore_state, reset_page;
	protected int		lbuttony, lbuttonh;

	private class OscId {
		public String id;
		public double freq;
	}

	public BookWindow(String pdir, String pfile) {	// only constructor that supports loading images
		if (pfile == null) return;
		dir = pdir;
		file = pfile;

		try {
			xbook = XMLTag.fromFile(pdir + pfile);
		} catch (Exception e) {
			throw new IllegalArgumentException("BookWindow(): failed to load file " + pdir + pfile);
		}

		constr_body();
	}

	public BookWindow(String bookfile) {
	// read the XML from file
		try {
			xbook = XMLTag.fromFile(bookfile);
			if (!xbook.name().equals("edoxbook")) return;
		} catch (Exception e) {
			throw new IllegalArgumentException("BookWindow(): failed to load file " + bookfile);
		}
	// clears all text within tags that contain subtags
		xbook.removeText();
	
		constr_body();
	}

	public BookWindow(XMLTag p_book) {
		if (p_book == null) throw new IllegalArgumentException("BookWindow(XMLTag): Tag is null");
		xbook = p_book;

		constr_body();
	}

	private void constr_body() {
		current_page = 0;
		scale = Edox.getUiScale();
		osc_ids = new ArrayList<OscId>();

		color_black = new java.awt.Color(0, 0, 0);
		color_white = new java.awt.Color(255, 255, 255);
		color_button = new java.awt.Color(120, 255, 40);
		color_nobutton = new java.awt.Color(127,127, 127);

		page_number = new java.awt.Font("Arial", 0, (int)(18 * scale));

	// try to get width and height from root tag
		try {
			width = Integer.parseInt(xbook.attr("w"));
			height = Integer.parseInt(xbook.attr("h"));
		} catch (Exception e) {
			System.out.println("BookWindow(): no window size or error, setting to 800 x 600");
			width = 800;
			height = 600;
		}

		try {
			if (xbook.attr("pagenumber").equals("false")) numbering = false;
		} catch (Exception e) { numbering = true; }

		pages = new ArrayList<BookPage>();
	//	System.out.println("Looking for pages...");
		for (int i = 0; i < xbook.nsub(); i++) {
			BookPage page;
			if (xbook.sub(i).name().equals("page")) {
				try {
					page = new BookPage(this, xbook.sub(i));
					pages.add(page);
				} catch (Exception e) {
					System.out.println("Failed to handle a page: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
	// create the drawing panel
		drawpanel = new BookPanel();
		drawpanel.setBounds(0, (int)(vertical_padding * scale), (int)(width * scale) - 1, (int)(height * scale) - 1); 
		drawpanel.addMouseListener(drawpanel);
		drawpanel.addMouseMotionListener(drawpanel);
		this.add(drawpanel);

	// set up basic window stuff
		this.setLayout(null);
		this.setVisible(true);
		insets = this.getInsets();
		this.setSize((int)(width * scale) + insets.left + insets.right, (int)((height + vertical_padding * 2) * scale) + insets.top + insets.bottom);
		this.setResizable(false);
		if (xbook.attr("name") != null) this.setTitle(xbook.attr("name"));
		BookWindow.this.addKeyListener(this);

		lbuttony = (int)(this.getHeight() - (35.0 * scale)) - insets.top;
	//	System.out.printf("Page button top y = %d\n", lbuttony);
		lbuttonh = (int)(30.0 * scale);
		constr_doButtons();

	//	System.out.println("Found a tag: " + xbook.name());

	//	System.out.println("Done looking for pages...");
		if (pages.size() > 0) pages.get(0).activate();

	// clean up on window closing
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent w) {
				timer.stop();
				pages.get(current_page).deactivate();
				timer = null;
				xbook = null;
				pages = null;;
				setVisible(false);
				dispose();
			}
		});

		timer = new Timer(40, this);
		timer.setActionCommand("timer");
		timer.start();

	//	other stuff
	//	System.out.println("BookWindow border width: " + insets);
	}

	private void constr_doButtons() {
		next_page = new JButton("->");				next_page.setBounds(this.getWidth() - (int)(110.0 * scale), lbuttony, (int)(100.0 * scale), lbuttonh);
		next_page.setActionCommand("next");			next_page.addActionListener(this);
		next_page.addKeyListener(this);				this.add(next_page);
		next_page.setInputMap(0, null);

		previous_page = new JButton("<-");			previous_page.setBounds(10, lbuttony, 100, lbuttonh);
		previous_page.setActionCommand("prev");		previous_page.addActionListener(this);
		previous_page.addKeyListener(this);			this.add(previous_page);
		previous_page.setInputMap(0, null);

		reset_page = new JButton("Reset page");		reset_page.setBounds((int)(10.0 * scale), (int)(5.0 * scale), (int)(150.0 * scale), lbuttonh);
		reset_page.setActionCommand("reset");		reset_page.addActionListener(this);
		reset_page.addKeyListener(this);			this.add(reset_page);
		reset_page.setInputMap(0, null);

		save_state = new JButton("Save state");		save_state.setBounds(this.getWidth() / 2 - (int)(75.0 * scale), (int)(5 * scale), (int)(150.0 * scale), lbuttonh);
		save_state.setActionCommand("save");		save_state.addActionListener(this);
		save_state.addKeyListener(this);			this.add(save_state);
		save_state.setInputMap(0, null);

		restore_state = new JButton("Restore state");	restore_state.setBounds(this.getWidth() - (int)(160.0 * scale), (int)(5.0 * scale), (int)(150.0 * scale), lbuttonh);
		restore_state.setActionCommand("restore");		restore_state.addActionListener(this);
		restore_state.addKeyListener(this);				this.add(restore_state);
		restore_state.setInputMap(0, null);
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		pages.get(current_page).keyPressed(e);
		repaint();
	}

	@Override
	public void keyReleased(KeyEvent e) {
	//	System.out.println("Key pressed");
		pages.get(current_page).keyReleased(e);
		repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if (command.equals("timer")) {
			this.repaint(); return; }


		if (command.equals("next")) {
			if (current_page < pages.size() - 1) {
				readPageIds();
				pages.get(current_page).deactivate();
				current_page++;
				setPageIds();
				pages.get(current_page).activate();
				this.repaint();
			}
		}

		if (command.equals("prev")) {
			if (current_page > 0) {
				readPageIds();
				pages.get(current_page).deactivate();
				current_page--;
				setPageIds();
				pages.get(current_page).activate();
				this.repaint();
			}
		}

		if (command.equals("save")) {
			pages.get(current_page).saveState();
		}

		if (command.equals("restore")) {
			pages.get(current_page).restoreState();
			this.repaint();
		}

		if (command.equals("reset")) {
			pages.get(current_page).reset();
			this.repaint();
		}
	}

	private void readPageIds() {
		ArrayList<Oscillator> osc_list = pages.get(current_page).getOscillators();
		ArrayList<Oscillator> hidden_list = pages.get(current_page).getHiddenOscillators();

		if (osc_list == null) return;
		for (int i = 0; i < osc_list.size(); i++) {
		// if no id, skip altogether
			if (osc_list.get(i).getId() == null) continue;

		// search for an existing id
			for (int j = 0; j < osc_ids.size(); j++) {
				if (osc_ids.get(j).id.equals(osc_list.get(i).getId())) {
					osc_ids.get(j).freq = osc_list.get(i).getFreq();
					continue;
				}
			}
		// if not found, make new id
			OscId newid = new OscId();
			newid.id = osc_list.get(i).getId();
			newid.freq = osc_list.get(i).getFreq();
			osc_ids.add(newid);
		}

		if (hidden_list == null) return;
		for (int i = 0; i < hidden_list.size(); i++) {
		// if no id, skip altogether
			if (hidden_list.get(i).getId() == null) continue;

		// search for an existing id
			for (int j = 0; j < osc_ids.size(); j++) {
				if (osc_ids.get(j).id.equals(hidden_list.get(i).getId())) {
					osc_ids.get(j).freq = hidden_list.get(i).getFreq();
					continue;
				}
			}
		// if not found, make new id
			OscId newid = new OscId();
			newid.id = hidden_list.get(i).getId();
			newid.freq = hidden_list.get(i).getFreq();
			osc_ids.add(newid);
		}
	}

	private void setPageIds() {
		ArrayList<Oscillator> osc_list = pages.get(current_page).getOscillators();
		ArrayList<Oscillator> hidden_list = pages.get(current_page).getHiddenOscillators();

	// If page has no oscillators, don't bother
		if (osc_list == null) return;
		for (int i = 0; i < osc_ids.size(); i++) {
			for (int j = 0; j < osc_list.size(); j++) {
				if (osc_list.get(j).getId() == null) continue;
				if (osc_list.get(j).getId().equals(osc_ids.get(i).id))
					osc_list.get(j).setFreq(osc_ids.get(i).freq);
			}
		}

		if (hidden_list == null) return;
		for (int i = 0; i < osc_ids.size(); i++) {
			for (int j = 0; j < hidden_list.size(); j++) {
				if (hidden_list.get(j).getId() == null) continue;
				if (hidden_list.get(j).getId().equals(osc_ids.get(i).id))
					hidden_list.get(j).setFreq(osc_ids.get(i).freq);
			}
		}
	}

	public String getDir() {
		return dir;
	}

	public String getFile() {
		return file;
	}

	private class BookPanel extends JPanel implements MouseListener, MouseMotionListener {

		public BookPanel() {
			this.setBounds(0, vertical_padding, (int)(width * scale) - 1, (int)(height * scale) - 1);
		}

		@Override
		public void paintComponent(Graphics g) {
		/*
			width = BookWindow.this.getWidth();
			height = BookWindow.this.height;
			if (width < 400) width = 400;
			if (height < 300) height = 300;

			this.setBounds(0, vertical_padding, width - 1, height - 1);
		*/
		// clear background
			g.setColor(color_white);
			g.fillRect(0,0, (int)(width * scale)- 1, (int)(height * scale) - 1);

		// pass on to the relevant page
			try {
				pages.get(current_page).draw(g, scale * 1.0);
			} catch (Exception e) { }

		// draw controls and page number
			g.setColor(color_black);
			if (numbering) {
				g.setFont(page_number);
				g.drawString(String.format("%d/%d", current_page + 1, pages.size()), (int)((width / 2.0 - 30.0) * scale), (int)((height - 20.0) * scale));
			}
		}

		public void mouseClicked(MouseEvent e) {
		// check for system button presses (switch page etc.)
			int mx = e.getX();
			int my = e.getY();

		// previous page-button
		/*	int prev_lx = (int)(scale * 20);
			int prev_rx = prev_lx + (int)(scale * 80);
			int prev_ly = BookWindow.this.getHeight() - (int)(scale * 80);
			int prev_ry = prev_ly + (int)(scale * 40);

		// previous page-button
			int next_lx = BookWindow.this.getWidth() - (int)(scale * 100);
			int next_rx = next_lx + (int)(scale * 80);
			int next_ly = prev_ly;	// y-coordinates are the same
			int next_ry = prev_ry;

		//	System.out.println("Mouse clicked...");
		
			if (current_page > 0) {
				if (mx >= prev_lx && mx <= prev_rx)
					if (my >= prev_ly && my <= prev_ry) {
					//	System.out.println("You pressed the previous page button!");
						current_page--;
						pages.get(current_page).activate();
						this.repaint();
					}
			}

			if (current_page < pages.size() - 1) {
				if (mx >= next_lx && mx <= next_rx)
					if (my >= next_ly && my <= next_ry) {
					//	System.out.println("You pressed the next page button!");
						current_page++;
						pages.get(current_page).activate();
						this.repaint();
					}
			}*/
		}

		public void mousePressed(MouseEvent e) {
		//	System.out.println("Mouse pressed...");
		}

		public void mouseReleased(MouseEvent e) {

		}

		public void mouseMoved(MouseEvent e) {

		}

		public void mouseDragged(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {

		}

		public void mouseExited(MouseEvent e) {

		}
	}
}
