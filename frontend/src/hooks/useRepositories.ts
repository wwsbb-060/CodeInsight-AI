import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { createRepo, getRepoList, getRepoById, deleteRepo } from '@/api/repository';
import type { CreateRepoRequest } from '@/types';

export function useRepoList() {
  return useQuery({
    queryKey: ['repositories'],
    queryFn: () => getRepoList().then((res) => res.data ?? []),
    // 有仓库在 Clone 中时自动轮询，每 3 秒刷新列表
    refetchInterval: (query) => {
      const repos = query.state.data;
      if (!repos || repos.length === 0) return false;
      const hasCloning = repos.some(
        (r) => r.status === 'PENDING' || r.status === 'CLONING',
      );
      return hasCloning ? 3000 : false;
    },
  });
}

export function useRepo(id: number) {
  return useQuery({
    queryKey: ['repository', id],
    queryFn: () => getRepoById(id).then((res) => res.data),
    enabled: id > 0,
    // Clone 中自动轮询，每 3 秒刷新
    refetchInterval: (query) => {
      const status = query.state.data?.status;
      return status === 'PENDING' || status === 'CLONING' ? 3000 : false;
    },
  });
}

export function useCreateRepo() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateRepoRequest) => createRepo(data),
    onSuccess: (res) => {
      if (res.code === 200) {
        qc.invalidateQueries({ queryKey: ['repositories'] });
      }
    },
  });
}

export function useDeleteRepo() {
  const qc = useQueryClient();

  return useMutation({
    mutationFn: (id: number) => deleteRepo(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['repositories'] });
    },
  });
}
