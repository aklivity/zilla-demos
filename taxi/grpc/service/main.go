package main

import (
	"context"
	"flag"
	"net"

	pb "github.com/aklivity/zilla-demos/taxi/grpc/service/taxiroute"
	"github.com/golang/glog"
	"google.golang.org/grpc"
	emptypb "google.golang.org/protobuf/types/known/emptypb"
)

const (
	port = ":50051"
)

type taxiRouteServer struct{
	pb.UnimplementedTaxiRouteServer
}

func (s *taxiRouteServer) CreateTaxi(ctx context.Context, in *pb.Route) (*emptypb.Empty, error) {
	defer glog.Flush()
	glog.Info("Received: %v", in.Timestamp)
	return &emptypb.Empty{}, nil
}

func main() {
	flag.Parse()
	defer glog.Flush()

	lis, err := net.Listen("tcp", port)
	if err != nil {
		glog.Fatal("failed to listen:", err)
	}
	glog.Info("Listening on port: ", port)
	s := grpc.NewServer()
	pb.RegisterTaxiRouteServer(s, &taxiRouteServer{})
	if err := s.Serve(lis); err != nil {
		glog.Fatal("failed to serve:", err)
	}
	glog.Info("Serving on port: ", port)
}
