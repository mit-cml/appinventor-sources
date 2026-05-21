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
 * Last port: geom/util/GeometryTransformer.java r320 (JTS-1.12)
 *
 **********************************************************************/

#include <geos/geom/util/GeometryTransformer.h>
#include <geos/geom/GeometryFactory.h>
#include <geos/geom/CoordinateSequenceFactory.h>
#include <geos/geom/Geometry.h>
#include <geos/geom/MultiPoint.h>
#include <geos/geom/MultiPolygon.h>
#include <geos/geom/MultiLineString.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/geom/Polygon.h>
#include <geos/geom/Point.h>
#include <geos/geom/LineString.h>
#include <geos/geom/LinearRing.h>
#include <geos/geom/GeometryCollection.h>
#include <geos/util/IllegalArgumentException.h>
#include <geos/util.h>

#include <typeinfo>
#include <cassert>

#ifndef GEOS_DEBUG
#define GEOS_DEBUG 0
#endif

#ifdef GEOS_DEBUG
#include <iostream>
#endif

using namespace std;

namespace geos {
namespace geom { // geos.geom
namespace util { // geos.geom.util


/*public*/
GeometryTransformer::GeometryTransformer()
	:
	factory(nullptr),
	inputGeom(nullptr),
	pruneEmptyGeometry(true),
	preserveGeometryCollectionType(true),
	preserveCollections(false),
	preserveType(false),
	skipTransformedInvalidInteriorRings(false)
{}

GeometryTransformer::~GeometryTransformer()
{
}

void GeometryTransformer::setSkipTransformedInvalidInteriorRings(bool b)
{
	skipTransformedInvalidInteriorRings = b;
}

/*public*/
unique_ptr<Geometry>
GeometryTransformer::transform(const Geometry* nInputGeom)
{
	using geos::util::IllegalArgumentException;

#if GEOS_DEBUG
	std::cerr << "GeometryTransformer::transform(Geometry " << nInputGeom << ");" << std::endl;
#endif

	inputGeom = nInputGeom;
	factory = inputGeom->getFactory();

	if ( const Point* p=dynamic_cast<const Point*>(inputGeom) )
		return transformPoint(p, nullptr);
	if ( const MultiPoint* mp=dynamic_cast<const MultiPoint*>(inputGeom) )
		return transformMultiPoint(mp, nullptr);
	if ( const LinearRing* lr=dynamic_cast<const LinearRing*>(inputGeom) )
		return transformLinearRing(lr, nullptr);
	if ( const LineString* ls=dynamic_cast<const LineString*>(inputGeom) )
		return transformLineString(ls, nullptr);
	if ( const MultiLineString* mls=dynamic_cast<const MultiLineString*>(inputGeom) )
		return transformMultiLineString(mls, nullptr);
	if ( const Polygon* p=dynamic_cast<const Polygon*>(inputGeom) )
		return transformPolygon(p, nullptr);
	if ( const MultiPolygon* mp=dynamic_cast<const MultiPolygon*>(inputGeom) )
		return transformMultiPolygon(mp, nullptr);
	if ( const GeometryCollection* gc=dynamic_cast<const GeometryCollection*>(inputGeom) )
		return transformGeometryCollection(gc, nullptr);

	throw IllegalArgumentException("Unknown Geometry subtype.");
}

std::unique_ptr<CoordinateSequence>
GeometryTransformer::createCoordinateSequence(
		std::unique_ptr< std::vector<Coordinate> > coords)
{
	return std::unique_ptr<CoordinateSequence>(
		factory->getCoordinateSequenceFactory()->create(
				coords.release())
	);
}

std::unique_ptr<CoordinateSequence>
GeometryTransformer::transformCoordinates(
		const CoordinateSequence* coords,
		const Geometry* parent)
{

    ::geos::ignore_unused_variable_warning(parent);
#if GEOS_DEBUG
	std::cerr << "GeometryTransformer::transformCoordinates(CoordinateSequence " << coords <<", Geometry " << parent << ");" << std::endl;
#endif

	return std::unique_ptr<CoordinateSequence>(coords->clone());
}

Geometry::Ptr
GeometryTransformer::transformPoint(
		const Point* geom,
		const Geometry* parent)
{
    ::geos::ignore_unused_variable_warning(parent);

#if GEOS_DEBUG
	std::cerr << "GeometryTransformer::transformPoint(Point " << geom <<", Geometry " << parent << ");" << std::endl;
#endif

	CoordinateSequence::Ptr cs(transformCoordinates(
		geom->getCoordinatesRO(), geom));

	return Geometry::Ptr(factory->createPoint(cs.release()));
}

Geometry::Ptr
GeometryTransformer::transformMultiPoint(
		const MultiPoint* geom,
		const Geometry* parent)
{
    ::geos::ignore_unused_variable_warning(parent);

#if GEOS_DEBUG
	std::cerr << "GeometryTransformer::transformMultiPoint(MultiPoint " << geom <<", Geometry " << parent << ");" << std::endl;
#endif

	vector<Geometry*>* transGeomList = new vector<Geometry*>();

	for (unsigned int i=0, n=static_cast<unsigned int>(geom->getNumGeometries()); i<n; i++)
	{
		const Point* p = dynamic_cast<const Point*>(geom->getGeometryN(i));
		assert(p);

		Geometry::Ptr transformGeom = transformPoint(p, geom);
		if ( transformGeom.get() == nullptr ) continue;
		if ( transformGeom->isEmpty() ) continue;

		// If an exception is thrown we'll leak
		transGeomList->push_back(transformGeom.release());
	}

	return Geometry::Ptr(factory->buildGeometry(transGeomList));

}

Geometry::Ptr
GeometryTransformer::transformLinearRing(
		const LinearRing* geom,
		const Geometry* parent)
{
    ::geos::ignore_unused_variable_warning(parent);

#if GEOS_DEBUG
	std::cerr << "GeometryTransformer::transformLinearRing(LinearRing " << geom <<", Geometry " << parent << ");" << std::endl;
#endif

	CoordinateSequence::Ptr seq(transformCoordinates(
		geom->getCoordinatesRO(), geom));

	auto seqSize = seq->size();

	// ensure a valid LinearRing
	if ( seqSize > 0 && seqSize < 4 && ! preserveType )
	{
		return factory->createLineString(std::move(seq));
	}
	else
	{
		return factory->createLinearRing(std::move(seq));
	}
}

Geometry::Ptr
GeometryTransformer::transformLineString(
		const LineString* geom,
		const Geometry* parent)
{
    ::geos::ignore_unused_variable_warning(parent);

#if GEOS_DEBUG
	std::cerr << "GeometryTransformer::transformLineString(LineString " << geom <<", Geometry " << parent << ");" << std::endl;
#endif

	// should check for 1-point sequences and downgrade them to points
	return factory->createLineString(
		transformCoordinates(geom->getCoordinatesRO(), geom));
}

Geometry::Ptr
GeometryTransformer::transformMultiLineString(
		const MultiLineString* geom,
		const Geometry* parent)
{
    ::geos::ignore_unused_variable_warning(parent);

#if GEOS_DEBUG
	std::cerr << "GeometryTransformer::transformMultiLineString(MultiLineString " << geom <<", Geometry " << parent << ");" << std::endl;
#endif

	vector<Geometry*>* transGeomList = new vector<Geometry*>();

	for (unsigned int i=0, n=static_cast<unsigned int>(geom->getNumGeometries()); i<n; i++)
	{
		const LineString* l = dynamic_cast<const LineString*>(
				geom->getGeometryN(i));
		assert(l);

		Geometry::Ptr transformGeom = transformLineString(l, geom);
		if ( transformGeom.get() == nullptr ) continue;
		if ( transformGeom->isEmpty() ) continue;

		// If an exception is thrown we'll leak
		transGeomList->push_back(transformGeom.release());
	}

	return Geometry::Ptr(factory->buildGeometry(transGeomList));

}

Geometry::Ptr
GeometryTransformer::transformPolygon(
		const Polygon* geom,
		const Geometry* parent)
{
    ::geos::ignore_unused_variable_warning(parent);

#if GEOS_DEBUG
	std::cerr << "GeometryTransformer::transformPolygon(Polygon " << geom <<", Geometry " << parent << ");" << std::endl;
#endif

	bool isAllValidLinearRings = true;

	const LinearRing* lr = dynamic_cast<const LinearRing*>(
			geom->getExteriorRing());
	assert(lr);

	Geometry::Ptr shell = transformLinearRing(lr, geom);
	if ( shell.get() == nullptr
		|| ! dynamic_cast<LinearRing*>(shell.get())
		|| shell->isEmpty() )
	{
		isAllValidLinearRings = false;
	}

	vector<Geometry*>* holes = new vector<Geometry*>();
	for (unsigned int i=0, n=static_cast<unsigned int>(geom->getNumInteriorRing()); i<n; i++)
	{
		const LinearRing* lr = dynamic_cast<const LinearRing*>(
			geom->getInteriorRingN(i));
		assert(lr);

		Geometry::Ptr hole(transformLinearRing(lr, geom));

		if ( hole.get() == nullptr || hole->isEmpty() ) {
			continue;
		}

		if ( ! dynamic_cast<LinearRing*>(hole.get()) )
		{
			if ( skipTransformedInvalidInteriorRings )
			    continue;
			isAllValidLinearRings = false;
		}

		holes->push_back(hole.release());
	}

	if ( isAllValidLinearRings)
	{
		Geometry* sh = shell.release();
		LinearRing* lr = dynamic_cast<LinearRing*>(sh);
    assert(lr);
		return Geometry::Ptr(factory->createPolygon(lr, holes));
	}
	else
	{
		// would like to use a manager constructor here
		vector<Geometry*>* components = new vector<Geometry*>();
		if ( shell.get() != nullptr ) {
			components->push_back(shell.release());
		}

		components->insert(components->end(),
			holes->begin(), holes->end());

		delete holes; // :(

		return Geometry::Ptr(factory->buildGeometry(components));
	}

}

Geometry::Ptr
GeometryTransformer::transformMultiPolygon(
		const MultiPolygon* geom,
		const Geometry* parent)
{
    ::geos::ignore_unused_variable_warning(parent);

#if GEOS_DEBUG
	std::cerr << "GeometryTransformer::transformMultiPolygon(MultiPolygon " << geom <<", Geometry " << parent << ");" << std::endl;
#endif

	unique_ptr< vector<Geometry*> > transGeomList( new vector<Geometry*>() );

	for (std::size_t i=0, n=geom->getNumGeometries(); i<n; i++)
	{
		const Polygon* p = dynamic_cast<const Polygon*>(
				geom->getGeometryN(i));
		assert(p);

		Geometry::Ptr transformGeom = transformPolygon(p, geom);
		if ( transformGeom.get() == nullptr ) continue;
		if ( transformGeom->isEmpty() ) continue;

		// If an exception is thrown we'll leak
		transGeomList->push_back(transformGeom.release());
	}

	return Geometry::Ptr(factory->buildGeometry(transGeomList.release()));

}

Geometry::Ptr
GeometryTransformer::transformGeometryCollection(
		const GeometryCollection* geom,
		const Geometry* parent)
{
    ::geos::ignore_unused_variable_warning(parent);

#if GEOS_DEBUG
	std::cerr << "GeometryTransformer::transformGeometryCollection(GeometryCollection " << geom <<", Geometry " << parent << ");" << std::endl;
#endif

	vector<Geometry*>* transGeomList = new vector<Geometry*>();

	for (std::size_t i=0, n=geom->getNumGeometries(); i<n; i++)
	{
		Geometry::Ptr transformGeom = transform(
			geom->getGeometryN(i)); // no parent ?
		if ( transformGeom.get() == nullptr ) continue;
		if ( pruneEmptyGeometry && transformGeom->isEmpty() ) continue;

		// If an exception is thrown we'll leak
		transGeomList->push_back(transformGeom.release());
	}

	if ( preserveGeometryCollectionType )
	{
		return Geometry::Ptr(factory->createGeometryCollection(
			transGeomList));
	}
	else
	{
		return Geometry::Ptr(factory->buildGeometry(transGeomList));
	}

}


} // namespace geos.geom.util
} // namespace geos.geom
} // namespace geos
