/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2009 2011 Sandro Santilli <strk@kbt.io>
 * Copyright (C) 2005-2006 Refractions Research Inc.
 * Copyright (C) 2001-2002 Vivid Solutions Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: geom/LineSegment.java r18 (JTS-1.11)
 *
 **********************************************************************/

#include <geos/geom/LineSegment.h>
#include <geos/geom/LineString.h> // for toGeometry
#include <geos/geom/Coordinate.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/geom/GeometryFactory.h>
#include <geos/geom/CoordinateArraySequence.h> // should we really be using this?
#include <geos/algorithm/CGAlgorithms.h>
#include <geos/algorithm/LineIntersector.h>
#include <geos/algorithm/HCoordinate.h>
#include <geos/algorithm/NotRepresentableException.h>
#include <geos/util/IllegalStateException.h>
#include <geos/profiler.h>
#include <geos/inline.h>

#include <algorithm> // for max
#include <sstream>
#include <cmath>

#ifndef GEOS_INLINE
# include <geos/geom/LineSegment.inl>
#endif

using namespace std;
//using namespace geos::algorithm;
using geos::algorithm::HCoordinate;
using geos::algorithm::NotRepresentableException;
using geos::algorithm::LineIntersector;

namespace geos {
namespace geom { // geos::geom


/*public*/
void
LineSegment::reverse()
{
	// TODO: use std::swap<>
	Coordinate temp=p0;
	p0=p1;
	p1=temp;
}

/*public*/
double
LineSegment::projectionFactor(const Coordinate& p) const
{
	if (p==p0) return 0.0;
	if (p==p1) return 1.0;
    // Otherwise, use comp.graphics.algorithms Frequently Asked Questions method
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
	double dx=p1.x-p0.x;
	double dy=p1.y-p0.y;
	double len2=dx*dx+dy*dy;
	double r=((p.x-p0.x)*dx+(p.y-p0.y)*dy)/len2;
	return r;
}

/*public*/
double
LineSegment::segmentFraction(const Coordinate& inputPt) const
{
	double segFrac = projectionFactor(inputPt);
	if (segFrac < 0.0)
		segFrac = 0.0;
	else if (segFrac > 1.0)
		segFrac = 1.0;
	return segFrac;
}

/*public*/
void
LineSegment::project(const Coordinate& p, Coordinate& ret) const
{
	if (p==p0 || p==p1) ret=p;
	double r=projectionFactor(p);
	ret=Coordinate(p0.x+r*(p1.x-p0.x),p0.y+r*(p1.y-p0.y));
}

bool
LineSegment::project(const LineSegment& seg, LineSegment& ret) const
{
	double pf0=projectionFactor(seg.p0);
	double pf1=projectionFactor(seg.p1);
	// check if segment projects at all

	if (pf0>=1.0 && pf1>=1.0) return false;

	if (pf0<=0.0 && pf1<=0.0) return false;

	Coordinate newp0;
	project(seg.p0, newp0);
	Coordinate newp1;
	project(seg.p1, newp1);

	ret.setCoordinates(newp0, newp1);

	return true;
}

//Coordinate*
void
LineSegment::closestPoint(const Coordinate& p, Coordinate& ret) const
{
	double factor=projectionFactor(p);
	if (factor>0 && factor<1) {
		project(p, ret);
		return;
	}
	double dist0=p0.distance(p);
	double dist1=p1.distance(p);
	if (dist0<dist1)
	{
		ret=p0;
		return;
	}
	ret=p1;
}

/*public*/
int
LineSegment::compareTo(const LineSegment& other) const
{
	int comp0=p0.compareTo(other.p0);
	if (comp0!=0) return comp0;
	return p1.compareTo(other.p1);
}

/*public*/
bool
LineSegment::equalsTopo(const LineSegment& other) const
{
	return (p0==other.p0 && p1==other.p1) || (p0==other.p1 && p1==other.p0);
}

/*public*/
int
LineSegment::orientationIndex(const LineSegment& seg) const
{
	int orient0 = algorithm::CGAlgorithms::orientationIndex(p0, p1, seg.p0);
	int orient1 = algorithm::CGAlgorithms::orientationIndex(p0, p1, seg.p1);
	// this handles the case where the points are L or collinear
	if (orient0 >= 0 && orient1 >= 0)
		return std::max(orient0, orient1);
	// this handles the case where the points are R or collinear
	if (orient0 <= 0 && orient1 <= 0)
        return std::max(orient0, orient1);
	// points lie on opposite sides ==> indeterminate orientation
	return 0;
}

CoordinateSequence*
LineSegment::closestPoints(const LineSegment& line)
{
	// test for intersection
	Coordinate intPt;
	if ( intersection(line, intPt) )
	{
		CoordinateSequence *cl=new CoordinateArraySequence(new vector<Coordinate>(2, intPt));
		return cl;
	}

	/*
	 * if no intersection closest pair contains at least one endpoint.
	 * Test each endpoint in turn.
	 */
	CoordinateSequence *closestPt=new CoordinateArraySequence(2);
	//vector<Coordinate> *cv = new vector<Coordinate>(2);

	double minDistance=DoubleMax;
	double dist;

	Coordinate close00;
	closestPoint(line.p0, close00);
	minDistance = close00.distance(line.p0);

	closestPt->setAt(close00, 0);
	closestPt->setAt(line.p0, 1);

	Coordinate close01;
	closestPoint(line.p1, close01);
	dist = close01.distance(line.p1);
	if (dist < minDistance) {
		minDistance = dist;
		closestPt->setAt(close01,0);
		closestPt->setAt(line.p1,1);
		//(*cv)[0] = close01;
		//(*cv)[1] = line.p1;
	}

	Coordinate close10;
	line.closestPoint(p0, close10);
	dist = close10.distance(p0);
		if (dist < minDistance) {
		minDistance = dist;
		closestPt->setAt(p0,0);
		closestPt->setAt(close10,1);
		//(*cv)[0] = p0;
		//(*cv)[1] = close10;
	}

	Coordinate close11;
	line.closestPoint(p1, close11);
	dist = close11.distance(p1);
	if (dist < minDistance) {
		minDistance = dist;
		closestPt->setAt(p1,0);
		closestPt->setAt(close11,1);
		//(*cv)[0] = p1;
		//(*cv)[1] = *close11;
	}

	return closestPt;
}

bool
LineSegment::intersection(const LineSegment& line, Coordinate& ret) const
{
	algorithm::LineIntersector li;
	li.computeIntersection(p0, p1, line.p0, line.p1);
	if (li.hasIntersection()) {
		ret=li.getIntersection(0);
		return true;
	}
	return false;
}

bool
LineSegment::lineIntersection(const LineSegment& line, Coordinate& ret) const
{
	try {
		HCoordinate::intersection(p0, p1, line.p0, line.p1, ret);
		return true;
	}
	catch (const NotRepresentableException& /*ex*/) {
		// eat this exception, and return null;
	}
	return false;
}


/* public */
void
LineSegment::pointAlongOffset(double segmentLengthFraction,
	                      double offsetDistance,
	                      Coordinate& ret) const
{
	// the point on the segment line
	double segx = p0.x + segmentLengthFraction * (p1.x - p0.x);
	double segy = p0.y + segmentLengthFraction * (p1.y - p0.y);

	double dx = p1.x - p0.x;
	double dy = p1.y - p0.y;
	double len = sqrt(dx * dx + dy * dy);

	double ux = 0.0;
	double uy = 0.0;
	if (offsetDistance != 0.0) {
		if (len <= 0.0) {
			throw util::IllegalStateException("Cannot compute offset from zero-length line segment");
		}

		// u is the vector that is the length of the offset,
		// in the direction of the segment
		ux = offsetDistance * dx / len;
		uy = offsetDistance * dy / len;
	}

	// the offset point is the seg point plus the offset
	// vector rotated 90 degrees CCW
	double offsetx = segx - uy;
	double offsety = segy + ux;

	ret = Coordinate(offsetx, offsety);
}

/* public */
std::unique_ptr<LineString>
LineSegment::toGeometry(const GeometryFactory& gf) const
{
	CoordinateSequence *cl=new CoordinateArraySequence();
	cl->add(p0);
	cl->add(p1);
	return std::unique_ptr<LineString>(
		gf.createLineString(cl) // ownership transferred
	);
}

} // namespace geos::geom
} // namespace geos
