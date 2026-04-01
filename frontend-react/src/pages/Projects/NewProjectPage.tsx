import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useNavigate } from 'react-router-dom';
import {
  ArrowLeft,
  ArrowRight,
  Sparkles,
  Check,
  Palette,
  SkipForward,
} from 'lucide-react';
import { Button, GlassCard, Input, FrameworkIcon } from '../../components/ui';
import { projectsApi } from '../../lib/api';
import { useProjectStore } from '../../lib/store';
import { cn } from '../../lib/utils';
import { templates, colorSchemes, designStyles, animationStyles } from '../../lib/templates';

/* ── framework options (must align with backend ProjectType) ── */
const frameworks = [
  { id: 'REACT', label: 'React', desc: 'Modern component-based UI library', color: 'from-sky-500/20 to-blue-600/10', border: 'border-sky-400/25' },
  { id: 'NEXTJS', label: 'Next.js', desc: 'Full-stack React with SSR & routing', color: 'from-slate-400/15 to-slate-600/10', border: 'border-slate-400/20' },
  { id: 'VUE', label: 'Vue', desc: 'Progressive & approachable framework', color: 'from-emerald-500/15 to-green-600/10', border: 'border-emerald-400/20' },
  { id: 'ANGULAR', label: 'Angular', desc: 'Enterprise-grade TypeScript framework', color: 'from-red-500/15 to-rose-600/10', border: 'border-red-400/20' },
  { id: 'SVELTE', label: 'SvelteKit', desc: 'Compiler-first with minimal boilerplate', color: 'from-orange-500/15 to-red-500/10', border: 'border-orange-400/20' },
  { id: 'ASTRO', label: 'Astro', desc: 'Content-first with component islands', color: 'from-purple-500/15 to-pink-500/10', border: 'border-purple-400/20' },
  { id: 'SOLID', label: 'SolidJS', desc: 'Fine-grained reactivity, no virtual DOM', color: 'from-blue-600/15 to-indigo-600/10', border: 'border-blue-500/20' },
  { id: 'REMIX', label: 'Remix', desc: 'Full-stack React with nested routing', color: 'from-slate-500/15 to-zinc-600/10', border: 'border-slate-400/20' },
  { id: 'VANILLA', label: 'Vanilla', desc: 'Plain HTML, CSS & JavaScript', color: 'from-yellow-500/15 to-orange-500/10', border: 'border-yellow-400/20' },
];

/* ── animation helpers ──────────────────────────────────────── */
const slideIn = { initial: { opacity: 0, x: 40 }, animate: { opacity: 1, x: 0 }, exit: { opacity: 0, x: -40 } };
const fadePop = { initial: { scale: 0.92, opacity: 0 }, animate: { scale: 1, opacity: 1 }, exit: { scale: 0.92, opacity: 0 } };
const springTransition = { type: 'spring' as const, stiffness: 350, damping: 30 };

