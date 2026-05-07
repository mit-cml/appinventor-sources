/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2005-2011 Refractions Research Inc.
 * Copyright (C) 2001-2002 Vivid Solutions Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: algorithm/PointLocator.java 95fbe34b (JTS-1.15.2-SNAPSHOT)
 *
 **********************************************************************/

#include <geos/algorithm/PointLocator.h>
#include <geos/algorithm/CGAlgorithms.h>
#include <geos/geom/Geometry.h>
#include <geos/geom/Point.h>
#include <geos/geom/LineString.h>
#include <geos/geom/LinearRing.h>
#include <geos/geom/MultiLineString.h>
#include <geos/geom/Polygon.h>
#include <geos/geom/MultiPolygon.h>
#include <geos/geom/Location.h>
#include <geos/geomgraph/GeometryGraph.h>

#include <cassert>
#include <typeinfo>

using namespace geos::geom;

namespace geos {
namespace algorithm { // geos.algorithm


int
PointLocator::locate(const Coordinate& p, const Geometry *geom)
{
	if (geom->isEmpty()) return Location::EXTERIOR;

	const LineString *ls_geom = dynamic_cast<const LineString *>(geom);
	if (ls_geom) return locate(p, ls_geom);

	const Polygon *poly_geom = dynamic_cast<const Polygon *>(geom);
	if (poly_geom) return locate(p, poly_geom);


	isIn=false;
	numBoundaries=0;
	computeLocation(p,geom);
	if (geomgraph::GeometryGraph::isInBoundary(numBoundaries)) return Location::BOUNDARY;
	if (numBoundaries>0 || isIn) return Location::INTERIOR;
	return Location::EXTERIOR;
}

/* private */
void
PointLocator::computeLocation(const Coordinate& p, const Geometry *geom)
{
	if (const Point *pt=dynamic_cast<const Point*>(geom))
	{
		updateLocationInfo(locate(p, pt));
	}
	else if (const LineString *ls=dynamic_cast<const LineString*>(geom))
	{
		updateLocationInfo(locate(p, ls));
	}
	else if (const Polygon *po=dynamic_cast<const Polygon*>(geom))
	{
		updateLocationInfo(locate(p, po));
	}
	else if (const MultiLineString *mls=dynamic_cast<const MultiLineString *>(geom))
	{
		for(std::size_t i=0, n=mls->getNumGeometries(); i<n; ++i)
		{
			const LineString *l=dynamic_cast<const LineString *>(mls->getGeometryN(i));
			updateLocationInfo(locate(p,l));
		}
	}
	else if (const MultiPolygon *mpo=dynamic_cast<const MultiPolygon *>(geom))
	{
		for(std::size_t i=0, n=mpo->getNumGeometries(); i<n; ++i)
		{
			const Polygon *po=dynamic_cast<const Polygon *>(mpo->getGeometryN(i));
			updateLocationInfo(locate(p, po));
		}
	}
	else if (const GeometryCollection *col=dynamic_cast<const GeometryCollection *>(geom))
	{
		for (GeometryCollection::const_iterator
				it=col->begin(), endIt=col->end();
				it != endIt;
				++it)
		{
			const Geometry *g2=*it;
			assert (g2!=geom); // is this check really needed ?
			computeLocation(p, g2);
		}
	}

}

/* private */
void
PointLocator::updateLocationInfo(int loc)
{
	if (loc==Location::INTERIOR) isIn=true;
	if (loc==Location::BOUNDARY) ++numBoundaries;
}

/* private */
int
PointLocator::locate(const Coordinate& p, const Point *pt)
{
	// no point in doing envelope test, since equality test is just as fast
	const Coordinate *ptCoord = pt->getCoordinate();
	if (ptCoord->equals2D(p))
		return Location::INTERIOR;
	return Location::EXTERIOR;
}

/* private */
int
PointLocator::locate(const Coordinate& p, const LineString *l)
{
	if (!l->getEnvelopeInternal()->intersects(p))
		return Location::EXTERIOR;

	const CoordinateSequence* pt=l->getCoordinatesRO();
	if (! l->isClosed()) {
		if ((p==pt->getAt(0)) || (p==pt->getAt(pt->getSize()-1))) {
			return Location::BOUNDARY;
		}
	}
	if (CGAlgorithms::isOnLine(p,pt))
		return Location::INTERIOR;
	return Location::EXTERIOR;
}

/* private */
int
PointLocator::locateInPolygonRing(const Coordinate& p, const LinearRing *ring)
{
	if (!ring->getEnvelopeInternal()->intersects(p)) {
		return Location::EXTERIOR;
	}

	const CoordinateSequence *cl = ring->getCoordinatesRO();

	if (CGAlgorithms::isOnLine(p,cl))
		return Location::BOUNDARY;
	if (CGAlgorithms::isPointInRing(p,cl))
		return Location::INTERIOR;
	return Location::EXTERIOR;
}

/* private */
int
PointLocator::locate(const Coordinate& p,const Polygon *poly)
{
	if (poly->isEmpty()) return Location::EXTERIOR;

	const LinearRing *shell=dynamic_cast<const LinearRing *>(poly->getExteriorRing());
	assert(shell);

	int shellLoc=locateInPolygonRing(p, shell);
	if (shellLoc==Location::EXTERIOR) return Location::EXTERIOR;
	if (shellLoc==Location::BOUNDARY) return Location::BOUNDARY;

	// now test if the point lies in or on the holes
	for(size_t i=0, n=poly->getNumInteriorRing(); i<n; ++i)
	{
		const LinearRing *hole=dynamic_cast<const LinearRing *>(poly->getInteriorRingN(i));
		int holeLoc=locateInPolygonRing(p,hole);
		if (holeLoc==Location::INTERIOR) return Location::EXTERIOR;
		if (holeLoc==Location::BOUNDARY) return Location::BOUNDARY;
	}
	return Location::INTERIOR;
}

} // namespace geos.algorithm
} // namespace geos

