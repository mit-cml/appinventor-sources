/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2005-2006 Refractions Research Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation. 
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: noding/snapround/HotPixel.java r320 (JTS-1.12)
 *
 **********************************************************************/

#ifndef GEOS_NODING_SNAPROUND_HOTPIXEL_INL
#define GEOS_NODING_SNAPROUND_HOTPIXEL_INL

#include <geos/noding/snapround/HotPixel.h>
#include <geos/util/math.h>
#include <geos/geom/Coordinate.h>

namespace geos {
namespace noding { // geos::noding
namespace snapround { // geos::noding::snapround

INLINE double
HotPixel::scale(double val) const
{
	// Math.round
	//return round(val*scaleFactor);
	return util::round(val*scaleFactor);
}

INLINE void
HotPixel::copyScaled(const geom::Coordinate& p, geom::Coordinate& pScaled) const
{
	pScaled.x = scale(p.x);
	pScaled.y = scale(p.y);
}

} // namespace geos::noding::snapround
} // namespace geos::noding
} // namespace geos

#endif // GEOS_NODING_SNAPROUND_HOTPIXEL_INL

