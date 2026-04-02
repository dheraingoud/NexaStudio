package com.nexastudio.generator;

import com.nexastudio.project.FileEntity;
import com.nexastudio.project.ProjectEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Next.js Project Scaffold Service.
 * Creates the initial file structure for a new Next.js project.
 */
@Service
public class NextJsScaffoldService {

    private static final Logger log = LoggerFactory.getLogger(NextJsScaffoldService.class);

    /**
     * Create scaffold files for a new project
     */
    public List<FileEntity> createScaffold(ProjectEntity project) {
        log.info("Creating Next.js scaffold for project: {}", project.getId());
        
        List<FileEntity> files = new ArrayList<>();
        
        // package.json
        files.add(createFile(project, "/package.json", getPackageJson(project.getName())));
        
        // TypeScript config
        files.add(createFile(project, "/tsconfig.json", getTsConfig()));
        
        // Tailwind config
        files.add(createFile(project, "/tailwind.config.ts", getTailwindConfig()));
        
        // PostCSS config
        files.add(createFile(project, "/postcss.config.mjs", getPostCssConfig()));
        
        // Next.js config
        files.add(createFile(project, "/next.config.ts", getNextConfig()));
        
        // Global CSS
        files.add(createFile(project, "/app/globals.css", getGlobalCss()));
        
        // Root layout
        files.add(createFile(project, "/app/layout.tsx", getRootLayout(project.getName())));
        
        // Home page
        files.add(createFile(project, "/app/page.tsx", getHomePage()));
        
        // README
        files.add(createFile(project, "/README.md", getReadme(project.getName())));
        
        // .gitignore
        files.add(createFile(project, "/.gitignore", getGitignore()));
        
        log.info("Created {} scaffold files", files.size());
        return files;
    }

    /**
     * Create a file entity
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

    /**
     * Determine file type from path
     */
    private FileEntity.FileType determineFileType(String path) {
        if (path.endsWith(".json") || path.endsWith(".config.ts") || path.endsWith(".config.mjs")) {
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

    /**
     * Check if file should be locked
     */
    private boolean isProtectedFile(String path) {
        return path.equals("/package.json") || 
               path.equals("/tsconfig.json") ||
               path.equals("/next.config.ts");
    }

    private String getPackageJson(String projectName) {
        String safeName = projectName.toLowerCase().replaceAll("[^a-z0-9-]", "-");
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
                "framer-motion": "^11.0.0",
                "lucide-react": "^0.321.0"
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
            """.formatted(safeName);
    }

    private String getTsConfig() {
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
                "plugins": [
                  {
                    "name": "next"
                  }
                ],
                "paths": {
                  "@/*": ["./*"]
                }
              },
              "include": ["next-env.d.ts", "**/*.ts", "**/*.tsx", ".next/types/**/*.ts"],
              "exclude": ["node_modules"]
            }
            """;
    }

    private String getTailwindConfig() {
        return """
            import type { Config } from "tailwindcss";
            
            const config: Config = {
              content: [
                "./pages/**/*.{js,ts,jsx,tsx,mdx}",
                "./components/**/*.{js,ts,jsx,tsx,mdx}",
                "./app/**/*.{js,ts,jsx,tsx,mdx}",
              ],
              theme: {
                extend: {
                  colors: {
                    background: "var(--background)",
                    foreground: "var(--foreground)",
                  },
                },
              },
              plugins: [],
            };
            
            export default config;
            """;
    }

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

    private String getNextConfig() {
        return """
            import type { NextConfig } from "next";
            
            const nextConfig: NextConfig = {
              reactStrictMode: true,
            };
            
            export default nextConfig;
            """;
    }

    private String getGlobalCss() {
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
              font-family: Arial, Helvetica, sans-serif;
            }
            """;
    }

    private String getRootLayout(String projectName) {
        return """
            import type { Metadata } from "next";
            import "./globals.css";
            
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
                  <body className="antialiased">
                    {children}
                  </body>
                </html>
              );
            }
            """.formatted(projectName);
    }

    private String getHomePage() {
        return """
            export default function Home() {
              return (
                <main className="flex min-h-screen flex-col items-center justify-center p-24">
                  <div className="text-center">
                    <h1 className="text-4xl font-bold mb-4">
                      Welcome to Your App
                    </h1>
                    <p className="text-gray-600 dark:text-gray-400 text-lg mb-8">
                      Start building by asking AI to generate components
                    </p>
                    <div className="flex gap-4 justify-center">
                      <a
                        href="#"
                        className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                      >
                        Get Started
                      </a>
                      <a
                        href="#"
                        className="px-6 py-3 border border-gray-300 dark:border-gray-700 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
                      >
                        Learn More
                      </a>
                    </div>
                  </div>
                </main>
              );
            }
            """;
    }

    private String getReadme(String projectName) {
        return """
            # %s
            
            This project was generated with [NexaStudio AI](https://nexastudio.io).
            
            ## Getting Started
            
            First, install the dependencies:
            
            ```bash
            npm install
            ```
            
            Then, run the development server:
            
            ```bash
            npm run dev
            ```
            
            Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.
            
            ## Tech Stack
            
            - [Next.js 14](https://nextjs.org/) - React Framework
            - [TypeScript](https://www.typescriptlang.org/) - Type Safety
            - [Tailwind CSS](https://tailwindcss.com/) - Styling
            
            ## Project Structure
            
            ```
            /app          - App Router pages and layouts
            /components   - Reusable UI components
            /lib          - Utility functions
            /public       - Static assets
            ```
            
            ## Learn More
            
            To learn more about Next.js, check out the [Next.js Documentation](https://nextjs.org/docs).
            """.formatted(projectName);
    }

    private String getGitignore() {
        return """
            # Dependencies
            /node_modules
            /.pnp
            .pnp.js
            
            # Testing
            /coverage
            
            # Next.js
            /.next/
            /out/
            
            # Production
            /build
            
            # Misc
            .DS_Store
            *.pem
            
            # Debug
            npm-debug.log*
            yarn-debug.log*
            yarn-error.log*
            
            # Local env files
            .env*.local
            
            # Vercel
            .vercel
            
            # TypeScript
            *.tsbuildinfo
            next-env.d.ts
            """;
    }
}
