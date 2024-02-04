class SineGenerator implements Signal16 {
	private long phase;
	private double sample_rate;

	private static short sine_table[];

	static {
		sine_table = new short[256];
		for (int i = 0; i < sine_table.length; i++) {
			sine_table[i] = (short)((Math.sin((double)i * 2.0 * Math.PI / (double)sine_table.length)) * 32767.0 + 0.5); // +0.5 is there to help avoid rounding errors from truncation
		}

		System.out.println("SineGenerator(static): Table ready");
	}

	public SineGenerator(double p_srate) {
		phase = 0;
		if (p_srate >= 8000.0 && p_srate <= 96000.0) sample_rate = p_srate;
		else sample_rate = 44100.0;
	}

	public static void init() {	// forces static constructor call

	}

	@Override
	public void reset() {
		phase = 0;
	}

	@Override
	public short[] generateBlock16(double freq, int length) {
		long i, tphase, phase_rate, table_select;
		long index_hi, index_lo;
		long high, low, frac, crossfade;
		int base_hi, base_lo;
		short dest[] = new short[length];

		phase_rate = (long)(freq / sample_rate * 4294967296.0);	// originally 20-bit, add 4 bits for 24-bit fraction
		tphase = this.phase;

		for (i = 0; i < length; i++) {
			index_lo = tphase >> 24;
			index_hi = ((tphase >> 24) + 1) & 0xFFL;
		//	if (index_lo > 128 && index_lo <= 292) System.out.printf("  index_lo = %d, inxed_hi = %d\n", index_lo, index_hi);

			high = ((long)sine_table[(int)index_hi]);
			low = ((long)sine_table[(int)index_lo]);

			frac = tphase & 0xFFFFFFL;

			dest[(int)i] = (short)(low + ((frac * (high - low)) >> 24));

			tphase += phase_rate;
			tphase = tphase & 0xFFFFFFFFL;
		//	if (tphase > 0xFFFFFFFF) System.out.println("too much phase " + tphase);
		//	System.out.printf("i_hi - i_lo = %d, high - low = %d  \t|| hi = %d, lo = %d\n",
		//		index_hi - index_lo, high - low, high, low);
		}
		this.phase = tphase;
		return dest;
	}
}
