/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2011 Sandro Santilli <strk@kbt.io>
 * Copyright (C) 2005 Refractions Research Inc.
 * Copyright (C) 2001-2002 Vivid Solutions Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: operation/buffer/OffsetCurveSetBuilder.java r378 (JTS-1.12)
 *
 **********************************************************************/

#include <geos/platform.h>
#include <geos/algorithm/CGAlgorithms.h>
#include <geos/algorithm/MinimumDiameter.h>
#include <geos/util/UnsupportedOperationException.h>
#include <geos/operation/buffer/OffsetCurveSetBuilder.h>
#include <geos/operation/buffer/OffsetCurveBuilder.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/geom/Geometry.h>
#include <geos/geom/GeometryFactory.h>
#include <geos/geom/GeometryCollection.h>
#include <geos/geom/Point.h>
#include <geos/geom/LinearRing.h>
#include <geos/geom/LineString.h>
#include <geos/geom/Polygon.h>
#include <geos/geom/Location.h>
#include <geos/geom/Triangle.h>
#include <geos/geomgraph/Position.h>
#include <geos/geomgraph/Label.h>
#include <geos/noding/NodedSegmentString.h>

#include <algorithm> // for min
#include <cmath>
#include <cassert>
#include <memory>
#include <vector>
#include <typeinfo>

#ifndef GEOS_DEBUG
#define GEOS_DEBUG 0
#endif

//using namespace geos::operation::overlay;
using namespace geos::geom;
using namespace geos::noding; // SegmentString
using namespace geos::geomgraph; // Label, Position
using namespace geos::algorithm; // CGAlgorithms

