package testclasses;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.util.Random;

public class TestDrawingSpeed {

	static int width = 512, height = 624;
	static Color background = Color.GRAY;
	static int sizeA = 34, sizeB = 17;
	static int xlen = 21, ylen = 30;
	static int N = 1;

	public static void test2() {

		Random rnd = new Random();
		Image retimage;
		Graphics g;
		Color tempcolor;
		long time;

		time = System.currentTimeMillis();

		int[] bufferPixels = new int[width * height];

		for (int i = 0; i < N; i++) {
			int x = rnd.nextInt(width);
			int y = rnd.nextInt(height);
			drawEllipse(bufferPixels, width, height, x - sizeA / 2, y - sizeA / 2, sizeA, sizeB);

			x = rnd.nextInt(width);
			y = rnd.nextInt(height);
			drawEllipse(bufferPixels, width, height, x - sizeA / 2, y - sizeA / 2, sizeB, sizeA);

			x = rnd.nextInt(width);
			y = rnd.nextInt(height);
			drawEllipse(bufferPixels, width, height, x - sizeA / 2, y - sizeA / 2, sizeA, sizeA);

			x = rnd.nextInt(width);
			y = rnd.nextInt(height);
			drawEllipse(bufferPixels, width, height, x - sizeA / 2, y - sizeA / 2, sizeA + 21, sizeA + 21);
			
			
		}

		MemoryImageSource source = new MemoryImageSource(width, height, bufferPixels, 0, width);
		Image image = Toolkit.getDefaultToolkit().createImage(source);

		retimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		g = retimage.getGraphics();
		tempcolor = g.getColor();
		g.setColor(background);
		g.fillRect(0, 0, width, height);
		g.setColor(tempcolor);
		g.drawImage(image, 0, 0, null);

		System.out.println("draw on TYPE_INT_RGB : " + (System.currentTimeMillis() - time));
		time = System.currentTimeMillis();

		new ij.ImagePlus("test2", retimage).show();

	}

	public static void test6() {

		Random rnd = new Random();
		Image retimage;
		Graphics g;
		Color tempcolor;
		long time;

		time = System.currentTimeMillis();

		int[] bufferPixels = new int[width * height];

		for (int i = 0; i < N; i++) {
			int x = rnd.nextInt(width);
			int y = rnd.nextInt(height);
			fillOval(bufferPixels, width, height, x - sizeA / 2, y - sizeA / 2, sizeA, sizeB);

			x = rnd.nextInt(width);
			y = rnd.nextInt(height);
			fillOval(bufferPixels, width, height, x - sizeA / 2, y - sizeA / 2, sizeB, sizeA);

			x = rnd.nextInt(width);
			y = rnd.nextInt(height);
			fillOval(bufferPixels, width, height, x - sizeA / 2, y - sizeA / 2, sizeA, sizeA);

			x = rnd.nextInt(width);
			y = rnd.nextInt(height);
			fillOval(bufferPixels, width, height, x - sizeA / 2, y - sizeA / 2, sizeA + 21, sizeA + 21);
		}

		MemoryImageSource source = new MemoryImageSource(width, height, bufferPixels, 0, width);
		Image image = Toolkit.getDefaultToolkit().createImage(source);

		retimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		g = retimage.getGraphics();
		tempcolor = g.getColor();
		g.setColor(background);
		g.fillRect(0, 0, width, height);
		g.setColor(tempcolor);
		g.drawImage(image, 0, 0, null);

		System.out.println("draw on TYPE_INT_RGB : " + (System.currentTimeMillis() - time));
		time = System.currentTimeMillis();

		new ij.ImagePlus("test2", retimage).show();

	}

