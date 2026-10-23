import { defineRemote } from '@company/build-config';

// 每个微前端只暴露 ./App，端口与 registry 中的 entry 一一对应。
export default defineRemote({
  name: 'demo05App',
  port: 5015,
  exposes: {
    './App': './src/App.tsx'
  }
});
