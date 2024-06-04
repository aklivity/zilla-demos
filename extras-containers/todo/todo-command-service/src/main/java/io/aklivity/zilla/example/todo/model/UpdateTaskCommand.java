package io.aklivity.zilla.example.todo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class UpdateTaskCommand implements Command
{
    String id;
    String title;
    Boolean completed;
}
