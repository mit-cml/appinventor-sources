goog.provide('AI.Blockly.TypeBlockPluginAdapter');

goog.require('AI.Blockly.TypeBlock');

const toOption = (displayText, legacyOption) => {
  const option = {
    blockType: legacyOption.canonicName,
    displayText
  };
  if (legacyOption.dropDown?.titleName && legacyOption.dropDown.value) {
    option.fieldValues = {
      [legacyOption.dropDown.titleName]: legacyOption.dropDown.value
    };
  }
  if (legacyOption.mutatorAttributes && Object.keys(legacyOption.mutatorAttributes).length) {
    const mutation = document.createElement('mutation');
    for (const [name, value] of Object.entries(legacyOption.mutatorAttributes)) {
      mutation.setAttribute(name, value);
    }
    option.extraState = Blockly.utils.xml.domToText(mutation);
  }
  return option;
};

const matcher = (options, query) => {
  const displayTexts = options.map(option => option.displayText);
  const legacyMatcher = new AI.Blockly.TypeBlock.ac.AIArrayMatcher(displayTexts, false);
  const matchedOptions = [];
  legacyMatcher.requestMatchingRows(query, 100, (_query, matchedDisplayTexts) => {
    matchedDisplayTexts.forEach(matchedDisplayText => {
      const matchedOption = options.find(option => option.displayText === matchedDisplayText);
      if (matchedOption) {
        matchedOptions.push(matchedOption);
      } else {
        const matchedLegacyOption = AI.Blockly.TypeBlock.matchNumberOrTextBlock(matchedDisplayText);
        if (matchedLegacyOption) {
          matchedOptions.push(toOption(matchedDisplayText, matchedLegacyOption));
        }
      }
    });
  });
  return matchedOptions;
};

class OptionGenerator {
  constructor(legacyTypeBlock) {
    this.legacyTypeBlock = legacyTypeBlock;
  }

  generateOptions() {
    this.legacyTypeBlock.lazyLoadOfOptions_();
    return Object.entries(this.legacyTypeBlock.TBOptions_)
      .map(([displayText, legacyOption]) => toOption(displayText, legacyOption))
      .sort((a, b) => a.displayText > b.displayText ? 1 : a.displayText < b.displayText ? -1 : 0);
  }
}

class ConnectionStrategy {
  constructor(legacyTypeBlock) {
    this.legacyTypeBlock = legacyTypeBlock;
  }

  canConnect(_newBlock, targetBlock, context) {
    return context.selectedBlock && context.selectedBlock === targetBlock;
  }

  connect(newBlock, targetBlock) {
    this.legacyTypeBlock.connectIfPossible(targetBlock, newBlock);
    return newBlock.getParent() !== null
      ? { success: true }
      : { success: false, reason: 'No compatible connection found' };
  }

  get priority() { return 100; }
  get name() { return 'Legacy'; }
}

AI.Blockly.TypeBlockPluginAdapter.create = workspace => {
  const legacyTypeBlock = new AI.Blockly.TypeBlock(workspace);
  return {
    needsReload: legacyTypeBlock.needsReload,
    matcher,
    optionGenerator: new OptionGenerator(legacyTypeBlock),
    connectionStrategy: new ConnectionStrategy(legacyTypeBlock)
  };
};
