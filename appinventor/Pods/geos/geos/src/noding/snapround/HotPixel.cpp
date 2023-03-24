/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2006 Refractions Research Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: noding/snapround/HotPixel.java r320 (JTS-1.12)
 *
 **********************************************************************/

#include <geos/noding/snapround/HotPixel.h>
#include <geos/noding/NodedSegmentString.h>
#include <geos/algorithm/LineIntersector.h>
#include <geos/geom/Coordinate.h>

#ifndef GEOS_INLINE
# include "geos/noding/snapround/HotPixel.inl"
#endif

#include <algorithm> // for std::min and std::max
#include <cassert>
#include <memory>

using namespace std;
using namespace geos::algorithm;
using namespace geos::geom;

namespace geos {
namespace noding { // geos.noding
namespace snapround { // geos.noding.snapround

HotPixel::HotPixel(const Coordinate& newPt, double newScaleFactor,
		LineIntersector& newLi)
	:
	li(newLi),
	pt(newPt),
	originalPt(newPt),
	scaleFactor(newScaleFactor)
{
	if (scaleFactor != 1.0) {
		assert( scaleFactor != 0 ); // or should it be an IllegalArgumentException ?
		pt.x=scale(pt.x);
		pt.y=scale(pt.y);
	}
	initCorners(pt);
}

const Envelope&
HotPixel::getSafeEnvelope() const
{
	static const double SAFE_ENV_EXPANSION_FACTOR = 0.75;

	if (safeEnv.get() == nullptr) {
		double safeTolerance = SAFE_ENV_EXPANSION_FACTOR / scaleFactor;
		safeEnv = unique_ptr<Envelope>(new Envelope(originalPt.x - safeTolerance,
			originalPt.x + safeTolerance,
			originalPt.y - safeTolerance,
			originalPt.y + safeTolerance
			));
	}
	return *safeEnv;
}

/*private*/
void
HotPixel::initCorners(const Coordinate& pt)
{
	double tolerance = 0.5;
	minx = pt.x - tolerance;
	maxx = pt.x + tolerance;
	miny = pt.y - tolerance;
	maxy = pt.y + tolerance;

	corner.resize(4);
	corner[0] = Coordinate(maxx, maxy);
	corner[1] = Coordinate(minx, maxy);
	corner[2] = Coordinate(minx, miny);
	corner[3] = Coordinate(maxx, miny);
}

bool
HotPixel::intersects(const Coordinate& p0,
		const Coordinate& p1) const
{
	if (scaleFactor == 1.0) return intersectsScaled(p0, p1);

	copyScaled(p0, p0Scaled);
	copyScaled(p1, p1Scaled);

	return intersectsScaled(p0Scaled, p1Scaled);
}

/* private */
bool
HotPixel::intersectsScaled(const Coordinate& p0,
		const Coordinate& p1) const
{

	double const segMinx = std::min(p0.x, p1.x);
	double const segMaxx = std::max(p0.x, p1.x);
	double const segMiny = std::min(p0.y, p1.y);
	double const segMaxy = std::max(p0.y, p1.y);

	bool isOutsidePixelEnv =  maxx < segMinx
                         || minx > segMaxx
                         || maxy < segMiny
                         || miny > segMaxy;

	if (isOutsidePixelEnv) return false;

	bool intersects = intersectsToleranceSquare(p0, p1);

	// Found bad envelope test
	assert(!(isOutsidePixelEnv && intersects));

	return intersects;
}

/*private*/
bool
HotPixel::intersectsToleranceSquare(const Coordinate& p0,
		const Coordinate& p1) const
{
    bool intersectsLeft = false;
    bool intersectsBottom = false;

    li.computeIntersection(p0, p1, corner[0], corner[1]);
    if (li.isProper()) return true;

    li.computeIntersection(p0, p1, corner[1], corner[2]);
    if (li.isProper()) return true;
    if (li.hasIntersection()) intersectsLeft = true;

    li.computeIntersection(p0, p1, corner[2], corner[3]);
    if (li.isProper()) return true;
    if (li.hasIntersection()) intersectsBottom = true;

    li.computeIntersection(p0, p1, corner[3], corner[0]);
    if (li.isProper()) return true;

    if (intersectsLeft && intersectsBottom) return true;

    if (p0.equals2D(pt)) return true;
    if (p1.equals2D(pt)) return true;

    return false;
}

/*private*/
bool
HotPixel::intersectsPixelClosure(const Coordinate& p0,
		const Coordinate& p1)
{
    li.computeIntersection(p0, p1, corner[0], corner[1]);
    if (li.hasIntersection()) return true;
    li.computeIntersection(p0, p1, corner[1], corner[2]);
    if (li.hasIntersection()) return true;
    li.computeIntersection(p0, p1, corner[2], corner[3]);
    if (li.hasIntersection()) return true;
    li.computeIntersection(p0, p1, corner[3], corner[0]);
    if (li.hasIntersection()) return true;

    return false;
}

bool
HotPixel::addSnappedNode(NodedSegmentString& segStr, size_t segIndex)
{
	const Coordinate& p0 = segStr.getCoordinate(static_cast<unsigned int>(segIndex));
	const Coordinate& p1 = segStr.getCoordinate(static_cast<unsigned int>(segIndex + 1));

	if (intersects(p0, p1))
	{
		//cout << "snapped: " <<  snapPt << endl;
		segStr.addIntersection(getCoordinate(), static_cast<unsigned int>(segIndex));
		return true;
	}
	return false;
}


} // namespace geos.noding.snapround
} // namespace geos.noding
} // namespace geos
