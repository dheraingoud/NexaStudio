import axios from 'axios';
import { useAuthStore } from './store';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('nexa_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor to unwrap ApiResponse and handle errors
api.interceptors.response.use(
  (response) => {
    // Backend wraps responses in ApiResponse { success, message, data }
    // Unwrap to get the actual data
    if (response.data && typeof response.data === 'object' && 'data' in response.data) {
      response.data = response.data.data;
    }
    return response;
  },
  (error) => {
    // Handle auth errors — redirect to login
    if (error.response?.status === 401 || error.response?.status === 403) {
      useAuthStore.getState().logout();
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }

    // Map raw errors to human-friendly messages
    const status = error.response?.status;
    const serverMsg = error.response?.data?.message;

    if (serverMsg) {
      error.message = serverMsg;
    } else if (error.code === 'ECONNABORTED' || error.code === 'ERR_NETWORK') {
      error.message = 'Connection failed. Please check your internet and try again.';
    } else if (error.message?.includes('timeout')) {
      error.message = 'Server took too long to respond. Please try again.';
    } else if (status === 404) {
      error.message = 'Resource not found. It may have been moved or deleted.';
    } else if (status === 409) {
      error.message = 'This item already exists. Please use a different name.';
    } else if (status === 429) {
      error.message = 'Too many requests. Please wait a moment and try again.';
    } else if (status && status >= 500) {
      error.message = 'Server error. Our team has been notified — please try again shortly.';
    }

    return Promise.reject(error);
  }
);

// Auth API - using username/password (no email)
export const authApi = {
  login: (username: string, password: string) =>
    api.post('/auth/login', { username, password }),
  register: (username: string, password: string) =>
    api.post('/auth/register', { username, password }),
  updatePassword: (currentPassword: string, newPassword: string) =>
    api.put('/auth/password', { currentPassword, newPassword }),
  logout: () => {
    localStorage.removeItem('nexa_token');
    localStorage.removeItem('nexa_user');
  },
};

// Projects API
export const projectsApi = {
  getAll: () => api.get('/projects'),
  getById: (id: string) => api.get(`/projects/${id}`),
  create: (data: { name: string; description?: string; type?: string; initialPrompt?: string }) =>
    api.post('/projects', data),
  update: (id: string, data: { name?: string; description?: string }) =>
    api.put(`/projects/${id}`, data),
  delete: (id: string) => api.delete(`/projects/${id}`),
  generate: (id: string, prompt: string, intent?: string, signal?: AbortSignal) =>
    api.post(`/projects/${id}/generate`, { prompt, intent }, { signal }),

  /**
   * SSE streaming generation.
   * Calls POST /projects/{id}/generate/stream and reads Server-Sent Events.
   * Returns an abort function, calls the provided callbacks on each event.
   */
  generateStream: (
    id: string,
    prompt: string,
    intent: string | undefined,
    callbacks: {
      onThinking?: (message: string) => void;
      onFile?: (file: { path: string; action: string; content: string; summary?: string }) => void;
      onComplete?: (response: { explanation?: string; changes?: unknown[]; processingTimeMs?: number }) => void;
      onError?: (message: string) => void;
    },
  ): (() => void) => {
    const controller = new AbortController();
    const token = localStorage.getItem('nexa_token');
    const url = `${API_BASE_URL}/projects/${id}/generate/stream`;

    (async () => {
      let completed = false;
      let receivedFiles = 0;
      try {
        const response = await fetch(url, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
          },
          body: JSON.stringify({ prompt, intent }),
          signal: controller.signal,
        });

        if (!response.ok) {
          const text = await response.text();
          callbacks.onError?.(text || `HTTP ${response.status}`);
          return;
        }

        const reader = response.body?.getReader();
        if (!reader) { callbacks.onError?.('No response body'); return; }

        const decoder = new TextDecoder();
        let buffer = '';

        const processLines = (lines: string[]) => {
          let currentEvent = '';
          for (const line of lines) {
            if (line.startsWith('event:')) {
              currentEvent = line.slice(6).trim();
            } else if (line.startsWith('data:')) {
              const rawData = line.slice(5).trim();
              if (!rawData) continue;
              try {
                const data = JSON.parse(rawData);
                switch (currentEvent) {
                  case 'thinking':
                    callbacks.onThinking?.(typeof data === 'string' ? data : data.message || 'Processing...');
                    break;
                  case 'file':
                    receivedFiles++;
                    callbacks.onFile?.(data);
                    break;
                  case 'complete':
                    completed = true;
                    callbacks.onComplete?.(data);
                    break;
                  case 'error':
                    completed = true;
                    callbacks.onError?.(typeof data === 'string' ? data : data.message || 'Generation failed');
                    break;
                }
              } catch {
                // non-JSON data line, ignore
              }
              currentEvent = '';
            }
          }
        };

        while (true) {
          const { done, value } = await reader.read();
          if (done) break;
          buffer += decoder.decode(value, { stream: true });

          // Parse SSE events from buffer
          const lines = buffer.split('\n');
          buffer = lines.pop() || ''; // keep incomplete line in buffer
          processLines(lines);
        }

        // Process any remaining buffer after stream ends
        if (buffer.trim()) {
          processLines(buffer.split('\n'));
        }

        // If stream ended normally without a complete event but files were received,
        // treat it as success
        if (!completed && receivedFiles > 0) {
          callbacks.onComplete?.({ explanation: 'Generation completed.', changes: [], processingTimeMs: 0 });
        }
      } catch (err: unknown) {
        if (completed) return; // Stream finished successfully, ignore close errors
        if ((err as Error).name === 'AbortError') return;
        // If we received files but the stream broke before the "complete" event,
        // treat it as a successful generation (connection closed after writes)
        if (receivedFiles > 0) {
          callbacks.onComplete?.({ explanation: 'Generation completed.', changes: [], processingTimeMs: 0 });
          return;
        }
        callbacks.onError?.((err as Error).message || 'Stream failed');
      }
    })();

    return () => controller.abort();
  },

  getFiles: (id: string) => api.get(`/projects/${id}/files`),
  getFile: (projectId: string, filePath: string) =>
    api.get(`/projects/${projectId}/file`, { params: { path: filePath } }),
  saveFile: (projectId: string, filePath: string, content: string) =>
    api.put(`/projects/${projectId}/file`, content, {
      params: { path: filePath },
      headers: { 'Content-Type': 'text/plain' },
    }),
  deleteFile: (projectId: string, filePath: string) =>
    api.delete(`/projects/${projectId}/file`, { params: { path: filePath } }),
  getPrompts: (id: string) => api.get(`/projects/${id}/prompts`),
  exportZip: (id: string) =>
    api.get(`/projects/${id}/export/direct`, { responseType: 'blob' }),
};

export default api;
