import '../styles/MyPage.css';
import {
  Smile,
  Image as ImageIcon,
  Calendar,
  Crown,
  UserCog,
} from 'lucide-react';

const profile = {
  name: 'admin',
  id: '1234',
  icon: (
    <Smile
      size={36}
      color="#6fcf6f"
      style={{ background: '#e0f7fa', borderRadius: '50%', padding: 4 }}
    />
  ),
  recordCount: 0,
  photoCount: 0,
  premium: true,
  premiumType: '연간',
};

const MyPage = () => {
  return (
    <div className="mypage-bg">
      {/* -- 주석 처리된 부분은 필요시 활성화 --
      <div className="mypage-title-block">
        <h2 className="mypage-title">든든한 계정 시스템</h2>
        <p className="mypage-desc">
          내 계정에 기록을 저장하고 <br />
          언제든 로그인해 불러올 수 있어요
        </p>
      </div>
      */}

      <div className="mypage-card">
        <div className="mypage-section">
          <div className="mypage-profile-row">
            <div className="mypage-profile-icon">{profile.icon}</div>
            <div className="mypage-profile-info">
              <div className="mypage-profile-name">{profile.name}</div>
              <div className="mypage-profile-id">#{profile.id}</div>
            </div>
          </div>
        </div>
        <div className="mypage-section">
          <div className="mypage-record-cards">
            <div className="mypage-record-card">
              <div className="mypage-record-icon">{profile.icon}</div>
              <div className="mypage-record-label">내 기록</div>
              <div className="mypage-record-value">{profile.recordCount}</div>
            </div>
            <div className="mypage-record-card">
              <div className="mypage-record-icon">
                <ImageIcon size={28} color="#6fcf6f" />
              </div>
              <div className="mypage-record-label">올린 사진</div>
              <div className="mypage-record-value">{profile.photoCount}</div>
            </div>
          </div>
        </div>
        <div className="mypage-section">
          <div className="mypage-menu-list">
            <div className="mypage-menu-item">
              <Calendar size={22} color="#4ecb71" />
              <span>테마 캘린더</span>
            </div>
            <div className="mypage-menu-item">
              <ImageIcon size={22} color="#4ecb71" />
              <span>사진 모아보기</span>
            </div>
          </div>
        </div>
        <div className="mypage-section">
          <div className="mypage-premium-row">
            <Crown size={22} color="#4ecb71" />
            <span>구독 중</span>
            <span className="mypage-premium-badge">{profile.premiumType}</span>
          </div>
        </div>
        <div className="mypage-section">
          <div className="mypage-menu-item">
            <UserCog size={22} color="#4ecb71" />
            <span>설정</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default MyPage;
