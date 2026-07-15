import { useState, useRef, useEffect } from 'react';
import { Input, Button, Tag, Typography } from 'antd';
import { SendOutlined, RobotOutlined, UserOutlined, FileTextOutlined } from '@ant-design/icons';
import { askQuestion } from '@/api/qa';
import type { ChatMessage } from '@/types';

const { Text } = Typography;

interface Props {
  reviewId: number;
}

export default function ChatPanel({ reviewId }: Props) {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, loading]);

  const handleSend = async () => {
    const question = input.trim();
    if (!question || loading) return;

    setMessages((prev) => [...prev, { role: 'user', content: question }]);
    setInput('');
    setLoading(true);

    try {
      const res = await askQuestion(reviewId, { question });
      if (res.code === 200 && res.data) {
        setMessages((prev) => [
          ...prev,
          {
            role: 'ai',
            content: res.data.answer,
            references: res.data.references ?? [],
          },
        ]);
      } else {
        setMessages((prev) => [
          ...prev,
          { role: 'ai', content: '抱歉，问答服务暂不可用：' + (res.message || '未知错误') },
        ]);
      }
    } catch {
      setMessages((prev) => [
        ...prev,
        { role: 'ai', content: '网络错误，请稍后重试。' },
      ]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ background: '#fff', borderRadius: 8, marginTop: 16, overflow: 'hidden' }}>
      {/* 标题 */}
      <div
        style={{
          padding: '12px 16px',
          borderBottom: '1px solid #f0f0f0',
          fontWeight: 600,
          display: 'flex',
          alignItems: 'center',
          gap: 8,
        }}
      >
        <RobotOutlined style={{ color: '#1677ff' }} />
        智能问答
        <Text type="secondary" style={{ fontSize: 12, fontWeight: 400 }}>
          AI 基于仓库源码回答你的问题
        </Text>
      </div>

      {/* 消息列表 */}
      <div style={{ maxHeight: 400, overflowY: 'auto', padding: 16, background: '#fafafa' }}>
        {messages.length === 0 && (
          <div style={{ textAlign: 'center', color: '#ccc', padding: 24 }}>
            向 AI 提问，了解这个项目的代码细节
          </div>
        )}

        {messages.map((msg, i) => (
          <div
            key={i}
            style={{
              display: 'flex',
              justifyContent: msg.role === 'user' ? 'flex-end' : 'flex-start',
              marginBottom: 16,
            }}
          >
            <div style={{ maxWidth: '85%' }}>
              {/* 气泡 */}
              <div
                style={{
                  padding: '10px 14px',
                  borderRadius: 12,
                  background: msg.role === 'user' ? '#1677ff' : '#fff',
                  color: msg.role === 'user' ? '#fff' : '#333',
                  border: msg.role === 'ai' ? '1px solid #e8e8e8' : 'none',
                  lineHeight: 1.6,
                  whiteSpace: 'pre-wrap',
                  wordBreak: 'break-word',
                  fontSize: 14,
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 6 }}>
                  {msg.role === 'ai' ? (
                    <RobotOutlined style={{ fontSize: 12, color: '#1677ff' }} />
                  ) : (
                    <UserOutlined style={{ fontSize: 12, color: '#fff' }} />
                  )}
                  <span style={{ fontSize: 11, color: msg.role === 'user' ? 'rgba(255,255,255,0.7)' : '#999' }}>
                    {msg.role === 'user' ? '你' : 'AI'}
                  </span>
                </div>
                {msg.content}
              </div>

              {/* 引用 */}
              {msg.references && msg.references.length > 0 && (
                <div style={{ marginTop: 6 }}>
                  {msg.references.map((ref, j) => (
                    <Tag
                      key={j}
                      icon={<FileTextOutlined />}
                      color="blue"
                      style={{ marginBottom: 4, fontSize: 11 }}
                    >
                      {ref.file} L{ref.startLine}-{ref.endLine}
                    </Tag>
                  ))}
                </div>
              )}
            </div>
          </div>
        ))}

        {/* loading */}
        {loading && (
          <div style={{ display: 'flex', justifyContent: 'flex-start', marginBottom: 16 }}>
            <div style={{ padding: 10, borderRadius: 12, background: '#e6f4ff', color: '#1677ff', fontSize: 14 }}>
              AI 思考中...
            </div>
          </div>
        )}

        <div ref={bottomRef} />
      </div>

      {/* 输入区 */}
      <div style={{ padding: '12px 16px', borderTop: '1px solid #f0f0f0', display: 'flex', gap: 8 }}>
        <Input.TextArea
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onPressEnter={(e) => {
            if (!e.shiftKey) {
              e.preventDefault();
              handleSend();
            }
          }}
          placeholder="输入问题，按 Enter 发送，Shift+Enter 换行"
          rows={2}
          disabled={loading}
          style={{ flex: 1 }}
        />
        <Button
          type="primary"
          icon={<SendOutlined />}
          onClick={handleSend}
          loading={loading}
          style={{ alignSelf: 'flex-end' }}
        >
          发送
        </Button>
      </div>
    </div>
  );
}
