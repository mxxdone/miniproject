import { createApp } from 'vue'
import { createPinia } from 'pinia' // pinia 라이브러리에서 createPinia를 직접 가져옵니다.
import App from './App.vue'
import router from './router'
import vuetify from './plugins/vuetify'

const app = createApp(App)

app.use(createPinia()) // createPinia()를 호출하여 Pinia 인스턴스를 생성하고 앱에 등록합니다.
app.use(router)
app.use(vuetify)

app.mount('#app')
