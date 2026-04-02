# NexaStudio — Full QA Bug Report

**Test Date:** 2026-04-01
**Frontend:** https://nexastudio-cg7o.onrender.com
**Backend:** https://nexastudio-29np.onrender.com
**Overall Status:** 🔴 CRITICAL — Several blocking bugs prevent end-to-end generation & verification

---

## 🏥 Backend Health Check

```json
GET https://nexastudio-29np.onrender.com/api/health → 200 OK
{
  "success": true,
  "data": {
    "status": "UP",
    "service": "nexastudio-backend",
    "version": "1.0.0",
    "aiModels": { "nvidia": true, "gemini": true }
  }
}
```

✅ Backend is alive and both AI models report as available.

---

## ✅ What Worked

| Feature | Status |
|---|---|
| Landing page loads | ✅ |
| Login form renders | ✅ |
| Login with valid credentials (sometimes) | ✅ |
| New Project wizard (all 4 steps render) | ✅ |
| Step 1 — Name & Description | ✅ |
| Step 2 — Framework selection UI | ✅ |
| Step 3 — Page type, color, design, animation pickers | ✅ |
| Step 4 — Summary + prompt textarea | ✅ |
| Project list page renders | ✅ |
| Sidebar navigation | ✅ |

---

## 🐛 Bugs Found

---

### BUG-001 — Intermittent 500 on Login [CRITICAL]

**Component:** `POST /api/auth/login`

**Exact Error:**
```
POST https://nexastudio-29np.onrender.com/api/auth/login → 500 Internal Server Error
UI: "Server error. Our team has been notified — please try again shortly."
```

**Root Cause — `JwtUtil.java` lines 159–163:**
The signing key is double-Base64-encoded. `secret.getBytes()` is encoded to Base64 string, then that string is decoded as Base64 — producing garbage bytes unpredictably, causing `io.jsonwebtoken.security.WeakKeyException`.

```java
// BROKEN (current code):
byte[] keyBytes = Decoders.BASE64.decode(
    java.util.Base64.getEncoder().encodeToString(secret.getBytes()) // encode then decode = wrong!
);
```

**Permanent Fix:**
```java
private SecretKey getSigningKey() {
    byte[] keyBytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    if (keyBytes.length < 32) {
        keyBytes = java.util.Arrays.copyOf(keyBytes, 32); // pad to min 32 bytes for HS256
    }
    return Keys.hmacShaKeyFor(keyBytes);
}
```

---

### BUG-002 — Spontaneous Logout / 401 Mid-Session [CRITICAL]

**Component:** `api.ts` response interceptor + missing refresh-token flow

**Exact Error:**
```
GET https://nexastudio-29np.onrender.com/api/projects/{id}/prompts → 401 Unauthorized
Console: AxiosError: Authentication failed.
```
User is silently redirected to `/login` mid-session, losing all progress.

**Root Causes:**
1. `api.ts` line 34: Calls `logout()` + redirect on **any** 401 with zero retry.
2. No refresh-token flow on frontend despite backend having `/api/auth/refresh`.
3. JWT `expiration` env var may be too short (< AI generation time).

