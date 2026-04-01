import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
  ArrowLeft,
  Sparkles,
  FileCode,
  FolderOpen,
  Trash2,
  Download,
  RefreshCw,
  ChevronRight,
  Clock,
  Layers,
} from 'lucide-react';
import { Button, GlassCard, Badge } from '../../components/ui';
import { projectsApi } from '../../lib/api';
import { useProjectStore } from '../../lib/store';
import { formatDate, formatRelativeTime, getFrameworkLabel, cn } from '../../lib/utils';
import { FrameworkIcon } from '../../components/ui';

/* ── helpers ──────────────────────────────────────────────── */
interface FileNode { name: string; path: string; type: 'file' | 'folder'; children?: FileNode[] }

function buildTree(paths: string[]): FileNode[] {
  const root: FileNode[] = [];
  for (const p of paths) {
    const parts = p.split('/');
    let current = root;
    let accum = '';
    parts.forEach((seg, i) => {
      accum = accum ? `${accum}/${seg}` : seg;
      const isFile = i === parts.length - 1;
      let node = current.find((n) => n.name === seg);
      if (!node) {
        node = { name: seg, path: accum, type: isFile ? 'file' : 'folder', children: isFile ? undefined : [] };
        current.push(node);
      }
      if (node.children) current = node.children;
    });
  }
  return root;
}

function TreeNode({ node, depth = 0, selected, onSelect }: {
  node: FileNode; depth?: number; selected: string; onSelect: (p: string) => void;
}) {
  const [open, setOpen] = useState(depth < 2);
  const isDir = node.type === 'folder';
  const active = selected === node.path;

  return (
    <div>
      <button
        onClick={() => { isDir ? setOpen(!open) : onSelect(node.path); }}
        className={cn(
          'w-full flex items-center gap-2 py-1.5 px-2 rounded-lg text-left transition-colors text-sm',
          active ? 'bg-mauve-600/15 text-mauve-300' : 'text-lilac-300/55 hover:bg-lilac-200/5 hover:text-lilac-200',
        )}
        style={{ paddingLeft: `${8 + depth * 14}px` }}
      >
        {isDir ? (
          <ChevronRight className={cn('w-3 h-3 transition-transform flex-shrink-0', open && 'rotate-90')} />
        ) : (
          <FileCode className="w-3.5 h-3.5 flex-shrink-0" />
        )}
        <span className="truncate">{node.name}</span>
      </button>
      {isDir && open && node.children?.map((c) => (
        <TreeNode key={c.path} node={c} depth={depth + 1} selected={selected} onSelect={onSelect} />
      ))}
    </div>
  );
}

const stagger = { hidden: { opacity: 0 }, visible: { opacity: 1, transition: { staggerChildren: 0.06 } } };
const rise = { hidden: { opacity: 0, y: 16 }, visible: { opacity: 1, y: 0, transition: { type: 'spring' as const, stiffness: 300, damping: 30 } } };

