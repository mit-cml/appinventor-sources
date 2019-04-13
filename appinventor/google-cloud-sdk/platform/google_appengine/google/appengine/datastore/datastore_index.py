#!/usr/bin/env python
#
# Copyright 2007 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
"""Primitives for dealing with datastore indexes.

Example index.yaml file:
------------------------

indexes:

- kind: Cat
  ancestor: no
  properties:
  - name: name
  - name: age
    direction: desc

- kind: Cat
  properties:
  - name: name
    direction: ascending
  - name: whiskers
    direction: descending

- kind: Store
  ancestor: yes
  properties:
  - name: business
    direction: asc
  - name: owner
    direction: asc

- kind: Mountain
  properties:
  - name: name
  - name: location
    mode: geospatial
"""








from __future__ import absolute_import

import google
from google.appengine._internal.ruamel import yaml

import copy
import itertools

from google.appengine.datastore import entity_pb

from google.appengine.api import appinfo
from google.appengine.api import datastore_types
from google.appengine.api import validation
from google.appengine.api import yaml_errors
from google.appengine.api import yaml_object
from google.appengine.datastore import datastore_pb








class Property(validation.Validated):
  """Representation for a property of an index as it appears in YAML.

  Attributes (all in string form):
    name: Name of attribute to sort by.
    direction: Direction of sort.
    mode: How the property is indexed. Either 'geospatial'
        or None (unspecified).
  """

  ATTRIBUTES = {
      'name': validation.Type(str, convert=False),
      'direction': validation.Optional([('asc', ('ascending',)),
                                        ('desc', ('descending',))]),
      'mode': validation.Optional(['geospatial'])
  }

  def IsAscending(self):






    return self.direction != 'desc'

  def CheckInitialized(self):
    if self.direction is not None and self.mode is not None:
      raise validation.ValidationError(
          'direction and mode are mutually exclusive')
    super(Property, self).CheckInitialized()


def PropertyPresenter(dumper, prop):
  """A PyYaml presenter for Property.

  It differs from the default by not outputting 'mode: null' and direction when
  mode is specified. This is done in order to ensure backwards compatibility.

  Args:
    dumper: the Dumper object provided by PyYaml.
    prop: the Property object to serialize.

  Returns:
    A PyYaml object mapping.
  """


  prop_copy = copy.copy(prop)


  if prop.mode is None:
    del prop_copy.mode

  if prop.direction is None:
    del prop_copy.direction

  return dumper.represent_object(prop_copy)


class Index(validation.Validated):
  """Individual index definition.

  Order of the properties determines a given index's sort priority.

  Attributes:
    kind: Datastore kind that index belongs to.
    ancestors: Include ancestors in index.
    properties: Properties to be included.
  """

  ATTRIBUTES = {
      'kind': validation.Type(str, convert=False),
      'ancestor': validation.Type(bool, convert=False, default=False),
      'properties': validation.Optional(validation.Repeated(Property)),
  }

  def CheckInitialized(self):
    self._Normalize()
    super(Index, self).CheckInitialized()

  def _Normalize(self):
    if self.properties is None:
      return
    is_geo = any(x.mode == 'geospatial' for x in self.properties)
    for prop in self.properties:
      if is_geo:
        if prop.direction is not None:
          raise validation.ValidationError(
              'direction not supported in a geospatial index')
      else:

        if prop.IsAscending():
          prop.direction = 'asc'


class IndexDefinitions(validation.Validated):
  """Top level for index definition file.

  Attributes:
    indexes: List of Index definitions.
  """

  ATTRIBUTES = {
      appinfo.APPLICATION: validation.Optional(appinfo.APPLICATION_RE_STRING),
      'indexes': validation.Optional(validation.Repeated(Index)),
  }


index_yaml = yaml.YAML(typ='unsafe')
index_yaml.representer.add_representer(Property, PropertyPresenter)


def ParseIndexDefinitions(document, open_fn=None):
  """Parse an individual index definitions document from string or stream.

  Args:
    document: Yaml document as a string or file-like stream.
    open_fn: Function for opening files. Unused.

  Raises:
    EmptyConfigurationFile when the configuration file is empty.
    MultipleConfigurationFile when the configuration file contains more than
    one document.

  Returns:
    Single parsed yaml file if one is defined, else None.
  """
  try:
    return yaml_object.BuildSingleObject(IndexDefinitions, document)
  except yaml_errors.EmptyConfigurationFile:
    return None


