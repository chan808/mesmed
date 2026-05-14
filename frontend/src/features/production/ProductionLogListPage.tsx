import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { productionLogApi } from './api';
import { formatDateTime } from '../../shared/util/format';
import { useAuth } from '../../shared/auth/useAuth';

export function ProductionLogListPage() {
  const navigate = useNavigate();
  const [todayOnly, setTodayOnly] = useState(false);
  const { role } = useAuth();
  const canCreate = role === 'ADMIN' || role === 'OPERATOR';

  const { data, isLoading } = useQuery({
    queryKey: ['production-logs', { todayOnly }],
    queryFn: () => productionLogApi.list(todayOnly),
  });

  return (
    <>
      <div className="content-header">
        <h1>생산 이력</h1>
        <div className="row">
          <label className="row" style={{ fontSize: 13 }}>
            <input
              type="checkbox"
              checked={todayOnly}
              onChange={(e) => setTodayOnly(e.target.checked)}
            />
            오늘 생산만
          </label>
          {canCreate && (
            <button className="primary" onClick={() => navigate('/production-logs/new')}>
              + 생산 이력 등록
            </button>
          )}
        </div>
      </div>

      {isLoading && <p className="empty">로딩 중…</p>}
      {data && data.length === 0 && (
        <p className="empty">생산 이력이 없습니다. PASS 상태 LOT만 등록 가능합니다.</p>
      )}
      {data && data.length > 0 && (
        <table>
          <thead>
            <tr>
              <th>LOT</th>
              <th>공정</th>
              <th>설비</th>
              <th>생산</th>
              <th>불량</th>
              <th>시작</th>
              <th>종료</th>
            </tr>
          </thead>
          <tbody>
            {data.map((log) => (
              <tr
                key={log.id}
                className="clickable"
                onClick={() => navigate(`/lots/${log.lotId}`)}
              >
                <td>{log.lotNo}</td>
                <td>{log.processName ?? '-'}</td>
                <td>{log.equipmentName ?? '-'}</td>
                <td className="num">{log.producedQty.toLocaleString()}</td>
                <td className="num">{log.defectQty.toLocaleString()}</td>
                <td>{formatDateTime(log.startedAt)}</td>
                <td>{formatDateTime(log.endedAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </>
  );
}
