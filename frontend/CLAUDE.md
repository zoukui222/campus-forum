# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Project Context: Campus Forum (MVP)
这是一个面向校园用户的论坛系统前端项目。目标是在1周内完成开发，核心原则是：**功能优先，代码简洁，尽量利用现有 UI 组件库，不重复造轮子。**

## 1. Tech Stack & Libraries
- **Framework**: Vue 3 (Composition API + `<script setup>`)
- **Build Tool**: Vite
- **UI Framework**: Element Plus (Primary UI source)
- **State Management**: Pinia (For User Auth & Global Settings)
- **Router**: Vue Router 4
- **HTTP Client**: Axios
- **Markdown**: `@kangc/v-md-editor/next` (VMdEditor) for editing and previewing.
- **Icons**: `@element-plus/icons-vue`
- **CSS**: SCSS (Use Element Plus utility classes where possible)

## 2. Directory Structure (Engineering Standard)
Follow this structure strictly:
- `src/api`: Encapsulate all API requests here, separated by modules (e.g., `auth.js`, `post.js`, `user.js`). **DO NOT** hardcode API calls in Vue files.
- `src/utils`: `request.js` (Axios instance with interceptors), `format.js` (Date formatting).
- `src/views`: Page-level components.
  - `layout`: Main layout (Header, Sidebar, MainContent).
  - `auth`: Login/Register pages.
  - `forum`: Post list, Post detail, Publish post.
  - `user`: Profile, Settings, Messages.
- `src/components`: Reusable components (e.g., `CommentList.vue`).
- `src/store`: Pinia stores (`userStore` for token/userInfo).

## 3. Coding Guidelines (Vue 3)
- **Composition API**: Always use `<script setup>`.
- **Reactivity**: Use `ref` for primitives, `reactive` for complex objects/forms.
- **Naming**: 
  - Components: `PascalCase` (e.g., `PostCard.vue`).
  - Files: `kebab-case` (e.g., `post-detail.vue`) or `PascalCase` (consistent within folders).
  - Variables/Functions: `camelCase`.
- **Element Plus**: 
  - Use `<el-form>` for all inputs.
  - Use `<el-message>` / `<el-notification>` for feedback.
  - Use `<el-skeleton>` for loading states.

## 4. Key Feature Implementation Specs

### 4.1 Authentication (JWT)
- Store JWT token in `localStorage` AND Pinia.
- **Axios Interceptor**:
  - Request: Add `Authorization: Bearer {token}` header.
  - Response: Catch `401` errors -> Clear token -> Redirect to `/login`.

### 4.2 Markdown Editor
- Use `v-md-editor` in editable mode for creating posts.
- Use `v-md-editor` in preview mode for displaying post details.
- Image Upload: Configure the editor to hook into our backend API (expecting a URL return).

### 4.3 Forum Logic
- **Post List**: Implement pagination using `<el-pagination>`.
- **Comments**: Simple flat list or 2-level nesting. Don't over-engineer.
- **Search**: Simple input triggering a query param update.

## 5. Development Workflow
1. When asked to implement a feature, verify `src/api` definition first.
2. If backend API is not provided, create a MOCK structure in `src/api` to simulate data so UI works.
3. Focus on "Happy Path" first (Main success scenario), then handle errors.

## 6. 注释规范
1. 必须使用中文注释

## 7. 不要生成文档说明，写代码前先给我一个方案，我确认方案了才开始实现

## 8. 回答我的问题必须使用中文