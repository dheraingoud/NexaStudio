package com.nexastudio.generator;

import com.nexastudio.project.FileEntity;
import com.nexastudio.project.ProjectEntity;
import com.nexastudio.project.ProjectEntity.ProjectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Framework Scaffold Service.
 * Creates complete initial file structures for different project types.
 * Supports Next.js, React (Vite), Vue, and Angular projects.
 */
@Service
public class FrameworkScaffoldService {

  private static final Logger log = LoggerFactory.getLogger(FrameworkScaffoldService.class);

  /**
   * Create scaffold files based on project type
   */
  public List<FileEntity> createScaffold(ProjectEntity project) {
    ProjectType type = project.getType();
    if (type == null)
      type = ProjectType.NEXTJS;

    log.info("Creating {} scaffold for project: {}", type, project.getId());

    return switch (type) {
      case REACT -> createReactScaffold(project);
      case VUE -> createVueScaffold(project);
      case ANGULAR -> createAngularScaffold(project);
      case SVELTE -> createSvelteScaffold(project);
      case ASTRO -> createAstroScaffold(project);
      case SOLID -> createSolidScaffold(project);
      case REMIX -> createRemixScaffold(project);
      case VANILLA -> createVanillaScaffold(project);
      default -> createNextJsScaffold(project);
    };
  }

  /*
   * ═══════════════════════════════════════════════════════════════════
   * Next.js Scaffold
   * ═══════════════════════════════════════════════════════════════════
   */

  private List<FileEntity> createNextJsScaffold(ProjectEntity project) {
    List<FileEntity> files = new ArrayList<>();
    String name = project.getName();

    files.add(createFile(project, "/package.json", getNextJsPackageJson(name)));
    files.add(createFile(project, "/tsconfig.json", getNextJsTsConfig()));
    files.add(createFile(project, "/next.config.ts", getNextJsConfig()));
    files.add(createFile(project, "/tailwind.config.ts", getNextJsTailwindConfig()));
    files.add(createFile(project, "/postcss.config.mjs", getPostCssConfig()));
    files.add(createFile(project, "/app/globals.css", getNextJsGlobalCss()));
    files.add(createFile(project, "/app/layout.tsx", getNextJsLayout(name)));
    files.add(createFile(project, "/app/page.tsx", getNextJsPage()));
    files.add(createFile(project, "/components/ui/button.tsx", getNextJsButton()));
    files.add(createFile(project, "/lib/utils.ts", getUtilsTs()));
    files.add(createFile(project, "/.gitignore", getGitignore()));
    files.add(createFile(project, "/README.md", getReadme(name, "Next.js")));

    log.info("Created {} Next.js scaffold files", files.size());
    return files;
  }

  /*
   * ═══════════════════════════════════════════════════════════════════
   * React (Vite) Scaffold
   * ═══════════════════════════════════════════════════════════════════
   */

  private List<FileEntity> createReactScaffold(ProjectEntity project) {
    List<FileEntity> files = new ArrayList<>();
    String name = project.getName();

    files.add(createFile(project, "/package.json", getReactPackageJson(name)));
    files.add(createFile(project, "/tsconfig.json", getReactTsConfig()));
    files.add(createFile(project, "/tsconfig.node.json", getReactTsConfigNode()));
    files.add(createFile(project, "/vite.config.ts", getViteConfig()));
    files.add(createFile(project, "/tailwind.config.js", getReactTailwindConfig()));
    files.add(createFile(project, "/postcss.config.js", getReactPostCssConfig()));
    files.add(createFile(project, "/index.html", getReactIndexHtml(name)));
    files.add(createFile(project, "/src/main.tsx", getReactMain()));
    files.add(createFile(project, "/src/App.tsx", getReactApp()));
    files.add(createFile(project, "/src/index.css", getReactGlobalCss()));
    files.add(createFile(project, "/src/components/ui/Button.tsx", getReactButton()));
    files.add(createFile(project, "/src/lib/utils.ts", getUtilsTs()));
    files.add(createFile(project, "/src/vite-env.d.ts", getViteEnvDts()));
    files.add(createFile(project, "/.gitignore", getGitignore()));
    files.add(createFile(project, "/README.md", getReadme(name, "React")));

    log.info("Created {} React scaffold files", files.size());
    return files;
  }

  /*
   * ═══════════════════════════════════════════════════════════════════
   * Vue Scaffold
   * ═══════════════════════════════════════════════════════════════════
   */

  private List<FileEntity> createVueScaffold(ProjectEntity project) {
    List<FileEntity> files = new ArrayList<>();
    String name = project.getName();

    files.add(createFile(project, "/package.json", getVuePackageJson(name)));
    files.add(createFile(project, "/tsconfig.json", getVueTsConfig()));
    files.add(createFile(project, "/vite.config.ts", getVueViteConfig()));
    files.add(createFile(project, "/tailwind.config.js", getReactTailwindConfig()));
    files.add(createFile(project, "/postcss.config.js", getReactPostCssConfig()));
    files.add(createFile(project, "/index.html", getVueIndexHtml(name)));
    files.add(createFile(project, "/src/main.ts", getVueMain()));
    files.add(createFile(project, "/src/App.vue", getVueApp()));
    files.add(createFile(project, "/src/style.css", getReactGlobalCss()));
    files.add(createFile(project, "/src/components/HelloWorld.vue", getVueHelloWorld()));
    files.add(createFile(project, "/src/vite-env.d.ts", getVueEnvDts()));
    files.add(createFile(project, "/.gitignore", getGitignore()));
    files.add(createFile(project, "/README.md", getReadme(name, "Vue")));

    log.info("Created {} Vue scaffold files", files.size());
    return files;
  }

  /*
   * ═══════════════════════════════════════════════════════════════════
   * Angular Scaffold
   * ═══════════════════════════════════════════════════════════════════
   */