def ParseMultipleIndexDefinitions(document):
  """Parse multiple index definitions documents from a string or stream.

  Args:
    document: Yaml document as a string or file-like stream.

  Returns:
    A list of datstore_index.IndexDefinitions objects, one for each document.
  """
  return yaml_object.BuildObjects(IndexDefinitions, document)


def IndexDefinitionsToKeys(indexes):
  """Convert IndexDefinitions to set of keys.

  Args:
    indexes: A datastore_index.IndexDefinitions instance, or None.

  Returns:
    A set of keys constructed from the argument, each key being a
    tuple of the form (kind, ancestor, properties) where properties is
    a tuple of PropertySpec objects.
  """
  keyset = set()
  if indexes is not None:
    if indexes.indexes:
      for index in indexes.indexes:
        keyset.add(IndexToKey(index))
  return keyset





def IndexToKey(index):
  """Convert Index to key.

  Args:
    index: A datastore_index.Index instance (not None!).

  Returns:
    A tuple of the form (kind, ancestor, properties) where properties
    is a sequence of PropertySpec objects derived from the Index.
  """


  props = []
  if index.properties is not None:
    for prop in index.properties:
      props.append(PropertySpec(name=prop.name,
                                direction = (ASCENDING if prop.IsAscending()
                                             else DESCENDING)))
  return index.kind, index.ancestor, tuple(props)





class PropertySpec(object):
  """Index property attributes required to satisfy a query."""

  def __init__(self, name, direction=None, mode=None):
    assert direction is None or mode is None
    self._name = name
    self._direction = direction
    self._mode = mode

  @property
  def name(self):
    return self._name

  @property
  def direction(self):
    return self._direction

  @property
  def mode(self):
    return self._mode

  def __eq__(self, other):
    if not isinstance(other, PropertySpec):
      return NotImplemented
    return self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not self == other

  def __tuple(self):
    """Produces a tuple for comparison purposes."""
    return (self._name, self._direction, self._mode)

  def __lt__(self, other):
    if not isinstance(other, PropertySpec):
      return NotImplemented
    return self.__tuple() < other.__tuple()

  def __le__(self, other):
    if not isinstance(other, PropertySpec):
      return NotImplemented
    return self.__tuple() <= other.__tuple()

  def __gt__(self, other):
    if not isinstance(other, PropertySpec):
      return NotImplemented
    return self.__tuple() > other.__tuple()

  def __ge__(self, other):
    if not isinstance(other, PropertySpec):
      return NotImplemented
    return self.__tuple() >= other.__tuple()

  def __hash__(self):
    return hash(('PropertySpec', self._name, self._direction, self._mode))

  def __repr__(self):
    builder = ['PropertySpec(name=%s' % self._name]
    if self._direction is not None:
      builder.append('direction=%s' % entity_pb.Index_Property.Direction_Name(self._direction))
    if self._mode is not None:
      builder.append('mode=%s' % entity_pb.Index_Property.Mode_Name(self._mode))
    return '%s)' % (', '.join(builder),)

  def Satisfies(self, other):
    """Determines whether existing index can satisfy requirements of a new query.

    Used in finding matching postfix with traditional "ordered" index specs.
    """
    assert isinstance(other, PropertySpec)
    if self._name != other._name:
      return False
    if self._mode is not None or other._mode is not None:



      return False
    if (other._direction is None):


      return True
    return self._direction == other._direction

  def CopyToIndexPb(self, pb):
    pb.set_name(self._name)









    if (self._mode is None):
      pb.set_direction(self._direction or ASCENDING)
    else:
      pb.set_mode(self._mode)



GEOSPATIAL = entity_pb.Index_Property.GEOSPATIAL
ASCENDING = entity_pb.Index_Property.ASCENDING
DESCENDING = entity_pb.Index_Property.DESCENDING


assert entity_pb.Index_Property.ASCENDING == datastore_pb.Query_Order.ASCENDING
assert (entity_pb.Index_Property.DESCENDING ==
        datastore_pb.Query_Order.DESCENDING)


