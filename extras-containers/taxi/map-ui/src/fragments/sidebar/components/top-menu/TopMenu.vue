<template>
  <v-layout row class="top-menu">
    <v-flex sm4>
      <v-btn flat class="no-padding" v-if="$mdAndDownResolution" @click="toggleSidebarIsPinned()">
        <v-icon :title="$t('topMenu.pinSidebar')" :medium="$lowResolution"
          :color="isPinned ? 'primary': 'dark'">
          lock</v-icon>
      </v-btn>
    </v-flex>
    <v-flex sm4>
      <v-btn class="no-padding pr2" flat @click="toggleSidebar()">
        <v-icon :title="$t('global.close')" color="dark" :medium="$lowResolution">close </v-icon>
      </v-btn>
    </v-flex>
  </v-layout>
</template>

<script src="./top-menu.js"></script>
<style scoped src="./top-menu.css"></style>
