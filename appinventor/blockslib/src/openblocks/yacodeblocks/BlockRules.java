// Copyright 2009 Google Inc. All Rights Reserved.

package openblocks.yacodeblocks;

import openblocks.codeblocks.Block;
import openblocks.codeblocks.BlockGenus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * BlockRules specifies rules and methods for Block linking in the Young Android language.
 * The algorithm for rule checking proceeds as such:
 * In the ya_lang_def.xml file, a block's sockets and plugs can be tagged with one or more types.
 * When checking to see if two blocks can be connected, the socket block's socket types and plug
 * block's plug types are checked for a match. (If either the plug or socket has no associated
 * types, the connection is allowed).
 * If this check fails, the plug block is then checked for possible coercion. The type coercion
 * map is checked to see if the plug's type can coerce to any of the socket blocks types. (If
 * either the plug or socket has no associated types, the coercion is prevented).
 *
 */
public class BlockRules {

  // Map from genus name to socket rules. Socket rules are a map from
  // socket label to a set of plug primitive types that the socket accepts.
  // TODO(user): use a pair rather than a hashset
  protected static Map<String, HashMap<String, HashSet<String>>> genusToSocketRules =
      new HashMap<String, HashMap<String, HashSet<String>>>();

  // Map from genus name to plug types. Plug types is a set of Codeblocks primitive types
  // that are associated with this plug.
  protected static Map<String, HashSet<String>> genusToPlugType =
      new HashMap<String, HashSet<String>>();

  // Map from component genus to component property rules. Property rules are a map from
  // component properties to a corresponding set of Codeblocks types.
  protected static Map<String, HashMap<String, HashSet<String>>> genusToPropertyRules =
      new HashMap<String, HashMap<String, HashSet<String>>>();

  // Map from primitive type to other coercible types.
  protected static Map<String, HashSet<String>> typeToCoercibleTypes =
      new HashMap<String, HashSet<String>>();

  // The list of primitive types allowed in Codeblocks
  private static final Set<String> YA_TYPES = new HashSet<String>(
      Arrays.asList("value", "text", "number", "list", "boolean", "color",
                    "argument", "null", "component", "InstantInTime"));

  private static final HashSet<String> COMPONENT_TYPES = new HashSet<String>(
      Arrays.asList("value", "component"));

  // private constructor for this utility class
  private BlockRules() {
  }

  /**
   * Create linking and coercion rules for all block genusus
   */
  protected static void initializeRules() {
    // Initialize coercion map
    initializeCoercionRules();
    // Create rules for all genuses
    for (String genus : BlockGenus.getAllGenuses()) {
      createBlockRules(genus);
    }
  }

  /**
   * Initialize coercion rules in Codeblocks.
   */
  private static void initializeCoercionRules() {
    permitCoercion("text", "number", true);
    permitCoercion("list", "text", false);
    permitCoercion("boolean", "text", false);
  }

  /**
   * Create coercion rules
   * @param startType the primitive type of the socket
   * @param endType the primitive type of the plug
   * @param reversible if the coercion is permissible in the reverse direction
   */
  private static void permitCoercion(String startType, String endType, boolean reversible) {
    HashSet<String> coercibleTypes = typeToCoercibleTypes.get(startType);
    if (coercibleTypes == null) {
      coercibleTypes = new HashSet<String>();
      typeToCoercibleTypes.put(startType, coercibleTypes);
    }
    coercibleTypes.add(endType);
    if (reversible) {
      permitCoercion(endType, startType, false);
    }
  }

