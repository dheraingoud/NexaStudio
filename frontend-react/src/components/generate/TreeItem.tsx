import React from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  ChevronRight,
  ChevronDown,
  FileCode2,
  FileJson,
  FileText,
  FileType2,
  FolderOpen,
  FolderClosed,
} from 'lucide-react';
import { cn } from '../../lib/utils';

/* ═══════════════════════════════════════════════════════════════════
   Types
   ═══════════════════════════════════════════════════════════════════ */

export interface FileTreeNode {
  name: string;
  path: string;
  type: 'file' | 'folder';
  children?: FileTreeNode[];
  language?: string;
}

export interface TreeItemProps {
  node: FileTreeNode;
  depth?: number;
  selectedPath: string | null;
  onSelect: (path: string) => void;
  expandedFolders: Set<string>;
  onToggleFolder: (path: string) => void;
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

/* ═══════════════════════════════════════════════════════════════════
   TreeItem Component
   File tree item with folder/file icons and expand/collapse animation
   ═══════════════════════════════════════════════════════════════════ */

const TreeItem = React.memo(function TreeItem({
  node,
  depth = 0,
  selectedPath,
  onSelect,
  expandedFolders,
  onToggleFolder,
}: TreeItemProps) {
  const isFolder = node.type === 'folder';
  const isExpanded = expandedFolders.has(node.path);
  const isSelected = selectedPath === node.path;

  return (
    <>
      <button
        onClick={() => (isFolder ? onToggleFolder(node.path) : onSelect(node.path))}
        className={cn(
          'w-full flex items-center gap-1.5 py-[5px] px-2 text-[12px] rounded-md transition-all group',
          isSelected
            ? 'bg-mauve-500/20 text-lilac-100'
            : 'text-lilac-300/70 hover:bg-lilac-200/8 hover:text-lilac-200'
        )}
        style={{ paddingLeft: `${depth * 12 + 8}px` }}
        aria-expanded={isFolder ? isExpanded : undefined}
        aria-selected={isSelected}
      >
        {isFolder ? (
          <>
            {isExpanded ? (
              <ChevronDown className="w-3 h-3 text-lilac-400/50 shrink-0" />
            ) : (
              <ChevronRight className="w-3 h-3 text-lilac-400/50 shrink-0" />
            )}
            {isExpanded ? (
              <FolderOpen className="w-3.5 h-3.5 text-mauve-400 shrink-0" />
            ) : (
              <FolderClosed className="w-3.5 h-3.5 text-mauve-400/70 shrink-0" />
            )}
          </>
        ) : (
          <>
            <span className="w-3 shrink-0" />
            {getFileIcon(node.language || 'text')}
          </>
        )}
        <span className="truncate">{node.name}</span>
      </button>

      <AnimatePresence>
        {isFolder && isExpanded && node.children && (
          <motion.div
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: 'auto', opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            transition={{ duration: 0.15 }}
            className="overflow-hidden"
          >
            {node.children.map((child) => (
              <TreeItem
                key={child.path}
                node={child}
                depth={depth + 1}
                selectedPath={selectedPath}
                onSelect={onSelect}
                expandedFolders={expandedFolders}
                onToggleFolder={onToggleFolder}
              />
            ))}
          </motion.div>
        )}
      </AnimatePresence>
    </>
  );
});

export default TreeItem;
