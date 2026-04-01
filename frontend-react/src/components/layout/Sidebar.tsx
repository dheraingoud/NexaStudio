import { useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Link, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  FolderKanban,
  Settings,
  LogOut,
  ChevronLeft,
  Plus,
  X,
} from 'lucide-react';
import { Logo } from '../ui';
import { useAuthStore, useUIStore } from '../../lib/store';
import { cn } from '../../lib/utils';

const nav = [
  { icon: LayoutDashboard, label: 'Dashboard', path: '/dashboard' },
  { icon: FolderKanban, label: 'Projects', path: '/projects' },
];

export default function Sidebar() {
  const location = useLocation();
  const { sidebarOpen, toggleSidebar, setSidebarOpen } = useUIStore();
  const { logout, user } = useAuthStore();
  const open = sidebarOpen;

  // Close sidebar on mobile navigation
  useEffect(() => {
    const isMobile = window.innerWidth < 768;
    if (isMobile) {
      setSidebarOpen(false);
    }
  }, [location.pathname, setSidebarOpen]);

  return (
    <>
      {/* Mobile backdrop overlay */}
      <AnimatePresence>
        {open && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={() => setSidebarOpen(false)}
            className="fixed inset-0 bg-black/50 backdrop-blur-sm z-30 md:hidden"
          />
        )}
      </AnimatePresence>

      <motion.aside
        initial={false}
        animate={{ width: open ? 240 : 72 }}
        transition={{ duration: 0.25, ease: [0.25, 1, 0.5, 1] }}
        className={cn(
          'fixed left-0 top-0 h-screen z-40 flex flex-col',
          // On mobile: overlay mode when open, hidden when closed
          'max-md:translate-x-0',
          !open && 'max-md:-translate-x-full'
        )}
      >
        {/* backdrop — frosted glass */}
        <div className="absolute inset-0 glass-sidebar" />
        {/* frosted top-edge highlight */}
        <div className="absolute top-0 left-0 right-0 h-px bg-gradient-to-r from-transparent via-lilac-200/20 to-transparent" />
        {/* subtle bottom glow accent */}
        <div className="absolute bottom-0 left-0 right-0 h-32 bg-gradient-to-t from-mauve-600/5 to-transparent pointer-events-none" />

        <div className="relative z-10 flex flex-col h-full px-3 py-4">
          {/* header */}
          <div className="flex items-center justify-between mb-6 px-1">
            <Logo size="sm" showText={open} />
            <motion.button
              onClick={toggleSidebar}
              className="p-1.5 rounded-xl hover:bg-lilac-200/8 text-lilac-500/50 hover:text-lilac-300 transition-colors"
              whileTap={{ scale: 0.9 }}
              aria-label={open ? 'Collapse sidebar' : 'Expand sidebar'}
            >
              <motion.div animate={{ rotate: open ? 0 : 180 }} transition={{ duration: 0.25 }}>
                {/* On mobile show X when open */}
                <ChevronLeft className="w-4 h-4 hidden md:block" />
                <X className="w-4 h-4 md:hidden" />
              </motion.div>
            </motion.button>
          </div>

          {/* new project */}
          <Link to="/projects/new" className="mb-5">
            <motion.div
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.97 }}
              className={cn(
                'flex items-center justify-center gap-2 py-2.5 rounded-[14px] font-medium text-sm transition-all duration-300',
                'bg-gradient-to-r from-mauve-500 to-mauve-700 text-white shadow-lg shadow-mauve-600/40',
                'hover:shadow-mauve-500/50 hover:brightness-110',
                !open && 'px-0'
              )}
            >
              <Plus className="w-4 h-4" />
              {open && <span>New Project</span>}
            </motion.div>
          </Link>

          {/* navigation */}
          <nav className="flex-1 space-y-0.5">
            {nav.map((item) => {
              const active = location.pathname === item.path || location.pathname.startsWith(item.path + '/');
              return (
                <Link key={item.path} to={item.path}>
                  <motion.div
                    whileHover={{ x: 2 }}
                    transition={{ type: 'spring', stiffness: 400, damping: 25 }}
                    className={cn(
                      'flex items-center gap-3 px-3 py-2.5 rounded-[14px] text-sm font-medium transition-all duration-200 relative',
                      active
                        ? 'bg-mauve-500/20 text-mauve-200 shadow-sm shadow-mauve-500/10'
                        : 'text-lilac-400/60 hover:text-lilac-100 hover:bg-lilac-200/8'
                    )}
                  >
                    {active && (
                      <motion.div
                        layoutId="sidebar-active"
                        className="absolute left-0 top-1/2 -translate-y-1/2 w-[3px] h-5 rounded-r-full bg-mauve-400"
                        transition={{ type: 'spring', stiffness: 380, damping: 30 }}
                      />
                    )}
                    <item.icon className={cn('w-[18px] h-[18px] flex-shrink-0', active && 'text-mauve-400')} />
                    {open && <span>{item.label}</span>}
                  </motion.div>
                </Link>
              );
            })}
          </nav>

          {/* bottom */}
          <div className="border-t border-lilac-200/6 pt-3 space-y-0.5">
            <Link to="/settings">
              <motion.div
                whileHover={{ x: 2 }}
                transition={{ type: 'spring', stiffness: 400, damping: 25 }}
                className={cn(
                  'flex items-center gap-3 px-3 py-2.5 rounded-[14px] text-sm font-medium transition-all duration-200',
                  location.pathname === '/settings'
                    ? 'bg-mauve-500/20 text-mauve-200 shadow-sm shadow-mauve-500/10'
                    : 'text-lilac-400/60 hover:text-lilac-100 hover:bg-lilac-200/8'
                )}
              >
                <Settings className="w-[18px] h-[18px] flex-shrink-0" />
                {open && <span>Settings</span>}
              </motion.div>
            </Link>

            <motion.button
              onClick={logout}
              whileHover={{ x: 2 }}
              transition={{ type: 'spring', stiffness: 400, damping: 25 }}
              className="w-full flex items-center gap-3 px-3 py-2.5 rounded-[14px] text-sm font-medium text-lilac-400/55 hover:text-red-400 hover:bg-red-500/5 transition-all"
            >
              <LogOut className="w-[18px] h-[18px] flex-shrink-0" />
              {open && <span>Logout</span>}
            </motion.button>
          </div>

          {/* user */}
          {open && user && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              className="mt-3 p-3 rounded-[14px] bg-lilac-200/[0.03] border border-lilac-200/6"
            >
              <div className="flex items-center gap-2.5">
                <div className="w-8 h-8 rounded-xl bg-gradient-to-br from-mauve-500 to-mauve-700 flex items-center justify-center text-xs font-bold text-white">
                  {user.username?.charAt(0).toUpperCase() || 'U'}
                </div>
                <p className="text-xs font-medium text-lilac-200 truncate">@{user.username}</p>
              </div>
            </motion.div>
          )}
        </div>
      </motion.aside>
    </>
  );
}
