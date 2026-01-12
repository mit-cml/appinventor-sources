/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2016 Daniel Baston
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: operation/distance/FacetSequence.java (f6187ee2 JTS-1.14)
 *
 **********************************************************************/

#include <geos/algorithm/CGAlgorithms.h>
#include <geos/operation/distance/FacetSequence.h>

using namespace geos::geom;
using namespace geos::operation::distance;
using namespace geos::algorithm;

FacetSequence::FacetSequence(const CoordinateSequence* pts, size_t start, size_t end) :
        pts(pts),
        start(start),
        end(end) {
    computeEnvelope();
}

size_t FacetSequence::size() const {
    return end - start;
}

bool FacetSequence::isPoint() const {
    return end - start == 1;
}

double FacetSequence::distance(const FacetSequence & facetSeq) const {
    bool isPointThis = isPoint();
    bool isPointOther = facetSeq.isPoint();

    if (isPointThis && isPointOther) {
        Coordinate pt = pts->getAt(start);
        Coordinate seqPt = facetSeq.pts->getAt(facetSeq.start);
        return pt.distance(seqPt);

    } else if (isPointThis) {
        Coordinate pt = pts->getAt(start);
        return computePointLineDistance(pt, facetSeq);
    } else if (isPointOther) {
        Coordinate seqPt = facetSeq.pts->getAt(facetSeq.start);
        return computePointLineDistance(seqPt, *this);
    }

    return computeLineLineDistance(facetSeq);
}

double FacetSequence::computePointLineDistance(const Coordinate & pt, const FacetSequence & facetSeq) const {
    double minDistance = std::numeric_limits<double>::infinity();
    double dist;
    Coordinate q0;
    Coordinate q1;

    for (size_t i = facetSeq.start; i < facetSeq.end - 1; i++) {
        facetSeq.pts->getAt(i, q0);
        facetSeq.pts->getAt(i + 1, q1);
        dist = CGAlgorithms::distancePointLine(pt, q0, q1);
        if (dist == 0.0)
            return dist;
        if (dist < minDistance)
            minDistance = dist;
    }

    return minDistance;
}

double FacetSequence::computeLineLineDistance(const FacetSequence & facetSeq) const {
    double minDistance = std::numeric_limits<double>::infinity();
    double dist;
    Coordinate p0, p1, q0, q1;

    for (size_t i = start; i < end - 1; i++) {
        pts->getAt(i, p0);
        pts->getAt(i + 1, p1);

        for (size_t j = facetSeq.start; j < facetSeq.end - 1; j++) {
            facetSeq.pts->getAt(j, q0);
            facetSeq.pts->getAt(j + 1, q1);

            dist = CGAlgorithms::distanceLineLine(p0, p1, q0, q1);
            if (dist == 0.0)
                return dist;
            if (dist < minDistance)
                minDistance = dist;
        }
    }

    return minDistance;
}

void FacetSequence::computeEnvelope() {
    env = Envelope();
    for (size_t i = start; i < end; i++) {
        env.expandToInclude(pts->getX(i), pts->getY(i));
    }
}

const Envelope * FacetSequence::getEnvelope() const {
    return &env;
}

const Coordinate * FacetSequence::getCoordinate(size_t index) const {
    return &(pts->getAt(start + index));
}

