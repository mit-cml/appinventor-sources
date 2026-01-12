/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2006 Refractions Research Inc.
 * Copyright (C) 2001-2002 Vivid Solutions Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: index/strtree/STRtree.java rev. 1.11
 *
 **********************************************************************/

#include <geos/index/strtree/STRtree.h>
#include <geos/index/strtree/BoundablePair.h>
#include <geos/geom/Envelope.h>

#include <vector>
#include <cassert>
#include <cmath>
#include <algorithm> // std::sort
#include <iostream> // for debugging
#include <limits>
#include <geos/util/GEOSException.h>

using namespace std;
using namespace geos::geom;

namespace geos {
namespace index { // geos.index
namespace strtree { // geos.index.strtree


static bool yComparator(Boundable *a, Boundable *b)
{
	assert(a);
	assert(b);
	const void* aBounds = a->getBounds();
	const void* bBounds = b->getBounds();
	assert(aBounds);
	assert(bBounds);
	const Envelope* aEnv = static_cast<const Envelope*>(aBounds);
	const Envelope* bEnv = static_cast<const Envelope*>(bBounds);

    // NOTE - mloskot:
    // The problem of instability is directly related to mathematical definition of
    // "strict weak ordering" as a fundamental requirement for binary predicate:
    //
    // if a is less than b then b is not less than a,
    // if a is less than b and b is less than c
    // then a is less than c,
    // and so on.
    //
    // For some very low values, this predicate does not fulfill this requiremnet,

    // NOTE - strk:
	// It seems that the '<' comparison here gives unstable results.
	// In particular, when inlines are on (for Envelope::getMinY and getMaxY)
	// things are fine, but when they are off we can even get a memory corruption !!
	//return STRtree::centreY(aEnv) < STRtree::centreY(bEnv);

    // NOTE - mloskot:
    // This comparison does not answer if a is "lower" than b
    // what is required for sorting. This comparison only answeres
    // if a and b are "almost the same" or different

    /*NOTE - cfis
      In debug mode VC++ checks the predicate in both directions.

      If !_Pred(_Left, _Right)
      Then an exception is thrown if _Pred(_Right, _Left).
      See xutility around line 320:

      	bool __CLRCALL_OR_CDECL _Debug_lt_pred(_Pr _Pred, _Ty1& _Left, _Ty2& _Right,
		const wchar_t *_Where, unsigned int _Line)*/

    //return std::fabs( STRtree::centreY(aEnv) - STRtree::centreY(bEnv) ) < 1e-30

    // NOTE - strk:
    // See http://trac.osgeo.org/geos/ticket/293
    // as for why simple comparison (<) isn't used here
    return AbstractSTRtree::compareDoubles(STRtree::centreY(aEnv),
                                           STRtree::centreY(bEnv));
}

/*public*/
STRtree::STRtree(size_t nodeCapacity): AbstractSTRtree(nodeCapacity)
{
}

/*public*/
STRtree::~STRtree()
{
}

bool
STRtree::STRIntersectsOp::intersects(const void* aBounds, const void* bBounds)
{
	return ((Envelope*)aBounds)->intersects((Envelope*)bBounds);
}

/*private*/
std::unique_ptr<BoundableList>
STRtree::createParentBoundables(BoundableList* childBoundables, int newLevel)
{
	assert(!childBoundables->empty());
	int minLeafCount=(int) ceil((double)childBoundables->size()/(double)getNodeCapacity());

	std::unique_ptr<BoundableList> sortedChildBoundables ( sortBoundables(childBoundables) );

	std::unique_ptr< vector<BoundableList*> > verticalSlicesV (
			verticalSlices(sortedChildBoundables.get(), (int)ceil(sqrt((double)minLeafCount)))
			);

	std::unique_ptr<BoundableList> ret (
		createParentBoundablesFromVerticalSlices(verticalSlicesV.get(), newLevel)
	);
	for (size_t i=0, vssize=verticalSlicesV->size(); i<vssize; ++i)
	{
		BoundableList* inner = (*verticalSlicesV)[i];
		delete inner;
	}

	return ret;
}

/*private*/
std::unique_ptr<BoundableList>
STRtree::createParentBoundablesFromVerticalSlices(std::vector<BoundableList*>* verticalSlices, int newLevel)
{
	assert(!verticalSlices->empty());
	std::unique_ptr<BoundableList> parentBoundables( new BoundableList() );

	for (size_t i=0, vssize=verticalSlices->size(); i<vssize; ++i)
	{
		std::unique_ptr<BoundableList> toAdd (
			createParentBoundablesFromVerticalSlice(
				(*verticalSlices)[i], newLevel)
			);
		assert(!toAdd->empty());

		parentBoundables->insert(
				parentBoundables->end(),
				toAdd->begin(),
				toAdd->end());
	}
	return parentBoundables;
}

/*protected*/
std::unique_ptr<BoundableList>
STRtree::createParentBoundablesFromVerticalSlice(BoundableList* childBoundables, int newLevel)
{
	return AbstractSTRtree::createParentBoundables(childBoundables, newLevel);
}

/*private*/
std::vector<BoundableList*>*
STRtree::verticalSlices(BoundableList* childBoundables, size_t sliceCount)
{
	size_t sliceCapacity = (size_t) ceil((double)childBoundables->size() / (double) sliceCount);
	vector<BoundableList*>* slices = new vector<BoundableList*>(sliceCount);

	size_t i=0, nchilds=childBoundables->size();

	for (size_t j=0; j<sliceCount; j++)
	{
		(*slices)[j]=new BoundableList();
		(*slices)[j]->reserve(sliceCapacity);
		size_t boundablesAddedToSlice = 0;
		while (i<nchilds && boundablesAddedToSlice<sliceCapacity)
		{
			Boundable *childBoundable=(*childBoundables)[i];
			++i;
			(*slices)[j]->push_back(childBoundable);
			++boundablesAddedToSlice;
		}
	}
	return slices;
}

/*public*/
const void* STRtree::nearestNeighbour(const Envelope* env, const void* item, ItemDistance* itemDist) {
	build();

	ItemBoundable bnd = ItemBoundable(env, (void*) item);
	BoundablePair bp(getRoot(), &bnd, itemDist);

	return nearestNeighbour(&bp).first;
}

std::pair<const void*, const void*> STRtree::nearestNeighbour(BoundablePair* initBndPair) {
	return nearestNeighbour(initBndPair, std::numeric_limits<double>::infinity());
}

std::pair<const void*, const void*> STRtree::nearestNeighbour(ItemDistance * itemDist) {
	BoundablePair bp(this->getRoot(), this->getRoot(), itemDist);
	return nearestNeighbour(&bp);
}

std::pair<const void*, const void*> STRtree::nearestNeighbour(STRtree* tree, ItemDistance* itemDist) {
	BoundablePair bp(getRoot(), tree->getRoot(), itemDist);
	return nearestNeighbour(&bp);
}

std::pair<const void*, const void*> STRtree::nearestNeighbour(BoundablePair* initBndPair, double maxDistance) {
	double distanceLowerBound = maxDistance;
	BoundablePair* minPair = nullptr;

	BoundablePair::BoundablePairQueue priQ;
	priQ.push(initBndPair);

	while(!priQ.empty() && distanceLowerBound > 0.0) {
		BoundablePair* bndPair = priQ.top();
		double currentDistance = bndPair->getDistance();

		/**
		 * If the distance for the first node in the queue
		 * is >= the current minimum distance, all other nodes
		 * in the queue must also have a greater distance.
		 * So the current minDistance must be the true minimum,
		 * and we are done.
		 */
		if (minPair && currentDistance >= distanceLowerBound)
			break;

		priQ.pop();

		/**
		 * If the pair members are leaves
		 * then their distance is the exact lower bound.
		 * Update the distanceLowerBound to reflect this
		 * (which must be smaller, due to the test
		 * immediately prior to this).
		 */
		if (bndPair->isLeaves()) {
			distanceLowerBound = currentDistance;
			minPair = bndPair;
		} else {
			/**
			 * Otherwise, expand one side of the pair,
			 * (the choice of which side to expand is heuristically determined)
			 * and insert the new expanded pairs into the queue
			 */
			bndPair->expandToQueue(priQ, distanceLowerBound);
		}

		if (bndPair != initBndPair && bndPair != minPair)
            delete bndPair;
	}

	/* Free any remaining BoundablePairs in the queue */
	while(!priQ.empty()) {
		BoundablePair* bndPair = priQ.top();
		priQ.pop();
		if (bndPair != initBndPair)
            delete bndPair;
	}

	if (!minPair)
		throw util::GEOSException("Error computing nearest neighbor");

	const void* item0 = dynamic_cast<const ItemBoundable*>(minPair->getBoundable(0))->getItem();
	const void* item1 = dynamic_cast<const ItemBoundable*>(minPair->getBoundable(1))->getItem();
	if (minPair != initBndPair)
        delete minPair;

	return std::pair<const void*, const void*>(item0, item1);
}

class STRAbstractNode: public AbstractNode{
public:

