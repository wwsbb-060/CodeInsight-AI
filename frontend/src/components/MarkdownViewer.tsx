import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { oneLight } from 'react-syntax-highlighter/dist/esm/styles/prism';

interface Props {
  content: string;
}

const style = {
  wrapper: {
    background: '#fff',
    borderRadius: 10,
    padding: '40px 48px',
    maxWidth: 900,
    lineHeight: 1.85,
    fontSize: 15,
    color: '#24292f',
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif',
    boxShadow: '0 1px 4px rgba(0,0,0,0.04)',
    border: '1px solid #e8ecf0',
  } as React.CSSProperties,
};

export default function MarkdownViewer({ content }: Props) {
  return (
    <div style={style.wrapper}>
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        components={{
          h1({ children }) {
            return <h1 style={{ fontSize: 26, fontWeight: 700, margin: '0 0 8px 0', paddingBottom: 12, borderBottom: '2px solid #e8ecf0', color: '#1a1a1a' }}>{children}</h1>;
          },
          h2({ children }) {
            const text = String(children);
            const isWarning = text.includes('潜在问题') || text.includes('Bug');
            const isSuggest = text.includes('改进建议');
            const color = isWarning ? '#cf1322' : isSuggest ? '#d46b08' : '#1a1a1a';
            return (
              <h2 style={{
                fontSize: 18, fontWeight: 600, margin: '32px 0 12px 0', color,
                paddingLeft: 12, borderLeft: `3px solid ${color === '#1a1a1a' ? '#1677ff' : color}`,
              }}>
                {children}
              </h2>
            );
          },
          h3({ children }) {
            return <h3 style={{ fontSize: 16, fontWeight: 600, margin: '20px 0 8px 0', color: '#333' }}>{children}</h3>;
          },
          p({ children }) {
            return <p style={{ margin: '0 0 14px 0' }}>{children}</p>;
          },
          ul({ children }) {
            return <ul style={{ margin: '0 0 14px 0', paddingLeft: 24 }}>{children}</ul>;
          },
          ol({ children }) {
            return <ol style={{ margin: '0 0 14px 0', paddingLeft: 24 }}>{children}</ol>;
          },
          li({ children }) {
            return <li style={{ marginBottom: 4 }}>{children}</li>;
          },
          blockquote({ children }) {
            return (
              <blockquote style={{
                margin: '0 0 14px 0', padding: '8px 16px',
                borderLeft: '4px solid #1677ff', background: '#f0f5ff', borderRadius: '0 4px 4px 0',
                color: '#555',
              }}>
                {children}
              </blockquote>
            );
          },
          code({ className, children }) {
            const match = /language-(\w+)/.exec(className || '');
            const codeStr = String(children).replace(/\n$/, '');
            if (!match && !codeStr.includes('\n')) {
              return (
                <code style={{ background: '#f0f0f0', padding: '2px 6px', borderRadius: 3, fontSize: '0.88em', fontFamily: 'Consolas, Monaco, monospace' }}>
                  {children}
                </code>
              );
            }
            return (
              <div style={{ margin: '0 0 14px 0', borderRadius: 6, overflow: 'hidden' }}>
                <SyntaxHighlighter style={oneLight} language={match ? match[1] : undefined} PreTag="div"
                  customStyle={{ margin: 0, borderRadius: 6, fontSize: 13 }}>
                  {codeStr}
                </SyntaxHighlighter>
              </div>
            );
          },
          hr() {
            return <hr style={{ border: 'none', borderTop: '1px solid #e8ecf0', margin: '28px 0' }} />;
          },
          table({ children }) {
            return (
              <div style={{ overflowX: 'auto', marginBottom: 14 }}>
                <table style={{ borderCollapse: 'collapse', width: '100%', fontSize: 14 }}>
                  {children}
                </table>
              </div>
            );
          },
          th({ children }) {
            return <th style={{ border: '1px solid #e0e0e0', padding: '10px 16px', background: '#f6f8fa', fontWeight: 600, textAlign: 'left' }}>{children}</th>;
          },
          td({ children }) {
            return <td style={{ border: '1px solid #e8ecf0', padding: '8px 16px' }}>{children}</td>;
          },
          strong({ children }) {
            return <strong style={{ fontWeight: 600, color: '#1a1a1a' }}>{children}</strong>;
          },
          a({ href, children }) {
            return <a href={href} style={{ color: '#1677ff' }} target="_blank" rel="noopener">{children}</a>;
          },
        }}
      >
        {content}
      </ReactMarkdown>
    </div>
  );
}
