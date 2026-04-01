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
        promptContext: 'Build a modern landing page with: a full-width gradient hero section with headline, subtext and CTA button; a features grid with icon cards (at least 6 features); a testimonials section with user avatars; a pricing section with 3 tiers; and a footer with social links and navigation columns.',
        tags: ['marketing', 'startup'],
    },
    {
        id: 'dashboard',
        name: 'Dashboard',
        description: 'Sidebar, stats, charts & data tables',
        icon: BarChart3,
        color: 'from-emerald-500/20 to-teal-600/15',
        promptContext: 'Build an analytics dashboard with: a collapsible sidebar with navigation links and user avatar; a top metric cards row showing key stats (revenue, users, growth, etc.) with trend indicators; a main chart area with a line/bar chart; a recent activity table with sortable columns; and a clean dark theme.',
        tags: ['analytics', 'admin'],
    },
    {
        id: 'ecommerce',
        name: 'E-Commerce',
        description: 'Product grid, cart, detail & checkout',
        icon: ShoppingBag,
        color: 'from-amber-500/20 to-orange-600/15',
        promptContext: 'Build an e-commerce storefront with: a top navigation bar with search, cart icon with item count; a product grid with image, title, price, and add-to-cart button for each product; a product detail page with image gallery, description, size/color selectors; a slide-out cart sidebar; and a responsive layout.',
        tags: ['store', 'products'],
    },
    {
        id: 'portfolio',
        name: 'Portfolio',
        description: 'Project gallery, about & contact form',
        icon: Image,
        color: 'from-pink-500/20 to-rose-600/15',
        promptContext: 'Build a creative portfolio website with: a minimal hero section with name and role; a filterable project gallery with hover effects; an about section with skills/tech stack; a contact form with name/email/message fields; smooth scroll navigation; and elegant typography.',
        tags: ['personal', 'creative'],
    },
    {
        id: 'blog',
        name: 'Blog',
        description: 'Post list, detail, categories & search',
        icon: BookOpen,
        color: 'from-cyan-500/20 to-blue-600/15',
        promptContext: 'Build a blog with: a header with site title and navigation; a featured post hero; a post list with thumbnail image, title, excerpt, date, and read-time; category filter tabs; a search bar; a post detail page with rich text content, author info, and related posts; and a newsletter signup section.',
        tags: ['content', 'writing'],
    },
    {
        id: 'saas',
        name: 'SaaS App',
        description: 'Pricing, features & auth pages',
        icon: Briefcase,
        color: 'from-blue-500/20 to-indigo-600/15',
        promptContext: 'Build a SaaS marketing site with: a hero with app screenshot mockup and CTA; a feature comparison grid with check/cross indicators; a pricing table with 3 tiers (Free/Pro/Enterprise) with toggle for monthly/annual; an FAQ accordion section; social proof with logos; and a sticky header navigation.',
        tags: ['product', 'business'],
    },
    {
        id: 'admin',
        name: 'Admin Panel',
        description: 'CRUD tables, forms & user management',
        icon: UserCog,
        color: 'from-slate-500/20 to-gray-600/15',
        promptContext: 'Build an admin panel with: a sidebar with collapsible menu items (Dashboard, Users, Products, Settings); a data table with search, sort, pagination, and row selection; a create/edit form with validation; a user management page with role badges; breadcrumb navigation; and toast notifications.',
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

