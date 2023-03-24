/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2011 Sandro Santilli <strk@kbt.io
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: geom/util/SineStarFactory.java r378 (JTS-1.12)
 *
 **********************************************************************/

#include <geos/geom/util/SineStarFactory.h>
#include <geos/geom/Coordinate.h>
#include <geos/geom/CoordinateSequenceFactory.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/geom/GeometryFactory.h>
#include <geos/geom/Envelope.h>
#include <geos/geom/Polygon.h>
#include <geos/geom/LinearRing.h>

#include <vector>
#include <cmath>
#include <memory>

#ifndef M_PI
#define M_PI        3.14159265358979323846
#endif

using namespace std;
//using namespace geos::geom;

namespace geos {
namespace geom { // geos::geom
namespace util { // geos::geom::util

/* public */
unique_ptr<Polygon>
SineStarFactory::createSineStar() const
{
  unique_ptr<Envelope> env ( dim.getEnvelope() );
  double radius = env->getWidth() / 2.0;

  double armRatio = armLengthRatio;
  if (armRatio < 0.0) armRatio = 0.0;
  if (armRatio > 1.0) armRatio = 1.0;

  double armMaxLen = armRatio * radius;
  double insideRadius = (1 - armRatio) * radius;

  double centreX = env->getMinX() + radius;
  double centreY = env->getMinY() + radius;

  unique_ptr< vector<Coordinate> > pts ( new vector<Coordinate>(nPts+1) );
  int iPt = 0;
  for (int i = 0; i < nPts; i++) {
    // the fraction of the way thru the current arm - in [0,1]
    double ptArcFrac = (i / (double) nPts) * numArms;
    double armAngFrac = ptArcFrac - floor(ptArcFrac);

    // the angle for the current arm - in [0,2Pi]
    // (each arm is a complete sine wave cycle)
    double armAng = 2 * M_PI * armAngFrac;
    // the current length of the arm
    double armLenFrac = (cos(armAng) + 1.0) / 2.0;

    // the current radius of the curve (core + arm)
    double curveRadius = insideRadius + armMaxLen * armLenFrac;

    // the current angle of the curve
    double ang = i * (2 * M_PI / nPts);
    double x = curveRadius * cos(ang) + centreX;
    double y = curveRadius * sin(ang) + centreY;
    (*pts)[iPt++] = coord(x, y);
  }
  (*pts)[iPt] = Coordinate((*pts)[0]);

  unique_ptr<CoordinateSequence> cs (
    geomFact->getCoordinateSequenceFactory()->create( pts.release() )
  );
  unique_ptr<LinearRing> ring ( geomFact->createLinearRing( cs.release() ) );
  unique_ptr<Polygon> poly ( geomFact->createPolygon(ring.release(), nullptr) );
  return poly;
}

} // namespace geos::geom::util
} // namespace geos::geom
} // namespace geos