export default function NewProjectPage() {
  const navigate = useNavigate();
  const { addProject } = useProjectStore();
  const [step, setStep] = useState(1);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = useState({
    name: '',
    description: '',
    type: '',
    initialPrompt: '',
    template: [] as string[],
    colorScheme: '',
    designStyle: '',
    animationStyleIds: [] as string[],
  });

  const canNext = () => {
    if (step === 1) return form.name.trim().length > 0;
    if (step === 2) return form.type !== '';
    return true;
  };

  const handleCreate = async () => {
    if (!form.name || !form.type) return;
    setIsLoading(true);
    setError('');
    try {
      // Build enhanced prompt from template + preferences + user prompt
      const parts: string[] = [];
      const selectedTemplates = templates.filter(t => form.template.includes(t.id));
      if (selectedTemplates.length === 1) {
        parts.push(selectedTemplates[0].promptContext);
      } else if (selectedTemplates.length > 1) {
        parts.push(`Build a multi-page website with the following pages as separate routes: ${selectedTemplates.map(t => t.name).join(', ')}. Each page should be its own route component.`);
        selectedTemplates.forEach(t => parts.push(`For the ${t.name} page: ${t.promptContext}`));
      }
      if (form.colorScheme) {
        const scheme = colorSchemes.find(c => c.id === form.colorScheme);
        if (scheme) parts.push(`Use a ${scheme.label} color theme.`);
      }
      if (form.designStyle) {
        const style = designStyles.find(d => d.id === form.designStyle);
        if (style) parts.push(`Use ${style.label} design style.`);
      }
      // Inject animation style prompts & collect libraries
      const extraLibs: string[] = [];
      for (const asId of form.animationStyleIds) {
        const as = animationStyles.find(a => a.id === asId);
        if (as) {
          parts.push(as.promptHint);
          extraLibs.push(...as.libraries);
        }
      }
      if (extraLibs.length > 0) {
        parts.push(`Make sure to install these npm packages: ${[...new Set(extraLibs)].join(', ')}.`);
      }
      if (form.initialPrompt) parts.push(form.initialPrompt);

      const enhancedPrompt = parts.join(' ');

      const response = await projectsApi.create({
        name: form.name,
        description: form.description,
        type: form.type,
        initialPrompt: enhancedPrompt || undefined,
      });
      addProject(response.data);
      const params = enhancedPrompt ? `?prompt=${encodeURIComponent(enhancedPrompt)}` : '';
      navigate(`/projects/${response.data.id}/generate${params}`);
    } catch (err: unknown) {
      const e = err as { response?: { data?: { message?: string } }; message?: string };
      setError(e.response?.data?.message || e.message || 'Failed to create project');
    } finally {
      setIsLoading(false);
    }
  };

  const totalSteps = 4;

  return (
    <div className="max-w-2xl mx-auto">
      {/* ── back ──────────────────────────────────────────── */}
      <motion.button
        initial={{ opacity: 0, x: -16 }}
        animate={{ opacity: 1, x: 0 }}
        onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-sm text-lilac-400/55 hover:text-lilac-200 transition-colors mb-10"
      >
        <ArrowLeft className="w-4 h-4" />
        Back
      </motion.button>

      {/* ── progress bar ──────────────────────────────────── */}
      <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="mb-10">
        <div className="flex items-center justify-between mb-2">
          <p className="text-xs text-lilac-400/50 uppercase tracking-wider font-medium">Step {step} of {totalSteps}</p>
        </div>
        <div className="h-1 rounded-full bg-lilac-200/8 overflow-hidden">
          <motion.div
            className="h-full rounded-full bg-gradient-to-r from-mauve-500 to-mauve-400"
            animate={{ width: `${(step / totalSteps) * 100}%` }}
            transition={{ duration: 0.4, ease: 'easeInOut' }}
          />
        </div>
      </motion.div>

      {/* ── step content ──────────────────────────────────── */}
      <AnimatePresence mode="wait">
        {step === 1 && (
          <motion.div key="s1" {...slideIn} transition={springTransition}>
            <h1 className="text-2xl font-bold text-lilac-100 mb-1">Name your project</h1>
            <p className="text-sm text-lilac-300/50 mb-8">A great name sets the tone for everything you build.</p>

            <div className="space-y-4">
              <Input
                label="Project Name"
                placeholder="e.g. Portfolio Redesign"
                value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
                autoFocus
              />
              <Input
                label="Description"
                placeholder="Optional — what's this project about?"
                value={form.description}
                onChange={(e) => setForm({ ...form, description: e.target.value })}
              />
            </div>
          </motion.div>
        )}

        {step === 2 && (
          <motion.div key="s2" {...slideIn} transition={springTransition}>
            <h1 className="text-2xl font-bold text-lilac-100 mb-1">Choose a framework</h1>
            <p className="text-sm text-lilac-300/50 mb-8">Pick the technology stack for your app.</p>

            <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
              {frameworks.map((fw) => {
                const selected = form.type === fw.id;
                return (
                  <motion.button
                    key={fw.id}
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.97 }}
                    onClick={() => setForm({ ...form, type: fw.id })}
                    className={cn(
                      'relative p-4 rounded-[18px] text-left transition-all duration-200 border',
                      selected
                        ? `bg-gradient-to-br ${fw.color} ${fw.border} ring-1 ring-mauve-400/20`
                        : 'border-lilac-200/8 hover:border-lilac-200/18 bg-lilac-200/[0.03]'
                    )}
                  >
                    <div className="flex items-center gap-2.5 mb-1.5">
                      <FrameworkIcon framework={fw.id} size={22} />
                      <span className="font-semibold text-sm text-lilac-100">{fw.label}</span>
                    </div>
                    <p className="text-[11px] text-lilac-300/50 leading-relaxed">{fw.desc}</p>
                    {selected && (
                      <motion.div {...fadePop} className="absolute top-3 right-3 w-5 h-5 rounded-full bg-mauve-500 flex items-center justify-center">
                        <Check className="w-3 h-3 text-white" />
                      </motion.div>
                    )}
                  </motion.button>
                );
              })}
            </div>
          </motion.div>
        )}

        {step === 3 && (
          <motion.div key="s3" {...slideIn} transition={springTransition}>
            <div className="flex items-center justify-between mb-1">
              <h1 className="text-2xl font-bold text-lilac-100">Pick your pages</h1>
              <button
                onClick={() => { setForm({ ...form, template: [], colorScheme: '', designStyle: '', animationStyleIds: [] }); setStep(4); }}
                className="flex items-center gap-1.5 text-xs text-lilac-400/50 hover:text-mauve-300 transition-colors"
              >
                <SkipForward className="w-3.5 h-3.5" />
                Skip
              </button>
            </div>
            <p className="text-sm text-lilac-300/50 mb-6">Select one or more page types to build a multi-page website, or skip to start from scratch.</p>

            {/* Template grid */}
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 mb-8">
              {templates.map((tmpl) => {
                const selected = form.template.includes(tmpl.id);
                const Icon = tmpl.icon;
                return (
                  <motion.button
                    key={tmpl.id}
                    whileHover={{ scale: 1.02 }}
                    whileTap={{ scale: 0.97 }}
                    onClick={() => setForm({
                      ...form,
                      template: selected
                        ? form.template.filter(id => id !== tmpl.id)
                        : [...form.template, tmpl.id],
                    })}
                    className={cn(
                      'relative p-4 rounded-[18px] text-left transition-all duration-200 border',
                      selected
                        ? `bg-gradient-to-br ${tmpl.color} border-mauve-400/25 ring-1 ring-mauve-400/20`
                        : 'border-lilac-200/8 hover:border-lilac-200/18 bg-lilac-200/[0.03]'
                    )}
                  >
                    <div className="flex items-center gap-2.5 mb-1.5">
                      <div className={cn(
                        'w-7 h-7 rounded-lg flex items-center justify-center',
                        selected ? 'bg-mauve-500/20' : 'bg-lilac-200/8'
                      )}>
                        <Icon className={cn('w-3.5 h-3.5', selected ? 'text-mauve-300' : 'text-lilac-400/50')} />
                      </div>
                      <span className="font-semibold text-sm text-lilac-100">{tmpl.name}</span>
                    </div>
                    <p className="text-[11px] text-lilac-300/50 leading-relaxed">{tmpl.description}</p>
                    {selected && (
                      <motion.div {...fadePop} className="absolute top-3 right-3 w-5 h-5 rounded-full bg-mauve-500 flex items-center justify-center">
                        <Check className="w-3 h-3 text-white" />
                      </motion.div>
                    )}
                  </motion.button>
                );
              })}
            </div>

            {/* Design preferences */}
            <div className="space-y-5">
              <div>
                <div className="flex items-center gap-2 mb-3">
                  <Palette className="w-3.5 h-3.5 text-lilac-400/40" />
                  <p className="text-xs text-lilac-400/50 uppercase tracking-wider font-medium">Color Scheme</p>
                </div>
                <div className="flex flex-wrap gap-2">
                  {colorSchemes.map((scheme) => (
                    <button
                      key={scheme.id}
                      onClick={() => setForm({ ...form, colorScheme: form.colorScheme === scheme.id ? '' : scheme.id })}
                      className={cn(
                        'flex items-center gap-2 px-3 py-1.5 rounded-full text-[11px] font-medium transition-all border',
                        form.colorScheme === scheme.id
                          ? 'bg-mauve-500/15 border-mauve-400/25 text-mauve-200'
                          : 'border-lilac-200/8 text-lilac-400/50 hover:border-lilac-200/18 hover:text-lilac-300'
                      )}
                    >
                      <div className="flex gap-0.5">
                        {scheme.colors.map((c, i) => (
                          <div key={i} className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: c }} />
                        ))}
                      </div>
                      {scheme.label}
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <p className="text-xs text-lilac-400/50 uppercase tracking-wider font-medium mb-3">Design Style</p>
                <div className="flex flex-wrap gap-2">
                  {designStyles.map((style) => (
                    <button
                      key={style.id}
                      onClick={() => setForm({ ...form, designStyle: form.designStyle === style.id ? '' : style.id })}
                      className={cn(
                        'px-3 py-1.5 rounded-full text-[11px] font-medium transition-all border',
                        form.designStyle === style.id
                          ? 'bg-mauve-500/15 border-mauve-400/25 text-mauve-200'
                          : 'border-lilac-200/8 text-lilac-400/50 hover:border-lilac-200/18 hover:text-lilac-300'
                      )}
                    >
                      {style.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Animation & Effects (multi-select) */}
              <div>
                <p className="text-xs text-lilac-400/50 uppercase tracking-wider font-medium mb-3">Animation & Effects</p>
                <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
                  {animationStyles.map((anim) => {
                    const isSelected = form.animationStyleIds.includes(anim.id);
                    const AnimIcon = anim.icon;
                    return (
                      <button
                        key={anim.id}
                        onClick={() => setForm({
                          ...form,
                          animationStyleIds: isSelected
                            ? form.animationStyleIds.filter(id => id !== anim.id)
                            : [...form.animationStyleIds, anim.id],
                        })}
                        className={cn(
                          'relative p-3 rounded-2xl text-left transition-all duration-200 border',
                          isSelected
                            ? `bg-gradient-to-br ${anim.color} border-mauve-400/25 ring-1 ring-mauve-400/15`
                            : 'border-lilac-200/8 hover:border-lilac-200/18 bg-lilac-200/[0.03]'
                        )}
                      >
                        <div className="flex items-center gap-2 mb-1">
                          <AnimIcon className={cn('w-3.5 h-3.5', isSelected ? 'text-mauve-300' : 'text-lilac-400/50')} />
                          <span className="font-semibold text-[11px] text-lilac-100">{anim.label}</span>
                        </div>
                        <p className="text-[10px] text-lilac-300/45 leading-relaxed">{anim.description}</p>
                        {anim.libraries.length > 0 && (
                          <p className="text-[9px] text-mauve-400/50 mt-1">{anim.libraries.join(', ')}</p>
                        )}
                        {isSelected && (
                          <motion.div {...fadePop} className="absolute top-2 right-2 w-4 h-4 rounded-full bg-mauve-500 flex items-center justify-center">
                            <Check className="w-2.5 h-2.5 text-white" />
                          </motion.div>
                        )}
                      </button>
                    );
                  })}
                </div>
              </div>
            </div>
          </motion.div>
        )}

        {step === 4 && (
          <motion.div key="s4" {...slideIn} transition={springTransition}>
            <h1 className="text-2xl font-bold text-lilac-100 mb-1">Describe what to build</h1>
            <p className="text-sm text-lilac-300/50 mb-8">
              {form.template
                ? 'Customize the template with your specific requirements, or leave blank for defaults.'
                : 'Give AI a prompt to scaffold your app — or leave blank for a bare project.'}
            </p>

            <GlassCard className="p-5 mb-5">
              <textarea
                value={form.initialPrompt}
                onChange={(e) => setForm({ ...form, initialPrompt: e.target.value })}
                placeholder={form.template
                  ? `e.g. Use blue and purple colors, add a testimonials carousel…`
                  : `e.g. A modern SaaS dashboard with a sidebar, charts, and a settings page…`}
                rows={5}
                className="w-full bg-transparent text-sm text-lilac-100 placeholder:text-lilac-400/30 outline-none resize-none leading-relaxed"
              />
            </GlassCard>

            {/* Summary */}
            <GlassCard variant="subtle" className="p-4">
              <p className="text-xs text-lilac-400/45 uppercase tracking-wider font-medium mb-3">Summary</p>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-lilac-400/55">Name</span>
                  <span className="text-lilac-100 font-medium">{form.name}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-lilac-400/55">Framework</span>
                  <span className="text-lilac-100 font-medium">{frameworks.find((f) => f.id === form.type)?.label}</span>
                </div>
                {form.template.length > 0 && (
                  <div className="flex justify-between">
                    <span className="text-lilac-400/55">Pages</span>
                    <span className="text-lilac-100 font-medium">{templates.filter(t => form.template.includes(t.id)).map(t => t.name).join(', ')}</span>
                  </div>
                )}
                {form.colorScheme && (
                  <div className="flex justify-between">
                    <span className="text-lilac-400/55">Colors</span>
                    <span className="text-lilac-100 font-medium">{colorSchemes.find(c => c.id === form.colorScheme)?.label}</span>
                  </div>
                )}
                {form.designStyle && (
                  <div className="flex justify-between">
                    <span className="text-lilac-400/55">Style</span>
                    <span className="text-lilac-100 font-medium">{designStyles.find(d => d.id === form.designStyle)?.label}</span>
                  </div>
                )}
                {form.description && (
                  <div className="flex justify-between">
                    <span className="text-lilac-400/55">Description</span>
                    <span className="text-lilac-100 font-medium truncate max-w-[200px]">{form.description}</span>
                  </div>
                )}
              </div>
            </GlassCard>

            {error && (
              <motion.p initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="text-xs text-red-400 mt-3">
                {error}
              </motion.p>
            )}
          </motion.div>
        )}
      </AnimatePresence>

      {/* ── nav buttons ───────────────────────────────────── */}
      <div className="flex items-center justify-between mt-10">
        <Button
          variant="secondary"
          onClick={() => setStep((s) => Math.max(1, s - 1))}
          disabled={step === 1}
          leftIcon={<ArrowLeft className="w-4 h-4" />}
        >
          Back
        </Button>

        {step < totalSteps ? (
          <Button
            variant="primary"
            onClick={() => setStep((s) => s + 1)}
            disabled={!canNext()}
            rightIcon={<ArrowRight className="w-4 h-4" />}
          >
            Continue
          </Button>
        ) : (
          <Button
            variant="primary"
            onClick={handleCreate}
            disabled={!canNext()}
            isLoading={isLoading}
            rightIcon={<Sparkles className="w-4 h-4" />}
          >
            Create Project
          </Button>
        )}
      </div>
    </div>
  );
}
