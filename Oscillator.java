import java.awt.Color;
import java.util.ArrayList;

// Absolutely no advanced layout stuff, place everything manually based on pixels but allow linear scaling and panning
// Oscillator UI element should have a context menu, which allows for resetting, changing waveform etc.


class Oscillator {	// UI component, reports himself to sound thread, sound thread calls getPitch()

//	tuning with keyboard stuff
	private static double up_normal, up_fast, up_slow, stop;	// global acceleration constants

	private static double	up_max_normal, down_max_normal,
							up_max_fast, down_max_fast,
							up_max_slow, down_max_slow;

//	UI related stuff (for BookPage), not needed for audio part but stored here for the sake of convenience
	private String name, id;
	private String key;		// activation key
	private int guitype;	// 0 = normal, 1 = slim, 2 = super slim 

	protected int x, y;		// relative upper left corner x and y coordinates for JPanel
	protected int z1x1, z1y1, z1x2, z1y2;	// zone 1, lef / right click with mouse, scaled
	protected int z2x1, z2y1, z2x2, z2y2;	// zone 2, activation light, scaled

	private boolean fixed;	// UI offers a different element for an oscillator whose freq cannot be changed
	private boolean hidden;
	private boolean active;	// used by UI to turn light on and off
	private boolean intermittent;	// not toggled
	private boolean display;

	public java.awt.Color color;

//	Sound generation
	private int state;	// 0 = static, 1 = slow up, 2 = normal up, 3 = fast up, -1 = slow down, -2 normal, -3 fast down
	private int volume;	// 15 bits, 0 - 1 maps to 0 - 32767
	private double rate, freq, init, min, max;	// rate = rate of change, freq multiplied by it + 1 each block
	private Signal16	signal16;		// generator, could be anything that produces a 16-bit signal
	private int pw;						// pulse width for square

	static {
		double sample_rate = Edox.audio_handler.getSampleRate();
		double stepsize = Edox.audio_handler.getStep();
		double buffers_per_second = sample_rate / stepsize;
	
		stop = Math.pow(0.000001, 1.0 / (buffers_per_second * 0.5));
	//	System.out.printf("Stop factor: %f\n", stop);
	//	up_normal = Math.pow(1.1, 1.0 / buffers_per_second);
		up_slow		= 0.000000001 * (stepsize / 64.0);
		up_normal	= 0.00000002 * (stepsize / 64.0);
		up_fast		= 0.00000200 * (stepsize / 64.0);
	//	down_normal = 1.0 / up_normal;
		up_max_normal = 0.005 * (stepsize / 64.0);
		down_max_normal = -0.005 * (stepsize / 64.0);

		up_max_fast = 0.1 * (stepsize / 64.0);
		down_max_fast = -0.1 * (stepsize / 64.0);

		up_max_slow = 0.000001 * (stepsize / 64.0);
		down_max_slow = -0.000001 * (stepsize / 64.0);
	}

	public Oscillator(XMLTag xosc) {
		
	}

	public Oscillator() {
		color = new java.awt.Color(255, 255, 255);
		active = true;
		display = false;
		guitype = 0;
		volume = 32767;
		freq = 256.0;
		signal16 = new SawGenerator(44100.0);
		rate = 0.0;
		state = 0;
		min = 0.1;
		max = Edox.audio_handler.getSampleRate() * 0.495;
		fixed = false;
		active = false;
		intermittent = false;
		init = 256.0;
		id = null;
	}
/*
	public static void main(String[] args) {
		Oscillator osc4 = new Oscillator(11000.0);
		Oscillator osc1 = new Oscillator(11024.0);
		Oscillator osc2 = new Oscillator(11025.0);
		Oscillator osc3 = new Oscillator(11026.0);
	}
*/
/*
	public Oscillator(double p_freq) {
		active = true;
		if (p_freq >= 0.5 && p_freq <= 22000.0)
			freq = p_freq;
		else freq = 256.0;
		signal16 = new SawGenerator(44100.0);
		rate = 0.0;
		state = 0;
		min = 0;
		max = Edox.audio_handler.getSampleRate() * 0.495;
	}
*/

// circle, button with light for activation, on / off text
	public void update() {
		if (state == 0) {
			rate *= stop;
		}

	// slow mode
		if (state == 1) {
			rate += up_slow * (Math.random() + 1.0) * 0.5;
			if (rate > up_max_slow) rate = up_max_slow;
		}
		if (state == -1) {
			rate -= up_slow * (Math.random() + 1.0) * 0.5;
			if (rate < down_max_slow) rate = down_max_slow;
		}
	// normal mode
		if (state == 2) {
			rate += up_normal * (0.67 + Math.random() * 0.33);
			if (rate > up_max_normal) rate = up_max_normal;
		}
		if (state == -2) {
			rate -= up_normal * (0.67 + Math.random() * 0.33);
			if (rate < down_max_normal) rate = down_max_normal;
		}

	// fast mode
		if (state == 3) {
			rate += up_fast;
			if (rate > up_max_fast) rate = up_max_fast;
		}
		if (state == -3) {
			rate -= up_fast;
			if (rate < down_max_fast) rate = down_max_fast;
		}

	//	freq *= 1.0 + rate;
		freq = freq + freq * rate;
		if (freq > max) freq = max;
		if (freq < min) freq = min;
	}

