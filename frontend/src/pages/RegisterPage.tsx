import { Link } from 'react-router-dom';
import { Form, Input, Button, App } from 'antd';
import { UserOutlined, LockOutlined, MailOutlined } from '@ant-design/icons';
import { useRegister } from '@/hooks/useAuth';
import type { RegisterRequest } from '@/types';

export default function RegisterPage() {
  const register = useRegister();
  const { message } = App.useApp();

  const onFinish = (values: RegisterRequest) => {
    register.mutate(values, {
      onSuccess: (res) => {
        if (res.code === 200) {
          message.success('注册成功，请登录');
        }
      },
      onError: () => message.error('注册失败，请重试'),
    });
  };

  return (
    <>
      <h3 style={{ textAlign: 'center', marginBottom: 24 }}>注册</h3>
      <Form onFinish={onFinish} size="large" autoComplete="off">
        <Form.Item name="username" rules={[{ required: true, message: '请输入用户名' }]}>
          <Input prefix={<UserOutlined />} placeholder="用户名" />
        </Form.Item>

        <Form.Item name="email" rules={[{ type: 'email', message: '请输入有效的邮箱' }]}>
          <Input prefix={<MailOutlined />} placeholder="邮箱（选填）" />
        </Form.Item>

        <Form.Item
          name="password"
          rules={[
            { required: true, message: '请输入密码' },
            { min: 6, message: '密码至少 6 位' },
          ]}
        >
          <Input.Password prefix={<LockOutlined />} placeholder="密码" />
        </Form.Item>

        <Form.Item>
          <Button type="primary" htmlType="submit" block loading={register.isPending}>
            注册
          </Button>
        </Form.Item>
      </Form>

      <div style={{ textAlign: 'center' }}>
        已有账号？<Link to="/login">立即登录</Link>
      </div>
    </>
  );
}
