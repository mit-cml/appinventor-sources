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
 **********************************************************************/


#include <geos/algorithm/locate/IndexedPointInAreaLocator.h>
#include <geos/geom/Geometry.h>
#include <geos/geom/Polygon.h>
#include <geos/geom/MultiPolygon.h>
#include <geos/geom/LineString.h>
#include <geos/geom/LineSegment.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/geom/util/LinearComponentExtracter.h>
#include <geos/index/intervalrtree/SortedPackedIntervalRTree.h>
#include <geos/util/IllegalArgumentException.h>
#include <geos/algorithm/RayCrossingCounter.h>
#include <geos/index/ItemVisitor.h>

#include <algorithm>
#include <typeinfo>

namespace geos {
namespace algorithm {
namespace locate {
//
// private:
//
IndexedPointInAreaLocator::IntervalIndexedGeometry::IntervalIndexedGeometry( const geom::Geometry & g)
{
	index = new index::intervalrtree::SortedPackedIntervalRTree();
	init( g);
}

IndexedPointInAreaLocator::IntervalIndexedGeometry::~IntervalIndexedGeometry( )
{
	delete index;

	for ( size_t i = 0, ni = allocatedSegments.size(); i < ni; ++i )
	{
		delete allocatedSegments[i];
	}
}

void
IndexedPointInAreaLocator::IntervalIndexedGeometry::init( const geom::Geometry & g)
{
	geom::LineString::ConstVect lines;
	geom::util::LinearComponentExtracter::getLines( g, lines);

	for ( size_t i = 0, ni = lines.size(); i < ni; i++ )
	{
		const geom::LineString * line = lines[ i ];
		geom::CoordinateSequence * pts = line->getCoordinates();

		addLine( pts);

		delete pts;
	}
}

void
IndexedPointInAreaLocator::IntervalIndexedGeometry::addLine( geom::CoordinateSequence * pts)
{
	for ( size_t i = 1, ni = pts->size(); i < ni; i++ )
	{
		geom::LineSegment * seg = new geom::LineSegment( (*pts)[ i - 1 ], (*pts)[ i ]);
		double const min = std::min( seg->p0.y, seg->p1.y);
		double const max = std::max( seg->p0.y, seg->p1.y);

		// NOTE: seg ownership still ours
		allocatedSegments.push_back(seg);
		index->insert( min, max, seg);
	}
}


void
IndexedPointInAreaLocator::buildIndex( const geom::Geometry & g)
{
	index = new IndexedPointInAreaLocator::IntervalIndexedGeometry( g);
}


//
// protected:
//

//
// public:
//
IndexedPointInAreaLocator::IndexedPointInAreaLocator( const geom::Geometry & g)
:	areaGeom( g)
{
	if (	typeid( areaGeom) != typeid( geom::Polygon)
		&&	typeid( areaGeom) != typeid( geom::MultiPolygon) )
		throw new util::IllegalArgumentException("Argument must be Polygonal");

	//areaGeom = g;

	buildIndex( areaGeom);
}

IndexedPointInAreaLocator::~IndexedPointInAreaLocator()
{
	delete index;
}

int
IndexedPointInAreaLocator::locate( const geom::Coordinate * /*const*/ p)
{
	algorithm::RayCrossingCounter rcc(*p);

	IndexedPointInAreaLocator::SegmentVisitor visitor( &rcc);

	index->query( p->y, p->y, &visitor);

	return rcc.getLocation();
}

void
IndexedPointInAreaLocator::SegmentVisitor::visitItem( void * item)
{
	geom::LineSegment * seg = (geom::LineSegment *)item;

	counter->countSegment( (*seg)[ 0 ], (*seg)[ 1 ]);
}

void
IndexedPointInAreaLocator::IntervalIndexedGeometry::query( double min, double max, index::ItemVisitor * visitor)
{
	index->query( min, max, visitor);
}


} // geos::algorithm::locate
} // geos::algorithm
} // geos