	public void setState(int p_state) {
		if (p_state < -3 || p_state > 3) return;
		else state = p_state;
	}

	public void reset() {
		if (init > 0.0) freq = init;
	}

	public boolean intermittent() {
		return intermittent;
	}

	public void mouseXYZ(int dx, int dy, int dz) {	// transmit how mouse + wheel moved relative to last position

	}

// samples in blocks of 128

	public short[] getSignal16(int length) {
		if (volume == 32767) return signal16.generateBlock16((float)freq, length);
		short[] ret = signal16.generateBlock16((float)freq, length);
		long temp;

		for (int i = 0; i < ret.length; i++) {
			temp = ret[i] * volume;
			temp = temp >> 15;
			ret[i] = (short)temp;
		}
		return ret;
	}

//	generic setters
	public void setXY(int px, int py) {
		x = px;
		y = py;
	}

	public void setKey(String p_key) {
		if (p_key == null) return;
		if (p_key.length() > 1) return;
		key = p_key;
	}

	public void setActive(boolean p) {
		active = p;
	/*	if (active) System.out.println("Oscillator.setActive(): true");
		else System.out.println("Oscillator.setActive(): false");*/
	}

	public void setIntermittent(boolean p) {
		intermittent = p;
	}

	public void setInit(double p_init) {
		this.init = p_init;
	}

	public void setVol(float p_vol) {
		if (p_vol < 0.0 || p_vol > 1.0) return;
		volume = (int)(p_vol * 32767.0);
	}

	public void volUp() {
		if (volume < 16384) volume = volume << 1;
		else volume = 32767;
	}

	public void volDown() {
		if (volume > 1) volume = volume >> 1;
	}

	public void setDisplay(boolean state) {
		display = state;
	}

//	generic getters
	public int getGuiType() {
		return guitype;
	}

	public String key() {
		return key;
	}

	public double getFreq() {
		return freq;
	}

	public String getId() {
		return id;
	}

	public int getVolume() {
		return volume;
	}

	public boolean display() {
		return display;
	}

	public int x() {
		return x;
	}

	public int y() {
		return y;
	}

// read optionals
	public boolean active() {
		return active;
	}

	public boolean fixed() {
		return fixed;
	}

	public String name() {
		return this.name;
	}

// set optionals
	public void toggleActive() {
		active = !active;
	}

	public void toggleFixed() {
		fixed = !fixed;
	}

	public void setName(String p_name) {
		if (p_name != null) this.name = new String(p_name);
	}

	public void setNameX(int pnx) {
	}

	public void setId(String p_id) {
		id = p_id;
	}

	public void setMax(double p_freq) {
		if (p_freq >= 0.1 && p_freq < Edox.audio_handler.getSampleRate() * 0.495) max = p_freq;
	}

	public void setMin(double p_freq) {
		if (p_freq >= 0.1 && p_freq < Edox.audio_handler.getSampleRate() * 0.495) min = p_freq;
	}

	public void setFreq(double p_freq) {
		if (p_freq >= 0.1 && p_freq < Edox.audio_handler.getSampleRate() * 0.495) freq = p_freq;
	}

	public void setWaveform(String p_type) {
		if (p_type.equals("sine")) {
			signal16 = new SineGenerator(44100.0);
		//	System.out.println("Oscillator.setWaveform(): Changed waveform to sine");
			return;
		}

		if (p_type.equals("square")) {
			signal16 = new SquareGenerator(44100.0);
		//	System.out.println("Oscillator.setWaveform(): Changed waveform to square");
			return;
		}

		if (p_type.equals("saw")) signal16 = new SawGenerator(44100.0);
	}

	public void setColor(int r, int g, int b) {
		if (r < 0) r = 0; if (r > 255) r = 255;
		if (g < 0) g = 0; if (g > 255) g = 255;
		if (b < 0) b = 0; if (b > 255) b = 255;

		color = new java.awt.Color(r, g, b);
	}

	public void setColor(java.awt.Color p_color) {
		if (p_color != null) color = p_color;
		else color = Cgraph.white;
	}

	public void setGuiType(String p) {
		if (p.equals("small")) guitype = 1;
	}
}
