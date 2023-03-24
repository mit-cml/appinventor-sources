/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2005-2006 Refractions Research Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: io/WKBReader.java rev. 1.1 (JTS-1.7)
 *
 **********************************************************************/

#include <geos/io/WKBReader.h>
#include <geos/io/WKBConstants.h>
#include <geos/io/ByteOrderValues.h>
#include <geos/io/ParseException.h>
#include <geos/geom/GeometryFactory.h>
#include <geos/geom/Coordinate.h>
#include <geos/geom/Point.h>
#include <geos/geom/LinearRing.h>
#include <geos/geom/LineString.h>
#include <geos/geom/Polygon.h>
#include <geos/geom/MultiPoint.h>
#include <geos/geom/MultiLineString.h>
#include <geos/geom/MultiPolygon.h>
#include <geos/geom/CoordinateSequenceFactory.h>
#include <geos/geom/CoordinateSequence.h>
#include <geos/geom/PrecisionModel.h>

#include <iomanip>
#include <ostream>
#include <sstream>
#include <string>

//#define DEBUG_WKB_READER 1

using namespace std;
using namespace geos::geom;

namespace geos {
namespace io { // geos.io

WKBReader::WKBReader()
	:
	factory(*(GeometryFactory::getDefaultInstance()))
{}

ostream &
WKBReader::printHEX(istream &is, ostream &os)
{
	static const char hex[] = "0123456789ABCDEF";

    std::streampos pos = is.tellg(); // take note of input stream get pointer
	is.seekg(0, ios::beg); // rewind input stream

	char each=0;
	while(is.read(&each, 1))
	{
		const unsigned char c=each;
		int low = (c & 0x0F);
		int high = (c >> 4);
		os << hex[high] << hex[low];
	}

	is.clear(); // clear input stream eof flag
	is.seekg(pos); // reset input stream position

	return os;
}


namespace {

unsigned char ASCIIHexToUChar(char val)
{
	switch ( val )
	{
		case '0' :
			return 0;
		case '1' :
			return 1;
		case '2' :
			return 2;
		case '3' :
			return 3;
		case '4' :
			return 4;
		case '5' :
			return 5;
		case '6' :
			return 6;
		case '7' :
			return 7;
		case '8' :
			return 8;
		case '9' :
			return 9;
		case 'A' :
		case 'a' :
			return 10;
		case 'B' :
		case 'b' :
			return 11;
		case 'C' :
		case 'c' :
			return 12;
		case 'D' :
		case 'd' :
			return 13;
		case 'E' :
		case 'e' :
			return 14;
		case 'F' :
		case 'f' :
			return 15;
		default:
			throw ParseException("Invalid HEX char");
	}
}

}  // namespace

// Must be an even number of characters in the istream.
// Throws a ParseException if there are an odd number of characters.
Geometry *
WKBReader::readHEX(istream &is)
{
	// setup input/output stream
	stringstream os(ios_base::binary|ios_base::in|ios_base::out);

	while( true )
	{
		const int input_high = is.get();
		if (input_high == char_traits<char>::eof())
			break;

		const int input_low = is.get();
		if (input_low == char_traits<char>::eof())
			throw ParseException("Premature end of HEX string");

		const char high = static_cast<char>(input_high);
		const char low = static_cast<char>(input_low);

		const unsigned char result_high = ASCIIHexToUChar(high);
		const unsigned char result_low = ASCIIHexToUChar(low);

		const unsigned char value =
			static_cast<char>((result_high<<4) + result_low);

#if DEBUG_HEX_READER
	cout<<"HEX "<<high<<low<<" -> DEC "<<(int)value<<endl;
#endif
		// write the value to the output stream
		os << value;
	}

	// now call read to convert the geometry
	return this->read(os);
}

Geometry *
WKBReader::read(istream &is)
{
	dis.setInStream(&is); // will default to machine endian
	return readGeometry();
}

Geometry *
WKBReader::readGeometry()
{
	// determine byte order
	unsigned char byteOrder = dis.readByte();

#if DEBUG_WKB_READER
	cout<<"WKB byteOrder: "<<(int)byteOrder<<endl;
#endif

	// default is machine endian
	if (byteOrder == WKBConstants::wkbNDR)
		dis.setOrder(ByteOrderValues::ENDIAN_LITTLE);
        else if (byteOrder == WKBConstants::wkbXDR)
                dis.setOrder(ByteOrderValues::ENDIAN_BIG);

	int typeInt = dis.readInt();
	int geometryType = typeInt & 0xff;

#if DEBUG_WKB_READER
	cout<<"WKB geometryType: "<<geometryType<<endl;
#endif

	bool hasZ = ((typeInt & 0x80000000) != 0);
	if (hasZ) inputDimension = 3;
	else inputDimension = 2; // doesn't handle M currently

#if DEBUG_WKB_READER
	cout<<"WKB hasZ: "<<hasZ<<endl;
#endif

#if DEBUG_WKB_READER
	cout<<"WKB dimensions: "<<inputDimension<<endl;
#endif

	bool hasSRID = ((typeInt & 0x20000000) != 0);

#if DEBUG_WKB_READER
	cout<<"WKB hasSRID: "<<hasSRID<<endl;
#endif

	int SRID = 0;
	if (hasSRID) SRID = dis.readInt(); // read SRID


	// allocate space for ordValues
	if ( ordValues.size() < inputDimension )
		ordValues.resize(inputDimension);

	Geometry *result;

	switch (geometryType) {
		case WKBConstants::wkbPoint :
			result = readPoint();
			break;
		case WKBConstants::wkbLineString :
			result = readLineString();
			break;
		case WKBConstants::wkbPolygon :
			result = readPolygon();
			break;
		case WKBConstants::wkbMultiPoint :
			result = readMultiPoint();
			break;
		case WKBConstants::wkbMultiLineString :
			result = readMultiLineString();
			break;
		case WKBConstants::wkbMultiPolygon :
			result = readMultiPolygon();
			break;
		case WKBConstants::wkbGeometryCollection :
			result = readGeometryCollection();
			break;
		default:
			stringstream err;
			err << "Unknown WKB type " << geometryType;
			throw  ParseException(err.str());
	}

	result->setSRID(SRID);
	return result;
}

Point *
WKBReader::readPoint()
{
	readCoordinate();
	if(inputDimension == 3){
	  return factory.createPoint(Coordinate(ordValues[0], ordValues[1], ordValues[2]));
	}else{
	  return factory.createPoint(Coordinate(ordValues[0], ordValues[1]));
	}
}

LineString *
WKBReader::readLineString()
{
	int size = dis.readInt();
#if DEBUG_WKB_READER
	cout<<"WKB npoints: "<<size<<endl;
#endif
	CoordinateSequence *pts = readCoordinateSequence(size);
	return factory.createLineString(pts);
}

LinearRing *
WKBReader::readLinearRing()
{
	int size = dis.readInt();
#if DEBUG_WKB_READER
	cout<<"WKB npoints: "<<size<<endl;
#endif
	CoordinateSequence *pts = readCoordinateSequence(size);
	return factory.createLinearRing(pts);
}

Polygon *
WKBReader::readPolygon()
{
	int numRings = dis.readInt();

#if DEBUG_WKB_READER
	cout<<"WKB numRings: "<<numRings<<endl;
#endif

        LinearRing *shell = nullptr;
        if( numRings > 0 )
            shell = readLinearRing();

	vector<Geometry *>*holes=nullptr;
	if ( numRings > 1 )
	{
		try {
			holes = new vector<Geometry *>(numRings-1);
			for (int i=0; i<numRings-1; i++)
				(*holes)[i] = (Geometry *)readLinearRing();
		} catch (...) {
			for (unsigned int i=0; i<holes->size(); i++)
				delete (*holes)[i];
			delete holes;
			delete shell;
			throw;
		}
	}
	return factory.createPolygon(shell, holes);
}

MultiPoint *
WKBReader::readMultiPoint()
{
	int numGeoms = dis.readInt();
	vector<Geometry *> *geoms = new vector<Geometry *>(numGeoms);

	try {
		for (int i=0; i<numGeoms; i++)
		{
			Geometry *g = readGeometry();
			if (!dynamic_cast<Point *>(g))
			{
				stringstream err;
				err << BAD_GEOM_TYPE_MSG << " MultiPoint";
				throw ParseException(err.str());
			}
			(*geoms)[i] = g;
		}
	} catch (...) {
		for (unsigned int i=0; i<geoms->size(); i++)
			delete (*geoms)[i];
		delete geoms;
		throw;
	}
	return factory.createMultiPoint(geoms);
}

MultiLineString *
WKBReader::readMultiLineString()
{
	int numGeoms = dis.readInt();
	vector<Geometry *> *geoms = new vector<Geometry *>(numGeoms);

	try {
		for (int i=0; i<numGeoms; i++)
		{
			Geometry *g = readGeometry();
			if (!dynamic_cast<LineString *>(g))
			{
				stringstream err;
				err << BAD_GEOM_TYPE_MSG << " LineString";
				throw  ParseException(err.str());
			}
			(*geoms)[i] = g;
		}
	} catch (...) {
		for (unsigned int i=0; i<geoms->size(); i++)
			delete (*geoms)[i];
		delete geoms;
		throw;
	}
	return factory.createMultiLineString(geoms);
}

MultiPolygon *
WKBReader::readMultiPolygon()
{
	int numGeoms = dis.readInt();
	vector<Geometry *> *geoms = new vector<Geometry *>(numGeoms);

	try {
		for (int i=0; i<numGeoms; i++)
		{
			Geometry *g = readGeometry();
			if (!dynamic_cast<Polygon *>(g))
			{
				stringstream err;
				err << BAD_GEOM_TYPE_MSG << " Polygon";
				throw  ParseException(err.str());
			}
			(*geoms)[i] = g;
		}
	} catch (...) {
		for (unsigned int i=0; i<geoms->size(); i++)
			delete (*geoms)[i];
		delete geoms;
		throw;
	}
	return factory.createMultiPolygon(geoms);
}

GeometryCollection *
WKBReader::readGeometryCollection()
{
	int numGeoms = dis.readInt();
	vector<Geometry *> *geoms = new vector<Geometry *>(numGeoms);

	try {
		for (int i=0; i<numGeoms; i++)
			(*geoms)[i] = (readGeometry());
	} catch (...) {
		for (unsigned int i=0; i<geoms->size(); i++)
			delete (*geoms)[i];
		delete geoms;
		throw;
	}
	return factory.createGeometryCollection(geoms);
}

CoordinateSequence *
WKBReader::readCoordinateSequence(int size)
{
	CoordinateSequence *seq = factory.getCoordinateSequenceFactory()->create(size, inputDimension);
	auto targetDim = seq->getDimension();
	if ( targetDim > inputDimension )
		targetDim = inputDimension;
	for (int i=0; i<size; i++) {
		readCoordinate();
		for (unsigned int j=0; j<targetDim; j++) {
			seq->setOrdinate(i, j, ordValues[j]);
		}
	}
	return seq;
}

void
WKBReader::readCoordinate()
{
	const PrecisionModel &pm = *factory.getPrecisionModel();
	for (unsigned int i=0; i<inputDimension; ++i)
	{
		if ( i <= 1 ) ordValues[i] = pm.makePrecise(dis.readDouble());
		else ordValues[i] = dis.readDouble();
	}
#if DEBUG_WKB_READER
	cout<<"WKB coordinate: "<<ordValues[0]<<","<<ordValues[1]<<endl;
#endif
}

} // namespace geos.io
} // namespace geos
