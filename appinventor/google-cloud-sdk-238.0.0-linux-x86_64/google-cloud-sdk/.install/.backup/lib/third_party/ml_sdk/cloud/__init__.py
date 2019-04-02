"""Google Cloud namespace package."""

# GOOGLE_INTERNAL_BEGIN
# pylint: disable=g-import-not-at-top
__path__.insert(0, __path__[-1] + '/core_future')
__version__ = 'future'
try:
  __future = __import__(__name__, fromlist=['core_future']).core_future
except (AttributeError, ImportError):
# GOOGLE_INTERNAL_END

  try:
      import pkg_resources
      pkg_resources.declare_namespace(__name__)
  except ImportError:
      import pkgutil
      __path__ = pkgutil.extend_path(__path__, __name__)


# GOOGLE_INTERNAL_BEGIN
else:
  __all__ = getattr(__future, '__all__', None)
  locals().update({k: getattr(__future, k) for k in __all__ or []})

  import os
  import pkgutil
  pkg_path = os.path.dirname(__file__)
  if [name for _, name, is_pkg in pkgutil.iter_modules([pkg_path]) if not is_pkg]:
    raise RuntimeError('Conflicting versions of core found: {:core, :core_future}')
# GOOGLE_INTERNAL_END
