import java.awt.*;
import java.awt.event.*;
import java.awt.Font;
import javax.swing.*;

import java.util.ArrayList;

class SwapRootWindow extends JFrame implements ActionListener {
	private Edox edox;
	private Scale scale;
	private XMLTag xmldata;
	private JTextField t_newroot;
	private JButton b_cancel, b_ok;
	private String error;

	public SwapRootWindow(Edox p_edox) {
		edox = p_edox;
		this.setTitle("Set New Scale Root");
		error = null;

		this.add(new SwapPanel());
	//	makeTextFields();
		makeButtons();

		t_newroot = new JTextField();
			t_newroot.setBounds(20, 60, 150, 30);
			this.add(t_newroot);

		this.setLayout(null);
		this.setVisible(true);
		this.setSize(400,200);
		this.setResizable(false);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent w) {
				xmldata = null;
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
			xmldata = null;
			setVisible(false);
			dispose();
		}

		if (cmd.equals("accept")) {
			scale = edox.getActiveScale();
			int newdeg;

			try {
				newdeg = Integer.parseInt(t_newroot.getText());
			} catch (Exception f) {
				error = "Invalid scale degree";
				return;
			};
			if (newdeg < 0 || newdeg >= scale.ndeg()) {
				error = "No such scale degree";
				return;
			}
			
			try {
				Scale newscale = new Scale(scale.getName(), scale.ndeg(), false);
				double ref_freq = scale.getFrequency(scale.getRefIndex());
				int new_ind = scale.getRefIndex() - newdeg;
				if (new_ind < 0) new_ind += scale.ndeg();
				newscale.setRef(new_ind, ref_freq);

				double newcents = scale.getDegree(newdeg).getCents();

				int c = 0;
				for (int i = newdeg; i < scale.ndeg(); i++) {
					double setcents = scale.getDegree(i).getCents() - newcents;
					if (setcents < 0.0) setcents += 1200.0;

					Interval set_me = new Interval(setcents);
					set_me.setName(scale.getDegree(i).getName());
					newscale.setDegree(c, set_me);

					c++;
				}

				for (int i = 0; i < newdeg; i++) {
					double setcents = scale.getDegree(i).getCents() - newcents;
					if (setcents < 0.0) setcents += 1200.0;

					Interval set_me = new Interval(setcents);
					set_me.setName(scale.getDegree(i).getName());
					newscale.setDegree(c, set_me);
					c++;
				}
				newscale.updateFreqs();
				edox.setScale(newscale);

				this.setVisible(false);
				this.dispose();
			} catch (Exception f) {
				f.printStackTrace();
			}
		}
	}

	private class SwapPanel extends JPanel {
		public SwapPanel() {
			setBounds(0,0, 400,200);
		}
		@Override
		public void paintComponent(Graphics g) {
			g.drawString("New Scale Root Degree:", 20, 38);
			if (error != null) g.drawString("Error: " + error, 20, 108);
		}
	}

}
