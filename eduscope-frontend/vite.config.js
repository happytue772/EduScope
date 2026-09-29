import {
  defineConfig
} from 'vite'

import react
  from '@vitejs/plugin-react'


/**
 * EduScope Vite 개발 서버 설정.
 *
 * Frontend:
 * React / Vite
 * http://localhost:5173
 *
 * Backend:
 * Spring Boot
 * http://localhost:8080
 *
 * 중요:
 * React의 /login Route와
 * Spring Security 로그인 처리 URL이
 * 충돌하지 않도록 /login은 Proxy하지 않는다.
 */
export default defineConfig({

  plugins: [
    react()
  ],


  server: {

    port: 5173,


    proxy: {

      /**
       * Spring Boot REST API.
       *
       * 예:
       * /api/auth/csrf
       * /api/auth/login
       * /api/auth/signup
       * /api/auth/me
       * /api/courses
       * /api/admin/users
       */
      '/api': {

        target:
          'http://localhost:8080',

        changeOrigin:
          true

      },
      /**
       * Spring Boot Actuator.
       *
       * ADMIN 시스템 상태 화면에서
       * 이후 사용한다.
       */
      '/actuator': {

        target:
          'http://localhost:8080',

        changeOrigin:
          true

      }

    }

  }

})