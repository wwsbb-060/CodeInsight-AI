// ===== API 统一响应 =====
export interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

// ===== 用户 =====
export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email?: string;
}

export interface LoginResponse {
  token: string;
  username: string;
}

// ===== 仓库 =====
export type RepoStatus = 'PENDING' | 'CLONING' | 'READY' | 'ERROR';

export interface RepositoryVO {
  id: number;
  userId: number;
  name: string;
  url: string;
  branch: string;
  status: RepoStatus;
  errorMsg?: string;
  fileCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateRepoRequest {
  url: string;
  branch?: string;
}

// ===== 评审 =====
export type ReviewStatus = 'PENDING' | 'ANALYZING' | 'COMPLETED' | 'ERROR';

export interface ReviewVO {
  id: number;
  repositoryId: number;
  userId: number;
  status: ReviewStatus;
  summary?: string;
  reportMarkdown?: string;
  memo?: string;
  digest?: string;
  aiModel?: string;
  tokenUsed: number;
  errorMsg?: string;
  createdAt: string;
  completedAt?: string;
}

export interface CreateReviewRequest {
  repositoryId: number;
}

// ===== Q&A =====
export interface QaRequest {
  question: string;
}

export interface QaReference {
  file: string;
  startLine: number;
  endLine: number;
  snippet: string;
}

export interface QaResponse {
  answer: string;
  references: QaReference[];
}

export interface ChatMessage {
  role: 'user' | 'ai';
  content: string;
  references?: QaReference[];
}

// ===== 路由 =====
export interface UserInfo {
  username: string;
  token: string;
}