	STRAbstractNode(int level, int capacity)
		:
		AbstractNode(level, capacity)
	{}

	~STRAbstractNode() override
	{
		delete (Envelope *)bounds;
	}

protected:

	void* computeBounds() const override
	{
		Envelope* bounds=nullptr;
		const BoundableList& b = *getChildBoundables();

		if ( b.empty() ) return nullptr;

		BoundableList::const_iterator i=b.begin();
		BoundableList::const_iterator e=b.end();

		bounds=new Envelope(* static_cast<const Envelope*>((*i)->getBounds()) );
		for(; i!=e; ++i)
		{
			const Boundable* childBoundable=*i;
			bounds->expandToInclude((Envelope*)childBoundable->getBounds());
		}
		return bounds;
	}

};

/*protected*/
AbstractNode*
STRtree::createNode(int level)
{
	AbstractNode *an = new STRAbstractNode(level, static_cast<int>(nodeCapacity));
	nodes->push_back(an);
	return an;
}

/*public*/
void
STRtree::insert(const Envelope *itemEnv, void* item)
{
	if (itemEnv->isNull()) { return; }
	AbstractSTRtree::insert(itemEnv, item);
}

/*private*/
std::unique_ptr<BoundableList>
STRtree::sortBoundables(const BoundableList* input)
{
	assert(input);
	std::unique_ptr<BoundableList> output ( new BoundableList(*input) );
	assert(output->size() == input->size());

	sort(output->begin(), output->end(), yComparator);
	return output;
}

} // namespace geos.index.strtree
} // namespace geos.index
} // namespace geos

