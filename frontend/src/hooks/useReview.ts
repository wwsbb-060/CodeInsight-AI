import { useQuery, useMutation } from '@tanstack/react-query';
import { createReview, getReviewList, getReviewById } from '@/api/review';

export function useReviewList() {
  return useQuery({
    queryKey: ['reviews'],
    queryFn: () => getReviewList().then((res) => res.data ?? []),
    // 有评审进行中时自动轮询
    refetchInterval: (query) => {
      const reviews = query.state.data;
      if (!reviews || reviews.length === 0) return false;
      const hasRunning = reviews.some(
        (r) => r.status === 'PENDING' || r.status === 'ANALYZING',
      );
      return hasRunning ? 3000 : false;
    },
  });
}

export function useReview(id: number) {
  return useQuery({
    queryKey: ['review', id],
    queryFn: () => getReviewById(id).then((res) => res.data),
    enabled: id > 0,
    // AI 分析中自动轮询，每 3 秒刷新
    refetchInterval: (query) => {
      const status = query.state.data?.status;
      return status === 'PENDING' || status === 'ANALYZING' ? 3000 : false;
    },
  });
}

export function useCreateReview() {
  return useMutation({
    mutationFn: (repositoryId: number) => createReview({ repositoryId }),
  });
}
