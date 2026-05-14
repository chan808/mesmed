import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { materialApi } from './api';
import { useAuth } from '../../shared/auth/useAuth';

export function MaterialListPage() {
  const navigate = useNavigate();
  const { role } = useAuth();
  const isAdmin = role === 'ADMIN';

  const { data, isLoading } = useQuery({
    queryKey: ['materials'],
    queryFn: materialApi.list,
  });

  return (
    <>
      <div className="content-header">
        <h1>원자재</h1>
        {isAdmin && (
          <button className="primary" onClick={() => navigate('/materials/new')}>
            + 원자재 등록
          </button>
        )}
      </div>

      {isLoading && <p className="empty">로딩 중…</p>}
      {data && data.length === 0 && (
        <p className="empty">
          {isAdmin
            ? '등록된 원자재가 없습니다. 우측 상단에서 등록하세요.'
            : '등록된 원자재가 없습니다.'}
        </p>
      )}
      {data && data.length > 0 && (
        <table>
          <thead>
            <tr>
              <th>코드</th>
              <th>이름</th>
              <th>분류</th>
              <th>단위</th>
              <th>규격</th>
            </tr>
          </thead>
          <tbody>
            {data.map((m) => (
              <tr
                key={m.id}
                className="clickable"
                onClick={() => navigate(`/materials/${m.id}`)}
              >
                <td>{m.code}</td>
                <td>{m.name}</td>
                <td>{m.category ?? '-'}</td>
                <td>{m.unit ?? '-'}</td>
                <td>{m.specStandard ?? '-'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {data && data.length > 0 && (
        <p className="muted mt-16" style={{ fontSize: 12 }}>
          원자재를 클릭하면 검사기준 관리 화면으로 이동합니다.
        </p>
      )}
    </>
  );
}
