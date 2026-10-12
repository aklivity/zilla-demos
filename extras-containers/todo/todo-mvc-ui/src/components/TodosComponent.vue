<script setup>
import { ref, computed } from 'vue';
import { useRoute } from 'vue-router';
import { Buffer } from "buffer";

import TodoFooter from './TodoFooter.vue';
import TodoHeader from './TodoHeader.vue';
import TodoItem from './TodoItem.vue';

const todos = ref([]);
const todosMap = {};
const route = useRoute();
const todoCommandUrl = "http://localhost:7114/tasks"

let eventStream = new EventSource(todoCommandUrl);
eventStream.addEventListener('delete', (event) => {
    let lastEventId = JSON.parse(event.lastEventId);
    let key = Buffer.from(lastEventId[0], "base64").toString("utf8");
    todos.value = todos.value.filter((t) => t.id !== key);
    delete todosMap[key];
}, false);

eventStream.onerror = console.log;

eventStream.onmessage = function (event) {
    let lastEventId = JSON.parse(event.lastEventId);
    let key = Buffer.from(lastEventId[0], "base64").toString("utf8");
    let todo = {
        ...JSON.parse(event.data),
        "etag": lastEventId[1],
    };

    if (!todosMap[key]) {
        todosMap[key] = todo;
        todos.value.push(todo);
    } else {
        todos.value = todos.value.map((t) => t.id == key ? { ...t, ...todo } : t);
    }
};

const filters = {
    all: (todos) => todos,
    active: (todos) => todos.value.filter((todo) => !todo.completed),
    completed: (todos) => todos.value.filter((todo) => todo.completed),
};

const activeTodos = computed(() => filters.active(todos));
const completedTodos = computed(() => filters.completed(todos));
const filteredTodos = computed(() => {
    switch (route.name) {
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
    if (globalThis.crypto?.randomUUID) {
        return globalThis.crypto.randomUUID();
    }

    const bytes = new Uint8Array(16);
    globalThis.crypto.getRandomValues(bytes);

    // RFC 4122 version 4 UUID
    bytes[6] = (bytes[6] & 0x0f) | 0x40;
    bytes[8] = (bytes[8] & 0x3f) | 0x80;

    const hex = Array.from(bytes, (b) => b.toString(16).padStart(2, "0")).join("");
    return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
}

function addTodo(value) {
    fetch(todoCommandUrl, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Idempotency-Key": `todoId-${uuid()}`,
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
            "Idempotency-Key": todo.id,
        }
    });
}

function toggleTodo(todo, value) {
    fetch(`${todoCommandUrl}/${todo.id}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            "Idempotency-Key": todo.id,
            "If-Match": todo.etag,
        },
        body: JSON.stringify({ ...todo, completed: value })
    });
};

function editTodo(todo, value) {
    fetch(`${todoCommandUrl}/${todo.id}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            "Idempotency-Key": todo.id,
            "If-Match": todo.etag,
        },
        body: JSON.stringify({ ...todo, title: value })
    });
}

function deleteCompleted() {
    todos.value.filter(todo => todo.completed).forEach(deleteTodo);
}
</script>

<template>
    <TodoHeader @add-todo="addTodo" />
    <main class="main" v-show="todos.length > 0">
        <div class="toggle-all-container">
            <input type="checkbox" id="toggle-all-input" class="toggle-all" v-model="toggleAllModel"
                :disabled="filteredTodos.value.length === 0" />
            <label class="toggle-all-label" htmlFor="toggle-all-input"> Toggle All Input </label>
        </div>
        <ul class="todo-list">
            <TodoItem v-for="(todo, index) in filteredTodos.value" :key="todo.id" :todo="todo" :index="index"
                @delete-todo="deleteTodo" @edit-todo="editTodo" @toggle-todo="toggleTodo" />
        </ul>
    </main>
    <TodoFooter :todos="todos" @delete-completed="deleteCompleted" />
</template>
