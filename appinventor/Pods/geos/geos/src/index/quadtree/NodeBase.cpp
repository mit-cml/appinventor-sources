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
 * Last port: index/quadtree/NodeBase.java rev 1.9 (JTS-1.10)
 *
 **********************************************************************/

#include <geos/index/quadtree/NodeBase.h>
#include <geos/index/quadtree/Node.h>
#include <geos/index/ItemVisitor.h>
#include <geos/geom/Envelope.h>
#include <geos/geom/Coordinate.h>
#include <geos/util.h>

#include <sstream>
#include <vector>
#include <algorithm>

#ifndef GEOS_DEBUG
#define GEOS_DEBUG 0
#endif

#if GEOS_DEBUG
#include <iostream>
#endif

using namespace std;
using namespace geos::geom;

namespace geos {
namespace index { // geos.index
namespace quadtree { // geos.index.quadtree

int
NodeBase::getSubnodeIndex(const Envelope *env, const Coordinate& centre)
{
	int subnodeIndex=-1;
	if (env->getMinX()>=centre.x) {
		if (env->getMinY()>=centre.y) subnodeIndex=3;
		if (env->getMaxY()<=centre.y) subnodeIndex=1;
	}
	if (env->getMaxX()<=centre.x) {
		if (env->getMinY()>=centre.y) subnodeIndex=2;
		if (env->getMaxY()<=centre.y) subnodeIndex=0;
	}
#if GEOS_DEBUG
	cerr<<"getSubNodeIndex("<<env->toString()<<", "<<centre.toString()<<") returning "<<subnodeIndex<<endl;
#endif
	return subnodeIndex;
}

NodeBase::NodeBase()
{
	subnode[0]=nullptr;
	subnode[1]=nullptr;
	subnode[2]=nullptr;
	subnode[3]=nullptr;
}

NodeBase::~NodeBase()
{
	delete subnode[0];
	delete subnode[1];
	delete subnode[2];
	delete subnode[3];
	subnode[0]=nullptr;
	subnode[1]=nullptr;
	subnode[2]=nullptr;
	subnode[3]=nullptr;
}

vector<void*>&
NodeBase::getItems()
{
	return items;
}

void
NodeBase::add(void* item)
{
	items.push_back(item);
	//GEOS_DEBUG itemCount++;
	//GEOS_DEBUG System.out.print(itemCount);
}

vector<void*>&
NodeBase::addAllItems(vector<void*>& resultItems) const
{
	// this node may have items as well as subnodes (since items may not
	// be wholely contained in any single subnode
	resultItems.insert(resultItems.end(), items.begin(), items.end());

	for(int i=0; i<4; ++i)
	{
		if ( subnode[i] )
		{
			subnode[i]->addAllItems(resultItems);
		}
	}
	return resultItems;
}

void
NodeBase::addAllItemsFromOverlapping(const Envelope& searchEnv,
                                     vector<void*>& resultItems) const
{
	if (!isSearchMatch(searchEnv))
		return;

	// this node may have items as well as subnodes (since items may not
	// be wholely contained in any single subnode
	resultItems.insert(resultItems.end(), items.begin(), items.end());

	for(int i=0; i<4; ++i)
	{
		if ( subnode[i] )
		{
			subnode[i]->addAllItemsFromOverlapping(searchEnv,
			                                       resultItems);
		}
	}
}

//<<TODO:RENAME?>> In Samet's terminology, I think what we're returning here is
//actually level+1 rather than depth. (See p. 4 of his book) [Jon Aquino]
unsigned int
NodeBase::depth() const
{
	unsigned int maxSubDepth=0;
	for (int i=0; i<4; ++i)
	{
		if (subnode[i] != nullptr)
		{
			unsigned int sqd=subnode[i]->depth();
			if ( sqd > maxSubDepth )
				maxSubDepth=sqd;
		}
	}
	return maxSubDepth + 1;
}

unsigned int
NodeBase::size() const
{
	unsigned int subSize=0;
	for(int i=0; i<4; i++)
	{
		if (subnode[i] != nullptr)
		{
			subSize += subnode[i]->size();
		}
	}
	return subSize + static_cast<unsigned int>(items.size());
}

unsigned int
NodeBase::getNodeCount() const
{
	unsigned int subSize=0;
	for(int i=0; i<4; ++i)
	{
		if (subnode[i] != nullptr)
		{
			subSize += subnode[i]->size();
		}
	}

	return subSize + 1;
}

string
NodeBase::toString() const
{
	ostringstream s;
	s<<"ITEMS:"<<items.size()<<endl;
	for (int i=0; i<4; i++)
	{
		s<<"subnode["<<i<<"] ";
		if ( subnode[i] == nullptr ) s<<"NULL";
		else s<<subnode[i]->toString();
		s<<endl;
	}
	return s.str();
}

/*public*/
void
NodeBase::visit(const Envelope* searchEnv, ItemVisitor& visitor)
{
	if (! isSearchMatch(*searchEnv)) return;

	// this node may have items as well as subnodes (since items may not
	// be wholely contained in any single subnode
	visitItems(searchEnv, visitor);

	for (int i = 0; i < 4; i++) {
		if (subnode[i] != nullptr) {
			subnode[i]->visit(searchEnv, visitor);
		}
	}
}

/*private*/
void
NodeBase::visitItems(const Envelope* searchEnv, ItemVisitor& visitor)
{
    ::geos::ignore_unused_variable_warning(searchEnv);

	// would be nice to filter items based on search envelope, but can't
	// until they contain an envelope
	for (vector<void*>::iterator i=items.begin(), e=items.end();
			i!=e; i++)
	{
		visitor.visitItem(*i);
	}
}

/*public*/
bool
NodeBase::remove(const Envelope* itemEnv, void* item)
{
	// use envelope to restrict nodes scanned
	if (! isSearchMatch(*itemEnv)) return false;

	bool found = false;
	for (int i = 0; i < 4; ++i)
	{
		if ( subnode[i] )
		{
			found = subnode[i]->remove(itemEnv, item);
			if (found)
			{
				// trim subtree if empty
				if (subnode[i]->isPrunable())
				{
					delete subnode[i];
					subnode[i] = nullptr;
				}
				break;
			}
		}
	}
	// if item was found lower down, don't need to search for it here
	if (found) return found;

	// otherwise, try and remove the item from the list of items
	// in this node
	vector<void*>::iterator foundIter =
		find(items.begin(), items.end(), item);
	if ( foundIter != items.end() ) {
		items.erase(foundIter);
		return true;
	} else {
		return false;
	}
}


} // namespace geos.index.quadtree
} // namespace geos.index
} // namespace geos
