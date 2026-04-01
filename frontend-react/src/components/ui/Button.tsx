import { motion, type HTMLMotionProps } from 'framer-motion';
import { forwardRef, type ReactNode } from 'react';
import { cn } from '../../lib/utils';
import { Loader2 } from 'lucide-react';

interface ButtonProps extends Omit<HTMLMotionProps<'button'>, 'children'> {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger';
  size?: 'sm' | 'md' | 'lg';
  isLoading?: boolean;
  leftIcon?: ReactNode;
  rightIcon?: ReactNode;
  children?: ReactNode;
}

const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  (
    {
      className,
      variant = 'primary',
      size = 'md',
      isLoading = false,
      leftIcon,
      rightIcon,
      children,
      disabled,
      ...props
    },
    ref
  ) => {
    const variants = {
      primary:
        'bg-gradient-to-r from-mauve-600 to-mauve-700 text-white shadow-lg shadow-mauve-600/25 hover:shadow-mauve-600/40 hover:from-mauve-500 hover:to-mauve-600',
      secondary:
        'bg-lilac-200/10 text-lilac-200 border border-lilac-200/20 hover:bg-lilac-200/15 hover:border-lilac-200/30',
      ghost:
        'bg-transparent text-lilac-200 hover:bg-lilac-200/10',
      danger:
        'bg-gradient-to-r from-red-600 to-red-700 text-white shadow-lg shadow-red-600/25 hover:shadow-red-600/40',
    };

    const sizes = {
      sm: 'px-4 py-1.5 text-sm rounded-xl gap-1.5',
      md: 'px-6 py-2.5 text-sm rounded-xl gap-2',
      lg: 'px-8 py-3.5 text-base rounded-xl gap-2.5',
    };

    return (
      <motion.button
        ref={ref}
        whileHover={{ scale: disabled || isLoading ? 1 : 1.02 }}
        whileTap={{ scale: disabled || isLoading ? 1 : 0.98 }}
        className={cn(
          'inline-flex items-center justify-center font-medium transition-all duration-300',
          'focus:outline-none focus-visible:ring-2 focus-visible:ring-mauve-500/50 focus-visible:ring-offset-2 focus-visible:ring-offset-slate-900',
          'disabled:opacity-50 disabled:cursor-not-allowed disabled:pointer-events-none',
          variants[variant],
          sizes[size],
          className
        )}
        disabled={disabled || isLoading}
        {...props}
      >
        {isLoading ? (
          <Loader2 className="w-4 h-4 animate-spin" />
        ) : (
          leftIcon
        )}
        {children}
        {!isLoading && rightIcon}
      </motion.button>
    );
  }
);

Button.displayName = 'Button';

export default Button;
