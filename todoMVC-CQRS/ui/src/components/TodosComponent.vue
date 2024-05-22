<script setup>
import { ref, computed, watch, onBeforeUpdate } from 'vue';
import { useRoute } from 'vue-router';
import {Buffer} from "buffer";

import TodoFooter from './TodoFooter.vue';
import TodoHeader from './TodoHeader.vue';
import TodoItem from './TodoItem.vue';

const todos = ref([]);
const todosRef = (el, index) => todos.value[index] = el
onBeforeUpdate(() => todos.value = [])

// This watcher will run on every update
watch(
  todos,
  (currentTodos, previousTodos) => {
    if (currentTodos.length !== previousTodos.length) {
      // The number of elements has changed. Stale side effects
      // need be cleaned up, and fresh side effects need to run.
      return
    }

    // We've established that the number of elements is the same,
    // but it's still possible that the elements could have changed
    // order.
    for (let i = 0; i < currentTodos.length; i++) {
      if (currentTodos[i].isSameNode(previousTodos[i])) {
        // The elements have changed order. Stale side effects
        // need be cleaned up, and fresh side effects need to run.
        return
      }
    }
  }
)
const route = useRoute();
const todoCommandUrl = "http://localhost:7114/tasks"

let eventStream = new EventSource(todoCommandUrl);
eventStream.addEventListener('delete', (event) => {
    let lastEventId = JSON.parse(event.lastEventId);
    let key = Buffer.from(lastEventId[0], "base64").toString("utf8");
    todos.value = todos.value.filter((t) => t.id !== key);
}, false);

// eventStream.onerror = function () {
// };

eventStream.onmessage = function (event) {
    let lastEventId = JSON.parse(event.lastEventId);
    let key = Buffer.from(lastEventId[0], "base64").toString("utf8");
    let todo = JSON.parse(event.data);
    // let newTodo = !todosMap[key]
    // todosMap[key] = {
    //     ...todo,
    //     "etag": lastEventId[1],
    //     key,
    // }
    // if (newTodo) {
    //     todos.value[key] = todosMap[key]
    // }
    todosRef({
        ...todo,
        "etag": lastEventId[1],
        key,
    }, key)

    console.log(todosMap[key]);
};

const filters = {
    all: (todos) => todos,
    active: (todos) => todos.value.filter((todo) => !todo.completed),
    completed: (todos) => todos.value.filter((todo) => todo.completed),
};

const activeTodos = computed(() => filters.active(todos));
const completedTodos = computed(() => filters.completed(todos));
const filteredTodos = computed(() => {
    switch(route.name) {
        case "active":
            return activeTodos;
        case "completed":
            return completedTodos;
        default:
            return todos;
    }
});

const toggleAllModel = computed({
    get() {
        return activeTodos.value.length === 0;
    },
    set(value) {
        todos.value.forEach((todo) => {
            todo.completed = value;
        });
    },
});

function uuid() {
    let uuid = "";
    for (let i = 0; i < 32; i++) {
        let random = (Math.random() * 16) | 0;

        if (i === 8 || i === 12 || i === 16 || i === 20)
            uuid += "-";

        uuid += (i === 12 ? 4 : i === 16 ? (random & 3) | 8 : random).toString(16);
    }
    return uuid;
}

function addTodo(value) {
    fetch(todoCommandUrl, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            title: value,
        })
    });
}

function deleteTodo(todo) {
    fetch(`${todoCommandUrl}/${todo.id}`, {
        method: "DELETE",
        headers: {
          "Content-Type": "application/json",
        //   "Idempotency-Key": uuid(),
        }
    });
}

function toggleTodo(todo, value) {
    // todo.completed = value;
    fetch(`${todoCommandUrl}/${todo.id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        //   "Idempotency-Key": uuid(),
        //   "If-Match": etag,
        },
        body: JSON.stringify({...todo, completed: value})
    });
};

function editTodo(todo, value) {
    // todo.title = value;
    fetch(`${todoCommandUrl}/${todo.id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        //   "Idempotency-Key": uuid(),
        //   "If-Match": etag,
        },
        body: JSON.stringify({...todo, title: value})
    });
}

function deleteCompleted() {
    todos.value.filter(todo => !todo.completed).forEach(deleteTodo);
}
</script>

<template>
    <TodoHeader @add-todo="addTodo" />
    <main class="main" v-show="todos.length > 0">
        <div class="toggle-all-container">
            <input type="checkbox" id="toggle-all-input" class="toggle-all" v-model="toggleAllModel" :disabled="filteredTodos.value.length === 0"/>
            <label class="toggle-all-label" htmlFor="toggle-all-input"> Toggle All Input </label>
        </div>
        <ul class="todo-list">
            <TodoItem v-for="(todo, index) in filteredTodos.value" :key="todo.id" :todo="todo" :index="index"
                @delete-todo="deleteTodo" @edit-todo="editTodo" @toggle-todo="toggleTodo" />
        </ul>
    </main>
    <TodoFooter :todos="todos" @delete-completed="deleteCompleted" />
</template>
