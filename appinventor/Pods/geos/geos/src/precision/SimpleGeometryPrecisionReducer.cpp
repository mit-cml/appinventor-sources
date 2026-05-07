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
 * Last port: precision/SimpleGeometryPrecisionReducer.cpp rev. 1.10 (JTS-1.7)
 *
 **********************************************************************/

#include <geos/precision/SimpleGeometryPrecisionReducer.h>
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

namespace {

class PrecisionReducerCoordinateOperation :
		public geom::util::CoordinateOperation
{
using CoordinateOperation::edit;
private:

	SimpleGeometryPrecisionReducer *sgpr;

public:

	PrecisionReducerCoordinateOperation(
		SimpleGeometryPrecisionReducer *newSgpr);

	/// Ownership of returned CoordinateSequence to caller
	CoordinateSequence* edit(const CoordinateSequence *coordinates,
	                         const Geometry *geom) override;
};

PrecisionReducerCoordinateOperation::PrecisionReducerCoordinateOperation(
		SimpleGeometryPrecisionReducer *newSgpr)
{
	sgpr=newSgpr;
}

CoordinateSequence*
PrecisionReducerCoordinateOperation::edit(const CoordinateSequence *cs,
                                          const Geometry *geom)
{
	if (cs->getSize()==0) return nullptr;

	unsigned int csSize=static_cast<unsigned int>(cs->getSize());

	vector<Coordinate> *vc = new vector<Coordinate>(csSize);

	// copy coordinates and reduce
	for (unsigned int i=0; i<csSize; ++i) {
		Coordinate coord=cs->getAt(i);
		sgpr->getPrecisionModel()->makePrecise(&coord);
		//reducedCoords->setAt(*coord,i);
		(*vc)[i] = coord;
	}

	// reducedCoords take ownership of 'vc'
	CoordinateSequence *reducedCoords =
		geom->getFactory()->getCoordinateSequenceFactory()->create(vc);

	// remove repeated points, to simplify returned geometry as
	// much as possible.
	//
	CoordinateSequence *noRepeatedCoords=CoordinateSequence::removeRepeatedPoints(reducedCoords);

	/**
	 * Check to see if the removal of repeated points
	 * collapsed the coordinate List to an invalid length
	 * for the type of the parent geometry.
	 * It is not necessary to check for Point collapses,
	 * since the coordinate list can
	 * never collapse to less than one point.
	 * If the length is invalid, return the full-length coordinate array
	 * first computed, or null if collapses are being removed.
	 * (This may create an invalid geometry - the client must handle this.)
	 */
	unsigned int minLength = 0;
	if (typeid(*geom)==typeid(LineString)) minLength = 2;
	if (typeid(*geom)==typeid(LinearRing)) minLength = 4;
	CoordinateSequence *collapsedCoords = reducedCoords;
	if (sgpr->getRemoveCollapsed())
	{
		delete reducedCoords; reducedCoords=nullptr;
		collapsedCoords=nullptr;
	}
	// return null or orginal length coordinate array
	if (noRepeatedCoords->getSize()<minLength) {
		delete noRepeatedCoords;
		return collapsedCoords;
	}
	// ok to return shorter coordinate array
	delete reducedCoords;
	return noRepeatedCoords;
}

} // anonymous namespace

//---------------------------------------------------------------


SimpleGeometryPrecisionReducer::SimpleGeometryPrecisionReducer(
		const PrecisionModel *pm)
	:
	newPrecisionModel(pm),
	removeCollapsed(true)
{
	//removeCollapsed = true;
	//changePrecisionModel = false;
	//newPrecisionModel = pm;
}

/**
 * Sets whether the reduction will result in collapsed components
 * being removed completely, or simply being collapsed to an (invalid)
 * Geometry of the same type.
 *
 * @param removeCollapsed if <code>true</code> collapsed components will be removed
 */
void
SimpleGeometryPrecisionReducer::setRemoveCollapsedComponents(bool nRemoveCollapsed)
{
	removeCollapsed=nRemoveCollapsed;
}

const PrecisionModel*
SimpleGeometryPrecisionReducer::getPrecisionModel()
{
	return newPrecisionModel;
}

bool
SimpleGeometryPrecisionReducer::getRemoveCollapsed()
{
	return removeCollapsed;
}

Geometry*
SimpleGeometryPrecisionReducer::reduce(const Geometry *geom)
{
	GeometryEditor geomEdit;
	PrecisionReducerCoordinateOperation prco(this);
	Geometry *g=geomEdit.edit(geom, &prco);
	return g;
}

} // namespace geos.precision
} // namespace geos

