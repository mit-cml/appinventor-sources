// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#import <Foundation/Foundation.h>

typedef struct pic_state pic_state;
typedef uint64_t pic_value;

@protocol ASTRecursiveVisitorDelegate <NSObject>

@optional
- (BOOL)visitForm:(pic_value)aForm withState:(pic_state *)pic;
- (BOOL)visitFloat:(pic_value)aFloat withState:(pic_state *)pic;
- (BOOL)visitInteger:(pic_value)aInt withState:(pic_state *)pic;
- (BOOL)visitChar:(pic_value)aChar withState:(pic_state *)pic;
- (BOOL)visitBoolean:(pic_value)boolean withState:(pic_state *)pic;
- (BOOL)visitNil:(pic_value)aNil withState:(pic_state *)pic;
- (BOOL)visitString:(pic_value)string withState:(pic_state *)pic;
- (BOOL)visitVector:(pic_value)aVector withState:(pic_state *)pic;
- (BOOL)visitID:(pic_value)anID withState:(pic_state *)pic;
- (BOOL)visitSymbol:(pic_value)symbol withState:(pic_state *)pic;

/*
enum {
  PIC_TYPE_INVALID = 1,
  PIC_TYPE_FLOAT   = 2,
  PIC_TYPE_INT     = 3,
  PIC_TYPE_CHAR    = 4,
  PIC_TYPE_EOF     = 5,
  PIC_TYPE_UNDEF   = 6,
  PIC_TYPE_TRUE    = 8,
  PIC_TYPE_NIL     = 7,
  PIC_TYPE_FALSE   = 9,
  PIC_IVAL_END     = 10,
  / * -------------------- * /
  PIC_TYPE_STRING  = 16,
  PIC_TYPE_VECTOR  = 17,
  PIC_TYPE_BLOB    = 18,
  PIC_TYPE_PORT    = 20,
  PIC_TYPE_ERROR   = 21,
  PIC_TYPE_ID      = 22,
  PIC_TYPE_ENV     = 23,
  PIC_TYPE_DATA    = 24,
  PIC_TYPE_DICT    = 25,
  PIC_TYPE_WEAK    = 26,
  PIC_TYPE_RECORD  = 27,
  PIC_TYPE_SYMBOL  = 28,
  PIC_TYPE_PAIR    = 29,
  PIC_TYPE_CXT     = 30,
  PIC_TYPE_CP      = 31,
  PIC_TYPE_FUNC    = 32,
  PIC_TYPE_IREP    = 33
};
*/

@end

@interface ASTRecursiveVisitor : NSObject

- (instancetype)initWithPicState:(pic_state *)pic;
- (BOOL)visit:(pic_value)form;

@property (assign) id<ASTRecursiveVisitorDelegate> delegate;

@end
