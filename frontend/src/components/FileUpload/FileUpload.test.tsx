import { render, screen, fireEvent } from '@testing-library/react';
import { vi } from 'vitest';
import { FileUpload } from './FileUpload';

const defaultProps = {
  selectedFile: null,
  analyzing: false,
  onFileSelected: vi.fn(),
  onAnalyze: vi.fn(),
};

describe('FileUpload', () => {
  it('disables the analyze button when no file is selected', () => {
    render(<FileUpload {...defaultProps} />);

    expect(screen.getByTestId('analyze-button')).toBeDisabled();
  });

  it('disables the analyze button while analyzing', () => {
    const file = new File([''], 'test.csv', { type: 'text/csv' });
    render(<FileUpload {...defaultProps} selectedFile={file} analyzing={true} />);

    expect(screen.getByTestId('analyze-button')).toBeDisabled();
  });

  it('shows "Analyzing..." label while analyzing', () => {
    const file = new File([''], 'test.csv', { type: 'text/csv' });
    render(<FileUpload {...defaultProps} selectedFile={file} analyzing={true} />);

    expect(screen.getByTestId('analyze-button')).toHaveTextContent('Analyzing...');
  });

  it('enables the analyze button when a file is selected and not analyzing', () => {
    const file = new File([''], 'test.csv', { type: 'text/csv' });
    render(<FileUpload {...defaultProps} selectedFile={file} />);

    expect(screen.getByTestId('analyze-button')).toBeEnabled();
  });

  it('shows the selected file name', () => {
    const file = new File([''], 'employees.csv', { type: 'text/csv' });
    render(<FileUpload {...defaultProps} selectedFile={file} />);

    expect(screen.getByTestId('selected-file')).toHaveTextContent('employees.csv');
  });

  it('does not show selected file name when no file is selected', () => {
    render(<FileUpload {...defaultProps} />);

    expect(screen.queryByTestId('selected-file')).not.toBeInTheDocument();
  });

  it('calls onAnalyze when the analyze button is clicked', () => {
    const onAnalyze = vi.fn();
    const file = new File([''], 'test.csv', { type: 'text/csv' });
    render(<FileUpload {...defaultProps} selectedFile={file} onAnalyze={onAnalyze} />);

    fireEvent.click(screen.getByTestId('analyze-button'));

    expect(onAnalyze).toHaveBeenCalledOnce();
  });

  it('calls onFileSelected with the chosen file', () => {
    const onFileSelected = vi.fn();
    render(<FileUpload {...defaultProps} onFileSelected={onFileSelected} />);

    const file = new File([''], 'test.csv', { type: 'text/csv' });
    const input = screen.getByTestId('file-input');
    Object.defineProperty(input, 'files', { value: [file], configurable: true });
    fireEvent.change(input);

    expect(onFileSelected).toHaveBeenCalledWith(file);
  });
});
