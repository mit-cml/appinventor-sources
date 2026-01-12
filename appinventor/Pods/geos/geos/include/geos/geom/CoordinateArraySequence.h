/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2006 Refractions Research Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************/

#ifndef GEOS_GEOM_COORDINATEARRAYSEQUENCE_H
#define GEOS_GEOM_COORDINATEARRAYSEQUENCE_H

#include <geos/export.h>
#include <vector>

#include <geos/geom/CoordinateSequence.h>

#include <geos/inline.h>

// Forward declarations
namespace geos {
	namespace geom {
		class Coordinate;
	}
}


namespace geos {
namespace geom { // geos.geom

/// The default implementation of CoordinateSequence
class GEOS_DLL CoordinateArraySequence : public CoordinateSequence {
public:

	CoordinateArraySequence(const CoordinateArraySequence &cl);

	CoordinateArraySequence(const CoordinateSequence &cl);

	CoordinateSequence *clone() const override;

	//const Coordinate& getCoordinate(int pos) const;
	const Coordinate& getAt(std::size_t pos) const override;

	/// Copy Coordinate at position i to Coordinate c
	void getAt(std::size_t i, Coordinate& c) const override;

	//int size() const;
	size_t getSize() const override;

	// @deprecated
	const std::vector<Coordinate>* toVector() const override;

	// See dox in CoordinateSequence.h
	void toVector(std::vector<Coordinate>&) const override;

	/// Construct an empty sequence
	CoordinateArraySequence();

	/// Construct sequence taking ownership of given Coordinate vector
	CoordinateArraySequence(std::vector<Coordinate> *coords,
                                std::size_t dimension = 0);

	/// Construct sequence allocating space for n coordinates
	CoordinateArraySequence(std::size_t n, std::size_t dimension = 0);

	~CoordinateArraySequence() override;

	bool isEmpty() const override { return empty(); }

	bool empty() const { return vect->empty(); }

	/// Reset this CoordinateArraySequence to the empty state
	void clear() { vect->clear(); }

	void add(const Coordinate& c) override;

	void add(const Coordinate& c, bool allowRepeated) override;

	/** \brief
	 * Inserts the specified coordinate at the specified position in
	 * this list.
	 *
	 * @param i the position at which to insert
	 * @param coord the coordinate to insert
	 * @param allowRepeated if set to false, repeated coordinates are
	 *                      collapsed
	 *
	 * NOTE: this is a CoordinateList interface in JTS
	 */
	void add(std::size_t i, const Coordinate& coord, bool allowRepeated) override;

	void setAt(const Coordinate& c, std::size_t pos) override;

	void deleteAt(std::size_t pos) override;

	std::string toString() const override;

	void setPoints(const std::vector<Coordinate> &v) override;

	double getOrdinate(std::size_t index,
			size_t ordinateIndex) const override;

	void setOrdinate(std::size_t index, std::size_t ordinateIndex,
			double value) override;

	void expandEnvelope(Envelope &env) const override;

    std::size_t getDimension() const override;

	void apply_rw(const CoordinateFilter *filter) override;

	void apply_ro(CoordinateFilter *filter) const override;

	CoordinateSequence& removeRepeatedPoints() override;

private:
	std::vector<Coordinate> *vect;
    mutable std::size_t dimension;
};

/// This is for backward API compatibility
typedef CoordinateArraySequence DefaultCoordinateSequence;

} // namespace geos.geom
} // namespace geos

#endif // ndef GEOS_GEOM_COORDINATEARRAYSEQUENCE_H
