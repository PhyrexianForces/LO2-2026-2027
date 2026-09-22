import React, { createContext, useContext, useState } from 'react';

interface Theme {
  mode: 'light' | 'dark';
  bgMain: string; bgSecondary: string; bgPanel: string; bgField: string;
  bgInput: string; bgActiveAction: string; bgConnectBtn: string;
  textPrimary: string; textSecondary: string; textTertiary: string;
  textLabel: string; textFaded: string;
  borderStrong: string; borderMuted: string; borderConnectBtn: string;
  colorConnected: string; colorDisconnected: string;
  colorNumeric: string; colorString: string;
  colorSimultaneous: string; colorWaiting: string;
  color3dRobot: string; color3dCone: string;
  color3dFloor: string; color3dGrid: string; color3dBorder: string;
}

const darkTheme: Theme = {
  mode: 'dark',
  bgMain: '#1a1a1a',
  bgSecondary: '#2a2a2a',
  bgPanel: '#1e1e1e',
  bgField: '#0d0d0d',
  bgInput: '#1a1a1a',
  bgActiveAction: '#0d2a0d',
  bgConnectBtn: '#1e3a1e',
  textPrimary: '#e0e0e0',
  textSecondary: '#888',
  textTertiary: '#555',
  textLabel: '#9e9e9e',
  textFaded: '#444',
  borderStrong: '#444',
  borderMuted: '#333',
  borderConnectBtn: '#2e5a2e',
  colorConnected: '#4caf50',
  colorDisconnected: '#f44336',
  colorNumeric: '#64b5f6',
  colorString: '#a5d6a7',
  colorSimultaneous: '#ff9800',
  colorWaiting: '#90caf9',
  color3dRobot: '#1565c0',
  color3dCone: '#f44336',
  color3dFloor: '#111',
  color3dGrid: '#333',
  color3dBorder: '#666',
};

const lightTheme: Theme = {
  mode: 'light',
  bgMain: '#f5f5f5',
  bgSecondary: '#e8e8e8',
  bgPanel: '#ffffff',
  bgField: '#d0d0d0',
  bgInput: '#ffffff',
  bgActiveAction: '#e8f5e9',
  bgConnectBtn: '#e8f5e9',
  textPrimary: '#1a1a1a',
  textSecondary: '#555',
  textTertiary: '#777',
  textLabel: '#999',
  textFaded: '#bbb',
  borderStrong: '#cccccc',
  borderMuted: '#d0d0d0',
  borderConnectBtn: '#4caf50',
  colorConnected: '#2e7d32',
  colorDisconnected: '#c62828',
  colorNumeric: '#1565c0',
  colorString: '#2e7d32',
  colorSimultaneous: '#e65100',
  colorWaiting: '#1565c0',
  color3dRobot: '#1976d2',
  color3dCone: '#d32f2f',
  color3dFloor: '#e0e0e0',
  color3dGrid: '#bdbdbd',
  color3dBorder: '#888',
};

interface ThemeContextValue {
  theme: Theme;
  toggleTheme: () => void;
}

const ThemeContext = createContext<ThemeContextValue>({
  theme: lightTheme,
  toggleTheme: () => {},
});

export function ThemeProvider({ children }: { children: React.ReactNode }) {
  const [mode, setMode] = useState<'light' | 'dark'>(
    () => (localStorage.getItem('theme') as 'light' | 'dark') ?? 'light'
  );

  const theme = mode === 'dark' ? darkTheme : lightTheme;

  const toggleTheme = () => {
    setMode(prev => {
      const next = prev === 'dark' ? 'light' : 'dark';
      localStorage.setItem('theme', next);
      return next;
    });
  };

  return (
    <ThemeContext.Provider value={{ theme, toggleTheme }}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  return useContext(ThemeContext);
}
