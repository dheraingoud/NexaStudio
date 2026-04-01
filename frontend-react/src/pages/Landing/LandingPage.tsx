import { useRef, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { motion, useScroll, useTransform, useMotionValue, useSpring, type Variants } from 'framer-motion';
import {
  ArrowRight,
  Sparkles,
  Code2,
  Zap,
  Layers,
  Globe,
  Shield,
} from 'lucide-react';
import { Button, GlassCard, Logo } from '../../components/ui';

/* ═══════════════════════════════════════════════
   Parallax Hero — 3D hover + scroll effect
   ═══════════════════════════════════════════════ */

function ParallaxHero() {
  const containerRef = useRef<HTMLDivElement>(null);

  // Mouse tracking
  const mouseX = useMotionValue(0);
  const mouseY = useMotionValue(0);
  const rotateX = useMotionValue(0);
  const rotateY = useMotionValue(0);

  // Smooth spring physics for that floaty 3D feel
  const springConfig = { damping: 20, stiffness: 80, mass: 1 };
  const x = useSpring(mouseX, springConfig);
  const y = useSpring(mouseY, springConfig);
  const rx = useSpring(rotateX, { damping: 30, stiffness: 100 });
  const ry = useSpring(rotateY, { damping: 30, stiffness: 100 });
  const baseShiftX = -15;
  const shiftedX = useTransform(x, (v) => v + baseShiftX);

  // Scroll-based parallax
  const { scrollY } = useScroll();
  const scrollOffset = useTransform(scrollY, [0, 800], [0, -100]);
  const scrollScale = useTransform(scrollY, [0, 600], [1.02, 1.08]);
  const scrollOpacity = useTransform(scrollY, [0, 600], [1, 0]);
  const scrollBlur = useTransform(scrollY, [0, 400], [0, 6]);

  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => {
      if (!containerRef.current) return;
      const rect = containerRef.current.getBoundingClientRect();
      const centerX = rect.left + rect.width / 2;
      const centerY = rect.top + rect.height / 2;

      // Calculate mouse position relative to center (-1 to 1)
      const relX = (e.clientX - centerX) / (rect.width / 2);
      const relY = (e.clientY - centerY) / (rect.height / 2);

      // Translate movement (parallax shift)
      mouseX.set(relX * -15);
      mouseY.set(relY * -10);

      // 3D rotation (tilt effect)
      rotateX.set(relY * -5); // Tilt up/down based on Y
      rotateY.set(relX * 5);  // Tilt left/right based on X
    };

    const handleMouseLeave = () => {
      // Reset to center when mouse leaves
      mouseX.set(0);
      mouseY.set(0);
      rotateX.set(0);
      rotateY.set(0);
    };

    const container = containerRef.current;
    if (container) {
      container.addEventListener('mousemove', handleMouseMove);
      container.addEventListener('mouseleave', handleMouseLeave);
    }

    return () => {
      if (container) {
        container.removeEventListener('mousemove', handleMouseMove);
        container.removeEventListener('mouseleave', handleMouseLeave);
      }
    };
  }, [mouseX, mouseY, rotateX, rotateY]);

  return (
    <div
      ref={containerRef}
      className="absolute inset-0 z-0 overflow-hidden"
      style={{ perspective: '1200px' }}
    >
      {/* Vignette effect */}
      <div
        className="absolute inset-0 z-10 pointer-events-none"
        style={{
          background: 'radial-gradient(ellipse at center, transparent 30%, rgba(8,7,14,0.7) 100%)'
        }}
      />

      {/* Main parallax image with 3D transform */}
      <motion.div
        className="absolute inset-0 z-0"
        style={{
          x: shiftedX,
          y,
          translateY: scrollOffset,
          scale: scrollScale,
          opacity: scrollOpacity,
          rotateX: rx,
          rotateY: ry,
          filter: useTransform(scrollBlur, (v) => `blur(${v}px)`),
          transformStyle: 'preserve-3d',
        }}
      >
        <img
          src="/hero-bg.png"
          alt=""
          className="w-full h-full object-cover select-none pointer-events-none"
          style={{
            filter: 'brightness(0.65) saturate(1.15) contrast(1.05)',
            objectPosition: '60% 30%',
          }}
          draggable={false}
        />
      </motion.div>

      {/* Floating accent layers that move at different speeds for depth */}
      <motion.div
        className="absolute top-[15%] left-[10%] w-[400px] h-[400px] rounded-full z-[5] pointer-events-none"
        style={{
          background: 'radial-gradient(circle, rgba(145,95,109,0.15) 0%, transparent 60%)',
          filter: 'blur(80px)',
          x: useSpring(mouseX, { ...springConfig, stiffness: 40 }),
          y: useSpring(mouseY, { ...springConfig, stiffness: 40 }),
        }}
      />
      <motion.div
        className="absolute bottom-[25%] right-[15%] w-[350px] h-[350px] rounded-full z-[5] pointer-events-none"
        style={{
          background: 'radial-gradient(circle, rgba(155,165,229,0.12) 0%, transparent 60%)',
          filter: 'blur(70px)',
          x: useSpring(mouseX, { ...springConfig, stiffness: 25 }),
          y: useSpring(mouseY, { ...springConfig, stiffness: 25 }),
        }}
      />
    </div>
  );
}

