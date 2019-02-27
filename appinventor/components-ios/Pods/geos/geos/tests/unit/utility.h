//
// GEOS Unit Test utilities, extension of TUT Framework namespace
//
#ifndef GEOS_TUT_UTILITY_H_INCLUDED
#define GEOS_TUT_UTILITY_H_INCLUDED

// tut
#include <tut/tut.hpp>
// geos
#include <geos/geom/Geometry.h>
#include <geos/geom/GeometryCollection.h>
#include <geos/geom/Coordinate.h>
#include <geos/geom/CoordinateArraySequence.h>
#include <geos/geom/CoordinateArraySequenceFactory.h>
#include <geos/geom/CoordinateSequenceFactory.h>
#include <geos/geom/Dimension.h>
#include <geos/geom/Point.h>
#include <geos/geom/Polygon.h>
#include <geos/geom/LinearRing.h>
#include <geos/geom/LineString.h>
#include <geos/geom/MultiPoint.h>
#include <geos/geom/MultiLineString.h>
#include <geos/geom/MultiPolygon.h>
#include <geos/geom/PrecisionModel.h>
#include <geos/geom/GeometryFactory.h>
#include <geos/geom/prep/PreparedGeometry.h>
#include <geos/io/WKTReader.h>
// std
#include <memory>
#include <cstdlib>
#include <cassert>
#include <string>
#include <vector>

