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
 **********************************************************************/


#include <stdlib.h>
#include <vector>

#include <geos/geomgraph/index/SegmentIntersector.h>
#include <geos/geomgraph/Edge.h>
#include <geos/geomgraph/Node.h>
#include <geos/algorithm/LineIntersector.h>
#include <geos/geom/Coordinate.h>
#include <geos/geom/CoordinateSequence.h>

#ifndef GEOS_DEBUG
#define GEOS_DEBUG 0
#endif

#define DEBUG_INTERSECT 0

#if GEOS_DEBUG || DEBUG_INTERSECT
#include <iostream>
#endif

using namespace std;
using namespace geos::geom;

namespace geos {
namespace geomgraph { // geos.geomgraph
namespace index { // geos.geomgraph.index

using namespace geos::algorithm;

bool
SegmentIntersector::isAdjacentSegments(int i1,int i2)
{
	return abs(i1-i2)==1;
}

void
SegmentIntersector::setBoundaryNodes(vector<Node*> *bdyNodes0,
	vector<Node*> *bdyNodes1)
{
	bdyNodes[0]=bdyNodes0;
	bdyNodes[1]=bdyNodes1;
}

/*
 * @return the proper intersection point, or <code>null</code>
 * if none was found
 */
Coordinate&
SegmentIntersector::getProperIntersectionPoint()
{
	return properIntersectionPoint;
}

bool
SegmentIntersector::hasIntersection()
{
	return hasIntersectionVar;
}

void
SegmentIntersector::setIsDoneIfProperInt(bool idwpi)
{
	isDoneWhenProperInt = idwpi;
}

bool
SegmentIntersector::getIsDone()
{
	return isDone;
}

/*
 * A proper intersection is an intersection which is interior to at least two
 * line segments.  Note that a proper intersection is not necessarily
 * in the interior of the entire Geometry, since another edge may have
 * an endpoint equal to the intersection, which according to SFS semantics
 * can result in the point being on the Boundary of the Geometry.
 */
bool
SegmentIntersector::hasProperIntersection()
{
	return hasProper;
}

/*
 * A proper interior intersection is a proper intersection which is <b>not</b>
 * contained in the set of boundary nodes set for this SegmentIntersector.
 */
bool
SegmentIntersector::hasProperInteriorIntersection()
{
	return hasProperInterior;
}

/*
 * A trivial intersection is an apparent self-intersection which in fact
 * is simply the point shared by adjacent line segments.
 * Note that closed edges require a special check for the point
 * shared by the beginning and end segments.
 */
bool
SegmentIntersector::isTrivialIntersection(Edge *e0,int segIndex0,Edge *e1,int segIndex1)
{
//	if (e0->equals(e1))
	if (e0==e1) {
		if (li->getIntersectionNum()==1) {
			if (isAdjacentSegments(segIndex0,segIndex1))
				return true;
			if (e0->isClosed()) {
				int maxSegIndex=e0->getNumPoints()-1;
				if ((segIndex0==0 && segIndex1==maxSegIndex)
					|| (segIndex1==0 && segIndex0==maxSegIndex)) {
					return true;
				}
			}
		}
	}
	return false;
}

/**
 * This method is called by clients of the EdgeIntersector class to test
 * for and add intersections for two segments of the edges being intersected.
 * Note that clients (such as MonotoneChainEdges) may choose not to intersect
 * certain pairs of segments for efficiency reasons.
 */
void
SegmentIntersector::addIntersections(Edge *e0,int segIndex0,Edge *e1,int segIndex1)
{

#if GEOS_DEBUG
	cerr<<"SegmentIntersector::addIntersections() called"<<endl;
#endif

//	if (e0->equals(e1) && segIndex0==segIndex1) return;
	if (e0==e1 && segIndex0==segIndex1) return;
	numTests++;
	const CoordinateSequence* cl0=e0->getCoordinates();
	const Coordinate& p00=cl0->getAt(segIndex0);
	const Coordinate& p01=cl0->getAt(segIndex0+1);
	const CoordinateSequence* cl1=e1->getCoordinates();
	const Coordinate& p10=cl1->getAt(segIndex1);
	const Coordinate& p11=cl1->getAt(segIndex1+1);
	li->computeIntersection(p00,p01,p10,p11);

	/*
	 * Always record any non-proper intersections.
	 * If includeProper is true, record any proper intersections as well.
	 */
	if (li->hasIntersection()) {
		if (recordIsolated) {
			e0->setIsolated(false);
			e1->setIsolated(false);
		}
		//intersectionFound = true;
		numIntersections++;

		// If the segments are adjacent they have at least one trivial
		// intersection, the shared endpoint.
		// Don't bother adding it if it is the
		// only intersection.
		if (!isTrivialIntersection(e0,segIndex0,e1,segIndex1))
		{
#if GEOS_DEBUG
			cerr<<"SegmentIntersector::addIntersections(): has !TrivialIntersection"<<endl;
#endif // DEBUG_INTERSECT
			hasIntersectionVar=true;
			if (includeProper || !li->isProper()) {
				//Debug.println(li);
				e0->addIntersections(li,segIndex0,0);
				e1->addIntersections(li,segIndex1,1);
#if GEOS_DEBUG
				cerr<<"SegmentIntersector::addIntersections(): includeProper || !li->isProper()"<<endl;
#endif // DEBUG_INTERSECT
			}
			if (li->isProper())
			{
				properIntersectionPoint=li->getIntersection(0);
#if GEOS_DEBUG
				cerr<<"SegmentIntersector::addIntersections(): properIntersectionPoint: "<<properIntersectionPoint.toString()<<endl;
#endif // DEBUG_INTERSECT
				hasProper=true;
				if (isDoneWhenProperInt)
				{
					isDone = true;
				}
				if (!isBoundaryPoint(li,bdyNodes))
					hasProperInterior=true;
			}
			//if (li.isCollinear())
			//hasCollinear = true;
		}
	}
}

/*private*/
bool
SegmentIntersector::isBoundaryPoint(LineIntersector *li,
		vector<Node*> *tstBdyNodes)
{
	if ( ! tstBdyNodes ) return false;

	for(vector<Node*>::iterator i=tstBdyNodes->begin();i<tstBdyNodes->end();i++) {
		Node *node=*i;
		const Coordinate& pt=node->getCoordinate();
		if (li->isIntersection(pt)) return true;
	}
	return false;
}


/*private*/
bool
SegmentIntersector::isBoundaryPoint(LineIntersector *li,
		vector<vector<Node*>*>& tstBdyNodes)
{
	if (isBoundaryPoint(li, tstBdyNodes[0])) return true;
	if (isBoundaryPoint(li, tstBdyNodes[1])) return true;
	return false;
}

} // namespace geos.geomgraph.index
} // namespace geos.geomgraph
} // namespace geos
