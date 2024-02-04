class Interval {
	private static double constant;
	private static int r_prec, c_prec; // display and storage precision for ratios and cents

	private String desc, comp;
	private double cents;
	private double ratio;
	private int num, div;

/*	****************************************************************
							CONSTRUCTORS
	**************************************************************** */

	static {
		constant = 1200.0 / Math.log(2);
	}

	public Interval(String p_desc, double p_cents) {
		if (p_cents <= 0.0) throw new IllegalArgumentException("Interval: Invalid ratio");

		if (p_desc != null && !p_desc.equals("")) desc = new String(p_desc);
		cents = p_cents;
		ratio = centsToRatio(cents);
		num = 0;
		div = 0;
	}

	public Interval(String p_desc, String p_comp, double p_cents) {
	//	if (p_desc == null || p_desc.equals("")) throw new IllegalArgumentException("Interval: Invalid description");
	//	if (p_comp == null || p_comp.equals("")) throw new IllegalArgumentException("Interval: Invalid short description");
		if (p_cents < 0.0) throw new IllegalArgumentException("Interval: Invalid cents");

		desc = p_desc;
		comp = p_comp;
		cents = p_cents;
		ratio = centsToRatio(p_cents);
		num = 0;
		div = 0;
	}

	public Interval(int pnum, int pdiv) {
		if (pnum <= 0 || pdiv <= 0) throw new IllegalArgumentException("Illegal argument");
		desc = String.format("%d/%d", pnum, pdiv);
		comp = desc;

		num = pnum;
		div = pdiv;
		ratio = (double)num / (double)div;
		cents = cents(ratio);
	}

	public Interval(double p_cents) {
		desc = null;
		comp = null;
		cents = p_cents;
		ratio = centsToRatio(p_cents);
		num = 0;
		div = 0;
	}

	public Interval(String p_desc, String p_comp, int pnum, int pdiv) {
		if (p_desc == null || p_desc.equals("")) throw new IllegalArgumentException("Interval: Invalid description");
		if (p_comp == null || p_comp.equals("")) throw new IllegalArgumentException("Interval: Invalid short description");
		if (pnum <= 0) throw new IllegalArgumentException("Interval: Invalid numerator");
		if (pdiv <= 0) throw new IllegalArgumentException("Interval: Invalid denominator");

		desc = new String(p_desc);
		comp = new String(p_comp);
		num = pnum;
		div = pdiv;
		ratio = (double)num / (double)div;
		cents = cents(ratio);
	}

	public Interval(String p_ratio) {
	//	if (p_ratio == null) throw new IllegalArgumentException("Interval(String): String is null");

		desc = null;
		comp = null;
		num = 0;
		div = 0;
		ratio = 0.0;
		cents = 0.0;

	// try straight integer first, will fail if floating point or division symbol present
		try {
			num = Integer.parseInt(p_ratio);
			div = 1;
			cents = cents(num);
			ratio = (double)num;
			return;
		} catch (Exception e) {
			num = 0;
			div = 0;
		}

	// try floating point next, will fail if division symbol present
		try {
			ratio = Double.parseDouble(p_ratio);
			cents = cents(ratio);
			div = 0;
			return;
		} catch (Exception e) {}

	// Try to interpret as a ratio
		StringBuffer bnum = new StringBuffer();
		StringBuffer bdiv = new StringBuffer();
		StringBuffer target = bnum;	// reading numerator first
		int divsymbols = 0;
		boolean error = false;

		for (int i = 0; i < p_ratio.length(); i++) {

			char c = p_ratio.charAt(i);

			if (c == '/') {
				if (divsymbols > 0) { error = true; break; }
				target = bdiv;	// switch to denominator
				divsymbols++;
				continue;
			}
		
		// ignore space characters
			if (c == ' ') continue;

			if ((c >= '0' && c <= '9') || c == '.') target.append(c);
			else error = true;	// if any other characters, there must be an error
		}

	//	System.out.printf("Interval(String): numerator = %s, denominator = %s\n", bnum.toString(), bdiv.toString());

	// see whether both values are integer
		try {
			num = Integer.parseInt(bnum.toString());
			div = Integer.parseInt(bdiv.toString());
			ratio = (double)num / (double)div;
			cents = cents(ratio);
			return;
		} catch (Exception e) {
			num = 0;
			div = 0;
		}

	// try double if integers failed
		try {
			ratio = Double.parseDouble(bnum.toString()) / Double.parseDouble(bdiv.toString());
			cents = cents(ratio);
			return;
		} catch (Exception e) {
			error = true;
		}

		if (error) throw new IllegalArgumentException(String.format("Inverval(String) ERROR: %s is not a recognizable ratio\n", p_ratio));
	}

	public Interval(Interval p_int) {
		if (p_int == null) throw new IllegalArgumentException("Interval(Interval): cannot copy null");

		if (p_int.desc != null) desc = new String(p_int.desc);
		else desc = null;

		if (p_int.comp != null) comp = new String(p_int.comp);
		else comp = null;

		num = p_int.num;
		div = p_int.div;
		ratio = p_int.ratio;
		cents = p_int.cents;
	}

/*	****************************************************************
						GET FUNCTIONS
	**************************************************************** */

	public String getName() {
		if (desc != null) return new String(desc);
		else return null;
	}

	public String getShort() {
		return new String(comp);
	}

	public double getCents() {
		return cents;
	}

	public double getRatio() {
		return ratio;
	}

	public int getNum() {
		return num;
	}

	public int getDenom() {
		return div;
	}

	public String toString() {
		StringBuilder ret = new StringBuilder();
		if (desc != null) {
			if (num != 0 && div != 0) return String.format("%s (%d/%d)", desc, num, div);
			else return String.format("%s, %.3f c", desc, cents);
		} else {
			if (num != 0 && div != 0) return String.format("%d/%d", num, div);
			else return String.format("%.3f c", cents);
		}
	}

	public String fullString() {
		StringBuilder full = new StringBuilder();
		if (desc != null) full.append(String.format("Name: %s\n", desc));
		if (comp != null) full.append(String.format("Short: %s\n", comp));
		if (num != 0 && div != 0) full.append(String.format("Integer ratio: %d/%d\n", num, div));
		full.append(String.format("Decimal ratio: %.5f\n", ratio));
		full.append(String.format("Cents: %.3f", cents));
		return full.toString();
	}

/*	****************************************************************
						SET FUNCTIONS
	**************************************************************** */

	public void setName(String p_desc) {
		if (p_desc == null) return;
		else desc = new String(p_desc);
	}

	public void setShort(String p_comp) {
		if (p_comp == null) return;
		else comp = new String(p_comp);
	}

	public void setCents(double p_cents) {
		cents = p_cents;
		ratio = Math.pow(2, p_cents / 1200.0);
		num = 0;
		div = 0;
	}

	public void setRatio(double p_ratio) {
		if (ratio < 0.0) throw new IllegalArgumentException("Interval.setRatio(): Negative ratio");
		cents = cents(p_ratio);
		ratio = p_ratio;
		num = 0;
		div = 0;
	}

	public void setFraction(int p_num, int p_div) {
		if (p_div < 1 || p_num < 1)
			throw new IllegalArgumentException("Interval.setFraction(): Numerator or denominator out of range");

		num = p_num; div = p_div;
		ratio = (double)p_num / (double)p_div;
		cents = cents(ratio);
	}

/*	****************************************************************
						UTILITY FUNCTIONS
	**************************************************************** */

	public static double cents(double p_ratio) {
		return constant * Math.log(p_ratio);
	}

	public static double centsToRatio(double p_cents) {
		return Math.pow(2, p_cents / 1200.0);
	}

	public static double offsetFrequency(double freq, double cents) {
		return freq * Math.pow(2, cents / 1200.0);
	}

	public static Interval add(Interval p_cents1, Interval p_cents2) {
		return new Interval(p_cents1.cents + p_cents2.cents);
	}

/*	****************************************************************
					TEXTFIELD PARSING FUNCTIONS
	**************************************************************** */

	public static Interval parseString(String p_string) {
		int dpoints = 0; // number of decimal points
		for (int i = 0; i < p_string.length(); i++) {
		
		}
		return null;
	}

	private static boolean validChar(char chr) {
		if ((chr >= '0' && chr <='9') || chr == '.' || chr == '/') return true;
		else return false;
	}
/*
	private void skipWhitespace() {
	//	System.out.println("Skipping whitespace..." + xmldata.charAt(i));
		for (; i < xmldata.length() - 1 && xmldata.charAt(i) <= 32; i++); // System.out.println("Skip counter " + i);
	//	System.out.println("Whitespace skipped..." + xmldata.charAt(i));
	}
*/
/*	****************************************************************
							TEST MAIN
	**************************************************************** */

	public static void main(String[] args) {
	//	System.out.println(String.format("cents(3 / 2) = %.2f", cents(3.0/2.0)));
	//	System.out.println(String.format("ratio of 702 cents = %.2f", centsToRatio(702.0)));
		Interval testi = new Interval("Perfect 5th", "P5", 3, 2);
		System.out.println(testi);
		testi = new Interval("12-TET 5th", 700.0);
		System.out.println(testi);
		testi = new Interval("11th Harmonic", "H11", 11, 8);
		System.out.println(testi.fullString());
		testi = new Interval("13th Harmonic", "H13", 13, 8);
		System.out.println(testi.fullString());

		System.out.println("Fifth of 220 is " + offsetFrequency(220.0, 701.96));
	//	System.out.println("500 + 700 cents = " + add(new Interval(500.0), new Interval(700.0)).getCents());
	}
}
