type FileUploadProps = {
  selectedFile: File | null;
  analyzing: boolean;
  onFileSelected: (file: File | null) => void;
  onAnalyze: () => void;
};

export function FileUpload({ selectedFile, analyzing, onFileSelected, onAnalyze }: FileUploadProps) {
  return (
    <section className="card upload-card">
      <div>
        <h2>Upload CSV file</h2>
        <p className="muted">Expected columns: EmpID, ProjectID, DateFrom, DateTo.</p>
      </div>

      <div className="upload-row">
        <input
          data-testid="file-input"
          type="file"
          accept=".csv,text/csv"
          onChange={(event) => onFileSelected(event.target.files?.[0] ?? null)}
        />
        <button data-testid="analyze-button" type="button" disabled={!selectedFile || analyzing} onClick={onAnalyze}>
          {analyzing ? 'Analyzing...' : 'Analyze'}
        </button>
      </div>

      {selectedFile && <p data-testid="selected-file" className="selected-file">Selected file: {selectedFile.name}</p>}
    </section>
  );
}
