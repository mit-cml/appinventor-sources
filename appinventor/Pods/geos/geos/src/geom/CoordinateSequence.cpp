/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2006 Refractions Research Inc.
 * Copyright (C) 2001-2002 Vivid Solutions Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************/

#include <geos/profiler.h>
#include <geos/geom/CoordinateSequence.h>
// FIXME: we should probably not be using CoordinateArraySequenceFactory
#include <geos/geom/CoordinateArraySequenceFactory.h>
#include <geos/geom/Coordinate.h>
#include <geos/geom/Envelope.h>

#include <cstdio>
#include <algorithm>
#include <vector>
#include <cassert>
#include <iterator>

using namespace std;

namespace geos {
namespace geom { // geos::geom

#if PROFILE
static Profiler *profiler = Profiler::instance();
#endif

bool
CoordinateSequence::hasRepeatedPoints() const
{
    const std::size_t size=getSize();
	for(std::size_t i=1; i<size; i++) {
		if (getAt(i-1)==getAt(i)) {
			return true;
		}
	}
	return false;
}

/*
 * Returns either the given coordinate array if its length is greater than the
 * given amount, or an empty coordinate array.
 */
CoordinateSequence *
CoordinateSequence::atLeastNCoordinatesOrNothing(size_t n,
		CoordinateSequence *c)
{
	if ( c->getSize() >= n )
	{
		return c;
	}
	else
	{
		// FIXME: return NULL rather then empty coordinate array
		return CoordinateArraySequenceFactory::instance()->create();
	}
}


bool
CoordinateSequence::hasRepeatedPoints(const CoordinateSequence *cl)
{
	const std::size_t size=cl->getSize();
	for(std::size_t i=1;i<size; i++) {
		if (cl->getAt(i-1)==cl->getAt(i)) {
			return true;
		}
	}
	return false;
}

const Coordinate*
CoordinateSequence::minCoordinate() const
{
	const Coordinate* minCoord=nullptr;
	const std::size_t size=getSize();
	for(std::size_t i=0; i<size; i++) {
		if(minCoord==nullptr || minCoord->compareTo(getAt(i))>0) {
			minCoord=&getAt(i);
		}
	}
	return minCoord;
}

const Coordinate*
CoordinateSequence::minCoordinate(CoordinateSequence *cl)
{
	const Coordinate* minCoord=nullptr;
	const std::size_t size=cl->getSize();
	for(std::size_t i=0;i<size; i++) {
		if(minCoord==nullptr || minCoord->compareTo(cl->getAt(i))>0) {
			minCoord=&(cl->getAt(i));
		}
	}
	return minCoord;
}

int
CoordinateSequence::indexOf(const Coordinate *coordinate,
		const CoordinateSequence *cl)
{
	size_t size=cl->getSize();
	for (size_t i=0; i<size; ++i)
	{
		if ((*coordinate)==cl->getAt(i))
		{
			return static_cast<int>(i); // FIXME: what if we overflow the int ?
		}
	}
	return -1;
}

void
CoordinateSequence::scroll(CoordinateSequence* cl,
		const Coordinate* firstCoordinate)
{
	// FIXME: use a standard algorithm instead
	std::size_t i, j=0;
	std::size_t ind=indexOf(firstCoordinate,cl);
	if (ind<1)
        return; // not found or already first

	const std::size_t length=cl->getSize();
	vector<Coordinate> v(length);
	for (i=ind; i<length; i++) {
		v[j++]=cl->getAt(i);
	}
	for (i=0; i<ind; i++) {
		v[j++]=cl->getAt(i);
	}
	cl->setPoints(v);
}

int
CoordinateSequence::increasingDirection(const CoordinateSequence& pts)
{
	size_t ptsize = pts.size();
	for (size_t i=0, n=ptsize/2; i<n; ++i)
	{
		size_t j = ptsize - 1 - i;
		// skip equal points on both ends
		int comp = pts[i].compareTo(pts[j]);
		if (comp != 0) return comp;
	}
	// array must be a palindrome - defined to be in positive direction
	return 1;
}

void
CoordinateSequence::reverse(CoordinateSequence *cl)
{

	// FIXME: use a standard algorithm
	int last = static_cast<int>(cl->getSize()) - 1;
	int mid=last/2;
	for(int i=0;i<=mid;i++) {
		const Coordinate tmp=cl->getAt(i);
		cl->setAt(cl->getAt(last-i),i);
		cl->setAt(tmp,last-i);
	}
}

bool
CoordinateSequence::equals(const CoordinateSequence *cl1,
		const CoordinateSequence *cl2)
{
	// FIXME: use std::equals()

	if (cl1==cl2) return true;
	if (cl1==nullptr||cl2==nullptr) return false;
	size_t npts1=cl1->getSize();
	if (npts1!=cl2->getSize()) return false;
	for (size_t i=0; i<npts1; i++) {
		if (!(cl1->getAt(i)==cl2->getAt(i))) return false;
	}
	return true;
}

/*public*/
void
CoordinateSequence::add(const vector<Coordinate>* vc, bool allowRepeated)
{
	assert(vc);
	for(size_t i=0; i<vc->size(); ++i)
	{
		add((*vc)[i], allowRepeated);
	}
}

/*public*/
void
CoordinateSequence::add(const Coordinate& c, bool allowRepeated)
{
	if (!allowRepeated) {
        std::size_t npts=getSize();
		if (npts>=1) {
			const Coordinate& last=getAt(npts-1);
			if (last.equals2D(c))
                return;
		}
	}
	add(c);
}

/* Here for backward compatibility */
//void
//CoordinateSequence::add(CoordinateSequence *cl, bool allowRepeated,
//		bool direction)
//{
//	add(cl, allowRepeated, direction);
//}

/*public*/
void
CoordinateSequence::add(const CoordinateSequence *cl,
		bool allowRepeated, bool direction)
{
	// FIXME:  don't rely on negative values for 'j' (the reverse case)

	const int npts = static_cast<int>(cl->getSize());
	if (direction) {
		for (int i=0; i<npts; i++) {
			add(cl->getAt(i), allowRepeated);
		}
	} else {
		for (int j=npts-1; j>=0; j--) {
			add(cl->getAt(j), allowRepeated);
		}
	}
}


/*public static*/
CoordinateSequence*
CoordinateSequence::removeRepeatedPoints(const CoordinateSequence *cl)
{
#if PROFILE
	static Profile *prof= profiler->get("CoordinateSequence::removeRepeatedPoints()");
	prof->start();
#endif
	const vector<Coordinate> *v=cl->toVector();

	vector<Coordinate> *nv=new vector<Coordinate>;
	nv->reserve(v->size());
	unique_copy(v->begin(), v->end(), back_inserter(*nv));
	CoordinateSequence* ret=CoordinateArraySequenceFactory::instance()->create(nv);

#if PROFILE
	prof->stop();
#endif
	return ret;
}

void
CoordinateSequence::expandEnvelope(Envelope &env) const
{
	const std::size_t size = getSize();
	for (std::size_t i=0; i<size; i++)
        env.expandToInclude(getAt(i));
}

std::ostream& operator<< (std::ostream& os, const CoordinateSequence& cs)
{
	os << "(";
	for (size_t i=0, n=cs.size(); i<n; ++i)
	{
		const Coordinate& c = cs[i];
		if ( i ) os << ", ";
		os << c;
	}
	os << ")";

	return os;
}

bool operator== ( const CoordinateSequence& s1, const CoordinateSequence& s2)
{
	return CoordinateSequence::equals(&s1, &s2);
}

bool operator!= ( const CoordinateSequence& s1, const CoordinateSequence& s2)
{
	return ! CoordinateSequence::equals(&s1, &s2);
}

} // namespace geos::geom
} // namespace geos