  private List<FileEntity> createAngularScaffold(ProjectEntity project) {
    List<FileEntity> files = new ArrayList<>();
    String name = project.getName();

    files.add(createFile(project, "/package.json", getAngularPackageJson(name)));
    files.add(createFile(project, "/tsconfig.json", getAngularTsConfig()));
    files.add(createFile(project, "/angular.json", getAngularJson(name)));
    files.add(createFile(project, "/tailwind.config.js", getAngularTailwindConfig()));
    files.add(createFile(project, "/src/main.ts", getAngularMain()));
    files.add(createFile(project, "/src/index.html", getAngularIndexHtml(name)));
    files.add(createFile(project, "/src/styles.css", getReactGlobalCss()));
    files.add(createFile(project, "/src/app/app.component.ts", getAngularAppComponent()));
    files.add(createFile(project, "/src/app/app.config.ts", getAngularAppConfig()));
    files.add(createFile(project, "/.gitignore", getGitignore()));
    files.add(createFile(project, "/README.md", getReadme(name, "Angular")));

    log.info("Created {} Angular scaffold files", files.size());
    return files;
  }

  /*
   * ═══════════════════════════════════════════════════════════════════
   * Svelte (SvelteKit) Scaffold
   * ═══════════════════════════════════════════════════════════════════
   */

  private List<FileEntity> createSvelteScaffold(ProjectEntity project) {
    List<FileEntity> files = new ArrayList<>();
    String name = project.getName();

    files.add(createFile(project, "/package.json", """
        {
          "name": "%s",
          "version": "0.1.0",
          "private": true,
          "scripts": {
            "dev": "vite dev",
            "build": "vite build",
            "preview": "vite preview"
          },
          "devDependencies": {
            "@sveltejs/adapter-auto": "^3.0.0",
            "@sveltejs/kit": "^2.0.0",
            "svelte": "^4.2.0",
            "svelte-check": "^3.6.0",
            "typescript": "^5.3.0",
            "vite": "^5.4.0",
            "autoprefixer": "^10.4.17",
            "postcss": "^8.4.33",
            "tailwindcss": "^3.4.1"
          }
        }
        """.formatted(safeName(name))));
    files.add(createFile(project, "/svelte.config.js", """
        import adapter from '@sveltejs/adapter-auto';
        import { vitePreprocess } from '@sveltejs/vite-plugin-svelte';

        /** @type {import('@sveltejs/kit').Config} */
        const config = {
          preprocess: vitePreprocess(),
          kit: { adapter: adapter() }
        };

        export default config;
        """));
    files.add(createFile(project, "/tailwind.config.js", """
        /** @type {import('tailwindcss').Config} */
        export default {
          content: ['./src/**/*.{html,js,svelte,ts}'],
          theme: { extend: {} },
          plugins: [],
        };
        """));
    files.add(createFile(project, "/postcss.config.js", getReactPostCssConfig()));
    files.add(createFile(project, "/src/app.css", getReactGlobalCss()));
    files.add(createFile(project, "/src/app.html", """
        <!doctype html>
        <html lang="en">
          <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1" />
            <title>%s</title>
            %%sveltekit.head%%
          </head>
          <body data-sveltekit-prerender="true">
            <div style="display: contents">%%sveltekit.body%%</div>
          </body>
        </html>
        """.formatted(name)));
    files.add(createFile(project, "/src/routes/+layout.svelte", """
        <script lang="ts">
          import '../app.css';
        </script>

        <slot />
        """));
    files.add(createFile(project, "/src/routes/+page.svelte",
        """
            <main class="flex min-h-screen flex-col items-center justify-center p-24 bg-gradient-to-b from-zinc-900 to-black">
              <div class="text-center space-y-6">
                <h1 class="text-5xl font-bold text-white">Welcome to Your App</h1>
                <p class="text-xl text-zinc-400 max-w-lg">
                  Start building amazing things with AI-powered code generation
                </p>
                <div class="flex gap-4 justify-center pt-4">
                  <button class="h-11 px-6 bg-white text-black rounded-lg font-medium hover:bg-zinc-200 transition-colors">
                    Get Started
                  </button>
                  <button class="h-11 px-6 border border-zinc-700 text-white rounded-lg font-medium hover:bg-zinc-800 transition-colors">
                    Learn More
                  </button>
                </div>
              </div>
            </main>
            """));
    files.add(createFile(project, "/.gitignore", getGitignore()));
    files.add(createFile(project, "/README.md", getReadme(name, "SvelteKit")));

    log.info("Created {} SvelteKit scaffold files", files.size());
    return files;
  }

  /*
   * ═══════════════════════════════════════════════════════════════════
   * Astro Scaffold
   * ═══════════════════════════════════════════════════════════════════
   */

