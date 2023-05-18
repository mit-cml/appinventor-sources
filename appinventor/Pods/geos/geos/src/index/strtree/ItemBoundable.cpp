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

#include <geos/index/strtree/ItemBoundable.h>

namespace geos {
namespace index { // geos.index
namespace strtree { // geos.index.strtree

ItemBoundable::ItemBoundable(const void* newBounds, void* newItem) :
    bounds(newBounds), item(newItem)
{
}

const void*
ItemBoundable::getBounds() const {
	return bounds;
}

void* ItemBoundable::getItem() const {
	return item;
}

} // namespace geos.index.strtree
} // namespace geos.index
} // namespace geos

