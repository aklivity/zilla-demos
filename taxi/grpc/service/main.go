package main

import (
	"context"
	"encoding/json"
	"flag"
	"fmt"
	"net"
	"os"
	"os/exec"

	pb "github.com/aklivity/zilla-demos/taxi/grpc/service/taxiroute"
	env "github.com/caitlinelfring/go-env-default"
	"github.com/golang/glog"
	"google.golang.org/grpc"
	"google.golang.org/protobuf/encoding/protojson"
	emptypb "google.golang.org/protobuf/types/known/emptypb"
)

var (
	servicePort = env.GetIntDefault("SERVICE_PORT", 50051)
)

type dataConfig struct {
	Name   string      `json:"NAME"`
	Type   string      `json:"TYPE"`
	Values [][]float64 `json:"VALUES"`
}

type topicConfig struct {
	Type         string       `json:"TYPE"`
	Prefix       string       `json:"PREFIX"`
	TimeInterval int          `json:"TIME_INTERVAL"`
	Data         []dataConfig `json:"DATA"`
}

type simulatorConfig struct {
	BrokerURL       string        `json:"BROKER_URL"`
	BrokerPort      int           `json:"BROKER_PORT"`
	ProtocolVersion int           `json:"PROTOCOL_VERSION"`
	CleanSession    bool          `json:"CLEAN_SESSION"`
	Qos             int           `json:"QOS"`
	Retained        bool          `json:"RETAINED"`
	Topics          []topicConfig `json:"TOPICS"`
}

type taxiRouteServer struct {
	pb.UnimplementedTaxiRouteServer
}

func (s *taxiRouteServer) CreateTaxi(ctx context.Context, in *pb.Route) (*emptypb.Empty, error) {
	defer glog.Flush()
	file, errs := os.CreateTemp("", fmt.Sprintf("%.0f-route-*.json", in.GetTimestamp()))
	if errs != nil {
		glog.Fatal(errs)
	}
	defer os.Remove(file.Name())

	inJson := protojson.Format(in)

	var coords struct {
		Values [][]float64 `json:"coordinates"`
	}
	json.Unmarshal([]byte(inJson), &coords)
	coords.Values = append(coords.Values, []float64{})

	simConfig := simulatorConfig{
		BrokerURL:       "localhost",
		BrokerPort:      1883,
		ProtocolVersion: 5,
		CleanSession:    false,
		Qos:             0,
		Retained:        true,
		Topics: []topicConfig{
			{
				Type:         "single",
				Prefix:       fmt.Sprintf("%.0f", in.GetTimestamp()),
				TimeInterval: int(in.GetDuration() / float64(len(coords.Values))),
				Data: []dataConfig{
					{
						Name:   "coordinate",
						Type:   "raw_values",
						Values: coords.Values,
					},
				},
			},
		},
	}
	jsonConfig, errs := json.Marshal(simConfig)
	if errs != nil {
		glog.Fatal(errs)
	}
	_, errs = file.Write(jsonConfig)
	if errs != nil {
		glog.Fatal(errs)
	}
	glog.Info(file.Name())
	cmd := exec.Command("python3", "mqtt-simulator/main.py", "-f", file.Name())
	// pipe, _ := cmd.StdoutPipe()
	if err := cmd.Run(); err != nil {
		glog.Fatal(err)
		glog.Flush()
	}
	// reader := bufio.NewReader(pipe)
	// line, err := reader.ReadString('\n')
	// if err != nil {
	// 	glog.Fatal(err)
	// 	glog.Flush()
	// }
	// for err == nil {
	// 	glog.Info(line)
	// 	glog.Flush()
	// 	line, err = reader.ReadString('\n')
	// }

	// save local
	errs = os.WriteFile(fmt.Sprintf("%.0f_route.json", in.GetTimestamp()), jsonConfig, 0644)
	if errs != nil {
		glog.Fatal(errs)
	}
	return &emptypb.Empty{}, errs
}

func main() {
	flag.Parse()
	defer glog.Flush()

	if _, err := os.Stat("mqtt-simulator/main.py"); err == nil {
		glog.Info("Simulator files exists\n");
	 } else {
		glog.Info("Simulator files do not exist\n");
	 }

	lis, err := net.Listen("tcp", fmt.Sprintf(":%d", servicePort))
	if err != nil {
		glog.Fatal("failed to listen:", err)
	}
	glog.Info("Listening on port: ", servicePort)
	s := grpc.NewServer()
	pb.RegisterTaxiRouteServer(s, &taxiRouteServer{})
	if err := s.Serve(lis); err != nil {
		glog.Fatal("failed to serve:", err)
	}
}