	public static void test4() {

		Random rnd = new Random();
		Image retimage;
		Graphics g;
		Color tempcolor;
		long time;

		time = System.currentTimeMillis();

		int[] bufferPixels = new int[width * height];

		for (int i = 0; i < N; i++) {
			int x = width / 2;// rnd.nextInt(width);
			int y = height / 2;// rnd.nextInt(height);
			// int xlen = rnd.nextInt(width / 10);
			// int ylen = rnd.nextInt(height / 10);
			drawLine(bufferPixels, width, height, x, y, x + xlen, y + ylen);
			drawLine(bufferPixels, width, height, x, y, x + xlen, y);
			drawLine(bufferPixels, width, height, x, y, x + xlen, y - ylen);
			drawLine(bufferPixels, width, height, x, y, x, y - ylen);
			drawLine(bufferPixels, width, height, x, y, x - xlen, y - ylen);
			drawLine(bufferPixels, width, height, x, y, x - xlen, y);
			drawLine(bufferPixels, width, height, x, y, x - xlen, y + ylen);
			drawLine(bufferPixels, width, height, x, y, x, y + ylen);
			drawRect(bufferPixels, width, height, x - xlen - 1, y - ylen - 1, xlen * 2 + 2, ylen * 2 + 2);
		}
		MemoryImageSource source = new MemoryImageSource(width, height, bufferPixels, 0, width);
		Image image = Toolkit.getDefaultToolkit().createImage(source);

		retimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		g = retimage.getGraphics();
		tempcolor = g.getColor();
		g.setColor(background);
		g.fillRect(0, 0, width, height);
		g.setColor(tempcolor);
		g.drawImage(image, 0, 0, null);

		System.out.println("draw on TYPE_INT_RGB : " + (System.currentTimeMillis() - time));
		time = System.currentTimeMillis();

		new ij.ImagePlus("test4", retimage).show();

	}

	public static void test8() {

		Random rnd = new Random();
		Image retimage;
		Graphics g;
		Color tempcolor;
		long time;

		time = System.currentTimeMillis();

		int[] bufferPixels = new int[width * height];

		for (int i = 0; i < N; i++) {
			int x = rnd.nextInt(width);
			int y = rnd.nextInt(height);
			fillRect(bufferPixels, width, height, x - sizeA / 2, y - sizeA / 2, sizeA, sizeB);

			x = rnd.nextInt(width);
			y = rnd.nextInt(height);
			fillRect(bufferPixels, width, height, x - sizeA / 2, y - sizeA / 2, sizeB, sizeA);

			x = rnd.nextInt(width);
			y = rnd.nextInt(height);
			fillRect(bufferPixels, width, height, x - sizeA / 2, y - sizeA / 2, sizeA, sizeA);

			x = rnd.nextInt(width);
			y = rnd.nextInt(height);
			fillRect(bufferPixels, width, height, x - sizeA / 2, y - sizeA / 2, sizeA + 21, sizeA + 21);
		}

		MemoryImageSource source = new MemoryImageSource(width, height, bufferPixels, 0, width);
		Image image = Toolkit.getDefaultToolkit().createImage(source);

		retimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		g = retimage.getGraphics();
		tempcolor = g.getColor();
		g.setColor(background);
		g.fillRect(0, 0, width, height);
		g.setColor(tempcolor);
		g.drawImage(image, 0, 0, null);

		System.out.println("draw on TYPE_INT_RGB : " + (System.currentTimeMillis() - time));
		time = System.currentTimeMillis();

		new ij.ImagePlus("test2", retimage).show();

	}

