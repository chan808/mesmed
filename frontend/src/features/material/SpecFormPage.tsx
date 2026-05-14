import { useState, useEffect, type FormEvent } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { materialApi, specApi } from './api';
import type {
  InspectionCategory,
  InspectionEquipment,
  InspectionMethod,
  InspectionTiming,
  MeasureType,
} from '../../shared/api/types';
import {
  CATEGORY_LABEL,
  CATEGORY_PRESET,
  EQUIPMENT_LABEL,
  METHOD_LABEL,
  TIMING_LABEL,
} from '../../shared/api/types';
import { errorMessage } from '../../shared/api/client';

interface FormState {
  category: InspectionCategory;
  itemName: string;
  specDesc: string;
  method: InspectionMethod | '';
  methodCustom: string;
  equipment: InspectionEquipment | '';
  equipmentCustom: string;
  timing: InspectionTiming | '';
  timingCustom: string;
  measureType: MeasureType;
  minValue: string;
  maxValue: string;
  unit: string;
}

const INITIAL: FormState = {
  category: 'OTHER',
  itemName: '',
  specDesc: '',
  method: '',
  methodCustom: '',
  equipment: '',
  equipmentCustom: '',
  timing: '',
  timingCustom: '',
  measureType: 'VISUAL',
  minValue: '',
  maxValue: '',
  unit: '',
};

