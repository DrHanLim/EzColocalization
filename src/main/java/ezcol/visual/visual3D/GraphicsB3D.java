package ezcol.visual.visual3D;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class GraphicsB3D {

	private int colorValue = Color.BLACK.getRGB();
	private BufferedImage txtImage;
	private Graphics txtGraphics;
	private int[] pixels;
	private int width, height;

	public GraphicsB3D(int width, int height) {
		this(null, width, height);
	}

	public GraphicsB3D(int[] pixels, int width, int height) {
		if (pixels != null && pixels.length == width * height) {
			this.pixels = pixels.clone();
			this.width = width;
			this.height = pixels.length / width;
		} else {
			this.pixels = new int[width * height];
			this.width = width;
			this.height = height;
		}
		txtImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		txtGraphics = txtImage.getGraphics();
		txtGraphics.setColor(new Color(255, 255, 255, 0));
		txtGraphics.fillRect(0, 0, width, height);
	}

	public int[] getPixels() {
		return pixels.clone();
	}

	public BufferedImage getImage() {
		BufferedImage bimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics bGr = bimage.getGraphics();
		// Draw the image on to the buffered image
		bimage.setRGB(0, 0, width, height, pixels, 0, width);
		bGr.drawImage(txtImage, 0, 0, null);
		bGr.dispose();
		return bimage;
	}

	public void setRGB(int colorValue) {
		this.colorValue = colorValue;
	}

	public int getRGB() {
		return colorValue;
	}

	public void setColor(Color color) {
		this.colorValue = color.getRGB();
	}

	public Color getColor() {
		return new Color(colorValue);
	}

	public void setFont(Font font) {
		txtGraphics.setFont(font);
	}

	public Font getFont() {
		return txtGraphics.getFont();
	}

	/**
	 * Custom function to draw a line to an integer array
	 * <p>
	 * see <A href=
	 * "http://www.geeksforgeeks.org/mid-point-line-generation-algorithm/"> http
	 * ://www.geeksforgeeks.org/mid-point-line-generation-algorithm/ </A>.
	 * 
	 * @param pixels
	 *            input
	 * @param width
	 *            offset
	 * @param x1
	 *            the first point's <i>x</i> coordinate.
	 * @param y1
	 *            the first point's <i>y</i> coordinate.
	 * @param x2
	 *            the second point's <i>x</i> coordinate.
	 * @param y2
	 *            the second point's <i>y</i> coordinate.
	 * @see Graphics
	 * 
	 */
	void drawLine(int x1, int y1, int x2, int y2) {
		int increX = y1 == y2 ? 0 : (y1 < y2 ? 1 : -1);
		int increY = x1 == x2 ? 0 : (x1 < x2 ? 1 : -1);

		// calculate dx & dy
		int dy = y2 - y1;
		int dx = x2 - x1;
		int y1_y2 = y1 + y2, x1_x2 = x1 + x2;
		boolean xDominant = dx * increY < dy * increX;

		// initial value of decision parameter d
		int d;
		if (xDominant)
			d = dx * increX - (dy / 2) * increY;
		else
			d = dy * increY - (dx / 2) * increX;

		int x = x1, y = y1;

		// Plot initial given point
		// putpixel(x,y) can be used to print pixel
		// of line in graphics
		drawPixel(x, y);
		drawPixel(x1_x2 - x, y1_y2 - y);

		if (increX == 0 && increY == 0)
			return;

		boolean notMid = true;

		// iterate through value of X
		while (notMid) {

			// E or East is chosen
			if (xDominant) {

				if (d * increX * increY <= 0) {
					d += dx * increX;
				}
				// NE or North East is chosen
				else {
					d += (dx * increX - dy * increY);
					x += increY;
				}

				y += increX;

				notMid = (increX >= 0 && y <= y1_y2 / 2) || (increX <= 0 && y >= y1_y2 / 2);

			} else {

				if (d * increX * increY <= 0) {
					d += dy * increY;
				} else {
					d += (dy * increY - dx * increX);
					y += increX;
				}

				x += increY;

				notMid = (increY >= 0 && x <= x1_x2 / 2) || (increY <= 0 && x >= x1_x2 / 2);
			}

			// Plot intermediate points
			// putpixel(x,y) is used to print pixel
			// of line in graphics
			drawPixel(x, y);
			drawPixel(x1_x2 - x, y1_y2 - y);
		}
	}
	
	void drawOval(int sx, int sy, int w, int h){
		if(w==h)
			drawOval(sx,sy,w);
		else
			drawEllipse(sx,sy,w,h);
	}
	

	void drawEllipse(int sx, int sy, int w, int h) {
		int x, y;
		int offsetA = w % 2, offsetB = h % 2;
		int a = (w - offsetA) / 2, b = (h - offsetB) / 2;
		int a2 = a * a, b2 = b * b;
		int cx = (w + offsetA) / 2, cy = (h + offsetB) / 2;
		x = w - cx;
		y = b - cy;

		drawPixel(x + cx + sx, y + cy + sy);

		// When radius is zero only a single
		// point will be printed
		if (w > 0) {
			drawPixel(x + cx + sx, h - y - cy + sy);
			drawPixel(w - x - cx + sx, y + cy + sy);
			drawPixel(w - x - cx + sx, h - y - cy + sy);
		}

		// Initialising the value of P
		int P = x * x * b2 - x * b2 + b2 / 4 + a2 - a2 * b2;

		while (x * b >= y * a) {

			y++;

			// Mid-point is inside or on the perimeter
			if (P <= 0)
				P = P + 2 * y * a2 + a2;

			// Mid-point is outside the perimeter
			else {

				x--;

				P = P + 2 * y * a2 + a2 - 2 * x * b2;

			}

			// All the perimeter points have already been printed
			if (x * b < y * a)
				break;

			// Printing the generated point and its reflection
			// in the other octants after translation

			drawPixel(x + cx + sx, y + cy + sy);
			drawPixel(x + cx + sx, h - y - cy + sy);
			drawPixel(w - x - cx + sx, h - y - cy + sy);
			drawPixel(w - x - cx + sx, y + cy + sy);

		}

		x = a - cx;
		y = h - cy;

		drawPixel(x + cx + sx, y + cy + sy);

		// When radius is zero only a single
		// point will be printed
		if (w > 0) {
			drawPixel(x + cx + sx, h - y - cy + sy);
			drawPixel(w - x - cx + sx, y + cy + sy);
			drawPixel(w - x - cx + sx, h - y - cy + sy);
		}

		P = y * y * a2 - y * a2 + a2 / 4 + b2 - a2 * b2;
		while (x * b <= y * a) {

			x++;

			// Mid-point is inside or on the perimeter
			if (P <= 0)
				P = P + 2 * x * b2 + b2;

			// Mid-point is outside the perimeter
			else {

				y--;

				P = P + 2 * x * b2 + b2 - 2 * y * a2;

			}

			// All the perimeter points have already been printed
			if (x * b > y * a)
				break;

			// Printing the generated point and its reflection
			// in the other octants after translation

			drawPixel(x + cx + sx, y + cy + sy);
			drawPixel(x + cx + sx, h - y - cy + sy);
			drawPixel(w - x - cx + sx, h - y - cy + sy);
			drawPixel(w - x - cx + sx, y + cy + sy);

		}
	}

	void drawOval(int sx, int sy, int size) {

		int x, y;
		int offset = size % 2;
		int r = (size - offset) / 2;
		int c = (size + offset) / 2;
		x = size - c;
		y = r - c;

		drawPixel(x + c + sx, y + c + sy);

		// When radius is zero only a single
		// point will be printed
		if (size > 0) {
			drawPixel(x + c + sx, size - y - c + sy);
			drawPixel(size - x - c + sx, y + c + sy);
			drawPixel(size - x - c + sx, size - y - c + sy);
			
			drawPixel(y + c + sx, x + c + sy);
			drawPixel(y + c + sx, size - x - c + sy);
			drawPixel(size - y - c + sx, x + c + sy);
			drawPixel(size - y - c + sx, size - x - c + sy);
		}

		// Initialising the value of P
		int P = x * x - x + 1 - r * r;

		while (x > y) {

			y++;

			// Mid-point is inside or on the perimeter
			if (P <= 0)
				P += + 2 * y + 1;

			// Mid-point is outside the perimeter
			else {

				x--;

				P += + 2 * y  + 1 - 2 * x;

			}

			// All the perimeter points have already been printed
			if (x < y)
				break;

			// Printing the generated point and its reflection
			// in the other octants after translation

			drawPixel(x + c + sx, y + c + sy);
			drawPixel(x + c + sx, size - y - c + sy);
			drawPixel(size - x - c + sx, size - y - c + sy);
			drawPixel(size - x - c + sx, y + c + sy);

			if(x!=y){
				drawPixel(y + c + sx, x + c + sy);
				drawPixel(y + c + sx, size - x - c + sy);
				drawPixel(size - y - c + sx, size - x - c + sy);
				drawPixel(size - y - c + sx, x + c + sy);
			}

		}
	}
	
	void fillOval(int sx, int sy, int w, int h){
		if(w==h)
			fillOval(sx,sy,w);
		else
			fillEllipse(sx,sy,w,h);
	}
	
	void fillOval(int sx, int sy, int size) {

		int x, y;
		int offset = size % 2;
		int r = (size - offset) / 2;
		int c = (size + offset) / 2;
		x = size - c;
		y = r - c;

		drawPixel(x + c + sx, y + c + sy);

		// When radius is zero only a single
		// point will be printed
		if (size > 0) {
			for (int j = size - y - c; j <= y+c; j++) {
				drawPixel(x + c + sx, j + sy);
				drawPixel(size - x - c + sx, j + sy);
				drawPixel(j + sx, x +c + sy);
				drawPixel(j + sx, size - x - c + sy);
			}
		}

		// Initialising the value of P
		int P = x * x - x + 1 - r * r;

		while (x > y) {

			y++;

			// Mid-point is inside or on the perimeter
			if (P <= 0)
				P += + 2 * y + 1;

			// Mid-point is outside the perimeter
			else {

				x--;

				P += + 2 * y  + 1 - 2 * x;

			}

			// All the perimeter points have already been printed
			if (x < y)
				break;

			// Printing the generated point and its reflection
			// in the other octants after translation

			for (int j = size - y - c; j <= y+c; j++) {
				drawPixel(x + c + sx, j + sy);
				drawPixel(size - x - c + sx, j + sy);
				drawPixel(j + sx, x +c + sy);
				drawPixel(j + sx, size - x - c + sy);
				
			}

		}
		
		for (int i = size - x - c + sx; i <= x + c + sx; i++) {
			for (int j = size - y - c + sy; j <= y + c + sy; j++) {
				drawPixel(i, j);
			}
		}
		
	}
	
	void fillEllipse(int sx, int sy, int w, int h) {

		int x, y;
		int offsetA = w % 2, offsetB = h % 2;
		int a = (w - offsetA) / 2, b = (h - offsetB) / 2;
		int a2 = a * a, b2 = b * b;
		int cx = (w + offsetA) / 2, cy = (h + offsetB) / 2;
		int innerX, innerY;

		x = w - cx;
		y = b - cy;

		// When radius is zero only a single
		// point will be printed
		if (w > 0) {
			for (int j = h - y - cy + sy; j <= y + cy + sy; j++) {
				drawPixel(x + cx + sx, j);
				drawPixel(w - x - cx + sx, j);
			}
		}

		// Initialising the value of P
		int P = x * x * b2 - x * b2 + b2 / 4 + a2 - a2 * b2;

		while (x * b >= y * a) {

			y++;

			// Mid-point is inside or on the perimeter
			if (P <= 0)
				P = P + 2 * y * a2 + a2;

			// Mid-point is outside the perimeter
			else {

				x--;

				P = P + 2 * y * a2 + a2 - 2 * x * b2;

			}

			// All the perimeter points have already been printed
			if (x * b < y * a)
				break;

			// Printing the generated point and its reflection
			// in the other octants after translation

			for (int j = h - y - cy + sy; j <= y + cy + sy; j++) {
				drawPixel(x + cx + sx, j);
				drawPixel(w - x - cx + sx, j);
			}

		}

		innerX = x;
		innerY = y;

		x = a - cx;
		y = h - cy;

		// When radius is zero only a single
		// point will be printed
		if (h > 0) {
			for (int i = w - x - cx + sx; i <= x + cx + sx; i++) {
				drawPixel(i, y + cy + sy);
				drawPixel(i, h - y - cy + sy);
			}
		}

		P = y * y * a2 - y * a2 + a2 / 4 + b2 - a2 * b2;
		while (x * b <= y * a) {

			x++;

			// Mid-point is inside or on the perimeter
			if (P <= 0)
				P = P + 2 * x * b2 + b2;

			// Mid-point is outside the perimeter
			else {

				y--;

				P = P + 2 * x * b2 + b2 - 2 * y * a2;

			}

			// All the perimeter points have already been printed
			if (x * b > y * a)
				break;

			// Printing the generated point and its reflection
			// in the other octants after translation
			for (int i = w - x - cx + sx; i <= x + cx + sx; i++) {
				drawPixel(i, y + cy + sy);
				drawPixel(i, h - y - cy + sy);
			}

		}

		innerX = x < innerX ? x : innerX;
		innerY = y < innerY ? y : innerY;

		for (int i = w - innerX - cx + sx; i <= innerX + cx + sx; i++) {
			for (int j = h - innerY - cy + sy; j <= innerY + cy + sy; j++) {
				drawPixel(i, j);
			}
		}

	}

	void drawRect(int x, int y, int w, int h) {

		for (int i = x; i <= x + w; i++) {
			drawPixel(i, y);
			drawPixel(i, y + h);
		}

		for (int j = y; j <= y + h; j++) {
			drawPixel(x, j);
			drawPixel(x + w, j);
		}

	}

	void fillRect(int x, int y, int w, int h) {
		for (int i = x; i <= x + w; i++) {
			for (int j = y; j <= y + h; j++) {
				drawPixel(i, j);
			}
		}
	}

	void drawPixel(int x, int y) {
		int idx = y * width + x;
		if (x >= 0 && x < width && y >= 0 && y < pixels.length / width)
			pixels[idx] = colorValue;
	}

	void drawSquare(int x, int y, int size) {
		drawLine(x - size / 2, y - size / 2, x + size / 2, y - size / 2);
		drawLine(x + size / 2, y - size / 2, x + size / 2, y + size / 2);
		drawLine(x + size / 2, y + size / 2, x - size / 2, y + size / 2);
		drawLine(x - size / 2, y + size / 2, x - size / 2, y - size / 2);
	}

	void drawCircle(int x, int y, int size) {
		drawOval(x - size / 2, y - size / 2, size, size);
	}

	void drawFilledCircle(int x, int y, int size) {
		fillOval(x - size / 2, y - size / 2, size, size);
	}

	void drawFilledSquare(int x, int y, int size) {
		fillRect(x - size / 2, y - size / 2, size, size);
	}

	void drawPlus(int x, int y, int size) {
		drawLine(x - size / 2, y, x + size / 2, y);
		drawLine(x, y - size / 2, x, y + size / 2);
	}

	void drawX(int x, int y, int size) {
		drawLine(x - size / 2, y - size / 2, x + size / 2, y + size / 2);
		drawLine(x - size / 2, y + size / 2, x + size / 2, y - size / 2);
	}

	void drawTriangle(int x, int y, int size) {
		drawLine(x, y - size / 2, x - size / 2, y + size / 2);
		drawLine(x - size / 2, y + size / 2, x + size / 2, y + size / 2);
		drawLine(x + size / 2, y + size / 2, x, y - size / 2);
	}

	void draw_arrow(Graphics g, int x1, int y1, int x2, int y2, boolean thick) {
		float length = (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
		if (length == 0.0f) {
			return;
		}
		float xinc = (float) (x2 - x1) / length;
		float yinc = (float) (y2 - y1) / length;
		float crossx = (float) x2 - xinc * 5.0f;
		float crossy = (float) y2 - yinc * 5.0f;
		float x3 = crossx - yinc * 3.5f;
		float x4 = crossx + yinc * 3.5f;
		float y3 = crossy + xinc * 3.5f;
		float y4 = crossy - xinc * 3.5f;
		drawLine(x1, y1, x2, y2, thick);
		drawLine(x2, y2, Math.round(x3), Math.round(y3), thick);
		drawLine(x2, y2, Math.round(x4), Math.round(y4), thick);
	}

	void drawLine(int x1, int y1, int x2, int y2, boolean thick) {
		if (thick) {
			drawThickLine(x1, y1, x2, y2);
		} else {
			drawLine(x1, y1, x2, y2);
		}
	}

	void drawThickLine(int x1, int y1, int x2, int y2) {
		float length = (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
		float dx = (float) (x2 - x1) / length;
		float dy = (float) (y2 - y1) / length;
		float xpos = (float) x1;
		float ypos = (float) y1;
		for (int i = 0; i < (int) length; i++) {
			drawPixel((int) xpos, (int) ypos);
			xpos += dx;
			ypos += dy;
		}
		drawPixel(x2, y2);
	}

	/*
	 * void drawDot(int x,int y){ drawLine(x-1,y-1,x,y-1); drawLine(x-1,y,x,y);
	 * }
	 */

	/**
	 * Not very many string will be drew, there hold it up
	 */
	void drawString(String str, int x, int y) {
		txtGraphics.setColor(new Color(colorValue));
		txtGraphics.drawString(str, x, y);
	}

}
