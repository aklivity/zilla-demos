package example;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.54.1)",
    comments = "Source: demo.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class DemoServiceGrpc {

  private DemoServiceGrpc() {}

  public static final String SERVICE_NAME = "example.DemoService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<example.Demo.DemoMessage,
      example.Demo.DemoMessage> getDemoUnaryMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DemoUnary",
      requestType = example.Demo.DemoMessage.class,
      responseType = example.Demo.DemoMessage.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<example.Demo.DemoMessage,
      example.Demo.DemoMessage> getDemoUnaryMethod() {
    io.grpc.MethodDescriptor<example.Demo.DemoMessage, example.Demo.DemoMessage> getDemoUnaryMethod;
    if ((getDemoUnaryMethod = DemoServiceGrpc.getDemoUnaryMethod) == null) {
      synchronized (DemoServiceGrpc.class) {
        if ((getDemoUnaryMethod = DemoServiceGrpc.getDemoUnaryMethod) == null) {
          DemoServiceGrpc.getDemoUnaryMethod = getDemoUnaryMethod =
              io.grpc.MethodDescriptor.<example.Demo.DemoMessage, example.Demo.DemoMessage>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DemoUnary"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  example.Demo.DemoMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  example.Demo.DemoMessage.getDefaultInstance()))
              .setSchemaDescriptor(new DemoServiceMethodDescriptorSupplier("DemoUnary"))
              .build();
        }
      }
    }
    return getDemoUnaryMethod;
  }

  private static volatile io.grpc.MethodDescriptor<example.Demo.DemoMessage,
      example.Demo.DemoMessage> getDemoClientStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DemoClientStream",
      requestType = example.Demo.DemoMessage.class,
      responseType = example.Demo.DemoMessage.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<example.Demo.DemoMessage,
      example.Demo.DemoMessage> getDemoClientStreamMethod() {
    io.grpc.MethodDescriptor<example.Demo.DemoMessage, example.Demo.DemoMessage> getDemoClientStreamMethod;
    if ((getDemoClientStreamMethod = DemoServiceGrpc.getDemoClientStreamMethod) == null) {
      synchronized (DemoServiceGrpc.class) {
        if ((getDemoClientStreamMethod = DemoServiceGrpc.getDemoClientStreamMethod) == null) {
          DemoServiceGrpc.getDemoClientStreamMethod = getDemoClientStreamMethod =
              io.grpc.MethodDescriptor.<example.Demo.DemoMessage, example.Demo.DemoMessage>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DemoClientStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  example.Demo.DemoMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  example.Demo.DemoMessage.getDefaultInstance()))
              .setSchemaDescriptor(new DemoServiceMethodDescriptorSupplier("DemoClientStream"))
              .build();
        }
      }
    }
    return getDemoClientStreamMethod;
  }

  private static volatile io.grpc.MethodDescriptor<example.Demo.DemoMessage,
      example.Demo.DemoMessage> getDemoServerStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DemoServerStream",
      requestType = example.Demo.DemoMessage.class,
      responseType = example.Demo.DemoMessage.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<example.Demo.DemoMessage,
      example.Demo.DemoMessage> getDemoServerStreamMethod() {
    io.grpc.MethodDescriptor<example.Demo.DemoMessage, example.Demo.DemoMessage> getDemoServerStreamMethod;
    if ((getDemoServerStreamMethod = DemoServiceGrpc.getDemoServerStreamMethod) == null) {
      synchronized (DemoServiceGrpc.class) {
        if ((getDemoServerStreamMethod = DemoServiceGrpc.getDemoServerStreamMethod) == null) {
          DemoServiceGrpc.getDemoServerStreamMethod = getDemoServerStreamMethod =
              io.grpc.MethodDescriptor.<example.Demo.DemoMessage, example.Demo.DemoMessage>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DemoServerStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  example.Demo.DemoMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  example.Demo.DemoMessage.getDefaultInstance()))
              .setSchemaDescriptor(new DemoServiceMethodDescriptorSupplier("DemoServerStream"))
              .build();
        }
      }
    }
    return getDemoServerStreamMethod;
  }

  private static volatile io.grpc.MethodDescriptor<example.Demo.DemoMessage,
      example.Demo.DemoMessage> getDemoBidiStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DemoBidiStream",
      requestType = example.Demo.DemoMessage.class,
      responseType = example.Demo.DemoMessage.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<example.Demo.DemoMessage,
      example.Demo.DemoMessage> getDemoBidiStreamMethod() {
    io.grpc.MethodDescriptor<example.Demo.DemoMessage, example.Demo.DemoMessage> getDemoBidiStreamMethod;
    if ((getDemoBidiStreamMethod = DemoServiceGrpc.getDemoBidiStreamMethod) == null) {
      synchronized (DemoServiceGrpc.class) {
        if ((getDemoBidiStreamMethod = DemoServiceGrpc.getDemoBidiStreamMethod) == null) {
          DemoServiceGrpc.getDemoBidiStreamMethod = getDemoBidiStreamMethod =
              io.grpc.MethodDescriptor.<example.Demo.DemoMessage, example.Demo.DemoMessage>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DemoBidiStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  example.Demo.DemoMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  example.Demo.DemoMessage.getDefaultInstance()))
              .setSchemaDescriptor(new DemoServiceMethodDescriptorSupplier("DemoBidiStream"))
              .build();
        }
      }
    }
    return getDemoBidiStreamMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static DemoServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DemoServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DemoServiceStub>() {
        @java.lang.Override
        public DemoServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DemoServiceStub(channel, callOptions);
        }
      };
    return DemoServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static DemoServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DemoServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DemoServiceBlockingStub>() {
        @java.lang.Override
        public DemoServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DemoServiceBlockingStub(channel, callOptions);
        }
      };
    return DemoServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static DemoServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<DemoServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<DemoServiceFutureStub>() {
        @java.lang.Override
        public DemoServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new DemoServiceFutureStub(channel, callOptions);
        }
      };
    return DemoServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void demoUnary(example.Demo.DemoMessage request,
        io.grpc.stub.StreamObserver<example.Demo.DemoMessage> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDemoUnaryMethod(), responseObserver);
    }

    /**
     */
    default io.grpc.stub.StreamObserver<example.Demo.DemoMessage> demoClientStream(
        io.grpc.stub.StreamObserver<example.Demo.DemoMessage> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getDemoClientStreamMethod(), responseObserver);
    }

    /**
     */
    default void demoServerStream(example.Demo.DemoMessage request,
        io.grpc.stub.StreamObserver<example.Demo.DemoMessage> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDemoServerStreamMethod(), responseObserver);
    }

    /**
     */
    default io.grpc.stub.StreamObserver<example.Demo.DemoMessage> demoBidiStream(
        io.grpc.stub.StreamObserver<example.Demo.DemoMessage> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getDemoBidiStreamMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service DemoService.
   */
  public static abstract class DemoServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return DemoServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service DemoService.
   */
  public static final class DemoServiceStub
      extends io.grpc.stub.AbstractAsyncStub<DemoServiceStub> {
    private DemoServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DemoServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DemoServiceStub(channel, callOptions);
    }

    /**
     */
    public void demoUnary(example.Demo.DemoMessage request,
        io.grpc.stub.StreamObserver<example.Demo.DemoMessage> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDemoUnaryMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<example.Demo.DemoMessage> demoClientStream(
        io.grpc.stub.StreamObserver<example.Demo.DemoMessage> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncClientStreamingCall(
          getChannel().newCall(getDemoClientStreamMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public void demoServerStream(example.Demo.DemoMessage request,
        io.grpc.stub.StreamObserver<example.Demo.DemoMessage> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getDemoServerStreamMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<example.Demo.DemoMessage> demoBidiStream(
        io.grpc.stub.StreamObserver<example.Demo.DemoMessage> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getDemoBidiStreamMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service DemoService.
   */
  public static final class DemoServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<DemoServiceBlockingStub> {
    private DemoServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DemoServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DemoServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public example.Demo.DemoMessage demoUnary(example.Demo.DemoMessage request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDemoUnaryMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<example.Demo.DemoMessage> demoServerStream(
        example.Demo.DemoMessage request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getDemoServerStreamMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service DemoService.
   */
  public static final class DemoServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<DemoServiceFutureStub> {
    private DemoServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected DemoServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new DemoServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<example.Demo.DemoMessage> demoUnary(
        example.Demo.DemoMessage request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDemoUnaryMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_DEMO_UNARY = 0;
  private static final int METHODID_DEMO_SERVER_STREAM = 1;
  private static final int METHODID_DEMO_CLIENT_STREAM = 2;
  private static final int METHODID_DEMO_BIDI_STREAM = 3;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_DEMO_UNARY:
          serviceImpl.demoUnary((example.Demo.DemoMessage) request,
              (io.grpc.stub.StreamObserver<example.Demo.DemoMessage>) responseObserver);
          break;
        case METHODID_DEMO_SERVER_STREAM:
          serviceImpl.demoServerStream((example.Demo.DemoMessage) request,
              (io.grpc.stub.StreamObserver<example.Demo.DemoMessage>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_DEMO_CLIENT_STREAM:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.demoClientStream(
              (io.grpc.stub.StreamObserver<example.Demo.DemoMessage>) responseObserver);
        case METHODID_DEMO_BIDI_STREAM:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.demoBidiStream(
              (io.grpc.stub.StreamObserver<example.Demo.DemoMessage>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getDemoUnaryMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              example.Demo.DemoMessage,
              example.Demo.DemoMessage>(
                service, METHODID_DEMO_UNARY)))
        .addMethod(
          getDemoClientStreamMethod(),
          io.grpc.stub.ServerCalls.asyncClientStreamingCall(
            new MethodHandlers<
              example.Demo.DemoMessage,
              example.Demo.DemoMessage>(
                service, METHODID_DEMO_CLIENT_STREAM)))
        .addMethod(
          getDemoServerStreamMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              example.Demo.DemoMessage,
              example.Demo.DemoMessage>(
                service, METHODID_DEMO_SERVER_STREAM)))
        .addMethod(
          getDemoBidiStreamMethod(),
          io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
            new MethodHandlers<
              example.Demo.DemoMessage,
              example.Demo.DemoMessage>(
                service, METHODID_DEMO_BIDI_STREAM)))
        .build();
  }

  private static abstract class DemoServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    DemoServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return example.Demo.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("DemoService");
    }
  }

  private static final class DemoServiceFileDescriptorSupplier
      extends DemoServiceBaseDescriptorSupplier {
    DemoServiceFileDescriptorSupplier() {}
  }

  private static final class DemoServiceMethodDescriptorSupplier
      extends DemoServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    DemoServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (DemoServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new DemoServiceFileDescriptorSupplier())
              .addMethod(getDemoUnaryMethod())
              .addMethod(getDemoClientStreamMethod())
              .addMethod(getDemoServerStreamMethod())
              .addMethod(getDemoBidiStreamMethod())
              .build();
        }
      }
    }
    return result;
  }
}
