import java.util.ArrayList;

class Scale {
	private String name;
	private boolean octaves;
	private double ref;			// reference frequency
	private int ref_index;		// reference index
	private ArrayList<Interval> steps;
	private double[] freqs;		// frequencies

	public Scale(String p_name, int p_steps, boolean type) {	// type false = unequal, true = Equal Division of Octave
		if (p_steps <= 0) throw new IllegalArgumentException("Scale(): Invalid number of steps");
		if (p_name != null && !p_name.equals("")) this.name = new String(p_name);
		steps = new ArrayList<Interval>();

		if (type == false) {
			for (int i = 0; i < p_steps; i++)
				steps.add(new Interval(0.0));
		} else {
			double stepsize = 1200.0 / (double)p_steps;
			steps = new ArrayList<Interval>();
			for (int i = 0; i < p_steps; i++)
				steps.add(new Interval(stepsize * i));
		}

		updateFreqs();
		octaves = true;
	}

	public Scale(XMLTag data) {
	// Get name
		try {
			name = new String(data.attr("name"));
		//	System.out.println("Scale name: " + name);
		} catch (Exception e) {
			System.out.println("Warning! Scale has no name");
		}

	// Get octaves
		try {
			if (data.attr("octaves").equals("false")) octaves = false;
			else octaves = true;
		} catch (Exception e) {
			octaves = true;
		}

	/*	if (octaves == true) System.out.println("Octaves set to repeat");
		else System.out.println("Octaves will not repeat");*/

	// get steps
		steps = new ArrayList<Interval>();

		for (int i = 0; i < data.nsub(); i++) {
			if (data.sub(i).name().equals("null")) {
				steps.add(null);
				continue;
			}

			if (data.sub(i).name().equals("tone")) {
				Interval temp = null;
				String desc = null;
				String comp = null;
				double cents = 0.0;
				String ratio = null;
				double dratio = 1.0;
				boolean have_cents = false;	// needed in case cents are exactly 0.0
				boolean no_ratio = true;

		//		System.out.println("Attempting to read degree name...");
				desc = data.sub(i).attr("name");
		//		System.out.println("Attempting to read degree short name...");
				comp = data.sub(i).attr("short");

			//	if (ratio != null) temp = new Interval(ratio);

				try {
					temp = new Interval(data.sub(i).attr("ratio"));
					no_ratio = false;
				} catch(Exception e) {
				//	System.out.println(e.getMessage());
				}

				if (no_ratio) {
					try {
						cents = Double.parseDouble(data.sub(i).attr("cents"));
						have_cents = true;
						temp = new Interval(cents);
					} catch (Exception e) {
						have_cents = false;
						System.out.printf("Scale(): Could not find cents at scale index %d", i);
					}
				}

				try {
					double temp_ref = Double.parseDouble(data.sub(i).attr("ref"));
					ref = temp_ref;
					ref_index = i;
				} catch (Exception e) {}

				if (temp != null) {
					if (desc != null) temp.setName(desc);
					if (comp != null) temp.setShort(comp);
					steps.add(temp);
				}
			}
		}
		updateFreqs();
	}

	public void print() {
	/*	System.out.println("Scale: " + name);
		System.out.println(String.format("Reference Tone: %d %s = %.2f Hz",
			ref_index, steps.get(ref_index).getName(), ref));
		if (steps != null) for (int i = 0; i < steps.size(); i++)
			System.out.println(String.format("  %s, %.2f", steps.get(i), freqs[i]));*/
	}

	public String getName() {
		if (name != null) return new String(name);
		else return null;
	}

	public int ndeg() {
		if (steps != null) return steps.size();
		else return 0;
	}

	public Interval getDegree(int i) {
		if (i < 0 || i >= steps.size()) throw new IllegalArgumentException("Scale.getDegree(): Index out of range");
		else return steps.get(i);
	}

	public double getFrequency(int i) {
		if (i < 0 || i >= steps.size()) throw new IllegalArgumentException("Scale.getFrequency(): Index out of range");
		else return freqs[i];
	}

	public void updateFreqs() {
		freqs = new double[steps.size()];
		for (int i = 0; i < steps.size(); i++) {
			if (steps.get(i) == null) freqs[i] = 0.0;
			else freqs[i] = Interval.offsetFrequency(ref, steps.get(i).getCents() - steps.get(ref_index).getCents());
		}
	}

	public void setDegree(int i, Interval p_int) {
		if (i < 0 || i >= steps.size()) throw new IllegalArgumentException("Scale.setDegree(): Index out of range");
		steps.set(i, p_int);
	}

	public boolean octaveRepeat() {
		return octaves;
	}

	public XMLTag toXML() {
		XMLTag writag = new XMLTag("scale");
		if (this.name != null && !this.name.equals("")) writag.addAttr("name", this.name);
		if (octaves) writag.addAttr("octaves", "true");
		else writag.addAttr("octaves","false");

		for (int i = 0; i < steps.size(); i++) {
			if (steps.get(i) == null) {
				writag.addSubtag(new XMLTag("null"));
				continue;
			}

			XMLTag tonetag = new XMLTag("tone");

			if (steps.get(i).getName() != null) tonetag.addAttr("name", steps.get(i).getName());
			if (steps.get(i).getNum() != 0 && steps.get(i).getDenom() != 0)
				tonetag.addAttr("ratio", String.format("%d/%d", steps.get(i).getNum(), steps.get(i).getDenom()));
			else tonetag.addAttr("cents", String.format("%f", steps.get(i).getCents()));
			if (i == ref_index) tonetag.addAttr("ref", String.format("%f", ref));
			writag.addSubtag(tonetag);
		}
		return writag;
	}

	public void toFile(String p_filename) {
		XMLTag writag = new XMLTag("scale");
		if (this.name != null && !this.name.equals("")) writag.addAttr("name", this.name);
		if (octaves) writag.addAttr("octaves", "true");
		else writag.addAttr("octaves","false");

		for (int i = 0; i < steps.size(); i++) {
			if (steps.get(i) == null) {
				writag.addSubtag(new XMLTag("null"));
				continue;
			}

			XMLTag tonetag = new XMLTag("tone");

			if (steps.get(i).getName() != null) tonetag.addAttr("name", steps.get(i).getName());
			if (steps.get(i).getNum() != 0 && steps.get(i).getDenom() != 0)
				tonetag.addAttr("ratio", String.format("%d/%d", steps.get(i).getNum(), steps.get(i).getDenom()));
			else tonetag.addAttr("cents", String.format("%f", steps.get(i).getCents()));
			if (i == ref_index) tonetag.addAttr("ref", String.format("%f", ref));
			writag.addSubtag(tonetag);
		}

		writag.writeToFile(p_filename);
	}

	public int getRefIndex() {
		return ref_index;
	}

	public void setRef(int p_ind, double p_freq) {
		if (p_ind < 0 && p_ind >= steps.size()) throw new IllegalArgumentException("Scale.setRef(): Reference tone index out of range");
		if (p_freq <= 0.0) throw new IllegalArgumentException("Scale.setRef(): Invalid reference tone frequency");
		this.ref_index = p_ind;
		this.ref = p_freq;

		updateFreqs();
	}
}
