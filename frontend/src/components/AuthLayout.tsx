import { Outlet } from 'react-router-dom';
import { Layout, Typography } from 'antd';
import { CodeOutlined } from '@ant-design/icons';

const { Content } = Layout;
const { Title } = Typography;

export default function AuthLayout() {
  return (
    <Layout style={{ minHeight: '100vh', background: '#f0f2f5' }}>
      <Content
        style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          padding: 24,
        }}
      >
        {/* Logo 区域 */}
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <CodeOutlined style={{ fontSize: 48, color: '#1677ff' }} />
          <Title level={2} style={{ marginTop: 12, marginBottom: 4 }}>
            CodeInsight AI
          </Title>
          <span style={{ color: '#888' }}>智能代码评审平台</span>
        </div>

        {/* 表单卡片 */}
        <div
          style={{
            width: 400,
            background: '#fff',
            borderRadius: 8,
            padding: '32px 32px 16px',
            boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
          }}
        >
          <Outlet />
        </div>
      </Content>
    </Layout>
  );
}
