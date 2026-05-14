import { useState, useMemo, type FormEvent } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { lotApi } from '../material/api';
import { equipmentApi, productionLogApi } from './api';
import { errorMessage } from '../../shared/api/client';

export function ProductionLogCreatePage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [searchParams] = useSearchParams();

  const { data: lots } = useQuery({ queryKey: ['lots'], queryFn: lotApi.list });
  const { data: equipment } = useQuery({
    queryKey: ['equipment'],
    queryFn: equipmentApi.list,
  });

  const passLots = useMemo(() => lots?.filter((l) => l.status === 'PASS') ?? [], [lots]);

  // ?lotId= 쿼리 파라미터로 들어오면 사전 선택
  const initialLotId = searchParams.get('lotId');
  const [lotId, setLotId] = useState<number | ''>(
    initialLotId ? Number(initialLotId) : '',
  );
  const [processName, setProcessName] = useState('');
  const [equipmentId, setEquipmentId] = useState<number | ''>('');
  const [producedQty, setProducedQty] = useState<number | ''>('');
  const [defectQty, setDefectQty] = useState<number | ''>('');
  const [startedAt, setStartedAt] = useState('');
  const [endedAt, setEndedAt] = useState('');

  const mutation = useMutation({
    mutationFn: productionLogApi.create,
    onSuccess: (log) => {
      queryClient.invalidateQueries({ queryKey: ['production-logs'] });
      queryClient.invalidateQueries({ queryKey: ['lots', log.lotId, 'history'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      navigate(`/lots/${log.lotId}`);
    },
  });

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (lotId === '' || producedQty === '' || !processName) return;
    mutation.mutate({
      lotId: Number(lotId),
      processName,
      equipmentId: equipmentId === '' ? undefined : Number(equipmentId),
      producedQty: Number(producedQty),
      defectQty: defectQty === '' ? undefined : Number(defectQty),
      startedAt: startedAt ? new Date(startedAt).toISOString() : undefined,
      endedAt: endedAt ? new Date(endedAt).toISOString() : undefined,
    });
  };

  return (
    <>
      <div className="content-header">
        <h1>생산 이력 등록</h1>
      </div>

      <form onSubmit={handleSubmit}>
        <div className="field">
          <label>대상 LOT (PASS 상태만)</label>
          <select
            value={lotId}
            onChange={(e) => setLotId(e.target.value ? Number(e.target.value) : '')}
            required
          >
            <option value="">선택…</option>
            {passLots.map((l) => (
              <option key={l.id} value={l.id}>
                {l.lotNo} — {l.rawMaterialName} (수량 {l.quantity.toLocaleString()})
              </option>
            ))}
          </select>
          {passLots.length === 0 && (
            <span className="error">
              PASS 상태의 LOT이 없습니다. 먼저 수입검사를 통과시켜 주세요.
            </span>
          )}
        </div>

        <div className="field">
          <label>공정명</label>
          <input
            value={processName}
            onChange={(e) => setProcessName(e.target.value)}
            placeholder="예: 점착제 코팅, 조립, 포장"
            required
          />
        </div>

        <div className="field">
          <label>설비 (선택)</label>
          <select
            value={equipmentId}
            onChange={(e) =>
              setEquipmentId(e.target.value ? Number(e.target.value) : '')
            }
          >
            <option value="">없음</option>
            {equipment?.map((eq) => (
              <option key={eq.id} value={eq.id}>
                {eq.name} ({eq.equipmentCode}) [{eq.status}]
              </option>
            ))}
          </select>
        </div>

        <div className="field-row">
          <div className="field" style={{ flex: 1 }}>
            <label>생산 수량</label>
            <input
              type="number"
              min={1}
              value={producedQty}
              onChange={(e) =>
                setProducedQty(e.target.value ? Number(e.target.value) : '')
              }
              required
            />
          </div>
          <div className="field" style={{ flex: 1 }}>
            <label>불량 수량 (선택, 기본 0)</label>
            <input
              type="number"
              min={0}
              value={defectQty}
              onChange={(e) =>
                setDefectQty(e.target.value ? Number(e.target.value) : '')
              }
            />
          </div>
        </div>

        <div className="field-row">
          <div className="field" style={{ flex: 1 }}>
            <label>시작 (선택, 기본 현재)</label>
            <input
              type="datetime-local"
              value={startedAt}
              onChange={(e) => setStartedAt(e.target.value)}
            />
          </div>
          <div className="field" style={{ flex: 1 }}>
            <label>종료 (선택)</label>
            <input
              type="datetime-local"
              value={endedAt}
              onChange={(e) => setEndedAt(e.target.value)}
            />
          </div>
        </div>

        {mutation.isError && <div className="error">{errorMessage(mutation.error)}</div>}

        <p className="muted" style={{ fontSize: 12 }}>
          PASS 아닌 LOT으로 등록 시도 시 서버에서 LOT_NOT_PASSED 예외가 발생합니다.
        </p>

        <div className="row-end">
          <button type="button" onClick={() => navigate('/production-logs')}>
            취소
          </button>
          <button
            type="submit"
            className="primary"
            disabled={mutation.isPending || passLots.length === 0}
          >
            {mutation.isPending ? '등록 중…' : '등록'}
          </button>
        </div>
      </form>
    </>
  );
}
