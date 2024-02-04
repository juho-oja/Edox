import java.awt.*;
import java.awt.event.*;
import java.awt.Font;
import javax.swing.*;

import java.util.ArrayList;

class BeatPairsWindow extends JFrame implements ActionListener {
	/*		Beat rate pairs only apply if both tones have the harmonics present
	Tone 1
		Scale Degree + Octave
			or
		Frequency 1

	Tone 2
		Scale Degree + Octave
			or
		cent offset
			or
		ratio
			or
		frequency
	
	Threshold Hz (maximum beat rate for consideration)
	Cutoff Harmonic
	*/

	private Edox edox;
	private XMLTag xmldata;
	private JTextField t_deg1, t_deg2, t_octave1, t_octave2, t_freq1, t_freq2;
	private double freq1, freq2;
	
	private JTextField t_cents1, t_cents2, t_thold, t_cutoff, t_maxh, t_mpairs;
	private double cents1, cents2, thold, cutoff, maxh, mpairs; 

	private JButton b_close, b_save, b_display;
	private String s_error;

	private static String[] keyr1, keyr2;
//	private static String[] keyr2;

	private static final int N_KEY_PAIRS = 9;
	static {
	// key rows
		keyr1 = new String[N_KEY_PAIRS];
			keyr1[0] = "a";
			keyr1[1] = "s";
			keyr1[2] = "d";
			keyr1[3] = "f";
			keyr1[4] = "g";
			keyr1[5] = "h";
			keyr1[6] = "j";
			keyr1[7] = "k";
			keyr1[8] = "l";

		keyr2 = new String[N_KEY_PAIRS];
			keyr2[0] = "q";
			keyr2[1] = "w";
			keyr2[2] = "e";
			keyr2[3] = "r";
			keyr2[4] = "t";
			keyr2[5] = "y";
			keyr2[6] = "u";
			keyr2[7] = "i";
			keyr2[8] = "o";
	}

