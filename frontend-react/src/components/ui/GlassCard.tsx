import { motion, type HTMLMotionProps } from 'framer-motion';
import { forwardRef } from 'react';
import { cn } from '../../lib/utils';

interface GlassCardProps extends HTMLMotionProps<'div'> {
  variant?: 'default' | 'subtle' | 'strong';
  hover?: boolean;
  glow?: boolean;
  notch?: boolean;
}

const GlassCard = forwardRef<HTMLDivElement, GlassCardProps>(
  (
    {
      className,
      variant = 'default',
      hover = false,
      glow = false,
      notch = false,
      children,
      ...props
    },
    ref
  ) => {
    const variants = {
      default: 'glass',
      subtle: 'glass-subtle',
      strong: 'glass-strong',
    };

    return (
      <motion.div
        ref={ref}
        className={cn(
          'rounded-[22px] overflow-hidden',
          variants[variant],
          hover &&
          'hover:border-lilac-200/20 hover:bg-lilac-200/10 transition-all duration-300 cursor-pointer',
          glow && 'glow-mauve',
          notch && 'notch',
          className
        )}
        whileHover={
          hover
            ? { y: -4, scale: 1.01, transition: { type: 'spring', stiffness: 400, damping: 25 } }
            : undefined
        }
        transition={{ duration: 0.3, ease: 'easeOut' }}
        {...props}
      >
        {children}
      </motion.div>
    );
  }
);

GlassCard.displayName = 'GlassCard';

export default GlassCard;