EQUALITY_OPERATORS = set([datastore_pb.Query_Filter.EQUAL])
INEQUALITY_OPERATORS = set([datastore_pb.Query_Filter.LESS_THAN,
                            datastore_pb.Query_Filter.LESS_THAN_OR_EQUAL,
                            datastore_pb.Query_Filter.GREATER_THAN,
                            datastore_pb.Query_Filter.GREATER_THAN_OR_EQUAL])
EXISTS_OPERATORS = set([datastore_pb.Query_Filter.EXISTS])


def Normalize(filters, orders, exists):
  """Normalizes filter and order query components.

  The resulting components have the same effect as the given components if used
  in a query.

  Args:
    filters: the filters set on the query
    orders: the orders set on the query
    exists: the names of properties that require an exists filter if
      not already specified

  Returns:
    (filter, orders) the reduced set of filters and orders
  """


  eq_properties = set()
  inequality_properties = set()


  for f in filters:
    if f.op() == datastore_pb.Query_Filter.IN and f.property_size() == 1:
      f.set_op(datastore_pb.Query_Filter.EQUAL)
    if f.op() in EQUALITY_OPERATORS:
      eq_properties.add(f.property(0).name())
    elif f.op() in INEQUALITY_OPERATORS:
      inequality_properties.add(f.property(0).name())

  eq_properties -= inequality_properties


  remove_set = eq_properties.copy()
  new_orders = []
  for o in orders:
    if o.property() not in remove_set:
      remove_set.add(o.property())
      new_orders.append(o)
  orders = new_orders

  remove_set.update(inequality_properties)


  new_filters = []
  for f in filters:
    if f.op() not in EXISTS_OPERATORS:
      new_filters.append(f)
      continue
    name = f.property(0).name()
    if name not in remove_set:
      remove_set.add(name)
      new_filters.append(f)


  for prop in exists:
    if prop not in remove_set:
      remove_set.add(prop)
      new_filter = datastore_pb.Query_Filter()
      new_filter.set_op(datastore_pb.Query_Filter.EXISTS)
      new_prop = new_filter.add_property()
      new_prop.set_name(prop)
      new_prop.set_multiple(False)
      new_prop.mutable_value()
      new_filters.append(new_filter)

  filters = new_filters




  if datastore_types.KEY_SPECIAL_PROPERTY in eq_properties:
    orders = []



  new_orders = []
  for o in orders:
    if o.property() == datastore_types.KEY_SPECIAL_PROPERTY:
      new_orders.append(o)
      break
    new_orders.append(o)
  orders = new_orders

  return (filters, orders)


def RemoveNativelySupportedComponents(filters, orders, exists):
  """ Removes query components that are natively supported by the datastore.

  The resulting filters and orders should not be used in an actual query.

  Args:
    filters: the filters set on the query
    orders: the orders set on the query
    exists: the names of properties that require an exists filter if
      not already specified

  Returns:
    (filters, orders) the reduced set of filters and orders
  """
  (filters, orders) = Normalize(filters, orders, exists)

  for f in filters:
    if f.op() in EXISTS_OPERATORS:



      return (filters, orders)


  has_key_desc_order = False
  if orders and orders[-1].property() == datastore_types.KEY_SPECIAL_PROPERTY:
    if orders[-1].direction() == ASCENDING:
      orders = orders[:-1]
    else:
      has_key_desc_order = True






  if not has_key_desc_order:
    for f in filters:
      if (f.op() in INEQUALITY_OPERATORS and
          f.property(0).name() != datastore_types.KEY_SPECIAL_PROPERTY):
        break
    else:
      filters = [
          f for f in filters
          if f.property(0).name() != datastore_types.KEY_SPECIAL_PROPERTY]

  return (filters, orders)


