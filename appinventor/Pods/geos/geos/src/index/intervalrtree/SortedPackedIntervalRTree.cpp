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
 **********************************************************************/

#include <geos/index/intervalrtree/SortedPackedIntervalRTree.h>
#include <geos/index/intervalrtree/IntervalRTreeNode.h>
#include <geos/index/intervalrtree/IntervalRTreeLeafNode.h>
#include <geos/index/intervalrtree/IntervalRTreeBranchNode.h>
#include <geos/index/ItemVisitor.h>
#include <geos/util/UnsupportedOperationException.h>

#include <algorithm>

#ifdef _MSC_VER
#pragma warning(disable : 4127)
#endif

namespace geos {
namespace index {
namespace intervalrtree {
//
// private:
//
void
SortedPackedIntervalRTree::init()
{
	if (root != nullptr) return;

	root = buildTree();
}

const IntervalRTreeNode *
SortedPackedIntervalRTree::buildTree()
{
	// sort the leaf nodes
	std::sort( leaves->begin(), leaves->end(), IntervalRTreeNode::compare );

	// now group nodes into blocks of two and build tree up recursively
	IntervalRTreeNode::ConstVect * src = leaves;
	IntervalRTreeNode::ConstVect * dest = new IntervalRTreeNode::ConstVect();

	while (true)
	{
		buildLevel( src, dest);

		if (dest->size() == 1)
		{
			const IntervalRTreeNode * r = (*dest)[ 0 ];
			delete src;
			delete dest;
			//delete leaves; // don't need anymore
			return r;
		}

		IntervalRTreeNode::ConstVect * temp = src;
		src = dest;
		dest = temp;
	}
}

void
SortedPackedIntervalRTree::buildLevel( IntervalRTreeNode::ConstVect * src, IntervalRTreeNode::ConstVect * dest)
{
	level++;

	dest->clear();

	for (size_t i = 0, ni = src->size(); i < ni; i += 2)
	{
		const IntervalRTreeNode * n1 = (*src)[ i ];

		if ( i + 1 < ni )
		{
			const IntervalRTreeNode * n2 = (*src)[ i + 1 ];

			const IntervalRTreeNode * node = new IntervalRTreeBranchNode( n1, n2 );

			dest->push_back( node);
		}
		else
		{
			dest->push_back( n1);
		}
	}
}

//
// protected:
//

//
// public:
//
SortedPackedIntervalRTree::SortedPackedIntervalRTree()
	:
	leaves( new IntervalRTreeNode::ConstVect()),
	root( nullptr),
	level( 0)
{ }

SortedPackedIntervalRTree::~SortedPackedIntervalRTree()
{
	if ( root != nullptr )
	{
		// deleting root cascades to all IntervalRTreeNode's
		delete root;
	}
	else // possibly IntervalRTreeNode's in leaves to delete
	{
		for ( size_t i = 0, ni = leaves->size(); i < ni; i++ )
			delete (*leaves)[i];

		delete leaves;
	}
}


void
SortedPackedIntervalRTree::insert( double min, double max, void * item)
{
	if (root != nullptr)
		throw new util::UnsupportedOperationException( "Index cannot be added to once it has been queried");

	leaves->push_back( new IntervalRTreeLeafNode( min, max, item));
}

void
SortedPackedIntervalRTree::query( double min, double max, index::ItemVisitor * visitor)
{
	init();

	root->query( min, max, visitor);
}

} // geos::intervalrtree
} // geos::index
} // geos
