/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2011 Sandro Santilli <strk@kbt.io>
 * Copyright (C) 2005 2006 Refractions Research Inc.
 * Copyright (C) 2001-2002 Vivid Solutions Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: geom/Polygon.java r320 (JTS-1.12)
 *
 **********************************************************************/

#ifndef GEOS_GEOM_POLYGON_H
#define GEOS_GEOM_POLYGON_H

#include <geos/export.h>
#include <string>
#include <vector>
#include <geos/platform.h>
#include <geos/geom/Geometry.h> // for inheritance
#include <geos/geom/Polygonal.h> // for inheritance
#include <geos/geom/Envelope.h> // for proper use of unique_ptr<>
#include <geos/geom/Dimension.h> // for Dimension::DimensionType

#include <geos/inline.h>

#include <memory> // for unique_ptr

// Forward declarations
namespace geos {
	namespace geom { // geos::geom
		class Coordinate;
		class CoordinateArraySequence;
		class CoordinateSequenceFilter;
		class LinearRing;
		class LineString;
	}
}

namespace geos {
namespace geom { // geos::geom

/**
 * \class Polygon geom.h geos.h
 *
 * \brief Represents a linear polygon, which may include holes.
 *
 * The shell and holes of the polygon are represented by {@link LinearRing}s.
 * In a valid polygon, holes may touch the shell or other holes at a single point.
 * However, no sequence of touching holes may split the polygon into two pieces.
 * The orientation of the rings in the polygon does not matter.
 * <p>
 *  The shell and holes must conform to the assertions specified in the <A
 *  HREF="http://www.opengis.org/techno/specs.htm">OpenGIS Simple Features
 *  Specification for SQL</A> .
 *
 */
class GEOS_DLL Polygon: public virtual Geometry, public Polygonal
{

public:

	friend class GeometryFactory;

	/// A vector of const Polygon pointers
	typedef std::vector<const Polygon *> ConstVect;

	~Polygon() override;

	/**
	 * Creates and returns a full copy of this {@link Polygon} object.
	 * (including all coordinates contained by it).
	 *
	 * @return a clone of this instance
	 */
	Geometry *clone() const override { return new Polygon(*this); }

	CoordinateSequence* getCoordinates() const override;

	size_t getNumPoints() const override;

	/// Returns surface dimension (2)
	Dimension::DimensionType getDimension() const override;

	/// Returns coordinate dimension.
	int getCoordinateDimension() const override;

	/// Returns 1 (Polygon boundary is a MultiLineString)
	int getBoundaryDimension() const override;

	/** \brief
	 * Computes the boundary of this geometry
	 *
	 * @return a lineal geometry (which may be empty)
	 * @see Geometry#getBoundary
	 */
	Geometry* getBoundary() const override;

	bool isEmpty() const override;

	/** \brief
	 * Tests if a valid polygon is simple.
	 * This method always returns true, since a valid polygon is always simple
	 *
	 * @return <code>true</code>
	 */
	bool isSimple() const override;

	/// Returns the exterior ring (shell)
	const LineString* getExteriorRing() const;

	/// Returns number of interior rings (hole)
	size_t getNumInteriorRing() const;

	/// Get nth interior ring (hole)
	const LineString* getInteriorRingN(std::size_t n) const;

	std::string getGeometryType() const override;
	GeometryTypeId getGeometryTypeId() const override;
	bool equalsExact(const Geometry *other, double tolerance=0) const override;
	void apply_rw(const CoordinateFilter *filter) override;
	void apply_ro(CoordinateFilter *filter) const override;
	void apply_rw(GeometryFilter *filter) override;
	void apply_ro(GeometryFilter *filter) const override;
	void apply_rw(CoordinateSequenceFilter& filter) override;
	void apply_ro(CoordinateSequenceFilter& filter) const override;

	Geometry* convexHull() const override;

	void normalize() override;

	Geometry* reverse() const override;

	int compareToSameClass(const Geometry *p) const override; //was protected

	const Coordinate* getCoordinate() const override;

	double getArea() const override;

 	/// Returns the perimeter of this <code>Polygon</code>
	double getLength() const override;

	void apply_rw(GeometryComponentFilter *filter) override;

	void apply_ro(GeometryComponentFilter *filter) const override;

	bool isRectangle() const override;

protected:


	Polygon(const Polygon &p);

	/**
	 * Constructs a <code>Polygon</code> with the given exterior
	 * and interior boundaries.
	 *
	 * @param  newShell  the outer boundary of the new Polygon,
	 *                   or <code>null</code> or an empty
	 *		     LinearRing if the empty geometry
	 *                   is to be created.
	 *
	 * @param  newHoles  the LinearRings defining the inner
	 *                   boundaries of the new Polygon, or
	 *                   null or empty LinearRing
	 *                   if the empty  geometry is to be created.
	 *
	 * @param newFactory the GeometryFactory used to create this geometry
	 *
	 * Polygon will take ownership of Shell and Holes LinearRings
	 */
	Polygon(LinearRing *newShell, std::vector<Geometry *> *newHoles,
		const GeometryFactory *newFactory);

	LinearRing *shell;

	std::vector<Geometry *> *holes; //Actually vector<LinearRing *>

	Envelope::Ptr computeEnvelopeInternal() const override;

private:

	void normalize(LinearRing *ring, bool clockwise);
};

} // namespace geos::geom
} // namespace geos

#endif // ndef GEOS_GEOM_POLYGON_H
