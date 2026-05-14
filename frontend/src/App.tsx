import { Routes, Route, Navigate } from 'react-router-dom';
import { Layout } from './shared/components/Layout';
import { ProtectedRoute } from './shared/components/ProtectedRoute';
import { LoginPage } from './features/auth/LoginPage';
import { DashboardPage } from './features/dashboard/DashboardPage';
import { MaterialListPage } from './features/material/MaterialListPage';
import { MaterialCreatePage } from './features/material/MaterialCreatePage';
import { MaterialDetailPage } from './features/material/MaterialDetailPage';
import { SpecFormPage } from './features/material/SpecFormPage';
import { LotListPage } from './features/material/LotListPage';
import { LotCreatePage } from './features/material/LotCreatePage';
import { LotDetailPage } from './features/material/LotDetailPage';
import { InspectionCreatePage } from './features/inspection/InspectionCreatePage';
import { EquipmentListPage } from './features/production/EquipmentListPage';
import { EquipmentCreatePage } from './features/production/EquipmentCreatePage';
import { AlarmListPage } from './features/production/AlarmListPage';
import { ProductionLogListPage } from './features/production/ProductionLogListPage';
import { ProductionLogCreatePage } from './features/production/ProductionLogCreatePage';

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<DashboardPage />} />

        <Route path="/materials" element={<MaterialListPage />} />
        <Route path="/materials/new" element={<MaterialCreatePage />} />
        <Route path="/materials/:id" element={<MaterialDetailPage />} />
        <Route path="/materials/:materialId/specs/new" element={<SpecFormPage />} />
        <Route
          path="/materials/:materialId/specs/:specId/edit"
          element={<SpecFormPage />}
        />

        <Route path="/lots" element={<LotListPage />} />
        <Route path="/lots/new" element={<LotCreatePage />} />
        <Route path="/lots/:id" element={<LotDetailPage />} />

        <Route path="/inspections/new" element={<InspectionCreatePage />} />

        <Route path="/production-logs" element={<ProductionLogListPage />} />
        <Route path="/production-logs/new" element={<ProductionLogCreatePage />} />

        <Route path="/equipment" element={<EquipmentListPage />} />
        <Route path="/equipment/new" element={<EquipmentCreatePage />} />

        <Route path="/alarms" element={<AlarmListPage />} />
      </Route>

      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
