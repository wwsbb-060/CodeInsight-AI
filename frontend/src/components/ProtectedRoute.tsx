import { Navigate } from 'react-router-dom';
import { getUser } from '@/hooks/useAuth';
import type { ReactNode } from 'react';

export default function ProtectedRoute({ children }: { children: ReactNode }) {
  const user = getUser();

  if (!user?.token) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}
