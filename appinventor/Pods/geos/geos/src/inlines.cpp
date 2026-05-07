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
 * This file is here to make all inlined functions also
 * available as non-inlines when building with GEOS_INLINES defined.
 *
 **********************************************************************/


// Only do something if GEOS_INLINE is defined
// Otherwise we'll end up with duplicated symbols
#ifdef GEOS_INLINE

// If using Visual C++ with GEOS_INLINE, do not build inline.obj
// otherwise linker will complain "multiple definition" errors.
// If using MingW with GEOS_INLINE to build a DLL then MingW's gcc
// has already generated the stubs for the contents of this file.
// Hence we need to supress it to avoid "multiple definition" errors
// during the final link phase
#if !defined(_MSC_VER) && (!defined(__MINGW32__) || defined(__MINGW32__) && !defined(GEOS_DLL_EXPORT) && !defined(DLL_EXPORT) )

// If using cygwin then we suppress the "multiple definition" errors by
// ignoring this section completely; the cygwin linker seems to handle
// the stubs correctly at link time by itself
#if !defined(__CYGWIN__)

// Undefine GEOS_INLINE so that .inl files
// will be ready for an implementation file
#undef GEOS_INLINE

#include <geos/inline.h>

#include <geos/io/WKTReader.inl>
#include <geos/io/ByteOrderDataInStream.inl>
#include <geos/operation/overlay/MinimalEdgeRing.inl>
#include <geos/geomgraph/DirectedEdge.inl>
#include <geos/geomgraph/GeometryGraph.inl>
#include <geos/algorithm/ConvexHull.inl>
#include <geos/geom/GeometryCollection.inl>
#include <geos/geom/LineSegment.inl>
#include <geos/geom/PrecisionModel.inl>
#include <geos/geom/Envelope.inl>
#include <geos/geom/Coordinate.inl>
#include <geos/geom/GeometryFactory.inl>
#include <geos/geom/MultiLineString.inl>
#include <geos/geom/MultiPolygon.inl>
#include <geos/geom/CoordinateArraySequenceFactory.inl>
#include <geos/noding/snapround/HotPixel.inl>
#include <geos/noding/MCIndexNoder.inl>

#endif // defined __CYGWIN__

#endif // defined __MINGW32__ and !defined GEOS_DLL_EXPORT

#endif // defined GEOS_INLINE
