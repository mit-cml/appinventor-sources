// -*- mode: objc; c-basic-offset: 2; -*-
// Copyright 2016-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

#include <stdio.h>

extern int yylex(void);

int main(int argc, char *argv[]) {
  yylex();
  return 0;
}
