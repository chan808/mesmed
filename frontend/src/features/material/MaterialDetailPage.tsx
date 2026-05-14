import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate, useParams } from 'react-router-dom';
import { materialApi, specApi } from './api';
import { useAuth } from '../../shared/auth/useAuth';
import { errorMessage } from '../../shared/api/client';
import { formatDateTime } from '../../shared/util/format';
import {
  CATEGORY_LABEL,
  EQUIPMENT_LABEL,
  METHOD_LABEL,
} from '../../shared/api/types';
import type { InspectionSpec } from './types';

function renderMethod(s: InspectionSpec): string {
  if (!s.method) return '-';
  if (s.method === 'OTHER') return s.methodCustom ?? '기타';
  return METHOD_LABEL[s.method];
}

function renderEquipment(s: InspectionSpec): string {
  if (!s.equipment) return '-';
  if (s.equipment === 'OTHER') return s.equipmentCustom ?? '기타';
  return EQUIPMENT_LABEL[s.equipment];
}

export function MaterialDetailPage() {
  const { id } = useParams<{ id: string }>();
  const materialId = Number(id);
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { role } = useAuth();
  const isAdmin = role === 'ADMIN';

  const { data: material } = useQuery({
    queryKey: ['materials', materialId],
    queryFn: () => materialApi.detail(materialId),
    enabled: !Number.isNaN(materialId),
  });

  const { data: specs, isLoading: specsLoading } = useQuery({
    queryKey: ['materials', materialId, 'specs'],
    queryFn: () => materialApi.specs(materialId),
    enabled: !Number.isNaN(materialId),
  });

  const removeSpec = useMutation({
    mutationFn: specApi.remove,
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: ['materials', materialId, 'specs'] }),
  });

  const removeMaterial = useMutation({
    mutationFn: materialApi.remove,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['materials'] });
      navigate('/materials');
    },
  });

  if (!material) return <p className="empty">로딩 중…</p>;

  return (
    <>
      <div className="content-header">
        <h1>
          {material.name} <span className="muted">({material.code})</span>
        </h1>
        {isAdmin && (
          <button
            className="danger"
            onClick={() => {
              if (window.confirm(`원자재 "${material.name}"를 삭제하시겠습니까?`)) {
                removeMaterial.mutate(material.id);
              }
            }}
          >
            원자재 삭제
          </button>
        )}
      </div>

      <div className="section">
        <div className="section-title">원자재 정보</div>
        <table>
          <tbody>
            <tr>
              <th style={{ width: 140 }}>분류</th>
              <td>{material.category ?? '-'}</td>
              <th style={{ width: 100 }}>단위</th>
              <td>{material.unit ?? '-'}</td>
            </tr>
            <tr>
              <th>대표 규격</th>
              <td colSpan={3}>{material.specStandard ?? '-'}</td>
            </tr>
            <tr>
              <th>등록일</th>
              <td colSpan={3}>{formatDateTime(material.createdAt)}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div className="section">
        <div className="row" style={{ justifyContent: 'space-between', marginBottom: 12 }}>
          <div className="section-title" style={{ margin: 0 }}>
            검사기준 ({specs?.length ?? 0})
          </div>
          {isAdmin && (
            <button
              className="primary"
              onClick={() => navigate(`/materials/${materialId}/specs/new`)}
            >
              + 검사기준 추가
            </button>
          )}
        </div>

        {specsLoading && <p className="empty">로딩 중…</p>}
        {specs && specs.length === 0 && (
          <p className="empty">등록된 검사기준이 없습니다.</p>
        )}
        {specs && specs.length > 0 && (
          <table>
            <thead>
              <tr>
                <th>분류</th>
                <th>항목</th>
                <th>규격</th>
                <th>측정 유형</th>
                <th>범위</th>
                <th>방법</th>
                <th>측정기기</th>
                <th>v</th>
                {isAdmin && <th></th>}
              </tr>
            </thead>
            <tbody>
              {specs.map((s) => (
                <tr key={s.id}>
                  <td>{CATEGORY_LABEL[s.category]}</td>
                  <td>{s.itemName}</td>
                  <td className="muted">{s.specDesc ?? '-'}</td>
                  <td>{s.measureType}</td>
                  <td className="muted">
                    {s.measureType === 'NUMERIC'
                      ? `${s.minValue ?? '-'} ~ ${s.maxValue ?? '-'} ${s.unit ?? ''}`
                      : '-'}
                  </td>
                  <td>{renderMethod(s)}</td>
                  <td>{renderEquipment(s)}</td>
                  <td className="num">{s.version}</td>
                  {isAdmin && (
                    <td>
                      <div className="row" style={{ gap: 6 }}>
                        <button
                          onClick={() =>
                            navigate(`/materials/${materialId}/specs/${s.id}/edit`)
                          }
                        >
                          수정
                        </button>
                        <button
                          className="danger"
                          onClick={() => {
                            if (
                              window.confirm(
                                `검사기준 "${s.itemName}"을 삭제하시겠습니까?`,
                              )
                            ) {
                              removeSpec.mutate(s.id);
                            }
                          }}
                        >
                          삭제
                        </button>
                      </div>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {removeSpec.isError && (
          <div className="error mt-16">{errorMessage(removeSpec.error)}</div>
        )}
        {removeMaterial.isError && (
          <div className="error mt-16">{errorMessage(removeMaterial.error)}</div>
        )}
        {isAdmin && (
          <p className="muted mt-16" style={{ fontSize: 12 }}>
            검사기준 "수정"은 기존 기준을 구버전으로 보존하고 신버전을 생성합니다 (이력 무결성 유지).
          </p>
        )}
      </div>
    </>
  );
}
