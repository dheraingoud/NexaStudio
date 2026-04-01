import { useEffect, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Link } from 'react-router-dom';
import {
  Plus,
  Search,
  Grid3X3,
  List,
  Clock,
  Trash2,
  ArrowUpRight,
} from 'lucide-react';
import { Button, GlassCard, Badge, Input } from '../../components/ui';
import { FrameworkIcon } from '../../components/ui/FrameworkIcon';
import { useProjectStore, type Project } from '../../lib/store';
import { projectsApi } from '../../lib/api';
import { formatRelativeTime, getFrameworkLabel, cn } from '../../lib/utils';

/* ── animations ──────────────────────────────────────────── */
const stagger = {
  hidden: { opacity: 0 },
  visible: { opacity: 1, transition: { staggerChildren: 0.04 } },
};

const rise = {
  hidden: { opacity: 0, y: 16 },
  visible: {
    opacity: 1,
    y: 0,
    transition: { type: 'spring' as const, stiffness: 300, damping: 30 },
  },
};

/* ── project card ────────────────────────────────────────── */
interface ProjectCardProps {
  project: Project;
  view: 'grid' | 'list';
  onDelete: (id: string) => void;
}

function ProjectCard({ project, view, onDelete }: ProjectCardProps) {
  const framework = project.type || project.framework || '';

  if (view === 'list') {
    return (
      <motion.div variants={rise}>
        <GlassCard hover className="p-4 flex items-center gap-4 group">
          <Link to={`/projects/${project.id}`} className="flex items-center gap-4 flex-1 min-w-0">
            <FrameworkIcon framework={framework} size={22} />
            <div className="flex-1 min-w-0">
              <p className="text-sm font-semibold text-lilac-100 truncate group-hover:text-mauve-300 transition-colors">
                {project.name}
              </p>
              <p className="text-xs text-lilac-400/45 truncate">
                {project.description || 'No description'}
              </p>
            </div>
            <Badge variant="mauve" size="sm">{getFrameworkLabel(framework)}</Badge>
            <div className="flex items-center gap-1.5 text-[11px] text-lilac-500/40">
              <Clock className="w-3 h-3" />
              {formatRelativeTime(project.updatedAt)}
            </div>
          </Link>
          <button
            onClick={() => onDelete(project.id)}
            className="p-2 rounded-xl hover:bg-red-500/10 text-lilac-500/30 hover:text-red-400 transition-colors"
            aria-label={`Delete ${project.name}`}
          >
            <Trash2 className="w-4 h-4" />
          </button>
        </GlassCard>
      </motion.div>
    );
  }

  return (
    <motion.div variants={rise} className="relative group">
      <Link to={`/projects/${project.id}`}>
        <GlassCard hover className="p-5 h-full flex flex-col">
          <div className="flex items-center justify-between mb-4">
            <FrameworkIcon framework={framework} size={28} />
            <Badge variant="mauve" size="sm">{getFrameworkLabel(framework)}</Badge>
          </div>
          <h3 className="font-semibold text-lilac-100 mb-1 truncate group-hover:text-mauve-300 transition-colors">
            {project.name}
          </h3>
          <p className="text-xs text-lilac-300/45 mb-4 line-clamp-2 flex-1">
            {project.description || 'No description'}
          </p>
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-1.5 text-[11px] text-lilac-500/40">
              <Clock className="w-3 h-3" />
              {formatRelativeTime(project.updatedAt)}
            </div>
            <ArrowUpRight className="w-4 h-4 text-lilac-500/25 group-hover:text-mauve-400 transition-colors" />
          </div>
        </GlassCard>
      </Link>
      {/* delete overlay */}
      <button
        onClick={(e) => {
          e.preventDefault();
          e.stopPropagation();
          onDelete(project.id);
        }}
        className={cn(
          'absolute top-3 right-3 p-1.5 rounded-xl z-10',
          'opacity-0 group-hover:opacity-100 transition-all',
          'hover:bg-red-500/15 text-lilac-500/30 hover:text-red-400'
        )}
        aria-label={`Delete ${project.name}`}
      >
        <Trash2 className="w-3.5 h-3.5" />
      </button>
    </motion.div>
  );
}

