/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2001-2002 Vivid Solutions Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: geomgraph/Quadrant.java rev. 1.8 (JTS-1.10)
 *
 **********************************************************************/

#include <sstream>

#include <geos/geomgraph/Quadrant.h>
#include <geos/util/IllegalArgumentException.h>

#include <geos/geom/Coordinate.h>

using namespace std;
using namespace geos::geom;

namespace geos {
namespace geomgraph { // geos.geomgraph

/* public static */
int
Quadrant::quadrant(double dx, double dy)
{
	if (dx == 0.0 && dy == 0.0) {
		ostringstream s;
		s<<"Cannot compute the quadrant for point ";
		s<<"("<<dx<<","<<dy<<")"<<endl;
		throw util::IllegalArgumentException(s.str());
	}
	if (dx >= 0) {
		if (dy >= 0)
			return NE;
		else
			return SE;
	} else {
		if (dy >= 0)
			return NW;
		else
			return SW;
	}
}

/* public static */
int
Quadrant::quadrant(const Coordinate& p0, const Coordinate& p1)
{
	if (p1.x == p0.x && p1.y == p0.y)
	{
		throw util::IllegalArgumentException("Cannot compute the quadrant for two identical points " + p0.toString());
	}

	if (p1.x >= p0.x) {
		if (p1.y >= p0.y)
			return NE;
		else
			return SE;
	}
	else {
		if (p1.y >= p0.y)
			return NW;
		else
			return SW;
	}
}

/* public static */
bool
Quadrant::isOpposite(int quad1, int quad2)
{
	if (quad1==quad2) return false;
	int diff=(quad1-quad2+4)%4;
	// if quadrants are not adjacent, they are opposite
	if (diff==2) return true;
	return false;
}

/* public static */
int
Quadrant::commonHalfPlane(int quad1, int quad2)
{
	// if quadrants are the same they do not determine a unique
	// common halfplane.
	// Simply return one of the two possibilities
	if (quad1==quad2) return quad1;
	int diff=(quad1-quad2+4)%4;
	// if quadrants are not adjacent, they do not share a common halfplane
	if (diff==2) return -1;
	//
	int min=(quad1<quad2)? quad1:quad2;
	int max=(quad1>quad2)? quad1:quad2;
	// for this one case, the righthand plane is NOT the minimum index;
	if (min==0 && max==3) return 3;
	// in general, the halfplane index is the minimum of the two
	// adjacent quadrants
	return min;
}

/* public static */
bool
Quadrant::isInHalfPlane(int quad, int halfPlane)
{
	if (halfPlane==SE) {
		return quad==SE || quad==SW;
	}
	return quad==halfPlane || quad==halfPlane+1;
}

/* public static */
bool
Quadrant::isNorthern(int quad)
{
	return quad==NE || quad==NW;
}

} // namespace geos.geomgraph
} // namespace geos
