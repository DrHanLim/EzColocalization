/*******************************************************************************
 * Copyright (c) 2013 Jay Unruh, Stowers Institute for Medical Research.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package ezcol.visual.visual3D;

public class Point3D {
	public int x, y, z;
	public int rx, ry, rz;

	public Point3D(int x1, int y1, int z1) {
		x = x1;
		y = y1;
		z = z1;
		rx = x;
		ry = y;
		rz = z;
	}

	public void moveto(int x1, int y1, int z1) {
		x = x1;
		y = y1;
		z = z1;
		rx = x;
		ry = y;
		rz = z;
	}

	public void reset() {
		rx = x;
		ry = y;
		rz = z;
	}

	public void translate(int transx, int transy, int transz) {
		x += transx;
		y += transy;
		z += transz;
		rx += transx;
		ry += transy;
		rz += transz;
	}

	public void rotatedeg(double degx1, double degy1, double degz1, int centerx, int centery, int centerz) {
		double radx = Math.toRadians(degx1);
		double rady = Math.toRadians(degy1);
		double radz = Math.toRadians(degz1);
		setrotation(radx, rady, radz, centerx, centery, centerz);
	}

	public void rotaterad(double radx1, double rady1, double radz1, int centerx, int centery, int centerz) {
		setrotation(radx1, rady1, radz1, centerx, centery, centerz);
	}

	public void rotatecossin(double cosx1, double cosy1, double cosz1, double sinx1, double siny1, double sinz1,
			int centerx, int centery, int centerz) {
		setrotation(cosx1, cosy1, cosz1, sinx1, siny1, sinz1, centerx, centery, centerz);
	}

	public void addrotatedeg(double degx1, double degy1, double degz1, int centerx, int centery, int centerz) {
		double radx = Math.toRadians(degx1);
		double rady = Math.toRadians(degy1);
		double radz = Math.toRadians(degz1);
		addrotation(radx, rady, radz, centerx, centery, centerz);
	}

	public void addrotaterad(double radx1, double rady1, double radz1, int centerx, int centery, int centerz) {
		addrotation(radx1, rady1, radz1, centerx, centery, centerz);
	}

	public void transform_perspective(double horizon_dist, int centerx, int centery, int centerz) {
		if (horizon_dist > 0.0) {
			double tempx = (double) (rx - centerx);
			double tempy = (double) (ry - centery);
			double tempz = (double) (rz - centerz);
			double temphordist = (tempz + horizon_dist) / horizon_dist;
			if (temphordist <= 0) {
				tempx = 0;
				tempy = 0;
			} else {
				tempx *= temphordist;
				tempy *= temphordist;
			}
			rx = centerx + (int) tempx;
			ry = centery + (int) tempy;
		}
	}

	public void transform_negative_perspective(double horizon_dist, int centerx, int centery, int centerz) {
		// here the horizon is in the foreground
		if (horizon_dist > 0.0) {
			double tempx = (double) (rx - centerx);
			double tempy = (double) (ry - centery);
			double tempz = (double) (centerz - rz);
			double temphordist = (tempz + horizon_dist) / horizon_dist;
			if (temphordist <= 0) {
				tempx = 0;
				tempy = 0;
			} else {
				tempx *= temphordist;
				tempy *= temphordist;
			}
			rx = centerx + (int) tempx;
			ry = centery + (int) tempy;
		}
	}

	public void setrotation(double dx, double dy, double dz, int centerx, int centery, int centerz) {
		// rotate about the x, y, and z axes in order
		double tempx = (double) (x - centerx);
		double tempy = (double) (y - centery);
		double tempz = (double) (z - centerz);
		if (dz != 0.0) {
			double sinval = Math.sin(-dz);
			double cosval = Math.cos(-dz);
			double tempx1 = tempx * cosval - tempy * sinval;
			double tempy1 = tempx * sinval + tempy * cosval;
			tempx = tempx1;
			tempy = tempy1;
		}
		if (dy != 0.0) {
			double sinval = Math.sin(dy);
			double cosval = Math.cos(dy);
			double tempx1 = tempx * cosval + tempz * sinval;
			double tempz1 = -tempx * sinval + tempz * cosval;
			tempx = tempx1;
			tempz = tempz1;
		}
		if (dx != 0.0) {
			double sinval = Math.sin(dx);
			double cosval = Math.cos(dx);
			double tempy1 = tempy * cosval - tempz * sinval;
			double tempz1 = tempy * sinval + tempz * cosval;
			tempy = tempy1;
			tempz = tempz1;
		}
		rx = centerx + (int) tempx;
		ry = centery + (int) tempy;
		rz = centerz + (int) tempz;
	}

	public void setrotation(double cosdx, double cosdy, double cosdz, double sindx, double sindy, double sindz,
			int centerx, int centery, int centerz) {
		// rotate about the x, y, and z axes in order
		double tempx = (double) (x - centerx);
		double tempy = (double) (y - centery);
		double tempz = (double) (z - centerz);
		
		double sinval,cosval,tempx1,tempy1,tempz1;
		if (sindz != 0.0) {
			sinval = sindz;
			cosval = cosdz;
			tempx1 = tempx * cosval - tempy * sinval;
			tempy1 = tempx * sinval + tempy * cosval;
			tempx = tempx1;
			tempy = tempy1;
		}
		if (sindy != 0.0) {
			sinval = sindy;
			cosval = cosdy;
			tempx1 = tempx * cosval + tempz * sinval;
			tempz1 = -tempx * sinval + tempz * cosval;
			tempx = tempx1;
			tempz = tempz1;
		}
		if (sindx != 0.0) {
			sinval = sindx;
			cosval = cosdx;
			tempy1 = tempy * cosval - tempz * sinval;
			tempz1 = tempy * sinval + tempz * cosval;
			tempy = tempy1;
			tempz = tempz1;
		}
		rx = centerx + (int) tempx;
		ry = centery + (int) tempy;
		rz = centerz + (int) tempz;
	}

	public void addrotation(double dx, double dy, double dz, int centerx, int centery, int centerz) {
		// rotate about the x, y, and z axes in order
		double tempx = (double) (rx - centerx);
		double tempy = (double) (ry - centery);
		double tempz = (double) (rz - centerz);
		if (dz != 0.0) {
			double sinval = Math.sin(-dz);
			double cosval = Math.cos(-dz);
			double tempx1 = tempx * cosval - tempy * sinval;
			double tempy1 = tempx * sinval + tempy * cosval;
			tempx = tempx1;
			tempy = tempy1;
		}
		if (dy != 0.0) {
			double sinval = Math.sin(dy);
			double cosval = Math.cos(dy);
			double tempx1 = tempx * cosval + tempz * sinval;
			double tempz1 = -tempx * sinval + tempz * cosval;
			tempx = tempx1;
			tempz = tempz1;
		}
		if (dx != 0.0) {
			double sinval = Math.sin(dx);
			double cosval = Math.cos(dx);
			double tempy1 = tempy * cosval - tempz * sinval;
			double tempz1 = tempy * sinval + tempz * cosval;
			tempy = tempy1;
			tempz = tempz1;
		}
		rx = centerx + (int) tempx;
		ry = centery + (int) tempy;
		rz = centerz + (int) tempz;
	}

	public boolean equals(Point3D p3d) {
		return x == p3d.x && y == p3d.y && z == p3d.z 
			&& rx == p3d.rx && ry == p3d.ry && rz == p3d.rz;
	}

}
