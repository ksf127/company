import { defineRemote } from '@company/build-config';

// 只暴露 ./App：远端的内部页面、组件不对外，shell 无法依赖其实现细节。
export default defineRemote({
  name: 'orderApp',
  port: 5001,
  exposes: {
    './App': './src/App.tsx'
  }
});
