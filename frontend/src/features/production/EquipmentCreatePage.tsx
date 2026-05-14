import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { equipmentApi } from './api';
import { errorMessage } from '../../shared/api/client';

export function EquipmentCreatePage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [equipmentCode, setEquipmentCode] = useState('');
  const [name, setName] = useState('');

  const mutation = useMutation({
    mutationFn: equipmentApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['equipment'] });
      navigate('/equipment');
    },
  });

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    mutation.mutate({ equipmentCode, name });
  };

  return (
    <>
      <div className="content-header">
        <h1>설비 등록</h1>
      </div>

      <form onSubmit={handleSubmit}>
        <div className="field">
          <label>설비 코드</label>
          <input
            value={equipmentCode}
            onChange={(e) => setEquipmentCode(e.target.value)}
            placeholder="예: EQ-001"
            required
          />
        </div>
        <div className="field">
          <label>설비명</label>
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="예: 점착제 코팅기"
            required
          />
        </div>

        {mutation.isError && <div className="error">{errorMessage(mutation.error)}</div>}

        <div className="row-end">
          <button type="button" onClick={() => navigate('/equipment')}>
            취소
          </button>
          <button type="submit" className="primary" disabled={mutation.isPending}>
            {mutation.isPending ? '등록 중…' : '등록'}
          </button>
        </div>
      </form>
    </>
  );
}
