import { useQuery } from '@tanstack/react-query';
import { useNavigate, useParams } from 'react-router-dom';
import { lotApi } from './api';
import { StatusBadge } from '../../shared/components/StatusBadge';
import { formatDateTime } from '../../shared/util/format';
import { useAuth } from '../../shared/auth/useAuth';

export function LotDetailPage() {
  const { id } = useParams<{ id: string }>();
  const lotId = Number(id);
  const navigate = useNavigate();
  const { role } = useAuth();
  const canCreateProduction = role === 'ADMIN' || role === 'OPERATOR';
  const canCreateInspection = role === 'ADMIN' || role === 'INSPECTOR';

  const { data, isLoading, isError } = useQuery({
    queryKey: ['lots', lotId, 'history'],
    queryFn: () => lotApi.history(lotId),
    enabled: !Number.isNaN(lotId),
  });

  if (isLoading) return <p className="empty">로딩 중…</p>;
  if (isError || !data) return <p className="error">LOT 이력을 불러올 수 없습니다.</p>;

  const { lot, inspectionRecords, productionLogs } = data;
  const isPass = lot.status === 'PASS';
  const canInspect = lot.status !== 'PASS';

  return (
    <>
      <div className="content-header">
        <h1>{lot.lotNo}</h1>
        <div className="row">
          {canCreateInspection && canInspect && (
            <button onClick={() => navigate('/inspections/new')}>
              + 이 LOT 검사 등록
            </button>
          )}
          {canCreateProduction && isPass && (
            <button
              className="primary"
              onClick={() => navigate(`/production-logs/new?lotId=${lotId}`)}
              title="PASS 상태 LOT만 생산 등록 가능"
            >
              + 이 LOT으로 생산 등록
            </button>
          )}
        </div>
      </div>

      <div className="section">
        <div className="section-title">LOT 정보</div>
        <table>
          <tbody>
            <tr>
              <th style={{ width: 140 }}>원자재</th>
              <td>
                {lot.rawMaterialName} <span className="muted">({lot.rawMaterialCode})</span>
              </td>
              <th style={{ width: 100 }}>상태</th>
              <td>
                <StatusBadge value={lot.status} />
              </td>
            </tr>
            <tr>
              <th>수량</th>
              <td>{lot.quantity.toLocaleString()}</td>
              <th>공급사</th>
              <td>{lot.supplier ?? '-'}</td>
            </tr>
            <tr>
              <th>입고 시각</th>
              <td colSpan={3}>{formatDateTime(lot.receivedAt)}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div className="section">
        <div className="section-title">검사 이력 ({inspectionRecords.length})</div>
        {inspectionRecords.length === 0 && (
          <p className="empty">아직 검사가 등록되지 않았습니다.</p>
        )}
        {inspectionRecords.map((rec) => (
          <div key={rec.recordId} className="mb-16">
            <div className="row mb-16">
              <strong>검사 #{rec.recordId}</strong>
              {rec.overallResult && <StatusBadge value={rec.overallResult} />}
              <span className="muted">{formatDateTime(rec.inspectedAt)}</span>
              {rec.inspectorName && <span className="muted">담당: {rec.inspectorName}</span>}
            </div>
            {rec.note && <p className="muted">{rec.note}</p>}
            <table>
              <thead>
                <tr>
                  <th>검사 항목</th>
                  <th>규격</th>
                  <th>실측값</th>
                  <th>결과</th>
                  <th>심각도</th>
                </tr>
              </thead>
              <tbody>
                {rec.details.map((d, idx) => (
                  <tr key={idx}>
                    <td>{d.itemName}</td>
                    <td className="muted">{d.specDesc ?? '-'}</td>
                    <td>{d.measuredValue ?? '-'}</td>
                    <td>
                      <StatusBadge value={d.result} />
                    </td>
                    <td>
                      {d.severity ? <StatusBadge value={d.severity} suffix="sev" /> : '-'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ))}
      </div>

      <div className="section">
        <div className="section-title">생산 이력 ({productionLogs.length})</div>
        {productionLogs.length === 0 && (
          <p className="empty">
            {isPass
              ? '아직 생산 이력이 없습니다. 우측 상단 "이 LOT으로 생산 등록"으로 추가하세요.'
              : '아직 생산 이력이 없습니다. PASS 상태가 되어야 등록 가능합니다.'}
          </p>
        )}
        {productionLogs.length > 0 && (
          <table>
            <thead>
              <tr>
                <th>공정</th>
                <th>설비</th>
                <th>생산</th>
                <th>불량</th>
                <th>시작</th>
                <th>종료</th>
              </tr>
            </thead>
            <tbody>
              {productionLogs.map((p) => (
                <tr key={p.logId}>
                  <td>{p.processName ?? '-'}</td>
                  <td>{p.equipmentName ?? '-'}</td>
                  <td className="num">{p.producedQty.toLocaleString()}</td>
                  <td className="num">{p.defectQty.toLocaleString()}</td>
                  <td>{formatDateTime(p.startedAt)}</td>
                  <td>{formatDateTime(p.endedAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </>
  );
}
