import { clsx, type ClassValue } from 'clsx';

export function cn(...inputs: ClassValue[]) {
  return clsx(inputs);
}

function toValidDate(input: string | Date): Date | null {
  const d = new Date(input);
  return Number.isNaN(d.getTime()) ? null : d;
}

export function formatDate(date: string | Date): string {
  const d = toValidDate(date);
  if (!d) return '—';
  return new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  }).format(d);
}

export function formatRelativeTime(date: string | Date): string {
  const then = toValidDate(date);
  if (!then) return '—';
  const now = new Date();
  const diffInSeconds = Math.floor((now.getTime() - then.getTime()) / 1000);

  if (diffInSeconds < 60) return 'Just now';
  if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}m ago`;
  if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)}h ago`;
  if (diffInSeconds < 604800) return `${Math.floor(diffInSeconds / 86400)}d ago`;
  
  return formatDate(date);
}

export function truncate(str: string, length: number): string {
  if (str.length <= length) return str;
  return str.slice(0, length) + '...';
}

export function getInitials(name: string): string {
  return name
    .split(' ')
    .map((n) => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);
}

export function getFrameworkIcon(framework: string): string {
  const icons: Record<string, string> = {
    react: 'REACT',
    nextjs: 'NEXTJS',
    vue: 'VUE',
    angular: 'ANGULAR',
    svelte: 'SVELTE',
    astro: 'ASTRO',
    solid: 'SOLID',
    remix: 'REMIX',
    vanilla: 'VANILLA',
  };
  return icons[framework?.toLowerCase()] || framework || 'REACT';
}

export function getFrameworkColor(framework: string): string {
  const colors: Record<string, string> = {
    react: 'from-blue-500 to-cyan-500',
    nextjs: 'from-gray-600 to-gray-800',
    vue: 'from-green-500 to-emerald-500',
    angular: 'from-red-500 to-pink-500',
    svelte: 'from-orange-500 to-red-500',
    astro: 'from-purple-500 to-pink-500',
    solid: 'from-blue-600 to-indigo-600',
    remix: 'from-slate-500 to-zinc-600',
    vanilla: 'from-yellow-500 to-orange-500',
  };
  return colors[framework?.toLowerCase()] || 'from-mauve-500 to-mauve-700';
}

export function getFrameworkLabel(type: string): string {
  const labels: Record<string, string> = {
    NEXTJS: 'Next.js',
    REACT: 'React',
    VUE: 'Vue',
    ANGULAR: 'Angular',
    SVELTE: 'SvelteKit',
    ASTRO: 'Astro',
    SOLID: 'SolidJS',
    REMIX: 'Remix',
    VANILLA: 'Vanilla',
  };
  return labels[type?.toUpperCase()] || type || 'Next.js';
}

export function normalizeProject(raw: Record<string, unknown>): Record<string, unknown> {
  const type = (raw.type as string) || (raw.framework as string) || 'NEXTJS';
  return { ...raw, type, framework: type };
}
