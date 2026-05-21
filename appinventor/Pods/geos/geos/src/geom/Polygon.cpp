/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2011 Sandro Santilli <strk@kbt.io>
 * Copyright (C) 2005-2006 Refractions Research Inc.
 * Copyright (C) 2001-2002 Vivid Solutions Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: geom/Polygon.java r320 (JTS-1.12)
 *
 **********************************************************************/

#include <geos/algorithm/CGAlgorithms.h>
#include <geos/util/IllegalArgumentException.h>
#include <geos/geom/Coordinate.h>
#include <geos/geom/Polygon.h>
#include <geos/geom/LinearRing.h>
#include <geos/geom/MultiLineString.h> // for getBoundary()
#include <geos/geom/GeometryFactory.h>
#include <geos/geom/Dimension.h>
#include <geos/geom/Envelope.h>
#include <geos/geom/CoordinateSequenceFactory.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/geom/CoordinateSequenceFilter.h>
#include <geos/geom/GeometryFilter.h>
#include <geos/geom/GeometryComponentFilter.h>

#include <vector>
#include <cmath> // for fabs
#include <cassert>
#include <algorithm>
#include <memory>

#ifndef GEOS_DEBUG
#define GEOS_DEBUG 0
#endif

using namespace std;
//using namespace geos::algorithm;

