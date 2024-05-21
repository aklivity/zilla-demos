<template>
  <div class="newTodo">
    <form @submit.prevent="addTodo()">
      <label>New Todo</label>
      <input
          v-model="newTodo"
          name="newTodo"
          ref="newTodo"
          autocomplete="off"
      >
      <button>Add Todo</button>
    </form>
  </div>
</template>

<script>
import { v4 } from 'uuid';
import { useAuth0 } from '@auth0/auth0-vue';

export default {
  name: 'NewTodo',
  setup() {
    const auth0 = useAuth0();
    return {
      auth0: auth0
    }
  },
  props: {
    taskCommandUrl: String
  },
  methods: {
    async addTodo() {
      const newTodo = this.$refs.newTodo;
      if (newTodo.value) {
        let authorization = {};
        if (this.auth0.isAuthenticated.value) {
          const accessToken = await this.auth0.getAccessTokenSilently();
          authorization = { Authorization: `Bearer ${accessToken}` };
        }
        const requestOptions = {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "Idempotency-Key": v4(),
            ...authorization
          },
          body: JSON.stringify({"name": newTodo.value})
        };
        await fetch(this.taskCommandUrl, requestOptions);
        newTodo.value = '';
      }
    }
  }
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
