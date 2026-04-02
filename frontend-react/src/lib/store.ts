import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export interface User {
  id: string;
  username: string;
}

export interface Project {
  id: string;
  name: string;
  description?: string;
  type: string;
  framework: string; // alias for type for backward compat
  status?: string;
  fileCount?: number;
  promptCount?: number;
  previewUrl?: string;
  createdAt: string;
  updatedAt: string;
  lastGeneratedAt?: string;
}

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (user: User, token: string, refreshToken?: string) => void;
  logout: () => void;
}

interface ProjectState {
  projects: Project[];
  currentProject: Project | null;
  isLoading: boolean;
  setProjects: (projects: Project[]) => void;
  setCurrentProject: (project: Project | null) => void;
  addProject: (project: Project) => void;
  updateProject: (id: string, data: Partial<Project>) => void;
  removeProject: (id: string) => void;
  setLoading: (loading: boolean) => void;
}

interface UIState {
  sidebarOpen: boolean;
  toggleSidebar: () => void;
  setSidebarOpen: (open: boolean) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      login: (user, token, refreshToken?) => {
        localStorage.setItem('nexa_token', token);
        localStorage.setItem('nexa_user', JSON.stringify(user));
        if (refreshToken) {
          localStorage.setItem('nexa_refresh_token', refreshToken);
        }
        set({ user, token, isAuthenticated: true });
      },
      logout: () => {
        localStorage.removeItem('nexa_token');
        localStorage.removeItem('nexa_user');
        localStorage.removeItem('nexa_refresh_token');
        set({ user: null, token: null, isAuthenticated: false });
      },
    }),
    { name: 'nexa-auth' }
  )
);

export const useProjectStore = create<ProjectState>((set) => ({
  projects: [],
  currentProject: null,
  isLoading: false,
  setProjects: (projects) => set({ projects }),
  setCurrentProject: (project) => set({ currentProject: project }),
  addProject: (project) =>
    set((state) => ({ projects: [...state.projects, project] })),
  updateProject: (id, data) =>
    set((state) => ({
      projects: state.projects.map((p) => (p.id === id ? { ...p, ...data } : p)),
    })),
  removeProject: (id) =>
    set((state) => ({ projects: state.projects.filter((p) => p.id !== id) })),
  setLoading: (loading) => set({ isLoading: loading }),
}));

export const useUIStore = create<UIState>((set) => ({
  sidebarOpen: true,
  toggleSidebar: () => set((state) => ({ sidebarOpen: !state.sidebarOpen })),
  setSidebarOpen: (open) => set({ sidebarOpen: open }),
}));