	public static void test10() {

		Random rnd = new Random();
		Image retimage;
		Graphics g;
		Color tempcolor;
		long time;

		

		int[] bufferPixels = new int[width * height];

		time = System.currentTimeMillis();
		for (int i = 0; i < N; i++) {
			int x = rnd.nextInt(width);
			int y = rnd.nextInt(height);
			fillOval(bufferPixels, width, height, x - sizeA / 2, y - sizeA / 2, sizeA, sizeA);

			x = rnd.nextInt(width);
			y = rnd.nextInt(height);
			fillOval(bufferPixels, width, height, x - sizeA / 2, y - sizeA / 2, sizeA + 21, sizeA + 21);
		}
		
		System.out.println("draw oval : " + (System.currentTimeMillis() - time));
		time = System.currentTimeMillis();
		
		time = System.currentTimeMillis();
		for (int i = 0; i < N; i++) {
			int x = rnd.nextInt(width);
			int y = rnd.nextInt(height);
			fillEllipse(bufferPixels, width, height, x - sizeA / 2, y - sizeA / 2, sizeA, sizeA);

			x = rnd.nextInt(width);
			y = rnd.nextInt(height);
			fillEllipse(bufferPixels, width, height, x - sizeA / 2, y - sizeA / 2, sizeA + 21, sizeA + 21);
		}
		
		System.out.println("draw ellipse : " + (System.currentTimeMillis() - time));
		time = System.currentTimeMillis();
		
		

		MemoryImageSource source = new MemoryImageSource(width, height, bufferPixels, 0, width);
		Image image = Toolkit.getDefaultToolkit().createImage(source);

		retimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		g = retimage.getGraphics();
		tempcolor = g.getColor();
		g.setColor(background);
		g.fillRect(0, 0, width, height);
		g.setColor(tempcolor);
		g.drawImage(image, 0, 0, null);

		System.out.println("draw on TYPE_INT_RGB : " + (System.currentTimeMillis() - time));
		time = System.currentTimeMillis();

		new ij.ImagePlus("test2", retimage).show();

	}
	
	public static void test12() {

		Random rnd = new Random();
		Image retimage;
		Graphics g;
		Color tempcolor;
		long time;

		

		int[] bufferPixels = new int[width * height];

		time = System.currentTimeMillis();
		for (int i = 0; i < N; i++) {
			int x = rnd.nextInt(width);
			int y = rnd.nextInt(height);
			drawOval(bufferPixels, width, height, x - sizeA / 2, y - sizeA / 2, sizeA, sizeA);

			x = rnd.nextInt(width);
			y = rnd.nextInt(height);
			drawOval(bufferPixels, width, height, x - sizeA / 2, y - sizeA / 2, sizeA + 21, sizeA + 21);
		}
		
		System.out.println("draw oval : " + (System.currentTimeMillis() - time));
		time = System.currentTimeMillis();
		
		time = System.currentTimeMillis();
		for (int i = 0; i < N; i++) {
			int x = rnd.nextInt(width);
			int y = rnd.nextInt(height);
			drawEllipse(bufferPixels, width, height, x - sizeA / 2, y - sizeA / 2, sizeA, sizeA);

			x = rnd.nextInt(width);
			y = rnd.nextInt(height);
			drawEllipse(bufferPixels, width, height, x - sizeA / 2, y - sizeA / 2, sizeA + 21, sizeA + 21);
		}
		
		System.out.println("draw ellipse : " + (System.currentTimeMillis() - time));
		time = System.currentTimeMillis();
		
		

		MemoryImageSource source = new MemoryImageSource(width, height, bufferPixels, 0, width);
		Image image = Toolkit.getDefaultToolkit().createImage(source);

		retimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		g = retimage.getGraphics();
		tempcolor = g.getColor();
		g.setColor(background);
		g.fillRect(0, 0, width, height);
		g.setColor(tempcolor);
		g.drawImage(image, 0, 0, null);

		System.out.println("draw on TYPE_INT_RGB : " + (System.currentTimeMillis() - time));
		time = System.currentTimeMillis();

		new ij.ImagePlus("test2", retimage).show();

	}
	
	/* http://www.geeksforgeeks.org/mid-point-line-generation-algorithm/ */
	public static void drawLine(int[] pixels, int width, int height, int x1, int y1, int x2, int y2) {
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
		drawDot(pixels, width, x, y);
		drawDot(pixels, width, x1_x2 - x, y1_y2 - y);
		// System.out.println("X1: " + X1 + ", Y1: " + Y1 + ", X1_X2 - x: " +
		// (X1_X2 - x) + ", Y1_Y2 - y: " + (Y1_Y2 - y));
		// System.out.println("X1: " + X1 + ", Y1: " + Y1 + ", X2: " + X2 + ",
		// Y2: " + Y2);

		// System.out.println("increX: " + increX + ", increY: " + increY);

		if (increX == 0 && increY == 0)
			return;

		boolean notMid = true;

		// iterate through value of X
		while (notMid) {

			// System.out.println("y: "+y+", Y2 - dy / 2: "+(Y2 - dy / 2));
			// System.out.println("x: " + x + ", y: " + y + ", d: " + d);

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
			drawDot(pixels, width, x, y);
			drawDot(pixels, width, x1_x2 - x, y1_y2 - y);
		}
	}

