import javax.sound.sampled.*;
import java.util.Calendar;

import java.util.ArrayList;

class SignalHandler extends Thread {

	private AudioFormat		audio_format;	// set to 44.1 kHz 16-bit signed little-endian
	private float 			sample_rate;
	private float			ms_per_sample;
	private SourceDataLine	line_out;

	private int				step_length;		// default 128, small values result in smooth pitch changes
	private int				steps_per_buffer;
	private int				sleep_ms, sleep_ns;
	private int				volume_shift;		// divide volume by power of 2

	private ArrayList<Oscillator> oscillators;	// initially 16 bit, rename float oscillators to something else
	private ArrayList<ArrayList<Oscillator>> sets;

//	thread safety so that oscillators do not get overwritten while inside the signal loop
	private Oscillator		modify_me;
	private int				modify_action;		// 0 = do nothing, 1 = add osc (modify_me), 2 = remove osc (modify_me), 3 = clear all oscillators

	private short[] 		signal_buffer;
	private byte[] 			audio_buffer;

	private boolean			run_me, write_lock, run_lock;

	public SignalHandler() {
		System.out.println("Setting up audio systems...");
		run_me = true;

		sample_rate = 44100.0f;
		ms_per_sample = 1.0f / 44.100f;

	try {
		step_length = Integer.parseInt(Edox.settings().sub("buffer").attr("segment"));
		steps_per_buffer = Integer.parseInt(Edox.settings().sub("buffer").attr("count"));
	} catch (Exception e) {
		step_length = 128;		// this is how many samples the thread attempts to write at a time
		steps_per_buffer = 32;	//
	}

	try {
		volume_shift = Integer.parseInt(Edox.settings().sub("volume").attr("bitshift"));
	} catch (Exception e) {
		volume_shift = 4;
	}
		sleep_ms = (int)(step_length * ms_per_sample);	// sleep for approximately 1 buffer step
		sleep_ns = (int)(step_length * ms_per_sample * 1000000.0) % 1000000;
		write_lock = false;
		run_lock = false;

	// Audio interfacing stuff.
	// last parameter of audio format is byte endiandness, java ints are stored in big endian format
		audio_format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, (float)44100.0, 16, 1, 2, 44100, true);
		try {
			// times 2 because buffer consists of shorts
			line_out = AudioSystem.getSourceDataLine(audio_format);
			System.out.println(line_out.getLineInfo());
		} catch (Exception e) {
			System.out.println("ERROR: SignalHandler(): No device available!");
		}

