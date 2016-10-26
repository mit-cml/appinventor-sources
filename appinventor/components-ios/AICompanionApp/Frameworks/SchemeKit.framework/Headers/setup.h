/**
 * See Copyright Notice in picrin.h
 */

#ifdef STANDALONE
#include "picconf.h"
#else
#import <SchemeKit/picconf.h>
#endif

#ifndef PIC_USE_LIBC
# define PIC_USE_LIBC 1
#endif

#ifndef PIC_USE_STDIO
# define PIC_USE_STDIO 1
#endif

#ifndef PIC_USE_WRITE
# define PIC_USE_WRITE 1
#endif

#ifndef PIC_JMPBUF
# include <setjmp.h>
# define PIC_JMPBUF jmp_buf
#endif

#ifndef PIC_SETJMP
# include <setjmp.h>
# define PIC_SETJMP(pic, buf) setjmp(buf)
#endif

#ifndef PIC_LONGJMP
# include <setjmp.h>
# define PIC_LONGJMP(pic, buf, val) longjmp((buf), (val))
#endif

#ifndef PIC_ABORT
void abort(void);
# define PIC_ABORT(pic) abort()
#endif

#ifndef PIC_BUFSIZ
# define PIC_BUFSIZ 1024
#endif

#ifndef PIC_ARENA_SIZE
# define PIC_ARENA_SIZE (8 * 1024)
#endif

#ifndef PIC_HEAP_PAGE_SIZE
# define PIC_HEAP_PAGE_SIZE (4 * 1024 * 1024)
#endif

#ifndef PIC_PAGE_REQUEST_THRESHOLD
# define PIC_PAGE_REQUEST_THRESHOLD(total) ((total) * 77 / 100)
#endif

#ifndef PIC_STACK_SIZE
# define PIC_STACK_SIZE 2048
#endif

#ifndef PIC_RESCUE_SIZE
# define PIC_RESCUE_SIZE 30
#endif

#ifndef PIC_SYM_POOL_SIZE
# define PIC_SYM_POOL_SIZE (2 * 1024)
#endif

#ifndef PIC_IREP_SIZE
# define PIC_IREP_SIZE 8
#endif

#ifndef PIC_POOL_SIZE
# define PIC_POOL_SIZE 8
#endif

#ifndef PIC_SYMS_SIZE
# define PIC_SYMS_SIZE 32
#endif

#ifndef PIC_ISEQ_SIZE
# define PIC_ISEQ_SIZE 1024
#endif

/* check compatibility */

#if __STDC_VERSION__ >= 199901L
# include <stdbool.h>
#else
# define bool char
# define true 1
# define false 0
#endif

#if __STDC_VERSION__ >= 199901L
# include <stddef.h>
#elif ! defined(offsetof)
# define offsetof(s,m) ((size_t)(&(((s *)0)->m) - 0))
#endif

#if __STDC_VERSION__ >= 199901L
# include <stdint.h>
#else
# if INT_MAX > 2147483640L      /* imported from luaconf.h */
typedef int int32_t;
typedef unsigned int uint32_t;
# else
typedef long int32_t;
typedef unsigned long uint32_t;
# endif
#endif

//#if __STDC_VERSION__ >= 201112L
//# include <stdnoreturn.h>
//# define PIC_NORETURN noreturn
#if __GNUC__ || __clang__
# define PIC_NORETURN __attribute__((noreturn))
#else
# define PIC_NORETURN
#endif

#if __STDC_VERSION__ >= 199901L
# define PIC_INLINE static inline
#elif __GNUC__ || __clang__
# define PIC_INLINE static __inline__
#else
# define PIC_INLINE static
#endif

#if defined(__cplusplus)
# define PIC_UNUSED(v)
#elif __GNUC__ || __clang__
# define PIC_UNUSED(v) v __attribute__((unused))
#else
# define PIC_UNUSED(v) v
#endif

#define PIC_GENSYM2_(x,y) PIC_G##x##_##y##_
#define PIC_GENSYM1_(x,y) PIC_GENSYM2_(x,y)
#if defined(__COUNTER__)
# define PIC_GENSYM(x) PIC_GENSYM1_(__COUNTER__,x)
#else
# define PIC_GENSYM(x) PIC_GENSYM1_(__LINE__,x)
#endif

