module.exports = {
  debug: true,
  branches: 'master',
  verifyConditions: [
    '@semantic-release/changelog',
    '@semantic-release/github',
    '@semantic-release/npm'
  ],
  prepare: [
    '@semantic-release/changelog',
    '@semantic-release/git',
    '@semantic-release/npm'
  ],
  publish: [
    '@semantic-release/github',
    '@semantic-release/npm'
  ],
  success: [
    '@semantic-release/github'
  ],
  releaseRules: [
    { breaking: true, release: 'major' },
    { revert: true, release: 'patch' },
    { type: 'feat', release: 'minor' },
    { type: 'fix', release: 'patch' },
    { type: 'perf', release: 'patch' },
    { type: 'chore', release: 'patch' }
  ],
}
