'use strict'

const packageJson = require('../package.json')

let env = {
  NODE_ENV: '"production"',
  PACKAGE_JSON: JSON.stringify(packageJson)
}

module.exports = env
