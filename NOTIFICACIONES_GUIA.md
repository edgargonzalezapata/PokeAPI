# 🔔 Guía de Notificaciones - PokePI

## ✅ Sistema de Notificaciones Implementado

### 📍 **¿Dónde encontrar las notificaciones?**

1. **Abrir la aplicación PokePI**
2. **Ir a la pestaña "Perfil"** (ícono de persona en la barra inferior)
3. **Buscar la tarjeta "Configurar Notificaciones"** 
   - Aparece después de la información del usuario
   - Tiene un ícono de campana 🔔
   - Botón "Configurar" a la derecha

### 🎛️ **Configuraciones Disponibles**

Al hacer clic en "Configurar", accedes a:

- ✅ **Notificaciones Generales** - Activar/desactivar todas las notificaciones
- 🌟 **Pokémon del Día** - Notificaciones diarias con un Pokémon destacado
- ⭐ **Actualizaciones de Favoritos** - Notificaciones sobre tus Pokémon favoritos
- 🚀 **Actualizaciones de App** - Nuevas funciones y actualizaciones
- 🧪 **Botón de Prueba** - Enviar notificación de prueba

### 🔧 **Funcionalidades Técnicas**

- **Firebase Cloud Messaging** para notificaciones push
- **WorkManager** para notificaciones locales diarias
- **DataStore** para guardar preferencias
- **Canales de notificación** para Android 8.0+
- **Optimización de batería** y conexión de red

### 🚀 **Para probar las notificaciones:**

1. Ve a Perfil → Configurar Notificaciones
2. Asegúrate de que las notificaciones estén activadas
3. Haz clic en "Enviar Notificación de Prueba"
4. ¡Deberías recibir una notificación inmediatamente!

### 📱 **Si no ves los cambios:**

1. **Fuerza el cierre** de la aplicación
2. **Reinstala** la aplicación desde Android Studio
3. **Verifica** que tienes los permisos de notificación activados
4. **Prueba** en un dispositivo físico si estás usando emulador

---
*Sistema implementado con Firebase FCM, WorkManager y Material Design 3*