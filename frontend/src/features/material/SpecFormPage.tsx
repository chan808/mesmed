import { useState, useEffect, type FormEvent } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { materialApi, specApi } from './api';
import type { MeasureType } from '../../shared/api/types';
import { errorMessage } from '../../shared/api/client';

interface FormState {
  itemName: string;
  specDesc: string;
  method: string;
  equipment: string;
  timing: string;
  measureType: MeasureType;
  minValue: string;
  maxValue: string;
  unit: string;
}

const INITIAL: FormState = {
  itemName: '',
  specDesc: '',
  method: '',
  equipment: '',
  timing: '',
  measureType: 'VISUAL',
  minValue: '',
  maxValue: '',
  unit: '',
};

export function SpecFormPage() {
  const { materialId: materialIdParam, specId: specIdParam } = useParams<{
    materialId: string;
    specId?: string;
  }>();
  const materialId = Number(materialIdParam);
  const specId = specIdParam ? Number(specIdParam) : undefined;
  const isEdit = specId !== undefined;

  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data: material } = useQuery({
    queryKey: ['materials', materialId],
    queryFn: () => materialApi.detail(materialId),
    enabled: !Number.isNaN(materialId),
  });

  const { data: specs } = useQuery({
    queryKey: ['materials', materialId, 'specs'],
    queryFn: () => materialApi.specs(materialId),
    enabled: isEdit && !Number.isNaN(materialId),
  });

  const [form, setForm] = useState<FormState>(INITIAL);

  // Edit 모드: 기존 spec 값으로 prefill
  useEffect(() => {
    if (!isEdit || !specs) return;
    const target = specs.find((s) => s.id === specId);
    if (!target) return;
    setForm({
      itemName: target.itemName,
      specDesc: target.specDesc ?? '',
      method: target.method ?? '',
      equipment: target.equipment ?? '',
      timing: target.timing ?? '',
      measureType: target.measureType,
      minValue: target.minValue?.toString() ?? '',
      maxValue: target.maxValue?.toString() ?? '',
      unit: target.unit ?? '',
    });
  }, [isEdit, specs, specId]);

  const mutation = useMutation({
    mutationFn: () => {
      const body = {
        rawMaterialId: materialId,
        itemName: form.itemName,
        specDesc: form.specDesc || undefined,
        method: form.method || undefined,
        equipment: form.equipment || undefined,
        timing: form.timing || undefined,
        measureType: form.measureType,
        minValue: form.measureType === 'NUMERIC' && form.minValue ? Number(form.minValue) : undefined,
        maxValue: form.measureType === 'NUMERIC' && form.maxValue ? Number(form.maxValue) : undefined,
        unit: form.measureType === 'NUMERIC' && form.unit ? form.unit : undefined,
      };
      return isEdit ? specApi.update(specId, body) : specApi.create(body);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['materials', materialId, 'specs'] });
      navigate(`/materials/${materialId}`);
    },
  });

  const update = <K extends keyof FormState>(key: K, value: FormState[K]) =>
    setForm((prev) => ({ ...prev, [key]: value }));

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    mutation.mutate();
  };

  return (
    <>
      <div className="content-header">
        <h1>
          {isEdit ? '검사기준 수정' : '검사기준 추가'}
          {material && (
            <span className="muted" style={{ fontSize: 14, marginLeft: 8 }}>
              — {material.name} ({material.code})
            </span>
          )}
        </h1>
      </div>

      <form onSubmit={handleSubmit}>
        <div className="field">
          <label>검사 항목명</label>
          <input
            value={form.itemName}
            onChange={(e) => update('itemName', e.target.value)}
            placeholder="예: 치수, 발색, ECG 성능"
            required
          />
        </div>

        <div className="field">
          <label>규격 (자유 설명)</label>
          <input
            value={form.specDesc}
            onChange={(e) => update('specDesc', e.target.value)}
            placeholder="예: 30mm ± 1mm / 이상 없을 것"
          />
        </div>

        <div className="field-row">
          <div className="field" style={{ flex: 1 }}>
            <label>검사 방법</label>
            <input
              value={form.method}
              onChange={(e) => update('method', e.target.value)}
              placeholder="예: 측정 / 육안 / TEST"
            />
          </div>
          <div className="field" style={{ flex: 1 }}>
            <label>측정기기</label>
            <input
              value={form.equipment}
              onChange={(e) => update('equipment', e.target.value)}
              placeholder="예: 캘리퍼 / ECG시뮬레이터"
            />
          </div>
          <div className="field" style={{ flex: 1 }}>
            <label>타이밍</label>
            <input
              value={form.timing}
              onChange={(e) => update('timing', e.target.value)}
              placeholder="예: 전수 / 샘플"
            />
          </div>
        </div>

        <div className="field">
          <label>측정 유형</label>
          <select
            value={form.measureType}
            onChange={(e) => update('measureType', e.target.value as MeasureType)}
          >
            <option value="VISUAL">VISUAL — 검사자가 PASS/FAIL 직접 선택</option>
            <option value="NUMERIC">NUMERIC — 측정값으로 서버 자동 판정</option>
          </select>
        </div>

        {form.measureType === 'NUMERIC' && (
          <div className="field-row">
            <div className="field" style={{ flex: 1 }}>
              <label>최소값</label>
              <input
                type="number"
                step="any"
                value={form.minValue}
                onChange={(e) => update('minValue', e.target.value)}
                placeholder="예: 29"
              />
            </div>
            <div className="field" style={{ flex: 1 }}>
              <label>최대값</label>
              <input
                type="number"
                step="any"
                value={form.maxValue}
                onChange={(e) => update('maxValue', e.target.value)}
                placeholder="예: 31"
              />
            </div>
            <div className="field" style={{ flex: 1 }}>
              <label>단위</label>
              <input
                value={form.unit}
                onChange={(e) => update('unit', e.target.value)}
                placeholder="예: mm / V"
              />
            </div>
          </div>
        )}

        {mutation.isError && <div className="error">{errorMessage(mutation.error)}</div>}

        {isEdit && (
          <p className="muted" style={{ fontSize: 12 }}>
            수정 시 기존 기준은 자동으로 구버전으로 보존되고 신버전이 생성됩니다.
          </p>
        )}

        <div className="row-end">
          <button type="button" onClick={() => navigate(`/materials/${materialId}`)}>
            취소
          </button>
          <button type="submit" className="primary" disabled={mutation.isPending}>
            {mutation.isPending ? '저장 중…' : isEdit ? '수정' : '등록'}
          </button>
        </div>
      </form>
    </>
  );
}
