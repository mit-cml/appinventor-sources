/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2001-2002 Vivid Solutions Inc.
 * Copyright (C) 2006 Refractions Research Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: algorithm/CentroidArea.java r612
 *
 **********************************************************************/

#include <geos/algorithm/CentroidArea.h>
#include <geos/algorithm/CGAlgorithms.h>
#include <geos/geom/Coordinate.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/geom/Geometry.h>
#include <geos/geom/GeometryCollection.h>
#include <geos/geom/LineString.h>
#include <geos/geom/Polygon.h>

#include <typeinfo>

using namespace geos::geom;

namespace geos {
namespace algorithm { // geos.algorithm

/*public*/
void
CentroidArea::add(const Geometry *geom)
{
  if(geom->isEmpty()) return;
	else if(const Polygon *poly=dynamic_cast<const Polygon*>(geom)) {
		setBasePoint(poly->getExteriorRing()->getCoordinateN(0));
		add(poly);
	}
	else if(const GeometryCollection *gc=dynamic_cast<const GeometryCollection*>(geom))
	{
        for(std::size_t i=0, n=gc->getNumGeometries(); i<n; ++i)
		{
			add(gc->getGeometryN(i));
		}
	}
}

/*public*/
void
CentroidArea::add(const CoordinateSequence *ring)
{
	setBasePoint(ring->getAt(0));
	addShell(ring);
}

/* TODO: deprecate this */
Coordinate*
CentroidArea::getCentroid() const
{
	Coordinate *cent = new Coordinate();
	getCentroid(*cent); // or return NULL on failure !
	return cent;
}

bool
CentroidArea::getCentroid(Coordinate& ret) const
{
	if ( areasum2 ) {
		ret = Coordinate(cg3.x/3.0/areasum2, cg3.y/3.0/areasum2);
	} else if ( totalLength ) {
		ret = Coordinate(centSum.x/totalLength, centSum.y/totalLength);
	} else {
		return false;
	}
	return true;
}

void
CentroidArea::setBasePoint(const Coordinate &newbasePt)
{
	basePt=newbasePt;
}

void
CentroidArea::add(const Polygon *poly)
{
	addShell(poly->getExteriorRing()->getCoordinatesRO());
	for(size_t i=0, n=poly->getNumInteriorRing(); i<n; ++i)
	{
		addHole(poly->getInteriorRingN(i)->getCoordinatesRO());
	}
}

void
CentroidArea::addShell(const CoordinateSequence *pts)
{
	bool isPositiveArea=!CGAlgorithms::isCCW(pts);
	std::size_t const n=pts->getSize()-1;
	for(std::size_t i=0; i<n; ++i)
	{
		addTriangle(basePt, pts->getAt(i), pts->getAt(i+1), isPositiveArea);
	}
	addLinearSegments(*pts);
}

void
CentroidArea::addHole(const CoordinateSequence *pts)
{
	bool isPositiveArea=CGAlgorithms::isCCW(pts);
	std::size_t const n=pts->getSize()-1;
	for(std::size_t i=0; i<n; ++i)
	{
		addTriangle(basePt, pts->getAt(i), pts->getAt(i+1), isPositiveArea);
	}
	addLinearSegments(*pts);
}

void
CentroidArea::addTriangle(const Coordinate &p0, const Coordinate &p1,
		const Coordinate &p2, bool isPositiveArea)
{
	double sign=(isPositiveArea)?1.0:-1.0;
	centroid3(p0,p1,p2,triangleCent3);
	double area2res=area2(p0,p1,p2);
	cg3.x+=sign*area2res*triangleCent3.x;
	cg3.y+=sign*area2res*triangleCent3.y;
	areasum2+=sign*area2res;
}

/**
 * Returns three times the centroid of the triangle p1-p2-p3.
 * The factor of 3 is
 * left in to permit division to be avoided until later.
 */
void
CentroidArea::centroid3(const Coordinate &p1, const Coordinate &p2,
		const Coordinate &p3, Coordinate &c)
{
	c.x=p1.x+p2.x+p3.x;
	c.y=p1.y+p2.y+p3.y;
}

/**
 * Returns twice the signed area of the triangle p1-p2-p3,
 * positive if a,b,c are oriented ccw, and negative if cw.
 */
double
CentroidArea::area2(const Coordinate &p1, const Coordinate &p2, const Coordinate &p3)
{
	return (p2.x-p1.x)*(p3.y-p1.y)-(p3.x-p1.x)*(p2.y-p1.y);
}

void
CentroidArea::addLinearSegments(const geom::CoordinateSequence& pts)
{
	std::size_t const n = pts.size()-1;
	for (std::size_t i = 0; i < n; ++i) {
		double segmentLen = pts[i].distance(pts[i + 1]);
		totalLength += segmentLen;

		double midx = (pts[i].x + pts[i + 1].x) / 2;
		centSum.x += segmentLen * midx;
		double midy = (pts[i].y + pts[i + 1].y) / 2;
		centSum.y += segmentLen * midy;
	}
}

} // namespace geos.algorithm
} //namespace geos
