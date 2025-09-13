# PokePI 🔥

Una aplicación Android moderna para explorar el mundo Pokémon con sistema de autenticación dual (GitHub OAuth + biométrica local) y favoritos personalizados por usuario.

## 🚀 Características

### 🔐 Autenticación Dual
- ✅ **GitHub OAuth** - Inicia sesión segura con tu cuenta de GitHub
- 🔒 **Autenticación Local** - Registro y login con credenciales locales
- 👆 **Biométrica** - Acceso rápido con huella dactilar o reconocimiento facial
- 🔄 **Cambio de Usuario** - Alterna entre diferentes sesiones fácilmente

### 🎮 Funcionalidades Pokémon
- 🔍 **Exploración Completa** - Feed infinito con todos los Pokémon de la API
- ⭐ **Favoritos por Usuario** - Sistema de favoritos específico para cada usuario
- 🔎 **Búsqueda Avanzada** - Por nombre y tipo de Pokémon
- 📊 **Estadísticas** - Tracking de Pokémon vistos, favoritos y tiempo de uso
- 🎨 **Interfaz Moderna** - Material Design 3 con tema personalizado Pokémon

### 💫 Experiencia de Usuario
- 🚀 **Carga Rápida** - Paginación eficiente con Paging 3
- 💾 **Cache Inteligente** - Room database para funcionamiento offline
- 🎯 **Navegación Fluida** - Bottom navigation entre secciones
- ⚡ **Indicadores de Carga** - Pokeball animada como loading indicator

## 📱 Pantallas de la App

### 🔐 Autenticación
- **Login Screen** - Selección entre GitHub OAuth y autenticación local
- **Registro Local** - Creación de cuenta con opción de activar biométrica
- **Autenticación Biométrica** - Login rápido con huella o reconocimiento facial

### 🏠 Feed Principal  
- **Lista Infinita** - Scroll infinito con todos los Pokémon
- **Búsqueda Inteligente** - Filtros por nombre y tipo
- **Marcado de Favoritos** - Corazón interactivo por usuario
- **Estadísticas Personales** - Counter de vistos, favoritos y tiempo

### ⭐ Favoritos
- **Lista Personal** - Solo los Pokémon marcados por el usuario actual
- **Gestión Rápida** - Toggle de favoritos desde la misma pantalla
- **Separación por Usuario** - Favoritos independientes entre cuentas

### 👤 Perfil
- **Información de Usuario** - Datos de GitHub o usuario local
- **Gestión de Sesión** - Logout y cambio de cuenta
- **Configuración Biométrica** - Activar/desactivar autenticación biométrica

## 🛠️ Tecnologías Utilizadas

### 🎨 Frontend & UI
- **Jetpack Compose** - UI toolkit declarativo moderno
- **Material Design 3** - Sistema de diseño de Google
- **Compose Navigation** - Navegación declarativa entre pantallas
- **Coil** - Carga de imágenes asíncrona y cache
- **Compose Animations** - Animaciones fluidas de la Pokeball

### 🌐 Networking & APIs
- **Retrofit 2** - Cliente HTTP type-safe para REST APIs
- **OkHttp 3** - Cliente HTTP eficiente con interceptors
- **Gson** - Serialización/deserialización JSON
- **PokéAPI** - API REST completa de Pokémon
- **GitHub API** - Integración OAuth y datos de usuario

### 🏗️ Arquitectura & Patrones
- **MVVM Pattern** - Model-View-ViewModel con separation of concerns
- **Repository Pattern** - Abstracción de fuentes de datos
- **Paging 3** - Paginación eficiente de listas grandes
- **StateFlow/LiveData** - Manejo reactivo de estado UI
- **Kotlin Coroutines** - Programación asíncrona y concurrencia
- **Hilt/Dagger** - Inyección de dependencias

### 💾 Base de Datos & Almacenamiento
- **Room Database** - SQLite object mapping con TypeConverters
- **Migración Automática** - Schema evolution transparente
- **DataStore** - Almacenamiento de preferencias type-safe
- **SharedPreferences** - Configuraciones de autenticación
- **Biometric Storage** - Almacenamiento seguro con encriptación

### 🔐 Seguridad & Autenticación  
- **BiometricPrompt** - API nativa de autenticación biométrica
- **OAuth 2.0** - Flow completo con GitHub
- **Token Management** - Refresh tokens y expiración
- **Local Authentication** - Hash seguro de contraseñas
- **Deep Linking** - Manejo seguro de redirects OAuth

## 🏗️ Arquitectura del Proyecto