const CATEGORIES: InspectionCategory[] = [
  'DIMENSION', 'APPEARANCE', 'COLOR', 'ELECTRICAL', 'PERFORMANCE', 'THERMAL', 'OTHER',
];
const METHODS: InspectionMethod[] = ['MEASURE', 'VISUAL_CHECK', 'TEST', 'OTHER'];
const EQUIPMENTS: InspectionEquipment[] = [
  'CALIPER', 'MICROMETER', 'MULTIMETER', 'ECG_SIMULATOR',
  'VISUAL_INSPECTION', 'THERMAL_CAMERA', 'STOPWATCH', 'HOST_DEVICE', 'OTHER',
];
const TIMINGS: InspectionTiming[] = ['FULL', 'SAMPLE', 'OTHER'];

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

  // Edit 모드 prefill
  useEffect(() => {
    if (!isEdit || !specs) return;
    const t = specs.find((s) => s.id === specId);
    if (!t) return;
    setForm({
      category: t.category,
      itemName: t.itemName,
      specDesc: t.specDesc ?? '',
      method: t.method ?? '',
      methodCustom: t.methodCustom ?? '',
      equipment: t.equipment ?? '',
      equipmentCustom: t.equipmentCustom ?? '',
      timing: t.timing ?? '',
      timingCustom: t.timingCustom ?? '',
      measureType: t.measureType,
      minValue: t.minValue?.toString() ?? '',
      maxValue: t.maxValue?.toString() ?? '',
      unit: t.unit ?? '',
    });
  }, [isEdit, specs, specId]);

  // 카테고리 변경 → preset으로 다른 필드 자동 채움
  // 단, 사용자가 이미 만진 값을 덮어쓰지 않으려면 정책 필요.
  // 여기선 단순화: "카테고리 변경 = 명시적 의도"로 보고 preset을 덮어씀.
  const applyCategoryPreset = (next: InspectionCategory) => {
    const preset = CATEGORY_PRESET[next];
    setForm((prev) => ({
      ...prev,
      category: next,
      method: preset.method ?? '',
      methodCustom: '',
      equipment: preset.equipment ?? '',
      equipmentCustom: '',
      measureType: preset.measureType ?? prev.measureType,
      unit: preset.unit ?? prev.unit,
    }));
  };

  const update = <K extends keyof FormState>(key: K, value: FormState[K]) =>
    setForm((prev) => ({ ...prev, [key]: value }));

  const mutation = useMutation({
    mutationFn: () => {
      const body = {
        rawMaterialId: materialId,
        category: form.category,
        itemName: form.itemName,
        specDesc: form.specDesc || undefined,
        method: form.method || undefined,
        methodCustom:
          form.method === 'OTHER' && form.methodCustom
            ? form.methodCustom
            : undefined,
        equipment: form.equipment || undefined,
        equipmentCustom:
          form.equipment === 'OTHER' && form.equipmentCustom
            ? form.equipmentCustom
            : undefined,
        timing: form.timing || undefined,
        timingCustom:
          form.timing === 'OTHER' && form.timingCustom
            ? form.timingCustom
            : undefined,
        measureType: form.measureType,
        minValue:
          form.measureType === 'NUMERIC' && form.minValue
            ? Number(form.minValue)
            : undefined,
        maxValue:
          form.measureType === 'NUMERIC' && form.maxValue
            ? Number(form.maxValue)
            : undefined,
        unit:
          form.measureType === 'NUMERIC' && form.unit ? form.unit : undefined,
      };
      return isEdit ? specApi.update(specId, body) : specApi.create(body);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['materials', materialId, 'specs'] });
      navigate(`/materials/${materialId}`);
    },
  });

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
          <label>검사 항목 분류 (Category)</label>
          <select
            value={form.category}
            onChange={(e) => applyCategoryPreset(e.target.value as InspectionCategory)}
          >
            {CATEGORIES.map((c) => (
              <option key={c} value={c}>
                {CATEGORY_LABEL[c]} ({c})
              </option>
            ))}
          </select>
          <span className="muted" style={{ fontSize: 12 }}>
            분류를 바꾸면 검사방법·측정기기·측정유형·단위가 추천값으로 채워집니다. 모두 수정 가능.
          </span>
        </div>

        <div className="field">
          <label>검사 항목명 (itemName, 자유)</label>
          <input
            value={form.itemName}
            onChange={(e) => update('itemName', e.target.value)}
            placeholder="예: 외경 치수, 발색 30초 후, ECG 신호 반응"
            required
          />
        </div>

        <div className="field">
          <label>규격 설명 (specDesc, 자유)</label>
          <input
            value={form.specDesc}
            onChange={(e) => update('specDesc', e.target.value)}
            placeholder="예: 30mm ± 1mm / 이상 없을 것"
          />
        </div>

        <div className="field-row">
          <div className="field" style={{ flex: 1 }}>
            <label>검사 방법</label>
            <select
              value={form.method}
              onChange={(e) => update('method', e.target.value as InspectionMethod | '')}
            >
              <option value="">선택 안 함</option>
              {METHODS.map((m) => (
                <option key={m} value={m}>
                  {METHOD_LABEL[m]}
                </option>
              ))}
            </select>
            {form.method === 'OTHER' && (
              <input
                value={form.methodCustom}
                onChange={(e) => update('methodCustom', e.target.value)}
                placeholder="기타 방법 자유 입력"
                style={{ marginTop: 6 }}
              />
            )}
          </div>

          <div className="field" style={{ flex: 1 }}>
            <label>측정 기기</label>
            <select
              value={form.equipment}
              onChange={(e) =>
                update('equipment', e.target.value as InspectionEquipment | '')
              }
            >
              <option value="">선택 안 함</option>
              {EQUIPMENTS.map((eq) => (
                <option key={eq} value={eq}>
                  {EQUIPMENT_LABEL[eq]}
                </option>
              ))}
            </select>
            {form.equipment === 'OTHER' && (
              <input
                value={form.equipmentCustom}
                onChange={(e) => update('equipmentCustom', e.target.value)}
                placeholder="기타 측정기기 자유 입력"
                style={{ marginTop: 6 }}
              />
            )}
          </div>

          <div className="field" style={{ flex: 1 }}>
            <label>검사 타이밍</label>
            <select
              value={form.timing}
              onChange={(e) => update('timing', e.target.value as InspectionTiming | '')}
            >
              <option value="">선택 안 함</option>
              {TIMINGS.map((t) => (
                <option key={t} value={t}>
                  {TIMING_LABEL[t]}
                </option>
              ))}
            </select>
            {form.timing === 'OTHER' && (
              <input
                value={form.timingCustom}
                onChange={(e) => update('timingCustom', e.target.value)}
                placeholder="기타 타이밍 자유 입력"
                style={{ marginTop: 6 }}
              />
            )}
          </div>
        </div>

        <div className="field">
          <label>측정 유형 (measureType)</label>
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
                placeholder="예: mm / V / ℃"
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
