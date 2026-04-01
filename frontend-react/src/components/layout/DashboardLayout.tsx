import { Outlet, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Menu } from 'lucide-react';
import Sidebar from './Sidebar';
import { useUIStore } from '../../lib/store';
import { Logo } from '../ui';

export default function DashboardLayout() {
  const { sidebarOpen, setSidebarOpen } = useUIStore();
  const location = useLocation();

  // Full-bleed pages (no padding) — they manage their own layout
  const isFullBleed = location.pathname.includes('/generate');

  return (
    <div className="relative h-[100dvh] h-screen w-full overflow-hidden bg-[#0c0a12]">
      {/* subtle bg effects — clamped inside the layout */}
      <div className="fixed inset-0 grid-pattern pointer-events-none opacity-60" />
      <div className="fixed top-[-200px] right-[-100px] w-[500px] h-[500px] orb orb-mauve opacity-10 pointer-events-none" />
      <div className="fixed bottom-[-200px] left-[-100px] w-[400px] h-[400px] orb orb-lilac opacity-8 pointer-events-none" />

      {/* Sidebar */}
      <Sidebar />

      {/* Mobile header */}
      <AnimatePresence>
        {!sidebarOpen && (
          <motion.div
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            transition={{ duration: 0.2 }}
            className="md:hidden fixed top-0 left-0 right-0 z-20 flex items-center justify-between p-3 glass"
          >
            <Logo size="sm" />
            <button
              onClick={() => setSidebarOpen(true)}
              className="p-2 rounded-xl hover:bg-lilac-200/8 text-lilac-400 transition-colors"
            >
              <Menu className="w-5 h-5" />
            </button>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Main content — flex-based layout to prevent overflow */}
      <motion.main
        className={`absolute inset-0 flex flex-col overflow-x-hidden ${isFullBleed ? 'overflow-hidden' : 'overflow-y-auto scrollbar-thin'}`}
        initial={false}
        animate={{
          marginLeft: typeof window !== 'undefined' && window.innerWidth >= 768
            ? (sidebarOpen ? 240 : 72)
            : 0
        }}
        transition={{ duration: 0.25, ease: [0.25, 1, 0.5, 1] }}
        style={{ willChange: 'margin-left' }}
      >
        {isFullBleed ? (
          <div className="flex-1 min-h-0">
            <Outlet />
          </div>
        ) : (
          <div className="flex-1 p-8 max-md:p-5 max-md:pt-18 min-h-0">
            <Outlet />
          </div>
        )}
      </motion.main>
    </div>
  );
}