def CompositeIndexForQuery(query):
  """Return the composite index needed for a query.

  A query is translated into a tuple, as follows:

  - The first item is the kind string, or None if we're not filtering
    on kind (see below).

  - The second item is a bool giving whether the query specifies an
    ancestor.

  - After that come (property, ASCENDING) pairs for those Filter
    entries whose operator is EQUAL or IN.  Since the order of these
    doesn't matter, they are sorted by property name to normalize them
    in order to avoid duplicates.

  - After that comes at most one (property, ASCENDING) pair for a
    Filter entry whose operator is on of the four inequalities.  There
    can be at most one of these.

  - After that come all the (property, direction) pairs for the Order
    entries, in the order given in the query.  Exceptions:
      (a) if there is a Filter entry with an inequality operator that matches
          the first Order entry, the first order pair is omitted (or,
          equivalently, in this case the inequality pair is omitted).
      (b) if an Order entry corresponds to an equality filter, it is ignored
          (since there will only ever be one value returned).
      (c) if there is an equality filter on __key__ all orders are dropped
          (since there will be at most one result returned).
      (d) if there is an order on __key__ all further orders are dropped (since
          keys are unique).
      (e) orders on __key__ ASCENDING are dropped (since this is supported
          natively by the datastore).

  - Finally, if there are Filter entries whose operator is EXISTS, and
    whose property names are not already listed, they are added, with
    the direction set to ASCENDING.

  This algorithm should consume all Filter and Order entries.

  Additional notes:

  - The low-level implementation allows queries that don't specify a
    kind; but the Python API doesn't support this yet.

  - If there's an inequality filter and one or more sort orders, the
    first sort order *must* match the inequality filter.

  - The following indexes are always built in and should be suppressed:
    - query on kind only;
    - query on kind and one filter *or* one order;
    - query on ancestor only, without kind (not exposed in Python yet);
    - query on kind and equality filters only, no order (with or without
      ancestor).

  - While the protocol buffer allows a Filter to contain multiple
    properties, we don't use this.  It is only needed for the IN operator
    but this is (currently) handled on the client side, so in practice
    each Filter is expected to have exactly one property.

  Args:
    query: A datastore_pb.Query instance.

  Returns:
    A tuple of the form (required, kind, ancestor, properties).
      required: boolean, whether the index is required;
      kind: the kind or None;
      ancestor: True if this is an ancestor query;
      properties: A tuple consisting of:
      - the prefix, represented by a set of property names
      - the postfix, represented by a tuple consisting of any number of:
        - Sets of property names or PropertySpec objects: these
          properties can appear in any order.
        - Sequences of PropertySpec objects: Indicates the properties
          must appear in the given order, with the specified direction (if
          specified in the PropertySpec).
  """
  required = True


  kind = query.kind()
  ancestor = query.has_ancestor()
  filters = query.filter_list()
  orders = query.order_list()



  for filter in filters:
    assert filter.op() != datastore_pb.Query_Filter.IN, 'Filter.op()==IN'
    nprops = len(filter.property_list())
    assert nprops == 1, 'Filter has %s properties, expected 1' % nprops
    if filter.op() == datastore_pb.Query_Filter.CONTAINED_IN_REGION:
      return CompositeIndexForGeoQuery(query)

  if not kind:


    required = False

  exists = list(query.property_name_list())
  exists.extend(query.group_by_property_name_list())

  filters, orders = RemoveNativelySupportedComponents(filters, orders, exists)


  eq_filters = [f for f in filters if f.op() in EQUALITY_OPERATORS]
  ineq_filters = [f for f in filters if f.op() in INEQUALITY_OPERATORS]
  exists_filters = [f for f in filters if f.op() in EXISTS_OPERATORS]
  assert (len(eq_filters) + len(ineq_filters) +
          len(exists_filters)) == len(filters), 'Not all filters used'

  if (kind and not ineq_filters and not exists_filters and
      not orders):



    names = set(f.property(0).name() for f in eq_filters)
    if not names.intersection(datastore_types._SPECIAL_PROPERTIES):
      required = False



  ineq_property = None
  if ineq_filters:
    for filter in ineq_filters:
      if (filter.property(0).name() ==
          datastore_types._UNAPPLIED_LOG_TIMESTAMP_SPECIAL_PROPERTY):
        continue
      if not ineq_property:
        ineq_property = filter.property(0).name()
      else:
        assert filter.property(0).name() == ineq_property



  group_by_props = set(query.group_by_property_name_list())


  prefix = frozenset(f.property(0).name() for f in eq_filters)

  postfix_ordered = [
      PropertySpec(name=order.property(), direction=order.direction())
      for order in orders]


  postfix_group_by = frozenset(f.property(0).name() for f in exists_filters
                               if f.property(0).name() in group_by_props)

  postfix_unordered = frozenset(f.property(0).name() for f in exists_filters
                                if f.property(0).name() not in group_by_props)


  if ineq_property:
    if orders:


      assert ineq_property == orders[0].property()
    else:
      postfix_ordered.append(PropertySpec(name=ineq_property))

  property_count = (len(prefix) + len(postfix_ordered) + len(postfix_group_by)
                    + len(postfix_unordered))
  if kind and not ancestor and property_count <= 1:


    required = False


    if postfix_ordered:
      prop = postfix_ordered[0]
      if (prop.name == datastore_types.KEY_SPECIAL_PROPERTY and
          prop.direction == DESCENDING):
        required = True


  props = prefix, (tuple(postfix_ordered), postfix_group_by, postfix_unordered)
  return required, kind, ancestor, props


