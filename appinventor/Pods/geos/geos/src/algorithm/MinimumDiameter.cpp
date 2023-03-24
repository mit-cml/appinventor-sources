/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2001-2002 Vivid Solutions Inc.
 * Copyright (C) 2005 Refractions Research Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: algorithm/MinimumDiameter.java r966
 *
 **********************************************************************
 *
 * TODO:
 * 	- avoid heap allocation for LineSegment and Coordinate
 *
 **********************************************************************/

#include <geos/algorithm/MinimumDiameter.h>
#include <geos/algorithm/ConvexHull.h>
#include <geos/geom/Geometry.h>
#include <geos/geom/LineSegment.h>
#include <geos/geom/Polygon.h>
#include <geos/geom/Point.h>
#include <geos/geom/LineString.h>
#include <geos/geom/CoordinateSequenceFactory.h>
#include <geos/geom/GeometryFactory.h>
#include <geos/geom/CoordinateSequence.h>

#include <typeinfo>
#include <cmath> // for fabs()

using namespace geos::geom;

namespace geos {
namespace algorithm { // geos.algorithm

/**
 * Computes the minimum diameter of a Geometry.
 * The minimum diameter is defined to be the
 * width of the smallest band that
 * contains the geometry,
 * where a band is a strip of the plane defined
 * by two parallel lines.
 * This can be thought of as the smallest hole that the geometry can be
 * moved through, with a single rotation.
 *
 * The first step in the algorithm is computing the convex hull of the Geometry.
 * If the input Geometry is known to be convex, a hint can be supplied to
 * avoid this computation.
 *
 * @see ConvexHull
 *
 * @version 1.4
 */

/**
 * Compute a minimum diameter for a giver {@link Geometry}.
 *
 * @param geom a Geometry
 */
MinimumDiameter::MinimumDiameter(const Geometry* newInputGeom)
{
	minBaseSeg=new LineSegment();
	minWidthPt=nullptr;
	minPtIndex=0;
	minWidth=0.0;
	inputGeom=newInputGeom;
	isConvex=false;
	convexHullPts=nullptr;
}

/**
 * Compute a minimum diameter for a giver Geometry,
 * with a hint if
 * the Geometry is convex
 * (e.g. a convex Polygon or LinearRing,
 * or a two-point LineString, or a Point).
 *
 * @param geom a Geometry which is convex
 * @param isConvex <code>true</code> if the input geometry is convex
 */
MinimumDiameter::MinimumDiameter(const Geometry* newInputGeom, const bool newIsConvex)
{
	minBaseSeg=new LineSegment();
	minWidthPt=nullptr;
	minWidth=0.0;
	inputGeom=newInputGeom;
	isConvex=newIsConvex;
	convexHullPts=nullptr;
}

MinimumDiameter::~MinimumDiameter()
{
	delete minBaseSeg;
	delete minWidthPt;
	delete convexHullPts;
}

/**
 * Gets the length of the minimum diameter of the input Geometry
 *
 * @return the length of the minimum diameter
 */
double
MinimumDiameter::getLength()
{
	computeMinimumDiameter();
	return minWidth;
}

/**
 * Gets the {@link Coordinate} forming one end of the minimum diameter
 *
 * @return a coordinate forming one end of the minimum diameter
 */
Coordinate*
MinimumDiameter::getWidthCoordinate()
{
	computeMinimumDiameter();
	return minWidthPt;
}

/**
 * Gets the segment forming the base of the minimum diameter
 *
 * @return the segment forming the base of the minimum diameter
 */
LineString*
MinimumDiameter::getSupportingSegment() {
	computeMinimumDiameter();
	const GeometryFactory *fact = inputGeom->getFactory();
	CoordinateSequence* cl=fact->getCoordinateSequenceFactory()->create();
	cl->add(minBaseSeg->p0);
	cl->add(minBaseSeg->p1);
	return fact->createLineString(cl);
}

/**
 * Gets a LineString which is a minimum diameter
 *
 * @return a LineString which is a minimum diameter
 */
LineString*
MinimumDiameter::getDiameter()
{
	computeMinimumDiameter();
	// return empty linestring if no minimum width calculated
	if (minWidthPt==nullptr)
		return inputGeom->getFactory()->createLineString(nullptr);

	Coordinate basePt;
	minBaseSeg->project(*minWidthPt, basePt);

	CoordinateSequence* cl=inputGeom->getFactory()->getCoordinateSequenceFactory()->create();
	cl->add(basePt);
	cl->add(*minWidthPt);
	return inputGeom->getFactory()->createLineString(cl);
}

/* private */
void
MinimumDiameter::computeMinimumDiameter()
{
	// check if computation is cached
	if (minWidthPt!=nullptr)
		return;
	if (isConvex)
		computeWidthConvex(inputGeom);
	else {
		ConvexHull ch(inputGeom);
		Geometry* convexGeom=ch.getConvexHull();
		computeWidthConvex(convexGeom);
		delete convexGeom;
	}
}

/* private */
void
MinimumDiameter::computeWidthConvex(const Geometry *geom)
{
	//System.out.println("Input = " + geom);
	delete convexHullPts;
	if (typeid(*geom)==typeid(Polygon))
	{
		const Polygon* p = dynamic_cast<const Polygon*>(geom);
		convexHullPts=p->getExteriorRing()->getCoordinates();
	}
	else
	{
		convexHullPts=geom->getCoordinates();
	}

	// special cases for lines or points or degenerate rings
	switch(convexHullPts->getSize())
	{
		case 0:
			minWidth=0.0;
			delete minWidthPt;
			minWidthPt=nullptr;
			delete minBaseSeg;
			minBaseSeg=nullptr;
			break;
		case 1:
			minWidth = 0.0;
			delete minWidthPt;
			minWidthPt=new Coordinate(convexHullPts->getAt(0));
			minBaseSeg->p0=convexHullPts->getAt(0);
			minBaseSeg->p1=convexHullPts->getAt(0);
			break;
		case 2:
		case 3:
			minWidth = 0.0;
			delete minWidthPt;
			minWidthPt=new Coordinate(convexHullPts->getAt(0));
			minBaseSeg->p0=convexHullPts->getAt(0);
			minBaseSeg->p1=convexHullPts->getAt(1);
			break;
		default:
			computeConvexRingMinDiameter(convexHullPts);
	}
}

/**
 * Compute the width information for a ring of {@link Coordinate}s.
 * Leaves the width information in the instance variables.
 *
 * @param pts
 * @return
 */
void
MinimumDiameter::computeConvexRingMinDiameter(const CoordinateSequence* pts)
{
	minWidth=DoubleMax;
	unsigned int currMaxIndex=1;
	LineSegment seg;

	// compute the max distance for all segments in the ring, and pick the minimum
	const std::size_t npts=pts->getSize();
	for (std::size_t i=1; i<npts; ++i) {
		seg.p0=pts->getAt(i-1);
		seg.p1=pts->getAt(i);
		currMaxIndex=findMaxPerpDistance(pts, &seg, currMaxIndex);
	}
}

unsigned int
MinimumDiameter::findMaxPerpDistance(const CoordinateSequence *pts,
		LineSegment* seg, unsigned int startIndex)
{
	double maxPerpDistance=seg->distancePerpendicular(pts->getAt(startIndex));
	double nextPerpDistance = maxPerpDistance;
	unsigned int maxIndex = startIndex;
	unsigned int nextIndex = maxIndex;
	while (nextPerpDistance >= maxPerpDistance) {
		maxPerpDistance = nextPerpDistance;
		maxIndex=nextIndex;
		nextIndex=getNextIndex(pts, maxIndex);
		nextPerpDistance = seg->distancePerpendicular(pts->getAt(nextIndex));
	}

	// found maximum width for this segment - update global min dist if appropriate
	if (maxPerpDistance < minWidth) {
		minPtIndex = maxIndex;
		minWidth = maxPerpDistance;
		delete minWidthPt;
		minWidthPt = new Coordinate(pts->getAt(minPtIndex));
		delete minBaseSeg;
		minBaseSeg = new LineSegment(*seg);
//      System.out.println(minBaseSeg);
//      System.out.println(minWidth);
	}
	return maxIndex;
}

unsigned int
MinimumDiameter::getNextIndex(const CoordinateSequence *pts,
	unsigned int index)
{
	if (++index >= pts->getSize()) index = 0;
	return index;
}

Geometry* MinimumDiameter::getMinimumRectangle()
{
	computeMinimumDiameter();

	if ( !minBaseSeg || !convexHullPts )
	{
		//return empty polygon
		return inputGeom->getFactory()->createPolygon();
	}

	// check if minimum rectangle is degenerate (a point or line segment)
	if (minWidth == 0.0) {
		if (minBaseSeg->p0.equals2D(minBaseSeg->p1)) {
			return inputGeom->getFactory()->createPoint(minBaseSeg->p0);
		}
		return minBaseSeg->toGeometry(*inputGeom->getFactory()).release();
	}

	// deltas for the base segment of the minimum diameter
	double dx = minBaseSeg->p1.x - minBaseSeg->p0.x;
	double dy = minBaseSeg->p1.y - minBaseSeg->p0.y;

	double minPara = DoubleMax;
	double maxPara = -DoubleMax;
	double minPerp = DoubleMax;
	double maxPerp = -DoubleMax;

	// compute maxima and minima of lines parallel and perpendicular to base segment
	std::size_t const n=convexHullPts->getSize();
	for (std::size_t i = 0; i < n; ++i) {

		double paraC = computeC(dx, dy, convexHullPts->getAt(i));
		if (paraC > maxPara) maxPara = paraC;
		if (paraC < minPara) minPara = paraC;

		double perpC = computeC(-dy, dx, convexHullPts->getAt(i));
		if (perpC > maxPerp) maxPerp = perpC;
		if (perpC < minPerp) minPerp = perpC;
	}

	// compute lines along edges of minimum rectangle
	LineSegment maxPerpLine = computeSegmentForLine(-dx, -dy, maxPerp);
	LineSegment minPerpLine = computeSegmentForLine(-dx, -dy, minPerp);
	LineSegment maxParaLine = computeSegmentForLine(-dy, dx, maxPara);
	LineSegment minParaLine = computeSegmentForLine(-dy, dx, minPara);

	// compute vertices of rectangle (where the para/perp max & min lines intersect)
	Coordinate p0, p1, p2, p3;
	maxParaLine.lineIntersection(maxPerpLine, p0);
	minParaLine.lineIntersection(maxPerpLine, p1);
	minParaLine.lineIntersection(minPerpLine, p2);
	maxParaLine.lineIntersection(minPerpLine, p3);

	const CoordinateSequenceFactory *csf =
	inputGeom->getFactory()->getCoordinateSequenceFactory();

	geom::CoordinateSequence *seq = csf->create(5, 2);
	seq->setAt(p0, 0);
	seq->setAt(p1, 1);
	seq->setAt(p2, 2);
	seq->setAt(p3, 3);
	seq->setAt(p0, 4); // close

	LinearRing* shell = inputGeom->getFactory()->createLinearRing( seq );
	return inputGeom->getFactory()->createPolygon( shell, nullptr );
}

double MinimumDiameter::computeC(double a, double b, const Coordinate& p)
{
	return a * p.y - b * p.x;
}

LineSegment MinimumDiameter::computeSegmentForLine(double a, double b, double c)
{
	Coordinate p0;
	Coordinate p1;
	/*
	* Line eqn is ax + by = c
	* Slope is a/b.
	* If slope is steep, use y values as the inputs
	*/
	if (fabs(b) > fabs(a) ) {
		p0 = Coordinate(0.0, c/b);
		p1 = Coordinate(1.0, c/b - a/b);
	}
	else {
		p0 = Coordinate(c/a, 0.0);
		p1 = Coordinate(c/a - b/a, 1.0);
	}
	return LineSegment(p0, p1);
}


Geometry *MinimumDiameter::getMinimumRectangle(Geometry *geom)
{
	MinimumDiameter md( geom );
	return md.getMinimumRectangle();
}

Geometry *MinimumDiameter::getMinimumDiameter(Geometry *geom)
{
	MinimumDiameter md( geom );
	return md.getDiameter();
}

} // namespace geos.algorithm
} // namespace geos

