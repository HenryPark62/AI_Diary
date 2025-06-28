import { useState, useRef, useEffect } from 'react';
import '../styles/ChatAI.css';
import { Smile, Send, Tag } from 'lucide-react';

const mockMessages = [
  {
    id: 1,
    sender: 'ai',
    text: '안녕!\n처음 만나서 정말 반가워.\n마음이 궁금하니까 뭐든 말해줘!',
    time: '오후 05:42',
    tags: [],
  },
  {
    id: 2,
    sender: 'user',
    text: '반가워요',
    time: '오후 05:58',
    tags: ['일', '즐거운'],
  },
  {
    id: 3,
    sender: 'ai',
    text: '나도 정말 반가워, 베타!\n오늘은 어떤 하루 보냈어?\n궁금해서 기다리고 있었어.',
    time: '오후 05:58',
    tags: [],
  },
  {
    id: 4,
    sender: 'user',
    text: '오늘 일이 즐거웠다니 내 마음도 환해지는 것 같아!',
    time: '오후 05:59',
    tags: [],
  },
];

const SUGGESTED_TAGS = ['즐거운', '우울', '불안', '감사', '일', '가족', '친구'];

const ChatAI = () => {
  const [messages, setMessages] = useState(mockMessages);
  const [input, setInput] = useState('');
  const [tags, setTags] = useState([]);
  const [tagInput, setTagInput] = useState('');
  const chatEndRef = useRef(null);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSend = () => {
    if (!input.trim() && tags.length === 0) return;
    setMessages([
      ...messages,
      {
        id: messages.length + 1,
        sender: 'user',
        text: input,
        time: new Date().toLocaleTimeString('ko-KR', {
          hour: '2-digit',
          minute: '2-digit',
          hour12: true,
        }),
        tags,
      },
    ]);
    setInput('');
    setTags([]);
    setTagInput('');
  };

  const handleTagAdd = (tag) => {
    if (!tags.includes(tag)) setTags([...tags, tag]);
    setTagInput('');
  };
  const handleTagRemove = (tag) => setTags(tags.filter((t) => t !== tag));

  return (
    <div className="chat-bg">
      <div className="chat-title-block">
        <h2 className="chat-title">AI와 대화</h2>
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
        <div ref={chatEndRef} />
      </div>
      <div className="chat-input-block">
        <div className="chat-tags-input">
          {tags.map((tag, i) => (
            <span
              className="chat-tag selected"
              key={i}
              onClick={() => handleTagRemove(tag)}
            >
              <Tag size={14} /> {tag} ×
            </span>
          ))}
          <input
            className="tag-input"
            type="text"
            placeholder="감정 태그 입력..."
            value={tagInput}
            onChange={(e) => setTagInput(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter' && tagInput.trim())
                handleTagAdd(tagInput.trim());
            }}
          />
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
          <input
            className="chat-input"
            type="text"
            placeholder="메시지를 입력하세요..."
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') handleSend();
            }}
          />
          <button className="chat-send-btn" onClick={handleSend}>
            <Send size={22} />
          </button>
        </div>
      </div>
    </div>
  );
};

export default ChatAI;
