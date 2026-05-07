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
 * Last port: geomgraph/Label.java r428 (JTS-1.12+)
 *
 **********************************************************************/

#include <geos/geomgraph/Label.h>
#include <geos/geomgraph/TopologyLocation.h>
#include <geos/geomgraph/Position.h>
#include <geos/geom/Location.h>

#include <string>
#include <sstream>
#include <iostream>
#include <cassert>


using namespace std;
using namespace geos::geom;

namespace geos {
namespace geomgraph { // geos.geomgraph

/*public static*/
Label
Label::toLineLabel(const Label &label)
{
	Label lineLabel(Location::UNDEF);
	for (int i=0; i<2; i++) {
		lineLabel.setLocation(i, label.getLocation(i));
	}
	return lineLabel;
}

/*public*/
Label::Label(int onLoc)
{
	elt[0]=TopologyLocation(onLoc);
	elt[1]=TopologyLocation(onLoc);
}

/*public*/
Label::Label(int geomIndex,int onLoc)
{
	assert(geomIndex>=0 && geomIndex<2);
	elt[0]=TopologyLocation(Location::UNDEF);
	elt[1]=TopologyLocation(Location::UNDEF);
	elt[geomIndex].setLocation(onLoc);
}

/*public*/
Label::Label(int onLoc,int leftLoc,int rightLoc)
{
	elt[0]=TopologyLocation(onLoc,leftLoc,rightLoc);
	elt[1]=TopologyLocation(onLoc,leftLoc,rightLoc);
}

/*public*/
Label::Label()
{
	elt[0]=TopologyLocation(Location::UNDEF);
	elt[1]=TopologyLocation(Location::UNDEF);
}

/*public*/
Label::Label(const Label &l)
{
	elt[0]=TopologyLocation(l.elt[0]);
	elt[1]=TopologyLocation(l.elt[1]);
}

/*public*/
Label&
Label::operator=(const Label &l)
{
	elt[0] = TopologyLocation(l.elt[0]);
	elt[1] = TopologyLocation(l.elt[1]);
	return *this;
}

/*public*/
Label::Label(int geomIndex,int onLoc,int leftLoc,int rightLoc)
{
	elt[0]=TopologyLocation(Location::UNDEF,Location::UNDEF,Location::UNDEF);
	elt[1]=TopologyLocation(Location::UNDEF,Location::UNDEF,Location::UNDEF);
	elt[geomIndex].setLocations(onLoc,leftLoc,rightLoc);
}

/*public*/
void
Label::flip()
{
	elt[0].flip();
	elt[1].flip();
}

/*public*/
int
Label::getLocation(int geomIndex, int posIndex) const
{
	assert(geomIndex>=0 && geomIndex<2);
	return elt[geomIndex].get(posIndex);
}

/*public*/
int
Label::getLocation(int geomIndex) const
{
	assert(geomIndex>=0 && geomIndex<2);
	return elt[geomIndex].get(Position::ON);
}

/*public*/
void
Label::setLocation(int geomIndex,int posIndex,int location)
{
	assert(geomIndex>=0 && geomIndex<2);
	elt[geomIndex].setLocation(posIndex,location);
}

/*public*/
void
Label::setLocation(int geomIndex,int location)
{
	assert(geomIndex>=0 && geomIndex<2);
	elt[geomIndex].setLocation(Position::ON,location);
}

/*public*/
void
Label::setAllLocations(int geomIndex,int location)
{
	assert(geomIndex>=0 && geomIndex<2);
	elt[geomIndex].setAllLocations(location);
}

/*public*/
void
Label::setAllLocationsIfNull(int geomIndex,int location)
{
	assert(geomIndex>=0 && geomIndex<2);
	elt[geomIndex].setAllLocationsIfNull(location);
}

/*public*/
void
Label::setAllLocationsIfNull(int location)
{
	setAllLocationsIfNull(0,location);
	setAllLocationsIfNull(1,location);
}

/*public*/
void
Label::merge(const Label &lbl)
{
	for (int i=0; i<2; i++) {
		elt[i].merge(lbl.elt[i]);
	}
}

/*public*/
int
Label::getGeometryCount() const
{
	int count = 0;
	if (!elt[0].isNull()) count++;
	if (!elt[1].isNull()) count++;
	return count;
}

/*public*/
bool
Label::isNull(int geomIndex) const
{
	assert(geomIndex>=0 && geomIndex<2);
	return elt[geomIndex].isNull();
}

/*public*/
bool
Label::isNull() const
{
	return elt[0].isNull() && elt[1].isNull();
}

/*public*/
bool
Label::isAnyNull(int geomIndex) const
{
	assert(geomIndex>=0 && geomIndex<2);
	return elt[geomIndex].isAnyNull();
}

/*public*/
bool
Label::isArea() const
{
	return elt[0].isArea() || elt[1].isArea();
}

/*public*/
bool
Label::isArea(int geomIndex) const
{
	assert(geomIndex>=0 && geomIndex<2);
	return elt[geomIndex].isArea();
}

/*public*/
bool
Label::isLine(int geomIndex) const
{
	assert(geomIndex>=0 && geomIndex<2);
	return elt[geomIndex].isLine();
}

/*public*/
bool
Label::isEqualOnSide(const Label& lbl, int side) const
{
	return
		elt[0].isEqualOnSide(lbl.elt[0], side)
		&& elt[1].isEqualOnSide(lbl.elt[1], side);
}

/*public*/
bool
Label::allPositionsEqual(int geomIndex, int loc) const
{
	assert(geomIndex>=0 && geomIndex<2);
	return elt[geomIndex].allPositionsEqual(loc);
}

/*public*/
void
Label::toLine(int geomIndex)
{
	assert(geomIndex>=0 && geomIndex<2);
	if (elt[geomIndex].isArea()) {
		elt[geomIndex]=TopologyLocation(elt[geomIndex].getLocations()[0]);
	}
}

string
Label::toString() const
{
	stringstream ss;
	ss << *this;
	return ss.str();
}

std::ostream&
operator<< (std::ostream& os, const Label& l)
{
	os << "A:"
	   << l.elt[0]
	   << " B:"
	   << l.elt[1];
	return os;
}

} // namespace geos.geomgraph
} // namespace geos
