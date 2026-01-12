/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
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
 **********************************************************************/

#include <geos/algorithm/SIRtreePointInRing.h>
#include <geos/algorithm/RobustDeterminant.h>
#include <geos/index/strtree/SIRtree.h>
#include <geos/geom/LinearRing.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/geom/LineSegment.h>
#include <geos/geom/Coordinate.h>

#include <vector>

using namespace std;
using namespace geos::index::strtree;
using namespace geos::geom;

namespace geos {
namespace algorithm { // geos.algorithm

SIRtreePointInRing::SIRtreePointInRing(LinearRing *newRing):
	PointInRing(),
	ring(newRing),
	sirTree(nullptr),
	crossings(0)
{
	buildIndex();
}

SIRtreePointInRing::~SIRtreePointInRing()
{
	delete sirTree;
}

void
SIRtreePointInRing::buildIndex()
{
	//Envelope *env=ring->getEnvelopeInternal();
	sirTree=new SIRtree();
	const CoordinateSequence *pts=ring->getCoordinatesRO();

	const std::size_t npts=pts->getSize();
	for(std::size_t i=1; i<npts; ++i)
	{
		if(pts->getAt(i-1)==pts->getAt(i)) continue; // Optimization suggested by MD. [Jon Aquino]
		LineSegment *seg=new LineSegment(pts->getAt(i-1), pts->getAt(i));
		sirTree->insert(seg->p0.y, seg->p1.y, seg);
	}
}

bool
SIRtreePointInRing::isInside(const Coordinate& pt)
{
	crossings=0;
	// test all segments intersected by vertical ray at pt
	vector<void*> *segs=sirTree->query(pt.y);
	//System.out.println("query size=" + segs.size());
	for(int i=0;i<(int)segs->size();i++) {
		LineSegment *seg=(LineSegment*) (*segs)[i];
		testLineSegment(pt,seg);
	}

	/*
	*  p is inside if number of crossings is odd.
	*/
	if ((crossings%2)==1) {
		return true;
	}
	return false;
}

void
SIRtreePointInRing::testLineSegment(const Coordinate& p,LineSegment *seg)
{
	double xInt;  // x intersection of segment with ray
	double x1;    // translated coordinates
	double y1;
	double x2;
	double y2;

	/*
	*  Test if segment crosses ray from test point in positive x direction.
	*/
	Coordinate& p1=seg->p0;
	Coordinate& p2=seg->p1;
	x1=p1.x-p.x;
	y1=p1.y-p.y;
	x2=p2.x-p.x;
	y2=p2.y-p.y;

	if (((y1>0) && (y2<=0)) ||
		((y2>0) && (y1<=0))) {
		/*
		*  segment straddles x axis,so compute intersection.
		*/
		xInt=RobustDeterminant::signOfDet2x2(x1,y1,x2,y2)/(y2-y1);
		//xsave=xInt;
		/*
		*  crosses ray if strictly positive intersection.
		*/
		if (0.0<xInt) {
			crossings++;
		}
	}
}

} // namespace geos.algorithm
} // namespace geos

