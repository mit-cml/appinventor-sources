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
 * Last port: geomgraph/Depth.java rev. 1.4 (JTS-1.10)
 *
 **********************************************************************/

#include <sstream>
#include <string>

#include <geos/geomgraph/Depth.h>
#include <geos/geomgraph/Label.h>
#include <geos/geomgraph/Position.h>
#include <geos/geom/Location.h>

#define NULL_VALUE -1

using namespace std;
using namespace geos::geom;

namespace geos {
namespace geomgraph { // geos.geomgraph

int
Depth::depthAtLocation(int location)
{
	if (location == Location::EXTERIOR) return 0;
	if (location == Location::INTERIOR) return 1;
	return NULL_VALUE;
}

Depth::Depth()
{
	// initialize depth array to a sentinel value
	for (int i=0; i<2; i++) {
		for (int j=0; j<3;j++) {
			depth[i][j]=NULL_VALUE;
		}
	}
}

Depth::~Depth()
{
//	delete[] &depth;
}

int
Depth::getDepth(int geomIndex,int posIndex) const
{
	return depth[geomIndex][posIndex];
}

void
Depth::setDepth(int geomIndex,int posIndex,int depthValue)
{
	depth[geomIndex][posIndex] = depthValue;
}

int
Depth::getLocation(int geomIndex,int posIndex) const
{
	if (depth[geomIndex][posIndex] <= 0) return Location::EXTERIOR;
	return Location::INTERIOR;
}

void
Depth::add(int geomIndex,int posIndex,int location)
{
	if (location == Location::INTERIOR)
		depth[geomIndex][posIndex]++;
}

/**
 * A Depth object is null (has never been initialized) if all depths are null.
 */
bool
Depth::isNull() const
{
	for (int i=0; i<2; i++) {
		for (int j=0; j<3; j++) {
			if (depth[i][j] != NULL_VALUE)
				return false;
		}
	}
	return true;
}

bool
Depth::isNull(int geomIndex) const
{
	return depth[geomIndex][1] == NULL_VALUE;
}

bool
Depth::isNull(int geomIndex, int posIndex) const
{
	return depth[geomIndex][posIndex] == NULL_VALUE;
}

int
Depth::getDelta(int geomIndex) const
{
	return depth[geomIndex][Position::RIGHT]-depth[geomIndex][Position::LEFT];
}

/**
 * Normalize the depths for each geometry, if they are non-null.
 * A normalized depth
 * has depth values in the set { 0, 1 }.
 * Normalizing the depths
 * involves reducing the depths by the same amount so that at least
 * one of them is 0.  If the remaining value is > 0, it is set to 1.
 */
void
Depth::normalize()
{
	for (int i=0; i<2; i++) {
		if (!isNull(i)) {
			int minDepth=depth[i][1];
			if (depth[i][2]<minDepth)
				minDepth=depth[i][2];
			if (minDepth<0) minDepth = 0;
			for (int j=1; j<3; j++) {
				int newValue=0;
				if (depth[i][j]>minDepth)
					newValue = 1;
				depth[i][j] = newValue;
			}
		}
	}
}

void
Depth::add(const Label& lbl)
{
	for (int i=0; i<2; i++) {
		for (int j=1; j<3; j++) {
			int loc=lbl.getLocation(i,j);
			if (loc==Location::EXTERIOR || loc==Location::INTERIOR)
			{
				// initialize depth if it is null, otherwise
				// add this location value
				if (isNull(i,j)) {
					depth[i][j]=depthAtLocation(loc);
				} else
					depth[i][j]+=depthAtLocation(loc);
			}
		}
	}
}

string
Depth::toString() const
{
	ostringstream s;
	s<<"A:"<<depth[0][1]<<","<<depth[0][2]<<" ";
	s<<"B:"<<depth[1][1]<<","<<depth[1][2]<<"]";
	return s.str();
}


} // namespace geos.geomgraph
} // namespace geos
