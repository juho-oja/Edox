import java.awt.*;
import java.awt.event.*;
import java.awt.Font;
import javax.swing.*;

import java.util.ArrayList;

class NewScale extends JFrame implements ActionListener {
	int type = 0;	// 0 = empty, 1 = equal division
	boolean default_type;	// false == empty, true = EDoO
	private String error = null;
	private Edox caller;
	private JTextField name, n_steps, ref_freq, ref_index;

	private JPanel radiobox;
	private ButtonGroup rb_type;
	private JRadioButton rb_edo, rb_empty;

	private JButton b_cancel, b_accept;

	public NewScale(Edox p_caller) {
		if (p_caller == null) throw new IllegalArgumentException("No callback reference");
		caller = p_caller;
		default_type = true;	// must agree with the default radio button selection

		name = new JTextField();
			name.setBounds(180, 20, 150, 30);
			this.add(name);

		n_steps = new JTextField();
			n_steps.setBounds(180, 60, 150, 30);
			this.add(n_steps);

		ref_freq = new JTextField();
			ref_freq.setBounds(180, 100, 150, 30);
			this.add(ref_freq);

		ref_index = new JTextField();
			ref_index.setBounds(180, 140, 150, 30);
			this.add(ref_index);

		setSize(500, 400);
		setLayout(null);
		setTitle("New Scale");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		radiobox = new JPanel(new GridLayout(2, 1));
			radiobox.setBounds(10, 180, 500, 80);

		rb_type = new ButtonGroup();

		rb_empty = new JRadioButton("Empty scale");
			rb_empty.setActionCommand("empty");
			rb_empty.addActionListener(this);
			rb_type.add(rb_empty);
			radiobox.add(rb_empty);
		rb_edo = new JRadioButton("Equal division of octave");
			rb_edo.setSelected(true);
			rb_edo.setActionCommand("edo");
			rb_edo.addActionListener(this);
			rb_type.add(rb_edo);
			radiobox.add(rb_edo);

		b_cancel = new JButton("Cancel");
			b_cancel.setActionCommand("cancel");
			b_cancel.addActionListener(this);
			b_cancel.setBounds(10, 330, 150, 30);
			this.add(b_cancel);

		b_accept = new JButton("Accept");
			b_accept.setActionCommand("accept");
			b_accept.addActionListener(this);
			b_accept.setBounds(180, 330, 150, 30);
			this.add(b_accept);

		this.add(radiobox);

		this.add(new DrawingArea());
		this.setVisible(true);
	//	repaint();
	}

	public Scale initScale() {
		return null;
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if (command.equals("cancel")) {
			setVisible(false);
			dispose();
		}

		if (command.equals("empty")) {
			default_type = false;
		}

		if (command.equals("edo")) {
		//	System.out.println("Value set to true (EDoO)");
			default_type = true;
		}

		if (command.equals("accept")) {
			if (default_type) doEdo();
			else doEmpty();
		}
	}

	private void doEmpty() {
		int steps = 0;
		try {
			steps = Integer.parseInt(n_steps.getText());
		} catch (Exception e) {
			error = "Could not parse number of steps";
			repaint();
			return;
		}
		setVisible(false);
		dispose();
	}

	private void doEdo() {
		String passname = null;
		int steps = 0;
		int refind = 0;
		double reffreq = 0;
		boolean freqfailure = false;

		try {
			steps = Integer.parseInt(n_steps.getText());
		} catch (Exception e) {
			error = "Could not parse number of steps";
			repaint();
			return;
		}

		try {
			reffreq = Double.parseDouble(ref_freq.getText());
		} catch (Exception e) {
			error = "Could not read reference frequency";
			freqfailure = true;
		}

		if (freqfailure || ref_index.getText().equals("")) {
			refind = 0;
		} else try {
				refind = Integer.parseInt(ref_index.getText());
			} catch (Exception e) {
				error = "Could not parse reference scale degree";
				repaint();
				return;
			}
		
		if (name.getText().equals("")) passname = null;
		else passname = name.getText();
		
		try {
			Scale tempscale = new Scale(passname, steps, default_type);
			if (!freqfailure) tempscale.setRef(refind, reffreq);
			tempscale.print();
			caller.setScale(tempscale);
		} catch (Exception e) {
			error = "Scale creation failed: " + e.getMessage();
			repaint();
			return;
		}

		setVisible(false);
		dispose();
	}

	private class DrawingArea extends JPanel {
		public DrawingArea() {
			setBounds(0, 0, 640, 480);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawString("Scale name:", 20, 38);
			g.drawString("Number of steps:", 20, 78);
			g.drawString("Reference frequency:", 20, 118);
			g.drawString("Reference scale degree:", 20, 158);
			if (error != null) {
				g.setColor(new java.awt.Color(255, 40, 30));
				g.drawString(error, 20, 288);
				g.setColor(new java.awt.Color(0, 0, 0));
			}
		}
	}
}
