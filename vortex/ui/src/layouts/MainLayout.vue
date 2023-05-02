<template>
  <q-layout view="hHh lpR fFf">

    <q-header elevated class="bg-primary text-white">
      <q-toolbar>
        <q-toolbar-title class="row items-center no-wrap">
          <img :height="75" src="https://docs.aklivity.io/zilla/latest/logo.png">
          <span class="q-ml-lg">Vortex Demo</span>
        </q-toolbar-title>
      </q-toolbar>
    </q-header>

    <q-page-container>
      <q-page class="flex content-start q-gutter-xs" padding>
        <div v-for="[key, msg] in messages" :key="key">
          <q-tooltip>
            {{msg}} | {{msg.title}} | {{msg.pulse}}
          </q-tooltip>
          <q-circular-progress
            :indeterminate="msg.pulse"
            size="40px"
            :thickness="0.4"
            :value="msg.value"
            font-size="50px"
            :color="msg.color"
            track-color="grey-3"
            center-color="grey-8"
            class="q-ma-md"
          />
        </div>
      </q-page>
    </q-page-container>

  </q-layout>
</template>

<script lang="ts">
import { defineComponent, reactive } from 'vue';

const events = new EventSource('http://localhost:8001/events');

export default defineComponent({
  name: 'MainLayout',

  setup () {
    const messages = reactive(new Map<number, {title:string, color:string, value:number, pulse:boolean}>([]));


    events.onmessage = function ({data}: MessageEvent) {
      var {title, id} = <{title:string, id:number}>JSON.parse(data);
      console.log(id, data);
      if (id){
        messages.set(id, {
          title,
          color: 'blue',
          value: id * 10,
          pulse: true,
        });
        setTimeout(() => {
          messages.set(id, { ...messages.get(id), pulse:false, color: 'green' })
        }, 500);
      }
    };

    return {
      messages,
    }
  },

  onUnmounted () {
    events.close();
    console.log('events closed', events);
  },
});
</script>
