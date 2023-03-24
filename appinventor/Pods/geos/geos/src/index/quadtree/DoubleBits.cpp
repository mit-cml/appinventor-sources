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
 **********************************************************************
 *
 * Last port: index/quadtree/DoubleBits.java rev. 1.7 (JTS-1.10)
 *
 **********************************************************************/

#include <geos/index/quadtree/DoubleBits.h>
#include <geos/util/IllegalArgumentException.h>

#include <string>
#include <cstring>

#if __STDC_IEC_559__
#define ASSUME_IEEE_DOUBLE 1
#else
#define ASSUME_IEEE_DOUBLE 0
#endif

#if ! ASSUME_IEEE_DOUBLE
#include <cmath>
#endif

namespace geos {
namespace index { // geos.index
namespace quadtree { // geos.index.quadtree

using namespace std;

double
DoubleBits::powerOf2(int exp)
{
	if (exp>1023 || exp<-1022)
		throw util::IllegalArgumentException("Exponent out of bounds");
#if ASSUME_IEEE_DOUBLE
	int64 expBias=exp+EXPONENT_BIAS;
	int64 bits=expBias << 52;
	double ret;
	memcpy(&ret, &bits, sizeof(int64));
	return ret;
#else
	return pow(2.0, exp);
#endif
}

int
DoubleBits::exponent(double d)
{
	DoubleBits db(d);
	return db.getExponent();
}

double
DoubleBits::truncateToPowerOfTwo(double d)
{
	DoubleBits db(d);
	db.zeroLowerBits(52);
	return db.getDouble();
}

string DoubleBits::toBinaryString(double d) {
	DoubleBits db(d);
	return db.toString();
}

double
DoubleBits::maximumCommonMantissa(double d1, double d2)
{
	if (d1==0.0 || d2==0.0) return 0.0;
	DoubleBits db1(d1);
	DoubleBits db2(d2);
	if (db1.getExponent() != db2.getExponent()) return 0.0;
	int maxCommon=db1.numCommonMantissaBits(db2);
	db1.zeroLowerBits(64-(12+maxCommon));
	return db1.getDouble();
}

/*public*/
DoubleBits::DoubleBits(double nx)
{
#if ASSUME_IEEE_DOUBLE
	memcpy(&xBits,&nx,sizeof(double));
#endif
	x = nx;
}

/*public*/
double
DoubleBits::getDouble() const
{
	return x;
}

/*public*/
int64
DoubleBits::biasedExponent() const
{
	int64 signExp=xBits>>52;
	int64 exp=signExp&0x07ff;
	//cerr<<"xBits:"<<xBits<<" signExp:"<<signExp<<" exp:"<<exp<<endl;
	return exp;
}

/*public*/
int
DoubleBits::getExponent() const
{
#if ASSUME_IEEE_DOUBLE
	return static_cast<int>(biasedExponent() - EXPONENT_BIAS);
#else
	if ( x <= 0 ) return 0; // EDOM || ERANGE
	return (int)((log(x)/log(2.0))+(x<1?-0.9:0.00000000001));
#endif
}

/*public*/
void
DoubleBits::zeroLowerBits(int nBits)
{
	long invMask=(1L<<nBits)-1L;
	long mask=~invMask;
	xBits&=mask;
}

/*public*/
int
DoubleBits::getBit(int i) const
{
	long mask=(1L<<i);
	return (xBits&mask)!=0?1:0;
}

/*public*/
int
DoubleBits::numCommonMantissaBits(const DoubleBits& db) const
{
	for (int i=0;i<52;i++) {
		//int bitIndex=i+12;
		if (getBit(i)!=db.getBit(i))
			return i;
	}
	return 52;
}

/*public*/
string
DoubleBits::toString() const
{
    // TODO: Fix it!

    return "FIXME: unimplemented DoubleBits::toString()";

    //String numStr = Long.toBinaryString(xBits);
    //// 64 zeroes!
    //String zero64 = "0000000000000000000000000000000000000000000000000000000000000000";
    //String padStr =  zero64 + numStr;
    //String bitStr = padStr.substring(padStr.length() - 64);
    //String str = bitStr.substring(0, 1) + "  "
    //+ bitStr.substring(1, 12) + "(" + getExponent() + ") "
    //+ bitStr.substring(12)
    //+ " [ " + x + " ]";
    //return str;
}

} // namespace geos.index.quadtree
} // namespace geos.index
} // namespace geos
