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
 * Last port: operation/buffer/BufferInputLineSimplifier.java r320 (JTS-1.12)
 *
 **********************************************************************/

#include <geos/operation/buffer/BufferInputLineSimplifier.h>
#include <geos/geom/CoordinateSequence.h> // for inlines
#include <geos/geom/CoordinateArraySequence.h> // for constructing the return
#include <geos/algorithm/CGAlgorithms.h> // for use

#include <memory>
#include <cmath>
#include <vector>

//#include <cassert>

using namespace geos::algorithm; // CGAlgorithms
using namespace geos::geom;
//using namespace geos::geomgraph; // DirectedEdge, Position

namespace geos {
namespace operation { // geos.operation
namespace buffer { // geos.operation.buffer

BufferInputLineSimplifier::BufferInputLineSimplifier(
		const geom::CoordinateSequence& input)
	:
	inputLine(input),
	angleOrientation(CGAlgorithms::COUNTERCLOCKWISE)
{}

/*public static*/
std::unique_ptr<geom::CoordinateSequence>
BufferInputLineSimplifier::simplify(const geom::CoordinateSequence& inputLine,
                                    double distanceTol)
{
	BufferInputLineSimplifier simp(inputLine);
	return simp.simplify(distanceTol);
}

/* public */
std::unique_ptr<geom::CoordinateSequence>
BufferInputLineSimplifier::simplify(double nDistanceTol)
{
	distanceTol = fabs(nDistanceTol);
	if (nDistanceTol < 0)
		angleOrientation = CGAlgorithms::CLOCKWISE;

	// rely on fact that boolean array is filled with false value
	static const int startValue = INIT;
	isDeleted.assign(inputLine.size(), startValue);

	bool isChanged = false;
	do {
		isChanged = deleteShallowConcavities();
	} while (isChanged);

	return collapseLine();
}

/* private */
bool
BufferInputLineSimplifier::deleteShallowConcavities()
{
	/**
	 * Do not simplify end line segments of the line string.
	 * This ensures that end caps are generated consistently.
	 */
	unsigned int index = 1;

	unsigned int midIndex = findNextNonDeletedIndex(index);
	unsigned int lastIndex = findNextNonDeletedIndex(midIndex);

	bool isChanged = false;
	while (lastIndex < inputLine.size())
	{
		// test triple for shallow concavity
		bool isMiddleVertexDeleted = false;
		if (isDeletable(index, midIndex, lastIndex,
		                distanceTol))
		{
			isDeleted[midIndex] = DELETE;
			isMiddleVertexDeleted = true;
			isChanged = true;
		}
		// move simplification window forward
		if (isMiddleVertexDeleted)
			index = lastIndex;
		else
			index = midIndex;

		midIndex = findNextNonDeletedIndex(index);
		lastIndex = findNextNonDeletedIndex(midIndex);
	}
	return isChanged;
}

/* private */
unsigned int
BufferInputLineSimplifier::findNextNonDeletedIndex(unsigned int index) const
{
	std::size_t next = index + 1;
	const std::size_t len = inputLine.size();
	while (next < len && isDeleted[next] == DELETE)
		next++;
	return static_cast<unsigned int>(next);
}

/* private */
std::unique_ptr<geom::CoordinateSequence>
BufferInputLineSimplifier::collapseLine() const
{
	std::unique_ptr<geom::CoordinateSequence> coordList(
		new CoordinateArraySequence());

	for (size_t i=0, n=inputLine.size(); i<n; ++i)
	{
		if (isDeleted[i] != DELETE)
			coordList->add(inputLine[i], false);
	}

	return coordList;
}

/* private */
bool
BufferInputLineSimplifier::isDeletable(int i0, int i1, int i2,
                                       double distanceTol) const
{
	const Coordinate& p0 = inputLine[i0];
	const Coordinate& p1 = inputLine[i1];
	const Coordinate& p2 = inputLine[i2];

	if (! isConcave(p0, p1, p2)) return false;
	if (! isShallow(p0, p1, p2, distanceTol)) return false;

	// MD - don't use this heuristic - it's too restricting
	// if (p0.distance(p2) > distanceTol) return false;

	return isShallowSampled(p0, p1, i0, i2, distanceTol);
}

/* private */
bool
BufferInputLineSimplifier::isShallowConcavity(const geom::Coordinate& p0,
                                              const geom::Coordinate& p1,
                                              const geom::Coordinate& p2,
                                              double distanceTol) const
{
	int orientation = CGAlgorithms::computeOrientation(p0, p1, p2);
	bool isAngleToSimplify = (orientation == angleOrientation);
	if (! isAngleToSimplify)
		return false;

	double dist = CGAlgorithms::distancePointLine(p1, p0, p2);
	return dist < distanceTol;
}

/* private */
bool
BufferInputLineSimplifier::isShallowSampled(const geom::Coordinate& p0,
                                            const geom::Coordinate& p2,
                                            int i0, int i2,
                                            double distanceTol) const
{
	// check every n'th point to see if it is within tolerance
	int inc = (i2 - i0) / NUM_PTS_TO_CHECK;
	if (inc <= 0) inc = 1;

	for (int i = i0; i < i2; i += inc) {
		if (! isShallow(p0, p2, inputLine[i], distanceTol))
			return false;
	}
	return true;
}

/* private */
bool
BufferInputLineSimplifier::isShallow(const geom::Coordinate& p0,
                                     const geom::Coordinate& p1,
                                     const geom::Coordinate& p2,
                                     double distanceTol) const
{
	double dist = CGAlgorithms::distancePointLine(p1, p0, p2);
	return dist < distanceTol;
}

/* private */
bool
BufferInputLineSimplifier::isConcave(const geom::Coordinate& p0,
                                     const geom::Coordinate& p1,
                                     const geom::Coordinate& p2) const
{
	int orientation = CGAlgorithms::computeOrientation(p0, p1, p2);
	bool isConcave = (orientation == angleOrientation);
	return isConcave;
}

} // namespace geos.operation.buffer
} // namespace geos.operation
} // namespace geos

