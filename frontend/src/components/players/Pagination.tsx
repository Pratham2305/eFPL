import React from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

interface PaginationProps {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
  onPageChange: (newPage: number) => void;
  onSizeChange: (newSize: number) => void;
}

export const Pagination: React.FC<PaginationProps> = ({
  page,
  size,
  totalElements,
  totalPages,
  hasNext,
  hasPrevious,
  onPageChange,
  onSizeChange
}) => {
  const from = totalElements === 0 ? 0 : page * size + 1;
  const to = Math.min((page + 1) * size, totalElements);

  return (
    <div style={{
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      padding: '0.8rem 1rem',
      flexWrap: 'wrap',
      gap: '0.8rem',
      fontSize: '0.82rem',
      color: 'var(--text-secondary)'
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        <span>
          Showing <strong style={{ color: '#fff' }}>{from}</strong>–<strong style={{ color: '#fff' }}>{to}</strong> of <strong style={{ color: 'var(--accent-cyan)' }}>{totalElements}</strong> players
        </span>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
          <span>Per page:</span>
          <select
            value={size}
            onChange={(e) => onSizeChange(Number(e.target.value))}
            style={{
              background: 'rgba(0, 0, 0, 0.4)',
              border: '1px solid var(--border-color)',
              color: '#fff',
              borderRadius: 6,
              padding: '0.2rem 0.5rem',
              fontSize: '0.8rem'
            }}
          >
            <option value={15}>15</option>
            <option value={25}>25</option>
            <option value={50}>50</option>
            <option value={100}>100</option>
          </select>
        </div>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
        <span>Page <strong style={{ color: '#fff' }}>{totalPages === 0 ? 0 : page + 1}</strong> of <strong style={{ color: '#fff' }}>{totalPages}</strong></span>
        <button
          className="btn btn-secondary"
          disabled={!hasPrevious}
          onClick={() => onPageChange(page - 1)}
          style={{ padding: '0.3rem 0.6rem', display: 'flex', alignItems: 'center', gap: '0.2rem', cursor: hasPrevious ? 'pointer' : 'not-allowed', opacity: hasPrevious ? 1 : 0.4 }}
        >
          <ChevronLeft size={14} /> Prev
        </button>
        <button
          className="btn btn-secondary"
          disabled={!hasNext}
          onClick={() => onPageChange(page + 1)}
          style={{ padding: '0.3rem 0.6rem', display: 'flex', alignItems: 'center', gap: '0.2rem', cursor: hasNext ? 'pointer' : 'not-allowed', opacity: hasNext ? 1 : 0.4 }}
        >
          Next <ChevronRight size={14} />
        </button>
      </div>
    </div>
  );
};

export default Pagination;
