/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2013 Sandro Santilli <strk@kbt.io>
 * Copyright (C) 2005-2006 Refractions Research Inc.
 * Copyright (C) 2001-2002 Vivid Solutions Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: algorithm/InteriorPointArea.java r728 (JTS-1.13+)
 *
 **********************************************************************/

#include <geos/algorithm/InteriorPointArea.h>
#include <geos/geom/Coordinate.h>
#include <geos/geom/Geometry.h>
#include <geos/geom/GeometryCollection.h>
#include <geos/geom/Polygon.h>
#include <geos/geom/LineString.h>
#include <geos/geom/Envelope.h>
#include <geos/geom/GeometryFactory.h>
#include <geos/geom/CoordinateSequenceFactory.h>

#include <vector>
#include <typeinfo>
#include <memory> // for unique_ptr

using namespace std;
using namespace geos::geom;

namespace geos {
namespace algorithm { // geos.algorithm

// file-statics
namespace {

  double avg(double a, double b){return (a+b)/2.0;}

  /**
   * Finds a safe bisector Y ordinate
   * by projecting to the Y axis
   * and finding the Y-ordinate interval
   * which contains the centre of the Y extent.
   * The centre of this interval is returned as the bisector Y-ordinate.
   *
   * @author mdavis
   *
   */
  class SafeBisectorFinder
  {
  public:
	  static double getBisectorY(const Polygon& poly)
	  {
		  SafeBisectorFinder finder(poly);
		  return finder.getBisectorY();
	  }
	  SafeBisectorFinder(const Polygon& nPoly)
      : poly(nPoly)
    {
		  // initialize using extremal values
		  hiY = poly.getEnvelopeInternal()->getMaxY();
		  loY = poly.getEnvelopeInternal()->getMinY();
		  centreY = avg(loY, hiY);
	  }

	  double getBisectorY()
	  {
		  process(*poly.getExteriorRing());
		  for (size_t i = 0; i < poly.getNumInteriorRing(); i++) {
			  process(*poly.getInteriorRingN(i));
		  }
		  double bisectY = avg(hiY, loY);
		  return bisectY;
	  }


	private:
	  const Polygon& poly;

	  double centreY;
	  double hiY;
	  double loY;

	  void process(const LineString& line) {
      const CoordinateSequence* seq = line.getCoordinatesRO();
      for (std::size_t i = 0, s = seq->size(); i < s; i++) {
        double y = seq->getY(i);
        updateInterval(y);
      }
    }

    void updateInterval(double y) {
      if (y <= centreY) {
        if (y > loY)
          loY = y;
      }
      else if (y > centreY) {
        if (y < hiY) {
          hiY = y;
        }
      }
    }

    SafeBisectorFinder(SafeBisectorFinder const&); /*= delete*/
    SafeBisectorFinder& operator=(SafeBisectorFinder const&); /*= delete*/
  };

} // anonymous namespace


/*public*/
InteriorPointArea::InteriorPointArea(const Geometry *g)
{
	foundInterior=false;
	maxWidth=0.0;
	factory=g->getFactory();
	add(g);
}

/*public*/
InteriorPointArea::~InteriorPointArea()
{
}

/*public*/
bool
InteriorPointArea::getInteriorPoint(Coordinate& ret) const
{
	if ( ! foundInterior ) return false;

	ret=interiorPoint;
	return true;
}

/*public*/
void
InteriorPointArea::add(const Geometry *geom)
{
	const Polygon *poly = dynamic_cast<const Polygon*>(geom);
	if ( poly ) {
		addPolygon(geom);
		return;
	}

	const GeometryCollection *gc = dynamic_cast<const GeometryCollection*>(geom);
	if ( gc )
	{
        for(std::size_t i=0, n=gc->getNumGeometries(); i<n; i++) {
			add(gc->getGeometryN(i));
		}
	}
}

/*private*/
void
InteriorPointArea::addPolygon(const Geometry *geometry)
{
  if (geometry->isEmpty()) return;

  Coordinate intPt;
  double width;

  unique_ptr<LineString> bisector ( horizontalBisector(geometry) );
  if ( bisector->getLength() == 0.0 ) {
    width = 0;
    intPt = bisector->getCoordinateN(0);
  }
  else {
    unique_ptr<Geometry> intersections ( bisector->intersection(geometry) );
    const Geometry *widestIntersection = widestGeometry(intersections.get());
    const Envelope *env = widestIntersection->getEnvelopeInternal();
    width=env->getWidth();
    env->centre(intPt);
  }
  if (!foundInterior || width>maxWidth) {
    interiorPoint = intPt;
    maxWidth = width;
    foundInterior=true;
  }
}

//@return if geometry is a collection, the widest sub-geometry; otherwise,
//the geometry itself
const Geometry*
InteriorPointArea::widestGeometry(const Geometry *geometry)
{
	const GeometryCollection *gc = dynamic_cast<const GeometryCollection*>(geometry);
	if ( gc ) {
		return widestGeometry(gc);
	} else {
		return geometry;
	}
}

const Geometry*
InteriorPointArea::widestGeometry(const GeometryCollection* gc) {
	if (gc->isEmpty()) {
		return gc;
	}
	const Geometry* widestGeometry=gc->getGeometryN(0);

	// scan remaining geom components to see if any are wider
	for(std::size_t i=1, n=gc->getNumGeometries(); i<n; i++) // start at 1
	{
		const Envelope *env1(gc->getGeometryN(i)->getEnvelopeInternal());
		const Envelope *env2(widestGeometry->getEnvelopeInternal());
		if (env1->getWidth()>env2->getWidth()) {
				widestGeometry=gc->getGeometryN(i);
		}
	}
	return widestGeometry;
}

/* private */
LineString*
InteriorPointArea::horizontalBisector(const Geometry *geometry)
{
	const Envelope *envelope=geometry->getEnvelopeInternal();

	/**
	 * Original algorithm.  Fails when geometry contains a horizontal
	 * segment at the Y midpoint.
	 */
	// Assert: for areas, minx <> maxx
	//double avgY=avg(envelope->getMinY(),envelope->getMaxY());

	double bisectY = SafeBisectorFinder::getBisectorY(*dynamic_cast<const Polygon *>(geometry));
	vector<Coordinate>*cv=new vector<Coordinate>(2);
	(*cv)[0].x = envelope->getMinX();
	(*cv)[0].y = bisectY;
	(*cv)[1].x = envelope->getMaxX();
	(*cv)[1].y = bisectY;

	CoordinateSequence *cl = factory->getCoordinateSequenceFactory()->create(cv);

	LineString *ret = factory->createLineString(cl);
	return ret;
}

} // namespace geos.algorithm
} // namespace geos