namespace geos {
namespace operation { // geos.operation
namespace buffer { // geos.operation.buffer

OffsetCurveSetBuilder::OffsetCurveSetBuilder(const Geometry& newInputGeom,
		double newDistance, OffsetCurveBuilder& newCurveBuilder):
	inputGeom(newInputGeom),
	distance(newDistance),
	curveBuilder(newCurveBuilder),
	curveList()
{
}

OffsetCurveSetBuilder::~OffsetCurveSetBuilder()
{
	for (size_t i=0, n=curveList.size(); i<n; ++i)
	{
		SegmentString* ss = curveList[i];
		delete ss;
	}
	for (size_t i=0, n=newLabels.size(); i<n; ++i)
		delete newLabels[i];
}

/* public */
std::vector<SegmentString*>&
OffsetCurveSetBuilder::getCurves()
{
	add(inputGeom);
	return curveList;
}

/*public*/
void
OffsetCurveSetBuilder::addCurves(const std::vector<CoordinateSequence*>& lineList,
	int leftLoc, int rightLoc)
{
	for (size_t i=0, n=lineList.size(); i<n; ++i)
	{
		CoordinateSequence *coords = lineList[i];
		addCurve(coords, leftLoc, rightLoc);
	}
}

/*private*/
void
OffsetCurveSetBuilder::addCurve(CoordinateSequence *coord,
	int leftLoc, int rightLoc)
{
#if GEOS_DEBUG
	std::cerr<<__FUNCTION__<<": coords="<<coord->toString()<<std::endl;
#endif
	// don't add null curves!
	if (coord->getSize() < 2) {
#if GEOS_DEBUG
		std::cerr<<" skipped (size<2)"<<std::endl;
#endif
		delete coord;
		return;
	}

	// add the edge for a coordinate list which is a raw offset curve
	Label *newlabel = new Label(0, Location::BOUNDARY, leftLoc, rightLoc);

	// coord ownership transferred to SegmentString
	SegmentString *e=new NodedSegmentString(coord, newlabel);

	// SegmentString doesnt own the sequence, so we need to delete in
	// the destructor
	newLabels.push_back(newlabel);
	curveList.push_back(e);
}


/*private*/
void
OffsetCurveSetBuilder::add(const Geometry& g)
{
  if (g.isEmpty()) {
#if GEOS_DEBUG
    std::cerr<<__FUNCTION__<<": skip empty geometry"<<std::endl;
#endif
    return;
  }

	const Polygon *poly = dynamic_cast<const Polygon *>(&g);
	if ( poly ) {
		addPolygon(poly);
		return;
	}

	const LineString *line = dynamic_cast<const LineString *>(&g);
	if ( line ) {
		addLineString(line);
		return;
	}

	const Point *point = dynamic_cast<const Point *>(&g);
	if ( point ) {
		addPoint(point);
		return;
	}

	const GeometryCollection *collection = dynamic_cast<const GeometryCollection *>(&g);
	if ( collection ) {
		addCollection(collection);
		return;
	}

	std::string out=typeid(g).name();
	throw util::UnsupportedOperationException("GeometryGraph::add(Geometry &): unknown geometry type: "+out);
}

/*private*/
void
OffsetCurveSetBuilder::addCollection(const GeometryCollection *gc)
{
	for (int i=0, n=static_cast<int>(gc->getNumGeometries()); i<n; i++) {
		const Geometry *g=gc->getGeometryN(i);
		add(*g);
	}
}

/*private*/
void
OffsetCurveSetBuilder::addPoint(const Point *p)
{
	if (distance <= 0.0) return;
	const CoordinateSequence *coord=p->getCoordinatesRO();
	std::vector<CoordinateSequence*> lineList;
	curveBuilder.getLineCurve(coord, distance, lineList);

	addCurves(lineList, Location::EXTERIOR, Location::INTERIOR);
	//delete lineList;
}

/*private*/
void
OffsetCurveSetBuilder::addLineString(const LineString *line)
{
	if (distance <= 0.0 && ! curveBuilder.getBufferParameters().isSingleSided())
	{
		return;
	}

#if GEOS_DEBUG
	std::cerr<<__FUNCTION__<<": "<<line->toString()<<std::endl;
#endif
	std::unique_ptr<CoordinateSequence> coord(CoordinateSequence::removeRepeatedPoints(line->getCoordinatesRO()));
#if GEOS_DEBUG
	std::cerr<<" After coordinate removal: "<<coord->toString()<<std::endl;
#endif
	std::vector<CoordinateSequence*> lineList;
	curveBuilder.getLineCurve(coord.get(), distance, lineList);
	addCurves(lineList, Location::EXTERIOR, Location::INTERIOR);
}

/*private*/
void
OffsetCurveSetBuilder::addPolygon(const Polygon *p)
{
	double offsetDistance=distance;

	int offsetSide=Position::LEFT;
	if (distance < 0.0)
	{
		offsetDistance = -distance;
		offsetSide = Position::RIGHT;
	}

	// FIXME: avoid the C-style cast
	const LinearRing *shell=(const LinearRing *)p->getExteriorRing();

	// optimization - don't bother computing buffer
	// if the polygon would be completely eroded
	if (distance < 0.0 && isErodedCompletely(shell, distance))
	{
#if GEOS_DEBUG
		std::cerr<<__FUNCTION__<<": polygon is eroded completely "<<std::endl;
#endif
		return;
	}

	// don't attempt to buffer a polygon
	// with too few distinct vertices
	CoordinateSequence *shellCoord =
		CoordinateSequence::removeRepeatedPoints(shell->getCoordinatesRO());
	if (distance <= 0.0 && shellCoord->size() < 3)
	{
		delete shellCoord;
		return;
	}

	addPolygonRing(
		shellCoord,
		offsetDistance,
		offsetSide,
		Location::EXTERIOR,
		Location::INTERIOR);

	delete shellCoord;

	for (size_t i=0, n=p->getNumInteriorRing(); i<n; ++i)
	{
		const LineString *hls=p->getInteriorRingN(i);
		assert(dynamic_cast<const LinearRing *>(hls));
		const LinearRing *hole=static_cast<const LinearRing *>(hls);

		// optimization - don't bother computing buffer for this hole
		// if the hole would be completely covered
		if (distance > 0.0 && isErodedCompletely(hole, -distance))
		{
			continue;
		}

		CoordinateSequence *holeCoord =
			CoordinateSequence::removeRepeatedPoints(hole->getCoordinatesRO());

		// Holes are topologically labelled opposite to the shell,
		// since the interior of the polygon lies on their opposite
		// side (on the left, if the hole is oriented CCW)
		addPolygonRing(
			holeCoord,
			offsetDistance,
			Position::opposite(offsetSide),
			Location::INTERIOR,
			Location::EXTERIOR);

		delete holeCoord;
	}
}

/* private */
void
OffsetCurveSetBuilder::addPolygonRing(const CoordinateSequence *coord,
	double offsetDistance, int side, int cwLeftLoc, int cwRightLoc)
{

	// don't bother adding ring if it is "flat" and
	// will disappear in the output
	if (offsetDistance == 0.0 && coord->size() < LinearRing::MINIMUM_VALID_SIZE)
		return;

	int leftLoc=cwLeftLoc;
	int rightLoc=cwRightLoc;
#if GEOS_DEBUG
	std::cerr<<"OffsetCurveSetBuilder::addPolygonRing: CCW: "<<CGAlgorithms::isCCW(coord)<<std::endl;
#endif
	if (coord->size() >= LinearRing::MINIMUM_VALID_SIZE
			&& CGAlgorithms::isCCW(coord))
	{
		leftLoc=cwRightLoc;
		rightLoc=cwLeftLoc;
#if GEOS_DEBUG
	std::cerr<<" side "<<side<<" becomes "<<Position::opposite(side)<<std::endl;
#endif
		side=Position::opposite(side);
	}
	std::vector<CoordinateSequence*> lineList;
	curveBuilder.getRingCurve(coord, side, offsetDistance, lineList);
	addCurves(lineList, leftLoc, rightLoc);
}

/*private*/
bool
OffsetCurveSetBuilder::isErodedCompletely(const LinearRing *ring,
	double bufferDistance)
{
	const CoordinateSequence *ringCoord = ring->getCoordinatesRO();

	// degenerate ring has no area
	if (ringCoord->getSize() < 4)
		return bufferDistance < 0;

	// important test to eliminate inverted triangle bug
	// also optimizes erosion test for triangles
	if (ringCoord->getSize() == 4)
		return isTriangleErodedCompletely(ringCoord, bufferDistance);

  const Envelope* env = ring->getEnvelopeInternal();
  double envMinDimension = std::min(env->getHeight(), env->getWidth());
  if (bufferDistance < 0.0 && 2 * std::abs(bufferDistance) > envMinDimension)
      return true;

	/**
	 * The following is a heuristic test to determine whether an
	 * inside buffer will be eroded completely->
	 * It is based on the fact that the minimum diameter of the ring
	 * pointset
	 * provides an upper bound on the buffer distance which would erode the
	 * ring->
	 * If the buffer distance is less than the minimum diameter, the ring
	 * may still be eroded, but this will be determined by
	 * a full topological computation->
	 *
	 */

/* MD  7 Feb 2005 - there's an unknown bug in the MD code,
 so disable this for now */
#if 0
	MinimumDiameter md(ring); //=new MinimumDiameter(ring);
	double minDiam = md.getLength();
	return minDiam < (2 * std::fabs(bufferDistance));
#endif

  return false;
}

/*private*/
bool
OffsetCurveSetBuilder::isTriangleErodedCompletely(
	const CoordinateSequence *triangleCoord, double bufferDistance)
{
	Triangle tri(triangleCoord->getAt(0), triangleCoord->getAt(1), triangleCoord->getAt(2));

	Coordinate inCentre;
	tri.inCentre(inCentre);
	double distToCentre=CGAlgorithms::distancePointLine(inCentre, tri.p0, tri.p1);
	bool ret = distToCentre < std::fabs(bufferDistance);
	return ret;
}


} // namespace geos.operation.buffer
} // namespace geos.operation
} // namespace geos
