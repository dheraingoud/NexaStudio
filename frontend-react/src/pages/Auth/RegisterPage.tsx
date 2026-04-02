import { useState, Suspense, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Canvas, useFrame } from '@react-three/fiber';
import { Float, Icosahedron, MeshWobbleMaterial } from '@react-three/drei';
import axios from 'axios';
import * as THREE from 'three';
import { User, Lock, ArrowRight, ShieldCheck, CheckCircle2, AlertCircle } from 'lucide-react';
import { Button, Input, Logo } from '../../components/ui';
import { useAuthStore } from '../../lib/store';
import { authApi } from '../../lib/api';

// 3D floating gem with lilac accent
function AuthGem() {
  const ref = useRef<THREE.Mesh>(null);

  useFrame(({ clock }) => {
    if (!ref.current) return;
    ref.current.rotation.y = clock.elapsedTime * 0.2;
    ref.current.rotation.x = Math.sin(clock.elapsedTime * 0.15) * 0.3;
  });

  return (
    <Float speed={0.5} rotationIntensity={0.4} floatIntensity={0.6}>
      <Icosahedron ref={ref} args={[1.8, 1]}>
        <MeshWobbleMaterial
          color="#E6E9F9"
          roughness={0.1}
          metalness={0.9}
          factor={0.25}
          speed={0.8}
          transparent
          opacity={0.25}
        />
      </Icosahedron>
    </Float>
  );
}

function AuthScene() {
  return (
    <Canvas camera={{ position: [0, 0, 5], fov: 45 }} style={{ position: 'absolute', inset: 0 }}>
      <ambientLight intensity={0.12} />
      <pointLight position={[3, 3, 3]} intensity={0.5} color="#915F6D" />
      <pointLight position={[-3, -2, 2]} intensity={0.3} color="#E6E9F9" />
      <Suspense fallback={null}>
        <AuthGem />
      </Suspense>
    </Canvas>
  );
}

// Password validation rules
const PASSWORD_RULES = [
  { id: 'length', label: 'At least 8 characters', test: (p: string) => p.length >= 8 },
  { id: 'number', label: 'Contains a number', test: (p: string) => /\d/.test(p) },
] as const;

// Extract error message with proper type guard
function getErrorMessage(err: unknown): string {
  if (axios.isAxiosError(err)) {
    return err.response?.data?.message || err.message || 'Registration failed';
  }
  if (err instanceof Error) {
    return err.message;
  }
  return 'Registration failed';
}

