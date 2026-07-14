import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { login as loginApi, register as registerApi, logout as logoutApi } from '@/api/auth';
import type { LoginRequest, RegisterRequest, UserInfo } from '@/types';

const USER_KEY = 'user';

export function getUser(): UserInfo | null {
  const raw = localStorage.getItem(USER_KEY);
  return raw ? JSON.parse(raw) : null;
}

export function useLogin() {
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (data: LoginRequest) => loginApi(data),
    onSuccess: (res) => {
      if (res.code === 200 && res.data) {
        localStorage.setItem(USER_KEY, JSON.stringify(res.data));
        navigate('/dashboard');
      }
    },
  });
}

export function useRegister() {
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (data: RegisterRequest) => registerApi(data),
    onSuccess: (res) => {
      if (res.code === 200) {
        navigate('/login');
      }
    },
  });
}

export function useLogout() {
  const navigate = useNavigate();

  return useMutation({
    mutationFn: () => logoutApi(),
    onSettled: () => {
      localStorage.removeItem(USER_KEY);
      navigate('/login');
    },
  });
}
