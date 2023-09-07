package main

import (
	// "bufio"
	"context"
	"flag"
	"fmt"
	"net"
	"os"

	// "os/exec"

	pb "github.com/aklivity/zilla-demos/taxi/grpc/service/taxiroute"
	"github.com/golang/glog"
	"google.golang.org/grpc"
	"google.golang.org/protobuf/encoding/protojson"
	emptypb "google.golang.org/protobuf/types/known/emptypb"
)

const (
	port = ":50051"
)

type taxiRouteServer struct {
	pb.UnimplementedTaxiRouteServer
}

func (s *taxiRouteServer) CreateTaxi(ctx context.Context, in *pb.Route) (*emptypb.Empty, error) {
	defer glog.Flush()
	err := os.WriteFile(fmt.Sprintf("%.0f_route.json", in.GetTimestamp()), []byte(protojson.Format(in)), 0644)
	return &emptypb.Empty{}, err
}

func main() {
	flag.Parse()
	defer glog.Flush()

	// cmd := exec.Command("mqtt-simulator/main.py")
	// stdout, err := cmd.StdoutPipe()
	// if err != nil {
	// 	glog.Fatal(err)
	// }

	// // start the command after having set up the pipe
	// if err := cmd.Run(); err != nil {
	// 	glog.Fatal(err)
	// }

	// // read command's stdout line by line
	// in := bufio.NewScanner(stdout)

	// for in.Scan() {
	// 	glog.Info(in.Text()) // write each line to your log, or anything you need
	// 	glog.Flush()
	// }
	// if err := in.Err(); err != nil {
	// 	glog.Fatal(err)
	// }

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
