FROM golang:1.19 AS build

WORKDIR /go/grpc-go
COPY . .

RUN go build server.go

FROM alpine AS run
COPY --from=build /go/grpc-go/server .
ENTRYPOINT ["./server"]
