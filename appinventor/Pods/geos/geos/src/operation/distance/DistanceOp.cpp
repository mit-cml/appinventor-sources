/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2011 Sandro Santilli <strk@kbt.io>
 * Copyright (C) 2006 Refractions Research Inc.
 * Copyright (C) 2001-2002 Vivid Solutions Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: operation/distance/DistanceOp.java r335 (JTS-1.12-)
 *
 **********************************************************************/

#include <geos/operation/distance/DistanceOp.h>
#include <geos/operation/distance/GeometryLocation.h>
#include <geos/operation/distance/ConnectedElementLocationFilter.h>
#include <geos/algorithm/PointLocator.h>
#include <geos/algorithm/CGAlgorithms.h>
#include <geos/geom/Coordinate.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/geom/CoordinateArraySequence.h>
#include <geos/geom/LineString.h>
#include <geos/geom/Point.h>
#include <geos/geom/Polygon.h>
#include <geos/geom/Envelope.h>
#include <geos/geom/LineSegment.h>
#include <geos/geom/util/PolygonExtracter.h>
#include <geos/geom/util/LinearComponentExtracter.h>
#include <geos/geom/util/PointExtracter.h>
#include <geos/util/IllegalArgumentException.h>

#include <vector>
#include <iostream>

#ifndef GEOS_DEBUG
#define GEOS_DEBUG 0
#endif

using namespace std;
using namespace geos::geom;
//using namespace geos::algorithm;

