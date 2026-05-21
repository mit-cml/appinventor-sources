/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2001-2002 Vivid Solutions Inc.
 * Copyright (C) 2006 Refractions Research Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: noding/SimpleNoder.java rev. 1.7 (JTS-1.9)
 *
 **********************************************************************/

#include <geos/noding/SimpleNoder.h>
#include <geos/noding/SegmentString.h>
#include <geos/noding/SegmentIntersector.h>
#include <geos/geom/CoordinateSequence.h>

using namespace geos::geom;

namespace geos {
namespace noding { // geos.noding

/*private*/
void
SimpleNoder::computeIntersects(SegmentString* e0, SegmentString* e1)
{
	assert(segInt); // must provide a segment intersector!

	const CoordinateSequence* pts0 = e0->getCoordinates();
	const CoordinateSequence* pts1 = e1->getCoordinates();
	for (unsigned int i0=0, n0=static_cast<unsigned int>(pts0->getSize()-1); i0<n0; i0++) {
		for (unsigned int i1=0, n1=static_cast<unsigned int>(pts1->getSize()-1); i1<n1; i1++) {
			segInt->processIntersections(e0, i0, e1, i1);
		}
	}

}

/*public*/
void
SimpleNoder::computeNodes(SegmentString::NonConstVect* inputSegmentStrings)
{
	nodedSegStrings=inputSegmentStrings;

	for (SegmentString::NonConstVect::const_iterator
			i0=inputSegmentStrings->begin(), i0End=inputSegmentStrings->end();
			i0!=i0End; ++i0)
	{
		SegmentString* edge0 = *i0;
		for (SegmentString::NonConstVect::iterator
			i1=inputSegmentStrings->begin(), i1End=inputSegmentStrings->end();
			i1!=i1End; ++i1)
		{
		        SegmentString* edge1 = *i1;
			computeIntersects(edge0, edge1);
		}
	}
}


} // namespace geos.noding
} // namespace geos
