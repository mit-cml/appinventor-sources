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
 **********************************************************************/

#ifndef GEOS_GEOMPREP_H
#define GEOS_GEOMPREP_H

#include <geos/geom/prep/PreparedGeometry.h>
#include <geos/geom/prep/PreparedGeometryFactory.h>
#include <geos/geom/prep/PreparedPoint.h>
#include <geos/geom/prep/PreparedLineString.h>
#include <geos/geom/prep/PreparedPolygon.h>

namespace geos {
namespace geom { // geos.geom

/** \brief
 * Contains classes and interfaces implementing algorithms that optimize the
 * performance of repeated calls to specific geometric operations.
 */
namespace prep { // geos.geom.prep

} // namespace geos.geom.prep
} // namespace geos.geom
} // namespace geos

#endif //GEOS_GEOMPREP_H
