# mcp-gateway-demo

An MCP gateway that puts three very different kinds of upstream behind one Zilla MCP endpoint, over Streamable HTTP on port `7114`:

| Toolkit | Upstream | Zilla binding |
|---|---|---|
| `everything` | [`@modelcontextprotocol/server-everything`](https://github.com/modelcontextprotocol/servers), a public reference MCP server | `mcp · client` |
| `github` | A mock GitHub REST API, described by `etc/specs/github.openapi.yaml`, with no MCP server involved | `mcp-openapi · client` |
| `kafka` | A real single-node Kafka broker, exposed as `produce_message` and `consume_messages` tools | `mcp-kafka · client` |

Every toolkit goes through the same `authn_jwt` guard and the same listing cache. The `scope` claim in a caller's JWT decides which tools show up in `tools/list`, so a caller only sees the tools its token authorizes.

| Port | Service |
|---|---|
| `7114` | Zilla, MCP over Streamable HTTP at `/mcp` |
| `7190` | Zilla, Prometheus metrics |
| `8080` | Kafka UI |
| `9092` | Kafka |

## Requirements

- [Docker Compose](https://docs.docker.com/compose/gettingstarted/)

## Start

```bash
docker compose up --wait
```

## Mint a Token

```bash
export JWT_FULL=$(docker compose run --rm jwt-cli encode \
    --alg "RS256" --kid "example" \
    --iss "https://auth.example.com" --aud "https://api.example.com" \
    --exp=+1d --no-iat \
    --payload "scope=github:tools github:pr:write github:issues:write kafka:tools" \
    --secret @/private.pem | tr -d '\r\n')
```

| Scope | Unlocks |
|---|---|
| `github:tools` | Read-only GitHub operations |
| `github:pr:write` | Create and update pull requests |
| `github:issues:write` | Create and update issues and comments |
| `kafka:tools` | Produce to and consume from the `orders` topic |

`private.pem` signs the test tokens for this demo only.

## Connect an MCP Client

Register the gateway with Claude Code:

```bash
claude mcp add zilla-mcp-gateway --transport http http://localhost:7114/mcp \
    --header "Authorization: Bearer $JWT_FULL"
```

Any MCP client that speaks Streamable HTTP can connect to `http://localhost:7114/mcp` with the same header, such as MCP Inspector:

```bash
npx @modelcontextprotocol/inspector --web \
    --transport http --server-url http://localhost:7114/mcp \
    --header "Authorization: Bearer $JWT_FULL"
```

The [Get Started: MCP Gateway](https://docs.aklivity.io/latest/ai-gateway/get-started/) guide walks through the demo step by step: scope-filtered `tools/list`, the eager tool cache, tool search, Kafka tools, and an unauthorized tool call.

## Stop

```bash
docker compose down
```
