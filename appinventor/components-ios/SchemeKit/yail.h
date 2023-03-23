// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#ifndef yail_h
#define yail_h

#import <Foundation/Foundation.h>

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

/**
 * Sets the time zone to use for rendering NSDate objects. The device's current time zone will be
 * used by default. This can be used to force the time zone to be fixed for consistency in testing.
 *
 * @param tz The time zone to be used for NSDate rendering.
 */
void yail_set_time_zone(NSTimeZone *tz);


@class YailList;
@class YailDictionary;
@class SCMValue;
@class SCMMethod;

/**
 * Returns the Objective-C class object pointed to by the given pic_value.
 *
 * @param pic The picrin state used for reporting errors
 * @param o The opaque Scheme value representing an Objective-C class
 */
Class *yail_native_class_objc(pic_state *pic, pic_value o);

/**
 * Returns the SCMMethod object pointed to by the given pic_value.
 *
 * @param pic The picrin state used for reporting errors
 * @param o The opaque Scheme value representing a SCMMethod instance
 */
SCMMethod *yail_native_method_objc(pic_state *pic, pic_value o);

/**
 * Unwraps an Objective-C object from an opaque Scheme value.
 *
 * @param pic The picrin state used for reporting errors
 * @param o The opaque Scheme value representing an Objective-C object
 */
id yail_native_instance_objc(pic_state *pic, pic_value o);

/**
 * Returns the SCMValue object pointed to by the given pic_value.
 *
 * @param pic The picrin state used for reporting errors
 * @param o The opaque Scheme value representing a SCMValue instance
 */
SCMValue *yail_scmvalue_objc(pic_state *pic, pic_value o);

/**
 * Returns the YailList object pointed to by the given pic_value.
 *
 * @param pic The picrin state used for reporting errors
 * @param o The opaque Scheme value representing a YailList instance
 */
YailList *yail_list_objc(pic_state *pic, pic_value o);

/**
 * Returns the YailDictionary object pointed to by the given pic_value.
 *
 * @param pic The picrin state used for reporting errors
 * @param o The opaque Scheme value representing a YailDictionary instance
 */
YailDictionary *yail_dict_objc(pic_state *pic, pic_value o);

#ifdef MEMDEBUG

/**
 * Gets the corresponding Scheme value for the @c object if the object is
 * present in the system. If the @c object is not already present, the
 * function returns nil.
 *
 * @param pic the picrin state
 * @param object an Objective-C object to look up
 * @return the corresponding Scheme value, or nil if the object isn't present
 */
pic_value yail_get_native_instance(pic_state *pic, id object);

#endif

#endif /* yail_h */
