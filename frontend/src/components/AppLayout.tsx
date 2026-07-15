import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { Layout, Menu, Button, Dropdown } from 'antd';
import {
  DashboardOutlined,
  AppstoreOutlined,
  FileTextOutlined,
  CodeOutlined,
  LogoutOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { getUser, useLogout } from '@/hooks/useAuth';

const { Header, Sider, Content } = Layout;

const menuItems = [
  { key: '/dashboard', icon: <DashboardOutlined />, label: '仪表盘' },
  { key: '/workspace', icon: <AppstoreOutlined />, label: '工作台' },
  { key: '/reports', icon: <FileTextOutlined />, label: '分析报告' },
];

export default function AppLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const logout = useLogout();
  const user = getUser();

  const selectedKey = (() => {
    if (location.pathname.startsWith('/reports/') || location.pathname === '/reports') return '/reports';
    if (location.pathname.startsWith('/workspace')) return '/workspace';
    return location.pathname;
  })();

  return (
    <Layout style={{ minHeight: '100vh' }}>
      {/* 侧边栏 */}
      <Sider
        width={220}
        theme="dark"
        style={{
          overflow: 'auto',
          position: 'fixed',
          left: 0,
          top: 0,
          bottom: 0,
        }}
      >
        {/* Logo */}
        <div
          style={{
            height: 64,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            borderBottom: '1px solid rgba(255,255,255,0.1)',
          }}
        >
          <CodeOutlined style={{ fontSize: 24, color: '#1677ff', marginRight: 8 }} />
          <span style={{ color: '#fff', fontSize: 16, fontWeight: 600 }}>
            CodeInsight AI
          </span>
        </div>

        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[selectedKey]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
          style={{ marginTop: 8 }}
        />
      </Sider>

      {/* 右侧主体 */}
      <Layout style={{ marginLeft: 220 }}>
        {/* 顶栏 */}
        <Header
          style={{
            background: '#fff',
            display: 'flex',
            justifyContent: 'flex-end',
            alignItems: 'center',
            padding: '0 24px',
            boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
            position: 'sticky',
            top: 0,
            zIndex: 10,
          }}
        >
          <Dropdown
            menu={{
              items: [
                {
                  key: 'logout',
                  icon: <LogoutOutlined />,
                  label: '退出登录',
                  danger: true,
                },
              ],
              onClick: () => logout.mutate(),
            }}
          >
            <Button type="text" icon={<UserOutlined />}>
              {user?.username ?? '用户'}
            </Button>
          </Dropdown>
        </Header>

        {/* 内容区 */}
        <Content style={{ padding: 24, minHeight: 'calc(100vh - 64px)' }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
