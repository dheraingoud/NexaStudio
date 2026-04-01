import { useState } from 'react';
import { motion, type Variants } from 'framer-motion';
import { useNavigate } from 'react-router-dom';
import { User, Lock, Save, LogOut, Shield } from 'lucide-react';
import { Button, GlassCard, Input } from '../../components/ui';
import { useAuthStore } from '../../lib/store';
import { authApi } from '../../lib/api';

const stagger: Variants = {
  hidden: { opacity: 0 },
  visible: { opacity: 1, transition: { staggerChildren: 0.08 } },
};
const rise: Variants = {
  hidden: { opacity: 0, y: 16 },
  visible: { opacity: 1, y: 0, transition: { type: 'spring', stiffness: 300, damping: 30 } },
};

export default function SettingsPage() {
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [msg, setMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const handlePasswordChange = async (e: React.FormEvent) => {
    e.preventDefault();
    setMsg(null);
    if (newPassword.length < 8) { setMsg({ type: 'error', text: 'Password must be at least 8 characters' }); return; }
    if (newPassword !== confirmPassword) { setMsg({ type: 'error', text: 'Passwords do not match' }); return; }

    setIsLoading(true);
    try {
      await authApi.updatePassword(currentPassword, newPassword);
      setMsg({ type: 'success', text: 'Password updated successfully' });
      setCurrentPassword(''); setNewPassword(''); setConfirmPassword('');
    } catch (err: unknown) {
      const e = err as { response?: { data?: { message?: string } } };
      setMsg({ type: 'error', text: e.response?.data?.message || 'Failed to update password' });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <motion.div variants={stagger} initial="hidden" animate="visible" className="max-w-2xl mx-auto">
      {/* header */}
      <motion.div variants={rise} className="mb-8">
        <h1 className="text-2xl font-bold text-lilac-100 mb-1">Settings</h1>
        <p className="text-sm text-lilac-400/50">Manage your account and preferences</p>
      </motion.div>

      {/* profile */}
      <motion.div variants={rise}>
        <GlassCard className="p-6 mb-5">
          <div className="flex items-center gap-3 mb-5">
            <div className="w-10 h-10 rounded-[14px] bg-gradient-to-br from-mauve-500 to-mauve-700 flex items-center justify-center text-white font-bold text-sm">
              {user?.username?.charAt(0).toUpperCase() || 'U'}
            </div>
            <div>
              <p className="text-sm font-semibold text-lilac-100">@{user?.username}</p>
              <p className="text-xs text-lilac-400/40">Your profile</p>
            </div>
          </div>
          <Input
            label="Username"
            value={user?.username || ''}
            disabled
            leftIcon={<User className="w-4 h-4" />}
            hint="Username cannot be changed"
          />
        </GlassCard>
      </motion.div>

      {/* password */}
      <motion.div variants={rise}>
        <GlassCard className="p-6 mb-5">
          <h2 className="text-base font-semibold text-lilac-100 mb-5 flex items-center gap-2">
            <Shield className="w-4 h-4 text-mauve-400" /> Security
          </h2>
          <form onSubmit={handlePasswordChange} className="space-y-4">
            <Input label="Current Password" type="password" placeholder="Enter current password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} leftIcon={<Lock className="w-4 h-4" />} required />
            <Input label="New Password" type="password" placeholder="At least 8 characters" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} leftIcon={<Lock className="w-4 h-4" />} required />
            <Input
              label="Confirm New Password"
              type="password"
              placeholder="Repeat new password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              leftIcon={<Lock className="w-4 h-4" />}
              error={confirmPassword && newPassword !== confirmPassword ? 'Passwords do not match' : undefined}
              required
            />

            {msg && (
              <motion.div
                initial={{ opacity: 0, y: -5 }}
                animate={{ opacity: 1, y: 0 }}
                className={`p-3 rounded-[14px] border text-xs ${msg.type === 'success' ? 'bg-emerald-500/8 border-emerald-500/15 text-emerald-400' : 'bg-red-500/8 border-red-500/15 text-red-400'}`}
              >
                {msg.text}
              </motion.div>
            )}

            <Button type="submit" variant="primary" isLoading={isLoading} leftIcon={<Save className="w-4 h-4" />}>
              Update Password
            </Button>
          </form>
        </GlassCard>
      </motion.div>

      {/* danger zone */}
      <motion.div variants={rise}>
        <GlassCard className="p-6 border-red-500/10">
          <h2 className="text-base font-semibold text-lilac-100 mb-2">Session</h2>
          <p className="text-xs text-lilac-400/45 mb-4">Sign out of your account on this device.</p>
          <Button variant="danger" onClick={() => { logout(); navigate('/'); }} leftIcon={<LogOut className="w-4 h-4" />}>
            Sign Out
          </Button>
        </GlassCard>
      </motion.div>
    </motion.div>
  );
}
