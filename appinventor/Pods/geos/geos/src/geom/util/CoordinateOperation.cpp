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

#include <typeinfo>

#include <geos/geom/util/CoordinateOperation.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/geom/LinearRing.h>
#include <geos/geom/LineString.h>
#include <geos/geom/Point.h>
#include <geos/geom/Geometry.h>
#include <geos/geom/GeometryFactory.h>

namespace geos {
namespace geom { // geos.geom
namespace util { // geos.geom.util

Geometry*
CoordinateOperation::edit(const Geometry *geometry,
		const GeometryFactory *factory)
{
		const LinearRing *ring = dynamic_cast<const LinearRing *>(geometry);
		if (ring) {
			const CoordinateSequence *coords = ring->getCoordinatesRO();
			CoordinateSequence *newCoords = edit(coords,geometry);
            // LinearRing instance takes over ownership of newCoords instance
			return factory->createLinearRing(newCoords);
		}
		const LineString *line = dynamic_cast<const LineString *>(geometry);
		if (line) {
			const CoordinateSequence *coords = line->getCoordinatesRO();
			CoordinateSequence *newCoords = edit(coords,geometry);
			return factory->createLineString(newCoords);
		}
		if (typeid(*geometry)==typeid(Point)) {
			CoordinateSequence *coords = geometry->getCoordinates();
			CoordinateSequence *newCoords = edit(coords,geometry);
			delete coords;
			return factory->createPoint(newCoords);
		}

		return geometry->clone();
}


} // namespace geos.geom.util
} // namespace geos.geom
} // namespace geos
