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
 * Last port: operation/valid/ConsistentAreaTester.java rev. 1.14 (JTS-1.10)
 *
 **********************************************************************/

#include <geos/operation/valid/ConsistentAreaTester.h>
#include <geos/algorithm/LineIntersector.h>
#include <geos/geomgraph/GeometryGraph.h>
#include <geos/geomgraph/EdgeEnd.h>
#include <geos/geomgraph/EdgeEndStar.h>
#include <geos/geomgraph/Edge.h>
#include <geos/geomgraph/index/SegmentIntersector.h>
#include <geos/geom/Coordinate.h>
#include <geos/operation/relate/RelateNodeGraph.h>
#include <geos/operation/relate/RelateNode.h>
#include <geos/operation/relate/EdgeEndBundle.h>

#include <memory> // unique_ptr
#include <cassert>

using namespace std;
using namespace geos::algorithm;
using namespace geos::geomgraph;
using namespace geos::geom;

namespace geos {
namespace operation { // geos.operation
namespace valid { // geos.operation.valid

ConsistentAreaTester::ConsistentAreaTester(GeometryGraph *newGeomGraph)
	:
	li(),
	geomGraph(newGeomGraph),
	nodeGraph(),
	invalidPoint()
{
}

ConsistentAreaTester::~ConsistentAreaTester()
{
}

Coordinate&
ConsistentAreaTester::getInvalidPoint()
{
	return invalidPoint;
}

bool
ConsistentAreaTester::isNodeConsistentArea()
{
	using geomgraph::index::SegmentIntersector;

	/**
	 * To fully check validity, it is necessary to
	 * compute ALL intersections, including self-intersections within a single edge.
	 */
	unique_ptr<SegmentIntersector> intersector(geomGraph->computeSelfNodes(&li, true, true));
	/**
	* A proper intersection means that the area is not consistent.
	*/
	if (intersector->hasProperIntersection()) {
		invalidPoint=intersector->getProperIntersectionPoint();
		return false;
	}
	nodeGraph.build(geomGraph);
	return isNodeEdgeAreaLabelsConsistent();
}

/*private*/
bool
ConsistentAreaTester::isNodeEdgeAreaLabelsConsistent()
{
	assert(geomGraph);

	map<Coordinate*,Node*,CoordinateLessThen>& nMap=nodeGraph.getNodeMap();
	map<Coordinate*,Node*,CoordinateLessThen>::iterator nodeIt;
	for(nodeIt=nMap.begin();nodeIt!=nMap.end();nodeIt++) {
		relate::RelateNode *node=static_cast<relate::RelateNode*>(nodeIt->second);
		if (!node->getEdges()->isAreaLabelsConsistent(*geomGraph)) {
			invalidPoint=node->getCoordinate();
			return false;
		}
	}
	return true;
}

/*public*/
bool
ConsistentAreaTester::hasDuplicateRings()
{
	map<Coordinate*,Node*,CoordinateLessThen>& nMap=nodeGraph.getNodeMap();
	map<Coordinate*,Node*,CoordinateLessThen>::iterator nodeIt;
	for(nodeIt=nMap.begin(); nodeIt!=nMap.end(); ++nodeIt)
	{
		assert(dynamic_cast<relate::RelateNode*>(nodeIt->second));
		relate::RelateNode *node=static_cast<relate::RelateNode*>(nodeIt->second);
		EdgeEndStar *ees=node->getEdges();
		EdgeEndStar::iterator endIt=ees->end();
		for(EdgeEndStar::iterator it=ees->begin(); it!=endIt; ++it)
		{
			assert(dynamic_cast<relate::EdgeEndBundle*>(*it));
			relate::EdgeEndBundle *eeb=static_cast<relate::EdgeEndBundle*>(*it);
			if (eeb->getEdgeEnds()->size()>1) {
				invalidPoint=eeb->getEdge()->getCoordinate(0);
				return true;
			}
		}
	}
	return false;
}

} // namespace geos.operation.valid
} // namespace geos.operation
} // namespace geos

