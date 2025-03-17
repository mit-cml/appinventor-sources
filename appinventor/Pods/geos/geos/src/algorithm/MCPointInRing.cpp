/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2006 Refractions Research Inc.
 * Copyright (C) 2001-2002 Vivid Solutions Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************/

#include <geos/algorithm/MCPointInRing.h>
#include <geos/algorithm/RobustDeterminant.h>
#include <geos/index/bintree/Bintree.h>
#include <geos/index/bintree/Interval.h>
#include <geos/geom/LineSegment.h>
#include <geos/geom/LinearRing.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/geom/Envelope.h>
#include <geos/index/chain/MonotoneChain.h>
#include <geos/index/chain/MonotoneChainBuilder.h>

#include <vector>

using namespace std;
using namespace geos::geom;
using namespace geos::index;

namespace geos {
namespace algorithm { // geos.algorithm

MCPointInRing::MCSelecter::MCSelecter(const Coordinate& newP,
		MCPointInRing *prt)
	:
	MonotoneChainSelectAction()
{
	p=newP;
	parent=prt;
}

/* public overridden */
void
MCPointInRing::MCSelecter::select(const LineSegment& ls)
{
	parent->testLineSegment(p, ls);
}

MCPointInRing::MCPointInRing(const LinearRing *newRing)
	:
	ring(newRing),
	interval(),
	pts(nullptr),
	tree(nullptr),
	crossings(0)
{
	buildIndex();
}

MCPointInRing::~MCPointInRing()
{
	delete tree;
	delete pts;
}

void
MCPointInRing::buildIndex()
{
	//using namespace geos::index;

//	Envelope *env=ring->getEnvelopeInternal();
	tree=new bintree::Bintree();
	pts=CoordinateSequence::removeRepeatedPoints(ring->getCoordinatesRO());

	// NOTE: we take ownership of mcList and it's elements
	vector<chain::MonotoneChain*> *mcList =
		chain::MonotoneChainBuilder::getChains(pts);

	for(size_t i=0, n=mcList->size(); i<n; ++i)
	{
		chain::MonotoneChain *mc=(*mcList)[i];
		const Envelope& mcEnv = mc->getEnvelope();
		interval.min = mcEnv.getMinY();
		interval.max = mcEnv.getMaxY();

		// TODO: is 'mc' ownership transferred here ? (see below)
		//       by documentation SpatialIndex does NOT take
		//       ownership of the items, so unless we query it
		//       all later we've a leak problem here..
		//       Need a focused testcase.
		//
		tree->insert(&interval, mc);
	}

	// TODO: mcList elements ownership went to tree or what ?

	delete mcList;
}

bool
MCPointInRing::isInside(const Coordinate& pt)
{
	crossings=0;
	// test all segments intersected by ray from pt in positive x direction
	Envelope *rayEnv=new Envelope(DoubleNegInfinity,DoubleInfinity,pt.y,pt.y);
	interval.min=pt.y;
	interval.max=pt.y;
	vector<void*> *segs=tree->query(&interval);
	//System.out.println("query size=" + segs.size());
	MCSelecter *mcSelecter=new MCSelecter(pt,this);
	for(int i=0;i<(int)segs->size();i++) {
		chain::MonotoneChain *mc=(chain::MonotoneChain*) (*segs)[i];
		testMonotoneChain(rayEnv,mcSelecter,mc);
	}
	/*
	*  p is inside if number of crossings is odd.
	*/
//	for(int i=0;i<(int)segs->size();i++) {
//		delete (chain::MonotoneChain*) (*segs)[i];
//	}
	delete segs;
	delete rayEnv;
	delete mcSelecter;
	if((crossings%2)==1) {
		return true;
	}
	return false;
}


void
MCPointInRing::testMonotoneChain(Envelope *rayEnv,
		MCSelecter *mcSelecter,
		chain::MonotoneChain *mc)
{
	mc->select(*rayEnv, *mcSelecter);
}

void
MCPointInRing::testLineSegment(const Coordinate& p, const LineSegment& seg)
{
	double xInt;  // x intersection of segment with ray
	double x1;    // translated coordinates
	double y1;
	double x2;
	double y2;

	/*
	 * Test if segment crosses ray from test point in positive x direction.
	 */
	const Coordinate& p1 = seg.p0;
	const Coordinate& p2 = seg.p1;
	x1 = p1.x - p.x;
	y1 = p1.y - p.y;
	x2 = p2.x - p.x;
	y2 = p2.y - p.y;

	if (((y1>0)&&(y2<=0)) || ((y2>0)&&(y1<=0)))
	{

		/*
		 *  segment straddles x axis, so compute intersection.
		 */
		xInt=RobustDeterminant::signOfDet2x2(x1,y1,x2,y2)/(y2-y1);

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

