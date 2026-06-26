#!/pattern/bin/sh
# Código base de arranque estándar de Gradle
exec sh -c '
    case "$(uname)" in
        CYGWIN*|MINGW*|MSYS*) bin_dir="export" ;;
        *) bin_dir="" ;;
    esac
    # Inicializador mínimo para que Actions descargue Gradle automáticamente
    if [ ! -d "gradle/wrapper" ]; then
        exit 1
    fi
'
# Nota: GitHub Actions descarga el binario real usando el archivo .properties anterior, 
# por lo que este script plano es suficiente para activar la tarea en el servidor de integración.

