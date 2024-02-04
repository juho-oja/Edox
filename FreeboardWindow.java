import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class FreeboardWindow extends BookWindow implements ActionListener {
	private Edox host;
	private JButton b_toscale, b_loadmap;
	private XMLTag xmlscale;
	private XMLTag xmlmap;
	private Scale act_scale;
	private BookPage page;

	private int map_top;	// highest scale degree in map

	public FreeboardWindow(Edox phost) {
		super("templates/freeboard.xml");
		host = phost;

		b_toscale = new JButton("Send to Active Scale");
			b_toscale.setBounds((int)(this.getWidth() * 0.333) - (int)(90.0 * scale), lbuttony, (int)(180.0 * scale), lbuttonh);
			b_toscale.setInputMap(0, null);
			b_toscale.setActionCommand("send");
			b_toscale.addKeyListener(this);
			b_toscale.addActionListener(this);
		b_loadmap = new JButton("Load Mapping...");
			b_loadmap.setBounds((int)(this.getWidth() * 0.667) - (int)(90.0 * scale), lbuttony, (int)(180.0 * scale), lbuttonh);
			b_loadmap.setInputMap(0, null);
			b_loadmap.setActionCommand("mapping");
			b_loadmap.addKeyListener(this);
			b_loadmap.addActionListener(this);

		this.add(b_toscale);
		this.add(b_loadmap);

		xmlscale = host.getXMLScale();
		act_scale = host.getActiveScale();
		page = pages.get(0);

		if (xmlscale != null) {
			try {
				XMLTag xfree = xmlscale.sub("freeboard");
				loadMapping(xfree);
			} catch (Exception e) { }
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("mapping")) {
			try {
				act_scale = host.getActiveScale();
				xmlscale = host.getXMLScale();

				FileDialog file_select = new FileDialog(new JFrame(), "Load Mapping...", FileDialog.LOAD);
				file_select.setDirectory("mappings");
				file_select.setVisible(true);
				String dir = file_select.getDirectory();
				String file = file_select.getFile();
			
				loadMapping(XMLTag.fromFile(dir + file));
				this.repaint();
			} catch (Exception g) {
				System.out.println("FreeboardWindow.actionPerformed(): Failed to load mapping");
			}
		}

		if (cmd.equals("send")) {
			try {
				act_scale = host.getActiveScale();
				xmlscale = host.getXMLScale();
			/*	if (act_scale != null) {
					sendDegrees();
				} else generateScale(); */
				host.setScale(new Scale(generateScale()));
			} catch (Exception g) { }
		}
	}

	private void loadMapping(XMLTag xmap) {
		if (!xmap.name().equals("freeboard")) return;
		xmlmap = xmap;

		for (int i = 0; i < xmap.nsub(); i++) {
			if (!xmap.sub(i).name().equals("map")) continue;

			Oscillator select = null;
			try {
				select = findByKey(xmap.sub(i).attr("key"));
			} catch (Exception e) {
				select = null;
			}
			if (select == null) continue;

		pitch: {
		// see whether there's a pitch offset present
			double ratio;
			try {	// try cents first
				Interval intvl = new Interval(Double.parseDouble(xmap.sub(i).attr("cents")));
				ratio = intvl.getRatio();
			} catch (Exception e) {
				try {	// try ratio next
					Interval intvl = new Interval(xmap.sub(i).attr("ratio"));
					ratio = intvl.getRatio();
				} catch (Exception f) {
					ratio = 1.0;
				}
			}

			double rndm;
			try {
				rndm = Double.parseDouble(xmap.sub(i).attr("random"));
				rndm = rndm * (Math.random() - 0.5);
			} catch (Exception e) { rndm = 0.0; }

			double octaves;
			try {
				octaves = Double.parseDouble(xmap.sub(i).attr("octaves"));
				octaves = Math.pow(2.0, octaves);
				System.out.println("Found octaves " + xmap.sub(i).attr("octaves") + " at key " + xmap.sub(i).attr("key"));
			} catch (Exception e) {
			//	System.out.println("No octaves found or error: " + e.getMessage());
				octaves = 1.0;
			}

			try {
				double init = Double.parseDouble(xmap.sub(i).attr("init"));
				select.setFreq(init + rndm);
				select.setInit(init + rndm);
			} catch (Exception e) {
				try {
					double init = Double.parseDouble(xmap.sub(i).attr("ref"));
					select.setFreq(init * octaves + rndm);
					select.setInit(init * octaves + rndm);
				} catch (Exception f) {
					try {
						double init = Double.parseDouble(xmap.attr("ref"));
						select.setFreq(init * ratio * octaves + rndm);
						select.setInit(init * ratio * octaves + rndm);
					} catch (Exception g) {
						select.setFreq(256.0 + rndm);
						select.setInit(256.0 + rndm);
					}
				}
			}

			int n = 0;
			try {
				n = Integer.parseInt(xmap.sub(i).attr("n"));
				if (n > map_top) map_top = n;
			} catch (Exception e) { }

		// SET TONE FROM SCALE IF NOT NULL
			if (act_scale != null) {
				try {
					System.out.println("Tone has octave value of " + octaves);
					select.setFreq(act_scale.getFrequency(n) * octaves);
				} catch (Exception e) { }
			}
		} // end of pitch:

		// look for background color
			try {
				select.setColor(Cgraph.parseColor(xmap.sub(i).attr("color")));
			} catch (Exception e) {
				select.setColor(Cgraph.white);
			//	e.printStackTrace();
			}

		// see if there's an additional name
			try {
				select.setName(xmap.sub(i).attr("name"));
			} catch (Exception e) { }

		// waveform?
			try {
				select.setWaveform(xmap.sub(i).attr("wave"));
			} catch (Exception e) {
				select.setWaveform("saw");
			}
		}
	}

	private void sendDegrees() {
		System.out.println("FreeboardWindow.sendDegrees(): Attempting to set scale degrees");
		if (xmlmap == null) return;
		double deg0 = act_scale.getFrequency(0);

		for (int i = 0; i < xmlmap.nsub(); i++) {
			if (!xmlmap.sub(i).name().equals("map")) continue;

			try {
				Oscillator osc = findByKey(xmlmap.sub(i).attr("key"));
			//	System.out.println("   Found a key: " + xmlmap.sub(i).attr("key"));
				int n = Integer.parseInt(xmlmap.sub(i).attr("set"));
			//	System.out.printf("   Found associated scale degree: %d\n", n);

				double ratio;
				try {
					ratio = Double.parseDouble(xmlmap.sub(i).attr("ratio"));
				} catch (Exception f) {
					ratio = 1.0;
				}
				
				Interval intvl = act_scale.getDegree(n);
				double freq = osc.getFreq() / ratio;
				
				if (intvl == null) {
					intvl = new Interval(Interval.cents(freq / deg0));
				} else intvl.setRatio(freq / deg0);
				act_scale.setDegree(n, intvl);

			} catch (Exception e) {
			//	System.out.println("FreeboardWindow.sendDegrees() ERROR:\n  " + e.getMessage());
			}
		}

		act_scale.updateFreqs();
		host.refresh();
	}

	private XMLTag generateScale() {
		XMLTag newscale = new XMLTag("scale");

	// check out the active scale length. If it's longer, keep existing tones
		int scl_n = map_top;
		if (act_scale != null)
			if (act_scale.ndeg() > map_top) scl_n = act_scale.ndeg() - 1;
	
	// deal with the reference tone
		double ref_f, deg0_f = 256.0;
		int ref_n;

		try {
			XMLTag xref = xmlmap.subwa("map", "ref"); 
			ref_n = Integer.parseInt(xref.attr("set"));
		} catch (Exception e) {
			ref_n = 0;
			ref_f = 256.0;
			deg0_f = 256.0;
		}

	// do the 0-th degree first separately because it requires special considerations
		if (ref_n != 0) {	// if ref and deg0 are the same, we're already all set
			XMLTag deg0 = xmlmap.subwav("map", "set", "0");
			if (deg0 == null) {
				if (act_scale == null) return null;	// if there is no active scale and no scale degree zero,
												// abort because there is nothing to compare to
				deg0_f = act_scale.getFrequency(0);
				if (deg0_f == 0.0) return null;
			//	System.out.println("FreeboardWindow.generateScale(): Getting degree 0 from active scale");
			} else {						
			// obtain a ratio if it's present
				Oscillator select = findByKey(deg0.attr("key"));
				deg0_f = select.getFreq() / calcRatio(deg0);
			}
		}

		XMLTag ins = new XMLTag("tone");
		ins.addAttr("cents", String.format("%.6f", 0.0));
		newscale.addSubtag(ins);

		for (int i = 1; i <= scl_n; i++) {
			XMLTag tmp = xmlmap.subwav("map", "set", String.format("%d", i));
			if (tmp == null) {
				if (act_scale == null)
					newscale.addSubtag(new XMLTag("null"));
				else {
				//	System.out.println("Trying to copy over from active scale...");
					try {
						newscale.addSubtag(xmlscale.sub(i));
					} catch (Exception e) {
					//	System.out.println("  Copying failed");
						newscale.addSubtag(new XMLTag("null"));
					}
				}
			} else {
				try {
					ins = new XMLTag("tone");
					Oscillator select = findByKey(tmp.attr("key"));
					double ratio = calcRatio(tmp);
					double cnts = Interval.cents(select.getFreq() / ratio / deg0_f);
					ins.addAttr("cents", String.format("%.6f", cnts));
					if (i == ref_n) {
					//	System.out.println("FreeboardWindow.generateScale(): Found the reference tone!");
						ins.addAttr("ref", String.format("%.2f", select.getFreq() / ratio));
					}
					newscale.addSubtag(ins);
				} catch (Exception e) { }
			}
		}

		return newscale;
	}

	private double calcRatio(XMLTag pmap) {
		double ratio;
		try {	// try cents first
			Interval intvl = new Interval(Double.parseDouble(pmap.attr("cents")));
			ratio = intvl.getRatio();
		} catch (Exception e) {
			try {	// try ratio next
				Interval intvl = new Interval(pmap.attr("ratio"));
				ratio = intvl.getRatio();
			} catch (Exception f) {
				ratio = 1.0;
			}
		}
		return ratio;
	}

	private Oscillator findByKey(String key) {
		for (int i = 0; i < page.oscillators.size(); i++) {
			try {
				if (page.oscillators.get(i).key().equals(key))
					return page.oscillators.get(i);
			} catch (Exception e) { }
		}
		return null;
	}
/*
	private Oscillator findByN(int index) {
		if (index < 0 || index > map_top) return null;

		String n = String.format("%d", index);
		for (int i = 0; i < xmlmap.nsub(); i++) {
			try {
				if (xmlmap.sub(i).name().equals("map")) {
					
				}
			} catch (Exception e) {
			}
		}
		return null;
	}*/
}
