# BIBLIA DE APLICACIÓN: VVC-EMERGENCY-SIGNAL

**ECOSISTEMA:** THE VANCARTIER AUTHORITY  
**SUITE:** VVC EDGE CONTROL  
**APK:** vvc-emergency-signal-debug.apk

---

## [ ] Portada

*   **Nombre:** VVC-EMERGENCY-SIGNAL
*   **Versión del documento:** 1.0.0
*   **Estado:** MVP (Minimum Viable Product)
*   **Fecha de actualización:** 26 de Junio, 2026

---

## [ ] Resumen ejecutivo

*   **Qué es:** Una herramienta de seguridad crítica diseñada para la activación inmediata de protocolos de auxilio.
*   **Para qué sirve:** Emite señales visuales y táctiles en código Morse (S.O.S.), obtiene la ubicación GPS exacta y envía alertas automáticas vía SMS a un contacto de confianza.
*   **Público objetivo:** Usuarios que requieren un mecanismo de alerta discreto o de alta visibilidad en situaciones de riesgo personal.
*   **Valor diferencial:** Activación dual (física y por voz mediante "Código Alfa"), integración con servicios de accesibilidad para escucha en segundo plano y funcionamiento offline resiliente.

---

## [ ] Filosofía y principios

*   **Objetivos:** Proporcionar una respuesta de emergencia en menos de 2 segundos tras la activación.
*   **Lo que la aplicación hará:** Emitir destellos de linterna y vibraciones en patrón S.O.S., obtener coordenadas GPS y despachar SMS con enlace a Google Maps.
*   **Lo que deliberadamente no hará:** No almacenará datos en la nube, no compartirá información con terceros fuera del contacto configurado y no requerirá conexión constante a internet para las señales físicas.

---

## [ ] Problema que resuelve
La dificultad de pedir ayuda en situaciones de pánico o restricción de movimiento donde el uso convencional del teléfono es imposible o demasiado lento.

---

## [ ] Casos de uso
1.  **Extravío en zonas remotas:** Activación de señal visual S.O.S. para rescate.
2.  **Riesgo inminente:** Envío de ubicación discreta mediante comando de voz.
3.  **Accidente:** Activación rápida para alertar a un familiar con posición exacta.

---

## [ ] Historias de usuario
*   "Como usuario, quiero decir una palabra clave para que mi teléfono pida ayuda sin que yo tenga que tocarlo."
*   "Como usuario, quiero que mi ubicación se envíe automáticamente para que me encuentren rápido."

---

## [ ] Alcance del MVP
*   Interfaz de configuración de contacto y palabra clave.
*   Bucle Morse (Luz/Vibración).
*   Activación por voz (Servicio de Accesibilidad).
*   Envío de SMS con link de Google Maps.

---

## [ ] Roadmap

*   **MVP:** Funcionalidades básicas de alerta y ubicación (Actual).
*   **V1:** Cifrado AES de preferencias y ofuscación de código.
*   **V2:** Grabación de audio ambiental automática al activar S.O.S.
*   **Futuro:** Integración con Vivian para gestión inteligente de la emergencia.

---

## [ ] Arquitectura general
Basada en Servicios de Android (Foreground Service para Morse y Accessibility Service para Voz) con persistencia local mediante SharedPreferences.

---

## [ ] Componentes internos
*   **MainActivity:** Gestión de UI y permisos.
*   **EmergencyService:** Lógica de hardware (Morse) y SMS.
*   **VoiceAccessibilityService:** Reconocimiento de voz persistente.

---

## [ ] Flujo de funcionamiento
1.  Detección (Botón UI o Comando de Voz).
2.  Inicio de `EmergencyService`.
3.  Obtención de última ubicación conocida.
4.  Envío de SMS.
5.  Inicio de bucle infinito Morse (Luz + Vibración).

---

## [ ] Pantallas de la aplicación
*   **Pantalla Única (Cyberpunk UI):** Contenedor de telemetría, campos de configuración dinámica y botón central de activación S.O.S.

---

## [ ] Funcionalidades detalladas
*   **Morse S.O.S.:** Patrón síncrono de 200ms (punto) y 600ms (raya).
*   **Reconocimiento Offline:** Uso de `EXTRA_PREFER_OFFLINE` para maximizar la disponibilidad.

