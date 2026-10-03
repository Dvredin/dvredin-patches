'use strict';

// npm audit cannot assess a Git-only correction by published version number.
// Verify both the installed source and behavior; never rename it to fake a fix.
const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { createHash } = require('node:crypto');
const { createRequire } = require('node:module');
const braces = require('braces');
const source = path.dirname(require.resolve('braces'));
const root = path.resolve(__dirname, '..');
const pin = 'git+https://github.com/micromatch/braces.git#28d440b5dd449dbf1fe6f3506cf94ecca4d02660';
const hashes = {
  'index.js': '332ea07c7b006361aad12aa994ca75dc1db8e8382b884909e2f38f10b85c88a4',
  'lib/utils.js': 'b5a7596aa67730412b3c029ef09e84e6b67b8e445cffd35d1d295549c89066c7',
  'lib/parse.js': 'b1bf766fba6a62035f78ecbda8a5fd94e921aa1c1ec0cdf3f467e9c836abed55',
  'lib/expand.js': '7ea3e14c2b2b256ef244fd3d83b8fcaa20aa2232b4e6d768c3bb6ab567f66cf5',
  'lib/constants.js': 'f9fb688959232eee3e6ad7906a5b0e3234815db49ee857ef86983d65b917dc7c',
  'lib/compile.js': 'b651f7715e6db8942ce61d3394357b4d81c8ece88240aa31a458ea1165edd195',
  'lib/stringify.js': '49dc2d8bafa74f34715a18a845bcb82ce66caaf3bab4cf117998e06b1f9a50a9',
};

function depthFailure(fn) {
  assert.throws(fn, error => (error instanceof SyntaxError || error instanceof RangeError)
    && /exceeds max depth/.test(error.message)
    && !/Maximum call stack/.test(error.message));
}

function deepAst() {
  let ast = { type: 'text', value: 'a' };
  for (let i = 0; i < 101; i++) ast = { type: 'brace', nodes: [ast] };
  return { type: 'root', nodes: [ast] };
}

test('exact immutable upstream source and original version are installed', () => {
  const manifest = JSON.parse(fs.readFileSync(path.join(root, 'package.json')));
  assert.equal(manifest.devDependencies.braces, pin);
  assert.equal(require(path.join(source, 'package.json')).version, '3.0.3');
  for (const [file, expected] of Object.entries(hashes)) {
    assert.equal(createHash('sha256').update(fs.readFileSync(path.join(source, file))).digest('hex'), expected, file);
  }
  const lock = fs.readFileSync(path.join(root, 'pnpm-lock.yaml'), 'utf8');
  assert.ok(lock.includes('28d440b5dd449dbf1fe6f3506cf94ecca4d02660'));
  assert.ok(!/^  braces@3\.0\.3:/m.test(lock), 'unpatched registry package remains in active lock');
});

test('every installed micromatch resolves to the reviewed correction', () => {
  const store = path.join(root, 'node_modules', '.pnpm');
  const matches = fs.readdirSync(store).filter(name => name.startsWith('micromatch@'));
  assert.ok(matches.length > 0);
  for (const name of matches) {
    const entry = path.join(store, name, 'node_modules', 'micromatch', 'package.json');
    const load = createRequire(entry);
    assert.equal(fs.realpathSync(load.resolve('braces')), fs.realpathSync(require.resolve('braces')));
  }
});

test('ordinary release globs retain brace and range behavior', () => {
  assert.deepEqual(braces.expand('patches-{1,2}.mpp'), ['patches-1.mpp', 'patches-2.mpp']);
  assert.deepEqual(braces.expand('v{1..3}'), ['v1', 'v2', 'v3']);
  assert.equal(braces.stringify(braces.parse('{a,{b,c}}'), { escapeInvalid: true }), '{a,{b,c}}');
});

test('deep patterns below the character limit reject before recursive exhaustion', () => {
  for (const pattern of ['('.repeat(4900) + 'a' + ')'.repeat(4900), '{a,'.repeat(2400) + 'z' + '}'.repeat(2400)]) {
    assert.ok(pattern.length < 10000);
    for (const action of ['parse', 'compile', 'expand', 'stringify']) depthFailure(() => braces[action](pattern));
  }
});

test('caller options cannot raise the safety ceiling', () => {
  const pattern = '{a,'.repeat(101) + 'z' + '}'.repeat(101);
  for (const maxDepth of [Infinity, Number.MAX_VALUE, 1000, NaN]) {
    depthFailure(() => braces.compile(pattern, { maxDepth }));
  }
  assert.doesNotThrow(() => braces.parse('{{a,b},c}', { maxDepth: 2 }));
  depthFailure(() => braces.parse('{{a,b},c}', { maxDepth: 1.5 }));
});

test('direct AST walkers have independent depth guards', () => {
  for (const action of ['compile', 'expand', 'stringify']) depthFailure(() => braces[action](deepAst()));
});

test('cyclic expansion parent chains terminate with a controlled error', () => {
  const ast = { type: 'paren', nodes: [{ type: 'text', value: 'a' }] };
  ast.parent = ast;
  assert.throws(() => braces.expand(ast), /AST parent chain contains a cycle/);
});
