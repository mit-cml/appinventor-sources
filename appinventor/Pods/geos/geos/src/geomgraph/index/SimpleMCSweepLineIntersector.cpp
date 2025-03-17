/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2001-2002 Vivid Solutions Inc.
 * Copyright (C) 2005 Refractions Research Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************/

#include <algorithm>
#include <vector>

#include <geos/geomgraph/index/SimpleMCSweepLineIntersector.h>
#include <geos/geomgraph/index/MonotoneChainEdge.h>
#include <geos/geomgraph/index/MonotoneChain.h>
#include <geos/geomgraph/index/SweepLineEvent.h>
#include <geos/geomgraph/Edge.h>
#include <geos/util/Interrupt.h>

using namespace std;

namespace geos {
namespace geomgraph { // geos.geomgraph
namespace index { // geos.geomgraph.index

SimpleMCSweepLineIntersector::SimpleMCSweepLineIntersector()
	//events(new vector<SweepLineEvent*>())
{
}

SimpleMCSweepLineIntersector::~SimpleMCSweepLineIntersector()
{
	for(size_t i=0; i<events.size(); ++i)
	{
		SweepLineEvent *sle=events[i];
		if (sle->isDelete()) delete sle;
	}
	//delete events;
}

void
SimpleMCSweepLineIntersector::computeIntersections(vector<Edge*> *edges,
	SegmentIntersector *si, bool testAllSegments)
{
	if (testAllSegments)
		add(edges,nullptr);
	else
		add(edges);
	computeIntersections(si);
}

void
SimpleMCSweepLineIntersector::computeIntersections(vector<Edge*> *edges0,
	vector<Edge*> *edges1, SegmentIntersector *si)
{
	add(edges0,edges0);
	add(edges1,edges1);
	computeIntersections(si);
}

void
SimpleMCSweepLineIntersector::add(vector<Edge*> *edges)
{
	for (size_t i=0; i<edges->size(); ++i)
	{
		Edge *edge=(*edges)[i];
		// edge is its own group
		add(edge, edge);
	}
}

void
SimpleMCSweepLineIntersector::add(vector<Edge*> *edges,void* edgeSet)
{
	for (size_t i=0; i<edges->size(); ++i)
	{
		Edge *edge=(*edges)[i];
		add(edge,edgeSet);
	}
}

void
SimpleMCSweepLineIntersector::add(Edge *edge, void* edgeSet)
{
	MonotoneChainEdge *mce=edge->getMonotoneChainEdge();
	vector<int> &startIndex=mce->getStartIndexes();
	size_t n = startIndex.size()-1;
	events.reserve(events.size()+(n*2));
	for(size_t i=0; i<n; ++i)
	{
		GEOS_CHECK_FOR_INTERRUPTS();
		MonotoneChain *mc=new MonotoneChain(mce, static_cast<int>(i));
		SweepLineEvent *insertEvent=new SweepLineEvent(edgeSet,mce->getMinX(static_cast<int>(i)),nullptr,mc);
		events.push_back(insertEvent);
		events.push_back(new SweepLineEvent(edgeSet,mce->getMaxX(static_cast<int>(i)),insertEvent,mc));
	}
}

/**
 * Because Delete Events have a link to their corresponding Insert event,
 * it is possible to compute exactly the range of events which must be
 * compared to a given Insert event object.
 */
void
SimpleMCSweepLineIntersector::prepareEvents()
{
	sort(events.begin(), events.end(), SweepLineEventLessThen());
	for(size_t i=0; i<events.size(); ++i)
	{
		GEOS_CHECK_FOR_INTERRUPTS();
		SweepLineEvent *ev=events[i];
		if (ev->isDelete())
		{
			ev->getInsertEvent()->setDeleteEventIndex(static_cast<int>(i));
		}
	}
}

void
SimpleMCSweepLineIntersector::computeIntersections(SegmentIntersector *si)
{
	nOverlaps=0;
	prepareEvents();
	for(size_t i=0; i<events.size(); ++i)
	{
		GEOS_CHECK_FOR_INTERRUPTS();
		SweepLineEvent *ev=events[i];
		if (ev->isInsert())
		{
			processOverlaps(static_cast<int>(i),ev->getDeleteEventIndex(),ev,si);
		}
		if (si->getIsDone())
		{
			break;
		}
	}
}

void
SimpleMCSweepLineIntersector::processOverlaps(int start, int end,
	SweepLineEvent *ev0, SegmentIntersector *si)
{
	MonotoneChain *mc0=(MonotoneChain*) ev0->getObject();

	/*
	 * Since we might need to test for self-intersections,
	 * include current insert event object in list of event objects to test.
	 * Last index can be skipped, because it must be a Delete event.
	 */
	for(int i=start; i<end; ++i)
	{
		SweepLineEvent *ev1=events[i];
		if (ev1->isInsert())
		{
			MonotoneChain *mc1=(MonotoneChain*) ev1->getObject();
			// don't compare edges in same group
			// null group indicates that edges should be compared
			if (ev0->edgeSet==nullptr || (ev0->edgeSet!=ev1->edgeSet))
			{
				mc0->computeIntersections(mc1,si);
				nOverlaps++;
			}
		}
	}
}

} // namespace geos.geomgraph.index
} // namespace geos.geomgraph
} // namespace geos
