module.exports = {
  content: [
    './index.html',
    './src/**/*.{vue,js,ts,jsx,tsx}'
  ],
  theme: {
    extend: {
      colors: {
        primary: '#6366F1',
        success: '#10B981'
      },
      borderRadius: {
        sm: '6px',
        md: '10px',
        lg: '14px'
      }
    }
  },
  plugins: []
}
