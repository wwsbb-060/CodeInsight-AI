import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Table, Tag, Button, Popconfirm, Input, App, Tooltip } from 'antd';
import {
  ReloadOutlined,
  EyeOutlined,
  DeleteOutlined,
  EditOutlined,
  FileTextOutlined,
  ToolOutlined,
  WarningOutlined,
} from '@ant-design/icons';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useReviewList } from '@/hooks/useReview';
import { useRepoList } from '@/hooks/useRepositories';
import { deleteReview, updateMemo } from '@/api/review';
import type { ReviewVO } from '@/types';

// ===== 状态映射 =====
const statusMap: Record<string, { color: string; text: string }> = {
  PENDING: { color: 'default', text: '等待中' },
  ANALYZING: { color: 'processing', text: '分析中' },
  COMPLETED: { color: 'success', text: '已完成' },
  ERROR: { color: 'error', text: '失败' },
};

// ===== JSON 摘要解析（兜底兼容格式异常） =====
interface DigestData {
  overview: string;
  techStack: string;
  findings: string[];
}

function parseDigest(raw?: string): DigestData | null {
  if (!raw) return null;
  try {
    const obj = JSON.parse(raw);
    if (obj && typeof obj.overview === 'string') return obj;
  } catch {
    // 忽略解析错误，返回 null 走兜底
  }
  return null;
}

