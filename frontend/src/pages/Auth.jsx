import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import '../styles/Auth.css';

const API_BASE = '/public/users';

const Auth = () => {
  const [tab, setTab] = useState('login'); // 'login' or 'register'
  // 입력값 상태
  const [loginId, setLoginId] = useState('');
  const [loginPw, setLoginPw] = useState('');
  const [registerId, setRegisterId] = useState('');
  const [registerPw, setRegisterPw] = useState('');
  const [registerNickname, setRegisterNickname] = useState('');
  // 에러 메시지 상태
  const [loginError, setLoginError] = useState('');
  const [registerError, setRegisterError] = useState('');
  const [registerStep, setRegisterStep] = useState(1); // 1: 기본정보, 2: 이메일코드, 3: 완료
  const [registerLoading, setRegisterLoading] = useState(false);
  const [loginLoading, setLoginLoading] = useState(false);
  const [verificationCode, setVerificationCode] = useState('');
  const [verificationError, setVerificationError] = useState('');
  const [emailForVerification, setEmailForVerification] = useState('');

  const navigate = useNavigate();
  const { login: loginContext } = useAuth();

  // 유효성 검사 함수
  const validateRegister = () => {
    if (!registerId.trim()) return '아이디(이메일)를 입력하세요.';
    if (!registerPw.trim()) return '비밀번호를 입력하세요.';
    if (!registerNickname.trim()) return '닉네임을 입력하세요.';
    if (registerNickname.length < 2 || registerNickname.length > 8)
      return '닉네임은 2~8글자여야 합니다.';
    return '';
  };

  const validateLogin = () => {
    if (!loginId.trim()) return '아이디(이메일)를 입력하세요.';
    if (!loginPw.trim()) return '비밀번호를 입력하세요.';
    return '';
  };

  // 회원가입 1단계: 임시 회원 생성
  const handleRegister = async (e) => {
    e.preventDefault();
    const err = validateRegister();
    setRegisterError(err);
    if (err) return;
    setRegisterLoading(true);
    setRegisterError('');
    try {
      const res = await fetch(`${API_BASE}/register/app`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: registerId,
          password: registerPw,
          nickname: registerNickname,
        }),
        credentials: 'include',
      });
      if (!res.ok) {
        const data = await res.json().catch(() => ({}));
        setRegisterError(
          data.message ||
            '회원가입 실패: 이미 등록된 이메일이거나 서버 오류입니다.'
        );
        setRegisterLoading(false);
        return;
      }
      setEmailForVerification(registerId);
      setRegisterStep(2);
      // 2단계: 이메일 인증코드 발송
      await fetch(
        `${API_BASE}/email/send?email=${encodeURIComponent(registerId)}`,
        {
          method: 'POST',
          credentials: 'include',
        }
      );
    } catch (err) {
      setRegisterError('네트워크 오류 또는 서버 오류입니다.');
    } finally {
      setRegisterLoading(false);
    }
  };

  // 회원가입 2단계: 이메일 인증코드 검증
  const handleVerifyCode = async (e) => {
    e.preventDefault();
    setVerificationError('');
    setRegisterLoading(true);
    try {
      const res = await fetch(
        `${API_BASE}/register/email/verify?email=${encodeURIComponent(
          emailForVerification
        )}&verificationCode=${encodeURIComponent(verificationCode)}`,
        {
          method: 'POST',
          credentials: 'include',
        }
      );
      if (!res.ok) {
        setVerificationError('인증코드가 올바르지 않거나 만료되었습니다.');
        setRegisterLoading(false);
        return;
      }
      setRegisterStep(3); // 완료 단계(추가 정보 입력은 추후)
    } catch (err) {
      setVerificationError('네트워크 오류 또는 서버 오류입니다.');
    } finally {
      setRegisterLoading(false);
    }
  };

  // 로그인
  const handleLogin = async (e) => {
    e.preventDefault();
    const err = validateLogin();
    setLoginError(err);
    if (err) return;
    setLoginLoading(true);
    setLoginError('');
    try {
      const res = await fetch(`${API_BASE}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: loginId, password: loginPw }),
        credentials: 'include',
      });
      if (!res.ok) {
        const data = await res.json().catch(() => ({}));
        setLoginError(
          data.message || '로그인 실패: 아이디 또는 비밀번호를 확인하세요.'
        );
        setLoginLoading(false);
        return;
      }
      const data = await res.json();
      loginContext(data); // AuthContext에 사용자 정보 저장
      navigate('/dashboard');
    } catch (err) {
      setLoginError('네트워크 오류 또는 서버 오류입니다.');
    } finally {
      setLoginLoading(false);
    }
  };

  return (
    <div className="auth-bg">
      <div className="auth-card">
        <h2>로그인 또는 회원가입</h2>
        <div className="social-login-block">
          <button className="kakao-btn">카카오로 시작하기</button>
          <button className="google-btn">구글로 시작하기</button>
        </div>
        <div className="auth-tabs">
          <button
            className={tab === 'login' ? 'active' : ''}
            onClick={() => setTab('login')}
          >
            로그인
          </button>
          <button
            className={tab === 'register' ? 'active' : ''}
            onClick={() => setTab('register')}
          >
            회원가입
          </button>
        </div>
        <div className="auth-form-block">
          {tab === 'login' ? (
            <form className="auth-form" onSubmit={handleLogin}>
              <input
                type="text"
                placeholder="아이디(이메일)"
                value={loginId}
                onChange={(e) => setLoginId(e.target.value)}
                autoComplete="username"
                disabled={loginLoading}
              />
              <input
                type="password"
                placeholder="비밀번호"
                value={loginPw}
                onChange={(e) => setLoginPw(e.target.value)}
                autoComplete="current-password"
                disabled={loginLoading}
              />
              {loginError && <div className="auth-error">{loginError}</div>}
              <button type="submit" disabled={loginLoading}>
                {loginLoading ? '로그인 중...' : '로그인'}
              </button>
            </form>
          ) : (
            <>
              {registerStep === 1 && (
                <form className="auth-form" onSubmit={handleRegister}>
                  <input
                    type="text"
                    placeholder="아이디(이메일)"
                    value={registerId}
                    onChange={(e) => setRegisterId(e.target.value)}
                    autoComplete="username"
                    disabled={registerLoading}
                  />
                  <input
                    type="password"
                    placeholder="비밀번호"
                    value={registerPw}
                    onChange={(e) => setRegisterPw(e.target.value)}
                    autoComplete="new-password"
                    disabled={registerLoading}
                  />
                  <input
                    type="text"
                    placeholder="닉네임 (2~8글자)"
                    value={registerNickname}
                    onChange={(e) => setRegisterNickname(e.target.value)}
                    autoComplete="nickname"
                    disabled={registerLoading}
                  />
                  {registerError && (
                    <div className="auth-error">{registerError}</div>
                  )}
                  <button type="submit" disabled={registerLoading}>
                    {registerLoading ? '처리 중...' : '회원가입'}
                  </button>
                </form>
              )}
              {registerStep === 2 && (
                <form className="auth-form" onSubmit={handleVerifyCode}>
                  <input
                    type="text"
                    placeholder="이메일로 받은 인증코드 입력"
                    value={verificationCode}
                    onChange={(e) => setVerificationCode(e.target.value)}
                    disabled={registerLoading}
                  />
                  {verificationError && (
                    <div className="auth-error">{verificationError}</div>
                  )}
                  <button type="submit" disabled={registerLoading}>
                    {registerLoading ? '인증 중...' : '인증하기'}
                  </button>
                </form>
              )}
              {registerStep === 3 && (
                <div className="auth-success">
                  회원가입이 완료되었습니다! 로그인 후 이용해 주세요.
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default Auth;
