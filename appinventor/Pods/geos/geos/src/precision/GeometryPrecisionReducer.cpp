/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2005-2006 Refractions Research Inc.
 * Copyright (C) 2001-2002 Vivid Solutions Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 ***********************************************************************
 *
 * Last port: precision/GeometryPrecisionReducer.cpp rev. 1.10 (JTS-1.7)
 *
 **********************************************************************/

#include <geos/precision/GeometryPrecisionReducer.h>
#include <geos/precision/PrecisionReducerCoordinateOperation.h>
#include <geos/geom/util/GeometryEditor.h>
#include <geos/geom/util/CoordinateOperation.h>
#include <geos/geom/Coordinate.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/geom/CoordinateSequenceFactory.h>
#include <geos/geom/PrecisionModel.h>
#include <geos/geom/GeometryFactory.h>
#include <geos/geom/LineString.h>
#include <geos/geom/LinearRing.h>

#include <vector>
#include <typeinfo>

using namespace std;
using namespace geos::geom;
using namespace geos::geom::util;

namespace geos {
namespace precision { // geos.precision


/* private */
unique_ptr<Geometry>
GeometryPrecisionReducer::reducePointwise(const Geometry &geom)
{
	unique_ptr<GeometryEditor> geomEdit;

  if ( newFactory ) {
      geomEdit.reset( new GeometryEditor(newFactory) );
  } else {
      geomEdit.reset( new GeometryEditor() );
  }

  /**
   * For polygonal geometries, collapses are always removed, in order
   * to produce correct topology
   */
  bool finalRemoveCollapsed = removeCollapsed;
  if (geom.getDimension() >= 2)
    finalRemoveCollapsed = true;

	PrecisionReducerCoordinateOperation prco(targetPM, finalRemoveCollapsed);

	std::unique_ptr<Geometry> g ( geomEdit->edit(&geom, &prco) );

	return g;
}

/* public */
unique_ptr<Geometry>
GeometryPrecisionReducer::reduce(const Geometry &geom)
{
  unique_ptr<Geometry> reducePW = reducePointwise(geom);

  if ( isPointwise ) return reducePW;

  //TODO: handle GeometryCollections containing polys
  if (! (dynamic_cast<const Polygonal*>(reducePW.get())) )
    return reducePW;

  // Geometry is polygonal - test if topology needs to be fixed
  if (reducePW->isValid()) return reducePW;

  // hack to fix topology.
  // TODO: implement snap-rounding and use that.
  return fixPolygonalTopology(*reducePW);

}


/* public */
GeometryPrecisionReducer::GeometryPrecisionReducer(const GeometryFactory &changeFactory)
      :
      newFactory(&changeFactory),
      targetPM(*(changeFactory.getPrecisionModel())),
      removeCollapsed(true),
      isPointwise(false)
{}

/* private */
unique_ptr<Geometry>
GeometryPrecisionReducer::fixPolygonalTopology(const geom::Geometry& geom )
{
  /**
   * If precision model was *not* changed, need to flip
   * geometry to targetPM, buffer in that model, then flip back
   */
  unique_ptr<Geometry> tmp;
  GeometryFactory::Ptr tmpFactory;

  const Geometry* geomToBuffer = &geom;

  if ( ! newFactory ) {
    tmpFactory = createFactory(*geom.getFactory(), targetPM);
    tmp.reset( tmpFactory->createGeometry(&geom) );
    geomToBuffer = tmp.get();
  }

  unique_ptr<Geometry> bufGeom ( geomToBuffer->buffer(0) );

  if ( ! newFactory ) {
    // a slick way to copy the geometry with the original precision factory
    bufGeom.reset( geom.getFactory()->createGeometry(bufGeom.get()) );
  }

  return bufGeom;
}

/* private */
GeometryFactory::Ptr
GeometryPrecisionReducer::createFactory( const GeometryFactory& oldGF,
                                         const PrecisionModel& newPM )
{
  GeometryFactory::Ptr newFactory(
    GeometryFactory::create(&newPM,
                        oldGF.getSRID(),
                        const_cast<CoordinateSequenceFactory*>(oldGF.getCoordinateSequenceFactory()))
  );
  return newFactory;
}

} // namespace geos.precision
} // namespace geos

