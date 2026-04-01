import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],

  resolve: {
    alias: {
      '@': '/src',
    },
  },

  server: {
    host: true,
    port: 5173,
  },

  build: {
    // Faster production builds
    target: 'es2020',
    // Chunk splitting for better caching
    rollupOptions: {
      output: {
        manualChunks: {
          'vendor-react': ['react', 'react-dom', 'react-router-dom'],
          'vendor-ui': ['framer-motion', 'lucide-react'],
          'vendor-state': ['zustand', 'axios'],
        },
      },
    },
    // Smaller chunk warnings
    chunkSizeWarningLimit: 1000,
  },
})