/* ═══════════════════════════════════════════════
   Animation Variants
   ═══════════════════════════════════════════════ */

const fadeUp: Variants = {
  hidden: { opacity: 0, y: 30 },
  visible: { opacity: 1, y: 0, transition: { type: 'spring', stiffness: 250, damping: 25 } },
};

const stagger: Variants = {
  hidden: { opacity: 0 },
  visible: { opacity: 1, transition: { staggerChildren: 0.1, delayChildren: 0.1 } },
};

/* ═══════════════════════════════════════════════
   Main Page — Minimal & Elegant
   ═══════════════════════════════════════════════ */

export default function LandingPage() {
  const containerRef = useRef<HTMLDivElement>(null);
  const { scrollYProgress } = useScroll();
  const navBg = useTransform(scrollYProgress, [0, 0.05], [0, 0.95]);

  const features = [
    { icon: Sparkles, title: 'AI Generation', desc: 'Natural language to production code in seconds.' },
    { icon: Code2, title: 'Multi-Framework', desc: 'React, Next.js, Vue, Angular — your stack, your rules.' },
    { icon: Zap, title: 'Instant Preview', desc: 'See your app render live as code is generated.' },
    { icon: Layers, title: 'Smart Structure', desc: 'Automatic routing, components, and best-practice layout.' },
    { icon: Shield, title: 'TypeScript-First', desc: 'Strict types, accessibility, and security built in.' },
    { icon: Globe, title: 'Deploy Ready', desc: 'Export and ship to Vercel, Netlify, or anywhere.' },
  ];

  return (
    <div ref={containerRef} className="min-h-screen relative overflow-x-hidden bg-[#08070e]">
      {/* Background */}
      <div className="fixed inset-0 -z-10">
        <div className="absolute top-[-20%] left-[10%] w-[700px] h-[700px] bg-mauve-600/5 rounded-full blur-[200px]" />
        <div className="absolute bottom-[-10%] right-[15%] w-[500px] h-[500px] bg-lilac-500/4 rounded-full blur-[160px]" />
      </div>

      {/* ── Nav ── */}
      <motion.nav
        style={{ backgroundColor: `rgba(8,7,14,${navBg})` }}
        className="fixed top-0 left-0 right-0 z-50 backdrop-blur-2xl border-b border-white/[0.04]"
      >
        <div className="mx-auto max-w-5xl px-6 py-3.5">
          <div className="flex items-center justify-between">
            <Logo size="md" />
            <div className="flex items-center gap-2.5">
              <Link to="/login">
                <Button variant="ghost" size="sm">Sign In</Button>
              </Link>
              <Link to="/register">
                <Button variant="primary" size="sm">Get Started</Button>
              </Link>
            </div>
          </div>
        </div>
      </motion.nav>

      {/* ── Hero ── */}
      <section className="relative min-h-screen flex items-center justify-center px-6">
        {/* Parallax image — behind text */}
        <ParallaxHero />

        {/* Signature on hero image */}
        <div className="absolute bottom-6 left-1/2 -translate-x-1/2 z-20">
          <span className="font-display text-sm sm:text-base font-bold tracking-tight text-gradient">
            Dherain Goud • v1.0
          </span>
        </div>

        <div className="relative z-10 max-w-3xl mx-auto text-center">
          <div>
            {/* Heading */}
            <h1 className="font-display text-5xl sm:text-6xl lg:text-7xl font-bold text-lilac-100 leading-[1.05] mb-6 tracking-tight">
              Describe it.{' '}
              <span className="text-gradient">Ship it.</span>
            </h1>

            {/* Sub */}
            <p className="text-lg text-lilac-300/50 max-w-lg mx-auto mb-10 leading-relaxed">
              Turn a single prompt into a production-ready web app. Components, routes, styling. All of it.
            </p>

            {/* CTA */}
            <div className="flex justify-center">
              <Link to="/register">
                <Button variant="primary" size="lg" rightIcon={<ArrowRight className="w-4 h-4" />}>
                  Start building free
                </Button>
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* ── Features ── */}
      <section className="py-32 px-6">
        <div className="max-w-5xl mx-auto">
          <motion.div
            initial={{ opacity: 0, y: 24 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.6 }}
            className="text-center mb-16"
          >
            <h2 className="font-display text-3xl sm:text-4xl font-bold text-lilac-100 mb-4">
              Everything to ship faster
            </h2>
            <p className="text-lilac-300/45 max-w-md mx-auto text-sm">
              From prompt to production in minutes.
            </p>
          </motion.div>

          <motion.div
            variants={stagger}
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true, margin: '-60px' }}
            className="grid sm:grid-cols-2 lg:grid-cols-3 gap-5"
          >
            {features.map((f) => (
              <motion.div key={f.title} variants={fadeUp}>
                <GlassCard className="p-7 h-full group">
                  <div className="w-10 h-10 rounded-2xl bg-mauve-500/12 border border-mauve-500/15 flex items-center justify-center mb-4 group-hover:border-mauve-500/30 transition-colors">
                    <f.icon className="w-5 h-5 text-mauve-400" />
                  </div>
                  <h3 className="text-sm font-semibold text-lilac-100 mb-1.5">{f.title}</h3>
                  <p className="text-xs text-lilac-300/45 leading-relaxed">{f.desc}</p>
                </GlassCard>
              </motion.div>
            ))}
          </motion.div>
        </div>
      </section>

      {/* ── How it works ── */}
      <section className="py-32 px-6">
        <div className="max-w-4xl mx-auto">
          <motion.div
            initial={{ opacity: 0, y: 24 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            className="text-center mb-16"
          >
            <h2 className="font-display text-3xl sm:text-4xl font-bold text-lilac-100 mb-4">
              How it works
            </h2>
          </motion.div>

          <motion.div
            variants={stagger}
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            className="grid md:grid-cols-3 gap-8"
          >
            {[
              { step: '01', title: 'Describe', desc: 'Tell NexaStudio what you want to build in plain English.' },
              { step: '02', title: 'Generate', desc: 'AI creates a full project with components, routing, and styles.' },
              { step: '03', title: 'Ship', desc: 'Iterate with follow-up prompts, then export and deploy.' },
            ].map((s) => (
              <motion.div key={s.step} variants={fadeUp} className="text-center">
                <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-gradient-to-br from-mauve-500/20 to-mauve-600/10 border border-mauve-500/15 text-sm font-bold text-mauve-400 mb-5">
                  {s.step}
                </div>
                <h3 className="font-display text-lg font-semibold text-lilac-100 mb-2">{s.title}</h3>
                <p className="text-sm text-lilac-300/45 leading-relaxed max-w-xs mx-auto">{s.desc}</p>
              </motion.div>
            ))}
          </motion.div>
        </div>
      </section>

      {/* ── CTA ── */}
      <section className="py-32 px-6">
        <div className="max-w-2xl mx-auto">
          <motion.div
            initial={{ opacity: 0, scale: 0.97 }}
            whileInView={{ opacity: 1, scale: 1 }}
            viewport={{ once: true }}
            transition={{ duration: 0.5 }}
          >
            <GlassCard className="p-14 text-center relative overflow-hidden" glow>
              <div className="absolute inset-0 bg-gradient-to-br from-mauve-600/8 via-transparent to-lilac-500/5" />
              <div className="relative">
                <h2 className="font-display text-3xl sm:text-4xl font-bold text-lilac-100 mb-4">
                  Ready to build?
                </h2>
                <p className="text-lilac-300/45 mb-8 max-w-sm mx-auto text-sm">
                  Join developers shipping faster with AI.
                </p>
                <Link to="/register">
                  <Button variant="primary" size="lg" rightIcon={<ArrowRight className="w-4 h-4" />}>
                    Create free account
                  </Button>
                </Link>
              </div>
            </GlassCard>
          </motion.div>
        </div>
      </section>

      {/* ── Footer ── */}
      <footer className="py-8 px-6 border-t border-white/[0.04]">
        <div className="max-w-5xl mx-auto flex items-center justify-between">
          <Logo size="sm" />
          <span className="text-xs text-lilac-400/30 text-center">Dherain Goud - v1.0</span>
          <span className="text-xs text-lilac-400/30">© {new Date().getFullYear()} NexaStudio</span>
        </div>
      </footer>
    </div>
  );
}
