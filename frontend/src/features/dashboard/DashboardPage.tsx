import { useQuery } from '@tanstack/react-query';
import { dashboardApi } from './api';
import type { DailyProduction } from './api';

export function DashboardPage() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['dashboard'],
    queryFn: dashboardApi.get,
    refetchInterval: 5000,
  });

  const { data: dailyData } = useQuery({
    queryKey: ['dashboard-daily'],
    queryFn: dashboardApi.getDailyProduction,
    refetchInterval: 30000,
  });

  return (
    <>
      <div className="content-header">
        <h1>대시보드</h1>
        <span className="muted">5초마다 자동 새로고침</span>
      </div>

      {isLoading && <p className="empty">로딩 중…</p>}
      {isError && <p className="error">데이터를 불러올 수 없습니다.</p>}

      {data && (
        <>
          <div className="card-grid">
            <Card label="금일 생산량" value={data.todayProducedQty} />
            <Card label="활성 알람" value={data.activeAlarmCount} />
            <Card
              label="설비 가동률"
              value={`${(data.equipmentRunningRate * 100).toFixed(0)}%`}
            />
            <Card label="검사 대기 LOT" value={data.pendingLotCount} />
          </div>

          <div className="chart-grid">
            <div className="chart-panel">
              <div className="chart-panel-title">일별 생산량 (최근 7일)</div>
              <ProductionTable data={dailyData ?? []} />
            </div>

            <div className="chart-panel">
              <div className="chart-panel-title">설비 가동률</div>
              <DonutChart
                segments={[
                  { value: data.runningEquipmentCount, color: 'var(--c-pass)', label: '가동 중' },
                  {
                    value: data.totalEquipmentCount - data.runningEquipmentCount,
                    color: 'var(--c-border)',
                    label: '비가동',
                  },
                ]}
                center={`${(data.equipmentRunningRate * 100).toFixed(0)}%`}
              />
            </div>

            <div className="chart-panel">
              <div className="chart-panel-title">LOT 현황</div>
              <DonutChart
                segments={[
                  { value: data.pendingLotCount, color: 'var(--c-pending)', label: '대기' },
                  { value: data.passLotCount, color: 'var(--c-pass)', label: 'PASS' },
                  { value: data.failLotCount, color: 'var(--c-fail)', label: 'FAIL' },
                  { value: data.holdLotCount, color: 'var(--c-hold)', label: 'HOLD' },
                ]}
                center={`${data.pendingLotCount + data.passLotCount + data.failLotCount + data.holdLotCount}건`}
              />
            </div>
          </div>
        </>
      )}
    </>
  );
}

function Card({ label, value }: { label: string; value: number | string }) {
  return (
    <div className="card">
      <div className="card-label">{label}</div>
      <div className="card-value">{value}</div>
    </div>
  );
}

interface DonutSegment {
  value: number;
  color: string;
  label: string;
}

function DonutChart({ segments, center }: { segments: DonutSegment[]; center?: string }) {
  const r = 38;
  const cx = 50;
  const cy = 50;
  const strokeWidth = 14;
  const circ = 2 * Math.PI * r;

  const total = segments.reduce((acc, s) => acc + s.value, 0);

  let cumulativeLen = 0;

  return (
    <div className="donut-wrap">
      <div className="donut-svg-wrap">
        <svg viewBox="0 0 100 100" className="donut-svg">
          <circle cx={cx} cy={cy} r={r} fill="none" stroke="var(--c-bg-soft)" strokeWidth={strokeWidth} />
          {total === 0 ? (
            <circle
              cx={cx} cy={cy} r={r}
              fill="none"
              stroke="var(--c-border)"
              strokeWidth={strokeWidth}
            />
          ) : (
            segments.map((seg, i) => {
              if (seg.value === 0) return null;
              const dash = (seg.value / total) * circ;
              const dashoffset = circ / 4 - cumulativeLen;
              cumulativeLen += dash;
              return (
                <circle
                  key={i}
                  cx={cx} cy={cy} r={r}
                  fill="none"
                  stroke={seg.color}
                  strokeWidth={strokeWidth}
                  strokeDasharray={`${dash} ${circ - dash}`}
                  strokeDashoffset={dashoffset}
                />
              );
            })
          )}
        </svg>
        {center && <div className="donut-center">{center}</div>}
      </div>
      <div className="donut-legend">
        {segments.map((seg, i) => (
          <div key={i} className="legend-item">
            <span className="legend-dot" style={{ background: seg.color }} />
            <span className="legend-label">{seg.label}</span>
            <span className="legend-value">{seg.value}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

function ProductionTable({ data }: { data: DailyProduction[] }) {
  if (data.length === 0) {
    return <p className="empty" style={{ margin: '24px 0' }}>데이터 없음</p>;
  }

  const formatDate = (dateStr: string) => {
    const d = new Date(dateStr);
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    const days = ['일', '월', '화', '수', '목', '금', '토'];
    return `${mm}/${dd} (${days[d.getDay()]})`;
  };

  const max = Math.max(...data.map((d) => d.qty), 1);

  return (
    <table className="prod-table">
      <thead>
        <tr>
          <th>날짜</th>
          <th>생산량</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        {data.map((row) => (
          <tr key={row.date}>
            <td>{formatDate(row.date)}</td>
            <td className="prod-qty">{row.qty.toLocaleString()}</td>
            <td className="prod-bar-cell">
              <div className="prod-bar-bg">
                <div
                  className="prod-bar-fill"
                  style={{ width: `${(row.qty / max) * 100}%` }}
                />
              </div>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
