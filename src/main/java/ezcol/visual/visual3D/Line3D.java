/*******************************************************************************
 * Copyright (c) 2013 Jay Unruh, Stowers Institute for Medical Research.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package ezcol.visual.visual3D;

import java.awt.*;

public class Line3D extends Element3D{
	public Point3D pt1,pt2;

	// here we have a 3D line
	public Line3D(int x11,int y11,int z11,int x21,int y21,int z21,Color color1){
		pt1=new Point3D(x11,y11,z11);
		pt2=new Point3D(x21,y21,z21);
		color=color1;
	}

	public void moveto(int x,int y,int z){
		int centerx=(int)(0.5f*(pt1.x+pt2.x));
		int centery=(int)(0.5f*(pt1.y+pt2.y));
		int centerz=(int)(0.5f*(pt1.z+pt2.z));
		translate(x-centerx,y-centery,z-centerz);
		pt1.reset();
		pt2.reset();
	}

	public void translate(int transx,int transy,int transz){
		pt1.translate(transx,transy,transz);
		pt2.translate(transx,transy,transz);
	}

	public void rotatedeg(double degx1,double degy1,double degz1,int centerx,int centery,int centerz){
		pt1.rotatedeg(degx1,degy1,degz1,centerx,centery,centerz);
		pt2.rotatedeg(degx1,degy1,degz1,centerx,centery,centerz);
	}

	public void rotaterad(double radx1,double rady1,double radz1,int centerx,int centery,int centerz){
		pt1.rotaterad(radx1,rady1,radz1,centerx,centery,centerz);
		pt2.rotaterad(radx1,rady1,radz1,centerx,centery,centerz);
	}

	public void rotatecossin(double cosx1,double cosy1,double cosz1,double sinx1,double siny1,double sinz1,int centerx,int centery,int centerz){
		pt1.rotatecossin(cosx1,cosy1,cosz1,sinx1,siny1,sinz1,centerx,centery,centerz);
		pt2.rotatecossin(cosx1,cosy1,cosz1,sinx1,siny1,sinz1,centerx,centery,centerz);
	}

	public void addrotatedeg(double degx1,double degy1,double degz1,int centerx,int centery,int centerz){
		pt1.addrotatedeg(degx1,degy1,degz1,centerx,centery,centerz);
		pt2.addrotatedeg(degx1,degy1,degz1,centerx,centery,centerz);
	}

	public void addrotaterad(double radx1,double rady1,double radz1,int centerx,int centery,int centerz){
		pt1.addrotaterad(radx1,rady1,radz1,centerx,centery,centerz);
		pt2.addrotaterad(radx1,rady1,radz1,centerx,centery,centerz);
	}

	public void transform_perspective(double horizon_dist,int centerx,int centery,int centerz){
		pt1.transform_perspective(horizon_dist,centerx,centery,centerz);
		pt2.transform_perspective(horizon_dist,centerx,centery,centerz);
	}

	public void transform_negative_perspective(double horizon_dist,int centerx,int centery,int centerz){
		pt1.transform_negative_perspective(horizon_dist,centerx,centery,centerz);
		pt2.transform_negative_perspective(horizon_dist,centerx,centery,centerz);
	}

	public int getzpos(){
		int zpos=(int)(0.5f*((float)pt1.rz+(float)pt2.rz));
		return zpos;
	}

	public void drawelement(Graphics g){
		Color tempcolor=g.getColor();
		g.setColor(color);
		drawLine(g,pt1.rx,pt1.ry,pt2.rx,pt2.ry,thick);
		g.setColor(tempcolor);
	}
	
	public void drawElement(GraphicsB3D g){
		Color tempcolor=g.getColor();
		g.setColor(color);
		g.drawLine(pt1.rx,pt1.ry,pt2.rx,pt2.ry,thick);
		g.setColor(tempcolor);
	}

}