**Permanent Fix — `api.ts`:**
```typescript
api.interceptors.response.use(
  (response) => {
    if (response.data && 'data' in response.data) response.data = response.data.data;
    return response;
  },
  async (error) => {
    const original = error.config;
    if (error.response?.status === 401 && !original._retry) {
      original._retry = true;
      const rt = localStorage.getItem('nexa_refresh_token');
      if (rt) {
        try {
          const res = await axios.post(`${API_BASE_URL}/auth/refresh`, { refreshToken: rt });
          const newToken = res.data?.data?.accessToken;
          if (newToken) {
            localStorage.setItem('nexa_token', newToken);
            original.headers.Authorization = `Bearer ${newToken}`;
            return api(original); // retry original request
          }
        } catch { /* fall through to logout */ }
      }
      useAuthStore.getState().logout();
      if (window.location.pathname !== '/login') window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

**Also fix `store.ts` login action to store refresh token:**
```typescript
login: (user, token, refreshToken?) => {
  localStorage.setItem('nexa_token', token);
  if (refreshToken) localStorage.setItem('nexa_refresh_token', refreshToken);
  set({ user, token, isAuthenticated: true });
},
```

**Also increase JWT expiry in `application.properties`:**
```properties
jwt.expiration=28800000        # 8 hours
jwt.refresh-expiration=604800000  # 7 days
```

---

### BUG-003 — 403 Forbidden on Newly Created Projects [CRITICAL]

**Component:** Backend project ownership check

**Exact Errors:**
```
GET https://nexastudio-29np.onrender.com/api/projects/{id} → 403 Forbidden
GET https://nexastudio-29np.onrender.com/api/projects/{id}/files → 403 Forbidden
```

**Root Cause:**
The JWT `userId` claim is malformed due to BUG-001, so `JwtUtil.extractUserId()` returns `null`. The backend stores `null` as the project owner. When any subsequent request tries to verify `project.getUserId().equals(currentUserId)`, it fails (NullPointerException or false), returning 403.

**Permanent Fix:**
1. Fix BUG-001 first — this fixes JWT claim extraction.
2. Add null-safety in backend `ProjectService.java`:
```java
if (project.getUserId() == null) {
    log.error("Project {} has null owner — data integrity issue", projectId);
    throw new AccessDeniedException("Project owner data is corrupt. Contact support.");
}
if (!project.getUserId().equals(currentUserId)) {
    throw new AccessDeniedException("Access denied.");
}
```
3. Write a DB migration to set userId on any null-owner projects using audit logs.

---

### BUG-004 — ProjectDetailPage Silently Redirects on Any Error [Medium]

**Component:** `ProjectDetailPage.tsx` lines 107–109

**Exact Code:**
```typescript
} catch (err) {
  console.error('Failed to load project:', err);
  navigate('/projects'); // silent dump with zero user feedback
}
```

**Permanent Fix:**
```typescript
} catch (err: unknown) {
  const status = (err as AxiosError)?.response?.status;
  if (status === 404) {
    setNotFound(true);
  } else if (status === 403) {
    toast.error('You do not have access to this project.');
    navigate('/projects');
  } else {
    toast.error('Failed to load project. Please try again.');
    setIsLoading(false); // stay on page so user can retry
  }
}
```

---

### BUG-005 — Wizard Step 2: Single-Select Framework (No Multi-Select) [Medium]

**Component:** `NewProjectPage.tsx` Step 2

**Description:**
Framework selection is radio-selection only (`form.type: string`). Clicking a second framework deselects the first. The product intends "5 frameworks per project" but the UI only allows 1.

**Code (lines 175, 181):**
```typescript
const selected = form.type === fw.id;           // single value check
onClick={() => setForm({ ...form, type: fw.id })} // overwrites, doesn't append
```

**Permanent Fix:**
```typescript
// Change form state type: string → types: string[]
types: [] as string[],

// Click handler:
onClick={() => setForm({
  ...form,
  types: form.types.includes(fw.id)
    ? form.types.filter(id => id !== fw.id)
    : [...form.types, fw.id],
})}

// canNext:
if (step === 2) return form.types.length > 0;

// POST — primary type + append others to prompt:
await projectsApi.create({
  name: form.name,
  description: form.description,
  type: form.types[0],
  initialPrompt: form.types.length > 1
    ? `Build using these technologies: ${form.types.join(', ')}. ` + (enhancedPrompt || '')
    : enhancedPrompt || undefined,
});
```

---

### BUG-006 — No Live Preview / Preview Button Missing [High]

**Component:** `ProjectDetailPage.tsx`

**Description:**
The project detail view shows only a read-only `<pre>` code viewer. There is no live preview iframe, no "Open in browser" button. The `Project` type already has `previewUrl?: string` but it is never rendered. Users cannot verify if generated code actually works.

**Permanent Fix — add a Preview tab:**
```tsx
// Tab switcher
<div className="flex gap-2 mb-4">
  <button onClick={() => setTab('code')}>Code</button>
  <button onClick={() => setTab('preview')}>Preview</button>
</div>

// Preview panel (for Vanilla/HTML projects)
{tab === 'preview' && indexHtmlContent && (
  <iframe
    title="Live Preview"
    srcDoc={indexHtmlContent}
    sandbox="allow-scripts"
    className="w-full h-[580px] rounded-xl border border-lilac-200/10 bg-white"
  />
)}

// For React/Vue — link to StackBlitz/CodeSandbox using generated files
{tab === 'preview' && currentProject?.previewUrl && (
  <a href={currentProject.previewUrl} target="_blank" rel="noopener noreferrer">
    <Button>Open Live Preview ↗</Button>
  </a>
)}
```

---

### BUG-007 — Generation State Lost on Navigation [High]

**Component:** `NewProjectPage.tsx` → `GeneratePage`

**Description:**
After creating a project, the user is immediately navigated to `/projects/{id}/generate?prompt=...`. If the user is auto-logged-out (BUG-002) mid-generation, or navigates away, the SSE stream is aborted and the project remains empty (0 files). Re-opening the project shows an empty file tree with no indication that generation was interrupted.

**Permanent Fix:**
```typescript
// GeneratePage.tsx on mount:
localStorage.setItem(`nexa_gen_${id}`, 'pending');
const abort = projectsApi.generateStream(id, prompt, intent, {
  onComplete: () => localStorage.removeItem(`nexa_gen_${id}`),
  onError: (msg) => localStorage.setItem(`nexa_gen_${id}`, `error:${msg}`),
});
return () => abort();

