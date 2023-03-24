/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2011 Sandro Santilli <strk@kbt.io>
 * Copyright (C) 2006 Refractions Research Inc.
 * Copyright (C) 2001-2002 Vivid Solutions Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: geom/util/GeometryEditor.java r320 (JTS-1.12)
 *
 **********************************************************************/

#include <geos/geom/util/GeometryEditor.h>
#include <geos/geom/GeometryFactory.h>
#include <geos/geom/Geometry.h>
#include <geos/geom/MultiPoint.h>
#include <geos/geom/MultiPolygon.h>
#include <geos/geom/MultiLineString.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/geom/Polygon.h>
#include <geos/geom/Point.h>
#include <geos/geom/LineString.h>
#include <geos/geom/LinearRing.h>
#include <geos/geom/GeometryCollection.h>
#include <geos/geom/util/GeometryEditorOperation.h>
#include <geos/util/UnsupportedOperationException.h>

#include <vector>
#include <cassert>
#include <typeinfo>

using namespace std;

namespace geos {
namespace geom { // geos.geom
namespace util { // geos.geom.util

/**
 * Creates a new GeometryEditor object which will create
 * an edited Geometry with the same GeometryFactory as the input Geometry.
 */
GeometryEditor::GeometryEditor(){
	factory=nullptr;
}

/**
 * Creates a new GeometryEditor object which will create
 * the edited Geometry with the given {@link GeometryFactory}
 *
 * @param factory the GeometryFactory to create the edited Geometry with
 */
GeometryEditor::GeometryEditor(const GeometryFactory *newFactory){
	factory=newFactory;
}

/**
 * Edit the input {@link Geometry} with the given edit operation.
 * Clients will create subclasses of GeometryEditorOperation or
 * CoordinateOperation to perform required modifications.
 *
 * @param geometry the Geometry to edit
 * @param operation the edit operation to carry out
 * @return a new {@link Geometry} which is the result of the editing
 */
Geometry*
GeometryEditor::edit(const Geometry *geometry, GeometryEditorOperation *operation)
{
	// if client did not supply a GeometryFactory, use the one from the input Geometry
	if (factory == nullptr)
		factory=geometry->getFactory();

  if ( const GeometryCollection *gc =
            dynamic_cast<const GeometryCollection*>(geometry) )
  {
		return editGeometryCollection(gc, operation);
  }

  if ( const Polygon *p = dynamic_cast<const Polygon*>(geometry) )
  {
		return editPolygon(p, operation);
  }

  if ( dynamic_cast<const Point*>(geometry) )
  {
		return operation->edit(geometry, factory);
  }

  if ( dynamic_cast<const LineString*>(geometry) )
  {
		return operation->edit(geometry, factory);
  }

    // Unsupported Geometry classes should be caught in the GeometryEditorOperation.
    assert(!static_cast<bool>("SHOULD NEVER GET HERE"));
    return nullptr;
}

Polygon*
GeometryEditor::editPolygon(const Polygon *polygon,GeometryEditorOperation *operation)
{
	Polygon* newPolygon= dynamic_cast<Polygon*>(
    operation->edit(polygon, factory)
  );
	if (newPolygon->isEmpty()) {
		//RemoveSelectedPlugIn relies on this behaviour. [Jon Aquino]
		if ( newPolygon->getFactory() != factory ) {
		  Polygon *ret = factory->createPolygon(nullptr, nullptr);
		  delete newPolygon;
		  return ret;
		} else {
		  return newPolygon;
		}
	}

	Geometry* editResult = edit(newPolygon->getExteriorRing(),operation);

	LinearRing* shell = dynamic_cast<LinearRing*>(editResult);
	if (shell->isEmpty()) {
		//RemoveSelectedPlugIn relies on this behaviour. [Jon Aquino]
		delete shell;
		delete newPolygon;
		return factory->createPolygon(nullptr,nullptr);
	}

	vector<Geometry*> *holes=new vector<Geometry*>;
	for (size_t i=0, n=newPolygon->getNumInteriorRing(); i<n; ++i)
	{

		Geometry *hole_geom = edit(newPolygon->getInteriorRingN(i),
			operation);

		LinearRing *hole = dynamic_cast<LinearRing*>(hole_geom);
		assert(hole);

		if (hole->isEmpty())
		{
			continue;
		}
		holes->push_back(hole);
	}
	delete newPolygon;
	return factory->createPolygon(shell,holes);
}

GeometryCollection*
GeometryEditor::editGeometryCollection(const GeometryCollection *collection, GeometryEditorOperation *operation)
{
	GeometryCollection *newCollection = dynamic_cast<GeometryCollection*>( operation->edit(collection,factory) );
	vector<Geometry*> *geometries = new vector<Geometry*>();
	for (std::size_t i=0, n=newCollection->getNumGeometries(); i<n; i++)
	{
		Geometry *geometry = edit(newCollection->getGeometryN(i),
			operation);
		if (geometry->isEmpty()) {
			delete geometry;
			continue;
		}
		geometries->push_back(geometry);
	}

	if (typeid(*newCollection)==typeid(MultiPoint)) {
		delete newCollection;
		return factory->createMultiPoint(geometries);
	}
	else if (typeid(*newCollection)==typeid(MultiLineString)) {
		delete newCollection;
		return factory->createMultiLineString(geometries);
	}
	else if (typeid(*newCollection)==typeid(MultiPolygon)) {
		delete newCollection;
		return factory->createMultiPolygon(geometries);
	}
	else {
		delete newCollection;
		return factory->createGeometryCollection(geometries);
	}
}

} // namespace geos.geom.util
} // namespace geos.geom
} // namespace geos
