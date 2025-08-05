import React from 'react';

const Navigation = ({ active, onNav, navItems }) => {
  return (
    <>
      {/* PC: 왼쪽 고정 네비게이션 */}
      <nav className="side-nav">
        <div className="nav-logo">🌱</div>
        <ul>
          {navItems.map((item) => (
            <li
              key={item.path}
              className={active === item.path ? 'active' : ''}
            >
              <button onClick={() => onNav(item.path)}>
                {item.icon}
                <span>{item.name}</span>
              </button>
            </li>
          ))}
        </ul>
      </nav>
      {/* 모바일: 하단 고정 네비게이션 */}
      <nav className="bottom-nav">
        {navItems.map((item) => (
          <button
            key={item.path}
            className={active === item.path ? 'active' : ''}
            onClick={() => onNav(item.path)}
          >
            {item.icon}
            <span>{item.name}</span>
          </button>
        ))}
      </nav>
    </>
  );
};

export default Navigation;
