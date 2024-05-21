<template>
  <h2>Todo List</h2>
  <ul  ref="tasks">
    <todo
        v-for="(task, key) in tasks"
        :key="key"
        :name="task.name"
        @on-delete="removeTodo(key)"
        @on-edit="editTodo(key, task.etag, $event)"
    />
  </ul>
</template>

<script>
import Todo from "./Todo.vue";
import {ref, watch} from 'vue';
import {v4} from 'uuid';
import {useAuth0} from "@auth0/auth0-vue";
import {Buffer} from "buffer";

window.Buffer = window.Buffer || Buffer;

export default {
  name: 'TodoList',
  inject: ['$error'],
  setup() {
    const auth0 = useAuth0();
    return {
      auth0: auth0
    }
  },
  data() {
    return {
      tasks: {}
    }
  },
  props: {
    taskCommandUrl: String
  },
  components: {
    Todo
  },
  created() {
    const newTodo = ref('');
    return {
      newTodo
    }
  },
  async mounted() {
    let tasks = this.tasks
    const taskCommandUrl = this.taskCommandUrl;
    const auth0 = this.auth0;
    const error = this.$error;
    let eventStream;
    await readTasks(auth0.isAuthenticated.value)
    async function readTasks(authenticated) {
      let sourceUrl = taskCommandUrl;
      if (authenticated) {
        const accessToken = await auth0.getAccessTokenSilently();
        sourceUrl = `${taskCommandUrl}?access_token=${accessToken}`;
      }

      eventStream?.close();
      eventStream = new EventSource(sourceUrl);
      eventStream.addEventListener('delete', (event) => {
        let lastEventId = JSON.parse(event.lastEventId);
        let key = Buffer.from(lastEventId[0], "base64").toString("utf8");
        delete tasks[key]
      }, false);

      eventStream.onerror = function (err) {
          setTimeout(() => {
            if (error && eventStream.readyState != EventSource.OPEN) {
               error.value = "Login Required";
            }
          }, 3000);
      };

      eventStream.onmessage = function (event) {
        let lastEventId = JSON.parse(event.lastEventId);
        let key = Buffer.from(lastEventId[0], "base64").toString("utf8");
        let task = JSON.parse(event.data)
        tasks[key] = {"name": task.name, "etag": lastEventId[1]}
      };
    }
    watch(this.auth0.isAuthenticated, readTasks);
  },
  methods: {
    async editTodo(key, etag, newTodoName) {
      let authorization = {};
      if (this.auth0.isAuthenticated.value) {
        const accessToken = await this.auth0.getAccessTokenSilently();
        authorization = { Authorization: `Bearer ${accessToken}` };
      }
      const requestOptions = {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          "Idempotency-Key": v4(),
          "If-Match": etag,
          ...authorization
        },
        body: JSON.stringify({"name": newTodoName})
      };
      await fetch(`${this.taskCommandUrl}/${key}`, requestOptions);
    },
    async removeTodo(key) {
      let authorization = {};
      if (this.auth0.isAuthenticated.value) {
        const accessToken = await this.auth0.getAccessTokenSilently();
        authorization = { Authorization: `Bearer ${accessToken}` };
      }
      const requestOptions = {
        method: "DELETE",
        headers: {
          "Content-Type": "application/json",
          "Idempotency-Key": v4(),
          ...authorization
        }
      };
      await fetch(`${this.taskCommandUrl}/${key}`, requestOptions);
    }
  },
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
h3 {
  margin: 40px 0 0;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}
a {
  color: #42b983;
}
</style>
