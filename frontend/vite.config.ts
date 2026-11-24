import { defineConfig } from 'vite';

export default defineConfig({
  root: '.',
  build: {
    outDir: '../src/main/resources/web',
    emptyOutDir: true,
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
