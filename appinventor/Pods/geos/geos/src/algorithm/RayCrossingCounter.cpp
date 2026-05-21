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
 * Last port: algorithm/RayCrossingCounter.java rev. 1.2 (JTS-1.9)
 *
 **********************************************************************/

#include <geos/algorithm/RayCrossingCounter.h>
#include <geos/algorithm/RobustDeterminant.h>
#include <geos/geom/Geometry.h>
#include <geos/geom/Location.h>
#include <geos/geom/Coordinate.h>
#include <geos/geom/CoordinateSequence.h>


namespace geos {
namespace algorithm {
//
// private:
//

//
// protected:
//

//
// public:
//
/*static*/ int
RayCrossingCounter::locatePointInRing(const geom::Coordinate& point,
                         const geom::CoordinateSequence& ring)
{
	RayCrossingCounter rcc(point);

	for (std::size_t i = 1, ni = ring.size(); i < ni; i++)
	{
		const geom::Coordinate & p1 = ring[ i - 1 ];
		const geom::Coordinate & p2 = ring[ i ];

		rcc.countSegment(p1, p2);

		if ( rcc.isOnSegment() )
			return rcc.getLocation();
	}
	return rcc.getLocation();
}

/*static*/ int
RayCrossingCounter::locatePointInRing(const geom::Coordinate& point,
	         const std::vector<const geom::Coordinate*>& ring)
{
	RayCrossingCounter rcc(point);

	for (std::size_t i = 1, ni = ring.size(); i < ni; i++)
	{
		const geom::Coordinate & p1 = *ring[ i - 1 ];
		const geom::Coordinate & p2 = *ring[ i ];

		rcc.countSegment(p1, p2);

		if ( rcc.isOnSegment() )
			return rcc.getLocation();
	}
	return rcc.getLocation();
}

/*public static*/
int
RayCrossingCounter::orientationIndex(const geom::Coordinate& p1,
         const geom::Coordinate& p2, const geom::Coordinate& q)
{
	// travelling along p1->p2, turn counter clockwise to get to q return 1,
	// travelling along p1->p2, turn clockwise to get to q return -1,
	// p1, p2 and q are colinear return 0.
	double dx1=p2.x-p1.x;
	double dy1=p2.y-p1.y;
	double dx2=q.x-p2.x;
	double dy2=q.y-p2.y;
	return RobustDeterminant::signOfDet2x2(dx1,dy1,dx2,dy2);
}

void
RayCrossingCounter::countSegment(const geom::Coordinate& p1,
                                 const geom::Coordinate& p2)
{
	// For each segment, check if it crosses
	// a horizontal ray running from the test point in
	// the positive x direction.

	// check if the segment is strictly to the left of the test point
	if (p1.x < point.x && p2.x < point.x)
		return;

	// check if the point is equal to the current ring vertex
	if (point.x == p2.x && point.y == p2.y)
	{
		isPointOnSegment = true;
		return;
	}

	// For horizontal segments, check if the point is on the segment.
	// Otherwise, horizontal segments are not counted.
	if (p1.y == point.y && p2.y == point.y)
	{
		double minx = p1.x;
		double maxx = p2.x;

		if (minx > maxx)
		{
			minx = p2.x;
			maxx = p1.x;
		}

		if (point.x >= minx && point.x <= maxx)
			isPointOnSegment = true;

		return;
	}

	// Evaluate all non-horizontal segments which cross a horizontal ray
	// to the right of the test pt.
	// To avoid double-counting shared vertices, we use the convention that
	// - an upward edge includes its starting endpoint, and excludes its
	//   final endpoint
	// - a downward edge excludes its starting endpoint, and includes its
	//   final endpoint
	if (((p1.y > point.y) && (p2.y <= point.y)) ||
		((p2.y > point.y) && (p1.y <= point.y)) )
	{
		// For an upward edge, orientationIndex will be positive when p1->p2
		// crosses ray. Conversely, downward edges should have negative sign.
		int sign = orientationIndex(p1, p2, point);
		if (sign == 0)
		{
			isPointOnSegment = true;
			return;
		}

		if (p2.y < p1.y)
			sign = -sign;

		// The segment crosses the ray if the sign is strictly positive.
		if (sign > 0)
			crossingCount++;
	}
}


int
RayCrossingCounter::getLocation()
{
	if (isPointOnSegment)
		return geom::Location::BOUNDARY;

	// The point is in the interior of the ring if the number
	// of X-crossings is odd.
	if ((crossingCount % 2) == 1)
		return geom::Location::INTERIOR;

	return geom::Location::EXTERIOR;
}


bool
RayCrossingCounter::isPointInPolygon()
{
	return getLocation() != geom::Location::EXTERIOR;
}


} // geos::algorithm
} // geos
