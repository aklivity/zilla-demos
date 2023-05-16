<template>
  <q-item-label v-for="[key, msg] in messages" :key="key">
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
  </q-item-label>
</template>

<script lang="ts">
import {
  defineComponent,
  PropType,
  computed,
  ref,
  toRef,
  Ref,
} from 'vue';
import { Todo, Meta } from './models';

function useClickCount() {
  const clickCount = ref(0);
  function increment() {
    clickCount.value += 1
    return clickCount.value;
  }

  return { clickCount, increment };
}

function useDisplayTodo(todos: Ref<Todo[]>) {
  const todoCount = computed(() => todos.value.length);
  return { todoCount };
}

export default defineComponent({
  name: 'ExampleComponent',
  props: {
    title: {
      type: String,
      required: true
    },
    todos: {
      type: Array as PropType<Todo[]>,
      default: () => []
    },
    meta: {
      type: Object as PropType<Meta>,
      required: true
    },
    active: {
      type: Boolean
    }
  },
  setup (props) {
    return { ...useClickCount(), ...useDisplayTodo(toRef(props, 'todos')) };
  },
});
</script>
