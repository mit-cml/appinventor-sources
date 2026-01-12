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
 * Last port: noding/snapround/SimpleSnapRounder.java r320 (JTS-1.12)
 *
 **********************************************************************/

#include <geos/noding/snapround/SimpleSnapRounder.h>
#include <geos/noding/snapround/HotPixel.h>
#include <geos/noding/SegmentString.h>
#include <geos/noding/NodedSegmentString.h>
#include <geos/noding/NodingValidator.h>
#include <geos/noding/MCIndexNoder.h>
#include <geos/noding/IntersectionFinderAdder.h>
#include <geos/geom/Coordinate.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/algorithm/LineIntersector.h>
#include <geos/algorithm/LineIntersector.h>

#include <vector>
#include <exception>
#include <iostream>
#include <cassert>

using namespace std;
using namespace geos::algorithm;
using namespace geos::geom;

namespace geos {
namespace noding { // geos.noding
namespace snapround { // geos.noding.snapround

/*public*/
SimpleSnapRounder::SimpleSnapRounder(const geom::PrecisionModel& newPm)
	:
	pm(newPm),
	li(&newPm),
	scaleFactor(newPm.getScale())
{
}

/*public*/
std::vector<SegmentString*>*
SimpleSnapRounder::getNodedSubstrings() const
{
	std::vector<SegmentString*> *ret = new std::vector<SegmentString*>();
	NodedSegmentString::getNodedSubstrings(nodedSegStrings->begin(), nodedSegStrings->end(), ret);
	return ret;
}

/*public*/
void
SimpleSnapRounder::computeNodes(
		std::vector<SegmentString*>* inputSegmentStrings)
{
	nodedSegStrings = inputSegmentStrings;
	snapRound(inputSegmentStrings, li);

	// testing purposes only - remove in final version
	assert(nodedSegStrings == inputSegmentStrings);
	checkCorrectness(*inputSegmentStrings);
}


/*private*/
void
SimpleSnapRounder::checkCorrectness(
		SegmentString::NonConstVect& inputSegmentStrings)
{
  SegmentString::NonConstVect resultSegStrings;
  NodedSegmentString::getNodedSubstrings(
    inputSegmentStrings.begin(), inputSegmentStrings.end(), &resultSegStrings
  );

  NodingValidator nv(resultSegStrings);

  try {
    nv.checkValid();
  }

  catch (const std::exception &ex) {

    for ( SegmentString::NonConstVect::iterator i=resultSegStrings.begin(),
                                                e=resultSegStrings.end();
          i!=e; ++i )
    {
      delete *i;
    }

    std::cerr << ex.what() << std::endl;
    throw;
  }

  for ( SegmentString::NonConstVect::iterator i=resultSegStrings.begin(),
                                              e=resultSegStrings.end();
        i!=e; ++i )
  {
    delete *i;
  }

}

/*private*/
void
SimpleSnapRounder::computeSnaps(const SegmentString::NonConstVect& segStrings,
		vector<Coordinate>& snapPts)
{
	for (SegmentString::NonConstVect::const_iterator
			i=segStrings.begin(), iEnd=segStrings.end();
			i!=iEnd; ++i)
	{
		NodedSegmentString* ss = dynamic_cast<NodedSegmentString*>(*i);

		computeSnaps(ss, snapPts);
	}
}

/*private*/
void
SimpleSnapRounder::computeSnaps(NodedSegmentString* ss, vector<Coordinate>& snapPts)
{
	for (vector<Coordinate>::iterator
		it=snapPts.begin(), itEnd=snapPts.end();
		it!=itEnd; ++it)
	{
		const Coordinate& snapPt = *it;
		HotPixel hotPixel(snapPt, scaleFactor, li);
		for (int i=0, n=ss->size()-1; i<n; ++i) {
			hotPixel.addSnappedNode(*ss, i);
		}
	}
}

/*private*/
void
SimpleSnapRounder::computeVertexSnaps(NodedSegmentString* e0, NodedSegmentString* e1)
{
	const CoordinateSequence* pts0 = e0->getCoordinates();
	const CoordinateSequence* pts1 = e1->getCoordinates();

	for (unsigned int i0=0, n0=static_cast<unsigned int>(pts0->getSize()-1); i0<n0; i0++)
	{
		const Coordinate& p0 = pts0->getAt(i0);

		HotPixel hotPixel(p0, scaleFactor, li);
		for (unsigned int i1=1, n1=static_cast<unsigned int>(pts1->getSize()-1); i1<n1; i1++)
		{
        		// don't snap a vertex to itself
			if (i0 == i1 && e0 == e1) {
				continue;
			}
//cerr<<"trying "<<p0<<" against "<<pts1->getAt(i1)<<" "<<pts1->getAt(i1 + 1)<<endl;
			bool isNodeAdded = hotPixel.addSnappedNode(*e1, i1);
			// if a node is created for a vertex, that vertex must be noded too
			if (isNodeAdded) {
				e0->addIntersection(p0, i0);
			}
		}
	}

}

/*public*/
void
SimpleSnapRounder::computeVertexSnaps(const SegmentString::NonConstVect& edges)
{
	for (SegmentString::NonConstVect::const_iterator
			i0=edges.begin(), i0End=edges.end();
			i0!=i0End; ++i0)
	{
		NodedSegmentString* edge0 = dynamic_cast<NodedSegmentString*>(*i0);
		assert(edge0);
		for (SegmentString::NonConstVect::const_iterator
				i1=edges.begin(), i1End=edges.end();
				i1!=i1End; ++i1)
		{
			NodedSegmentString* edge1 = dynamic_cast<NodedSegmentString*>(*i1);
			assert(edge1);
			computeVertexSnaps(edge0, edge1);
		}
	}
}


/*private*/
void
SimpleSnapRounder::snapRound(SegmentString::NonConstVect* segStrings,
		LineIntersector& li)
{
	assert(segStrings);

	vector<Coordinate> intersections;
	findInteriorIntersections(*segStrings, li, intersections);
	computeSnaps(*segStrings, intersections);
	computeVertexSnaps(*segStrings);
}

/*private*/
void
SimpleSnapRounder::findInteriorIntersections(
	SegmentString::NonConstVect& segStrings,
	LineIntersector& li, vector<Coordinate>& ret)
{
	IntersectionFinderAdder intFinderAdder(li, ret);
	MCIndexNoder noder;
	noder.setSegmentIntersector(&intFinderAdder);
	noder.computeNodes(&segStrings);
	//intFinderAdder.getInteriorIntersections();
}


} // namespace geos.noding.snapround
} // namespace geos.noding
} // namespace geos