	private static void drawDot(int[] pixels, int offset, int x, int y, int value) {
		int idx = y * offset + x;
		if (x >= 0 && x < offset && y >= 0 && y < pixels.length / offset)
			pixels[idx] = value;
	}

	private static void drawDot(int[] pixels, int offset, int x, int y) {
		drawDot(pixels, offset, x, y, Color.WHITE.getRGB());
	}

	public static void drawOval2(int[] pixels, int width, int height, int sx, int sy, int w, int h) {

		int x, y;
		int a = (w + 1) / 2, b = (h + 1) / 2;
		int a2 = a * a, b2 = b * b;
		int cx = (w - 1) / 2, cy = (h + 1) / 2;
		x = w;
		y = b;

		drawDot(pixels, width, x + sx, y + sy);

		// When radius is zero only a single
		// point will be printed
		if (w > 0) {
			drawDot(pixels, width, x + sx, h - y + sy);
			drawDot(pixels, width, w - x + sx, y + sy);
			drawDot(pixels, width, w - x + sx, h - y + sy);
		}

		// Initialising the value of P
		int P = (x - cx) * (x - cx) * b2 - (x - cx) * b2 + b2 / 4 + a2 - a2 * b2;

		while (x - cx > y - cy) {

			System.out.println("x: " + (x - a) + ", y: " + (y - b) + ", P: " + P);

			y++;

			// Mid-point is inside or on the perimeter
			if (P <= 0)
				P = P + 2 * (y - cy) * a2 + a2;

			// Mid-point is outside the perimeter
			else {

				x--;

				P = P + 2 * (y - cy) * a2 + a2 - 2 * (x - cx) * b2;

			}

			// All the perimeter points have already been printed
			if (x - cx < y - cy)
				break;

			// Printing the generated point and its reflection
			// in the other octants after translation

			drawDot(pixels, width, x + sx, y + sy);
			drawDot(pixels, width, x + sx, h - y + sy);
			drawDot(pixels, width, w - x + sx, h - y + sy);
			drawDot(pixels, width, w - x + sx, y + sy);

		}

		x = a;
		y = h;

		drawDot(pixels, width, x + sx, y + sy);

		// When radius is zero only a single
		// point will be printed
		if (h > 0) {
			drawDot(pixels, width, w - x + sx, y + sy);
			drawDot(pixels, width, x + sx, h - y + sy);
			drawDot(pixels, width, w - x + sx, h - y + sy);
		}

		P = (y - cy) * (y - cy) * a2 - (y - cy) * a2 + a2 / 4 + b2 - a2 * b2;
		while (x - cx < y - cy) {
			x++;

			// Mid-point is inside or on the perimeter
			if (P <= 0)
				P = P + 2 * (x - cx) * b2 + b2;

			// Mid-point is outside the perimeter
			else {

				y--;

				P = P + 2 * (x - cx) * b2 + b2 - 2 * (y - cy) * a2;

			}

			// All the perimeter points have already been printed
			if (x - cx > y - cy)
				break;

			// Printing the generated point and its reflection
			// in the other octants after translation

			drawDot(pixels, width, x + sx, y + sy);
			drawDot(pixels, width, x + sx, h - y + sy);
			drawDot(pixels, width, w - x + sx, h - y + sy);
			drawDot(pixels, width, w - x + sx, y + sy);

		}

		drawRect(pixels, width, height, sx - 1, sy - 1, w + 2, h + 2);
	}
	
	public static void drawOval(int[] pixels, int width, int height, int sx, int sy, int w, int h) {
		if(w==h)
			drawCircle(pixels,width,height,sx,sy,w);
		else
			drawEllipse(pixels,width,height,sx,sy,w,h);
	}

