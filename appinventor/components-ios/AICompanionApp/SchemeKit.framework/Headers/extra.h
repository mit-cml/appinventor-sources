/**
 * See Copyright Notice in picrin.h
 */

#ifndef PICRIN_EXTRA_H
#define PICRIN_EXTRA_H

#if defined(__cplusplus)
extern "C" {
#endif


#if PIC_USE_LIBC
void *pic_default_allocf(void *, void *, size_t);
#endif

pic_value pic_read(pic_state *, pic_value port);
pic_value pic_read_cstr(pic_state *, const char *);

pic_value pic_expand(pic_state *, pic_value program, pic_value env);
pic_value pic_eval(pic_state *, pic_value program, const char *lib);

void pic_load(pic_state *, pic_value port);
void pic_load_cstr(pic_state *, const char *);

#define pic_stdin(pic) pic_funcall(pic, "picrin.base", "current-input-port", 0)
#define pic_stdout(pic) pic_funcall(pic, "picrin.base", "current-output-port", 0)
#define pic_stderr(pic) pic_funcall(pic, "picrin.base", "current-error-port", 0)

#if PIC_USE_STDIO
pic_value pic_fopen(pic_state *, FILE *, const char *mode);
#endif
pic_value pic_fmemopen(pic_state *, const char *buf, size_t len, const char *mode);
int pic_fgetbuf(pic_state *, pic_value port, const char **buf, int *len);

/* utility macros */

#define pic_for_each(var, list, it)                                     \
  for (it = (list); ! pic_nil_p(pic, it); it = pic_cdr(pic, it))        \
    if ((var = pic_car(pic, it)), true)

#define pic_push(pic, item, place) (place = pic_cons(pic, item, place))
#define pic_pop(pic, place) (place = pic_cdr(pic, place))

#define pic_void(pic, exec) pic_void_(pic, PIC_GENSYM(ai), exec)
#define pic_void_(pic,ai,exec) do {             \
    size_t ai = pic_enter(pic);                 \
    exec;                                       \
    pic_leave(pic, ai);                         \
  } while (0)

#define pic_deflibrary(pic, lib) do {           \
    if (! pic_find_library(pic, lib)) {         \
      pic_make_library(pic, lib);               \
    }                                           \
    pic_in_library(pic, lib);                   \
  } while (0)

#define pic_try pic_try_(PIC_GENSYM(cont), PIC_GENSYM(jmp))
#define pic_try_(cont, jmp)                                             \
  do {                                                                  \
    extern pic_value pic_start_try(pic_state *, PIC_JMPBUF *);          \
    extern void pic_end_try(pic_state *, pic_value);                    \
    extern pic_value pic_err(pic_state *);                              \
    PIC_JMPBUF jmp;                                                     \
    if (PIC_SETJMP(pic, jmp) == 0) {                                    \
      pic_value pic_try_cookie_ = pic_start_try(pic, &jmp);
#define pic_catch(e) pic_catch_(e, PIC_GENSYM(label))
#define pic_catch_(e, label)                              \
      pic_end_try(pic, pic_try_cookie_);                  \
    } else {                                              \
      e = pic_err(pic);                                   \
      goto label;                                         \
    }                                                     \
  } while (0);                                            \
  if (0)                                                  \
  label:

/* for debug */

void pic_warnf(pic_state *, const char *, ...);
pic_value pic_get_backtrace(pic_state *);
#if PIC_USE_WRITE
void pic_print_error(pic_state *, pic_value port, pic_value err);
#endif

#if defined(__cplusplus)
}
#endif

#endif
