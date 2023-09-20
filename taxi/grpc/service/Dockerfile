FROM golang:1.21-alpine AS build
WORKDIR /src
COPY . .
RUN go build -o /bin/service ./main.go

FROM python:3.11-alpine

WORKDIR /usr/src/app

COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt
COPY default_routes.json default_routes.json
COPY mqtt-simulator mqtt-simulator

COPY --from=build /bin/service /usr/bin/service
EXPOSE 50051
CMD ["service", "-logtostderr=true"]
