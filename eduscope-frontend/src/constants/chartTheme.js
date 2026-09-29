/**
 * EduScope Premium 공통 차트 디자인.
 *
 * 모든 Recharts 화면에서 같은 색상 체계를 사용한다.
 */
export const CHART_COLORS = {

  primary: '#4FA8FF',

  secondary: '#66D7FF',

  deepBlue: '#7B8CFF',

  success: '#43D9A3',

  warning: '#FFC261',

  danger: '#FF7386',

  muted: '#90A9BC',

  grid: 'rgba(158, 183, 202, 0.18)',

  axis: '#9EB7CA',

  tooltipBorder: 'rgba(103, 212, 255, 0.28)',

  tooltipBackground: '#0B2034'
}


/**
 * 모든 차트 Tooltip 공통 스타일.
 */
export const TOOLTIP_STYLE = {

  backgroundColor:
    CHART_COLORS.tooltipBackground,

  border:
    '1px solid '
    + CHART_COLORS.tooltipBorder,

  borderRadius: '14px',

  color: '#F4F9FF',

  boxShadow:
    '0 18px 44px rgba(0, 0, 0, 0.32)'
}