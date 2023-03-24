/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2006      Refractions Research Inc.
 * Copyright (C) 2001-2002 Vivid Solutions Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: geomgraph/EdgeNodingValidator.java rev. 1.6 (JTS-1.10)
 *
 **********************************************************************/

#include <vector>

#include <geos/geomgraph/EdgeNodingValidator.h>
#include <geos/geomgraph/Edge.h>
#include <geos/noding/BasicSegmentString.h>
#include <geos/geom/CoordinateSequence.h>

using namespace std;
using namespace geos::noding;
using namespace geos::geom;

namespace geos {
namespace geomgraph { // geos.geomgraph

vector<SegmentString*>&
EdgeNodingValidator::toSegmentStrings(vector<Edge*>& edges)
{
	// convert Edges to SegmentStrings
	for(size_t i=0, n=edges.size(); i<n; ++i) {
		Edge *e=edges[i];
		CoordinateSequence* cs=e->getCoordinates()->clone();
		newCoordSeq.push_back(cs);
		segStr.push_back(new BasicSegmentString(cs, e));
	}
	return segStr;
}

EdgeNodingValidator::~EdgeNodingValidator()
{
	for (SegmentString::NonConstVect::iterator
			i=segStr.begin(), e=segStr.end();
			i != e;
			++i)
	{
		delete *i;
	}

	for(size_t i=0, n=newCoordSeq.size(); i<n; ++i)
		delete newCoordSeq[i];
}

} // namespace geos.geomgraph
} // namespace geos
