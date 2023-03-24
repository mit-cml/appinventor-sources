/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2011 Sandro Santilli <strk@kbt.io>
 * Copyright (C) 2006 Refractions Research Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: operation/union/CascadedPolygonUnion.java r487 (JTS-1.12+)
 * Includes custom code to deal with https://trac.osgeo.org/geos/ticket/837
 *
 **********************************************************************/

#include <geos/operation/union/CascadedPolygonUnion.h>
#include <geos/geom/Geometry.h>
#include <geos/geom/GeometryFactory.h>
#include <geos/geom/Polygon.h>
#include <geos/geom/MultiPolygon.h>
#include <geos/geom/util/GeometryCombiner.h>
#include <geos/geom/util/PolygonExtracter.h>
#include <geos/index/strtree/STRtree.h>
// std
#include <cassert>
#include <cstddef>
#include <memory>
#include <vector>
#include <sstream>

#include <geos/operation/valid/IsValidOp.h>
#include <geos/operation/IsSimpleOp.h>
#include <geos/algorithm/BoundaryNodeRule.h>
#include <geos/util/TopologyException.h>
#include <string>
#include <iomanip>

//#define GEOS_DEBUG_CASCADED_UNION 1
//#define GEOS_DEBUG_CASCADED_UNION_PRINT_INVALID 1

namespace {

inline bool
check_valid(const geos::geom::Geometry& g, const std::string& label, bool doThrow=false, bool validOnly=false)
{
  using namespace geos;

  if ( dynamic_cast<const geos::geom::Lineal*>(&g) ) {
    if ( ! validOnly ) {
      operation::IsSimpleOp sop(g, algorithm::BoundaryNodeRule::getBoundaryEndPoint());
      if ( ! sop.isSimple() )
      {
        if ( doThrow ) {
          throw geos::util::TopologyException(
            label + " is not simple");
        }
        return false;
      }
    }
  } else {
    operation::valid::IsValidOp ivo(&g);
    if ( ! ivo.isValid() )
    {
      using operation::valid::TopologyValidationError;
      TopologyValidationError* err = ivo.getValidationError();
#ifdef GEOS_DEBUG_CASCADED_UNION
      std::cerr << label << " is INVALID: "
        << err->toString()
        << " (" << std::setprecision(20)
        << err->getCoordinate() << ")"
        << std::endl
#ifdef GEOS_DEBUG_CASCADED_UNION_PRINT_INVALID
        << "<a>" << std::endl
        << g.toString() << std::endl
        << "</a>" << std::endl
#endif
        ;
#endif // GEOS_DEBUG_CASCADED_UNION
      if ( doThrow ) {
        throw geos::util::TopologyException(
          label + " is invalid: " + err->toString(),
                err->getCoordinate());
      }
      return false;
    }
  }
  return true;
}

} // anonymous namespace

