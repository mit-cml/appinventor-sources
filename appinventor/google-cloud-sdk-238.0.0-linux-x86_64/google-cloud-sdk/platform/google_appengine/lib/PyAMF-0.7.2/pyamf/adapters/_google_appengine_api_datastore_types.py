from google.appengine.api import datastore_types

import pyamf


class GeoPtStub(object):
    pass


class GeoPtClassAlias(pyamf.ClassAlias):
    def createInstance(self, codec=None):
        return GeoPtStub()

    def getCustomProperties(self):
        self.static_attrs.extend(['lat', 'lon'])
        self.sealed = True

    def applyAttributes(self, obj, attrs, codec=None):
        obj.__dict__.clear()
        obj.__class__ = datastore_types.GeoPt

        obj.__init__(lat=attrs['lat'], lon=attrs['lon'])


pyamf.register_alias_type(GeoPtClassAlias, datastore_types.GeoPt)
pyamf.register_class(
    datastore_types.GeoPt, 'google.appengine.api.datastore_types.GeoPt'
)
