import type { GlobalThemeOverrides } from 'naive-ui'

const primary = '#0f766e'
const primaryHover = '#0d9488'
const primaryPressed = '#115e59'

export const lightThemeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: primary,
    primaryColorHover: primaryHover,
    primaryColorPressed: primaryPressed,
    primaryColorSuppl: '#14b8a6',
    borderRadius: '8px',
    borderRadiusSmall: '6px',
    fontFamily:
      "Inter, system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif",
    fontWeightStrong: '600',
  },
  Card: {
    borderRadius: '12px',
    paddingMedium: '20px 24px',
    titleFontSizeMedium: '16px',
  },
  Button: {
    borderRadiusMedium: '6px',
    borderRadiusSmall: '6px',
  },
  Menu: {
    borderRadius: '8px',
    itemHeight: '40px',
  },
  DataTable: {
    borderRadius: '8px',
    thPaddingMedium: '12px 16px',
    tdPaddingMedium: '12px 16px',
  },
  Tag: {
    borderRadius: '6px',
  },
  Input: {
    borderRadius: '6px',
  },
}

export const darkThemeOverrides: GlobalThemeOverrides = {
  ...lightThemeOverrides,
  common: {
    ...lightThemeOverrides.common,
    primaryColor: '#14b8a6',
    primaryColorHover: '#2dd4bf',
    primaryColorPressed: '#0d9488',
    primaryColorSuppl: '#5eead4',
  },
}
