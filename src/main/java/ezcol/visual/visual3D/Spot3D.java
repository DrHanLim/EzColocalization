/*******************************************************************************
 * Copyright (c) 2013 Jay Unruh, Stowers Institute for Medical Research.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package ezcol.visual.visual3D;

import java.awt.*;

public class Spot3D extends Element3D{
	public Point3D point;
	public int shape; // shapes are square,plus,x,triangle
	public int shapesize=8;
	private int transshapesize;
	
	//EDIT:
	public static final int SQUARE = 0, PLUS = 1, CROSS = 2, TRIANGLE = 3, CIRCLE = 4, FILLED_SQUARE = 5, FILLED_CIRCLE = 6, ALL_SHAPES = 7;

	public Spot3D(int x,int y,int z,int shape1,Color color1){
		point=new Point3D(x,y,z);
		shape=shape1;
		color=color1;
		transshapesize=shapesize;
	}
	
	public Spot3D(int x,int y,int z,int shape1,Color color1, int shapesize){
		point=new Point3D(x,y,z);
		shape=shape1;
		color=color1;
		if(shapesize>this.shapesize)
			this.shapesize = shapesize;
		transshapesize = this.shapesize;
	}

	public void moveto(int ptx,int pty,int ptz){
		point.moveto(ptx,pty,ptz);
	}

	public void translate(int transx,int transy,int transz){
		point.translate(transx,transy,transz);
	}

	public void rotatedeg(double degx1,double degy1,double degz1,int centerx,int centery,int centerz){
		point.rotatedeg(degx1,degy1,degz1,centerx,centery,centerz);
	}

	public void rotaterad(double radx1,double rady1,double radz1,int centerx,int centery,int centerz){
		point.rotaterad(radx1,rady1,radz1,centerx,centery,centerz);
	}

	public void rotatecossin(double cosx1,double cosy1,double cosz1,double sinx1,double siny1,double sinz1,int centerx,int centery,int centerz){
		point.rotatecossin(cosx1,cosy1,cosz1,sinx1,siny1,sinz1,centerx,centery,centerz);
	}

	public void addrotatedeg(double degx1,double degy1,double degz1,int centerx,int centery,int centerz){
		point.addrotatedeg(degx1,degy1,degz1,centerx,centery,centerz);
	}

	public void addrotaterad(double radx1,double rady1,double radz1,int centerx,int centery,int centerz){
		point.addrotaterad(radx1,rady1,radz1,centerx,centery,centerz);
	}

	public void transform_perspective(double horizon_dist,int centerx,int centery,int centerz){
		point.transform_perspective(horizon_dist,centerx,centery,centerz);
		if(horizon_dist<=0.0){
			transshapesize=shapesize;
		}else{
			double tempz=(double)(point.z-centerz);
			double temphordist=(tempz+horizon_dist)/horizon_dist;
			if(temphordist<=0){
				transshapesize=0;
			}else{
				transshapesize=(int)(shapesize*temphordist);
			}
		}
	}

	public void transform_negative_perspective(double horizon_dist,int centerx,int centery,int centerz){
		point.transform_negative_perspective(horizon_dist,centerx,centery,centerz);
		if(horizon_dist<=0.0){
			transshapesize=shapesize;
		}else{
			double tempz=(double)(centerz-point.z);
			double temphordist=(horizon_dist-tempz)/horizon_dist;
			if(temphordist<=0){
				transshapesize=0;
			}else{
				transshapesize=(int)(shapesize*temphordist);
			}
		}
	}

	public int getzpos(){
		return point.rz;
	}
	
	public void drawelement(Graphics g){
		Color tempcolor=g.getColor();
		g.setColor(color);
		switch(shape){
			case SQUARE:
				drawSquare(g,point.rx,point.ry,transshapesize);
				break;
			case PLUS:
				drawPlus(g,point.rx,point.ry,transshapesize);
				break;
			case CROSS:
				drawX(g,point.rx,point.ry,transshapesize);
				break;
			case TRIANGLE:
				drawTriangle(g,point.rx,point.ry,transshapesize);
				break;
			case CIRCLE:
				drawCircle(g,point.rx,point.ry,transshapesize);
				break;
			case FILLED_SQUARE:
				drawFilledSquare(g,point.rx,point.ry,transshapesize);
				break;
			case FILLED_CIRCLE:
				drawFilledCircle(g,point.rx,point.ry,transshapesize);
		}
		g.setColor(tempcolor);
	}

	void drawSquare(Graphics g,int x,int y,int size){
		g.drawLine(x-size/2,y-size/2,x+size/2,y-size/2);
		g.drawLine(x+size/2,y-size/2,x+size/2,y+size/2);
		g.drawLine(x+size/2,y+size/2,x-size/2,y+size/2);
		g.drawLine(x-size/2,y+size/2,x-size/2,y-size/2);
	}

	void drawCircle(Graphics g,int x,int y,int size){
		g.drawOval(x-size/2,y-size/2,size,size);
	}

	void drawFilledCircle(Graphics g,int x,int y,int size){
		g.fillOval(x-size/2,y-size/2,size,size);
	}

	void drawFilledSquare(Graphics g,int x,int y,int size){
		g.fillRect(x-size/2,y-size/2,size,size);
	}

	void drawPlus(Graphics g,int x,int y,int size){
		g.drawLine(x-size/2,y,x+size/2,y);
		g.drawLine(x,y-size/2,x,y+size/2);
	}

	void drawX(Graphics g,int x,int y,int size){
		g.drawLine(x-size/2,y-size/2,x+size/2,y+size/2);
		g.drawLine(x-size/2,y+size/2,x+size/2,y-size/2);
	}

	void drawTriangle(Graphics g,int x,int y,int size){
		g.drawLine(x,y-size/2,x-size/2,y+size/2);
		g.drawLine(x-size/2,y+size/2,x+size/2,y+size/2);
		g.drawLine(x+size/2,y+size/2,x,y-size/2);
	}
	
	public void drawElement(GraphicsB3D g){
		Color tempcolor = g.getColor();
		g.setColor(color);
		switch(shape){
			case SQUARE:
				g.drawSquare(point.rx,point.ry,transshapesize);
				break;
			case PLUS:
				g.drawPlus(point.rx,point.ry,transshapesize);
				break;
			case CROSS:
				g.drawX(point.rx,point.ry,transshapesize);
				break;
			case TRIANGLE:
				g.drawTriangle(point.rx,point.ry,transshapesize);
				break;
			case CIRCLE:
				g.drawCircle(point.rx,point.ry,transshapesize);
				break;
			case FILLED_SQUARE:
				g.drawFilledSquare(point.rx,point.ry,transshapesize);
				break;
			case FILLED_CIRCLE:
				g.drawFilledCircle(point.rx,point.ry,transshapesize);
		}
		g.setColor(tempcolor);
	}
	
}
