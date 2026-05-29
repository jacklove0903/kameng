/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        bg: {
          primary: '#0a0a0f',
          secondary: '#12121a',
          tertiary: '#1a1a26',
          elevated: '#22223a',
        },
        border: {
          DEFAULT: '#2a2a3e',
          hover: '#3a3a5e',
        },
        accent: {
          DEFAULT: '#6c5ce7',
          hover: '#7d6ff0',
        },
        success: '#00b894',
        warning: '#fdcb6e',
        danger: '#e17055',
      },
    },
  },
  plugins: [],
}
