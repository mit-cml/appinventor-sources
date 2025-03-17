/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2013 Sandro Santilli <strk@kbt.io>
 * Copyright (C) 2006 Refractions Research Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************
 *
 * Last port: ORIGINAL WORK
 *
 **********************************************************************
 *
 * This file provides a single templated function, taking two
 * const Geometry pointers, applying a binary operator to them
 * and returning a result Geometry in an unique_ptr<>.
 *
 * The binary operator is expected to take two const Geometry pointers
 * and return a newly allocated Geometry pointer, possibly throwing
 * a TopologyException to signal it couldn't succeed due to robustness
 * issues.
 *
 * This function will catch TopologyExceptions and try again with
 * slightly modified versions of the input. The following heuristic
 * is used:
 *
 *	- Try with original input.
 *	- Try removing common bits from input coordinate values
 *	- Try snaping input geometries to each other
 *	- Try snaping input coordinates to a increasing grid (size from 1/25 to 1)
 *	- Try simplifiying input with increasing tolerance (from 0.01 to 0.04)
 *
 * If none of the step succeeds the original exception is thrown.
 *
 * Note that you can skip Grid snapping, Geometry snapping and Simplify policies
 * by a compile-time define when building geos.
 * See USE_TP_SIMPLIFY_POLICY, USE_PRECISION_REDUCTION_POLICY and
 * USE_SNAPPING_POLICY macros below.
 *
 *
 **********************************************************************/

#ifndef GEOS_GEOM_BINARYOP_H
#define GEOS_GEOM_BINARYOP_H

#include <geos/algorithm/BoundaryNodeRule.h>
#include <geos/geom/Geometry.h>
#include <geos/geom/GeometryCollection.h>
#include <geos/geom/Polygon.h>
#include <geos/geom/Lineal.h>
#include <geos/geom/PrecisionModel.h>
#include <geos/geom/GeometryFactory.h>
#include <geos/precision/CommonBitsRemover.h>
#include <geos/precision/SimpleGeometryPrecisionReducer.h>
#include <geos/precision/GeometryPrecisionReducer.h>

#include <geos/operation/overlay/snap/GeometrySnapper.h>

#include <geos/simplify/TopologyPreservingSimplifier.h>
#include <geos/operation/IsSimpleOp.h>
#include <geos/operation/valid/IsValidOp.h>
#include <geos/operation/valid/TopologyValidationError.h>
#include <geos/util/TopologyException.h>
#include <geos/util.h>

#include <memory> // for unique_ptr

//#define GEOS_DEBUG_BINARYOP 1
#define GEOS_DEBUG_BINARYOP_PRINT_INVALID 1

#ifdef GEOS_DEBUG_BINARYOP
# include <iostream>
# include <iomanip>
# include <sstream>
#endif


/*
 * Always try original input first
 */
#ifndef USE_ORIGINAL_INPUT
# define USE_ORIGINAL_INPUT 1
#endif

/*
 * Check validity of operation between original geometries
 */
#define GEOS_CHECK_ORIGINAL_RESULT_VALIDITY 0


/*
 * Define this to use PrecisionReduction policy
 * in an attempt at by-passing binary operation
 * robustness problems (handles TopologyExceptions)
 */
#ifndef USE_PRECISION_REDUCTION_POLICY
# define USE_PRECISION_REDUCTION_POLICY 1
#endif

/*
 * Check validity of operation performed
 * by precision reduction policy.
 *
 * Precision reduction policy reduces precision of inputs
 * and restores it in the result. The restore phase may
 * introduce invalidities.
 *
 */
#define GEOS_CHECK_PRECISION_REDUCTION_VALIDITY 0

/*
 * Define this to use TopologyPreserving simplification policy
 * in an attempt at by-passing binary operation
 * robustness problems (handles TopologyExceptions)
 */
#ifndef USE_TP_SIMPLIFY_POLICY
//# define USE_TP_SIMPLIFY_POLICY 1
#endif

