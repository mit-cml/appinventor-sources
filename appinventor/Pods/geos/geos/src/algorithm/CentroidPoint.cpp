/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2001-2002 Vivid Solutions Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************/

#include <geos/algorithm/CentroidPoint.h>
#include <geos/geom/Coordinate.h>
#include <geos/geom/Geometry.h>
#include <geos/geom/GeometryCollection.h>
#include <geos/geom/MultiPoint.h>
#include <geos/geom/Point.h>

#include <typeinfo>

using namespace geos::geom;

namespace geos {
namespace algorithm { // geos.algorithm


void
CentroidPoint::add(const Geometry *geom)
{
  if ( const Point *p = dynamic_cast<const Point*>(geom) )
  {
		add(p->getCoordinate());
  }
  else if ( const GeometryCollection *gc =
            dynamic_cast<const GeometryCollection*>(geom) )
  {
    for(std::size_t i=0, n=gc->getNumGeometries(); i<n; ++i) {
      add(gc->getGeometryN(i));
    }
  }
}

void
CentroidPoint::add(const Coordinate *pt)
{
	ptCount+=1;
	centSum.x += pt->x;
	centSum.y += pt->y;
}

Coordinate*
CentroidPoint::getCentroid() const
{
	return new Coordinate(centSum.x/ptCount, centSum.y/ptCount);
}

bool
CentroidPoint::getCentroid(Coordinate& ret) const
{
	if ( ptCount == 0.0 ) return false;
	ret=Coordinate(centSum.x/ptCount, centSum.y/ptCount);
	return true;
}

} // namespace geos.algorithm
} // namespace geos

