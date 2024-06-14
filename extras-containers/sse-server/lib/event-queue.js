/* should be a stream managing backpressure */
class EventQueue {
  constructor (options) {
    this.options = options || {}
    this.backlog = []
    this.stored = []
  }

  attachResponse (res) {
    this._res = res
    res.writeHead(200, {
      'content-type': 'text/event-stream',
      'Access-Control-Allow-Origin': '*'
    })
    res.flushHeaders()
  }

  sendEvent (event, flushing) {
    if (event.name === 'clear') {
      this.stored = []
      this.backlog = []
    } else {
      if (event.name === 'view' && !flushing) {
        this.stored.push(event)
        if (this.options.verbose) console.log('stored: ', this.stored.length)
      }
      if (this._res) {
        let payload = ''
        if (event.name) {
          payload += `event: ${event.name}\n`
        }
        if (event.data) {
          const lines = event.data.split('\n');
          payload += `data: ${lines.join('\ndata: ')}\n`
        }
        if (payload != '') {
          payload += '\n'
          this._res.write(payload)
        }
        if (this.options.verbose) {
          console.error('Sending SSE:')
          process.stderr.write(payload)
        }
      } else if (event.name !== 'view') {
        this.backlog.push(event)
        if (this.options.verbose) console.log('backlog: ', this.backlog.length)
      }
    }
  }

  flush () {
    for (const event of this.stored) {
      this.sendEvent(event, true)
    }
    while (this.backlog.length) {
      const event = this.backlog.shift()
      this.sendEvent(event, true)
    }
  }

  end () {
    if (this._res) this._res.end()
  }
}

export default EventQueue
