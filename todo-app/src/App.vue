<template>
  <div id="app" class="d-flex flex-column h-100">
    <nav-bar />
    <div class="container flex-grow-1">
      <auth-error />
      <div class="mt-5">
        <router-view />
      </div>
    </div>
  </div>
</template>
<script>
import NavBar from "./components/NavBar";
import AuthError from "./components/AuthError";

export default {
  components: {
    NavBar,
    AuthError
  }
};
</script>
