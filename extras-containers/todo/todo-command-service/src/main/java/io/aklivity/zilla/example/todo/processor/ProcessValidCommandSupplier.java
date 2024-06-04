package io.aklivity.zilla.example.todo.processor;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.logging.log4j.util.Strings;

import io.aklivity.zilla.example.todo.model.Command;
import io.aklivity.zilla.example.todo.model.CreateTaskCommand;
import io.aklivity.zilla.example.todo.model.DeleteTaskCommand;
import io.aklivity.zilla.example.todo.model.UpdateTaskCommand;
import io.aklivity.zilla.example.todo.model.Task;
import io.aklivity.zilla.example.todo.model.TaskSnapshotState;

public class ProcessValidCommandSupplier implements ProcessorSupplier<String, Command, String, Object>
{
    private final String taskSnapshotStateStoreName;
    private final String snapshot;
    private final String replyTo;

    private KeyValueStore<String, TaskSnapshotState> taskSnapshotStore;

    public ProcessValidCommandSupplier(String taskSnapshotStateStoreName, String snapshot, String replyTo)
    {
        this.taskSnapshotStateStoreName = taskSnapshotStateStoreName;
        this.snapshot = snapshot;
        this.replyTo = replyTo;
    }

    @Override
    public Processor<String, Command, String, Object> get()
    {
        return new ProcessValidCommand();
    }

    class ProcessValidCommand implements Processor<String, Command, String, Object>
    {
        ProcessorContext context;

        @Override
        public void init(final ProcessorContext context)
        {
            this.context = context;
            taskSnapshotStore = (KeyValueStore) context.getStateStore(taskSnapshotStateStoreName);
        }

        @Override
        public void process(Record<String, Command> newCommand)
        {
            final String key = newCommand.key();
            final Command command = newCommand.value();
            final Headers headers = newCommand.headers();
//            final Header idempotencyKeyHeader = headers.lastHeader("idempotency-key");
            final Header correlationId = headers.lastHeader("zilla:correlation-id");
            final Headers newResponseHeaders = new RecordHeaders();
            newResponseHeaders.add(correlationId);
            final Headers newSnapshotHeaders = new RecordHeaders();
            final Header contentType = new RecordHeader("content-type", "application/json".getBytes());

//            if (idempotencyKeyHeader == null)
//            {
//                newResponseHeaders.add(":status", "400".getBytes());
//                final Record reply = newCommand.withHeaders(newResponseHeaders).withValue("Missing idempotency-key header");
//                context.forward(reply, replyTo);
//            }
            if (command instanceof CreateTaskCommand)
            {
                String etagValue = "1";
                newSnapshotHeaders.add("etag", etagValue.getBytes());
                newSnapshotHeaders.add(contentType);
                final Record newSnapshot = newCommand
                        .withHeaders(newSnapshotHeaders)
                        .withValue(Task.builder()
                                .id(key)
                                .title(((CreateTaskCommand) command).getTitle())
                                .completed(false)
                                .build());
                context.forward(newSnapshot, snapshot);
                taskSnapshotStore.putIfAbsent(key, TaskSnapshotState.builder()
                        .etag(etagValue).idempotencyKey(key.getBytes()).build());

                final Header path = headers.lastHeader(":path");
                newResponseHeaders.add(":status", "201".getBytes());
                newResponseHeaders.add("location", String.format("%s/%s", new String(path.value()),
                        new String(key)).getBytes());
                final Record reply = newCommand.withHeaders(newResponseHeaders).withValue(Strings.EMPTY);
                context.forward(reply, replyTo);
            }
            else if (command instanceof UpdateTaskCommand)
            {
                final TaskSnapshotState taskSnapshotState = taskSnapshotStore.get(key);
                final String currentEtag = taskSnapshotState.getEtag();
                final String newEtag = Integer.toString(Integer.parseInt(currentEtag) + 1);
                taskSnapshotState.setEtag(newEtag);
                newSnapshotHeaders.add("etag", newEtag.getBytes());
                newSnapshotHeaders.add(contentType);
                final Record newSnapshot = newCommand
                        .withHeaders(newSnapshotHeaders)
                        .withValue(Task.builder()
                                .id(((UpdateTaskCommand) command).getId())
                                .title(((UpdateTaskCommand) command).getTitle())
                                .completed(((UpdateTaskCommand) command).getCompleted())
                                .build());
                context.forward(newSnapshot, snapshot);
                taskSnapshotStore.put(key, taskSnapshotState);

                newResponseHeaders.add(":status", "204".getBytes());
                final Record reply = newCommand.withHeaders(newResponseHeaders).withValue(Strings.EMPTY);
                context.forward(reply, replyTo);
            }
            else if (command instanceof DeleteTaskCommand)
            {
                final Record newSnapshot = newCommand.withHeaders(newResponseHeaders).withValue(null);
                context.forward(newSnapshot, snapshot);
                taskSnapshotStore.delete(key);

                newResponseHeaders.add(":status", "204".getBytes());
                final Record reply = newCommand.withHeaders(newResponseHeaders).withValue(Strings.EMPTY);
                context.forward(reply, replyTo);
            }
            else
            {
                newResponseHeaders.add(":status", "400".getBytes());
                final Record reply = newCommand.withHeaders(newResponseHeaders).withValue("Unsupported command");
                context.forward(reply, replyTo);
            }
        }
    }
}
