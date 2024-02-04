import java.awt.*;
import java.awt.event.*;
import java.awt.Font;
import javax.swing.*;

import java.io.FileWriter;
import java.util.ArrayList;

class ScaleEditor extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
	private int startx, starty, shiftx, shifty, initx, inity;
	private int sx, sy;				// selection x, y
	private int fontsize, row_height;

	private ArrayList<Interval> intervals;
	private ArrayList<Scale> scales;
	private Scale active_scale;
	private ArrayList<ArrayList<java.awt.Color>> highlights;

	public ScaleEditor(ArrayList<Interval> p_intervals) {
		intervals = p_intervals;

		sx = 0;
		sy = 0;
		shiftx = 0;
		shifty = 0;

		row_height = 25;
		fontsize = 14;
		//	setBounds(0, 80, Edox.this.getWidth(), Edox.this.getHeight() - 80);
		setBackground(new java.awt.Color(255,255,255));
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public ScaleEditor(ArrayList<Interval> p_intervals, ArrayList<Scale> p_scales) {
		intervals = p_intervals;
		scales = p_scales;

		sx = 0;
		sy = 0;
		shiftx = 0;
		shifty = 0;

		row_height = 25;
		fontsize = 14;
		//	setBounds(0, 80, Edox.this.getWidth(), Edox.this.getHeight() - 80);
		setBackground(new java.awt.Color(255,255,255));
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void updateData(ArrayList<Interval> p_intervals, ArrayList<Scale> p_scales) {
		intervals = p_intervals;
		scales = p_scales;
		repaint();
	}

	public void updateScale(Scale p_scale) {
		active_scale = p_scale;
		shifty = 0;
	}

	public int getSelectedX() {
		return 0;
	}

	public int getSelectedY() {
		return 0;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
	// Draw scale
		try {
		//	System.out.println("Drawing " + active_scale.ndeg() + " scale degrees");
			Interval paska = null;
		//	System.out.println("Drawing interval " + paska.getCents());

		//	System.out.println("Panel height: " + this.getHeight());
			// draw default
			int ref = active_scale.getRefIndex();
			for (int i = 0; i < active_scale.ndeg(); i++) {
				if (i * row_height + 50 - shifty < 0) continue;
				if (i * row_height + 20 - shifty > this.getHeight()) break;
				paska = active_scale.getDegree(i);
				g.drawString(String.format("%d", i), 40, i * row_height + 50 - shifty);

				if (paska == null) {
					g.drawString("null", 100, i * row_height + 50 - shifty);
					continue;
				}

				if (paska.getName() != null) g.drawString(paska.getName(), 100, i * row_height + 50 - shifty);
				g.drawString(String.format("%.3f", paska.getCents()), 240, i * row_height + 50 - shifty);
				g.drawString(String.format("%.6f", paska.getRatio()), 360, i * row_height + 50 - shifty);
				g.drawString(String.format("%.3f Hz", active_scale.getFrequency(i)), 480, i * row_height + 50 - shifty);

				if (ref == i) {
					g.drawString("R", 10, i * row_height + 50 - shifty);
					g.drawString("R", 600, i * row_height + 50 - shifty);
				}
			//	g.drawString("Sample text", 20, i * 30 + 30);
			}

			// draw up
			if (active_scale.octaveRepeat()) {
				boolean breaker = false;
				for (int i = 1; breaker == false; i++) { // octaves
				//	int oct = (int)Math.pow(2, i);
					int oct = 1 << i;
				// first y of set = active_scale.ndeg() * i + j * 30 + 50 - shifty
					for (int j = 0; j < active_scale.ndeg(); j++) {
						int tempy = i * active_scale.ndeg() * row_height + j * row_height + 50 - shifty;
						if (tempy < 20) continue;

						paska = active_scale.getDegree(j);
						g.drawString(String.format("+%d %d", i, j), 20, tempy);

						if (paska == null) {
							g.drawString("null", 80, tempy);
							continue;
						}

						if (paska.getName() != null) g.drawString(paska.getName(), 80, tempy);
						g.drawString(String.format("%.3f", paska.getCents()), 240, tempy);
						g.drawString(String.format("%.6f", paska.getRatio() * oct), 360, tempy);
						g.drawString(String.format("%.3f Hz", active_scale.getFrequency(j) * oct), 480, tempy);

						if (tempy > this.getHeight()) breaker = true;
					}
				}

			// draw down
				breaker = false;
				for (int i = -1; breaker == false; i--) {
					double oct = Math.pow(2, i);
					for (int j = active_scale.ndeg() - 1; j >= 0; j--) { 
						int tempy = i * active_scale.ndeg() * row_height + j * row_height + 50 - shifty;
						if (tempy > this.getHeight()) continue;

						paska = active_scale.getDegree(j);
						g.drawString(String.format("%d %d", i, j), 20, tempy);

						if (paska == null) {
							g.drawString("null", 80, tempy);
							continue;
						}

						if (paska.getName() != null) g.drawString(paska.getName(), 80, tempy);
						g.drawString(String.format("%.3f", paska.getCents()), 240, tempy);
						g.drawString(String.format("%.6f", paska.getRatio() * oct), 360, tempy);
						g.drawString(String.format("%.3f Hz", active_scale.getFrequency(j) * oct), 480, tempy);

						if (tempy < 0) breaker = true;
					}
				}
			}
		// Labels
			g.setColor(new java.awt.Color(255, 255, 255));
			g.fillRect(0,0, 800, 28);
			g.setColor(new java.awt.Color(0, 0 ,0));

			g.drawString("Index", 40, 20);
			g.drawString("Name", 100, 20);
			g.drawString("Cents", 240, 20);
			g.drawString("Ratio", 360, 20);
			g.drawString("Frequency", 480, 20);
			g.drawLine(10, 28, 800, 28);
		} catch (Exception e) {
		//	System.out.println("ScaleEditor.paintComponent(): Something went wrong - " + e.getMessage());
		}
	}

// MouseListener
	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		startx = e.getX();
		starty = e.getY();

		initx = shiftx;
		inity = shifty;
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}
		
	// MouseMotionListener
	public void mouseMoved(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		shiftx = initx + startx - e.getX();
		shifty = inity + starty - e.getY();
		repaint();
	}

// KeyListener
	public void keyPressed(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
	}
}


