/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2012 Excensus LLC.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: triangulate/quadedge/QuadEdgeSubdivision.java r524
 *
 **********************************************************************/
#include <geos/triangulate/quadedge/QuadEdgeSubdivision.h>

#include <algorithm>
#include <vector>
#include <set>
#include <iostream>

#include <geos/geom/Polygon.h>
#include <geos/geom/LineSegment.h>
#include <geos/geom/LineString.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/geom/CoordinateArraySequence.h>
#include <geos/geom/CoordinateSequenceFactory.h>
#include <geos/geom/CoordinateArraySequenceFactory.h>
#include <geos/geom/CoordinateList.h>
#include <geos/geom/GeometryCollection.h>
#include <geos/geom/GeometryFactory.h>
#include <geos/util/IllegalArgumentException.h>
#include <geos/util/GEOSException.h>
#include <geos/triangulate/quadedge/QuadEdge.h>
#include <geos/triangulate/quadedge/QuadEdgeLocator.h>
#include <geos/triangulate/quadedge/LastFoundQuadEdgeLocator.h>
#include <geos/triangulate/quadedge/LocateFailureException.h>
#include <geos/triangulate/quadedge/TriangleVisitor.h>
#include <geos/geom/Triangle.h>


using namespace geos::geom;
using namespace std;
namespace geos {
namespace triangulate { //geos.triangulate
namespace quadedge { //geos.triangulate.quadedge

void
QuadEdgeSubdivision::getTriangleEdges(const QuadEdge &startQE,
        const QuadEdge* triEdge[3])
{
    triEdge[0] = &startQE;
    triEdge[1] = &triEdge[0]->lNext();
    triEdge[2] = &triEdge[1]->lNext();
    if (&triEdge[2]->lNext() != triEdge[0]) {
        throw new
            util::IllegalArgumentException("Edges do not form a triangle");
    }
}

QuadEdgeSubdivision::QuadEdgeSubdivision(const geom::Envelope &env, double tolerance) :
        tolerance(tolerance),
        locator(new LastFoundQuadEdgeLocator(this))
{
    edgeCoincidenceTolerance = tolerance / EDGE_COINCIDENCE_TOL_FACTOR;
    createFrame(env);
    initSubdiv(startingEdges);
    quadEdges.push_back(startingEdges[0]);
    createdEdges.push_back(startingEdges[0]);
    quadEdges.push_back(startingEdges[1]);
    createdEdges.push_back(startingEdges[1]);
    quadEdges.push_back(startingEdges[2]);
    createdEdges.push_back(startingEdges[2]);
}

QuadEdgeSubdivision::~QuadEdgeSubdivision()
{
    for(QuadEdgeList::iterator iter=createdEdges.begin(); iter!=createdEdges.end(); ++iter)
    {
        (*iter)->free();
        delete *iter;
    }
}

void
QuadEdgeSubdivision::createFrame(const geom::Envelope &env)
{
    double deltaX = env.getWidth();
    double deltaY = env.getHeight();
    double offset = 0.0;
    if (deltaX > deltaY) {
        offset = deltaX * 10.0;
    } else {
        offset = deltaY * 10.0;
    }

    frameVertex[0] = Vertex((env.getMaxX() + env.getMinX()) / 2.0, env
            .getMaxY() + offset);
    frameVertex[1] = Vertex(env.getMinX() - offset, env.getMinY() - offset);
    frameVertex[2] = Vertex(env.getMaxX() + offset, env.getMinY() - offset);

    frameEnv = Envelope(frameVertex[0].getCoordinate(), frameVertex[1]
            .getCoordinate());
    frameEnv.expandToInclude(frameVertex[2].getCoordinate());
}
void
QuadEdgeSubdivision::initSubdiv(QuadEdge* initEdges[3])
{
    std::unique_ptr<QuadEdge> tmp_ptr;
    // build initial subdivision from frame
    tmp_ptr = QuadEdge::makeEdge(frameVertex[0], frameVertex[1]);
    initEdges[0] = tmp_ptr.get();
    tmp_ptr.release();


    tmp_ptr = QuadEdge::makeEdge(frameVertex[1], frameVertex[2]);
    initEdges[1] = tmp_ptr.get();
    tmp_ptr.release();

    QuadEdge::splice(initEdges[0]->sym(), *initEdges[1]);

    tmp_ptr = QuadEdge::makeEdge(frameVertex[2], frameVertex[0]);
    initEdges[2] = tmp_ptr.get();
    tmp_ptr.release();

    QuadEdge::splice(initEdges[1]->sym(), *initEdges[2]);
    QuadEdge::splice(initEdges[2]->sym(), *initEdges[0]);
}

QuadEdge&
QuadEdgeSubdivision::makeEdge(const Vertex &o, const Vertex &d)
{
    std::unique_ptr<QuadEdge> q0 = QuadEdge::makeEdge(o, d);
    QuadEdge *q0_ptr = q0.get();
    q0.release();

    createdEdges.push_back(q0_ptr);
    quadEdges.push_back(q0_ptr);
    return *q0_ptr;
}

QuadEdge&
QuadEdgeSubdivision::connect(QuadEdge &a, QuadEdge &b)
{
    std::unique_ptr<QuadEdge> q0 = QuadEdge::connect(a, b);
    QuadEdge *q0_ptr = q0.get();
    q0.release();

    createdEdges.push_back(q0_ptr);
    quadEdges.push_back(q0_ptr);
    return *q0_ptr;
}

void
QuadEdgeSubdivision::remove(QuadEdge &e)
{
    QuadEdge::splice(e, e.oPrev());
    QuadEdge::splice(e.sym(), e.sym().oPrev());

    // this is inefficient on a std::vector, but this method should be called infrequently
    quadEdges.erase(std::remove(quadEdges.begin(), quadEdges.end(), &e), quadEdges.end());

    //mark these edges as removed
    e.remove();

}

QuadEdge*
QuadEdgeSubdivision::locateFromEdge(const Vertex &v,
        const QuadEdge &startEdge) const
{
    ::geos::ignore_unused_variable_warning(startEdge);

    int iter = 0;
    int maxIter = static_cast<int>(quadEdges.size());

    QuadEdge *e = startingEdges[0];

    for (;;)
    {
        ++iter;
        /**
         * So far it has always been the case that failure to locate indicates an
         * invalid subdivision. So just fail completely. (An alternative would be
         * to perform an exhaustive search for the containing triangle, but this
         * would mask errors in the subdivision topology)
         *
         * This can also happen if two vertices are located very close together,
         * since the orientation predicates may experience precision failures.
         */
        if (iter > maxIter) {
            throw LocateFailureException("");
        }

        if ((v.equals(e->orig())) || (v.equals(e->dest()))) {
            break;
        } else if (v.rightOf(*e)) {
            e = &e->sym();
        } else if (!v.rightOf(e->oNext())) {
            e = &e->oNext();
        } else if (!v.rightOf(e->dPrev())) {
            e = &e->dPrev();
        } else {
            // on edge or in triangle containing edge
            break;
        }
    }
    return e;
}

QuadEdge*
QuadEdgeSubdivision::locate(const Coordinate &p0, const Coordinate &p1)
{
    // find an edge containing one of the points
    QuadEdge *e = locator->locate(Vertex(p0));
    if (e == nullptr)
        return nullptr;

    // normalize so that p0 is origin of base edge
    QuadEdge *base = e;
    if (e->dest().getCoordinate().equals2D(p0))
        base = &e->sym();
    // check all edges around origin of base edge
    QuadEdge *locEdge = base;
    do {
        if (locEdge->dest().getCoordinate().equals2D(p1))
            return locEdge;
        locEdge = &locEdge->oNext();
    } while (locEdge != base);
    return nullptr;
}

QuadEdge&
QuadEdgeSubdivision::insertSite(const Vertex &v)
{
    QuadEdge *e = locate(v);

    if ((v.equals(e->orig(), tolerance)) || (v.equals(e->dest(), tolerance))) {
        return *e; // point already in subdivision.
    }

    // Connect the new point to the vertices of the containing
    // triangle (or quadrilateral, if the new point fell on an
    // existing edge.)
    QuadEdge *base = &makeEdge(e->orig(), v);
    QuadEdge::splice(*base, *e);
    QuadEdge *startEdge = base;
    do {
        base = &connect(*e, base->sym());
        e = &base->oPrev();
    } while (&e->lNext() != startEdge);

    return *startEdge;
}

bool
QuadEdgeSubdivision::isFrameEdge(const QuadEdge &e) const
{
    if (isFrameVertex(e.orig()) || isFrameVertex(e.dest()))
        return true;
    return false;
}

bool
QuadEdgeSubdivision::isFrameBorderEdge(const QuadEdge &e) const
{
    // check other vertex of triangle to left of edge
    Vertex vLeftTriOther = e.lNext().dest();
    if (isFrameVertex(vLeftTriOther))
        return true;
    // check other vertex of triangle to right of edge
    Vertex vRightTriOther = e.sym().lNext().dest();
    if (isFrameVertex(vRightTriOther))
        return true;

    return false;
}

bool
QuadEdgeSubdivision::isFrameVertex(const Vertex &v) const
{
    if (v.equals(frameVertex[0]))
        return true;
    if (v.equals(frameVertex[1]))
        return true;
    if (v.equals(frameVertex[2]))
        return true;
    return false;
}

bool
QuadEdgeSubdivision::isOnEdge(const QuadEdge &e, const Coordinate &p) const
{
    geom::LineSegment seg;
    seg.setCoordinates(e.orig().getCoordinate(), e.dest().getCoordinate());
    double dist = seg.distance(p);
    // heuristic (hack?)
    return dist < edgeCoincidenceTolerance;
}

bool
QuadEdgeSubdivision::isVertexOfEdge(const QuadEdge &e, const Vertex &v) const
{
    if ((v.equals(e.orig(), tolerance)) || (v.equals(e.dest(), tolerance))) {
        return true;
    }
    return false;
}

std::unique_ptr<QuadEdgeSubdivision::QuadEdgeList>
QuadEdgeSubdivision::getPrimaryEdges(bool includeFrame)
{
    QuadEdgeList *edges = new QuadEdgeList();
    QuadEdgeStack edgeStack;
    QuadEdgeSet visitedEdges;

    edgeStack.push(startingEdges[0]);

    while (!edgeStack.empty())
    {
        QuadEdge *edge = edgeStack.top();
        edgeStack.pop();
        if (visitedEdges.find(edge) == visitedEdges.end())
        {
            QuadEdge* priQE = (QuadEdge*)&edge->getPrimary();

            if (includeFrame || ! isFrameEdge(*priQE))
                edges->push_back(priQE);

            edgeStack.push(&edge->oNext());
            edgeStack.push(&edge->sym().oNext());

            visitedEdges.insert(edge);
            visitedEdges.insert(&edge->sym());
        }
    }
    return std::unique_ptr<QuadEdgeList>(edges);
}

QuadEdge**
QuadEdgeSubdivision::fetchTriangleToVisit(QuadEdge *edge,
        QuadEdgeStack &edgeStack, bool includeFrame, QuadEdgeSet &visitedEdges)
{
    QuadEdge *curr = edge;
    int edgeCount = 0;
    bool isFrame = false;
    do
    {
        triEdges[edgeCount] = curr;

        if (isFrameEdge(*curr))
            isFrame = true;

        // push sym edges to visit next
        QuadEdge *sym = &curr->sym();
        if (visitedEdges.find(sym) == visitedEdges.end())
            edgeStack.push(sym);

        // mark this edge as visited
        visitedEdges.insert(curr);

        edgeCount++;
        curr = &curr->lNext();

    } while (curr != edge);

    if (isFrame && !includeFrame)
        return nullptr;
    return triEdges;
}

class
QuadEdgeSubdivision::TriangleCoordinatesVisitor : public TriangleVisitor {
private:
    QuadEdgeSubdivision::TriList *triCoords;
    CoordinateArraySequenceFactory coordSeqFact;

public:
    TriangleCoordinatesVisitor(QuadEdgeSubdivision::TriList *triCoords): triCoords(triCoords)
    {
    }

    void visit(QuadEdge* triEdges[3]) override
    {
        geom::CoordinateSequence *coordSeq = coordSeqFact.create(4,0);
        for (int i = 0; i < 3; i++) {
            Vertex v = triEdges[i]->orig();
            coordSeq->setAt(v.getCoordinate(), i);
        }
        coordSeq->setAt(triEdges[0]->orig().getCoordinate(), 3);
        triCoords->push_back(coordSeq);
    }
};


class
QuadEdgeSubdivision::TriangleCircumcentreVisitor : public TriangleVisitor
{
public:
	void visit(QuadEdge* triEdges[3]) override
	{
		Triangle triangle(triEdges[0]->orig().getCoordinate(),
				triEdges[1]->orig().getCoordinate(), triEdges[2]->orig().getCoordinate());
		Coordinate cc;
		triangle.circumcentre(cc);

		Vertex ccVertex(cc);

		for(int i=0 ; i<3 ; i++){
			triEdges[i]->rot().setOrig(ccVertex);
		}
	}
};


void
QuadEdgeSubdivision::getTriangleCoordinates(QuadEdgeSubdivision::TriList* triList, bool includeFrame)
{
    TriangleCoordinatesVisitor visitor(triList);
    visitTriangles((TriangleVisitor*)&visitor, includeFrame);
}

void
QuadEdgeSubdivision::visitTriangles(TriangleVisitor *triVisitor, bool includeFrame)
{

    QuadEdgeStack edgeStack;
    edgeStack.push(startingEdges[0]);

    QuadEdgeSet visitedEdges;

    while (!edgeStack.empty()) {
        QuadEdge *edge = edgeStack.top();
        edgeStack.pop();
        if (visitedEdges.find(edge) == visitedEdges.end()) {
            QuadEdge **triEdges = fetchTriangleToVisit(edge, edgeStack,
                    includeFrame, visitedEdges);
            if (triEdges != nullptr)
                triVisitor->visit(triEdges);
        }
    }
}

std::unique_ptr<geom::MultiLineString>
QuadEdgeSubdivision::getEdges(const geom::GeometryFactory& geomFact)
{
    std::unique_ptr<QuadEdgeList> quadEdges(getPrimaryEdges(false));
    std::vector<Geometry *> edges(quadEdges->size());
    const CoordinateSequenceFactory *coordSeqFact = geomFact.getCoordinateSequenceFactory();
    int i = 0;
    for (QuadEdgeSubdivision::QuadEdgeList::iterator it = quadEdges->begin(); it != quadEdges->end(); ++it)
    {
        QuadEdge *qe = *it;
        CoordinateSequence *coordSeq = coordSeqFact->create((std::vector<geom::Coordinate>*)nullptr);;

        coordSeq->add(qe->orig().getCoordinate());
        coordSeq->add(qe->dest().getCoordinate());

        edges[i++] = static_cast<Geometry*>(geomFact.createLineString(*coordSeq));

        delete coordSeq;
    }

    geom::MultiLineString* result = geomFact.createMultiLineString(edges);

    for(std::vector<Geometry*>::iterator it=edges.begin(); it!=edges.end(); ++it)
        delete *it;

    return std::unique_ptr<MultiLineString>(result);
}

std::unique_ptr<GeometryCollection>
QuadEdgeSubdivision::getTriangles( const GeometryFactory &geomFact)
{
    TriList triPtsList;
    getTriangleCoordinates(&triPtsList, false);
    std::vector<Geometry*> tris;

    for(TriList::const_iterator it = triPtsList.begin();
            it != triPtsList.end(); ++it)
    {
        CoordinateSequence *coordSeq = *it;
        Polygon *tri = geomFact.createPolygon(
                geomFact.createLinearRing(coordSeq), nullptr);
        tris.push_back(static_cast<Geometry*>(tri));
    }
    GeometryCollection* ret =  geomFact.createGeometryCollection(tris);

    //release memory
    for(std::vector<Geometry*>::iterator it=tris.begin(); it!=tris.end(); ++it)
        delete *it;
    tris.clear();

    return std::unique_ptr<GeometryCollection>(ret);
}


//Methods for VoronoiDiagram
std::unique_ptr<geom::GeometryCollection>
QuadEdgeSubdivision::getVoronoiDiagram(const geom::GeometryFactory& geomFact)
{
	std::unique_ptr< std::vector<geom::Geometry*> > vorCells = getVoronoiCellPolygons(geomFact);
	return std::unique_ptr<GeometryCollection>(geomFact.createGeometryCollection(vorCells.release()));
}

std::unique_ptr<geom::MultiLineString>
QuadEdgeSubdivision::getVoronoiDiagramEdges(const geom::GeometryFactory& geomFact)
{
	std::unique_ptr< std::vector<geom::Geometry*> > vorCells = getVoronoiCellEdges(geomFact);
	return std::unique_ptr<MultiLineString>(geomFact.createMultiLineString(vorCells.release()));
}

std::unique_ptr< std::vector<geom::Geometry*> >
QuadEdgeSubdivision::getVoronoiCellPolygons(const geom::GeometryFactory& geomFact)
{
	std::unique_ptr< std::vector<geom::Geometry*> > cells(new std::vector<geom::Geometry*>);
	TriangleCircumcentreVisitor* tricircumVisitor = new TriangleCircumcentreVisitor();
	visitTriangles((TriangleVisitor*)tricircumVisitor, true);

	std::unique_ptr<QuadEdgeSubdivision::QuadEdgeList> edges = getVertexUniqueEdges(false);

	for(QuadEdgeSubdivision::QuadEdgeList::iterator it=edges->begin() ; it!=edges->end() ; ++it)
	{
		QuadEdge *qe = *it;
		std::unique_ptr<geom::Geometry> poly = getVoronoiCellPolygon(qe,geomFact);

		cells->push_back(poly.release());
	}
	delete tricircumVisitor;
	return cells;
}

std::unique_ptr< std::vector<geom::Geometry*> >
QuadEdgeSubdivision::getVoronoiCellEdges(const geom::GeometryFactory& geomFact)
{
	std::unique_ptr< std::vector<geom::Geometry*> > cells(new std::vector<geom::Geometry*>);
	TriangleCircumcentreVisitor* tricircumVisitor = new TriangleCircumcentreVisitor();
	visitTriangles((TriangleVisitor*)tricircumVisitor, true);

	std::unique_ptr<QuadEdgeSubdivision::QuadEdgeList> edges = getVertexUniqueEdges(false);

	for(QuadEdgeSubdivision::QuadEdgeList::iterator it=edges->begin() ; it!=edges->end() ; ++it)
	{
		QuadEdge *qe = *it;
		std::unique_ptr<geom::Geometry> poly = getVoronoiCellEdge(qe,geomFact);

		cells->push_back(poly.release());
	}
	delete tricircumVisitor;
	return cells;
}

std::unique_ptr<geom::Geometry>
QuadEdgeSubdivision::getVoronoiCellPolygon(QuadEdge* qe ,const geom::GeometryFactory& geomFact)
{
	std::vector<Coordinate> cellPts;
	QuadEdge *startQE = qe;
	do{
		Coordinate cc = qe->rot().orig().getCoordinate();
		if ( cellPts.empty() || cellPts.back() != cc ) // no duplicates
			cellPts.push_back(cc);
		qe = &qe->oPrev();

	}while ( qe != startQE);


	//CoordList from a vector of Coordinates.
	geom::CoordinateList coordList(cellPts);
	//for checking close ring in CoordList class:
	coordList.closeRing();

	if(coordList.size() < 4)
	{
		coordList.insert(coordList.end(),*(coordList.end()),true);
	}

	std::unique_ptr<Coordinate::Vect> pts = coordList.toCoordinateArray();
	std::unique_ptr<geom::Geometry> cellPoly(
		geomFact.createPolygon(geomFact.createLinearRing(new geom::CoordinateArraySequence(pts.release())),nullptr));

	Vertex v = startQE->orig();
	Coordinate c(0,0);
	c = v.getCoordinate();
	cellPoly->setUserData(reinterpret_cast<void*>(&c));
	return cellPoly;
}

std::unique_ptr<geom::Geometry>
QuadEdgeSubdivision::getVoronoiCellEdge(QuadEdge* qe ,const geom::GeometryFactory& geomFact)
{
	std::vector<Coordinate> cellPts;
	QuadEdge *startQE = qe;
	do{
		Coordinate cc = qe->rot().orig().getCoordinate();
		if ( cellPts.empty() || cellPts.back() != cc ) // no duplicates
			cellPts.push_back(cc);
		qe = &qe->oPrev();

	}while ( qe != startQE);


	//CoordList from a vector of Coordinates.
	geom::CoordinateList coordList(cellPts);
	//for checking close ring in CoordList class:
	coordList.closeRing();

	std::unique_ptr<Coordinate::Vect> pts = coordList.toCoordinateArray();
	std::unique_ptr<geom::Geometry> cellEdge(
		geomFact.createLineString(new geom::CoordinateArraySequence(pts.release())));

	Vertex v = startQE->orig();
	Coordinate c(0,0);
	c = v.getCoordinate();
	cellEdge->setUserData(reinterpret_cast<void*>(&c));
	return cellEdge;
}

std::unique_ptr<QuadEdgeSubdivision::QuadEdgeList>
QuadEdgeSubdivision::getVertexUniqueEdges(bool includeFrame)
{
	std::unique_ptr<QuadEdgeSubdivision::QuadEdgeList> edges(new QuadEdgeList());
	std::set<Vertex> visitedVertices;
	for(QuadEdgeSubdivision::QuadEdgeList::iterator it=quadEdges.begin() ; it!=quadEdges.end() ; ++it)
	{
		QuadEdge *qe = (QuadEdge*)(*it);
		Vertex v = qe->orig();


		if(visitedVertices.find(v) == visitedVertices.end())	//if v not found
		{
			visitedVertices.insert(v);
			if(includeFrame || ! QuadEdgeSubdivision::isFrameVertex(v))
			{
				edges->push_back(qe);
			}
		}
		QuadEdge *qd = &(qe->sym());
		Vertex vd = qd->orig();


		if(visitedVertices.find(vd) == visitedVertices.end()){
			visitedVertices.insert(vd);
			if(includeFrame || ! QuadEdgeSubdivision::isFrameVertex(vd)){
				edges->push_back(qd);
			}
		}
	}
	return edges;
}

} //namespace geos.triangulate.quadedge
} //namespace geos.triangulate
} //namespace goes
