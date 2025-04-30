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
 * Last port: geom/prep/PreparedLineString.java rev 1.3 (JTS-1.10)
 *
 **********************************************************************/


#include <geos/geom/prep/PreparedLineString.h>
#include <geos/geom/prep/PreparedLineStringIntersects.h>
#include <geos/noding/SegmentStringUtil.h>
#include <geos/noding/FastSegmentSetIntersectionFinder.h>

namespace geos {
namespace geom { // geos.geom
namespace prep { // geos.geom.prep

/*
 * public:
 */

PreparedLineString::~PreparedLineString()
{
	delete segIntFinder;
	for ( noding::SegmentString::ConstVect::size_type i = 0,
	     ni = segStrings.size(); i < ni; ++i )
	{
		delete segStrings[ i ];
	}
}

noding::FastSegmentSetIntersectionFinder *
PreparedLineString::getIntersectionFinder()
{
	if (! segIntFinder)
	{
		noding::SegmentStringUtil::extractSegmentStrings( &getGeometry(), segStrings );
		segIntFinder = new noding::FastSegmentSetIntersectionFinder( &segStrings );
	}

	return segIntFinder;
}

bool
PreparedLineString::intersects(const geom::Geometry * g) const
{
	if (! envelopesIntersect(g))
    {
        return false;
    }

    PreparedLineString& prep = *(const_cast<PreparedLineString*>(this));

    return PreparedLineStringIntersects::intersects(prep, g);
}

} // namespace geos.geom.prep
} // namespace geos.geom
} // namespace geos
