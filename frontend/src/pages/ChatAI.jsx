import { useState, useRef, useEffect } from 'react';
import '../styles/ChatAI.css';
import { Smile, Send, Tag } from 'lucide-react';

const SUGGESTED_TAGS = ['즐거운', '우울', '불안', '감사', '일', '가족', '친구'];

// AI 첫 멘트
const INITIAL_AI_MESSAGE = {
  id: 1,
  sender: 'ai',
  text: '안녕!\n처음 만나서 정말 반가워.\n마음이 궁금하니까 뭐든 말해줘!',
  time: new Date().toLocaleTimeString('ko-KR', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: true,
  }),
  tags: [],
};

const ChatAI = () => {
  const [messages, setMessages] = useState([INITIAL_AI_MESSAGE]);
  const [input, setInput] = useState('');
  const [tags, setTags] = useState([]);
  const [tagInput, setTagInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const chatEndRef = useRef(null);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, loading]);

  // AI와 대화
  const fetchAIResponse = async (userMessages) => {
    try {
      setLoading(true);
      setError('');
      const systemPrompt = {
        role: 'system',
        content:
          '너는 친절하고 공감하는 AI 친구야. 사용자의 감정과 태그도 함께 참고해서 대답해줘.',
      };
      const chatMessages = [
        systemPrompt,
        ...userMessages.map((msg) => ({
          role: msg.sender === 'user' ? 'user' : 'assistant',
          content:
            msg.text +
            (msg.tags && msg.tags.length > 0
              ? `\n[태그: ${msg.tags.join(', ')}]`
              : ''),
        })),
      ];
      const res = await fetch('http://localhost:5000/api/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ messages: chatMessages }),
      });
      const data = await res.json();
      if (!data.choices || !data.choices[0]?.message?.content) {
        throw new Error('AI 응답이 올바르지 않습니다.');
      }
      return data.choices[0].message.content;
    } catch (e) {
      setError('AI 응답에 실패했어요. 다시 시도해 주세요.');
      return 'AI 응답에 실패했어요. 잠시 후 다시 시도해 주세요.';
    } finally {
      setLoading(false);
    }
  };

  // 메시지 전송
  const handleSend = async () => {
    if (!input.trim() && tags.length === 0) return;
    const now = new Date().toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: true,
    });
    const userMsg = {
      id: messages.length + 1,
      sender: 'user',
      text: input,
      time: now,
      tags,
    };
    setMessages((prev) => [...prev, userMsg]);
    setInput('');
    setTags([]);
    setTagInput('');

    // AI 답변
    const aiText = await fetchAIResponse([...messages, userMsg]);
    const aiMsg = {
      id: messages.length + 2,
      sender: 'ai',
      text: aiText,
      time: new Date().toLocaleTimeString('ko-KR', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: true,
      }),
      tags: [],
    };
    setMessages((prev) => [...prev, aiMsg]);
  };

  const handleTagAdd = (tag) => {
    if (!tags.includes(tag)) setTags([...tags, tag]);
    setTagInput('');
  };
  const handleTagRemove = (tag) => setTags(tags.filter((t) => t !== tag));

  // textarea
  const inputRef = useRef();
  useEffect(() => {
    if (inputRef.current) {
      inputRef.current.style.height = 'auto';
      inputRef.current.style.height = inputRef.current.scrollHeight + 'px';
    }
  }, [input]);

  return (
    <div className="chat-ai-outer">
      <div className="chat-container">
        <div className="chat-title-block">
          <h2 className="chat-title">마인드와 대화 해보세요!</h2>
        </div>
        <div className="chat-window">
          {messages.map((msg) => (
            <div
              key={msg.id}
              className={`chat-row ${msg.sender === 'user' ? 'user' : 'ai'}`}
            >
              <div className="chat-bubble-block">
                {msg.sender === 'ai' && (
                  <div className="chat-avatar">
                    <Smile size={32} color="#fff" />
                  </div>
                )}
                <div className={`chat-bubble ${msg.sender}`}>
                  {msg.text.split('\n').map((line, idx) => (
                    <div key={idx}>{line}</div>
                  ))}
                  {msg.tags && msg.tags.length > 0 && (
                    <div className="chat-tags">
                      {msg.tags.map((tag, i) => (
                        <span className="chat-tag" key={i}>
                          <Tag size={14} /> {tag}
                        </span>
                      ))}
                    </div>
                  )}
                </div>
                {msg.sender === 'user' && (
                  <div className="chat-avatar user">
                    <Smile size={32} color="#6fcf6f" />
                  </div>
                )}
              </div>
              <div className="chat-time">{msg.time}</div>
            </div>
          ))}
          {loading && (
            <div className="chat-row ai">
              <div className="chat-bubble-block">
                <div className="chat-avatar">
                  <Smile size={32} color="#fff" />
                </div>
                <div className="chat-bubble ai">
                  AI가 답변을 작성 중입니다...
                </div>
              </div>
            </div>
          )}
          <div ref={chatEndRef} />
        </div>
        <div className="chat-input-block">
          <div className="chat-tags-input">
            <input
              className="tag-input"
              type="text"
              placeholder="지금 기분을 적어주세요!"
              value={tagInput}
              onChange={(e) => setTagInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && tagInput.trim())
                  handleTagAdd(tagInput.trim());
              }}
            />
            <div className="selected-tags-list">
              {tags.map((tag, i) => (
                <span
                  className="chat-tag selected"
                  key={i}
                  onClick={() => handleTagRemove(tag)}
                >
                  <Tag size={14} /> {tag} ×
                </span>
              ))}
            </div>
            <div className="suggested-tags">
              {SUGGESTED_TAGS.map((tag) => (
                <span
                  key={tag}
                  className="suggested-tag"
                  onClick={() => handleTagAdd(tag)}
                >
                  {tag}
                </span>
              ))}
            </div>
          </div>
          <div className="chat-input-row">
            <textarea
              className="chat-input"
              ref={inputRef}
              placeholder="메시지를 입력하세요..."
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey && !loading) {
                  e.preventDefault();
                  handleSend();
                }
              }}
              disabled={loading}
              rows={1}
            />
            <button
              className="chat-send-btn"
              onClick={handleSend}
              disabled={loading}
            >
              <Send size={22} />
            </button>
          </div>
          {error && <div className="chat-error">{error}</div>}
        </div>
      </div>
    </div>
  );
};

export default ChatAI;
