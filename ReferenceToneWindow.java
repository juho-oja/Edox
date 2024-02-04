import java.awt.*;
import java.awt.event.*;
import java.awt.Font;
import javax.swing.*;

import java.util.ArrayList;

class ReferenceToneWindow extends JFrame implements ActionListener {
	private Edox edox;
	private Scale scale;
	private JTextField t_newroot, t_freq;
	private JButton b_cancel, b_ok;
	private String error;

	public ReferenceToneWindow(Edox p_edox) {
		edox = p_edox;
		scale = p_edox.getActiveScale();
		this.setTitle("Set Reference Tone");
		error = null;

		this.add(new RefPanel());
	//	makeTextFields();
		makeButtons();

		t_newroot = new JTextField();
			t_newroot.setBounds(250, 20, 100, 30);
			this.add(t_newroot);

		t_freq = new JTextField();
			t_freq.setBounds(250, 70, 100, 30);
			this.add(t_freq);

		try {
			t_freq.setText("" + scale.getFrequency(scale.getRefIndex()));
		} catch (Exception e) { };

		try {
			t_newroot.setText("" + scale.getRefIndex());
		} catch (Exception e) { };

		this.setLayout(null);
		this.setVisible(true);
		this.setSize(400,200);
		this.setResizable(false);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent w) {
				setVisible(false);
				dispose();
			}
		});
	}

	private void makeButtons() {
		b_cancel = new JButton("Cancel");
			b_cancel.addActionListener(this);
			b_cancel.setActionCommand("cancel");
			b_cancel.setBounds(20, 120, 120, 30);
			this.add(b_cancel);

		b_ok = new JButton("OK");
			b_ok.addActionListener(this);
			b_ok.setActionCommand("accept");
			b_ok.setBounds(260, 120, 120, 30);
			this.add(b_ok);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("cancel")) {
			setVisible(false);
			dispose();
		}

		if (cmd.equals("accept")) {
			try {
				int newroot = Integer.parseInt(t_newroot.getText());
				if (newroot < 0 || newroot >= scale.ndeg()) {
					error = "Error: Scale degree out of range";
					return;
				}

				double newfreq = Double.parseDouble(t_freq.getText());
				scale.setRef(newroot, newfreq);
			// destroy window
				edox.setScale(scale);

				this.setVisible(false);
				this.dispose();
			} catch (Exception f) {
				error = "Error: Invalid scale degree";
				return;
			}
		}
	}

	private class RefPanel extends JPanel {
		public RefPanel() {
			setBounds(0,0, 400,200);
		}
		@Override
		public void paintComponent(Graphics g) {
			g.drawString("Reference scale degree:", 20, 38);
			g.drawString("Reference frequency:", 20, 88);
			if (error != null) g.drawString("Error: " + error, 20, 108);
		}
	}
}
