/// <reference types="@testing-library/jest-dom" />
import { render, screen } from '@testing-library/react';
import App from './App';
import { describe, it, expect } from 'vitest';

describe('App Component', () => {
  it('renders correctly', () => {
    render(<App />);
    expect(screen.getAllByText(/Detectify/i).length).toBeGreaterThan(0);
  });
});