export default function RegisterPage() {
  const navigate = useNavigate();
  const { login } = useAuthStore();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const allRulesMet = PASSWORD_RULES.every((rule) => rule.test(password));
  const passwordsMatch = password === confirmPassword && confirmPassword.length > 0;
  const canSubmit = allRulesMet && passwordsMatch && username.trim().length > 0;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!allRulesMet) {
      setError('Password does not meet the requirements');
      return;
    }
    if (!passwordsMatch) {
      setError('Passwords do not match');
      return;
    }

    setIsLoading(true);
    try {
      const { data } = await authApi.register(username, password);
      login({ id: data.user.id, username: data.user.username }, data.accessToken, data.refreshToken);
      navigate('/dashboard', { replace: true });
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen min-h-[100dvh] flex relative overflow-hidden bg-[#08070e]">
      {/* Left panel — 3D visualization */}
      <div className="hidden lg:flex lg:w-1/2 items-center justify-center relative">
        <AuthScene />
        <div className="relative z-10 max-w-md px-12 text-center pointer-events-none select-none">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.3, type: 'spring', stiffness: 300, damping: 30 }}
          >
            <ShieldCheck className="w-8 h-8 text-mauve-400 mx-auto mb-4 opacity-60" />
            <h2 className="text-2xl font-display font-bold text-lilac-100 mb-3">
              Start building today
            </h2>
            <p className="text-sm text-lilac-300/50 leading-relaxed">
              Create an account and start generating production-ready code in seconds — completely free.
            </p>
          </motion.div>
        </div>
      </div>

      {/* Right panel — Register form */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-6 relative">
        <div className="absolute inset-0 bg-gradient-to-br from-[#08070e] via-[#0d0c14] to-[#08070e]" />

        <motion.div
          initial={{ opacity: 0, y: 24 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, ease: [0.22, 1, 0.36, 1] }}
          className="w-full max-w-sm relative z-10"
        >
          <div className="glass-strong rounded-2xl p-8">
            {/* Logo */}
            <Link to="/" className="flex justify-center mb-10 transition-opacity hover:opacity-80">
              <Logo size="lg" />
            </Link>

            {/* Header */}
            <header className="text-center mb-8">
              <h1 className="text-2xl font-display font-bold text-lilac-100 mb-1.5">
                Create account
              </h1>
              <p className="text-sm text-lilac-300/50">Set up your workspace in seconds</p>
            </header>

            {/* Form */}
            <form onSubmit={handleSubmit} className="space-y-5">
              <Input
                label="Username"
                placeholder="Choose a username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                leftIcon={<User className="w-4 h-4" />}
                autoComplete="username"
                required
              />
              <Input
                label="Password"
                type="password"
                placeholder="Create a password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                leftIcon={<Lock className="w-4 h-4" />}
                autoComplete="new-password"
                required
              />
              <Input
                label="Confirm Password"
                type="password"
                placeholder="Repeat your password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                leftIcon={<Lock className="w-4 h-4" />}
                autoComplete="new-password"
                required
              />

              {/* Password requirements */}
              <AnimatePresence>
                {password.length > 0 && (
                  <motion.div
                    initial={{ opacity: 0, height: 0 }}
                    animate={{ opacity: 1, height: 'auto' }}
                    exit={{ opacity: 0, height: 0 }}
                    transition={{ duration: 0.2 }}
                    className="glass-subtle rounded-xl p-3 space-y-2"
                  >
                    {PASSWORD_RULES.map((rule) => {
                      const met = rule.test(password);
                      return (
                        <div
                          key={rule.id}
                          className="flex items-center gap-2.5 transition-opacity duration-200"
                        >
                          <CheckCircle2
                            className={`w-4 h-4 shrink-0 transition-colors duration-200 ${
                              met ? 'text-emerald-400' : 'text-lilac-300/25'
                            }`}
                          />
                          <span
                            className={`text-sm transition-colors duration-200 ${
                              met ? 'text-lilac-200' : 'text-lilac-300/40'
                            }`}
                          >
                            {rule.label}
                          </span>
                        </div>
                      );
                    })}
                    
                    {confirmPassword.length > 0 && (
                      <div className="flex items-center gap-2.5 transition-opacity duration-200">
                        <CheckCircle2
                          className={`w-4 h-4 shrink-0 transition-colors duration-200 ${
                            passwordsMatch ? 'text-emerald-400' : 'text-lilac-300/25'
                          }`}
                        />
                        <span
                          className={`text-sm transition-colors duration-200 ${
                            passwordsMatch ? 'text-lilac-200' : 'text-lilac-300/40'
                          }`}
                        >
                          Passwords match
                        </span>
                      </div>
                    )}
                  </motion.div>
                )}
              </AnimatePresence>

              {/* Error display */}
              <AnimatePresence mode="wait">
                {error && (
                  <motion.div
                    initial={{ opacity: 0, y: -8, scale: 0.96 }}
                    animate={{ opacity: 1, y: 0, scale: 1 }}
                    exit={{ opacity: 0, y: -4, scale: 0.98 }}
                    transition={{ duration: 0.2, ease: 'easeOut' }}
                    className="glass-subtle p-3 rounded-xl flex items-start gap-2.5 border border-red-500/20 bg-red-500/5"
                  >
                    <AlertCircle className="w-4 h-4 text-red-400 shrink-0 mt-0.5" />
                    <p className="text-sm text-red-300/90 leading-snug">{error}</p>
                  </motion.div>
                )}
              </AnimatePresence>

              <Button
                type="submit"
                variant="primary"
                size="lg"
                isLoading={isLoading}
                rightIcon={<ArrowRight className="w-4 h-4 transition-transform group-hover:translate-x-0.5" />}
                className="w-full mt-2 group"
                disabled={!canSubmit}
              >
                Create Account
              </Button>
            </form>

            {/* Footer */}
            <footer className="mt-8 text-center">
              <p className="text-sm text-lilac-300/45">
                Already have an account?{' '}
                <Link
                  to="/login"
                  className="text-mauve-400 hover:text-mauve-300 font-medium transition-colors duration-200"
                >
                  Sign in
                </Link>
              </p>
            </footer>
          </div>
        </motion.div>
      </div>
    </div>
  );
}