  private List<FileEntity> createAstroScaffold(ProjectEntity project) {
    List<FileEntity> files = new ArrayList<>();
    String name = project.getName();

    files.add(createFile(project, "/package.json", """
        {
          "name": "%s",
          "version": "0.1.0",
          "private": true,
          "scripts": {
            "dev": "astro dev",
            "build": "astro build",
            "preview": "astro preview"
          },
          "dependencies": {
            "astro": "^4.0.0",
            "@astrojs/tailwind": "^5.1.0",
            "tailwindcss": "^3.4.1"
          },
          "devDependencies": {
            "typescript": "^5.3.0"
          }
        }
        """.formatted(safeName(name))));
    files.add(createFile(project, "/astro.config.mjs", """
        import { defineConfig } from 'astro/config';
        import tailwind from '@astrojs/tailwind';

        export default defineConfig({
          integrations: [tailwind()],
        });
        """));
    files.add(createFile(project, "/tsconfig.json", """
        {
          "extends": "astro/tsconfigs/strict"
        }
        """));
    files.add(createFile(project, "/src/layouts/Layout.astro", """
        ---
        interface Props { title: string; }
        const { title } = Astro.props;
        ---
        <!doctype html>
        <html lang="en">
          <head>
            <meta charset="UTF-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <title>{title}</title>
          </head>
          <body>
            <slot />
          </body>
        </html>
        """));
    files.add(createFile(project, "/src/pages/index.astro",
        """
            ---
            import Layout from '../layouts/Layout.astro';
            ---
            <Layout title="%s">
              <main class="flex min-h-screen flex-col items-center justify-center p-24 bg-gradient-to-b from-zinc-900 to-black">
                <div class="text-center space-y-6">
                  <h1 class="text-5xl font-bold text-white">Welcome to Your App</h1>
                  <p class="text-xl text-zinc-400 max-w-lg">
                    Start building amazing things with AI-powered code generation
                  </p>
                  <div class="flex gap-4 justify-center pt-4">
                    <button class="h-11 px-6 bg-white text-black rounded-lg font-medium hover:bg-zinc-200 transition-colors">
                      Get Started
                    </button>
                    <button class="h-11 px-6 border border-zinc-700 text-white rounded-lg font-medium hover:bg-zinc-800 transition-colors">
                      Learn More
                    </button>
                  </div>
                </div>
              </main>
            </Layout>
            """
            .formatted(name)));
    files.add(createFile(project, "/.gitignore", getGitignore()));
    files.add(createFile(project, "/README.md", getReadme(name, "Astro")));

    log.info("Created {} Astro scaffold files", files.size());
    return files;
  }

  /*
   * ═══════════════════════════════════════════════════════════════════
   * SolidJS Scaffold
   * ═══════════════════════════════════════════════════════════════════
   */

  private List<FileEntity> createSolidScaffold(ProjectEntity project) {
    List<FileEntity> files = new ArrayList<>();
    String name = project.getName();

    files.add(createFile(project, "/package.json", """
        {
          "name": "%s",
          "version": "0.1.0",
          "private": true,
          "scripts": {
            "dev": "vite",
            "build": "vite build",
            "preview": "vite preview"
          },
          "dependencies": {
            "solid-js": "^1.8.0",
            "@solidjs/router": "^0.13.0"
          },
          "devDependencies": {
            "vite": "^5.4.0",
            "vite-plugin-solid": "^2.10.0",
            "typescript": "^5.3.0",
            "autoprefixer": "^10.4.17",
            "postcss": "^8.4.33",
            "tailwindcss": "^3.4.1"
          }
        }
        """.formatted(safeName(name))));
    files.add(createFile(project, "/vite.config.ts", """
        import { defineConfig } from 'vite';
        import solid from 'vite-plugin-solid';

        export default defineConfig({
          plugins: [solid()],
        });
        """));
    files.add(createFile(project, "/tsconfig.json", """
        {
          "compilerOptions": {
            "strict": true,
            "target": "ESNext",
            "module": "ESNext",
            "moduleResolution": "bundler",
            "jsxImportSource": "solid-js",
            "jsx": "preserve",
            "noEmit": true,
            "isolatedModules": true,
            "skipLibCheck": true
          }
        }
        """));
    files.add(createFile(project, "/tailwind.config.js", getReactTailwindConfig()));
    files.add(createFile(project, "/postcss.config.js", getReactPostCssConfig()));
    files.add(createFile(project, "/index.html", """
        <!doctype html>
        <html lang="en">
          <head>
            <meta charset="UTF-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <title>%s</title>
          </head>
          <body>
            <div id="root"></div>
            <script type="module" src="/src/index.tsx"></script>
          </body>
        </html>
        """.formatted(name)));
    files.add(createFile(project, "/src/index.tsx", """
        import { render } from 'solid-js/web';
        import App from './App';
        import './index.css';

        render(() => <App />, document.getElementById('root')!);
        """));
    files.add(createFile(project, "/src/App.tsx",
        """
            export default function App() {
              return (
                <main class="flex min-h-screen flex-col items-center justify-center p-24 bg-gradient-to-b from-zinc-900 to-black">
                  <div class="text-center space-y-6">
                    <h1 class="text-5xl font-bold text-white">Welcome to Your App</h1>
                    <p class="text-xl text-zinc-400 max-w-lg">
                      Start building amazing things with AI-powered code generation
                    </p>
                    <div class="flex gap-4 justify-center pt-4">
                      <button class="h-11 px-6 bg-white text-black rounded-lg font-medium hover:bg-zinc-200 transition-colors">
                        Get Started
                      </button>
                      <button class="h-11 px-6 border border-zinc-700 text-white rounded-lg font-medium hover:bg-zinc-800 transition-colors">
                        Learn More
                      </button>
                    </div>
                  </div>
                </main>
              );
            }
            """));
    files.add(createFile(project, "/src/index.css", getReactGlobalCss()));
    files.add(createFile(project, "/.gitignore", getGitignore()));
    files.add(createFile(project, "/README.md", getReadme(name, "SolidJS")));

    log.info("Created {} SolidJS scaffold files", files.size());
    return files;
  }

  /*
   * ═══════════════════════════════════════════════════════════════════
   * Remix Scaffold
   * ═══════════════════════════════════════════════════════════════════
   */

