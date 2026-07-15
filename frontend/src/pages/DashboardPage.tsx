import { Card, Col, Row, Statistic, Timeline, Tag, Spin, Typography } from 'antd';
import {
  DatabaseOutlined,
  FileTextOutlined,
  ThunderboltOutlined,
  CalendarOutlined,
} from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { getOverview, getTokenTrend, getActivity, type ActivityItem } from '@/api/stats';
import ReactECharts from 'echarts-for-react';

const { Text } = Typography;

const statusMap: Record<string, { color: string; text: string }> = {
  COMPLETED: { color: 'success', text: '完成' },
  ANALYZING: { color: 'processing', text: '分析中' },
  PENDING: { color: 'default', text: '等待中' },
  ERROR: { color: 'error', text: '失败' },
};

export default function DashboardPage() {
  const { data: overview } = useQuery({
    queryKey: ['stats', 'overview'],
    queryFn: () => getOverview().then((r) => r.data),
  });

  const { data: trend = [] } = useQuery({
    queryKey: ['stats', 'tokens'],
    queryFn: () => getTokenTrend().then((r) => r.data ?? []),
  });

  const { data: activity = [], isLoading: activityLoading } = useQuery({
    queryKey: ['stats', 'activity'],
    queryFn: () => getActivity().then((r) => r.data ?? []),
  });

  const chartOption = {
    tooltip: { trigger: 'axis' as const },
    xAxis: {
      type: 'category' as const,
      data: trend.map((t) => t.date),
    },
    yAxis: { type: 'value' as const, name: 'Token' },
    series: [
      {
        data: trend.map((t) => t.tokens),
        type: 'line' as const,
        smooth: true,
        areaStyle: { opacity: 0.15 },
        itemStyle: { color: '#1677ff' },
      },
    ],
    grid: { left: 50, right: 20, top: 20, bottom: 30 },
  };

  return (
    <>
      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="总仓库"
              value={overview?.totalRepos ?? '-'}
              prefix={<DatabaseOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="总报告"
              value={overview?.totalReviews ?? '-'}
              prefix={<FileTextOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="本月分析"
              value={overview?.monthReviews ?? '-'}
              prefix={<CalendarOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="累计 Token"
              value={overview?.totalTokens?.toLocaleString() ?? '-'}
              prefix={<ThunderboltOutlined />}
            />
          </Card>
        </Col>
      </Row>

      {/* Token 趋势图 */}
      <Card title="Token 消耗趋势（近 7 天）" style={{ marginBottom: 16 }}>
        {trend.length > 0 ? (
          <ReactECharts option={chartOption} style={{ height: 260 }} />
        ) : (
          <div style={{ textAlign: 'center', color: '#ccc', padding: 40 }}>
            暂无数据
          </div>
        )}
      </Card>

      {/* 最近活动 */}
      <Card title="最近分析记录">
        {activityLoading ? (
          <Spin />
        ) : activity.length > 0 ? (
          <Timeline
            items={activity.map((a: ActivityItem) => {
              const s = statusMap[a.status] ?? { color: 'default', text: a.status };
              return {
                color: s.color === 'success' ? 'green' : s.color === 'error' ? 'red' : 'blue',
                children: (
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                      <Text strong>{a.repoName}</Text>
                      <Tag color={s.color}>{s.text}</Tag>
                      {a.aiModel && <Text type="secondary" style={{ fontSize: 12 }}>{a.aiModel}</Text>}
                      {a.tokenUsed > 0 && (
                        <Text type="secondary" style={{ fontSize: 12 }}>
                          {a.tokenUsed.toLocaleString()} Token
                        </Text>
                      )}
                    </div>
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      {a.createdAt?.replace('T', ' ').substring(0, 19)}
                    </Text>
                    {a.errorMsg && (
                      <div>
                        <Text type="danger" style={{ fontSize: 12 }}>{a.errorMsg}</Text>
                      </div>
                    )}
                  </div>
                ),
              };
            })}
          />
        ) : (
          <div style={{ textAlign: 'center', color: '#ccc', padding: 24 }}>
            暂无记录
          </div>
        )}
      </Card>
    </>
  );
}