#if __GNUC__
# define GCC_VERSION (__GNUC__ * 10000 + __GNUC_MINOR__ * 100 + __GNUC_PATCHLEVEL__)
#endif
#if GCC_VERSION >= 40500 || __clang__
# define PIC_UNREACHABLE() (__builtin_unreachable())
#else
# define PIC_UNREACHABLE() (assert(false))
#endif
#if __GNUC__
# undef GCC_VERSION
#endif

#define PIC_SWAP(type,a,b) PIC_SWAP_HELPER_(type, PIC_GENSYM(tmp), a, b)
#define PIC_SWAP_HELPER_(type,tmp,a,b)          \
  do {                                          \
    type tmp = (a);                             \
    (a) = (b);                                  \
    (b) = tmp;                                  \
  } while (0)


#if PIC_USE_LIBC

#include <string.h>
#include <ctype.h>
#include <assert.h>
#include <stdlib.h>

#else

# define assert(v) (void)0

PIC_INLINE int
isspace(int c)
{
  return c == ' ' || c == '\t' || c == '\r' || c == '\v' || c == '\f' || c == '\n';
}

PIC_INLINE int
tolower(int c)
{
  return ('A' <= c && c <= 'Z') ? c - 'A' + 'a' : c;
}

PIC_INLINE int
isdigit(int c)
{
  return '0' <= c && c <= '9';
}

PIC_INLINE char *
strchr(const char *s, int c)
{
  do {
    if (*s == c)
      return (char *)s;
  } while (*s++ != '\0');
  return NULL;
}

PIC_INLINE size_t
strlen(const char *s)
{
  size_t l = 0;

  while (*s++) {
    l++;
  }
  return l;
}

PIC_INLINE int
strcmp(const char *s1, const char *s2)
{
  while (*s1 && *s1 == *s2) {
    s1++;
    s2++;
  }
  return (unsigned)*s1 - (unsigned)*s2;
}

PIC_INLINE long
strtol(const char *nptr, char **endptr, int base)
{
  long l = 0;
  char c;
  int n;

  while (1) {
    c = *nptr;
    if ('0' <= c && c <= '9')
      n = c - '0';
    else if ('a' <= c && c <= 'z')
      n = c - 'a' + 10;
    else if ('A' <= c && c <= 'Z')
      n = c - 'A' + 10;
    else
      goto exit;

    if (base <= n)
      goto exit;

    l = l * base + n;
    nptr++;
  }
 exit:
  if (endptr)
    *endptr = (char *)nptr;
  return l;
}

PIC_INLINE void *
memset(void *s, int n, size_t c)
{
  char *p = s;

  while (c-- > 0) {
    *p++ = n;
  }
  return s;
}

PIC_INLINE void *
memcpy(void *dst, const void *src, size_t n)
{
  const char *s = src;
  char *d = dst;

  while (n-- > 0) {
    *d++ = *s++;
  }
  return d;
}

PIC_INLINE void *
memmove(void *dst, const void *src, size_t n)
{
  const char *s = src;
  char *d = dst;

  if (d <= s || d >= s + n) {
    memcpy(dst, src, n);
  } else {
    s += n;
    d += n;
    while (n-- > 0) {
      *--d = *--s;
    }
  }
  return d;
}

PIC_INLINE int
memcmp(const void *b1, const void *b2, size_t n)
{
  const char *s1 = b1, *s2 = b2;

  while (*s1 == *s2 && n-- > 0) {
    s1++;
    s2++;
  }
  return (unsigned)*s1 - (unsigned)*s2;
}

PIC_INLINE char *
strcpy(char *dst, const char *src)
{
  char *d = dst;

  while ((*dst++ = *src++) != 0);

  return d;
}

