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
 * Last port: geom/Lineal.java r320 (JTS-1.12)
 *
 **********************************************************************/

#ifndef GEOS_GEOM_LINEAL_H
#define GEOS_GEOM_LINEAL_H

#include <geos/export.h>
#include <geos/geom/Geometry.h> // for inheritance

#ifdef _MSC_VER
#pragma warning(push)
#pragma warning(disable: 4589) // warning C4589 : Constructor of abstract class 'Lineal' ignores initializer for virtual base class 'Geometry'
#endif

namespace geos {
namespace geom { // geos::geom

/**
 * Identifies {@link Geometry} subclasses which
 * are 1-dimensional and with components which are {@link LineString}s.
 */
class GEOS_DLL Lineal : public virtual Geometry
{
protected:
  Lineal(): Geometry(nullptr) {}
};

} // namespace geos::geom
} // namespace geos

#ifdef _MSC_VER
#pragma warning(pop)
#endif

#endif // ndef GEOS_GEOM_LINEAL_H
