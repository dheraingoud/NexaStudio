import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';
import {
  Plus,
  FolderKanban,
  Sparkles,
  Clock,
  ArrowRight,
  Zap,
  TrendingUp,
  Code2,
  Trash2,
  BarChart3,
} from 'lucide-react';
import { Button, GlassCard } from '../../components/ui';
import { FrameworkIcon } from '../../components/ui/FrameworkIcon';
import { useAuthStore, useProjectStore, type Project } from '../../lib/store';
import { projectsApi } from '../../lib/api';
import { formatRelativeTime, cn } from '../../lib/utils';

/* ── animation helpers — Apple spring physics ──── */
const stagger = {
  hidden: { opacity: 0 },
  visible: { opacity: 1, transition: { staggerChildren: 0.08 } },
};
const rise = {
  hidden: { opacity: 0, y: 20 },
  visible: {
    opacity: 1,
    y: 0,
    transition: { type: 'spring' as const, stiffness: 300, damping: 30 },
  },
};

/* ── Stat tile (bento) ─────────────────────────── */
interface StatProps {
  icon: React.ElementType<{ className?: string }>;
  label: string;
  value: string | number;
  accent?: string;
  trend?: 'up' | 'neutral';
}

function Stat({ icon: Icon, label, value, accent, trend = 'neutral' }: StatProps) {
  return (
    <GlassCard className="p-5 flex flex-col justify-between h-full group" hover>
      <div className="flex items-center justify-between mb-4">
        <div className={cn(
          'p-2.5 rounded-xl transition-all duration-300 group-hover:scale-110',
          accent || 'bg-mauve-600/15'
        )}>
          <Icon className="w-5 h-5 text-mauve-400" />
        </div>
        {trend === 'up' && (
          <TrendingUp className="w-4 h-4 text-emerald-400/60" />
        )}
      </div>
      <p className="text-3xl font-bold text-lilac-100 tracking-tight">{value}</p>
      <p className="text-xs text-lilac-400/55 mt-1 uppercase tracking-wider">{label}</p>
    </GlassCard>
  );
}

/* ── Compact project row ───────────────────────── */
interface ProjectRowProps {
  project: Project;
  onDelete: (id: string) => void;
}

function ProjectRow({ project, onDelete }: ProjectRowProps) {
  const framework = project.framework || project.type;
  
  return (
    <div className="flex items-center gap-3 px-4 py-3 rounded-xl hover:bg-lilac-200/[0.04] transition-all duration-200 group">
      <Link to={`/projects/${project.id}/generate`} className="flex items-center gap-3 flex-1 min-w-0">
        <FrameworkIcon framework={framework} size={22} />
        <div className="flex-1 min-w-0">
          <p className="text-sm font-medium text-lilac-100 truncate group-hover:text-mauve-300 transition-colors">
            {project.name}
          </p>
          <p className="text-xs text-lilac-400/45 truncate">
            {project.description || 'No description'}
          </p>
        </div>
        <div className="flex items-center gap-1.5 text-[11px] text-lilac-500/40">
          <Clock className="w-3 h-3" />
          {formatRelativeTime(project.updatedAt)}
        </div>
      </Link>
      <button
        onClick={() => onDelete(project.id)}
        className={cn(
          'p-2 rounded-xl transition-all duration-200',
          'opacity-0 group-hover:opacity-100',
          'hover:bg-red-500/10 text-lilac-500/30 hover:text-red-400'
        )}
        title="Delete project"
        aria-label={`Delete ${project.name}`}
      >
        <Trash2 className="w-4 h-4" />
      </button>
    </div>
  );
}

/* ── Quick-action pill ─────────────────────────── */
interface ActionPillProps {
  icon: React.ElementType<{ className?: string }>;
  label: string;
  to: string;
  gradient: string;
}

function ActionPill({ icon: Icon, label, to, gradient }: ActionPillProps) {
  return (
    <Link to={to}>
      <motion.div
        whileHover={{ scale: 1.02, y: -2 }}
        whileTap={{ scale: 0.98 }}
        transition={{ type: 'spring', stiffness: 400, damping: 25 }}
        className={cn(
          'flex items-center gap-3 p-4 rounded-2xl cursor-pointer',
          'bg-gradient-to-br transition-shadow duration-300',
          gradient,
          'shadow-lg shadow-black/20 hover:shadow-xl hover:shadow-black/30'
        )}
      >
        <Icon className="w-5 h-5 text-white/90" />
        <span className="text-sm font-semibold text-white/90">{label}</span>
      </motion.div>
    </Link>
  );
}

