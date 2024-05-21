<template>
  <div id="main">
    <div>
      <img className="center" alt="Zilla logo" src="../assets/logo.png">
    </div>
    <h1>Todo App</h1>
    <NewTodo :taskCommandUrl="taskApiUrl" />
    <TodoList :taskCommandUrl="taskApiUrl"/>
  </div>
</template>

<script>
import NewTodo from '../components/NewTodo'
import TodoList from '../components/TodoList'
import { Buffer } from 'buffer';
import {useAuth0} from "@auth0/auth0-vue";

export default {
  name: 'Home',
  setup() {
    const auth0 = useAuth0();
    return {
      auth0: auth0
    }
  },
  components: {
    NewTodo,
    TodoList
  },
  data() {
    return {
      taskApiUrl: "http://localhost:8080/tasks",
    }
  }
}
</script>

<style lang="scss">
$border: 2px solid rgba(
    $color: white,
    $alpha: 0.35,
);
$size1: 6px;
$size2: 12px;
$size3: 18px;
$size4: 24px;
$size5: 48px;
$backgroundColor: #27292d;
$textColor: white;
$primaryColor: #0d9b76;
$secondTextColor: #1f2023;
body {
  margin: 0;
  padding: 0;
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: $backgroundColor;
  color: $textColor;
  #main {
    max-width: 600px;
    margin-left: auto;
    margin-right: auto;
    padding: 20px;
    h1 {
      font-weight: bold;
      font-size: 28px;
      text-align: center;
    }
    div {
      text-align: center;
      img {
        width: 250px;
        height: auto;
      }
    }
    form {
      display: flex;
      flex-direction: column;
      width: 100%;
      label {
        font-size: 14px;
        font-weight: bold;
      }
      input,
      button {
        height: $size5;
        box-shadow: none;
        outline: none;
        padding-left: $size2;
        padding-right: $size2;
        border-radius: $size1;
        font-size: 18px;
        margin-top: $size1;
        margin-bottom: $size2;
      }
      input {
        background-color: transparent;
        border: $border;
        color: inherit;
      }
    }
    button {
      cursor: pointer;
      background-color: $primaryColor;
      border: 1px solid $primaryColor;
      color: $secondTextColor;
      font-weight: bold;
      outline: none;
      border-radius: $size1;
      margin-left: $size1;
    }
    h2 {
      font-size: 22px;
      border-bottom: $border;
      padding-bottom: $size1;
    }
    ul {
      padding: 10px;
      li {
        display: flex;
        justify-content: space-between;
        border: $border;
        padding: $size2 $size4;
        border-radius: $size1;
        margin-bottom: $size2;
        span {
            width: 80%;
        }
        .done {
          text-decoration: line-through;
        }
        button {
          font-size: $size2;
          padding: $size1;
        }
        input {
          font-size: $size2;
          padding: $size1;
        }
        form {
          height: 10%;
        }
      }
    }
    h4 {
      text-align: center;
      opacity: 0.5;
      margin: 0;
    }
  }
}
</style>
