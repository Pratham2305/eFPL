import React, { useState } from 'react';
import { Send, Bot, User, X } from 'lucide-react';

interface AiChatModalProps {
  isOpen: boolean;
  onClose: () => void;
}

interface Message {
  sender: 'user' | 'bot';
  text: string;
}

const AiChatModal: React.FC<AiChatModalProps> = ({ isOpen, onClose }) => {
  const [query, setQuery] = useState('');
  const [messages, setMessages] = useState<Message[]>([
    { sender: 'bot', text: 'Hello! I am your FPL Assistant. Ask me questions about transfers, captain choice, or squad weaknesses!' }
  ]);
  const [isSending, setIsSending] = useState(false);

  if (!isOpen) return null;

  const handleSend = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!query.trim() || isSending) return;

    const userText = query;
    setQuery('');
    setMessages((prev) => [...prev, { sender: 'user', text: userText }]);
    setIsSending(true);

    try {
      const res = await fetch('/api/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ query: userText }),
      });

      if (!res.ok) throw new Error('Chat failed');
      const data = await res.json();
      setMessages((prev) => [...prev, { sender: 'bot', text: data.reply }]);
    } catch (err) {
      setMessages((prev) => [...prev, { sender: 'bot', text: 'Sorry, I encountered an issue retrieving data.' }]);
    } finally {
      setIsSending(false);
    }
  };

  return (
    <div style={{
      position: 'fixed',
      bottom: 24,
      right: 24,
      width: 380,
      height: 520,
      zIndex: 1000,
      display: 'flex',
      flexDirection: 'column',
      borderRadius: 'var(--radius-md)',
      background: 'rgba(15, 23, 42, 0.95)',
      backdropFilter: 'blur(20px)',
      border: '1px solid var(--accent-cyan)',
      boxShadow: '0 12px 40px rgba(0, 0, 0, 0.6)'
    }}>
      {/* Header */}
      <div style={{
        padding: '0.8rem 1rem',
        borderBottom: '1px solid var(--border-color)',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        background: 'rgba(56, 189, 248, 0.1)'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 700, fontSize: '0.95rem' }}>
          <Bot size={18} color="var(--accent-cyan)" />
          FPL AI Chat Assistant
        </div>
        <button onClick={onClose} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
          <X size={18} />
        </button>
      </div>

      {/* Messages Feed */}
      <div style={{ flex: 1, padding: '1rem', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '0.8rem' }}>
        {messages.map((m, i) => (
          <div key={i} style={{
            display: 'flex',
            gap: '0.5rem',
            alignSelf: m.sender === 'user' ? 'flex-end' : 'flex-start',
            maxWidth: '85%'
          }}>
            {m.sender === 'bot' && <Bot size={16} color="var(--accent-cyan)" style={{ marginTop: 4 }} />}
            <div style={{
              padding: '0.6rem 0.8rem',
              borderRadius: 8,
              fontSize: '0.85rem',
              lineHeight: 1.4,
              whiteSpace: 'pre-wrap',
              background: m.sender === 'user' ? 'var(--accent-purple)' : 'rgba(255,255,255,0.06)',
              color: '#fff'
            }}>
              {m.text}
            </div>
            {m.sender === 'user' && <User size={16} color="var(--accent-primary)" style={{ marginTop: 4 }} />}
          </div>
        ))}
        {isSending && <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontStyle: 'italic' }}>Thinking...</div>}
      </div>

      {/* Input Form */}
      <form onSubmit={handleSend} style={{ padding: '0.8rem', borderTop: '1px solid var(--border-color)', display: 'flex', gap: '0.4rem' }}>
        <input
          type="text"
          placeholder="Ask a question..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          style={{
            flex: 1,
            padding: '0.5rem 0.8rem',
            borderRadius: 'var(--radius-sm)',
            background: 'rgba(0,0,0,0.4)',
            border: '1px solid var(--border-color)',
            color: '#fff',
            fontSize: '0.85rem'
          }}
        />
        <button type="submit" className="btn btn-primary" style={{ padding: '0.5rem 0.8rem' }} disabled={isSending}>
          <Send size={14} />
        </button>
      </form>
    </div>
  );
};

export default AiChatModal;