namespace geos {
namespace operation { // geos.operation
namespace geounion {  // geos.operation.geounion

///////////////////////////////////////////////////////////////////////////////
void GeometryListHolder::deleteItem(geom::Geometry* item)
{
    delete item;
}

///////////////////////////////////////////////////////////////////////////////
geom::Geometry* CascadedPolygonUnion::Union(std::vector<geom::Polygon*>* polys)
{
    CascadedPolygonUnion op (polys);
    return op.Union();
}

geom::Geometry* CascadedPolygonUnion::Union(const geom::MultiPolygon* multipoly)
{
    std::vector<geom::Polygon*> polys;

    typedef geom::MultiPolygon::const_iterator iterator;
    iterator end = multipoly->end();
    for (iterator i = multipoly->begin(); i != end; ++i)
        polys.push_back(dynamic_cast<geom::Polygon*>(*i));

    CascadedPolygonUnion op (&polys);
    return op.Union();
}

geom::Geometry* CascadedPolygonUnion::Union()
{
    if (inputPolys->empty())
        return nullptr;

    geomFactory = inputPolys->front()->getFactory();

    /**
     * A spatial index to organize the collection
     * into groups of close geometries.
     * This makes unioning more efficient, since vertices are more likely
     * to be eliminated on each round.
     */
    index::strtree::STRtree index(STRTREE_NODE_CAPACITY);

    typedef std::vector<geom::Polygon*>::iterator iterator_type;
    iterator_type end = inputPolys->end();
    for (iterator_type i = inputPolys->begin(); i != end; ++i) {
        geom::Geometry* g = dynamic_cast<geom::Geometry*>(*i);
        index.insert(g->getEnvelopeInternal(), g);
    }

    std::unique_ptr<index::strtree::ItemsList> itemTree (index.itemsTree());

    return unionTree(itemTree.get());
}

geom::Geometry* CascadedPolygonUnion::unionTree(
    index::strtree::ItemsList* geomTree)
{
    /**
     * Recursively unions all subtrees in the list into single geometries.
     * The result is a list of Geometry's only
     */
    std::unique_ptr<GeometryListHolder> geoms(reduceToGeometries(geomTree));
    return binaryUnion(geoms.get());
}

geom::Geometry* CascadedPolygonUnion::binaryUnion(GeometryListHolder* geoms)
{
    return binaryUnion(geoms, 0, geoms->size());
}

geom::Geometry* CascadedPolygonUnion::binaryUnion(GeometryListHolder* geoms,
    std::size_t start, std::size_t end)
{
    if (end - start <= 1) {
        return unionSafe(geoms->getGeometry(start), nullptr);
    }
    else if (end - start == 2) {
        return unionSafe(geoms->getGeometry(start), geoms->getGeometry(start + 1));
    }
    else {
        // recurse on both halves of the list
        std::size_t mid = (end + start) / 2;
        std::unique_ptr<geom::Geometry> g0 (binaryUnion(geoms, start, mid));
        std::unique_ptr<geom::Geometry> g1 (binaryUnion(geoms, mid, end));
        return unionSafe(g0.get(), g1.get());
    }
}

GeometryListHolder*
CascadedPolygonUnion::reduceToGeometries(index::strtree::ItemsList* geomTree)
{
    std::unique_ptr<GeometryListHolder> geoms (new GeometryListHolder());

    typedef index::strtree::ItemsList::iterator iterator_type;
    iterator_type end = geomTree->end();
    for (iterator_type i = geomTree->begin(); i != end; ++i) {
        if ((*i).get_type() == index::strtree::ItemsListItem::item_is_list) {
            std::unique_ptr<geom::Geometry> geom (unionTree((*i).get_itemslist()));
            geoms->push_back_owned(geom.get());
            geom.release();
        }
        else if ((*i).get_type() == index::strtree::ItemsListItem::item_is_geometry) {
            geoms->push_back(reinterpret_cast<geom::Geometry*>((*i).get_geometry()));
        }
        else {
            assert(!static_cast<bool>("should never be reached"));
        }
    }

    return geoms.release();
}

geom::Geometry*
CascadedPolygonUnion::unionSafe(geom::Geometry* g0, geom::Geometry* g1)
{
    if (g0 == nullptr && g1 == nullptr)
        return nullptr;

    if (g0 == nullptr)
        return g1->clone();
    if (g1 == nullptr)
        return g0->clone();

    return unionOptimized(g0, g1);
}

geom::Geometry*
CascadedPolygonUnion::unionOptimized(geom::Geometry* g0, geom::Geometry* g1)
{
    geom::Envelope const* g0Env = g0->getEnvelopeInternal();
    geom::Envelope const* g1Env = g1->getEnvelopeInternal();

    if (!g0Env->intersects(g1Env))
        return geom::util::GeometryCombiner::combine(g0, g1);

    if (g0->getNumGeometries() <= 1 && g1->getNumGeometries() <= 1)
        return unionActual(g0, g1);

    geom::Envelope commonEnv;
    g0Env->intersection(*g1Env, commonEnv);
    return unionUsingEnvelopeIntersection(g0, g1, commonEnv);
}

/* private */
geom::Geometry*
CascadedPolygonUnion::unionUsingEnvelopeIntersection(geom::Geometry* g0,
    geom::Geometry* g1, geom::Envelope const& common)
{
    std::vector<geom::Geometry*> disjointPolys;

#if GEOS_DEBUG_CASCADED_UNION
    check_valid(*g0, "unionUsingEnvelopeIntersection g0");
    check_valid(*g1, "unionUsingEnvelopeIntersection g1");
#endif

    std::unique_ptr<geom::Geometry> g0Int(extractByEnvelope(common, g0, disjointPolys));
    std::unique_ptr<geom::Geometry> g1Int(extractByEnvelope(common, g1, disjointPolys));

#if GEOS_DEBUG_CASCADED_UNION
    check_valid(*g0Int, "unionUsingEnvelopeIntersection g0Int");
    check_valid(*g1Int, "unionUsingEnvelopeIntersection g1Int");
#endif

    std::unique_ptr<geom::Geometry> u(unionActual(g0Int.get(), g1Int.get()));

#if GEOS_DEBUG_CASCADED_UNION
    if ( ! check_valid(*u, "unionUsingEnvelopeIntersection unionActual return") )
    {
#if GEOS_DEBUG_CASCADED_UNION_PRINT_INVALID
      std::cerr << " union between the following is invalid"
        << "<a>" << std::endl
        << *g0Int << std::endl
        << "</a>" << std::endl
        << "<b>" << std::endl
        << *g1Int << std::endl
        << "</b>" << std::endl
        ;
#endif
    }
#endif

    if ( disjointPolys.empty() ) return u.release();

#if GEOS_DEBUG_CASCADED_UNION
    for ( size_t i=0; i<disjointPolys.size(); ++i )
    {
      std::ostringstream os; os << "dp"<< i;
      check_valid(*(disjointPolys[i]), os.str());
    }
#endif

    // TODO: find, in disjointPolys, those which now have their
    // environment intersect the environment of the union "u"
    // and collect them in another vector to be unioned

    std::vector<geom::Geometry*> polysOn;
    std::vector<geom::Geometry*> polysOff;
    geom::Envelope const* uEnv = u->getEnvelopeInternal(); // TODO: check for EMPTY ?
    extractByEnvelope(*uEnv, disjointPolys, polysOn, polysOff);
#if GEOS_DEBUG_CASCADED_UNION
    std::cerr << "unionUsingEnvelopeIntersection: " << polysOn.size() << "/" << disjointPolys.size() << " polys intersect union of final thing" << std::endl;
#endif

    std::unique_ptr<geom::Geometry> ret;
    if ( polysOn.empty() ) {
      disjointPolys.push_back(u.get());
      ret.reset( geom::util::GeometryCombiner::combine(disjointPolys));
    } else {
      // TODO: could be further tweaked to only union with polysOn
      //       and combine with polysOff, but then it'll need again to
      //       recurse in the check for disjoint/intersecting
      ret.reset( geom::util::GeometryCombiner::combine(disjointPolys) );
      ret.reset( unionActual(ret.get(), u.get()) );
    }

#if GEOS_DEBUG_CASCADED_UNION
    check_valid(*ret, "unionUsingEnvelopeIntersection returned geom");
#endif

    return ret.release();
}

/* private */
void
CascadedPolygonUnion::extractByEnvelope(geom::Envelope const& env,
    std::vector<geom::Geometry*>& sourceGeoms,
    std::vector<geom::Geometry*>& intersectingGeoms,
    std::vector<geom::Geometry*>& disjointGeoms)
{
    for (std::vector<geom::Geometry*>::iterator i=sourceGeoms.begin(),
         e=sourceGeoms.end(); i!=e; ++i)
    {
        geom::Geometry* elem = *i;
        if (elem->getEnvelopeInternal()->intersects(env))
            intersectingGeoms.push_back(elem);
        else
            disjointGeoms.push_back(elem);
    }
}

/* private */
void
CascadedPolygonUnion::extractByEnvelope(geom::Envelope const& env,
    geom::Geometry* geom,
    std::vector<geom::Geometry*>& intersectingGeoms,
    std::vector<geom::Geometry*>& disjointGeoms)
{
    for (std::size_t i = 0; i < geom->getNumGeometries(); i++) {
        geom::Geometry* elem = const_cast<geom::Geometry*>(geom->getGeometryN(i));
        if (elem->getEnvelopeInternal()->intersects(env))
            intersectingGeoms.push_back(elem);
        else
            disjointGeoms.push_back(elem);
    }
}

/* private */
geom::Geometry*
CascadedPolygonUnion::extractByEnvelope(geom::Envelope const& env,
    geom::Geometry* geom, std::vector<geom::Geometry*>& disjointGeoms)
{
    std::vector<geom::Geometry*> intersectingGeoms;
    extractByEnvelope(env, geom, intersectingGeoms, disjointGeoms);

    return geomFactory->buildGeometry(intersectingGeoms);
}

geom::Geometry*
CascadedPolygonUnion::unionActual(geom::Geometry* g0, geom::Geometry* g1)
{
    return restrictToPolygons(std::unique_ptr<geom::Geometry>(g0->Union(g1))).release();
}

std::unique_ptr<geom::Geometry>
CascadedPolygonUnion::restrictToPolygons(std::unique_ptr<geom::Geometry> g)
{
    using namespace geom;
    using namespace std;

    if ( dynamic_cast<Polygonal*>(g.get()) ) {
      return g;
    }

    Polygon::ConstVect polygons;
    geom::util::PolygonExtracter::getPolygons(*g, polygons);

    if (polygons.size() == 1)
      return std::unique_ptr<Geometry>(polygons[0]->clone());

    typedef vector<Geometry *> GeomVect;

    Polygon::ConstVect::size_type n = polygons.size();
    GeomVect* newpolys = new GeomVect(n);
    for (Polygon::ConstVect::size_type i=0; i<n; ++i) {
        (*newpolys)[i] = polygons[i]->clone();
    }
    return unique_ptr<Geometry>(
      g->getFactory()->createMultiPolygon(newpolys)
    );

}

} // namespace geos.operation.union
} // namespace geos.operation
} // namespace geos
