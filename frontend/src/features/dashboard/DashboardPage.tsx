import { useQuery } from '@tanstack/react-query';
import { dashboardApi } from './api';

export function DashboardPage() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['dashboard'],
    queryFn: dashboardApi.get,
    refetchInterval: 5000,
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
        <div className="card-grid">
          <Card label="금일 생산량" value={data.todayProducedQty} />
          <Card label="활성 알람" value={data.activeAlarmCount} />
          <Card
            label="설비 가동률"
            value={`${(data.equipmentRunningRate * 100).toFixed(0)}%`}
          />
          <Card label="검사 대기 LOT" value={data.pendingLotCount} />
        </div>
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
