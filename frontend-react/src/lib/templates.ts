import {
    Layout,
    ShoppingBag,
    BarChart3,
    BookOpen,
    Briefcase,
    Image,
    UserCog,
    Wand2,
    ArrowDown,
    Layers,
    Box,
    Minus,
    type LucideIcon,
} from 'lucide-react';

export interface ProjectTemplate {
    id: string;
    name: string;
    description: string;
    icon: LucideIcon;
    color: string;
    promptContext: string;
    tags: string[];
}

export interface AnimationStyle {
    id: string;
    label: string;
    description: string;
    icon: LucideIcon;
    libraries: string[];
    promptHint: string;
    color: string;
}

export const templates: ProjectTemplate[] = [
    {
        id: 'landing',
        name: 'Landing Page',
        description: 'Hero, features, testimonials, CTA & footer',
        icon: Layout,
        color: 'from-violet-500/20 to-indigo-600/15',
        promptContext: 'Build an ultra-modern, high-end landing page. Include: a stunning full-width gradient hero section with an immersive glowing background, bold typography headline, subtext, and a sleek animated CTA button. Add a premium features grid using frosted glass cards with glowing borders and beautiful icons (at least 6 features). Add an elegant testimonials section with avatar images and subtle hover depth. Include a clear, beautiful pricing section (3 tiers) with an emphasized tier. Finish with a minimalist footer with social links. The design must be flawlessly responsive, using generous whitespace, perfect typography, and a polished, premium aesthetic that rivals top tech company sites.',
        tags: ['marketing', 'startup'],
    },
    {
        id: 'dashboard',
        name: 'Dashboard',
        description: 'Sidebar, stats, charts & data tables',
        icon: BarChart3,
        color: 'from-emerald-500/20 to-teal-600/15',
        promptContext: 'Build a sophisticated, next-generation analytics dashboard. Features: a sleek, collapsible sidebar with glowing active states and user profile. A top row of metric widgets featuring glassmorphism backgrounds, subtle drop shadows, and mini sparkline trend indicators. A gorgeous main chart area with a smooth line or bar chart. A recent activity data table with sortable columns, perfect alignment, and pill-shaped status badges. The entire layout must feel incredibly premium, using a dark theme with neon accents, ultra-refined borders, and smooth transitions on all interactive elements.',
        tags: ['analytics', 'admin'],
    },
    {
        id: 'ecommerce',
        name: 'E-Commerce',
        description: 'Product grid, cart, detail & checkout',
        icon: ShoppingBag,
        color: 'from-amber-500/20 to-orange-600/15',
        promptContext: 'Build a luxurious, high-performance e-commerce storefront. Must include: an elegant sticky top navigation with search and a cart badge. A stunning product grid showcasing high-quality product cards with subtle image zoom on hover, minimal pricing text, and a sleek primary add-to-cart button. A beautifully laid out product detail view featuring an expansive image gallery, refined typography for descriptions, and chic size/color interactable selectors. Include a slide-out cart sidebar with smooth enter/exit animations. The overall vibe should be minimalist but highly engaging, similar to premium fashion or tech brand sites.',
        tags: ['store', 'products'],
    },
    {
        id: 'portfolio',
        name: 'Portfolio',
        description: 'Project gallery, about & contact form',
        icon: Image,
        color: 'from-pink-500/20 to-rose-600/15',
        promptContext: 'Build an award-winning, creative developer/designer portfolio. Include: a minimalist hero section focusing on a massive, elegant headline and the creator\'s role with subtle gradient text. A visually arresting, filterable project gallery utilizing masonry layout and beautiful hover reveal effects for project details. A clean about section displaying skills inside pill-shaped tags or animated bars. A sleek contact form with floating labels and an elegant submit button. Navigation should use smooth scrolling. Ensure impeccable typography, lots of negative space, and absolute pixel perfection.',
        tags: ['personal', 'creative'],
    },
    {
        id: 'blog',
        name: 'Blog',
        description: 'Post list, detail, categories & search',
        icon: BookOpen,
        color: 'from-cyan-500/20 to-blue-600/15',
        promptContext: 'Build a premium editorial blog platform. Features: a pristine header with site branding and navigation. A featured post hero component with an immersive dark overlay on the cover image and striking typography. A grid of recent posts where each article card has a gorgeous hover state lifting the card and highlighting the title. Include inline category pills, elegant author avatars, dates, and read-time indicators. Add a beautifully styled search bar and category filter tabs. The post detail page must focus heavily on readability with optimal line-height, beautiful blockquotes, and elegantly spaced typography.',
        tags: ['content', 'writing'],
    },
    {
        id: 'saas',
        name: 'SaaS App',
        description: 'Pricing, features & auth pages',
        icon: Briefcase,
        color: 'from-blue-500/20 to-indigo-600/15',
        promptContext: 'Build a top-tier SaaS product marketing site. Essential elements: a powerful hero section that displays a floating, perspective-tilted mockup of the app UI. A stunning feature comparison grid with vibrant check/cross icons. A highly persuasive pricing table with 3 tiers (Free/Pro/Enterprise) complete with a sleek monthly/annual toggle pill. A refined FAQ accordion section with smooth height transitions. Add social proof via grayscale client logos that become colored on hover. The aesthetic must be exceptionally clean, modern, and trust-inspiring.',
        tags: ['product', 'business'],
    },
    {
        id: 'admin',
        name: 'Admin Panel',
        description: 'CRUD tables, forms & user management',
        icon: UserCog,
        color: 'from-slate-500/20 to-gray-600/15',
        promptContext: 'Build a cutting-edge admin/internal tool interface. Must include: an ultra-clean sidebar navigation system. A robust data table utilizing minimal borders, alternating row subtle backgrounds, and action dropdown menus. Gorgeous form inputs with focus rings matching the brand accent color. A user management view displaying avatar images and role badges (like "Admin", "Editor"). Make sure to include breadcrumb navigation and floating toast notifications. Prioritize a pristine, noise-free layout that reduces cognitive load, heavily utilizing subtle grays for borders and vibrant colors exclusively for actions.',
        tags: ['management', 'internal'],
    },
];

