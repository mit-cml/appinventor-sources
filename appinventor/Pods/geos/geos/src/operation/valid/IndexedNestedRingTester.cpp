/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2009 Sandro Santilli <strk@kbt.io>
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: operation/valid/IndexedNestedRingTester.java r399 (JTS-1.12)
 *
 **********************************************************************/

#include "IndexedNestedRingTester.h"

#include <geos/geom/LinearRing.h> // for use
#include <geos/algorithm/CGAlgorithms.h> // for use
#include <geos/operation/valid/IsValidOp.h> // for use (findPtNotNode)
#include <geos/index/strtree/STRtree.h> // for use

// Forward declarations
namespace geos {
	namespace geom {
		class CoordinateSequence;
		class Envelope;
	}
}

namespace geos {
namespace operation { // geos.operation
namespace valid { // geos.operation.valid

bool
IndexedNestedRingTester::isNonNested()
{
	buildIndex();

	for (size_t i=0, n=rings.size(); i<n; ++i)
	{
		const geom::LinearRing* innerRing = rings[i];
		const geom::CoordinateSequence *innerRingPts=innerRing->getCoordinatesRO();
		std::vector<void*> results;
		index->query(innerRing->getEnvelopeInternal(), results);
		for (size_t j=0, jn=results.size(); j<jn; ++j)
		{
			const geom::LinearRing* searchRing = static_cast<const geom::LinearRing*>(results[j]);
			const geom::CoordinateSequence *searchRingPts=searchRing->getCoordinatesRO();

			if (innerRing==searchRing)
				continue;

			if (!innerRing->getEnvelopeInternal()->intersects(
				searchRing->getEnvelopeInternal()))
			{
				continue;
			}

                        const geom::Coordinate *innerRingPt =
				IsValidOp::findPtNotNode(innerRingPts,
							 searchRing,
							 graph);

        /**
         * If no non-node pts can be found, this means
         * that the searchRing touches ALL of the innerRing vertices.
         * This indicates an invalid polygon, since either
         * the two holes create a disconnected interior,
         * or they touch in an infinite number of points
         * (i.e. along a line segment).
         * Both of these cases are caught by other tests,
         * so it is safe to simply skip this situation here.
         */
        if ( ! innerRingPt ) continue;

                        // Unable to find a ring point not a node of
			// the search ring
			assert(innerRingPt!=nullptr);

			bool isInside = algorithm::CGAlgorithms::isPointInRing(
					*innerRingPt, searchRingPts);

			if (isInside) {
				nestedPt = innerRingPt;
				return false;
			}

		}
	}

	return true;
}

IndexedNestedRingTester::~IndexedNestedRingTester()
{
	delete index;
	//delete totalEnv;
}

void
IndexedNestedRingTester::buildIndex()
{
	delete index;

	index = new index::strtree::STRtree();
	for (size_t i=0, n=rings.size(); i<n; ++i)
	{
		const geom::LinearRing* ring = rings[i];
		const geom::Envelope* env = ring->getEnvelopeInternal();
		index->insert(env, (void*)ring);
	}
}

} // namespace geos.operation.valid
} // namespace geos.operation
} // namespace geos