// ===== 页面组件 =====
export default function ReportsPage() {
  const navigate = useNavigate();
  const { message } = App.useApp();
  const qc = useQueryClient();

  const { data: reviews = [], isLoading, refetch } = useReviewList();
  const { data: repos = [] } = useRepoList();

  // 备注编辑状态
  const [memoState, setMemoState] = useState<{
    reviewId: number;
    text: string;
  } | null>(null);

  // 仓库 ID → 仓库名
  const repoNameMap = useMemo(() => {
    const map: Record<number, string> = {};
    repos.forEach((r) => { map[r.id] = r.name; });
    return map;
  }, [repos]);

  // 删除
  const delMutation = useMutation({
    mutationFn: (id: number) => deleteReview(id),
    onSuccess: () => {
      message.success('已删除');
      qc.invalidateQueries({ queryKey: ['reviews'] });
    },
    onError: () => message.error('删除失败'),
  });

  // 保存备注
  const saveMemo = useMutation({
    mutationFn: ({ id, memo }: { id: number; memo: string }) => updateMemo(id, memo),
    onSuccess: () => {
      message.success('备注已保存');
      setMemoState(null);
      qc.invalidateQueries({ queryKey: ['reviews'] });
    },
    onError: () => message.error('保存失败'),
  });

  // ===== 表格列 =====
  const columns = [
    {
      title: '仓库名称',
      dataIndex: 'repositoryId',
      key: 'repo',
      render: (repoId: number) => repoNameMap[repoId] ?? `仓库 #${repoId}`,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (s: string) => {
        const item = statusMap[s] ?? { color: 'default', text: s };
        return <Tag color={item.color}>{item.text}</Tag>;
      },
    },
    {
      title: 'AI 模型',
      dataIndex: 'aiModel',
      key: 'model',
      render: (v: string | null) => v ?? '-',
    },
    {
      title: 'Token',
      dataIndex: 'tokenUsed',
      key: 'token',
      render: (v: number) => v?.toLocaleString() ?? '-',
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'time',
      render: (v: string) => v?.replace('T', ' ').substring(0, 19) ?? '-',
    },
    {
      title: '操作',
      key: 'actions',
      width: 140,
      render: (_: unknown, record: ReviewVO) => (
        <Button.Group size="small">
          <Tooltip title="查看完整报告">
            <Button
              type="link"
              size="small"
              icon={<EyeOutlined />}
              onClick={() => navigate(`/reports/${record.id}`)}
            />
          </Tooltip>
          <Popconfirm
            title="确定删除此报告？"
            onConfirm={() => delMutation.mutate(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Tooltip title="删除报告">
              <Button type="link" size="small" danger icon={<DeleteOutlined />} />
            </Tooltip>
          </Popconfirm>
        </Button.Group>
      ),
    },
  ];

  // ===== 展开行：摘要 + 备注 =====
  const expandedRowRender = (record: ReviewVO) => {
    const digest = parseDigest(record.digest);
    const isEditing = memoState?.reviewId === record.id;

    return (
      <div style={{ padding: '8px 0' }}>
        {/* 摘要卡片 */}
        {digest ? (
          <div style={{ display: 'flex', gap: 16, marginBottom: 16 }}>
            {/* 项目概述 */}
            <div
              style={{
                flex: 1,
                background: '#f6ffed',
                borderRadius: 8,
                padding: '12px 16px',
                borderLeft: '4px solid #52c41a',
              }}
            >
              <div style={{ color: '#52c41a', fontWeight: 600, marginBottom: 6 }}>
                <FileTextOutlined style={{ marginRight: 6 }} />
                项目概述
              </div>
              <div style={{ color: '#333', lineHeight: 1.6, fontSize: 13 }}>
                {digest.overview}
              </div>
            </div>

            {/* 技术栈 */}
            <div
              style={{
                width: 260,
                background: '#e6f4ff',
                borderRadius: 8,
                padding: '12px 16px',
                borderLeft: '4px solid #1677ff',
              }}
            >
              <div style={{ color: '#1677ff', fontWeight: 600, marginBottom: 6 }}>
                <ToolOutlined style={{ marginRight: 6 }} />
                技术栈
              </div>
              <div style={{ color: '#333', lineHeight: 1.6, fontSize: 13 }}>
                {digest.techStack}
              </div>
            </div>

            {/* 主要发现 */}
            <div
              style={{
                width: 300,
                background: '#fff7e6',
                borderRadius: 8,
                padding: '12px 16px',
                borderLeft: '4px solid #fa8c16',
              }}
            >
              <div style={{ color: '#fa8c16', fontWeight: 600, marginBottom: 6 }}>
                <WarningOutlined style={{ marginRight: 6 }} />
                主要发现
              </div>
              {digest.findings.length > 0 ? (
                <ul style={{ margin: 0, paddingLeft: 18, color: '#333', fontSize: 13, lineHeight: 1.8 }}>
                  {digest.findings.map((f, i) => (
                    <li key={i}>{f}</li>
                  ))}
                </ul>
              ) : (
                <span style={{ color: '#999', fontSize: 13 }}>暂无</span>
              )}
            </div>
          </div>
        ) : (
          <div style={{ marginBottom: 12, color: '#999', fontSize: 13 }}>
            摘要暂未生成，请等待评审完成
          </div>
        )}

        {/* 备注区 */}
        <div
          style={{
            display: 'flex',
            alignItems: 'flex-start',
            gap: 8,
            padding: '8px 0',
            borderTop: '1px solid #f0f0f0',
          }}
        >
          <span style={{ color: '#888', whiteSpace: 'nowrap', lineHeight: '32px' }}>
            📝 备注：
          </span>

          {isEditing ? (
            <>
              <Input.TextArea
                value={memoState!.text}
                onChange={(e) =>
                  setMemoState({ ...memoState!, text: e.target.value })
                }
                rows={2}
                maxLength={500}
                style={{ flex: 1 }}
                placeholder="输入备注内容..."
              />
              <Button
                type="primary"
                size="small"
                loading={saveMemo.isPending}
                onClick={() =>
                  saveMemo.mutate({ id: record.id, memo: memoState!.text })
                }
              >
                保存
              </Button>
              <Button size="small" onClick={() => setMemoState(null)}>
                取消
              </Button>
            </>
          ) : record.memo ? (
            <>
              <span style={{ flex: 1, color: '#333', lineHeight: '32px' }}>
                {record.memo}
              </span>
              <Button
                type="link"
                size="small"
                icon={<EditOutlined />}
                onClick={() =>
                  setMemoState({ reviewId: record.id, text: record.memo! })
                }
              >
                修改
              </Button>
            </>
          ) : (
            <>
              <span style={{ flex: 1, color: '#ccc', lineHeight: '32px' }}>
                暂无备注
              </span>
              <Button
                type="link"
                size="small"
                icon={<EditOutlined />}
                onClick={() =>
                  setMemoState({ reviewId: record.id, text: '' })
                }
              >
                添加备注
              </Button>
            </>
          )}
        </div>
      </div>
    );
  };

  // ===== 渲染 =====
  return (
    <Card
      title="分析报告"
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
        dataSource={reviews}
        rowKey="id"
        loading={isLoading}
        pagination={false}
        expandable={{
          expandedRowRender,
          rowExpandable: () => true,
        }}
        locale={{ emptyText: '暂无评审记录，请先到工作台导入仓库并发起评审' }}
      />
    </Card>
  );
}
