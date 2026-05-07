/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2012 Excensus LLC.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: triangulate/quadedge/TrianglePredicate.java r524
 *
 **********************************************************************/

#include <geos/triangulate/quadedge/TrianglePredicate.h>

#include <geos/geom/Coordinate.h>

namespace geos {
namespace geom { // geos.geom

bool
TrianglePredicate::isInCircleNonRobust(
		const Coordinate &a, const Coordinate &b, const Coordinate &c,
		const Coordinate &p)
{
	bool isInCircle =
		(a.x * a.x + a.y * a.y) * triArea(b, c, p)
		- (b.x * b.x + b.y * b.y) * triArea(a, c, p)
		+ (c.x * c.x + c.y * c.y) * triArea(a, b, p)
		- (p.x * p.x + p.y * p.y) * triArea(a, b, c)
		> 0;
	return isInCircle;
}

bool
TrianglePredicate::isInCircleNormalized(
		const Coordinate &a, const Coordinate &b, const Coordinate &c,
		const Coordinate &p)
{
	double adx = a.x - p.x;
	double ady = a.y - p.y;
	double bdx = b.x - p.x;
	double bdy = b.y - p.y;
	double cdx = c.x - p.x;
	double cdy = c.y - p.y;

	double abdet = adx * bdy - bdx * ady;
	double bcdet = bdx * cdy - cdx * bdy;
	double cadet = cdx * ady - adx * cdy;
	double alift = adx * adx + ady * ady;
	double blift = bdx * bdx + bdy * bdy;
	double clift = cdx * cdx + cdy * cdy;

	double disc = alift * bcdet + blift * cadet + clift * abdet;
	return disc > 0;
}

double
TrianglePredicate::triArea(const Coordinate &a,
		const Coordinate &b, const Coordinate &c)
{
	return (b.x - a.x) * (c.y - a.y)
		- (b.y - a.y) * (c.x - a.x);
}

bool
TrianglePredicate::isInCircleRobust(
		const Coordinate &a, const Coordinate &b, const Coordinate &c,
		const Coordinate &p)
{
	//checkRobustInCircle(a, b, c, p);
	//	return isInCircleNonRobust(a, b, c, p);
	return isInCircleNormalized(a, b, c, p);
}

} // namespace geos.geom
} // namespace geos

