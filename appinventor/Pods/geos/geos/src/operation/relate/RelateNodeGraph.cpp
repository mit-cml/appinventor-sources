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
 * Last port: operation/relate/RelateNodeGraph.java rev. 1.11 (JTS-1.10)
 *
 **********************************************************************/

#include <geos/operation/relate/RelateNodeGraph.h>
#include <geos/operation/relate/RelateNodeFactory.h>
#include <geos/operation/relate/EdgeEndBuilder.h>
#include <geos/operation/relate/RelateNode.h>
#include <geos/geomgraph/NodeMap.h>
#include <geos/geomgraph/GeometryGraph.h>
#include <geos/geomgraph/EdgeIntersectionList.h>
#include <geos/geomgraph/Edge.h>
#include <geos/geomgraph/Label.h>
#include <geos/geom/Location.h>

#include <vector>
#include <map>

using namespace std;
using namespace geos::geomgraph;
using namespace geos::geom;

namespace geos {
namespace operation { // geos.operation
namespace relate { // geos.operation.relate

RelateNodeGraph::RelateNodeGraph()
{
	nodes=new NodeMap(RelateNodeFactory::instance());
}

RelateNodeGraph::~RelateNodeGraph() {
	delete nodes;
}

map<Coordinate*,Node*,CoordinateLessThen>&
RelateNodeGraph::getNodeMap()
{
	return nodes->nodeMap;
}

void
RelateNodeGraph::build(GeometryGraph *geomGraph)
{
	// compute nodes for intersections between previously noded edges
	computeIntersectionNodes(geomGraph,0);

	/**
	 * Copy the labelling for the nodes in the parent Geometry.  These override
	 * any labels determined by intersections.
	 */
	copyNodesAndLabels(geomGraph,0);

	/**
	 * Build EdgeEnds for all intersections.
	 */
	EdgeEndBuilder *eeBuilder=new EdgeEndBuilder();
	vector<EdgeEnd*> *eeList=eeBuilder->computeEdgeEnds(geomGraph->getEdges());
	insertEdgeEnds(eeList);
	delete eeBuilder;
	delete eeList;
	//Debug.println("==== NodeList ===");
	//Debug.print(nodes);
}

/**
 * Insert nodes for all intersections on the edges of a Geometry.
 * Label the created nodes the same as the edge label if they do not
 * already have a label.
 * This allows nodes created by either self-intersections or
 * mutual intersections to be labelled.
 * Endpoint nodes will already be labelled from when they were inserted.
 *
 * Precondition: edge intersections have been computed.
 */
void
RelateNodeGraph::computeIntersectionNodes(GeometryGraph *geomGraph,
	int argIndex)
{
	vector<Edge*> *edges=geomGraph->getEdges();
	vector<Edge*>::iterator edgeIt=edges->begin();
	for( ; edgeIt<edges->end(); ++edgeIt)
	{
		Edge *e=*edgeIt;
		int eLoc=e->getLabel().getLocation(argIndex);
		EdgeIntersectionList &eiL=e->getEdgeIntersectionList();
		EdgeIntersectionList::iterator eiIt=eiL.begin();
		EdgeIntersectionList::iterator eiEnd=eiL.end();
		for( ; eiIt!=eiEnd; ++eiIt) {
			EdgeIntersection *ei=*eiIt;
			RelateNode *n=(RelateNode*) nodes->addNode(ei->coord);
			if (eLoc==Location::BOUNDARY)
				n->setLabelBoundary(argIndex);
			else {
				if (n->getLabel().isNull(argIndex))
				  n->setLabel(argIndex,Location::INTERIOR);
			}
		}
	}
}

/**
 * Copy all nodes from an arg geometry into this graph.
 * The node label in the arg geometry overrides any previously computed
 * label for that argIndex.
 * (E.g. a node may be an intersection node with
 * a computed label of BOUNDARY,
 * but in the original arg Geometry it is actually
 * in the interior due to the Boundary Determination Rule)
 */
void
RelateNodeGraph::copyNodesAndLabels(GeometryGraph *geomGraph,int argIndex)
{
	map<Coordinate*,Node*,CoordinateLessThen> &nMap=geomGraph->getNodeMap()->nodeMap;
	map<Coordinate*,Node*,CoordinateLessThen>::iterator nodeIt;
	for(nodeIt=nMap.begin();nodeIt!=nMap.end();nodeIt++) {
		Node *graphNode=nodeIt->second;
		Node *newNode=nodes->addNode(graphNode->getCoordinate());
		newNode->setLabel(argIndex,graphNode->getLabel().getLocation(argIndex));
		//node.print(System.out);
	}
}

void
RelateNodeGraph::insertEdgeEnds(vector<EdgeEnd*> *ee)
{
	for(vector<EdgeEnd*>::iterator i=ee->begin();i<ee->end();i++) {
		EdgeEnd *e=*i;
		nodes->add(e);
	}
}

} // namespace geos.operation.relate
} // namespace geos.operation
} // namespace geos