	public BeatPairsWindow(Edox p_edox) {
		edox = p_edox;
		this.setTitle("Overtone Beat Pairs");
		setDefaultValues();

		this.add(new Lables());
		makeTextFields();
		makeButtons();

		this.setLayout(null);
		this.setVisible(true);
		this.setSize(640,480);
		this.setResizable(false);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent w) {
				xmldata = null;
				setVisible(false);
				dispose();
			}
		});
	}

	private void makeButtons() {
		b_close = new JButton("Close");
			b_close.addActionListener(this);
			b_close.setActionCommand("close");
			b_close.setBounds(20, 410, 120, 30);
			this.add(b_close);

		b_save = new JButton("Save");
			b_save.addActionListener(this);
			b_save.setActionCommand("save");
			b_save.setBounds(260, 410, 120, 30);
			this.add(b_save);

		b_display = new JButton("Display");
			b_display.addActionListener(this);
			b_display.setActionCommand("display");
			b_display.setBounds(500, 410, 120, 30);
			this.add(b_display);
	}

	private class Lables extends JPanel {
		public Lables() {
			setBounds(0,0, 640,480);
		}
		@Override
		public void paintComponent(Graphics g) {
			g.drawString("Tone 1 scale degree:", 20, 38);
				g.drawString("Octave shift:", 350, 38);
			g.drawString("Offset (cents):", 20, 78);
			g.drawString("Absolute frequency:", 20, 118);

			g.drawString("Tone 2 scale degree:", 20, 198);
				g.drawString("Octave shift:", 350, 198);
			g.drawString("Offset (cents):", 20, 238);
			g.drawString("Absolute frequency:", 20, 278);

			g.drawString("Max beat rate:", 20, 338);
				g.drawString("Max harmonic:", 240, 338);
			g.drawString("Cutoff freq:", 20, 378);
				g.drawString("N of pairs:", 240, 378);
		}
	}

	private void makeTextFields() {
	// tone 1
		t_deg1 = new JTextField();
			t_deg1.setBounds(180, 20, 150, 30);
			t_deg1.addActionListener(this);
			t_deg1.setActionCommand("up1");
			this.add(t_deg1);

		t_octave1 = new JTextField("0");
			t_octave1.setBounds(450, 20, 60, 30);
			t_octave1.addActionListener(this);
			t_octave1.setActionCommand("up1");
			this.add(t_octave1);

		t_cents1 = new JTextField("0");
			t_cents1.setBounds(180, 60, 150, 30);
			t_cents1.addActionListener(this);
			t_cents1.setActionCommand("up1");
			this.add(t_cents1);

		t_freq1 = new JTextField("0");
			t_freq1.setBounds(180, 100, 150, 30);
			this.add(t_freq1);

	// tone 2
		t_deg2 = new JTextField();
			t_deg2.setBounds(180, 180, 150, 30);
			t_deg2.addActionListener(this);
			t_deg2.setActionCommand("up2");
			this.add(t_deg2);

		t_octave2 = new JTextField("0");
			t_octave2.setBounds(450, 180, 60, 30);
			t_octave2.addActionListener(this);
			t_octave2.setActionCommand("up2");
			this.add(t_octave2);

		t_cents2 = new JTextField("0");
			t_cents2.setBounds(180, 220, 150, 30);
			t_cents2.addActionListener(this);
			t_cents2.setActionCommand("up2");
			this.add(t_cents2);

		t_freq2 = new JTextField("0");
			t_freq2.setBounds(180, 260, 150, 30);
			this.add(t_freq2);

	// criteria
		t_thold = new JTextField("15");
			t_thold.setBounds(140, 320, 80, 30);
			this.add(t_thold);
		t_cutoff = new JTextField("8000");
			t_cutoff.setBounds(140, 360, 80, 30);
			this.add(t_cutoff);
		t_maxh = new JTextField("32");
			t_maxh.setBounds(360, 320, 80, 30);
			this.add(t_maxh);
		t_mpairs = new JTextField("7");
			t_mpairs.setBounds(360, 360, 80, 30);
			this.add(t_mpairs);
	}

	private void setDefaultValues() {
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("close")) {
			xmldata = null;
			setVisible(false);
			dispose();
		}

		if (cmd.equals("up1")) updateFreq1();
		if (cmd.equals("up2")) updateFreq2();

		if (cmd.equals("save")) {
			try {
				updateFreq1();
				updateFreq2();
				interpretFields();
				makeChart(Double.parseDouble(t_freq1.getText()), Double.parseDouble(t_freq2.getText()));

				FileDialog file_select = new FileDialog(new JFrame(), "Save As", FileDialog.SAVE);
				file_select.setDirectory("books/generated");
				file_select.setVisible(true);
				String dir = file_select.getDirectory();
				String file = file_select.getFile();

				if (dir != null && file != null)
					xmldata.writeToFile(dir + file);

			} catch (Exception f) {};
		}

		if (cmd.equals("display")) {
			try {
				updateFreq1();
				updateFreq2();
				interpretFields();
				makeChart(Double.parseDouble(t_freq1.getText()), Double.parseDouble(t_freq2.getText()));
				new BookWindow(xmldata);
			} catch (Exception f) {
				s_error = f.getMessage();
			}
		}
	}

	private void updateFreq1() {
		int deg1, oct1;
		double cents1, cents2;
		Scale scale = edox.getActiveScale();
		if (scale == null) return;

		try {
			deg1 = Integer.parseInt(t_deg1.getText());
		} catch (Exception e) {
			return;
		}
		
		if (deg1 < 0 || deg1 >= scale.ndeg()) return;

		try {
			oct1 = Integer.parseInt(t_octave1.getText());
		} catch (Exception e) {
			oct1 = 0;
		}

		if (oct1 < -8 || oct1 > 8) return;

		try {
			cents1 = Double.parseDouble(t_cents1.getText());
		} catch (Exception e) {
			cents1 = 0.0;
		}

		double shift = 1.0;
		if (oct1 >= 0) {
			for (int i = 0; i < oct1; i++)
				shift *= 2.0;
		} else {
			oct1 = oct1 * -1;
			for (int i = 0; i < oct1; i++)
				shift *= 0.5;
		}

		t_freq1.setText(String.format("%.6f", scale.getFrequency(deg1) * Interval.centsToRatio(cents1) * shift));
		this.repaint();
	}

	private void updateFreq2() {
		int deg2, oct2;
		double cents2;
		Scale scale = edox.getActiveScale();
		if (scale == null) return;

		try {
			deg2 = Integer.parseInt(t_deg2.getText());
		} catch (Exception e) {
			return;
		}
		
		if (deg2 < 0 || deg2 >= scale.ndeg()) return;

		try {
			oct2 = Integer.parseInt(t_octave2.getText());
		} catch (Exception e) {
			oct2 = 0;
		}

		if (oct2 < -8 || oct2 > 8) return;

		try {
			cents2 = Double.parseDouble(t_cents2.getText());
		} catch (Exception e) {
			cents2 = 0.0;
		}

		double shift = 1.0;
		if (oct2 >= 0) {
			for (int i = 0; i < oct2; i++)
				shift *= 2.0;
		} else {
			oct2 = oct2 * -1;
			for (int i = 0; i < oct2; i++)
				shift *= 0.5;
		}

		t_freq2.setText(String.format("%.6f", scale.getFrequency(deg2) * Interval.centsToRatio(cents2) * shift));
		this.repaint();
	}

	private void interpretFields() {
		try {
			thold = Double.parseDouble(t_thold.getText());	
		} catch (Exception e) {
			throw new IllegalArgumentException("Cannot interpret max beat rate");
		}

		if (thold < 0 || thold > 40.0) throw new IllegalArgumentException("Invalid max beat rate");

		try {
			maxh = Integer.parseInt(t_maxh.getText());	
		} catch (Exception e) {
			throw new IllegalArgumentException("Cannot interpret max harmonic number");
		}

		if (maxh < 2 || maxh > 1024) throw new IllegalArgumentException("Invalid max harmonic");

		try {
			cutoff = Double.parseDouble(t_cutoff.getText());	
		} catch (Exception e) {
			throw new IllegalArgumentException("Cannot interpret cutoff frequency");
		}

		if (cutoff < 0.1 || cutoff > 22000.0) throw new IllegalArgumentException("Invalid cutoff frequency");

		try {
			mpairs = Integer.parseInt(t_mpairs.getText());	
		} catch (Exception e) {
			throw new IllegalArgumentException("Cannot interpret max harmonic number");
		}

		if (mpairs < 1 || mpairs > 8) throw new IllegalArgumentException("Invalide pairs count, must be between 1 and 8");
	}

	private void makeChart(double p1, double p2) {

		int x1 = 20; int x2 = 500;
		int y1 = 300; int y2 = 400;
		int pairs = 0;
		double max_pitch = 8000.0;

		xmldata = new XMLTag("edoxbook");
			xmldata.addAttribute("w", "1280");
			xmldata.addAttribute("h", "720");
		XMLTag page1 = new XMLTag("page");
			xmldata.addSubtag(page1);
		XMLTag page2 = new XMLTag("page");
			xmldata.addSubtag(page2);

		XMLTag left = new XMLTag();
		XMLTag right = new XMLTag();
		// 
			XMLTag temposc = new XMLTag("osc");
			temposc.addAttribute("x", String.format("%d", x1));
			temposc.addAttribute("y", String.format("%d", y1));
			temposc.addAttribute("name", String.format("%.1f Hz", p1));
			temposc.addAttribute("init", String.format("%.6f", p1));
			temposc.addAttribute("type", "fixed");
			temposc.addAttribute("key", keyr1[0]);
			left.addSubtag(temposc);
			x1 += 80;
			y1 -= 20;

			temposc = new XMLTag("osc");
			temposc.addAttribute("x", String.format("%d", x2));
			temposc.addAttribute("y", String.format("%d", y2));
			temposc.addAttribute("name", String.format("%.1f Hz", p2));
			temposc.addAttribute("init", String.format("%.6f", p2));
			temposc.addAttribute("type", "fixed");
			temposc.addAttribute("key", keyr2[0]);
			right.addSubtag(temposc);
			x2 += 80;
			y2 -= 20;


		for (int i = 1; i <= maxh && pairs < mpairs; i++) {

			if (p1 * (double)i > cutoff) break;

			for (int j = 1; j <= maxh; j++) {

				if (p2 * (double)j > max_pitch) break;

				double diff = p1 * (double)i - p2 * (double)j;
				if (diff <= thold && diff >= thold * -1.0) {
				//	System.out.printf("Found eligible pair %d and %d\n", i ,j);
				// p1 oscillator
					temposc = new XMLTag("osc");
					temposc.addAttribute("x", String.format("%d", x1 + pairs * 80));
					temposc.addAttribute("y", String.format("%d", y1 - pairs * 20));
					temposc.addAttribute("name", String.format("%d", i));
					temposc.addAttribute("init", String.format("%.6f", p1 * (double)i));
					temposc.addAttribute("wave", "sine");
					temposc.addAttribute("vol", String.format("%.6f", 1.0 / (double)i));
					temposc.addAttribute("type", "fixed");
					if (pairs < (N_KEY_PAIRS - 1)) temposc.addAttribute("key", keyr1[pairs + 1]);
					left.addSubtag(temposc);

				// p2 oscillator
					temposc = new XMLTag("osc");
					temposc.addAttribute("x", String.format("%d", x2 + pairs * 80));
					temposc.addAttribute("y", String.format("%d", y2 - pairs * 20));
					temposc.addAttribute("name", String.format("%d", j));
					temposc.addAttribute("init", String.format("%.6f", p2 * (double)j));
					temposc.addAttribute("wave", "sine");
					temposc.addAttribute("vol", String.format("%.6f", 1.0 / (double)j));
					temposc.addAttribute("type", "fixed");
					if (pairs < (N_KEY_PAIRS - 1)) temposc.addAttribute("key", keyr2[pairs + 1]);
					right.addSubtag(temposc);

					pairs++;
				}
			}
		}
		for (int i = 0; i < left.nsub(); i++)
			page1.addSubtag(left.sub(i));

		for (int i = 0; i < right.nsub(); i++)
			page1.addSubtag(right.sub(i));

	//	xmldata.writeToFile("scratch/paska.xml");
		left = null; right = null;
	}
}
