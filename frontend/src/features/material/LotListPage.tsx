import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { lotApi } from './api';
import { StatusBadge } from '../../shared/components/StatusBadge';
import { formatDateTime } from '../../shared/util/format';

export function LotListPage() {
  const navigate = useNavigate();
  const { data, isLoading } = useQuery({
    queryKey: ['lots'],
    queryFn: lotApi.list,
  });

  return (
    <>
      <div className="content-header">
        <h1>LOT</h1>
        <button className="primary" onClick={() => navigate('/lots/new')}>
          + LOT 입고 등록
        </button>
      </div>

      {isLoading && <p className="empty">로딩 중…</p>}
      {data && data.length === 0 && (
        <p className="empty">등록된 LOT이 없습니다. 우측 상단에서 입고 등록해 보세요.</p>
      )}
      {data && data.length > 0 && (
        <table>
          <thead>
            <tr>
              <th>LOT 번호</th>
              <th>원자재</th>
              <th>수량</th>
              <th>공급사</th>
              <th>입고 시각</th>
              <th>상태</th>
            </tr>
          </thead>
          <tbody>
            {data.map((lot) => (
              <tr
                key={lot.id}
                className="clickable"
                onClick={() => navigate(`/lots/${lot.id}`)}
              >
                <td>{lot.lotNo}</td>
                <td>
                  {lot.rawMaterialName}
                  <span className="muted"> ({lot.rawMaterialCode})</span>
                </td>
                <td className="num">{lot.quantity.toLocaleString()}</td>
                <td>{lot.supplier ?? '-'}</td>
                <td>{formatDateTime(lot.receivedAt)}</td>
                <td>
                  <StatusBadge value={lot.status} />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </>
  );
}
