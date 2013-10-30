/* You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @fileoverview Generating Yail for catagories of blocks.
 * @author andrew.f.mckinney@gmail.com (Andrew F. McKinney) Due to the frequency
 *         of long strings, the 80-column wrap rule need not apply to language
 *         files.
 */

/**
 * Lyn's History:
 * [lyn, 10/29/13] Fixed bug in handling parameters of zero-arg procedures.
 * [lyn, 10/27/13] Modified procedure names to begin with YAIL_PROC_TAG (currently 'p$')
 *     and parameters to begin with YAIL_LOCAL_VAR_TAG (currently '$').
 *     At least on Kawa-legal first character is necessary to ensure AI identifiers
 *     satisfy Kawa's identifier rules. And the procedure 'p$' tag is necessary to
 *     distinguish procedures from globals (which use the 'g$' tag).
 * [lyn, 01/15/2013] Edited to remove STACK (no longer necessary with DO-THEN-RETURN)
 */

Blockly.Yail = Blockly.Generator.get('Yail');

Blockly.Yail.YAIL_PROC_TAG = 'p$'; // See notes on this in generators/yail/variables.js

// Generator code for procedure call with return
// [lyn, 01/15/2013] Edited to remove STACK (no longer necessary with DO-THEN-RETURN)
Blockly.Yail.procedures_defreturn = function() {
  var argPrefix = Blockly.Yail.YAIL_LOCAL_VAR_TAG
                  + (Blockly.usePrefixInYail && this.arguments_.length != 0 ? "param_" : "");
  var args = this.arguments_.map(function (arg) {return argPrefix + arg;}).join(' ');
  var procName = Blockly.Yail.YAIL_PROC_TAG + this.getTitleValue('NAME');
  var returnVal = Blockly.Yail.valueToCode(this, 'RETURN', Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_FALSE;
  var code = Blockly.Yail.YAIL_DEFINE + Blockly.Yail.YAIL_OPEN_COMBINATION + procName
      + Blockly.Yail.YAIL_SPACER + args + Blockly.Yail.YAIL_CLOSE_COMBINATION 
      + Blockly.Yail.YAIL_SPACER + returnVal + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

// Generator code for procedure call with return
Blockly.Yail.procedures_defnoreturn = function() {
  var argPrefix = Blockly.Yail.YAIL_LOCAL_VAR_TAG
                  + (Blockly.usePrefixInYail && this.arguments_.length != 0 ? "param_" : "");
  var args = this.arguments_.map(function (arg) {return argPrefix + arg;}).join(' ');
  var procName = Blockly.Yail.YAIL_PROC_TAG + this.getTitleValue('NAME');
  var body = Blockly.Yail.statementToCode(this, 'STACK', Blockly.Yail.ORDER_NONE)  || Blockly.Yail.YAIL_FALSE;
  var code = Blockly.Yail.YAIL_DEFINE + Blockly.Yail.YAIL_OPEN_COMBINATION + procName
      + Blockly.Yail.YAIL_SPACER + args + Blockly.Yail.YAIL_CLOSE_COMBINATION + body
      + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

Blockly.Yail.procedure_lexical_variable_get = function() {
  return Blockly.Yail.lexical_variable_get.call(this);
}

//call the do return in control category
Blockly.Yail.procedures_do_then_return = function() {
  return Blockly.Yail.controls_do_then_return.call(this);
}

// Generator code for procedure call with return
Blockly.Yail.procedures_callnoreturn = function() {
  var procName = Blockly.Yail.YAIL_PROC_TAG + this.getTitleValue('PROCNAME');
  var argCode = [];
  for ( var x = 0;this.getInput("ARG" + x); x++) {
    argCode[x] = Blockly.Yail.valueToCode(this, 'ARG' + x, Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_FALSE;
  }
  var code = Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_GET_VARIABLE + procName
      + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER + argCode.join(' ')
      + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return code;
};

// Generator code for procedure call with return
Blockly.Yail.procedures_callreturn = function() {
  var procName = Blockly.Yail.YAIL_PROC_TAG + this.getTitleValue('PROCNAME');
  var argCode = [];
  for ( var x = 0; this.getInput("ARG" + x); x++) {
    argCode[x] = Blockly.Yail.valueToCode(this, 'ARG' + x, Blockly.Yail.ORDER_NONE) || Blockly.Yail.YAIL_FALSE;
  }
  var code = Blockly.Yail.YAIL_OPEN_COMBINATION + Blockly.Yail.YAIL_GET_VARIABLE + procName
      + Blockly.Yail.YAIL_CLOSE_COMBINATION + Blockly.Yail.YAIL_SPACER + argCode.join(' ')
      + Blockly.Yail.YAIL_CLOSE_COMBINATION;
  return [ code, Blockly.Yail.ORDER_ATOMIC ];
};