```
app/
├── data/
│   ├── api/           # Servicios REST (PokéAPI, GitHub API)
│   ├── database/      # Room DAOs y base de datos
│   │   ├── PokemonDao.kt
│   │   ├── UserFavoriteDao.kt
│   │   ├── UserStatsDao.kt
│   │   └── PokeDatabase.kt
│   ├── model/         # Entidades y modelos de datos
│   │   ├── Pokemon.kt
│   │   ├── UserFavorite.kt
│   │   └── UserStats.kt
│   ├── paging/        # PagingSources para listas infinitas
│   ├── repository/    # Repositorios con abstracción de datos
│   └── network/       # Configuración HTTP y interceptors
├── presentation/
│   ├── auth/          # Sistema de autenticación dual
│   │   ├── LoginScreen.kt
│   │   ├── LocalRegisterScreen.kt
│   │   └── ProfileScreen.kt
│   ├── feed/          # Feed principal de Pokémon
│   │   ├── FeedScreen.kt
│   │   └── FeedViewModel.kt
│   ├── favorites/     # Gestión de favoritos por usuario
│   │   ├── FavoritesScreen.kt
│   │   └── FavoritesViewModel.kt
│   └── components/    # Componentes reutilizables
├── di/                # Inyección de dependencias (Hilt)
└── ui/theme/          # Tema Material Design 3
```

### 🔄 Flujo de Datos

```
UI Layer (Compose) 
    ↕️
ViewModel (StateFlow)
    ↕️
Repository (Business Logic)
    ↕️
┌─ Remote API (Retrofit) ─┐    ┌─ Local DB (Room) ─┐
│  • PokéAPI             │ ←→ │  • Pokemon Cache   │
│  • GitHub API          │    │  • User Favorites  │
└─────────────────────────┘    │  • User Stats     │
                               └───────────────────┘
```

## ⚙️ Configuración del Proyecto

### Prerequisitos
- Android Studio Hedgehog | 2023.1.1 o superior
- JDK 11 o superior
- Android SDK 24 (mínimo) - 36 (target)
- Kotlin 1.9.0+

### GitHub OAuth Setup

1. **Crea una GitHub OAuth App:**
   - Ve a GitHub.com → Settings → Developer settings → OAuth Apps
   - Haz clic en "New OAuth App"
   - Completa los datos:
     - **Application name**: `PokePI`
     - **Homepage URL**: `http://localhost`
     - **Authorization callback URL**: `pokepi://oauth/callback`

2. **Configura las credenciales:**
   - Las credenciales ya están configuradas en `GitHubApiService.kt`
   - Para producción, considera usar BuildConfig para mayor seguridad

### Instalación

1. **Clona el repositorio:**
```bash
git clone https://github.com/edgargonzalezapata/PokeAPI.git
cd PokeAPI
```

2. **Abre el proyecto:**
   - Abre Android Studio
   - File → Open → Selecciona la carpeta del proyecto

3. **Sincroniza las dependencias:**
   - Android Studio sincronizará automáticamente
   - O ejecuta: `./gradlew build`

4. **Ejecuta la aplicación:**
   - Conecta un dispositivo Android o inicia un emulador
   - Haz clic en "Run" o presiona `Ctrl+R`

## 🔐 Flujos de Autenticación

### GitHub OAuth Flow
1. **Usuario selecciona GitHub** → Se abre navegador externo
2. **Autenticación en GitHub** → Usuario autoriza la aplicación  
3. **Redirect con código** → GitHub redirige con authorization code
4. **Exchange de tokens** → App intercambia código por access token
5. **Obtención de perfil** → Fetch de datos del usuario de GitHub
6. **Sesión establecida** → Acceso completo a la aplicación

### Autenticación Local + Biométrica
1. **Registro inicial** → Usuario crea cuenta local con email/password
2. **Configuración biométrica** → Opcionalmente activa huella/face ID
3. **Login posterior** → Puede usar credenciales o biométrica
4. **Sesión independiente** → Favoritos y datos separados de GitHub

### Sistema Dual
- **Cambio de usuario** → Logout automático y limpieza de estado
- **Persistencia separada** → Favoritos únicos por tipo de autenticación
- **Seguridad** → Tokens encriptados y credenciales hasheadas

## 🧪 Testing

```bash
# Ejecutar tests unitarios
./gradlew test

# Ejecutar tests instrumentados
./gradlew connectedAndroidTest
```

## 📦 Build

```bash
# Build debug
./gradlew assembleDebug

# Build release
./gradlew assembleRelease
```



## 🤝 Contribuir

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📝 Licencia

Este proyecto está bajo la Licencia MIT. Ver `LICENSE` para más detalles.

## 👨‍💻 Autor

**Edgar González** - [@edgargonzalezapata](https://github.com/edgargonzalezapata)

## 🙏 Reconocimientos

- [PokéAPI](https://pokeapi.co/) - La RESTful Pokémon API
- [GitHub API](https://docs.github.com/en/rest) - Para la autenticación OAuth
- [Material Design](https://material.io/) - Sistema de diseño
- [Android Developers](https://developer.android.com/) - Documentación y guías

---

**¿Te gusta el proyecto? ¡Dale una ⭐ al repositorio!**