namespace geos {
namespace operation { // geos.operation
namespace distance { // geos.operation.distance

using namespace geom;
//using namespace geom::util;

/*public static (deprecated)*/
double
DistanceOp::distance(const Geometry *g0, const Geometry *g1)
{
	DistanceOp distOp(g0,g1);
	return distOp.distance();
}

/*public static*/
double
DistanceOp::distance(const Geometry& g0, const Geometry& g1)
{
	DistanceOp distOp(g0,g1);
	return distOp.distance();
}

/*public static deprecated*/
CoordinateSequence*
DistanceOp::closestPoints(const Geometry *g0, const Geometry *g1)
{
	DistanceOp distOp(g0,g1);
	return distOp.nearestPoints();
}

/*public static*/
CoordinateSequence*
DistanceOp::nearestPoints(const Geometry *g0, const Geometry *g1)
{
	DistanceOp distOp(g0,g1);
	return distOp.nearestPoints();
}

DistanceOp::DistanceOp(const Geometry *g0, const Geometry *g1):
	geom(2),
	terminateDistance(0.0),
	minDistanceLocation(nullptr),
	minDistance(DoubleMax)
{
	geom[0] = g0;
	geom[1] = g1;
}

DistanceOp::DistanceOp(const Geometry& g0, const Geometry& g1):
	geom(2),
	terminateDistance(0.0),
	minDistanceLocation(nullptr),
	minDistance(DoubleMax)
{
	geom[0] = &g0;
	geom[1] = &g1;
}

DistanceOp::DistanceOp(const Geometry& g0, const Geometry& g1, double tdist)
	:
	geom(2),
	terminateDistance(tdist),
	minDistanceLocation(nullptr),
	minDistance(DoubleMax)
{
	geom[0] = &g0;
	geom[1] = &g1;
}

DistanceOp::~DistanceOp()
{
	size_t i;
	for (i=0; i<newCoords.size(); i++) delete newCoords[i];
	if ( minDistanceLocation )
	{
		for (i=0; i<minDistanceLocation->size(); i++)
		{
			delete (*minDistanceLocation)[i];
		}
		delete minDistanceLocation;
	}
}

/**
* Report the distance between the closest points on the input geometries.
*
* @return the distance between the geometries
*/
double
DistanceOp::distance()
{
	using geos::util::IllegalArgumentException;

	if ( geom[0] == nullptr || geom[1] == nullptr )
		throw IllegalArgumentException("null geometries are not supported");
	if ( geom[0]->isEmpty() || geom[1]->isEmpty() ) return 0.0;
	computeMinDistance();
	return minDistance;
}

/* public */
CoordinateSequence*
DistanceOp::closestPoints()
{
	return nearestPoints();
}


/* public */
CoordinateSequence*
DistanceOp::nearestPoints()
{
	// lazily creates minDistanceLocation
	computeMinDistance();

	assert(nullptr != minDistanceLocation);
	std::vector<GeometryLocation*>& locs = *minDistanceLocation;

	// Empty input geometries result in this behaviour
	if ( locs[0] == nullptr || locs[1] == nullptr )
	{
		// either both or none are set..
		assert(locs[0] == nullptr && locs[1] == nullptr);

		return nullptr;
	}

	GeometryLocation* loc0 = locs[0];
	GeometryLocation* loc1 = locs[1];
	const Coordinate& c0 = loc0->getCoordinate();
	const Coordinate& c1 = loc1->getCoordinate();

	CoordinateSequence* nearestPts = new CoordinateArraySequence();
	nearestPts->add(c0);
	nearestPts->add(c1);

	return nearestPts;
}

/*private, unused!*/
vector<GeometryLocation*>* DistanceOp::nearestLocations(){
	computeMinDistance();
	return minDistanceLocation;
}

void
DistanceOp::updateMinDistance(vector<GeometryLocation*>& locGeom, bool flip)
{
	assert(minDistanceLocation);

	// if not set then don't update
	if (locGeom[0]==nullptr)
	{
#if GEOS_DEBUG
std::cerr << "updateMinDistance called with loc[0] == null and loc[1] == " << locGeom[1] << std::endl;
#endif
		assert(locGeom[1] == nullptr);
		return;
	}

	delete (*minDistanceLocation)[0];
	delete (*minDistanceLocation)[1];
	if (flip) {
		(*minDistanceLocation)[0]=locGeom[1];
		(*minDistanceLocation)[1]=locGeom[0];
	} else {
		(*minDistanceLocation)[0]=locGeom[0];
		(*minDistanceLocation)[1]=locGeom[1];
	}
}

/*private*/
void
DistanceOp::computeMinDistance()
{
	// only compute once!
	if (minDistanceLocation) return;

#if GEOS_DEBUG
	std::cerr << "---Start: " << geom[0]->toString() << " - " << geom[1]->toString() << std::endl;
#endif

	minDistanceLocation = new vector<GeometryLocation*>(2);

	computeContainmentDistance();

	if (minDistance <= terminateDistance)
	{
		return;
	}

	computeFacetDistance();

#if GEOS_DEBUG
	std::cerr << "---End " << std::endl;
#endif
}

/*private*/
void
DistanceOp::computeContainmentDistance()
{
	using geom::util::PolygonExtracter;

	Polygon::ConstVect polys1;
	PolygonExtracter::getPolygons(*(geom[1]), polys1);


#if GEOS_DEBUG
	std::cerr << "PolygonExtracter found " << polys1.size() << " polygons in geometry 2" << std::endl;
#endif

	// NOTE:
	// Expected to fill minDistanceLocation items
	// if minDistance <= terminateDistance

	vector<GeometryLocation*> *locPtPoly = new vector<GeometryLocation*>(2);
	// test if either geometry has a vertex inside the other
	if ( ! polys1.empty() )
	{
		vector<GeometryLocation*> *insideLocs0 =
			ConnectedElementLocationFilter::getLocations(geom[0]);
		computeInside(insideLocs0, polys1, locPtPoly);
		if (minDistance <= terminateDistance) {
			assert( (*locPtPoly)[0] );
			assert( (*locPtPoly)[1] );
			(*minDistanceLocation)[0] = (*locPtPoly)[0];
			(*minDistanceLocation)[1] = (*locPtPoly)[1];
			delete locPtPoly;
			for (size_t i=0; i<insideLocs0->size(); i++)
			{
				GeometryLocation *l = (*insideLocs0)[i];
				if ( l != (*minDistanceLocation)[0] &&
					l != (*minDistanceLocation)[1] )
				{
					delete l;
				}
			}
			delete insideLocs0;
			return;
		}
		for (size_t i=0; i<insideLocs0->size(); i++)
			delete (*insideLocs0)[i];
		delete insideLocs0;
	}

	Polygon::ConstVect polys0;
	PolygonExtracter::getPolygons(*(geom[0]), polys0);

#if GEOS_DEBUG
	std::cerr << "PolygonExtracter found " << polys0.size() << " polygons in geometry 1" << std::endl;
#endif


	if ( ! polys0.empty() )
	{
		vector<GeometryLocation*> *insideLocs1 = ConnectedElementLocationFilter::getLocations(geom[1]);
		computeInside(insideLocs1, polys0, locPtPoly);
		if (minDistance <= terminateDistance) {
// flip locations, since we are testing geom 1 VS geom 0
			assert( (*locPtPoly)[0] );
			assert( (*locPtPoly)[1] );
			(*minDistanceLocation)[0] = (*locPtPoly)[1];
			(*minDistanceLocation)[1] = (*locPtPoly)[0];
			delete locPtPoly;
			for (size_t i=0; i<insideLocs1->size(); i++)
			{
				GeometryLocation *l = (*insideLocs1)[i];
				if ( l != (*minDistanceLocation)[0] &&
					l != (*minDistanceLocation)[1] )
				{
					delete l;
				}
			}
			delete insideLocs1;
			return;
		}
		for (size_t i=0; i<insideLocs1->size(); i++)
			delete (*insideLocs1)[i];
		delete insideLocs1;
	}

	delete locPtPoly;

	// If minDistance <= terminateDistance we must have
	// set minDistanceLocations to some non-null item
	assert( minDistance > terminateDistance ||
	        ( (*minDistanceLocation)[0] && (*minDistanceLocation)[1] ) );
}


/*private*/
void
DistanceOp::computeInside(vector<GeometryLocation*> *locs,
		const Polygon::ConstVect& polys,
		vector<GeometryLocation*> *locPtPoly)
{
	for (size_t i=0, ni=locs->size(); i<ni; ++i)
	{
		GeometryLocation *loc=(*locs)[i];
		for (size_t j=0, nj=polys.size(); j<nj; ++j)
		{
			computeInside(loc, polys[j], locPtPoly);
			if (minDistance<=terminateDistance) return;
		}
	}
}

/*private*/
void
DistanceOp::computeInside(GeometryLocation *ptLoc,
		const Polygon *poly,
		vector<GeometryLocation*> *locPtPoly)
{
	const Coordinate &pt=ptLoc->getCoordinate();

	// if pt is not in exterior, distance to geom is 0
	if (Location::EXTERIOR!=ptLocator.locate(pt, static_cast<const Geometry *>(poly)))
	{
		minDistance = 0.0;
		(*locPtPoly)[0] = ptLoc;
		GeometryLocation *locPoly = new GeometryLocation(poly, pt);
		(*locPtPoly)[1] = locPoly;
		return;
	}
}

/*private*/
void
DistanceOp::computeFacetDistance()
{
	using geom::util::LinearComponentExtracter;
	using geom::util::PointExtracter;

	vector<GeometryLocation*> locGeom(2);

	/**
	 * Geometries are not wholely inside, so compute distance from lines
	 * and points
	 * of one to lines and points of the other
	 */
	LineString::ConstVect lines0;
	LineString::ConstVect lines1;
	LinearComponentExtracter::getLines(*(geom[0]), lines0);
	LinearComponentExtracter::getLines(*(geom[1]), lines1);

#if GEOS_DEBUG
	std::cerr << "LinearComponentExtracter found "
	          << lines0.size() << " lines in geometry 1 and "
	          << lines1.size() << " lines in geometry 2 "
	          << std::endl;
#endif

	Point::ConstVect pts0;
	Point::ConstVect pts1;
	PointExtracter::getPoints(*(geom[0]), pts0);
	PointExtracter::getPoints(*(geom[1]), pts1);

#if GEOS_DEBUG
	std::cerr << "PointExtracter found "
	          << pts0.size() << " points in geometry 1 and "
	          << pts1.size() << " points in geometry 2 "
	          << std::endl;
#endif

	// exit whenever minDistance goes LE than terminateDistance
	computeMinDistanceLines(lines0, lines1, locGeom);
	updateMinDistance(locGeom, false);
	if (minDistance <= terminateDistance) {
#if GEOS_DEBUG
		std::cerr << "Early termination after line-line distance" << std::endl;
#endif
		return;
	};

	locGeom[0]=nullptr;
	locGeom[1]=nullptr;
	computeMinDistanceLinesPoints(lines0, pts1, locGeom);
	updateMinDistance(locGeom, false);
	if (minDistance <= terminateDistance) {
#if GEOS_DEBUG
		std::cerr << "Early termination after lines0-points1 distance" << std::endl;
#endif
		return;
	};

	locGeom[0]=nullptr;
	locGeom[1]=nullptr;
	computeMinDistanceLinesPoints(lines1, pts0, locGeom);
	updateMinDistance(locGeom, true);
	if (minDistance <= terminateDistance){
#if GEOS_DEBUG
		std::cerr << "Early termination after lines1-points0 distance" << std::endl;
#endif
		return;
	};

	locGeom[0]=nullptr;
	locGeom[1]=nullptr;
	computeMinDistancePoints(pts0, pts1, locGeom);
	updateMinDistance(locGeom, false);

#if GEOS_DEBUG
	std::cerr << "termination after pts-pts distance" << std::endl;
#endif
}

/*private*/
void
DistanceOp::computeMinDistanceLines(
		const LineString::ConstVect& lines0,
		const LineString::ConstVect& lines1,
		vector<GeometryLocation*>& locGeom)
{
	for (size_t i=0, ni=lines0.size(); i<ni; ++i)
	{
		const LineString *line0=lines0[i];
		for (size_t j=0, nj=lines1.size(); j<nj; ++j) {
			const LineString *line1=lines1[j];
			computeMinDistance(line0, line1, locGeom);
			if (minDistance<=terminateDistance) return;
		}
	}
}

/*private*/
void
DistanceOp::computeMinDistancePoints(
		const Point::ConstVect& points0,
		const Point::ConstVect& points1,
		vector<GeometryLocation*>& locGeom)
{
	for (size_t i=0, ni=points0.size(); i<ni; ++i)
	{
		const Point *pt0 = points0[i];
		for (size_t j=0, nj=points1.size(); j<nj; ++j)
		{
			const Point *pt1 = points1[j];
			double dist = pt0->getCoordinate()->distance(*(pt1->getCoordinate()));

#if GEOS_DEBUG
	std::cerr << "Distance "
	          << pt0->toString() << " - "
	          << pt1->toString() << ": "
	          << dist << ", minDistance: " << minDistance
	          << std::endl;
#endif

			if (dist < minDistance)
			{
				minDistance = dist;
				// this is wrong - need to determine closest points on both segments!!!
				delete locGeom[0];
				locGeom[0] = new GeometryLocation(pt0, 0, *(pt0->getCoordinate()));
				delete locGeom[1];
				locGeom[1] = new GeometryLocation(pt1, 0, *(pt1->getCoordinate()));
			}

			if (minDistance<=terminateDistance) return;
		}
	}
}

/*private*/
void
DistanceOp::computeMinDistanceLinesPoints(
		const LineString::ConstVect& lines,
		const Point::ConstVect& points,
		vector<GeometryLocation*>& locGeom)
{
	for (size_t i=0;i<lines.size();i++)
	{
		const LineString *line=lines[i];
		for (size_t j=0;j<points.size();j++)
		{
			const Point *pt=points[j];
			computeMinDistance(line,pt,locGeom);
			if (minDistance<=terminateDistance) return;
		}
	}
}

/*private*/
void
DistanceOp::computeMinDistance(
		const LineString *line0,
		const LineString *line1,
		vector<GeometryLocation*>& locGeom)
{
	using geos::algorithm::CGAlgorithms;

	const Envelope *env0=line0->getEnvelopeInternal();
	const Envelope *env1=line1->getEnvelopeInternal();
	if (env0->distance(env1)>minDistance) {
		return;
	}

	const CoordinateSequence *coord0=line0->getCoordinatesRO();
	const CoordinateSequence *coord1=line1->getCoordinatesRO();
	size_t npts0=coord0->getSize();
	size_t npts1=coord1->getSize();

	// brute force approach!
	for(size_t i=0; i<npts0-1; ++i)
	{
		for(size_t j=0; j<npts1-1; ++j)
		{
			double dist=CGAlgorithms::distanceLineLine(coord0->getAt(i),coord0->getAt(i+1),
				coord1->getAt(j),coord1->getAt(j+1));
			if (dist < minDistance) {
				minDistance = dist;
				LineSegment seg0(coord0->getAt(i), coord0->getAt(i + 1));
				LineSegment seg1(coord1->getAt(j), coord1->getAt(j + 1));
				CoordinateSequence* closestPt = seg0.closestPoints(seg1);
				Coordinate *c1 = new Coordinate(closestPt->getAt(0));
				Coordinate *c2 = new Coordinate(closestPt->getAt(1));
				newCoords.push_back(c1);
				newCoords.push_back(c2);
				delete closestPt;

				delete locGeom[0];
				locGeom[0] = new GeometryLocation(line0, static_cast<int>(i), *c1);
				delete locGeom[1];
				locGeom[1] = new GeometryLocation(line1, static_cast<int>(j), *c2);
			}
			if (minDistance<=terminateDistance) return;
		}
	}
}

/*private*/
void
DistanceOp::computeMinDistance(const LineString *line,
		const Point *pt,
		vector<GeometryLocation*>& locGeom)
{
	using geos::algorithm::CGAlgorithms;

	const Envelope *env0=line->getEnvelopeInternal();
	const Envelope *env1=pt->getEnvelopeInternal();
	if (env0->distance(env1)>minDistance) {
		return;
	}
	const CoordinateSequence *coord0=line->getCoordinatesRO();
	Coordinate *coord=new Coordinate(*(pt->getCoordinate()));
	newCoords.push_back(coord);

	// brute force approach!
	size_t npts0=coord0->getSize();
	for(size_t i=0; i<npts0-1; ++i)
	{
		double dist=CGAlgorithms::distancePointLine(*coord,coord0->getAt(i),coord0->getAt(i+1));
        	if (dist < minDistance) {
          		minDistance = dist;
			LineSegment seg(coord0->getAt(i), coord0->getAt(i + 1));
			Coordinate segClosestPoint;
			seg.closestPoint(*coord, segClosestPoint);

			delete locGeom[0];
			locGeom[0] = new GeometryLocation(line, static_cast<int>(i), segClosestPoint);
			delete locGeom[1];
			locGeom[1] = new GeometryLocation(pt, 0, *coord);
        	}
		if (minDistance<=terminateDistance) return;
	}
}

/* public static */
bool
DistanceOp::isWithinDistance(const geom::Geometry& g0,
	                     const geom::Geometry& g1,
	                     double distance)
{
	DistanceOp distOp(g0, g1, distance);
	return distOp.distance() <= distance;
}

} // namespace geos.operation.distance
} // namespace geos.operation
} // namespace geos
