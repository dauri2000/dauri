# Preferencias de entrega del proyecto

El usuario pidió guardar GitHub como opción para futuras descargas de este proyecto.

- Entregar los archivos descargables mediante el repositorio `dauri2000/dauri`, usando una rama de entrega y enlaces HTTPS a archivos realmente subidos.
- Los enlaces internos de archivos (`/workspace/...` y `sandbox:...`) no aparecen como descargas en esta interfaz. No volver a presentarlos como enlaces de descarga disponibles.
- Comprobar la subida y, cuando el acceso lo permita, descargar de nuevo el archivo y verificar su hash antes de dar el enlace.
- Para archivos grandes, preparar un paquete o actualizador pequeño que pueda alojarse en GitHub. Verificar el resultado completo y conservar los archivos originales.
- Si se entrega un actualizador, explicar los pasos concretos para ejecutarlo y distinguir las correcciones probadas de los fallos todavía pendientes.

Cada tarea de cloud ya tiene un entorno aislado. Usar los checkouts existentes; no crear worktrees de Git salvo petición expresa del usuario.
