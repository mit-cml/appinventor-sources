/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2001-2002 Vivid Solutions Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: noding/MCIndexSegmentSetMutualIntersector.java r388 (JTS-1.12)
 *
 **********************************************************************/

#include <geos/noding/MCIndexSegmentSetMutualIntersector.h>
#include <geos/noding/SegmentSetMutualIntersector.h>
#include <geos/noding/SegmentString.h>
#include <geos/noding/SegmentIntersector.h>
#include <geos/index/SpatialIndex.h>
#include <geos/index/chain/MonotoneChain.h>
#include <geos/index/chain/MonotoneChainBuilder.h>
#include <geos/index/chain/MonotoneChainOverlapAction.h>
#include <geos/index/strtree/STRtree.h>
// std
#include <cstddef>

using namespace geos::index::chain;

namespace geos {
namespace noding { // geos::noding

/*private*/
void
MCIndexSegmentSetMutualIntersector::addToIndex(SegmentString* segStr)
{
    MonoChains segChains;
    MonotoneChainBuilder::getChains(segStr->getCoordinates(),
      segStr, segChains);

    MonoChains::size_type n = segChains.size();
    chainStore.reserve(chainStore.size() + n);
    for (MonoChains::size_type i = 0; i < n; i++)
    {
        MonotoneChain * mc = segChains[i];
        mc->setId(indexCounter++);
        index->insert(&(mc->getEnvelope()), mc);
        chainStore.push_back(mc);
    }
}


/*private*/
void
MCIndexSegmentSetMutualIntersector::intersectChains()
{
    MCIndexSegmentSetMutualIntersector::SegmentOverlapAction overlapAction( *segInt);

    for (MonoChains::size_type i = 0, ni = monoChains.size(); i < ni; ++i)
    {
        MonotoneChain * queryChain = (MonotoneChain *)monoChains[i];

        std::vector<void*> overlapChains;
        index->query( &(queryChain->getEnvelope()), overlapChains);

        for (std::size_t j = 0, nj = overlapChains.size(); j < nj; j++)
        {
            MonotoneChain * testChain = (MonotoneChain *)(overlapChains[j]);

            queryChain->computeOverlaps( testChain, &overlapAction);
            nOverlaps++;
            if (segInt->isDone())
                return;
        }
    }
}

/*private*/
void
MCIndexSegmentSetMutualIntersector::addToMonoChains(SegmentString* segStr)
{
    MonoChains segChains;
    MonotoneChainBuilder::getChains(segStr->getCoordinates(),
                                    segStr, segChains);

    MonoChains::size_type n = segChains.size();
    monoChains.reserve(monoChains.size() + n);
    for (MonoChains::size_type i = 0; i < n; i++)
    {
        MonotoneChain* mc = segChains[i];
        mc->setId( processCounter++ );
        monoChains.push_back(mc);
    }
}

/* public */
MCIndexSegmentSetMutualIntersector::MCIndexSegmentSetMutualIntersector()
:	monoChains(),
index(new geos::index::strtree::STRtree()),
indexCounter(0),
processCounter(0),
nOverlaps(0)
{
}

/* public */
MCIndexSegmentSetMutualIntersector::~MCIndexSegmentSetMutualIntersector()
{
    delete index;

    MonoChains::iterator i, e;

    for (i = chainStore.begin(), e = chainStore.end(); i != e; ++i) {
        delete *i;
    }

    for (i = monoChains.begin(), e = monoChains.end(); i != e; i++) {
      delete *i;
    }
}

/* public */
void
MCIndexSegmentSetMutualIntersector::setBaseSegments(SegmentString::ConstVect* segStrings)
{
    // NOTE - mloskot: const qualifier is removed silently, dirty.

    for (std::size_t i = 0, n = segStrings->size(); i < n; i++)
    {
        const SegmentString* css = (*segStrings)[i];
        SegmentString* ss = const_cast<SegmentString*>(css);
        addToIndex(ss);
    }
}

/*public*/
void
MCIndexSegmentSetMutualIntersector::process(SegmentString::ConstVect * segStrings)
{
    processCounter = indexCounter + 1;
    nOverlaps = 0;

    for (MonoChains::iterator i = monoChains.begin(), e = monoChains.end();
         i != e; i++)
    {
      delete *i;
    }
    monoChains.clear();

    for (SegmentString::ConstVect::size_type i = 0, n = segStrings->size(); i < n; i++)
    {
        SegmentString * seg = (SegmentString *)((*segStrings)[i]);
        addToMonoChains( seg);
    }
    intersectChains();
}


/* public */
void
MCIndexSegmentSetMutualIntersector::SegmentOverlapAction::overlap(
	MonotoneChain& mc1, size_t start1, MonotoneChain& mc2, size_t start2)
{
    SegmentString * ss1 = (SegmentString *)(mc1.getContext());
    SegmentString * ss2 = (SegmentString *)(mc2.getContext());

    si.processIntersections(ss1, static_cast<int>(start1), ss2, static_cast<int>(start2));
}

} // geos::noding
} // geos