def CompositeIndexForGeoQuery(query):
  """Builds a descriptor for a composite index needed for a geo query.

  Args:
    query: A datastore_pb.Query instance.

  Returns:
    A tuple in the same form as produced by CompositeIndexForQuery.
  """
  required = True
  kind = query.kind()

  assert not query.has_ancestor()
  ancestor = False
  filters = query.filter_list()
  preintersection_props = set()
  geo_props = set()
  for filter in filters:
    name = filter.property(0).name()
    if filter.op() == datastore_pb.Query_Filter.EQUAL:
      preintersection_props.add(PropertySpec(name=name))
    else:

      assert filter.op() == datastore_pb.Query_Filter.CONTAINED_IN_REGION
      geo_props.add(PropertySpec(name=name, mode=GEOSPATIAL))

  prefix = frozenset(preintersection_props)
  postfix = (frozenset(geo_props),)
  return required, kind, ancestor, (prefix, postfix)


def GetRecommendedIndexProperties(properties):
  """Converts the properties returned by datastore_index.CompositeIndexForQuery
  into a recommended list of index properties with the desired constraints.

  Sets (of property names or PropertySpec objects) are sorted, so as to
  normalize them.

  Args:
    properties: See datastore_index.CompositeIndexForQuery

  Returns:
    A tuple of PropertySpec objects.

  """

  prefix, postfix = properties
  result = []
  for sub_list in itertools.chain((prefix,), postfix):
    if isinstance(sub_list, (frozenset, set)):



      result.extend([(p if isinstance(p, PropertySpec)
                      else PropertySpec(name=p)) for p in sorted(sub_list)])
    else:
      result.extend([(PropertySpec(name=p.name, direction=ASCENDING)
                      if p.direction is None else p) for p in sub_list])

  return tuple(result)


def _MatchPostfix(postfix_props, index_props):
  """Matches a postfix constraint with an existing index.

  postfix_props constraints are specified through a list of:
  - sets of string: any order any direction;
  - list of tuples(string, direction): the given order, and, if specified, the
  given direction.

  For example (PropertySpec objects shown here in their legacy shorthand form):
    [set('A', 'B'), [('C', None), ('D', ASC)]]
  matches:
    [('F', ASC), ('B', ASC), ('A', DESC), ('C', DESC), ('D', ASC)]
  with a return value of [('F', ASC)], but does not match:
    [('F', ASC), ('A', DESC), ('C', DESC), ('D', ASC)]
    [('B', ASC), ('F', ASC), ('A', DESC), ('C', DESC), ('D', ASC)]
    [('F', ASC), ('B', ASC), ('A', DESC), ('C', DESC), ('D', DESC)]

  Args:
    postfix_props: A tuple of sets and lists, as output by
        CompositeIndexForQuery. They should define the requirements for the
        postfix of the index.
    index_props: A list of PropertySpec objects that
        define the index to try and match.

  Returns:
    The list of PropertySpec objects that define the prefix properties
    in the given index.  None if the constraints could not be
    satisfied.

  """


  index_props_rev = reversed(index_props)
  for property_group in reversed(postfix_props):
    index_group_iter = itertools.islice(index_props_rev, len(property_group))
    if isinstance(property_group, (frozenset, set)):

      index_group = set(prop.name for prop in index_group_iter)
      if index_group != property_group:
        return None
    else:


      index_group = list(index_group_iter)
      if len(index_group) != len(property_group):
        return None
      for candidate, spec in zip(index_group, reversed(property_group)):
        if not candidate.Satisfies(spec):
          return None
  remaining = list(index_props_rev)
  remaining.reverse()
  return remaining


