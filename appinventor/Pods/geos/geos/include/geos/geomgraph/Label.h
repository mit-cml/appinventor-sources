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
 * Last port: geomgraph/Label.java r428 (JTS-1.12+)
 *
 **********************************************************************/


#ifndef GEOS_GEOMGRAPH_LABEL_H
#define GEOS_GEOMGRAPH_LABEL_H

#include <geos/export.h>
#include <geos/geomgraph/TopologyLocation.h>

#include <geos/inline.h>

#include <iosfwd> // for operator<<

namespace geos {
namespace geomgraph { // geos.geomgraph

/** \brief
 * A <code>Label</code> indicates the topological relationship of a component
 * of a topology graph to a given <code>Geometry</code>.
 * This class supports labels for relationships to two <code>Geometry</code>s,
 * which is sufficient for algorithms for binary operations.
 *
 * Topology graphs support the concept of labeling nodes and edges in the graph.
 * The label of a node or edge specifies its topological relationship to one or
 * more geometries.  (In fact, since JTS operations have only two arguments labels
 * are required for only two geometries).  A label for a node or edge has one or
 * two elements, depending on whether the node or edge occurs in one or both of the
 * input <code>Geometry</code>s.  Elements contain attributes which categorize the
 * topological location of the node or edge relative to the parent
 * <code>Geometry</code>; that is, whether the node or edge is in the interior,
 * boundary or exterior of the <code>Geometry</code>.  Attributes have a value
 * from the set <code>{Interior, Boundary, Exterior}</code>.  In a node each
 * element has  a single attribute <code>&lt;On&gt;</code>.  For an edge each element has a
 * triplet of attributes <code>&lt;Left, On, Right&gt;</code>.
 *
 * It is up to the client code to associate the 0 and 1 <code>TopologyLocation</code>s
 * with specific geometries.
 *
 */
class GEOS_DLL Label {

public:

	friend std::ostream& operator<< (std::ostream&, const Label&);

	/** \brief
	 * Converts a Label to a Line label
	 * (that is, one with no side Locations)
	 *
	 */
	static Label toLineLabel(const Label& label);

	/** \brief
	 * Construct a Label with a single location for both Geometries.
	 */
	Label(int onLoc);

	/** \brief
	 * Construct a Label with the location specified
	 * for the given Geometry.
	 *
	 * Other geometry location will be set to
	 * Location::UNDEF.
	 */
	Label(int geomIndex, int onLoc);

	/** \brief
	 * Construct a Label with On, Left and Right locations for both Geometries.
	 *
	 * Initialize the locations for both Geometries to the given values.
	 */
	Label(int onLoc, int leftLoc, int rightLoc);

	/// Copy ctor
	Label(const Label &l);

	Label& operator= (const Label& l);

	/** \brief
	 * Initialize both locations to Location::UNDEF
	 *
	 * isNull() should return true after this kind of construction
	 */
	Label();

	/** \brief
	 * Construct a Label with On, Left and Right locations for the
	 * given Geometries.
	 * Initialize the locations for the other Geometry to
	 * Location::UNDEF
	 */
	Label(int geomIndex, int onLoc, int leftLoc, int rightLoc);

	void flip();

	int getLocation(int geomIndex, int posIndex) const;

	int getLocation(int geomIndex) const;

	void setLocation(int geomIndex, int posIndex, int location);

	void setLocation(int geomIndex, int location);

	void setAllLocations(int geomIndex, int location);

	void setAllLocationsIfNull(int geomIndex, int location);

	void setAllLocationsIfNull(int location);

	/** \brief
	 * Merge this label with another one.
	 *
	 * Merging updates any null attributes of this label with the attributes
	 * from lbl
	 */
	void merge(const Label &lbl);

	int getGeometryCount() const;

	bool isNull() const;

	bool isNull(int geomIndex) const;

	bool isAnyNull(int geomIndex) const;

	bool isArea() const;

	bool isArea(int geomIndex) const;

	bool isLine(int geomIndex) const;

	bool isEqualOnSide(const Label &lbl, int side) const;

	bool allPositionsEqual(int geomIndex, int loc) const;

	/** \brief
	 * Converts one GeometryLocation to a Line location
	 */
	void toLine(int geomIndex);

	std::string toString() const;

private:

	TopologyLocation elt[2];
};

std::ostream& operator<< (std::ostream&, const Label&);

} // namespace geos.geomgraph
} // namespace geos


//#ifdef GEOS_INLINE
//# include "geos/geomgraph/Label.inl"
//#endif

#endif // ifndef GEOS_GEOMGRAPH_LABEL_H

