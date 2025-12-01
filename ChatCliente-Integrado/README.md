# ChatCliente-Integrado

Aplicación de chat cliente con interfaz gráfica completa integrada con el backend.

## Estructura del Proyecto

```
ChatCliente-Integrado/
├── src/
│   ├── chatcliente/
│   │   ├── ChatCliente.java      # Clase principal (main)
│   │   ├── LoginUI.java          # Interfaz de login
│   │   ├── ChatUI.java           # Interfaz principal del chat
│   │   └── Cliente.java          # Cliente de red (backend)
│   ├── common/
│   │   └── Peticion.java         # Clase para comunicación con servidor
│   └── models/
│       ├── Usuario.java          # Modelo de usuario
│       └── Mensaje.java          # Modelo de mensaje
├── build.xml
├── manifest.mf
└── README.md
```

## Características

- **Login y Registro**: Interfaz gráfica para autenticación de usuarios
- **Chat en Tiempo Real**: Interfaz completa de chat con:
  - Área de mensajes con scroll automático
  - Lista de usuarios conectados
  - Campo de texto para escribir mensajes
  - Envío de mensajes con Enter o botón
- **Comunicación con Servidor**: 
  - Conexión TCP/IP con el servidor
  - Recepción de mensajes en tiempo real mediante hilo separado
  - Manejo de errores de conexión

## Componentes

### Backend (sin modificar)
- `Cliente.java`: Singleton para manejar la conexión con el servidor
- `Peticion.java`: Clase serializable para enviar/recibir datos
- `Usuario.java`: Modelo de usuario

### Frontend (GUI)
- `LoginUI.java`: Ventana de login y registro
- `ChatUI.java`: Ventana principal del chat con todas las funcionalidades
- `Mensaje.java`: Modelo de mensaje para el chat

## Cómo Ejecutar

1. Asegúrate de que el servidor esté corriendo en `localhost:5000`
2. Compila el proyecto:
   ```bash
   javac -d build/classes -sourcepath src src/chatcliente/*.java src/common/*.java src/models/*.java
   ```
3. Ejecuta la aplicación:
   ```bash
   java -cp build/classes chatcliente.ChatCliente
   ```

O desde NetBeans:
- Abre el proyecto en NetBeans
- Ejecuta el proyecto (F6)

## Funcionalidades del Chat

- **Enviar Mensajes**: Escribe en el campo inferior y presiona Enter o clic en "Enviar"
- **Ver Usuarios**: La lista de la izquierda muestra los usuarios conectados
- **Mensajes en Tiempo Real**: Los mensajes recibidos aparecen automáticamente
- **Cerrar Sesión**: Cierra la ventana para desconectarte del servidor

## Notas

- El backend original (`Cliente.java`, `Peticion.java`, `Usuario.java`) no ha sido modificado
- La integración se realizó agregando la GUI (`ChatUI.java`) y conectándola con el backend existente
- El proyecto mantiene la misma estructura de paquetes que el original

