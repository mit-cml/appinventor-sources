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
 **********************************************************************/

#include <geos/algorithm/CentroidLine.h>
#include <geos/geom/Coordinate.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/geom/Geometry.h>
#include <geos/geom/GeometryCollection.h>
#include <geos/geom/LineString.h>

#include <typeinfo>

using namespace geos::geom;

namespace geos {
namespace algorithm { // geos.algorithm

/*public*/
void
CentroidLine::add(const Geometry *geom)
{
	const LineString* ls = dynamic_cast<const LineString*>(geom);
	if ( ls )
	{
		add(ls->getCoordinatesRO());
		return;
	}

	const GeometryCollection* gc = dynamic_cast<const GeometryCollection*>(geom);
	if (gc)
	{
        for(std::size_t i=0, n=gc->getNumGeometries(); i<n; i++) {
			add(gc->getGeometryN(i));
		}
	}
}

/*public*/
void
CentroidLine::add(const CoordinateSequence *pts)
{
	std::size_t const npts=pts->getSize();

	for(std::size_t i=1; i<npts; ++i)
	{
		const Coordinate &p1=pts->getAt(i-1);
		const Coordinate &p2=pts->getAt(i);

		double segmentLen=p1.distance(p2);
		totalLength+=segmentLen;
		double midx=(p1.x+p2.x)/2;
		centSum.x+=segmentLen*midx;
		double midy=(p1.y+p2.y)/2;
		centSum.y+=segmentLen*midy;
	}
}

Coordinate *
CentroidLine::getCentroid() const
{
	return new Coordinate(centSum.x/totalLength, centSum.y/totalLength);
}

bool
CentroidLine::getCentroid(Coordinate& c) const
{
	if ( totalLength == 0.0 ) return false;
	c=Coordinate(centSum.x/totalLength, centSum.y/totalLength);
	return true;
}

} // namespace geos.algorithm
} // namespace geos
