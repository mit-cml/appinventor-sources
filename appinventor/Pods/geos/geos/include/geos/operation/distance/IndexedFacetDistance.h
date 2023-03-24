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
 * Last port: operation/distance/IndexedFacetDistance.java (f6187ee2 JTS-1.14)
 *
 **********************************************************************/

#ifndef GEOS_INDEXEDFACETDISTANCE_H
#define GEOS_INDEXEDFACETDISTANCE_H

#include <geos/operation/distance/FacetSequenceTreeBuilder.h>

namespace geos {
    namespace operation {
        namespace distance {
            class GEOS_DLL IndexedFacetDistance {
            public:
                IndexedFacetDistance(const geom::Geometry * g) :
                        cachedTree(FacetSequenceTreeBuilder::build(g))
                        {}

                static double distance(const geom::Geometry * g1, const geom::Geometry * g2);

                double getDistance(const geom::Geometry * g) const;

                ~IndexedFacetDistance();

            private:
                std::unique_ptr<geos::index::strtree::STRtree> cachedTree;

            };
        }
    }
}

#endif //GEOS_INDEXEDFACETDISTANCE_H
