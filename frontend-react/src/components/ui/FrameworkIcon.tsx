import { cn } from '../../lib/utils';

interface FrameworkIconProps {
  framework: string;
  size?: number;
  className?: string;
}

export function FrameworkIcon({ framework, size = 24, className }: FrameworkIconProps) {
  const fw = framework?.toUpperCase() || '';
  const s = size;

  switch (fw) {
    case 'REACT':
      return (
        <svg width={s} height={s} viewBox="0 0 32 32" className={cn('shrink-0', className)}>
          <circle cx="16" cy="16" r="3" fill="#61DAFB" />
          <ellipse cx="16" cy="16" rx="14" ry="5.5" fill="none" stroke="#61DAFB" strokeWidth="1.5" />
          <ellipse cx="16" cy="16" rx="14" ry="5.5" fill="none" stroke="#61DAFB" strokeWidth="1.5" transform="rotate(60 16 16)" />
          <ellipse cx="16" cy="16" rx="14" ry="5.5" fill="none" stroke="#61DAFB" strokeWidth="1.5" transform="rotate(120 16 16)" />
        </svg>
      );
    case 'NEXTJS':
      return (
        <svg width={s} height={s} viewBox="0 0 32 32" className={cn('shrink-0', className)}>
          <circle cx="16" cy="16" r="14" fill="white" />
          <path d="M13 10v12l9-12H13z" fill="black" />
          <circle cx="21" cy="21" r="1.5" fill="black" />
        </svg>
      );
    case 'VUE':
      return (
        <svg width={s} height={s} viewBox="0 0 32 32" className={cn('shrink-0', className)}>
          <path d="M2 4h5.5L16 17.5 24.5 4H30L16 28 2 4z" fill="#41B883" />
          <path d="M7.5 4L16 18.5 24.5 4H20L16 11 12 4H7.5z" fill="#35495E" />
        </svg>
      );
    case 'ANGULAR':
      return (
        <svg width={s} height={s} viewBox="0 0 32 32" className={cn('shrink-0', className)}>
          <path d="M16 2L3 7l2 18L16 30l11-5 2-18L16 2z" fill="#DD0031" />
          <path d="M16 2v28l11-5 2-18L16 2z" fill="#C3002F" />
          <path d="M16 6L9 22h2.5l1.4-3.5h6.2L20.5 22H23L16 6zm0 5l2.3 5.5h-4.6L16 11z" fill="white" />
        </svg>
      );
    case 'SVELTE':
      return (
        <svg width={s} height={s} viewBox="0 0 32 32" className={cn('shrink-0', className)}>
          <path d="M26.5 5.5C24 1.5 18.7 0.3 14.8 2.5L8.5 6.5C6.8 7.5 5.5 9.2 5 11.1c-0.4 1.5-0.2 3 0.3 4.4-0.8 1.2-1.2 2.7-1.1 4.1 0.2 2 1.1 3.8 2.6 5.1C9 27.5 12.5 28.5 15.8 27.8c0.9 1 2 1.8 3.3 2.2 3.4 1.1 7.2-0.1 9.3-3 1.7-2.3 2-5.3 1-7.9 0.8-1.2 1.2-2.7 1.1-4.1-0.3-3.5-2.8-6.5-4-9.5z" fill="#FF3E00" />
          <path d="M14 24c-2 0.5-4.2-0.5-5.2-2.3-0.5-1-0.5-2.1-0.2-3.1l0.2-0.5 0.5 0.3c0.8 0.6 1.8 1 2.7 1.2l0.3 0v0.3c0 0.5 0.2 1 0.5 1.3 0.5 0.5 1.3 0.7 2 0.4l0.3-0.1 5.3-3.3c0.5-0.3 0.8-0.8 0.8-1.3s-0.1-1.1-0.5-1.5c-0.5-0.5-1.3-0.7-2-0.4l-2 1.2c-2.2 1.4-5.1 0.7-6.5-1.5-0.5-1-0.5-2.1-0.2-3.2 0.3-1.1 1-2 2-2.6l5.3-3.3c2.2-1.3 5-0.6 6.5 1.5 0.5 1 0.5 2.1 0.2 3.1l-0.2 0.5-0.5-0.3c-0.8-0.6-1.8-1-2.7-1.2l-0.3 0v-0.3c0-0.5-0.2-1-0.5-1.3-0.5-0.5-1.3-0.7-2-0.4l-0.3 0.1-5.3 3.3c-0.5 0.3-0.8 0.8-0.8 1.3s0.1 1.1 0.5 1.5c0.5 0.5 1.3 0.7 2 0.4l2-1.2c2.2-1.4 5.1-0.7 6.5 1.5 0.5 1 0.5 2.1 0.2 3.2-0.3 1.1-1 2-2 2.6l-5.3 3.3-0.5 0.2z" fill="white" />
        </svg>
      );
    case 'ASTRO':
      return (
        <svg width={s} height={s} viewBox="0 0 32 32" className={cn('shrink-0', className)}>
          <path d="M10.5 28c-1-2.5 0-5.5 2.5-7-0.5 2 0.5 3.5 1.5 4 1.2 0.5 2.5 0.3 3.2-0.5 0-0 0 0 0-0.1-0.5-3.5 3-5 5.5-8-0.5 5-2 8-4.5 10-1 0.8-2.2 1.3-3.5 1.5-1.5 0.2-3.2-0.1-4.7 0.1z" fill="#FF5D01" />
          <path d="M21 3L11 6v14l3-1.5V9l7-3V3z" fill="#FF5D01" />
          <path d="M11 6l10-3v3l-7 3v9.5L11 20V6z" fill="white" fillOpacity="0.3" />
        </svg>
      );
    case 'SOLID':
      return (
        <svg width={s} height={s} viewBox="0 0 32 32" className={cn('shrink-0', className)}>
          <path d="M5 6l7-2 14 7-7 2L5 6z" fill="#76B3E1" />
          <path d="M26 11L12 18l-7-5 14-7 7 5z" fill="#4377BB" />
          <path d="M5 13l7 5 14-7v7L12 25l-7-5V13z" fill="#335D92" />
          <path d="M12 18l14-7v7L12 25V18z" fill="#2C4F7C" />
        </svg>
      );
    case 'REMIX':
      return (
        <svg width={s} height={s} viewBox="0 0 32 32" className={cn('shrink-0', className)}>
          <path d="M24 26H8v-2h5.5c2 0 3-1 3-3V10c0-2-1-3.5-3.5-3.5H8V4h6c4 0 6 2.5 6 6v7c0 3-1.5 4.5-4 5h8v4z" fill="white" />
          <path d="M8 4h5c4 0 6.5 2 6.5 6v7c0 2.5-1 4-3 4.7V22c0 2-1 3.5-3 3.5H8V4z" fill="white" fillOpacity="0.5" />
        </svg>
      );
    case 'VANILLA':
      return (
        <svg width={s} height={s} viewBox="0 0 32 32" className={cn('shrink-0', className)}>
          <path d="M4 4h24v18.5l-12 6.5-12-6.5V4z" fill="#E44D26" />
          <path d="M16 26l9-5V6H16v20z" fill="#F16529" />
          <path d="M10 9h12v2H12v3h8v7l-4 2-4-2v-3h2v2l2 1 2-1v-3H10V9z" fill="white" />
        </svg>
      );
    default:
      return (
        <svg width={s} height={s} viewBox="0 0 32 32" className={cn('shrink-0', className)}>
          <rect x="4" y="4" width="24" height="24" rx="4" fill="none" stroke="currentColor" strokeWidth="2" />
          <path d="M10 12h12M10 16h8M10 20h10" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
        </svg>
      );
  }
}
