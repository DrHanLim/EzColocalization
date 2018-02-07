/*******************************************************************************
 * Copyright (c) 2013 Jay Unruh, Stowers Institute for Medical Research.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package ezcol.visual.visual3D;

import java.awt.*;

public class Polygon3D extends Element3D{
	public Point3D[] pt;
	public boolean closed;

	// here we have a 3D line
	public Polygon3D(int[] xpts,int[] ypts,int[] zpts,Color color1){
		pt=new Point3D[xpts.length];
		color=color1;
		for(int i=0;i<xpts.length;i++){
			pt[i]=new Point3D(xpts[i],ypts[i],zpts[i]);
		}
		color=color1;
		closed=true;
	}

	public Polygon3D(Polygon poly,Color color1){
		pt=new Point3D[poly.xpoints.length];
		color=color1;
		for(int i=0;i<poly.xpoints.length;i++){
			pt[i]=new Point3D(poly.xpoints[i],poly.ypoints[i],0);
		}
		color=color1;
		closed=true;
	}

	public Polygon getPolygon(){
		int[] xpoints=new int[pt.length];
		int[] ypoints=new int[pt.length];
		for(int i=0;i<pt.length;i++){
			xpoints[i]=pt[i].rx;
			ypoints[i]=pt[i].ry;
		}
		return new Polygon(xpoints,ypoints,pt.length);
	}

	public void moveto(int x,int y,int z){
		translate(x-pt[0].rx,y-pt[0].ry,z-pt[0].rz);
	}

	public void translate(int transx,int transy,int transz){
		for(int i=0;i<pt.length;i++){
			pt[i].translate(transx,transy,transz);
		}
	}

	public void rotatedeg(double degx1,double degy1,double degz1,int centerx,int centery,int centerz){
		for(int i=0;i<pt.length;i++){
			pt[i].rotatedeg(degx1,degy1,degz1,centerx,centery,centerz);
		}
	}

	public void rotaterad(double radx1,double rady1,double radz1,int centerx,int centery,int centerz){
		for(int i=0;i<pt.length;i++){
			pt[i].rotaterad(radx1,rady1,radz1,centerx,centery,centerz);
		}
	}

	public void rotatecossin(double cosx1,double cosy1,double cosz1,double sinx1,double siny1,double sinz1,int centerx,int centery,int centerz){
		for(int i=0;i<pt.length;i++){
			pt[i].rotatecossin(cosx1,cosy1,cosz1,sinx1,siny1,sinz1,centerx,centery,centerz);
		}
	}

	public void addrotatedeg(double degx1,double degy1,double degz1,int centerx,int centery,int centerz){
		for(int i=0;i<pt.length;i++){
			pt[i].addrotatedeg(degx1,degy1,degz1,centerx,centery,centerz);
		}
	}

	public void addrotaterad(double radx1,double rady1,double radz1,int centerx,int centery,int centerz){
		for(int i=0;i<pt.length;i++){
			pt[i].addrotaterad(radx1,rady1,radz1,centerx,centery,centerz);
		}
	}

	public void transform_perspective(double horizon_dist,int centerx,int centery,int centerz){
		for(int i=0;i<pt.length;i++){
			pt[i].transform_perspective(horizon_dist,centerx,centery,centerz);
		}
	}

	public void transform_negative_perspective(double horizon_dist,int centerx,int centery,int centerz){
		for(int i=0;i<pt.length;i++){
			pt[i].transform_negative_perspective(horizon_dist,centerx,centery,centerz);
		}
	}

	public int getzpos(){
		float sum=0.0f;
		for(int i=0;i<pt.length;i++){
			sum+=pt[i].rz;
		}
		int zpos=(int)(sum/pt.length);
		return zpos;
	}

	public void drawelement(Graphics g){
		Color tempcolor=g.getColor();
		g.setColor(color);
		for(int i=1;i<pt.length;i++){
			drawLine(g,pt[i-1].rx,pt[i-1].ry,pt[i].rx,pt[i].ry,thick);
		}
		if(closed){
			drawLine(g,pt[pt.length-1].rx,pt[pt.length-1].ry,pt[0].rx,pt[0].ry,thick);
		}
		g.setColor(tempcolor);
	}
	
	public void drawElement(GraphicsB3D g){
		Color tempcolor=g.getColor();
		g.setColor(color);
		for(int i=1;i<pt.length;i++){
			g.drawLine(pt[i-1].rx,pt[i-1].ry,pt[i].rx,pt[i].ry,thick);
		}
		if(closed){
			g.drawLine(pt[pt.length-1].rx,pt[pt.length-1].ry,pt[0].rx,pt[0].ry,thick);
		}
		g.setColor(tempcolor);
	}

}
