/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2012 Sandro Santilli <strk@kbt.io>
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 ***********************************************************************
 *
 * Last port: precision/PrecisionreducerCoordinateOperation.java r591 (JTS-1.12)
 *
 **********************************************************************/

#include <geos/precision/PrecisionReducerCoordinateOperation.h>
#include <geos/geom/PrecisionModel.h>
#include <geos/geom/CoordinateSequenceFactory.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/geom/GeometryFactory.h>
#include <geos/geom/Geometry.h>
#include <geos/geom/LineString.h>
#include <geos/geom/LinearRing.h>

#include <vector>

using namespace geos::geom;
using namespace std;

namespace geos {
namespace precision { // geos.precision

CoordinateSequence*
PrecisionReducerCoordinateOperation::edit(const CoordinateSequence *cs,
                                          const Geometry *geom)
{
	unsigned int csSize = static_cast<unsigned int>(cs->getSize());

	if ( csSize == 0 ) return nullptr;

	vector<Coordinate> *vc = new vector<Coordinate>(csSize);

	// copy coordinates and reduce
	for (unsigned int i=0; i<csSize; ++i) {
		Coordinate coord=cs->getAt(i);
		targetPM.makePrecise(&coord);
		(*vc)[i] = coord;
	}

	// reducedCoords take ownership of 'vc'
	CoordinateSequence *reducedCoords =
		geom->getFactory()->getCoordinateSequenceFactory()->create(vc);

	// remove repeated points, to simplify returned geometry as
	// much as possible.
	//
	CoordinateSequence *noRepeatedCoords=CoordinateSequence::removeRepeatedPoints(reducedCoords);

	/**
	 * Check to see if the removal of repeated points
	 * collapsed the coordinate List to an invalid length
	 * for the type of the parent geometry.
	 * It is not necessary to check for Point collapses,
	 * since the coordinate list can
	 * never collapse to less than one point.
	 * If the length is invalid, return the full-length coordinate array
	 * first computed, or null if collapses are being removed.
	 * (This may create an invalid geometry - the client must handle this.)
	 */
	unsigned int minLength = 0;
	if ( dynamic_cast<const LineString*>(geom) ) minLength = 2;
	if ( dynamic_cast<const LinearRing*>(geom) ) minLength = 4;

	CoordinateSequence *collapsedCoords = reducedCoords;
	if ( removeCollapsed )
	{
		delete reducedCoords; reducedCoords=nullptr;
		collapsedCoords=nullptr;
	}

	// return null or orginal length coordinate array
	if ( noRepeatedCoords->getSize() < minLength ) {
		delete noRepeatedCoords;
		return collapsedCoords;
	}

	// ok to return shorter coordinate array
	delete reducedCoords;
	return noRepeatedCoords;
}


} // namespace geos.precision
} // namespace geos
