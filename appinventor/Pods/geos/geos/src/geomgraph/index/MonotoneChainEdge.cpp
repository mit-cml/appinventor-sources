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
 **********************************************************************/

#include <cassert>
#include <vector>

#include <geos/geomgraph/Edge.h>
#include <geos/geomgraph/index/MonotoneChainEdge.h>
#include <geos/geomgraph/index/MonotoneChainIndexer.h>
#include <geos/geomgraph/index/SegmentIntersector.h>
#include <geos/geom/Coordinate.h>
#include <geos/geom/CoordinateSequence.h>

using namespace std;
using namespace geos::geom;

namespace geos {
namespace geomgraph { // geos.geomgraph
namespace index { // geos.geomgraph.index

/**
 * MonotoneChains are a way of partitioning the segments of an edge to
 * allow for fast searching of intersections.
 * They have the following properties:
 *
 *  -  the segments within a monotone chain will never intersect each other
 *  -  the envelope of any contiguous subset of the segments in a monotone
 *     chain is simply the envelope of the endpoints of the subset.
 *
 * Property 1 means that there is no need to test pairs of segments from
 * within the same monotone chain for intersection.
 * Property 2 allows binary search to be used to find the intersection
 * points of two monotone chains.
 * For many types of real-world data, these properties eliminate a large
 * number of segment comparisons, producing substantial speed gains.
 * @version 1.1
 */

MonotoneChainEdge::~MonotoneChainEdge()
{
}

MonotoneChainEdge::MonotoneChainEdge(Edge *newE):
	e(newE),
	pts(newE->getCoordinates())
{
	assert(e);
	MonotoneChainIndexer mcb;
	mcb.getChainStartIndices(pts, startIndex);
	assert(e);
}

const CoordinateSequence*
MonotoneChainEdge::getCoordinates()
{
	assert(pts);
	return pts;
}

vector<int> &
MonotoneChainEdge::getStartIndexes()
{
	return startIndex;
}

double
MonotoneChainEdge::getMinX(int chainIndex)
{
	double x1=pts->getAt(startIndex[chainIndex]).x;
	double x2=pts->getAt(startIndex[chainIndex+1]).x;
	return x1<x2?x1:x2;
}

double
MonotoneChainEdge::getMaxX(int chainIndex)
{
	double x1=pts->getAt(startIndex[chainIndex]).x;
	double x2=pts->getAt(startIndex[chainIndex+1]).x;
	return x1>x2?x1:x2;
}

void
MonotoneChainEdge::computeIntersects(const MonotoneChainEdge &mce,
	SegmentIntersector &si)
{
	size_t I=startIndex.size()-1;
	size_t J=mce.startIndex.size()-1;
	for(size_t i=0; i<I; ++i)
	{
		for(size_t j=0; j<J; ++j)
		{
			computeIntersectsForChain(static_cast<int>(i),mce,static_cast<int>(j),si);
		}
	}
}

void
MonotoneChainEdge::computeIntersectsForChain(int chainIndex0,
	const MonotoneChainEdge &mce, int chainIndex1,
	SegmentIntersector &si)
{
	computeIntersectsForChain(startIndex[chainIndex0],
		startIndex[chainIndex0+1], mce,
		mce.startIndex[chainIndex1],
		mce.startIndex[chainIndex1+1],
		si);
}

void
MonotoneChainEdge::computeIntersectsForChain(int start0, int end0,
	const MonotoneChainEdge &mce, int start1, int end1,
	SegmentIntersector &ei)
{
	// terminating condition for the recursion
	if (end0-start0==1 && end1-start1==1) {
		ei.addIntersections(e, start0, mce.e, start1);
		return;
	}

	const Coordinate& p00=pts->getAt(start0);
	const Coordinate& p01=pts->getAt(end0);
	const Coordinate& p10=mce.pts->getAt(start1);
	const Coordinate& p11=mce.pts->getAt(end1);

	// nothing to do if the envelopes of these chains don't overlap
	env1.init(p00,p01);
	env2.init(p10,p11);

	if (!env1.intersects(&env2)) return;
	// the chains overlap, so split each in half and iterate
	// (binary search)
	int mid0=(start0+end0)/2;
	int mid1=(start1+end1)/2;

	// Assert: mid != start or end
	// (since we checked above for end - start <= 1)
	// check terminating conditions before recursing
	if (start0<mid0) {
		if (start1<mid1)
			computeIntersectsForChain(start0, mid0, mce,
				start1, mid1, ei);
		if (mid1<end1)
			computeIntersectsForChain(start0, mid0, mce,
				mid1, end1, ei);
	}
	if (mid0<end0) {
		if (start1<mid1)
			computeIntersectsForChain(mid0, end0, mce,
				start1, mid1, ei);
		if (mid1<end1)
			computeIntersectsForChain(mid0, end0, mce,
				mid1, end1, ei);
	}
}

} // namespace geos.geomgraph.index
} // namespace geos.geomgraph
} // namespace geos
