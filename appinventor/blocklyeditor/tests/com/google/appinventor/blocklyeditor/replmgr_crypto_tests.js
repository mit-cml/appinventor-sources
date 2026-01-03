suite('ReplMgr crypto', function() {
  const SHA1_HEX = /^[0-9a-f]{40}$/;

  test('hash connection code for companion pairing', async function() {
    const replcode = 'mjqbfg';
    const rendezvouscode = await Blockly.ReplMgr.sha1(replcode);

    chai.assert.match(rendezvouscode, SHA1_HEX);
  });

  test('sign messages to companion for authentication', async function() {
    const replcode = 'ktvhdp';
    const code = '(define-event Button1 Click do (set-and-coerce-property! \'Label1 \'Text "clicked" \'text))';
    const seq_count = 1;
    const blockid = 'aB3!xY@z7#pQ$mK%nL^w';
    top.ReplState = { replcode: replcode };
    const mac = await Blockly.ReplMgr.hmac(`${code}${seq_count}"${blockid}"`);

    chai.assert.match(mac, SHA1_HEX);
  });
});
