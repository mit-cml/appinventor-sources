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
 **********************************************************************/

#include <geos/index/strtree/AbstractSTRtree.h>
#include <geos/index/strtree/AbstractNode.h>
#include <geos/index/strtree/ItemBoundable.h>
#include <geos/index/ItemVisitor.h>
// std
#include <algorithm>
#include <vector>
#include <typeinfo>
#include <cstddef>
#include <cassert>

using namespace std;

namespace geos {
namespace index { // geos.index
namespace strtree { // geos.index.strtree


AbstractSTRtree::~AbstractSTRtree()
{
	assert(nullptr != itemBoundables);
    BoundableList::iterator it = itemBoundables->begin();
    BoundableList::iterator end = itemBoundables->end();
	while (it != end)
	{
		delete *it;
        ++it;
	}
	delete itemBoundables;

	assert(nullptr != nodes);
	for (std::size_t i = 0, nsize = nodes->size(); i < nsize; i++)
    {
		delete (*nodes)[i];
    }
	delete nodes;
}

/*public*/
void
AbstractSTRtree::build()
{
	if(built)
		return;

	root=(itemBoundables->empty()?createNode(0):createHigherLevels(itemBoundables,-1));
	built=true;
}

/*protected*/
std::unique_ptr<BoundableList>
AbstractSTRtree::createParentBoundables(BoundableList* childBoundables,
		int newLevel)
{
	assert(!childBoundables->empty());
	std::unique_ptr< BoundableList > parentBoundables ( new BoundableList() );
	parentBoundables->push_back(createNode(newLevel));

	std::unique_ptr< BoundableList > sortedChildBoundables ( sortBoundables(childBoundables) );

	for (BoundableList::iterator i=sortedChildBoundables->begin(),
			e=sortedChildBoundables->end();
			i!=e; i++)
	//for(std::size_t i=0, scbsize=sortedChildBoundables->size(); i<scbsize; ++i)
	{
		Boundable *childBoundable=*i; // (*sortedChildBoundables)[i];

		AbstractNode *last = lastNode(parentBoundables.get());
		if (last->getChildBoundables()->size() == nodeCapacity)
		{
			last=createNode(newLevel);
			parentBoundables->push_back(last);
		}
		last->addChildBoundable(childBoundable);
	}
	return parentBoundables;
}

/*private*/
AbstractNode*
AbstractSTRtree::createHigherLevels(BoundableList* boundablesOfALevel, int level)
{
	assert(!boundablesOfALevel->empty());
	std::unique_ptr< BoundableList > parentBoundables (
			createParentBoundables(boundablesOfALevel,level+1)
			);

	if (parentBoundables->size()==1)
	{
		// Cast from Boundable to AbstractNode
		AbstractNode *ret = static_cast<AbstractNode*>(parentBoundables->front());
		return ret;
	}
	AbstractNode *ret = createHigherLevels(parentBoundables.get(), level+1);
	return ret;
}

/*protected*/
void
AbstractSTRtree::insert(const void* bounds,void* item)
{
	// Cannot insert items into an STR packed R-tree after it has been built
	assert(!built);
	itemBoundables->push_back(new ItemBoundable(bounds,item));
}

/*protected*/
void
AbstractSTRtree::query(const void* searchBounds, vector<void*>& matches)
{
	if (!built) build();

	if (itemBoundables->empty()) {
		assert(root->getBounds()==nullptr);
		return;
	}

	if (getIntersectsOp()->intersects(root->getBounds(), searchBounds))
	{
		query(searchBounds,root, &matches);
	}
}

/*protected*/
void
AbstractSTRtree::query(const void* searchBounds, ItemVisitor& visitor)
{
	if (!built) build();

	if (itemBoundables->empty()) {
        assert(root->getBounds()==nullptr);
        return;
	}

	if (getIntersectsOp()->intersects(root->getBounds(),searchBounds))
	{
		query(searchBounds, *root, visitor);
	}
}

/*protected*/
void
AbstractSTRtree::query(const void* searchBounds, const AbstractNode& node,
		ItemVisitor& visitor)
{

	const BoundableList& boundables = *(node.getChildBoundables());

	for (BoundableList::const_iterator i=boundables.begin(), e=boundables.end();
			i!=e; i++)
	{
		const Boundable* childBoundable = *i;
		if (!getIntersectsOp()->intersects(childBoundable->getBounds(), searchBounds)) {
			continue;
		}

		if(const AbstractNode *an=dynamic_cast<const AbstractNode*>(childBoundable))
		{
			query(searchBounds, *an, visitor);
		}
		else if (const ItemBoundable *ib=dynamic_cast<const ItemBoundable *>(childBoundable))
		{
			visitor.visitItem(ib->getItem());
		}
		else
		{
			assert(0); // unsupported childBoundable type
		}
	}
}

/* protected */
bool
AbstractSTRtree::remove(const void* searchBounds, void* item)
{
	if (!built) build();
	if (itemBoundables->empty()) {
		assert(root->getBounds() == nullptr);
	}
	if (getIntersectsOp()->intersects(root->getBounds(), searchBounds)) {
		return remove(searchBounds, *root, item);
	}
	return false;
}

/* private */
bool
AbstractSTRtree::remove(const void* searchBounds, AbstractNode& node, void* item)
{
	// first try removing item from this node
	if ( removeItem(node, item) ) return true;

	BoundableList& boundables = *(node.getChildBoundables());

	// next try removing item from lower nodes
	for (BoundableList::iterator i=boundables.begin(), e=boundables.end();
			i!=e; i++)
	{
		Boundable* childBoundable = *i;
		if (!getIntersectsOp()->intersects(childBoundable->getBounds(), searchBounds))
			continue;

		if (AbstractNode *an=dynamic_cast<AbstractNode*>(childBoundable))
		{
			// if found, record child for pruning and exit
			if ( remove(searchBounds, *an, item) )
			{
				if (an->getChildBoundables()->empty()) {
					boundables.erase(i);
				}
				return true;
			}
		}
	}

	return false;
}

/*private*/
bool
AbstractSTRtree::removeItem(AbstractNode& node, void* item)
{
	BoundableList& boundables = *(node.getChildBoundables());

	BoundableList::iterator childToRemove = boundables.end();

	for (BoundableList::iterator i=boundables.begin(),
			e=boundables.end();
			i!=e; i++)
	{
		Boundable* childBoundable = *i;
		if (ItemBoundable *ib=dynamic_cast<ItemBoundable*>(childBoundable))
		{
			if ( ib->getItem() == item) childToRemove = i;
		}
	}
	if (childToRemove != boundables.end()) {
		boundables.erase(childToRemove);
		return true;
	}
	return false;
}



/*public*/
void
AbstractSTRtree::query(const void* searchBounds,
	const AbstractNode* node, vector<void*> *matches)
{
	assert(node);

	const BoundableList& vb = *(node->getChildBoundables());


	IntersectsOp *io=getIntersectsOp();
	//std::size_t vbsize=vb.size();
	//cerr<<"AbstractSTRtree::query: childBoundables: "<<vbsize<<endl;
	for(BoundableList::const_iterator i=vb.begin(), e=vb.end();
			i!=e; ++i)
	{
		const Boundable* childBoundable=*i;
		if (!io->intersects(childBoundable->getBounds(), searchBounds))
		{
			continue;
		}

		if(const AbstractNode *an=dynamic_cast<const AbstractNode*>(childBoundable))
		{
			query(searchBounds, an, matches);
		}
		else if (const ItemBoundable *ib=dynamic_cast<const ItemBoundable *>(childBoundable))
		{
			matches->push_back(ib->getItem());
		}
		else
		{
			assert(0); // unsupported childBoundable type
		}
	}
}

void
AbstractSTRtree::iterate(ItemVisitor& visitor)
{
    for (BoundableList::const_iterator i=itemBoundables->begin(), e=itemBoundables->end();
			i!=e; i++)
	{
		const Boundable* boundable = *i;
		if (const ItemBoundable *ib=dynamic_cast<const ItemBoundable *>(boundable))
		{
			visitor.visitItem(ib->getItem());
		}
        }
}

/*protected*/
std::unique_ptr<BoundableList>
AbstractSTRtree::boundablesAtLevel(int level)
{
	std::unique_ptr<BoundableList> boundables ( new BoundableList() );
	boundablesAtLevel(level, root, boundables.get());
	return boundables;
}

/*public*/
void
AbstractSTRtree::boundablesAtLevel(int level, AbstractNode* top,
		BoundableList* boundables)
{
	assert(level>-2);
	if (top->getLevel()==level)
	{
		boundables->push_back(top);
		return;
	}

	assert(top);

	const BoundableList& vb = *(top->getChildBoundables());

	for(BoundableList::const_iterator i=vb.begin(), e=vb.end();
			i!=e; ++i)
	{
		Boundable* boundable=*i;
		if (typeid(*boundable)==typeid(AbstractNode))
		{
			boundablesAtLevel(level, (AbstractNode*)boundable,
				boundables);
		}
		else
		{
			assert(typeid(*boundable)==typeid(ItemBoundable));
			if (level==-1)
			{
				boundables->push_back(boundable);
			}
		}
	}
	return;
}

ItemsList* AbstractSTRtree::itemsTree(AbstractNode* node)
{
    std::unique_ptr<ItemsList> valuesTreeForNode (new ItemsList());

    BoundableList::iterator end = node->getChildBoundables()->end();
    for (BoundableList::iterator i = node->getChildBoundables()->begin();
         i != end; ++i)
    {
        Boundable* childBoundable = *i;
        if (dynamic_cast<AbstractNode*>(childBoundable)) {
            ItemsList* valuesTreeForChild =
                itemsTree(static_cast<AbstractNode*>(childBoundable));
            // only add if not null (which indicates an item somewhere in this tree
            if (valuesTreeForChild != nullptr)
                valuesTreeForNode->push_back_owned(valuesTreeForChild);
        }
        else if (dynamic_cast<ItemBoundable*>(childBoundable)) {
            valuesTreeForNode->push_back(
                static_cast<ItemBoundable*>(childBoundable)->getItem());
        }
        else {
            assert(!static_cast<bool>("should never be reached"));
        }
    }
    if (valuesTreeForNode->empty())
        return nullptr;

    return valuesTreeForNode.release();
}

ItemsList* AbstractSTRtree::itemsTree()
{
    if (!built) {
        build();
    }

    ItemsList* valuesTree (itemsTree(root));
    if (valuesTree == nullptr)
        return new ItemsList();

    return valuesTree;
}

} // namespace geos.index.strtree
} // namespace geos.index
} // namespace geos