	/* http://www.geeksforgeeks.org/mid-point-circle-drawing-algorithm/ */
	public static void drawEllipse(int[] pixels, int width, int height, int sx, int sy, int w, int h) {

		int x, y;
		int offsetA = w % 2, offsetB = h % 2;
		int a = (w - offsetA) / 2, b = (h - offsetB) / 2;
		int a2 = a * a, b2 = b * b;
		int cx = (w + offsetA) / 2, cy = (h + offsetB) / 2;
		x = w - cx;
		y = b - cy;

		drawDot(pixels, width, x + cx + sx, y + cy + sy);

		// When radius is zero only a single
		// point will be printed
		if (w > 0) {
			drawDot(pixels, width, x + cx + sx, h - y - cy + sy);
			drawDot(pixels, width, w - x - cx + sx, y + cy + sy);
			drawDot(pixels, width, w - x - cx + sx, h - y - cy + sy);
		}

		// Initialising the value of P
		int P = x * x * b2 - x * b2 + b2 / 4 + a2 - a2 * b2;

		while (x * b >= y * a) {

			// System.out.println("x: " + x + ", y: " + y);

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

			drawDot(pixels, width, x + cx + sx, y + cy + sy);
			drawDot(pixels, width, x + cx + sx, h - y - cy + sy);
			drawDot(pixels, width, w - x - cx + sx, h - y - cy + sy);
			drawDot(pixels, width, w - x - cx + sx, y + cy + sy);

		}

		x = a - cx;
		y = h - cy;

		drawDot(pixels, width, x + cx + sx, y + cy + sy);

		// When radius is zero only a single
		// point will be printed
		if (w > 0) {
			drawDot(pixels, width, x + cx + sx, h - y - cy + sy);
			drawDot(pixels, width, w - x - cx + sx, y + cy + sy);
			drawDot(pixels, width, w - x - cx + sx, h - y - cy + sy);
		}

		P = y * y * a2 - y * a2 + a2 / 4 + b2 - a2 * b2;
		while (x * b <= y * a) {

			//System.out.println("x: " + x + ", y: " + y);

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

			drawDot(pixels, width, x + cx + sx, y + cy + sy);
			drawDot(pixels, width, x + cx + sx, h - y - cy + sy);
			drawDot(pixels, width, w - x - cx + sx, h - y - cy + sy);
			drawDot(pixels, width, w - x - cx + sx, y + cy + sy);

		}

		drawRect(pixels, width, height, sx - 1, sy - 1, w + 2, h + 2);
	}

	public static void drawCircle(int[] pixels, int width, int height, int sx, int sy, int size) {

		int x, y;
		int offset = size % 2;
		int r = (size - offset) / 2;
		int c = (size + offset) / 2;
		x = size - c;
		y = r - c;

		drawDot(pixels, width, x + c + sx, y + c + sy);

		// When radius is zero only a single
		// point will be printed
		if (size > 0) {
			drawDot(pixels, width, x + c + sx, size - y - c + sy);
			drawDot(pixels, width, size - x - c + sx, y + c + sy);
			drawDot(pixels, width, size - x - c + sx, size - y - c + sy);
			
			drawDot(pixels, width, y + c + sx, x + c + sy);
			drawDot(pixels, width, y + c + sx, size - x - c + sy);
			drawDot(pixels, width, size - y - c + sx, x + c + sy);
			drawDot(pixels, width, size - y - c + sx, size - x - c + sy);
		}

		// Initialising the value of P
		int P = x * x - x + 1 - r * r;

		while (x > y) {

			 //System.out.println("x: " + x + ", y: " + y + ", P: "+P + ", r: "+r);

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

			drawDot(pixels, width, x + c + sx, y + c + sy);
			drawDot(pixels, width, x + c + sx, size - y - c + sy);
			drawDot(pixels, width, size - x - c + sx, size - y - c + sy);
			drawDot(pixels, width, size - x - c + sx, y + c + sy);

			if(x!=y){
				drawDot(pixels, width, y + c + sx, x + c + sy);
				drawDot(pixels, width, y + c + sx, size - x - c + sy);
				drawDot(pixels, width, size - y - c + sx, size - x - c + sy);
				drawDot(pixels, width, size - y - c + sx, x + c + sy);
			}

		}
	}

