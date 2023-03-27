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
 **********************************************************************
 *
 * Last port: geom/prep/PreparedPolygonPredicate.java rev. 1.4 (JTS-1.10)
 * (2007-12-12)
 *
 **********************************************************************/

#include <geos/geom/prep/PreparedPolygonPredicate.h>
#include <geos/geom/prep/PreparedPolygon.h>
#include <geos/geom/Coordinate.h>
#include <geos/geom/util/ComponentCoordinateExtracter.h>
#include <geos/geom/Location.h>
#include <geos/algorithm/locate/PointOnGeometryLocator.h>
#include <geos/algorithm/locate/SimplePointInAreaLocator.h>
// std
#include <cstddef>

namespace geos {
namespace geom { // geos.geom
namespace prep { // geos.geom.prep
//
// private:
//

//
// protected:
//
bool
PreparedPolygonPredicate::isAllTestComponentsInTarget(const geom::Geometry* testGeom) const
{
    geom::Coordinate::ConstVect pts;
    geom::util::ComponentCoordinateExtracter::getCoordinates(*testGeom, pts);

    for (std::size_t i = 0, ni = pts.size(); i < ni; i++)
    {
        const geom::Coordinate* pt = pts[i];
        const int loc = prepPoly->getPointLocator()->locate(pt);
        if (geom::Location::EXTERIOR == loc)
        {
            return false;
        }
    }
    return true;
}

bool
PreparedPolygonPredicate::isAllTestComponentsInTargetInterior(
		const geom::Geometry* testGeom) const
{
    geom::Coordinate::ConstVect pts;
    geom::util::ComponentCoordinateExtracter::getCoordinates(*testGeom, pts);

    for (std::size_t i = 0, ni = pts.size(); i < ni; i++)
    {
        const geom::Coordinate * pt = pts[i];
        const int loc = prepPoly->getPointLocator()->locate(pt);
        if (geom::Location::INTERIOR != loc)
        {
            return false;
        }
    }
    return true;
}

bool
PreparedPolygonPredicate::isAnyTestComponentInTarget(
		const geom::Geometry* testGeom) const
{
    geom::Coordinate::ConstVect pts;
    geom::util::ComponentCoordinateExtracter::getCoordinates(*testGeom, pts);

    for (std::size_t i = 0, ni = pts.size(); i < ni; i++)
    {
        const Coordinate* pt = pts[i];
        const int loc = prepPoly->getPointLocator()->locate(pt);
        if (geom::Location::EXTERIOR != loc)
        {
            return true;
        }
    }
    return false;
}

bool
PreparedPolygonPredicate::isAnyTestComponentInTargetInterior(
	const geom::Geometry * testGeom) const
{
    geom::Coordinate::ConstVect pts;
    geom::util::ComponentCoordinateExtracter::getCoordinates(*testGeom, pts);

    for (std::size_t i = 0, ni = pts.size(); i < ni; i++)
    {
        const Coordinate * pt = pts[i];
        const int loc = prepPoly->getPointLocator()->locate(pt);
        if (geom::Location::INTERIOR == loc)
        {
            return true;
        }
    }
    return false;
}

bool
PreparedPolygonPredicate::isAnyTargetComponentInAreaTest(
	const geom::Geometry* testGeom,
	const geom::Coordinate::ConstVect* targetRepPts) const
{
    algorithm::locate::SimplePointInAreaLocator piaLoc(testGeom);

    for (std::size_t i = 0, ni = targetRepPts->size(); i < ni; i++)
    {
        const geom::Coordinate * pt = (*targetRepPts)[i];
        const int loc = piaLoc.locate(pt);
        if (geom::Location::EXTERIOR != loc)
        {
            return true;
        }
    }

    return false;
}

//
// public:
//

} // namespace geos.geom.prep
} // namespace geos.geom
} // namespace geos
