# VVC-EMERGENCY-SIGNAL-VVC-EMERGENCY-SIGNAL
1. Especificación del Repositorio
Para garantizar el estándar técnico de la suite VVC, el repositorio debe configurarse exactamente con los siguientes parámetros de inicialización:
Nombre del Repositorio: vvc-emergency-signal
Visibilidad: Privado (Private)
Lenguaje Mandatorio: Kotlin
SDK Mínimo: API 26 (Android 8.0)
SDK Objetivo: API 34 (Android 14)
Licencia: MIT License
Gitignore: Android
2. Estructura del Proyecto y Orden de Archivos
Manus debe codificar el módulo respetando de forma estricta el siguiente orden de archivos para prevenir fallos de dependencias cruzadas durante la compilación:
app/src/main/AndroidManifest.xml -> Declaración estricta de permisos de hardware y tipo de servicio en primer plano.
app/src/main/res/layout/activity_main.xml -> Interfaz gráfica limpia de alto contraste optimizada para entornos críticos.
app/src/main/java/com/vvc/emergencysignal/EmergencyService.kt -> Orquestador síncrono en hilo dedicado para el bucle de Flash LED y vibración háptica.
app/src/main/java/com/vvc/emergencysignal/MainActivity.kt -> Pasarela de interfaz, verificación de estados y control de ciclo de vida de permisos.
3. Tiempos Oficiales del Código Morse (S.O.S.)
El bucle de hardware utiliza intervalos milimétricos basados en el estándar internacional de telegrafía para asegurar la visibilidad del patrón ... --- ...:
Punto (.): 200 ms (Duración de encendido)
Raya (-): 600 ms (Duración de encendido)
Espacio entre elementos de la misma letra: 200 ms (Apagado)
Espacio entre letras: 400 ms (Apagado)
Espacio entre bucles completos (S.O.S. <-> S.O.S.): 2000 ms (Apagado de seguridad)
4. Restricciones de Ejecución (Anti-Alucinación)
REGLA 1: Queda estrictamente prohibida la adición de animaciones, layouts intermedios, bibliotecas de terceros o dependencias de UI no especificadas en la orden base de Manus.
REGLA 2: Prohibido alterar el patrón del temporizador del código Morse.
REGLA 3: Todo el hardware (cámara y vibración) debe desactivarse inmediatamente al destruir el servicio para evitar bloqueos del kernel de Android.
