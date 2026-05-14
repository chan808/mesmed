import { useState, useMemo, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation } from '@tanstack/react-query';
import { lotApi, materialApi } from '../material/api';
import { inspectionApi } from './api';
import type { InspectionDetailItem } from './types';
import type { InspectionResult, InspectionSeverity } from '../../shared/api/types';
import { errorMessage } from '../../shared/api/client';
import { StatusBadge } from '../../shared/components/StatusBadge';

export function InspectionCreatePage() {
  const navigate = useNavigate();

  const { data: lots } = useQuery({
    queryKey: ['lots'],
    queryFn: lotApi.list,
  });

  const [lotId, setLotId] = useState<number | ''>('');
  const [note, setNote] = useState('');

  const selectedLot = useMemo(
    () => lots?.find((l) => l.id === lotId),
    [lots, lotId],
  );

  const { data: specs } = useQuery({
    queryKey: ['materials', selectedLot?.rawMaterialId, 'specs'],
    queryFn: () => materialApi.specs(selectedLot!.rawMaterialId),
    enabled: !!selectedLot,
  });

  const [inputs, setInputs] = useState<Record<number, { value: string; result: '' | InspectionResult; severity: '' | InspectionSeverity }>>({});

  const updateInput = (specId: number, patch: Partial<{ value: string; result: '' | InspectionResult; severity: '' | InspectionSeverity }>) => {
    setInputs((prev) => {
      const base = prev[specId] ?? { value: '', result: '' as const, severity: '' as const };
      return { ...prev, [specId]: { ...base, ...patch } };
    });
  };

  const mutation = useMutation({
    mutationFn: inspectionApi.create,
    onSuccess: (res) => navigate(`/lots/${res.lotId}`),
  });

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (!selectedLot || !specs) return;

    const details: InspectionDetailItem[] = specs.map((spec) => {
      const input = inputs[spec.id] ?? { value: '', result: '', severity: '' };
      return {
        inspectionSpecId: spec.id,
        measuredValue: input.value || null,
        // NUMERIC은 서버 자동 판정 → result null 전송 / VISUAL은 검사자 선택 필수
        result: spec.measureType === 'NUMERIC' ? null : (input.result || null),
        severity: input.severity || null,
      };
    });

    mutation.mutate({
      lotId: selectedLot.id,
      note: note || undefined,
      details,
    });
  };

  const inspectableLots = lots?.filter((l) => l.status !== 'PASS') ?? [];

  return (
    <>
      <div className="content-header">
        <h1>수입검사 등록</h1>
      </div>

      <form onSubmit={handleSubmit}>
        <div className="field">
          <label>대상 LOT</label>
          <select
            value={lotId}
            onChange={(e) => {
              setLotId(e.target.value ? Number(e.target.value) : '');
              setInputs({});
            }}
            required
          >
            <option value="">선택…</option>
            {inspectableLots.map((l) => (
              <option key={l.id} value={l.id}>
                {l.lotNo} — {l.rawMaterialName} [{l.status}]
              </option>
            ))}
          </select>
          <span className="muted" style={{ fontSize: 12 }}>
            PASS 상태가 아닌 LOT만 선택 가능합니다.
          </span>
        </div>

        {selectedLot && specs && specs.length === 0 && (
          <p className="error">
            이 원자재({selectedLot.rawMaterialName})에 등록된 검사 기준이 없습니다.
          </p>
        )}

        {selectedLot && specs && specs.length > 0 && (
          <div className="section">
            <div className="section-title">
              {selectedLot.rawMaterialName} 검사 항목 ({specs.length})
            </div>
            <table>
              <thead>
                <tr>
                  <th>항목</th>
                  <th>규격</th>
                  <th>측정 유형</th>
                  <th>실측값</th>
                  <th>판정</th>
                  <th>심각도</th>
                </tr>
              </thead>
              <tbody>
                {specs.map((spec) => {
                  const isNumeric = spec.measureType === 'NUMERIC';
                  const input = inputs[spec.id] ?? { value: '', result: '', severity: '' };
                  return (
                    <tr key={spec.id}>
                      <td>{spec.itemName}</td>
                      <td className="muted">{spec.specDesc ?? '-'}</td>
                      <td>{spec.measureType}</td>
                      <td>
                        <input
                          type={isNumeric ? 'number' : 'text'}
                          step={isNumeric ? 'any' : undefined}
                          value={input.value}
                          onChange={(e) => updateInput(spec.id, { value: e.target.value })}
                          placeholder={
                            isNumeric
                              ? `${spec.minValue ?? '-'} ~ ${spec.maxValue ?? '-'} ${spec.unit ?? ''}`
                              : '관찰 결과'
                          }
                          style={{ width: 140 }}
                        />
                      </td>
                      <td>
                        {isNumeric ? (
                          <span className="muted" style={{ fontSize: 12 }}>
                            서버 자동 판정
                          </span>
                        ) : (
                          <select
                            value={input.result}
                            onChange={(e) =>
                              updateInput(spec.id, {
                                result: e.target.value as '' | InspectionResult,
                              })
                            }
                          >
                            <option value="">선택…</option>
                            <option value="PASS">PASS</option>
                            <option value="FAIL">FAIL</option>
                          </select>
                        )}
                      </td>
                      <td>
                        <select
                          value={input.severity}
                          onChange={(e) =>
                            updateInput(spec.id, {
                              severity: e.target.value as '' | InspectionSeverity,
                            })
                          }
                        >
                          <option value="">-</option>
                          <option value="MINOR">MINOR</option>
                          <option value="MAJOR">MAJOR</option>
                          <option value="CRITICAL">CRITICAL</option>
                        </select>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}

        <div className="field">
          <label>비고 (선택)</label>
          <textarea
            value={note}
            onChange={(e) => setNote(e.target.value)}
            rows={3}
          />
        </div>

        {mutation.isError && (
          <div className="error">{errorMessage(mutation.error)}</div>
        )}

        {mutation.isSuccess && (
          <div className="row">
            <span>검사 결과:</span>
            <StatusBadge value={mutation.data.overallResult} />
            <span className="muted">→ LOT 상태로 반영됨</span>
          </div>
        )}

        <div className="row-end">
          <button type="button" onClick={() => navigate('/lots')}>
            취소
          </button>
          <button
            type="submit"
            className="primary"
            disabled={!selectedLot || !specs || specs.length === 0 || mutation.isPending}
          >
            {mutation.isPending ? '판정 중…' : '검사 등록 + 자동 판정'}
          </button>
        </div>
      </form>
    </>
  );
}
