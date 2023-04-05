/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2006 Refractions Research Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: simplify/TopologyPreservingSimplifier.java r536 (JTS-1.12+)
 *
 **********************************************************************/

#include <geos/simplify/TopologyPreservingSimplifier.h>
#include <geos/simplify/TaggedLinesSimplifier.h>
#include <geos/simplify/LineSegmentIndex.h> // for unique_ptr dtor
#include <geos/simplify/TaggedLineString.h>
#include <geos/simplify/TaggedLineStringSimplifier.h> // for unique_ptr dtor
#include <geos/algorithm/LineIntersector.h> // for unique_ptr dtor
// for LineStringTransformer inheritance
#include <geos/geom/util/GeometryTransformer.h>
// for LineStringMapBuilderFilter inheritance
#include <geos/geom/GeometryComponentFilter.h>
#include <geos/geom/Geometry.h> // for unique_ptr dtor
#include <geos/geom/LineString.h>
#include <geos/geom/LinearRing.h>
#include <geos/util/IllegalArgumentException.h>

#include <memory> // for unique_ptr
#include <map>
#include <cassert>
#include <iostream>

#ifndef GEOS_DEBUG
#define GEOS_DEBUG 0
#endif

#ifdef GEOS_DEBUG
#include <iostream>
#endif

using namespace geos::geom;

namespace geos {
namespace simplify { // geos::simplify

typedef std::map<const geom::Geometry*, TaggedLineString* > LinesMap;


namespace { // module-statics

class LineStringTransformer: public geom::util::GeometryTransformer
{

public:

	/**
	 * User's constructor.
	 * @param nMap - reference to LinesMap instance.
	 */
	LineStringTransformer(LinesMap& simp);

protected:

	CoordinateSequence::Ptr transformCoordinates(
			const CoordinateSequence* coords,
			const Geometry* parent) override;

private:

	LinesMap& linestringMap;

};

/*
 * helper class to transform a map iterator so to return value_type
 * on dereference.
 * TODO: generalize this to be a "ValueIterator" with specializations
 *       for std::map and std::vector
 */
class LinesMapValueIterator {

	LinesMap::iterator _iter;

public:

	LinesMapValueIterator(LinesMap::iterator iter)
		:
		_iter(iter)
	{
	}

	// copy ctor
	LinesMapValueIterator(const LinesMapValueIterator& o)
		:
		_iter(o._iter)
	{
	}

	// assignment
	LinesMapValueIterator& operator=(const LinesMapValueIterator& o)
	{
		_iter=o._iter;
		return *this;
	}

	// postfix++
	void operator++(int)
	{
		_iter++;
	}

	// ++suffix
	void operator++()
	{
		++_iter;
	}

	// inequality operator
	bool operator!=(const LinesMapValueIterator& other) const
	{
		return _iter != other._iter;
	}

	TaggedLineString* operator*()
	{
		return _iter->second;
	}
};


/*public*/
LineStringTransformer::LineStringTransformer(LinesMap& nMap)
	:
	linestringMap(nMap)
{
}

/*protected*/
CoordinateSequence::Ptr
LineStringTransformer::transformCoordinates(
		const CoordinateSequence* coords,
		const Geometry* parent)
{
#if GEOS_DEBUG
	std::cerr << __FUNCTION__ << ": parent: " << parent
	          << std::endl;
#endif
	if ( dynamic_cast<const LineString*>(parent) )
	{
		LinesMap::iterator it = linestringMap.find(parent);
		assert( it != linestringMap.end() );

		TaggedLineString* taggedLine = it->second;
#if GEOS_DEBUG
		std::cerr << "LineStringTransformer[" << this << "] "
		     << " getting result Coordinates from "
		     << " TaggedLineString[" << taggedLine << "]"
		     << std::endl;
#endif

		assert(taggedLine);
		assert(taggedLine->getParent() == parent);

		return taggedLine->getResultCoordinates();
	}

	// for anything else (e.g. points) just copy the coordinates
	return GeometryTransformer::transformCoordinates(coords, parent);
}

//----------------------------------------------------------------------

/*
 * A filter to add linear geometries to the linestring map
 * with the appropriate minimum size constraint.
 * Closed {@link LineString}s (including {@link LinearRing}s
 * have a minimum output size constraint of 4,
 * to ensure the output is valid.
 * For all other linestrings, the minimum size is 2 points.
 *
 * This class populates the given LineString=>TaggedLineString map
 * with newly created TaggedLineString objects.
 * Users must take care of deleting the map's values (elem.second).
 * TODO: Consider container of unique_ptr
 *
 */
class LineStringMapBuilderFilter: public geom::GeometryComponentFilter
{

public:

	// no more needed
	//friend class TopologyPreservingSimplifier;

	/**
	 * Filters linear geometries.
	 *
	 * geom a geometry of any type
	 */
	void filter_ro(const Geometry* geom) override;


	/**
	 * User's constructor.
	 * @param nMap - reference to LinesMap instance.
	 */
	LineStringMapBuilderFilter(LinesMap& nMap);

private:

	LinesMap& linestringMap;

    // Declare type as noncopyable
    LineStringMapBuilderFilter(const LineStringMapBuilderFilter& other) = delete;
    LineStringMapBuilderFilter& operator=(const LineStringMapBuilderFilter& rhs) = delete;
};

/*public*/
LineStringMapBuilderFilter::LineStringMapBuilderFilter(LinesMap& nMap)
	:
	linestringMap(nMap)
{
}

/*public*/
void
LineStringMapBuilderFilter::filter_ro(const Geometry* geom)
{
	TaggedLineString* taggedLine;

	if ( const LineString* ls =
			dynamic_cast<const LineString*>(geom) )
	{
    int minSize = ls->isClosed() ? 4 : 2;
		taggedLine = new TaggedLineString(ls, minSize);
	}
	else
	{
		return;
	}

	// Duplicated Geometry pointers shouldn't happen
	if ( ! linestringMap.insert(std::make_pair(geom, taggedLine)).second )
	{
		std::cerr << __FILE__ << ":" << __LINE__
		     << "Duplicated Geometry components detected"
		     << std::endl;

		delete taggedLine;
	}
}


} // end of module-statics

/*public static*/
std::unique_ptr<geom::Geometry>
TopologyPreservingSimplifier::simplify(
		const geom::Geometry* geom,
		double tolerance)
{
	TopologyPreservingSimplifier tss(geom);
        tss.setDistanceTolerance(tolerance);
	return tss.getResultGeometry();
}

/*public*/
TopologyPreservingSimplifier::TopologyPreservingSimplifier(const Geometry* geom)
	:
	inputGeom(geom),
	lineSimplifier(new TaggedLinesSimplifier())
{
}

/*public*/
void
TopologyPreservingSimplifier::setDistanceTolerance(double d)
{
	using geos::util::IllegalArgumentException;

	if ( d < 0.0 )
		throw IllegalArgumentException("Tolerance must be non-negative");

	lineSimplifier->setDistanceTolerance(d);
}


/*public*/
std::unique_ptr<geom::Geometry>
TopologyPreservingSimplifier::getResultGeometry()
{

	// empty input produces an empty result
	if (inputGeom->isEmpty()) return std::unique_ptr<Geometry>(inputGeom->clone());

	LinesMap linestringMap;

	std::unique_ptr<geom::Geometry> result;

	try {
		LineStringMapBuilderFilter lsmbf(linestringMap);
		inputGeom->apply_ro(&lsmbf);

#if GEOS_DEBUG
	std::cerr << "LineStringMapBuilderFilter applied, "
	          << " lineStringMap contains "
	          << linestringMap.size() << " elements\n";
#endif

		LinesMapValueIterator begin(linestringMap.begin());
		LinesMapValueIterator end(linestringMap.end());
		lineSimplifier->simplify(begin, end);


#if GEOS_DEBUG
	std::cerr << "all TaggedLineString simplified\n";
#endif

		LineStringTransformer trans(linestringMap);
		result = trans.transform(inputGeom);

#if GEOS_DEBUG
	std::cerr << "inputGeom transformed\n";
#endif

	} catch (...) {
		for (LinesMap::iterator
				it = linestringMap.begin(),
				itEnd = linestringMap.end();
				it != itEnd;
				++it)
		{
			delete it->second;
		}

		throw;
	}

	for (LinesMap::iterator
			it = linestringMap.begin(),
			itEnd = linestringMap.end();
			it != itEnd;
			++it)
	{
		delete it->second;
	}

#if GEOS_DEBUG
	std::cerr << "returning result\n";
#endif

	return result;
}

} // namespace geos::simplify
} // namespace geos

