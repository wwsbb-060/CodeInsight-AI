import { Link } from 'react-router-dom';
import { Form, Input, Button, App } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useLogin } from '@/hooks/useAuth';
import type { LoginRequest } from '@/types';

export default function LoginPage() {
  const login = useLogin();
  const { message } = App.useApp();

  const onFinish = (values: LoginRequest) => {
    login.mutate(values, {
      onError: () => message.error('登录失败，请检查用户名和密码'),
    });
  };

  return (
    <>
      <h3 style={{ textAlign: 'center', marginBottom: 24 }}>登录</h3>
      <Form onFinish={onFinish} size="large" autoComplete="off">
        <Form.Item name="username" rules={[{ required: true, message: '请输入用户名' }]}>
          <Input prefix={<UserOutlined />} placeholder="用户名" />
        </Form.Item>

        <Form.Item name="password" rules={[{ required: true, message: '请输入密码' }]}>
          <Input.Password prefix={<LockOutlined />} placeholder="密码" />
        </Form.Item>

        <Form.Item>
          <Button type="primary" htmlType="submit" block loading={login.isPending}>
            登录
          </Button>
        </Form.Item>
      </Form>

      <div style={{ textAlign: 'center' }}>
        还没有账号？<Link to="/register">立即注册</Link>
      </div>
    </>
  );
}
