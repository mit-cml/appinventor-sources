/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2012  Sandro Santilli <strk@kbt.io>
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * NOTE: this is not in JTS. JTS has a snapround/GeometryNoder though
 *
 **********************************************************************/

#include <geos/noding/GeometryNoder.h>
#include <geos/noding/SegmentString.h>
#include <geos/noding/NodedSegmentString.h>
#include <geos/noding/OrientedCoordinateArray.h>
#include <geos/noding/Noder.h>
#include <geos/geom/Geometry.h>
#include <geos/geom/PrecisionModel.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/geom/GeometryFactory.h>
#include <geos/geom/LineString.h>

#include <geos/noding/IteratedNoder.h>

#include <geos/algorithm/LineIntersector.h>
#include <geos/noding/IntersectionAdder.h>
#include <geos/noding/MCIndexNoder.h>

#include <geos/noding/snapround/SimpleSnapRounder.h>
#include <geos/noding/snapround/MCIndexSnapRounder.h>

#include <memory> // for unique_ptr
#include <iostream>

namespace geos {
namespace noding { // geos.noding

namespace {

/**
 * Add every linear element in a geometry into SegmentString vector
 */
class SegmentStringExtractor: public geom::GeometryComponentFilter {
public:
  SegmentStringExtractor(SegmentString::NonConstVect& to)
    : _to(to)
  {}

  void filter_ro(const geom::Geometry * g) override {
    const geom::LineString *ls = dynamic_cast<const geom::LineString *>(g);
    if ( ls ) {
      geom::CoordinateSequence* coord = ls->getCoordinates();
      // coord ownership transferred to SegmentString
      SegmentString *ss = new NodedSegmentString(coord, nullptr);
      _to.push_back(ss);
    }
  }
private:
  SegmentString::NonConstVect& _to;

  SegmentStringExtractor(SegmentStringExtractor const&); /*= delete*/
  SegmentStringExtractor& operator=(SegmentStringExtractor const&); /*= delete*/
};

}


/* public static */
std::unique_ptr<geom::Geometry>
GeometryNoder::node(const geom::Geometry& geom)
{
  GeometryNoder noder(geom);
  return noder.getNoded();
}

/* public */
GeometryNoder::GeometryNoder(const geom::Geometry& g)
  :
  argGeom(g)
{
}

/* private */
std::unique_ptr<geom::Geometry>
GeometryNoder::toGeometry(SegmentString::NonConstVect& nodedEdges)
{
  const geom::GeometryFactory *geomFact = argGeom.getFactory();

  std::set< OrientedCoordinateArray > ocas;

  // Create a geometry out of the noded substrings.
  std::vector< geom::Geometry* >* lines = new std::vector< geom::Geometry * >();
  lines->reserve(nodedEdges.size());
  for (auto &ss :  nodedEdges)
  {
    const geom::CoordinateSequence* coords = ss->getCoordinates();

    // Check if an equivalent edge is known
    OrientedCoordinateArray oca1( *coords );
    if ( ocas.insert(oca1).second ) {
      geom::Geometry* tmp = geomFact->createLineString( coords->clone() );
      lines->push_back( tmp );
    }
  }

  std::unique_ptr<geom::Geometry> noded ( geomFact->createMultiLineString( lines ) );

  return noded;
}

/* public */
std::unique_ptr<geom::Geometry>
GeometryNoder::getNoded()
{
  SegmentString::NonConstVect lineList;
  extractSegmentStrings(argGeom, lineList);

  Noder& noder = getNoder();
  SegmentString::NonConstVect* nodedEdges = nullptr;

  try {
    noder.computeNodes( &lineList );
    nodedEdges = noder.getNodedSubstrings();
  }
  catch (const std::exception& ex)
  {
    for (size_t i=0, n=lineList.size(); i<n; ++i)
      delete lineList[i];
    throw;
  }

  std::unique_ptr<geom::Geometry> noded = toGeometry(*nodedEdges);

  for (auto &elem : (*nodedEdges))
    delete elem;
  delete nodedEdges;

  for (auto &elem : lineList)
    delete elem;

  return noded;
}

/* private static */
void
GeometryNoder::extractSegmentStrings(const geom::Geometry& g,
                                     SegmentString::NonConstVect& to)
{
  SegmentStringExtractor ex(to);
	g.apply_ro(&ex);
}

/* private */
Noder&
GeometryNoder::getNoder()
{
  if ( ! noder.get() )
  {
    const geom::PrecisionModel *pm = argGeom.getFactory()->getPrecisionModel();
#if 0
    using algorithm::LineIntersector;
		LineIntersector li;
		IntersectionAdder intersectionAdder(li);
    noder.reset( new MCIndexNoder(&intersectionAdder) );
#else

    IteratedNoder* in = new IteratedNoder(pm);
    //in->setMaximumIterations(0);
    noder.reset( in );

    //using snapround::SimpleSnapRounder;
    //noder.reset( new SimpleSnapRounder(*pm) );

    //using snapround::MCIndexSnapRounder;
    //noder.reset( new MCIndexSnapRounder(*pm) );
#endif
  }
  return *noder;


}

} // namespace geos.noding
} // namespace geos
