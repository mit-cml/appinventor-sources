/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2009 Sandro Santilli <strk@kbt.io>
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: noding/BasicSegmentString.java rev. 1.1 (JTS-1.9)
 *
 **********************************************************************/

#include <geos/noding/BasicSegmentString.h>
#include <geos/geom/Coordinate.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/util/IllegalArgumentException.h>
#include <geos/noding/Octant.h>
//#include <geos/profiler.h>

#ifndef GEOS_DEBUG
#define GEOS_DEBUG 0
#endif

#include <iostream>
#include <sstream>

using namespace geos::algorithm;
using namespace geos::geom;

namespace geos {
namespace noding { // geos.noding



/*public*/
int
BasicSegmentString::getSegmentOctant(unsigned int index) const
{
	if (index >= size() - 1) return -1;
	return Octant::octant(getCoordinate(index), getCoordinate(index+1));
}

/* virtual public */
const geom::Coordinate&
BasicSegmentString::getCoordinate(unsigned int i) const
{
	return pts->getAt(i);
}

/* virtual public */
geom::CoordinateSequence*
BasicSegmentString::getCoordinates() const
{
	return pts;
}

/* virtual public */
bool
BasicSegmentString::isClosed() const
{
	return pts->getAt(0)==pts->getAt(size()-1);
}

/* public virtual */
std::ostream&
BasicSegmentString::print(std::ostream& os) const
{
	os << "BasicSegmentString: " << std::endl;
	os << " LINESTRING" << *(pts) << ";" << std::endl;

	return os;
}


} // namespace geos.noding
} // namespace geos

