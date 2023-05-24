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
 * Last port: geom/Envelope.java rev 1.46 (JTS-1.10)
 *
 **********************************************************************/

#include <geos/geom/Envelope.h>
#include <geos/geom/Coordinate.h>

#include <algorithm>
#include <sstream>
#include <cmath>

#ifndef GEOS_INLINE
# include <geos/geom/Envelope.inl>
#endif

#ifndef GEOS_DEBUG
#define GEOS_DEBUG 0
#endif

#if GEOS_DEBUG
#include <iostream>
#endif

using namespace std;

namespace geos {
namespace geom { // geos::geom

/*public*/
bool
Envelope::intersects(const Coordinate& p1, const Coordinate& p2,
		const Coordinate& q)
{
	//OptimizeIt shows that Math#min and Math#max here are a bottleneck.
    //Replace with direct comparisons. [Jon Aquino]
    if (((q.x >= (p1.x < p2.x ? p1.x : p2.x)) && (q.x <= (p1.x > p2.x ? p1.x : p2.x))) &&
        ((q.y >= (p1.y < p2.y ? p1.y : p2.y)) && (q.y <= (p1.y > p2.y ? p1.y : p2.y)))) {
			return true;
	}
	return false;
}

/*public*/
bool
Envelope::intersects(const Coordinate& p1, const Coordinate& p2,
	const Coordinate& q1, const Coordinate& q2)
{
	double minq=min(q1.x,q2.x);
	double maxq=max(q1.x,q2.x);
	double minp=min(p1.x,p2.x);
	double maxp=max(p1.x,p2.x);
	if(minp>maxq)
		return false;
	if(maxp<minq)
		return false;
	minq=min(q1.y,q2.y);
	maxq=max(q1.y,q2.y);
	minp=min(p1.y,p2.y);
	maxp=max(p1.y,p2.y);
	if(minp>maxq)
		return false;
	if(maxp<minq)
		return false;
	return true;
}

/*public*/
double
Envelope::distance(double x0,double y0,double x1,double y1)
{
	double dx=x1-x0;
	double dy=y1-y0;
	return sqrt(dx*dx+dy*dy);
}

/*public*/
Envelope::Envelope(void)
{
	init();
}

/*public*/
Envelope::Envelope(double x1, double x2, double y1, double y2)
{
	init(x1, x2, y1, y2);
}

/*public*/
Envelope::Envelope(const Coordinate& p1, const Coordinate& p2)
{
	init(p1, p2);
}

/*public*/
Envelope::Envelope(const Coordinate& p)
{
	init(p);
}

/*public*/
Envelope::Envelope(const Envelope &env)
	:
	minx(env.minx),
	maxx(env.maxx),
	miny(env.miny),
	maxy(env.maxy)
{
#if GEOS_DEBUG
	std::cerr<<"Envelope copy"<<std::endl;
#endif
	//init(env.minx, env.maxx, env.miny, env.maxy);
}

/*public*/
Envelope::Envelope(const string &str)
{
  // The string should be in the format:
  // Env[7.2:2.3,7.1:8.2]

  // extract out the values between the [ and ] characters
  string::size_type index = str.find("[");
  string coordString = str.substr(index + 1, str.size() - 1 - 1);

  // now split apart the string on : and , characters
  vector<string> values = split(coordString, ":,");

  // create a new envelopet
  init(::atof(values[0].c_str()),
       ::atof(values[1].c_str()),
       ::atof(values[2].c_str()),
       ::atof(values[3].c_str()));
}

/*public*/
Envelope::~Envelope(void) {}

/*public*/
void
Envelope::init()
{
	setToNull();
}

/*public*/
void
Envelope::init(double x1, double x2, double y1, double y2)
{
	if (x1 < x2) {
		minx = x1;
		maxx = x2;
	} else {
		minx = x2;
		maxx = x1;
	}
	if (y1 < y2) {
		miny = y1;
		maxy = y2;
	} else {
		miny = y2;
		maxy = y1;
	}
}

/*public*/
void
Envelope::init(const Coordinate& p1, const Coordinate& p2)
{
	init(p1.x, p2.x, p1.y, p2.y);
}

/*public*/
void
Envelope::init(const Coordinate& p)
{
	init(p.x, p.x, p.y, p.y);
}

#if 0
/**
 *  Initialize an <code>Envelope</code> from an existing Envelope.
 *
 *@param  env  the Envelope to initialize from
 */
void
Envelope::init(Envelope env)
{
	init(env.minx, env.maxx, env.miny, env.maxy);
}
#endif // 0

/*public*/
void
Envelope::setToNull()
{
	minx=0;
	maxx=-1;
	miny=0;
	maxy=-1;
}

/*public*/
double
Envelope::getWidth() const
{
	if (isNull()) {
		return 0;
	}
	return maxx - minx;
}

/*public*/
double
Envelope::getHeight() const
{
	if (isNull()) {
		return 0;
	}
	return maxy - miny;
}

/*public*/
void
Envelope::expandToInclude(const Coordinate& p)
{
	expandToInclude(p.x, p.y);
}

/*public*/
void
Envelope::expandToInclude(double x, double y)
{
	if (isNull()) {
		minx = x;
		maxx = x;
		miny = y;
		maxy = y;
	} else {
		if (x < minx) {
			minx = x;
		}
		if (x > maxx) {
			maxx = x;
		}
		if (y < miny) {
			miny = y;
		}
		if (y > maxy) {
			maxy = y;
		}
	}
}

/*public*/
void
Envelope::expandToInclude(const Envelope* other)
{
	if (other->isNull()) {
		return;
	}
	if (isNull()) {
		minx = other->getMinX();
		maxx = other->getMaxX();
		miny = other->getMinY();
		maxy = other->getMaxY();
	} else {
		if (other->minx < minx) {
			minx = other->minx;
		}
		if (other->maxx > maxx) {
			maxx = other->maxx;
		}
		if (other->miny < miny) {
			miny = other->miny;
		}
		if (other->maxy > maxy) {
			maxy = other->maxy;
		}
	}
}

/*public*/
bool
Envelope::covers(double x, double y) const
{
	if (isNull()) return false;
	return x >= minx &&
		x <= maxx &&
		y >= miny &&
		y <= maxy;
}


/*public*/
bool
Envelope::covers(const Envelope& other) const
{
	if (isNull() || other.isNull()) return false;

	return
		other.getMinX() >= minx &&
		other.getMaxX() <= maxx &&
		other.getMinY() >= miny &&
		other.getMaxY() <= maxy;
}




/*public*/
bool
Envelope::equals(const Envelope* other) const
{
	if (isNull()) return other->isNull();
	return  other->getMinX() == minx &&
			other->getMaxX() == maxx &&
			other->getMinY() == miny &&
			other->getMaxY() == maxy;
}

/* public */
std::ostream& operator<< (std::ostream& os, const Envelope& o)
{
	os << "Env[" << o.minx << ":" << o.maxx << ","
		 << o.miny << ":" << o.maxy << "]";
	return os;
}


/*public*/
string
Envelope::toString() const
{
	ostringstream s;
	s<<*this;
	return s.str();
}

/*public*/
double
Envelope::distance(const Envelope* env) const
{
	if (intersects(env)) return 0;
	double dx=0.0;
	if(maxx<env->minx) dx=env->minx-maxx;
	if(minx>env->maxx) dx=minx-env->maxx;
	double dy=0.0;
	if(maxy<env->miny) dy=env->miny-maxy;
	if(miny>env->maxy) dy=miny-env->maxy;
	// if either is zero, the envelopes overlap either vertically or horizontally
	if (dx==0.0) return dy;
	if (dy==0.0) return dx;
	return sqrt(dx*dx+dy*dy);
}

/*public*/
bool
operator==(const Envelope& a, const Envelope& b)
{
	if (a.isNull()) {
		return b.isNull();
	}
	if (b.isNull()) {
		return a.isNull();
	}
	return a.getMaxX() == b.getMaxX() &&
		   a.getMaxY() == b.getMaxY() &&
		   a.getMinX() == b.getMinX() &&
		   a.getMinY() == b.getMinY();
}

/*public*/
int
Envelope::hashCode() const
{
	//Algorithm from Effective Java by Joshua Bloch [Jon Aquino]
	int result = 17;
	result = 37 * result + Coordinate::hashCode(minx);
	result = 37 * result + Coordinate::hashCode(maxx);
	result = 37 * result + Coordinate::hashCode(miny);
	result = 37 * result + Coordinate::hashCode(maxy);
	return result;
}

/*public static*/
vector<string>
Envelope::split(const string &str, const string &delimiters)
{
  vector<string> tokens;

  // Find first "non-delimiter".
  string::size_type lastPos = 0;
  string::size_type pos = str.find_first_of(delimiters, lastPos);

  while (string::npos != pos || string::npos != lastPos)
  {
    // Found a token, add it to the vector.
    tokens.push_back(str.substr(lastPos, pos - lastPos));

    // Skip delimiters.  Note the "not_of"
    lastPos = str.find_first_not_of(delimiters, pos);

    // Find next "non-delimiter"
    pos = str.find_first_of(delimiters, lastPos);
  }

  return tokens;
}

/*public*/
bool
Envelope::centre(Coordinate& centre) const
{
	if (isNull()) return false;
	centre.x=(getMinX() + getMaxX()) / 2.0;
	centre.y=(getMinY() + getMaxY()) / 2.0;
	return true;
}

/*public*/
bool
Envelope::intersection(const Envelope& env, Envelope& result) const
{
	if (isNull() || env.isNull() || ! intersects(env)) return false;

	double intMinX = minx > env.minx ? minx : env.minx;
	double intMinY = miny > env.miny ? miny : env.miny;
	double intMaxX = maxx < env.maxx ? maxx : env.maxx;
	double intMaxY = maxy < env.maxy ? maxy : env.maxy;
	result.init(intMinX, intMaxX, intMinY, intMaxY);
	return true;
}

/*public*/
void
Envelope::translate(double transX, double transY)
{
	if (isNull()) return;
	init(getMinX() + transX, getMaxX() + transX,
		getMinY() + transY, getMaxY() + transY);
}


/*public*/
void
Envelope::expandBy(double deltaX, double deltaY)
{
	if (isNull()) return;

	minx -= deltaX;
	maxx += deltaX;
	miny -= deltaY;
	maxy += deltaY;

	// check for envelope disappearing
	if (minx > maxx || miny > maxy)
		setToNull();
}

/*public*/
Envelope&
Envelope::operator=(const Envelope& e)
{
#if GEOS_DEBUG
	std::cerr<<"Envelope assignment"<<std::endl;
#endif
	if ( &e != this ) // is this check useful ?
	{
		minx=e.minx;
		maxx=e.maxx;
		miny=e.miny;
		maxy=e.maxy;
	}
	return *this;
}


} // namespace geos::geom
} // namespace geos
