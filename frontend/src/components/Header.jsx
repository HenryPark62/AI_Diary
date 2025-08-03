import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { CalendarDays } from 'lucide-react';
import '../styles/Header.css';

const Header = () => {
  const { user, login, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogin = () => {
    navigate('/auth');
  };

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <header className="header">
      <div className="header-content">
        <Link to="/" className="logo">
          <CalendarDays size={24} />
          <span>MindNote</span>
        </Link>
        <div className="auth-buttons">
          {user ? (
            <button className="btn btn-logout" onClick={handleLogout}>
              로그아웃
            </button>
          ) : (
            <button className="btn btn-login" onClick={handleLogin}>
              로그인
            </button>
          )}
        </div>
      </div>
    </header>
  );
};

export default Header;
