/*******************************************************************************
 * Copyright (c) 2013 Jay Unruh, Stowers Institute for Medical Research.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package ezcol.visual.visual3D;

import java.awt.*;

public class Cube3D extends Element3D{
	public Point3D[] pt;
	public final int size;

	// here we have a 3D line
	public Cube3D(int x,int y,int z,int size1,Color color1){
		pt=new Point3D[8];
		size=size1;
		int halfsize=size/2;
		// start with the upper square
		pt[0]=new Point3D(x-halfsize,y-halfsize,z-halfsize);
		pt[1]=new Point3D(x+halfsize,y-halfsize,z-halfsize);
		pt[2]=new Point3D(x+halfsize,y+halfsize,z-halfsize);
		pt[3]=new Point3D(x-halfsize,y+halfsize,z-halfsize);
		pt[4]=new Point3D(x-halfsize,y-halfsize,z+halfsize);
		pt[5]=new Point3D(x+halfsize,y-halfsize,z+halfsize);
		pt[6]=new Point3D(x+halfsize,y+halfsize,z+halfsize);
		pt[7]=new Point3D(x-halfsize,y+halfsize,z+halfsize);
		color=color1;
	}

	public void moveto(int x,int y,int z){
		int halfsize=size/2;
		pt[0].moveto(x-halfsize,y-halfsize,z-halfsize);
		pt[1].moveto(x+halfsize,y-halfsize,z-halfsize);
		pt[2].moveto(x+halfsize,y+halfsize,z-halfsize);
		pt[3].moveto(x-halfsize,y+halfsize,z-halfsize);
		pt[4].moveto(x-halfsize,y-halfsize,z+halfsize);
		pt[5].moveto(x+halfsize,y-halfsize,z+halfsize);
		pt[6].moveto(x+halfsize,y+halfsize,z+halfsize);
		pt[7].moveto(x-halfsize,y+halfsize,z+halfsize);
	}

	public void translate(int transx,int transy,int transz){
		for(int i=0;i<8;i++){
			pt[i].translate(transx,transy,transz);
		}
	}

	public void rotatedeg(double degx1,double degy1,double degz1,int centerx,int centery,int centerz){
		for(int i=0;i<8;i++){
			pt[i].rotatedeg(degx1,degy1,degz1,centerx,centery,centerz);
		}
	}

	public void rotaterad(double radx1,double rady1,double radz1,int centerx,int centery,int centerz){
		for(int i=0;i<8;i++){
			pt[i].rotaterad(radx1,rady1,radz1,centerx,centery,centerz);
		}
	}

	public void rotatecossin(double cosx1,double cosy1,double cosz1,double sinx1,double siny1,double sinz1,int centerx,int centery,int centerz){
		for(int i=0;i<8;i++){
			pt[i].rotatecossin(cosx1,cosy1,cosz1,sinx1,siny1,sinz1,centerx,centery,centerz);
		}
	}

	public void addrotatedeg(double degx1,double degy1,double degz1,int centerx,int centery,int centerz){
		for(int i=0;i<8;i++){
			pt[i].addrotatedeg(degx1,degy1,degz1,centerx,centery,centerz);
		}
	}

	public void addrotaterad(double radx1,double rady1,double radz1,int centerx,int centery,int centerz){
		for(int i=0;i<8;i++){
			pt[i].addrotaterad(radx1,rady1,radz1,centerx,centery,centerz);
		}
	}

	public void transform_perspective(double horizon_dist,int centerx,int centery,int centerz){
		for(int i=0;i<8;i++){
			pt[i].transform_perspective(horizon_dist,centerx,centery,centerz);
		}
	}

	public void transform_negative_perspective(double horizon_dist,int centerx,int centery,int centerz){
		for(int i=0;i<8;i++){
			pt[i].transform_negative_perspective(horizon_dist,centerx,centery,centerz);
		}
	}

	public int getzpos(){
		float sum=0.0f;
		for(int i=0;i<8;i++){
			sum+=pt[i].rz;
		}
		int zpos=(int)(0.125f*sum);
		return zpos;
	}

	public void drawelement(Graphics g){
		Color tempcolor=g.getColor();
		g.setColor(color);
		// draw the top square
		drawLine(g,pt[0].rx,pt[0].ry,pt[1].rx,pt[1].ry,thick);
		drawLine(g,pt[1].rx,pt[1].ry,pt[2].rx,pt[2].ry,thick);
		drawLine(g,pt[2].rx,pt[2].ry,pt[3].rx,pt[3].ry,thick);
		drawLine(g,pt[3].rx,pt[3].ry,pt[0].rx,pt[0].ry,thick);
		// draw the bottom square
		drawLine(g,pt[4].rx,pt[4].ry,pt[5].rx,pt[5].ry,thick);
		drawLine(g,pt[5].rx,pt[5].ry,pt[6].rx,pt[6].ry,thick);
		drawLine(g,pt[6].rx,pt[6].ry,pt[7].rx,pt[7].ry,thick);
		drawLine(g,pt[7].rx,pt[7].ry,pt[4].rx,pt[4].ry,thick);
		// draw the vertical lines
		drawLine(g,pt[0].rx,pt[0].ry,pt[4].rx,pt[4].ry,thick);
		drawLine(g,pt[1].rx,pt[1].ry,pt[5].rx,pt[5].ry,thick);
		drawLine(g,pt[2].rx,pt[2].ry,pt[6].rx,pt[6].ry,thick);
		drawLine(g,pt[3].rx,pt[3].ry,pt[7].rx,pt[7].ry,thick);
		g.setColor(tempcolor);
	}
	
	public void drawElement(GraphicsB3D g){
		Color tempcolor=g.getColor();
		g.setColor(color);
		// draw the top square
		g.drawLine(pt[0].rx,pt[0].ry,pt[1].rx,pt[1].ry,thick);
		g.drawLine(pt[1].rx,pt[1].ry,pt[2].rx,pt[2].ry,thick);
		g.drawLine(pt[2].rx,pt[2].ry,pt[3].rx,pt[3].ry,thick);
		g.drawLine(pt[3].rx,pt[3].ry,pt[0].rx,pt[0].ry,thick);
		// draw the bottom square
		g.drawLine(pt[4].rx,pt[4].ry,pt[5].rx,pt[5].ry,thick);
		g.drawLine(pt[5].rx,pt[5].ry,pt[6].rx,pt[6].ry,thick);
		g.drawLine(pt[6].rx,pt[6].ry,pt[7].rx,pt[7].ry,thick);
		g.drawLine(pt[7].rx,pt[7].ry,pt[4].rx,pt[4].ry,thick);
		// draw the vertical lines
		g.drawLine(pt[0].rx,pt[0].ry,pt[4].rx,pt[4].ry,thick);
		g.drawLine(pt[1].rx,pt[1].ry,pt[5].rx,pt[5].ry,thick);
		g.drawLine(pt[2].rx,pt[2].ry,pt[6].rx,pt[6].ry,thick);
		g.drawLine(pt[3].rx,pt[3].ry,pt[7].rx,pt[7].ry,thick);
		g.setColor(tempcolor);
	}

}