  /**
   * Create block connection rules for a block's plugs and sockets. One can either include
   * individual primitives for plugs and sockets, or exclude individual primitives for plugs
   * and sockets (in which case all primitive types except those mentioned will be permissible).
   * @param genusName The genus for which to make rules
   */
  public static void createBlockRules(String genusName) {
    BlockGenus genus = BlockGenus.getGenusWithName(genusName);
    // Include valid plug types
    String plugType;
    int idx = 1;
    while ((plugType = genus.getProperty("plug-type-" + idx)) != null) {
      HashSet<String> plugTypes = genusToPlugType.get(genusName);
      if (plugTypes == null) {
        plugTypes = new HashSet<String>();
        genusToPlugType.put(genusName, plugTypes);
      }
      plugTypes.add(plugType);
      idx++;
    }
    // Exclude invalid plug types
    String plugExclude;
    idx = 1;
    while ((plugExclude = genus.getProperty("type-exclude-" + idx)) != null) {
      HashSet<String> plugTypes = genusToPlugType.get(genusName);
      if (plugTypes == null) {
        plugTypes = new HashSet<String>(YA_TYPES);
        genusToPlugType.put(genusName, plugTypes);
      }
      plugTypes.remove(plugExclude);
      idx++;
    }
    // Include valid socket types
    String socketType;
    idx = 1;
    while ((socketType = genus.getProperty("socket-allow-" + idx)) != null) {
      // Socket types in the form 'socketLabel/type'
      String socketTuple[] = socketType.split("/");
      HashMap<String, HashSet<String>> socketMap = genusToSocketRules.get(genusName);
      if (socketMap == null) {
        socketMap = new HashMap<String, HashSet<String>>();
        genusToSocketRules.put(genusName, socketMap);
      }
      HashSet<String> socketList = socketMap.get(socketTuple[0]);
      if (socketList == null) {
        socketList = new HashSet<String>();
        socketMap.put(socketTuple[0], socketList);
      }
      socketList.add(socketTuple[1]);
      idx++;
    }
    // Exclude invalid socket types
    String socketExclude;
    idx = 1;
    while ((socketType = genus.getProperty("socket-exclude-" + idx)) != null) {
      // Socket types in the form 'socketLabel/type'
      String socketTuple[] = socketType.split("/");
      HashMap<String, HashSet<String>> socketMap = genusToSocketRules.get(genusName);
      if (socketMap == null) {
        socketMap = new HashMap<String, HashSet<String>>();
        genusToSocketRules.put(genusName, socketMap);
      }
      HashSet<String> socketList = socketMap.get(socketTuple[0]);
      if (socketList == null) {
        socketList = new HashSet<String>(YA_TYPES);
        socketMap.put(socketTuple[0], socketList);
      }
      socketList.remove(socketTuple[1]);
      idx++;
    }
  }

  /**
   * Create block connection rules for a component property.
   * @param componentGenus The genus name of the component
   * @param propName The name of the component property
   * @param type The Codeblocks type of the component's property
   */
  protected static void createPropertyRules(String componentGenus, String propName, String type) {
    HashMap<String, HashSet<String>> propertyMap =
        genusToPropertyRules.get(componentGenus);
    if (propertyMap == null) {
      propertyMap = new HashMap<String, HashSet<String>>();
      genusToPropertyRules.put(componentGenus, propertyMap);
    }
    HashSet<String> propertyList = propertyMap.get(propName);
    if (propertyList == null) {
      propertyList = new HashSet<String>();
      propertyMap.put(propName, propertyList);
    }
    propertyList.add(type);
  }

  /**
   * Determine if two genuses can be connected according to the YA rules.
   * @param socketBlock the socket block in this block connection
   * @param plugBlock the plug block in this block connection
   * @param socketLabel the label on the socket
   */
  protected static boolean canLink(Block socketBlock, Block plugBlock, String socketLabel) {
    HashSet<String> allowablePlugs = getAllowablePlugs(socketBlock, socketLabel);
    HashSet<String> plugTypes = getPlugTypes(plugBlock);
    if (allowablePlugs == null || plugTypes == null) {
      // This socket has no restrictions or
      // this plug has no restrictions
      return true;
    }
    boolean linkable = false;
    for (String plugType : plugTypes) {
      if (allowablePlugs.contains(plugType)) {
        linkable = true;
      }
    }
    return linkable;
  }

  private static HashSet<String> getAllowablePlugs(Block socketBlock, String socketLabel) {
    String socketGenus = socketBlock.getGenusName();
    if (isComponentSetter(socketGenus)) {
      // block genus in the form ComponentName.Property
      String blockLabel = socketBlock.getBlockLabel();
      String componentName = blockLabel.substring(0, blockLabel.indexOf('.'));
      String property = blockLabel.substring(blockLabel.indexOf('.') + 1);
      String socketComponentGenus = WorkspaceControllerHolder.get().
          getComponentBlockManager().getGenusFromComponentName(componentName);
      // TODO(user): remove 'value' hack
      if (socketGenus.equals("componentTypeSetter") && socketLabel.equals("component")) {
        // TOOD(markf): It would be nice if we could use the actual component type rather than
        // just the generic "component" type.
        return COMPONENT_TYPES;
      }
      if (getPropertyTypes(socketComponentGenus, property) ==  null) {
        return null;
      } else {
        HashSet<String> propertyTypes =
            new HashSet<String>(getPropertyTypes(socketComponentGenus, property));
        propertyTypes.add("value");
        return propertyTypes;
      }
    } else {
      HashMap<String, HashSet<String>> socketRuleMap = genusToSocketRules.get(socketGenus);
      if (socketRuleMap == null) {
        // This socket block's genus no restrictions
        return null;
      }
      if (socketRuleMap.containsKey("*")) {
        return socketRuleMap.get("*");
      } else {
        return socketRuleMap.get(socketLabel);
      }
    }
  }