  private List<FileEntity> createRemixScaffold(ProjectEntity project) {
    List<FileEntity> files = new ArrayList<>();
    String name = project.getName();

    files.add(createFile(project, "/package.json", """
        {
          "name": "%s",
          "version": "0.1.0",
          "private": true,
          "sideEffects": false,
          "scripts": {
            "dev": "remix vite:dev",
            "build": "remix vite:build",
            "start": "remix-serve ./build/server/index.js"
          },
          "dependencies": {
            "@remix-run/node": "^2.8.0",
            "@remix-run/react": "^2.8.0",
            "@remix-run/serve": "^2.8.0",
            "isbot": "^5.1.0",
            "react": "^18.2.0",
            "react-dom": "^18.2.0"
          },
          "devDependencies": {
            "@remix-run/dev": "^2.8.0",
            "autoprefixer": "^10.4.17",
            "postcss": "^8.4.33",
            "tailwindcss": "^3.4.1",
            "typescript": "^5.3.0",
            "vite": "^5.4.0"
          }
        }
        """.formatted(safeName(name))));
    files.add(createFile(project, "/tailwind.config.js", """
        /** @type {import('tailwindcss').Config} */
        export default {
          content: ['./app/**/*.{js,ts,jsx,tsx}'],
          theme: { extend: {} },
          plugins: [],
        };
        """));
    files.add(createFile(project, "/postcss.config.js", getReactPostCssConfig()));
    files.add(createFile(project, "/app/tailwind.css", getReactGlobalCss()));
    files.add(createFile(project, "/app/root.tsx", """
        import type { LinksFunction } from "@remix-run/node";
        import { Links, Meta, Outlet, Scripts, ScrollRestoration } from "@remix-run/react";
        import stylesheet from "./tailwind.css?url";

        export const links: LinksFunction = () => [
          { rel: "stylesheet", href: stylesheet },
        ];

        export default function App() {
          return (
            <html lang="en">
              <head>
                <meta charSet="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1" />
                <Meta />
                <Links />
              </head>
              <body>
                <Outlet />
                <ScrollRestoration />
                <Scripts />
              </body>
            </html>
          );
        }
        """));
    files.add(createFile(project, "/app/routes/_index.tsx",
        """
            import type { MetaFunction } from "@remix-run/node";

            export const meta: MetaFunction = () => {
              return [{ title: "%s" }, { name: "description", content: "Built with NexaStudio AI" }];
            };

            export default function Index() {
              return (
                <main className="flex min-h-screen flex-col items-center justify-center p-24 bg-gradient-to-b from-zinc-900 to-black">
                  <div className="text-center space-y-6">
                    <h1 className="text-5xl font-bold text-white">Welcome to Your App</h1>
                    <p className="text-xl text-zinc-400 max-w-lg">
                      Start building amazing things with AI-powered code generation
                    </p>
                    <div className="flex gap-4 justify-center pt-4">
                      <button className="h-11 px-6 bg-white text-black rounded-lg font-medium hover:bg-zinc-200 transition-colors">
                        Get Started
                      </button>
                      <button className="h-11 px-6 border border-zinc-700 text-white rounded-lg font-medium hover:bg-zinc-800 transition-colors">
                        Learn More
                      </button>
                    </div>
                  </div>
                </main>
              );
            }
            """
            .formatted(name)));
    files.add(createFile(project, "/.gitignore", getGitignore()));
    files.add(createFile(project, "/README.md", getReadme(name, "Remix")));

    log.info("Created {} Remix scaffold files", files.size());
    return files;
  }

  /*
   * ═══════════════════════════════════════════════════════════════════
   * Vanilla (HTML/CSS/JS) Scaffold
   * ═══════════════════════════════════════════════════════════════════
   */

  private List<FileEntity> createVanillaScaffold(ProjectEntity project) {
    List<FileEntity> files = new ArrayList<>();
    String name = project.getName();

    files.add(createFile(project, "/index.html",
        """
            <!doctype html>
            <html lang="en">
              <head>
                <meta charset="UTF-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                <title>%s</title>
                <script src="https://cdn.tailwindcss.com"></script>
                <link rel="stylesheet" href="/styles/main.css" />
              </head>
              <body class="bg-gradient-to-b from-zinc-900 to-black min-h-screen flex items-center justify-center">
                <main class="text-center space-y-6 p-24">
                  <h1 class="text-5xl font-bold text-white">Welcome to Your App</h1>
                  <p class="text-xl text-zinc-400 max-w-lg mx-auto">
                    Start building amazing things with AI-powered code generation
                  </p>
                  <div class="flex gap-4 justify-center pt-4">
                    <button class="h-11 px-6 bg-white text-black rounded-lg font-medium hover:bg-zinc-200 transition-colors">
                      Get Started
                    </button>
                    <button class="h-11 px-6 border border-zinc-700 text-white rounded-lg font-medium hover:bg-zinc-800 transition-colors">
                      Learn More
                    </button>
                  </div>
                </main>
                <script type="module" src="/scripts/main.js"></script>
              </body>
            </html>
            """
            .formatted(name)));
    files.add(createFile(project, "/styles/main.css", """
        * {
          margin: 0;
          padding: 0;
          box-sizing: border-box;
        }

        body {
          font-family: system-ui, -apple-system, sans-serif;
          -webkit-font-smoothing: antialiased;
        }
        """));
    files.add(createFile(project, "/scripts/main.js", """
        // Main application script
        console.log('App initialized');

        document.addEventListener('DOMContentLoaded', () => {
          // App logic here
        });
        """));
    files.add(createFile(project, "/.gitignore", getGitignore()));
    files.add(createFile(project, "/README.md", getReadme(name, "Vanilla HTML/CSS/JS")));

    log.info("Created {} Vanilla scaffold files", files.size());
    return files;
  }

  /*
   * ═══════════════════════════════════════════════════════════════════
   * Helper Methods
   * ═══════════════════════════════════════════════════════════════════
   */

  private FileEntity createFile(ProjectEntity project, String path, String content) {
    return FileEntity.builder()
        .project(project)
        .path(path)
        .content(content)
        .type(determineFileType(path))
        .generated(false)
        .locked(isProtectedFile(path))
        .build();
  }

  private FileEntity.FileType determineFileType(String path) {
    if (path.endsWith(".json") || path.endsWith(".config.ts") || path.endsWith(".config.js")
        || path.endsWith(".config.mjs")) {
      return FileEntity.FileType.CONFIG;
    }
    if (path.endsWith(".md")) {
      return FileEntity.FileType.DOCUMENTATION;
    }
    if (path.endsWith(".css")) {
      return FileEntity.FileType.ASSET;
    }
    return FileEntity.FileType.CODE;
  }

