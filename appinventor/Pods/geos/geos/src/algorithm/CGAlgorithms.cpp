/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2011 Sandro Santilli <strk@kbt.io>
 * Copyright (C) 2005 2006 Refractions Research Inc.
 * Copyright (C) 2001-2002 Vivid Solutions Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: algorithm/CGAlgorithms.java r378 (JTS-1.12)
 *
 **********************************************************************/

#include <geos/algorithm/CGAlgorithms.h>
#include <geos/algorithm/RobustDeterminant.h>
#include <geos/algorithm/LineIntersector.h>
#include <geos/algorithm/RayCrossingCounter.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/geom/Coordinate.h>
#include <geos/geom/Location.h>
#include <geos/util/IllegalArgumentException.h>

#include <algorithm>
//#include <cstdio>
#include <cmath>

using namespace std;

using namespace geos::geom;

namespace geos {
namespace algorithm { // geos.algorithm

/*public static*/
int
CGAlgorithms::orientationIndex(const Coordinate& p1,const Coordinate& p2,const Coordinate& q)
{
	return RayCrossingCounter::orientationIndex(p1, p2, q);
}

/*public static*/
bool
CGAlgorithms::isPointInRing(const Coordinate& p, const CoordinateSequence* ring)
{
	return locatePointInRing(p, *ring) != Location::EXTERIOR;
}

/*public static*/
bool
CGAlgorithms::isPointInRing(const Coordinate& p,
		const Coordinate::ConstVect& ring)
{
	return locatePointInRing(p, ring) != Location::EXTERIOR;
}

/*public static*/
int
CGAlgorithms::locatePointInRing(const Coordinate& p,
		const CoordinateSequence& ring)
{
	return RayCrossingCounter::locatePointInRing(p, ring);
}

/*public static*/
int
CGAlgorithms::locatePointInRing(const Coordinate& p,
		const std::vector<const geom::Coordinate*>& ring)
{
	return RayCrossingCounter::locatePointInRing(p, ring);
}

/*public static*/
bool
CGAlgorithms::isOnLine(const Coordinate& p, const CoordinateSequence* pt)
{
	//LineIntersector lineIntersector;
	size_t ptsize = pt->getSize();
	if ( ptsize == 0 ) return false;

	const Coordinate *pp=&(pt->getAt(0));
	for(size_t i=1; i<ptsize; ++i)
	{
		const Coordinate &p1=pt->getAt(i);
		if ( LineIntersector::hasIntersection(p, *pp, p1) )
			return true;
		pp=&p1;
	}
	return false;
}

/*public static*/
bool
CGAlgorithms::isCCW(const CoordinateSequence* ring)
{
	// sanity check
	if (ring->getSize() < 4)
	{
		throw util::IllegalArgumentException("Ring has fewer than 3 points, so orientation cannot be determined");
	}

	// # of points without closing endpoint
	const std::size_t nPts=ring->getSize()-1;

	// find highest point
	const Coordinate *hiPt=&ring->getAt(0);
	int hiIndex=0;
	for (std::size_t i=1; i<=nPts; ++i)
	{
		const Coordinate *p=&ring->getAt(i);
		if (p->y > hiPt->y) {
			hiPt = p;
			hiIndex = static_cast<int>(i);
		}
	}

	// find distinct point before highest point
	int iPrev = hiIndex;
	do {
		iPrev = iPrev - 1;
		if (iPrev < 0)
            iPrev = static_cast<int>(nPts);
	} while (ring->getAt(iPrev)==*hiPt && iPrev!=hiIndex);

	// find distinct point after highest point
	int iNext = hiIndex;
	do {
		iNext = (iNext + 1) % static_cast<int>(nPts);
	} while (ring->getAt(iNext)==*hiPt && iNext != hiIndex);

	const Coordinate *prev=&ring->getAt(iPrev);
	const Coordinate *next=&ring->getAt(iNext);

	/*
	 * This check catches cases where the ring contains an A-B-A
	 * configuration of points.
	 * This can happen if the ring does not contain 3 distinct points
	 * (including the case where the input array has fewer than 4 elements),
	 * or it contains coincident line segments.
	 */
	if ( prev->equals2D(*hiPt) || next->equals2D(*hiPt) ||
		prev->equals2D(*next) )
	{
		return false;
		// MD - don't bother throwing exception,
		// since this isn't a complete check for ring validity
		//throw  IllegalArgumentException("degenerate ring (does not contain 3 distinct points)");
	}

	int disc = computeOrientation(*prev, *hiPt, *next);

	/**
	 *  If disc is exactly 0, lines are collinear.
	 * There are two possible cases:
	 *  (1) the lines lie along the x axis in opposite directions
	 *  (2) the lines lie on top of one another
	 *
	 *  (1) is handled by checking if next is left of prev ==> CCW
	 *  (2) should never happen, so we're going to ignore it!
	 *  (Might want to assert this)
	 */
	bool isCCW=false;

	if (disc == 0) {
		// poly is CCW if prev x is right of next x
		isCCW = (prev->x > next->x);
	} else {
		// if area is positive, points are ordered CCW
		isCCW = (disc > 0);
	}

	return isCCW;
}

/*public static*/
int
CGAlgorithms::computeOrientation(const Coordinate& p1, const Coordinate& p2,
		const Coordinate& q)
{
	return orientationIndex(p1,p2,q);
}

/*public static*/
double
CGAlgorithms::distancePointLine(const Coordinate& p, const Coordinate& A,
		const Coordinate& B)
{
	//if start==end, then use pt distance
	if (A==B) return p.distance(A);

    // otherwise use comp.graphics.algorithms Frequently Asked Questions method
    /*(1)     	      AC dot AB
                   r = ---------
                         ||AB||^2
		r has the following meaning:
		r=0 P = A
		r=1 P = B
		r<0 P is on the backward extension of AB
		r>1 P is on the forward extension of AB
		0<r<1 P is interior to AB
	*/
	double r=((p.x-A.x)*(B.x-A.x)+(p.y-A.y)*(B.y-A.y))/
			 ((B.x-A.x)*(B.x-A.x)+(B.y-A.y)*(B.y-A.y));
	if (r<=0.0) return p.distance(A);
	if (r>=1.0) return p.distance(B);
    /*(2)
		     (Ay-Cy)(Bx-Ax)-(Ax-Cx)(By-Ay)
		s = -----------------------------
		             	L^2

		Then the distance from C to P = |s|*L.
	*/
	double s=((A.y-p.y)*(B.x-A.x)-(A.x-p.x)*(B.y-A.y))/
			 ((B.x-A.x)*(B.x-A.x)+(B.y-A.y)*(B.y-A.y));
	return fabs(s)*sqrt(((B.x-A.x)*(B.x-A.x)+(B.y-A.y)*(B.y-A.y)));
}

/*public static*/
double
CGAlgorithms::distancePointLinePerpendicular(const Coordinate& p,const Coordinate& A,const Coordinate& B)
{
    // use comp.graphics.algorithms Frequently Asked Questions method
    /*(2)
                     (Ay-Cy)(Bx-Ax)-(Ax-Cx)(By-Ay)
                s = -----------------------------
                                     L^2

                Then the distance from C to P = |s|*L.
        */

	double s = ((A.y - p.y) *(B.x - A.x) - (A.x - p.x)*(B.y - A.y) )
              /
            ((B.x - A.x) * (B.x - A.x) + (B.y - A.y) * (B.y - A.y) );
    return fabs(s)*sqrt(((B.x - A.x) * (B.x - A.x) + (B.y - A.y) * (B.y - A.y)));
}

/*public static*/
double
CGAlgorithms::distanceLineLine(const Coordinate& A, const Coordinate& B,
		const Coordinate& C, const Coordinate& D)
{
	// check for zero-length segments
	if (A==B) return distancePointLine(A,C,D);
	if (C==D) return distancePointLine(D,A,B);

    // AB and CD are line segments
    /* from comp.graphics.algo

	Solving the above for r and s yields
				(Ay-Cy)(Dx-Cx)-(Ax-Cx)(Dy-Cy)
	           r = ----------------------------- (eqn 1)
				(Bx-Ax)(Dy-Cy)-(By-Ay)(Dx-Cx)

		 	(Ay-Cy)(Bx-Ax)-(Ax-Cx)(By-Ay)
		s = ----------------------------- (eqn 2)
			(Bx-Ax)(Dy-Cy)-(By-Ay)(Dx-Cx)
	Let P be the position vector of the intersection point, then
		P=A+r(B-A) or
		Px=Ax+r(Bx-Ax)
		Py=Ay+r(By-Ay)
	By examining the values of r & s, you can also determine some other
limiting conditions:
		If 0<=r<=1 & 0<=s<=1, intersection exists
		r<0 or r>1 or s<0 or s>1 line segments do not intersect
		If the denominator in eqn 1 is zero, AB & CD are parallel
		If the numerator in eqn 1 is also zero, AB & CD are collinear.

	*/
	double r_top=(A.y-C.y)*(D.x-C.x)-(A.x-C.x)*(D.y-C.y);
	double r_bot=(B.x-A.x)*(D.y-C.y)-(B.y-A.y)*(D.x-C.x);
	double s_top=(A.y-C.y)*(B.x-A.x)-(A.x-C.x)*(B.y-A.y);
	double s_bot=(B.x-A.x)*(D.y-C.y)-(B.y-A.y)*(D.x-C.x);
	if ((r_bot==0)||(s_bot==0)) {
		return std::min(distancePointLine(A,C,D),
						std::min(distancePointLine(B,C,D),
						std::min(distancePointLine(C,A,B), distancePointLine(D,A,B))));
	}
	double s=s_top/s_bot;
	double r=r_top/r_bot;
	if ((r<0)||( r>1)||(s<0)||(s>1)) {
		//no intersection
		return std::min(distancePointLine(A,C,D),
						std::min(distancePointLine(B,C,D),
						std::min(distancePointLine(C,A,B), distancePointLine(D,A,B))));
	}
	return 0.0; //intersection exists
}

/*public static*/
double
CGAlgorithms::signedArea(const CoordinateSequence* ring)
{
	size_t npts=ring->getSize();

	if (npts<3) return 0.0;

	Coordinate pp;
	Coordinate cp = ring->getAt(0);
	Coordinate np = ring->getAt(1);
	double x0 = cp.x;
        np.x -= x0;
	double sum=0.0;
	for (size_t i=1; i<npts; ++i)
	{
		pp.y = cp.y;
		cp.x = np.x;
		cp.y = np.y;
		ring->getAt(i, np);
                np.x -= x0;
		sum += cp.x * (np.y - pp.y);
	}
	return -sum/2.0;
}

/*public static*/
double
CGAlgorithms::length(const CoordinateSequence* pts)
{
	// optimized for processing CoordinateSequences

	size_t npts=pts->getSize();
	if (npts <= 1) return 0.0;

	double len = 0.0;

	const Coordinate& p0 = pts->getAt(0);
	double x0 = p0.x;
	double y0 = p0.y;

	for(size_t i = 1; i < npts; ++i)
	{
		const Coordinate& p = pts->getAt(i);
		double x1 = p.x;
		double y1 = p.y;
		double dx = x1 - x0;
		double dy = y1 - y0;

		len += sqrt(dx * dx + dy * dy);

		x0 = x1;
		y0 = y1;
	}

	return len;
}


} // namespace geos.algorithm
} // namespace geos
