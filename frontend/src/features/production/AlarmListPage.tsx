import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { alarmApi } from './api';
import { StatusBadge } from '../../shared/components/StatusBadge';
import { formatDateTime } from '../../shared/util/format';
import { errorMessage } from '../../shared/api/client';

export function AlarmListPage() {
  const [activeOnly, setActiveOnly] = useState(true);
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ['alarms', { activeOnly }],
    queryFn: () => alarmApi.list(activeOnly),
  });

  const resolve = useMutation({
    mutationFn: alarmApi.resolve,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['alarms'] }),
  });

  return (
    <>
      <div className="content-header">
        <h1>알람</h1>
        <label className="row" style={{ fontSize: 13 }}>
          <input
            type="checkbox"
            checked={activeOnly}
            onChange={(e) => setActiveOnly(e.target.checked)}
          />
          미해소만 보기
        </label>
      </div>

      {isLoading && <p className="empty">로딩 중…</p>}
      {data && data.length === 0 && <p className="empty">알람이 없습니다.</p>}
      {data && data.length > 0 && (
        <table>
          <thead>
            <tr>
              <th>설비</th>
              <th>코드</th>
              <th>메시지</th>
              <th>심각도</th>
              <th>발생</th>
              <th>해소</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {data.map((a) => (
              <tr key={a.id}>
                <td>{a.equipmentName}</td>
                <td>{a.alarmCode ?? '-'}</td>
                <td>{a.message ?? '-'}</td>
                <td>
                  <StatusBadge value={a.severity} />
                </td>
                <td>{formatDateTime(a.occurredAt)}</td>
                <td>{a.resolved ? formatDateTime(a.resolvedAt) : <span className="muted">진행중</span>}</td>
                <td>
                  {!a.resolved && (
                    <button onClick={() => resolve.mutate(a.id)} disabled={resolve.isPending}>
                      해소
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {resolve.isError && <div className="error">{errorMessage(resolve.error)}</div>}
    </>
  );
}