	public static void fillOval(int[] pixels, int width, int height, int sx, int sy, int w, int h){
		if(w==h)
			fillCircle(pixels,width,height,sx,sy,w);
		else
			fillEllipse(pixels,width,height,sx,sy,w,h);
	}
	
	public static void fillCircle(int[] pixels, int width, int height, int sx, int sy, int size) {

		int x, y;
		int offset = size % 2;
		int r = (size - offset) / 2;
		int c = (size + offset) / 2;
		x = size - c;
		y = r - c;

		drawDot(pixels, width, x + c + sx, y + c + sy);

		// When radius is zero only a single
		// point will be printed
		if (size > 0) {
			for (int j = size - y - c; j <= y+c; j++) {
				drawDot(pixels, width, x + c + sx, j + sy);
				drawDot(pixels, width, size - x - c + sx, j + sy);
				drawDot(pixels, width, j + sx, x +c + sy);
				drawDot(pixels, width, j + sx, size - x - c + sy);
			}
		}

		// Initialising the value of P
		int P = x * x - x + 1 - r * r;

		while (x > y) {

			 //System.out.println("x: " + x + ", y: " + y + ", P: "+P + ", r: "+r);

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
				drawDot(pixels, width, x + c + sx, j + sy);
				drawDot(pixels, width, size - x - c + sx, j + sy);
				drawDot(pixels, width, j + sx, x +c + sy);
				drawDot(pixels, width, j + sx, size - x - c + sy);
				
			}

		}
		