  private boolean isProtectedFile(String path) {
    return path.equals("/package.json") ||
        path.equals("/tsconfig.json") ||
        path.equals("/next.config.ts") ||
        path.equals("/vite.config.ts") ||
        path.equals("/angular.json");
  }

  private String safeName(String name) {
    return name.toLowerCase().replaceAll("[^a-z0-9-]", "-");
  }

  /*
   * ═══════════════════════════════════════════════════════════════════
   * Next.js File Templates
   * ═══════════════════════════════════════════════════════════════════
   */

  private String getNextJsPackageJson(String name) {
    return """
        {
          "name": "%s",
          "version": "0.1.0",
          "private": true,
          "scripts": {
            "dev": "next dev",
            "build": "next build",
            "start": "next start",
            "lint": "next lint"
          },
          "dependencies": {
            "next": "14.2.0",
            "react": "^18.2.0",
            "react-dom": "^18.2.0",
            "clsx": "^2.1.0",
            "tailwind-merge": "^2.2.0"
          },
          "devDependencies": {
            "@types/node": "^20.11.0",
            "@types/react": "^18.2.0",
            "@types/react-dom": "^18.2.0",
            "autoprefixer": "^10.4.17",
            "postcss": "^8.4.33",
            "tailwindcss": "^3.4.1",
            "typescript": "^5.3.0"
          }
        }
        """.formatted(safeName(name));
  }

  private String getNextJsTsConfig() {
    return """
        {
          "compilerOptions": {
            "lib": ["dom", "dom.iterable", "esnext"],
            "allowJs": true,
            "skipLibCheck": true,
            "strict": true,
            "noEmit": true,
            "esModuleInterop": true,
            "module": "esnext",
            "moduleResolution": "bundler",
            "resolveJsonModule": true,
            "isolatedModules": true,
            "jsx": "preserve",
            "incremental": true,
            "plugins": [{ "name": "next" }],
            "paths": {
              "@/*": ["./*"]
            }
          },
          "include": ["next-env.d.ts", "**/*.ts", "**/*.tsx", ".next/types/**/*.ts"],
          "exclude": ["node_modules"]
        }
        """;
  }

  private String getNextJsConfig() {
    return """
        import type { NextConfig } from "next";

        const nextConfig: NextConfig = {
          reactStrictMode: true,
        };

        export default nextConfig;
        """;
  }

  private String getNextJsTailwindConfig() {
    return """
        import type { Config } from "tailwindcss";

        const config: Config = {
          content: [
            "./pages/**/*.{js,ts,jsx,tsx,mdx}",
            "./components/**/*.{js,ts,jsx,tsx,mdx}",
            "./app/**/*.{js,ts,jsx,tsx,mdx}",
          ],
          theme: {
            extend: {},
          },
          plugins: [],
        };

        export default config;
        """;
  }

  private String getNextJsGlobalCss() {
    return """
        @tailwind base;
        @tailwind components;
        @tailwind utilities;

        :root {
          --background: #ffffff;
          --foreground: #171717;
        }

        @media (prefers-color-scheme: dark) {
          :root {
            --background: #0a0a0a;
            --foreground: #ededed;
          }
        }

        body {
          color: var(--foreground);
          background: var(--background);
        }
        """;
  }

  private String getNextJsLayout(String name) {
    return """
        import type { Metadata } from "next";
        import { Inter } from "next/font/google";
        import "./globals.css";

        const inter = Inter({ subsets: ["latin"] });

        export const metadata: Metadata = {
          title: "%s",
          description: "Built with NexaStudio AI",
        };

        export default function RootLayout({
          children,
        }: Readonly<{
          children: React.ReactNode;
        }>) {
          return (
            <html lang="en">
              <body className={inter.className}>{children}</body>
            </html>
          );
        }
        """.formatted(name);
  }

  private String getNextJsPage() {
    return """
        import { Button } from "@/components/ui/button";

        export default function Home() {
          return (
            <main className="flex min-h-screen flex-col items-center justify-center p-24 bg-gradient-to-b from-zinc-900 to-black">
              <div className="text-center space-y-6">
                <h1 className="text-5xl font-bold text-white">
                  Welcome to Your App
                </h1>
                <p className="text-xl text-zinc-400 max-w-lg">
                  Start building amazing things with AI-powered code generation
                </p>
                <div className="flex gap-4 justify-center pt-4">
                  <Button>Get Started</Button>
                  <Button variant="outline">Learn More</Button>
                </div>
              </div>
            </main>
          );
        }
        """;
  }

  private String getNextJsButton() {
    return """
        import { cn } from "@/lib/utils";
        import { ButtonHTMLAttributes, forwardRef } from "react";

        interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
          variant?: "default" | "outline" | "ghost";
          size?: "sm" | "md" | "lg";
        }

        const Button = forwardRef<HTMLButtonElement, ButtonProps>(
          ({ className, variant = "default", size = "md", ...props }, ref) => {
            return (
              <button
                ref={ref}
                className={cn(
                  "inline-flex items-center justify-center rounded-lg font-medium transition-colors",
                  "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2",
                  "disabled:pointer-events-none disabled:opacity-50",
                  {
                    "bg-white text-black hover:bg-zinc-200": variant === "default",
                    "border border-zinc-700 bg-transparent text-white hover:bg-zinc-800": variant === "outline",
                    "bg-transparent text-white hover:bg-zinc-800": variant === "ghost",
                  },
                  {
                    "h-9 px-3 text-sm": size === "sm",
                    "h-11 px-6 text-base": size === "md",
                    "h-14 px-8 text-lg": size === "lg",
                  },
                  className
                )}
                {...props}
              />
            );
          }
        );

        Button.displayName = "Button";

        export { Button };
        """;
  }

  /*
   * ═══════════════════════════════════════════════════════════════════
   * React (Vite) File Templates
   * ═══════════════════════════════════════════════════════════════════
   */

