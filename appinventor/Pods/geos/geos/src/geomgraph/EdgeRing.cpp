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
 * Last port: geomgraph/EdgeRing.java r428 (JTS-1.12+)
 *
 **********************************************************************/

#include <geos/util/Assert.h>
#include <geos/util/TopologyException.h>
#include <geos/algorithm/CGAlgorithms.h>
#include <geos/geomgraph/EdgeRing.h>
#include <geos/geomgraph/DirectedEdge.h>
#include <geos/geomgraph/DirectedEdgeStar.h>
#include <geos/geomgraph/Edge.h>
#include <geos/geomgraph/Node.h>
#include <geos/geomgraph/Label.h>
#include <geos/geomgraph/Position.h>
#include <geos/geom/CoordinateSequenceFactory.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/geom/GeometryFactory.h>
#include <geos/geom/LinearRing.h>
#include <geos/geom/Location.h>
#include <geos/geom/Envelope.h>

#include <vector>
#include <cassert>
#include <iostream> // for operator<<

#ifndef GEOS_DEBUG
#define GEOS_DEBUG 0
#endif

#if GEOS_DEBUG
#include <iostream>
#endif

using namespace std;
using namespace geos::algorithm;
using namespace geos::geom;

namespace geos {
namespace geomgraph { // geos.geomgraph

EdgeRing::EdgeRing(DirectedEdge *newStart,
		const GeometryFactory *newGeometryFactory)
	:
        startDe(newStart),
        geometryFactory(newGeometryFactory),
	holes(),
        maxNodeDegree(-1),
	edges(),
	pts(newGeometryFactory->getCoordinateSequenceFactory()->create()),
        label(Location::UNDEF), // new Label(Location::UNDEF)),
        ring(nullptr),
        isHoleVar(false),
        shell(nullptr)
{
	/*
	 * Commented out to fix different polymorphism in C++ (from Java)
	 * Make sure these calls are made by derived classes !
	 */
	//computePoints(start);
	//computeRing();
#if GEOS_DEBUG
	cerr << "EdgeRing[" << this << "] ctor" << endl;
#endif
	testInvariant();
}

EdgeRing::~EdgeRing()
{
	testInvariant();

	/*
	 * If we constructed a ring, we did by transferring
	 * ownership of the CoordinateSequence, so it will be
	 * destroyed by `ring' dtor and we must not destroy
	 * it twice.
	 */
	if ( ring == nullptr )
	{
		delete pts;
	}
	else
	{
		delete ring;
	}

	for(size_t i=0, n=holes.size(); i<n; ++i)
	{
		delete holes[i];
	}

#if GEOS_DEBUG
	cerr << "EdgeRing[" << this << "] dtor" << endl;
#endif
}

bool
EdgeRing::isIsolated()
{
	testInvariant();
	return (label.getGeometryCount()==1);
}

bool
EdgeRing::isHole()
{
	testInvariant();

	// We can't tell if this is an hole
	// unless we computed the ring
	// see computeRing()
	assert(ring);

	return isHoleVar;
}


LinearRing*
EdgeRing::getLinearRing()
{
	testInvariant();
//	return new LinearRing(*ring);
	return ring;
}

Label&
EdgeRing::getLabel()
{
	testInvariant();
	return label;
}

bool
EdgeRing::isShell()
{
	testInvariant();
	return (shell==nullptr);
}

EdgeRing*
EdgeRing::getShell()
{
	testInvariant();
	return shell;
}

void
EdgeRing::setShell(EdgeRing *newShell)
{
	shell=newShell;
	if (shell!=nullptr) shell->addHole(this);
	testInvariant();
}

void
EdgeRing::addHole(EdgeRing *edgeRing)
{
	holes.push_back(edgeRing);
	testInvariant();
}

/*public*/
Polygon*
EdgeRing::toPolygon(const GeometryFactory* geometryFactory)
{
	testInvariant();

	size_t nholes=holes.size();
	vector<Geometry *> *holeLR=new vector<Geometry *>(nholes);
	for (size_t i=0; i<nholes; ++i)
	{
		Geometry *hole=holes[i]->getLinearRing()->clone();
        	(*holeLR)[i]=hole;
	}

	// We don't use "clone" here because
	// GeometryFactory::createPolygon really
	// wants a LinearRing
	//
	LinearRing *shellLR=new LinearRing(*(getLinearRing()));
	return geometryFactory->createPolygon(shellLR, holeLR);
}

/*public*/
void
EdgeRing::computeRing()
{
	testInvariant();

	if (ring!=nullptr) return;   // don't compute more than once
	ring=geometryFactory->createLinearRing(pts);
	isHoleVar=CGAlgorithms::isCCW(pts);

	testInvariant();

}

/*public*/
vector<DirectedEdge*>&
EdgeRing::getEdges()
{
	testInvariant();

	return edges;
}

/*protected*/
void
EdgeRing::computePoints(DirectedEdge *newStart)
	// throw(const TopologyException &)
{
	startDe=newStart;
	DirectedEdge *de=newStart;
	bool isFirstEdge=true;
	do {
		//util::Assert::isTrue(de!=NULL,"EdgeRing::computePoints: found null Directed Edge");
		//assert(de!=NULL); // EdgeRing::computePoints: found null Directed Edge
		if(de==nullptr)
			throw util::TopologyException(
				"EdgeRing::computePoints: found null Directed Edge");

		if (de->getEdgeRing()==this)
			throw util::TopologyException(
				"Directed Edge visited twice during ring-building",
				de->getCoordinate());

		edges.push_back(de);
		const Label& deLabel = de->getLabel();
		assert(deLabel.isArea());
		mergeLabel(deLabel);
		addPoints(de->getEdge(),de->isForward(),isFirstEdge);
		isFirstEdge=false;
		setEdgeRing(de,this);
		de=getNext(de);
	} while (de!=startDe);

	testInvariant();

}

/*public*/
int
EdgeRing::getMaxNodeDegree()
{

	testInvariant();

	if (maxNodeDegree<0) computeMaxNodeDegree();
	return maxNodeDegree;
}

/*private*/
void
EdgeRing::computeMaxNodeDegree()
{
	maxNodeDegree=0;
	DirectedEdge *de=startDe;
	do {
		Node *node=de->getNode();
		EdgeEndStar* ees = node->getEdges();
		assert(dynamic_cast<DirectedEdgeStar*>(ees));
		DirectedEdgeStar* des = static_cast<DirectedEdgeStar*>(ees);
		int degree=des->getOutgoingDegree(this);
		if (degree>maxNodeDegree) maxNodeDegree=degree;
		de=getNext(de);
	} while (de!=startDe);
	maxNodeDegree *= 2;

	testInvariant();

}

/*public*/
void
EdgeRing::setInResult()
{
	DirectedEdge *de=startDe;
	do {
		de->getEdge()->setInResult(true);
		de=de->getNext();
	} while (de!=startDe);

	testInvariant();

}

/*protected*/
void
EdgeRing::mergeLabel(const Label& deLabel)
{
	mergeLabel(deLabel, 0);
	mergeLabel(deLabel, 1);

	testInvariant();

}

/*protected*/
void
EdgeRing::mergeLabel(const Label& deLabel, int geomIndex)
{

	testInvariant();

	int loc=deLabel.getLocation(geomIndex, Position::RIGHT);
	// no information to be had from this label
	if (loc==Location::UNDEF) return;

	// if there is no current RHS value, set it
	if (label.getLocation(geomIndex)==Location::UNDEF) {
		label.setLocation(geomIndex,loc);
		return;
	}
}

/*protected*/
void
EdgeRing::addPoints(Edge *edge, bool isForward, bool isFirstEdge)
{
	// EdgeRing::addPoints: can't add points after LinearRing construction
	assert(ring==nullptr);

	assert(edge);
	const CoordinateSequence* edgePts=edge->getCoordinates();

	assert(edgePts);
	size_t numEdgePts=edgePts->getSize();

	assert(pts);

	if (isForward) {
		size_t startIndex=1;
		if (isFirstEdge) startIndex=0;
		for (size_t i=startIndex; i<numEdgePts; ++i)
		{
			pts->add(edgePts->getAt(i));
		}
	}

	else { // is backward
		size_t startIndex=numEdgePts-1;
		if (isFirstEdge) startIndex=numEdgePts;
		//for (int i=startIndex;i>=0;i--)
		for (size_t i=startIndex; i>0; --i)
		{
			pts->add(edgePts->getAt(i-1));
		}
	}

	testInvariant();

}

/*public*/
bool
EdgeRing::containsPoint(const Coordinate& p)
{

	testInvariant();

	assert(ring);

	const Envelope* env=ring->getEnvelopeInternal();
	assert(env);
	if ( ! env->contains(p) ) return false;

	if ( ! CGAlgorithms::isPointInRing(p, ring->getCoordinatesRO()) )
		return false;

	for (vector<EdgeRing*>::iterator i=holes.begin(); i<holes.end(); ++i)
	{
		EdgeRing *hole=*i;
		assert(hole);
		if (hole->containsPoint(p))
		{
			return false;
		}
	}
	return true;
}

std::ostream& operator<< (std::ostream& os, const EdgeRing& er)
{
	os << "EdgeRing[" << &er << "]: "
	   << std::endl
	   << "Points: " << er.pts
	   << std::endl;
	return os;
}

} // namespace geos.geomgraph
} // namespace geos