		for (int i = size - x - c + sx; i <= x + c + sx; i++) {
			for (int j = size - y - c + sy; j <= y + c + sy; j++) {
				drawDot(pixels, width, i, j);
			}
		}
		
	}

	
	public static void fillEllipse(int[] pixels, int width, int height, int sx, int sy, int w, int h) {

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
				drawDot(pixels, width, x + cx + sx, j);
				drawDot(pixels, width, w - x - cx + sx, j);
			}
		}

		// Initialising the value of P
		int P = x * x * b2 - x * b2 + b2 / 4 + a2 - a2 * b2;

		while (x * b >= y * a) {

			// System.out.println("x: " + x + ", y: " + y);

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
				drawDot(pixels, width, x + cx + sx, j);
				drawDot(pixels, width, w - x - cx + sx, j);
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
				drawDot(pixels, width, i, y + cy + sy);
				drawDot(pixels, width, i, h - y - cy + sy);
			}
		}

		P = y * y * a2 - y * a2 + a2 / 4 + b2 - a2 * b2;
		while (x * b <= y * a) {

			// System.out.println("x: " + x + ", y: " + y);

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
				drawDot(pixels, width, i, y + cy + sy);
				drawDot(pixels, width, i, h - y - cy + sy);
			}

		}

		innerX = x < innerX ? x : innerX;
		innerY = y < innerY ? y : innerY;

		for (int i = w - innerX - cx + sx; i <= innerX + cx + sx; i++) {
			for (int j = h - innerY - cy + sy; j <= innerY + cy + sy; j++) {
				drawDot(pixels, width, i, j);
			}
		}

		drawRect(pixels, width, height, sx - 1, sy - 1, w + 2, h + 2);
	}

	public static void drawRect(int[] pixels, int width, int height, int x, int y, int w, int h) {

		for (int i = x; i <= x + w; i++) {
			drawDot(pixels, width, i, y, Color.YELLOW.getRGB());
			drawDot(pixels, width, i, y + h, Color.YELLOW.getRGB());
		}

		for (int j = y; j <= y + h; j++) {
			drawDot(pixels, width, x, j, Color.YELLOW.getRGB());
			drawDot(pixels, width, x + w, j, Color.YELLOW.getRGB());
		}

	}

	public static void fillRect(int[] pixels, int width, int height, int x, int y, int w, int h) {

		for (int i = x; i <= x + w; i++) {
			for (int j = y; j <= y + h; j++) {
				drawDot(pixels, width, i, j, Color.YELLOW.getRGB());
			}
		}

	}

	public static void drawOval4(int[] pixels, int width, int height, int x, int y, int w, int h) {

		int innerW = (w) / 2, innerH = (h) / 2;
		int outerW = (w + 1) / 2, outerH = (h + 1) / 2;
		int xCenter = w / 2, yCenter = h / 2;

		for (int i = 0; i < w; i++) {
			if (i + x >= width || i + x < 0)
				continue;
			for (int j = 0; j < h; j++) {
				if (j + y >= height || j + y < 0)
					continue;

				if ((((i - xCenter) * (i - xCenter) * outerH * outerH
						+ (j - yCenter) * (j - yCenter) * outerW * outerW) < outerW * outerW * outerH * outerH)
						&& (((i - xCenter) * (i - xCenter) * innerH * innerH
								+ (j - yCenter) * (j - yCenter) * innerW * innerW) >= innerW * innerW * innerH
										* innerH)) {
					pixels[(x + i) * width + y + j] = Color.WHITE.getRGB();
				}
			}
		}

		drawRect(pixels, width, height, x, y, w, h);

	}

	public static void test1() {

		Random rnd = new Random();

		Image retimage;
		Graphics g;
		Color tempcolor;
		long time;

		time = System.currentTimeMillis();

		retimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		g = retimage.getGraphics();
		tempcolor = g.getColor();
		g.setColor(background);
		g.fillRect(0, 0, width, height);
		g.setColor(tempcolor);

		for (int i = 0; i < N; i++) {
			int x = rnd.nextInt(width);
			int y = rnd.nextInt(height);
			g.setColor(Color.WHITE);
			g.drawOval(x - sizeA / 2, y - sizeA / 2, sizeA, sizeB);

			x = rnd.nextInt(width);
			y = rnd.nextInt(height);
			g.drawOval(x - sizeA / 2, y - sizeA / 2, sizeB, sizeA);

			x = rnd.nextInt(width);
			y = rnd.nextInt(height);
			g.drawOval(x - sizeA / 2, y - sizeA / 2, sizeA, sizeA);

			x = rnd.nextInt(width);
			y = rnd.nextInt(height);
			g.drawOval(x - sizeA / 2, y - sizeA / 2, sizeA + 21, sizeA + 21);

			g.setColor(Color.YELLOW);
			g.drawRect(x - sizeA / 2 - 1, y - sizeA / 2 - 1, sizeA + 2, sizeB + 2);
		}
		System.out.println("draw on TYPE_INT_RGB : " + (System.currentTimeMillis() - time));
		time = System.currentTimeMillis();

		new ij.ImagePlus("test", retimage).show();

	}

	public static void test3() {

		Random rnd = new Random();

		Image retimage;
		Graphics g;
		Color tempcolor;
		long time;

		time = System.currentTimeMillis();

		retimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		g = retimage.getGraphics();
		tempcolor = g.getColor();
		g.setColor(background);
		g.fillRect(0, 0, width, height);
		g.setColor(tempcolor);

		for (int i = 0; i < N; i++) {
			g.setColor(Color.WHITE);
			int x = width / 2;// rnd.nextInt(width);
			int y = height / 2;// rnd.nextInt(height);
			// int xlen = rnd.nextInt(width / 10);
			// int ylen = rnd.nextInt(height / 10);
			g.drawLine(x, y, x + xlen, y + ylen);
			g.drawLine(x, y, x + xlen, y);
			g.drawLine(x, y, x + xlen, y - ylen);
			g.drawLine(x, y, x, y - ylen);
			g.drawLine(x, y, x - xlen, y - ylen);
			g.drawLine(x, y, x - xlen, y);
			g.drawLine(x, y, x - xlen, y + ylen);
			g.drawLine(x, y, x, y + ylen);
			g.setColor(Color.YELLOW);
			g.drawRect(x - xlen - 1, y - ylen - 1, xlen * 2 + 2, ylen * 2 + 2);
		}
		System.out.println("draw on TYPE_INT_RGB : " + (System.currentTimeMillis() - time));
		time = System.currentTimeMillis();

		new ij.ImagePlus("test3", retimage).show();

	}
}
