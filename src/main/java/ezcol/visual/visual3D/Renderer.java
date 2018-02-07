/*******************************************************************************
 * Copyright (c) 2013 Jay Unruh, Stowers Institute for Medical Research.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package ezcol.visual.visual3D;

import java.awt.*;
import java.awt.image.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
//import java.io.ByteArrayOutputStream;
//import org.freehep.graphicsio.emf.EMFGraphics2D;
import java.util.Vector;

public class Renderer {
	// this class returns an awt image based on transformations of 3D spots and
	// lines
	public Set<Element3D> elementSet = new HashSet<Element3D>();
	public Vector<Element3D> elements = new Vector<Element3D>();
	public int width, height, centerx, centery, centerz;
	public double degx, degy, degz, horizon_dist;
	public Color background;

	public Renderer(int width1, int height1) {
		width = width1;
		centerx = width / 2;
		height = height1;
		centery = height / 2;
		centerz = height / 2;
		degx = 0.0;
		degy = 0.0;
		degz = 0.0;
		background = Color.GRAY;
	}
	
	public void flush(){
		elementSet.clear();
		elements.clear();
	}

	public void addelement(Element3D element) {
		// element.translate(width/2,width/2,height/2);
		if (!elementSet.contains(element)) {
			elements.add(element);
			elementSet.add(element);
		}
	}

	public void addText3D(String s, int x, int y, int z, Color color) {
		addelement(new Text3D(s, x, y, z, color));
	}

	public void addLine3D(int x1, int y1, int z1, int x2, int y2, int z2, Color color) {
		addelement(new Line3D(x1, y1, z1, x2, y2, z2, color));
	}

	public void addPoint3D(int x, int y, int z, int shape, Color color) {
		addelement(new Spot3D(x, y, z, shape % Spot3D.ALL_SHAPES, color, shape / Spot3D.ALL_SHAPES));
	}

	public void addCube3D(int x, int y, int z, int size, Color color) {
		addelement(new Cube3D(x, y, z, size, color));
	}

	// public void setelementarray(element3D[] elements) {
	// this.elements.addAll(new ArrayList<element3D>(Arrays.asList(elements)));
	/*
	 * for(int i=0;i<elements.size();i++){
	 * elements.get(i).translate(width/2,width/2,height/2); }
	 */
	// }

	public void addpointarray(int[] x, int[] y, int[] z, int shape, Color color) {
		for (int i = 0; i < x.length; i++)
			elements.add(new Spot3D(x[i], y[i], z[i], shape, color));
	}

	public void addpolyline(int[] x, int[] y, int[] z, Color color) {
		for (int i = 0; i < x.length - 1; i++)
			elements.add(new Line3D(x[i], y[i], z[i], x[i + 1], y[i + 1], z[i + 1], color));
	}

	public void setrotation(int degx1, int degy1, int degz1) {
		degx = (double) degx1;
		degy = (double) degy1;
		degz = (double) degz1;
		double cosx = Math.cos(Math.toRadians(degx));
		double cosy = Math.cos(Math.toRadians(degy));
		double cosz = Math.cos(Math.toRadians(-degz));
		double sinx = Math.sin(Math.toRadians(degx));
		double siny = Math.sin(Math.toRadians(degy));
		double sinz = Math.sin(Math.toRadians(-degz));
		for (int i = 0; i < elements.size(); i++) {
			elements.get(i).rotatecossin(cosx, cosy, cosz, sinx, siny, sinz, centerx, centery, centerz);
		}
	}

	public void setrotation(float degx1, float degy1, float degz1) {
		degx = (double) degx1;
		degy = (double) degy1;
		degz = (double) degz1;
		double cosx = Math.cos(Math.toRadians(degx));
		double cosy = Math.cos(Math.toRadians(degy));
		double cosz = Math.cos(Math.toRadians(-degz));
		double sinx = Math.sin(Math.toRadians(degx));
		double siny = Math.sin(Math.toRadians(degy));
		double sinz = Math.sin(Math.toRadians(-degz));
		for (int i = 0; i < elements.size(); i++) {
			elements.get(i).rotatecossin(cosx, cosy, cosz, sinx, siny, sinz, centerx, centery, centerz);
		}
	}

	public void set_perspective(double horizon_dist1) {
		horizon_dist = horizon_dist1;
		for (int i = 0; i < elements.size(); i++) {
			elements.get(i).transform_perspective(horizon_dist, centerx, centery, centerz);
		}
	}

	public void set_negative_perspective(double horizon_dist1) {
		horizon_dist = horizon_dist1;
		for (int i = 0; i < elements.size(); i++) {
			elements.get(i).transform_negative_perspective(horizon_dist, centerx, centery, centerz);
		}
	}

	public void rotate(int dx, int dy, int dz) {
		degx += (double) dx;
		degy += (double) dy;
		degz += (double) dz;
		for (int i = 0; i < elements.size(); i++) {
			elements.get(i).rotatedeg(degx, degy, degz, centerx, centery, centerz);
		}
	}

	public void addrotation(int dx, int dy, int dz) {
		for (int i = 0; i < elements.size(); i++) {
			elements.get(i).addrotatedeg((double) dx, (double) dy, (double) dz, centerx, centery, centerz);
		}
	}

	private void setyorder() {
		// here we sort the element list in order of decreasing z value
		// that way closer z values will be drawn last, on top of further z
		// values
		float[] zvals = new float[elements.size()];
		for (int i = 0; i < elements.size(); i++) {
			zvals[i] = (float) elements.get(i).getzpos();
		}

		Collections.sort(elements, new Comparator<Element3D>() {

			@Override
			public int compare(Element3D o1, Element3D o2) {
				// TODO Auto-generated method stub
				if (o1.getzpos() < o2.getzpos())
					return 1;
				else if (o1.getzpos() > o2.getzpos())
					return -1;
				else
					return 0;
			}

		});
	}

	@SuppressWarnings("deprecation")
	public Image renderimage() {

		/* @deprecated
		 * too slow to use Graphics to draw
		 * Image retimage=new
		 * BufferedImage(width,height,BufferedImage.TYPE_4BYTE_ABGR); Graphics
		 * g=retimage.getGraphics(); Color tempcolor=g.getColor();
		 * g.setColor(background); g.fillRect(0,0,width,height);
		 * g.setColor(tempcolor);
		 */

		GraphicsB3D g3D = new GraphicsB3D(width, height);
		Color tempColor = g3D.getColor();
		g3D.setColor(background);
		g3D.fillRect(0, 0, width, height);
		g3D.setColor(tempColor);

		setyorder();

		for (int i = 0; i < elements.size(); i++) {
			elements.get(i).drawElement(g3D);
		}

		Image retimage = g3D.getImage();

		return retimage;
	}

	/*
	 * public byte[] renderEMF(){ try{ ByteArrayOutputStream os=new
	 * ByteArrayOutputStream(); EMFGraphics2D g=new EMFGraphics2D(os,new
	 * Dimension(width,height)); g=new EMFGraphics2D(os,new
	 * Dimension(width,height)); g.setDeviceIndependent(true); g.startExport();
	 * Color tempcolor=g.getColor(); g.setColor(background);
	 * g.fillRect(0,0,width,height); g.setColor(tempcolor); setyorder(); for(int
	 * i=0;i<elements.size();i++){ elements.get(i).drawelement(g); }
	 * g.endExport(); return os.toByteArray(); }catch(Throwable e){ return null;
	 * } }
	 */

	public float[] renderfloat(float value) {
		Image retimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics g = retimage.getGraphics();
		Color tempcolor = g.getColor();
		g.setColor(background);
		g.fillRect(0, 0, width, height);
		g.setColor(tempcolor);
		setyorder();
		for (int i = 0; i < elements.size(); i++) {
			elements.get(i).drawelement(g);
		}
		int[] pixels = ((BufferedImage) retimage).getRGB(0, 0, width, height, null, 0, width);
		float[] output = new float[width * height];
		for (int i = 0; i < width * height; i++) {
			if ((pixels[i] & 0xffffff) > 0)
				output[i] = value;
		}
		return output;
	}

	public Image renderanalglyph(Color leftcolor, Color rightcolor, double angle) {
		Color[] colors = new Color[elements.size()];
		for (int i = 0; i < elements.size(); i++) {
			colors[i] = copycolor(elements.get(i).color);
		}
		Color[] leftcolors = new Color[elements.size()];
		for (int i = 0; i < elements.size(); i++) {
			leftcolors[i] = scalecolor(leftcolor, colormagnitude(colors[i]));
		}
		Color[] rightcolors = new Color[elements.size()];
		for (int i = 0; i < elements.size(); i++) {
			rightcolors[i] = scalecolor(rightcolor, colormagnitude(colors[i]));
		}
		// set up the drawing
		Image retimage1 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Image retimage2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics g1 = retimage1.getGraphics();
		Color tempcolor = g1.getColor();
		g1.setColor(background);
		g1.fillRect(0, 0, width, height);
		g1.setColor(tempcolor);
		Graphics g2 = retimage2.getGraphics();
		tempcolor = g2.getColor();
		g2.setColor(background);
		g2.fillRect(0, 0, width, height);
		g2.setColor(tempcolor);

		// start by drawing the left color
		// for(int
		// i=0;i<elements.size();i++){elements.get(i).color=scalecolor(leftcolor,colormagnitude(colors[i]));}
		for (int i = 0; i < elements.size(); i++) {
			elements.get(i).color = leftcolor;
		}
		addrotation(0, -(int) (angle / 2.0), 0);
		setyorder();
		for (int i = 0; i < elements.size(); i++) {
			elements.get(i).drawelement(g1);
		}

		// now draw the right color
		// for(int
		// i=0;i<elements.size();i++){elements.get(i).color=scalecolor(rightcolor,colormagnitude(colors[i]));}
		for (int i = 0; i < elements.size(); i++) {
			elements.get(i).color = rightcolor;
		}
		setrotation((int) degx, (int) degy, (int) degz);
		set_perspective(horizon_dist);
		addrotation(0, (int) (angle / 2.0), 0);
		setyorder();
		for (int i = 0; i < elements.size(); i++) {
			elements.get(i).drawelement(g2);
		}
		// now reset everything
		for (int i = 0; i < elements.size(); i++) {
			elements.get(i).color = colors[i];
		}
		setrotation((int) degx, (int) degy, (int) degz);
		set_perspective(horizon_dist);
		return combinecoloranalglyphs(retimage1, retimage2);
	}

	public Image renderinterlacedanalglyph(double angle) {
		return renderinterlacedanalglyph(angle, 0);
	}

	public Image renderinterlacedanalglyph(double angle, int interlacetype) {
		// note that interlace types are 0:diagonal,1:vertical,and 2:horizontal
		// set up the drawing
		Image retimage1 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Image retimage2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics g1 = retimage1.getGraphics();
		Color tempcolor = g1.getColor();
		g1.setColor(background);
		g1.fillRect(0, 0, width, height);
		g1.setColor(tempcolor);
		Graphics g2 = retimage2.getGraphics();
		tempcolor = g2.getColor();
		g2.setColor(background);
		g2.fillRect(0, 0, width, height);
		g2.setColor(tempcolor);

		// start by drawing the left image
		double tempdegy = degy;
		addrotation(0, -(int) (angle / 2.0), 0);
		setyorder();
		for (int i = 0; i < elements.size(); i++) {
			elements.get(i).drawelement(g1);
		}

		// now draw the right image
		setrotation((int) degx, (int) degy, (int) degz);
		set_perspective(horizon_dist);
		addrotation(0, (int) (angle / 2.0), 0);
		setyorder();
		for (int i = 0; i < elements.size(); i++) {
			elements.get(i).drawelement(g2);
		}
		// now reset everything
		setrotation((int) degx, (int) tempdegy, (int) degz);
		set_perspective(horizon_dist);
		return interlaceanalglyphs(retimage1, retimage2, interlacetype);
	}

	public Image combinecoloranalglyphs(Image leftimage, Image rightimage) {
		// here we add two images typically orthogonal color spaces should be
		// used
		int width = leftimage.getWidth(null);
		int height = leftimage.getHeight(null);
		int[] pix1 = getimagepixels(leftimage);
		int[] pix2 = getimagepixels(rightimage);
		int[] retpix = new int[width * height];
		for (int i = 0; i < width; i++) {
			// run the left image first
			for (int j = 0; j < height; j++) {
				retpix[i + j * width] = maxcolor(pix1[i + j * width], pix2[i + j * width]);
			}
		}
		return getimagefrompixels(retpix, width, height);
	}

	public Image interlaceanalglyphs(Image leftimage, Image rightimage, int interlace) {
		int width = leftimage.getWidth(null);
		int height = leftimage.getHeight(null);
		int[] pix1 = getimagepixels(leftimage);
		int[] pix2 = getimagepixels(rightimage);
		int[] retpix = new int[width * height];
		// eliminate every other line and every other row
		if (interlace < 2) {
			boolean even = true;
			boolean vinterlace = (interlace == 1);
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j += 2) {
					int pixval1 = 0;
					int pixval2 = 0;
					if (vinterlace || even) {
						pixval1 = maxcolor(pix1[j + i * width], pix1[j + 1 + i * width]);
						pixval2 = maxcolor(pix2[j + i * width], pix2[j + 1 + i * width]);
					} else {
						pixval1 = maxcolor(pix2[j + i * width], pix2[j + 1 + i * width]);
						pixval2 = maxcolor(pix1[j + i * width], pix1[j + 1 + i * width]);
					}
					retpix[j + i * width] = pixval1;
					retpix[j + 1 + i * width] = pixval2;
				}
				even = !even;
			}
		} else {
			for (int i = 0; i < height; i += 2) {
				for (int j = 0; j < width; j++) {
					int pixval1 = 0;
					int pixval2 = 0;
					pixval1 = maxcolor(pix1[j + i * width], pix1[j + (i + 1) * width]);
					pixval2 = maxcolor(pix2[j + i * width], pix2[j + (i + 1) * width]);
					retpix[j + i * width] = pixval1;
					retpix[j + 1 + i * width] = pixval2;
				}
			}
		}
		return getimagefrompixels(retpix, width, height);
	}

	public int maxcolor(int pix1, int pix2) {
		boolean white1 = (pix1 == 0xffffffff);
		boolean white2 = (pix2 == 0xffffffff);
		int r1 = (pix1 & 0xff0000) >> 16;
		int g1 = (pix1 & 0xff00) >> 8;
		int b1 = pix1 & 0xff;
		if (!white2) {
			int r2 = (pix2 & 0xff0000) >> 16;
			int g2 = (pix2 & 0xff00) >> 8;
			int b2 = (pix2 & 0xff);
			if (!white1) {
				r1 += r2;
				g1 += g2;
				b1 += b2;
			} else {
				r1 = r2;
				g1 = g2;
				b1 = b2;
			}
		}
		return 0xff000000 + (r1 << 16) + (g1 << 8) + b1;
	}

	public int colormagnitude(Color colorval) {
		int r1 = colorval.getRed();
		int max = r1;
		int g1 = colorval.getGreen();
		if (g1 > max) {
			max = g1;
		}
		int b1 = colorval.getBlue();
		if (b1 > max) {
			max = b1;
		}
		// int mag=(int)Math.sqrt(r1*r1+g1*g1+b1*b1);
		// if(mag>255){mag=255;}
		// return (int)(0.33f*(r1+g1+b1));
		return max;
	}

	public Color scalecolor(Color colorval, int scale) {
		float factor = (float) scale / 255.0f;
		int r1 = (int) (factor * colorval.getRed());
		int g1 = (int) (factor * colorval.getGreen());
		int b1 = (int) (factor * colorval.getBlue());
		return new Color(r1, g1, b1);
	}

	public Color copycolor(Color colorval) {
		return new Color(colorval.getRed(), colorval.getGreen(), colorval.getBlue());
	}

	public Image getimagefrompixels(int[] pixels, int width, int height) {
		MemoryImageSource source = new MemoryImageSource(width, height, pixels, 0, width);
		return Toolkit.getDefaultToolkit().createImage(source);
	}

	public int[] getimagepixels(Image input) {
		int width = input.getWidth(null);
		int height = input.getHeight(null);
		int[] pixels = new int[width * height];
		PixelGrabber pg = new PixelGrabber(input, 0, 0, width, height, pixels, 0, width);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
		}
		;
		return pixels;
	}
	
	public void setBackground(Color color){
		background = color;
	}

}
