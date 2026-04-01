import { motion } from 'framer-motion';

interface LogoProps {
  size?: 'sm' | 'md' | 'lg' | 'xl';
  showText?: boolean;
  className?: string;
  mono?: boolean;
}

export default function Logo({ size = 'md', showText = true, className, mono = false }: LogoProps) {
  const iconSizes = { sm: 30, md: 36, lg: 48, xl: 64 };
  const textSizes = { sm: 'text-[16px]', md: 'text-xl', lg: 'text-2xl', xl: 'text-4xl' };
  const s = iconSizes[size];

  return (
    <motion.div
      className={`flex items-center gap-2.5 select-none ${className ?? ''}`}
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.5 }}
    >
      {/* ── Flower Prism — larger, richer, rotating ── */}
      <motion.div
        className="relative group"
        initial={{ opacity: 0, scale: 0.4 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.6, type: 'spring', stiffness: 200, damping: 16 }}
        whileHover={{ scale: 1.15 }}
      >
        {/* Glow */}
        <div
          className="absolute -inset-3 rounded-full opacity-35 group-hover:opacity-60 transition-opacity duration-700 blur-xl animate-[spin_12s_linear_infinite]"
          style={{
            background: 'conic-gradient(from 0deg, #6D28D9, #DB2777, #0891B2, #7C3AED, #DB2777, #6D28D9)',
          }}
        />

        <svg
          width={s}
          height={s}
          viewBox="0 0 48 48"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
          className="relative z-10 animate-[spin_20s_linear_infinite]"
        >
          <defs>
            <linearGradient id="p0" x1="50%" y1="0%" x2="50%" y2="100%">
              <stop offset="0%" stopColor="#6D28D9" />
              <stop offset="100%" stopColor="#8B5CF6" />
            </linearGradient>
            <linearGradient id="p1" x1="100%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stopColor="#8B5CF6" />
              <stop offset="100%" stopColor="#DB2777" />
            </linearGradient>
            <linearGradient id="p2" x1="100%" y1="100%" x2="0%" y2="0%">
              <stop offset="0%" stopColor="#DB2777" />
              <stop offset="100%" stopColor="#E11D48" />
            </linearGradient>
            <linearGradient id="p3" x1="50%" y1="100%" x2="50%" y2="0%">
              <stop offset="0%" stopColor="#E11D48" />
              <stop offset="100%" stopColor="#D97706" />
            </linearGradient>
            <linearGradient id="p4" x1="0%" y1="100%" x2="100%" y2="0%">
              <stop offset="0%" stopColor="#D97706" />
              <stop offset="100%" stopColor="#0891B2" />
            </linearGradient>
            <linearGradient id="p5" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="#0891B2" />
              <stop offset="100%" stopColor="#6D28D9" />
            </linearGradient>
          </defs>

          {/* 6 petals — teardrop shapes radiating outward */}
          {[0, 1, 2, 3, 4, 5].map((i) => (
            <g key={i} transform={`rotate(${i * 60} 24 24)`}>
              <path
                d="M24 24 C20.5 17, 18.5 8, 24 3 C29.5 8, 27.5 17, 24 24Z"
                fill={`url(#p${i})`}
                opacity="0.9"
              />
            </g>
          ))}

          {/* Center */}
          <circle cx="24" cy="24" r="3.5" fill="white" opacity="0.85" />
          <circle cx="24" cy="24" r="1.8" fill="white" />
        </svg>
      </motion.div>

      {/* ── Title — Syne font, theme gradient (lilac/mauve) ── */}
      {showText && (
        <motion.span
          className={`${textSizes[size]} font-extrabold tracking-tight leading-none`}
          initial={{ opacity: 0, x: -8 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.5, delay: 0.15 }}
          style={{
            fontFamily: "'Syne', 'Outfit', sans-serif",
            background: mono
              ? 'linear-gradient(135deg, rgba(230,233,249,0.9), rgba(230,233,249,0.5))'
              : 'linear-gradient(135deg, #E6E9F9 0%, #C4B5FD 35%, #915F6D 65%, #B87385 100%)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            backgroundClip: 'text',
          }}
        >
          NexaStudio
        </motion.span>
      )}
    </motion.div>
  );
}
