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
 * Last port: noding/MCIndexNoder.java rev. 1.6 (JTS-1.9)
 *
 **********************************************************************/

#include <geos/noding/MCIndexNoder.h>
#include <geos/noding/SegmentIntersector.h>
#include <geos/noding/NodedSegmentString.h>
#include <geos/index/chain/MonotoneChain.h>
#include <geos/index/chain/MonotoneChainBuilder.h>
#include <geos/util/Interrupt.h>

#include <cassert>
#include <functional>
#include <algorithm>

#ifndef GEOS_DEBUG
#define GEOS_DEBUG 0
#endif

#ifndef GEOS_INLINE
# include <geos/noding/MCIndexNoder.inl>
#endif

using namespace std;
using namespace geos::index::chain;

namespace geos {
namespace noding { // geos.noding

/*public*/
void
MCIndexNoder::computeNodes(SegmentString::NonConstVect* inputSegStrings)
{
	nodedSegStrings = inputSegStrings;
	assert(nodedSegStrings);

	for_each(nodedSegStrings->begin(), nodedSegStrings->end(),
			bind1st(mem_fun(&MCIndexNoder::add), this));

	intersectChains();
//cerr<<"MCIndexNoder: # chain overlaps = "<<nOverlaps<<endl;
}

/*private*/
void
MCIndexNoder::intersectChains()
{
	assert(segInt);

	SegmentOverlapAction overlapAction(*segInt);

	for (vector<MonotoneChain*>::iterator
			i=monoChains.begin(), iEnd=monoChains.end();
			i != iEnd;
			++i)
	{

		GEOS_CHECK_FOR_INTERRUPTS();

		MonotoneChain* queryChain = *i;
		assert(queryChain);
		vector<void*> overlapChains;
		index.query(&(queryChain->getEnvelope()), overlapChains);
		for (vector<void*>::iterator
			j=overlapChains.begin(), jEnd=overlapChains.end();
			j != jEnd;
			++j)
		{
			MonotoneChain* testChain = static_cast<MonotoneChain*>(*j);
			assert(testChain);

			/**
			 * following test makes sure we only compare each
			 * pair of chains once and that we don't compare a
			 * chain to itself
			 */
			if (testChain->getId() > queryChain->getId()) {
				queryChain->computeOverlaps(testChain,
						&overlapAction);
				nOverlaps++;
			}

			// short-circuit if possible
			if (segInt->isDone()) return;

		}
	}
}

/*private*/
void
MCIndexNoder::add(SegmentString* segStr)
{
	vector<MonotoneChain*> segChains;

	// segChains will contain nelwy allocated MonotoneChain objects
	MonotoneChainBuilder::getChains(segStr->getCoordinates(),
			segStr, segChains);

	for(vector<MonotoneChain*>::iterator
			it=segChains.begin(), iEnd=segChains.end();
			it!=iEnd; ++it)
	{
		MonotoneChain* mc = *it;
		assert(mc);

		mc->setId(idCounter++);
		index.insert(&(mc->getEnvelope()), mc);

		// MonotoneChain objects deletion delegated to destructor
		monoChains.push_back(mc);
	}
}

MCIndexNoder::~MCIndexNoder()
{
	for(vector<MonotoneChain*>::iterator
			i=monoChains.begin(), iEnd=monoChains.end();
			i!=iEnd; ++i)
	{
		assert(*i);
		delete *i;
	}
}

void
MCIndexNoder::SegmentOverlapAction::overlap(MonotoneChain& mc1, size_t start1,
		MonotoneChain& mc2, size_t start2)
{
	SegmentString* ss1 = const_cast<SegmentString*>(
		static_cast<const SegmentString *>(mc1.getContext())
		);
	assert(ss1);

	SegmentString* ss2 = const_cast<SegmentString*>(
		static_cast<const SegmentString *>(mc2.getContext())
		);
	assert(ss2);

	si.processIntersections(ss1, static_cast<int>(start1), ss2, static_cast<int>(start2));
}


} // namespace geos.noding
} // namespace geos