/* ── main ─────────────────────────────────────────────────── */
export default function ProjectsListPage() {
  const { projects, setProjects, removeProject, isLoading, setLoading } = useProjectStore();
  const [view, setView] = useState<'grid' | 'list'>('grid');
  const [search, setSearch] = useState('');
  const [filter, setFilter] = useState('all');

  useEffect(() => {
    const fetchProjects = async () => {
      setLoading(true);
      try {
        const res = await projectsApi.getAll();
        setProjects(res.data);
      } catch (error) {
        console.error('Failed to fetch projects:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchProjects();
  }, [setProjects, setLoading]);

  const handleDelete = async (id: string) => {
    if (!confirm('Delete this project? This cannot be undone.')) return;
    try {
      await projectsApi.delete(id);
      removeProject(id);
    } catch (error) {
      console.error('Failed to delete project:', error);
    }
  };

  const filteredProjects = projects.filter((project) => {
    const framework = (project.type || project.framework || '').toLowerCase();
    const query = search.toLowerCase();
    const matchesSearch = 
      project.name.toLowerCase().includes(query) || 
      (project.description || '').toLowerCase().includes(query);
    const matchesFilter = filter === 'all' || framework === filter.toLowerCase();
    return matchesSearch && matchesFilter;
  });

  const frameworkTypes = [
    'all',
    ...new Set(
      projects
        .map((p) => (p.type || p.framework || '').toUpperCase())
        .filter(Boolean)
    ),
  ];

  return (
    <motion.div 
      variants={stagger} 
      initial="hidden" 
      animate="visible" 
      className="max-w-7xl mx-auto"
    >
      {/* header */}
      <motion.div variants={rise} className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
        <div>
          <h1 className="text-2xl font-bold text-lilac-100 mb-0.5">Projects</h1>
          <p className="text-sm text-lilac-400/50">
            {projects.length} project{projects.length !== 1 ? 's' : ''}
          </p>
        </div>
        <Link to="/projects/new">
          <Button variant="primary" leftIcon={<Plus className="w-4 h-4" />}>
            New Project
          </Button>
        </Link>
      </motion.div>

      {/* toolbar */}
      <motion.div variants={rise} className="flex flex-col sm:flex-row gap-3 mb-6">
        <div className="flex-1 max-w-xs">
          <Input
            placeholder="Search…"
            leftIcon={<Search className="w-4 h-4" />}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <div className="flex items-center gap-2">
          {/* framework filter */}
          <div className="flex items-center gap-0.5 p-1 rounded-xl bg-lilac-200/[0.04] border border-lilac-200/[0.08]">
            {frameworkTypes.map((type) => (
              <button
                key={type}
                onClick={() => setFilter(type)}
                className={cn(
                  'px-3 py-1.5 text-xs rounded-lg transition-all font-medium',
                  filter.toUpperCase() === type.toUpperCase()
                    ? 'bg-mauve-600/25 text-mauve-300'
                    : 'text-lilac-400/50 hover:text-lilac-200'
                )}
              >
                {type === 'all' ? 'All' : getFrameworkLabel(type)}
              </button>
            ))}
          </div>
          {/* view toggle */}
          <div className="flex items-center p-1 rounded-xl bg-lilac-200/[0.04] border border-lilac-200/[0.08]">
            <button
              onClick={() => setView('grid')}
              className={cn(
                'p-2 rounded-lg transition-all',
                view === 'grid' ? 'bg-mauve-600/25 text-mauve-300' : 'text-lilac-400/40 hover:text-lilac-200'
              )}
              aria-label="Grid view"
            >
              <Grid3X3 className="w-4 h-4" />
            </button>
            <button
              onClick={() => setView('list')}
              className={cn(
                'p-2 rounded-lg transition-all',
                view === 'list' ? 'bg-mauve-600/25 text-mauve-300' : 'text-lilac-400/40 hover:text-lilac-200'
              )}
              aria-label="List view"
            >
              <List className="w-4 h-4" />
            </button>
          </div>
        </div>
      </motion.div>

      {/* content */}
      {isLoading ? (
        <div className={cn(
          view === 'grid' 
            ? 'grid sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4' 
            : 'space-y-3'
        )}>
          {Array.from({ length: 8 }).map((_, i) => (
            <div 
              key={i} 
              className={cn('skeleton rounded-2xl', view === 'grid' ? 'h-48' : 'h-18')} 
            />
          ))}
        </div>
      ) : filteredProjects.length > 0 ? (
        <motion.div
          layout
          variants={stagger}
          initial="hidden"
          animate="visible"
          className={cn(
            view === 'grid' 
              ? 'grid sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4' 
              : 'space-y-3'
          )}
        >
          <AnimatePresence>
            {filteredProjects.map((project) => (
              <ProjectCard 
                key={project.id} 
                project={project} 
                view={view} 
                onDelete={handleDelete} 
              />
            ))}
          </AnimatePresence>
        </motion.div>
      ) : (
        <motion.div variants={rise}>
          <GlassCard className="p-14 text-center">
            <div className="w-14 h-14 rounded-2xl bg-mauve-600/15 flex items-center justify-center mx-auto mb-5">
              <Search className="w-7 h-7 text-mauve-400" />
            </div>
            <h3 className="text-lg font-semibold text-lilac-100 mb-1.5">
              {search ? 'No results' : 'No projects yet'}
            </h3>
            <p className="text-sm text-lilac-300/50 mb-6">
              {search ? 'Try a different search term' : 'Create your first project to get started'}
            </p>
            {!search && (
              <Link to="/projects/new">
                <Button variant="primary" leftIcon={<Plus className="w-4 h-4" />}>
                  Create Project
                </Button>
              </Link>
            )}
          </GlassCard>
        </motion.div>
      )}
    </motion.div>
  );
}