  private String getReactPackageJson(String name) {
    return """
        {
          "name": "%s",
          "private": true,
          "version": "0.1.0",
          "type": "module",
          "scripts": {
            "dev": "vite",
            "build": "tsc -b && vite build",
            "lint": "eslint .",
            "preview": "vite preview"
          },
          "dependencies": {
            "react": "^18.3.1",
            "react-dom": "^18.3.1",
            "react-router-dom": "^6.22.0",
            "clsx": "^2.1.0",
            "tailwind-merge": "^2.2.0",
            "framer-motion": "^11.0.0",
            "lucide-react": "^0.321.0"
          },
          "devDependencies": {
            "@types/react": "^18.3.0",
            "@types/react-dom": "^18.3.0",
            "@vitejs/plugin-react": "^4.3.0",
            "autoprefixer": "^10.4.17",
            "postcss": "^8.4.33",
            "tailwindcss": "^3.4.1",
            "typescript": "^5.5.0",
            "vite": "^5.4.0"
          }
        }
        """.formatted(safeName(name));
  }

  private String getReactTsConfig() {
    return """
        {
          "compilerOptions": {
            "target": "ES2020",
            "useDefineForClassFields": true,
            "lib": ["ES2020", "DOM", "DOM.Iterable"],
            "module": "ESNext",
            "skipLibCheck": true,
            "moduleResolution": "bundler",
            "allowImportingTsExtensions": true,
            "resolveJsonModule": true,
            "isolatedModules": true,
            "noEmit": true,
            "jsx": "react-jsx",
            "strict": true,
            "noUnusedLocals": true,
            "noUnusedParameters": true,
            "noFallthroughCasesInSwitch": true,
            "paths": {
              "@/*": ["./src/*"]
            }
          },
          "include": ["src"],
          "references": [{ "path": "./tsconfig.node.json" }]
        }
        """;
  }

  private String getReactTsConfigNode() {
    return """
        {
          "compilerOptions": {
            "composite": true,
            "skipLibCheck": true,
            "module": "ESNext",
            "moduleResolution": "bundler",
            "allowSyntheticDefaultImports": true,
            "strict": true
          },
          "include": ["vite.config.ts"]
        }
        """;
  }

  private String getViteConfig() {
    return """
        import { defineConfig } from 'vite';
        import react from '@vitejs/plugin-react';
        import path from 'path';

        export default defineConfig({
          plugins: [react()],
          resolve: {
            alias: {
              '@': path.resolve(__dirname, './src'),
            },
          },
        });
        """;
  }

  private String getReactTailwindConfig() {
    return """
        /** @type {import('tailwindcss').Config} */
        export default {
          content: [
            "./index.html",
            "./src/**/*.{js,ts,jsx,tsx}",
          ],
          theme: {
            extend: {},
          },
          plugins: [],
        };
        """;
  }

  private String getReactPostCssConfig() {
    return """
        export default {
          plugins: {
            tailwindcss: {},
            autoprefixer: {},
          },
        };
        """;
  }

  private String getReactIndexHtml(String name) {
    return """
        <!doctype html>
        <html lang="en">
          <head>
            <meta charset="UTF-8" />
            <link rel="icon" type="image/svg+xml" href="/vite.svg" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <title>%s</title>
          </head>
          <body>
            <div id="root"></div>
            <script type="module" src="/src/main.tsx"></script>
          </body>
        </html>
        """.formatted(name);
  }

  private String getReactMain() {
    return """
        import { StrictMode } from 'react';
        import { createRoot } from 'react-dom/client';
        import App from './App';
        import './index.css';

        createRoot(document.getElementById('root')!).render(
          <StrictMode>
            <App />
          </StrictMode>,
        );
        """;
  }

  private String getReactApp() {
    return """
        import { Button } from '@/components/ui/Button';

        function App() {
          return (
            <main className="flex min-h-screen flex-col items-center justify-center p-24 bg-gradient-to-b from-zinc-900 to-black">
              <div className="text-center space-y-6">
                <h1 className="text-5xl font-bold text-white">
                  Welcome to Your App
                </h1>
                <p className="text-xl text-zinc-400 max-w-lg">
                  Start building amazing things with AI-powered code generation
                </p>
                <div className="flex gap-4 justify-center pt-4">
                  <Button>Get Started</Button>
                  <Button variant="outline">Learn More</Button>
                </div>
              </div>
            </main>
          );
        }

        export default App;
        """;
  }

  private String getReactGlobalCss() {
    return """
        @tailwind base;
        @tailwind components;
        @tailwind utilities;

        :root {
          font-family: Inter, system-ui, Avenir, Helvetica, Arial, sans-serif;
          line-height: 1.5;
          font-weight: 400;
          color: #213547;
          background-color: #ffffff;
        }

        @media (prefers-color-scheme: dark) {
          :root {
            color: #ffffff;
            background-color: #0a0a0a;
          }
        }

        body {
          margin: 0;
          min-height: 100vh;
        }
        """;
  }

  private String getReactButton() {
    return """
        import { cn } from '@/lib/utils';
        import { ButtonHTMLAttributes, forwardRef } from 'react';

        interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
          variant?: 'default' | 'outline' | 'ghost';
          size?: 'sm' | 'md' | 'lg';
        }

        const Button = forwardRef<HTMLButtonElement, ButtonProps>(
          ({ className, variant = 'default', size = 'md', ...props }, ref) => {
            return (
              <button
                ref={ref}
                className={cn(
                  'inline-flex items-center justify-center rounded-lg font-medium transition-colors',
                  'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2',
                  'disabled:pointer-events-none disabled:opacity-50',
                  {
                    'bg-white text-black hover:bg-zinc-200': variant === 'default',
                    'border border-zinc-700 bg-transparent text-white hover:bg-zinc-800': variant === 'outline',
                    'bg-transparent text-white hover:bg-zinc-800': variant === 'ghost',
                  },
                  {
                    'h-9 px-3 text-sm': size === 'sm',
                    'h-11 px-6 text-base': size === 'md',
                    'h-14 px-8 text-lg': size === 'lg',
                  },
                  className
                )}
                {...props}
              />
            );
          }
        );

        Button.displayName = 'Button';

        export { Button };
        """;
  }

