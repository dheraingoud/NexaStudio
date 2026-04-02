import { useState, useRef, useEffect, useCallback, useMemo } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import {
  Send,
  Loader2,
  FileCode2,
  Eye,
  Code2,
  Monitor,
  Tablet,
  Smartphone,
  Copy,
  Check,
  Download,
  X,
  Square,
  RefreshCw,
  ArrowLeft,
  PanelLeftClose,
  PanelLeftOpen,
  Bot,
  FolderOpen,
  AlertTriangle,
} from 'lucide-react';
import { projectsApi } from '../../lib/api';
import { useProjectStore } from '../../lib/store';
import { cn } from '../../lib/utils';

// Extracted memoized components
import {
  TreeItem,
  type FileTreeNode,
  type GeneratedFile,
  ChatMessageBubble,
  type ChatMessage,
  DeviceFrame,
  type DeviceView,
} from '../../components/generate';

/* ═══════════════════════════════════════════════════════════════════
   Local Types
   ═══════════════════════════════════════════════════════════════════ */

type ViewMode = 'preview' | 'code';

/* ═══════════════════════════════════════════════════════════════════
   Helpers
   ═══════════════════════════════════════════════════════════════════ */

function getFileIcon(lang: string) {
  const iconClass = "w-3.5 h-3.5";
  const colors: Record<string, string> = {
    typescript: "text-blue-400",
    tsx: "text-blue-400",
    javascript: "text-yellow-400",
    jsx: "text-yellow-400",
    json: "text-amber-400",
    css: "text-pink-400",
    scss: "text-pink-400",
    html: "text-orange-400",
    markdown: "text-slate-400",
    svg: "text-emerald-400",
    vue: "text-green-400",
    svelte: "text-orange-500",
    astro: "text-purple-400",
    text: "text-slate-400",
  };
  return <FileCode2 className={`${iconClass} ${colors[lang] || "text-slate-400"}`} />;
}

function detectLanguage(path: string): string {
  const ext = path.split('.').pop()?.toLowerCase() || '';
  const map: Record<string, string> = {
    ts: 'typescript', tsx: 'tsx', js: 'javascript', jsx: 'jsx',
    json: 'json', css: 'css', html: 'html', md: 'markdown',
    svg: 'svg', txt: 'text', yml: 'yaml', yaml: 'yaml',
    vue: 'vue', mjs: 'javascript', cjs: 'javascript',
    svelte: 'svelte', astro: 'astro', scss: 'scss',
  };
  return map[ext] || 'text';
}

