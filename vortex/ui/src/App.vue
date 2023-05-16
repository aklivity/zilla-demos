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
                  <q-input filled v-model="name" label="Name" class="col" />
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
                  <q-btn @click="newVisitor" color="primary" label="Send" />
                </div>
              </div>

              <div class="row">
              </div>
              <div class="row  content-start" style="width: 100%; height: 70%; overflow: scroll;">
                <div class="q-ma-sm flex justify-center" v-for="[key, msg] in reversedList" :key="key">
                  <q-circular-progress
                    :style="{ color: msg.color }"
                    :indeterminate="msg.pulse"
                    size="30px"
                    :thickness="0.4"
                    :value="msg.loopCount"
                    font-size="30px"
                    track-color="transparent"
                    center-color="grey-7"
                    class="q-ma-xs"
                  />
                  <p class="q-ma-sm">
                    {{msg.name}}
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
import { defineComponent, reactive, ref, computed } from 'vue';

const messages = reactive(
      new Map<
        string,
        { name: string; color: string; loopCount: number; pulse: boolean }
      >([])
    );

const reversedList = computed(() => {
  return Array.from(messages).reverse()
})

const getKey = (name: string, color: string) => `${name}${color.replace('#', '')}`

const sendBoothVisitor =  async(name: string, color: string, loopCount: number) => {
  console.log('POSTing', getKey(name,color));

  await fetch(`http://localhost:8080/BoothVisitor/${getKey(name,color)}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
       },
      body: JSON.stringify({ name, color, loopCount })
    });
};


const events = new EventSource('http://localhost:8080/LoopProgress');
events.onmessage = ({ data }: MessageEvent) => {
  var { name, color, loopCount } = <
    { name: string; color: string; loopCount: number }
  >JSON.parse(data);
  if (name && color) {
    const key = getKey(name, color);
    // console.log('message received', key);
    const current = messages.get(key);
    if (loopCount > (current?.loopCount ? current.loopCount : 0)) {
      messages.set(key, {
        name,
        color,
        loopCount,
        pulse: false,
      });
      if (loopCount < 100) {
        // console.log('send new message', loopCount)
        sendBoothVisitor(name, color, loopCount)
      }
    }
  }
};

export default defineComponent({
  name: 'App',

  setup() {
    return {
      messages,
      name: ref(''),
      color: ref('#5A7CD9'),
      reversedList,
    };
  },

  onUnmounted() {
    events.close();
    console.log('events closed', events);
  },

  methods: {
    newVisitor() {
      if (this.name && this.color) {
        sendBoothVisitor(this.name, this.color, 0);
        this.name = '';
        this.color = '#5A7CD9';
      }
    }
  },
});
</script>