  private String getViteEnvDts() {
    return """
        /// <reference types="vite/client" />
        """;
  }

  /*
   * ═══════════════════════════════════════════════════════════════════
   * Vue File Templates
   * ═══════════════════════════════════════════════════════════════════
   */

  private String getVuePackageJson(String name) {
    return """
        {
          "name": "%s",
          "private": true,
          "version": "0.1.0",
          "type": "module",
          "scripts": {
            "dev": "vite",
            "build": "vue-tsc -b && vite build",
            "preview": "vite preview"
          },
          "dependencies": {
            "vue": "^3.4.0",
            "vue-router": "^4.2.0",
            "pinia": "^2.1.0"
          },
          "devDependencies": {
            "@vitejs/plugin-vue": "^5.0.0",
            "autoprefixer": "^10.4.17",
            "postcss": "^8.4.33",
            "tailwindcss": "^3.4.1",
            "typescript": "^5.5.0",
            "vite": "^5.4.0",
            "vue-tsc": "^2.0.0"
          }
        }
        """.formatted(safeName(name));
  }

  private String getVueTsConfig() {
    return """
        {
          "compilerOptions": {
            "target": "ES2020",
            "useDefineForClassFields": true,
            "module": "ESNext",
            "lib": ["ES2020", "DOM", "DOM.Iterable"],
            "skipLibCheck": true,
            "moduleResolution": "bundler",
            "allowImportingTsExtensions": true,
            "resolveJsonModule": true,
            "isolatedModules": true,
            "noEmit": true,
            "jsx": "preserve",
            "strict": true,
            "noUnusedLocals": true,
            "noUnusedParameters": true,
            "noFallthroughCasesInSwitch": true
          },
          "include": ["src/**/*.ts", "src/**/*.tsx", "src/**/*.vue"]
        }
        """;
  }

  private String getVueViteConfig() {
    return """
        import { defineConfig } from 'vite';
        import vue from '@vitejs/plugin-vue';

        export default defineConfig({
          plugins: [vue()],
        });
        """;
  }

  private String getVueIndexHtml(String name) {
    return """
        <!doctype html>
        <html lang="en">
          <head>
            <meta charset="UTF-8" />
            <link rel="icon" type="image/svg+xml" href="/vite.svg" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <title>%s</title>
          </head>
          <body>
            <div id="app"></div>
            <script type="module" src="/src/main.ts"></script>
          </body>
        </html>
        """.formatted(name);
  }

  private String getVueMain() {
    return """
        import { createApp } from 'vue';
        import './style.css';
        import App from './App.vue';

        createApp(App).mount('#app');
        """;
  }

  private String getVueApp() {
    return """
        <script setup lang="ts">
        import HelloWorld from './components/HelloWorld.vue';
        </script>

        <template>
          <main class="flex min-h-screen flex-col items-center justify-center p-24 bg-gradient-to-b from-zinc-900 to-black">
            <HelloWorld msg="Welcome to Your App" />
          </main>
        </template>
        """;
  }

  private String getVueHelloWorld() {
    return """
        <script setup lang="ts">
        defineProps<{ msg: string }>();
        </script>

        <template>
          <div class="text-center space-y-6">
            <h1 class="text-5xl font-bold text-white">{{ msg }}</h1>
            <p class="text-xl text-zinc-400 max-w-lg">
              Start building amazing things with AI-powered code generation
            </p>
            <div class="flex gap-4 justify-center pt-4">
              <button class="h-11 px-6 bg-white text-black rounded-lg font-medium hover:bg-zinc-200 transition-colors">
                Get Started
              </button>
              <button class="h-11 px-6 border border-zinc-700 text-white rounded-lg font-medium hover:bg-zinc-800 transition-colors">
                Learn More
              </button>
            </div>
          </div>
        </template>
        """;
  }

  private String getVueEnvDts() {
    return """
        /// <reference types="vite/client" />

        declare module '*.vue' {
          import type { DefineComponent } from 'vue'
          const component: DefineComponent<{}, {}, any>
          export default component
        }
        """;
  }

  /*
   * ═══════════════════════════════════════════════════════════════════
   * Angular File Templates
   * ═══════════════════════════════════════════════════════════════════
   */

  private String getAngularPackageJson(String name) {
    return """
        {
          "name": "%s",
          "version": "0.1.0",
          "scripts": {
            "ng": "ng",
            "start": "ng serve",
            "build": "ng build",
            "watch": "ng build --watch --configuration development",
            "test": "ng test"
          },
          "private": true,
          "dependencies": {
            "@angular/animations": "^17.0.0",
            "@angular/common": "^17.0.0",
            "@angular/compiler": "^17.0.0",
            "@angular/core": "^17.0.0",
            "@angular/forms": "^17.0.0",
            "@angular/platform-browser": "^17.0.0",
            "@angular/platform-browser-dynamic": "^17.0.0",
            "@angular/router": "^17.0.0",
            "rxjs": "~7.8.0",
            "tslib": "^2.3.0",
            "zone.js": "~0.14.0"
          },
          "devDependencies": {
            "@angular-devkit/build-angular": "^17.0.0",
            "@angular/cli": "^17.0.0",
            "@angular/compiler-cli": "^17.0.0",
            "autoprefixer": "^10.4.17",
            "postcss": "^8.4.33",
            "tailwindcss": "^3.4.1",
            "typescript": "~5.2.0"
          }
        }
        """.formatted(safeName(name));
  }

