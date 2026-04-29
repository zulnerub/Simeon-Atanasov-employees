import { useState } from 'react';
import { analyzeFile } from './api/collaborationApi';
import { ErrorPanel } from './components/ErrorPanel';
import { FileUpload } from './components/FileUpload';
import { ProjectOverlapTable } from './components/ProjectOverlapTable';
import { ResultSummary } from './components/ResultSummary';
import type { AnalyzeResponse, ApiErrorResponse } from './types/collaboration';
import './styles.css';

function App() {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [result, setResult] = useState<AnalyzeResponse | null>(null);
  const [error, setError] = useState<ApiErrorResponse | null>(null);
  const [analyzing, setAnalyzing] = useState(false);

  async function handleAnalyze() {
    if (!selectedFile) {
      return;
    }

    setAnalyzing(true);
    setResult(null);
    setError(null);

    try {
      const response = await analyzeFile(selectedFile);
      setResult(response);
    } catch (err) {
      setError(err as ApiErrorResponse);
    } finally {
      setAnalyzing(false);
    }
  }

  return (
    <main className="page">
      <header className="hero">
        <h1>Employee Collaboration Analyzer</h1>
        <p>
          Upload a CSV file and find the pair of employees who worked together on common projects
          for the longest total period.
        </p>
      </header>

      <FileUpload
        selectedFile={selectedFile}
        analyzing={analyzing}
        onFileSelected={setSelectedFile}
        onAnalyze={handleAnalyze}
      />

      {error && <ErrorPanel error={error} />}
      {result && (
              <>
                  <ResultSummary result={result} />
                  <ProjectOverlapTable projects={result.projects} />
              </>
          )
      }
    </main>
  );
}

export default App;