/* ── Animation / Design style preferences ── */
export const animationStyles: AnimationStyle[] = [
    {
        id: 'smooth-animations',
        label: 'Smooth Animations',
        description: 'Fluid page transitions & micro-interactions',
        icon: Wand2,
        libraries: ['framer-motion'],
        promptHint: 'Use Framer Motion for all animations: page transitions with AnimatePresence, hover scale/glow effects, staggered list reveals with variants, and smooth micro-interactions. Wrap pages in motion.div with fade/slide transitions.',
        color: 'from-purple-500/20 to-violet-600/15',
    },
    {
        id: 'scroll-animations',
        label: 'Scroll Animations',
        description: 'Elements animate as you scroll down the page',
        icon: ArrowDown,
        libraries: ['gsap'],
        promptHint: 'Use GSAP with ScrollTrigger for scroll-driven animations: parallax backgrounds, fade-in-up on scroll, pinned hero sections, horizontal scroll galleries, and staggered card reveals as they enter the viewport.',
        color: 'from-green-500/20 to-emerald-600/15',
    },
    {
        id: 'glass-design',
        label: 'Glass Design',
        description: 'Frosted glass cards & translucent overlays',
        icon: Layers,
        libraries: [],
        promptHint: 'Use glassmorphism design throughout: backdrop-blur-xl, semi-transparent backgrounds (bg-white/5 dark:bg-white/10), subtle 1px borders (border-white/10), frosted glass cards and navbars, layered depth with shadows. Dark rich backgrounds with glass overlays.',
        color: 'from-sky-500/20 to-cyan-600/15',
    },
    {
        id: '3d-effects',
        label: '3D & Depth',
        description: 'Perspective transforms, parallax, depth layers',
        icon: Box,
        libraries: [],
        promptHint: 'Use CSS 3D transforms and perspective for depth: card tilt on hover with preserve-3d, parallax mouse-follow effects, layered z-depth with shadows, perspective grid backgrounds, and transform-style preserve-3d on containers.',
        color: 'from-amber-500/20 to-orange-600/15',
    },
    {
        id: 'minimal-clean',
        label: 'Minimal & Clean',
        description: 'Focus on typography, whitespace & content',
        icon: Minus,
        libraries: [],
        promptHint: 'Keep the design minimal and clean. Focus on beautiful typography, generous whitespace, content hierarchy, and subtle hover states. No heavy animations — only gentle opacity/color transitions on interactive elements.',
        color: 'from-slate-500/20 to-gray-600/15',
    },
];

/* ── Design preferences ── */
export const colorSchemes = [
    { id: 'dark-modern', label: 'Modern Dark', colors: ['#0f172a', '#1e293b', '#7c3aed', '#06b6d4'] },
    { id: 'vibrant', label: 'Vibrant', colors: ['#1a1a2e', '#16213e', '#e94560', '#0f3460'] },
    { id: 'minimal-light', label: 'Minimal Light', colors: ['#ffffff', '#f8fafc', '#1e293b', '#3b82f6'] },
    { id: 'corporate', label: 'Corporate Blue', colors: ['#0f172a', '#1e3a5f', '#3b82f6', '#60a5fa'] },
    { id: 'warm', label: 'Warm Sunset', colors: ['#1c1917', '#292524', '#f97316', '#fbbf24'] },
    { id: 'forest', label: 'Forest Green', colors: ['#0a1f0a', '#14532d', '#22c55e', '#86efac'] },
];

export const designStyles = [
    { id: 'glassmorphism', label: 'Glassmorphism' },
    { id: 'flat', label: 'Flat / Minimal' },
    { id: 'neumorphism', label: 'Neumorphism' },
    { id: 'material', label: 'Material Design' },
    { id: 'brutalist', label: 'Brutalist' },
];

