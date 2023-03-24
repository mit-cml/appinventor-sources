/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2006-2011 Refractions Research Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: geom/util/GeometryCombiner.java r320 (JTS-1.12)
 *
 **********************************************************************/

#include <geos/geom/util/GeometryCombiner.h>
#include <geos/geom/Geometry.h>
#include <geos/geom/GeometryFactory.h>
#include <geos/geom/GeometryCollection.h>

namespace geos {
namespace geom { // geos.geom
namespace util { // geos.geom.util

Geometry* GeometryCombiner::combine(std::vector<Geometry*> const& geoms)
{
    GeometryCombiner combiner(geoms);
    return combiner.combine();
}

Geometry* GeometryCombiner::combine(const Geometry* g0, const Geometry* g1)
{
    std::vector<Geometry*> geoms;
    geoms.push_back(const_cast<Geometry*>(g0));
    geoms.push_back(const_cast<Geometry*>(g1));

    GeometryCombiner combiner(geoms);
    return combiner.combine();
}

Geometry* GeometryCombiner::combine(const Geometry* g0, const Geometry* g1,
                                    const Geometry* g2)
{
    std::vector<Geometry*> geoms;
    geoms.push_back(const_cast<Geometry*>(g0));
    geoms.push_back(const_cast<Geometry*>(g1));
    geoms.push_back(const_cast<Geometry*>(g2));

    GeometryCombiner combiner(geoms);
    return combiner.combine();
}

GeometryCombiner::GeometryCombiner(std::vector<Geometry*> const& geoms)
  : geomFactory(extractFactory(geoms)), skipEmpty(false), inputGeoms(geoms)
{
}

GeometryFactory const*
GeometryCombiner::extractFactory(std::vector<Geometry*> const& geoms)
{
    return geoms.empty() ? nullptr : geoms.front()->getFactory();
}

Geometry* GeometryCombiner::combine()
{
    std::vector<Geometry*> elems;

    std::vector<Geometry*>::const_iterator end = inputGeoms.end();
    for (std::vector<Geometry*>::const_iterator i = inputGeoms.begin();
         i != end; ++i)
    {
        extractElements(*i, elems);
    }

    if (elems.empty()) {
        if (geomFactory != nullptr) {
            return geomFactory->createGeometryCollection(nullptr);
        }
        return nullptr;
    }

    // return the "simplest possible" geometry
    return geomFactory->buildGeometry(elems);
}

void
GeometryCombiner::extractElements(Geometry* geom, std::vector<Geometry*>& elems)
{
    if (geom == nullptr)
        return;

    for (std::size_t i = 0; i < geom->getNumGeometries(); ++i) {
        Geometry* elemGeom = const_cast<Geometry*>(geom->getGeometryN(i));
        if (skipEmpty && elemGeom->isEmpty())
            continue;
        elems.push_back(elemGeom);
    }
}

} // namespace geos.geom.util
} // namespace geos.geom
} // namespace geos

