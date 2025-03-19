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
 * Last port: noding/snapround/MCIndexSnapRounder.java r486 (JTS-1.12+)
 *
 **********************************************************************/

#include <geos/noding/MCIndexNoder.h>
#include <geos/noding/snapround/MCIndexSnapRounder.h>
#include <geos/noding/snapround/HotPixel.h>
#include <geos/noding/IntersectionFinderAdder.h>
#include <geos/noding/NodingValidator.h>
#include <geos/geom/Coordinate.h>
#include <geos/geom/CoordinateSequence.h>

#include <geos/inline.h>

#include <functional> // std::mem_fun, std::bind1st
#include <algorithm> // std::for_each
#include <vector>


using namespace std;
using namespace geos::geom;

namespace geos {
namespace noding { // geos.noding
namespace snapround { // geos.noding.snapround

/*private*/
void
MCIndexSnapRounder::findInteriorIntersections(MCIndexNoder& noder,
		NodedSegmentString::NonConstVect* segStrings,
		vector<Coordinate>& intersections)
{
	IntersectionFinderAdder intFinderAdder(li, intersections);
	noder.setSegmentIntersector(&intFinderAdder);
	noder.computeNodes(segStrings);
}

/* private */
void
MCIndexSnapRounder::computeIntersectionSnaps(vector<Coordinate>& snapPts)
{
	for (vector<Coordinate>::iterator
			it=snapPts.begin(), itEnd=snapPts.end();
			it!=itEnd;
			++it)
	{
		Coordinate& snapPt = *it;
		HotPixel hotPixel(snapPt, scaleFactor, li);
		pointSnapper->snap(hotPixel);
	}
}

/*private*/
void
MCIndexSnapRounder::computeVertexSnaps(NodedSegmentString* e)
{
	CoordinateSequence& pts0 = *(e->getCoordinates());
	for (unsigned int i=0, n=static_cast<unsigned int>(pts0.size()-1); i<n; ++i)
	{
		HotPixel hotPixel(pts0[i], scaleFactor, li);
		bool isNodeAdded = pointSnapper->snap(hotPixel, e, i);
		// if a node is created for a vertex, that vertex must be noded too
		if (isNodeAdded) {
			e->addIntersection(pts0[i], i);
		}
	}
}

/*public*/
void
MCIndexSnapRounder::computeVertexSnaps(SegmentString::NonConstVect& edges)
{
	SegmentString::NonConstVect::iterator i=edges.begin(), e=edges.end();
	for (; i!=e; ++i)
	{
		NodedSegmentString* edge0 =
			dynamic_cast<NodedSegmentString*>(*i);
		assert(edge0);
		computeVertexSnaps(edge0);
	}
}

/*private*/
void
MCIndexSnapRounder::snapRound(MCIndexNoder& noder,
		SegmentString::NonConstVect* segStrings)
{
	vector<Coordinate> intersections;
 	findInteriorIntersections(noder, segStrings, intersections);
	computeIntersectionSnaps(intersections);
	computeVertexSnaps(*segStrings);

}

/*public*/
void
MCIndexSnapRounder::computeNodes(SegmentString::NonConstVect* inputSegmentStrings)
{
	nodedSegStrings = inputSegmentStrings;
	MCIndexNoder noder;
	pointSnapper.release(); // let it leak ?!
	pointSnapper.reset(new MCIndexPointSnapper(noder.getIndex()));
	snapRound(noder, inputSegmentStrings);

	// testing purposes only - remove in final version
	assert(nodedSegStrings == inputSegmentStrings);
	//checkCorrectness(*inputSegmentStrings);
}

/*private*/
void
MCIndexSnapRounder::checkCorrectness(
	SegmentString::NonConstVect& inputSegmentStrings)
{
	unique_ptr<SegmentString::NonConstVect> resultSegStrings(
		NodedSegmentString::getNodedSubstrings(inputSegmentStrings)
	);

	NodingValidator nv(*(resultSegStrings.get()));
	try {
		nv.checkValid();
	} catch (const std::exception &ex) {
		std::cerr << ex.what() << std::endl;
		throw;
	}
}


} // namespace geos.noding.snapround
} // namespace geos.noding
} // namespace geos

