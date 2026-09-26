import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig(({ mode }) => {
  // Load env variables based on the current mode (development/production)
  const env = loadEnv(mode, process.cwd(), '')

  return {
    plugins: [react(), tailwindcss()],
    server: {
      proxy: {
        '/api': {
          // If a VITE_API_URL is defined, use it. Otherwise, fallback to your local java instance
          target: env.VITE_API_URL || 'http://localhost:8000',
          changeOrigin: true,
        }
      }
    }
  }
})

