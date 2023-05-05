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
      <q-page class="q-pa-sm">
        <div class="row items-start q-gutter-sm">
          <q-responsive :ratio="1" class="col"  style="max-width: 30%; max-height: 90vh">
            <div class="q-gutter-lg">
              <div class="row justify-end text-h4 q-pa-md">
                Send a Message
              </div>
              <div class="row justify-end">
                <q-input filled v-model="title" label="Title" class="col" />
              </div>
              <div class="row justify-end">
                <q-input
                  filled
                  v-model="color"
                  class="col"
                >
                  <template v-slot:append>
                    <q-icon name="colorize" class="cursor-pointer">
                      <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                        <q-color
                          no-header
                          no-footer
                          default-view="palette"
                          v-model="color" />
                      </q-popup-proxy>
                    </q-icon>
                  </template>
                </q-input>
              </div>

              <div class="row justify-end">
                <q-btn @click="sendMsg" color="primary" label="Send" />
              </div>
            </div>
          </q-responsive>

          <q-responsive :ratio="1" class="col"  style="max-width: 70%; max-height: 90vh; overflow: scroll;">
            <div class="flex content-start q-gutter-xs">
              <div v-for="[key, msg] in messages" :key="key">
                <q-tooltip>
                  {{msg.title}}
                </q-tooltip>
                <q-circular-progress
                  :style="{ color: msg.color }"
                  :indeterminate="msg.pulse"
                  size="40px"
                  :thickness="0.4"
                  :value="msg.value"
                  font-size="50px"
                  track-color="transparent"
                  center-color="grey-4"
                  class="q-ma-md"
                />
              </div>
            </div>
          </q-responsive>
        </div>

      </q-page>
    </q-page-container>

  </q-layout>
</template>

<script lang="ts">
import { defineComponent, reactive, ref } from 'vue';

const events = new EventSource('http://localhost:8080/events');

export default defineComponent({
  name: 'MainLayout',

  setup () {
    const messages = reactive(new Map<number, {title:string, color:string, value:number, pulse:boolean}>([]));

    events.onmessage = function ({data}: MessageEvent) {
      var {title, id, color, value} = <{title:string, id:number, color:string, value:number}>JSON.parse(data);
      console.log(id, data);
      if (id){
        messages.set(id, {
          title,
          color: 'blue',
          value: value,
          pulse: true,
        });
        setTimeout(() => {
          messages.set(id, { ...messages.get(id), pulse:false, color: color })
          //send message back over http
        }, 500);
      }
    };

    for (let i = 1; i <= 300; i++) {
      messages.set(i, {
        title: `hello ${i}`,
        color: 'green',
        value: i,
        pulse: false,
      });
    }



    return {
      messages,
      title: ref(''),
      color: ref(''),
    }
  },

  onUnmounted () {
    events.close();
    console.log('events closed', events);
  },

  methods: {
    sendMsg() {
      // const requestOptions = {
      //     method: 'POST',
      //     headers: { 'Content-Type': 'application/json' },
      //     body: JSON.stringify({ greeting: 'Vue 3 POST Request Example' })
      // };
      // fetch('http://localhost:8080/events', requestOptions)
      //   .then(response => response.json())
      //   .then(console.log);
      this.messages.set(1, {
        title: this.title,
        color: 'green',
        value: 100,
        pulse: true,
      });
      setTimeout(() => {
        this.messages.set(1, { ...this.messages.get(1), pulse:false, color: this.color })
        console.log(JSON.stringify(this.messages.get(1)));
      }, 500);
      
    }
  }

});
</script>
