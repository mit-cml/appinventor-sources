/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2011 Sandro Santilli <strk@kbt.io>
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
 * Last port: io/WKTWriter.java rev. 1.34 (JTS-1.7)
 *
 **********************************************************************/

#include <geos/platform.h>
#include <geos/io/WKTWriter.h>
#include <geos/io/Writer.h>
#include <geos/io/CLocalizer.h>
#include <geos/geom/Coordinate.h>
#include <geos/geom/Point.h>
#include <geos/geom/LinearRing.h>
#include <geos/geom/LineString.h>
#include <geos/geom/Polygon.h>
#include <geos/geom/MultiPoint.h>
#include <geos/geom/MultiLineString.h>
#include <geos/geom/MultiPolygon.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/geom/PrecisionModel.h>
#include <geos/util/IllegalArgumentException.h>

#include <algorithm> // for min
#include <typeinfo>
#include <cstdio> // should avoid this
#include <string>
#include <sstream>
#include <cassert>
#include <cmath>
#include <iomanip>

using namespace std;
using namespace geos::geom;

namespace geos {
namespace io { // geos.io

WKTWriter::WKTWriter():
  decimalPlaces(6),
	isFormatted(false),
	roundingPrecision(-1),
	trim(false),
	level(0),
	defaultOutputDimension(2),
	old3D(false)
{
}

/* public */
void
WKTWriter::setOutputDimension(int dims)
{
	if ( dims < 2 || dims > 3 )
		throw util::IllegalArgumentException("WKT output dimension must be 2 or 3");
	defaultOutputDimension = dims;
}

WKTWriter::~WKTWriter() {}

/*static*/
string
WKTWriter::toLineString(const CoordinateSequence& seq)
{
	stringstream buf(ios_base::in|ios_base::out);
    buf << "LINESTRING ";
	unsigned int npts = static_cast<unsigned int>(seq.getSize());
	if ( npts == 0 )
	{
		buf << "EMPTY";
	}
	else
	{
		buf << "(";
		for (unsigned int i=0; i<npts; ++i)
		{
			if (i) buf << ", ";
			buf << seq.getX(i) << " " << seq.getY(i);
#if PRINT_Z
			buf << seq.getZ(i);
#endif
		}
		buf << ")";
	}

	return buf.str();
}

/*static*/
string
WKTWriter::toLineString(const Coordinate& p0, const Coordinate& p1)
{
	stringstream ret(ios_base::in|ios_base::out);
	ret << "LINESTRING (" << p0.x << " " << p0.y;
#if PRINT_Z
	ret << " " << p0.z;
#endif
	ret << ", " << p1.x << " " << p1.y;
#if PRINT_Z
	ret << " " << p1.z;
#endif
    ret << ")";

	return ret.str();
}

/*static*/
string
WKTWriter::toPoint(const Coordinate& p0)
{
	stringstream ret(ios_base::in|ios_base::out);
    ret << "POINT (";
#if PRINT_Z
	ret << p0.x << " " << p0.y  << " " << p0.z << " )";
#else
	ret << p0.x << " " << p0.y  << " )";
#endif
	return ret.str();
}

void
WKTWriter::setRoundingPrecision(int p0)
{
	if(p0 < -1) {
		p0 = -1;
	}
	roundingPrecision = p0;
}

void
WKTWriter::setTrim(bool p0)
{
	trim = p0;
}

string WKTWriter::write(const Geometry *geometry) {
	Writer sw;
  writeFormatted(geometry,false,&sw);
	string res=sw.toString();
	return res;
}

void WKTWriter::write(const Geometry *geometry, Writer *writer) {
	writeFormatted(geometry, false, writer);
}

string WKTWriter::writeFormatted(const Geometry *geometry) {
	Writer sw;
	writeFormatted(geometry, true, &sw);
	return sw.toString();
}

void WKTWriter::writeFormatted(const Geometry *geometry, Writer *writer) {
	writeFormatted(geometry, true, writer);
}

void
WKTWriter::writeFormatted(const Geometry *geometry, bool isFormatted,
                          Writer *writer)
{
        CLocalizer clocale;
	this->isFormatted=isFormatted;
  decimalPlaces = roundingPrecision == -1 ? geometry->getPrecisionModel()->getMaximumSignificantDigits() : roundingPrecision;
	appendGeometryTaggedText(geometry, 0, writer);
}

void
WKTWriter::appendGeometryTaggedText(const Geometry *geometry, int level,
		Writer *writer)
{
  outputDimension = std::min( defaultOutputDimension,
                         geometry->getCoordinateDimension() );

  indent(level, writer);
  if ( const Point* point = dynamic_cast<const Point*>(geometry) )
  {
    appendPointTaggedText(point->getCoordinate(),level,writer);
  }
  else if ( const LinearRing* lr =
    dynamic_cast<const LinearRing*>(geometry) )
  {
    appendLinearRingTaggedText(lr, level, writer);
  }
  else if ( const LineString* ls =
    dynamic_cast<const LineString*>(geometry) )
  {
    appendLineStringTaggedText(ls, level, writer);
  }
  else if ( const Polygon* x =
    dynamic_cast<const Polygon*>(geometry) )
  {
    appendPolygonTaggedText(x, level, writer);
  }
  else if ( const MultiPoint* x =
    dynamic_cast<const MultiPoint*>(geometry) )
  {
    appendMultiPointTaggedText(x, level, writer);
  }
  else if ( const MultiLineString* x =
    dynamic_cast<const MultiLineString*>(geometry) )
  {
    appendMultiLineStringTaggedText(x, level, writer);
  }
  else if ( const MultiPolygon* x =
    dynamic_cast<const MultiPolygon*>(geometry) )
  {
    appendMultiPolygonTaggedText(x, level, writer);
  }
  else if ( const GeometryCollection* x =
    dynamic_cast<const GeometryCollection*>(geometry) )
  {
    appendGeometryCollectionTaggedText(x, level, writer);
  }
  else
  {
    assert(0); // Unsupported Geometry implementation
  }
}

/*protected*/
void
WKTWriter::appendPointTaggedText(const Coordinate* coordinate, int level,
		Writer *writer)
{
	writer->write("POINT ");
	if( outputDimension == 3 && !old3D && coordinate != nullptr )
		writer->write( "Z " );

	appendPointText(coordinate, level, writer);
}

void
WKTWriter::appendLineStringTaggedText(const LineString *lineString, int level,
		Writer *writer)
{
	writer->write("LINESTRING ");
	if( outputDimension == 3 && !old3D && !lineString->isEmpty() )
		writer->write( "Z " );

	appendLineStringText(lineString, level, false, writer);
}

/*
 * Converts a <code>LinearRing</code> to &lt;LinearRing Tagged Text&gt;
 * format, then appends it to the writer.
 *
 * @param  linearRing  the <code>LinearRing</code> to process
 * @param  writer      the output writer to append to
 */
void WKTWriter::appendLinearRingTaggedText(const LinearRing* linearRing, int level, Writer *writer) {
	writer->write("LINEARRING ");
	if( outputDimension == 3 && !old3D && !linearRing->isEmpty() )
		writer->write( "Z " );
	appendLineStringText((LineString*)linearRing, level, false, writer);
}

void WKTWriter::appendPolygonTaggedText(const Polygon *polygon, int level, Writer *writer) {
	writer->write("POLYGON ");
	if( outputDimension == 3 && !old3D && !polygon->isEmpty())
		writer->write( "Z " );
	appendPolygonText(polygon, level, false, writer);
}

void WKTWriter::appendMultiPointTaggedText(const MultiPoint *multipoint, int level, Writer *writer) {
	writer->write("MULTIPOINT ");
	if( outputDimension == 3 && !old3D && !multipoint->isEmpty() )
		writer->write( "Z " );
	appendMultiPointText(multipoint, level, writer);
}

void WKTWriter::appendMultiLineStringTaggedText(const MultiLineString *multiLineString, int level,Writer *writer) {
	writer->write("MULTILINESTRING ");
	if( outputDimension == 3 && !old3D && !multiLineString->isEmpty() )
		writer->write( "Z " );
	appendMultiLineStringText(multiLineString, level, false, writer);
}

void WKTWriter::appendMultiPolygonTaggedText(const MultiPolygon *multiPolygon, int level, Writer *writer) {
	writer->write("MULTIPOLYGON ");
	if( outputDimension == 3 && !old3D && !multiPolygon->isEmpty() )
		writer->write( "Z " );
	appendMultiPolygonText(multiPolygon, level, writer);
}

void WKTWriter::appendGeometryCollectionTaggedText(const GeometryCollection *geometryCollection, int level,Writer *writer) {
	writer->write("GEOMETRYCOLLECTION ");
	if( outputDimension == 3 && !old3D && !geometryCollection->isEmpty() )
		writer->write( "Z " );
	appendGeometryCollectionText(geometryCollection, level, writer);
}

void
WKTWriter::appendPointText(const Coordinate* coordinate, int /*level*/,
		Writer *writer)
{
	if (coordinate==nullptr) {
		writer->write("EMPTY");
	} else {
		writer->write("(");
		appendCoordinate(coordinate, writer);
		writer->write(")");
	}
}

/* protected */
void
WKTWriter::appendCoordinate(const Coordinate* coordinate,
		Writer *writer)
{
	writer->write(writeNumber(coordinate->x));
	writer->write(" ");
	writer->write(writeNumber(coordinate->y));
	if( outputDimension == 3 )
	{
		writer->write(" ");
		if( ISNAN(coordinate->z) )
			writer->write(writeNumber(0.0));
		else
			writer->write(writeNumber(coordinate->z));
	}
}

/* protected */
string
WKTWriter::writeNumber(double d) {

  std::stringstream ss;

  if ( ! trim ) ss << std::fixed;
  ss << std::setprecision(decimalPlaces >= 0 ? decimalPlaces : 0) << d;

	return ss.str();
}

void
WKTWriter::appendLineStringText(const LineString *lineString, int level,
		bool doIndent, Writer *writer)
{
	if (lineString->isEmpty()) {
		writer->write("EMPTY");
	} else {
		if (doIndent) indent(level, writer);
		writer->write("(");
		for(size_t i=0, n=lineString->getNumPoints(); i<n; ++i)
		{
			if (i>0) {
				writer->write(", ");
				if (i%10==0) indent(level + 2, writer);
			}
			appendCoordinate(&(lineString->getCoordinateN(static_cast<int>(i))), writer);
		}
		writer->write(")");
	}
}

void
WKTWriter::appendPolygonText(const Polygon *polygon, int /*level*/,
		bool indentFirst, Writer *writer)
{
	if (polygon->isEmpty()) {
		writer->write("EMPTY");
	} else {
		if (indentFirst) indent(level, writer);
		writer->write("(");
		appendLineStringText(polygon->getExteriorRing(), level, false, writer);
		for (size_t i=0, n=polygon->getNumInteriorRing(); i<n; ++i)
		{
			writer->write(", ");
			const LineString *ls=polygon->getInteriorRingN(i);
			appendLineStringText(ls, level + 1, true, writer);
		}
		writer->write(")");
	}
}

void
WKTWriter::appendMultiPointText(const MultiPoint *multiPoint,
		int /*level*/, Writer *writer)
{
	if (multiPoint->isEmpty()) {
		writer->write("EMPTY");
	} else {
		writer->write("(");
		for (unsigned int i=0, n=static_cast<unsigned int>(multiPoint->getNumGeometries());
				i<n; i++)
		{
			if (i > 0)
			{
				writer->write(", ");
			}
			appendCoordinate(
				dynamic_cast<const Point*>(multiPoint->getGeometryN(i))->getCoordinate(),
				writer);
		}
		writer->write(")");
	}
}

void WKTWriter::appendMultiLineStringText(const MultiLineString *multiLineString, int level, bool indentFirst,
											Writer *writer) {
	if (multiLineString->isEmpty()) {
		writer->write("EMPTY");
	} else {
		int level2=level;
		bool doIndent=indentFirst;
		writer->write("(");
		for (unsigned int i=0, n=static_cast<unsigned int>(multiLineString->getNumGeometries());
				i<n; i++)
		{
			if (i>0) {
				writer->write(", ");
				level2=level+1;
				doIndent=true;
			}
			const LineString* ls = dynamic_cast<const LineString *>(
			  multiLineString->getGeometryN(i)
			);
			appendLineStringText(ls, level2, doIndent, writer);
		}
		writer->write(")");
	}
}

void WKTWriter::appendMultiPolygonText(const MultiPolygon *multiPolygon, int level, Writer *writer) {
	if (multiPolygon->isEmpty()) {
		writer->write("EMPTY");
	} else {
		int level2=level;
		bool doIndent=false;
		writer->write("(");
		for (unsigned int i=0, n=static_cast<unsigned int>(multiPolygon->getNumGeometries());
				i < n; i++)
		{
			if (i>0) {
				writer->write(", ");
				level2=level+1;
				doIndent=true;
			}
			const Polygon *p = dynamic_cast<const Polygon *>(
			  multiPolygon->getGeometryN(i)
			);
			appendPolygonText(p, level2, doIndent, writer);
		}
		writer->write(")");
	}
}

void
WKTWriter::appendGeometryCollectionText(
		const GeometryCollection *geometryCollection,
		int level,
		Writer *writer)
{
	if (geometryCollection->isEmpty()) {
		writer->write("EMPTY");
	} else {
		int level2=level;
		writer->write("(");
		for (unsigned int i=0, n=static_cast<unsigned int>(geometryCollection->getNumGeometries());
				i < n ; ++i)
		{
			if (i>0) {
				writer->write(", ");
				level2=level+1;
			}
			appendGeometryTaggedText(geometryCollection->getGeometryN(i),level2,writer);
		}
		writer->write(")");
	}
}

void WKTWriter::indent(int level, Writer *writer) {
	if (!isFormatted || level<=0) return;
	writer->write("\n");
	writer->write(string(INDENT * level, ' '));
}

} // namespace geos.io
} // namespace geos