// ProjectDetailPage.tsx on mount — check for interrupted generation:
const pending = localStorage.getItem(`nexa_gen_${id}`);
if (pending === 'pending') {
  // Show banner: "Generation was interrupted. Click 'AI Generate' to resume."
  setShowResumeBanner(true);
}
```

---

### BUG-008 — THREE.WebGLRenderer: Context Lost [Low]

**Component:** Landing page Three.js background animation

**Exact Console Warning:**
```
THREE.WebGLRenderer: Context Lost.
```
Background 3D animation disappears permanently after tab is backgrounded and re-focused.

**Permanent Fix:**
```typescript
renderer.domElement.addEventListener('webglcontextlost', (e) => {
  e.preventDefault();
});
renderer.domElement.addEventListener('webglcontextrestored', () => {
  initScene(); // re-initialize renderer on restore
});
```

---

### BUG-009 — CDN Tailwind Used in Production [Low]

**Component:** `index.html`

**Exact Console Warning:**
```
cdn.tailwindcss.com should not be used in production.
```
Adds ~300KB runtime overhead, disables JIT purging, slows first paint.

**Permanent Fix:**
Remove the CDN `<script>` from `index.html`. Add Tailwind as a PostCSS plugin in `vite.config.ts` with proper content path configuration and JIT compilation.

---

## 📊 Project Creation & Generation Matrix

| Project | Created | Generation Started | Files Generated | Preview Works |
|---|---|---|---|---|
| E-Commerce Platform | ✅ | ⚠️ (redirected to /generate) | ❓ (403 on re-entry) | ❌ |
| AI Dashboard | ❌ (401 logout loop) | ❌ | ❌ | ❌ |
| Real-time Chat App | ❌ | ❌ | ❌ | ❌ |
| Mobile Banking App | ❌ | ❌ | ❌ | ❌ |
| DevOps Pipeline Tool | ❌ | ❌ | ❌ | ❌ |

**Root blocker chain:** BUG-001 → BUG-002 → BUG-003. Fix these three in order to unlock the full flow.

---

## 🔧 Priority Fix Order

| Priority | Bug | File | Impact |
|---|---|---|---|
| **P0** | BUG-001: JWT key double-encoding | `JwtUtil.java:159` | Fixes login 500s |
| **P0** | BUG-002: No refresh-token retry | `api.ts:34` + `store.ts` | Fixes mid-session logout |
| **P1** | BUG-003: Null userId ownership | `ProjectService.java` | Fixes 403 on new projects |
| **P1** | BUG-006: No live preview | `ProjectDetailPage.tsx` | Enables generation verification |
| **P2** | BUG-007: Lost generation state | `GeneratePage.tsx` | Prevents silent data loss |
| **P2** | BUG-005: Multi-select frameworks | `NewProjectPage.tsx:175` | Core product feature |
| **P3** | BUG-004: Silent redirects | `ProjectDetailPage.tsx:107` | UX clarity |
| **P4** | BUG-008: WebGL context loss | Landing page Three.js | Visual polish |
| **P4** | BUG-009: CDN Tailwind | `index.html` | Performance |

---

## 🌐 Network Failures

| Endpoint | Method | Status | Root Cause |
|---|---|---|---|
| `/api/auth/login` | POST | 500 | JWT signing key double-encoding |
| `/api/projects/{id}` | GET | 403 | Null userId stored as project owner |
| `/api/projects/{id}/files` | GET | 403 | Same |
| `/api/projects/{id}/prompts` | GET | 401 | Token expired, no refresh retry |

---

## 📋 Console Errors

| Error | Source |
|---|---|
| `THREE.WebGLRenderer: Context Lost.` | Landing Three.js animation |
| `cdn.tailwindcss.com should not be used in production` | `index.html` |
| `AxiosError: Authentication failed.` | `api.ts` interceptor on 401 |
| `Failed to load project: AxiosError 403` | `ProjectDetailPage.tsx:108` |

---

*All fixes are traceable to exact source file + line numbers in the `frontend-react/` and `backend-java/` directories.*
