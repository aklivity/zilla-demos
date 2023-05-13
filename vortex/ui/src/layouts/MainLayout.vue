<template>
  <q-layout view="hHh lpR fFf">
    <q-header elevated class="bg-primary text-white">
      <q-toolbar>
        <q-toolbar-title class="row items-center no-wrap">
          <img
            :height="75"
            src="https://docs.aklivity.io/zilla/latest/logo.png"
          />
          <span class="q-ml-lg">Vortex Demo</span>
        </q-toolbar-title>
      </q-toolbar>
    </q-header>

    <q-page-container>
      <q-page class="q-pa-sm" style="height: 90vh;">
        <div class="row items-start q-gutter-xs" style="height: 90vh;">
          <q-responsive
            :ratio="1"
            class="col fit"
            style="max-width: 40%"
          >
            <div class="q-gutter-lg q-pt-lg">
              <div class="row justify-center">
                <q-img src="vortex-demo-diagram@2x.png" spinner-color="white" />
              </div>
            </div>
          </q-responsive>

          <q-responsive
            :ratio="1"
            class="col fit"
          >
            <div class="q-gutter-md fit">
              <div class="row text-h4">Send a Message</div>

              <div class="row q-gutter-sm j">
                <div class="col">
                  <q-input filled v-model="title" label="Name" class="col" />
                </div>
                <div class="col-3">
                  <q-input
                    filled
                    label="Favourite Colour"
                    v-model="color"
                    :bg-color="color"
                    :style="{ backgroundColor: color }"
                    class="col"
                  >
                    <template v-slot:append>
                      <q-icon name="colorize" class="cursor-pointer">
                        <q-popup-proxy
                          cover
                          transition-show="scale"
                          transition-hide="scale"
                        >
                          <q-color
                            no-header
                            no-footer
                            default-view="palette"
                            v-model="color"
                          />
                        </q-popup-proxy>
                      </q-icon>
                    </template>
                  </q-input>
                </div>
                <div class="col-1 flex">
                  <q-btn @click="sendMsg" color="primary" label="Send" />
                </div>
              </div>

              <div class="row">
              </div>
              <div class="row" style="width: 100%; height: 70%; overflow: scroll;">
                <div class="q-ma-sm flex justify-center" v-for="[key, msg] in messages" :key="key">
                  <q-circular-progress
                    :style="{ color: msg.color }"
                    :indeterminate="msg.pulse"
                    size="30px"
                    :thickness="0.4"
                    :value="msg.value"
                    font-size="30px"
                    track-color="transparent"
                    center-color="grey-7"
                    class="q-ma-xs"
                  />
                  <p class="q-ma-sm">
                    {{msg.title}}
                  </p>
                </div>
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

const events = new EventSource('http://localhost:8080/LoopProgress');

export default defineComponent({
  name: 'MainLayout',

  setup() {
    const messages = reactive(
      new Map<
        number,
        { title: string; color: string; value: number; pulse: boolean }
      >([])
    );

    events.onmessage = function ({ data }: MessageEvent) {
      var { title, id, color, value } = <
        { title: string; id: number; color: string; value: number }
      >JSON.parse(data);
      console.log(id, data);
      if (id) {
        messages.set(id, {
          title,
          color: 'blue',
          value: value,
          pulse: true,
        });
        setTimeout(() => {
          messages.set(id, { ...messages.get(id), pulse: false, color: color });
          //send message back over http
        }, 500);
      }
    };

    for (let i = 1; i <= 300; i++) {
      messages.set(i, {
        title: `hello ${i}`,
        color: '#1ED5A7',
        value: i,
        pulse: false,
      });
    }

    return {
      messages,
      title: ref(''),
      color: ref(''),
    };
  },

  onUnmounted() {
    events.close();
    console.log('events closed', events);
  },

  methods: {
    sendMsg() {
      const requestOptions = {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ greeting: 'Vue 3 POST Request Example' })
      };
      fetch('http://localhost:8080/BoothVisitor', requestOptions)
        // .then(response => response.json())
        .then(console.log);
      this.messages.set(1, {
        title: this.title,
        color: '#1ED5A7',
        value: 100,
        pulse: true,
      });
      setTimeout(() => {
        this.messages.set(1, {
          ...this.messages.get(1),
          pulse: false,
          color: this.color,
        });
        console.log(JSON.stringify(this.messages.get(1)));
      }, 500);
    },
  },
});
</script>
