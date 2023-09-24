'use strict'

const packageJson = require('../package.json')

let ORSKEY = process.env.ORSKEY
let BITLYLOGIN = process.env.BITLYLOGIN
let BITLYAPIKEY = process.env.BITLYAPIKEY
let TAXIROUTEAPI = process.env.TAXIROUTEAPI
let TAXILOCATIONAPI = process.env.TAXILOCATIONAPI

let env = {
  NODE_ENV: '"production"',
  PACKAGE_JSON: JSON.stringify(packageJson)
}

if (ORSKEY) {
  env.ORSKEY = `"${ORSKEY}"`
}
if (BITLYLOGIN) {
  env.BITLYLOGIN = `"${BITLYLOGIN}"`
}
if (BITLYAPIKEY) {
  env.BITLYAPIKEY = `"${BITLYAPIKEY}"`
}
if (TAXIROUTEAPI) {
  env.TAXIROUTEAPI = `"${TAXIROUTEAPI}"`
}
if (TAXILOCATIONAPI) {
  env.TAXILOCATIONAPI = `"${TAXILOCATIONAPI}"`
}

module.exports = env
