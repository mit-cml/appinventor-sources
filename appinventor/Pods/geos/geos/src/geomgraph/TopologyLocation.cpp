/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2001-2002 Vivid Solutions Inc.
 * Copyright (C) 2005 Refractions Research Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: geomgraph/TopologyLocation.java r428 (JTS-1.12+)
 *
 **********************************************************************/

#include <geos/geomgraph/TopologyLocation.h>
#include <geos/geomgraph/Position.h>
#include <geos/geom/Location.h>

#include <vector>
#include <sstream>
#include <iostream>
#include <cassert>

using namespace std;
using namespace geos::geom;

namespace geos {
namespace geomgraph { // geos.geomgraph

/*public*/
TopologyLocation::TopologyLocation(const vector<int> &newLocation):
	location(newLocation.size(), Location::UNDEF)
{
}

/*public*/
TopologyLocation::TopologyLocation()
{
}

/*public*/
TopologyLocation::~TopologyLocation()
{
}

/*public*/
TopologyLocation::TopologyLocation(int on, int left, int right):
	location(3)
{
	location[Position::ON]=on;
	location[Position::LEFT]=left;
	location[Position::RIGHT]=right;
}

/*public*/
TopologyLocation::TopologyLocation(int on):
	location(1, on)
{
	//(*location)[Position::ON]=on;
}

/*public*/
TopologyLocation::TopologyLocation(const TopologyLocation &gl)
  :
	location(gl.location)
{
}

/*public*/
TopologyLocation&
TopologyLocation::operator= (const TopologyLocation &gl)
{
	location = gl.location;
  return *this;
}

/*public*/
int
TopologyLocation::get(size_t posIndex) const
{
	// should be an assert() instead ?
	if (posIndex<location.size()) return location[posIndex];
	return Location::UNDEF;
}

/*public*/
bool
TopologyLocation::isNull() const
{
	for (size_t i=0, sz=location.size(); i<sz; ++i) {
		if (location[i]!=Location::UNDEF) return false;
	}
	return true;
}

/*public*/
bool
TopologyLocation::isAnyNull() const
{
	for (size_t i=0, sz=location.size(); i<sz; ++i) {
		if (location[i]==Location::UNDEF) return true;
	}
	return false;
}

/*public*/
bool
TopologyLocation::isEqualOnSide(const TopologyLocation &le, int locIndex) const
{
	return location[locIndex]==le.location[locIndex];
}

/*public*/
bool
TopologyLocation::isArea() const
{
	return location.size()>1;
}

/*public*/
bool
TopologyLocation::isLine() const
{
	return location.size()==1;
}

/*public*/
void
TopologyLocation::flip()
{
	if (location.size()<=1) return;
	int temp=location[Position::LEFT];
	location[Position::LEFT]=location[Position::RIGHT];
	location[Position::RIGHT] = temp;
}

/*public*/
void
TopologyLocation::setAllLocations(int locValue)
{
	for (size_t i=0, sz=location.size(); i<sz; ++i) {
		location[i]=locValue;
	}
}

/*public*/
void
TopologyLocation::setAllLocationsIfNull(int locValue)
{
	for (size_t i=0, sz=location.size(); i<sz; ++i) {
		if (location[i]==Location::UNDEF) location[i]=locValue;
	}
}

/*public*/
void
TopologyLocation::setLocation(size_t locIndex, int locValue)
{
	location[locIndex]=locValue;
}

/*public*/
void
TopologyLocation::setLocation(int locValue)
{
	setLocation(Position::ON, locValue);
}

/*public*/
const vector<int> &
TopologyLocation::getLocations() const
{
	return location;
}

/*public*/
void
TopologyLocation::setLocations(int on, int left, int right)
{
	assert(location.size() >= 3);
	location[Position::ON]=on;
	location[Position::LEFT]=left;
	location[Position::RIGHT]=right;
}

/*public*/
bool
TopologyLocation::allPositionsEqual(int loc) const
{
	for (size_t i=0, sz=location.size(); i<sz; ++i) {
		if (location[i]!=loc) return false;
	}
	return true;
}

/*public*/
void
TopologyLocation::merge(const TopologyLocation &gl)
{
	// if the src is an Area label & and the dest is not, increase the dest to be an Area
	size_t sz=location.size();
	size_t glsz=gl.location.size();
	if (glsz>sz) {
		location.resize(3);
		location[Position::LEFT]=Location::UNDEF;
		location[Position::RIGHT]=Location::UNDEF;
	}
	for (size_t i=0; i<sz; ++i) {
		if (location[i]==Location::UNDEF && i<glsz)
			location[i]=gl.location[i];
	}
}

string
TopologyLocation::toString() const
{
	stringstream ss;
	ss << *this;
	return ss.str();
}

std::ostream& operator<< (std::ostream& os, const TopologyLocation& tl)
{
	if (tl.location.size()>1) os << Location::toLocationSymbol(tl.location[Position::LEFT]);
	os << Location::toLocationSymbol(tl.location[Position::ON]);
	if (tl.location.size()>1) os << Location::toLocationSymbol(tl.location[Position::RIGHT]);
	return os;
}

} // namespace geos.geomgraph
} // namespace geos


