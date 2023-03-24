/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2009  Sandro Santilli <strk@kbt.io>
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: jtstest/testrunner/BufferResultMatcher.java rev rev 1.6 (JTS-1.11)
 *
 **********************************************************************/

#ifndef XMLTESTER_BUFFERRESULTMATCHER_H
#define XMLTESTER_BUFFERRESULTMATCHER_H

// Forward declarations
namespace geos {
	namespace geom {
		class Geometry;
	}
}

namespace geos {
namespace xmltester {

class BufferResultMatcher
{
public:
	bool isBufferResultMatch(const geom::Geometry& actualBuffer,
	                         const geom::Geometry& expectedBuffer,
	                         double distance);

private:

	static double MAX_RELATIVE_AREA_DIFFERENCE;

	static double MAX_HAUSDORFF_DISTANCE_FACTOR;

	/*
	 * The minimum distance tolerance which will be used.
	 * This is required because densified vertices do no lie
	 * precisely on their parent segment.
	 */
	static double MIN_DISTANCE_TOLERANCE;

	bool isSymDiffAreaInTolerance(const geom::Geometry& actualBuffer,
	                              const geom::Geometry& expectedBuffer);

	bool isBoundaryHausdorffDistanceInTolerance(
                        const geom::Geometry& actualBuffer,
	                const geom::Geometry& expectedBuffer,
	                double distance);
};

} // namespace geos::xmltester
} // namespace geos

#endif // XMLTESTER_BUFFERRESULTMATCHER_H
