package main

import (
	"context"
	"flag"
	"fmt"
	"net/http"

	"github.com/golang/glog"
	"github.com/grpc-ecosystem/grpc-gateway/v2/runtime"
	"github.com/rs/cors"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"

	gw "github.com/aklivity/zilla-demos/taxi/grpc/gateway/taxiroute" // Update
	env "github.com/caitlinelfring/go-env-default"
)

var (
	// command-line options:
	// gRPC server endpoint
	grpcServerEndpoint = env.GetDefault("GRPC_SERVER_ENDPOINT", "localhost:7114")
	gatewayPort = env.GetIntDefault("GATEWAY_PORT", 8085)
)

func run() error {
	ctx := context.Background()
	ctx, cancel := context.WithCancel(ctx)
	defer cancel()

	// Register gRPC server endpoint
	// Note: Make sure the gRPC server is running properly and accessible
	mux := runtime.NewServeMux()
	opts := []grpc.DialOption{grpc.WithTransportCredentials(insecure.NewCredentials())}
	err := gw.RegisterTaxiRouteHandlerFromEndpoint(ctx, mux, grpcServerEndpoint, opts)
	if err != nil {
		return err
	}

	handler := cors.New(cors.Options{
		AllowedOrigins: []string{"*"},
		AllowedMethods: []string{
			http.MethodPost,
			http.MethodGet,
		},
		AllowedHeaders:   []string{"*"},
		AllowCredentials: false,
	}).Handler(mux)
  
	// Start HTTP server (and proxy calls to gRPC server endpoint)
	glog.Info("running on :", gatewayPort)
	return http.ListenAndServe(fmt.Sprintf(":%d", gatewayPort), handler)
}

func main() {
	flag.Parse()
	defer glog.Flush()

	if err := run(); err != nil {
		glog.Fatal(err)
	}
}
