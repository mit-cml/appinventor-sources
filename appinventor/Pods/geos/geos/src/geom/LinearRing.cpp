/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
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
 * Last port: geom/LinearRing.java r320 (JTS-1.12)
 *
 **********************************************************************/

#include <geos/geom/LinearRing.h>
#include <geos/geom/Dimension.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/geom/GeometryFactory.h>
#include <geos/util/IllegalArgumentException.h>

#include <cassert>
#include <sstream>
#include <string>
#include <memory>

using namespace std;

namespace geos {
namespace geom { // geos::geom

/*public*/
LinearRing::LinearRing(const LinearRing &lr): Geometry(lr), LineString(lr) {}

/*public*/
LinearRing::LinearRing(CoordinateSequence* newCoords,
		const GeometryFactory *newFactory)
	:
	Geometry(newFactory),
	LineString(newCoords, newFactory)
{
	validateConstruction();
}

/*public*/
LinearRing::LinearRing(CoordinateSequence::Ptr newCoords,
		const GeometryFactory *newFactory)
	:
	Geometry(newFactory),
	LineString(std::move(newCoords), newFactory)
{
	validateConstruction();
}


void
LinearRing::validateConstruction()
{
	// Empty ring is valid
	if ( points->isEmpty() ) return;

	if ( !LineString::isClosed() )
	{
		throw util::IllegalArgumentException(
		  "Points of LinearRing do not form a closed linestring"
		);
	}

	if ( points->getSize() < MINIMUM_VALID_SIZE )
	{
		std::ostringstream os;
		os << "Invalid number of points in LinearRing found "
		   << points->getSize() << " - must be 0 or >= 4";
		throw util::IllegalArgumentException(os.str());
	}
}



// superclass LineString will delete internal CoordinateSequence
LinearRing::~LinearRing(){
}

int
LinearRing::getBoundaryDimension() const
{
	return Dimension::False;
}

bool
LinearRing::isSimple() const
{
	return true;
}

bool
LinearRing::isClosed() const
{
	if ( points->isEmpty() ) {
		// empty LinearRings are closed by definition
		return true;
	}
	return LineString::isClosed();
}

string LinearRing::getGeometryType() const {
	return "LinearRing";
}

void LinearRing::setPoints(CoordinateSequence* cl){
	const vector<Coordinate> *v=cl->toVector();
	points->setPoints(*(v));
	//delete v;
}

GeometryTypeId
LinearRing::getGeometryTypeId() const {
	return GEOS_LINEARRING;
}

Geometry*
LinearRing::reverse() const
{
	if (isEmpty()) {
		return clone();
	}

	assert(points.get());
	CoordinateSequence* seq = points->clone();
	CoordinateSequence::reverse(seq);
	assert(getFactory());
	return getFactory()->createLinearRing(seq);
}

} // namespace geos::geom
} // namespace geos
