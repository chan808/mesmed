import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';

const NAV = [
  { to: '/dashboard', label: '대시보드' },
  { to: '/materials', label: '원자재' },
  { to: '/lots', label: 'LOT' },
  { to: '/inspections/new', label: '수입검사 등록' },
  { to: '/production-logs', label: '생산 이력' },
  { to: '/equipment', label: '설비' },
  { to: '/alarms', label: '알람' },
];

export function Layout() {
  const { userId, role, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="sidebar-brand">medmes MES</div>
        <nav>
          {NAV.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                isActive ? 'sidebar-link active' : 'sidebar-link'
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-footer">
          <div>userId {userId}</div>
          <div>role {role}</div>
          <button onClick={handleLogout}>로그아웃</button>
        </div>
      </aside>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
