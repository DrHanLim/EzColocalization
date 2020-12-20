/*******************************************************************************
 * Copyright (c) 2013 Jay Unruh, Stowers Institute for Medical Research.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package ezcol.visual.visual3D;

import java.awt.Color;
import java.awt.Graphics;

public class Arrow3D extends Line3D{

	// here we have a 3D arrow
	public Arrow3D(int x11,int y11,int z11,int x21,int y21,int z21,Color color1){
		super(x11,y11,z11,x21,y21,z21,color1);
	}

	public void drawelement(Graphics g){
		Color tempcolor=g.getColor();
		g.setColor(color);
		draw_arrow(g,pt1.rx,pt1.ry,pt2.rx,pt2.ry);
		g.setColor(tempcolor);
	}

	public void draw_arrow(Graphics g,int x1,int y1,int x2,int y2){
		float length=(float)Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
		if(length==0.0f){
			return;
		}
		float xinc=(float)(x2-x1)/length;
		float yinc=(float)(y2-y1)/length;
		float crossx=(float)x2-xinc*5.0f;
		float crossy=(float)y2-yinc*5.0f;
		float x3=crossx-yinc*3.5f;
		float x4=crossx+yinc*3.5f;
		float y3=crossy+xinc*3.5f;
		float y4=crossy-xinc*3.5f;
		drawLine(g,x1,y1,x2,y2,thick);
		drawLine(g,x2,y2,Math.round(x3),Math.round(y3),thick);
		drawLine(g,x2,y2,Math.round(x4),Math.round(y4),thick);
	}

}
