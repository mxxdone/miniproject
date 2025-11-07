// frontend/src/plugins/vuetify.js

// Styles
import 'vuetify/styles'
import '@mdi/font/css/materialdesignicons.css'

// Vuetify
import { createVuetify } from 'vuetify'

const anyangLight = {
  dark: false,
  colors: {
    primary: '#4f1b87',
    'on-primary': '#ffffff',
    secondary: '#cda55f',
    'on-secondary': '#1f0149',
    background: '#ffffff',
    surface: '#ffffff',
    'on-surface': '#1f0149',
    outline: '#E7E5EA',
    success: '#10B981',
    warning: '#F59E0B',
    info: '#3B82F6',
    error: '#EF4444',
  },
  variables: {
    'rounded-sm': '8px',
    'rounded-md': '12px',
    'rounded-lg': '16px',
    'shadow-sm': '0 1px 2px rgba(0,0,0,.05)',
    'shadow-md': '0 6px 20px rgba(0,0,0,.08)',
  },
};

const anyangDark = {
  dark: true,
  colors: {
    primary: '#4f1b87',
    'on-primary': '#ffffff',
    secondary: '#cda55f',
    'on-secondary': '#230a3a',
    background: '#1E1E1E',
    surface: '#272727',
    'on-surface': '#EDEAF2',
    outline: '#3c2757',
    success: '#22C55E',
    warning: '#FBBF24',
    info: '#60A5FA',
    error: '#F87171',
    'input-bg': '#42424203',
  },
  variables: {
    'rounded-sm': '8px',
    'rounded-md': '12px',
    'rounded-lg': '16px',
    'shadow-sm': '0 1px 2px rgba(0,0,0,.35)',
    'shadow-md': '0 8px 26px rgba(0,0,0,.45)',
  },
};

export default createVuetify({
  theme: {
    defaultTheme: 'anyangLight',
    themes: { anyangLight, anyangDark },
  },
  defaults: {
    VAppBar: { color: 'primary', flat: true, height: 64, class: 'text-white' },
    VBtn: { color: 'primary', rounded: 'md', class: 'text-none' },
    VCard: { rounded: 'lg', elevation: 0, class: 'bordered' },
    VTextField: { variant: 'solo', density: 'comfortable'},
    VTextarea: { variant: 'solo', density: 'comfortable'},
    VChip: { color: 'secondary', variant: 'tonal' },
    VList: { density: 'comfortable', class: 'bordered' },
    VPagination: { rounded: 'md' },
    VAlert: { variant: 'tonal' },
  },
});
