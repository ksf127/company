import { defineRemote } from '@company/build-config';

export default defineRemote({
  name: 'userApp',
  port: 5002,
  exposes: {
    './App': './src/App.tsx'
  }
});
