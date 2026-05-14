import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { equipmentApi } from './api';
import { StatusBadge } from '../../shared/components/StatusBadge';
import { formatDateTime } from '../../shared/util/format';
import { useAuth } from '../../shared/auth/useAuth';
import { errorMessage } from '../../shared/api/client';
import type { EquipmentStatus } from '../../shared/api/types';

const STATUS_OPTIONS: EquipmentStatus[] = ['RUNNING', 'IDLE', 'ERROR', 'MAINTENANCE'];

export function EquipmentListPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { role } = useAuth();
  const canManage = role === 'ADMIN' || role === 'OPERATOR';

  const { data, isLoading } = useQuery({
    queryKey: ['equipment'],
    queryFn: equipmentApi.list,
  });

  const update = useMutation({
    mutationFn: ({ id, status }: { id: number; status: EquipmentStatus }) =>
      equipmentApi.updateStatus(id, status),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['equipment'] }),
  });

  return (
    <>
      <div className="content-header">
        <h1>설비</h1>
        {role === 'ADMIN' && (
          <button className="primary" onClick={() => navigate('/equipment/new')}>
            + 설비 등록
          </button>
        )}
      </div>

      {isLoading && <p className="empty">로딩 중…</p>}
      {data && data.length === 0 && <p className="empty">등록된 설비가 없습니다.</p>}
      {data && data.length > 0 && (
        <table>
          <thead>
            <tr>
              <th>코드</th>
              <th>이름</th>
              <th>상태</th>
              <th>마지막 정비</th>
              {canManage && <th>상태 변경</th>}
            </tr>
          </thead>
          <tbody>
            {data.map((eq) => (
              <tr key={eq.id}>
                <td>{eq.equipmentCode}</td>
                <td>{eq.name}</td>
                <td>
                  <StatusBadge value={eq.status} />
                </td>
                <td>{formatDateTime(eq.lastMaintainedAt)}</td>
                {canManage && (
                  <td>
                    <select
                      value={eq.status}
                      onChange={(e) =>
                        update.mutate({
                          id: eq.id,
                          status: e.target.value as EquipmentStatus,
                        })
                      }
                      disabled={update.isPending}
                    >
                      {STATUS_OPTIONS.map((s) => (
                        <option key={s} value={s}>
                          {s}
                        </option>
                      ))}
                    </select>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {update.isError && (
        <div className="error mt-16">{errorMessage(update.error)}</div>
      )}
    </>
  );
}
