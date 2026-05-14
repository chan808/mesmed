import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { materialApi } from './api';
import { errorMessage } from '../../shared/api/client';

export function MaterialCreatePage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const [code, setCode] = useState('');
  const [name, setName] = useState('');
  const [category, setCategory] = useState('');
  const [unit, setUnit] = useState('EA');
  const [specStandard, setSpecStandard] = useState('');

  const mutation = useMutation({
    mutationFn: materialApi.create,
    onSuccess: (m) => {
      queryClient.invalidateQueries({ queryKey: ['materials'] });
      navigate(`/materials/${m.id}`);
    },
  });

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    mutation.mutate({
      code,
      name,
      category: category || undefined,
      unit: unit || undefined,
      specStandard: specStandard || undefined,
    });
  };

  return (
    <>
      <div className="content-header">
        <h1>원자재 등록</h1>
      </div>

      <form onSubmit={handleSubmit}>
        <div className="field">
          <label>코드</label>
          <input
            value={code}
            onChange={(e) => setCode(e.target.value)}
            placeholder="예: RM-001"
            required
          />
        </div>
        <div className="field">
          <label>이름</label>
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="예: 펫패치A"
            required
          />
        </div>
        <div className="field">
          <label>분류 (선택)</label>
          <input
            value={category}
            onChange={(e) => setCategory(e.target.value)}
            placeholder="예: 바이오센서"
          />
        </div>
        <div className="field">
          <label>단위 (선택)</label>
          <input
            value={unit}
            onChange={(e) => setUnit(e.target.value)}
            placeholder="예: EA"
          />
        </div>
        <div className="field">
          <label>대표 규격 (선택)</label>
          <input
            value={specStandard}
            onChange={(e) => setSpecStandard(e.target.value)}
            placeholder="예: 30mm ± 1mm"
          />
        </div>

        {mutation.isError && <div className="error">{errorMessage(mutation.error)}</div>}

        <div className="row-end">
          <button type="button" onClick={() => navigate('/materials')}>
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
