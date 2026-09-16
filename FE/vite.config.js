import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // Il browser parla solo con Vite: /api viene inoltrato al backend sulla 3001 (stessa origine, niente CORS)
    proxy: {
      '/api': 'http://localhost:3001',
    },
  },
})
