import { useState } from 'react';
import { Routes, Route, useNavigate, useLocation, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { User, BarChart2, MessageCircle, BookOpen } from 'lucide-react';
import Calendar from '../components/Calendar';
import DiaryEntry from '../components/DiaryEntry'; // DiaryEntry의 경로는 실제 위치에 맞게 확인해주세요.
import GuidedTour from '../components/GuidedTour';
import Navigation from '../components/Navigation';
import { formatDate } from '../utils/dateUtils';
import '../styles/Dashboard.css';
import MyPage from './MyPage';
import Analysis from './Analysis';
import ChatAI from './ChatAI';

const navItems = [
  { name: '일기작성', icon: <BookOpen size={24} />, path: '/dashboard' },
  { name: '마이페이지', icon: <User size={24} />, path: '/dashboard/mypage' },
  { name: '분석', icon: <BarChart2 size={24} />, path: '/dashboard/analysis' },
  {
    name: 'AI와 대화',
    icon: <MessageCircle size={24} />,
    path: '/dashboard/chat',
  },
];

const DashboardHome = () => {
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [entries, setEntries] = useState(() => {
    const savedEntries = localStorage.getItem('diaryEntries');
    return savedEntries ? JSON.parse(savedEntries) : {};
  });

  const handleDateSelect = (date) => setSelectedDate(date);
  
  // [수정] saveEntry 함수가 isSecurityEnhanced 파라미터를 받도록 변경
  const saveEntry = (date, emotion, content, isSecurityEnhanced) => {
    const dateKey = formatDate(date);
    setEntries((prevEntries) => {
      // [수정] 저장할 객체에 isSecurityEnhanced 정보도 포함
      const updated = { ...prevEntries, [dateKey]: { emotion, content, isSecurityEnhanced } };
      localStorage.setItem('diaryEntries', JSON.stringify(updated));
      console.log('저장된 데이터:', updated[dateKey]);
      return updated;
    });
  };

  return (
    <div className="dashboard">
      <h1>감정 일기</h1>
      <div className="dashboard-content">
        <Calendar
          selectedDate={selectedDate}
          onDateSelect={handleDateSelect}
          entries={entries}
        />
        <DiaryEntry
          date={selectedDate}
          initialEmotion={entries[formatDate(selectedDate)]?.emotion || ''}
          initialContent={entries[formatDate(selectedDate)]?.content || ''}
          onSave={saveEntry}
        />
      </div>
      <GuidedTour />
    </div>
  );
};

// Dashboard 컴포넌트는 수정할 필요 없이 기존과 동일합니다.
const Dashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [active, setActive] = useState(location.pathname);

  if (!user) return <Navigate to="/" replace />;

  const handleNav = (path) => {
    setActive(path);
    navigate(path);
  };

  return (
    <div className="app-layout">
      <Navigation active={active} onNav={handleNav} navItems={navItems} />
      <main className="main-content">
        <Routes>
          <Route index element={<DashboardHome />} />
          <Route path="mypage" element={<MyPage />} />
          <Route path="analysis" element={<Analysis />} />
          <Route path="chat" element={<ChatAI />} />
        </Routes>
      </main>
    </div>
  );
};

export default Dashboard;