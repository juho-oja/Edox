import java.awt.*;
import java.awt.event.*;
import java.awt.Font;
import javax.swing.*;

import java.util.ArrayList;

// keep order, shift cents and reference tone

/*

*/

class TransposeWindow extends JFrame implements ActionListener {
	private Edox edox;
	private Scale scale;
	private JTextField t_newroot;
	private JButton b_cancel, b_ok;
	private String error;

	public TransposeWindow(Edox p_edox) {
		edox = p_edox;
		scale = p_edox.getActiveScale();
		this.setTitle("Transpose Scale");
		error = null;

		this.add(new RefPanel());
	//	makeTextFields();
		makeButtons();

		t_newroot = new JTextField();
			t_newroot.setBounds(250, 20, 100, 30);
			this.add(t_newroot);

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
			Scale scale = edox.getActiveScale();	// old scale
			if (scale == null) return;

			int newroot;
			double centshift;

			try {
				newroot = scale.ndeg() - Integer.parseInt(t_newroot.getText());
				centshift = scale.getDegree(newroot).getCents();
			} catch (Exception f) {
				return;
			}

			try {
				Scale newbuilt = new Scale(scale.getName(), scale.ndeg(), false);

				int reftone = scale.getRefIndex();	// original reference tone
				double ref_freq = scale.getFrequency(reftone);		// original reference frequency
				
				int counter = 0;	// start filling forward from new root
				for (int i = newroot; i < scale.ndeg(); i++) {
					double newcents = scale.getDegree(i).getCents() - centshift;
					Interval insert = new Interval(scale.getDegree(counter).getName(), "", newcents);
					newbuilt.setDegree(counter, insert);
					counter++;
				}

				for (int i = 0; i < newroot; i++) { // add remaining tones from the beginning of original scale
					double newcents = scale.getDegree(i).getCents() - centshift;
					if (newcents < 0.0) newcents += 1200.0;
					Interval insert = new Interval(scale.getDegree(counter).getName(), null, newcents);
					newbuilt.setDegree(counter, insert);
					counter++;
				}
				
				newbuilt.setRef(reftone, ref_freq);
				newbuilt.updateFreqs();
				edox.setScale(newbuilt);

				this.setVisible(false);
				this.dispose();
			} catch (Exception f) {
				error = "Invalid error";
				f.printStackTrace();
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
			g.drawString("New Scale Zero:", 20, 38);
			g.drawString("Note: function has no effect on equally tempered scales", 20, 88);
			if (error != null) g.drawString("Error: " + error, 20, 108);
		}
	}
}