---

## [ ] Módulos
*   `ConfigModule`: Gestión de SharedPreferences.
*   `SignalModule`: Control de Camera2 API y Vibrator.
*   `LocationModule`: Integración con LocationManager.

---

## [ ] Permisos Android

| Permiso | Motivo | Impacto |
| :--- | :--- | :--- |
| `CAMERA` | Control de la linterna para señal visual. | Bajo (Solo linterna). |
| `VIBRATE` | Señal táctil S.O.S. | Nulo. |
| `ACCESS_FINE_LOCATION` | Obtención de coordenadas exactas. | Alto (Privacidad). |
| `SEND_SMS` | Envío automático de alerta. | Medio (Costo SMS). |
| `RECORD_AUDIO` | Escucha de la palabra clave. | Alto (Privacidad). |
| `BIND_ACCESSIBILITY_SERVICE` | Escucha en segundo plano persistente. | Muy Alto. |

---

## [ ] Dependencias
*   `androidx.core:core-ktx`: Extensiones Kotlin.
*   `androidx.appcompat`: Compatibilidad de UI.
*   `com.google.android.material`: Componentes de diseño.

---

## [ ] Integración con la Suite VVC
*   **Comparte:** Configuración de identidad visual.
*   **Recibe:** Parámetros de seguridad global.
*   **Envía:** Estado de alerta a Vivian.

---

## [ ] Integración con Vivian
*   **Comandos:** "Vivian, activa protocolo S.O.S."
*   **Automatizaciones:** Envío de reporte de incidente tras la desactivación.

---

## [ ] Base de datos
*   **Entidades:** UserPreferences.
*   **Persistencia:** SharedPreferences (`VVC_PREFS`).

---

## [ ] Almacenamiento
*   **Archivos:** `ic_launcher.png` (Iconografía).
*   **Caché:** Datos temporales de ubicación.

---

## [ ] Seguridad
*   **Datos sensibles:** Teléfono de contacto y palabra clave.
*   **Cifrado:** (Planificado V1) AES-256 para SharedPreferences.
*   **Riesgos:** Revocación de permisos por el sistema.

---

## [ ] Rendimiento
*   **RAM:** < 50MB en ejecución.
*   **CPU:** Uso mínimo (optimizado con hilos secundarios).
*   **Batería:** Consumo alto solo durante la fase activa de S.O.S. (linterna).

---

## [ ] Compatibilidad
*   **Versiones:** Android 8.0 (API 26) hasta Android 14 (API 34).
*   **Restricciones:** Requiere hardware de cámara con flash.

---

## [ ] Configuración
*   `etEmergencyContact`: Número destino.
*   `etVoiceCodeWord`: Palabra clave personalizada.

---

## [ ] Logs y diagnóstico
*   Uso de `Log.d` para trazabilidad de eventos de voz y errores de GPS.

---

## [ ] Manejo de errores
*   Reinicio automático del `SpeechRecognizer` tras timeouts.
*   Fallback de `GPS_PROVIDER` a `NETWORK_PROVIDER`.

---

## [ ] Exportación e importación
*   (No disponible en MVP).

---

## [ ] Accesibilidad
*   Uso de `AccessibilityService` como núcleo de activación por voz.

---

## [ ] Internacionalización
*   Soporte inicial para Español (Latinoamérica).

---

## [ ] Riesgos técnicos
*   Matado de procesos por capas de optimización de batería (OEMs).
*   Falta de señal GPS en interiores.

---

## [ ] Limitaciones conocidas
*   El reconocimiento de voz puede verse afectado por ruido ambiental extremo.

---

## [ ] Ideas futuras
*   Integración con botones de volumen para activación física discreta.

---

## [ ] Backlog priorizado
1.  Implementar cifrado AES.
2.  Añadir grabación de audio.
3.  Optimización de consumo de batería en reposo.

---

## [ ] Glosario
*   **S.O.S.:** Save Our Souls (Señal de socorro).
*   **Vivian:** Asistente inteligente de la Suite VVC.

---

## [ ] Referencias
*   [Android Developer Documentation](https://developer.android.com/)
*   [Google Maps API Docs](https://developers.google.com/maps)
