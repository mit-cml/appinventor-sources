/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2011 Sandro Santilli <strk@kbt.io>
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
 * Last port: geomgraph/EdgeIntersectionList.java r428 (JTS-1.12+)
 *
 **********************************************************************/

#include <geos/geomgraph/EdgeIntersectionList.h>
#include <geos/geomgraph/EdgeIntersection.h>
#include <geos/geomgraph/Edge.h>
#include <geos/geomgraph/Label.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/geom/CoordinateArraySequence.h> // shouldn't be using this
#include <geos/geom/Coordinate.h>

#include <sstream>
#include <string>
#include <vector>
#include <set>
#include <utility> // std::pair

#ifndef GEOS_DEBUG
#define GEOS_DEBUG 0
#endif

#if GEOS_DEBUG || GEOS_DEBUG_INTERSECT
#include <iostream>
#endif

using namespace std;
using namespace geos::geom;

namespace geos {
namespace geomgraph { // geos.geomgraph

EdgeIntersectionList::EdgeIntersectionList(Edge *newEdge):
	edge(newEdge)
{
}

EdgeIntersectionList::~EdgeIntersectionList()
{
	for (EdgeIntersectionList::iterator it=nodeMap.begin(),
		endIt=nodeMap.end();
		it!=endIt; ++it)
	{
		delete *it;
	}
}

EdgeIntersection*
EdgeIntersectionList::add(const Coordinate& coord,
	int segmentIndex, double dist)
{
	EdgeIntersection *eiNew=new EdgeIntersection(coord, segmentIndex, dist);

	pair<EdgeIntersectionList::iterator, bool> p = nodeMap.insert(eiNew);
	if ( p.second ) { // new EdgeIntersection inserted
		return eiNew;
	} else {
		delete eiNew;
		return *(p.first);
	}
}

bool
EdgeIntersectionList::isEmpty() const
{
	return nodeMap.empty();
}

bool
EdgeIntersectionList::isIntersection(const Coordinate& pt) const
{
	EdgeIntersectionList::const_iterator
		it=nodeMap.begin(),
		endIt=nodeMap.end();

	for (; it!=endIt; ++it)
	{
		EdgeIntersection *ei=*it;
		if (ei->coord==pt) return true;
	}
	return false;
}

void
EdgeIntersectionList::addEndpoints()
{
	int maxSegIndex=edge->getNumPoints()-1;
	add(edge->pts->getAt(0), 0, 0.0);
	add(edge->pts->getAt(maxSegIndex), maxSegIndex, 0.0);
}

void
EdgeIntersectionList::addSplitEdges(vector<Edge*> *edgeList)
{
	// ensure that the list has entries for the first and last point
	// of the edge
	addEndpoints();

	EdgeIntersectionList::iterator it=nodeMap.begin();

	// there should always be at least two entries in the list
	EdgeIntersection *eiPrev=*it;
	++it;

	while (it!=nodeMap.end()) {
		EdgeIntersection *ei=*it;
		Edge *newEdge=createSplitEdge(eiPrev,ei);
		edgeList->push_back(newEdge);
		eiPrev=ei;
		it++;
	}
}

Edge *
EdgeIntersectionList::createSplitEdge(EdgeIntersection *ei0,
	EdgeIntersection *ei1)
{
#if GEOS_DEBUG
	cerr<<"["<<this<<"] EdgeIntersectionList::createSplitEdge()"<<endl;
#endif // GEOS_DEBUG
	int npts=ei1->segmentIndex-ei0->segmentIndex+2;

	const Coordinate& lastSegStartPt=edge->pts->getAt(ei1->segmentIndex);

	// if the last intersection point is not equal to the its segment
	// start pt, add it to the points list as well.
	// (This check is needed because the distance metric is not totally
	// reliable!). The check for point equality is 2D only - Z values
	// are ignored
	bool useIntPt1=ei1->dist>0.0 || !ei1->coord.equals2D(lastSegStartPt);

	if (!useIntPt1) --npts;

#if GEOS_DEBUG
	cerr<<"    npts:"<<npts<<endl;
#endif // GEOS_DEBUG

	vector<Coordinate> *vc=new vector<Coordinate>();
	vc->reserve(npts);

	vc->push_back(ei0->coord);
	for(int i=ei0->segmentIndex+1; i<=ei1->segmentIndex;i++)
	{
		if ( ! useIntPt1 && ei1->segmentIndex == i )
		{
			vc->push_back(ei1->coord);
		}
		else
		{
			vc->push_back(edge->pts->getAt(i));
		}
	}

	if (useIntPt1)
	{
		vc->push_back(ei1->coord);
	}

	CoordinateSequence* pts=new CoordinateArraySequence(vc);

	return new Edge(pts, edge->getLabel());
}

string
EdgeIntersectionList::print() const
{
	std::stringstream ss;
  ss << *this;
  return ss.str();
}

std::ostream&
operator<< (std::ostream&os, const EdgeIntersectionList& e)
{
  os << "Intersections:" << std::endl;
  EdgeIntersectionList::const_iterator it=e.begin(), endIt=e.end();
  for (; it!=endIt; ++it) {
    EdgeIntersection *ei=*it;
    os << *ei << endl;
  }
  return os;
}

} // namespace geos.geomgraph
} // namespace

