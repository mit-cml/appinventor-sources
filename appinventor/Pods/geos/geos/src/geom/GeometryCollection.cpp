/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2001-2002 Vivid Solutions Inc.
 * Copyright (C) 2005 2006 Refractions Research Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: geom/GeometryCollection.java rev. 1.41
 *
 **********************************************************************/

#include <geos/geom/GeometryCollection.h>
#include <geos/algorithm/CGAlgorithms.h>
#include <geos/util/IllegalArgumentException.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/geom/CoordinateSequenceFilter.h>
#include <geos/geom/CoordinateArraySequenceFactory.h>
#include <geos/geom/Dimension.h>
#include <geos/geom/GeometryFactory.h>
#include <geos/geom/GeometryFilter.h>
#include <geos/geom/GeometryComponentFilter.h>
#include <geos/geom/Envelope.h>

#ifndef GEOS_INLINE
# include <geos/geom/GeometryCollection.inl>
#endif

#include <algorithm>
#include <vector>
#include <memory>

using namespace std;

namespace geos {
namespace geom { // geos::geom

/*protected*/
GeometryCollection::GeometryCollection(const GeometryCollection &gc)
	:
	Geometry(gc)
{
	size_t ngeoms=gc.geometries->size();

	geometries=new vector<Geometry *>(ngeoms);
	for(size_t i=0; i<ngeoms; ++i)
	{
		(*geometries)[i]=(*gc.geometries)[i]->clone();
    // Drop SRID from inner geoms
		(*geometries)[i]->setSRID(0);
	}
}

/*protected*/
GeometryCollection::GeometryCollection(vector<Geometry *> *newGeoms, const GeometryFactory *factory):
	Geometry(factory)
{
	if (newGeoms==nullptr) {
		geometries=new vector<Geometry *>();
		return;
	}
	if (hasNullElements(newGeoms)) {
		throw  util::IllegalArgumentException("geometries must not contain null elements\n");
		return;
	}
	geometries=newGeoms;

  // Drop SRID from inner geoms
	size_t ngeoms=geometries->size();
	for(size_t i=0; i<ngeoms; ++i)
	{
		(*geometries)[i]->setSRID(0);
	}
}

/*
 * Collects all coordinates of all subgeometries into a CoordinateSequence.
 *
 * Returns a newly the collected coordinates
 *
 */
CoordinateSequence *
GeometryCollection::getCoordinates() const
{
	vector<Coordinate> *coordinates = new vector<Coordinate>(getNumPoints());

	int k = -1;
	for (size_t i=0; i<geometries->size(); ++i) {
		CoordinateSequence* childCoordinates=(*geometries)[i]->getCoordinates();
		size_t npts=childCoordinates->getSize();
		for (size_t j=0; j<npts; ++j) {
			k++;
			(*coordinates)[k] = childCoordinates->getAt(j);
		}
		delete childCoordinates;
	}
	return CoordinateArraySequenceFactory::instance()->create(coordinates);
}

bool
GeometryCollection::isEmpty() const
{
	for (size_t i=0; i<geometries->size(); ++i) {
		if (!(*geometries)[i]->isEmpty()) {
			return false;
		}
	}
	return true;
}

Dimension::DimensionType
GeometryCollection::getDimension() const
{
	Dimension::DimensionType dimension=Dimension::False;
	for (size_t i=0, n=geometries->size(); i<n; ++i)
	{
		dimension=max(dimension,(*geometries)[i]->getDimension());
	}
	return dimension;
}

int
GeometryCollection::getBoundaryDimension() const
{
	int dimension=Dimension::False;
	for(size_t i=0; i<geometries->size(); ++i) {
		dimension=max(dimension,(*geometries)[i]->getBoundaryDimension());
	}
	return dimension;
}

int
GeometryCollection::getCoordinateDimension() const
{
	int dimension=2;

	for (size_t i=0, n=geometries->size(); i<n; ++i)
	{
		dimension=max(dimension,(*geometries)[i]->getCoordinateDimension());
	}
	return dimension;
}

size_t
GeometryCollection::getNumGeometries() const
{
	return geometries->size();
}

const Geometry*
GeometryCollection::getGeometryN(size_t n) const
{
	return (*geometries)[n];
}

size_t
GeometryCollection::getNumPoints() const
{
	size_t numPoints = 0;
	for (size_t i=0, n=geometries->size(); i<n; ++i)
	{
		numPoints +=(*geometries)[i]->getNumPoints();
	}
	return numPoints;
}

string
GeometryCollection::getGeometryType() const
{
	return "GeometryCollection";
}

Geometry*
GeometryCollection::getBoundary() const
{
	throw util::IllegalArgumentException("Operation not supported by GeometryCollection\n");
}

bool
GeometryCollection::equalsExact(const Geometry *other, double tolerance) const
{
	if (!isEquivalentClass(other)) return false;

	const GeometryCollection* otherCollection=dynamic_cast<const GeometryCollection *>(other);
	if ( ! otherCollection ) return false;

	if (geometries->size()!=otherCollection->geometries->size()) {
		return false;
	}
	for (size_t i=0; i<geometries->size(); ++i) {
		if (!((*geometries)[i]->equalsExact((*(otherCollection->geometries))[i],tolerance)))
		{
			return false;
		}
	}
	return true;
}

void
GeometryCollection::apply_rw(const CoordinateFilter *filter)
{
	for (size_t i=0; i<geometries->size(); ++i)
	{
		(*geometries)[i]->apply_rw(filter);
	}
}

void
GeometryCollection::apply_ro(CoordinateFilter *filter) const
{
	for (size_t i=0; i<geometries->size(); ++i)
	{
		(*geometries)[i]->apply_ro(filter);
	}
}

void
GeometryCollection::apply_ro(GeometryFilter *filter) const
{
	filter->filter_ro(this);
	for(size_t i=0; i<geometries->size(); ++i)
	{
		(*geometries)[i]->apply_ro(filter);
	}
}

void
GeometryCollection::apply_rw(GeometryFilter *filter)
{
	filter->filter_rw(this);
	for(size_t i=0; i<geometries->size(); ++i)
	{
		(*geometries)[i]->apply_rw(filter);
	}
}

void
GeometryCollection::normalize()
{
	for (size_t i=0; i<geometries->size(); ++i) {
		(*geometries)[i]->normalize();
	}
	sort(geometries->begin(), geometries->end(), GeometryGreaterThen());
}

Envelope::Ptr
GeometryCollection::computeEnvelopeInternal() const
{
	Envelope::Ptr envelope(new Envelope());
	for (size_t i=0; i<geometries->size(); i++) {
		const Envelope *env=(*geometries)[i]->getEnvelopeInternal();
		envelope->expandToInclude(env);
	}
	return envelope;
}

int
GeometryCollection::compareToSameClass(const Geometry *g) const
{
  const GeometryCollection* gc = dynamic_cast<const GeometryCollection*>(g);
	return compare(*geometries, *(gc->geometries));
}

const Coordinate*
GeometryCollection::getCoordinate() const
{
	// should use unique_ptr here or return NULL or throw an exception !
	// 	--strk;
	if (isEmpty()) return new Coordinate();
    	return (*geometries)[0]->getCoordinate();
}

/**
 * @return the area of this collection
 */
double
GeometryCollection::getArea() const
{
	double area=0.0;
	for(size_t i=0; i<geometries->size(); ++i)
	{
        	area+=(*geometries)[i]->getArea();
	}
	return area;
}

/**
 * @return the total length of this collection
 */
double
GeometryCollection::getLength() const
{
	double sum=0.0;
	for(size_t i=0; i<geometries->size(); ++i)
	{
        	sum+=(*geometries)[i]->getLength();
	}
	return sum;
}

void
GeometryCollection::apply_rw(GeometryComponentFilter *filter)
{
	filter->filter_rw(this);
	for(size_t i=0; i<geometries->size(); ++i)
	{
        	(*geometries)[i]->apply_rw(filter);
	}
}

void
GeometryCollection::apply_ro(GeometryComponentFilter *filter) const
{
	filter->filter_ro(this);
	for(size_t i=0; i<geometries->size(); ++i)
	{
		(*geometries)[i]->apply_ro(filter);
	}
}

void
GeometryCollection::apply_rw(CoordinateSequenceFilter& filter)
{
	size_t ngeoms = geometries->size();
	if (ngeoms == 0 ) return;
	for (size_t i = 0; i < ngeoms; ++i)
	{
		(*geometries)[i]->apply_rw(filter);
		if (filter.isDone()) break;
	}
	if (filter.isGeometryChanged()) geometryChanged();
}

void
GeometryCollection::apply_ro(CoordinateSequenceFilter& filter) const
{
	size_t ngeoms = geometries->size();
	if (ngeoms == 0 ) return;
	for (size_t i = 0; i < ngeoms; ++i)
	{
		(*geometries)[i]->apply_ro(filter);
		if (filter.isDone()) break;
	}

	assert(!filter.isGeometryChanged()); // read-only filter...
	//if (filter.isGeometryChanged()) geometryChanged();
}

GeometryCollection::~GeometryCollection()
{
	for(size_t i=0; i<geometries->size(); ++i)
	{
		delete (*geometries)[i];
	}
	delete geometries;
}

GeometryTypeId
GeometryCollection::getGeometryTypeId() const
{
	return GEOS_GEOMETRYCOLLECTION;
}

Geometry*
GeometryCollection::reverse() const
{
	if (isEmpty()) {
		return clone();
	}

	auto* reversed = new std::vector<Geometry*>{geometries->size()};

	std::transform(geometries->begin(),
				   geometries->end(),
				   reversed->begin(),
				   [](const Geometry* g) {
					   return g->reverse();
				   });

	return getFactory()->createGeometryCollection(reversed);
}

} // namespace geos::geom
} // namespace geos
