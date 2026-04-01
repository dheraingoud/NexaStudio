import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  FileCode2,
  FileJson,
  FileText,
  FileType2,
  ChevronDown,
  Plus,
  Minus,
  Pencil,
  Circle,
} from 'lucide-react';
import { cn } from '../../lib/utils';

/* ═══════════════════════════════════════════════════════════════════
   Types
   ═══════════════════════════════════════════════════════════════════ */

export interface GeneratedFile {
  path: string;
  content: string;
  action: 'CREATE' | 'UPDATE' | 'DELETE';
  language: string;
}

export interface FileChangesCardProps {
  /** Array of files that have been changed */
  files: GeneratedFile[];
  /** Callback when a file is clicked to view */
  onViewFile: (path: string) => void;
}

/* ═══════════════════════════════════════════════════════════════════
   Helper Functions
   ═══════════════════════════════════════════════════════════════════ */

function getFileIcon(lang: string) {
  switch (lang) {
    case 'typescript':
    case 'tsx':
      return <FileCode2 className="w-3.5 h-3.5 text-blue-400" />;
    case 'javascript':
    case 'jsx':
      return <FileCode2 className="w-3.5 h-3.5 text-amber-400" />;
    case 'json':
      return <FileJson className="w-3.5 h-3.5 text-yellow-400" />;
    case 'css':
    case 'scss':
      return <FileType2 className="w-3.5 h-3.5 text-pink-400" />;
    case 'vue':
      return <FileCode2 className="w-3.5 h-3.5 text-emerald-400" />;
    case 'html':
      return <FileCode2 className="w-3.5 h-3.5 text-orange-400" />;
    case 'svelte':
      return <FileCode2 className="w-3.5 h-3.5 text-orange-500" />;
    case 'markdown':
      return <FileText className="w-3.5 h-3.5 text-lilac-300" />;
    default:
      return <FileText className="w-3.5 h-3.5 text-lilac-400/60" />;
  }
}

function getActionColor(action: string) {
  switch (action) {
    case 'CREATE':
      return 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20';
    case 'UPDATE':
      return 'text-amber-400 bg-amber-500/10 border-amber-500/20';
    case 'DELETE':
      return 'text-red-400 bg-red-500/10 border-red-500/20';
    default:
      return 'text-lilac-400 bg-lilac-500/10 border-lilac-500/20';
  }
}

function getActionIcon(action: string) {
  switch (action) {
    case 'CREATE':
      return <Plus className="w-3 h-3 text-emerald-400" />;
    case 'UPDATE':
      return <Pencil className="w-3 h-3 text-amber-400" />;
    case 'DELETE':
      return <Minus className="w-3 h-3 text-red-400" />;
    default:
      return <Circle className="w-3 h-3 text-lilac-400" />;
  }
}

/* ═══════════════════════════════════════════════════════════════════
   FileChangesCard Component
   Copilot-style file changes summary with expandable list
   ═══════════════════════════════════════════════════════════════════ */

const FileChangesCard = React.memo(function FileChangesCard({
  files,
  onViewFile,
}: FileChangesCardProps) {
  const [expanded, setExpanded] = useState(true);

  // Count files by action type
  const creates = files.filter((f) => f.action === 'CREATE');
  const updates = files.filter((f) => f.action === 'UPDATE');
  const deletes = files.filter((f) => f.action === 'DELETE');

  return (
    <div className="mt-3 rounded-lg border border-lilac-200/10 overflow-hidden bg-lilac-200/[0.02]">
      {/* Header with toggle and counts */}
      <button
        onClick={() => setExpanded(!expanded)}
        className="w-full flex items-center justify-between px-3 py-2 hover:bg-lilac-200/5 transition-colors"
        aria-expanded={expanded}
      >
        <div className="flex items-center gap-2">
          <FileCode2 className="w-3.5 h-3.5 text-mauve-400" aria-hidden="true" />
          <span className="text-xs font-medium text-lilac-200">
            {files.length} file{files.length !== 1 ? 's' : ''} changed
          </span>
        </div>
        <div className="flex items-center gap-2">
          {creates.length > 0 && (
            <span className="text-[10px] font-medium text-emerald-400 bg-emerald-500/10 px-1.5 py-0.5 rounded">
              +{creates.length}
            </span>
          )}
          {updates.length > 0 && (
            <span className="text-[10px] font-medium text-amber-400 bg-amber-500/10 px-1.5 py-0.5 rounded">
              ~{updates.length}
            </span>
          )}
          {deletes.length > 0 && (
            <span className="text-[10px] font-medium text-red-400 bg-red-500/10 px-1.5 py-0.5 rounded">
              -{deletes.length}
            </span>
          )}
          <ChevronDown
            className={cn(
              'w-3.5 h-3.5 text-lilac-400/40 transition-transform',
              !expanded && '-rotate-90'
            )}
            aria-hidden="true"
          />
        </div>
      </button>

      {/* Expandable file list */}
      <AnimatePresence>
        {expanded && (
          <motion.div
            initial={{ height: 0 }}
            animate={{ height: 'auto' }}
            exit={{ height: 0 }}
            transition={{ duration: 0.15 }}
            className="overflow-hidden border-t border-lilac-200/8"
          >
            {files.map((file) => (
              <button
                key={file.path}
                onClick={() => onViewFile(file.path)}
                className="w-full flex items-center gap-2 px-3 py-1.5 text-left hover:bg-lilac-200/5 transition-colors group"
              >
                {/* Action icon */}
                <div
                  className={cn(
                    'w-4 h-4 rounded flex items-center justify-center shrink-0',
                    getActionColor(file.action)
                  )}
                >
                  {getActionIcon(file.action)}
                </div>

                {/* File type icon */}
                {getFileIcon(file.language)}

                {/* File path */}
                <span className="text-[11px] text-lilac-300/70 truncate flex-1 group-hover:text-lilac-200 transition-colors font-mono">
                  {file.path}
                </span>

                {/* Action badge */}
                <span
                  className={cn(
                    'text-[9px] uppercase font-semibold tracking-wider px-1.5 py-0.5 rounded',
                    getActionColor(file.action)
                  )}
                >
                  {file.action}
                </span>
              </button>
            ))}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
});

export default FileChangesCard;
