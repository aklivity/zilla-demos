FROM golang:1.21-alpine AS build
WORKDIR /src
COPY . .
RUN go build -o /bin/gateway ./main.go

FROM alpine
COPY --from=build /bin/gateway /usr/bin/gateway
EXPOSE 8080
CMD ["gateway", "-logtostderr=true"]