  private static HashSet<String> getPlugTypes(Block plugBlock) {
    String plugGenus = plugBlock.getGenusName();
    if (isComponentGetter(plugGenus)) {
      // block genus in the form ComponentName.Property
      String blockLabel = plugBlock.getBlockLabel();
      String componentName = blockLabel.substring(0, blockLabel.indexOf('.'));
      String property = blockLabel.substring(blockLabel.indexOf('.') + 1);
      String plugComponentGenus = WorkspaceControllerHolder.get().
          getComponentBlockManager().getGenusFromComponentName(componentName);
      return getPropertyTypes(plugComponentGenus, property);
    } else {
      return genusToPlugType.get(plugGenus);
    }
  }

  private static boolean isComponentGetter(String plugGenus) {
    return plugGenus.equals("componentGetter") || plugGenus.equals("componentTypeGetter");
  }

  private static boolean isComponentSetter(String plugGenus) {
    return plugGenus.equals("componentSetter") || plugGenus.equals("componentTypeSetter");
  }

  private static HashSet<String> getPropertyTypes(String genus, String property) {
    HashMap<String, HashSet<String>> propertyRules = genusToPropertyRules.get(genus);
    if (propertyRules == null) {
      return null;
    }
    return propertyRules.get(property);
  }

  /**
   * Determine if two blocks can be connected through type coercion.
   * @param socketBlock the block containing the socket in this block connection
   * @param socketLabel the label on the socket
   * @return the result of the coercion
   */
  protected static CoercionResult canCoerce(Block socketBlock, Block plugBlock,
                                            String socketLabel) {
    String socketGenus = socketBlock.getGenusName();
    String plugGenus = plugBlock.getGenusName();
    HashSet<String> socketTypes = getAllowablePlugs(socketBlock, socketLabel);
    HashSet<String> plugTypes = getPlugTypes(plugBlock);
    if (socketTypes == null || plugTypes == null) {
      // This socket or plug has no possible coercions.
      return new CoercionResult(false);
    }
    // For each plug and socket type, check if plug type can be coerced to the socket type
    for (String plugType : plugTypes) {
      HashSet<String> plugCoercibleTypes = typeToCoercibleTypes.get(plugType);
      if (plugCoercibleTypes == null) {
        continue;
      }
      for (String socketType : socketTypes) {
        HashSet<String> socketCoercibleTypes = typeToCoercibleTypes.get(socketType);
        if (socketCoercibleTypes == null) {
          continue;
        }
        if (plugCoercibleTypes.contains(socketType)) {
          return canCoerceBlockToType(plugBlock, socketType);
        }
      }
    }
    return new CoercionResult(false, CoercionErrorReason(plugBlock, socketTypes, plugTypes));
  }

  private static String CoercionErrorReason(Block block,
                                            HashSet<String> socketTypes,
                                            HashSet<String> plugTypes) {
    // Note: (halabelson) We remove "value" from the types listed in
    // the error message even though it might be legal to put a value
    // block here.  It seems more clear to say "foo isn't a number"
    // than to say "foo isn't one of [value, number]".
    HashSet<String> reducedTypes = new HashSet<String>(socketTypes);
    reducedTypes.remove("value");
    //TODO(halabelson): Handle the case where there is more than one plug type.  This case
    // does not currently occur?
    String plugType = (String) plugTypes.toArray()[0];
    String firstSocketType = (String) reducedTypes.toArray()[0];
    if (reducedTypes.size() == 1) {
      // The standard wording of the error message is confusing in the case of argument sockets,
      // so we use a special error message.
      if (firstSocketType.equals("argument")) {
          return "this socket requires a name block.";
      } else {
        return block.getBlockLabel() + " is " + addArticle(plugType) + " and can't be " +
            addArticle(firstSocketType) + ".";
      }
    } else {
          // TODO(halabelson): This case of multiple target types current occurs only with typing to
          // plug in an argument block for a variable value.  Think about improving the error
          // message here.
          return block.getBlockLabel() + " is not one of " + reducedTypes.toString();
    }
  }

  private static String addArticle(String text) {
    HashSet<String> vowels = new HashSet<String>(Arrays.asList("a","e","i","o","u"));
    if (vowels.contains(text.substring(0,1))) {
      return "an " + text;
    } else {
      return "a " + text;
    }
  }

  /**
   * Determine if this block can be coerced to a new type.
   * @param block the block to coerce
   * @param type the target type for the coercion
   * @return the success of the coercion
   */
  private static CoercionResult canCoerceBlockToType(Block block, String type) {
    String blockLabel = block.getBlockLabel();
    String genus = block.getGenusName();
    if (type.equals("number") && genus.equals("text")) {
      if (!blockLabel.matches(YABlockCompiler.INTEGER_REGEXP) &&
          !blockLabel.matches(YABlockCompiler.FLONUM_REGEXP)) {
        return new CoercionResult(false, blockLabel + " is not a number.");
      }
    }
    return new CoercionResult(true);
  }
}