  private String getAngularTsConfig() {
    return """
        {
          "compileOnSave": false,
          "compilerOptions": {
            "outDir": "./dist/out-tsc",
            "forceConsistentCasingInFileNames": true,
            "strict": true,
            "noImplicitOverride": true,
            "noPropertyAccessFromIndexSignature": true,
            "noImplicitReturns": true,
            "noFallthroughCasesInSwitch": true,
            "skipLibCheck": true,
            "esModuleInterop": true,
            "sourceMap": true,
            "declaration": false,
            "experimentalDecorators": true,
            "moduleResolution": "bundler",
            "importHelpers": true,
            "target": "ES2022",
            "module": "ES2022",
            "useDefineForClassFields": false,
            "lib": ["ES2022", "dom"]
          },
          "angularCompilerOptions": {
            "enableI18nLegacyMessageIdFormat": false,
            "strictInjectionParameters": true,
            "strictInputAccessModifiers": true,
            "strictTemplates": true
          }
        }
        """;
  }

  private String getAngularJson(String name) {
    String safeName = safeName(name);
    return """
        {
          "$schema": "./node_modules/@angular/cli/lib/config/schema.json",
          "version": 1,
          "newProjectRoot": "projects",
          "projects": {
            "%s": {
              "projectType": "application",
              "root": "",
              "sourceRoot": "src",
              "prefix": "app",
              "architect": {
                "build": {
                  "builder": "@angular-devkit/build-angular:application",
                  "options": {
                    "outputPath": "dist/%s",
                    "index": "src/index.html",
                    "browser": "src/main.ts",
                    "polyfills": ["zone.js"],
                    "tsConfig": "tsconfig.json",
                    "assets": ["src/favicon.ico", "src/assets"],
                    "styles": ["src/styles.css"],
                    "scripts": []
                  }
                },
                "serve": {
                  "builder": "@angular-devkit/build-angular:dev-server",
                  "configurations": {
                    "development": {
                      "buildTarget": "%s:build:development"
                    }
                  },
                  "defaultConfiguration": "development"
                }
              }
            }
          }
        }
        """.formatted(safeName, safeName, safeName);
  }

  private String getAngularTailwindConfig() {
    return """
        /** @type {import('tailwindcss').Config} */
        module.exports = {
          content: [
            "./src/**/*.{html,ts}",
          ],
          theme: {
            extend: {},
          },
          plugins: [],
        };
        """;
  }

  private String getAngularMain() {
    return """
        import { bootstrapApplication } from '@angular/platform-browser';
        import { appConfig } from './app/app.config';
        import { AppComponent } from './app/app.component';

        bootstrapApplication(AppComponent, appConfig)
          .catch((err) => console.error(err));
        """;
  }

  private String getAngularIndexHtml(String name) {
    return """
        <!doctype html>
        <html lang="en">
        <head>
          <meta charset="utf-8">
          <title>%s</title>
          <base href="/">
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <link rel="icon" type="image/x-icon" href="favicon.ico">
        </head>
        <body>
          <app-root></app-root>
        </body>
        </html>
        """.formatted(name);
  }

  private String getAngularAppComponent() {
    return """
        import { Component } from '@angular/core';

        @Component({
          selector: 'app-root',
          standalone: true,
          template: `
            <main class="flex min-h-screen flex-col items-center justify-center p-24 bg-gradient-to-b from-zinc-900 to-black">
              <div class="text-center space-y-6">
                <h1 class="text-5xl font-bold text-white">
                  Welcome to Your App
                </h1>
                <p class="text-xl text-zinc-400 max-w-lg">
                  Start building amazing things with AI-powered code generation
                </p>
                <div class="flex gap-4 justify-center pt-4">
                  <button class="h-11 px-6 bg-white text-black rounded-lg font-medium hover:bg-zinc-200 transition-colors">
                    Get Started
                  </button>
                  <button class="h-11 px-6 border border-zinc-700 text-white rounded-lg font-medium hover:bg-zinc-800 transition-colors">
                    Learn More
                  </button>
                </div>
              </div>
            </main>
          `,
        })
        export class AppComponent {
          title = 'my-app';
        }
        """;
  }

  private String getAngularAppConfig() {
    return """
        import { ApplicationConfig } from '@angular/core';
        import { provideRouter } from '@angular/router';

        export const appConfig: ApplicationConfig = {
          providers: [
            provideRouter([]),
          ]
        };
        """;
  }

  /*
   * ═══════════════════════════════════════════════════════════════════
   * Common Templates
   * ═══════════════════════════════════════════════════════════════════
   */

  private String getPostCssConfig() {
    return """
        /** @type {import('postcss-load-config').Config} */
        const config = {
          plugins: {
            tailwindcss: {},
            autoprefixer: {},
          },
        };

        export default config;
        """;
  }

  private String getUtilsTs() {
    return """
        import { clsx, type ClassValue } from 'clsx';
        import { twMerge } from 'tailwind-merge';

        export function cn(...inputs: ClassValue[]) {
          return twMerge(clsx(inputs));
        }
        """;
  }

  private String getGitignore() {
    return """
        # Dependencies
        node_modules
        .pnp
        .pnp.js

        # Testing
        coverage

        # Build
        .next
        out
        dist
        build
        .angular

        # Misc
        .DS_Store
        *.pem

        # Debug
        npm-debug.log*
        yarn-debug.log*
        yarn-error.log*

        # Local env files
        .env*.local
        .env

        # TypeScript
        *.tsbuildinfo
        next-env.d.ts

        # IDE
        .idea
        .vscode
        """;
  }

  private String getReadme(String name, String framework) {
    return """
        # %s

        This is a %s project generated with [NexaStudio AI](https://nexastudio.io).

        ## Getting Started

        First, install the dependencies:

        ```bash
        npm install
        ```

        Then, run the development server:

        ```bash
        npm run dev
        ```

        Open [http://localhost:3000](http://localhost:3000) (or the port shown in terminal) to see the result.

        ## Tech Stack

        - %s
        - TypeScript
        - Tailwind CSS

        ## Learn More

        To learn more, check out the [%s Documentation](https://google.com/search?q=%s+documentation).
        """.formatted(name, framework, framework, framework, framework.toLowerCase().replace(" ", "+"));
  }
}
