import React, { useState, useEffect, useRef } from 'react';
import { Send, Bot, User, X, RefreshCw, AlertTriangle, Sparkles, Database, RotateCcw } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import { ChatHealth, ChatResponse } from '../types';

interface AiChatModalProps {
  isOpen: boolean;
  onClose: () => void;
}

interface MessageItem {
  id: string;
  sender: 'user' | 'bot';
  text: string;
  intent?: string;
  source?: 'LLM' | 'DETERMINISTIC_FALLBACK' | string;
  dataAsOf?: string;
  warning?: string | null;
  error?: boolean;
}

const SUGGESTED_PROMPTS = [
  'What transfer should I make this week?',
  'Who should I captain?',
  'What all are my remaining chips?',
  "What are my squad's weaknesses?",
  'Compare Salah and Palmer'
];

export const AiChatModal: React.FC<AiChatModalProps> = ({ isOpen, onClose }) => {
  const [query, setQuery] = useState('');
  const [conversationId, setConversationId] = useState<string | null>(() => localStorage.getItem('chat_conversation_id'));
  const [health, setHealth] = useState<ChatHealth | null>(null);
  const [isSending, setIsSending] = useState(false);
  const [lastFailedQuery, setLastFailedQuery] = useState<string | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const [messages, setMessages] = useState<MessageItem[]>([
    {
      id: 'greeting',
      sender: 'bot',
      text: "👋 **Hello Manager!** I am your grounded FPL Assistant.\n\nAsk me about upcoming transfers, captaincy choices, remaining chips, or squad weaknesses based directly on your live squad intelligence.",
      source: 'LLM'
    }
  ]);

  // Check Ollama health status on modal open
  useEffect(() => {
    if (isOpen) {
      fetchHealth();
    }
  }, [isOpen]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isSending]);

  const fetchHealth = async () => {
    try {
      const res = await fetch('/api/chat/health');
      if (res.ok) {
        const data: ChatHealth = await res.json();
        setHealth(data);
      }
    } catch {
      // Ignored
    }
  };

  const handleNewChat = () => {
    setConversationId(null);
    localStorage.removeItem('chat_conversation_id');
    setMessages([
      {
        id: 'new-greeting-' + Date.now(),
        sender: 'bot',
        text: "✨ **Started a new conversation.**\n\nAsk me anything about your squad, transfer moves, or chip planning!",
        source: 'LLM'
      }
    ]);
  };

  const executeSend = async (userText: string) => {
    if (!userText.trim() || isSending) return;

    const userMsgId = 'user-' + Date.now();
    setMessages((prev) => [
      ...prev,
      { id: userMsgId, sender: 'user', text: userText }
    ]);
    setQuery('');
    setIsSending(true);
    setLastFailedQuery(null);

    try {
      const res = await fetch('/api/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          query: userText,
          conversationId: conversationId || undefined
        })
      });

      if (!res.ok) {
        throw new Error(`Chat request failed (${res.status})`);
      }

      const data: ChatResponse = await res.json();

      if (data.conversationId) {
        setConversationId(data.conversationId);
        localStorage.setItem('chat_conversation_id', data.conversationId);
      }

      const botMsgId = 'bot-' + Date.now();
      setMessages((prev) => [
        ...prev,
        {
          id: botMsgId,
          sender: 'bot',
          text: data.reply,
          intent: data.intent,
          source: data.source,
          dataAsOf: data.dataAsOf,
          warning: data.warning
        }
      ]);
    } catch (err: any) {
      setLastFailedQuery(userText);
      setMessages((prev) => [
        ...prev,
        {
          id: 'err-' + Date.now(),
          sender: 'bot',
          text: "⚠️ **Connection Error**: Unable to reach the assistant service. Please check your backend connection.",
          error: true
        }
      ]);
    } finally {
      setIsSending(false);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    executeSend(query);
  };

  const handlePromptClick = (promptText: string) => {
    executeSend(promptText);
  };

  if (!isOpen) return null;

  const isLlmOnline = health?.ollamaReachable && health?.modelAvailable;

  return (
    <div style={{
      position: 'fixed',
      bottom: 24,
      right: 24,
      width: 480,
      maxWidth: 'calc(100vw - 32px)',
      height: 640,
      maxHeight: 'calc(100vh - 48px)',
      zIndex: 1000,
      display: 'flex',
      flexDirection: 'column',
      borderRadius: 'var(--radius-lg, 12px)',
      background: 'rgba(15, 23, 42, 0.98)',
      backdropFilter: 'blur(24px)',
      border: '1px solid rgba(56, 189, 248, 0.35)',
      boxShadow: '0 20px 50px rgba(0, 0, 0, 0.75)'
    }}>
      {/* Header */}
      <div style={{
        padding: '0.8rem 1.2rem',
        borderBottom: '1px solid rgba(255, 255, 255, 0.08)',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        background: 'linear-gradient(90deg, rgba(56, 189, 248, 0.12), rgba(168, 85, 247, 0.08))'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
          <div style={{
            width: 32,
            height: 32,
            borderRadius: 8,
            background: 'rgba(56, 189, 248, 0.2)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}>
            <Bot size={20} color="var(--accent-cyan)" />
          </div>
          <div>
            <div style={{ fontWeight: 700, fontSize: '0.95rem', color: '#f8fafc', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
              FPL AI Assistant
            </div>
            {/* Health status badge */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', fontSize: '0.72rem', marginTop: 1 }}>
              <span style={{
                width: 7,
                height: 7,
                borderRadius: '50%',
                background: isLlmOnline ? '#10b981' : '#f59e0b',
                display: 'inline-block'
              }} />
              <span style={{ color: isLlmOnline ? '#34d399' : '#fbbf24' }}>
                {isLlmOnline ? `LLM Ready (${health?.model})` : 'Data Fallback Mode'}
              </span>
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <button
            onClick={handleNewChat}
            title="Start new conversation"
            style={{
              background: 'rgba(255, 255, 255, 0.06)',
              border: '1px solid rgba(255, 255, 255, 0.12)',
              borderRadius: 6,
              color: 'var(--text-secondary, #94a3b8)',
              padding: '0.3rem 0.6rem',
              fontSize: '0.75rem',
              display: 'flex',
              alignItems: 'center',
              gap: '0.3rem',
              cursor: 'pointer'
            }}
          >
            <RotateCcw size={13} />
            New Chat
          </button>
          <button
            onClick={onClose}
            style={{ background: 'none', border: 'none', color: '#94a3b8', cursor: 'pointer', padding: '0.3rem' }}
          >
            <X size={20} />
          </button>
        </div>
      </div>

      {/* Suggested Quick Prompts */}
      <div style={{
        padding: '0.6rem 1rem',
        borderBottom: '1px solid rgba(255, 255, 255, 0.06)',
        display: 'flex',
        gap: '0.4rem',
        overflowX: 'auto',
        background: 'rgba(0, 0, 0, 0.25)',
        whiteSpace: 'nowrap'
      }}>
        {SUGGESTED_PROMPTS.map((promptText, idx) => (
          <button
            key={idx}
            onClick={() => handlePromptClick(promptText)}
            disabled={isSending}
            style={{
              background: 'rgba(56, 189, 248, 0.08)',
              border: '1px solid rgba(56, 189, 248, 0.25)',
              borderRadius: 14,
              color: '#38bdf8',
              fontSize: '0.73rem',
              padding: '0.25rem 0.65rem',
              cursor: isSending ? 'not-allowed' : 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '0.3rem',
              flexShrink: 0
            }}
          >
            <Sparkles size={11} />
            {promptText}
          </button>
        ))}
      </div>

      {/* Messages Feed */}
      <div style={{
        flex: 1,
        padding: '1rem',
        overflowY: 'auto',
        display: 'flex',
        flexDirection: 'column',
        gap: '1rem'
      }}>
        {messages.map((m) => {
          const isUser = m.sender === 'user';
          return (
            <div
              key={m.id}
              style={{
                display: 'flex',
                flexDirection: 'column',
                alignSelf: isUser ? 'flex-end' : 'flex-start',
                maxWidth: isUser ? '85%' : '94%'
              }}
            >
              <div style={{
                display: 'flex',
                gap: '0.5rem',
                alignItems: 'flex-start',
                flexDirection: isUser ? 'row-reverse' : 'row'
              }}>
                <div style={{
                  width: 26,
                  height: 26,
                  borderRadius: 6,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  background: isUser ? 'var(--accent-purple, #a855f7)' : 'rgba(56, 189, 248, 0.2)',
                  flexShrink: 0,
                  marginTop: 2
                }}>
                  {isUser ? <User size={14} color="#fff" /> : <Bot size={15} color="var(--accent-cyan)" />}
                </div>

                <div style={{
                  padding: '0.75rem 1rem',
                  borderRadius: 10,
                  fontSize: '0.86rem',
                  lineHeight: 1.5,
                  background: isUser ? 'linear-gradient(135deg, #7c3aed, #9333ea)' : 'rgba(30, 41, 59, 0.75)',
                  border: isUser ? '1px solid rgba(168, 85, 247, 0.4)' : '1px solid rgba(255, 255, 255, 0.08)',
                  color: '#f8fafc',
                  overflowWrap: 'break-word',
                  boxShadow: '0 4px 12px rgba(0, 0, 0, 0.2)'
                }}>
                  {/* Warning banner if LLM fell back or had issue */}
                  {m.warning && (
                    <div style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.4rem',
                      padding: '0.4rem 0.6rem',
                      borderRadius: 6,
                      background: 'rgba(245, 158, 11, 0.15)',
                      border: '1px solid rgba(245, 158, 11, 0.3)',
                      color: '#fbbf24',
                      fontSize: '0.75rem',
                      marginBottom: '0.6rem'
                    }}>
                      <AlertTriangle size={13} style={{ flexShrink: 0 }} />
                      <span>{m.warning}</span>
                    </div>
                  )}

                  {/* Markdown Renderer for Bot, plain text for User */}
                  {isUser ? (
                    <div>{m.text}</div>
                  ) : (
                    <div className="chat-markdown" style={{ color: '#e2e8f0' }}>
                      <ReactMarkdown
                        components={{
                          h3: ({ node, ...props }) => (
                            <h3 style={{ fontSize: '0.95rem', fontWeight: 700, margin: '0.6rem 0 0.3rem', color: '#38bdf8' }} {...props} />
                          ),
                          table: ({ node, ...props }) => (
                            <div style={{ overflowX: 'auto', margin: '0.5rem 0' }}>
                              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.78rem' }} {...props} />
                            </div>
                          ),
                          th: ({ node, ...props }) => (
                            <th style={{ background: 'rgba(255, 255, 255, 0.08)', padding: '0.35rem 0.5rem', textAlign: 'left', border: '1px solid rgba(255, 255, 255, 0.12)' }} {...props} />
                          ),
                          td: ({ node, ...props }) => (
                            <td style={{ padding: '0.35rem 0.5rem', border: '1px solid rgba(255, 255, 255, 0.08)' }} {...props} />
                          ),
                          ul: ({ node, ...props }) => (
                            <ul style={{ paddingLeft: '1.2rem', margin: '0.4rem 0' }} {...props} />
                          ),
                          li: ({ node, ...props }) => (
                            <li style={{ margin: '0.2rem 0' }} {...props} />
                          ),
                          p: ({ node, ...props }) => (
                            <p style={{ margin: '0.4rem 0' }} {...props} />
                          )
                        }}
                      >
                        {m.text}
                      </ReactMarkdown>
                    </div>
                  )}

                  {/* Metadata Footer for Bot */}
                  {!isUser && (m.source || m.intent) && (
                    <div style={{
                      marginTop: '0.6rem',
                      paddingTop: '0.4rem',
                      borderTop: '1px solid rgba(255, 255, 255, 0.06)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      fontSize: '0.7rem',
                      color: '#64748b'
                    }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                        {m.source === 'LLM' ? (
                          <span style={{
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '0.25rem',
                            color: '#34d399',
                            background: 'rgba(16, 185, 129, 0.1)',
                            padding: '0.15rem 0.4rem',
                            borderRadius: 4
                          }}>
                            <Sparkles size={11} /> Grounded LLM
                          </span>
                        ) : (
                          <span style={{
                            display: 'inline-flex',
                            alignItems: 'center',
                            gap: '0.25rem',
                            color: '#38bdf8',
                            background: 'rgba(56, 189, 248, 0.1)',
                            padding: '0.15rem 0.4rem',
                            borderRadius: 4
                          }}>
                            <Database size={11} /> Data Fallback
                          </span>
                        )}
                        {m.intent && (
                          <span style={{ color: '#94a3b8' }}>• {m.intent}</span>
                        )}
                      </div>
                      {m.dataAsOf && (
                        <span>{new Date(m.dataAsOf).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                      )}
                    </div>
                  )}
                </div>
              </div>
            </div>
          );
        })}

        {/* Typing indicator */}
        {isSending && (
          <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', alignSelf: 'flex-start' }}>
            <div style={{
              width: 26,
              height: 26,
              borderRadius: 6,
              background: 'rgba(56, 189, 248, 0.2)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}>
              <Bot size={15} color="var(--accent-cyan)" />
            </div>
            <div style={{
              padding: '0.6rem 0.9rem',
              borderRadius: 10,
              background: 'rgba(30, 41, 59, 0.75)',
              border: '1px solid rgba(255, 255, 255, 0.08)',
              color: '#94a3b8',
              fontSize: '0.82rem',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem'
            }}>
              <div className="pulse" style={{ fontSize: '1rem' }}>⚽</div>
              <span>Analysing squad data & xPoints...</span>
            </div>
          </div>
        )}

        {/* Retry button on error */}
        {lastFailedQuery && !isSending && (
          <div style={{ textAlign: 'center', margin: '0.5rem 0' }}>
            <button
              onClick={() => executeSend(lastFailedQuery)}
              style={{
                background: 'rgba(239, 68, 68, 0.15)',
                border: '1px solid rgba(239, 68, 68, 0.3)',
                color: '#f87171',
                borderRadius: 6,
                padding: '0.4rem 0.8rem',
                fontSize: '0.78rem',
                cursor: 'pointer',
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.4rem'
              }}
            >
              <RefreshCw size={13} />
              Retry last query
            </button>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Input bar */}
      <form
        onSubmit={handleSubmit}
        style={{
          padding: '0.8rem 1rem',
          borderTop: '1px solid rgba(255, 255, 255, 0.08)',
          background: 'rgba(15, 23, 42, 0.95)',
          display: 'flex',
          gap: '0.6rem'
        }}
      >
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Ask e.g. What transfer should I make this week?"
          disabled={isSending}
          style={{
            flex: 1,
            background: 'rgba(0, 0, 0, 0.35)',
            border: '1px solid rgba(255, 255, 255, 0.12)',
            borderRadius: 8,
            padding: '0.6rem 0.8rem',
            color: '#f8fafc',
            fontSize: '0.85rem',
            outline: 'none'
          }}
        />
        <button
          type="submit"
          disabled={!query.trim() || isSending}
          style={{
            background: !query.trim() || isSending ? 'rgba(255, 255, 255, 0.08)' : 'linear-gradient(135deg, #0284c7, #38bdf8)',
            border: 'none',
            borderRadius: 8,
            color: '#fff',
            padding: '0 0.9rem',
            cursor: !query.trim() || isSending ? 'not-allowed' : 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}
        >
          <Send size={16} />
        </button>
      </form>
    </div>
  );
};

export default AiChatModal;