namespace tut
{

//
// Helper typedefs
//
typedef geos::geom::Coordinate* CoordinatePtr;
typedef geos::geom::Coordinate const* CoordinateCPtr;

typedef geos::geom::CoordinateSequence* CoordSeqPtr;
typedef geos::geom::CoordinateSequence const* CoordSeqCPtr;

typedef geos::geom::CoordinateArraySequence* CoordArrayPtr;
typedef geos::geom::CoordinateArraySequence const* CoordArrayCPtr;

typedef geos::geom::Geometry* GeometryPtr;
typedef geos::geom::Geometry const* GeometryCPtr;

typedef geos::geom::Point* PointPtr;
typedef geos::geom::Point const* PointCPtr;
typedef geos::geom::LinearRing* LinearRingPtr;
typedef geos::geom::LinearRing const* LinearRingCPtr;
typedef geos::geom::LineString* LineStringPtr;
typedef geos::geom::LineString const* LineStringCPtr;
typedef geos::geom::Polygon* PolygonPtr;
typedef geos::geom::Polygon const* PolygonCPtr;

typedef geos::geom::GeometryCollection* GeometryColPtr;
typedef geos::geom::GeometryCollection const* GeometryColCPtr;

typedef geos::geom::MultiPoint* MultiPointPtr;
typedef geos::geom::MultiPoint const* MultiPointCPtr;
typedef geos::geom::MultiLineString* MultiLineStringPtr;
typedef geos::geom::MultiLineString const* MultiLineStringCPtr;
typedef geos::geom::MultiPolygon* MultiPolygonPtr;
typedef geos::geom::MultiPolygon const* MultiPolygonCPtr;

// prepared geometries always returend as const
typedef geos::geom::prep::PreparedGeometry const* PreparedGeometryPtr;

//
// Type cast helper utilities
//

template<typename Type, typename InstanceType>
inline bool isInstanceOf(InstanceType const* instance)
{
    assert(nullptr != instance);
    return (nullptr != dynamic_cast<Type const*>(instance) );
}

template<typename Type, typename InstanceType>
inline Type const* instanceOf(InstanceType const* instance)
{
    assert(nullptr != instance);
    return dynamic_cast<Type const*>(instance);
}

//
// Geometries structure comparators
//

template <typename T1, typename T2>
inline void ensure_equals_geometry(T1 const* lhs, T2 const* rhs)
{
    assert(0 != lhs);
    assert(0 != rhs);

    assert(!"DIFFERENT TYPES ARE NOT OF THE SAME STRUCTURE");
}

template <typename T>
inline void ensure_equals_geometry(T const* lhs, T const* rhs)
{
    assert(nullptr != lhs);
    assert(nullptr != rhs);

    using geos::geom::Polygon;
    using geos::geom::GeometryCollection;

    ensure_equals("is-valid do not match",
                  lhs->isValid(), rhs->isValid());

    ensure_equals("is-empty do not match",
                  lhs->isEmpty(), rhs->isEmpty());

    if (!isInstanceOf<GeometryCollection>(lhs)
         && !isInstanceOf<GeometryCollection>(rhs))
    {
        ensure_equals("is-simple do not match",
                      lhs->isSimple(), rhs->isSimple());
    }

    ensure_equals("type do not match",
                  lhs->getGeometryType(), rhs->getGeometryType());

    ensure_equals("type id do not match",
                  lhs->getGeometryTypeId(), rhs->getGeometryTypeId());

    ensure_equals("dimension do not match",
                  lhs->getDimension(), rhs->getDimension());

    ensure_equals("boundary dimension do not match",
                  lhs->getBoundaryDimension(), rhs->getBoundaryDimension());

    // NOTE - mloskot: Intentionally disabled, so simplified geometry
    // can be compared to its original
    //ensure_equals("number of points do not match",
    //              lhs->getNumPoints(), rhs->getNumPoints());

    // Dispatch to run more specific testes
    if (isInstanceOf<Polygon>(lhs)
        && isInstanceOf<Polygon>(rhs))
    {
        ensure_equals_geometry(instanceOf<Polygon>(lhs),
                        instanceOf<Polygon>(rhs));
    }
    else if (isInstanceOf<GeometryCollection>(lhs)
             && isInstanceOf<GeometryCollection>(rhs))
    {
        ensure_equals_geometry(instanceOf<GeometryCollection>(lhs),
                        instanceOf<GeometryCollection>(rhs));
    }
}

template <>
inline void ensure_equals_geometry(geos::geom::Polygon const* lhs,
                                   geos::geom::Polygon const* rhs)
{
    assert(nullptr != lhs);
    assert(nullptr != rhs);

    ensure_equals("number of interior ring do not match",
                  lhs->getNumInteriorRing(), rhs->getNumInteriorRing());
}

template <>
inline void ensure_equals_geometry(geos::geom::GeometryCollection const* lhs,
                                   geos::geom::GeometryCollection const* rhs)
{
    assert(nullptr != lhs);
    assert(nullptr != rhs);

    using geos::geom::Geometry;

    ensure_equals("number of geometries do not match",
                  lhs->getNumGeometries(), rhs->getNumGeometries());

    for (std::size_t i = 0, n = lhs->getNumGeometries(); i < n; ++i)
    {
        Geometry const* g1 = lhs->getGeometryN(i);
        Geometry const* g2 = rhs->getGeometryN(i);
        ensure_equals_geometry(g1, g2); // breaks on failure
    }
}

template <>
inline void ensure_equals_geometry(geos::geom::Geometry const* lhs,
                                   geos::geom::prep::PreparedGeometry const* rhs)
{
    assert(nullptr != lhs);
    assert(nullptr != rhs);

    geos::geom::Geometry const& pg = rhs->getGeometry();
    ensure_equals_geometry(lhs, &pg);
}

//
// Utility functions
//

// Decodes hex-encoded WKB/EWKB to raw binary.
struct wkb_hex_decoder
{
    typedef std::vector<unsigned char> binary_type;

    // bytes [out] - buffer for binary output
    static void decode(std::string const& hexstr, binary_type& bytes)
    {
        bytes.clear();
        for(std::string::size_type i = 0; i < hexstr.size() / 2; ++i)
        {
            std::istringstream iss(hexstr.substr(i * 2, 2));
            unsigned int n;
            iss >> std::hex >> n;
            bytes.push_back(static_cast<unsigned char>(n));
        }
    }
};


} // namespace tut

#endif // #ifndef GEOS_TUT_UTILITY_H_INCLUDED

