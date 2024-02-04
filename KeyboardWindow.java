import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class KeyboardWindow extends BookWindow implements ActionListener {
	private Edox host;
	private JButton b_save, b_toscale;
	private XMLTag xmldata;
	private BookPage page;

	public KeyboardWindow(Edox phost) {
		super(Edox.settings().sub("tempering").attr("template"));
		host = phost;

		b_toscale = new JButton("Send to Active Scale");
			b_toscale.setBounds((int)(this.getWidth() * 0.333) - (int)(90.0 * scale), lbuttony, (int)(180.0 * scale), lbuttonh);
			b_toscale.setInputMap(0, null);
			b_toscale.setActionCommand("send");
			b_toscale.addKeyListener(this);
			b_toscale.addActionListener(this);
		b_save = new JButton("Export to file...");
			b_save.setBounds((int)(this.getWidth() * 0.667) - (int)(90.0 * scale), lbuttony, (int)(180.0 * scale), lbuttonh);
			b_save.setInputMap(0, null);
			b_save.setActionCommand("export");
			b_save.addKeyListener(this);
			b_save.addActionListener(this);

		this.add(b_save);
		this.add(b_toscale);

		if (host.getActiveScale().ndeg() == 12) {
			System.out.println("KeyboardWindow(): Loading frequencies from active scale");
			page = pages.get(0);
			loadDegrees();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("export")) {
			generateXML();

			FileDialog file_select = new FileDialog(new JFrame(), "Save As", FileDialog.SAVE);
			file_select.setDirectory("scales");
			file_select.setVisible(true);
			String dir = file_select.getDirectory();
			String file = file_select.getFile();

			if (dir != null && file != null)
				xmldata.writeToFile(dir + file);
		}

		if (e.getActionCommand().equals("send")) {
			try {
				generateXML();
				Scale updated = new Scale(xmldata);
				host.setScale(updated);
			} catch (Exception f) { }
		}
		super.actionPerformed(e);
	}

	private void sendToActiveScale() {

	}

	private void generateXML() {
		xmldata = new XMLTag("scale");
		xmldata.addAttribute("octaves", "true");

		try {
			page = pages.get(0);
			Oscillator find_me = findById("c");
			double rootf = find_me.getFreq();

			XMLTag tone = new XMLTag("tone");
			tone.addAttribute("name", "C");
			tone.addAttribute("cents", "0.0");
			xmldata.addSubtag(tone);

			double freq = findById("cs").getFreq();
			tone = new XMLTag("tone");
			tone.addAttribute("name", "C#");
			tone.addAttribute("cents", String.format("%.3f", Interval.cents(freq / rootf)));
			xmldata.addSubtag(tone);

			freq = findById("d").getFreq();
			tone = new XMLTag("tone");
			tone.addAttribute("name", "D");
			tone.addAttribute("cents", String.format("%.3f", Interval.cents(freq / rootf)));
			xmldata.addSubtag(tone);

			freq = findById("eb").getFreq();
			tone = new XMLTag("tone");
			tone.addAttribute("name", "Eb");
			tone.addAttribute("cents", String.format("%.3f", Interval.cents(freq / rootf)));
			xmldata.addSubtag(tone);

			freq = findById("e").getFreq();
			tone = new XMLTag("tone");
			tone.addAttribute("name", "E");
			tone.addAttribute("cents", String.format("%.3f", Interval.cents(freq / rootf)));
			xmldata.addSubtag(tone);

		try {	// see whether higher octave version exists, if it does, take an average
			freq = (findById("f").getFreq() * 2.0 + findById("f2").getFreq()) * 0.5;
			tone = new XMLTag("tone");
			tone.addAttribute("name", "F");
			tone.addAttribute("cents", String.format("%.3f", Interval.cents(freq / rootf)));
			xmldata.addSubtag(tone);
		} catch (Exception g) { // if above failed, go with a single value
			freq = findById("f").getFreq() * 2.0;
			tone = new XMLTag("tone");
			tone.addAttribute("name", "F");
			tone.addAttribute("cents", String.format("%.3f", Interval.cents(freq / rootf)));
			xmldata.addSubtag(tone);
		}

		try {
			freq = (findById("fs").getFreq() * 2.0 + findById("fs2").getFreq()) * 0.5;
			tone = new XMLTag("tone");
			tone.addAttribute("name", "F#");
			tone.addAttribute("cents", String.format("%.3f", Interval.cents(freq / rootf)));
			xmldata.addSubtag(tone);
		} catch (Exception g) {
			freq = findById("fs").getFreq() * 2.0;
			tone = new XMLTag("tone");
			tone.addAttribute("name", "F#");
			tone.addAttribute("cents", String.format("%.3f", Interval.cents(freq / rootf)));
			xmldata.addSubtag(tone);
		}

		try {
			freq = (findById("g").getFreq() * 2.0 + findById("g2").getFreq()) * 0.5;
			tone = new XMLTag("tone");
			tone.addAttribute("name", "G");
			tone.addAttribute("cents", String.format("%.3f", Interval.cents(freq / rootf)));
			xmldata.addSubtag(tone);
		} catch (Exception g) {
			freq = findById("g").getFreq() * 2.0;
			tone = new XMLTag("tone");
			tone.addAttribute("name", "G");
			tone.addAttribute("cents", String.format("%.3f", Interval.cents(freq / rootf)));
			xmldata.addSubtag(tone);
		}

		try {
			freq = (findById("gs").getFreq() * 2.0 + findById("gs2").getFreq()) * 0.5;
			tone = new XMLTag("tone");
			tone.addAttribute("name", "G#");
			tone.addAttribute("cents", String.format("%.3f", Interval.cents(freq / rootf)));
			xmldata.addSubtag(tone);
		} catch (Exception g) {
			freq = findById("gs").getFreq() * 2.0;
			tone = new XMLTag("tone");
			tone.addAttribute("name", "G#");
			tone.addAttribute("cents", String.format("%.3f", Interval.cents(freq / rootf)));
			xmldata.addSubtag(tone);
		}

			freq = findById("ra").getFreq();
			tone = new XMLTag("tone");
			tone.addAttribute("name", "A");
			tone.addAttribute("cents", String.format("%.3f", Interval.cents(freq / rootf)));
			tone.addAttribute("ref", String.format("%.2f", freq));
			xmldata.addSubtag(tone);

			freq = findById("bb").getFreq() * 2.0;
			tone = new XMLTag("tone");
			tone.addAttribute("name", "Bb");
			tone.addAttribute("cents", String.format("%.3f", Interval.cents(freq / rootf)));
			xmldata.addSubtag(tone);

			freq = findById("b").getFreq() * 2.0;
			tone = new XMLTag("tone");
			tone.addAttribute("name", "B");
			tone.addAttribute("cents", String.format("%.3f", Interval.cents(freq / rootf)));
			xmldata.addSubtag(tone);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Oscillator findById(String p_id) {
		for (int i = 0; i < page.oscillators.size(); i++)
			try {
				if (page.oscillators.get(i).getId().equals(p_id))
					return page.oscillators.get(i);
			} catch (Exception e) {}
		return null;
	}

	private void loadDegrees() {
		Scale temp = host.getActiveScale();

		try {
			findById("c").setFreq(temp.getFrequency(0));
		} catch (Exception e) { }

		try {
			findById("c0").setFreq(temp.getFrequency(0) * 0.5);
		} catch (Exception e) { }

		try {
			findById("cr").setFreq(temp.getFrequency(0));
		} catch (Exception e) { }

		try {
			findById("cs").setFreq(temp.getFrequency(1));
		} catch (Exception e) { }

		try {
			findById("cs0").setFreq(temp.getFrequency(1) * 0.5);
		} catch (Exception e) { }

		try {
			findById("d").setFreq(temp.getFrequency(2));
		} catch (Exception e) { }

		try {
			findById("d0").setFreq(temp.getFrequency(2) * 0.5);
		} catch (Exception e) { }

		try {
			findById("eb").setFreq(temp.getFrequency(3));
		} catch (Exception e) { }

		try {
			findById("eb0").setFreq(temp.getFrequency(3) * 0.5);
		} catch (Exception e) { }

		try {
			findById("e").setFreq(temp.getFrequency(4));
		} catch (Exception e) { }

		try {
			findById("e0").setFreq(temp.getFrequency(4) * 0.5);
		} catch (Exception e) { }

		try {
			findById("f").setFreq(temp.getFrequency(5) * 0.5);
		} catch (Exception e) { }

		try {
			findById("f2").setFreq(temp.getFrequency(5));
		} catch (Exception e) { }

		try {
			findById("f0").setFreq(temp.getFrequency(5) * 0.25);
		} catch (Exception e) { }

		try {
			findById("fs").setFreq(temp.getFrequency(6) * 0.5);
		} catch (Exception e) { }

		try {
			findById("fs2").setFreq(temp.getFrequency(6));
		} catch (Exception e) { }

		try {
			findById("fs0").setFreq(temp.getFrequency(6) * 0.25);
		} catch (Exception e) { }

		try {
			findById("g").setFreq(temp.getFrequency(7) * 0.5);
		} catch (Exception e) { }

		try {
			findById("g2").setFreq(temp.getFrequency(7));
		} catch (Exception e) { }

		try {
			findById("g0").setFreq(temp.getFrequency(7) * 0.25);
		} catch (Exception e) { }

		try {
			findById("gs").setFreq(temp.getFrequency(8) * 0.5);
		} catch (Exception e) { }

		try {
			findById("gs2").setFreq(temp.getFrequency(8));
		} catch (Exception e) { }

		try {
			findById("gs0").setFreq(temp.getFrequency(8) * 0.25);
		} catch (Exception e) { }

		try {
			findById("a").setFreq(temp.getFrequency(9) * 0.5);
		} catch (Exception e) { }

		try {
			findById("ra").setFreq(temp.getFrequency(9));
		} catch (Exception e) { }

		try {
			findById("a0").setFreq(temp.getFrequency(9) * 0.25);
		} catch (Exception e) { }

		try {
			findById("bb").setFreq(temp.getFrequency(10) * 0.5);
		} catch (Exception e) { }

		try {
			findById("bb0").setFreq(temp.getFrequency(10) * 0.25);
		} catch (Exception e) { }

		try {
			findById("b").setFreq(temp.getFrequency(11) * 0.5);
		} catch (Exception e) { }

		try {
			findById("b0").setFreq(temp.getFrequency(11) * 0.25);
		} catch (Exception e) { }
	}
}
