import EventQueue from './lib/event-queue.js'
import { getObject } from './lib/util.js'
import http2 from 'http2'
import net from 'net'
import fs from 'fs'

class SSEServer {
  constructor (options) {
    this.options = options || {}
    this.eventQueue = new EventQueue(this.options)
    this._buf = ''
  }

  _onInputSocketReadable (chunk) {
    if (chunk) {
      if (this.options.verbose) console.error('chunk', chunk.trim())
      this._buf += chunk
      while (this._buf.length) {
        const event = getObject(this._buf)
        if (event) {
          this._buf = this._buf.replace(event, '').trim()
          this.eventQueue.sendEvent(JSON.parse(event))
        } else {
          if (this.options.verbose) {
            console.error('Event not found', this._buf)
          }
          break
        }
      }
    }
  }

  createServer () {

    const options = {
      key: fs.readFileSync(this.options.keyPath),
      cert: fs.readFileSync(this.options.certPath),
      allowHTTP1: true
    }

    const sse = http2.createSecureServer(options, (req, res) => {
      this.eventQueue.attachResponse(res)
      this.eventQueue.flush()
    })
    sse.timeout = 0;

    const input = net.createServer(connection => {
      connection.setEncoding('utf8')
      connection.on('data', this._onInputSocketReadable.bind(this))
      connection.on('error', err => {
        if (err.code !== 'ECONNRESET') console.log(err)
      })
    })

    return { sse, input }
  }
}

export default SSEServer

/*
if one input chunk is bad JSON, parsing the input never works again as the buffer is forever invalid
 */
