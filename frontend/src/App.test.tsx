/// <reference types="@testing-library/jest-dom" />
import { render, screen } from '@testing-library/react';
import App from './App';
import { describe, it, expect } from 'vitest';

describe('App', () => {
  it('renders heading', () => {
    render(<App />);
    expect(screen.getByText(/Fake Company Detector/i)).toBeInTheDocument();
  });
});
