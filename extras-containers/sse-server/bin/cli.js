#!/usr/bin/env node
import SSEServer from 'sse-server'
import commandLineArgs from 'command-line-args'
import commandLineUsage from 'command-line-usage'

const optionDefinitions = [
  { name: 'input-port', alias: 'i', defaultValue: 9090, type: Number },
  { name: 'https-port', alias: 'p', defaultValue: 9000, type: Number },
  { name: 'key-path', alias: 'k',  type: String },
  { name: 'cert-path', alias: 'c',  type: String },
  { name: 'verbose', alias: 'v', type: Boolean },
  { name: 'help', alias: 'h', type: Boolean }
]
const options = commandLineArgs(optionDefinitions, { camelCase: true})
if (options.help) {
  const usage = commandLineUsage([
    {
      header: 'sse-server',
      content: 'Pipe an object stream from terminal to browser.'
    },
    {
      header: 'Options',
      optionList: optionDefinitions
    },
    {
      content: ''
    }
  ])
  console.error(usage)
} else {
  const sseServer = new SSEServer(options)
  const server = sseServer.createServer()
  server.sse.listen(options.httpsPort, () => console.error(`SSE server: https://localhost:${options.httpsPort}`))
  process.on('SIGINT', () => {
    sseServer.eventQueue.end()
    process.exit(0)
  })
  if (options.verbose) {
    setInterval(function () {
      console.error('buffer size:', sseServer._buf.length)
    }, 10000)
  }
  if (!process.stdin.isTTY) {
    process.stdin.setEncoding('utf8')
    process.stdin.on('data', sseServer._onInputSocketReadable.bind(sseServer))
  } else {
    server.input.listen(options.inputPort, () => console.error(`Input socket: localhost:${options.inputPort}`))
  }
}
