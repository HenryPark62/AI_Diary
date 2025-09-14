import { useState } from 'react';
import {
  Outlet,
  Routes,
  Route,
  useNavigate,
  useLocation,
  Navigate,
} from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { User, BarChart2, MessageCircle, BookOpen, Globe } from 'lucide-react';
import Calendar from '../components/Calendar';
import DiaryEntry from '../components/DiaryEntry';
import GuidedTour from '../components/GuidedTour';
import Navigation from '../components/Navigation';
import { formatDate } from '../utils/dateUtils';
import '../styles/Dashboard.css';
import MyPage from './MyPage';
import Analysis from './Analysis';
import ChatAI from './ChatAI';
import Community from './Community';

// 내부 페이지 컴포넌트 (임시)
// const Analysis = () => <div style={{ padding: 32 }}>분석</div>;

const navItems = [
  { name: '대시보드', icon: <BookOpen size={24} />, path: '/dashboard' },
  { name: '마이페이지', icon: <User size={24} />, path: '/dashboard/mypage' },
  { name: '분석', icon: <BarChart2 size={24} />, path: '/dashboard/analysis' },
  {
    name: 'AI와 대화',
    icon: <MessageCircle size={24} />,
    path: '/dashboard/chat',
  },
  { name: '커뮤니티', icon: <Globe size={24} />, path: '/dashboard/community' },
];

const DashboardHome = () => {
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [entries, setEntries] = useState(() => {
    const savedEntries = localStorage.getItem('diaryEntries');
    return savedEntries ? JSON.parse(savedEntries) : {};
  });

  const handleDateSelect = (date) => setSelectedDate(date);
  const saveEntry = (date, emotion, content) => {
    const dateKey = formatDate(date);
    setEntries((prevEntries) => {
      const updated = { ...prevEntries, [dateKey]: { emotion, content } };
      localStorage.setItem('diaryEntries', JSON.stringify(updated));
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

const Dashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [active, setActive] = useState(location.pathname);

  // 로그인 안 했으면 홈으로

  /* - 로그인 로직 완성시 주석 해제 필요
  if (!user) return <Navigate to="/" replace />;
  */

  // 네비게이션 클릭 시 이동
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
          <Route path="community" element={<Community />} />
        </Routes>
      </main>
    </div>
  );
};

export default Dashboard;