/*
 * Use common bits removal policy.
 * If enabled, this would be tried /before/
 * Geometry snapping.
 */
#ifndef USE_COMMONBITS_POLICY
# define USE_COMMONBITS_POLICY 1
#endif

/*
 * Check validity of operation performed
 * by common bits removal policy.
 *
 * This matches what EnhancedPrecisionOp does in JTS
 * and fixes 5 tests of invalid outputs in our testsuite
 * (stmlf-cases-20061020-invalid-output.xml)
 * and breaks 1 test (robustness-invalid-output.xml) so much
 * to prevent a result.
 *
 */
#define GEOS_CHECK_COMMONBITS_VALIDITY 1

/*
 * Use snapping policy
 */
#ifndef USE_SNAPPING_POLICY
# define USE_SNAPPING_POLICY 1
#endif

/* Remove common bits before snapping */
#ifndef CBR_BEFORE_SNAPPING
# define CBR_BEFORE_SNAPPING 1
#endif


/*
 * Check validity of result from SnapOp
 */
#define GEOS_CHECK_SNAPPINGOP_VALIDITY 0


namespace geos {
namespace geom { // geos::geom

inline bool
check_valid(const Geometry& g, const std::string& label, bool doThrow=false, bool validOnly=false)
{
  if ( dynamic_cast<const Lineal*>(&g) ) {
    if ( ! validOnly ) {
      operation::IsSimpleOp sop(g, algorithm::BoundaryNodeRule::getBoundaryEndPoint());
      if ( ! sop.isSimple() )
      {
        if ( doThrow ) {
          throw geos::util::TopologyException(
            label + " is not simple");
        }
        return false;
      }
    }
  } else {
    operation::valid::IsValidOp ivo(&g);
    if ( ! ivo.isValid() )
    {
      using operation::valid::TopologyValidationError;
      TopologyValidationError* err = ivo.getValidationError();
#ifdef GEOS_DEBUG_BINARYOP
      std::cerr << label << " is INVALID: "
        << err->toString()
        << " (" << std::setprecision(20)
        << err->getCoordinate() << ")"
        << std::endl
#ifdef GEOS_DEBUG_BINARYOP_PRINT_INVALID
        << "<A>" << std::endl
        << g.toString()
        << std::endl
        << "</A>" << std::endl
#endif
        ;
#endif
      if ( doThrow ) {
        throw geos::util::TopologyException(
          label + " is invalid: " + err->toString(),
                err->getCoordinate());
      }
      return false;
    }
  }
	return true;
}

/*
 * Attempt to fix noding of multilines and
 * self-intersection of multipolygons
 *
 * May return the input untouched.
 */
inline std::unique_ptr<Geometry>
fix_self_intersections(std::unique_ptr<Geometry> g, const std::string& label)
{
  ::geos::ignore_unused_variable_warning(label);
#ifdef GEOS_DEBUG_BINARYOP
	std::cerr << label << " fix_self_intersection (UnaryUnion)" << std::endl;
#endif

  // Only multi-components can be fixed by UnaryUnion
  if ( ! dynamic_cast<const GeometryCollection*>(g.get()) ) return g;

  using operation::valid::IsValidOp;

  IsValidOp ivo(g.get());

  // Polygon is valid, nothing to do
  if ( ivo.isValid() ) return g;

  // Not all invalidities can be fixed by this code

  using operation::valid::TopologyValidationError;
  TopologyValidationError* err = ivo.getValidationError();
  switch ( err->getErrorType() ) {
    case TopologyValidationError::eRingSelfIntersection:
    case TopologyValidationError::eTooFewPoints: // collapsed lines
#ifdef GEOS_DEBUG_BINARYOP
	    std::cerr << label << " ATTEMPT_TO_FIX: " << err->getErrorType() << ": " << *g << std::endl;
#endif
      g = g->Union();
#ifdef GEOS_DEBUG_BINARYOP
	    std::cerr << label << " ATTEMPT_TO_FIX succeeded.. " << std::endl;
#endif
      return g;
    case TopologyValidationError::eSelfIntersection:
      // this one is within a single component, won't be fixed
    default:
#ifdef GEOS_DEBUG_BINARYOP
	    std::cerr << label << " invalidity is: " << err->getErrorType() << std::endl;
#endif
      return g;
  }
}


/// \brief
/// Apply a binary operation to the given geometries
/// after snapping them to each other after common-bits
/// removal.
///
template <class BinOp>
std::unique_ptr<Geometry>
SnapOp(const Geometry* g0, const Geometry *g1, BinOp _Op)
{
	typedef std::unique_ptr<Geometry> GeomPtr;

	//using geos::precision::GeometrySnapper;
	using geos::operation::overlay::snap::GeometrySnapper;

	// Snap tolerance must be computed on the original
	// (not commonbits-removed) geoms
	double snapTolerance = GeometrySnapper::computeOverlaySnapTolerance(*g0, *g1);
#if GEOS_DEBUG_BINARYOP
	std::cerr<< std::setprecision(20) << "Computed snap tolerance: "<<snapTolerance<<std::endl;
#endif


#if CBR_BEFORE_SNAPPING
	// Compute common bits
	geos::precision::CommonBitsRemover cbr;
	cbr.add(g0); cbr.add(g1);
#if GEOS_DEBUG_BINARYOP
	std::cerr<<"Computed common bits: "<<cbr.getCommonCoordinate()<<std::endl;
#endif

	// Now remove common bits
	GeomPtr rG0( cbr.removeCommonBits(g0->clone()) );
	GeomPtr rG1( cbr.removeCommonBits(g1->clone()) );

#if GEOS_DEBUG_BINARYOP
	check_valid(*rG0, "CBR: removed-bits geom 0");
	check_valid(*rG1, "CBR: removed-bits geom 1");
#endif

	const Geometry& operand0 = *rG0;
	const Geometry& operand1 = *rG1;
#else // don't CBR before snapping
	const Geometry& operand0 = *g0;
	const Geometry& operand1 = *g1;
#endif


	GeometrySnapper snapper0( operand0 );
	GeomPtr snapG0( snapper0.snapTo(operand1, snapTolerance) );
	//snapG0 = fix_self_intersections(snapG0, "SNAP: snapped geom 0");

	// NOTE: second geom is snapped on the snapped first one
	GeometrySnapper snapper1( operand1 );
	GeomPtr snapG1( snapper1.snapTo(*snapG0, snapTolerance) );
	//snapG1 = fix_self_intersections(snapG1, "SNAP: snapped geom 1");

	// Run the binary op
	GeomPtr result( _Op(snapG0.get(), snapG1.get()) );

#if GEOS_DEBUG_BINARYOP
	check_valid(*result, "SNAP: result (before common-bits addition");
#endif

#if CBR_BEFORE_SNAPPING
	// Add common bits back in
	cbr.addCommonBits( result.get() );
	//result = fix_self_intersections(result, "SNAP: result (after common-bits addition)");

  check_valid(*result, "CBR: result (after common-bits addition)", true);

#endif

	return result;
}

template <class BinOp>
std::unique_ptr<Geometry>
BinaryOp(const Geometry* g0, const Geometry *g1, BinOp _Op)
{
	typedef std::unique_ptr<Geometry> GeomPtr;

	GeomPtr ret;
	geos::util::TopologyException origException;

#ifdef USE_ORIGINAL_INPUT
	// Try with original input
	try
	{
#if GEOS_DEBUG_BINARYOP
		std::cerr << "Trying with original input." << std::endl;
#endif
		ret.reset(_Op(g0, g1));

#if GEOS_CHECK_ORIGINAL_RESULT_VALIDITY
		check_valid(*ret, "Overlay result between original inputs", true, true);
#endif

#if GEOS_DEBUG_BINARYOP
		std::cerr << "Attempt with original input succeeded" << std::endl;
#endif
		return ret;
	}
	catch (const geos::util::TopologyException& ex)
	{
		origException=ex;
#if GEOS_DEBUG_BINARYOP
		std::cerr << "Original exception: " << ex.what() << std::endl;
#endif
	}
#endif // USE_ORIGINAL_INPUT

	check_valid(*g0, "Input geom 0", true, true);
	check_valid(*g1, "Input geom 1", true, true);

#if USE_COMMONBITS_POLICY
	// Try removing common bits (possibly obsoleted by snapping below)
	//
	// NOTE: this policy was _later_ implemented
	//       in JTS as EnhancedPrecisionOp
	// TODO: consider using the now-ported EnhancedPrecisionOp
	//       here too
	//
	try
	{
		GeomPtr rG0;
		GeomPtr rG1;
		precision::CommonBitsRemover cbr;

#if GEOS_DEBUG_BINARYOP
		std::cerr << "Trying with Common Bits Remover (CBR)" << std::endl;
#endif

		cbr.add(g0);
		cbr.add(g1);

		rG0.reset( cbr.removeCommonBits(g0->clone()) );
		rG1.reset( cbr.removeCommonBits(g1->clone()) );

#if GEOS_DEBUG_BINARYOP
		check_valid(*rG0, "CBR: geom 0 (after common-bits removal)");
		check_valid(*rG1, "CBR: geom 1 (after common-bits removal)");
#endif

		ret.reset( _Op(rG0.get(), rG1.get()) );

#if GEOS_DEBUG_BINARYOP
		check_valid(*ret, "CBR: result (before common-bits addition)");
#endif

		cbr.addCommonBits( ret.get() );

#if GEOS_CHECK_COMMONBITS_VALIDITY
		check_valid(*ret, "CBR: result (after common-bits addition)", true);
#endif

#if GEOS_DEBUG_BINARYOP
		std::cerr << "Attempt with CBR succeeded" << std::endl;
#endif

		return ret;
	}
	catch (const geos::util::TopologyException& ex)
	{
        ::geos::ignore_unused_variable_warning(ex);
#if GEOS_DEBUG_BINARYOP
		std::cerr << "CBR: " << ex.what() << std::endl;
#endif
	}
#endif

	// Try with snapping
	//
	// TODO: possible optimization would be reusing the
	//       already common-bit-removed inputs and just
	//       apply geometry snapping, whereas the current
	//       SnapOp function does both.
// {
#if USE_SNAPPING_POLICY

#if GEOS_DEBUG_BINARYOP
	std::cerr << "Trying with snapping " << std::endl;
#endif

	try {
		ret = SnapOp(g0, g1, _Op);
#if GEOS_CHECK_SNAPPINGOP_VALIDITY
		check_valid(*ret, "SNAP: result", true, true);
#endif
#if GEOS_DEBUG_BINARYOP
		std::cerr << "SnapOp succeeded" << std::endl;
#endif
		return ret;

	}
	catch (const geos::util::TopologyException& ex)
	{
        ::geos::ignore_unused_variable_warning(ex);
#if GEOS_DEBUG_BINARYOP
		std::cerr << "SNAP: " << ex.what() << std::endl;
#endif
	}

#endif // USE_SNAPPING_POLICY }

// {
#if USE_PRECISION_REDUCTION_POLICY


	// Try reducing precision
	try
	{
		long unsigned int g0scale =
            static_cast<long unsigned int>(g0->getFactory()->getPrecisionModel()->getScale());
		long unsigned int g1scale =
            static_cast<long unsigned int>(g1->getFactory()->getPrecisionModel()->getScale());

#if GEOS_DEBUG_BINARYOP
		std::cerr << "Original input scales are: "
              << g0scale
              << " and "
              << g1scale
              << std::endl;
#endif

		double maxScale = 1e16;

    // Don't use a scale bigger than the input one
    if ( g0scale && static_cast<double>(g0scale) < maxScale ) maxScale = static_cast<double>(g0scale);
    if ( g1scale && static_cast<double>(g1scale) < maxScale ) maxScale = static_cast<double>(g1scale);


		for (double scale=maxScale; scale >= 1; scale /= 10)
		{
			PrecisionModel pm(scale);
			GeometryFactory::Ptr gf = GeometryFactory::create(&pm);
#if GEOS_DEBUG_BINARYOP
			std::cerr << "Trying with scale " << scale << std::endl;
#endif

			precision::GeometryPrecisionReducer reducer( *gf );
			GeomPtr rG0( reducer.reduce(*g0) );
			GeomPtr rG1( reducer.reduce(*g1) );

#if GEOS_DEBUG_BINARYOP
			check_valid(*rG0, "PR: geom 0 (after precision reduction)");
			check_valid(*rG1, "PR: geom 1 (after precision reduction)");
#endif

			try
			{
				ret.reset( _Op(rG0.get(), rG1.get()) );
        // restore original precision (least precision between inputs)
        if ( g0->getFactory()->getPrecisionModel()->compareTo( g1->getFactory()->getPrecisionModel() ) < 0 ) {
          ret.reset( g0->getFactory()->createGeometry(ret.get()) );
        }
        else {
          ret.reset( g1->getFactory()->createGeometry(ret.get()) );
        }

#if GEOS_CHECK_PRECISION_REDUCTION_VALIDITY
				check_valid(*ret, "PR: result (after restore of original precision)", true);
#endif

#if GEOS_DEBUG_BINARYOP
				std::cerr << "Attempt with scale " << scale << " succeeded" << std::endl;
#endif
				return ret;
			}
			catch (const geos::util::TopologyException& ex)
			{
#if GEOS_DEBUG_BINARYOP
				std::cerr << "Reduced with scale (" << scale << "): "
				          << ex.what() << std::endl;
#endif
				if ( scale == 1 ) throw ex;
			}

		}

	}
	catch (const geos::util::TopologyException& ex)
	{
#if GEOS_DEBUG_BINARYOP
		std::cerr << "Reduced: " << ex.what() << std::endl;
#endif
		::geos::ignore_unused_variable_warning(ex);
	}

#endif
// USE_PRECISION_REDUCTION_POLICY }





// {
#if USE_TP_SIMPLIFY_POLICY

	// Try simplifying
	try
	{

		double maxTolerance = 0.04;
		double minTolerance = 0.01;
		double tolStep = 0.01;

		for (double tol = minTolerance; tol <= maxTolerance; tol += tolStep)
		{
#if GEOS_DEBUG_BINARYOP
			std::cerr << "Trying simplifying with tolerance " << tol << std::endl;
#endif

			GeomPtr rG0( simplify::TopologyPreservingSimplifier::simplify(g0, tol) );
			GeomPtr rG1( simplify::TopologyPreservingSimplifier::simplify(g1, tol) );

			try
			{
				ret.reset( _Op(rG0.get(), rG1.get()) );
				return ret;
			}
			catch (const geos::util::TopologyException& ex)
			{
				if ( tol >= maxTolerance ) throw ex;
#if GEOS_DEBUG_BINARYOP
				std::cerr << "Simplified with tolerance (" << tol << "): "
				          << ex.what() << std::endl;
#endif
			}

		}

		return ret;

	}
	catch (const geos::util::TopologyException& ex)
	{
#if GEOS_DEBUG_BINARYOP
		std::cerr << "Simplified: " << ex.what() << std::endl;
#endif
	}

#endif
// USE_TP_SIMPLIFY_POLICY }

#if GEOS_DEBUG_BINARYOP
	std::cerr << "No attempts worked to union " << std::endl;
	std::cerr << "Input geometries:" << std::endl
	          << "<A>" << std::endl
					  << g0->toString() << std::endl
						<< "</A>" << std::endl
	          << "<B>" << std::endl
					  << g1->toString() << std::endl
						<< "</B>" << std::endl;
#endif

	throw origException;
}


} // namespace geos::geom
} // namespace geos

#endif // GEOS_GEOM_BINARYOP_H