def MinimalCompositeIndexForQuery(query, index_defs):
  """Computes the minimal composite index for this query.

  Unlike datastore_index.CompositeIndexForQuery, this function takes into
  account indexes that already exist in the system.

  Args:
    query: the datastore_pb.Query to compute suggestions for
    index_defs: a list of datastore_index.Index objects that already exist.

  Returns:
    None if no index is needed, otherwise the minimal index in the form
  (is_most_efficient, kind, ancestor, properties). Where is_most_efficient is a
  boolean denoting if the suggested index is the most efficient (i.e. the one
  returned by datastore_index.CompositeIndexForQuery). kind and ancestor
  are the same variables returned by datastore_index.CompositeIndexForQuery.
  properties is a tuple consisting of the prefix and postfix properties
  returend by datastore_index.CompositeIndexForQuery.
  """

  required, kind, ancestor, (prefix, postfix) = CompositeIndexForQuery(query)

  if not required:
    return None


  remaining_dict = {}

  for definition in index_defs:
    if (kind != definition.kind or

        (not ancestor and definition.ancestor)):
      continue

    _, _, index_props = IndexToKey(definition)

    index_prefix = _MatchPostfix(postfix, index_props)

    if index_prefix is None:

      continue

    remaining_index_props = set([prop.name for prop in index_prefix])

    if remaining_index_props - prefix:

      continue


    index_postfix = tuple(index_props[len(index_prefix):])
    remaining = remaining_dict.get(index_postfix)
    if remaining is None:
      remaining = prefix.copy(), ancestor


    props_remaining, ancestor_remaining = remaining
    props_remaining -= remaining_index_props
    if definition.ancestor:
      ancestor_remaining = False

    if not (props_remaining or ancestor_remaining):
      return None

    if (props_remaining, ancestor_remaining) == remaining:
      continue


    remaining_dict[index_postfix] = (props_remaining, ancestor_remaining)

  if not remaining_dict:
    return (True, kind, ancestor, (prefix, postfix))

  def calc_cost(minimal_props, minimal_ancestor):
    result = len(minimal_props)
    if minimal_ancestor:
      result += 2
    return result


  minimal_postfix, remaining = remaining_dict.popitem()
  minimal_props, minimal_ancestor = remaining
  minimal_cost = calc_cost(minimal_props, minimal_ancestor)
  for index_postfix, (props_remaining, ancestor_remaining) in (
      remaining_dict.items()):
    cost = calc_cost(props_remaining, ancestor_remaining)
    if cost < minimal_cost:
      minimal_cost = cost
      minimal_postfix = index_postfix
      minimal_props = props_remaining
      minimal_ancestor = ancestor_remaining


  props = frozenset(minimal_props), (minimal_postfix, frozenset(), frozenset())
  return False, kind, minimal_ancestor, props






def IndexYamlForQuery(kind, ancestor, props):
  """Return the composite index definition YAML needed for a query.

  Given a query, the arguments for this method can be computed with:
    _, kind, ancestor, props = datastore_index.CompositeIndexForQuery(query)
    props = datastore_index.GetRecommendedIndexProperties(props)

  Args:
    kind: the kind or None
    ancestor: True if this is an ancestor query, False otherwise
    props: PropertySpec objects

  Returns:
    A string with the YAML for the composite index needed by the query.
  """

  serialized_yaml = []
  serialized_yaml.append('- kind: %s' % kind)
  if ancestor:
    serialized_yaml.append('  ancestor: yes')
  if props:
    serialized_yaml.append('  properties:')
    for prop in props:
      serialized_yaml.append('  - name: %s' % prop.name)
      if prop.direction == DESCENDING:
        serialized_yaml.append('    direction: desc')
      if prop.mode is GEOSPATIAL:
        serialized_yaml.append('    mode: geospatial')
  return '\n'.join(serialized_yaml)


