import React from 'react';
import { motion } from 'framer-motion';
import { Loader2, Check } from 'lucide-react';
import { cn } from '../../lib/utils';

/* ═══════════════════════════════════════════════════════════════════
   Types
   ═══════════════════════════════════════════════════════════════════ */

export interface ThinkingIndicatorProps {
  /** Array of thinking step messages to display */
  steps: string[];
  /** Current step index (0-based) */
  currentStep: number;
}

/* ═══════════════════════════════════════════════════════════════════
   ThinkingIndicator Component
   Claude-style AI thinking animation with step progress
   ═══════════════════════════════════════════════════════════════════ */

const ThinkingIndicator = React.memo(function ThinkingIndicator({
  steps,
  currentStep,
}: ThinkingIndicatorProps) {
  return (
    <div className="space-y-2">
      {/* Animated dots with "Thinking" label */}
      <div className="flex items-center gap-2 mb-3">
        <div className="flex gap-1" aria-hidden="true">
          {[0, 1, 2].map((i) => (
            <motion.div
              key={i}
              className="w-1.5 h-1.5 rounded-full bg-mauve-400"
              animate={{
                opacity: [0.3, 1, 0.3],
                scale: [0.8, 1.1, 0.8],
              }}
              transition={{
                duration: 1.2,
                repeat: Infinity,
                delay: i * 0.2,
                ease: 'easeInOut',
              }}
            />
          ))}
        </div>
        <span className="text-xs text-lilac-300/60 font-medium">Thinking</span>
      </div>

      {/* Step progress list */}
      <div className="space-y-1.5 pl-1" role="status" aria-live="polite">
        {steps.slice(0, currentStep + 1).map((step, i) => (
          <motion.div
            key={i}
            initial={{ opacity: 0, x: -8 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.3 }}
            className="flex items-center gap-2"
          >
            {i < currentStep ? (
              <Check className="w-3 h-3 text-emerald-400 shrink-0" aria-hidden="true" />
            ) : (
              <motion.div
                animate={{ rotate: 360 }}
                transition={{ duration: 1, repeat: Infinity, ease: 'linear' }}
              >
                <Loader2 className="w-3 h-3 text-mauve-400 shrink-0" aria-hidden="true" />
              </motion.div>
            )}
            <span
              className={cn(
                'text-xs',
                i < currentStep ? 'text-lilac-400/50' : 'text-lilac-200/80'
              )}
            >
              {step}
            </span>
          </motion.div>
        ))}
      </div>
    </div>
  );
});

export default ThinkingIndicator;
