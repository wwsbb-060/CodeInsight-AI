import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { vscDarkPlus } from 'react-syntax-highlighter/dist/esm/styles/prism';

interface Props {
  content: string;
}

export default function MarkdownViewer({ content }: Props) {
  return (
    <div style={{ padding: 24, background: '#fff', borderRadius: 8 }}>
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        components={{
          code({ className, children, ...props }) {
            const match = /language-(\w+)/.exec(className || '');
            const codeStr = String(children).replace(/\n$/, '');
            const isInline = !match && !codeStr.includes('\n');

            // 行内代码
            if (isInline) {
              return (
                <code
                  {...props}
                  style={{
                    background: '#f5f5f5',
                    padding: '2px 6px',
                    borderRadius: 4,
                    fontSize: '0.9em',
                  }}
                >
                  {children}
                </code>
              );
            }

            // 代码块
            return (
              <SyntaxHighlighter
                style={vscDarkPlus}
                language={match ? match[1] : undefined}
                PreTag="div"
              >
                {codeStr}
              </SyntaxHighlighter>
            );
          },
          table({ children }) {
            return (
              <div style={{ overflowX: 'auto' }}>
                <table
                  style={{
                    borderCollapse: 'collapse',
                    width: '100%',
                  }}
                >
                  {children}
                </table>
              </div>
            );
          },
          th({ children }) {
            return (
              <th
                style={{
                  border: '1px solid #e8e8e8',
                  padding: '8px 16px',
                  background: '#fafafa',
                  textAlign: 'left',
                }}
              >
                {children}
              </th>
            );
          },
          td({ children }) {
            return (
              <td
                style={{
                  border: '1px solid #e8e8e8',
                  padding: '8px 16px',
                }}
              >
                {children}
              </td>
            );
          },
        }}
      >
        {content}
      </ReactMarkdown>
    </div>
  );
}
