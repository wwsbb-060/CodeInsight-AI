import { createBrowserRouter, Navigate } from 'react-router-dom';
import AuthLayout from '@/components/AuthLayout';
import AppLayout from '@/components/AppLayout';
import ProtectedRoute from '@/components/ProtectedRoute';
import LoginPage from '@/pages/LoginPage';
import RegisterPage from '@/pages/RegisterPage';
import DashboardPage from '@/pages/DashboardPage';
import ReportsPage from '@/pages/ReportsPage';
import ReportPage from '@/pages/ReportPage';

const router = createBrowserRouter([
  // 未登录态路由
  {
    element: <AuthLayout />,
    children: [
      { path: '/login', element: <LoginPage /> },
      { path: '/register', element: <RegisterPage /> },
    ],
  },

  // 已登录态路由
  {
    element: (
      <ProtectedRoute>
        <AppLayout />
      </ProtectedRoute>
    ),
    children: [
      { path: '/dashboard', element: <DashboardPage /> },
      { path: '/reports', element: <ReportsPage /> },
      { path: '/reports/:id', element: <ReportPage /> },
    ],
  },

  // 兜底重定向
  { path: '/', element: <Navigate to="/dashboard" replace /> },
  { path: '*', element: <Navigate to="/dashboard" replace /> },
]);

export default router;
