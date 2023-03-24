/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2012 Excensus LLC.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: triangulate/quadedge/QuadEdge.java r524
 *
 **********************************************************************/

#include <geos/triangulate/quadedge/QuadEdge.h>

namespace geos {
namespace triangulate { //geos.triangulate
namespace quadedge { //geos.triangulate.quadedge

using namespace geos::geom;

std::unique_ptr<QuadEdge>
QuadEdge::makeEdge(const Vertex &o, const Vertex &d)
{
	QuadEdge *q0 = new QuadEdge();
	//q1-q3 are free()'d by q0
	QuadEdge *q1 = new QuadEdge();
	QuadEdge *q2 = new QuadEdge();
	QuadEdge *q3 = new QuadEdge();

	q0->_rot = q1;
	q1->_rot = q2;
	q2->_rot = q3;
	q3->_rot = q0;

	q0->setNext(q0);
	q1->setNext(q3);
	q2->setNext(q2);
	q3->setNext(q1);

	QuadEdge *base = q0;
	base->setOrig(o);
	base->setDest(d);

	return std::unique_ptr<QuadEdge>(base);
}

std::unique_ptr<QuadEdge>
QuadEdge::connect(QuadEdge &a, QuadEdge &b)
{
	std::unique_ptr<QuadEdge> q0 = makeEdge(a.dest(), b.orig());
	splice(*q0, a.lNext());
	splice(q0->sym(), b);
	return q0;
}

void
QuadEdge::splice(QuadEdge &a, QuadEdge &b)
{
	QuadEdge &alpha = a.oNext().rot();
	QuadEdge &beta = b.oNext().rot();

	QuadEdge &t1 = b.oNext();
	QuadEdge &t2 = a.oNext();
	QuadEdge &t3 = beta.oNext();
	QuadEdge &t4 = alpha.oNext();

	a.setNext(&t1);
	b.setNext(&t2);
	alpha.setNext(&t3);
	beta.setNext(&t4);
}

void
QuadEdge::swap(QuadEdge &e)
{
	QuadEdge &a = e.oPrev();
	QuadEdge &b = e.sym().oPrev();
	splice(e, a);
	splice(e.sym(), b);
	splice(e, a.lNext());
	splice(e.sym(), b.lNext());
	e.setOrig(a.dest());
	e.setDest(b.dest());
}

QuadEdge::QuadEdge() : _rot(nullptr), vertex(), next(nullptr), data(nullptr), isAlive(true)
{ }

QuadEdge::~QuadEdge()
{
}

void
QuadEdge::free()
{
	if(_rot)
	{
		if(_rot->_rot)
		{
			if(_rot->_rot->_rot)
			{
				delete _rot->_rot->_rot;
				_rot->_rot->_rot = nullptr;
			}
			delete _rot->_rot;
			_rot->_rot = nullptr;
		}
		delete _rot;
		_rot = nullptr;
	}
}

const QuadEdge&
QuadEdge::getPrimary() const
{
	if (orig().getCoordinate().compareTo(dest().getCoordinate()) <= 0)
		return *this;
	else
		return sym();
}

void
QuadEdge::setData(void* data)
{
	this->data = data;
}

void*
QuadEdge::getData()
{
	return data;
}

void
QuadEdge::remove()
{
	rot().rot().rot().isAlive = false;
	rot().rot().isAlive = false;
	rot().isAlive = false;
	isAlive = false;
}

bool
QuadEdge::equalsNonOriented(const QuadEdge &qe) const
{
	if (equalsOriented(qe))
		return true;
	if (equalsOriented(qe.sym()))
		return true;
	return false;
}

bool
QuadEdge::equalsOriented(const QuadEdge &qe) const
{
	if (orig().getCoordinate().equals2D(qe.orig().getCoordinate())
			&& dest().getCoordinate().equals2D(qe.dest().getCoordinate()))
		return true;
	return false;
}

std::unique_ptr<LineSegment>
QuadEdge::toLineSegment() const
{
	return std::unique_ptr<geom::LineSegment>(
			new geom::LineSegment(vertex.getCoordinate(), dest().getCoordinate()));
}

} //namespace geos.triangulate.quadedge
} //namespace geos.triangulate
} //namespace goes

