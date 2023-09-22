package main

import (
	"bufio"
	"context"
	"encoding/json"
	"flag"
	"fmt"
	"io"
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
	servicePort   = env.GetIntDefault("SERVICE_PORT", 50051)
	brokerURL     = env.GetDefault("BROKER_HOST", "localhost")
	brokerPort    = env.GetIntDefault("BROKER_PORT", 1883)
	printSim      = env.GetBoolDefault("PRINT_SIM_LOGS", false)
	defaultRoutes = env.GetBoolDefault("DEFAUlT_ROUTES", false)
	logChannel  = make(chan string)
)

type dataConfig struct {
	Name        string      `json:"NAME"`
	PayloadRoot any         `json:"PAYLOAD_ROOT"`
	Type        string      `json:"TYPE"`
	Values      [][]float64 `json:"VALUES"`
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
	Retain          bool          `json:"RETAIN"`
	Topics          []topicConfig `json:"TOPICS"`
}

type taxiRouteServer struct {
	pb.UnimplementedTaxiRouteServer
}

func printStdPipe(pipe io.ReadCloser, ch chan string) {
	go func(ch chan string) {
		defer glog.Flush()
		reader := bufio.NewReader(pipe)
		for {
			line, err := reader.ReadString('\n')
			if err != nil {
				glog.Fatal(err)
				glog.Flush()
				close(ch)
				return
			}
			for err == nil {
				glog.Info(line)
				glog.Flush()
				line, err = reader.ReadString('\n')
			}
			ch <- line
		}
	}(ch)
}

func runSim(fileName string) {
	defer glog.Flush()
	cmd := exec.Command("python3", "mqtt-simulator/main.py", "-f", fileName)
	glog.Info("python3", "mqtt-simulator/main.py", "-f", fileName)
	pipe, _ := cmd.StdoutPipe()
	pipeErr, _ := cmd.StderrPipe()
	if err := cmd.Start(); err != nil {
		glog.Fatal(err)
		glog.Flush()
	}
	glog.Info("Running simulator for file: ", fileName)
	if printSim {
		printStdPipe(pipe, logChannel)
		printStdPipe(pipeErr, logChannel)
	}
	go func() {
		defer glog.Flush()
		if err := cmd.Wait(); err != nil {
			if exiterr, ok := err.(*exec.ExitError); ok {
				glog.Info("Exit Status: ", exiterr.ExitCode())
				glog.Info(exiterr)
			} else {
				glog.Info("Exit Status: ", exiterr.ExitCode())
				glog.Fatal("Simulation Error: ", err)
			}
		}
		if fileName != "" {
			glog.Info("Simulation done deleting: ", fileName)
			// os.Remove(fileName)
		}
		
	}()
}

func (s *taxiRouteServer) CreateTaxi(ctx context.Context, in *pb.Route) (*emptypb.Empty, error) {
	defer glog.Flush()
	file, errs := os.CreateTemp("", fmt.Sprintf("%s-route-*.json", in.GetKey()))
	if errs != nil {
		glog.Fatal(errs)
	}

	inJson := protojson.Format(in)

	var coords struct {
		Values [][]float64 `json:"coordinates"`
	}
	json.Unmarshal([]byte(inJson), &coords)
	endMark := coords.Values[len(coords.Values)-1]
	endMark = append(endMark, -1)
	coords.Values = append(coords.Values, endMark)

	simConfig := simulatorConfig{
		BrokerURL:       brokerURL,
		BrokerPort:      brokerPort,
		ProtocolVersion: 5,
		CleanSession:    false,
		Qos:             0,
		Retain:          true,
		Topics: []topicConfig{
			{
				Type:         "single",
				Prefix:       in.GetKey(),
				TimeInterval: int(in.GetDuration() / float64(len(coords.Values))),
				Data: []dataConfig{
					{
						Name: "coordinate",
						PayloadRoot: struct {
							Key string `json:"key"`
						}{Key: in.GetKey()},
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

	if printSim {
		glog.Info(simConfig)
	}

	runSim(file.Name())

	return &emptypb.Empty{}, errs
}

func main() {
	flag.Parse()
	defer glog.Flush()
	defer close(logChannel)

	if _, err := os.Stat("mqtt-simulator/main.py"); err == nil {
		glog.Info("Simulator files exist\n")
		if _, err := os.Stat("default_routes.json"); defaultRoutes && err == nil {
			glog.Info("default_routes file exist\n")
			if defaultRoutes {
				runSim("default_routes.json")
			}
		}
	} else {
		glog.Info("Simulator files do not exist\n")
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
