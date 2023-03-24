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
 * Last port: geom/LineString.java r320 (JTS-1.12)
 *
 **********************************************************************/

#include <geos/util/IllegalArgumentException.h>
#include <geos/algorithm/CGAlgorithms.h>
#include <geos/geom/Coordinate.h>
#include <geos/geom/CoordinateSequenceFactory.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/geom/CoordinateSequenceFilter.h>
#include <geos/geom/CoordinateFilter.h>
#include <geos/geom/Dimension.h>
#include <geos/geom/GeometryFilter.h>
#include <geos/geom/GeometryComponentFilter.h>
#include <geos/geom/GeometryFactory.h>
#include <geos/geom/LineString.h>
#include <geos/geom/Point.h>
#include <geos/geom/MultiPoint.h> // for getBoundary
#include <geos/geom/Envelope.h>

#include <algorithm>
#include <typeinfo>
#include <memory>
#include <cassert>

using namespace std;
using namespace geos::algorithm;

namespace geos {
namespace geom { // geos::geom

/*protected*/
LineString::LineString(const LineString &ls)
	:
	Geometry(ls),
	points(ls.points->clone())
{
	//points=ls.points->clone();
}

Geometry*
LineString::reverse() const
{
	if (isEmpty()) {
		return clone();
	}

	assert(points.get());
	CoordinateSequence* seq = points->clone();
	CoordinateSequence::reverse(seq);
	assert(getFactory());
	return getFactory()->createLineString(seq);
}


/*private*/
void
LineString::validateConstruction()
{
	if (points.get()==nullptr)
	{
		points.reset(getFactory()->getCoordinateSequenceFactory()->create());
		return;
	}

	if (points->size()==1)
	{
		throw util::IllegalArgumentException("point array must contain 0 or >1 elements\n");
	}
}

/*protected*/
LineString::LineString(CoordinateSequence *newCoords,
		const GeometryFactory *factory)
	:
	Geometry(factory),
	points(newCoords)
{
	validateConstruction();
}

/*public*/
LineString::LineString(CoordinateSequence::Ptr newCoords,
		const GeometryFactory *factory)
	:
	Geometry(factory),
	points(std::move(newCoords))
{
	validateConstruction();
}


LineString::~LineString()
{
	//delete points;
}

CoordinateSequence*
LineString::getCoordinates() const
{
	assert(points.get());
	return points->clone();
	//return points;
}

const CoordinateSequence*
LineString::getCoordinatesRO() const
{
	assert(nullptr != points.get());
	return points.get();
}

const Coordinate&
LineString::getCoordinateN(int n) const
{
	assert(points.get());
	return points->getAt(n);
}

Dimension::DimensionType
LineString::getDimension() const
{
	return Dimension::L; // line
}

int
LineString::getCoordinateDimension() const
{
    return (int) points->getDimension();
}

int
LineString::getBoundaryDimension() const
{
	if (isClosed()) {
		return Dimension::False;
	}
	return 0;
}

bool
LineString::isEmpty() const
{
	assert(points.get());
	return points->isEmpty();
}

size_t
LineString::getNumPoints() const
{
	assert(points.get());
	return points->getSize();
}

Point*
LineString::getPointN(size_t n) const
{
	assert(getFactory());
	assert(points.get());
	return getFactory()->createPoint(points->getAt(n));
}

Point*
LineString::getStartPoint() const
{
	if (isEmpty()) {
		return nullptr;
		//return new Point(NULL,NULL);
	}
	return getPointN(0);
}

Point*
LineString::getEndPoint() const
{
	if (isEmpty()) {
		return nullptr;
		//return new Point(NULL,NULL);
	}
	return getPointN(getNumPoints() - 1);
}

bool
LineString::isClosed() const
{
	if (isEmpty()) {
		return false;
	}
	return getCoordinateN(0).equals2D(getCoordinateN(static_cast<int>(getNumPoints()-1)));
}

bool
LineString::isRing() const
{
	return isClosed() && isSimple();
}

string
LineString::getGeometryType() const
{
	return "LineString";
}

Geometry*
LineString::getBoundary() const
{
	if (isEmpty()) {
		return getFactory()->createMultiPoint();
	}

	// using the default OGC_SFS MOD2 rule, the boundary of a
	// closed LineString is empty
	if (isClosed()) {
		return getFactory()->createMultiPoint();
	}
	vector<Geometry*> *pts=new vector<Geometry*>();
	pts->push_back(getStartPoint());
	pts->push_back(getEndPoint());
	MultiPoint *mp = getFactory()->createMultiPoint(pts);
	return mp;
}

bool
LineString::isCoordinate(Coordinate& pt) const
{
	assert(points.get());
	std::size_t npts=points->getSize();
	for (std::size_t i = 0; i<npts; i++) {
		if (points->getAt(i)==pt) {
			return true;
		}
	}
	return false;
}

/*protected*/
Envelope::Ptr
LineString::computeEnvelopeInternal() const
{
	if (isEmpty()) {
		// We don't return NULL here
		// as it would indicate "unknown"
		// envelope. In this case we
		// *know* the envelope is EMPTY.
		return Envelope::Ptr(new Envelope());
	}

	assert(points.get());
	const Coordinate& c=points->getAt(0);
	double minx = c.x;
	double miny = c.y;
	double maxx = c.x;
	double maxy = c.y;
	std::size_t npts=points->getSize();
	for (std::size_t i=1; i<npts; i++) {
		const Coordinate &c=points->getAt(i);
		minx = minx < c.x ? minx : c.x;
		maxx = maxx > c.x ? maxx : c.x;
		miny = miny < c.y ? miny : c.y;
		maxy = maxy > c.y ? maxy : c.y;
	}

	// caller expects a newly allocated Envelope.
	// this function won't be called twice, unless
	// cached Envelope is invalidated (set to NULL)
	return Envelope::Ptr(new Envelope(minx, maxx, miny, maxy));
}

bool
LineString::equalsExact(const Geometry *other, double tolerance) const
{
	if (!isEquivalentClass(other)) {
		return false;
	}

	const LineString *otherLineString=dynamic_cast<const LineString*>(other);
	assert(otherLineString);
	size_t npts=points->getSize();
	if (npts!=otherLineString->points->getSize()) {
		return false;
	}
	for (size_t i=0; i<npts; ++i) {
		if (!equal(points->getAt(i),otherLineString->points->getAt(i),tolerance)) {
			return false;
		}
	}
	return true;
}

void
LineString::apply_rw(const CoordinateFilter *filter)
{
	assert(points.get());
	points->apply_rw(filter);
}

void
LineString::apply_ro(CoordinateFilter *filter) const
{
	assert(points.get());
	points->apply_ro(filter);
}

void LineString::apply_rw(GeometryFilter *filter)
{
	assert(filter);
	filter->filter_rw(this);
}

void LineString::apply_ro(GeometryFilter *filter) const
{
	assert(filter);
	filter->filter_ro(this);
}

/*public*/
void
LineString::normalize()
{
	assert(points.get());
	std::size_t npts=points->getSize();
	std::size_t n=npts/2;
	for (std::size_t i=0; i<n; i++) {
		std::size_t j = npts - 1 - i;
		if (!(points->getAt(i)==points->getAt(j))) {
			if (points->getAt(i).compareTo(points->getAt(j)) > 0) {
				CoordinateSequence::reverse(points.get());
			}
			return;
		}
	}
}

int
LineString::compareToSameClass(const Geometry *ls) const
{
	const LineString *line=dynamic_cast<const LineString*>(ls);
	assert(line);
	// MD - optimized implementation
	std::size_t mynpts=points->getSize();
	std::size_t othnpts=line->points->getSize();
	if ( mynpts > othnpts ) return 1;
	if ( mynpts < othnpts ) return -1;
	for (std::size_t i=0; i<mynpts; i++)
	{
		int cmp=points->getAt(i).compareTo(line->points->getAt(i));
		if (cmp) return cmp;
	}
	return 0;
}

const Coordinate*
LineString::getCoordinate() const
{
	if (isEmpty()) return nullptr;
	return &(points->getAt(0));
}

double
LineString::getLength() const
{
	return CGAlgorithms::length(points.get());
}

void
LineString::apply_rw(GeometryComponentFilter *filter)
{
	assert(filter);
	filter->filter_rw(this);
}

void
LineString::apply_ro(GeometryComponentFilter *filter) const
{
	assert(filter);
	filter->filter_ro(this);
}

void
LineString::apply_rw(CoordinateSequenceFilter& filter)
{
	size_t npts=points->size();
	if (!npts) return;
	for (size_t i = 0; i<npts; ++i)
	{
		filter.filter_rw(*points, i);
		if (filter.isDone()) break;
	}
	if (filter.isGeometryChanged()) geometryChanged();
}

void
LineString::apply_ro(CoordinateSequenceFilter& filter) const
{
	size_t npts=points->size();
	if (!npts) return;
	for (size_t i = 0; i<npts; ++i)
	{
		filter.filter_ro(*points, i);
		if (filter.isDone()) break;
	}
	//if (filter.isGeometryChanged()) geometryChanged();
}

GeometryTypeId
LineString::getGeometryTypeId() const
{
	return GEOS_LINESTRING;
}

} // namespace geos::geom
} // namespace geos
