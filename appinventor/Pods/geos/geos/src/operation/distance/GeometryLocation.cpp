/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
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
 * Last port: operation/distance/GeometryLocation.java rev. 1.7 (JTS-1.10)
 *
 **********************************************************************/

#include <geos/operation/distance/GeometryLocation.h>

using namespace geos::geom;

namespace geos {
namespace operation { // geos.operation
namespace distance { // geos.operation.distance

/**
* Constructs a GeometryLocation specifying a point on a geometry, as well as the
* segment that the point is on (or INSIDE_AREA if the point is not on a segment).
*/
GeometryLocation::GeometryLocation(const Geometry *newComponent, int newSegIndex, const Coordinate &newPt)
{
	component = newComponent;
	segIndex = newSegIndex;
	pt = newPt;
}

/**
* Constructs a GeometryLocation specifying a point inside an area geometry.
*/
GeometryLocation::GeometryLocation(const Geometry *newComponent, const Coordinate &newPt)
{
	component = newComponent;
	segIndex = INSIDE_AREA;
	pt = newPt;
}

/**
* Returns the geometry associated with this location.
*/
const Geometry* GeometryLocation::getGeometryComponent() {
	return component;
}
/**
* Returns the segment index for this location. If the location is inside an
* area, the index will have the value INSIDE_AREA;
*
* @return the segment index for the location, or INSIDE_AREA
*/
int
GeometryLocation::getSegmentIndex()
{
	return segIndex;
}
/**
* Returns the location.
*/
Coordinate&
GeometryLocation::getCoordinate()
{
	return pt;
}

bool GeometryLocation::isInsideArea() {
	return segIndex == INSIDE_AREA;
}

} // namespace geos.operation.distance
} // namespace geos.operation
} // namespace geos
