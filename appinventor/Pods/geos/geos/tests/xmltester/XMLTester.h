/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2005 Refractions Research Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************/

#ifndef GEOS_XMLTESTER_H
#define GEOS_XMLTESTER_H

#include <geos/geom/GeometryFactory.h>
#include <geos/geom/PrecisionModel.h>
#include <geos/profiler.h>
#include "tinyxml/tinyxml.h"

using namespace geos;

class XMLTester {

private:
	enum {
		SHOW_RUN_INFO=1,
		SHOW_CASE,
		SHOW_TEST,
		SHOW_RESULT,
		SHOW_GEOMS,
		SHOW_GEOMS_FULL,
		PRED
	};

	void parsePrecisionModel(const TiXmlElement* el);
	void parseRun(const TiXmlNode* node);
	void parseCase(const TiXmlNode* node);
	void parseTest(const TiXmlNode* node);
	void runPredicates(const geom::Geometry *a, const geom::Geometry *b);
	geom::Geometry *parseGeometry(const std::string &in, const char* label="parsed");
	static std::string trimBlanks(const std::string &in);
	void printGeom(std::ostream& os, const geom::Geometry *g);
	std::string printGeom(const geom::Geometry *g);
	void printTest(bool success, const std::string& expected_result, const std::string& actual_result, const util::Profile&);

	geom::Geometry *gA;
	geom::Geometry *gB;
	geom::Geometry *gT;

	bool usePrepared;
	std::unique_ptr<geom::PrecisionModel> pm;
	geom::GeometryFactory::Ptr factory;
	std::unique_ptr<io::WKTReader> wktreader;
	std::unique_ptr<io::WKTWriter> wktwriter;
	std::unique_ptr<io::WKBReader> wkbreader;
	std::unique_ptr<io::WKBWriter> wkbwriter;
	TiXmlDocument xml;

	int verbose;
	int test_predicates;

	int failed;
	int succeeded;
	int caseCount;
	int testCount;
	std::string opSignature;

	int testFileCount;
	int totalTestCount;

	const std::string *curr_file;
	std::string curr_case_desc;

	bool testValidOutput;
	bool testValidInput;
	bool sqlOutput;
	bool HEXWKB_output;

	bool testValid(const geom::Geometry* g, const std::string& label);

public:
	XMLTester();
	~XMLTester();
	void run(const std::string &testFile);
	void resultSummary(std::ostream &os) const;
	void resetCounters();

	/*
	 * Values:
	 *	0: Show case description, run tests, show result
	 *	1: Show parsed geometry values
	 *	2: Run predicates
	 *
	 * Return previously set verbosity level
	 */
	int setVerbosityLevel(int val);

	int getFailuresCount() { return failed; }

	void testOutputValidity(bool val) { testValidOutput=val; }
	void testInputValidity(bool val) { testValidInput=val; }
	void setSQLOutput(bool val) { sqlOutput=val; }
	void setHEXWKBOutput(bool val) { HEXWKB_output=val; }

};

#endif // GEOS_XMLTESTER_H
