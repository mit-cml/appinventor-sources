/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2006 Refractions Research Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 *
 **********************************************************************
 *
 * Last port: algorithm/RayCrossingCounter.java rev. 1.2 (JTS-1.9)
 *
 **********************************************************************/

#ifndef GEOS_ALGORITHM_RAYCROSSINGCOUNTER_H
#define GEOS_ALGORITHM_RAYCROSSINGCOUNTER_H

#include <geos/export.h>

#include <vector>

// forward declarations
namespace geos {
	namespace geom {
		class Coordinate;
		class CoordinateSequence;
	}
}


namespace geos {
namespace algorithm {

/** \brief
 * Counts the number of segments crossed by a horizontal ray extending to the right
 * from a given point, in an incremental fashion.
 *
 * This can be used to determine whether a point lies in a {@link Polygonal} geometry.
 * The class determines the situation where the point lies exactly on a segment.
 * When being used for Point-In-Polygon determination, this case allows short-circuiting
 * the evaluation.
 *
 * This class handles polygonal geometries with any number of shells and holes.
 * The orientation of the shell and hole rings is unimportant.
 * In order to compute a correct location for a given polygonal geometry,
 * it is essential that <b>all</b> segments are counted which
 * <ul>
 * <li>touch the ray
 * <li>lie in in any ring which may contain the point
 * </ul>
 * The only exception is when the point-on-segment situation is detected, in which
 * case no further processing is required.
 * The implication of the above rule is that segments
 * which can be a priori determined to <i>not</i> touch the ray
 * (i.e. by a test of their bounding box or Y-extent)
 * do not need to be counted.  This allows for optimization by indexing.
 *
 * @author Martin Davis
 *
 */
class GEOS_DLL RayCrossingCounter
{
private:
	const geom::Coordinate& point;

	int crossingCount;

	// true if the test point lies on an input segment
    bool isPointOnSegment;

    // Declare type as noncopyable
    RayCrossingCounter(const RayCrossingCounter& other) = delete;
    RayCrossingCounter& operator=(const RayCrossingCounter& rhs) = delete;

public:
	/**
	 * Determines the {@link Location} of a point in a ring.
	 * This method is an exemplar of how to use this class.
	 *
	 * @param p the point to test
	 * @param ring an array of Coordinates forming a ring
	 * @return the location of the point in the ring
	 */
	static int locatePointInRing(const geom::Coordinate& p,
	                             const geom::CoordinateSequence& ring);

	/// Semantically equal to the above, just different args encoding
	static int locatePointInRing(const geom::Coordinate& p,
	         const std::vector<const geom::Coordinate*>& ring);

	/** \brief
	 * Returns the index of the direction of the point <code>q</code>
	 * relative to a vector specified by <code>p1-p2</code>.
	 *
	 * @param p1 the origin point of the vector
	 * @param p2 the final point of the vector
	 * @param q the point to compute the direction to
	 *
	 * @return 1 if q is counter-clockwise (left) from p1-p2
	 * @return -1 if q is clockwise (right) from p1-p2
	 * @return 0 if q is collinear with p1-p2
	 */
	static int orientationIndex(const geom::Coordinate& p1,
	         const geom::Coordinate& p2,
	         const geom::Coordinate& q);

	RayCrossingCounter(const geom::Coordinate& point)
	:	point( point),
		crossingCount( 0),
		isPointOnSegment( false)
	{ }

	/**
	 * Counts a segment
	 *
	 * @param p1 an endpoint of the segment
	 * @param p2 another endpoint of the segment
	 */
	void countSegment(const geom::Coordinate& p1,
	                  const geom::Coordinate& p2);

	/**
	 * Reports whether the point lies exactly on one of the supplied segments.
	 * This method may be called at any time as segments are processed.
	 * If the result of this method is <tt>true</tt>,
	 * no further segments need be supplied, since the result
	 * will never change again.
	 *
	 * @return true if the point lies exactly on a segment
	 */
	bool isOnSegment()
	{
		return isPointOnSegment;
	}

	/**
	 * Gets the {@link Location} of the point relative to
	 * the ring, polygon
	 * or multipolygon from which the processed segments were provided.
	 * <p>
	 * This method only determines the correct location
	 * if <b>all</b> relevant segments must have been processed.
	 *
	 * @return the Location of the point
 	 */
	int getLocation();

	/**
	 * Tests whether the point lies in or on
	 * the ring, polygon
	 * or multipolygon from which the processed segments were provided.
	 * <p>
	 * This method only determines the correct location
	 * if <b>all</b> relevant segments must have been processed.
	 *
	 * @return true if the point lies in or on the supplied polygon
	 */
	bool isPointInPolygon();

};

} // geos::algorithm
} // geos

#endif // GEOS_ALGORITHM_RAYCROSSINGCOUNTER_H
