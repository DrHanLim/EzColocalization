/*******************************************************************************
 * Copyright (c) 2013 Jay Unruh, Stowers Institute for Medical Research.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package ezcol.visual.visual3D;

import java.awt.*;

public abstract class Element3D{

	public Color color;

	public boolean thick;

	public abstract int getzpos();

	public abstract void translate(int transx,int transy,int transz);

	public abstract void moveto(int ptx,int pty,int ptz);

	public abstract void rotatedeg(double degx1,double degy1,double degz1,int centerx,int centery,int centerz);

	public abstract void rotaterad(double radx1,double rady1,double radz1,int centerx,int centery,int centerz);

	public abstract void rotatecossin(double cosx1,double cosy1,double cosz1,double sinx1,double siny1,double sinz1,int centerx,int centery,int centerz);

	public abstract void addrotatedeg(double degx1,double degy1,double degz1,int centerx,int centery,int centerz);

	public abstract void addrotaterad(double radx1,double rady1,double radz1,int centerx,int centery,int centerz);

	public abstract void transform_perspective(double horizon_dist,int centerx,int centery,int centerz);

	public abstract void transform_negative_perspective(double horizon_dist,int centerx,int centery,int centerz);

	@Deprecated
	public abstract void drawelement(Graphics g);
	
	public abstract void drawElement(GraphicsB3D g);

	public void drawLine(Graphics g,int x1,int y1,int x2,int y2,boolean thick){
		if(thick){
			drawThickLine(g,x1,y1,x2,y2);
		}else{
			g.drawLine(x1,y1,x2,y2);
		}
	}

	public void drawThickLine(Graphics g,int x1,int y1,int x2,int y2){
		float length=(float)Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
		float dx=(float)(x2-x1)/length;
		float dy=(float)(y2-y1)/length;
		float xpos=(float)x1;
		float ypos=(float)y1;
		for(int i=0;i<(int)length;i++){
			drawDot(g,(int)xpos,(int)ypos);
			xpos+=dx;
			ypos+=dy;
		}
		drawDot(g,x2,y2);
	}

	public void drawDot(Graphics g,int x,int y){
		g.drawLine(x-1,y-1,x,y-1);
		g.drawLine(x-1,y,x,y);
	}
	
}
