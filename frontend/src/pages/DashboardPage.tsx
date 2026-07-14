import { useState } from 'react';
import {
  Card,
  Input,
  Button,
  Table,
  Tag,
  Space,
  Popconfirm,
  App,
} from 'antd';
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import { useRepoList, useCreateRepo, useDeleteRepo } from '@/hooks/useRepositories';
import { useCreateReview } from '@/hooks/useReview';
import type { RepositoryVO } from '@/types';

const statusMap: Record<string, { color: string; text: string }> = {
  PENDING: { color: 'default', text: '等待中' },
  CLONING: { color: 'processing', text: 'Clone 中' },
  READY: { color: 'success', text: '就绪' },
  ERROR: { color: 'error', text: '失败' },
};

export default function DashboardPage() {
  const [url, setUrl] = useState('');
  const [reviewingRepoId, setReviewingRepoId] = useState<number | null>(null);
  const { message } = App.useApp();

  const { data: repos = [], isLoading, refetch } = useRepoList();
  const createRepo = useCreateRepo();
  const deleteRepo = useDeleteRepo();
  const createReview = useCreateReview();

  const handleImport = () => {
    if (!url.trim()) {
      message.warning('请输入 GitHub 仓库地址');
      return;
    }
    createRepo.mutate(
      { url: url.trim() },
      {
        onSuccess: (res) => {
          if (res.code === 200) {
            message.success('仓库导入成功，正在 Clone...');
            setUrl('');
            refetch();
          } else {
            message.error(res.message);
          }
        },
        onError: () => message.error('导入失败，请检查仓库地址是否正确'),
      },
    );
  };

  const handleReview = (repoId: number) => {
    setReviewingRepoId(repoId);
    createReview.mutate(repoId, {
      onSuccess: (res) => {
        setReviewingRepoId(null);
        if (res.code === 200 && res.data) {
          message.success('评审任务已创建，请前往左侧「分析报告」查看进度');
        } else {
          message.error(res.message ?? '创建评审失败');
        }
      },
      onError: () => {
        setReviewingRepoId(null);
        message.error('创建评审失败，请确认仓库已 Clone 完成');
      },
    });
  };

  const columns = [
    {
      title: '仓库名称',
      dataIndex: 'name',
      key: 'name',
      ellipsis: true,
    },
    {
      title: '分支',
      dataIndex: 'branch',
      key: 'branch',
      width: 100,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (s: string) => {
        const item = statusMap[s] ?? { color: 'default', text: s };
        return <Tag color={item.color}>{item.text}</Tag>;
      },
    },
    {
      title: '操作',
      key: 'actions',
      width: 220,
      render: (_: unknown, record: RepositoryVO) => (
        <Space>
          {record.status === 'READY' && (
            <Button
              type="link"
              size="small"
              loading={reviewingRepoId === record.id}
              disabled={reviewingRepoId !== null}
              onClick={() => handleReview(record.id)}
            >
              {reviewingRepoId === record.id ? '创建中...' : '开始评审'}
            </Button>
          )}
          <Popconfirm
            title="确定删除此仓库？"
            onConfirm={() => deleteRepo.mutate(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" size="small" danger>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <>
      {/* 导入区域 */}
      <Card style={{ marginBottom: 16 }}>
        <Space.Compact style={{ width: '100%' }}>
          <Input
            placeholder="输入 GitHub 仓库地址，例如 https://github.com/user/repo.git"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            onPressEnter={handleImport}
            size="large"
          />
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={handleImport}
            loading={createRepo.isPending}
            size="large"
          >
            导入
          </Button>
        </Space.Compact>
      </Card>

      {/* 仓库列表 */}
      <Card
        title="我的仓库"
        extra={
          <Button
            icon={<ReloadOutlined />}
            onClick={async () => {
              await refetch();
              message.success('刷新成功');
            }}
            loading={isLoading}
          >
            刷新
          </Button>
        }
      >
        <Table
          columns={columns}
          dataSource={repos}
          rowKey="id"
          loading={isLoading}
          pagination={false}
          locale={{ emptyText: '暂无仓库，上方输入 GitHub 地址开始导入' }}
        />
      </Card>
    </>
  );
}
