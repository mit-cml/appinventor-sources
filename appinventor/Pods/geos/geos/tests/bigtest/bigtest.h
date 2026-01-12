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
 **********************************************************************/


#ifndef GEOS_BIGTEST_H
#define GEOS_BIGTEST_H

#include <memory>

using namespace std;
using namespace geos;

// Forward declaration
namespace geos {
	namespace geom {
		class Polygon;
		class CoordinateSequence;
		class GeometryFactory;
	}
}

class GeometryTestFactory {
public:
	static geom::Polygon* createBox(geom::GeometryFactory *fact,double minx,double miny,int nSide,double segLen);
	static geom::CoordinateSequence* createBox(double minx,double miny,int nSide,double segLen);
	static geom::CoordinateSequence* createCircle(double basex,double basey,double size,int nPts);
	static geom::Polygon* createCircle(geom::GeometryFactory *fact,double basex,double basey,double size,int nPts);
	static geom::CoordinateSequence* createSineStar(double basex,double basey,double size,double armLen,int nArms,int nPts);
	static geom::Polygon* createSineStar(geom::GeometryFactory *fact,double basex,double basey,double size,double armLen,int nArms,int nPts);
};

#endif
