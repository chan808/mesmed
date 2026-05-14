import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation } from '@tanstack/react-query';
import { lotApi, materialApi } from './api';
import { errorMessage } from '../../shared/api/client';

export function LotCreatePage() {
  const navigate = useNavigate();
  const { data: materials } = useQuery({
    queryKey: ['materials'],
    queryFn: materialApi.list,
  });

  const [rawMaterialId, setRawMaterialId] = useState<number | ''>('');
  const [quantity, setQuantity] = useState<number | ''>('');
  const [supplier, setSupplier] = useState('');
  const [lotNo, setLotNo] = useState('');

  const mutation = useMutation({
    mutationFn: lotApi.create,
    onSuccess: (lot) => navigate(`/lots/${lot.id}`),
  });

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (rawMaterialId === '' || quantity === '') return;
    mutation.mutate({
      rawMaterialId: Number(rawMaterialId),
      quantity: Number(quantity),
      supplier: supplier || undefined,
      lotNo: lotNo || undefined,
    });
  };

  return (
    <>
      <div className="content-header">
        <h1>LOT 입고 등록</h1>
      </div>

      <form onSubmit={handleSubmit}>
        <div className="field">
          <label>원자재</label>
          <select
            value={rawMaterialId}
            onChange={(e) => setRawMaterialId(e.target.value ? Number(e.target.value) : '')}
            required
          >
            <option value="">선택…</option>
            {materials?.map((m) => (
              <option key={m.id} value={m.id}>
                {m.name} ({m.code})
              </option>
            ))}
          </select>
        </div>

        <div className="field">
          <label>수량</label>
          <input
            type="number"
            min={1}
            value={quantity}
            onChange={(e) => setQuantity(e.target.value ? Number(e.target.value) : '')}
            required
          />
        </div>

        <div className="field">
          <label>공급사 (선택)</label>
          <input value={supplier} onChange={(e) => setSupplier(e.target.value)} />
        </div>

        <div className="field">
          <label>LOT 번호 (비우면 자동 생성)</label>
          <input
            value={lotNo}
            onChange={(e) => setLotNo(e.target.value)}
            placeholder="예: LOT-20260514-001"
          />
        </div>

        {mutation.isError && (
          <div className="error">{errorMessage(mutation.error)}</div>
        )}

        <div className="row-end">
          <button type="button" onClick={() => navigate('/lots')}>
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
