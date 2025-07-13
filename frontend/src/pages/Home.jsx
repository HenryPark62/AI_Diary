import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Calendar, Heart } from 'lucide-react';
import '../styles/Home.css';

const Home = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (user) {
      navigate('/dashboard');
    }
  }, [user, navigate]);

  return (
    <>
      <div className="home">
        <section className="hero">
          <h1 className="home-h1">감정을 일기로 보관해볼까요?</h1>
          <p className="home-text">
            다이어리로 일상의 감정을 기록하고 반영해보세요
          </p>
          <div className="hero-illustration">
            <Calendar size={48} className="icon" />
            <Heart size={48} className="icon" />
          </div>
        </section>

        <section className="features">
          <div className="feature-card">
            <h2>캘린더</h2>
            <p>직관적인 캘린더 인터페이스로 일일 항목을 쓸 수 있어요</p>
          </div>
          <div className="feature-card">
            <h2>감정 추적</h2>
            <p>
              AI가 감정 패턴을 시각적으로 <br />
              추적해줘요
            </p>
          </div>
          <div className="feature-card">
            <h2>프라이빗 및 보안</h2>
            <p>
              일기는 사적이고 안전하게 <br />
              보관해드려요
            </p>
          </div>
        </section>
        <section className="cta">
          <h2>여행을 시작할 준비가 되셨나요?</h2>
          <p>시작하시려면 로그인해주세요!</p>
        </section>
      </div>
    </>
  );
};

export default Home;
