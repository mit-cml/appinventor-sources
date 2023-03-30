/**********************************************************************
 *
 * GEOS - Geometry Engine Open Source
 * http://geos.osgeo.org
 *
 * Copyright (C) 2001-2002 Vivid Solutions Inc.
 *
 * This is free software; you can redistribute and/or modify it under
 * the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation.
 * See the COPYING file for more information.
 *
 **********************************************************************/


/*////////////////////////////////////////////////////////////////////////////
 *  Project:
 *    Memory_and_Exception_Trace
 *
 * ///////////////////////////////////////////////////////////////////////////
 *  File:
 *    Stackwalker.h
 *
 *  Remarks:
 *
 *
 *  Note:
 *
 *
 *  Author:
 *    Jochen Kalmbach
 *
 *////////////////////////////////////////////////////////////////////////////

#ifndef __STACKWALKER_H__
#define __STACKWALKER_H__

// Only valid in the following environment: Intel platform, MS VC++ 5/6/7
#ifndef _X86_
#error Only INTEL envirnoments are supported!
#endif

// Only MS VC++ 5 to 7
#if (_MSC_VER < 1100) || (_MSC_VER > 1300)
//#warning Only MS VC++ 5/6/7 supported. Check if the '_CrtMemBlockHeader' has not changed with this compiler!
#endif

typedef enum eAllocCheckOutput
{
  ACOutput_Simple,
  ACOutput_Advanced,
  ACOutput_XML
};

// Make extern "C", so it will also work with normal C-Programs
#ifdef __cplusplus
extern "C" {
#endif
extern int InitAllocCheckWN(eAllocCheckOutput eOutput, LPCTSTR pszFilename, ULONG ulShowStackAtAlloc = 0);
extern int InitAllocCheck(eAllocCheckOutput eOutput = ACOutput_Simple, BOOL bSetUnhandledExeptionFilter = TRUE, ULONG ulShowStackAtAlloc = 0);  // will create the filename by itself

extern ULONG DeInitAllocCheck();

extern DWORD StackwalkFilter( EXCEPTION_POINTERS *ep, DWORD status, LPCTSTR pszLogFile);

#ifdef __cplusplus
}
#endif

#endif  // __STACKWALKER_H__
