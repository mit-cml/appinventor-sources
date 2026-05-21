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
 **********************************************************************
 *
 * Last port: linearref/LinearIterator.java r463
 *
 **********************************************************************/


#include <geos/geom/LineString.h>
#include <geos/linearref/LinearIterator.h>
#include <geos/linearref/LinearLocation.h>
#include <geos/linearref/LengthLocationMap.h>
#include <geos/util/IllegalArgumentException.h>

using namespace geos::geom;

namespace geos
{
namespace linearref   // geos.linearref
{

unsigned int LinearIterator::segmentEndVertexIndex(const LinearLocation& loc)
{
	if (loc.getSegmentFraction() > 0.0)
		return loc.getSegmentIndex() + 1;
	return loc.getSegmentIndex();
}

LinearIterator::LinearIterator(const Geometry* linear) :
		vertexIndex(0),
		componentIndex(0),
		linear(linear),
		numLines(static_cast<unsigned int>(linear->getNumGeometries()))
{
	loadCurrentLine();
}


LinearIterator::LinearIterator(const Geometry* linear, const LinearLocation& start):
		vertexIndex(segmentEndVertexIndex(start)),
		componentIndex(start.getComponentIndex()),
		linear(linear),
		numLines(static_cast<unsigned int>(linear->getNumGeometries()))
{
	loadCurrentLine();
}

LinearIterator::LinearIterator(const Geometry* linear, unsigned int componentIndex, unsigned int vertexIndex) :
		vertexIndex(vertexIndex),
		componentIndex(componentIndex),
		linear(linear),
		numLines(static_cast<unsigned int>(linear->getNumGeometries()))
{
	loadCurrentLine();
}

void LinearIterator::loadCurrentLine()
{
	if (componentIndex >= numLines)
	{
		currentLine = nullptr;
		return;
	}
	currentLine = dynamic_cast<const LineString *> (linear->getGeometryN(componentIndex));
	if ( ! currentLine ) {
		throw util::IllegalArgumentException("LinearIterator only supports lineal geometry components");
	}
}

bool LinearIterator::hasNext() const
{
	if (componentIndex >= numLines) return false;
	if (componentIndex == numLines - 1
			&& vertexIndex >= currentLine->getNumPoints())
		return false;
	return true;
}

void LinearIterator::next()
{
	if (! hasNext()) return;

	vertexIndex++;
	if (vertexIndex >= currentLine->getNumPoints())
	{
		componentIndex++;
		loadCurrentLine();
		vertexIndex = 0;
	}
}

bool LinearIterator::isEndOfLine() const
{
	if (componentIndex >= numLines) return false;
	//LineString currentLine = (LineString) linear.getGeometryN(componentIndex);
	if (!currentLine)
		return false;
	if (vertexIndex < currentLine->getNumPoints() - 1)
		return false;
	return true;
}

unsigned int LinearIterator::getComponentIndex() const
{
	return componentIndex;
}

unsigned int LinearIterator::getVertexIndex() const
{
	return vertexIndex;
}

const LineString* LinearIterator::getLine() const
{
	return currentLine;
}

Coordinate LinearIterator::getSegmentStart() const
{
	return currentLine->getCoordinateN(vertexIndex);
}

Coordinate LinearIterator::getSegmentEnd() const
{
	if (vertexIndex < getLine()->getNumPoints() - 1)
		return currentLine->getCoordinateN(vertexIndex + 1);
	Coordinate c;
	c.setNull();
	return c;
}
}
}
