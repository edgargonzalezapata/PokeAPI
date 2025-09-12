# PokePI 🔥

Una aplicación Android moderna para explorar el mundo Pokémon con autenticación GitHub OAuth integrada.

## 🚀 Características

- ✅ **Autenticación GitHub OAuth** - Inicia sesión segura con tu cuenta de GitHub
- 🎨 **Material Design 3** - Interfaz moderna siguiendo las últimas guías de diseño
- 🏗️ **Arquitectura MVVM** - Patrón arquitectónico robusto y escalable
- 📱 **Jetpack Compose** - UI declarativa y reactiva
- 🔒 **Almacenamiento Seguro** - Tokens guardados con DataStore
- 🌐 **Navegador Externo** - Autenticación OAuth usando el navegador del sistema

## 📱 Capturas de Pantalla

### Pantalla de Bienvenida
La aplicación te recibe con una interfaz limpia y moderna, permitiendo iniciar sesión con GitHub de forma segura.

### Pantalla Principal
Después de autenticarte, accedes a tu perfil personalizado con información de GitHub y un preview de las funcionalidades Pokémon por venir.

## 🛠️ Tecnologías Utilizadas

### Frontend
- **Jetpack Compose** - UI toolkit moderno de Android
- **Material 3** - Sistema de diseño de Google
- **Compose Navigation** - Navegación declarativa
- **Coil** - Carga de imágenes asíncrona

### Networking & API
- **Retrofit 2** - Cliente HTTP type-safe
- **OkHttp 3** - Cliente HTTP eficiente
- **Gson** - Serialización/deserialización JSON
- **GitHub API** - Integración con GitHub OAuth

### Arquitectura & Estado
- **MVVM Pattern** - Model-View-ViewModel
- **Repository Pattern** - Abstracción de datos
- **StateFlow** - Manejo reactivo de estado
- **Kotlin Coroutines** - Programación asíncrona

### Almacenamiento
- **DataStore** - Almacenamiento de preferencias moderno
- **SharedPreferences** - Para configuraciones simples

## 🏗️ Arquitectura

```
app/
├── data/
│   ├── api/           # Servicios de API (GitHub)
│   ├── model/         # Modelos de datos
│   ├── network/       # Configuración de red
│   ├── repository/    # Repositorios de datos
│   └── storage/       # Almacenamiento local
├── presentation/
│   ├── auth/          # Autenticación y perfiles
│   └── home/          # Pantallas principales
└── ui/theme/          # Tema y estilos
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

## 🔐 Flujo de Autenticación

1. **Usuario inicia la app** → Pantalla de bienvenida
2. **Toca "Continuar con GitHub"** → Se abre el navegador
3. **Autentica en GitHub** → GitHub redirige a la app
4. **App recibe el código** → Intercambia código por token
5. **Obtiene perfil de usuario** → Muestra pantalla principal

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

## 🚧 Roadmap

- [ ] Integración con PokéAPI
- [ ] Sistema de favoritos
- [ ] Búsqueda de Pokémon
- [ ] Información detallada de Pokémon
- [ ] Colección personal
- [ ] Modo offline
- [ ] Notificaciones push

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