/* ── main ─────────────────────────────────────────────────── */
export default function ProjectDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { currentProject, setCurrentProject, removeProject } = useProjectStore();
  const [isLoading, setIsLoading] = useState(true);
  const [files, setFiles] = useState<string[]>([]);
  const [selectedFile, setSelectedFile] = useState('');
  const [fileContent, setFileContent] = useState('');
  const [loadingFile, setLoadingFile] = useState(false);

  useEffect(() => {
    if (!id) return;
    const load = async () => {
      setIsLoading(true);
      try {
        const [pRes, fRes] = await Promise.all([
          projectsApi.getById(id),
          projectsApi.getFiles(id).catch(() => ({ data: [] })),
        ]);
        setCurrentProject(pRes.data);
        // Backend returns FileDTO[] with { path, name, ... } - extract paths
        const rawFiles = fRes.data || [];
        const filePaths = Array.isArray(rawFiles)
          ? rawFiles.map((f: { path?: string } | string) => (typeof f === 'string' ? f : f?.path || '')).filter(Boolean)
          : [];
        setFiles(filePaths);
        if (filePaths.length > 0) setSelectedFile(filePaths[0]);
      } catch (err) {
        console.error('Failed to load project:', err);
        navigate('/projects');
      } finally {
        setIsLoading(false);
      }
    };
    load();
  }, [id, setCurrentProject, navigate]);

  // load file content when selection changes
  useEffect(() => {
    if (!id || !selectedFile) return;
    const loadFile = async () => {
      setLoadingFile(true);
      try {
        const res = await projectsApi.getFile(id, selectedFile);
        // Backend returns FileDTO with { content, path, ... }
        const data = res.data;
        const content = typeof data === 'string'
          ? data
          : (data?.content || JSON.stringify(data, null, 2));
        setFileContent(content);
      } catch (err) {
        console.error('Failed to load file:', err);
        setFileContent('// Unable to load file');
      } finally {
        setLoadingFile(false);
      }
    };
    loadFile();
  }, [id, selectedFile]);

  const handleDelete = async () => {
    if (!id || !confirm('Delete this project permanently?')) return;
    try { await projectsApi.delete(id); removeProject(id); navigate('/projects'); } catch { }
  };

  const tree = buildTree(files);
  const fw = currentProject?.type || currentProject?.framework || '';

  if (isLoading) {
    return (
      <div className="max-w-7xl mx-auto space-y-6">
        <div className="h-6 w-24 skeleton rounded-lg" />
        <div className="grid lg:grid-cols-4 gap-6">
          <div className="h-[500px] skeleton rounded-[22px]" />
          <div className="lg:col-span-3 h-[500px] skeleton rounded-[22px]" />
        </div>
      </div>
    );
  }

  if (!currentProject) {
    return (
      <div className="text-center py-20">
        <h2 className="text-xl text-lilac-100">Project not found</h2>
        <Link to="/projects"><Button variant="secondary" className="mt-4">Go to Projects</Button></Link>
      </div>
    );
  }

  return (
    <motion.div variants={stagger} initial="hidden" animate="visible" className="max-w-7xl mx-auto">
      {/* back */}
      <motion.button
        variants={rise}
        onClick={() => navigate('/projects')}
        className="flex items-center gap-2 text-sm text-lilac-400/50 hover:text-lilac-200 transition-colors mb-6"
      >
        <ArrowLeft className="w-4 h-4" /> Back to Projects
      </motion.button>

      {/* project header */}
      <motion.div variants={rise} className="mb-6">
        <GlassCard className="p-6">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div className="flex items-center gap-4">
              <FrameworkIcon framework={fw} size={40} />
              <div>
                <h1 className="text-2xl font-bold text-lilac-100">{currentProject.name}</h1>
                <p className="text-sm text-lilac-300/50">{currentProject.description || 'No description'}</p>
              </div>
            </div>
            <div className="flex items-center gap-2 flex-wrap">
              <Badge variant="mauve">{getFrameworkLabel(fw)}</Badge>
              <div className="flex items-center gap-1.5 text-xs text-lilac-500/40">
                <Clock className="w-3 h-3" />
                {formatRelativeTime(currentProject.updatedAt)}
              </div>
            </div>
          </div>
          <div className="flex flex-wrap gap-2 mt-5">
            <Link to={`/projects/${id}/generate`}>
              <Button variant="primary" leftIcon={<Sparkles className="w-4 h-4" />}>AI Generate</Button>
            </Link>
            <Button variant="secondary" leftIcon={<Download className="w-4 h-4" />}
              onClick={async () => { if (id) { try { const r = await projectsApi.exportZip(id); const url = URL.createObjectURL(new Blob([r.data])); const a = document.createElement('a'); a.href = url; a.download = `${currentProject.name}.zip`; a.click(); } catch { } } }}
            >
              Export
            </Button>
            <Button variant="ghost" leftIcon={<Trash2 className="w-4 h-4" />} className="text-red-400 hover:bg-red-500/10" onClick={handleDelete}>
              Delete
            </Button>
          </div>
        </GlassCard>
      </motion.div>

      {/* IDE-like layout */}
      <motion.div variants={rise} className="grid lg:grid-cols-4 gap-4">
        {/* file tree */}
        <GlassCard className="p-3 max-h-[600px] overflow-auto">
          <div className="flex items-center gap-2 px-2 pb-3 mb-2 border-b border-lilac-200/8">
            <FolderOpen className="w-4 h-4 text-mauve-400" />
            <span className="text-xs font-semibold text-lilac-300/70 uppercase tracking-wider">Files</span>
            <span className="ml-auto text-[11px] text-lilac-500/35">{files.length}</span>
          </div>
          {tree.length > 0 ? (
            tree.map((n) => <TreeNode key={n.path} node={n} selected={selectedFile} onSelect={setSelectedFile} />)
          ) : (
            <p className="text-xs text-lilac-400/35 text-center py-8">No files yet — use AI Generate to scaffold.</p>
          )}
        </GlassCard>

        {/* code viewer */}
        <GlassCard className="lg:col-span-3 overflow-hidden flex flex-col max-h-[600px]">
          {/* tab bar */}
          <div className="flex items-center gap-3 px-4 py-2.5 border-b border-lilac-200/8 bg-lilac-200/[0.02]">
            <div className="flex gap-1.5">
              <div className="w-2.5 h-2.5 rounded-full bg-red-500/60" />
              <div className="w-2.5 h-2.5 rounded-full bg-amber-500/60" />
              <div className="w-2.5 h-2.5 rounded-full bg-emerald-500/60" />
            </div>
            {selectedFile && (
              <span className="text-xs text-lilac-300/55 font-mono">{selectedFile}</span>
            )}
          </div>
          {/* content */}
          <div className="flex-1 overflow-auto p-5 font-mono text-sm leading-relaxed">
            {loadingFile ? (
              <div className="space-y-2">
                {Array.from({ length: 12 }).map((_, i) => (
                  <div key={i} className="h-4 skeleton rounded" style={{ width: `${40 + Math.random() * 50}%` }} />
                ))}
              </div>
            ) : fileContent ? (
              <pre className="text-lilac-200/85 whitespace-pre-wrap">{fileContent}</pre>
            ) : (
              <div className="flex flex-col items-center justify-center h-full text-lilac-400/30">
                <Layers className="w-10 h-10 mb-3" />
                <p className="text-sm">Select a file to view</p>
              </div>
            )}
          </div>
        </GlassCard>
      </motion.div>

      {/* project info row */}
      <motion.div variants={rise} className="grid sm:grid-cols-3 gap-4 mt-4">
        <GlassCard variant="subtle" className="p-4 flex items-center gap-3">
          <Layers className="w-5 h-5 text-mauve-400/60" />
          <div>
            <p className="text-xs text-lilac-400/45">Files</p>
            <p className="text-lg font-bold text-lilac-100">{files.length}</p>
          </div>
        </GlassCard>
        <GlassCard variant="subtle" className="p-4 flex items-center gap-3">
          <Clock className="w-5 h-5 text-mauve-400/60" />
          <div>
            <p className="text-xs text-lilac-400/45">Created</p>
            <p className="text-sm font-medium text-lilac-100">{formatDate(currentProject.createdAt)}</p>
          </div>
        </GlassCard>
        <GlassCard variant="subtle" className="p-4 flex items-center gap-3">
          <RefreshCw className="w-5 h-5 text-mauve-400/60" />
          <div>
            <p className="text-xs text-lilac-400/45">Last Updated</p>
            <p className="text-sm font-medium text-lilac-100">{formatDate(currentProject.updatedAt)}</p>
          </div>
        </GlassCard>
      </motion.div>
    </motion.div>
  );
}
