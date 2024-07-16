FROM node:20 AS build

WORKDIR /opt/client
RUN git clone -b @grpc/grpc-js@1.11.0 --depth 1 --shallow-submodules https://github.com/grpc/grpc-node
WORKDIR /opt/client/grpc-node/examples
RUN npm install

FROM node:20-alpine AS runner

COPY --from=build /opt/client/grpc-node/examples /examples

ENTRYPOINT ["node", "/examples/metadata/server.js"]
