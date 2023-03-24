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
 **********************************************************************
 *
 * Last port: util/GeometricShapeFactory.java rev 1.14 (JTS-1.10+)
 * (2009-03-19)
 *
 **********************************************************************/

#include <geos/util/GeometricShapeFactory.h>
#include <geos/geom/Coordinate.h>
#include <geos/geom/CoordinateSequenceFactory.h>
#include <geos/geom/GeometryFactory.h>
#include <geos/geom/PrecisionModel.h>
#include <geos/geom/Envelope.h>

#include <vector>
#include <cmath>
#include <memory>

#ifndef M_PI
#define M_PI        3.14159265358979323846
#endif

using namespace std;
using namespace geos::geom;

namespace geos {
namespace util { // geos.util

GeometricShapeFactory::GeometricShapeFactory(const GeometryFactory* factory)
	:
	geomFact(factory),
	precModel(factory->getPrecisionModel()),
	nPts(100)
{
}

void
GeometricShapeFactory::setBase(const Coordinate& base)
{
	dim.setBase(base);
}

void
GeometricShapeFactory::setCentre(const Coordinate& centre)
{
	dim.setCentre(centre);
}

void
GeometricShapeFactory::setNumPoints(int nNPts)
{
	nPts=nNPts;
}

void
GeometricShapeFactory::setSize(double size)
{
	dim.setSize(size);
}

void
GeometricShapeFactory::setWidth(double width)
{
	dim.setWidth(width);
}

void
GeometricShapeFactory::setHeight(double height)
{
	dim.setHeight(height);
}

Polygon*
GeometricShapeFactory::createRectangle()
{
	int i;
	int ipt = 0;
	int nSide = nPts / 4;
	if (nSide < 1) nSide = 1;
	std::unique_ptr<Envelope> env ( dim.getEnvelope() );
	double XsegLen = env->getWidth() / nSide;
	double YsegLen = env->getHeight() / nSide;

	vector<Coordinate> *vc = new vector<Coordinate>(4*nSide+1);
	//CoordinateSequence* pts=new CoordinateArraySequence(4*nSide+1);

	for (i = 0; i < nSide; i++) {
		double x = env->getMinX() + i * XsegLen;
		double y = env->getMinY();
		(*vc)[ipt++] = coord(x, y);
	}
	for (i = 0; i < nSide; i++) {
		double x = env->getMaxX();
		double y = env->getMinY() + i * YsegLen;
		(*vc)[ipt++] = coord(x, y);
	}
	for (i = 0; i < nSide; i++) {
		double x = env->getMaxX() - i * XsegLen;
		double y = env->getMaxY();
		(*vc)[ipt++] = coord(x, y);
	}
	for (i = 0; i < nSide; i++) {
		double x = env->getMinX();
		double y = env->getMaxY() - i * YsegLen;
		(*vc)[ipt++] = coord(x, y);
	}
	(*vc)[ipt++] = (*vc)[0];
	CoordinateSequence *cs = geomFact->getCoordinateSequenceFactory()->create(vc);
	LinearRing* ring=geomFact->createLinearRing(cs);
	Polygon* poly=geomFact->createPolygon(ring, nullptr);
	return poly;
}

Polygon*
GeometricShapeFactory::createCircle()
{
	std::unique_ptr<Envelope> env ( dim.getEnvelope() );
	double xRadius = env->getWidth() / 2.0;
	double yRadius = env->getHeight() / 2.0;

	double centreX = env->getMinX() + xRadius;
	double centreY = env->getMinY() + yRadius;
	env.reset();

	vector<Coordinate>*pts=new vector<Coordinate>(nPts+1);
	int iPt = 0;
	for (int i = 0; i < nPts; i++) {
		double ang = i * (2 * 3.14159265358979 / nPts);
		double x = xRadius * cos(ang) + centreX;
		double y = yRadius * sin(ang) + centreY;
		(*pts)[iPt++] = coord(x, y);
	}
	(*pts)[iPt++] = (*pts)[0];
	CoordinateSequence *cs=geomFact->getCoordinateSequenceFactory()->create(pts);
	LinearRing* ring = geomFact->createLinearRing(cs);
	Polygon* poly=geomFact->createPolygon(ring,nullptr);
	return poly;
}

LineString*
GeometricShapeFactory::createArc(double startAng, double angExtent)
{
	std::unique_ptr<Envelope> env ( dim.getEnvelope() );
	double xRadius = env->getWidth() / 2.0;
	double yRadius = env->getHeight() / 2.0;

	double centreX = env->getMinX() + xRadius;
	double centreY = env->getMinY() + yRadius;
	env.reset();

	double angSize = angExtent;
	if (angSize <= 0.0 || angSize > 2 * M_PI)
		angSize = 2 * M_PI;
	double angInc = angSize / ( nPts - 1 );

	vector<Coordinate> *pts = new vector<Coordinate>(nPts);
	int iPt = 0;
	for (int i = 0; i < nPts; i++) {
		double ang = startAng + i * angInc;
		double x = xRadius * cos(ang) + centreX;
		double y = yRadius * sin(ang) + centreY;
		(*pts)[iPt++] = coord(x, y);
	}
	CoordinateSequence *cs = geomFact->getCoordinateSequenceFactory()->create(pts);
	LineString* line = geomFact->createLineString(cs);
	return line;
}

Polygon*
GeometricShapeFactory::createArcPolygon(double startAng, double angExtent)
{
	std::unique_ptr<Envelope> env ( dim.getEnvelope() );
	double xRadius = env->getWidth() / 2.0;
	double yRadius = env->getHeight() / 2.0;

	double centreX = env->getMinX() + xRadius;
	double centreY = env->getMinY() + yRadius;
	env.reset();

	double angSize = angExtent;
	if (angSize <= 0.0 || angSize > 2 * M_PI)
		angSize = 2 * M_PI;
	double angInc = angSize / ( nPts - 1 );

	vector<Coordinate> *pts = new vector<Coordinate>(nPts + 2);
	int iPt = 0;
	(*pts)[iPt++] = coord(centreX, centreY);
	for (int i = 0; i < nPts; i++) {
		double ang = startAng + i * angInc;
		double x = xRadius * cos(ang) + centreX;
		double y = yRadius * sin(ang) + centreY;
		(*pts)[iPt++] = coord(x, y);
	}
	(*pts)[iPt++] = coord(centreX, centreY);

	CoordinateSequence *cs = geomFact->getCoordinateSequenceFactory()->create(pts);
	LinearRing* ring = geomFact->createLinearRing(cs);
	Polygon* geom = geomFact->createPolygon(ring, nullptr);
	return geom;
}

GeometricShapeFactory::Dimensions::Dimensions()
	:
	base(Coordinate::getNull()),
	centre(Coordinate::getNull())
{
}

void
GeometricShapeFactory::Dimensions::setBase(const Coordinate& newBase)
{
	base=newBase;
}

void
GeometricShapeFactory::Dimensions::setCentre(const Coordinate& newCentre)
{
	centre=newCentre;
}

void
GeometricShapeFactory::Dimensions::setSize(double size)
{
	height = size;
	width = size;
}

void
GeometricShapeFactory::Dimensions::setWidth(double nWidth)
{
	width=nWidth;
}

void GeometricShapeFactory::Dimensions::setHeight(double nHeight)
{
	height=nHeight;
}

Envelope*
GeometricShapeFactory::Dimensions::getEnvelope() const
{
	if (!base.isNull()) {
		return new Envelope(base.x, base.x + width, base.y, base.y + height);
	}
	if (!centre.isNull()) {
		return new Envelope(centre.x - width/2, centre.x + width/2,centre.y - height/2, centre.y + height/2);
	}
	return new Envelope(0, width, 0, height);
}

/*protected*/
Coordinate
GeometricShapeFactory::coord(double x, double y) const
{
	Coordinate ret(x, y);
	precModel->makePrecise(&ret);
	return ret;
}

} // namespace geos.util
} // namespace geos