PIC_INLINE double
atof(const char *nptr)
{
  int c;
  double f, g, h;
  int exp, s, i, e;
  unsigned u;

  /* note that picrin_read always assures that *nptr is a digit, never a '+' or '-' */
  /* in other words, the result of atof will always be positive */

  /* mantissa */
  /* pre '.'  */
  u = *nptr++ - '0';
  while (isdigit(c = *nptr)) {
    u = u * 10 + (*nptr++ - '0');
  }
  if (c == '.') {
    nptr++;
    /* after '.' */
    g = 0, e = 0;
    while (isdigit(c = *nptr)) {
      g = g * 10 + (*nptr++ - '0');
      e++;
    }
    h = 1.0;
    while (e-- > 0) {
      h /= 10;
    }
    f = u + g * h;
  }
  else {
    f = u;
  }
  /* suffix, i.e., exponent */
  s = 0;
  exp = 0;
  c = *nptr;

  if (c == 'e' && c == 'E') {
    nptr++;
    switch ((c = *nptr++)) {
    case '-':
      s = 1;
    case '+':
      c = *nptr++;
    default:
      exp = c - '0';
      while (isdigit(c = *nptr)) {
        exp = exp * 10 + (*nptr++ - '0');
      }
    }
  }
  e = 10;
  for (i = 0; exp; ++i) {
    if ((exp & 1) != 0) {
      f = s ? f / e : (f * e);
    }
    e *= e;
    exp >>= 1;
  }
  return f;
}

#endif

#if PIC_USE_STDIO
# include <stdio.h>

PIC_INLINE void
pic_dtoa(double dval, char *buf)
{
  sprintf(buf, "%g", dval);
}

#else

PIC_INLINE void
pic_dtoa(double dval, char *buf)
{
# define fabs(x) ((x) >= 0 ? (x) : -(x))
  long lval, tlval;
  int ival;
  int scnt, ecnt, cnt = 0;
  if (dval < 0) {
    dval = -dval;
    buf[cnt++] = '-';
  }
  lval = tlval = (long)dval;
  scnt = cnt;
  do {
    buf[cnt++] = '0' + (tlval % 10);
  } while ((tlval /= 10) != 0);
  ecnt = cnt;
  while (scnt < ecnt) {
    char c = buf[scnt];
    buf[scnt++] = buf[--ecnt];
    buf[ecnt] = c;
  }
  buf[cnt++] = '.';
  dval -= lval;
  if ((ival = fabs(dval) * 1e4 + 0.5) == 0) {
    buf[cnt++] = '0';
    buf[cnt++] = '0';
    buf[cnt++] = '0';
    buf[cnt++] = '0';
  } else {
    if (ival < 1000) buf[cnt++] = '0';
    if (ival <  100) buf[cnt++] = '0';
    if (ival <   10) buf[cnt++] = '0';
    scnt = cnt;
    do {
      buf[cnt++] = '0' + (ival % 10);
    } while ((ival /= 10) != 0);
    ecnt = cnt;
    while (scnt < ecnt) {
      char c = buf[scnt];
      buf[scnt++] = buf[--ecnt];
      buf[ecnt] = c;
    }
  }
  buf[cnt] = 0;
}

#endif

#ifndef PIC_DOUBLE_TO_CSTRING
#define PIC_DOUBLE_TO_CSTRING pic_dtoa
#endif
void PIC_DOUBLE_TO_CSTRING(double, char *);

#ifndef PIC_CSTRING_TO_DOUBLE
#define PIC_CSTRING_TO_DOUBLE atof
#endif
double PIC_CSTRING_TO_DOUBLE(const char *);

/* optional features available? */

#if (defined(__GNUC__) || defined(__clang__)) && ! defined(__STRICT_ANSI__)
# define PIC_DIRECT_THREADED_VM 1
#else
# define PIC_DIRECT_THREADED_VM 0
#endif

#if __x86_64__ && (defined(__GNUC__) || defined(__clang__)) && ! defined(__STRICT_ANSI__)
# define PIC_NAN_BOXING 1
#else
# define PIC_NAN_BOXING 0
#endif

#if PIC_USE_LIBC && (defined (__unix__) || (defined (__APPLE__) && defined (__MACH__)))
# include <unistd.h>
# define PIC_MEMALIGN(pic, buf, alignment, size) posix_memalign(buf, alignment, size)
# define PIC_BITMAP_GC 1
#endif
