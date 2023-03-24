/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2006      Refractions Research Inc.
 * Copyright (C) 2001-2002 Vivid Solutions Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: noding/SegmentNodeList.java rev. 1.8 (JTS-1.10)
 *
 **********************************************************************/

#include <cassert>
#include <set>

#include <geos/profiler.h>
#include <geos/util/GEOSException.h>
#include <geos/noding/SegmentNodeList.h>
#include <geos/noding/NodedSegmentString.h>
#include <geos/noding/SegmentString.h> // for use
#include <geos/geom/Coordinate.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/geom/CoordinateArraySequence.h> // FIXME: should we really be using this ?

#ifndef GEOS_DEBUG
#define GEOS_DEBUG 0
#endif

#ifdef GEOS_DEBUG
#include <iostream>
#endif

//using namespace std;
using namespace geos::geom;

namespace geos {
namespace noding { // geos.noding

#if PROFILE
static Profiler *profiler = Profiler::instance();
#endif


SegmentNodeList::~SegmentNodeList()
{
	std::set<SegmentNode *, SegmentNodeLT>::iterator it=nodeMap.begin();
	for(; it!=nodeMap.end(); it++)
	{
		delete *it;
	}
}

SegmentNode*
SegmentNodeList::add(const Coordinate& intPt, size_t segmentIndex)
{
	SegmentNode *eiNew=new SegmentNode(edge, intPt, static_cast<unsigned int>(segmentIndex),
			edge.getSegmentOctant(static_cast<unsigned int>(segmentIndex)));

	std::pair<SegmentNodeList::iterator,bool> p = nodeMap.insert(eiNew);
	if ( p.second ) { // new SegmentNode inserted
		return eiNew;
	} else {

		// sanity check
		assert(eiNew->coord.equals2D(intPt));

		delete eiNew;
		return *(p.first);
	}
}

void SegmentNodeList::addEndpoints()
{
	int maxSegIndex = edge.size() - 1;
	add(&(edge.getCoordinate(0)), 0);
	add(&(edge.getCoordinate(maxSegIndex)), maxSegIndex);
}

/* private */
void
SegmentNodeList::addCollapsedNodes()
{
	std::vector<size_t> collapsedVertexIndexes;

	findCollapsesFromInsertedNodes(collapsedVertexIndexes);
	findCollapsesFromExistingVertices(collapsedVertexIndexes);

	// node the collapses
	for (std::vector<size_t>::iterator
		i=collapsedVertexIndexes.begin(),
			e=collapsedVertexIndexes.end();
		i != e; ++i)
	{
		auto vertexIndex = static_cast<unsigned int>(*i);
		add(edge.getCoordinate(vertexIndex), vertexIndex);
	}
}


/* private */
void
SegmentNodeList::findCollapsesFromExistingVertices(
			std::vector<size_t>& collapsedVertexIndexes)
{
	if ( edge.size() < 2 ) return; // or we'll never exit the loop below

	for (size_t i=0, n=edge.size()-2; i<n; ++i)
	{
		const Coordinate& p0 = edge.getCoordinate(static_cast<unsigned int>(i));
		const Coordinate& p2 = edge.getCoordinate(static_cast<unsigned int>(i + 2));
		if (p0.equals2D(p2)) {
			// add base of collapse as node
			collapsedVertexIndexes.push_back(i + 1);
		}
	}
}

/* private */
void
SegmentNodeList::findCollapsesFromInsertedNodes(
		std::vector<size_t>& collapsedVertexIndexes)
{
	size_t collapsedVertexIndex;

	// there should always be at least two entries in the list,
	// since the endpoints are nodes
	iterator it = begin();
	SegmentNode* eiPrev = *it;
	++it;
	for(iterator itEnd=end(); it!=itEnd; ++it)
	{
		SegmentNode *ei=*it;
      		bool isCollapsed = findCollapseIndex(*eiPrev, *ei,
				collapsedVertexIndex);
		if (isCollapsed)
			collapsedVertexIndexes.push_back(collapsedVertexIndex);

		eiPrev = ei;
	}
}

/* private */
bool
SegmentNodeList::findCollapseIndex(SegmentNode& ei0, SegmentNode& ei1,
		size_t& collapsedVertexIndex)
{
	// only looking for equal nodes
	if (! ei0.coord.equals2D(ei1.coord)) return false;

	int numVerticesBetween = ei1.segmentIndex - ei0.segmentIndex;
	if (! ei1.isInterior()) {
		numVerticesBetween--;
	}

	// if there is a single vertex between the two equal nodes,
	// this is a collapse
	if (numVerticesBetween == 1) {
		collapsedVertexIndex = ei0.segmentIndex + 1;
		return true;
	}
	return false;
}


/* public */
void
SegmentNodeList::addSplitEdges(std::vector<SegmentString*>& edgeList)
{

	// testingOnly
#if GEOS_DEBUG
	std::cerr<<__FUNCTION__<<" entered"<<std::endl;
	std::vector<SegmentString*> testingSplitEdges;
#endif

	// ensure that the list has entries for the first and last
	// point of the edge
	addEndpoints();
	addCollapsedNodes();

	// there should always be at least two entries in the list
	// since the endpoints are nodes
	iterator it=begin();
	SegmentNode *eiPrev=*it;
	assert(eiPrev);
	it++;
	for(iterator itEnd=end(); it!=itEnd; ++it)
	{
		SegmentNode *ei=*it;
		assert(ei);

		if ( ! ei->compareTo(*eiPrev) ) continue;

		SegmentString *newEdge=createSplitEdge(eiPrev, ei);
		edgeList.push_back(newEdge);
#if GEOS_DEBUG
		testingSplitEdges.push_back(newEdge);
#endif
		eiPrev = ei;
	}
#if GEOS_DEBUG
	std::cerr<<__FUNCTION__<<" finished, now checking correctness"<<std::endl;
	checkSplitEdgesCorrectness(testingSplitEdges);
#endif
}

/*private*/
void
SegmentNodeList::checkSplitEdgesCorrectness(std::vector<SegmentString*>& splitEdges)
{
	const CoordinateSequence *edgePts=edge.getCoordinates();
	assert(edgePts);

	// check that first and last points of split edges
	// are same as endpoints of edge
	SegmentString *split0=splitEdges[0];
	assert(split0);

	const Coordinate& pt0=split0->getCoordinate(0);
	if (!(pt0==edgePts->getAt(0)))
		throw util::GEOSException("bad split edge start point at " + pt0.toString());

	SegmentString *splitn=splitEdges[splitEdges.size()-1];
	assert(splitn);

	const CoordinateSequence *splitnPts=splitn->getCoordinates();
	assert(splitnPts);

	const Coordinate &ptn=splitnPts->getAt(splitnPts->getSize()-1);
	if (!(ptn==edgePts->getAt(edgePts->getSize()-1)))
		throw util::GEOSException("bad split edge end point at " + ptn.toString());
}

/*private*/
SegmentString*
SegmentNodeList::createSplitEdge(SegmentNode *ei0, SegmentNode *ei1)
{
	assert(ei0);
	assert(ei1);

	size_t npts = ei1->segmentIndex - ei0->segmentIndex + 2;

	const Coordinate &lastSegStartPt=edge.getCoordinate(ei1->segmentIndex);

	// if the last intersection point is not equal to the its
	// segment start pt, add it to the points list as well.
	// (This check is needed because the distance metric is not
	// totally reliable!)

	// The check for point equality is 2D only - Z values are ignored

	// Added check for npts being == 2 as in that case NOT using second point
	// would mean creating a SegmentString with a single point
	// FIXME: check with mbdavis about this, ie: is it a bug in the caller ?
	//
	bool useIntPt1 = npts == 2 || (ei1->isInterior() || ! ei1->coord.equals2D(lastSegStartPt));

	if (! useIntPt1) {
		npts--;
	}

	CoordinateSequence *pts = new CoordinateArraySequence(npts);
	size_t ipt = 0;
	pts->setAt(ei0->coord, ipt++);
	for (size_t i=ei0->segmentIndex+1; i<=ei1->segmentIndex; i++)
	{
		pts->setAt(edge.getCoordinate(static_cast<unsigned int>(i)),ipt++);
	}
	if (useIntPt1) 	pts->setAt(ei1->coord, ipt++);

	// SegmentString takes ownership of CoordinateList 'pts'
	SegmentString *ret = new NodedSegmentString(pts, edge.getData());

#if GEOS_DEBUG
	std::cerr<<" SegmentString created"<<std::endl;
#endif

	return ret;
}

std::ostream&
operator<< (std::ostream& os, const SegmentNodeList& nlist)
{
	os << "Intersections: (" << nlist.nodeMap.size() << "):" << std::endl;

	std::set<SegmentNode*,SegmentNodeLT>::const_iterator
			it = nlist.nodeMap.begin(),
			itEnd = nlist.nodeMap.end();

	for(; it!=itEnd; it++)
	{
		SegmentNode *ei=*it;
		os << " " << *ei;
	}
	return os;
}

} // namespace geos.noding
} // namespace geos