/* ── Main ──────────────────────────────────────── */
export default function DashboardPage() {
  const { user } = useAuthStore();
  const { projects, setProjects, removeProject, setLoading, isLoading } = useProjectStore();
  const [greeting, setGreeting] = useState('');

  useEffect(() => {
    const h = new Date().getHours();
    setGreeting(h < 12 ? 'Good morning' : h < 18 ? 'Good afternoon' : 'Good evening');
  }, []);

  useEffect(() => {
    const fetchProjects = async () => {
      setLoading(true);
      try {
        const res = await projectsApi.getAll();
        setProjects(res.data);
      } catch (e) {
        console.error('Failed to fetch projects:', e);
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
    } catch (e) {
      console.error('Failed to delete project:', e);
    }
  };

  const recentProjects = projects.slice(0, 6);

  return (
    <motion.div 
      variants={stagger} 
      initial="hidden" 
      animate="visible" 
      className="max-w-7xl mx-auto space-y-8"
    >
      {/* ── Header ────────────────────────────────── */}
      <motion.div variants={rise} className="flex flex-col sm:flex-row sm:items-end justify-between gap-4">
        <div>
          <p className="text-sm text-lilac-400/50 mb-1">{greeting}</p>
          <h1 className="text-3xl font-display font-bold text-lilac-100">
            Welcome back, <span className="text-gradient">{user?.username || 'Developer'}</span>
          </h1>
        </div>
        <Link to="/projects/new">
          <Button variant="primary" leftIcon={<Plus className="w-4 h-4" />}>
            New Project
          </Button>
        </Link>
      </motion.div>

      {/* ── Bento stats grid ──────────────────────── */}
      <motion.div variants={rise} className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <Stat 
          icon={FolderKanban} 
          label="Projects" 
          value={projects.length} 
          trend={projects.length > 0 ? 'up' : 'neutral'}
        />
        <Stat 
          icon={Sparkles} 
          label="Generations" 
          value="—" 
          accent="bg-violet-600/15" 
        />
        <Stat 
          icon={Zap} 
          label="Deployments" 
          value="—" 
          accent="bg-amber-600/15" 
        />
        <Stat 
          icon={BarChart3} 
          label="Uptime" 
          value="100%" 
          accent="bg-emerald-600/15"
          trend="up"
        />
      </motion.div>

      {/* ── Quick actions row ─────────────────────── */}
      <motion.div variants={rise} className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <ActionPill icon={Plus} label="New Project" to="/projects/new" gradient="from-mauve-600 to-mauve-800" />
        <ActionPill icon={Sparkles} label="AI Generate" to="/projects/new" gradient="from-violet-600 to-purple-800" />
        <ActionPill icon={FolderKanban} label="All Projects" to="/projects" gradient="from-emerald-600 to-teal-800" />
      </motion.div>

      {/* ── Main content: 2/3 + 1/3 ──────────────── */}
      <div className="grid lg:grid-cols-3 gap-6">
        {/* Recent projects */}
        <motion.div variants={rise} className="lg:col-span-2">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-lilac-100">Recent Projects</h2>
            <Link to="/projects">
              <Button variant="ghost" size="sm" rightIcon={<ArrowRight className="w-4 h-4" />}>
                View All
              </Button>
            </Link>
          </div>

          {isLoading ? (
            <div className="space-y-3">
              {Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className="h-16 skeleton rounded-xl" />
              ))}
            </div>
          ) : recentProjects.length > 0 ? (
            <GlassCard className="divide-y divide-lilac-200/[0.06] overflow-hidden">
              {recentProjects.map((project) => (
                <ProjectRow key={project.id} project={project} onDelete={handleDelete} />
              ))}
            </GlassCard>
          ) : (
            <GlassCard className="p-14 text-center">
              <div className="w-14 h-14 rounded-2xl bg-mauve-600/15 flex items-center justify-center mx-auto mb-5">
                <FolderKanban className="w-7 h-7 text-mauve-400" />
              </div>
              <h3 className="text-lg font-semibold text-lilac-100 mb-1.5">No projects yet</h3>
              <p className="text-sm text-lilac-300/50 mb-6">
                Create your first project and start building with AI
              </p>
              <Link to="/projects/new">
                <Button variant="primary" leftIcon={<Plus className="w-4 h-4" />}>
                  Create Project
                </Button>
              </Link>
            </GlassCard>
          )}
        </motion.div>

        {/* Activity */}
        <motion.div variants={rise}>
          <h2 className="text-lg font-semibold text-lilac-100 mb-4">Activity</h2>
          <GlassCard className="p-4">
            {projects.length > 0 ? (
              <div className="space-y-3">
                {projects.slice(0, 5).map((project) => (
                  <div key={project.id} className="flex items-start gap-3 group">
                    <div className="w-1.5 h-1.5 rounded-full bg-mauve-500 mt-2 group-hover:scale-125 transition-transform" />
                    <div className="flex-1 min-w-0">
                      <p className="text-sm text-lilac-200">
                        <span className="text-lilac-400/60">Created</span>{' '}
                        <span className="font-medium truncate">{project.name}</span>
                      </p>
                      <p className="text-xs text-lilac-500/40 mt-0.5">
                        {formatRelativeTime(project.createdAt)}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-sm text-lilac-400/40 text-center py-8">No activity yet</p>
            )}
          </GlassCard>

          {/* Tips card */}
          <GlassCard className="p-5 mt-4" glow>
            <div className="flex items-center gap-2 mb-3">
              <Code2 className="w-4 h-4 text-mauve-400" />
              <span className="text-xs font-semibold text-lilac-300/70 uppercase tracking-wider">
                Pro tip
              </span>
            </div>
            <p className="text-sm text-lilac-200 leading-relaxed">
              Use{' '}
              <kbd className="px-1.5 py-0.5 rounded-lg bg-lilac-200/10 text-xs font-mono text-mauve-300">
                AI Generate
              </kbd>{' '}
              to scaffold a full app from a single prompt — landing pages, dashboards, even e-commerce stores.
            </p>
          </GlassCard>
        </motion.div>
      </div>
    </motion.div>
  );
}
