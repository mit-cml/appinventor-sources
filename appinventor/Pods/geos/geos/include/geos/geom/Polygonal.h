/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2011  Sandro Santilli <strk@kbt.io>
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: geom/Polygonal.java r320 (JTS-1.12)
 *
 **********************************************************************/

#ifndef GEOS_GEOM_POLYGONAL_H
#define GEOS_GEOM_POLYGONAL_H

#include <geos/export.h>
#include <geos/geom/Geometry.h> // for inheritance

#ifdef _MSC_VER
#pragma warning(push)
#pragma warning(disable: 4589) // warning C4589 : Constructor of abstract class 'Puntal' ignores initializer for virtual base class 'Geometry'
#endif

namespace geos {
namespace geom { // geos::geom

/**
 * Identifies {@link Geometry} subclasses which
 * are 2-dimensional and with components which are {@link Polygon}s.
 */
class GEOS_DLL Polygonal : public virtual Geometry
{
protected:
  Polygonal(): Geometry(nullptr) {}
};

} // namespace geos::geom
} // namespace geos

#ifdef _MSC_VER
#pragma warning(pop)
#endif

#endif // ndef GEOS_GEOM_POLYGONAL_H
