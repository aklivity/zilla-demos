# sse-server

Pipe an event stream from terminal to browser.

## Synopsis

To generate the certificate and key.

```bash
openssl req -x509 -newkey rsa:2048 -nodes -sha256 -subj '/CN=localhost' \
  -keyout localhost-privkey.pem -out localhost-cert.pem
```

Launch the server.

```
$ sse-server -k ./localhost-privkey.pem -c localhost-cert.pem
SSE server: https://localhost:9000 
Input socket: localhost:9090
```

Pipe events to the input socket.

```
$ echo '{ "name": "something", "data": "one" }' | nc -c localhost 9090
$ echo '{ "name": "something", "data": "two" }' | nc -c localhost 9090
```

Connect a browser to the SSE server to consume the [server-sent events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events).

```
$ curl -k https://localhost:9000
event: something
data: "one"

event: something
data: "two"
```

* * *
