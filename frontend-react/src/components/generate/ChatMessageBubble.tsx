import React from 'react';
import { motion } from 'framer-motion';
import { Bot, AlertCircle } from 'lucide-react';
import { cn, formatRelativeTime } from '../../lib/utils';
import ThinkingIndicator from './ThinkingIndicator';
import FileChangesCard from './FileChangesCard';
import type { GeneratedFile } from './FileChangesCard';

/* ═══════════════════════════════════════════════════════════════════
   Types
   ═══════════════════════════════════════════════════════════════════ */

export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
  files?: GeneratedFile[];
  status?: 'pending' | 'thinking' | 'generating' | 'complete' | 'error';
  processingTime?: number;
}

export interface ChatMessageBubbleProps {
  /** The chat message to display */
  message: ChatMessage;
  /** Callback when a file is clicked to view (for assistant messages) */
  onViewFile?: (path: string) => void;
  /** Current thinking step index for thinking animation */
  thinkingStep?: number;
  /** Array of thinking step messages */
  thinkingSteps?: string[];
}

/* ═══════════════════════════════════════════════════════════════════
   Default Thinking Steps
   ═══════════════════════════════════════════════════════════════════ */

const DEFAULT_THINKING_STEPS = [
  'Analyzing your request...',
  'Understanding project context...',
  'Planning file structure...',
  'Generating components...',
  'Writing styles and logic...',
  'Validating output...',
];

/* ═══════════════════════════════════════════════════════════════════
   ChatMessageBubble Component
   Professional chat message display with user/assistant variants
   ═══════════════════════════════════════════════════════════════════ */

const ChatMessageBubble = React.memo(function ChatMessageBubble({
  message,
  onViewFile,
  thinkingStep,
  thinkingSteps = DEFAULT_THINKING_STEPS,
}: ChatMessageBubbleProps) {
  const isUser = message.role === 'user';
  const isThinking = message.status === 'thinking' || message.status === 'generating';

  return (
    <motion.div
      initial={{ opacity: 0, y: 6 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.2, ease: [0.25, 1, 0.5, 1] }}
      className={cn('group relative', isUser ? 'flex justify-end' : '')}
    >
      {isUser ? (
        /* ═══ User Message ═══ */
        <div className="max-w-[85%]">
          <div className="bg-mauve-500/12 border border-mauve-500/12 rounded-2xl rounded-br-sm px-4 py-2.5">
            <p className="text-[13px] leading-relaxed text-lilac-100 whitespace-pre-wrap">
              {message.content}
            </p>
          </div>
          <div className="flex items-center justify-end gap-1 mt-1 opacity-0 group-hover:opacity-100 transition-opacity">
            <span className="text-[10px] text-lilac-500/25">
              {formatRelativeTime(message.timestamp)}
            </span>
          </div>
        </div>
      ) : (
        /* ═══ Assistant Message ═══ */
        <div className="max-w-full">
          {/* Header with avatar and name */}
          <div className="flex items-center gap-2 mb-2">
            <div
              className={cn(
                'w-6 h-6 rounded-lg flex items-center justify-center',
                message.status === 'error'
                  ? 'bg-red-500/12 border border-red-500/15'
                  : 'bg-gradient-to-br from-mauve-500/20 to-purple-500/12 border border-mauve-500/12'
              )}
            >
              {message.status === 'error' ? (
                <AlertCircle className="w-3.5 h-3.5 text-red-400" aria-hidden="true" />
              ) : (
                <Bot className="w-3.5 h-3.5 text-mauve-300" aria-hidden="true" />
              )}
            </div>
            <span className="text-[11px] font-medium text-lilac-400/45">NexaStudio AI</span>
            {message.processingTime && (
              <span className="text-[10px] text-lilac-500/25">
                {(message.processingTime / 1000).toFixed(1)}s
              </span>
            )}
          </div>

          {/* Content area */}
          <div className="pl-8">
            {isThinking ? (
              /* Thinking animation */
              <ThinkingIndicator steps={thinkingSteps} currentStep={thinkingStep ?? 0} />
            ) : (
              <>
                {/* Error state */}
                {message.status === 'error' ? (
                  <div className="rounded-lg bg-red-500/5 border border-red-500/12 px-3 py-2.5">
                    <p className="text-[13px] leading-relaxed text-red-200/80 whitespace-pre-wrap">
                      {message.content}
                    </p>
                  </div>
                ) : (
                  /* Normal message content */
                  <p className="text-[13px] leading-relaxed text-lilac-200/80 whitespace-pre-wrap">
                    {message.content}
                  </p>
                )}

                {/* File changes card */}
                {message.files && message.files.length > 0 && onViewFile && (
                  <FileChangesCard files={message.files} onViewFile={onViewFile} />
                )}
              </>
            )}
          </div>
        </div>
      )}
    </motion.div>
  );
});

export default ChatMessageBubble;
