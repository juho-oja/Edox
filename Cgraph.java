import java.awt.Graphics;
import java.awt.Font;
import java.util.ArrayList;

class Cgraph {
	public static final int BLACK = 0;
	public static final int WHITE = 1;
	public static final int BLUE = 2;
	public static final int RED = 3;
	public static java.awt.Color black, white, blue, red;

	public static final int SMALL = 10;
	public static final int NORMAL = 12;
	public static final int LARGE = 14;
	public static final int EXTRA_LARGE = 18;

	public static ArrayList<Font> fonts;
	public static ArrayList<Font> fonts_thin;

	static {	//	Font size in px is index + 8
		fonts = new ArrayList<Font>();
		fonts_thin = new ArrayList<Font>();

		for (int i = 0; i < 16; i++)
			fonts.add(new Font("Arial Bold", 0, i + 8));

		for (int i = 0; i < 16; i++)
			fonts_thin.add(new Font("Arial", 0, i + 8));

		black = new java.awt.Color(0, 0, 0);
		white = new java.awt.Color(255, 255, 255);
		blue = new java.awt.Color(160, 180, 255);
		red = new java.awt.Color(255, 140, 120);
	}

	private java.awt.Color stroke_color;
	private java.awt.Color fill_color;

	private int fontsize;	// straight up index into the font table, size - 8

	public Cgraph() {
		stroke_color = black;
		fill_color = white;
		fontsize = 4;
	}

	public void setStroke(int p) {
		if (p == 0) stroke_color = black;
		if (p == 1) stroke_color = white;
		if (p == 2) stroke_color = blue;
		if (p == 3) stroke_color = red;
	}

	public void setStroke(int r, int g, int b) {
		if (r < 0 || g < 0 || b < 0) return;
		if (r > 255 || g > 255 || b > 255) return;
		stroke_color = new java.awt.Color(r, g, b);
	}

	public void setFill(int r, int g, int b) {
		if (r < 0 || g < 0 || b < 0) return;
		if (r > 255 || g > 255 || b > 255) return;
		fill_color = new java.awt.Color(r, g, b);
	}

	public void fontSize(int p) {
		if (p < 8 || p > 24) return;
		fontsize = p - 8;
	}

	public void contrastString(Graphics g, String p_text, int px, int py) {
		g.setColor(black);
		g.setFont(fonts.get(fontsize));
		g.drawString(p_text, px - 1, py - 1);
		g.drawString(p_text, px + 1, py - 1);
		g.drawString(p_text, px - 1, py + 1);
		g.drawString(p_text, px + 1, py + 1);

		g.setColor(stroke_color);
		g.setFont(fonts.get(fontsize));
		g.drawString(p_text, px, py);
	}

	public void thinString(Graphics g, String p_text, int px, int py) {
		g.setColor(black);
		g.setFont(fonts_thin.get(fontsize));
	//	g.setFont(fonts.get(fontsize));
		g.drawString(p_text, px - 1, py - 1);
		g.drawString(p_text, px + 1, py - 1);
		g.drawString(p_text, px - 1, py + 1);
		g.drawString(p_text, px + 1, py + 1);

		g.drawString(p_text, px, py - 2);
		g.drawString(p_text, px + 2, py);
		g.drawString(p_text, px - 2, py);
		g.drawString(p_text, px, py + 2);

		g.setColor(stroke_color);
		g.drawString(p_text, px, py);
	}

	public void drawOriginSymbol(Graphics g, int px, int py) {
		g.setColor(black);
		g.drawRect(px - 3, py - 3, 6, 6);
		g.drawLine(px,py, px, py);
		g.setColor(stroke_color);
		g.drawRect(px - 2, py - 2, 4, 4);
	}

	public void drawAirbase(Graphics g, int px, int py) {
		g.setColor(black);
	//	g.fillOval();
		g.drawRect(px - 3, py - 3, 6, 6);
		g.setColor(stroke_color);
		g.drawRect(px - 2, py - 2, 4, 4);
	}

	public static java.awt.Color parseColor(String string) {
		if (string == null) return null;
		if (string.length() != 6) throw new IllegalArgumentException(".constr_parseColor(): Invalid color length");
		int r, g, b;
		r = charValue(string.charAt(0)) << 4;
		r += charValue(string.charAt(1));

		g = charValue(string.charAt(2)) << 4;
		g += charValue(string.charAt(3));

		b = charValue(string.charAt(4)) << 4;
		b += charValue(string.charAt(5));

		return new java.awt.Color(r, g, b);
	}

	private static int charValue(char c) {
		if (c >= '0' && c <= '9')
			return c - '0';

		if (c >= 'a' && c <= 'f')
			return 10 + c - 'a';

		throw new IllegalArgumentException(String.format(".charValue(): %c is not a valid hex digit", c));
	}
/*
	public void contrastString(Graphics g, int p_size, String p_text, int px, int py) {
	}
*/
}