		oscillators = new ArrayList<Oscillator>();
		sets = new ArrayList<ArrayList<Oscillator>>();
		signal_buffer = new short[step_length * steps_per_buffer];
		audio_buffer = new byte[step_length * steps_per_buffer * 2];
	}

	public int getSleep() {
		return sleep_ms;
	}

	public double getSampleRate() {
		return sample_rate;
	}

	public int getStep() {	// 
		return step_length;
	}

	public static void processAudio() {
	// use .yield() to give up processor time
	/*	
		try {
			int buffer_length = 32768;
			byte[] buffer = new byte[buffer_length];
			long start = Calendar.getInstance().getTimeInMillis();
			line_out.open(audio_format, buffer_length);
			line_out.start();
			for (; Calendar.getInstance().getTimeInMillis() - start <= 2000;) { // 10 buffer cycles
				for (int j = 0; j < buffer_length >> 1; j++) {
					phase += phase_rate;
					phase2 += phase_rate2;
					if (phase >= 1.0f) phase = 0.0f;
					if (phase2 >= 1.0f) phase2 = 0.0f;
					int value = (int)(phase * 32768) + (int)(phase2 * 32768) - 32768;
					buffer[j + 1] = (byte)(value & 0x00FF);
					buffer[j] = (byte)(value >> 8);
				}
				line_out.write(buffer, 0, buffer_length >> 1);
			}

			line_out.stop();
		} catch (Exception e) {
			System.out.println("No playback possible!");
		} */
		return;
	}

	public void run() {
	for (;;) {
		try {
			line_out.open(audio_format, step_length * steps_per_buffer * 2);
			line_out.start();
			System.out.printf("SignalHandler.run(): Bytes available for writing: %d\n", line_out.available());
			for (;;) {

			// see if there's an operation waiting to be done
				if (modify_action > 0) {
					if (modify_action == 1) {
						oscillators.add(modify_me);
					//	System.out.println("SignalHandler.run(): Oscillator added");
					}
					if (modify_action == 2) thd_removeOscillator();
					if (modify_action == 3) thd_clearOscillators();

					modify_action = 0;
					modify_me = null;
				}
			
			// build a new buffer step if there is space in the buffer, else sleep for 1 buffer step
				if (sets != null && line_out.available() >= (step_length << 1)) {
					write_lock = true;

					for (int i = 0; i < step_length; i++)
						signal_buffer[i] = 0;

				// mix all the oscillators together
					for (int i = 0; i < sets.size(); i++) {
						if (sets.get(i) == null) continue;
						for (int j = 0; j < sets.get(i).size(); j++) {
							if (sets.get(i).get(j).active() == false) continue;
							short[] temp = sets.get(i).get(j).getSignal16(step_length);
							for (int k = 0; k < step_length; k++)
								signal_buffer[k] += temp[k] >> volume_shift;
							sets.get(i).get(j).update();
						}
					}
				// transform shorts into bytes
					for (int i = 0; i < step_length; i++) {
						audio_buffer[i << 1] = (byte)((signal_buffer[i] >> 8) & 0xFF);
						audio_buffer[(i << 1) + 1] = (byte)(signal_buffer[i] & 0xFF);
					}

					line_out.write(audio_buffer, 0, step_length << 1);
					write_lock = false;
					continue;
				} else {
					write_lock = false;
					this.sleep(sleep_ms, sleep_ns);	// sleep a while to see if buffer can be written into later
				}
			}
		} catch (Exception e) {
			System.out.println("ERROR: SignalHandler(): Playback error - " + e);
			write_lock = false;
			run_lock = false;
			e.printStackTrace();
			try {
				this.sleep(sleep_ms, sleep_ns);
			} catch (Exception f) { }
		}
	}
	}

	public void addOscillator(Oscillator p_osc) {
		int i; /*
		for (i = 0; i < oscillators.size(); i++) {	// see if it's already been added
			if (oscillators.get(i) == p_osc) return;
		}*/
		thd_waitloop();
		modify_action = 1;
		modify_me = p_osc;
	//	System.out.println("SignalHandler.addOscillator(): called and set");
	}

	private void thd_waitloop() {
		for (; write_lock == true; ) {
			try {
				this.sleep(sleep_ms);
			} catch (Exception e) { }
		}
		run_lock = true;
	}

	private void thd_removeOscillator() {
		int i;
		for (i = 0; i < oscillators.size(); i++) {
			if (oscillators.get(i) == modify_me) {
				oscillators.remove(i);
				return;
			}
		}
	}

	public void removeOscillator(Oscillator p_osc) {
		if (p_osc == null) return;
		thd_waitloop();

		modify_action = 2;
		modify_me = p_osc;
	}
/*
	public void setOscillators(ArrayList<Oscillator> p_oscs) {
		if (p_oscs != null) oscillators = p_oscs;
		return;
	}
*/
	public void clearOscillators() {
		thd_waitloop();
		modify_action = 3;
		modify_me = null;
		return;
	}

	public void thd_clearOscillators() {
		oscillators = null;
		oscillators = new ArrayList<Oscillator>();
	//	System.out.println("SignalHandler.clearOscillators(): Success");
		return;
	}
/*
	private class ContextHolder { // simple index into Context
		ArrayList<Oscillator> oscillators;
	}
*/

	public void newContext(ArrayList<Oscillator> p_oscs) {
		thd_waitloop();

		for (int i = 0; i < sets.size(); i++) {
			if (sets.get(i) == null) {
				sets.set(i, p_oscs);
				return;
			}
		}

		sets.add(p_oscs);
		run_lock = false;
		return;
	}

	public void removeContext(ArrayList<Oscillator> p_oscs) {
		thd_waitloop();

		for (int i = 0; i < sets.size(); i++) {
			if (sets.get(i) == p_oscs) {
				sets.set(i, null);
				run_lock = false;
				return;
			}
		}
		run_lock = false;
	}
}
