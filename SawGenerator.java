import java.io.*;

class SawGenerator implements Signal, Signal16 {

	private long phase;
	private double sample_rate;

	private static short[] saw_table;

	static {
		short temp;
		System.out.println("SawGenerator(static): Reading saw tables from file...");
		saw_table = new short[256 * 8];	// 8 octaves, 256 samples per table
		try {
			InputStream infile = new FileInputStream("saw_table_256.dat");
			DataInputStream inreader = new DataInputStream(infile);
			for (int i = 0; i < 256 * 8; i++) {
				temp = inreader.readShort();
				saw_table[i] = (short)((temp & 0xFF) << 8);
				saw_table[i] |= (temp & 0xFF00) >> 8;
			}
		/*
			OutputStream outfile = new FileOutputStream("saw_table_reflect.dat");
			DataOutputStream outwriter = new DataOutputStream(outfile);
			for (int i = 0; i < 256 * 8; i++) {
				outwriter.writeShort(saw_table[i]);
			}
		*/
		// print out a couple to see whats happening
		/*	for (int i = 0; i < 256; i++) {
				System.out.printf("%d\n", saw_table[i]);
			}*/

		} catch (Exception e) {
			System.out.println("ERROR: SawGenerator(): cannot read saw wavetable");
		}
	}

	public SawGenerator(double p_srate) {
		phase = 0;
		if (p_srate >= 8000.0 && p_srate <= 96000.0) sample_rate = p_srate;
		else sample_rate = 44100.0;
	}

	@Override
	public float[] generateBlock(float freq, int length) {
		return null;
	}

	@Override
	public short[] generateBlock16(double freq, int length) {
		long i, tphase, phase_rate, table_select;
		long index_hi, index_lo;
		long high, low, frac, crossfade;
		int base_hi, base_lo;
		short dest[] = new short[length];

		phase_rate = (long)(freq / sample_rate * 4294967296.0); // transform to fixed point
		tphase = this.phase;

		table_select = phase_rate >> 24;	// 24 bits fraction, leave 8 bits for table select

		for (i = 0; table_select > 0; i++)
			table_select = table_select >> 1;

		crossfade = (1 << (23 + i)) - 1;	// Make a bit mask to remove the first bit of phase rate
	//	System.out.printf("%08X = crossfade mask\n%08X = phase rate\n", crossfade, phase_rate);
		crossfade &= phase_rate;
		crossfade = crossfade >> (i + 11);	// Leave 12 bits for crossfade
	//	if (crossfade > 4095) System.out.println("Crossfade error");
	//	crossfade = 4095;

		base_lo = (int)(i << 8);
	//	if (i < 7) i++;	// crossfade only if not at highest table (1/4 to 1/2 sample rate)
		if (i < 7 && phase_rate > 0x00FFFFFFL) i++;	// crossfade only if not at highest table (1/4 to 1/2 sample rate)
		base_hi = (int)(256 * i);
	//	System.out.printf("base_lo = %d, base_hi = %d\n", base_lo, base_hi);

		for (i = 0; i < length; i++) {
			index_lo = tphase >> 24;
			index_hi = ((tphase >> 24) + 1) & 0xFFL;
		//	if (index_lo > 128 && index_lo <= 292) System.out.printf("  index_lo = %d, inxed_hi = %d\n", index_lo, index_hi);

			high = ((long)saw_table[base_hi + (int)index_hi] * crossfade) >> 12;
			high += ((long)saw_table[base_lo + (int)index_hi] * (4095 - crossfade)) >> 12;

			low = ((long)saw_table[base_hi + (int)index_lo] * crossfade) >> 12;
			low += ((long)saw_table[base_lo + (int)index_lo] * (4095 - crossfade)) >> 12;

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

	@Override
	public void reset() {
		phase = 0;
	}

	public static void init() {	// forces the static constructor call, to prevent glitching
								// due to file operation (loading wavetable)
	}
/*
	public static void tableCrossfade(double freq, double sample_rate) {
		long i, tphase, phase_rate, table_select;
		long index_hi, index_lo;
		long high, low, frac, crossfade;
		int base_hi, base_lo;

		phase_rate = (long)(freq / sample_rate * 4294967296.0);	// originally 20-bit, add 4 bits for 24-bit fraction
		tphase = 0;

		table_select = phase_rate >> 24;	// 24 bits fraction, leave 8 bits for table select

		for (i = 0; table_select > 0; i++)
			table_select = table_select >> 1;

		crossfade = (1 << (23 + i)) - 1;	// Make a bit mask to remove the first bit of phase rate
		System.out.printf("%08X = crossfade mask\n%08X = phase rate\n", crossfade, phase_rate);
		crossfade &= phase_rate;
		System.out.printf("%08X = phase_rate after masking\n", crossfade);
		crossfade = crossfade >> (i + 11);	// Leave 12 bits for crossfade
		System.out.printf("%d = final crossfade\n", crossfade);
	//	if (crossfade > 4095) System.out.println("Crossfade error");
		crossfade = 4095;

		base_lo = (int)(i << 8);
		System.out.printf("Tables %d ", i);
		if (i < 7 && phase_rate > 0x01FFFFE7L) i++;	// crossfade only if not at highest table (1/4 to 1/2 sample rate)
		base_hi = (int)(256 * i);
		System.out.printf("and %d chosen\n", i);
	}
*/
}
