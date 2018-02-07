/*******************************************************************************
 * Copyright (c) 2013 Jay Unruh, Stowers Institute for Medical Research.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package ezcol.visual.visual3D;

import java.awt.*;

public class Text3D extends Element3D{
	public Point3D point;
	public String text; // shapes are square,plus,x,triangle
	public static final int fontsize=10;

	public Text3D(String text1,int x,int y,int z,Color color1){
		point=new Point3D(x,y,z);
		text=text1;
		color=color1;
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
	}

	public void transform_negative_perspective(double horizon_dist,int centerx,int centery,int centerz){
		point.transform_negative_perspective(horizon_dist,centerx,centery,centerz);
	}

	public int getzpos(){
		return point.rz;
	}

	public void drawelement(Graphics g){
		Color tempcolor=g.getColor();
		g.setColor(color);
		Font temp=g.getFont().deriveFont((float)fontsize);
		g.setFont(temp);
		g.drawString(text,point.rx,point.ry);
		g.setColor(tempcolor);
	}
	
	public void drawElement(GraphicsB3D g){
		Color tempcolor=g.getColor();
		g.setColor(color);
		Font temp=g.getFont().deriveFont((float)fontsize);
		g.setFont(temp);
		g.drawString(text,point.rx,point.ry);
		g.setColor(tempcolor);
	}

}