def IndexXmlForQuery(kind, ancestor, props):
  """Return the composite index definition XML needed for a query.

  Given a query, the arguments for this method can be computed with:
    _, kind, ancestor, props = datastore_index.CompositeIndexForQuery(query)
    props = datastore_index.GetRecommendedIndexProperties(props)

  Args:
    kind: the kind or None
    ancestor: True if this is an ancestor query, False otherwise
    props: PropertySpec objects

  Returns:
    A string with the XML for the composite index needed by the query.
  """

  serialized_xml = []


  is_geo = any(p.mode is GEOSPATIAL for p in props)
  if is_geo:
    ancestor_clause = ''
  else:
    ancestor_clause = 'ancestor="%s"' % ('true' if ancestor else 'false',)
  serialized_xml.append('  <datastore-index kind="%s" %s>'
                        % (kind, ancestor_clause))
  for prop in props:
    if prop.mode is GEOSPATIAL:
      qual = ' mode="geospatial"'
    elif is_geo:
      qual = ''
    else:

      qual = ' direction="%s"' % ('desc' if prop.direction == DESCENDING
                                  else 'asc')
    serialized_xml.append('    <property name="%s"%s />' % (prop.name, qual))
  serialized_xml.append('  </datastore-index>')
  return '\n'.join(serialized_xml)


def IndexDefinitionToProto(app_id, index_definition):
  """Transform individual Index definition to protocol buffer.

  Args:
    app_id: Application id for new protocol buffer CompositeIndex.
    index_definition: datastore_index.Index object to transform.

  Returns:
    New entity_pb.CompositeIndex with default values set and index
    information filled in.
  """
  proto = entity_pb.CompositeIndex()

  proto.set_app_id(app_id)
  proto.set_id(0)
  proto.set_state(entity_pb.CompositeIndex.WRITE_ONLY)

  definition_proto = proto.mutable_definition()
  definition_proto.set_entity_type(index_definition.kind)
  definition_proto.set_ancestor(index_definition.ancestor)

  if index_definition.properties is not None:
    is_geo = any(x.mode == 'geospatial' for x in index_definition.properties)
    for prop in index_definition.properties:
      prop_proto = definition_proto.add_property()
      prop_proto.set_name(prop.name)

      if prop.mode == 'geospatial':
        prop_proto.set_mode(entity_pb.Index_Property.GEOSPATIAL)
      elif is_geo:




        pass
      elif prop.IsAscending():
        prop_proto.set_direction(entity_pb.Index_Property.ASCENDING)
      else:
        prop_proto.set_direction(entity_pb.Index_Property.DESCENDING)

  return proto


def IndexDefinitionsToProtos(app_id, index_definitions):
  """Transform multiple index definitions to composite index records

  Args:
    app_id: Application id for new protocol buffer CompositeIndex.
    index_definition: A list of datastore_index.Index objects to transform.

  Returns:
    A list of tranformed entity_pb.Compositeindex entities with default values
    set and index information filled in.
  """
  return [IndexDefinitionToProto(app_id, index)
          for index in index_definitions]


def ProtoToIndexDefinition(proto):
  """Transform individual index protocol buffer to index definition.

  Args:
    proto: An instance of entity_pb.CompositeIndex to transform.

  Returns:
    A new instance of datastore_index.Index.
  """
  properties = []
  proto_index = proto.definition()
  for prop_proto in proto_index.property_list():
    prop_definition = Property(name=prop_proto.name())

    if prop_proto.mode() == entity_pb.Index_Property.GEOSPATIAL:
      prop_definition.mode = 'geospatial'
    elif prop_proto.direction() == entity_pb.Index_Property.DESCENDING:
      prop_definition.direction = 'desc'
    elif prop_proto.direction() == entity_pb.Index_Property.ASCENDING:
      prop_definition.direction = 'asc'

    properties.append(prop_definition)

  index = Index(kind=proto_index.entity_type(), properties=properties)
  if proto_index.ancestor():
    index.ancestor = True
  return index


def ProtosToIndexDefinitions(protos):
  """Transform multiple index protocol buffers to index definitions.

  Args:
    A list of entity_pb.Index records.
  """
  return [ProtoToIndexDefinition(definition) for definition in protos]
