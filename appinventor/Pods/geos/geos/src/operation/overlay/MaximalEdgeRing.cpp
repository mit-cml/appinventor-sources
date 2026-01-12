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
 * Last port: operation/overlay/MaximalEdgeRing.java rev. 1.15 (JTS-1.10)
 *
 **********************************************************************/

#include <geos/operation/overlay/MaximalEdgeRing.h>
#include <geos/operation/overlay/MinimalEdgeRing.h>
#include <geos/geomgraph/EdgeRing.h>
#include <geos/geomgraph/DirectedEdge.h>
#include <geos/geomgraph/Node.h>
#include <geos/geomgraph/EdgeEndStar.h>
#include <geos/geomgraph/DirectedEdgeStar.h>

#include <cassert>
#include <vector>

#ifndef GEOS_DEBUG
#define GEOS_DEBUG 0
#endif

#if GEOS_DEBUG
#include <iostream>
#endif

using namespace std;
using namespace geos::geomgraph;
using namespace geos::geom;

namespace geos {
namespace operation { // geos.operation
namespace overlay { // geos.operation.overlay

/*public*/
// CGAlgorithms obsoleted
MaximalEdgeRing::MaximalEdgeRing(DirectedEdge *start,
		const GeometryFactory *geometryFactory)
	// throw(const TopologyException &)
	:
	EdgeRing(start, geometryFactory)
{
	computePoints(start);
	computeRing();
#if GEOS_DEBUG
	cerr << "MaximalEdgeRing[" << this << "] ctor" << endl;
#endif
}

/*public*/
MaximalEdgeRing::~MaximalEdgeRing()
{
#if GEOS_DEBUG
	cerr << "MaximalEdgeRing[" << this << "] dtor" << endl;
#endif
}

/*public*/
DirectedEdge*
MaximalEdgeRing::getNext(DirectedEdge *de)
{
	return de->getNext();
}

/*public*/
void
MaximalEdgeRing::setEdgeRing(DirectedEdge *de,EdgeRing *er)
{
	de->setEdgeRing(er);
}

/*public*/
void
MaximalEdgeRing::linkDirectedEdgesForMinimalEdgeRings()
{
	DirectedEdge* de=startDe;
	do {
		Node* node=de->getNode();
		EdgeEndStar* ees = node->getEdges();

		assert(dynamic_cast<DirectedEdgeStar*>(ees));
		DirectedEdgeStar* des = static_cast<DirectedEdgeStar*>(ees);

		des->linkMinimalDirectedEdges(this);

		de=de->getNext();

	} while (de!=startDe);
}

/*public*/
vector<MinimalEdgeRing*>*
MaximalEdgeRing::buildMinimalRings()
{
	vector<MinimalEdgeRing*> *minEdgeRings=new vector<MinimalEdgeRing*>;
	buildMinimalRings(*minEdgeRings);
	return minEdgeRings;
}

/*public*/
void
MaximalEdgeRing::buildMinimalRings(vector<MinimalEdgeRing*>& minEdgeRings)
{
	DirectedEdge *de=startDe;
	do {
		if(de->getMinEdgeRing()==nullptr) {
			MinimalEdgeRing *minEr=new MinimalEdgeRing(de, geometryFactory);
			minEdgeRings.push_back(minEr);
		}
		de=de->getNext();
	} while(de!=startDe);
}

/*public*/
void
MaximalEdgeRing::buildMinimalRings(vector<EdgeRing*>& minEdgeRings)
{
	DirectedEdge *de=startDe;
	do {
		if(de->getMinEdgeRing()==nullptr) {
			MinimalEdgeRing *minEr=new MinimalEdgeRing(de, geometryFactory);
			minEdgeRings.push_back(minEr);
		}
		de=de->getNext();
	} while(de!=startDe);
}

} // namespace geos.operation.overlay
} // namespace geos.operation
} // namespace geos