function buildFileTree(files: GeneratedFile[]): FileTreeNode[] {
  const root: FileTreeNode[] = [];
  for (const file of files) {
    const parts = file.path.replace(/^\//, '').split('/');
    let current = root;
    for (let i = 0; i < parts.length; i++) {
      const name = parts[i];
      const isFile = i === parts.length - 1;
      const pathSoFar = '/' + parts.slice(0, i + 1).join('/');
      let existing = current.find((n) => n.name === name);
      if (!existing) {
        existing = { name, path: pathSoFar, type: isFile ? 'file' : 'folder', children: isFile ? undefined : [], language: isFile ? file.language : undefined };
        current.push(existing);
      }
      if (!isFile && existing.children) current = existing.children;
    }
  }
  const sort = (nodes: FileTreeNode[]): FileTreeNode[] => {
    nodes.sort((a, b) => { if (a.type !== b.type) return a.type === 'folder' ? -1 : 1; return a.name.localeCompare(b.name); });
    nodes.forEach((n) => n.children && sort(n.children));
    return nodes;
  };
  return sort(root);
}

function generateId() { return Math.random().toString(36).substring(2, 9); }

const THINKING_STEPS = [
  'Analyzing your request...',
  'Understanding project context...',
  'Planning file structure...',
  'Generating components...',
  'Writing styles and logic...',
  'Validating output...',
];

/* ═══════════════════════════════════════════════════════════════════
   Main Page Component
   ═══════════════════════════════════════════════════════════════════ */

export default function GeneratePage() {
  const { id } = useParams<{ id: string }>();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { currentProject, setCurrentProject } = useProjectStore();

  const initialPrompt = searchParams.get('prompt') || '';
  const initialPromptRef = useRef(initialPrompt);

  // Chat state
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [prompt, setPrompt] = useState('');
  const [isGenerating, setIsGenerating] = useState(false);
  const [hasTriggeredInitial, setHasTriggeredInitial] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState<{ title: string; message: string } | null>(null);
  const [reloadTick, setReloadTick] = useState(0);
  const [thinkingStep, setThinkingStep] = useState(0);

  // Files state
  const [files, setFiles] = useState<GeneratedFile[]>([]);
  const [fileTree, setFileTree] = useState<FileTreeNode[]>([]);
  const [selectedFile, setSelectedFile] = useState<string | null>(null);
  const [expandedFolders, setExpandedFolders] = useState<Set<string>>(new Set());

  // View state
  const [viewMode, setViewMode] = useState<ViewMode>('preview');
  const [deviceView, setDeviceView] = useState<DeviceView>('desktop');
  const [showFiles, setShowFiles] = useState(() => window.innerWidth >= 768);
  const [showChat, setShowChat] = useState(true);
  const [previewKey, setPreviewKey] = useState(0);
  const generationAbortFn = useRef<(() => void) | null>(null);
  const loadedKeyRef = useRef<string | null>(null);
  const [chatWidth, setChatWidth] = useState(() => window.innerWidth >= 1280 ? 420 : 340);
  const isResizing = useRef(false);
  const [isResizingState, setIsResizingState] = useState(false);
  const chatPanelRef = useRef<HTMLDivElement>(null);

  // Code writing animation state
  const [isWritingFiles, setIsWritingFiles] = useState(false);
  const [writingFileIndex, setWritingFileIndex] = useState(-1);
  const [writingCharIndex, setWritingCharIndex] = useState(0);
  const [writingFiles, setWritingFiles] = useState<GeneratedFile[]>([]);
  const writtenPathsRef = useRef<Set<string>>(new Set());

  // Refs
  const chatContainerRef = useRef<HTMLDivElement>(null);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const [copied, setCopied] = useState(false);
  const userScrolledUp = useRef(false);

  // Update page title with project name
  useEffect(() => {
    if (currentProject?.name) {
      document.title = `${currentProject.name} - NexaStudio`;
    }
    return () => {
      document.title = 'NexaStudio';
    };
  }, [currentProject?.name]);

  // Thinking step animator
  useEffect(() => {
    if (!isGenerating) { setThinkingStep(0); return; }
    const interval = setInterval(() => {
      setThinkingStep((prev) => prev < THINKING_STEPS.length - 1 ? prev + 1 : prev);
    }, 2500);
    return () => clearInterval(interval);
  }, [isGenerating]);

  // Auto-resize textarea
  useEffect(() => {
    const el = textareaRef.current;
    if (!el) return;
    el.style.height = 'auto';
    el.style.height = Math.min(el.scrollHeight, 120) + 'px';
  }, [prompt]);

  // Chat panel drag-to-resize
  const handleResizeStart = useCallback((e: React.MouseEvent) => {
    e.preventDefault();
    isResizing.current = true;
    setIsResizingState(true);
    const startX = e.clientX;
    const startW = chatWidth;
    const onMove = (ev: MouseEvent) => {
      if (!isResizing.current) return;
      const delta = ev.clientX - startX;
      setChatWidth(Math.max(280, Math.min(600, startW + delta)));
    };
    const onUp = () => { isResizing.current = false; setIsResizingState(false); document.removeEventListener('mousemove', onMove); document.removeEventListener('mouseup', onUp); document.body.style.cursor = ''; document.body.style.userSelect = ''; };
    document.body.style.cursor = 'col-resize';
    document.body.style.userSelect = 'none';
    document.addEventListener('mousemove', onMove);
    document.addEventListener('mouseup', onUp);
  }, [chatWidth]);

  /* ─── Code Writing Animation ─── */
  const startWritingAnimation = useCallback((newFiles: GeneratedFile[]) => {
    if (newFiles.length === 0) return;
    setWritingFiles(newFiles);
    setWritingFileIndex(0);
    setWritingCharIndex(0);
    setIsWritingFiles(true);
    setViewMode('code');
    setShowFiles(true);
    // Select the first file to write
    setSelectedFile(newFiles[0].path);
    // Expand all necessary folders
    setExpandedFolders(prev => {
      const folders = new Set(prev);
      newFiles.forEach((c) => {
        const parts = c.path.replace(/^\//, '').split('/');
        for (let i = 1; i < parts.length; i++) folders.add('/' + parts.slice(0, i).join('/'));
      });
      return folders;
    });
  }, []);

  // Typewriter animation effect
  useEffect(() => {
    if (!isWritingFiles || writingFileIndex < 0 || writingFileIndex >= writingFiles.length) return;

    const currentFile = writingFiles[writingFileIndex];
    const totalChars = currentFile.content.length;

    if (writingCharIndex >= totalChars) {
      // Move to next file or finish
      const nextIndex = writingFileIndex + 1;
      if (nextIndex < writingFiles.length) {
        // Small delay between files
        const timer = setTimeout(() => {
          setWritingFileIndex(nextIndex);
          setWritingCharIndex(0);
          setSelectedFile(writingFiles[nextIndex].path);
        }, 300);
        return () => clearTimeout(timer);
      } else {
        // All files written — switch to preview
        const timer = setTimeout(() => {
          setIsWritingFiles(false);
          setWritingFiles([]);
          setWritingFileIndex(-1);
          setWritingCharIndex(0);
          setViewMode('preview');
          setPreviewKey(k => k + 1);
        }, 600);
        return () => clearTimeout(timer);
      }
    }

    // Type characters in chunks for speed (30-80 chars per tick)
    const chunkSize = Math.max(30, Math.min(80, Math.floor(totalChars / 40)));
    const timer = setTimeout(() => {
      setWritingCharIndex(prev => Math.min(prev + chunkSize, totalChars));
    }, 18);
    return () => clearTimeout(timer);
  }, [isWritingFiles, writingFileIndex, writingCharIndex, writingFiles]);

  // Get the displayed content of the currently-writing file
  const getDisplayedFileContent = useCallback((file: GeneratedFile) => {
    if (!isWritingFiles) return file.content;
    const wf = writingFiles[writingFileIndex];
    if (wf && wf.path === file.path) {
      return file.content.slice(0, writingCharIndex);
    }
    // Check if this file was already fully written
    const idx = writingFiles.findIndex(f => f.path === file.path);
    if (idx >= 0 && idx < writingFileIndex) return file.content;
    // Not yet written
    if (idx > writingFileIndex) return '';
    return file.content;
  }, [isWritingFiles, writingFiles, writingFileIndex, writingCharIndex]);

  const mapApiFilesToGenerated = useCallback((rawFiles: Array<{ path?: string; content?: string; filePath?: string } | string>): GeneratedFile[] => {
    if (!Array.isArray(rawFiles)) return [];
    return rawFiles
      .map((f) => {
        if (typeof f === 'string') {
          return {
            path: f,
            content: '',
            action: 'UPDATE' as const,
            language: detectLanguage(f),
          };
        }
        const path = f.path || f.filePath || '';
        return {
          path,
          content: f.content || '',
          action: 'UPDATE' as const,
          language: detectLanguage(path),
        };
      })
      .filter((f) => f.path);
  }, []);

  const syncFilesFromServer = useCallback(async (animateNewWrites = false) => {
    if (!id) return;
    try {
      const filesRes = await projectsApi.getFiles(id);
      const serverFiles = mapApiFilesToGenerated(filesRes.data || []);

      if (serverFiles.length > 0) {
        setFiles(serverFiles);
        setExpandedFolders((prev) => {
          const folders = new Set(prev);
          serverFiles.forEach((f) => {
            const parts = f.path.replace(/^\//, '').split('/');
            for (let i = 1; i < parts.length; i++) {
              folders.add('/' + parts.slice(0, i).join('/'));
            }
          });
          return folders;
        });

        setSelectedFile((prev) => {
          if (prev && serverFiles.some((f) => f.path === prev)) return prev;
          return serverFiles[0]?.path || prev;
        });

        if (animateNewWrites) {
          const newWrites = serverFiles.filter((f) => !writtenPathsRef.current.has(f.path));
          newWrites.forEach((f) => writtenPathsRef.current.add(f.path));
          if (newWrites.length > 0 && !isWritingFiles) {
            startWritingAnimation(newWrites);
          }
        }

        setPreviewKey((k) => k + 1);
      }
    } catch {
      // best-effort live sync; final response will still reconcile
    }
  }, [id, mapApiFilesToGenerated, isWritingFiles, startWritingAnimation]);

  /* ─── Generation Logic (SSE Streaming) ─── */
  const triggerGeneration = useCallback(async (userPrompt: string, isInitialPrompt = false) => {
    if (!userPrompt.trim() || !id || isGenerating) return;
    userScrolledUp.current = false;

    // Track generation state in localStorage for recovery
    localStorage.setItem(`nexa_gen_${id}`, 'pending');

    const assistantMessage: ChatMessage = { id: generateId(), role: 'assistant', content: '', timestamp: new Date(), status: 'thinking' };
    const streamedFiles: GeneratedFile[] = [];

    if (isInitialPrompt) {
      setMessages(prev => {
        const updated = [...prev];
        for (let i = updated.length - 1; i >= 0; i--) {
          if (updated[i].role === 'assistant') { updated[i] = assistantMessage; break; }
        }
        return updated;
      });
    } else {
      const userMessage: ChatMessage = { id: generateId(), role: 'user', content: userPrompt.trim(), timestamp: new Date() };
      setMessages(prev => [...prev, userMessage, assistantMessage]);
    }
    setPrompt('');
    setIsGenerating(true);
    setThinkingStep(0);
    writtenPathsRef.current = new Set(files.map((f) => f.path));

    const startTime = Date.now();
    let thinkingIdx = 0;

    const abort = projectsApi.generateStream(id, userPrompt.trim(), undefined, {
      onThinking: () => {
        // Map thinking messages to step indices for the ThinkingIndicator
        thinkingIdx = Math.min(thinkingIdx + 1, THINKING_STEPS.length - 1);
        setThinkingStep(thinkingIdx);
        // Update the assistant message status to show we're still generating
        setMessages(prev => prev.map(m =>
          m.id === assistantMessage.id ? { ...m, status: 'generating' as const } : m
        ));
      },

      onFile: (file) => {
        const genFile: GeneratedFile = {
          path: file.path,
          content: file.content || '',
          action: (file.action || 'CREATE') as 'CREATE' | 'UPDATE' | 'DELETE',
          language: detectLanguage(file.path),
        };
        streamedFiles.push(genFile);

        // Add to displayed files immediately
        setFiles(prev => {
          const existing = prev.findIndex(f => f.path === genFile.path);
          if (existing >= 0) { const updated = [...prev]; updated[existing] = genFile; return updated; }
          return [...prev, genFile];
        });

        // Expand folders
        setExpandedFolders(prev => {
          const folders = new Set(prev);
          const parts = genFile.path.replace(/^\//, '').split('/');
          for (let i = 1; i < parts.length; i++) folders.add('/' + parts.slice(0, i).join('/'));
          return folders;
        });

        // Auto-select first streamed file
        if (streamedFiles.length === 1) {
          setSelectedFile(genFile.path);
          setShowFiles(true);
          setViewMode('code');
        }

        // Start writing animation for new files
        if (!writtenPathsRef.current.has(genFile.path)) {
          writtenPathsRef.current.add(genFile.path);
          startWritingAnimation([genFile]);
        }
      },

      onComplete: (response) => {
        const processingTime = Date.now() - startTime;
        
        // Clear generation tracking - completed successfully
        localStorage.removeItem(`nexa_gen_${id}`);
        
        setMessages(prev => prev.map(m =>
          m.id === assistantMessage.id ? {
            ...m,
            content: response.explanation || `Generated ${streamedFiles.length} file${streamedFiles.length !== 1 ? 's' : ''} successfully. What would you like to improve?`,
            status: 'complete' as const,
            files: streamedFiles,
            processingTime,
          } : m
        ));

        // Final sync from server to ensure consistency
        void syncFilesFromServer(false);

        // Switch to preview
        window.setTimeout(() => {
          setIsWritingFiles(false);
          setWritingFiles([]);
          setWritingFileIndex(-1);
          setWritingCharIndex(0);
          setViewMode('preview');
          setPreviewKey((k) => k + 1);
        }, 600);

        setIsGenerating(false);
        generationAbortFn.current = null;
      },

      onError: (message) => {
        const processingTime = Date.now() - startTime;
        
        // Mark generation as errored in localStorage
        localStorage.setItem(`nexa_gen_${id}`, `error:${message}`);
        
        setMessages(prev => prev.map(m =>
          m.id === assistantMessage.id ? {
            ...m,
            content: `Error: ${message}. Please try again.`,
            status: 'error' as const,
            processingTime,
          } : m
        ));

        // If we got some files before the error, still show them
        if (streamedFiles.length > 0) {
          void syncFilesFromServer(false);
        }

        window.setTimeout(() => {
          setIsWritingFiles(false);
          setWritingFiles([]);
          setWritingFileIndex(-1);
          setWritingCharIndex(0);
          setViewMode('preview');
          setPreviewKey((k) => k + 1);
        }, 120);

        setIsGenerating(false);
        generationAbortFn.current = null;
      },
    });

    // Store abort function for the stop button
    generationAbortFn.current = abort;

    // Cleanup: abort on unmount or id change
    return () => {
      if (generationAbortFn.current) {
        generationAbortFn.current();
        generationAbortFn.current = null;
      }
    };
  }, [id, isGenerating, files, syncFilesFromServer, startWritingAnimation]);

  // Handle stop generation
  const handleStopGeneration = useCallback(() => {
    const abortFn = generationAbortFn.current;
    if (abortFn) {
      abortFn();
      generationAbortFn.current = null;
      // Clear pending state when user manually stops
      if (id) {
        localStorage.removeItem(`nexa_gen_${id}`);
      }
    }
  }, [id]);

  /* ─── Load project, files, and chat history ─── */
  useEffect(() => {
    if (!id) return;

    const loadKey = `${id}:${reloadTick}`;
    if (loadedKeyRef.current === loadKey) return;
    loadedKeyRef.current = loadKey;

    setIsLoading(true);
    setLoadError(null);

    const timeout = setTimeout(() => {
      setIsLoading(false);
      setLoadError({
        title: 'Project loading timed out',
        message: 'The backend is taking longer than expected. Please retry.',
      });
    }, 30000);

    Promise.all([
      projectsApi.getById(id),
      projectsApi.getFiles(id),
      projectsApi.getPrompts(id).catch(() => ({ data: [] })),
    ]).then(([projectRes, filesRes, promptsRes]) => {
      clearTimeout(timeout);
      setCurrentProject(projectRes.data);

      const rawFiles = filesRes.data || [];
      const existingFiles: GeneratedFile[] = Array.isArray(rawFiles)
        ? rawFiles.map((f: { path?: string; content?: string; filePath?: string } | string) => {
          if (typeof f === 'string') return { path: f, content: '', action: 'CREATE' as const, language: detectLanguage(f) };
          const path = f.path || f.filePath || '';
          return { path, content: f.content || '', action: 'CREATE' as const, language: detectLanguage(path) };
        }).filter(f => f.path)
        : [];

      if (existingFiles.length > 0) {
        setFiles(existingFiles);
        const rootFolders = new Set<string>();
        existingFiles.forEach((f) => { const parts = f.path.replace(/^\//, '').split('/'); if (parts.length > 1) rootFolders.add('/' + parts[0]); });
        setExpandedFolders(rootFolders);
        setPreviewKey((k) => k + 1);
      }

      // Build chat history from saved prompts
      const rawPrompts = Array.isArray(promptsRes.data) ? promptsRes.data : [];
      const historyMessages: ChatMessage[] = [];

      for (const p of rawPrompts) {
        if (p.text || p.prompt) {
          historyMessages.push({ id: p.id || generateId(), role: 'user', content: p.text || p.prompt || '', timestamp: p.createdAt ? new Date(p.createdAt) : new Date() });
        }
        if (p.aiResponse || p.errorMessage) {
          historyMessages.push({
            id: generateId(), role: 'assistant',
            content: p.status === 'FAILED' ? `Error: ${p.errorMessage || 'Generation failed'}` : (p.aiResponse || 'Completed.'),
            timestamp: p.completedAt ? new Date(p.completedAt) : new Date(),
            status: p.status === 'FAILED' ? 'error' : 'complete',
            processingTime: p.processingTimeMs,
          });
        }
      }

      const savedPrompt = initialPromptRef.current;
      const initialKey = id && savedPrompt ? `nexa.initialPrompt.${id}.${savedPrompt}` : '';
      const promptAlreadyUsed = initialKey ? sessionStorage.getItem(initialKey) === '1' : false;

      // If there's an initial prompt that hasn't been used yet, show it as the first user message
      const welcomeMessages: ChatMessage[] = [];
      
      if (savedPrompt && !promptAlreadyUsed) {
        welcomeMessages.push({
          id: generateId(), role: 'user',
          content: savedPrompt,
          timestamp: new Date(),
        });
        welcomeMessages.push({
          id: generateId(), role: 'assistant',
          content: existingFiles.length > 0
            ? `Project loaded with ${existingFiles.length} files. Processing your request...`
            : 'Starting generation...',
          timestamp: new Date(), status: 'complete',
        });
      } else {
        welcomeMessages.push({
          id: generateId(), role: 'assistant',
          content: existingFiles.length > 0
            ? `Project loaded with ${existingFiles.length} files. How can I help you improve it?`
            : "Ready to build. Describe what you'd like to create.",
          timestamp: new Date(), status: 'complete',
        });
      }

      setMessages(historyMessages.length > 0 ? [...welcomeMessages, ...historyMessages] : welcomeMessages);
      setIsLoading(false);
    }).catch((err: unknown) => {
      clearTimeout(timeout);
      setIsLoading(false);
      loadedKeyRef.current = null;

      const status = (err as { response?: { status?: number } })?.response?.status;
      if (status === 404) {
        setLoadError({ title: 'Project not found', message: 'This project may have been deleted or is no longer accessible.' });
        return;
      }

      if (status === 403) {
        setLoadError({ title: 'Access denied', message: 'You do not have permission to access this project.' });
        return;
      }

      setLoadError({ title: 'Unable to load project', message: 'We could not load this project right now. Please retry.' });
    });

    return () => clearTimeout(timeout);
  }, [id, setCurrentProject, reloadTick]);

  /* ─── Auto-trigger initial prompt from NewProjectPage ─── */
  useEffect(() => {
    const savedPrompt = initialPromptRef.current;
    if (!savedPrompt || !id) return;
    const key = `nexa.initialPrompt.${id}.${savedPrompt}`;
    const alreadyUsed = sessionStorage.getItem(key) === '1';
    if (!alreadyUsed && !hasTriggeredInitial && !isLoading && currentProject) {
      setHasTriggeredInitial(true);
      sessionStorage.setItem(key, '1');
      navigate(`/projects/${id}/generate`, { replace: true });
      setTimeout(() => triggerGeneration(savedPrompt, true), 300);
    }
  }, [hasTriggeredInitial, isLoading, id, currentProject, triggerGeneration, navigate]);

  useEffect(() => { if (files.length > 0) setFileTree(buildFileTree(files)); }, [files]);

  useEffect(() => {
    const container = chatContainerRef.current;
    if (!container || userScrolledUp.current) return;
    // Keep chat pinned only when user hasn't intentionally scrolled up.
    container.scrollTop = container.scrollHeight;
  }, [messages]);

  useEffect(() => {
    const container = chatContainerRef.current;
    if (!container) return;
    const handleScroll = () => {
      const { scrollTop, scrollHeight, clientHeight } = container;
      const distanceFromBottom = scrollHeight - scrollTop - clientHeight;
      // Treat any meaningful movement away from bottom as manual reading mode.
      userScrolledUp.current = distanceFromBottom > 8;
    };
    container.addEventListener('scroll', handleScroll, { passive: true });
    return () => container.removeEventListener('scroll', handleScroll);
  }, []);

  useEffect(() => {
    if (textareaRef.current) { textareaRef.current.style.height = 'auto'; textareaRef.current.style.height = `${Math.min(textareaRef.current.scrollHeight, 150)}px`; }
  }, [prompt]);

  const activeFile = files.find((f) => f.path === selectedFile);
  const displayedContent = activeFile ? getDisplayedFileContent(activeFile) : '';
  const toggleFolder = useCallback((path: string) => { setExpandedFolders((prev) => { const next = new Set(prev); if (next.has(path)) next.delete(path); else next.add(path); return next; }); }, []);
  const handleCopy = useCallback(() => { if (activeFile) { navigator.clipboard.writeText(activeFile.content); setCopied(true); setTimeout(() => setCopied(false), 2000); } }, [activeFile]);
  const handleGenerate = () => triggerGeneration(prompt);
  const handleKeyDown = (e: React.KeyboardEvent) => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); handleGenerate(); } };
  const handleViewFile = useCallback((path: string) => {
    setSelectedFile(path); setShowFiles(true); setViewMode('code');
    setExpandedFolders(prev => { const folders = new Set(prev); const parts = path.replace(/^\//, '').split('/'); for (let i = 1; i < parts.length; i++) folders.add('/' + parts.slice(0, i).join('/')); return folders; });
  }, []);

  /* ─── Preview HTML ─── */
  const projectType = currentProject?.type?.toUpperCase() || 'REACT';

  const previewHtml = useMemo(() => {
    const normalizeFilePath = (path: string) => { const clean = path.replace(/\\/g, '/'); return clean.startsWith('/') ? clean : `/${clean.replace(/^\.\/?/, '')}`; };
    const cssFiles = files.filter(f => f.path.endsWith('.css'));
    const globalCss = cssFiles.map(f => f.content || '').join('\n');

    // ── Empty state ──
    const emptyHtml = `<!DOCTYPE html><html><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1.0"><script src="https://cdn.tailwindcss.com"><\/script><style>*{box-sizing:border-box}body{font-family:system-ui,-apple-system,sans-serif;background:#09080e;color:#e6e9f9;display:flex;align-items:center;justify-content:center;min-height:100vh;margin:0;text-align:center}.c{padding:2rem;max-width:500px}h2{font-size:1.25rem;margin-bottom:0.75rem;background:linear-gradient(135deg,#915F6D,#a78bfa);-webkit-background-clip:text;-webkit-text-fill-color:transparent}p{color:#a5a5c0;font-size:0.875rem;line-height:1.6}</style></head><body><div class="c"><h2>Ready to Generate</h2><p>Describe what you want to build and the preview will appear here.</p></div></body></html>`;

    // ── Shared tailwind config ──
    const twConfig = `tailwind.config={darkMode:'class',theme:{extend:{colors:{background:'#09090b',foreground:'#fafafa',primary:{DEFAULT:'#7c3aed',foreground:'#fafafa'},secondary:{DEFAULT:'#18181b',foreground:'#a1a1aa'},muted:{DEFAULT:'#27272a',foreground:'#a1a1aa'},accent:{DEFAULT:'#8b5cf6',foreground:'#fafafa'},card:{DEFAULT:'rgba(24,24,27,0.5)',foreground:'#fafafa'},border:'#27272a',ring:'#7c3aed',destructive:{DEFAULT:'#ef4444',foreground:'#fafafa'}},animation:{'fade-in':'fadeIn 0.6s ease-out','slide-up':'slideUp 0.6s ease-out','slide-down':'slideDown 0.4s ease-out','scale-in':'scaleIn 0.3s ease-out','spin-slow':'spin 3s linear infinite','pulse-slow':'pulse 3s ease-in-out infinite','float':'float 6s ease-in-out infinite','glow':'glow 2s ease-in-out infinite','shimmer':'shimmer 2s linear infinite'},keyframes:{fadeIn:{'0%':{opacity:'0'},'100%':{opacity:'1'}},slideUp:{'0%':{opacity:'0',transform:'translateY(20px)'},'100%':{opacity:'1',transform:'translateY(0)'}},slideDown:{'0%':{opacity:'0',transform:'translateY(-10px)'},'100%':{opacity:'1',transform:'translateY(0)'}},scaleIn:{'0%':{opacity:'0',transform:'scale(0.95)'},'100%':{opacity:'1',transform:'scale(1)'}},float:{'0%,100%':{transform:'translateY(0)'},'50%':{transform:'translateY(-10px)'}},glow:{'0%,100%':{boxShadow:'0 0 5px rgba(139,92,246,0.3)'},'50%':{boxShadow:'0 0 20px rgba(139,92,246,0.6)'}},shimmer:{'0%':{backgroundPosition:'-200% 0'},'100%':{backgroundPosition:'200% 0'}}},backdropBlur:{xs:'2px'},backgroundImage:{'gradient-radial':'radial-gradient(var(--tw-gradient-stops))','gradient-conic':'conic-gradient(from 180deg at 50% 50%, var(--tw-gradient-stops))'}}}}`;

    // ── Shared base styles ──
    const baseStyle = `${globalCss}*{box-sizing:border-box}html{scroll-behavior:smooth}body{margin:0;font-family:'Inter',system-ui,-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;background:#09090b;color:#fafafa;-webkit-font-smoothing:antialiased;-moz-osx-font-smoothing:grayscale}#root,#app{min-height:100vh}::selection{background:rgba(139,92,246,0.3);color:#fff}::-webkit-scrollbar{width:6px}::-webkit-scrollbar-track{background:#09090b}::-webkit-scrollbar-thumb{background:#27272a;border-radius:3px}::-webkit-scrollbar-thumb:hover{background:#3f3f46}`;

    // ── VANILLA preview ──
    if (projectType === 'VANILLA') {
      const htmlFile = files.find(f => f.path.endsWith('/index.html') || f.path.endsWith('.html'));
      if (!htmlFile?.content) return emptyHtml;
      let html = htmlFile.content;
      // Inject CSS files
      const cssContent = cssFiles.map(f => `<style>${f.content || ''}</style>`).join('');
      html = html.replace('</head>', `${cssContent}</head>`);
      // Inject JS files inline
      const jsFiles = files.filter(f => f.path.endsWith('.js') && f.content);
      const jsContent = jsFiles.map(f => `<script>${f.content}<\/script>`).join('');
      html = html.replace('</body>', `${jsContent}</body>`);
      return html;
    }

    // ── VUE preview ──
    if (projectType === 'VUE') {
      const appFile = files.find(f => f.path.includes('/App.vue'));
      const mainEntry = appFile || files.find(f => f.path.endsWith('.vue') && f.content);
      if (!mainEntry?.content) return emptyHtml;

      const vueFiles = files.filter(f => f.path.endsWith('.vue') && f.content);
      const tsFiles = files.filter(f => /\.(t|j)s$/.test(f.path) && f.content && !f.path.endsWith('.d.ts'));
      const allFiles = [...vueFiles, ...tsFiles, ...cssFiles].reduce<Record<string, string>>((acc, f) => { acc[normalizeFilePath(f.path)] = f.content || ''; return acc; }, {});
      const entryPath = normalizeFilePath(mainEntry.path);

      return `<!DOCTYPE html><html lang="en" class="dark"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1.0"><title>Preview</title>
<link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin><link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
<script type="importmap">{"imports":{
"vue":"https://unpkg.com/vue@3/dist/vue.esm-browser.js",
"vue-router":"https://esm.sh/vue-router@4?dev&external=vue",
"pinia":"https://esm.sh/pinia@2?dev&external=vue",
"@vue/devtools-api":"https://esm.sh/@vue/devtools-api@6?dev",
"axios":"https://esm.sh/axios@1?dev",
"date-fns":"https://esm.sh/date-fns@3?dev",
"uuid":"https://esm.sh/uuid@9?dev"
}}</script>
<script src="https://cdn.tailwindcss.com"><\/script>
<script>${twConfig}<\/script>
<script src="https://unpkg.com/@babel/standalone/babel.min.js"><\/script>
<style>${baseStyle}</style>
<script id="__NEXA_DATA__" type="application/json">${JSON.stringify({ files: allFiles, entry: entryPath }).replace(/</g, '\\u003c')}</script>
</head><body><div id="app"></div>
<script type="module">
import{createApp,ref,reactive,computed,watch,watchEffect,onMounted,onUnmounted,defineComponent,h,toRefs,nextTick,provide,inject}from'vue';
import{createRouter,createMemoryHistory,RouterLink,RouterView,useRouter,useRoute}from'vue-router';
import{createPinia,defineStore}from'pinia';
const data=JSON.parse(document.getElementById('__NEXA_DATA__').textContent);
const files=data.files;const entryPath=data.entry;
const normalizePath=(p)=>{const c=p.replace(/\\\\\\\\/g,'/');return c.startsWith('/')?c:'/'+c.replace(/^\\.\\/?/,'')};
const resolveAlias=(s)=>{if(!s.startsWith('@/')&&!s.startsWith('~/'))return s;const rest=s.slice(2);const srcPath='/src/'+rest;const rootPath='/'+rest;if(Object.keys(files).some(f=>f===srcPath||f.startsWith(srcPath+'.')||f.startsWith(srcPath+'/')))return srcPath;return rootPath};
const resolvePath=(from,spec)=>{const r=resolveAlias(spec);if(!r.startsWith('.')&&!r.startsWith('/'))return r;const d=normalizePath(from).replace(/\\/[^\\/]*$/,'/');const j=r.startsWith('/')?r:d+r;const parts=normalizePath(j).split('/');const stack=[];for(const p of parts){if(!p||p==='.')continue;if(p==='..')stack.pop();else stack.push(p)}return'/'+stack.join('/')};
const resolveFile=(path)=>{if(files[path])return path;for(const ext of['.vue','.ts','.js','/index.vue','/index.ts','/index.js']){if(files[path+ext])return path+ext}return path};
const extractSFCParts=(src)=>{const tmpl=src.match(/<template[^>]*>([\\s\\S]*?)<\\/template>/);const script=src.match(/<script[^>]*>([\\s\\S]*?)<\\/script>/);const style=src.match(/<style[^>]*>([\\s\\S]*?)<\\/style>/);return{template:tmpl?tmpl[1]:'<div></div>',script:script?script[1]:'',style:style?style[1]:''}};
const vueExternals=['vue','vue-router','pinia','axios','date-fns','uuid'];
const stripImports=(code)=>{let cleaned=code;const importMap={};cleaned=cleaned.replace(/import\\s+[\\s\\S]*?from\\s+['"]([^'"]+)['"]/g,(m,spec)=>{if(!vueExternals.some(e=>spec===e||spec.startsWith(e+'/')))return m;return'/* '+m+' */'});cleaned=cleaned.replace(/export\\s+default\\s+/,'const __default__=');return cleaned};
/* Build Vue component from SFC source */
const buildComp=(src)=>{
  if(!src)return defineComponent({render(){return h('div')}});
  const parts=extractSFCParts(src);
  if(parts.style){const s=document.createElement('style');s.textContent=parts.style;document.head.appendChild(s)}
  /* Parse <script setup> variable names from ref()/reactive()/computed()/etc declarations */
  const isSetup=/<script[^>]*\\bsetup\\b/.test(src);
  const scriptSrc=parts.script||'';
  const varNames=[];
  const refPattern=/(?:const|let|var)\\s+(\\w+)\\s*=\\s*(?:ref|reactive|computed|shallowRef|shallowReactive)\\s*[<(]/g;
  let m;while((m=refPattern.exec(scriptSrc))!==null)varNames.push(m[1]);
  /* Also detect simple const declarations and function declarations */
  const constPattern=/(?:const|let|var)\\s+(\\w+)\\s*=/g;
  while((m=constPattern.exec(scriptSrc))!==null){if(!varNames.includes(m[1]))varNames.push(m[1])}
  const fnPattern=/(?:function|const)\\s+(\\w+)\\s*(?:=\\s*)?\\(/g;
  while((m=fnPattern.exec(scriptSrc))!==null){if(!varNames.includes(m[1]))varNames.push(m[1])}
  return defineComponent({
    template:parts.template,
    setup(){
      const data={};
      varNames.forEach(n=>{data[n]=ref('')});
      /* Provide router stubs and common Vue utilities */
      data.router={push:()=>{},replace:()=>{},back:()=>{},go:()=>{}};
      data.route={params:{},query:{},path:'/',name:'',meta:{}};
      return data;
    }
  });
};
/* Register all .vue files as global components */
const vueComponents=new Map();
for(const[fpath,src]of Object.entries(files)){
  if(!fpath.endsWith('.vue')||!src)continue;
  const name=fpath.split('/').pop().replace('.vue','');
  try{vueComponents.set(name,buildComp(src))}catch(e){console.warn('Vue comp build error:',name,e)}
}
try{
const App=vueComponents.get('App')||buildComp(files[entryPath]);
const routes=[{path:'/:pathMatch(.*)*',component:defineComponent({template:'<div></div>'})}];
const router=createRouter({history:createMemoryHistory(),routes});
const pinia=createPinia();
const app=createApp(App);app.use(router);app.use(pinia);
app.component('RouterLink',RouterLink);app.component('RouterView',RouterView);
app.component('router-link',RouterLink);app.component('router-view',RouterView);
/* Register all project components globally */
for(const[name,comp]of vueComponents){if(name!=='App'){app.component(name,comp);app.component(name.replace(/([A-Z])/g,'-$1').replace(/^-/,'').toLowerCase(),comp)}}
app.config.warnHandler=()=>{};
app.config.errorHandler=(err)=>console.warn('Vue preview:',err);
/* Also inject all CSS files */
for(const[fpath,src]of Object.entries(files)){if(fpath.endsWith('.css')&&src){const s=document.createElement('style');s.textContent=src;document.head.appendChild(s)}}
app.mount('#app');
}catch(e){console.error('Preview error:',e);document.getElementById('app').innerHTML='<div style="padding:2rem;text-align:center"><h3 style="color:#dc2626">Vue Preview Error</h3><pre style="text-align:left;overflow:auto;font-size:12px">'+e.message+'<\\/pre></div>'}
<\/script></body></html>`;
    }

    // ── SVELTE preview ──
    if (projectType === 'SVELTE') {
      const pageFile = files.find(f => f.path.includes('+page.svelte') || f.path.includes('/App.svelte'));
      const mainSvelte = pageFile || files.find(f => f.path.endsWith('.svelte') && f.content);
      if (!mainSvelte?.content) return emptyHtml;

      const svelteFiles = files.filter(f => f.path.endsWith('.svelte') && f.content);
      const allContent = [...svelteFiles, ...cssFiles].reduce<Record<string, string>>((acc, f) => { acc[normalizeFilePath(f.path)] = f.content || ''; return acc; }, {});

      return `<!DOCTYPE html><html lang="en" class="dark"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1.0"><title>Preview</title>
<link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin><link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
<script src="https://cdn.tailwindcss.com"><\/script>
<script>${twConfig}<\/script>
<style>${baseStyle}</style>
<script id="__NEXA_DATA__" type="application/json">${JSON.stringify({ files: allContent, entry: normalizeFilePath(mainSvelte.path) }).replace(/</g, '\\u003c')}</script>
</head><body><div id="app"></div>
<script type="module">
const data=JSON.parse(document.getElementById('__NEXA_DATA__').textContent);
const files=data.files;const entryPath=data.entry;
/* Inject all CSS files */
for(const[fp,src]of Object.entries(files)){if(fp.endsWith('.css')&&src){const s=document.createElement('style');s.textContent=src;document.head.appendChild(s)}}
try{
const src=files[entryPath];
if(!src)throw new Error('Entry file not found: '+entryPath);
/* Try Svelte compiler first */
let mounted=false;
try{
const{compile}=await import('https://esm.sh/svelte@4/compiler?dev');
const result=compile(src,{generate:'dom',css:'injected',filename:entryPath,sveltePath:'https://esm.sh/svelte@4'});
const code=result.js.code
  .replace(/from\\s*["']svelte\\/internal["']/g,'from "https://esm.sh/svelte@4/internal?dev"')
  .replace(/from\\s*["']svelte\\/store["']/g,'from "https://esm.sh/svelte@4/store?dev"')
  .replace(/from\\s*["']svelte\\/motion["']/g,'from "https://esm.sh/svelte@4/motion?dev"')
  .replace(/from\\s*["']svelte\\/transition["']/g,'from "https://esm.sh/svelte@4/transition?dev"')
  .replace(/from\\s*["']svelte\\/easing["']/g,'from "https://esm.sh/svelte@4/easing?dev"')
  .replace(/from\\s*["']svelte["']/g,'from "https://esm.sh/svelte@4?dev"');
const blob=URL.createObjectURL(new Blob([code],{type:'text/javascript'}));
const mod=await import(blob);
new mod.default({target:document.getElementById('app')});
mounted=true;
}catch(compileErr){console.warn('Svelte compile failed, using static fallback:',compileErr)}
/* Static fallback: render the HTML from the svelte template */
if(!mounted){
  const tmpl=src.match(/<template[^>]*>([\\s\\S]*?)<\\/template>/);
  const html=tmpl?tmpl[1]:src.replace(/<script[\\s\\S]*?<\\/script>/g,'').replace(/<style[\\s\\S]*?<\\/style>/g,'');
  const style=src.match(/<style[^>]*>([\\s\\S]*?)<\\/style>/);
  if(style){const s=document.createElement('style');s.textContent=style[1];document.head.appendChild(s)}
  const cleaned=html
    .replace(/\\{[^}]*\\}/g,'Sample Text')
    .replace(/#each\\s+[^}]*\\}/g,'')
    .replace(/#if\\s+[^}]*\\}/g,'')
    .replace(/\\{\\/each\\}/g,'')
    .replace(/\\{\\/if\\}/g,'')
    .replace(/on:\\w+\\|?[^=]*=[^"]*"/g,'');
  document.getElementById('app').innerHTML=cleaned+'<div style="position:fixed;bottom:12px;right:12px;background:rgba(255,62,0,0.1);border:1px solid rgba(255,62,0,0.2);color:#fca;padding:6px 12px;border-radius:8px;font-size:11px;backdrop-filter:blur(8px);z-index:9999;font-family:system-ui">Svelte Static Preview</div>';
}
}catch(e){console.error('Preview error:',e);document.getElementById('app').innerHTML='<div style="padding:2rem;text-align:center"><h3 style="color:#dc2626">Svelte Preview Error</h3><pre style="text-align:left;overflow:auto;font-size:12px">'+e.message+'<\\/pre></div>'}
<\/script></body></html>`;
    }

    // ── SOLID preview — rendered via React pipeline with solid-js shims ──
    // Falls through to the React/Babel handler below

    // ── ANGULAR preview (limited — show rendered HTML from template) ──
    if (projectType === 'ANGULAR') {
      const appComponent = files.find(f => f.path.includes('app.component.ts') && f.content);
      const templateFile = files.find(f => f.path.includes('app.component.html') && f.content);
      const templateContent = templateFile?.content || '';
      const inlineTemplate = appComponent?.content?.match(/template\s*:\s*`([\s\S]*?)`/)?.[1] || '';
      const htmlToRender = templateContent || inlineTemplate;

      // Collect all Angular CSS (component styles + global styles)
      const angularCss = files.filter(f => f.path.endsWith('.css') && f.content).map(f => f.content).join('\n');
      const componentCss = appComponent?.content?.match(/styles\s*:\s*\[\s*`([\s\S]*?)`\s*\]/)?.[1] || '';

      if (!htmlToRender) return emptyHtml;

      const cleanedHtml = htmlToRender
        .replace(/@for\s*\([^)]*\)\s*\{/g, '<!-- loop -->')
        .replace(/@if\s*\([^)]*\)\s*\{/g, '')
        .replace(/@else\s*\{/g, '')
        .replace(/\}\s*$/gm, '')
        .replace(/\{\{[^}]*\}\}/g, 'Sample Text')
        .replace(/\[routerLink\]="[^"]*"/g, 'href="#"')
        .replace(/\(click\)="[^"]*"/g, '')
        .replace(/\*ngFor="[^"]*"/g, '')
        .replace(/\*ngIf="[^"]*"/g, '')
        .replace(/\[ngClass\]="[^"]*"/g, '')
        .replace(/\[class\.\w+\]="[^"]*"/g, '')
        .replace(/\[style\.\w+\]="[^"]*"/g, '')
        .replace(/<router-outlet\s*\/?>/g, '<div></div>')
        .replace(/<app-\w+[^>]*\/?>/g, '');

      return `<!DOCTYPE html><html lang="en" class="dark"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1.0"><title>Preview</title>
<link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin><link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
<script src="https://cdn.tailwindcss.com"><\/script>
<script>${twConfig}<\/script>
<style>${baseStyle}
${angularCss}
${componentCss}
.nexa-angular-badge{position:fixed;bottom:12px;right:12px;background:rgba(220,38,38,0.1);border:1px solid rgba(220,38,38,0.2);color:#fca5a5;padding:6px 12px;border-radius:8px;font-size:11px;backdrop-filter:blur(8px);z-index:9999;font-family:system-ui}
</style>
</head><body><div id="app">${cleanedHtml}</div>
<div class="nexa-angular-badge">Angular Static Preview — Export to run full app</div>
</body></html>`;
    }

    // ── ASTRO preview (static rendering) ──
    if (projectType === 'ASTRO') {
      const pageFile = files.find(f => f.path.includes('/pages/index.astro') || f.path.endsWith('.astro'));
      if (!pageFile?.content) return emptyHtml;

      // Extract HTML body from astro file (between --- frontmatter ---)
      const astroContent = pageFile.content;
      const htmlPart = astroContent.replace(/---[\s\S]*?---/, '').trim();
      // Strip JSX-like expressions
      const cleanedHtml = htmlPart
        .replace(/\{[^}]*\}/g, 'Sample Text')
        .replace(/<[A-Z][a-zA-Z]*[^>]*\/>/g, '')  // Remove self-closing components
        .replace(/<([A-Z][a-zA-Z]*)[^>]*>([\s\S]*?)<\/\1>/g, '$2');  // Unwrap components to children

      return `<!DOCTYPE html><html lang="en" class="dark"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1.0"><title>Preview</title>
<link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin><link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
<script src="https://cdn.tailwindcss.com"><\/script>
<script>${twConfig}<\/script>
<style>${baseStyle}
.nexa-astro-badge{position:fixed;bottom:12px;right:12px;background:rgba(147,51,234,0.1);border:1px solid rgba(147,51,234,0.2);color:#c4b5fd;padding:6px 12px;border-radius:8px;font-size:11px;backdrop-filter:blur(8px);z-index:9999;font-family:system-ui}
</style>
</head><body>${cleanedHtml}
<div class="nexa-astro-badge">Astro Static Preview — Export to run full app</div>
</body></html>`;
    }

    // ── REACT / NEXTJS / REMIX / SOLID (JSX-based preview) ──
    const pageFile = files.find(f => f.path.includes('/app/page.tsx') || f.path.includes('/pages/index.tsx'));
    const mainFile = files.find(f => f.path.endsWith('/main.tsx') || f.path.endsWith('/main.jsx'));
    const appFile = files.find(f => f.path.includes('/App.tsx') || f.path.includes('/App.jsx'));
    // For Remix, look for route files
    const remixEntry = projectType === 'REMIX' ? files.find(f => f.path.includes('/routes/_index') || f.path.includes('/routes/index') || f.path.includes('/root.tsx')) : null;
    const mainComponentFile = (projectType === 'REACT' || projectType === 'SOLID')
      ? (mainFile || appFile || pageFile || remixEntry || files.find(f => f.path.endsWith('.tsx') && f.content && (f.content.includes('export default') || f.content.includes('export function'))))
      : (remixEntry || pageFile || mainFile || appFile || files.find(f => f.path.endsWith('.tsx') && f.content && (f.content.includes('export default') || f.content.includes('export function'))));

    if (!mainComponentFile || !mainComponentFile.content) return emptyHtml;

    const codeFiles = files.filter(f => /\.(t|j)sx?$/.test(f.path) && f.content);
    const fileMap = [...codeFiles, ...cssFiles].reduce<Record<string, string>>((acc, f) => { acc[normalizeFilePath(f.path)] = f.content || ''; return acc; }, {});
    const entryPath = normalizeFilePath(mainComponentFile.path);

    // Build shim URLs (Next.js + Remix)
    const shimEntries = projectType === 'REMIX' ? `
"@remix-run/react":"data:text/javascript,${encodeURIComponent("import React from'react';export const Link=({to,children,...r})=>React.createElement('a',{href:to,...r},children);export const Form=({children,...r})=>React.createElement('form',r,children);export const Outlet=()=>React.createElement('div');export const useLoaderData=()=>({});export const useActionData=()=>null;export const useNavigation=()=>({state:'idle'});export const useNavigate=()=>()=>{};export const useParams=()=>({});export const useSearchParams=()=>[new URLSearchParams(),()=>{}];export const json=(d)=>d;export const redirect=(u)=>new Response(null,{status:302,headers:{Location:u}});export const NavLink=Link;")}",
"@remix-run/node":"data:text/javascript,${encodeURIComponent("export const json=(d)=>d;export const redirect=(u)=>new Response(null,{status:302,headers:{Location:u}});")}"`  : '';

    const nextShims = projectType === 'NEXTJS' ? `
"next/link":"data:text/javascript,${encodeURIComponent("import React from'react';export default function Link({href,children,...rest}){return React.createElement('a',{href,...rest},children);}")}",
"next/image":"data:text/javascript,${encodeURIComponent("import React from'react';export default function Image(props){return React.createElement('img',props);}")}",
"next/head":"data:text/javascript,${encodeURIComponent("import React from'react';export default function Head(){return null;}")}",
"next/script":"data:text/javascript,${encodeURIComponent("import React from'react';export default function Script(props){return React.createElement('script',props);}")}",
"next/navigation":"data:text/javascript,${encodeURIComponent("export const useRouter=()=>({push:()=>{},replace:()=>{},back:()=>{},prefetch:async()=>{}});export const usePathname=()=>'';export const useSearchParams=()=>new URLSearchParams();export const redirect=()=>{throw new Error('redirect()')};export const notFound=()=>{throw new Error('notFound()')};export const useParams=()=>({});")}",
"next/router":"data:text/javascript,${encodeURIComponent("export const useRouter=()=>({push:()=>{},replace:()=>{},back:()=>{}});export default{};")}",
"next/font/google":"data:text/javascript,${encodeURIComponent("export const Inter=()=>({className:''});export const Poppins=()=>({className:''});export default{};")}"`  : '';

    const solidShims = projectType === 'SOLID' ? `
"solid-js":"data:text/javascript,${encodeURIComponent("import React from'react';export const createSignal=(init)=>{const[v,s]=React.useState(init);return[()=>v,s]};export const createEffect=(fn)=>React.useEffect(fn,[]);export const onMount=(fn)=>React.useEffect(fn,[]);export const onCleanup=(fn)=>React.useEffect(()=>fn,[]);export const createMemo=(fn)=>React.useMemo(fn,[]);export const createResource=(fn)=>{const[v,s]=React.useState(null);React.useEffect(()=>{fn().then(s)},[]);return[()=>v,{loading:!v,error:null,refetch:()=>fn().then(s)}]};export const Show=({when,children,fallback})=>when?typeof children==='function'?children():children:(fallback||null);export const For=({each,children})=>(each||[]).map((item,i)=>children(item,()=>i));export const Switch=({children,fallback})=>{const arr=React.Children.toArray(children);for(const c of arr){if(c?.props?.when)return c}return fallback||null};export const Match=({when,children})=>when?(typeof children==='function'?children():children):null;export const Index=({each,children})=>(each||[]).map((item,i)=>children(()=>item,()=>i));export const Dynamic=({component:C,...p})=>C?React.createElement(C,p):null;export const Suspense=({children})=>typeof children==='function'?children():children;export const ErrorBoundary=({children})=>typeof children==='function'?children():children;export const batch=(fn)=>fn();export const untrack=(fn)=>fn();export const mergeProps=(...s)=>Object.assign({},...s);export const splitProps=(p,...keys)=>{const r=keys.map(k=>{const o={};k.forEach(n=>{if(n in p)o[n]=p[n]});return o});return[...r,p]};export const children=(fn)=>fn;export const lazy=(fn)=>React.lazy(fn);export const createContext=(d)=>React.createContext(d);export const useContext=(c)=>React.useContext(c);")}",
"solid-js/web":"data:text/javascript,${encodeURIComponent("export const render=()=>{};export const hydrate=()=>{};export const isServer=false;export const Portal=({children})=>children;export const Dynamic=({component:C,...p})=>React.createElement(C,p);")}",
"@solidjs/router":"data:text/javascript,${encodeURIComponent("import React from'react';export const A=({href,children,...r})=>React.createElement('a',{href,...r},children);export const useNavigate=()=>()=>{};export const useParams=()=>({});export const useSearchParams=()=>[{},()=>{}];export const useLocation=()=>({pathname:'/',search:'',hash:''});export const Router=({children})=>typeof children==='function'?children():children;export const Route=({component:C})=>C?React.createElement(C):null;export const Routes=({children})=>typeof children==='function'?children():children;export const Navigate=()=>null;")}"` : '';

    return `<!DOCTYPE html><html lang="en" class="dark"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1.0"><title>Preview</title>
<link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin><link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">
<script src="https://unpkg.com/@babel/standalone/babel.min.js"><\/script>
<script src="https://cdn.tailwindcss.com"><\/script>
<script type="importmap">{"imports":{
"react":"https://esm.sh/react@18?dev",
"react/":"https://esm.sh/react@18/",
"react/jsx-runtime":"https://esm.sh/react@18/jsx-runtime?dev",
"react/jsx-dev-runtime":"https://esm.sh/react@18/jsx-dev-runtime?dev",
"react-dom":"https://esm.sh/react-dom@18?dev",
"react-dom/":"https://esm.sh/react-dom@18/",
"react-dom/client":"https://esm.sh/react-dom@18/client?dev",
"framer-motion":"https://esm.sh/framer-motion@11?dev&external=react,react-dom",
"lucide-react":"https://esm.sh/lucide-react@0.400?dev&external=react",
"clsx":"https://esm.sh/clsx@2?dev",
"tailwind-merge":"https://esm.sh/tailwind-merge@2?dev",
"three":"https://esm.sh/three@0.160?dev",
"@react-three/fiber":"https://esm.sh/@react-three/fiber@8?dev&external=react,react-dom,three",
"@react-three/drei":"https://esm.sh/@react-three/drei@9?dev&external=react,react-dom,three",
"gsap":"https://esm.sh/gsap@3?dev",
"lenis":"https://esm.sh/lenis@1?dev",
"react-router-dom":"https://esm.sh/react-router-dom@6?dev&external=react,react-dom",
"react-icons/fa":"https://esm.sh/react-icons@5/fa?dev&external=react",
"react-icons/fi":"https://esm.sh/react-icons@5/fi?dev&external=react",
"react-icons/hi":"https://esm.sh/react-icons@5/hi?dev&external=react",
"react-icons/md":"https://esm.sh/react-icons@5/md?dev&external=react",
"react-icons/io":"https://esm.sh/react-icons@5/io?dev&external=react",
"react-icons/io5":"https://esm.sh/react-icons@5/io5?dev&external=react",
"react-icons/bi":"https://esm.sh/react-icons@5/bi?dev&external=react",
"react-icons/ai":"https://esm.sh/react-icons@5/ai?dev&external=react",
"react-icons/bs":"https://esm.sh/react-icons@5/bs?dev&external=react",
"react-icons/ri":"https://esm.sh/react-icons@5/ri?dev&external=react",
"@heroicons/react/24/outline":"https://esm.sh/@heroicons/react@2/24/outline?dev&external=react",
"@heroicons/react/24/solid":"https://esm.sh/@heroicons/react@2/24/solid?dev&external=react",
"@heroicons/react/20/solid":"https://esm.sh/@heroicons/react@2/20/solid?dev&external=react",
"recharts":"https://esm.sh/recharts@2?dev&external=react,react-dom",
"axios":"https://esm.sh/axios@1?dev",
"date-fns":"https://esm.sh/date-fns@3?dev",
"uuid":"https://esm.sh/uuid@9?dev"${nextShims ? ',' + nextShims : ''}${shimEntries ? ',' + shimEntries : ''}${solidShims ? ',' + solidShims : ''}
}}</script>
<script>${twConfig}<\/script>
<style>${baseStyle}</style>
<script id="__NEXA_DATA__" type="application/json">${JSON.stringify({ files: fileMap, entry: entryPath }).replace(/</g, '\\u003c')}</script>
</head><body><div id="root"></div>
<script type="module">
import React from'react';import{createRoot}from'react-dom/client';
const data=JSON.parse(document.getElementById('__NEXA_DATA__').textContent);const files=data.files;const entryPath=data.entry;
const runtimeProjectType=${JSON.stringify(projectType)};
const shimModules=${projectType === 'NEXTJS' ? "{'next/link':1,'next/image':1,'next/head':1,'next/script':1,'next/navigation':1,'next/router':1,'next/font/google':1}" : projectType === 'REMIX' ? "{'@remix-run/react':1,'@remix-run/node':1}" : projectType === 'SOLID' ? "{'solid-js':1,'solid-js/web':1,'@solidjs/router':1}" : '{}'};
const normalizePath=(p)=>{const c=p.replace(/\\\\\\\\/g,'/');return c.startsWith('/')?c:'/'+c.replace(/^\\.\\/?/,'')};
const resolveAlias=(s)=>{if(!s.startsWith('@/')&&!s.startsWith('~/'))return s;const rest=s.slice(2);const srcPath='/src/'+rest;const rootPath='/'+rest;if(Object.keys(files).some(f=>f===srcPath||f.startsWith(srcPath+'.')||f.startsWith(srcPath+'/')))return srcPath;return rootPath};
const resolvePath=(from,spec)=>{const r=resolveAlias(spec);if(!r.startsWith('.')&&!r.startsWith('/'))return r;const d=normalizePath(from).replace(/\\/[^\\/]*$/,'/');const j=r.startsWith('/')?r:d+r;const parts=normalizePath(j).split('/');const stack=[];for(const p of parts){if(!p||p==='.')continue;if(p==='..')stack.pop();else stack.push(p)}return'/'+stack.join('/')};
const esmUrl=(spec)=>'https://esm.sh/'+spec+'?dev&external=react,react-dom';
const moduleCache=new Map();
const compileCode=(code,filename)=>{try{return Babel.transform(code,{filename,presets:['typescript',['react',{runtime:'automatic'}]],sourceType:'module'}).code}catch(err){throw new Error('Babel error in '+filename+': '+err.message)}};
/* Find ALL files with Router wrapper tags and pick ONE as the host to prevent nesting */
const routerTagFiles=Object.entries(files).filter(([p,c])=>typeof c==='string'&&/<(BrowserRouter|HashRouter|MemoryRouter)\\b/.test(c));
const routerFilePaths=new Set(routerTagFiles.map(([p])=>p));
const pickRouterHost=()=>{const paths=[...routerFilePaths];const app=paths.find(p=>/\\/App\\.(tsx|ts|jsx|js)$/.test(p));if(app)return app;const entry=paths.find(p=>/\\/(main|index)\\.(tsx|ts|jsx|js)$/.test(p));if(entry)return entry;return paths[0]||''};
const routerHostFile=routerFilePaths.size>0?pickRouterHost():'';
const patchRouterSource=(src,filePath)=>{
let s=src;
const shouldStrip=routerFilePaths.size>1&&filePath!==routerHostFile;
if(shouldStrip){
/* Non-host files: strip Router tags completely to prevent nesting */
s=s.replace(/<BrowserRouter[^>]*>/g,'<>');
s=s.replace(/<\\/BrowserRouter>/g,'</>');
s=s.replace(/<HashRouter[^>]*>/g,'<>');
s=s.replace(/<\\/HashRouter>/g,'</>');
s=s.replace(/<MemoryRouter[^>]*>/g,'<>');
s=s.replace(/<\\/MemoryRouter>/g,'</>');
}else{
s=s.replace(/\\bBrowserRouter\\b/g,'MemoryRouter');
s=s.replace(/\\bHashRouter\\b/g,'MemoryRouter');
}
s=s.replace(/\\bcreateBrowserRouter\\b/g,'createMemoryRouter');
s=s.replace(/\\bcreateHashRouter\\b/g,'createMemoryRouter');
s=s.replace(new RegExp('<RouterProvider\\\\s+router=\\\\{[^}]+\\\\}\\\\s*\\\\/?>','g'),'');
if(!shouldStrip){
s=s.replace(/<MemoryRouter(?![^>]*initialEntries)([^>]*)>/g,"<MemoryRouter initialEntries={['/']}$1>");
}
return s;};
/* Check if any source file already contains a Router so we don't double-wrap */
const sourceHasRouter=()=>{for(const [p,c] of Object.entries(files)){if(typeof c==='string'){if(/\\b(BrowserRouter|HashRouter|MemoryRouter|RouterProvider|createBrowserRouter|createHashRouter|createMemoryRouter)\\b/.test(c))return true}}return false};
const externalModules=['react','react-dom','react-dom/client','framer-motion','lucide-react','clsx','tailwind-merge','three','@react-three/fiber','@react-three/drei','gsap','lenis','react-router-dom','react-icons','@heroicons/react','recharts','axios','date-fns','uuid','@remix-run/react','@remix-run/node','solid-js','solid-js/web','@solidjs/router'];
const rewriteImports=(code,fromPath)=>{return code
  .replace(/from\\s+['"]([^'"]+)['"]/g,(match,spec)=>{if(shimModules[spec])return match;if(externalModules.some(m=>spec===m||spec.startsWith(m+'/')))return match;if(!spec.startsWith('.')&&!spec.startsWith('/')&&!spec.startsWith('@/')&&!spec.startsWith('~/'))return match.replace(spec,esmUrl(spec));const resolved=resolvePath(fromPath,spec);const url=buildModule(resolved);return url?match.replace(spec,url):match})
  .replace(/import\\s+['"]([^'"]+)['"]/g,(match,spec)=>{if(shimModules[spec])return match;if(externalModules.some(m=>spec===m||spec.startsWith(m+'/')))return match;if(!spec.startsWith('.')&&!spec.startsWith('/')&&!spec.startsWith('@/')&&!spec.startsWith('~/'))return match.replace(spec,esmUrl(spec));const resolved=resolvePath(fromPath,spec);const url=buildModule(resolved);return url?match.replace(spec,url):match})
  .replace(/import\\(\\s*['"]([^'"]+)['"]\\s*\\)/g,(match,spec)=>{if(shimModules[spec])return match;if(externalModules.some(m=>spec===m||spec.startsWith(m+'/')))return match;if(!spec.startsWith('.')&&!spec.startsWith('/')&&!spec.startsWith('@/')&&!spec.startsWith('~/'))return match.replace(spec,esmUrl(spec));const resolved=resolvePath(fromPath,spec);const url=buildModule(resolved);return url?match.replace(spec,url):match})};
const resolveFile=(path)=>{if(files[path])return path;for(const ext of['.tsx','.ts','.jsx','.js','/index.tsx','/index.ts','/index.jsx','/index.js']){if(files[path+ext])return path+ext}return path};
const buildModule=(path)=>{const resolved=resolveFile(normalizePath(path));if(moduleCache.has(resolved))return moduleCache.get(resolved);moduleCache.set(resolved,'');
if(resolved.endsWith('.css')){const css=files[resolved]||'';const js="const s=document.createElement('style');s.textContent="+JSON.stringify(css)+";document.head.appendChild(s);export default {};";const url=URL.createObjectURL(new Blob([js],{type:'text/javascript'}));moduleCache.set(resolved,url);return url}
let source=files[resolved];if(!source){const stubJs="const Stub=()=>null;Stub.displayName='Stub('+"+JSON.stringify(resolved)+"+')';export default Stub;export const Button=Stub;export const Input=Stub;export const Label=Stub;export const Card=Stub;export const Badge=Stub;export const Dialog=Stub;export const cn=(...args)=>args.filter(Boolean).join(' ');";const stubUrl=URL.createObjectURL(new Blob([stubJs],{type:'text/javascript'}));moduleCache.set(resolved,stubUrl);return stubUrl}
/* Virtual utilities module for injecting missing common functions */
if(!window.__nexaUtilsUrl){const utilsJs="export const cn=(...a)=>a.filter(Boolean).join(' ');export const clsx=(...a)=>a.flat(Infinity).filter(v=>typeof v==='string'&&v).join(' ');export const twMerge=(s)=>s;";window.__nexaUtilsUrl=URL.createObjectURL(new Blob([utilsJs],{type:'text/javascript'}))}
if(['REACT','NEXTJS','REMIX'].includes(runtimeProjectType)){source=patchRouterSource(source,resolved)}
/* Check original source for missing utility imports */
const needsCn=/\\bcn\\(/.test(source)&&!/import\\s*\\{[^}]*\\bcn\\b/.test(source)&&!/export\\s+(function|const|let|var)\\s+cn\\b/.test(source)&&!/(const|let|var)\\s+cn\\s*=/.test(source)&&!/function\\s+cn\\s*\\(/.test(source);
const needsClsx=/\\bclsx\\(/.test(source)&&!/import[^;]*\\bclsx\\b/.test(source)&&!/(const|let|var)\\s+clsx\\s*=/.test(source);
let compiled;try{compiled=compileCode(source,resolved)}catch(e){const fallbackStub="export default ()=>null;";const url=URL.createObjectURL(new Blob([fallbackStub],{type:'text/javascript'}));moduleCache.set(resolved,url);return url}
/* Inject imports for missing utilities from virtual module */
const injects=[];if(needsCn)injects.push('cn');if(needsClsx)injects.push('clsx');
if(injects.length>0){compiled='import {'+injects.join(',')+'}from"'+window.__nexaUtilsUrl+'";\\n'+compiled}
/* Auto-inject missing named imports from popular packages */
const _has=(s)=>new RegExp('\\\\b'+s+'\\\\b').test(compiled);
const _imp=(s)=>new RegExp('import[^;]*\\\\b'+s+'\\\\b').test(compiled)||new RegExp('(const|let|var|function|class)\\\\s+'+s+'\\\\b').test(compiled)||new RegExp('export\\\\s+(default\\\\s+)?(function|class|const)\\\\s+'+s+'\\\\b').test(compiled);
const _ai=(syms,pkg)=>{const miss=syms.filter(s=>_has(s)&&!_imp(s));if(miss.length)compiled='import {'+miss.join(',')+'}from"'+pkg+'";\\n'+compiled};
_ai(['motion','AnimatePresence','useAnimation','useInView','useScroll','useTransform','useSpring','useMotionValue','LayoutGroup','Reorder','LazyMotion','domAnimation','domMax'],'framer-motion');
_ai(['useState','useEffect','useRef','useMemo','useCallback','useContext','createContext','useReducer','forwardRef','memo','Fragment','lazy','startTransition','useTransition','useDeferredValue','useId','useImperativeHandle','useLayoutEffect','Suspense','Children','cloneElement','isValidElement'],'react');
_ai(['Link','NavLink','useNavigate','useParams','useLocation','useSearchParams','Outlet','Navigate','Routes','Route','useMatch','useResolvedPath'],'react-router-dom');
if(/from\\s*['"]lucide-react['"]/.test(compiled)){_ai(['Search','Menu','X','ArrowRight','ArrowLeft','ArrowUp','ArrowDown','ChevronDown','ChevronRight','ChevronLeft','ChevronUp','Check','Plus','Minus','Star','Heart','Home','User','Settings','Bell','Mail','Phone','MapPin','Calendar','Clock','Edit','Trash','Trash2','Eye','EyeOff','Download','Upload','Share','ExternalLink','Copy','Save','Filter','MoreHorizontal','MoreVertical','AlertCircle','AlertTriangle','Info','HelpCircle','LogIn','LogOut','UserPlus','Users','Briefcase','Building','FileText','FolderOpen','Sun','Moon','Zap','Activity','BarChart','TrendingUp','DollarSign','CreditCard','ShoppingCart','Package','Globe','Monitor','Smartphone','Lock','Unlock','Key','Award','BookOpen','Bookmark','Tag','List','Grid','Layers','Layout','Sidebar','Maximize','Minimize','RefreshCw','Loader','Loader2','XCircle','CheckCircle','CircleUser','Send','MessageSquare','Inbox','Archive','Hash','AtSign','Link2','Paperclip','Image','Camera','Github','Linkedin','PenSquare','Pencil','PanelLeft','PanelRight','GripVertical','Shield','ShieldCheck','Sparkles','Wand2','Bot','Brain','Cpu','Database','Server','Terminal','Code','Code2','FileCode','FilePlus','FolderPlus','GitBranch','Rocket','Target','Trophy','Wallet','Receipt','LayoutDashboard','CircleDollarSign','BadgeCheck','Timer','TimerOff','Gauge','PieChart','LineChart','BarChart2','BarChart3','Table','Columns','Rows','SlidersHorizontal','Cog','Wrench','HardDrive','Wifi','Cloud','CloudUpload','CloudDownload','Folder','File','Files','ClipboardList','ClipboardCheck','ListChecked','SquareCheck','Circle','Square','Triangle','Hexagon','Octagon','Power','PlayCircle','PauseCircle','StopCircle','SkipForward','SkipBack','Volume2','VolumeX','Headphones','Radio','Tv','Projector','Gamepad2','Dice1','Dice2','Dice3','Car','Bike','Bus','Plane','Ship','Train','MapPinned','Navigation','Compass','Flag','Milestone','Signpost','BadgePercent','Banknote','Coins','PiggyBank','TrendingDown','ArrowUpRight','ArrowDownRight','MoveUp','MoveDown','MoveLeft','MoveRight','Expand','Shrink','ZoomIn','ZoomOut','RotateCcw','RotateCw','Repeat','Shuffle','Type','Bold','Italic','Underline','Strikethrough','AlignLeft','AlignCenter','AlignRight','AlignJustice','Heading','Heading1','Heading2','Heading3','Quote','MessageCircle','MessagesSquare','Reply','Forward','Share2','ThumbsUp','ThumbsDown','Smile','Frown','Meh','Angry','PartyPopper','Cake'],'lucide-react')}
if(/\\bReact\\./.test(compiled)&&!/import\\s+React[\\s,{]/.test(compiled)&&!/import\\s*\\*\\s*as\\s+React/.test(compiled)){compiled='import React from"react";\\n'+compiled}
const rewritten=rewriteImports(compiled,resolved);const url=URL.createObjectURL(new Blob([rewritten],{type:'text/javascript'}));moduleCache.set(resolved,url);return url};
class ErrorBoundary extends React.Component{constructor(props){super(props);this.state={hasError:false,error:null}}static getDerivedStateFromError(error){return{hasError:true,error}}render(){if(this.state.hasError){return React.createElement('div',{style:{padding:'2rem',textAlign:'center',background:'#fee2e2',minHeight:'100vh',display:'flex',alignItems:'center',justifyContent:'center'}},React.createElement('div',{style:{background:'white',padding:'2rem',borderRadius:'12px',maxWidth:'600px'}},React.createElement('h3',{style:{color:'#dc2626',margin:'0 0 1rem'}},'Preview Error'),React.createElement('pre',{style:{background:'#fef2f2',padding:'1rem',borderRadius:'8px',textAlign:'left',overflow:'auto',fontSize:'12px'}},this.state.error?.message||'Unknown error')))}return this.props.children}}
try{
const url=buildModule(entryPath);if(!url)throw new Error('Cannot load: '+entryPath);const mod=await import(url);const rootEl=document.getElementById('root');const App=mod.default||mod.App||mod.Page||Object.values(mod).find(v=>typeof v==='function');
const hasRouter=sourceHasRouter();
const isSelfMountEntry=/createRoot|ReactDOM\\.render/.test(files[entryPath]||'');
/* Poll for self-mount completion (entry files like main.tsx call createRoot().render()) */
let selfMounted=false;
if(isSelfMountEntry){for(let i=0;i<30;i++){await new Promise(r=>setTimeout(r,100));if(rootEl&&(rootEl.childElementCount>0||(rootEl.textContent||'').trim())){selfMounted=true;break}}}
/* Skip createRoot if entry already mounted to avoid React warning */
let root;
if(!selfMounted){try{root=createRoot(rootEl)}catch(_){root=null}}
const wrapWithRouter=(el)=>{try{const rrd=window.__rrd||(window.__rrd=null);return import('react-router-dom').then(rrd=>{window.__rrd=rrd;return React.createElement(rrd.MemoryRouter,{initialEntries:['/']},el)}).catch(()=>el)}catch(e){return Promise.resolve(el)}};
if(!App){
  if(selfMounted){/* Entry self-mounted successfully */}
  else if(root){
    const appPath=resolveFile('/src/App');

    if(files[appPath]){
      const appUrl=buildModule(appPath);const appMod=await import(appUrl);
      const FallbackApp=appMod.default||appMod.App||Object.values(appMod).find(v=>typeof v==='function');
      if(FallbackApp){
        let fallbackEl=React.createElement(FallbackApp);
        if(['REACT','NEXTJS','REMIX'].includes(runtimeProjectType)&&!hasRouter){fallbackEl=await wrapWithRouter(fallbackEl)}
        root.render(React.createElement(ErrorBoundary,null,fallbackEl));
      }else{throw new Error('No component found in '+entryPath)}
    }else{throw new Error('No component found in '+entryPath)}
  }
}else if(root){
  let appEl=React.createElement(App);
  if(['REACT','NEXTJS','REMIX'].includes(runtimeProjectType)&&!hasRouter){appEl=await wrapWithRouter(appEl)}
  root.render(React.createElement(ErrorBoundary,null,appEl))
}

await new Promise(r=>setTimeout(r,80));
if(root&&rootEl&&rootEl.childElementCount===0&&!(rootEl.textContent||'').trim()){
  const appPath=resolveFile('/src/App');
  if(files[appPath]){
    try{
      const appUrl=buildModule(appPath);const appMod=await import(appUrl);
      const FallbackApp=appMod.default||appMod.App||Object.values(appMod).find(v=>typeof v==='function');
      if(FallbackApp){
        let fallbackEl=React.createElement(FallbackApp);
        if(['REACT','NEXTJS','REMIX'].includes(runtimeProjectType)&&!hasRouter){fallbackEl=await wrapWithRouter(fallbackEl)}
        root.render(React.createElement(ErrorBoundary,null,fallbackEl));
      }
    }catch(_){/* ignore and show diagnostic below */}
  }
}

await new Promise(r=>setTimeout(r,80));
if(rootEl&&rootEl.childElementCount===0&&!(rootEl.textContent||'').trim()){
  rootEl.innerHTML='<div style="padding:2rem;text-align:center;color:#e2e8f0"><h3 style="margin:0 0 0.75rem;color:#f8fafc">Preview rendered no visible route</h3><p style="margin:0;color:#94a3b8">Try adding a <code>/</code> or <code>/dashboard</code> route in the generated app.</p></div>';
}
}catch(e){console.error('Preview error:',e);document.getElementById('root').innerHTML='<div style="padding:2rem;text-align:center"><h3 style="color:#dc2626">Preview Error</h3><pre style="text-align:left;overflow:auto;font-size:12px">'+e.message+'<\\/pre></div>'}
<\/script></body></html>`;
  }, [files, projectType]);

  // Post-process: inject link containment so preview links don't escape the iframe
  const containedPreviewHtml = useMemo(() => {
    if (!previewHtml) return previewHtml;
    // Prevent all navigation that could break out of the iframe:
    // 1. <a> tag clicks (absolute paths, external URLs)
    // 2. window.location assignments
    // 3. form submissions
    const linkScript = `<script>
document.addEventListener('click',function(e){var a=e.target.closest('a');if(!a||!a.href)return;var h=a.getAttribute('href')||'';if(h.startsWith('#')){var id=h.slice(1);var el=id?document.getElementById(id)||document.querySelector('[name="'+id+'"]'):null;if(el){e.preventDefault();el.scrollIntoView({behavior:'smooth',block:'start'})}return}if(h.startsWith('http')||h.startsWith('//')){e.preventDefault();return}if(h.startsWith('/')||h.startsWith('?')){e.preventDefault();return}});
document.addEventListener('submit',function(e){e.preventDefault()},true);
try{var _loc=window.location;Object.defineProperty(window,'location',{get:function(){return _loc},set:function(v){console.warn('Preview blocked location change:',v)}})}catch(x){}
window.addEventListener('error',function(e){if(!document.getElementById('__nexa_err')){var d=document.createElement('div');d.id='__nexa_err';d.style.cssText='position:fixed;bottom:12px;left:12px;right:12px;max-height:120px;overflow:auto;background:rgba(220,38,38,0.12);border:1px solid rgba(220,38,38,0.3);color:#fca5a5;padding:10px 14px;border-radius:10px;font-size:12px;font-family:monospace;backdrop-filter:blur(8px);z-index:99999;white-space:pre-wrap;word-break:break-word';document.body.appendChild(d)}document.getElementById('__nexa_err').textContent=(e.message||'Unknown error')+(e.filename?' in '+e.filename.split('/').pop():'')});
window.addEventListener('unhandledrejection',function(e){if(!document.getElementById('__nexa_err')){var d=document.createElement('div');d.id='__nexa_err';d.style.cssText='position:fixed;bottom:12px;left:12px;right:12px;max-height:120px;overflow:auto;background:rgba(220,38,38,0.12);border:1px solid rgba(220,38,38,0.3);color:#fca5a5;padding:10px 14px;border-radius:10px;font-size:12px;font-family:monospace;backdrop-filter:blur(8px);z-index:99999;white-space:pre-wrap;word-break:break-word';document.body.appendChild(d)}var msg=e.reason?e.reason.message||String(e.reason):'Promise rejected';document.getElementById('__nexa_err').textContent=msg});
<\/script>`;
    return previewHtml.replace(/<\/head>/i, linkScript + '</head>');
  }, [previewHtml]);

  /* ═══════════════════════════════════════════════════════════════════
     Render
     ═══════════════════════════════════════════════════════════════════ */

  if (isLoading) {
    return (
      <div className="h-full w-full flex items-center justify-center bg-[#09080e]">
        <motion.div initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} className="text-center">
          <div className="w-12 h-12 rounded-xl bg-lilac-200/5 border border-lilac-200/10 flex items-center justify-center mx-auto mb-4">
            <Loader2 className="w-5 h-5 animate-spin text-mauve-400" />
          </div>
          <p className="text-sm text-lilac-300/50 font-medium">Loading project...</p>
        </motion.div>
      </div>
    );
  }

  if (loadError) {
    return (
      <div className="h-full w-full flex items-center justify-center bg-[#09080e] p-6">
        <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="max-w-md w-full rounded-2xl border border-red-500/20 bg-red-500/5 p-6 text-center">
          <div className="w-12 h-12 rounded-xl bg-red-500/10 border border-red-500/20 flex items-center justify-center mx-auto mb-4">
            <AlertTriangle className="w-5 h-5 text-red-300" />
          </div>
          <h2 className="text-lg font-semibold text-lilac-100 mb-2">{loadError.title}</h2>
          <p className="text-sm text-lilac-300/70 mb-6">{loadError.message}</p>
          <div className="flex items-center justify-center gap-2">
            <button
              onClick={() => setReloadTick((v) => v + 1)}
              className="px-4 py-2 rounded-lg bg-mauve-500/20 text-mauve-200 hover:bg-mauve-500/30 transition-colors text-sm"
            >
              Retry
            </button>
            <button
              onClick={() => navigate('/projects')}
              className="px-4 py-2 rounded-lg border border-lilac-200/15 text-lilac-200/70 hover:bg-lilac-200/5 transition-colors text-sm"
            >
              Back to Projects
            </button>
          </div>
        </motion.div>
      </div>
    );
  }

  return (
    <div className="h-full w-full flex max-md:flex-col overflow-hidden bg-[#09080e]">

      {/* ═══ Left Panel: Chat ═══ */}
      <AnimatePresence>
        {showChat && (
        <motion.div
          ref={chatPanelRef}
          initial={{ width: 0, opacity: 0 }}
          animate={{ width: chatWidth, opacity: 1 }}
          exit={{ width: 0, opacity: 0 }}
          transition={{ duration: isResizingState ? 0 : 0.2, ease: [0.25, 1, 0.5, 1] }}
          className="max-md:!w-full max-md:h-[45dvh] flex flex-col border-r max-md:border-r-0 max-md:border-b border-lilac-200/8 shrink-0 overflow-hidden bg-[#0b0a10] relative"
        >
          {/* Resize handle */}
          <div
            onMouseDown={handleResizeStart}
            className="absolute right-0 top-0 bottom-0 w-1 cursor-col-resize z-20 group hover:bg-mauve-500/30 transition-colors max-md:hidden"
          >
            <div className="absolute right-0 top-0 bottom-0 w-1 bg-mauve-500/0 group-hover:bg-mauve-500/40 transition-colors" />
          </div>
          {/* Header */}
          <div className="h-12 px-4 flex items-center justify-between border-b border-lilac-200/8 shrink-0 bg-[#0b0a10]">
            <div className="flex items-center gap-2.5">
              <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-mauve-500/20 to-purple-500/15 flex items-center justify-center border border-mauve-500/12">
                <Bot className="w-3.5 h-3.5 text-mauve-300" />
              </div>
              <div>
                <h1 className="text-[13px] font-semibold text-lilac-100">NexaStudio AI</h1>
                <p className="text-[10px] text-lilac-400/40">{currentProject?.name || 'Project'}</p>
              </div>
            </div>
            <button onClick={() => setShowChat(false)} className="p-1.5 rounded-md hover:bg-lilac-200/8 text-lilac-400/40 hover:text-lilac-300 transition-colors" aria-label="Hide chat panel">
              <PanelLeftClose className="w-4 h-4" />
            </button>
          </div>

          {/* Messages */}
          <div ref={chatContainerRef} className="flex-1 overflow-y-auto px-4 py-4 space-y-5 scrollbar-thin">
            {messages.map((message) => (
              <ChatMessageBubble
                key={message.id}
                message={message}
                onViewFile={handleViewFile}
                thinkingStep={message.status === 'thinking' || message.status === 'generating' ? thinkingStep : undefined}
                thinkingSteps={THINKING_STEPS}
              />
            ))}
          </div>

          {/* Input */}
          <div className="p-3 border-t border-lilac-200/8 shrink-0 bg-[#0b0a10]">
            <div className="rounded-xl border border-lilac-200/10 bg-lilac-200/[0.03] focus-within:border-mauve-500/30 focus-within:ring-1 focus-within:ring-mauve-500/10 transition-all">
              <textarea
                ref={textareaRef}
                value={prompt}
                onChange={(e) => setPrompt(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="Describe what to build or change..."
                disabled={isGenerating}
                rows={1}
                className="w-full px-3.5 pt-2.5 pb-1 bg-transparent text-[13px] text-lilac-100 placeholder:text-lilac-400/30 focus:outline-none resize-none disabled:opacity-40"
                style={{ minHeight: '36px', maxHeight: '120px' }}
              />
              <div className="flex items-center justify-between px-2 pb-2">
                <p className="text-[10px] text-lilac-500/20 pl-1.5">Enter to send · Shift+Enter for new line</p>
                <div className="flex items-center gap-1">
                  {isGenerating && (
                    <motion.button initial={{ scale: 0 }} animate={{ scale: 1 }} onClick={handleStopGeneration}
                      className="p-1.5 rounded-lg bg-red-500/10 text-red-400 hover:bg-red-500/20 transition-all border border-red-500/15">
                      <Square className="w-3.5 h-3.5" />
                    </motion.button>
                  )}
                  <button onClick={handleGenerate} disabled={!prompt.trim() || isGenerating}
                    className={cn('p-2 rounded-lg transition-all', prompt.trim() && !isGenerating ? 'bg-mauve-500 text-white hover:bg-mauve-600 active:scale-95' : 'bg-lilac-200/5 text-lilac-400/25')}>
                    {isGenerating ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <Send className="w-3.5 h-3.5" />}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </motion.div>
        )}
      </AnimatePresence>

      {/* ═══ Right Panel: Preview / Code ═══ */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Toolbar */}
        <div className="h-12 px-3 flex items-center justify-between border-b border-lilac-200/8 shrink-0 bg-[#0b0a10]/90 backdrop-blur-sm">
          <div className="flex items-center gap-2">
            {!showChat && (
              <>
                <button onClick={() => navigate('/projects')} className="p-1.5 rounded-md text-lilac-400/50 hover:text-lilac-300 hover:bg-lilac-200/8 transition-colors"><ArrowLeft className="w-4 h-4" /></button>
                <button onClick={() => setShowChat(true)} className="p-1.5 rounded-md text-lilac-400/50 hover:text-lilac-300 hover:bg-lilac-200/8 transition-colors" title="Show chat" aria-label="Show chat panel"><PanelLeftOpen className="w-4 h-4" /></button>
                <div className="w-px h-5 bg-lilac-200/8 mx-1" />
              </>
            )}
            <div className="flex items-center gap-0.5 p-0.5 rounded-lg bg-lilac-200/5 border border-lilac-200/8">
              <button onClick={() => setViewMode('preview')} className={cn('flex items-center gap-1.5 px-2.5 py-1 rounded-md text-[11px] font-medium transition-all', viewMode === 'preview' ? 'bg-mauve-500/15 text-mauve-300 shadow-sm' : 'text-lilac-400/50 hover:text-lilac-300')}>
                <Eye className="w-3.5 h-3.5" />Preview
              </button>
              <button onClick={() => setViewMode('code')} className={cn('flex items-center gap-1.5 px-2.5 py-1 rounded-md text-[11px] font-medium transition-all', viewMode === 'code' ? 'bg-mauve-500/15 text-mauve-300 shadow-sm' : 'text-lilac-400/50 hover:text-lilac-300')}>
                <Code2 className="w-3.5 h-3.5" />Code
              </button>
            </div>
          </div>

          {viewMode === 'preview' && !isWritingFiles && (
            <div className="flex items-center gap-0.5">
              {([{ id: 'desktop' as DeviceView, icon: Monitor }, { id: 'tablet' as DeviceView, icon: Tablet }, { id: 'mobile' as DeviceView, icon: Smartphone }]).map(({ id: d, icon: Icon }) => (
                <button key={d} onClick={() => setDeviceView(d)} aria-label={`${d} view`} className={cn('p-1.5 rounded-md transition-colors', deviceView === d ? 'bg-mauve-500/15 text-mauve-300' : 'text-lilac-400/35 hover:text-lilac-300 hover:bg-lilac-200/8')}>
                  <Icon className="w-3.5 h-3.5" />
                </button>
              ))}
              <div className="w-px h-4 bg-lilac-200/8 mx-1" />
              <button onClick={() => setPreviewKey(k => k + 1)} className="p-1.5 rounded-md text-lilac-400/35 hover:text-lilac-300 hover:bg-lilac-200/8 transition-colors" title="Refresh" aria-label="Refresh preview"><RefreshCw className="w-3.5 h-3.5" /></button>
            </div>

          )}
          {isWritingFiles && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              className="flex items-center gap-2"
            >
              <motion.div
                animate={{ rotate: 360 }}
                transition={{ duration: 1, repeat: Infinity, ease: 'linear' }}
              >
                <Loader2 className="w-3.5 h-3.5 text-emerald-400" />
              </motion.div>
              <span className="text-[11px] text-emerald-400 font-medium">
                Writing file {writingFileIndex + 1}/{writingFiles.length}
              </span>
              <div className="w-24 h-1.5 rounded-full bg-lilac-200/8 overflow-hidden">
                <motion.div
                  className="h-full rounded-full bg-emerald-500"
                  animate={{ width: `${((writingFileIndex + (writingCharIndex / (writingFiles[writingFileIndex]?.content.length || 1))) / writingFiles.length) * 100}%` }}
                  transition={{ duration: 0.1 }}
                />
              </div>
            </motion.div>
          )}

          <div className="flex items-center gap-1">
            <button onClick={() => setShowFiles(!showFiles)} className={cn('flex items-center gap-1.5 px-2.5 py-1.5 rounded-md text-[11px] font-medium transition-colors', showFiles ? 'bg-mauve-500/15 text-mauve-300' : 'text-lilac-400/50 hover:text-lilac-300 hover:bg-lilac-200/8')}>
              <FolderOpen className="w-3.5 h-3.5" /><span className="hidden sm:inline">Files</span>
              {files.length > 0 && <span className="text-[9px] px-1 py-0.5 rounded bg-lilac-200/8 tabular-nums">{files.length}</span>}
            </button>
            <button onClick={async () => { if (id) { try { const r = await projectsApi.exportZip(id); const url = URL.createObjectURL(new Blob([r.data])); const a = document.createElement('a'); a.href = url; a.download = `${currentProject?.name || 'project'}.zip`; a.click(); URL.revokeObjectURL(url); } catch { /* silent */ } } }}
              className="flex items-center gap-1.5 px-2.5 py-1.5 rounded-md text-[11px] font-medium text-lilac-400/50 hover:text-lilac-300 hover:bg-lilac-200/8 transition-colors">
              <Download className="w-3.5 h-3.5" /><span className="hidden sm:inline">Export</span>
            </button>
          </div>
        </div>

        {/* Content Area */}
        <div className="flex-1 flex overflow-hidden">
          {/* Files panel */}
          <AnimatePresence>
            {showFiles && (
              <motion.div initial={{ width: 0, opacity: 0 }} animate={{ width: 220, opacity: 1 }} exit={{ width: 0, opacity: 0 }} transition={{ duration: 0.2, ease: [0.25, 1, 0.5, 1] }}
                className="border-r border-lilac-200/8 flex flex-col shrink-0 overflow-hidden bg-[#0a090f]">
                <div className="px-3 py-2 flex items-center justify-between border-b border-lilac-200/8">
                  <span className="text-[11px] font-semibold text-lilac-300/50 uppercase tracking-wider">Explorer</span>
                  <button onClick={() => setShowFiles(false)} className="p-1 rounded hover:bg-lilac-200/8 text-lilac-400/30 hover:text-lilac-300 transition-colors"><X className="w-3 h-3" /></button>
                </div>
                <div className="flex-1 overflow-y-auto py-1 scrollbar-thin">
                  {fileTree.length > 0 ? fileTree.map((node) => (
                    <TreeItem key={node.path} node={node} selectedPath={selectedFile} onSelect={(path) => { setSelectedFile(path); setViewMode('code'); }} expandedFolders={expandedFolders} onToggleFolder={toggleFolder} />
                  )) : (
                    <div className="px-4 py-10 text-center">
                      <FolderOpen className="w-6 h-6 text-lilac-500/15 mx-auto mb-2" />
                      <p className="text-[11px] text-lilac-400/30">No files yet</p>
                    </div>
                  )}
                </div>
              </motion.div>
            )}
          </AnimatePresence>

          {/* Main content */}
          <div className="flex-1 overflow-hidden">
            <AnimatePresence mode="wait">
              {viewMode === 'preview' ? (
                <motion.div key="preview" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} className="h-full">
                  <DeviceFrame device={deviceView} url="localhost:3000">
                    <iframe key={previewKey} srcDoc={containedPreviewHtml} className="w-full h-full border-0 bg-[#09090b]" sandbox="allow-scripts allow-same-origin" title="Preview" />
                  </DeviceFrame>
                </motion.div>
              ) : (
                <motion.div key="code" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} className="h-full overflow-auto bg-[#09080e]">
                  {activeFile ? (
                    <div className="font-mono text-sm">
                      <div className="sticky top-0 z-10 flex items-center justify-between px-4 py-2 border-b border-lilac-200/8 bg-[#0b0a10]/95 backdrop-blur-sm">
                        <div className="flex items-center gap-2">
                          {getFileIcon(activeFile.language)}
                          <span className="text-[11px] text-lilac-300/70 font-mono">{activeFile.path}</span>
                          {isWritingFiles && writingFiles[writingFileIndex]?.path === activeFile.path && (
                            <motion.span
                              animate={{ opacity: [1, 0.3, 1] }}
                              transition={{ duration: 0.8, repeat: Infinity }}
                              className="text-[10px] text-emerald-400 font-medium ml-2"
                            >
                              writing...
                            </motion.span>
                          )}
                        </div>
                        <button onClick={handleCopy} className="p-1.5 rounded-md hover:bg-lilac-200/8 text-lilac-400/50 hover:text-lilac-300 transition-colors">
                          {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                        </button>
                      </div>
                      <div className="p-4">
                        {displayedContent.split('\n').map((line, i) => (
                          <div key={i} className="flex hover:bg-lilac-200/[0.02] group">
                            <span className="w-10 text-right pr-3 text-lilac-500/20 select-none text-[11px] shrink-0 tabular-nums">{i + 1}</span>
                            <pre className="text-lilac-200/80 text-[12px] whitespace-pre overflow-x-auto"><code>{line || ' '}</code></pre>
                          </div>
                        ))}
                        {isWritingFiles && writingFiles[writingFileIndex]?.path === activeFile.path && (
                          <motion.span
                            animate={{ opacity: [1, 0] }}
                            transition={{ duration: 0.5, repeat: Infinity }}
                            className="inline-block w-2 h-4 bg-mauve-400 ml-0.5 -mb-0.5"
                          />
                        )}
                      </div>
                    </div>
                  ) : (
                    <div className="h-full flex items-center justify-center text-lilac-400/25">
                      <div className="text-center"><FileCode2 className="w-8 h-8 mx-auto mb-2" /><p className="text-[13px]">Select a file to view</p></div>
                    </div>
                  )}
                </motion.div>
              )}
            </AnimatePresence>
          </div>
        </div>
      </div>
    </div>
  );
}
