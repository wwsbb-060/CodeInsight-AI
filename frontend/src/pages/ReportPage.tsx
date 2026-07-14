import { useParams, useNavigate } from 'react-router-dom';
import { Button, Result, Space, Typography, Tag, Spin } from 'antd';
import { ArrowLeftOutlined, DownloadOutlined } from '@ant-design/icons';
import { useReview } from '@/hooks/useReview';
import { getReviewReport } from '@/api/review';
import MarkdownViewer from '@/components/MarkdownViewer';

const { Text } = Typography;

const statusMap: Record<string, { color: string; text: string }> = {
  PENDING: { color: 'default', text: '等待中' },
  ANALYZING: { color: 'processing', text: '分析中' },
  COMPLETED: { color: 'success', text: '已完成' },
  ERROR: { color: 'error', text: '失败' },
};

export default function ReportPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const reviewId = Number(id);

  const { data: review, isLoading, error } = useReview(reviewId);

  const handleDownload = async () => {
    try {
      const markdown = await getReviewReport(reviewId);
      const blob = new Blob([markdown], { type: 'text/markdown' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `code-review-report-${reviewId}.md`;
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      // ignore
    }
  };

  // 初始加载
  if (isLoading || !review) {
    return (
      <div style={{ textAlign: 'center', padding: 48 }}>
        <Spin size="large" />
      </div>
    );
  }

  // 出错
  if (error || review.status === 'ERROR') {
    return (
      <Result
        status="error"
        title="评审失败"
        subTitle={review?.errorMsg || '未知错误'}
        extra={
          <Button onClick={() => navigate('/reports')}>返回报告列表</Button>
        }
      />
    );
  }

  const statusItem = statusMap[review.status] ?? { color: 'default', text: review.status };

  return (
    <>
      {/* 顶部操作栏 */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/reports')}>
            返回报告列表
          </Button>
          <Tag color={statusItem.color}>{statusItem.text}</Tag>
          {review.aiModel && <Text type="secondary">模型：{review.aiModel}</Text>}
          <Text type="secondary">Token：{review.tokenUsed}</Text>
        </Space>

        {review.status === 'COMPLETED' && (
          <Button icon={<DownloadOutlined />} onClick={handleDownload}>
            下载 Markdown
          </Button>
        )}
      </div>

      {/* 分析中 / 等待中 */}
      {(review.status === 'PENDING' || review.status === 'ANALYZING') && (
        <div style={{ textAlign: 'center', padding: '60px 0', color: '#999' }}>
          <Spin size="default" style={{ marginRight: 12 }} />
          正在分析仓库代码，请稍候... 状态自动刷新
        </div>
      )}

      {/* 已完成 — 渲染报告 */}
      {review.status === 'COMPLETED' && (
        <MarkdownViewer content={review.reportMarkdown ?? '*暂无报告内容*'} />
      )}
    </>
  );
}
