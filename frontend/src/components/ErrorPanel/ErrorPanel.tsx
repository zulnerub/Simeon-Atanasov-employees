import type { ApiErrorResponse } from '../../types/collaboration';

type ErrorPanelProps = {
  error: ApiErrorResponse;
};

export function ErrorPanel({ error }: ErrorPanelProps) {
  return (
    <section className="card error-card">
      <h2>{error.message}</h2>
      {error.errors.length > 0 && (
        <ul>
          {error.errors.map((item) => (
            <li key={item}>{item}</li>
          ))}
        </ul>
      )}
    </section>
  );
}
