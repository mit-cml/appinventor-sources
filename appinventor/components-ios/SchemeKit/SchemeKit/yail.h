//
//  yail.h
//  SchemeKit
//
//  Created by Evan Patton on 10/13/16.
//  Copyright © 2016 MIT Center for Mobile Learning. All rights reserved.
//

#ifndef yail_h
#define yail_h

/**
 *  Attempts to discover a native symbol from the Objective-C/Swift runtime and register it
 *  for the given symbol identified by uid.
 *
 *  @param pic picrin state. if a symbol is found, it will be registered as a global in the state's
 *  symbol table.
 *  @param uid symbolic identifier for the candidate method. For exanple, MockComponent:make
 *
 *  @return 0 if a match for the symbol identified by uid is found, otherwise 1. The global symbol
 *  table of pic will be updated if successful.
 */
int yail_resolve_native_symbol(pic_state *pic, pic_value uid);

#endif /* yail_h */