namespace geos {
namespace geom { // geos::geom

/*protected*/
Polygon::Polygon(const Polygon &p)
	:
	Geometry(p)
{
	shell=new LinearRing(*p.shell);
	size_t nholes=p.holes->size();
	holes=new vector<Geometry *>(nholes);
	for(size_t i=0; i<nholes; ++i)
	{
    // TODO: holes is a vector of Geometry, anyway
    //       so there's no point in casting here,
    //       just use ->clone instead !
		const LinearRing* lr = dynamic_cast<const LinearRing *>((*p.holes)[i]);
		LinearRing *h=new LinearRing(*lr);
		(*holes)[i]=h;
	}
}

/*protected*/
Polygon::Polygon(LinearRing *newShell, vector<Geometry *> *newHoles,
		const GeometryFactory *newFactory):
	Geometry(newFactory)
{
	if (newShell==nullptr) {
		shell=getFactory()->createLinearRing(nullptr);
	}
	else
	{
		if (newHoles != nullptr && newShell->isEmpty() && hasNonEmptyElements(newHoles)) {
			throw util::IllegalArgumentException("shell is empty but holes are not");
		}
		shell=newShell;
	}

	if (newHoles==nullptr)
	{
		holes=new vector<Geometry *>();
	}
	else
	{
		if (hasNullElements(newHoles)) {
			throw util::IllegalArgumentException("holes must not contain null elements");
		}
		for (size_t i=0; i<newHoles->size(); i++)
			if ( (*newHoles)[i]->getGeometryTypeId() != GEOS_LINEARRING)
				throw util::IllegalArgumentException("holes must be LinearRings");
		holes=newHoles;
	}
}

CoordinateSequence*
Polygon::getCoordinates() const
{
	if (isEmpty()) {
		return getFactory()->getCoordinateSequenceFactory()->create();
	}

	vector<Coordinate> *cl = new vector<Coordinate>;

	// reserve space in the vector for all the polygon points
	cl->reserve(getNumPoints());

	// Add shell points
	const CoordinateSequence* shellCoords=shell->getCoordinatesRO();
	shellCoords->toVector(*cl);

	// Add holes points
	size_t nholes=holes->size();
	for (size_t i=0; i<nholes; ++i)
	{
		const LinearRing* lr = dynamic_cast<const LinearRing *>((*holes)[i]);
		const CoordinateSequence* childCoords = lr->getCoordinatesRO();
		childCoords->toVector(*cl);
	}

	return getFactory()->getCoordinateSequenceFactory()->create(cl);
}

size_t
Polygon::getNumPoints() const
{
	size_t numPoints = shell->getNumPoints();
	for(size_t i=0, n=holes->size(); i<n; ++i)
	{
		const LinearRing* lr = dynamic_cast<const LinearRing *>((*holes)[i]);
		numPoints += lr->getNumPoints();
	}
	return numPoints;
}

Dimension::DimensionType
Polygon::getDimension() const
{
	return Dimension::A; // area
}

int
Polygon::getCoordinateDimension() const
{
	int dimension=2;

	if( shell != nullptr )
		dimension = max(dimension,shell->getCoordinateDimension());

	size_t nholes=holes->size();
	for (size_t i=0; i<nholes; ++i)
	{
        dimension = max(dimension,(*holes)[i]->getCoordinateDimension());
	}

	return dimension;
}

int
Polygon::getBoundaryDimension() const
{
	return 1;
}

bool
Polygon::isEmpty() const
{
	return shell->isEmpty();
}

bool
Polygon::isSimple() const
{
	return true;
}

const LineString*
Polygon::getExteriorRing() const
{
	return shell;
}

size_t
Polygon::getNumInteriorRing() const
{
	return holes->size();
}

const LineString*
Polygon::getInteriorRingN(size_t n) const
{
  const LinearRing* lr = dynamic_cast<const LinearRing *>((*holes)[n]);
	return lr;
}

string
Polygon::getGeometryType() const
{
	return "Polygon";
}

// Returns a newly allocated Geometry object
/*public*/
Geometry*
Polygon::getBoundary() const
{
	/*
	 * We will make sure that what we
	 * return is composed of LineString,
	 * not LinearRings
	 */

	const GeometryFactory* gf = getFactory();

	if (isEmpty()) {
		return gf->createMultiLineString();
	}

	if ( ! holes->size() )
	{
		return gf->createLineString(*shell).release();
	}

	vector<Geometry *> *rings = new vector<Geometry *>(holes->size()+1);

	(*rings)[0] = gf->createLineString(*shell).release();
	for(size_t i=0, n=holes->size(); i<n; ++i)
	{
		const LinearRing* hole = dynamic_cast<const LinearRing *>((*holes)[i]);
		assert( hole );
		LineString* ls = gf->createLineString( *hole ).release();
		(*rings)[i + 1] = ls;
	}
	MultiLineString *ret = getFactory()->createMultiLineString(rings);
	return ret;
}

Envelope::Ptr
Polygon::computeEnvelopeInternal() const
{
	return Envelope::Ptr(new Envelope(*(shell->getEnvelopeInternal())));
}

bool
Polygon::equalsExact(const Geometry *other, double tolerance) const
{
	const Polygon* otherPolygon=dynamic_cast<const Polygon*>(other);
	if ( ! otherPolygon ) return false;

	if (!shell->equalsExact(otherPolygon->shell, tolerance)) {
		return false;
	}

	size_t nholes = holes->size();

	if (nholes != otherPolygon->holes->size()) {
		return false;
	}

	for (size_t i=0; i<nholes; i++)
	{
		const Geometry* hole=(*holes)[i];
		const Geometry* otherhole=(*(otherPolygon->holes))[i];
		if (!hole->equalsExact(otherhole, tolerance))
		{
			return false;
		}
	}

	return true;
}

void
Polygon::apply_ro(CoordinateFilter *filter) const
{
	shell->apply_ro(filter);
	for(size_t i=0, n=holes->size(); i<n; ++i)
	{
		const LinearRing* lr = dynamic_cast<const LinearRing *>((*holes)[i]);
		lr->apply_ro(filter);
	}
}

void
Polygon::apply_rw(const CoordinateFilter *filter)
{
	shell->apply_rw(filter);
	for(size_t i=0, n=holes->size(); i<n; ++i)
	{
		LinearRing* lr = dynamic_cast<LinearRing *>((*holes)[i]);
		lr->apply_rw(filter);
	}
}

void
Polygon::apply_rw(GeometryFilter *filter)
{
	filter->filter_rw(this);
}

void
Polygon::apply_ro(GeometryFilter *filter) const
{
	filter->filter_ro(this);
}

Geometry*
Polygon::convexHull() const
{
	return getExteriorRing()->convexHull();
}

void
Polygon::normalize()
{
	normalize(shell, true);
	for(size_t i=0, n=holes->size(); i<n; ++i)
	{
		LinearRing* lr = dynamic_cast<LinearRing *>((*holes)[i]);
		normalize(lr, false);
	}
	sort(holes->begin(), holes->end(), GeometryGreaterThen());
}

int
Polygon::compareToSameClass(const Geometry *g) const
{
	const Polygon* p = dynamic_cast<const Polygon*>(g);
	return shell->compareToSameClass(p->shell);
}

/*
 * TODO: check this function, there should be CoordinateSequence copy
 *       reduction possibility.
 */
void
Polygon::normalize(LinearRing *ring, bool clockwise)
{
	if (ring->isEmpty()) {
		return;
	}
	CoordinateSequence* uniqueCoordinates=ring->getCoordinates();
	uniqueCoordinates->deleteAt(uniqueCoordinates->getSize()-1);
	const Coordinate* minCoordinate=CoordinateSequence::minCoordinate(uniqueCoordinates);
	CoordinateSequence::scroll(uniqueCoordinates, minCoordinate);
	uniqueCoordinates->add(uniqueCoordinates->getAt(0));
	if (algorithm::CGAlgorithms::isCCW(uniqueCoordinates)==clockwise) {
		CoordinateSequence::reverse(uniqueCoordinates);
	}
	ring->setPoints(uniqueCoordinates);
	delete(uniqueCoordinates);
}

const Coordinate*
Polygon::getCoordinate() const
{
	return shell->getCoordinate();
}

/*
 *  Returns the area of this <code>Polygon</code>
 *
 * @return the area of the polygon
 */
double
Polygon::getArea() const
{
	double area=0.0;
	area+=fabs(algorithm::CGAlgorithms::signedArea(shell->getCoordinatesRO()));
	for(size_t i=0, n=holes->size(); i<n; ++i)
	{
		const LinearRing *lr = dynamic_cast<const LinearRing *>((*holes)[i]);
		const CoordinateSequence *h=lr->getCoordinatesRO();
        	area-=fabs(algorithm::CGAlgorithms::signedArea(h));
	}
	return area;
}

/**
 * Returns the perimeter of this <code>Polygon</code>
 *
 * @return the perimeter of the polygon
 */
double
Polygon::getLength() const
{
	double len=0.0;
	len+=shell->getLength();
	for(size_t i=0, n=holes->size(); i<n; ++i)
	{
		len+=(*holes)[i]->getLength();
	}
	return len;
}

void
Polygon::apply_ro(GeometryComponentFilter *filter) const
{
	filter->filter_ro(this);
	shell->apply_ro(filter);
	for(size_t i=0, n=holes->size(); i<n; ++i)
	{
        	(*holes)[i]->apply_ro(filter);
	}
}

void
Polygon::apply_rw(GeometryComponentFilter *filter)
{
	filter->filter_rw(this);
	shell->apply_rw(filter);
	for(size_t i=0, n=holes->size(); i<n; ++i)
	{
        	(*holes)[i]->apply_rw(filter);
	}
}

void
Polygon::apply_rw(CoordinateSequenceFilter& filter)
{
	shell->apply_rw(filter);

	if (! filter.isDone())
	{
		for (size_t i=0, n=holes->size(); i<n; ++i)
		{
			(*holes)[i]->apply_rw(filter);
			if (filter.isDone())
			break;
        	}
	}
	if (filter.isGeometryChanged()) geometryChanged();
}

void
Polygon::apply_ro(CoordinateSequenceFilter& filter) const
{
	shell->apply_ro(filter);

	if (! filter.isDone())
	{
		for (size_t i=0, n=holes->size(); i<n; ++i)
		{
			(*holes)[i]->apply_ro(filter);
			if (filter.isDone())
			break;
        	}
	}
	//if (filter.isGeometryChanged()) geometryChanged();
}

Polygon::~Polygon()
{
	delete shell;
	for(size_t i=0, n=holes->size(); i<n; ++i)
	{
		delete (*holes)[i];
	}
	delete holes;
}

GeometryTypeId
Polygon::getGeometryTypeId() const
{
	return GEOS_POLYGON;
}

bool
Polygon::isRectangle() const
{
	if ( getNumInteriorRing() != 0 ) return false;
	assert(shell!=nullptr);
	if ( shell->getNumPoints() != 5 ) return false;

	const CoordinateSequence &seq = *(shell->getCoordinatesRO());

	// check vertices have correct values
	const Envelope &env = *getEnvelopeInternal();
	for (int i=0; i<5; i++) {
		double x = seq.getX(i);
		if (! (x == env.getMinX() || x == env.getMaxX())) return false;
		double y = seq.getY(i);
		if (! (y == env.getMinY() || y == env.getMaxY())) return false;
	}

	// check vertices are in right order
	double prevX = seq.getX(0);
	double prevY = seq.getY(0);
	for (int i = 1; i <= 4; i++) {
		double x = seq.getX(i);
		double y = seq.getY(i);
		bool xChanged = (x != prevX);
		bool yChanged = (y != prevY);
		if (xChanged == yChanged) return false;
		prevX = x;
		prevY = y;
	}
	return true;
}

Geometry*
Polygon::reverse() const
{
	if (isEmpty()) {
		return clone();
	}

	auto* exteriorRingReversed = dynamic_cast<LinearRing*>(shell->reverse());
	auto* interiorRingsReversed = new std::vector<Geometry*>{holes->size()};

	std::transform(holes->begin(),
				   holes->end(),
				   interiorRingsReversed->begin(),
				   [](const Geometry * g) {
		             return g->reverse();
	});

	return getFactory()->createPolygon(exteriorRingReversed, interiorRingsReversed);
}

} // namespace geos::geom
} // namespace geos
